@echo off

:: author YGQ

color 0A
title DiconsoleStopProcess,Please do not close Window

:: Close the execution window
::if "%1" == "h" goto begin 
::mshta vbscript:createobject("wscript.shell").run("""%~nx0"" h",0)(window.close)&&exit 
:::begin 

set MAIN_CLASS=com.uinnova.di.diconsole.DBDiconsoleApplication

:: Query dip processId
set File=%TEMP%\sthUnique.tmp
wmic process where (Name="java.exe" AND CommandLine LIKE "%%%MAIN_CLASS%%%") get ProcessId /value | find "ProcessId" >%File%
set /P _string=<%File%
set _pid=%_string:~10%

if defined _string (
	taskkill /pid %_pid% -t -f >nul
	echo Kill process by force %_pid%
)

exit. & pause 
