#!/bin/bash

directory="/mnt/d/Thesis/CWE89_SQL_Injection/Target/s01/"
cd $directory
for x in $(ls)
do

#echo $x
cd $x
for y in $(ls)
do
SUB='String'
cd "/mnt/d/Thesis/CWE89_SQL_Injection/"
if [[ "$y" == *"$SUB"* ]]; then
  java ExtractQuery "$directory/$x/$y" "$directory/$x/$y.sql" 
fi
done

cd $directory
done
