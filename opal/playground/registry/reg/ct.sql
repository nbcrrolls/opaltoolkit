CREATE DATABASE OpalServices; 

USE OpalServices;

CREATE TABLE `Registry` (
  `name` varchar(256),
  `url` varchar(512),
  `host` varchar(128),
  `numCpuTotal` int(8),
  `numCpuFree` int(8),
  `numJobsRunning` int(8),
  `numJobsQueued` int(8),	
  PRIMARY KEY  (`url`)
)