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
#
# Global configuration file for shell scripts on all production
# machines, including also several utility functions
#
# A.) Configuration
#
#
# Set the base for the whole system ..
if [ ! "$BASE" ] ; then
	BASE="$HOME"
fi
export BASE
#
#
export	SYSTEM_CONFIG='{
	"trigger-port": "8450",
	"direct-path": "true",
	"direct-path-incoming": "/home/openemm/var/spool/DIRECT",
	"direct-path-archive": "/home/openemm/var/spool/ARCHIVE",
	"direct-path-recover": "/home/openemm/var/spool/RECOVER",
	"direct-path-queues": "/home/openemm/var/spool/QUEUE",
	"licence": "0",
	"dbid": "openemm",
	"merger-address": "127.0.0.1",
	"filter-name": "localhost",
	"mailout-server": "localhost",
	"mailout-port": "8093",
	"direct-path-server": "localhost",
	"direct-path-port": "9403",
	"python-auto-install-modules": "true"
}'
export	DBCFG_PATH="$BASE/etc/dbcfg"
#
#
pathstrip="$BASE/bin/pathstrip"
cq="$BASE/bin/config-query"
for path in $pathstrip $cq; do
	if [ ! -x "$path" ]; then
		die "incomplete installation: missing $path"
	fi
done
#
version="`$cq build.version`"
if [ ! "$version" ]; then
	if [ -f "$BASE/scripts/build.spec" ]; then
		version="`cut '-d;' -f1 $BASE/scripts/build.spec`"
	elif [ -x "$BASE/bin/xmlback" ]; then
		version="`$BASE/bin/xmlback -V | awk '{ print $3 }'`"
	else
		version="current"
	fi
fi
licence="`$cq licence`"
system="`uname -s`"
host="`uname -n | cut -d. -f1`"
optbases="$BASE"
softwarebases="$BASE/opt"
# .. and for java ..
LC_ALL=C
NLS_LANG=american_america.UTF8
export LC_ALL LANG NLS_LANG
if [ ! "$JBASE" ] ; then
	JBASE="$BASE/JAVA"
