from github import Github
import urllib.request
import os

g = Github("AwsafAlam", "") #username, password

#for a specific repo
base_url = "https://raw.githubusercontent.com/"
repo_url = "josephmisiti/awesome-machine-learning/master/"
repo = g.get_repo("josephmisiti/awesome-machine-learning")
#parent_dir = "./dl/" + repo_url.replace("/", "\\")
parent_dir = "./dl/" + repo_url

print(parent_dir)
os.makedirs(parent_dir)
contents = repo.get_contents("")
while len(contents) >= 1:
    file_content = contents.pop(0)
    if file_content.type == "dir":
        #os.makedirs(parent_dir + file_content.path.replace("/", "\\") )
        os.makedirs(parent_dir + file_content.path )
        contents.extend(repo.get_contents(file_content.path))
    else:
        path = file_content.path
        if path.find(".py", -3) != -1: # .cpp for cpp file
            print(base_url+repo_url+path, parent_dir + path)            
            urllib.request.urlretrieve(base_url+repo_url+path, parent_dir + path )
            #urllib.request.urlretrieve(base_url+repo_url+path, parent_dir + path.replace("/", "\\") )


#urllib.request.urlretrieve('https://raw.githubusercontent.com/PyGithub/PyGithub/master/tests/ReplayData/AuthenticatedUser.testAttributes.txt',"D:\\ok.txt")
