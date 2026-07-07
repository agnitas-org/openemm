#!/bin/bash

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
python311="`which python3.11 2>/dev/null`"
if [ "$python311" ]; then {
  PYTHON="$python311"
} elif test -e "/home/openemm/opt/python3/bin/python3"; then {
	PYTHON="/home/openemm/opt/python3/bin/python3"
} elif test -e "/home/openemm/opt/python/bin/python3"; then {
	PYTHON="/home/openemm/opt/python/bin/python3"
} elif test -e "/home/openemm/opt/python/bin/python"; then {
	PYTHON="/home/openemm/opt/python/bin/python"
} elif test -e "/opt/agnitas.com/software/python3"; then {
	PYTHON="/opt/agnitas.com/software/python3/bin/python3"
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
		echo "   zypper install -y python python-xml python3-pip python-gdbm"
		echo "   pip3 install py3dns dnspython dkimpy cryptography requests setproctitle aiodns aiohttp aiohttp-xmlrpc aiosmtplib msgpack websockets asyncinotify asyncssh"
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
		
		echo "OpenEMM needs additional python modules"
		echo "Command to install python modules:"
		echo "   pip3 install py3dns dnspython dkimpy cryptography requests setproctitle aiodns aiohttp aiohttp-xmlrpc aiosmtplib msgpack websockets asyncinotify asyncssh"
		read -p "Install python modules now? Y/n " yn
		answer=no
		case ${yn} in
			[YyJj]* ) answer=yes;;
		esac

		if [ "${answer}" = "yes" ]; then {
			pip3 install py3dns dnspython dkimpy cryptography requests setproctitle aiodns aiohttp aiohttp-xmlrpc aiosmtplib msgpack websockets asyncinotify asyncssh
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
python_home="$(dirname "$(dirname "${PYTHON}")")"
export PYTHONHOME=${python_home}
if test -e "${scriptDir}/../scripts/OST.py"; then {
	${PYTHON} "${scriptDir}/../scripts/OST.py" "$@"
} else {
	${PYTHON} "${scriptDir}/OST.py" "$@"
} fi
