#! /usr/bin/env python

"""
Author: Adrian Leong (cheeky4n6monkey@gmail.com)

Python script to extract JPG's from Android Gallery3D app imgcache, mini and micro cache files.
Script also creates a HTML table containing the extracted JPGs and image metadata.

Special Thanks to: LSB, Rob (@TheHexNinja), Terry Olson, Jason Eddy, Jeremy Dupuis and Cindy Murphy for their assistance and insights into the imgcache behaviour.

The record format observed for a Galaxy S4 (GT-i9505) / Galaxy Core Prime (SM-G360G) / J1 (SM-J100Y):

[16 unknown bytes (1st record has 20 bytes before)]
[4 byte LE Record Size]
[UTF16LE Item Path string]
[UTF16LE Index Number String]
[UTF16LE encoded "+"]
[UTF16LE Unknown Number String]
[8 byte LE int = Unix Timestamp (UTC) in ms for pics and video]
[Cached JPG image]

eg
[16 unknown bytes]
[0x6D71 0000 LE 4 byte int]
["/local/image/item/" for pics] OR ["/local/image/video/" for video thumbnails]
["44"]
["+"]
["1"]
[0x507CF7EF4F010000 = 1442840018000 dec = Mon, 21 September 2015 12:53:38.000 UTC via DCode]
[JPEG starts with xFFD8 ... ends with xFFD9]

The record size (eg 0x6D71 0000 LE = 0x716D = 29037 dec. bytes) includes everything from the start of the item path string until the last byte of the embedded JPG (it does NOT include the Record size) 

There can be more than one imagecache file located in /data/com.sec.android.gallery3d/cache/
eg imagecache.0 and imagecache.1
Other files may also contain cached images (eg mini.0, micro.0).
The script should handle these as they use the same record structure.

Running the script examples:
python imgcache-parse-mod.py -f imgcache.0 -o output.html
(will parse BOTH picture and video thumbnail cache items)

python imgcache-parse-mod.py -f imgcache.0 -o output.html -p
(will parse picture cache items ONLY)

python imgcache-parse-mod.py -f imgcache.0 -o output.html -v
(will parse video thumbnail cache items ONLY)

Versions:
2016-08-03 = Initial version (modified from imgcache-parse.py)

"""

import sys
import os
import struct
import datetime
import hashlib
from optparse import OptionParser

version_string = "imgcache-parse-mod.py v2016-08-03"

# Find all indices of a substring in a given string (Python recipe) 
# From http://code.activestate.com/recipes/499314-find-all-indices-of-a-substring-in-a-given-string/
def all_indices(bigstring, substring, listindex=[], offset=0):
    i = bigstring.find(substring, offset)
    while i >= 0:
        listindex.append(i)
        i = bigstring.find(substring, i + 1)

    return listindex

print("Running " + version_string + "\n")

usage = " %prog -f inputfile -o outputfile"

    
# Open imgcache file for binary read
<<<<<<< HEAD
filename="C:\\Users\\Air\\Desktop\\imgCacheParser\\imgcache[1].0"
=======
filename="C:\\Users\\mufassirmughal\\Desktop\\imgCacheParser\\imgcache[1].0"
>>>>>>> 45d37391cae0f982293a0b27b3832c005baecdc7
htmlfile="output.html"
try:
	fb = open(filename, "rb")
except:
    print("Error - Input file failed to open!")
    exit(-1)

filesize = os.stat(filename).st_size # get imgcache filesize  

# Read file into one BINARY string (shouldn't be too large)
filestring = fb.read()
# print(filestring)
# Search the binary string for the hex equivalent of "/local/image/item/" which appears in each imgcache record
substring1 = "\x2F\x00\x6C\x00\x6F\x00\x63\x00\x61\x00\x6C\x00\x2F\x00\x69\x00\x6D\x00\x61\x00\x67\x00\x65\x00\x2F\x00\x69\x00\x74\x00\x65\x00\x6D\x00\x2F\x00".encode()
# print(substring1)
# print(substring1.decode())
# Search for hex equivalent of "/local/video/item/" 
substring2 = "\x2F\x00\x6C\x00\x6F\x00\x63\x00\x61\x00\x6C\x00\x2F\x00\x76\x00\x69\x00\x64\x00\x65\x00\x6F\x00\x2F\x00\x69\x00\x74\x00\x65\x00\x6D\x00\x2F\x00".encode()

#hits = all_indices(filestring, substring1, [])
#hits = all_indices(filestring, substring2, [])

pichits = all_indices(filestring, substring1, [])
vidhits = all_indices(filestring, substring2, [])
tmphits = pichits + vidhits
hits = sorted(tmphits)
    
print("Paths found = " + str(len(hits)) + "\n")

MAXPATH = 200 # 100 x UTF16 chars = max path size
outputdict = {} # dictionary sorted by JPG offset. Contains extracted filename, size, item path string and MD5 tuple.

for hit in hits:
    jpgfound = False
    charcount = 0
    jpgstart = 0 # imgcache file offset for this image's FFD8
    pathname = ""

    fb.seek(hit)
    fb.seek(hit-4) # record size occurs 4 bytes before path
