import sys  #for command line argument
# from git import Repo
import os
import shutil
import tempfile
import urllib.parse
from random import randint
import time
import mysql.connector
import requests
import wget
import json
import simplejson
import csv
import math
import config
import myParser

query_limit = 5


headers = {
    'Authorization': 'token ' + config.token,
}

def downloader():
    response = requests.get('https://api.github.com/', headers=headers)

    print(response)



    if not os.path.exists(config.output_dir):
        os.makedirs(config.output_dir)

    mycursor = config.mydb.cursor()

    while 1 > 0:
        sql = "select * from repos WHERE language = %s AND downloaded = 0 ORDER BY RAND() LIMIT %s"
        val = (config.lang, query_limit)

        mycursor.execute(sql, val)
        myResult = mycursor.fetchall()

        if len(myResult) < 5:
            print("Not enough links to download")
            break

        #============================================== download

        for entry in myResult:
            print(entry)

            print("url: " + entry[1])

            item = requests.get(entry[1], headers=headers).json()

            if len(item) < 6:   #for invalid requests
                print("not found")
                continue

            # Obtain user and repository names
            user = item['owner']['login']
            repository = item['name']

            # Download the zip file of the current project
            # print("Downloading repository '%s' from user '%s' ..." % (repository, user))
            # url = item['clone_url']
            # fileToDownload = url[0:len(url) - 4] + "/archive/master.zip"
            # fileName = item['full_name'] + ".zip"
            jsonName = item['full_name'] + ".json"

            # if not os.path.exists(OUTPUT_FOLDER+user):
            #     os.mkdir(OUTPUT_FOLDER+user)


            # wget.download(fileToDownload, OUTPUT_FOLDER + fileName)

            myParser.parse(item['full_name'])
            print(item['full_name'])

            with open(config.output_dir + jsonName, "w") as jsonFile:
                json.dump(item, jsonFile)

            # print(user)
            # print(repository)

            sql = "UPDATE repos SET downloaded = 1 WHERE id = %s LIMIT 5"
            val = (str(entry[0]), )

            mycursor.execute(sql, val)
            config.mydb.commit()

        time.sleep(1)

#downloader()

# ==== Selective download
# g = Github("AwsafAlam", "******") #username, password

# #for a specific repo
# base_url = "https://raw.githubusercontent.com/"
# repo_url = "skylot/jadx/master/"
# repo = g.get_repo("skylot/jadx")
# #parent_dir = "./dl/" + repo_url.replace("/", "\\")
# parent_dir = "./dl/" + repo_url

# print(parent_dir)
# os.makedirs(parent_dir)
# contents = repo.get_contents("")
# while len(contents) > 1:
#     file_content = contents.pop(0)
#     if file_content.type == "dir":
#         #os.makedirs(parent_dir + file_content.path.replace("/", "\\") )
#         os.makedirs(parent_dir + file_content.path )
#         contents.extend(repo.get_contents(file_content.path))
#     else:
#         path = file_content.path
#         if path.find(".java", -5) != -1: # .cpp for cpp file
# 		print(base_url+repo_url+path, parent_dir + path)            
# 		urllib.request.urlretrieve(base_url+repo_url+path, parent_dir + path )
# 	    #urllib.request.urlretrieve(base_url+repo_url+path, parent_dir + path.replace("/", "\\") )


#urllib.request.urlretrieve('https://raw.githubusercontent.com/PyGithub/PyGithub/master/tests/ReplayData/AuthenticatedUser.testAttributes.txt',"D:\\ok.txt")
