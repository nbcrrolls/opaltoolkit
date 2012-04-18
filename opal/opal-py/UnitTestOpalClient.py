

import unittest
import OpalClient
import time
import urllib
import tempfile


class TestSequenceFunctions(unittest.TestCase):

    def setUp(self):
        dateurl = "http://rocce-vm3.ucsd.edu/opal2/services/date"
        self.dateClient = OpalClient.OpalService(dateurl)
        self.email = "clem@sdsc.edu"
	
    def test_launchJob(self):
        #construct a opalService
        print "  --  Launch date job  --  "
        jobStatus = self.dateClient.launchJobNB("", [], email = self.email)
        while jobStatus.isRunning() :
            time.sleep(3)
            jobStatus.updateStatus()
        #ok job is finished
        self.assertTrue( jobStatus.isSuccessful(), "Submitted job failed!\nUrl:" + jobStatus.getBaseURL() )
        print "job execution finished sucessfully: ", jobStatus.getBaseURL()
        outputURL = jobStatus.getBaseURL() + "/stdout.txt"
        output = urllib.urlopen(outputURL).read()
        self.assertTrue( len(output) < 50, "Output of the date is longer than expected: " + output)
        files = jobStatus.getOutputFiles()
        self.assertTrue(len(files) == 0, "Date job should not produce any output file")
        print "Output file:"
        for i in files:
            print "\t", i
		
    def test_destroy(self):
        print "  --  Launch and destroy  --  "
        jobStatus = self.dateClient.launchJobNB("", [], email = self.email)
        jobStatus.destroyJob()
        self.assertFalse(jobStatus.isSuccessful(), "Destroy job should fail the job")


    def test_apbsClient(self):
        print "  --  Apbs client  --  "
        url = "http://kryptonite.nbcr.net/opal2/services/ApbsOpalService"
        self.apbsClient = OpalClient.OpalService(url)
        argList = "-molecule ion.xml -config apbs.in"
        inputFile = ["etc/apbs.in", "etc/ion.xml"]
        jobStatus = self.apbsClient.launchJobNB(argList, inputFile)
        while jobStatus.isRunning() :
            time.sleep(3)
            jobStatus.updateStatus()
        #ok job is finished
        self.assertTrue( jobStatus.isSuccessful(), "APBS job failed!\nUrl:" + jobStatus.getBaseURL() )
        print "job execution finished sucessfully: ", jobStatus.getBaseURL()
        files = jobStatus.getOutputFiles()
        self.assertTrue(len(files) > 0, "Apbs client should produce several output files")
        print "Output file:"
        for i in files:
            print "\t", i
        tempDir = tempfile.mkdtemp()
        #apbs does not create results.tar.gz
        self.assertFalse( jobStatus.downloadOutput(tempDir), "results.tar.gz should not be present for this job")

    def dtest_vinaScreening(self):
        print "  --  VinaScreening  --  "
        url = "http://rocce-vm3.ucsd.edu/opal2/services/autodockvina_screening_1.1.2"
        self.vinaClient = OpalClient.OpalService(url)
        argList = "--config test.config --receptor 2HTY_A-2HTY_A.pdbqt --ligand_db sample"
        inputFile = ["etc/test.config", "etc/2HTY_A-2HTY_A.pdbqt"]
        jobStatus = self.vinaClient.launchJobNB(argList, inputFile, email = self.email)
        while jobStatus.isRunning() :
            time.sleep(3)
            jobStatus.updateStatus()
        #ok job is finished
        self.assertTrue( jobStatus.isSuccessful(), "Vina job failed!\nUrl:" + jobStatus.getBaseURL() )
        print "job execution finished sucessfully: ", jobStatus.getBaseURL()
        files = jobStatus.getOutputFiles()
        self.assertTrue(len(files) > 0, "Apbs client should produce several output files")
        print "Output file:"
        for i in files:
            print "\t", i
        tempDir = tempfile.mkdtemp()
        print "Downloading at output file at: ", tempDir
        assertTrue(jobStatus.downloadOutput(tempDir) , "Unable to download results.tar.gz from the server!!")



    def dtest_vinaScreeningKill(self):
        print "  --  VinaScreening  --  "
        url = "http://rocce-vm3.ucsd.edu/opal2/services/autodockvina_screening_1.1.2"
        self.vinaClient = OpalClient.OpalService(url)
        argList = "--config test.config --receptor 2HTY_A-2HTY_A.pdbqt --ligand_db sample"
        inputFile = ["etc/test.config", "etc/2HTY_A-2HTY_A.pdbqt"]
        jobStatus = self.vinaClient.launchJobNB(argList, inputFile, email = self.email)
        self.assertTrue( jobStatus.isRunning() )
        time.sleep(20)
        killTime = time.time()
        jobStatus.destroyJob()
        jobStatus.updateStatus()
        while jobStatus.isRunning() :
            time.sleep(3)
            jobStatus.updateStatus()
        self.assertFalse(jobStatus.isSuccessful(), "job is supposed to fail but it didn't")
        print "Killing job required: ", str( time.time() - killTime )
        print "Killed job base URL: ", jobStatus.getBaseURL()


if __name__ == '__main__':
    unittest.main()


