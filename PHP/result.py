import csv
with open('accuracy.csv', 'w', newline='') as file:
    writer = csv.writer(file)

    for i in range(1,51):
        fileHandle = open ('D:\\Thesis\\Undergraduate-Thesis\\PHP\\Clustering\\output\\output'+str(i)+'.txt',"r" )
        lineList = fileHandle.readlines()
        fileHandle.close()
        print (float(lineList[len(lineList)-1].split(":")[-1]))
        writer.writerow([i,float(lineList[len(lineList)-1].split(":")[-1])])