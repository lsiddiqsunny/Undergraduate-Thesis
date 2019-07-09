# Notes for paper on Tree LSTM

Collected paper on tree LSTM and notes on them.

1. Improved Semantic Representations FromTree-Structured Long Short-Term Memory Networks

   **Abstract**

   ```Because  of  their  superior  ability  to  pre-serve   sequence   information   over   time,Long  Short-Term  Memory  (LSTM)  net-works,   a  type  of  recurrent  neural  net-work with a more complex computationalunit, have obtained strong results on a va-riety  of  sequence  modeling  tasks.Theonly underlying LSTM structure that hasbeen  explored  so  far  is  a  linear  chain.However,  natural  language  exhibits  syn-tactic properties that would naturally com-bine words to phrases.  We introduce theTree-LSTM, a generalization of LSTMs totree-structured network topologies.  Tree-LSTMs  outperform  all  existing  systemsand strong LSTM baselines on two tasks:predicting the semantic relatedness of twosentences  (SemEval  2014,  Task  1)  andsentiment  classification  (Stanford  Senti-ment Treebank)```

   **My notes**

   Will be added soon.

2. Automatic Source Code Summarization with Extended Tree-LSTM

   **Abstract**

   ```Neural  machine  translation  models  are  used  to  automatically  generate  a  document  from  given  source  codesince this can be regarded as a machine translation task.  Source code summarization is one of the components forautomatic document generation, which generates a summary in natural language from given source code.  Thissuggests that techniques used in neural machine translation, such as Long Short-Term Memory (LSTM), can beused for source code summarization.  However, there is a considerable difference between source code and naturallanguage:  Source code is essentiallystructured, having loops and conditional branching, etc.  Therefore, there issome obstacle to apply known machine translation models to source code.Abstract syntax trees (ASTs) capture these structural properties and play an important role in recent machinelearning studies on source code.  Tree-LSTM is proposed as a generalization of LSTMs for tree-structured data.However, there is a critical issue when applying it to ASTs:  It cannot handle a tree that contains nodes havingan  arbitrary  number  of  children  and  their  order  simultaneously,  which  ASTs  generally  have  such  nodes.   Toaddress this issue, we propose an extension of Tree-LSTM, which we callMulti-way Tree-LSTMand apply it forsource code summarization.  As a result of computational experiments, our proposal achieved better results whencompared with several state-of-the-art techniques.```

   **My notes**

   Will be added soon.

3. Improving Tree-LSTM with Tree Attention

   **Abstract**

   ```In Natural Language Processing (NLP), we often need to extract information from tree topology.Sentence structure can be represented via a dependency tree or a constituency tree structure. For this reason, a variant of LSTMs, named Tree-LSTM, was proposed to work on tree topology. In this paper, we design a generalized attention framework for both dependence and constituency trees by encoding variants of decomposable attention inside a Tree-LSTM cell. We evaluated our models on a semantic relatedness task and achieved notable results compared to Tree-LSTM based methods with no attention as well as other neural and non-neural methods and good results compared to Tree-LSTM based methods with attention.```

   **My notes**

   Will be added soon.

4. Improving Tree-LSTM with Tree Attention

   **Abstract**

   ```Optimizing compilers, as well as other translator systems, often work by rewriting expressions ac-cording to equivalence preserving rules.  Given an input expression and its optimized form, findingthe sequence of rules that were applied is a non-trivial task.  Most of the time, the tools provideno proof, of any kind, of the equivalence between the original expression and its optimized form.In this work, we propose to reconstruct proofs of equivalence of simple mathematical expressions,after the fact, by finding paths of equivalence preserving transformations between expressions.  Wepropose to find those sequences of transformations using a search algorithm,  guided by a neuralnetwork heuristic. Using a Tree-LSTM recursive neural network, we learn a distributed representa-tion of expressions where the Manhattan distance between vectors approximately corresponds to therewrite distance between expressions. We then show how the neural network can be efficiently usedto search for transformation paths, leading to substantial gain in speed compared to an uninformedexhaustive search.  In one of our experiments, our neural-network guided search algorithm is ableto solve more instances with a 2 seconds timeout per instance than breadth-first search does with a5 minutes timeout per instance.```

   **My notes**

   Will be added soon.

5. Structure Tree-LSTM: Structure-aware Attentional Document Encoders

   **Abstract**

   ```We propose a method to create document representations that reflect their internal structure. We modify Tree-LSTMs to hierarchically merge basic elements like words and sentences into blocks of increasing complexity. Our Structure Tree-LSTM implements a hierarchical attention mechanism over individual components and combinations thereof. We thus emphasize the usefulness of Tree-LSTMs for texts larger than a sentence. We show that structure-aware encoders can be used to improve the performance of document classification. We demonstrate that our method is resilient to changes to the basic building blocks, as it performs well with both sentence and word embeddings. The Structure Tree-LSTM outperforms all the baselines on two datasets when structural clues like sections are available, but also in the presence of mere paragraphs. On a third dataset from the medical domain, our model achieves competitive performance with the state of the art. This result shows the Structure Tree-LSTM can leverage dependency relations other than text structure, such as a set of reports on the same patient.```

   **My notes**

   Will be added soon.

6. Tree-to-tree Neural Networks for Program Translation

   **Abstract**

   ```Program translation is an important tool to migrate legacy code in one language into an ecosystem built in a different language. In this work, we are the first to employ deep neural networks toward tackling this problem. We observe that program translation is a modular procedure, in which a sub-tree of the source tree is translated into the corresponding target sub-tree at each step. To capture this intuition, we design a tree-to-tree neural network to translate a source tree into a target one. Meanwhile, we develop an attention mechanism for the tree-to-tree model, so that when the decoder expands one non-terminal in the target tree, the attention mechanism locates the corresponding sub-tree in the source tree to guide the expansion of the decoder. We evaluate the program translation capability of our tree-to-tree model against several state-of-the-art approaches. Compared against other neural translation models, we observe that our approach is consistently better than the baselines with a margin of up to 15 points. Further, our approach can improve the previous state-of-the-art program translation approaches by a margin of 20 points on the translation of real-world projects```

   **My notes**

   Will be added soon.
