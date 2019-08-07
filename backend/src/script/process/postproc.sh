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
rc=0
postproc="`dirname $0`"/postproc
if [ -d $postproc ]; then
	for fname in $postproc/* ; do
		if [ -x "$fname" ]; then
			"$fname"
			if [ $? -eq 0 ]; then
				log "INFO: $fname had been executed successfully, removing it"
				rm "$fname"
			else
				log "ERROR: Failed to execute $fname"
			fi
		fi
	done
	count=0
	for fname in $postproc/* ; do
		if [ -f "$fname" ] || [ -d "$fname" ]; then
			log "WARNING: $fname still here"
			count=`expr $count + 1`
		fi
	done
	if [ $count -eq 0 ] && rmdir $postproc ; then
		log "INFO: Removing empty directory $postproc"
	else
		log "INFO: Keeping non empty directory $postproc"
		rc=1
	fi
fi
exit $rc
