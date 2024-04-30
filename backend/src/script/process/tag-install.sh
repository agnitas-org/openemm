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
tagnames() {
	python3 -c 'import sys, os, re
from contextlib import suppress

tagnames = ["agn" + (os.path.basename (sys.argv[1]).split (".")[0]).upper ()]

with suppress (IOError), open (sys.argv[1]) as fd:
	pattern = re.compile ("install:(.*);$")
	for line in fd:
		if (mtch := pattern.search (line)) is not None:
			new_tagnames = [f"agn{_f.upper ()}:{_f}" for _f in mtch.group (1).replace (",", " ").split ()]
			if new_tagnames:
				tagnames = new_tagnames
			break
print (" ".join (f"-t {_t}" for _t in tagnames))
' "$1"
}
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
		$cmd `tagnames $script` $script
		if [ $? -ne 0 ]; then
			log "ERROR: Failed to install $script"
			irc=1
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
