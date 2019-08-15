touch output.txt #make a output file containing the result of the following query


ls>output.txt
j=0;
i=0;
while read -r file; do
  echo "$file"
  cd "$file"
  ls	
  cp code.java solution.java
  cd ..
  
  #cp  "$file" "/media/lsiddiqsunny/New Volume/Thesis/Mined/Test Data for Jahin/Final_Input_For_Test_Run/"$i # move the folder to the final folder   
done < "output.txt"
rm output.txt
