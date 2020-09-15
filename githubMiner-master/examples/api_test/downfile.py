import os
from git import Repo
import shutil
import tempfile
import urllib.parse
import requests

# api = 'https://api.github.com/search/repositories?q='
api = 'https://api.github.com/search/repositories?q=java+language:java&sort=stars&order=desc'

# param  = 'java'
# url  = api + urllib.parse.urlencode({'address':param})


'''
    For the given path, get the List of all files in the directory tree 
'''
def getListOfFiles(dirName):
    # create a list of file and sub directories 
    # names in the given directory 
    listOfFile = os.listdir(dirName)
    allFiles = list()
    # Iterate over all the entries
    for entry in listOfFile:
        # Create full path
        fullPath = os.path.join(dirName, entry)
        # If entry is a directory then get the list of files in this directory 
        if os.path.isdir(fullPath):
            allFiles = allFiles + getListOfFiles(fullPath)
        else:
            allFiles.append(fullPath)
                
    return allFiles

json_data = requests.get(api).json()

print('Response length : ', (json_data['total_count']) )
print(len(json_data['items']))
print(json_data['items'][0]['full_name'])

i = 0
for item in json_data['items']:
    url = item['html_url']
    print(url)

    # Create temporary dir
    t = tempfile.mkdtemp()
    # Clone into temporary dir
    Repo.clone_from( url+'.git' , t, branch='master', depth=1)
    # Copy desired file from temporary dir
    # print(os.path.dirname(t))
    # print(os.listdir(t))

    for item in getListOfFiles(t):
        print(item," -- \n")

    # shutil.move(os.path.join(t, 'layers'+'/'), '.')
    shutil.move(t, '.')

    # shutil.copytree(os.path.join(t, ''), '~/Desktop/gitCrawler/', symlinks=False, ignore=None, ignore_dangling_symlinks=False)
    # Remove temporary dir
    # shutil.rmtree(t)
    i = i+1
