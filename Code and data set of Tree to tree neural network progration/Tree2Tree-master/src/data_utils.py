import sys
import gzip
import os
import re
import tarfile
import operator
import json
import cPickle as pickle
from Tree import *

from six.moves import urllib

# Special vocabulary symbols
_PAD = b"_PAD"
_GO = b"_GO"
_EOS = b"_EOS"
_UNK = b"_UNK"
_NT = b"_NT"
_LEFT_BRACKET = b"("
_RIGHT_BRACKET = b")"
_START_VOCAB = [_PAD, _GO, _EOS, _UNK, _NT, _LEFT_BRACKET, _RIGHT_BRACKET]

PAD_ID = 0
GO_ID = 1
EOS_ID = 2
UNK_ID = 3
NT_ID = 4
LEFT_BRACKET_ID = 5
RIGHT_BRACKET_ID = 6

def add_tokens_from_code(code, vocab, format):
  if format == 'tree':
    tok = str(code["root"])
    if not (tok in vocab):
      vocab.append(tok)
    for sub_tree in code["children"]:
      vocab = add_tokens_from_code(sub_tree, vocab, format)
  else:
    for tok in code:
      if not (str(tok) in vocab):
        vocab.append(str(tok))
  return vocab

def get_max_num_children(tree):
  max_num_children = len(tree['children'])
  for child in tree['children']:
    t = get_max_num_children(child)
    max_num_children = max(t, max_num_children)
  return max_num_children

def calStat(data, serialize):
  if serialize:
    min_len = 10000
    max_len = 0
    avg_len = 0
    for seq in data:
      l = len(seq)
      min_len = min(min_len, l)
      max_len = max(max_len, l)
      avg_len += l
    return min_len, max_len, avg_len * 1.0 / len(data)
  min_len = 0
  max_len = 0
  avg_len = 0
  for tree in data:
    current_len = get_max_num_children(tree)
    avg_len = avg_len + current_len
    max_len = max(max_len, current_len)
  return min_len, max_len, avg_len * 1.0 / len(data)

def build_vocab(train_data, vocab_filename, input_format, output_format):

  if not vocab_filename:
    source_vocab_list = []
    target_vocab_list = []
    for prog in train_data:
      if input_format == 'seq':
        source_prog = prog['source_prog']
      else:
        source_prog = prog['source_ast']
      if output_format == 'seq':
        target_prog = prog['target_prog']
      else:
        target_prog = prog['target_ast']
      source_vocab_list = add_tokens_from_code(source_prog, source_vocab_list, input_format)
      target_vocab_list = add_tokens_from_code(target_prog, target_vocab_list, output_format)
    vocab = {}
    vocab["source"] = source_vocab_list
    vocab["target"] = target_vocab_list
  else:
    vocab = pickle.load(open(vocab_filename))
    source_vocab_list = vocab["source"]
    target_vocab_list = vocab["target"]

  source_vocab_list = _START_VOCAB[:] + source_vocab_list
  target_vocab_list = _START_VOCAB[:] + target_vocab_list
  source_vocab_dict = {}
  target_vocab_dict = {}
  for idx, token in enumerate(source_vocab_list):
    source_vocab_dict[token] = idx
  for idx, token in enumerate(target_vocab_list):
    target_vocab_dict[token] = idx
  return source_vocab_dict, target_vocab_dict, source_vocab_list, target_vocab_list

def ast_to_token_ids(code, vocab, serialize):
  if serialize:
    current = []
    current.append(vocab.get(str(code['root']), UNK_ID))
    if len(code['children']) > 0:
      current.append(LEFT_BRACKET_ID)
      for sub_tree in code['children']:
        child = ast_to_token_ids(sub_tree, vocab, serialize)
        current = current + child
      current.append(RIGHT_BRACKET_ID)
    return current
  else:
    current = {}
    current['root'] = vocab.get(str(code['root']), UNK_ID)
    current['children'] = []
    for sub_tree in code['children']:
      current['children'].append(ast_to_token_ids(sub_tree, vocab, serialize))
    return current

def serialize_tree(tree):
  current = []
  current.append(LEFT_BRACKET_ID)
  current.append(tree['root'])
  if len(tree['children']) > 0:
    for sub_tree in tree['children']:
      child = serialize_tree(sub_tree)
      current = current + child
  current.append(RIGHT_BRACKET_ID)
  return current

def raw_program_to_token_ids(prog, vocab):
  return [vocab[str(t)] for t in prog] + [EOS_ID]

def prepare_data(init_data, source_vocab, target_vocab, input_format, output_format, source_serialize, target_serialize):
  data = []
  for prog in init_data:
    if input_format == 'seq':
      source_prog = prog['source_prog']
    else:
      source_prog = prog['source_ast']
    if output_format == 'seq':
      target_prog = prog['target_prog']
    else:
      target_prog = prog['target_ast']
    if input_format == 'seq':
      source_prog = raw_program_to_token_ids(source_prog, source_vocab)
    else:
      source_prog = ast_to_token_ids(source_prog, source_vocab, source_serialize)
    if output_format == 'seq':
      target_prog = raw_program_to_token_ids(target_prog, target_vocab)
    else:
      target_prog = ast_to_token_ids(target_prog, target_vocab, target_serialize)
    data.append((source_prog, target_prog))
  if input_format == 'tree' and (not source_serialize):
    data = build_trees(data, target_serialize)
  return data

def build_trees(init_dataset, target_serialize=False):
  data_set = []
  for (source, target) in init_dataset:
    source_trees = TreeManager()
    source_trees.build_binary_tree_from_dict(source)
    if target_serialize:
      target_seq = target[:]
      data_set.append((source, target, source_trees, target_seq))
    else:
      target_trees = TreeManager()
      target_trees.build_binary_tree_from_dict(target)
      data_set.append((source, target, source_trees, target_trees))
  return data_set
    