<<<<<<< HEAD
    picSize = fb.read(4)
    recsize = struct.unpack("<I", picSize)[0] # size does NOT include these 4 bytes. From start of path string to xFFD9 at end of JPG file
    print("JPG Size: "+str(recsize))
=======
    picX = fb.read(4)
    print("Byte Value: "+ str(picX))
    #print(binascii.unhexlify(picX))
    recsize = struct.unpack("<I", picX)[0] # size does NOT include these 4 bytes. From start of path string to xFFD9 at end of JPG file
>>>>>>> 45d37391cae0f982293a0b27b3832c005baecdc7
    jpgend = hit + recsize + 1 # should point to the byte after FFD9
    print("JPG End: "+str(jpgend))
    if (jpgend > filesize + 1):
        print("Bad end of JPG offset calculated for JPG starting at " + hex(hit).rstrip("L").upper() + " ... skipping!\n")
        break

    # Path string processing
    fb.seek(hit)
    # Read in 2 byte chunks until we come across xFF xD8 OR MAXPATH characters are read
    while not jpgfound:
        rawtmp = fb.read(2)
        readint = struct.unpack("<H", rawtmp)[0]
        #print(hex(readint).rstrip("L").upper())
        charcount += 2
        if (charcount > MAXPATH):
            print("Max number of characters read for path - skipping this hit\n")
            break
        if (readint == 0xD8FF): # Have run into the LE xFFxD8 JPG Header
            jpgfound = True
            jpgstart = fb.tell()-2
<<<<<<< HEAD
            print("JPG Start: "+str(jpgstart))
=======
            print("jpg start: "+str(jpgstart))
>>>>>>> 45d37391cae0f982293a0b27b3832c005baecdc7
            break

    if (jpgfound):
        #print("hit = " + hex(hit).rstrip("L").upper() + ", end = " + hex(jpgstart-8).rstrip("L").upper())
        pathname = filestring[hit:jpgstart-8].decode('utf-16-le')
        print("pathname = " + pathname)
        print("pathname Size = " + str(len(pathname)))
    else:
        continue # skip
        
    # Extract binary timestamp eg 1390351440000
    timestamp = struct.unpack("<Q", filestring[jpgstart-8:jpgstart])[0]

    # Convert timestamp (ms) into human readable ISO format (UTC). Replace ":" with "-" (more filename friendly)
    try:
        timestring = datetime.datetime.utcfromtimestamp(timestamp/1000).strftime("%Y-%m-%dT%H-%M-%S")
    except:
        timestring = "Error"

    #print("JPG start = " + hex(jpgstart).rstrip("L").upper())
    #print("JPG end = " + hex(jpgend).rstrip("L").upper())
    # Extract JPG to file
    if (jpgstart > 0):
        rawjpgoutput = filestring[jpgstart:jpgend]
        print("Raw output len: "+str(len(rawjpgoutput)))
        # filename = input imgcache filename + JPG start hex offset + decimal UNIX timestamp string + human readable timestamp in UTC
        if ("video" in pathname):
            outputfilename = filename + "_vid_" + hex(jpgstart).rstrip("L").upper() + "_" + str(timestamp) + "_" + timestring + ".jpg"
        else:
            outputfilename = filename + "_pic_" + hex(jpgstart).rstrip("L").upper() + "_" + str(timestamp) + "_" + timestring + ".jpg"
        try:
            outputjpg = open(outputfilename, "wb")
        except:
            print("Trouble Opening JPEG Output File: ", outputfilename)
            exit(-1)
        print(outputfilename) 
        print("JPG output size(bytes) = " + str(len(rawjpgoutput)) + " from offset = " + hex(jpgstart).rstrip("L").upper() + "\n")
        outputjpg.write(rawjpgoutput)
        outputjpg.close()

        # Calculate MD5 of picture file we just wrote
        md5hash = ""
        with open(outputfilename, 'rb') as pic:
            md5 = hashlib.md5()
            md5.update(pic.read()) # file shouldn't be that large so just read into memory
            md5hash = md5.hexdigest().upper()

        # pic size
        picsize = os.stat(outputfilename).st_size

        # store filename, size, item path string and MD5 tuple in output HTML table dictionary
        outputdict[jpgstart] = (outputfilename, str(picsize), pathname, md5hash)
# End of hits loop
fb.close()

# Write output HTML table
try:
    outputHTML = open(htmlfile, "w")
except:
    print("Trouble Opening HTML Output File: ", outputHTML)
    exit(-1)

# HTML table header
outputHTML.write("<html><table border=\"3\" style=\"width:100%\"><tr>" + \
                 "<th>Extracted JPG Filename</th><th>Filesize(bytes)</th>" + \
                 "<th>Item Path String</th><th>MD5 Hash</th><th>Extracted Picture</th></tr>")

# sort dict by key (ie JPG file offset)
orderedkeys = outputdict.keys()
# orderedkeys.sort()
                 
for key in orderedkeys:
    filename, size, itempath, md5 = outputdict[key]
    outputHTML.write("<tr><td>" + filename + "</td><td>" + size + "</td><td>" + \
                     itempath + "</td><td>" + md5 + "</td>" + \
                     "<td><img src=\"" + filename + "\"></img><td></tr>")
outputHTML.write("</table></html>")
outputHTML.close()

print("Processed " + str(len(outputdict.keys())) + " cached pictures. Exiting ...\n")

