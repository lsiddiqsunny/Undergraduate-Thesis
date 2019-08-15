touch output.txt #make a output file containing the result of the following query


ls>output.txt
j=0;
i=8653;
while read -r file; do
  #echo "$file"

  	((j++))
	if [ $j -eq 1 ]
	then 
		continue;
	fi
	if [ $j -eq 2 ]
	then 
		continue;
	fi
	((i++))
  mv  "$file" "/media/lsiddiqsunny/New Volume/Thesis/Mined/Test Data for Jahin/Final_Input_For_Test_Run/"$i # move the folder to the final folder   
done < "output.txt"
#rm output.txt
