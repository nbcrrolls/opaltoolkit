#!/bin/sh

VERSION=1.9.2

echo "Creating a temporary directory for the tarball"
rm -rf opal-perl-$VERSION
mkdir opal-perl-$VERSION

# copy dummy client, README, LICENSE, XSD, and WSDL into the temp directory
echo "Copying sources into temporary directory"
cp -r pdb2pqrclient.pl LICENSE README docs etc wsdl OpalServices.pm OpalTypes.pm opal-perl-$VERSION/
rm -rf `find opal-perl-$VERSION -name ".svn"`

# create a tarball
echo "Creating a tarball"
tar zcvf opal-perl-$VERSION.tar.gz opal-perl-$VERSION/*

# delete the temporary directory
echo "Cleaning up"
rm -rf opal-perl-$VERSION
