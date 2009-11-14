#!/bin/bash

dpfZipFile=$1
mapZipFile=$2

echo $dpfZipFile $mapZipFile

wget -O dpfFiles.zip $dpfZipFile
wget -O mapFiles.zip  $mapZipFile

export CLIENT_HOME="/home/sriram/Software/autodock-ws-client"

# set the classpath
CLASSPATH=`echo $CLIENT_HOME/axis2/lib/*.jar | tr ' ' ':'`
CLASSPATH=$CLIENT_HOME/target/classes:$CLASSPATH
export CLASSPATH

java -Djavax.net.ssl.trustStore=$HOME/.keystore org.renci.ws.AutodockAdvancedAsynchClient srikrish <passwd> dpfFiles.zip mapFiles.zip .
