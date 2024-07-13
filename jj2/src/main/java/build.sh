#!/bin/bash -x
shopt -s globstar
lib="/usr/lib64/jvm/java"
inc="-I${lib}/include"
rpath="${lib}/lib/server"
name="libCompiler.so"
exe="ejavac"
#name="task.so"
main="main.cpp"
source="token.cpp"
#source="task.cpp"

if [[ ! -e "$exe" ]]; then
g++ -v -w -Ofast -D library -fPIC -Wall -Wl,-rpath,"${rpath}" -o main{,.cpp} "${inc}"{,/linux} "-L${rpath}" -ljvm

if [ $? -ne 0 ]
then
  exit $?
fi
fi

if [[ ! -e "$name" ]]; then
g++ -v -w -Ofast -D library -fPIC -shared\
 -Wall -o $name $source\
 "${inc}"{,/linux}

if [ $? -ne 0 ]
then
  exit $?
fi
fi

for i in com/ejavac/*.java
do
javac "$i"
if [ $? -ne 0 ]
then
  exit $?
fi
done

jar cvfm ejavac.jar META-INF/MANIFEST.MF com $name org
if [ $? -ne 0 ]
then
  exit $?
fi

for i in com/ejavac/*.class
do
 rm -f $i
done

