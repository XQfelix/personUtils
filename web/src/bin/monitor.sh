#!/bin/sh

#crontab -e   */10 * * * * /mydata/visio_server/blackmirror.visio-0.0.1-SNAPSHOT/bin/monitor.sh

VISIO_HOME=/mydata/visio_server/blackmirror.visio-0.0.1-SNAPSHOT
cd $VISIO_HOME/bin
   
pid=`ps -ef | grep diconsole-0.0.1-SNAPSHOT.jar|grep -v grep|grep -v 'monitor.sh'|awk '{print $2}'`
if [ ${#pid} -ne 0 ]; then
   echo diconsole ..............has Started! ${pid}
else
   ./start.sh
fi
