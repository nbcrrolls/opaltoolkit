import sys
import time
import httplib
import os
import getopt

from AppService_client import \
     AppServiceLocator, getAppMetadataRequest, launchJobRequest, \
     queryStatusRequest, getOutputsRequest, \
     launchJobBlockingRequest, getOutputAsBase64ByNameRequest
from AppService_types import ns0
from ZSI.TC import String

try:
#    opts, args = getopt.getopt(sys.argv[1:], "l:r:a:j:b:f:", ["help", "output="])
    opts, args = getopt.getopt(sys.argv[1:], "l:r:a:j:b:f:")
except getopt.GetoptError, err:
    print str("Option not recognized")
    sys.exit(2)

if opts == []:
    print "USAGE: Please visit https://www.nbcr.net/pub/wiki/index.php?title=Opal_Client for documentation"
    sys.exit(0)

opt_url = ""
opt_req = ""
opt_arg = ""
opt_jid = ""
opt_att = []

for o, a in opts:
    if o == "-l":
        opt_url = a
    elif o == "-r":
        opt_req = a
    elif o == "-a":
        opt_arg = a
    elif o == "-j":
        opt_jid = a
    elif o == "-b" or o == "-f":
        a = a.split(',')
        for i in a:
            opt_att.append(i.strip('"').strip(' '))

##### add stuff to check if url etc was entered

# Set the protocol to http or https
# proto = "http"
proto = opt_url.split(':')[0]

# Ignore these values if you are not using security
# Otherwise, set the locations for the X509 certificate and key
#cert = "/Users/sriramkrishnan/certs/apbs_service.cert.pem"
#key = "/Users/sriramkrishnan/certs/apbs_service.privkey"

# If you are using a proxy cert, set both the cert 
# and key to the location of the proxy
# Proxies have to be RFC 3820 compliant (use grid-proxy-init -rfc)
# cert = "/tmp/x509up_u506"
# key = "/tmp/x509up_u506"

# Host and port for remote services
# baseURL = proto + "://ws.nbcr.net/"
url_host = opt_url.split("://")[1].split("/")[0] + '/'
baseURL = proto + "://" + url_host

# Retrieve a reference to the AppServicePort
appLocator = AppServiceLocator()

url_service = opt_url.split("://")[1].split(url_host)[1]

if proto == "http":
     appServicePort = appLocator.getAppServicePort(baseURL + url_service)
else:
    if proto == "https":
	# example of ssl invocation
	#appServicePort = appLocator.getAppServicePort(
	#    baseURL + "opal2/services/Pdb2pqrOpalService",
	#    ssl=1,
	#    transdict=dict(cert_file=cert, key_file=key),
	#    transport=httplib.HTTPSConnection)
        appServicePort = appLocator.getAppServicePort(
	    baseURL + url_service,
	    ssl=1,
	    transdict=dict(cert_file=cert, key_file=key),
	    transport=httplib.HTTPSConnection)
    else:
	print "Unknown protocol: ", proto
	sys.exit(1)
	
appname = os.path.basename(url_service)

if opt_req == "getAppMetadata":
    print "Getting Application Metadata"
    req = getAppMetadataRequest()
    resp = appServicePort.getAppMetadata(req)
    print "Usage:", resp._usage
elif opt_req == "launchJob":
    req = launchJobRequest()
    req._argList = opt_arg
    inputFiles = []
    inputFile_arg = opt_att

    for i in inputFile_arg:
        inputFile = ns0.InputFileType_Def('inputFile')
        inputFile._name = os.path.basename(i)
        inputFile._attachment = open(i, "r")
        inputFiles.append(inputFile)

    req._inputFile = inputFiles

    # Launch job, and retrieve job ID
    print "Launching remote " + appname + " job"
    resp = appServicePort.launchJob(req)
    jobID = resp._jobID
    print "Received Job ID:", jobID
elif opt_req == "launchJobBlocking":
    req = launchJobBlockingRequest()
    req._argList = opt_arg
    inputFiles = []
    inputFile_arg = opt_att

    for i in inputFile_arg:
        inputFile = ns0.InputFileType_Def('inputFile')
        inputFile._name = os.path.basename(i)
        inputFile._attachment = open(i, "r")
        inputFiles.append(inputFile)

    req._inputFile = inputFiles

    # Launch a blocking job
    print "Launching blocking " + appname + " job"
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
elif opt_req == "queryStatus":
    jobID = opt_jid

    if jobID == "":
        print" ERROR: jobID must be specified with \"-j\" for queryStatus"
        sys.exit(0)
        
    status = appServicePort.queryStatus(queryStatusRequest(jobID))

    print appname + " status:"
    print "\tCode:", status._code
    print "\tMessage:", status._message
    print "\tOutput Base URL:", status._baseURL
elif opt_req == "getOutputs":
    jobID = opt_jid

    if jobID == "":
        print" ERROR: jobID must be specified with \"-j\" for queryStatus"
        sys.exit(0)

    status = appServicePort.queryStatus(queryStatusRequest(jobID))
    
    # Retrieve job outputs, if execution is successful
    if status._code == 8: # 8 = GramJob.STATUS_DONE
        print "Retrieving " + appname + " output metadata: "
        resp = appServicePort.getOutputs(getOutputsRequest(jobID))

        # Retrieve a listing of all output files
        print "\tStandard Output:", resp._stdOut, "\n", \
              "\tStandard Error:", resp._stdErr
        if (resp._outputFile != None):
            for i in range(0, resp._outputFile.__len__()):
                print "\t" + resp._outputFile[i]._name, ":", resp._outputFile[i]._url
    else:
        print "Error: " + appname + " " + jobID + " failed"
        print "\tCode:", status._code
        print "\tMessage:", status._message
        print "\tOutput Base URL:", status._baseURL
        sys.exit(0)
elif opt_req == "getOutputAsBase64ByName":
    jobID = jid

    if jobID == "":
        print" ERROR: jobID must be specified with \"-j\" for getOutputAsBase64ByName"
        sys.exit(0)

    print "Downloading " + appname + " output: "
    req = getOutputAsBase64ByNameRequest()
    req._jobID = jobID
    req._fileName = "sample.pqr"
    resp = appServicePort.getOutputAsBase64ByName(req)
    print resp
else:
    print "ERROR: Unsupported argument for -r"
    sys.exit(0)

