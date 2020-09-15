mysql database-> table repos in schema miner

Commands->
Finder.py lang: gets repository urls of random dates of given language=lang into repos table
Downloader.py lang: downloads randomly selected repo of given language=lang to coded directory= "G:/MinedZips/", 
			also dumps all the info from git api into a json file

Token.txt-> 1st line contains a personal access token from github. It increases hourly limit to 5000 downloads.
		this file is added to gitignoe. otherwise token will be revoked.
