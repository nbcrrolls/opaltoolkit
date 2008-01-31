#!/usr/bin/perl

use OpalServices;
use OpalTypes;
use File::Basename;

$location = "http://ws.nbcr.net:8080/opal/services/Pdb2pqrOpalService";
$pdbfile = "./etc/sample.pdb";

$pdb2pqr = OpalServices->new(service_url => $location);

$result = $pdb2pqr->getAppMetadata();
print "App Metadata:\n";
print "\tUsage: ",$result->getUsage(),"\n";
print "\tInfo:\n";
print $result->getInfo(),"\n";

$result = $pdb2pqr->getAppConfig();
print "\nApp Config:\n";
print "\tBinary Location: ",$result->getBinaryLocation(),"\n";
print "\tDefault Args: ",$result->getDefaultArgs(),"\n";

open MYFILE, $pdbfile;
my $file = do {local $/; <MYFILE>};
close MYFILE;
$filename = fileparse($pdbfile);
$inputFile = InputFileType->new($filename,$file);
$req = JobInputType->new();
$commandline = "--ff=amber ".$filename." sample.pqr";
$req->setArgs($commandline);
$req->setInputFile($inputFile);

$result = $pdb2pqr->launchJob($req);
print "\nJob Launched:\n";
print "\tCode: ",$result->getCode(),"\n";
print "\tMessage: ",$result->getMessage(),"\n";

$statuscode = 0;
$jobid = $result->getJobID();
while ($statuscode != 8) {
  $status = $pdb2pqr->queryStatus($jobid);
  $statuscode=$status->getCode();
  print "Query Status:\n";
  print "\tCode: ",$status->getCode(),"\n";
  print "\tMessage: ",$status->getMessage(),"\n";
  sleep(5);
}

$output = $pdb2pqr->getOutputs($jobid);
@list = $output->getFiles();
print "\nList of output files:\n";
foreach (@list) {
  print "\t",$_->getName(),": ",$_->getURL(),"\n";
}

print "\nDownloading output file - sample.pqr:\n";
$req = OutputsByNameInputType->new($jobid,"sample.pqr");
$result = $pdb2pqr->getOutputAsBase64ByName($req);
print $result;
