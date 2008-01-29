#!/usr/bin/perl

use OpalServices;
use OpalTypes;

$location = "http://localhost:8080/opal/services/BabelServicePort";
$pdbfile = "./etc/benzene.pdb";

$babel = OpalServices->new(service_url => $location);

$result = $babel->getAppMetadata();
print "App Metadata:\n";
print "\tUsage: ",$result->getUsage(),"\n";
print "\tInfo:\n";
print $result->getInfo(),"\n";

$result = $babel->getAppConfig();
print "\nApp Config:\n";
print "\tBinary Location: ",$result->getBinaryLocation(),"\n";
print "\tDefault Args: ",$result->getDefaultArgs(),"\n";

open MYFILE, $pdbfile;
my $file = do {local $/; <MYFILE>};
close MYFILE;

$inputFile = InputFileType->new("benzene.pdb",$file);
$req = JobInputType->new();
$req->setArgs("-ipdb benzene.pdb -h -opdb output.pdb");
$req->setInputFile($inputFile);

$result = $babel->launchJob($req);
print "\nJob Launched:\n";
print "\tCode: ",$result->getCode(),"\n";
print "\tMessage: ",$result->getMessage(),"\n";

$statuscode = 0;
$jobid = $result->getJobID();
while ($statuscode != 8) {
  $status = $babel->queryStatus($jobid);
  $statuscode=$status->getCode();
  print "Query Status:\n";
  print "\tCode: ",$status->getCode(),"\n";
  print "\tMessage: ",$status->getMessage(),"\n";
  sleep(5);
}

$output = $babel->getOutputs($jobid);
@list = $output->getFiles();
print "\nList of output files:\n";
foreach (@list) {
  print "\t",$_->getName(),": ",$_->getURL(),"\n";
}

print "\nDownloading output file - output.pdb:\n";
$req = OutputsByNameInputType->new($jobid,"output.pdb");
$result = $babel->getOutputAsBase64ByName($req);
print $result;
