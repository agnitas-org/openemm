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
case "$1" in
start)
	active bav
	shift
	starter $HOME/bin/watchdog.sh -bo- -- $HOME/bin/bav -L INFO -l "$@"
	;;
stop)
	softterm "$HOME/scripts/watchdog.py -bo- -- $HOME/bin/bav" "$HOME/scripts/watchdog3.py -bo- -- $HOME/bin/bav"
	;;
status)
	patternstatus 2 "$HOME/bin/bav -L"
	;;
*)
	echo "Usage: $0 [ start | stop ]"
	exit 1
	;;
esac
