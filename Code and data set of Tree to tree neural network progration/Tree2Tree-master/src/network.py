import random

import numpy as np
from six.moves import xrange
import time

import torch
import torch.nn as nn
import torch.optim as optim
from torch import cuda
from torch.autograd import Variable
from torch.nn.utils import clip_grad_norm
import torch.nn.functional as F
from Tree import *

import data_utils

def repackage_state(h):
    """Wraps hidden states in new Variables, to detach them from their history."""
    if type(h) == Variable:
        return Variable(h.data)
    else:
        return tuple(repackage_state(v) for v in h)

class Seq2SeqModel(nn.Module):
  def __init__(self,
               source_vocab_size,
               target_vocab_size,
               source_vocab,
               target_vocab,
               max_source_len,
               max_target_len,
               max_depth,
               embedding_size,
               hidden_size,
               num_layers,
               max_gradient_norm,
               batch_size,
               learning_rate,
               dropout_rate
              ):
    super(Seq2SeqModel, self).__init__()
    self.source_vocab_size = source_vocab_size
    self.target_vocab_size = target_vocab_size
    self.source_vocab = source_vocab
    self.target_vocab = target_vocab
    self.max_source_len = max_source_len
    self.max_target_len = max_target_len
    self.max_depth = max_depth
    self.embedding_size = embedding_size
    self.hidden_size = hidden_size
    self.batch_size = batch_size
    self.num_layers = num_layers
    self.max_gradient_norm = max_gradient_norm
    self.learning_rate = learning_rate
    self.dropout_rate = dropout_rate
    self.cuda_flag = cuda.is_available()

    if self.dropout_rate > 0:
      self.dropout = nn.Dropout(p=self.dropout_rate)

    self.encoder_embedding = nn.Embedding(self.source_vocab_size, self.embedding_size)
    self.encoder = nn.LSTM(input_size=self.embedding_size, hidden_size=self.hidden_size, num_layers=self.num_layers, batch_first=True, dropout=self.dropout_rate)

    self.decoder_embedding = nn.Embedding(self.target_vocab_size, self.embedding_size)
    self.decoder = nn.LSTM(input_size=self.embedding_size, hidden_size=self.hidden_size, num_layers=self.num_layers, batch_first=True, dropout=self.dropout_rate)

    self.attention_encoder_linear = nn.Linear(self.hidden_size, self.hidden_size)
    self.attention_decoder_linear = nn.Linear(self.hidden_size, self.hidden_size)
    self.attention_tanh = nn.Tanh()

    self.output_linear_layer = nn.Linear(self.hidden_size, self.target_vocab_size, bias=True)
    self.output_softmax_layer = nn.LogSoftmax()

    self.loss_function = nn.NLLLoss()
    self.optimizer = optim.RMSprop(self.parameters(), lr=self.learning_rate, alpha=0.95)

  def init_weights(self, param_init):
    for param in self.parameters():
      param.data.uniform_(-param_init, param_init)

  def decay_learning_rate(self, learning_rate_decay_factor):
    self.learning_rate *= learning_rate_decay_factor
    self.optimizer = optim.RMSprop(self.parameters(), lr=self.learning_rate, alpha=0.95)

  def train(self):
    if self.max_gradient_norm > 0:
      clip_grad_norm(self.parameters(), self.max_gradient_norm)
    self.optimizer.step()

  def attention(self, encoder_outputs, decoder_output):
    dotted = torch.bmm(encoder_outputs, decoder_output.unsqueeze(2))
    dotted = dotted.squeeze()
    if len(dotted.size()) == 1:
      dotted = dotted.unsqueeze(0)
    attention = nn.Softmax()(dotted)
    encoder_attention = torch.bmm(torch.transpose(encoder_outputs, 1, 2), attention.unsqueeze(2))
    encoder_attention = encoder_attention.squeeze()
    if len(encoder_attention.size()) == 1:
      encoder_attention = encoder_attention.unsqueeze(0)    
    res = self.attention_tanh(self.attention_encoder_linear(encoder_attention) + self.attention_decoder_linear(decoder_output))
    return res

  def encode(self, encoder_inputs):

    embedding = self.encoder_embedding(encoder_inputs)
    init_state = (Variable(embedding.data.new(self.num_layers, self.batch_size, self.hidden_size).zero_()),
      Variable(embedding.data.new(self.num_layers, self.batch_size, self.hidden_size).zero_()))

    encoder_outputs, encoder_state = self.encoder(embedding, init_state)

    return encoder_outputs, encoder_state

  def predict(self, decoder_output, encoder_outputs):
    output = self.attention(encoder_outputs, decoder_output)
    if self.dropout_rate > 0:
      output = self.dropout(output)
    output_linear = self.output_linear_layer(output)
    output_softmax = self.output_softmax_layer(output_linear)
    return output_softmax

  def decode(self, encoder_outputs, encoder_state, decoder_inputs, feed_previous, init_start_tokens=None):
    init_state = encoder_state
    predictions = []
    if init_start_tokens is not None:
      start_tokens = init_start_tokens
    else:
      start_tokens = self.decoder_embedding(decoder_inputs[:,0].unsqueeze(1))
    if feed_previous:
      states = []
      embedding = start_tokens
      state = init_state
      for time_step in xrange(1, self.max_target_len):
        state = repackage_state(state)
        output, state = self.decoder(embedding, state)
        states.append(state)
        output_squeeze = output.squeeze()
        if len(output_squeeze.size()) == 1:
          output_squeeze = output_squeeze.unsqueeze(0)
        prediction = self.predict(output_squeeze, encoder_outputs)
        decoder_input = prediction.max(1)[1]
        embedding = self.decoder_embedding(decoder_input.squeeze().unsqueeze(0))
        embedding = torch.transpose(embedding, 0, 1)
        predictions.append(prediction)
    else:
      states = []
      embedding = start_tokens
      state = init_state
      for time_step in xrange(1, self.max_target_len):
        state = repackage_state(state)
        output, state = self.decoder(embedding, state)
        states.append(state)
        output_squeeze = output.squeeze()
        if len(output_squeeze.size()) == 1:
          output_squeeze = output_squeeze.unsqueeze(0)
        prediction = self.predict(output_squeeze, encoder_outputs)
        decoder_input = decoder_inputs[:,time_step]
        embedding = self.decoder_embedding(decoder_input.squeeze().unsqueeze(0))
        embedding = torch.transpose(embedding, 0, 1)
        predictions.append(prediction)

    return predictions, states

  def forward(self, encoder_inputs, decoder_inputs, feed_previous=False):
    encoder_outputs, encoder_state = self.encode(encoder_inputs)
    predictions, _ = self.decode(encoder_outputs, encoder_state, decoder_inputs, feed_previous)
    return predictions

  def get_batch(self, data, start_idx):

    encoder_inputs, decoder_inputs = [], []

    for i in xrange(self.batch_size):
      if i + start_idx < len(data):
        encoder_input, decoder_input = data[i + start_idx]
      else:
        encoder_input, decoder_input = data[i + start_idx - len(data)]
     
      encoder_pad = [data_utils.PAD_ID] * (self.max_source_len - len(encoder_input))
      encoder_inputs.append(list(reversed(encoder_input + encoder_pad)))
      decoder_pad_size = self.max_target_len - len(decoder_input) - 1
      decoder_inputs.append([data_utils.GO_ID] + decoder_input +
                            [data_utils.PAD_ID] * decoder_pad_size)

    return np.array(encoder_inputs), np.array(decoder_inputs)

