#!/usr/bin/python2

import sys, commands, os

#insert here the location of you autodock executable 
AUTODOCK = '''/home/install/usr/apps/autodock-4.0.1/bin/autodock4 '''
#insert here the location of this script
CURRENTBIN = '''/home/apbs_user/Software/opal-bins/run_autodock.py ''';
CURL = '''/usr/bin/wget -r -nH --cut-dirs=1 '''

cmd = '''ulimit -s unlimited ; ''' + AUTODOCK
cmds = []
links = []
garbage = []

for thing in sys.argv:
        if thing.startswith("http"):
                links.append(thing)
        elif thing.startswith(CURRENTBIN):
                garbage.append(thing)
        else:
                cmds.append(thing)

for url in links:
        wCmd = CURL + url
        #print 'getting link ' + wCmd
        stat,out = commands.getstatusoutput(wCmd)

if ( os.path.exists("index.html") ):
    os.remove("index.html")

for arg in cmds:
        cmd+=arg + ''' '''

#print 'running cmd ' + cmd

status,output = commands.getstatusoutput(cmd)

print output
