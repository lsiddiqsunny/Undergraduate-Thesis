import time
import Finder
while True:
    try:
        Finder.finder()
    except Exception as e:
        print(e)
        time.sleep(20)
        pass
    else:
        break
