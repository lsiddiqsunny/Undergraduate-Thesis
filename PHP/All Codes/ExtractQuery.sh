 #!bin/bash

directory="./Before/Temp"
cd $directory

for x in $(ls)
do
	#now in Temp folder
	
	#num=$(echo $x | tr "_" "\n")
	#set -- $num
	#echo $2
	echo $x
	cd $x
	#now in before_x folder
	
	n=$(ls | wc -l)
	#n=$((n-1))
	cd ../../..
	#now in data folder
	i=1
	while [ $i -le $n ]
	do
		echo "$directory/$x/String$i.txt"
		java ExtractQuery "$directory/$x/$y/String$i.txt" "./Before/Temp2/$x/Query$i.sql" "./Before/Temp2/$x/"
		i=$((i+1))
	done
	cd $directory
done