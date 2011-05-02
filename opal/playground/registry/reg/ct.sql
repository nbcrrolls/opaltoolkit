USE Emp;

CREATE TABLE `HOSTS` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(250) default NULL,
  `numCpuTotal` int(8),
  `numCpuFree` int(8),
  `numJobsRunning` int(8),
  `numJobsQueued` int(8),	
  `services` varchar(65536),
  PRIMARY KEY  (`id`)
)