class Seq2TreeModel(Seq2SeqModel):

  def convert_node_to_tensor(self, node):
    pad_size = self.max_target_len - len(node['value']) - 1
    node['value'] = [data_utils.GO_ID] + node['value'] + [data_utils.PAD_ID] * pad_size
    node['value'] = Variable(torch.LongTensor(node['value']))
    if self.cuda_flag:
      node['value'] = node['value'].cuda()
    for idx in xrange(len(node['children'])):
      node['children'][idx] = self.convert_node_to_tensor(node['children'][idx])
    return node

  def build_data_node(self, current_node, idx_batch, depth, parent):
    node = {'value': [], 'idx_batch': idx_batch, 'depth': depth, 'parent': parent, 'children': []}
    node['value'].append(current_node['root'])
    for child in current_node['children']:
      node['value'].append(data_utils.NT_ID)
      node['children'].append(self.build_data_node(child, idx_batch, depth + 1, node))
    node['value'].append(data_utils.EOS_ID)
    return node

  def build_prediction_node(self, current_prediction, start_token, target_node, decoder_input, idx_batch, depth, parent):
    node = {'prediction': current_prediction, 'start_token': start_token, 'target_node': target_node, 'value': decoder_input, 'idx_batch': idx_batch, 'depth': depth, 'parent': parent, 'children': []}
    return node

  def forward(self, encoder_inputs, init_decoder_inputs, feed_previous=False):

    prediction_nodes = []

    init_encoder_outputs, encoder_state = self.encode(encoder_inputs)
    queue = []
    final_predictions = []

    default_start_token = Variable(torch.LongTensor([data_utils.GO_ID]))
    if self.cuda_flag:
      default_start_token = default_start_token.cuda()
    default_start_token = self.decoder_embedding(default_start_token.unsqueeze(0)).squeeze().unsqueeze(0)

    for idx in xrange(len(init_decoder_inputs)):
      current_target = init_decoder_inputs[idx]
      current_node = self.build_prediction_node([], default_start_token, current_target, current_target['value'], current_target['idx_batch'], current_target['depth'], None)
      prediction_nodes.append(current_node)
      queue.append(((encoder_state[0][:,idx,:].unsqueeze(1), encoder_state[1][:,idx,:].unsqueeze(1)), idx))
      final_predictions.append(idx)
    predictions_per_batch = []

    head = 0
    while head < len(queue):
      init_h_states = []
      init_c_states = []
      decoder_inputs_node_idx = []
      while head < len(queue) and len(init_h_states) < self.batch_size:
        if prediction_nodes[queue[head][1]]['depth'] <= self.max_depth:
          init_h_states.append(queue[head][0][0])
          init_c_states.append(queue[head][0][1])
          decoder_inputs_node_idx.append(queue[head][1])
        head += 1
      if len(decoder_inputs_node_idx) == 0:
        break
      init_h_states = torch.cat(init_h_states, 1)
      init_c_states = torch.cat(init_c_states, 1)
      decoder_inputs = []
      encoder_outputs = []
      start_tokens = []
      for node_idx in decoder_inputs_node_idx:
        decoder_inputs.append(prediction_nodes[node_idx]['value'].unsqueeze(0))
        encoder_outputs.append(init_encoder_outputs[prediction_nodes[node_idx]['idx_batch']].unsqueeze(0))
        start_tokens.append(prediction_nodes[node_idx]['start_token'].unsqueeze(0))
      decoder_inputs = torch.cat(decoder_inputs, 0)
      encoder_outputs = torch.cat(encoder_outputs, 0)
      start_tokens = torch.cat(start_tokens, 0)
      predictions_logits, states = self.decode(encoder_outputs, (init_h_states, init_c_states), decoder_inputs, feed_previous, start_tokens)
      predictions_per_batch.append((predictions_logits, decoder_inputs))

      predictions = []
      for time_step in xrange(self.max_target_len - 1):
        predictions.append(predictions_logits[time_step].max(1)[1])

      for idx_node in xrange(len(decoder_inputs_node_idx)):
          num_node = decoder_inputs_node_idx[idx_node]
          current_target = prediction_nodes[num_node]['target_node']
          current_predictions = []
          for time_step in xrange(self.max_target_len - 1):
            current_predictions.append(predictions[time_step][idx_node])
          current_predictions = torch.cat(current_predictions, 0)
          prediction_nodes[num_node]['prediction'] = current_predictions
          if feed_previous == False:
            child_idx = 0
            for idx in xrange(self.max_target_len - 1):
              if prediction_nodes[num_node]['value'][idx + 1].data[0] == data_utils.NT_ID:
                new_target = current_target['children'][child_idx]
                new_node = self.build_prediction_node([], states[idx][0][self.num_layers - 1:,idx_node,:], new_target, new_target['value'], new_target['idx_batch'], new_target['depth'], num_node)
                prediction_nodes.append(new_node)
                prediction_nodes[num_node]['children'].append(len(prediction_nodes) - 1)
                queue.append(((states[idx][0][:,idx_node,:].unsqueeze(1), states[idx][1][:,idx_node,:].unsqueeze(1)), len(prediction_nodes) - 1))
                child_idx += 1
          else:
            child_idx = 0
            for idx in xrange(self.max_target_len - 1):
              if prediction_nodes[num_node]['prediction'][idx].data[0] == data_utils.EOS_ID:
                break
              if prediction_nodes[num_node]['prediction'][idx].data[0] == data_utils.NT_ID:
                if current_target == None or child_idx >= len(current_target['children']):
                  default_value = [data_utils.GO_ID, data_utils.EOS_ID] + [data_utils.PAD_ID] * (self.max_target_len - 2)
                  default_value = Variable(torch.LongTensor(default_value))
                  if self.cuda_flag:
                    default_value = default_value.cuda()
                  new_node = self.build_prediction_node([], states[idx][0][self.num_layers - 1:,idx_node,:], None, default_value, prediction_nodes[num_node]['idx_batch'], prediction_nodes[num_node]['depth'] + 1, num_node)
                else:
                  new_target = current_target['children'][child_idx]
                  new_node = self.build_prediction_node([], states[idx][0][self.num_layers - 1:,idx_node,:], new_target, new_target['value'], new_target['idx_batch'], new_target['depth'], num_node)
                prediction_nodes.append(new_node)
                prediction_nodes[num_node]['children'].append(len(prediction_nodes) - 1)
                if current_target and child_idx < len(current_target['children']) or new_node['depth'] <= 10:
                  queue.append(((states[idx][0][:,idx_node,:].unsqueeze(1), states[idx][1][:,idx_node,:].unsqueeze(1)), len(prediction_nodes) - 1))
                child_idx += 1

    return predictions_per_batch, prediction_nodes, final_predictions

  def tree2seq(self, prediction_nodes, node_idx):
    prediction = [data_utils.LEFT_BRACKET_ID]
    child_idx = 0
    node = prediction_nodes[node_idx]
    current_words = node['prediction']
    current_children = node['children']
    for item in current_words[:]:
      if item.data[0] == data_utils.EOS_ID:
        break
      elif item.data[0] != data_utils.NT_ID:
        prediction.append(item.data[0])
      else:
        if child_idx >= len(current_children):
          prediction = prediction + [data_utils.LEFT_BRACKET_ID, data_utils.RIGHT_BRACKET_ID]
        else:
          prediction = prediction + self.tree2seq(prediction_nodes, current_children[child_idx])
          child_idx += 1
    prediction.append(data_utils.RIGHT_BRACKET_ID)
    return prediction

  def get_batch(self, data, start_idx):

    encoder_inputs, decoder_inputs = [], []

    for i in xrange(self.batch_size):
      if i + start_idx < len(data):
        encoder_input, decoder_input = data[i + start_idx]
      else:
        encoder_input, decoder_input = data[i + start_idx - len(data)]

      encoder_pad = [data_utils.PAD_ID] * (self.max_source_len - len(encoder_input))
      encoder_inputs.append(list(reversed(encoder_input + encoder_pad)))
      decoder_inputs.append(self.build_data_node(decoder_input, i, 0, None))

    return np.array(encoder_inputs), decoder_inputs

