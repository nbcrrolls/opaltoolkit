import sys
import time
import httplib

from AppService_client import \
     AppServiceLocator, getAppMetadataRequest, launchJobRequest, \
     launchJobBlockingRequest, getOutputAsBase64ByNameRequest, \
     queryStatusRequest, getOutputsRequest
from AppService_types import ns0
from ZSI.TC import String

# Set the protocol to http or https
proto = "https"

# Ignore these values if you are not using security
# set the locations for the X509 certificate and key
#cert = "/Users/sriramkrishnan/certs/apbs_service.cert.pem"
#key = "/Users/sriramkrishnan/certs/apbs_service.privkey"

# If you are using a proxy cert, set both the cert 
# and key to the location of the proxy
# Proxies have to be RFC 3820 compliant (use grid-proxy-init -rfc)
cert = "/tmp/x509up_u1000"
key = "/tmp/x509up_u1000"

# Host and port for remote services
baseURL = proto + "://rocks-171.sdsc.edu:8443/"
#baseURL = proto + "://localhost:8080/"

# Retrieve a reference to the AppServicePort
appLocator = AppServiceLocator()
if proto == "http":
    appServicePort = appLocator.getAppServicePort(baseURL + "opal2/services/BabelServicePort")
else:
    if proto == "https":
	# example of ssl invocation
	appServicePort = appLocator.getAppServicePort(
	    baseURL + "opal2/services/cat",
	    ssl=1,
        transdict={'cert_file': cert, 'key_file': key},
	    transport=httplib.HTTPSConnection)
        print "setting cert to: " + cert
    else:
	print "Unknown protocol: ", proto
	sys.exit(1)
	
# Make remote invocation to get application metadata
print "Getting Application Metadata"
req = getAppMetadataRequest()
resp = appServicePort.getAppMetadata(req)
print "Usage:", resp._usage

# Set up remote job launch
req = launchJobRequest()
req._argList = "-ipdb benzene.pdb -h -opdb output.pdb"
inputFiles = []
inputFile = ns0.InputFileType_Def('inputFile')
inputFile._name ='benzene.pdb' 
sampleFile = open("./etc/benzene.pdb", "r")
sampleFileString = sampleFile.read()
sampleFile.close()
inputFile._contents = sampleFileString
inputFiles.append(inputFile)
req._inputFile = inputFiles

# Launch job, and retrieve job ID
print "Launching remote Babel job"
resp = appServicePort.launchJob(req)
jobID = resp._jobID
print "Received Job ID:", jobID

# Poll for job status
status = resp._status
print "Polling job status"
while 1:
    # print current status
    print "Status:"
    print "\tCode:", status._code
    print "\tMessage:", status._message
    print "\tOutput Base URL:", status._baseURL

    if (status._code == 8) or (status._code == 4): # STATUS_DONE || STATUS_FAILED
        break

    # Sleep for 30 seconds
    print "Waiting 30 seconds"
    time.sleep(30)
    
    # Query job status
    status = appServicePort.queryStatus(queryStatusRequest(jobID))

# Retrieve job outputs, if execution is successful
if status._code == 8: # 8 = GramJob.STATUS_DONE
    print "Retrieving Babel output metadata: "
    resp = appServicePort.getOutputs(getOutputsRequest(jobID))

    # Retrieve a listing of all output files
    print "\tStandard Output:", resp._stdOut, "\n", \
	  "\tStandard Error:", resp._stdErr
    if (resp._outputFile != None):
	for i in range(0, resp._outputFile.__len__()):
	    print "\t" + resp._outputFile[i]._name, ":", resp._outputFile[i]._url


    # Retrieve an output file as a Base64 encoded binary
    print "Downloading Babel output: "
    req = getOutputAsBase64ByNameRequest()
    req._jobID = jobID
    req._fileName = "stdout.txt"
    resp = appServicePort.getOutputAsBase64ByName(req)
    print resp

# Set up blocking job launch
req = launchJobBlockingRequest()
req._argList = "-ipdb benzene.pdb -h -opdb output.pdb"
inputFiles = []
inputFile = ns0.InputFileType_Def('inputFile')
inputFile._name = 'benzene.pdb'
sampleFile = open("./etc/benzene.pdb", "r")
sampleFileString = sampleFile.read()
sampleFile.close()
inputFile._contents = sampleFileString
inputFiles.append(inputFile)
req._inputFile = inputFiles

# Launch a blocking job
print "Launching blocking Babel job"
resp = appServicePort.launchJobBlocking(req)
print "Status:", resp._status._code, "-", resp._status._message
print "Base Output URL:", resp._status._baseURL

# List job outputs, if execution is successful
if resp._status._code == 8: # 8 = GramJob.STATUS_DONE
    out = resp._jobOut

    print "\tStandard Output:", out._stdOut, "\n", \
	  "\tStandard Error:", out._stdErr
    if (out._outputFile != None):
	for i in range(0, out._outputFile.__len__()):
	    print "\t" + out._outputFile[i]._name, ":", out._outputFile[i]._url
