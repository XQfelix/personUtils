@echo off

:: author YGQ

color 0A
title DiconsoleRunningProcess,Please do not close Window
:: Close the execution window
::if "%1" == "h" goto begin 
::mshta vbscript:createobject("wscript.shell").run("""%~nx0"" h",0)(window.close)&&exit 
:::begin 

:: Check diconsole process
set MAIN_CLASS=com.uinnova.di.diconsole.DBDiconsoleApplication
set PARAMTER=--spring.profiles.active=diconsole
set File=%TEMP%\sthUnique.tmp
:: Query dip processIds
wmic process where (Name="java.exe" AND CommandLine LIKE "%%%MAIN_CLASS%%%") get ProcessId /value | find "ProcessId" >%File%
set /P _string=<%File%
set _pid=%_string:~10%

if defined _string (
	taskkill /pid %_pid% -t -f >nul
	echo close last running %_pids%
)

set JAVA_HOME=%JAVA_HOME%
:: echo %JAVA_HOME%
cd ..
set LIB_JARS=.\lib\*
set LOG=log4j.properties

:: start diconsole
java -Dfile.encoding=UTF-8 -Xms256M -Xmx1024M -XX:PermSize=256M -XX:MaxPermSize=512M -classpath .\conf;%LIB_JARS% -client %MAIN_CLASS% %PARAMTER%
exit. & pause
