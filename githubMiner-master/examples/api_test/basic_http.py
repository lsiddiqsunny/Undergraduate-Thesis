import urllib.parse
import requests

api = 'https://jsonplaceholder.typicode.com/todos/'

# param  = 'lhr'

# url  = api + urllib.parse.urlencode({'address':param})

json_data = requests.get(api).json()

for item in json_data:
    print('title : '+item['title'])