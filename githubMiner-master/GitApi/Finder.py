import sys  #for command line argument
import os
import shutil
import tempfile
import urllib.parse
from random import randint
import time
import mysql.connector
import requests

sleep_time = 0

start_page = 1
end_page = 35
lang = sys.argv[1]

# print(start_page)
# print(end_page)
print("Language = " + lang)

#sort=stars, forks, help-wanted-issues, updated
#order=asc, desc

#maximum search result = 1000
#maximum in one page = 30
#maximum pages = 34


f = open("Token.txt", "r")
token = f.read()
f.close()
print(token)

headers = {
    'Authorization': 'token ' + token,
}

response = requests.get('https://api.github.com/', headers=headers)

print(response)


mydb = mysql.connector.connect(
  host="localhost",
  user="root",
  passwd="3985",
  database="miner"
)

# print(mydb)

mycursor = mydb.cursor()

# sql = "INSERT INTO repos (id, url, language, downloaded) VALUES (%s, %s, %s, %s)"
# val = ("1", "Highway 21", lang, "0")
# mycursor.execute(sql, val)

#============================================================== function start

def getRepos(urlAddr):
    for pageNo in range(int(start_page), int(end_page)):
        pageUrl = urlAddr + '&page=' + str(pageNo)
        print("pageUrl: " + pageUrl)

        json_data = requests.get(pageUrl, headers=headers).json()

        # if pageNo % 3 == 0:   #doesn't work

        time.sleep(sleep_time)  # dont overload the git api

        in_db = 0

        if len(json_data) > 2:
            print("Length of the page = " + str(len(json_data['items'])))

            if len(json_data['items']) < 1:
                break

            for itemNo in range(0, len(json_data['items'])):
                print("ID: " + str(json_data['items'][itemNo]['id']) + ", item url: " + json_data['items'][itemNo]['url'])

                sql = "SELECT * FROM repos WHERE id = %s"

                val = (str(json_data['items'][itemNo]['id']), )

                # val = ("1", )

                mycursor.execute(sql, val)

                myResult = mycursor.fetchall()

                if len(myResult) < 1:
                    sql = "INSERT INTO repos (id, url, language, downloaded) VALUES (%s, %s, %s, %s)"
                    val = (json_data['items'][itemNo]['id'], json_data['items'][itemNo]['url'], lang, "0")

                    mycursor.execute(sql, val)
                    mydb.commit()

                    print("Added to database")

                else:
                    in_db = in_db+1
                    print("Already in database")

            if in_db > len(json_data['items'])/2:
                break


#============================================================== function end


while 1 > 0:
    year = randint(2010, 2019)
    month = randint(1, 12)
    day = randint(1, 30)

    if month == 2 and day > 28:
        continue
    if year == 2019 and month > 4:
        continue

    if month < 10:
        monthStr = "0" + str(month)
    else:
        monthStr = str(month)

    if day < 10:
        dayStr = "0" + str(day)
    else:
        dayStr = str(day)

    dateStr = "+created:" + str(year) + "-" + monthStr + "-" + dayStr

    url = 'https://api.github.com/search/repositories?q=' + 'language:' + lang + dateStr + "&sort=updated&order=desc"
    getRepos(url)

    url = 'https://api.github.com/search/repositories?q=' + 'language:' + lang + dateStr + "&sort=updated&order=asc"
    getRepos(url)

    # url = 'https://api.github.com/search/repositories?q=' + 'language:' + lang + dateStr + "&sort=stars&order=desc"
    # getRepos(url)
    #
    # url = 'https://api.github.com/search/repositories?q=' + 'language:' + lang + dateStr + "&sort=stars&order=asc"
    # getRepos(url)
    #
    # url = 'https://api.github.com/search/repositories?q=' + 'language:' + lang + dateStr + "&sort=forks&order=desc"
    # getRepos(url)
    #
    # url = 'https://api.github.com/search/repositories?q=' + 'language:' + lang + dateStr + "&sort=forks&order=asc"
    # getRepos(url)
    #
    # url = 'https://api.github.com/search/repositories?q=' + 'language:' + lang + dateStr + "&sort=help-wanted-issues&order=desc"
    # getRepos(url)
    #
    # url = 'https://api.github.com/search/repositories?q=' + 'language:' + lang + dateStr + "&sort=help-wanted-issues&order=asc"
    # getRepos(url)

    f = open("Date.txt", "a")
    f.write(dateStr + "\n")
    f.close()


