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
installer() {
	if [ $# -eq 2 ]; then
		opts="$1"
		shift
	else
		opts=""
	fi
	cmd="script-tag $opts"
	script="$1"
	irc=0
	case "$script" in
	*.lua)
		luatc "$script"
		if [ $? -ne 0 ]; then
			log "ERROR: Failed to validate $script"
			irc=1
		fi
		;;
	esac
	if [ $irc -eq 0 ]; then
		tname="agn`basename $script | cut -d. -f1 | tr a-z A-Z`"
		$cmd -t "$tname" "$script"
		if [ $? -ne 0 ]; then
			log "ERROR: Failed to install $script"
			irc=1
		else
			if [ "`basename $script`" = "filter.lua" ]; then
				for func in `awk '/function t_/ { print $2 }' $script | cut -d_ -f2`; do
					ufunc="`echo $func | tr a-z A-Z`"
					$cmd -Tt agn${ufunc}:t_$func $fname
					if [ $? -ne 0 ]; then
						log "ERROR: Failed to install tag agn$ufunc for $script"
						irc=1
					fi
				done
			fi
		fi
	fi
	return $irc
}
#
rc=0
tags="`dirname $0`"/tags
if [ -d $tags ]; then
	count=0
	for fname in $tags/* ; do
		if [ -f $fname ]; then
			installer "$fname"
			if [ $? -eq 0 ]; then
				log "INFO: $fname had been installed successfully, removing it"
				rm "$fname"
			else
				count=`expr $count + 1`
				log "ERROR: Failed to install $fname"
			fi
		elif [ -d $fname ]; then
			cid="`basename $fname`"
			ccount=0
			for cname in $fname/* ; do
				if [ -f $cname ]; then
					installer "-c $cid" "$cname"
					if [ $? -eq 0 ]; then
						log "INFO: $cname had been installed successfully for company $cid, removing it"
						rm "$cname"
					else
						ccount=`expr $ccount + 1`
						log "ERROR: Failed to install $cname for company $cid"
					fi
				fi
			done
			if [ $ccount -eq 0 ] && rmdir $fname ; then
				log "INFO: Removing empty directory $fname"
			else
				count=`expr $count + 1`
				log "ERROR: Keeping non empty directory $fname"
			fi
		fi
	done
	if [ $count -eq 0 ] && rmdir $tags ; then
		log "INFO: Removing empty directory $tags"
	else
		log "INFO: Keeping non empty directory $tags"
		rc=1
	fi
fi
exit $rc
