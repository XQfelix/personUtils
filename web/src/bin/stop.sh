#!/bin/sh
cd `dirname $0`

LOG=com.web.WebApplication

export PRO_PID=`ps -ef | grep $LOG|grep -v grep|awk '{print $2}'`
echo $PRO_PID

if [ -n "$PRO_PID" ]; then
   kill -9 $PRO_PID
fi

sleep 2

./DBScan.sh
