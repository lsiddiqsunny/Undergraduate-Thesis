import os
import re

'''
#base_path = 'D:\\Thesis\\Mined\\Dataset From Sunny - Copy\\'
base_path = 'C:\\Users\\shami\\Documents\\Undergrad-Thesis\\Getafix Code\\Dataset from Jahin - Copy\\'

directories = [x[0].split('\\')[len(x[0].split('\\'))-1]
               for x in os.walk(base_path)]
print(directories)
# os.makedirs(base_path+'Temp')

directoryList = []
for x in directories:
    try:
        i = int(x)
        directoryList.append(x)
    except ValueError as verr:
        print('Value Error for'+x)
    except Exception as ex:
        print('Other Error')
for x in directoryList:
    with open(base_path+x) as search:
        #f = open(base_path+x+'\\String.txt', "w")
        # f.close()

        i = int(1)
        for line in search:
            line = line.rstrip()
            # if 'select' in line.lower() or 'update' in line.lower() or 'insert' in line.lower():
            if (re.search(r'DELETE.*FROM.*|SELECT.*FROM.*|INSERT.*INTO.*|UPDATE.*SET.*', line.lower(), re.IGNORECASE) != None):
                f = open(base_path+x+'\\String'+str(i)+'.txt', "w")
                f.write(line)
                i += 1
                f.close()
'''

base_path = 'D:\\Thesis\\Undergraduate-Thesis\\PHP\\All Codes\\Dataset\\'
directories = [x for x in os.walk(base_path)]
# os.makedirs(base_path+'Temp')
#print(directories)
#for x in directories[0][1]:
#   print(x)

for x in directories[0][1]:
    # os.makedirs(base_path+'Temp\\'+x.split('.')[0])
    
    print(x)
    if x=='Temp':
    	continue
    with open(base_path+x+'/before.php') as search:
        print(search)
        i = 1
        ends = True
        for line in search:
            if(ends == False):
                f.write(" "+line)
                if(line.find(";") >= 0):
                    f.close()
                    ends = True
                continue
            #line = line.rstrip()
            if (re.search(r'DELETE.*FROM.*WHERE.*|SELECT.*FROM.*WHERE.*|INSERT.*INTO.*|UPDATE.*SET.*', line.lower(), re.IGNORECASE) != None):
                if not os.path.exists(base_path+'Temp/'+x):
                    os.makedirs(base_path+'Temp/'+x)
                f = open(base_path+'Temp/'+x+'/beforeString'+str(i)+'.txt', "w")
                f.write(line)
                i += 1
                if(line.find(";") == -1):
                    ends = False
                    continue
                f.close()
