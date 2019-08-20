import csv
import shutil, os, glob


def moveAllFilesinDir(srcDir, dstDir):
    # Check if both the are directories
    if os.path.isdir(srcDir) and os.path.isdir(dstDir):
        # Iterate over all the files in source directory
        for filePath in glob.glob(srcDir + '\*'):
            # Move each file to destination Directory
            shutil.move(filePath, dstDir);
    else:
        print("srcDir & dstDir should be Directories")



sourceDir = 'D:\Thesis\Mined\Test Data'
destDir = 'D:\Thesis\Mined\Dataset From Sunny'


with open('../log.csv') as csv_file:
    csv_reader = csv.reader(csv_file, delimiter=',')
    line_count = 0
    for row in csv_reader:
        if line_count == 0:
            print(f'Column names are {", ".join(row)}')
            line_count += 1
        else:
            if "done" in row[1]:
                if not os.path.exists(destDir+"\\"+row[0]):
                    os.makedirs(destDir+"\\"+row[0])

                moveAllFilesinDir(sourceDir+"\\"+row[0], destDir+"\\"+row[0])
                #print()
            line_count += 1
    print(f'Processed {line_count} lines.')