class TreeEncoder(nn.Module):
  def __init__(self,
               source_vocab_size,
               embedding_size,
               hidden_size,
               batch_size
               ):
    super(TreeEncoder, self).__init__()
    self.source_vocab_size = source_vocab_size
    self.embedding_size = embedding_size
    self.hidden_size = hidden_size
    self.batch_size = batch_size
    self.cuda_flag = cuda.is_available()

    self.encoder_embedding = nn.Embedding(self.source_vocab_size, self.embedding_size)

    self.ix = nn.Linear(self.hidden_size, self.embedding_size, bias=True)
    self.ilh = nn.Linear(self.hidden_size, self.hidden_size)
    self.irh = nn.Linear(self.hidden_size, self.hidden_size)

    self.fx = nn.Linear(self.hidden_size, self.embedding_size, bias=True)
    self.flh = nn.Linear(self.hidden_size, self.hidden_size)
    self.frh = nn.Linear(self.hidden_size, self.hidden_size)

    self.ox = nn.Linear(self.hidden_size, self.embedding_size, bias=True)
    self.olh = nn.Linear(self.hidden_size, self.hidden_size)
    self.orh = nn.Linear(self.hidden_size, self.hidden_size)

    self.ux = nn.Linear(self.hidden_size, self.embedding_size, bias=True)
    self.ulh = nn.Linear(self.hidden_size, self.hidden_size)
    self.urh = nn.Linear(self.hidden_size, self.hidden_size)

  def calc_root(self, inputs, child_h, child_c):
    i = F.sigmoid(self.ix(inputs) + self.ilh(child_h[:, 0]) + self.irh(child_h[:, 1]))
    o = F.sigmoid(self.ox(inputs) + self.olh(child_h[:, 0]) + self.orh(child_h[:, 1]))
    u = F.tanh(self.ux(inputs) + self.ulh(child_h[:, 0]) + self.urh(child_h[:, 1]))

    fx = self.fx(inputs)
    fx = torch.stack([fx, fx], dim=1)
    fl = self.flh(child_h[:, 0])
    fr = self.frh(child_h[:, 1])
    f = torch.stack([fl, fr], dim=1)
    f = f + fx
    f = F.sigmoid(f)
    fc = F.torch.mul(f,child_c)
    c = F.torch.mul(i,u) + F.torch.sum(fc,1)
    h = F.torch.mul(o, F.tanh(c))
    return h, c

  def encode(self, encoder_inputs, children_h, children_c):
    embedding = self.encoder_embedding(encoder_inputs)
    embedding = embedding.squeeze()
    if len(embedding.size()) == 1:
      embedding = embedding.unsqueeze(0)
    encoder_outputs = self.calc_root(embedding, children_h, children_c)
    return encoder_outputs

  def forward(self, encoder_managers):
    queue = []
    head = 0
    max_num_trees = 0
    visited_idx = []

    for encoder_manager_idx in range(len(encoder_managers)):
      encoder_manager = encoder_managers[encoder_manager_idx]
      max_num_trees = max(max_num_trees, encoder_manager.num_trees)
      idx = encoder_manager.num_trees - 1
      while idx >= 0:
        current_tree = encoder_manager.get_tree(idx)
        canVisited = True
        if current_tree.lchild is not None:
          ltree = encoder_manager.get_tree(current_tree.lchild)
          if ltree.state is None:
            canVisited = False
        if current_tree.rchild is not None:
          rtree = encoder_manager.get_tree(current_tree.rchild)
          if rtree.state is None:
            canVisited = False
        if canVisited:
          root = current_tree.root
          if current_tree.lchild is None:
            children_c = Variable(torch.zeros(self.hidden_size))
            children_h = Variable(torch.zeros(self.hidden_size))
            if self.cuda_flag:
              children_c = children_c.cuda()
              children_h = children_h.cuda()
          else:
            children_h, children_c = ltree.state
            children_h = children_h
            children_c = children_c
          if current_tree.rchild is None:
            rchild_c = Variable(torch.zeros(self.hidden_size))
            rchild_h = Variable(torch.zeros(self.hidden_size))
            if self.cuda_flag:
              rchild_c = rchild_c.cuda()
              rchild_h = rchild_h.cuda()
            children_c = torch.stack([children_c, rchild_c], dim=0)
            children_h = torch.stack([children_h, rchild_h], dim=0)
          else:
            rchild_h, rchild_c = rtree.state
            rchild_h = rchild_h
            rchild_c = rchild_c
            children_c = torch.stack([children_c, rchild_c], dim=0)
            children_h = torch.stack([children_h, rchild_h], dim=0)
          queue.append((encoder_manager_idx, idx, root, children_h, children_c))
        else:
          break
        idx -= 1
      visited_idx.append(idx)

    while head < len(queue):
      encoder_inputs = []
      children_h = []
      children_c = []
      tree_idxes = []
      while head < len(queue):
        encoder_manager_idx, idx, root, child_h, child_c = queue[head]
        current_tree = encoder_managers[encoder_manager_idx].get_tree(idx)
        tree_idxes.append((encoder_manager_idx, idx))
        encoder_inputs.append(root)
        children_h.append(child_h)
        children_c.append(child_c)
        head += 1
      encoder_inputs = torch.stack(encoder_inputs, dim=0)
      children_h = torch.stack(children_h, dim=0)
      children_c = torch.stack(children_c, dim=0)
      if self.cuda_flag:
        encoder_inputs = encoder_inputs.cuda()
      encoder_outputs = self.encode(encoder_inputs, children_h, children_c)
      for i in range(len(tree_idxes)):
        current_encoder_manager_idx, current_idx = tree_idxes[i]
        child_h = encoder_outputs[0][i]
        child_c = encoder_outputs[1][i]
        encoder_managers[current_encoder_manager_idx].trees[current_idx].state = child_h, child_c

        current_tree = encoder_managers[current_encoder_manager_idx].get_tree(current_idx)

        if current_tree.parent == visited_idx[current_encoder_manager_idx]:
          encoder_manager_idx = current_encoder_manager_idx
          encoder_manager = encoder_managers[encoder_manager_idx]
          idx = visited_idx[encoder_manager_idx]

          while idx >= 0:
            current_tree = encoder_manager.get_tree(idx)
            canVisited = True
            if current_tree.lchild is not None:
              ltree = encoder_manager.get_tree(current_tree.lchild)
              if ltree.state is None:
                canVisited = False
            if current_tree.rchild is not None:
              rtree = encoder_manager.get_tree(current_tree.rchild)
              if rtree.state is None:
                canVisited = False

            if canVisited:
              root = current_tree.root
              if current_tree.lchild is None:
                children_c = Variable(torch.zeros(self.hidden_size))
                children_h = Variable(torch.zeros(self.hidden_size))
                if self.cuda_flag:
                  children_c = children_c.cuda()
                  children_h = children_h.cuda()
              else:
                children_h, children_c = ltree.state
                children_h = children_h
                children_c = children_c

              if current_tree.rchild is None:
                rchild_c = Variable(torch.zeros(self.hidden_size))
                rchild_h = Variable(torch.zeros(self.hidden_size))
                if self.cuda_flag:
                  rchild_c = rchild_c.cuda()
                  rchild_h = rchild_h.cuda()
              else:
                rchild_h, rchild_c = rtree.state
                rchild_h = rchild_h
                rchild_c = rchild_c

              children_c = torch.stack([children_c, rchild_c], dim=0)
              children_h = torch.stack([children_h, rchild_h], dim=0)
              queue.append((encoder_manager_idx, idx, root, children_h, children_c))
            else:
              break
            idx -= 1
          visited_idx[encoder_manager_idx] = idx

    PAD_state_token = Variable(torch.zeros(self.hidden_size))
    if self.cuda_flag:
      PAD_state_token = PAD_state_token.cuda()

    encoder_h_state = []
    encoder_c_state = []
    init_encoder_outputs = []
    init_attention_masks = []
    for encoder_manager in encoder_managers:
      root = encoder_manager.get_tree(0)
      h, c = root.state
      encoder_h_state.append(h)
      encoder_c_state.append(c)
      init_encoder_output = []
      for tree in encoder_manager.trees:
        init_encoder_output.append(tree.state[0])
      attention_mask = [0] * len(init_encoder_output)
      current_len = len(init_encoder_output)
      if current_len < max_num_trees:
        init_encoder_output = init_encoder_output + [PAD_state_token] * (max_num_trees - current_len)
        attention_mask = attention_mask + [1] * (max_num_trees - current_len)
      attention_mask = Variable(torch.ByteTensor(attention_mask))
      if self.cuda_flag:
        attention_mask = attention_mask.cuda()
      init_attention_masks.append(attention_mask)
      init_encoder_output = torch.stack(init_encoder_output, dim=0)
      init_encoder_outputs.append(init_encoder_output)

    init_encoder_outputs = torch.stack(init_encoder_outputs, dim=0)
    init_attention_masks = torch.stack(init_attention_masks, dim=0)

    return init_encoder_outputs, init_attention_masks, encoder_h_state, encoder_c_state

