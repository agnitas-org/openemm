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
active once
#
cd $HOME
#
once=$HOME/scripts/once
stat=$HOME/var/run/once.stat
#
case "$1" in
start)
	rm -f $stat
	if [ -d $once ]; then
		count=0
		files=""
		for fname in $once/* ; do
			if [ -f $fname ]; then
				ok="False"
				if [ -x $fname ]; then
					log "INFO: Starting $fname"
					$fname
					rc=$?
					if [ $rc -eq 0 ]; then
						log "INFO: $fname ended successful, removing it"
						ok="True"
					else
						log "ERROR: $fname returns $rc keep it"
					fi
				else
					log "INFO: Skip non executable file $fname"
				fi
				if [ "$ok" = "True" ]; then
					rm -f $fname
				else
					count=`expr $count + 1`
					if [ "$files" ]; then
						files="$files $fname"
					else
						files="$fname"
					fi
				fi
			fi
		done
		if [ $count -eq 0 ]; then
			log "INFO: $once is empty, removing it"
			rmdir $once
			rc=$?
			if [ $rc -ne 0 ]; then
				log "ERROR: Failed to remove $once with code $rc"
			fi
		else
			echo "incomplete $count files remaining: $files" > $stat
		fi
	fi
	;;
status)
	if [ -f $stat ]; then
		cat $stat
	else
		echo "ok"
	fi
	;;
*)
	echo "Usage: $0 [ start | status ]"
	exit 1
	;;
esac

				
