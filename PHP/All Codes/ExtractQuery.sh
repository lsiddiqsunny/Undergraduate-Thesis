 #!bin/bash

directory="./Dataset/Temp"
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
	while [ $i -le $((n/2)) ]
	do
		echo "$directory/$x/beforeString$i.txt"
		java ExtractQuery "$directory/$x/beforeString$i.txt" "./Dataset/Temp2/$x/beforeQuery$i.sql" "./Dataset/Temp2/$x/"
		i=$((i+1))
	done
	cd $directory
done