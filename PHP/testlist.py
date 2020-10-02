fileHandle = open ('D:\\Thesis\\Undergraduate-Thesis\\PHP\\list.txt',"r" )
lineList = fileHandle.readlines()
fileHandle.close()

f = open ('D:\\Thesis\\Undergraduate-Thesis\\PHP\\Clustering\\test_list.txt',"w" )
for line in lineList:
    print (int(line.split(":")[0]))
    f.write(line.split(":")[0])
    f.write('\n')
f.close()