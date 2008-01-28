#!/bin/bash
#
# LC this file create the html and pdf of the docbook
#
#  0.1 first version


VERSION=1.9.2
# where sources file are located
SOURCEDIR=src/docbook
DSLFILE=$SOURCEDIR/etc
# where the output will be placed
DESTDIR=target/site/docs/$VERSION



#
# check the dest directory
if [ ! -d $DESTDIR ] ;
then 
    mkdir -p $DESTDIR
fi

echo Generating the html version....
jw --backend html --dsl $DSLFILE/rocks.dsl\#html --output $DESTDIR $SOURCEDIR/index.sgml

echo Generating the pdf version....
jw --backend pdf --dsl $DSLFILE/rocks.dsl\#print --output $DESTDIR $SOURCEDIR/index.sgml


# finalizing the work...
mv $DESTDIR/index.pdf $DESTDIR/opal-referenceguide.pdf
mkdir -p $DESTDIR/images
cp $SOURCEDIR/images/* $DESTDIR/images
cp $SOURCEDIR/rocks.css  $DESTDIR


