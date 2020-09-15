# https://api.github.com/search/repositories?q=tetris+language:java&sort=stars&order=desc

import urllib.parse
import requests
import wget

OUTPUT_FOLDER = "./zips/"

# api = 'https://api.github.com/search/repositories?q='
api = 'https://api.github.com/search/repositories?q=c+language:c&sort=stars&order=desc'

# param  = 'java'
# url  = api + urllib.parse.urlencode({'address':param})

json_data = requests.get(api).json()

# print('Response  : ', (json_data) )
print(len(json_data['items']))
print(json_data['items'][0]['full_name'])

for item in json_data['items']:
    url = item['html_url'] + "/archive/master.zip"
    print(url)
    fileName = item['full_name'].replace("/","_") + ".zip"
    wget.download(url, out=OUTPUT_FOLDER + fileName)