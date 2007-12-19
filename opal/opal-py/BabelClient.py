import sys
import time
import httplib

from AppService_services import \
     AppServiceLocator, getAppMetadataRequestWrapper, launchJobRequestWrapper, \
     launchJobBlockingRequestWrapper, getOutputAsBase64ByNameRequestWrapper
from AppService_services_types import ns1
from ZSI.TC import String

# Set the protocol to http or https
proto = "http"

# Ignore these values if you are not using security
# set the locations for the X509 certificate and key
cert = "/Users/sriramkrishnan/certs/apbs_service.cert.pem"
key = "/Users/sriramkrishnan/certs/apbs_service.privkey"

# If you are using a proxy cert, set both the cert 
# and key to the location of the proxy
# Proxies have to be RFC 3820 compliant (use grid-proxy-init -rfc)
# cert = "/tmp/x509up_u506"
# key = "/tmp/x509up_u506"

# Host and port for remote services
# baseURL = proto + "://rocks-106.sdsc.edu:8080/"
baseURL = proto + "://localhost:8080/"

# Retrieve a reference to the AppServicePort
appLocator = AppServiceLocator()
if proto == "http":
    appServicePort = appLocator.getAppServicePortType(
	baseURL + "opal/services/BabelServicePort")
else:
    if proto == "https":
	# example of ssl invocation
	appServicePort = appLocator.getAppServicePortType(
	    baseURL + "opal/services/BabelServicePort",
	    ssl=1,
	    cert_file=cert,
	    key_file=key,
	    transport=httplib.HTTPSConnection)
    else:
	print "Unknown protocol: ", proto
	sys.exit(1)
	
# Make remote invocation to get application metadata
print "Getting Application Metadata"
req = getAppMetadataRequestWrapper()
resp = appServicePort.getAppMetadata(req)
print "Usage:", resp.Get_usage()

# Set up remote job launch
req = launchJobRequestWrapper()
req.Set_argList("-ipdb benzene.pdb -h -opdb output.pdb")
inputFiles = []
inputFile = ns1.InputFileType_Def()
inputFile.Set_name('benzene.pdb')
sampleFile = open("./etc/benzene.pdb", "r")
sampleFileString = sampleFile.read()
sampleFile.close()
inputFile.Set_contents(sampleFileString)
inputFiles.append(inputFile)
req.Set_inputFile(inputFiles)

# Launch job, and retrieve job ID
print "Launching remote Babel job"
resp = appServicePort.launchJob(req)
jobID = resp.Get_jobID()
print "Received Job ID:", jobID

# Poll for job status
status = resp.Get_status()
print "Polling job status"
while 1:
    # print current status
    print "Status:"
    print "\tCode:", status.Get_code()
    print "\tMessage:", status.Get_message()
    print "\tOutput Base URL:", status.Get_baseURL()

    if (status.Get_code() == 8) or (status.Get_code() == 4): # STATUS_DONE || STATUS_FAILED
        break

    # Sleep for 30 seconds
    print "Waiting 30 seconds"
    time.sleep(30)
    
    # Query job status
    status = appServicePort.queryStatus(jobID)

# Retrieve job outputs, if execution is successful
if status.Get_code() == 8: # 8 = GramJob.STATUS_DONE
    print "Retrieving Babel output metadata: "
    resp = appServicePort.getOutputs(jobID)

    # Retrieve a listing of all output files
    print "\tStandard Output:", resp.Get_stdOut(), "\n", \
	  "\tStandard Error:", resp.Get_stdErr()
    if (resp.Get_outputFile() != None):
	for i in range(0, resp.Get_outputFile().__len__()):
	    print "\t" + resp.Get_outputFile()[i].Get_name(), ":", resp.Get_outputFile()[i].Get_url()


    # Retrieve an output file as a Base64 encoded binary
    print "Downloading Babel output: "
    req = getOutputAsBase64ByNameRequestWrapper()
    req.Set_jobID(jobID)
    req.Set_fileName("output.pdb")
    resp = appServicePort.getOutputAsBase64ByName(req)
    print resp

# Set up blocking job launch
req = launchJobBlockingRequestWrapper()
req.Set_argList("-ipdb benzene.pdb -h -opdb output.pdb")
inputFiles = []
inputFile = ns1.InputFileType_Def()
inputFile.Set_name('benzene.pdb')
sampleFile = open("./etc/benzene.pdb", "r")
sampleFileString = sampleFile.read()
sampleFile.close()
inputFile.Set_contents(sampleFileString)
inputFiles.append(inputFile)
req.Set_inputFile(inputFiles)

# Launch a blocking job
print "Launching blocking Babel job"
resp = appServicePort.launchJobBlocking(req)
print "Status:", resp.Get_status().Get_code(), "-", resp.Get_status().Get_message()
print "Base Output URL:", resp.Get_status().Get_baseURL()

# List job outputs, if execution is successful
if resp.Get_status().Get_code() == 8: # 8 = GramJob.STATUS_DONE
    out = resp.Get_jobOut()

    print "\tStandard Output:", out.Get_stdOut(), "\n", \
	  "\tStandard Error:", out.Get_stdErr()
    if (out.Get_outputFile() != None):
	for i in range(0, out.Get_outputFile().__len__()):
	    print "\t" + out.Get_outputFile()[i].Get_name(), ":", out.Get_outputFile()[i].Get_url()
