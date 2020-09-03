import os
import csv
file_arr = os.listdir('./Test_Edit Trees')
with open('linenumber.csv', 'w', newline='') as file:
    writer = csv.writer(file)
    for file in file_arr:
        with open('/media/lsiddiqsunny/New Volume/Thesis/Getafix/gumtree-spoon-ast-diff+clustering/Before/before_'+file[7:-5]+'.java', 'r+') as f:
            content = f.read()
        linenum = ''
        with open('/media/lsiddiqsunny/New Volume/Thesis/Getafix/gumtree-spoon-ast-diff+clustering/Before/before_'+file[7:-5]+'.java','r') as myFile:
            print(' IN '+file[7:-5]+" : ")
            lookup = 'select '
            for num, line in enumerate(myFile, 1):
                if lookup.lower() in line.lower():
                    linenum+=str(num)+' '
                    print ('found at line:', num)

        with open('/media/lsiddiqsunny/New Volume/Thesis/Getafix/gumtree-spoon-ast-diff+clustering/Before/before_'+file[7:-5]+'.java','r') as myFile:
            lookup = 'insert '
            for num, line in enumerate(myFile, 1):
                if lookup.lower() in line.lower():
                    linenum+=str(num)+' '
                    print ('found at line:', num)
        with open('/media/lsiddiqsunny/New Volume/Thesis/Getafix/gumtree-spoon-ast-diff+clustering/Before/before_'+file[7:-5]+'.java','r') as myFile:                   
            lookup = 'delete '
            for num, line in enumerate(myFile, 1):
                if lookup.lower() in line.lower():
                    linenum+=str(num)+' '
                    print ('found at line:', num)
        with open('/media/lsiddiqsunny/New Volume/Thesis/Getafix/gumtree-spoon-ast-diff+clustering/Before/before_'+file[7:-5]+'.java','r') as myFile:

            lookup = 'update '
            for num, line in enumerate(myFile, 1):
                if lookup.lower() in line.lower():
                    linenum+=str(num)+' '
                    print ('found at line:', num)

        with open('/media/lsiddiqsunny/New Volume/Thesis/Getafix/gumtree-spoon-ast-diff+clustering/Before/before_'+file[7:-5]+'.java','r') as myFile:

            lookup = 'statement'
            for num, line in enumerate(myFile, 1):
                if lookup.lower() in line.lower():
                    linenum+=str(num)+' '
                    print ('found at line:', num)
        with open('/media/lsiddiqsunny/New Volume/Thesis/Getafix/gumtree-spoon-ast-diff+clustering/Before/before_'+file[7:-5]+'.java','r') as myFile:

            lookup = 'execute'
            for num, line in enumerate(myFile, 1):
                if lookup.lower() in line.lower():
                    linenum+=str(num)+' '
                    print ('found at line:', num)
        os.system('java Fixer '+'"/media/lsiddiqsunny/New Volume/Thesis/Getafix/gumtree-spoon-ast-diff+clustering/Before/before_"'+file[7:-5]+'.java '+linenum) 
        writer.writerow([file[7:-5] , linenum])


        
