#!/bin/sh

# if Opal is installed at a standard location, replace OPAL_HOME appropriately
export AXIS2_HOME=`cat build.properties | grep "axis2.home" | sed 's/axis2.home=//'`

# set the classpath
CLASSPATH=`echo $AXIS2_HOME/lib/*.jar | tr ' ' ':'`
CLASSPATH=$PWD/target/classes:$CLASSPATH
export CLASSPATH
echo $CLASSPATH
