from github import Github
import mysql.connector

#specify your github token
token = ""
g = Github(token)

#specify file types you want to download
required_file_types = [".java"]

#specify the language you want to search github for
lang = "java"

#specify directory to store the unzipped projects
output_dir = "D:/Thesis/Mined/"


#specify database credentials
mydb = mysql.connector.connect(
    host="localhost",
    user="root",
    passwd="",
    database="miner"
)