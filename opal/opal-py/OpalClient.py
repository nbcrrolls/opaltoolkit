
import sys
import time
import httplib
import os
import getopt

from AppService_client import \
     AppServiceLocator, getAppMetadataRequest, launchJobRequest, \
     queryStatusRequest, getOutputsRequest, \
     launchJobBlockingRequest, getOutputAsBase64ByNameRequest, destroyRequest
from AppService_types import ns0
from ZSI.TC import String




class JobStatus:
    """ This class represent a Opal job status and can be used 
    after launching a job to monitor its execution"""

    def __init__(self, opalService, jobID):
        """ """
        self.opalService = opalService
        self.jobID = jobID
        self.jobStatus = self.opalService.appServicePort.queryStatus(queryStatusRequest(jobID))

    def updateStatus(self):
        """ this function retrive a updated version of the jobStatus 
        from the Opal server """ 
        #import pdb; pdb.set_trace()
        self.jobStatus = self.opalService.appServicePort.queryStatus(queryStatusRequest(self.jobID))


    def getError(self):
        """ It return the error message of the job """
        return self.jobStatus._message

    def getBaseURL(self):
        """ it return the URL that contains all the job outputs """
        return self.jobStatus._baseURL

    def getStatus(self):
        """ it return the numeric representation of the status of the job """
        return self.jobStatus._code

    def getJobId(self):
        """ it return the jobid of this job """
        return self.jobID

    def isRunning(self):
        """ this function returns true is the job is still running false if it finished
        """
        if self.jobStatus._code != 8 and self.jobStatus._code != 4:
            return True
        return False

    def isSuccessful(self):
        """ If the job sucesfully finished its execution,
        this function returns true """
        if self.jobStatus._code == 8:
            return True
        return False

    def destroyJob(self):
        """ it destroies the running jobs """
        req = destroyRequest(self.jobID)
        self.jobStatus = self.opalService.appServicePort.destroy( req )
		



class OpalService:

    def __init__(self, url):
        self.url = url
        appLocator = AppServiceLocator()
        self.appServicePort = appLocator.getAppServicePort(self.url)

    def getServiceMetadata(self):
        """ """
        req = getAppMetadataRequest()
        resp = self.appServicePort.getAppMetadata(req)
        return resp

    def getURL(self):
        return self.url


    def launchJobNB(self, commandline, inFilesPath, numProcs = None):
        """ invoke the execution of the remote scientific application
        using Opal a return right away
        
        @returns: a jobStatus Oject which can be used to monitor its execution"""

        inputFiles = []
        if inFilesPath != None:
            for i in inFilesPath:
                inputFile = ns0.InputFileType_Def('inputFile')
                inputFile._name = os.path.basename(i)
                if self.isOpal2():
                    #use attachment this is opal2 server
                    if os.name == 'dos' or os.name == 'nt':
                        inputFile._attachment = open(i, "rb")
                    else:
                        inputFile._attachment = open(i, "r")
                else:
                    #it's not a opal2 server don't user attachment
                    infile = open(i, "r")
                    inputFile._contents = infile.read()
                    infile.close()
                inputFiles.append(inputFile)

        req = launchJobRequest()
        req._argList = commandline
        req._inputFile = inputFiles
        if numProcs :
            req._numProcs = numProcs
        jobStatus = self.appServicePort.launchJob(req)
        return JobStatus(self, jobStatus._jobID)
    
    def isOpal2(self):
        """ it returns true if this service points to a opal2 server
            false in the other cases
        """
        if self.url.find("/opal2/") != -1:
            return True
        else:
            return False

    def launchJobBlocking(self, commandline, inFilesPath, numProcs = None):
        """     """
        jobStatus = self.launchJobNB(commandline, inFilesPath, numProcs)
        while jobStatus.isRunning() :
            time.sleep(30)
            jobStatus.updateStatus()
        #ok job is finished
        return jobStatus



#        # List job outputs, if execution is successful
#        if resp._status._code == 8: # 8 = GramJob.STATUS_DONE
#            out = resp._jobOut
#
#            print "\tStandard Output:", out._stdOut, "\n", \
#                  "\tStandard Error:", out._stdErr
#            if (out._outputFile != None):
#                for i in range(0, out._outputFile.__len__()):
#                    print "\t" + out._outputFile[i]._name, ":", out._outputFile[i]._url




