import sys
import time
import httplib
import os
import getopt
import OpalClient

def usage():
    print ""
    print "Usage: python GenericServiceClient.py"
    print "-l <url>                   service url"
    print "-a <args>                  command line arguments"
    print "-b/-f <attch1,attch2,..>   local input files as binary attachments"
    print "-j <job_id>                job id for a run"
    print "-n <num_procs>             number of processors for parallel job"
    print "-r <operation>             remote operation to invoke:"
    print "                           [getAppMetadata|launchJob|queryStatus|getOutputs]"
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


appname = os.path.basename(opt_url)
client = OpalClient.OpalService(opt_url)

	
# get application metadata and print usage
if opt_req == "getAppMetadata":
    print "Getting Application Metadata"
    resp = client.getServiceMetadata()
    print "Usage:", resp._usage

# launch remote Opal job
elif opt_req == "launchJob" or opt_req == "launchJobBlocking":

    argList = opt_arg

    if opt_num != None:
        numProcs = int(opt_num)
    else:
        numProcs = None
        
    inputFiles = opt_att + opt_ifu


    
    if opt_req == "launchJob":
        print "Launching remote " + appname + " job"
        jobStatus = client.launchJobNB(argList, inputFiles, numProcs)
        print "Received Job ID:", jobStatus.getJobId()
        print "Base Output URL:", jobStatus.getBaseURL()
    elif opt_req == "launchJobBlocking":
        print "Launching blocking " + appname + " job"
        jobStatus = client.launchJobBlocking(argList, inputFiles, numProcs)
        print "Status: ", jobStatus.getStatus() , " - ", jobStatus.getError()
        print "Base Output URL:", jobStatus.getBaseURL()

        # List job outputs, if execution is successful
        if jobStatus.isSuccessful() : 
            print "\tStandard Output:", jobStatus.getURLstdout(), "\n", \
                  "\tStandard Error:", jobStatus.getURLstderr()
            for i in jobStatus.getOutputFiles():
                print "\t" + i

# query status for non blocking job
elif opt_req == "queryStatus":
    jobID = opt_jid

    if jobID == "":
        print" ERROR: jobID must be specified with \"-j\" for queryStatus"
        sys.exit(0)
        
    status = OpalClient.JobStatus(client, jobID) 

    print appname + " status:"
    print "\tCode:", status.getStatus()
    print "\tMessage:", status.getError()
    print "\tOutput Base URL:", status.getBaseURL()

# get output metadata for finished job
elif opt_req == "getOutputs":
    jobID = opt_jid

    if jobID == None or jobID == "":
        print" ERROR: jobID must be specified with \"-j\" for getOutputs"
        sys.exit(0)

    print "Retrieving " + appname + " status"
    jobStatus = OpalClient.JobStatus(client, jobID)
    
    # Retrieve job outputs, if execution is successful
    if jobStatus.isSuccessful(): # 8 = GramJob.STATUS_DONE
        print "Job " + appname + " " + jobID + " execution terminated successfully"

    elif jobStatus.isRunning():
        print "Job " + appname + " " + jobID + " is still running"
    else:
        ##Error
        print "Job " + appname + " " + jobID + " is failed"
    print "\tCode:", jobStatus.getStatus()
    print "\tMessage:", jobStatus.getError()
    print "\tOutput Base URL:", jobStatus.getBaseURL()
    # Retrieve a listing of all output files
    print "\tStandard Output:", jobStatus.getURLstdout(), "\n", \
          "\tStandard Error:", jobStatus.getURLstderr()
    for i in jobStatus.getOutputFiles():
        print "\t" + i

# unsupported operation
else:
    print "ERROR: Unsupported argument for -r"
    usage()

