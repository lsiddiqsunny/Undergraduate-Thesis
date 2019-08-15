# recursivejson.py
import json

def extract_values(obj, key):
    """Pull all values of specified key from nested JSON."""
    arr = []

    def extract(obj, arr, key):
        """Recursively search for values of key in JSON tree."""
        if isinstance(obj, dict):
            for k, v in obj.items():
                if isinstance(v, (dict, list)):
                    extract(v, arr, key)
                elif k == key:
                    arr.append(v)
        elif isinstance(obj, list):
            for item in obj:
                extract(item, arr, key)
        return arr

    results = extract(obj, arr, key)
    return results


Myset = {}
Myset = set()

<<<<<<< HEAD
<<<<<<< HEAD
for i in range(21665):
=======
for i in range(8013):
>>>>>>> 6bc91c01667b80d78e1998ceef4516381f6f35f8
=======
for i in range(8013):
>>>>>>> 6bc91c01667b80d78e1998ceef4516381f6f35f8

    filename='D:\\Thesis\\Mined\\Input_From_Test_Run\\Test_'+str(i+1)+'\\ast.json'
    print(filename+"\n")
    f=open(filename, encoding="utf8")
    names = extract_values(json.load(f), 'type')
    for name in names:
        Myset.add(name)

f1=open("keywords.txt","w+")
for x in Myset:
    print(x+"\n")
    f1.write(x+"\n")