#!/bin/sh

pid=`ps -ef | grep com.uinnova.di.diconsole.ESDiconsoleApplication|grep -v grep|awk '{print $2}'`
if [ ${#pid} -ne 0 ]; then
   echo diconsole ..............has Started! ${pid}
else
   echo diconsole ..............has Stopped!
fi

