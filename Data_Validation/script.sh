touch output.txt #make a output file containing the result of the following query
grep -EiRl "\"select.*from.*where|\"Insert.*into|\"update.*set|\"delete.*from.*where" ./ >output.txt # search sql query

touch output2.txt # make a temporary output file
while read -r file; do
  #echo "$file"
  IFS='/'
  read -ra ADDR<<<"$file" #split the result by '/'
  j=0;
  a1=""
  a2=""
  for i in "${ADDR[@]}"; do
	((j++))
	if [ $j -eq 1 ]
	then 
		a="$i"
	fi
	if [ $j -eq 2 ]
	then 
		b="$i"
	fi
	
  done
echo $a"/""$b">>output2.txt # get the folder name
IFS=' '
done < "output.txt"

sort output2.txt | uniq >output.txt
rm output2.txt

while read -r file; do
  #echo "$file"
  cp -r "$file" "/media/lsiddiqsunny/New Volume/Thesis/Mined/Final_Output_From_28_5_2019" # move the folder to the final folder   
done < "output.txt"
rm output.txt
