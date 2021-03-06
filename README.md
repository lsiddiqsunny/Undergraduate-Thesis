# SQL vulnaribility detection and refactoring

We are going to detect SQL vulnaribility by Machine learning and refactoring them by replacing prepared statement.

## Motivation

Our motivation for thesis is this paper : [sql vulnerability refactoring](Paper/sql_vulnerability_refactoring.pdf)

Abstract of the paper :

```Since 2002, over 10% of total cyber vulnerabilities were SQL injection vulnerabilities (SQLIVs). This paper
presents an algorithm of prepared statement replacement for removing SQLIVs by replacing SQL statements
with prepared statements. Prepared statements have a static structure, which prevents SQL injection
attacks from changing the logical structure of a prepared statement. We created a prepared
statement replacement algorithm and a corresponding tool for automated fix generation. We conducted
four case studies of open source projects to evaluate the capability of the algorithm and its automation.
The empirical results show that prepared statement code correctly replaced 94% of the SQLIVs in these
projects.
```

## Way to the goal

Our work for this project till now :

1. Read the paper and present the [slide](Presentation/Review_On_automated_prepared_statement_generation_to_remove.pptx)
2. Collect source code from the orginal paper writer : [Code base for PSR ALGO](Code_base_for_PSR_ALGO/Fixer.java)
3. Prepared miner for mining java code : [Miner](GitMiner/README.md)
4. Collected data : [Mined data](https://drive.google.com/drive/folders/1SyAFe6xUG84n4uQ1ChQG_REgEhU_0mhJ?usp=sharing)
5. Work on java parser : [Java parser](Java_parser/README.md)
6. Collected paper and notes on TreeLSTM : [Paper](Tree_LSTM/README.md)
7. Data validation for input : [Data validation](Data_Validation/script.sh)
8. Model created and solution finder: [Getafix for SQLIFIX](Getafix)

Find our paper here: <https://lsiddiqsunny.github.io/public/SANER_2021.pdf> and thesis paper [here](1505065_1505069_Thesis.pdf)
