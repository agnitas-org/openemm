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
ctrlfile="$HOME/var/run/sendmail-control.sh"
qdir=/var/spool/mqueue
sdir=/var/spool/slowqueue
#
case "$1" in
setup)
	active mlcontrol
	$HOME/bin/smctrl service stop
	#
	(
		echo "#!/bin/sh"
		echo "#"
	) > $ctrlfile
	if [ "$MTA" = "sendmail" ]; then
		if [ -d $sdir ]; then
			echo "$HOME/bin/smctrl stop $sdir" >> $ctrlfile
		fi
	fi
	(
		echo "$HOME/bin/smctrl service stop"
		echo "sleep 2"
		echo "$HOME/bin/smctrl service start"
	) >> $ctrlfile
	chmod 750 $ctrlfile
	;;
teardown)
	rm -f $ctrlfile
	;;
start)
	active mlcontrol
	messagen "Delay .. "
	sleep 2
	message "done."
	$HOME/bin/smctrl service start
	if [ "$MTA" = "sendmail" ]; then
		if [ -d $qdir ] && [ -d $sdir ] ; then
			count="`ps laxwww | grep sendmail | grep $sdir | grep -v grep | wc -l`"
			while [ $count -lt 2 ] ; do
				messagen "Sendmail instance .. "
				pidf="-OPidFile=/var/run/sendmail-`basename $sdir`-${count}.pid"
				$HOME/bin/smctrl $SENDMAIL_DSN_OPT -OQueueDirectory=$sdir $pidf -q60m
				echo "$HOME/bin/smctrl $SENDMAIL_DSN_OPT -OQueueDirectory=$sdir $pidf -q60m" >> $ctrlfile
				count=`expr $count + 1`
				message "$count started."
			done
			if [ -x $HOME/bin/qctrl ]; then
				messagen "Starting qctrl .. "
				$HOME/bin/qctrl -d 900 -L info move $qdir $sdir 'tries:10'
				message "done."
			fi
		fi
	fi
	;;
stop)
	if [ -x $HOME/bin/qctrl ]; then
		as=root terminator $HOME/bin/qctrl
	fi
	if [ "$MTA" = "sendmail" ]; then
		if [ -d $sdir ]; then
			messagen "Stopping sendmails .. "
			$HOME/bin/smctrl stop $sdir
			message " done."
		fi
	fi
	$HOME/bin/smctrl service stop
	messagen "Delay .. "
	sleep 2
	message "done."
	;;
status)
	if [ "$MTA" = "sendmail" ]; then
		as=- patternstatus 2 sendmail
	else
		as=- patternstatus master
	fi
	;;
*)
	echo "Usage: $0 [ start | stop | status | setup | teardown ]"
	exit 1
	;;
esac
