#!/bin/sh

cd `dirname $0`
cd ..
export PRO_HOME=`pwd`
echo PRO_HOME=$PRO_HOME


#JAVA_HOME=/dix/jdk1.8.0_172

if [ $JAVA_HOME == "" ]
then 
 echo ">>JAVA_HOME is not set!"
 exit 0
fi

JAVA_EXEC=$JAVA_HOME/bin/java
#echo $JAVA_EXEC
#jars
cd $PRO_HOME/lib

JARS=$JARS:$PRO_HOME/lib/*

#for jarfile in `ls *.jar`
#do
	#echo $jarfile
#	JARS=$JARS:$PRO_HOME/lib/$jarfile
#done

#echo JARS=$JARS

#exit 0

cd $PRO_HOME

#main class
MAIN_CLASS=com.uinnova.di.dicom.JasyptEncryptorMain

$JAVA_EXEC -classpath "$JARS" -DPRO_HOME=$PRO_HOME -Dfile.encoding=utf8 $MAIN_CLASS $1 $2
