import os
import re

base_path = 'D:\\Thesis\CWE89_SQL_Injection\\CWE89_SQL_Injection\\s01\\'
directories = [x[2]
               for x in os.walk(base_path)]
# os.makedirs(base_path+'Temp')

for x in directories[0]:
  print(x)

for x in directories[0]:
    # os.makedirs(base_path+'Temp\\'+x.split('.')[0])
    with open(base_path+x, encoding="utf8") as search:
        i = 1
        ends = True
        for line in search:
            if(ends == False):
                f = open("D:\Thesis\CWE89_SQL_Injection\Target\s01\\"+x+'\\String'+str(i)+'.txt',  "w")
                f.write(" "+line)
                if(line.find(";") >= 0):
                    f.close()
                    ends = True
                continue
            #line = line.rstrip()
            if (re.search(r'DELETE.*FROM.*WHERE.*|SELECT.*FROM.*WHERE.*|INSERT.*INTO.*|UPDATE.*SET.*', line.lower(), re.IGNORECASE) != None):
                if not os.path.exists("D:\Thesis\CWE89_SQL_Injection\Target\s01\\"+x):
                    os.makedirs("D:\Thesis\CWE89_SQL_Injection\Target\s01\\"+x)
                f = open("D:\Thesis\CWE89_SQL_Injection\Target\s01\\"+x+'\\String'+str(i)+'.txt',  "w")
                f.write(line)
                i += 1
                if(line.find(";") == -1):
                    ends = False
                    continue
                f.close()