class Tree2SeqModel(nn.Module):
  def __init__(self,
               source_vocab_size,
               target_vocab_size,
               source_vocab,
               target_vocab,
               max_target_len,
               max_depth,
               embedding_size,
               hidden_size,
               num_layers,
               max_gradient_norm,
               batch_size,
               learning_rate,
               dropout_rate
              ):
    super(Tree2SeqModel, self).__init__()
    self.source_vocab_size = source_vocab_size
    self.target_vocab_size = target_vocab_size
    self.source_vocab = source_vocab
    self.target_vocab = target_vocab
    self.max_target_len = max_target_len
    self.max_depth = max_depth
    self.embedding_size = embedding_size
    self.hidden_size = hidden_size
    self.batch_size = batch_size
    self.num_layers = num_layers
    self.max_gradient_norm = max_gradient_norm
    self.learning_rate = learning_rate
    self.dropout_rate = dropout_rate
    self.cuda_flag = cuda.is_available()

    if self.dropout_rate > 0:
      self.dropout = nn.Dropout(p=self.dropout_rate)


    self.encoder = TreeEncoder(self.source_vocab_size, self.embedding_size, self.hidden_size, self.batch_size)

    self.decoder_embedding = nn.Embedding(self.target_vocab_size, self.embedding_size)
    self.decoder = nn.LSTM(input_size=self.embedding_size, hidden_size=self.hidden_size, num_layers=self.num_layers, batch_first=True, dropout=self.dropout_rate)

    self.attention_encoder_linear = nn.Linear(self.hidden_size, self.hidden_size)
    self.attention_decoder_linear = nn.Linear(self.hidden_size, self.hidden_size)
    self.attention_tanh = nn.Tanh()

    self.output_linear_layer = nn.Linear(self.hidden_size, self.target_vocab_size, bias=True)
    self.output_softmax_layer = nn.LogSoftmax()

    self.loss_function = nn.NLLLoss()
    self.optimizer = optim.Adam(self.parameters(), lr=self.learning_rate)

  def init_weights(self, param_init):
    for param in self.parameters():
      param.data.uniform_(-param_init, param_init)

  def decay_learning_rate(self, learning_rate_decay_factor):
    self.learning_rate *= learning_rate_decay_factor
    self.optimizer = optim.Adam(self.parameters(), lr=self.learning_rate)

  def train(self):
    if self.max_gradient_norm > 0:
      clip_grad_norm(self.parameters(), self.max_gradient_norm)
    self.optimizer.step()

  def attention(self, encoder_outputs, attention_masks, decoder_output):
    dotted = torch.bmm(encoder_outputs, decoder_output.unsqueeze(2))
    dotted = dotted.squeeze()
    if len(dotted.size()) == 1:
      dotted = dotted.unsqueeze(0)
    dotted.data.masked_fill_(attention_masks.data, -float('inf'))
    attention = nn.Softmax()(dotted)
    encoder_attention = torch.bmm(torch.transpose(encoder_outputs, 1, 2), attention.unsqueeze(2))
    encoder_attention = encoder_attention.squeeze()
    if len(encoder_attention.size()) == 1:
      encoder_attention = encoder_attention.unsqueeze(0)    
    res = self.attention_tanh(self.attention_encoder_linear(encoder_attention) + self.attention_decoder_linear(decoder_output))
    return res

  def predict(self, decoder_output, encoder_outputs, attention_masks):
    attention_output = self.attention(encoder_outputs, attention_masks, decoder_output)
    if self.dropout_rate > 0:
      output = self.dropout(attention_output)
    else:
      output = attention_output
    output_linear = self.output_linear_layer(output)
    output_softmax = self.output_softmax_layer(output_linear)
    return output_softmax, attention_output

  def decode(self, encoder_outputs, attention_masks, encoder_state, decoder_inputs, feed_previous):
    init_state = encoder_state
    predictions = []
    start_tokens = self.decoder_embedding(decoder_inputs[:,0].unsqueeze(1))
    attention_output = Variable(torch.zeros(decoder_inputs.size()[0], self.hidden_size))
    if self.cuda_flag:
      attention_output = attention_output.cuda()
    if feed_previous:
      states = []
      embedding = start_tokens
      state = init_state
      for time_step in xrange(1, self.max_target_len):
        state = repackage_state(state)
        attention_output = attention_output.unsqueeze(1)
        output, state = self.decoder(embedding, state)
        states.append(state)
        output_squeeze = output.squeeze()
        if len(output_squeeze.size()) == 1:
          output_squeeze = output_squeeze.unsqueeze(0)
        prediction, attention_output = self.predict(output_squeeze, encoder_outputs, attention_masks)
        decoder_input = prediction.max(1)[1]
        embedding = self.decoder_embedding(decoder_input.squeeze().unsqueeze(0))
        embedding = torch.transpose(embedding, 0, 1)
        predictions.append(prediction)
    else:
      states = []
      embedding = start_tokens
      state = init_state
      for time_step in xrange(1, self.max_target_len):
        state = repackage_state(state)
        attention_output = attention_output.unsqueeze(1)
        output, state = self.decoder(embedding, state)
        states.append(state)
        output_squeeze = output.squeeze()
        if len(output_squeeze.size()) == 1:
          output_squeeze = output_squeeze.unsqueeze(0)
        prediction, attention_output = self.predict(output_squeeze, encoder_outputs, attention_masks)
        decoder_input = decoder_inputs[:,time_step]
        embedding = self.decoder_embedding(decoder_input.squeeze().unsqueeze(0))
        embedding = torch.transpose(embedding, 0, 1)
        predictions.append(prediction)

    return predictions, states

  def forward(self, encoder_managers, decoder_inputs, feed_previous=False):

    init_encoder_outputs, init_attention_masks, encoder_h_state, encoder_c_state = self.encoder(encoder_managers)
    for i in range(len(encoder_h_state)):
      encoder_h_state[i] = encoder_h_state[i].unsqueeze(0).unsqueeze(0)
      encoder_c_state[i] = encoder_c_state[i].unsqueeze(0).unsqueeze(0)
    encoder_h_state = torch.cat(encoder_h_state, 1)
    encoder_c_state = torch.cat(encoder_c_state, 1)
    predictions, _ = self.decode(init_encoder_outputs, init_attention_masks, (encoder_h_state, encoder_c_state), decoder_inputs, feed_previous)
    for idx in range(len(encoder_managers)):
      encoder_managers[idx].clear_states()
    return predictions

  def get_batch(self, data, start_idx):

    encoder_managers, decoder_inputs = [], []

    for i in xrange(self.batch_size):
      if i + start_idx < len(data):
        _, _, encoder_manager, decoder_input = data[i + start_idx]
      else:
        _, _, encoder_manager, decoder_input = data[i + start_idx - len(data)]

      encoder_managers.append(encoder_manager)
      decoder_pad_size = self.max_target_len - len(decoder_input) - 1
      decoder_inputs.append([data_utils.GO_ID] + decoder_input +
                            [data_utils.PAD_ID] * decoder_pad_size)

    return encoder_managers, np.array(decoder_inputs)

