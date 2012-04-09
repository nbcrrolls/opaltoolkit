#!/usr/bin/python
#this example invoke APBS service on ws.nbcr.net and it uses SOAP attachments 
#to upload input files on the server

import sys
import time
import httplib
import OpalClient


# Service URL
apbsURL = "http://kryptonite.nbcr.net/opal2/services/apbs_1.3"
client = OpalClient.OpalService(apbsURL)

# command-line arguments
argList = "apbs.in"
inputFile = ["etc/apbs.in", "etc/ion.xml"]

print "Launching remote Apbs job"
jobStatus = client.launchJobNB(argList, inputFile)
while jobStatus.isRunning() :
    time.sleep(3)
    print "Polling job status"
    jobStatus.updateStatus()
    
if jobStatus.isSuccessful():
    print "Job execution finished sucessfully."
else:
    print "Job execution failed."
    print "Check the stdout.txt and the stderr.txt for errors...."

print "Job URL: ", jobStatus.getBaseURL()
# Retrieve a listing of all output files
print "\tStandard Output:", jobStatus.getURLstdout(), "\n", \
  "\tStandard Error:", jobStatus.getURLstderr()

files = jobStatus.getOutputFiles()
for i in files: 
    print "\t" + i
