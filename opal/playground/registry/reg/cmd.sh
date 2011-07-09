#cp=`./cp.sh`

libdir=/home/jren/opaltoolkit/trunk/opal/playground/registry/lib
files=`ls $libdir`
cp=".:"

for i in $files; do
  cp=$cp:$libdir/$i
done

javac -cp $cp *.java
java -cp $cp Registry
#java -cp $cp RegistryClient