class Tree2TreeModel(nn.Module):
  def __init__(self,
               source_vocab_size,
               target_vocab_size,
               source_vocab,
               target_vocab,
               max_depth,
               embedding_size,
               hidden_size,
               num_layers,
               max_gradient_norm,
               batch_size,
               learning_rate,
               dropout_rate,
               no_pf,
               no_attention
              ):
    super(Tree2TreeModel, self).__init__()
    self.source_vocab_size = source_vocab_size
    self.target_vocab_size = target_vocab_size
    self.source_vocab = source_vocab
    self.target_vocab = target_vocab
    self.max_depth = max_depth
    self.embedding_size = embedding_size
    self.hidden_size = hidden_size
    self.batch_size = batch_size
    self.num_layers = num_layers
    self.max_gradient_norm = max_gradient_norm
    self.learning_rate = learning_rate
    self.dropout_rate = dropout_rate
    self.no_pf = no_pf
    self.no_attention = no_attention
    self.cuda_flag = cuda.is_available()

    if self.dropout_rate > 0:
      self.dropout = nn.Dropout(p=self.dropout_rate)

    self.encoder = TreeEncoder(self.source_vocab_size, self.embedding_size, self.hidden_size, self.batch_size)

    self.decoder_embedding = nn.Embedding(self.target_vocab_size, self.embedding_size)

    if self.no_pf:
      self.decoder_l = nn.LSTM(input_size=self.embedding_size, hidden_size=self.hidden_size, num_layers=self.num_layers, batch_first=True, dropout=self.dropout_rate)
      self.decoder_r = nn.LSTM(input_size=self.embedding_size, hidden_size=self.hidden_size, num_layers=self.num_layers, batch_first=True, dropout=self.dropout_rate)
    else:
      self.decoder_l = nn.LSTM(input_size=self.embedding_size + self.hidden_size, hidden_size=self.hidden_size, num_layers=self.num_layers, batch_first=True, dropout=self.dropout_rate)
      self.decoder_r = nn.LSTM(input_size=self.embedding_size + self.hidden_size, hidden_size=self.hidden_size, num_layers=self.num_layers, batch_first=True, dropout=self.dropout_rate)

    self.attention_linear = nn.Linear(self.hidden_size * 2, self.hidden_size, bias=True)
    self.attention_tanh = nn.Tanh()

    self.output_linear_layer = nn.Linear(self.hidden_size, self.target_vocab_size, bias=True)

    self.loss_function = nn.CrossEntropyLoss(size_average=False)
    self.optimizer = optim.Adam(self.parameters(), lr=self.learning_rate)

  def init_weights(self, param_init):
    for param in self.parameters():
      param.data.uniform_(-param_init, param_init)

  def decay_learning_rate(self, learning_rate_decay_factor):
    self.learning_rate *= learning_rate_decay_factor
    self.optimizer = optim.Adam(self.parameters(), lr=self.learning_rate)

  def train(self):
    if self.max_gradient_norm > 0:
      clip_grad_norm(self.parameters(), self.max_gradient_norm)
    self.optimizer.step()

  def attention(self, encoder_outputs, attention_masks, decoder_output):
    dotted = torch.bmm(encoder_outputs, decoder_output.unsqueeze(2))
    dotted = dotted.squeeze()
    if len(dotted.size()) == 1:
      dotted = dotted.unsqueeze(0)
    dotted.data.masked_fill_(attention_masks.data, -float('inf'))
    attention = nn.Softmax()(dotted)
    encoder_attention = torch.bmm(torch.transpose(encoder_outputs, 1, 2), attention.unsqueeze(2))
    encoder_attention = encoder_attention.squeeze()
    if len(encoder_attention.size()) == 1:
      encoder_attention = encoder_attention.unsqueeze(0)    
    res = self.attention_tanh(self.attention_linear(torch.cat([decoder_output, encoder_attention], 1)))
    return res

  def tree2seq(self, prediction_manager, current_idx):
    current_tree = prediction_manager.get_tree(current_idx)
    if current_tree.prediction == data_utils.EOS_ID:
      return []
    prediction = [data_utils.LEFT_BRACKET_ID]
    prediction.append(current_tree.prediction)
    if current_tree.lchild is not None:
      prediction = prediction + self.tree2seq(prediction_manager, current_tree.lchild)
    prediction.append(data_utils.RIGHT_BRACKET_ID)
    if current_tree.rchild is not None:
      prediction = prediction + self.tree2seq(prediction_manager, current_tree.rchild)
    return prediction

  def predict(self, decoder_output, encoder_outputs, attention_masks):
    if self.no_attention:
      output = decoder_output
      attention_output = decoder_output
    else:
      attention_output = self.attention(encoder_outputs, attention_masks, decoder_output)
      if self.dropout_rate > 0:
        output = self.dropout(attention_output)
      else:
        output = attention_output
    output_linear = self.output_linear_layer(output)
    return output_linear, attention_output

  def decode(self, encoder_outputs, attention_masks, init_state, init_decoder_inputs, attention_inputs):
      embedding = self.decoder_embedding(init_decoder_inputs)
      state_l = repackage_state(init_state)
      state_r = repackage_state(init_state)
      if self.no_pf:
        decoder_inputs = embedding
      else:
        decoder_inputs = torch.cat([embedding, attention_inputs], 2)
      output_l, state_l = self.decoder_l(decoder_inputs, state_l)
      output_r, state_r = self.decoder_r(decoder_inputs, state_r)
      output_l = output_l.squeeze()
      if len(output_l.size()) == 1:
        output_l = output_l.unsqueeze(0)
      output_r = output_r.squeeze()
      if len(output_r.size()) == 1:
        output_r = output_r.unsqueeze(0)
      prediction_l, attention_output_l = self.predict(output_l, encoder_outputs, attention_masks)
      prediction_r, attention_output_r = self.predict(output_r, encoder_outputs, attention_masks)
      return prediction_l, prediction_r, state_l, state_r, attention_output_l, attention_output_r

  def forward(self, encoder_managers, decoder_managers, feed_previous=False):

    init_encoder_outputs, init_attention_masks, encoder_h_state, encoder_c_state = self.encoder(encoder_managers)

    queue = []

    prediction_managers = []
    for idx in range(len(decoder_managers)):
      prediction_managers.append(TreeManager())

    for idx in xrange(len(decoder_managers)):
      current_target_manager_idx = idx
      current_target_idx = 0
      current_prediction_idx = prediction_managers[idx].create_binary_tree(data_utils.GO_ID, None, 0)
      prediction_managers[idx].trees[current_prediction_idx].state = encoder_h_state[idx].unsqueeze(0), encoder_c_state[idx].unsqueeze(0)
      prediction_managers[idx].trees[current_prediction_idx].target = 0
      queue.append((idx, current_prediction_idx))

    head = 0
    predictions_per_batch = []
    EOS_token = Variable(torch.LongTensor([data_utils.EOS_ID]))

    while head < len(queue):
      init_h_states = []
      init_c_states = []
      decoder_inputs = []
      attention_inputs = []
      encoder_outputs = []
      attention_masks = []
      target_seqs_l = []
      target_seqs_r = []
      tree_idxes = []
      while head < len(queue):
        current_tree = prediction_managers[queue[head][0]].get_tree(queue[head][1])
        target_manager_idx = queue[head][0]
        target_idx = current_tree.target
        if target_idx is not None:
          target_tree = decoder_managers[target_manager_idx].get_tree(target_idx)
        else:
          target_tree = None
        if target_tree is not None:
          init_h_state = current_tree.state[0]
          init_c_state = current_tree.state[1]
          init_h_state = torch.cat([init_h_state] * self.num_layers, dim=0)
          init_c_state = torch.cat([init_c_state] * self.num_layers, dim=0)
          init_h_states.append(init_h_state)
          init_c_states.append(init_c_state)
          tree_idxes.append((queue[head][0], queue[head][1]))
          decoder_input = current_tree.root
          decoder_inputs.append(decoder_input)
          if current_tree.attention is None:
            attention_input = Variable(torch.zeros(self.hidden_size))
            if self.cuda_flag:
              attention_input = attention_input.cuda()
          else:
            attention_input = current_tree.attention
          attention_inputs.append(attention_input)
          if queue[head][1] == 0:
            target_seq_l = target_tree.root
            target_seq_r = EOS_token
          else:
            if target_tree is not None and target_tree.lchild is not None:
              target_seq_l = decoder_managers[target_manager_idx].trees[target_tree.lchild].root
            else:
              target_seq_l = EOS_token
            if target_tree is not None and target_tree.rchild is not None:
              target_seq_r = decoder_managers[target_manager_idx].trees[target_tree.rchild].root
            else:
              target_seq_r = EOS_token
          target_seqs_l.append(target_seq_l)
          target_seqs_r.append(target_seq_r)
          encoder_outputs.append(init_encoder_outputs[queue[head][0]])
          attention_masks.append(init_attention_masks[queue[head][0]])
        head += 1
      if len(tree_idxes) == 0:
        break
      init_h_states = torch.stack(init_h_states, dim=1)
      init_c_states = torch.stack(init_c_states, dim=1)
      decoder_inputs = torch.stack(decoder_inputs, dim=0)
      attention_inputs = torch.stack(attention_inputs, dim=0).unsqueeze(1)
      target_seqs_l = torch.cat(target_seqs_l, 0)
      target_seqs_r = torch.cat(target_seqs_r, 0)
      if self.cuda_flag:
        decoder_inputs = decoder_inputs.cuda()
        target_seqs_l = target_seqs_l.cuda()
        target_seqs_r = target_seqs_r.cuda()
      encoder_outputs = torch.stack(encoder_outputs, dim=0)
      attention_masks = torch.stack(attention_masks, dim=0)

      predictions_logits_l, predictions_logits_r, states_l, states_r, attention_outputs_l, attention_outputs_r = self.decode(encoder_outputs, attention_masks, (init_h_states, init_c_states), decoder_inputs, attention_inputs)
      predictions_per_batch.append((predictions_logits_l, target_seqs_l))
      predictions_per_batch.append((predictions_logits_r, target_seqs_r))

      if feed_previous:
        predictions_l = predictions_logits_l.max(1)[1]
        predictions_r = predictions_logits_r.max(1)[1]

      for i in xrange(len(tree_idxes)):
          current_prediction_manager_idx, current_prediction_idx = tree_idxes[i]
          target_manager_idx = current_prediction_manager_idx
          current_prediction_tree = prediction_managers[current_prediction_manager_idx].get_tree(current_prediction_idx)
          target_idx = current_prediction_tree.target
          if target_idx is None:
            target_tree = None
          else:
            target_tree = decoder_managers[target_manager_idx].get_tree(target_idx)
          if feed_previous == False:
            if current_prediction_idx == 0:
              nxt_l_prediction_idx = prediction_managers[current_prediction_manager_idx].create_binary_tree(target_tree.root, current_prediction_idx, current_prediction_tree.depth + 1)
              prediction_managers[current_prediction_manager_idx].trees[current_prediction_idx].lchild = nxt_l_prediction_idx
              prediction_managers[current_prediction_manager_idx].trees[nxt_l_prediction_idx].target = target_idx
              prediction_managers[current_prediction_manager_idx].trees[nxt_l_prediction_idx].state = states_l[0][:,i,:], states_l[1][:, i, :]
              prediction_managers[current_prediction_manager_idx].trees[nxt_l_prediction_idx].attention = attention_outputs_l[i]
              queue.append((current_prediction_manager_idx, nxt_l_prediction_idx))
            else:
              if target_tree.lchild is not None:
                nxt_l_prediction_idx = prediction_managers[current_prediction_manager_idx].create_binary_tree(\
                  decoder_managers[target_manager_idx].trees[target_tree.lchild].root, current_prediction_idx, current_prediction_tree.depth + 1)
                prediction_managers[current_prediction_manager_idx].trees[nxt_l_prediction_idx].target = target_tree.lchild
                prediction_managers[current_prediction_manager_idx].trees[current_prediction_idx].lchild = nxt_l_prediction_idx
                prediction_managers[current_prediction_manager_idx].trees[nxt_l_prediction_idx].state = states_l[0][:,i,:], states_l[1][:, i, :]
                prediction_managers[current_prediction_manager_idx].trees[nxt_l_prediction_idx].attention = attention_outputs_l[i]
                queue.append((current_prediction_manager_idx, nxt_l_prediction_idx))
              if target_idx == 0:
                continue
              if target_tree.rchild is not None:
                nxt_r_prediction_idx = prediction_managers[current_prediction_manager_idx].create_binary_tree(\
                  decoder_managers[target_manager_idx].trees[target_tree.rchild].root, current_prediction_idx, current_prediction_tree.depth + 1)
                prediction_managers[current_prediction_manager_idx].trees[nxt_r_prediction_idx].target = target_tree.rchild
                prediction_managers[current_prediction_manager_idx].trees[current_prediction_idx].rchild = nxt_r_prediction_idx
                prediction_managers[current_prediction_manager_idx].trees[nxt_r_prediction_idx].state = states_r[0][:,i,:], states_r[1][:, i, :]
                prediction_managers[current_prediction_manager_idx].trees[nxt_r_prediction_idx].attention = attention_outputs_r[i]
                queue.append((current_prediction_manager_idx, nxt_r_prediction_idx))
          else:
            if current_prediction_idx == 0:
              nxt_l_prediction_idx = prediction_managers[current_prediction_manager_idx].create_binary_tree(predictions_l[i].data[0], current_prediction_idx, current_prediction_tree.depth + 1)
              prediction_managers[current_prediction_manager_idx].trees[current_prediction_idx].lchild = nxt_l_prediction_idx
              prediction_managers[current_prediction_manager_idx].trees[nxt_l_prediction_idx].prediction = predictions_l[i].data[0]
              prediction_managers[current_prediction_manager_idx].trees[nxt_l_prediction_idx].target = target_idx
              prediction_managers[current_prediction_manager_idx].trees[nxt_l_prediction_idx].state = states_l[0][:,i,:], states_l[1][:, i, :]
              prediction_managers[current_prediction_manager_idx].trees[nxt_l_prediction_idx].attention = attention_outputs_l[i]
              queue.append((current_prediction_manager_idx, nxt_l_prediction_idx))
            else:
              if predictions_l[i].data[0] != data_utils.EOS_ID:
                if target_tree is None or target_tree.lchild is None:
                  nxt_l_prediction_idx = prediction_managers[current_prediction_manager_idx].create_binary_tree(predictions_l[i].data[0], current_prediction_idx, current_prediction_tree.depth + 1)
                  prediction_managers[current_prediction_manager_idx].trees[nxt_l_prediction_idx].target = None
                else:
                  nxt_l_prediction_idx = prediction_managers[current_prediction_manager_idx].create_binary_tree(predictions_l[i].data[0], current_prediction_idx, current_prediction_tree.depth + 1)
                  prediction_managers[current_prediction_manager_idx].trees[nxt_l_prediction_idx].target = target_tree.lchild
                prediction_managers[current_prediction_manager_idx].trees[current_prediction_idx].lchild = nxt_l_prediction_idx
                prediction_managers[current_prediction_manager_idx].trees[nxt_l_prediction_idx].prediction = predictions_l[i].data[0]
                prediction_managers[current_prediction_manager_idx].trees[nxt_l_prediction_idx].state = states_l[0][:,i,:], states_l[1][:, i, :]
                prediction_managers[current_prediction_manager_idx].trees[nxt_l_prediction_idx].attention = attention_outputs_l[i]
                queue.append((current_prediction_manager_idx, nxt_l_prediction_idx))
              if target_idx == 0:
                continue
              if predictions_r[i].data[0] == data_utils.EOS_ID:
                continue
              if target_tree is None or target_tree.rchild is None:
                nxt_r_prediction_idx = prediction_managers[current_prediction_manager_idx].create_binary_tree(predictions_r[i].data[0], current_prediction_idx, current_prediction_tree.depth + 1)
                prediction_managers[current_prediction_manager_idx].trees[nxt_r_prediction_idx].target = None
              else:
                nxt_r_prediction_idx = prediction_managers[current_prediction_manager_idx].create_binary_tree(predictions_r[i].data[0], current_prediction_idx, current_prediction_tree.depth + 1)
                prediction_managers[current_prediction_manager_idx].trees[nxt_r_prediction_idx].target = target_tree.rchild
              prediction_managers[current_prediction_manager_idx].trees[current_prediction_idx].rchild = nxt_r_prediction_idx
              prediction_managers[current_prediction_manager_idx].trees[nxt_r_prediction_idx].prediction = predictions_r[i].data[0]
              prediction_managers[current_prediction_manager_idx].trees[nxt_r_prediction_idx].state = states_r[0][:,i,:], states_r[1][:, i, :]
              prediction_managers[current_prediction_manager_idx].trees[nxt_r_prediction_idx].attention = attention_outputs_r[i]
              queue.append((current_prediction_manager_idx, nxt_r_prediction_idx))
    return predictions_per_batch, prediction_managers


  def get_batch(self, data, start_idx):

    encoder_managers, decoder_managers = [], []

    for i in xrange(self.batch_size):
      if i + start_idx < len(data):
        encoder_input, decoder_input, encoder_manager, decoder_manager = data[i + start_idx]
      else:
        encoder_input, decoder_input, encoder_manager, decoder_manager = data[i + start_idx - len(data)]

      encoder_managers.append(encoder_manager)
      decoder_managers.append(decoder_manager)

    return encoder_managers, decoder_managers
