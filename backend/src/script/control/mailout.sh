#!/bin/sh
####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
#	-*- sh -*-
#
. $HOME/scripts/config.sh
#
#
#	1.) Configuration section
java="java -Xms256m -Xmx1024m"
if [ "$LOG_HOME" ] ; then
	java="$java -Dlog.home=$LOG_HOME"
fi
java="$java -Djava.security.egd=file:///dev/urandom"

case "$1" in
start)
	active mailout

	$HOME/scripts/jsync.py
	base=${HOME}/JAVA
	inifile=-Dorg.agnitas.backend.ini_filename=${base}/Mailout.ini
	xmlrpc=org.agnitas.backend.MailoutServerXMLRPC
	logfile=${base}/org/agnitas/backend/BACKEND_`date +%Y%m%d`.LOG
	touch $logfile
	wd="$HOME/bin/watchdog.sh -imerger -r1800 -p$HOME/bin/recovery.sh"
	#
	#	2.) Start backend
	echo -n "Starting backend .. "
	( (
		$wd -- $java $inifile $xmlrpc '*' &
	) >> $logfile 2>&1 & )
	echo "done."
	slogfile=${HOME}/log/backend.log
	rm -f $slogfile
	ln $logfile $slogfile
	;;
stop)
	first="true"
	while true ; do
		$HOME/scripts/in-progress.py
		if [ $? -ne 0 ]; then
			if [ "$first" ]; then
				echo "Looks like at least one mailing is currently in progress,"
				echo "we will wait (and retry) until processing is finished."
				first=""
				svcout "waiting for finishing mail generation .. "
			fi
			sleep 5
		else
			softterm "$HOME/scripts/watchdog.py -imerger -r1800"
			break
		fi
	done
	;;
status)
	patternstatus 2 "$java"
	;;
*)
	echo "Usage: $0 [ start | stop ]"
	exit 1
	;;
esac
