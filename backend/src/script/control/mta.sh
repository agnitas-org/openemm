#!/bin/sh
####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
#
. $HOME/scripts/config.sh
#
sm="$BASE/bin/smctrl"
if [ "$MTA" = "postfix" ]; then
	case "$1" in
	start|stop)
		mstart "Restart postfix: "
		$sm service restart
		mend "done"
		;;
	status)
		as=- patternstatus master
		;;
	*)
		echo "Usage: $0 [ start | stop | status ]"
		exit 1
		;;
	esac
else
	case "$1" in
	start)
		mstart "Stopping obsolete sendmail processes: "
		$sm stop
		mend "done"
		#
		mstart "Starting sendmails: "
		mproceed "listener"
		run="/var/run"
		for i in sendmail mta; do
			if [ -d "$run/$i" ]; then
				run="$run/$i"
				break
			fi
		done
		$sm service start
		mproceed "mail queues"
		for stage in 1 2 3 4; do
			case "$stage" in
			1)	queue="ADMIN0"
				param="-q1m -OTimeout.iconnect=20s -OTimeout.connect=15s"
				count="2"
				;;
			2)	queue="QUEUE"
				param="-q1m -OTimeout.iconnect=20s -OTimeout.connect=15s"
				count="2"
				;;
			3)	queue="MIDQUEUE"
				param="-q30m"
				count="1"
				;;
			4)	queue="SLOWQUEUE"
				param="-q90m"
				count="1"
				;;
			esac
			mproceed "$queue"
			path="$BASE/var/spool/$queue"
			if [ ! -d $path ]; then
				mkdir -p $path
			fi
			n=0
			while [ $n -lt $count ]; do
				n=`expr $n + 1`
				$sm $param -NNEVER "-OQueueDirectory=$path" "-OPidFile=$run/sendmail-openemm-`echo $queue | tr A-Z a-z`-${n}.pid"
			done
		done
		$HOME/bin/qctrl -d780   move $BASE/var/spool/QUEUE $BASE/var/spool/MIDQUEUE tries:3
		$HOME/bin/qctrl -d3240  move $BASE/var/spool/MIDQUEUE $BASE/var/spool/SLOWQUEUE tries:10
		$HOME/bin/qctrl -d20880 force move $BASE/var/spool/SLOWQUEUE /dev/null maxage:6d
		mend "done"
		;;
	stop)
		as=root terminator $HOME/bin/qctrl
		mstart "Stop all sendmail processes: "
		$sm service stop
		$sm stop
		mend "done"
		;;
	status)
		as=root patternstatus 6 "sendmail.*$HOME/"
		as=root patternstatus 3 "$HOME/bin/qctrl"
		;;
	*)
		echo "Usage: $0 [ start | stop ]"
		exit 1
		;;
	esac
fi
