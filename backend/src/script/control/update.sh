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
. $HOME/scripts/config.sh
#
py3select $HOME/scripts/update3.py $HOME/scripts/update.py
case "$1" in
start)
	active update
	shift

	if py3available; then
		starter $command -bw "$@" account mailtrack bounce deliver release
	else
		starter $command "$@" account mailtrack bounce deliver
	fi
	;;
stop)
	softterm $commands
	;;
status)

	if py3available; then
		patternstatus 5 $command
	else
		patternstatus 4 $command
	fi
	;;
*)
	echo "Usage: $0 [ start | stop | status ]"
	exit 1
	;;
esac
