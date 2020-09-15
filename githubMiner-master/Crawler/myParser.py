import urllib.request
import os
import config

base_url = "https://raw.githubusercontent.com/"

def parse(repo_full_name):
   # print(repo_full_name)
    repo_url = repo_full_name + "/master/"
    repo = config.g.get_repo(repo_full_name)
    parent_dir = config.output_dir + repo_url.replace("/", "\\")
    if not os.path.exists(parent_dir):
        os.makedirs(parent_dir)
    contents = repo.get_contents("")
    while len(contents) >= 1:
        file_content = contents.pop(0)
        #was previously assigning it here for printing...path=file_content.path 
        if file_content.type == "dir":
            if not os.path.exists(parent_dir + file_content.path.replace("/", "\\")):
                os.makedirs(parent_dir + file_content.path.replace("/", "\\"))
            contents.extend(repo.get_contents(file_content.path))
        else:
            path = file_content.path
            filename, extension = os.path.splitext(path)
            if extension in config.required_file_types:
                urllib.request.urlretrieve(base_url+repo_url+path, parent_dir+path.replace("/", "\\") )
    
    return 1  #downloaded required files

#parse("AwsafAlam/githubMiner")
