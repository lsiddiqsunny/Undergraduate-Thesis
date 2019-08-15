# Tree-to-tree Neural Networks for Program Translation

This repo provides the code to replicate the experiments in the paper

> Xinyun Chen, Chang Liu, Dawn Song, <cite> Tree-to-tree Neural Networks for Program Translation </cite>,
> in NeurIPS 2018

Paper [[arXiv](https://arxiv.org/abs/1802.03691)][[NeurIPS](https://papers.nips.cc/paper/7521-tree-to-tree-neural-networks-for-program-translation)]

# Prerequisites

PyTorch

# Datasets

The datasets can be found [here](https://drive.google.com/open?id=1LDQOVcgFLrTRjIXH3Tc7kzmoAGK2vVKo).

# Usage

## Model architectures

The code includes the implementation of following models:

* Seq2seq: in `src/translate.py`, set `--network` to be `seq2seq`.
* Seq2tree: in `src/translate.py`, set `--network` to be `seq2tree`.
* Tree2seq: in `src/translate.py`, set `--network` to be `tree2seq`.
* Tree2tree: in `src/translate.py`, set `--network` to be `tree2tree`.
    * Without attention: set `--no_attention` to be `True`.
    * Without parent attention feeding: set `--no_pf` to be `True`.

## Run experiments

In the following we list some important arguments in `translate.py`:
* `--train_data`, `--val_data`, `--test_data`: path to the preprocessed dataset.
* `--load_model`: path to the pretrained model (optional).
* `--train_dir`: path to the folder to save the model checkpoints.
* `--input_format`, `--output_format`: can be chosen from `seq` (tokenized sequential program) and `tree` (parse tree).
* `--test`: add this command during the test time, and remember to set `--load_model` during evaluation.

```bash
python translate.py --network tree2tree --train_dir ../model_ckpts/tree2tree/ --input_format tree --output_format tree
```

# Citation

If you use the code in this repo, please cite the following paper:

```
@inproceedings{chen2018tree,
  title={Tree-to-tree Neural Networks for Program Translation},
  author={Chen, Xinyun and Liu, Chang and Song, Dawn},
  booktitle={Proceedings of the 31st Advances in Neural Information Processing Systems},
  year={2018}
}
```
