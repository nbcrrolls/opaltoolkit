#!/usr/bin/perl

use OpalServices;
use OpalTypes;
use File::Basename;

# Set the location of our service as well as the name of the pdb file to input
# A list of NBCR services can be found at http://ws.nbcr.net:8080/opal/servlet/AxisServlet
$location = "http://ws.nbcr.net:8080/opal/services/Pdb2pqrOpalService";
$pdbfile = "./etc/sample.pdb";

# Instantiate a new service object to interact with.  Pass it the service location
$pdb2pqr = OpalServices->new(service_url => $location);

# Make a remote invocation using the service object to retrieve the application metadata
$result = $pdb2pqr->getAppMetadata();
print "App Metadata:\n";
print "\tUsage: ",$result->getUsage(),"\n";
print "\tInfo:\n";
print $result->getInfo(),"\n";

# Similar invocation for getting the application configuration
# $result = $pdb2pqr->getAppConfig();
# print "\nApp Config:\n";
# print "\tBinary Location: ",$result->getBinaryLocation(),"\n";
# print "\tDefault Args: ",$result->getDefaultArgs(),"\n";

# Open the pdb file and read all of it into a string
open MYFILE, $pdbfile;
my $file = do {local $/; <MYFILE>};
close MYFILE;
# Parse to retrieve just the file name.  Create new input file object.  Create new job input object.
#  Set job input commandline arguments, attach input file.
$filename = fileparse($pdbfile);
$inputFile = InputFileType->new($filename,$file);
$req = JobInputType->new();
$commandline = "--ff=amber ".$filename." sample.pqr";
$req->setArgs($commandline);
$req->setInputFile($inputFile);

# Launch job and retrieve job ID
$result = $pdb2pqr->launchJob($req);
print "Job Launched:\n";
print "\tCode: ",$result->getCode(),"\n";
print "\tMessage: ",$result->getMessage(),"\n";
print "\tBase URL: ",$result->getBaseURL(),"\n";

# Loop until job GRAM status is 8 (Finished)
$statuscode = 0;
$jobid = $result->getJobID();
while ($statuscode != 8) {
  $status = $pdb2pqr->queryStatus($jobid);
  $statuscode=$status->getCode();
  print "Query Status:\n";
  print "\tCode: ",$status->getCode(),"\n";
  print "\tMessage: ",$status->getMessage(),"\n";
  print "\tBase URL: ",$status->getBaseURL(),"\n";
  sleep(5);
}
# Get list of job output files
$output = $pdb2pqr->getOutputs($jobid);
@list = $output->getFiles();
print "\nList of output files:\n";
print "\tStandard Output: ",$output->getStdOut(),"\n";
print "\tStandard Error: ",$output->getStdErr(),"\n";
foreach (@list) {
  print "\t",$_->getName(),": ",$_->getURL(),"\n";
}

# Retrieve output file as a Base64 encoded binary
# print "\nDownloading output file - sample.pqr: ";
# $req = OutputsByNameInputType->new($jobid,"sample.pqr");
# $result = $pdb2pqr->getOutputAsBase64ByName($req);
# $outfile = "sample.pqr";
# open OUTFILE, ">sample.pqr";
# print OUTFILE $result;
# close OUTFILE;
# print "Done.\n";