fi
if [ ! "$JAVAHOME" ] ; then
	for softwarebase in $softwarebases "/usr" "/opt"; do
		java="$softwarebase/java"
		if [ -d $java ] ; then
			for sdk in $java/*sdk* ; do
				if [ -d $sdk ] ; then
					JAVAHOME=$sdk
					break
				fi
			done
			if [ ! "$JAVAHOME" ] ; then
				JAVAHOME=$java
			fi
			break
		fi
	done
fi
if [ "$JAVAHOME" ] ; then
	PATH="$JAVAHOME/bin:$PATH"
	export PATH JAVAHOME
fi
if [ "$JBASE" ] && [ -d $JBASE ] ; then
	cp="$JBASE"
	for jar in $JBASE/*.jar $JBASE/*.zip ; do
		if [ -f $jar ] ; then
			cp="$cp:$jar"
		fi
	done
	if [ "$CLASSPATH" ] ; then
		CLASSPATH="$CLASSPATH:$cp"
	else
		CLASSPATH="$cp"
	fi
fi
# .. and for python3 ..
req="$BASE/scripts/.requirements.txt"
for softwarebase in $softwarebases; do
	path="$softwarebase/python3"
	if [ -d $path/bin ] ; then
		PATH="$path/bin:$PATH"
		break
	fi
done
export PATH
#
# Logging
#
umask 002
if [ "$LOG_HOME" ] ; then
	logpath="$LOG_HOME"
else
	logpath="$BASE/var/log"
fi
loghost="`uname -n | cut -d. -f1`"
logname="`basename -- $0`"
loglast=0
#
# MTA related
#
export MTA="postfix"
sendmail="/usr/sbin/sendmail"
if [ ! -x $sendmail ] ; then
	sendmail="/usr/lib/sendmail"
fi
#
SENDMAIL_DSN="`$cq enable-sendmail-dsn`"
if [ "$SENDMAIL_DSN" = "true" ]; then
	SENDMAIL_DSN_OPT=""
else
	SENDMAIL_DSN_OPT="-NNEVER"
fi
export SENDMAIL_DSN SENDMAIL_DSN_OPT
#
# B.) Routine collection
#
messagen() {
	echo -n "$*"
}
message() {
	echo "$*"
}
error() {
	echo "$*" 1>&2
}
epoch() {
	date '+%s'
}
log() {
	__fname="$logpath/`date +%Y%m%d`-${loghost}-${logname}.log"
	echo "[`date '+%d.%m.%Y  %H:%M:%S'`] $$ $*" >> $__fname
	loglast="`epoch`"
}
mark() {
	if [ $# -eq 1 ] ; then
		__dur=`expr $1 \* 60`
	else
		__dur=3600
	fi
	__now="`epoch`"
	if [ `expr $loglast + $__dur` -lt $__now ] ; then
		log "-- MARK --"
	fi
}
elog() {
	log "$*"
	error "$*"
}
mlog() {
	log "$*"
	message "$*"
}
die() {
	if [ $# -gt 0 ] ; then
		elog "$*"
	fi
	exit 1
}
mstart() {
	messagen "$* "
}
mproceed() {
	if [ $# -eq 0 ] ; then
		messagen "."
	else
		messagen " $* "
	fi
}
mend() {
	message " $*."
}
msleep() {
	if [ $# -ne 1 ] ; then
		__end=1
	else
		__end=$1
	fi
	__cur=0
	while [ $__cur -lt $__end ] ; do
		mproceed
		sleep 1
		__cur=`expr $__cur + 1`
	done
}
#
uid() {
	__uid="`id | tr ' ' '\n' | egrep '^uid=' | tr -cd '[0-9]'`"
	if [ ! "$__uid" ] ; then
		__uid="-1"
	fi
	echo "$__uid"
}
#
onErrorSendMail () {
	__rc=$?
	if [ $__rc -ne 0 ]; then
		mailsend -s "[ERROR] `date +%Y%m%d` $0 [on `uname -n`]" -m "$@" `$cq alert-mail`
	fi
}
#
setupVirtualEnviron() {
	local	python3 minor_minimum candidate
	
	python3="`which python3.11 2>/dev/null`"
	if [ $? -ne 0 ] || [ ! "$python3" ]; then
		python3=""
		minor_minimum="8"
		for candidate in `which -a python3 2>/dev/null`; do
			if [ -x "$candidate" ]; then
				"$candidate" -c "import sys; sys.exit ($minor_minimum > sys.version_info.minor)"
				if [ $? -eq 0 ]; then
					python3="$candidate"
					break
				fi
			fi
		done
		if [ ! "$python3" ]; then
			die "no python3 found with matches at least the minimum version of 3.$minor_minimum"
		fi
	fi
	pyversion="`$python3 -c \"import sys; print ('.'.join (str (_v) for _v in sys.version_info))\"`"
	osversion="`$cq -ec 'return osid'`"
	if [ "$osversion" ]; then
		pyversion="${pyversion}-${osversion}"
	fi
	venv="$BASE/.venv.$pyversion"
	if [ "$application" ]; then
		venv="${venv}-${application}"
	fi
	if [ ! -d "$venv" ]; then
		$python3 -m venv --system-site-packages "$venv"
		if [ -f "$req" ]; then
			rm -f "$req"
		fi
	fi
	if [ ! "$VIRTUAL_ENV" ] || [ ! "$VIRTUAL_ENV" = "$venv" ]; then
		if [ -d "$venv" ]; then
			source "$venv/bin/activate"
		fi
	fi
	[ "$VIRTUAL_ENV" = "$venv" ]
	return $?
}
updateVirtualEnviron() {
	setupVirtualEnviron || return 1
	python3 -m pip install -U pip
	python3 -m pip install -U `python3 -m pip list --format=freeze | cut -d= -f1`
}	
moduleinstalled() {
	python3 -c "
import	sys
try:
	import $@
	sys.exit (0)
except ImportError:
	sys.exit (1)
"
}
require() {
	if [ "$1" = "--update" ] || [ "$1" = "-U" ]; then
		shift
		__update="true"
	else
		__update="false"
	fi
	if [ $# -lt 1 ]; then
		error "Usage: $0 [--update | -U] <module> [<module-filename>]"
		return 1
	fi
	#
	__module="$1"
	__name="$2"
	if [ ! "$__name" ]; then
		__name="$__module"
	fi
	setupVirtualEnviron || return 1
	moduleinstalled "$__module"
	if [ $? -ne 0 ]; then
		python3 -m pip install -U pip
		python3 -m pip install "$__name"
		if [ $? -ne 0 ]; then
			error "Failed to install $__name"
			return 1
		fi
		moduleinstalled "$__module"
		if [ $? -ne 0 ]; then
			error "Module $__module not found, even after installation of ${__name}, see following output for details:"
			python3 -c "import $__module" 1>&2
			return 1
		else
			message "Installed module $__module from $__name"
		fi
	elif [ "$__update" = "true" ]; then
		python3 -m pip install -U "$__module" || return 1
	fi
}
requires() {
	require "$@" || exit 1
}
#
getproc() {
	if [ $# -gt 0 ]; then
		local __pat __psopt __user __pids
		__pat="$1"
		if [ $# -gt 1 ] ; then
			__psopt="$2"
		else
			__psopt="-a"
		fi
		if [ "$as" ]; then
			__user="$as"
		else
			__user="`whoami`"
		fi
		if [ "$__user" = "-" ]; then
			__psopt="-e $__psopt"
		else
			__psopt="-u $__user $__psopt"
		fi
		__pids="`/bin/ps -fww $__psopt | grep -- \"$__pat\" | grep -v grep | awk '{ print $2 }' | grep -v PID`"
		echo $__pids
	else
		echo "Usage: $0 <process-pattern> [<ps-opts>]" 1>&2
	fi
}
#
terminator() {
	while [ $# -gt 0 ] ; do
		local	__pat __run __sig
		__pat="$1"
		shift
		if [ "$__pat" ] ; then
			for __sig in 15 9 ; do
				__run="`getproc \"$__pat\"`"
				if [ "$__run" ] ; then
					messagen "Stop $__pat program with signal $__sig .. "
					kill -$__sig $__run >/dev/null 2>&1
					sleep 2
					message "done."
				fi
			done
		fi
	done
}
#
softterm() {
	while [ $# -gt 0 ] ; do
		local	__pat __repeat __run
		__pat="$1"
		shift
		if [ "$__pat" ] ; then
			for sv in 2 4 6 8 10 ; do
				__repeat="on"
				while [ $__repeat = "on" ]; do
					__repeat="off"
					__run="`getproc \"$__pat\"`"
					if [ "$__run" ] ; then
						messagen "Stop $__pat program  .. "
						kill -15 $__run >/dev/null 2>&1
						sleep 1
						__run="`getproc \"$__pat\"`"
						if [ "$__run" ]; then
							messagen "delaying $sv seconds .. "
							sleep `expr $sv - 1`
							if [ $sv -eq 10 ]; then
								__repeat="on"
							fi
						fi
						message "done."
					fi
				done
			done
		fi
	done
}
#
patternstatus() {
	local	__min __other __count
	if [ $# -gt 1 ]; then
		__min="$1"
		shift
	else
		__min=1
	fi
	if [ $# -gt 1 ]; then
		__other="$1"
		shift
	else
		__other=0
	fi
	__count="`getproc \"$@\" | wc -w`"
	if [ $__count -ge $__min ]; then
		echo "running"
	elif [ $__count -gt $__other ]; then
		echo "incomplete"
	else
		echo "stopped"
	fi
}
#
starter() {
	messagen "Start $* .. "
	(
		nohup "$@" > /dev/null 2>&1 &
	)
	message "done."
}
#
active() {
	cmd="$BASE/bin/activator"
	if [ -x "$cmd" ]; then
		"$cmd" "$@"
		rc=$?
		case "$rc" in
		0)
			;;
		1)
			error "Service $@ is marked as inactive."
			exit 0
			;;
		*)
			error "Service management for $@ failed with $rc, aborting."
			exit $rc
			;;
		esac
	else
		error "No service management installed, $service requires it and will not start without it, aborting."
		exit 1
	fi
}
#
if [ "$LD_LIBRARY_PATH" ] ; then
	LD_LIBRARY_PATH="$BASE/lib:$LD_LIBRARY_PATH"
else
	LD_LIBRARY_PATH="$BASE/lib"
fi
export LD_LIBRARY_PATH
LD_LIBRARY_PATH="`$pathstrip -s \"$LD_LIBRARY_PATH\"`"
export LD_LIBRARY_PATH
#
if [ "$PATH" ] ; then
	PATH="$BASE/bin:$PATH"
else
	PATH="$BASE/bin"
fi
if [ "`uid`" = "0" ] && [ -d "$BASE/sbin" ]; then
	PATH="$BASE/sbin:$PATH"
fi
if [ -d "$BASE/lbin" ]; then
	PATH="$BASE/lbin:$PATH"
fi
PATH="`$pathstrip -s \"$PATH\"`"
export PATH
#
if [ "$CLASSPATH" ] ; then
	CLASSPATH="`$pathstrip -cb \"$CLASSPATH\"`"
	export CLASSPATH
fi
#
if [ "$PYTHONPATH" ] ; then
	PYTHONPATH="$BASE/lib:$PYTHONPATH"
else
	PYTHONPATH="$BASE/lib"
fi
if [ -d "$BASE/plugins" ]; then
	PYTHONPATH="$BASE/plugins:$PYTHONPATH"
fi
PYTHONPATH="$BASE/scripts:$PYTHONPATH"
PYTHONPATH="`$pathstrip -s \"$PYTHONPATH\"`"
export PYTHONPATH
#
export VERSION="$version"
export LICENCE="$licence"
#
setupVirtualEnviron || die "failed to setup virtual environment"
if [ "$BASE" = "$HOME" ] && [ "`$cq python-auto-install-modules:false`" = "true" ]; then
	"$BASE/scripts/requirements.py"
	rc=$?
	case "$rc" in
	0)
		if [ -f "$req" ]; then
			[ -d "$BASE/var/tmp" ] || mkdir -p "$HOME/var/tmp"
			upgrade() {
				__log="$BASE/var/tmp/upgrade.log.$$"
				python3 -m pip --require-virtualenv install "$@" > $__log 2>&1
				rc=$?
				if [ $rc -ne 0 ]; then
					cat $__log
				fi
				rm -f $__log
				return $rc
			}
			messagen "Upgrade \"pip\" .. "
			upgrade --upgrade pip
			messagen "process \"$req\" .. "
			upgrade --requirement "$req"
			if [ $? -ne 0 ]; then
				echo "# failed to process at `date +%c`" >> "$req"
				die "Failed to install/upgrade python packages from file \"$req\""
			fi
			message "done."
		else
			die "Missing created requirements file \"$req\""
		fi
		;;
	1)
		die "Failed to preparse requirements file due to invalid input file"
		;;
	2)
		die "Failed to preparse requirements file due to missing input file"
		;;
	3)
		# already processed, nothing to do
		;;
	*)
		die "Failed to preparse requirements file with exit status $rc"
		;;
	esac
else
	requires msgpack
fi
