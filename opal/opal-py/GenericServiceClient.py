import sys
import time
import httplib
import os
import getopt

from AppService_client import \
     AppServiceLocator, getAppMetadataRequest, launchJobRequest, \
     queryStatusRequest, getOutputsRequest, \
     launchJobBlockingRequest, getOutputAsBase64ByNameRequest, \
     destroyRequest
from AppService_types import ns0
from ZSI.TC import String

def usage():
    print ""
    print "Usage: python GenericServiceClient.py"
    print "-l <url>                   service url"
    print "-a <args>                  command line arguments"
    print "-b/-f <attch1,attch2,..>   local input files as a binary attachment"
    print "-j <job_id>                job id for a run"
    print "-n <num_procs>             number of processors for parallel job"
    print "-r <operation>             remote operation to invoke:"
    print "                           [getAppMetadata|launchJob|queryStatus|getOutputs|destroy]"
    print "-u <url1,url2,..>          input file urls"
    sys.exit(0)

try:
#    opts, args = getopt.getopt(sys.argv[1:], "l:r:a:j:b:f:", ["help", "output="])
    opts, args = getopt.getopt(sys.argv[1:], "l:r:a:j:b:f:n:u:")
except getopt.GetoptError, err:
    print "ERROR: Unsupported option"
    usage()

if opts == []:
    print "ERROR: no option was used"
    usage()

opt_url = ""
opt_req = ""
opt_jid = ""
opt_arg = None
opt_num = None
opt_att = []  # input file attachments
opt_ifu = []  # input file urls

for o, a in opts:
    if o == "-l":
        opt_url = a
    elif o == "-r":
        opt_req = a
    elif o == "-a":
        opt_arg = a
    elif o == "-j":
        opt_jid = a
    elif o == "-n":
        opt_num = a
    elif o == "-u":
        a = a.split(',')
        for i in a:
            opt_ifu.append(i.strip('"').strip(' '))
    elif o == "-b" or o == "-f":
        a = a.split(',')
        for i in a:
            opt_att.append(i.strip('"').strip(' '))

if opt_req == "":
    print "ERROR: Missing option -r"
    usage

##### add stuff to check if url etc was entered

# Ignore these values if you are not using security
# Otherwise, set the locations for the X509 certificate and key
#cert = "/Users/sriramkrishnan/certs/apbs_service.cert.pem"
#key = "/Users/sriramkrishnan/certs/apbs_service.privkey"

# If you are using a proxy cert, set both the cert 
# and key to the location of the proxy
# Proxies have to be RFC 3820 compliant (use grid-proxy-init -rfc)
# cert = "/tmp/x509up_u506"
# key = "/tmp/x509up_u506"

try:
    # Set the protocol to http or https
    proto = opt_url.split(':')[0]
    url_host = opt_url.split("://")[1].split("/")[0] + '/'
    baseURL = proto + "://" + url_host
except:
    print "ERROR: Invalid Opal service URL"
    usage()

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
elif opt_req == "launchJob" or opt_req == "launchJobBlocking":
    req = launchJobRequest()
    req._argList = opt_arg

    if opt_num != None:
        req._numProcs = int(opt_num)
        
    inputFiles = []
    inputFile_arg = opt_att

    for i in inputFile_arg:
        inputFile = ns0.InputFileType_Def('inputFile')
        inputFile._name = os.path.basename(i)
        inputFile._attachment = open(i, "r")
        inputFiles.append(inputFile)

    for i in opt_ifu:
        inputFile = ns0.InputFileType_Def('inputFile')
        inputFile._name = os.path.basename(i)
        inputFile._location = i
        inputFiles.append(inputFile)

    req._inputFile = inputFiles
    
    if opt_req == "launchJob":
        print "Launching remote " + appname + " job"
        resp = appServicePort.launchJob(req)
    elif opt_req == "launchJobBlocking":
        print "Launching blocking " + appname + " job"
        resp = appServicePort.launchJobBlocking(req)        
    
    jobID = resp._jobID
    print "Received Job ID:", jobID
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
elif opt_req == "destroy":
    jobID = opt_jid 

    if jobID == "":
        print" ERROR: jobID must be specified with \"-j\" for queryStatus"
        sys.exit(0)

    print "Destroying remote " + appname + " job"

    resp = appServicePort.destroy(destroyRequest(jobID))
    status = appServicePort.queryStatus(queryStatusRequest(jobID))

    print appname + " final status:"
    print "\tCode:", status._code
    print "\tMessage:", status._message
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
        print "Error: " + appname + " " + jobID + " failed or not completed"
        print "\tCode:", status._code
        print "\tMessage:", status._message
        print "\tOutput Base URL:", status._baseURL
else:
    print "ERROR: Unsupported argument for -r"
    usage()

