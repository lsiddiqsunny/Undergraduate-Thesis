import time
import Downloader
while True:
    try:
        Downloader.downloader()
    except Exception as e:
        print(e)
        time.sleep(20)
        pass
    else:
        break
