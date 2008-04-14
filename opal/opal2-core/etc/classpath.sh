#!/bin/sh

# if Opal is installed at a standard location, replace OPAL_HOME appropriately
OPAL_HOME=$PWD

# set the classpath
CLASSPATH=`echo $OPAL_HOME/lib/*.jar | tr ' ' ':'`
CLASSPATH=`echo $OPAL_HOME/webapps/opal2/WEB-INF/lib/*.jar | tr ' ' ':'`:$CLASSPATH
CLASSPATH=$OPAL_HOME:$OPAL_HOME/build/classes:$OPAL_HOME/lib:$CLASSPATH
export CLASSPATH
echo $CLASSPATH
