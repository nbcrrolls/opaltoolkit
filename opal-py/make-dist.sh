#!/bin/sh

VERSION=1.9alpha

echo "Creating a temporary directory for the tarball"
rm -rf opal-py-$VERSION
mkdir opal-py-$VERSION

# copy dummy client, README, LICENSE, XSD, and WSDL into the temp directory
echo "Copying sources into temporary directory"
cp -r Pdb2pqrClient.py LICENSE README docs etc wsdl opal-py-$VERSION/
rm -rf `find opal-py-$VERSION -name ".svn"`

# create a tarball
echo "Creating a tarball"
tar zcvf opal-py-$VERSION.tar.gz opal-py-$VERSION/*

# delete the temporary directory
echo "Cleaning up"
rm -rf opal-py-$VERSION