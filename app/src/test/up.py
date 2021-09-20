#!/usr/bin/python

import fileinput
import os
import fnmatch
import sys
import re
import time

matches = []
for root, dirnames, filenames in os.walk("."):
    for filename in fnmatch.filter(filenames, "*.java"):
        matches.append(os.path.join(root, filename))

for filename in matches:
    orig = open(filename).read()

    new = re.sub('PillpopperLog.Say\s*\(\s*String.format\(([^;]+)\)\s*\)\s*;', 'PillpopperLog.Say(\\1);', orig)
    new = re.sub('PillpopperLog.Say\s*\(\s*Locale[^,]*,\s*', 'PillpopperLog.Say(', new)
    
    if orig != new:
        sys.stdout.write("Updating %s\n" % (filename))
        f = open(filename, "w")
        f.write(new)
        f.close()
