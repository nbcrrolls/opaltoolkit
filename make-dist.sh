#!/bin/sh

VERSION=2.0.0

echo "Creating a temporary directory for the tarball"
rm -rf opal-py-$VERSION
mkdir opal-py-$VERSION

# copy dummy client, README, LICENSE, XSD, and WSDL into the temp directory
echo "Copying sources into temporary directory"
cp -r Pdb2pqrClient.py ApbsClient.py AppService_* zsi-2.1_a2.tar.gz LICENSE README CHANGELOG docs etc wsdl opal-py-$VERSION/
rm -rf `find opal-py-$VERSION -name ".svn"`

# create a tarball
echo "Creating a tarball"
tar zcvf opal-py-$VERSION.tar.gz opal-py-$VERSION/*

# delete the temporary directory
echo "Cleaning up"
rm -rf opal-py-$VERSION
