#!/bin/bash

libdir=/home/jren/registry/lib
files=`ls $libdir`
cp=".:"

for i in $files; do
  cp=$cp:$libdir/$i
done

echo $cp