@echo off

:: author YGQ

color 0A
title encryptorProcess

set JAVA_HOME=%JAVA_HOME%
:: echo %JAVA_HOME%
cd ..
set LIB_JARS=.\lib\*
set LOG=log4j.properties
set MAIN_CLASS=com.uinnova.di.dicom.JasyptEncryptorMain

:: start dism
java -Xms64m -Xmx1024m -classpath %LIB_JARS% -client %MAIN_CLASS% %1 %2
exit. & pause
