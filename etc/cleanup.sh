#!/bin/sh

CATALINA_HOME=/home/apbs_user/Software/jakarta-tomcat-5.0.30
echo "Using CATALINA_HOME:" $CATALINA_HOME

# number of days to keep old scratch directories
n=3
echo "Keeping working directories for" $n "days"

# clean up old directories
echo "Cleaning up old working directories"
cd $CATALINA_HOME/webapps/ROOT
dirs=`find . -type d -mtime +$n -print | egrep "app" | wc -l`
echo "Number of old directories to delete:" $dirs
rm -rf `find . -type d -mtime +$n -print | egrep "app"`
echo "Done"
