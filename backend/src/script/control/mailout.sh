#!/bin/sh
####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
#
. $HOME/scripts/config.sh
#
#
#	1.) Configuration section
java="java -Dlog.print=true"
if [ "$LOG_HOME" ] ; then
	java="$java -Dlog.home=$LOG_HOME"
fi
java="$java -Djava.security.egd=file:///dev/urandom"

case "$1" in
start)
	active mailout
	if [ -x $HOME/bin/jsync.sh ]; then
		$HOME/bin/jsync.sh
	fi
	xmlrpc=com.agnitas.backend.MailoutServerXMLRPC
	logfile=${HOME}/log/backend.log
	> $logfile
	#
	#	2.) Start backend
	echo -n "Starting backend .. "
	$HOME/bin/watchdog.sh \
		-imerger \
		-r1800 \
		"-bo$logfile" \
		"-p$HOME/bin/recovery.sh" \
		"-nmemory=mailout java.memory merger.java-memory -Xms1g -Xmx4g" \
		"-nhost=mailout host merger.host *" \
		-- \
		$java '$memory' $xmlrpc '$host'
	echo "done."
	#
	#	3.) Start exception watching process
	if [ -x $HOME/bin/emerg.sh ]; then
		$HOME/bin/emerg.sh start
	fi
	;;
stop)
	if [ ! "`patternstatus 2 \"$java\"`" = "stopped" ]; then
		first="true"
		while true ; do
			$HOME/bin/in-progress.sh
			if [ $? -ne 0 ]; then
				if [ "$first" ]; then
					echo "Looks like at least one mailing is currently in progress,"
					echo "we will wait (and retry) until processing is finished."
					first=""
				fi
				sleep 5
			else
				softterm "$HOME/scripts/watchdog.py -imerger -r1800" "$HOME/scripts/watchdog3.py -imerger -r1800"
				break
			fi
		done
	fi
	if [ -x $HOME/bin/emerg.sh ]; then
		$HOME/bin/emerg.sh stop
	fi
	;;
status)
	patternstatus 2 "$java"
	;;
*)
	echo "Usage: $0 [ start | stop ]"
	exit 1
	;;
esac
