#!/bin/sh

java -Djavax.net.ssl.trustStore=$HOME/.keystore org.renci.ws.AutodockBasicAsyncClient $*