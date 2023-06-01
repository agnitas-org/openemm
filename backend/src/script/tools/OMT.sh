#!/bin/bash
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

scriptDir=$(dirname "`readlink -f \"${0}\"`")

osVendor="Unknown"
if [ -f /etc/os-release ]; then {
	if grep -q "SLES" "/etc/os-release" || grep -q "Suse" "/etc/os-release"; then {
	  osVendor="Suse"
	} fi
} fi
if [ -f /etc/centos-release ]; then {
	  osVendor="CentOS"
} fi

echo "Checking python installation ..."
if test -e "/home/openemm/opt/python3/bin/python3"; then {
	PYTHON="/home/openemm/opt/python3/bin/python3"
} elif test -e "/home/openemm/opt/python/bin/python3"; then {
	PYTHON="/home/openemm/opt/python/bin/python3"
} elif test -e "/home/openemm/opt/python/bin/python"; then {
	PYTHON="/home/openemm/opt/python/bin/python"
} elif test -e "/usr/bin/python3"; then {
	PYTHON="/usr/bin/python3"
} else {
	PYTHON="python3"
} fi
pythonavailable="$(${PYTHON} -c 'print("OK")' 2>&1)"
if [ ! $? -eq 0 ]; then {
	tput setaf 1 # set text output to red
	echo "Mandatory Python 3 runtime is missing!"
	tput sgr0 # reset text color
	if [ "${osVendor}" == "Suse" ]; then {
		echo "Commands to install python runtime:"
		echo "zypper install -y python python-xml python3-pip python-gdbm"
		echo "pip3 install pydns xlrd xlwt xlutils paramiko pyspf ipaddr dnspython pydkim pycrypto requests httpie setproctitle inotify"
		exit 1
	} else {
		echo "Command to install python runtime: 'sudo yum -y install python'"
	
		#discard randomly earlier pressed keys by user
		read -t 1 -n 100 discard
	
		read -p "Install python runtime now? Y/n " yn
		answer=no
		case ${yn} in
			[YyJj]* ) answer=yes;;
		esac
	
		if [ "${answer}" = "yes" ]; then {
			sudo yum -y install python
	
			pythonavailable="$(${PYTHON} -c 'print("OK")' 2>&1)"
			if [ ! $? -eq 0 ]; then {
				tput setaf 1 # set text output to red
				echo "Mandatory python runtime is still missing!"
				tput sgr0 # reset text color
				exit 1
			} fi
		} else {
			tput setaf 1 # set text output to red
			echo "Mandatory python runtime is still missing!"
			tput sgr0 # reset text color
			exit 1
		} fi
	} fi
} fi

pythonversion="$(${PYTHON} -c 'import sys; print(".".join(map(str, sys.version_info[:3])))' 2>&1)"
if [ $? -eq 0 ]; then {
	echo "Python version found: ${pythonversion} (${PYTHON})"
} else {
	tput setaf 1 # set text output to red
	echo "Mandatory python runtime is missing!"
	tput sgr0 # reset text color
} fi

if [[ ! ${pythonversion} == 3.* ]]; then {
	tput setaf 1 # set text output to red
	echo "ERROR: Python version must be 3.x (3.8.1+) for Backend to operate"
	tput sgr0 # reset text color
	exit 1
} fi

echo "Checking Zip installation ..."
which zip > /dev/null 2>&1
if [ ! $? -eq 0 ]; then {
	tput setaf 1 # set text output to red
	echo "Mandatory Zip os module is missing!"
	tput sgr0 # reset text color
	if [ "${osVendor}" == "Suse" ]; then {
		echo "Command to install Zip:"
		echo "zypper install -y zip"
		exit 1
	} else {
		echo "Commands to install Zip:"
		echo "	sudo yum -y install zip"
	
		#discard randomly earlier pressed keys by user
		read -t 1 -n 100 discard
	
		read -p "Install Zip now? Y/n " yn
		answer=no
		case ${yn} in
			[YyJj]* ) answer=yes;;
		esac
	
		if [ "${answer}" = "yes" ]; then {
			sudo yum -y install zip
	
			which zip > /dev/null 2>&1
			if [ ! $? -eq 0 ]; then {
				tput setaf 1 # set text output to red
				echo "Mandatory Zip os module is still missing!"
				tput sgr0 # reset text color
				exit 1
			} else {
				zipVersionText=$(zip -v | grep "This is Zip ")
				if [[ ${zipVersionText} =~ ^.*([0-9]+\.[0-9]+).*$ ]]; then {
					echo "Zip version ${BASH_REMATCH[1]} found"
				} fi
			} fi
		} else {
			tput setaf 1 # set text output to red
			echo "Mandatory Zip os module still missing!"
			tput sgr0 # reset text color
			
			#discard randomly earlier pressed keys by user
			read -t 1 -n 100 discard
		
			read -p "Start anyway (cannot send zipped logfiles via email)? y/N " yn
			answer=no
			case ${yn} in
				[YyJj]* ) answer=yes;;
			esac
			
			if [ ! "${answer}" = "yes" ]; then {
				exit 1
			} fi
		} fi
	} fi
} else {
	zipVersionText=$(zip -v | grep "This is Zip ")
	if [[ ${zipVersionText} =~ ^.*([0-9]+\.[0-9]+).*$ ]]; then {
		echo "Zip version ${BASH_REMATCH[1]} found"
	} fi
} fi

echo "Starting python ..."
${PYTHON} "${scriptDir}/../scripts/OMT.py" $@
