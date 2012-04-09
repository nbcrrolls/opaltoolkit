

import unittest
import OpalClient
import time
import urllib



class TestSequenceFunctions(unittest.TestCase):

    def setUp(self):
        dateurl = "http://rocce-vm3.ucsd.edu/opal2/services/date"
        self.dateClient = OpalClient.OpalService(dateurl)
        #sleepurl = "http://rocce-vm3.ucsd.edu/opal2/services/sleep"
        #self.sleepClient = OpalClient.OpalService(sleepurl)

	
    def test_launchJob(self):
        #construct a opalService
        #invoke its execution
        jobStatus = self.dateClient.launchJobNB("",[])
        while jobStatus.isRunning() :
            time.sleep(3)
            jobStatus.updateStatus()
        #ok job is finished
        self.assertTrue( jobStatus.isSuccessful(), "Submitted job failed!\nUrl:" + self.dateClient.getURL() )
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
        print "Launch and destroy"
        jobStatus = self.dateClient.launchJobNB("",[])
        jobStatus.destroyJob()
        self.assertFalse(  jobStatus.isSuccessful(), "Destroy job should fail the job")





if __name__ == '__main__':
    unittest.main()


