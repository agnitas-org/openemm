#!/bin/bash
if [ -f ~/.bash_profile ]; then {
	. ~/.bash_profile
} fi

VERSION="1.8.0"
LANG="en_US.ISO8859_1"
#LANG="de_DE.UTF-8"
AGN_SOFTWARE="/opt/agnitas.com/software"
CATALINA_BASE="$HOME/tomcat"
CATALINA_HOME="$AGN_SOFTWARE/tomcat"
CATALINA_TMPDIR="$HOME/temp"
JAVA_HOME="$AGN_SOFTWARE/java"

WS_OPTS="-Dcom.sun.xml.wss.debug=FaultDetail"

#### Configure options for Java VM
# HEAPDUMP_OPTS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/console/heapdumps"
# JAVA_FLIGHTRECORDER_OPTS="-XX:FlightRecorderOptions=repository=/home/console/flight-recordings -XX:StartFlightRecording=maxage=1h,name=fr.jfr"

JAVA_GC_OPTS="-XX:+UseG1GC"
JAVA_OPTS="${JAVA_FLIGHTRECORDER_OPTS} ${HEAPDUMP_OPTS} ${JAVA_GC_OPTS} -Xms256m -Xmx1536m -Xss256k -XX:-OmitStackTraceInFastThrow -Dorg.apache.jasper.runtime.BodyContentImpl.LIMIT_BUFFER=true -Dorg.apache.jasper.Constants.DEFAULT_TAG_BUFFER_SIZE=8192 ${WS_OPTS}"
#JAVA_OPTS for debugging purposes
#JAVA_OPTS="-Xms256m -Xmx1536m -Xss256k -agentlib:hprof=heap=sites,depth=5,file=/home/console/hprof.log -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8099"

if ! test -e "$HOME/bin/setenv.sh"; then {
	echo "export JAVA_HOME=\"/opt/agnitas.com/software/java\"" >> $HOME/bin/setenv.sh
	echo "export CATALINA_HOME=\"/opt/agnitas.com/software/tomcat\"" >> $HOME/bin/setenv.sh
	echo "export PROXY_HOST=" >> $HOME/bin/setenv.sh
	echo "export PROXY_PORT=" >> $HOME/bin/setenv.sh
	echo "export NO_PROXY_HOSTS=" >> $HOME/bin/setenv.sh
	echo "source $HOME/scripts/config.sh" >> $HOME/bin/setenv.sh
	
	chmod u+x $HOME/bin/setenv.sh
} fi

LOG_DIRECTORY="$HOME/tomcat/logs"
MIN_NOFILE_NR=16348
MIN_NOFILE_COMMAND=`ulimit -n`
MAX_SECONDS_KILL_9=20
MAX_SECONDS_FOR_RESTART=5
SYS_TCP_SYNACK_RETRIES=2
SYS_TCP_SYNCOOKIES=1
SYS_TCP_MAX_SYN_BACKLOG=1024
SYS_TCP_SYN_RETRIES=2
PROGRAMM_NAME=`basename $0`
ADDITIONAL_USER=
NAGIOS_NOTIFICATION_CONTROL_SCRIPT_1=
NAGIOS_NOTIFICATION_CONTROL_SCRIPT_2=
ICINGA_NOTIFICATION_CONTROL_SCRIPT_1=
ICINGA_NOTIFICATION_CONTROL_SCRIPT_2=

DB_PROPERTIES_FILE=/opt/agnitas.com/etc/dbcfg
if [ -f ${HOME}/etc/dbcfg ]; then {
	DB_PROPERTIES_FILE=${HOME}/etc/dbcfg
} fi
DB_PROPERTIES_ITEM_NAME=emm
CONTEXT_TEMPLATE_FILE=$CATALINA_BASE/conf/context.xml.template
CONTEXT_DESTINATION_FILE=$CATALINA_BASE/conf/context.xml
# file in CONTEXT_DESTINATION_FILE is going to be replaced if it exists

# Backend base dir
EMM_BACKEND_HOME=${HOME}

if [ -f ${HOME}/bin/setenv.sh ]; then {
	 echo "Using ${HOME}/bin/setenv.sh"
	 . ${HOME}/bin/setenv.sh
} fi

if [ "${ORACLE_HOME}" == "" ]; then {
	if test -e "/opt/agnitas.com/software/oracle-instantclient"; then {
		export ORACLE_HOME="/opt/agnitas.com/software/oracle-instantclient"
		if [ "${LD_LIBRARY_PATH}" == "" ]; then {
			export LD_LIBRARY_PATH="${ORACLE_HOME}"
		} else {
			export LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:${ORACLE_HOME}"
		} fi
		if test -e "${HOME}/tomcat/lib/"; then {
			rm -f ${HOME}/tomcat/lib/ojdbc8.jar
			rm -f ${HOME}/tomcat/lib/orai18n.jar
			rm -f ${HOME}/tomcat/lib/ucp.jar
			rm -f ${HOME}/tomcat/lib/xstreams.jar
			ln -s /opt/agnitas.com/software/oracle-instantclient/*.jar ${HOME}/tomcat/lib/
		} fi
	} elif test -e "/opt/agnitas.com/software/oracle/product/11.2.0/client_1"; then {
		export ORACLE_HOME="/opt/agnitas.com/software/oracle/product/11.2.0/client_1"
		if [ "${LD_LIBRARY_PATH}" == "" ]; then {
			export LD_LIBRARY_PATH="${ORACLE_HOME}/lib"
		} else {
			export LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:${ORACLE_HOME}/lib"
		} fi
	} fi
} elif [ "${LD_LIBRARY_PATH}" == "" ]; then {
	if test -e "${ORACLE_HOME}/lib"; then {
		export LD_LIBRARY_PATH="${ORACLE_HOME}/lib"
	} elif test -e "${ORACLE_HOME}"; then {
		export LD_LIBRARY_PATH="${ORACLE_HOME}"
	} fi
} else {
	if test -e "${ORACLE_HOME}/lib"; then {
		export LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:${ORACLE_HOME}/lib"
	} elif test -e "${ORACLE_HOME}"; then {
		export LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:${ORACLE_HOME}"
	} fi
} fi

if [ "${TNS_ADMIN}" == "" ]; then {
	if test -e "/opt/agnitas.com/etc"; then {
		export TNS_ADMIN="/opt/agnitas.com/etc"
	} fi
} fi

ADDITIONAL_PROPERTIES="$(readlink -f ${0}).additional.properties"
if [ -f ${ADDITIONAL_PROPERTIES} ]; then {
	echo "Using additional properties: ${ADDITIONAL_PROPERTIES}"
	. ${ADDITIONAL_PROPERTIES}
} fi

if [ ! -d $CATALINA_TMPDIR ]; then {
	mkdir $CATALINA_TMPDIR
} fi

print_sysctl_lines(){
	echo "Please insert/update this lines in your /etc/sysctl.conf"
	echo "############################################################################"
	echo "net.ipv4.tcp_synack_retries = $SYS_TCP_SYNACK_RETRIES"
	echo "net.ipv4.tcp_syncookies = $SYS_TCP_SYNCOOKIES"
	echo "net.ipv4.tcp_max_syn_backlog = $SYS_TCP_MAX_SYN_BACKLOG"
	echo "net.ipv4.tcp_syn_retries = $SYS_TCP_SYN_RETRIES"
	echo "############################################################################"
	echo "You can use sysctl -p (as root) to Update your System in Liveusage"
}

test_ulimit() {
	if [ $MIN_NOFILE_COMMAND -ge $MIN_NOFILE_NR ]; then {
		echo "OK"
	} else {
		echo "ulimit(nofile) must be greater or equal then $MIN_NOFILE_NR, at this Time ulimit -n is $MIN_NOFILE_COMMAND"
		exit 2
	} fi
}

test_proc_vs_variable() {
	if [ `cat $1` = $2 ]; then {
		echo "OK"
	} else {
		echo "FAILED"
		print_sysctl_lines
		exit 2
	} fi
}

search_file_exist() {
	if [ -e $1 ]; then {
		echo "OK"
	} else {
		echo "FAILED"
		exit 2
	} fi
}

search_directory_exist() {
	if [ -d $1 ]; then {
		echo "OK"
	} else {
		echo "FAILED"
		exit 2
	} fi
}

test_config_all() {
	echo -n "Check Java installation:          "
	if [ -e "$JAVA_HOME/bin/java" ]; then {
		javaVersionRaw=$("$JAVA_HOME/bin/java" -version 2>&1)
		if [[ ${javaVersionRaw} =~ \"([0-9]+.[0-9]+.[0-9]+_[0-9]+)\" ]]; then {
			javaVersion=${BASH_REMATCH[1]}
			if [[ ${javaVersionRaw} =~ openjdk ]]; then {
				javaVersion="${javaVersion} (OpenJDK)"
			} else {
				javaVersion="${javaVersion} (Oracle)"
			} fi
		} elif [[ ${javaVersionRaw} =~ \"([0-9]+.[0-9]+.[0-9]+.[0-9]+)\" ]]; then {
			javaVersion=${BASH_REMATCH[1]}
			if [[ ${javaVersionRaw} =~ openjdk ]]; then {
				javaVersion="${javaVersion} (OpenJDK)"
			} else {
				javaVersion="${javaVersion} (Oracle)"
			} fi
		} elif [[ ${javaVersionRaw} =~ \"([0-9]+.[0-9]+.[0-9]+)\" ]]; then {
			javaVersion=${BASH_REMATCH[1]}
			if [[ ${javaVersionRaw} =~ openjdk ]]; then {
				javaVersion="${javaVersion} (OpenJDK)"
			} else {
				javaVersion="${javaVersion} (Oracle)"
			} fi
		} elif [[ ${javaVersionRaw} =~ \"([0-9]+.[0-9]+)\" ]]; then {
			javaVersion=${BASH_REMATCH[1]}
			if [[ ${javaVersionRaw} =~ openjdk ]]; then {
				javaVersion="${javaVersion} (OpenJDK)"
			} else {
				javaVersion="${javaVersion} (Oracle)"
			} fi
		} elif [[ ${javaVersionRaw} =~ \"([0-9]+)\" ]]; then {
			javaVersion=${BASH_REMATCH[1]}
			if [[ ${javaVersionRaw} =~ openjdk ]]; then {
				javaVersion="${javaVersion} (OpenJDK)"
			} else {
				javaVersion="${javaVersion} (Oracle)"
			} fi
		} else {
			javaVersion="Unkown"
		} fi
		
		echo "OK ($javaVersion)"
	} else {
		echo "FAILED (JAVA_HOME: $JAVA_HOME)"
		exit 2
	} fi
	
	echo -n "Check Tomcat installation:        "
	if [ -e "$CATALINA_HOME/lib/catalina.jar" ]; then {
		tomcatVersion=`"$JAVA_HOME/bin/java" -cp "$CATALINA_HOME/lib/catalina.jar" org.apache.catalina.util.ServerInfo | grep "Server number:"`
		tomcatVersion=$(echo ${tomcatVersion} | cut -d':' -f 2 | tr -d '[:space:]')
		if [ "${tomcatVersion}" != "" ]; then {
			echo "OK (${tomcatVersion})"
		} else {
			echo "OK (Unknown version)"
		} fi
	} else {
		echo "FAILED (CATALINA_HOME: $CATALINA_HOME)"
		exit 2
	} fi
	
	echo -n "Check Tomcat TEMP Directory:      "
	search_directory_exist $CATALINA_TMPDIR
	
	echo -n "Check ulimit -n:                  "
	test_ulimit
	echo -n "JAVA Parameters:                  "	
	echo $JAVA_OPTS
}

test_config_rdir() {
	echo -n "Check System tcp_synack_retries:  "
	test_proc_vs_variable /proc/sys/net/ipv4/tcp_synack_retries $SYS_TCP_SYNACK_RETRIES
	echo -n "Check System tcp_syncookies:      "
	test_proc_vs_variable /proc/sys/net/ipv4/tcp_syncookies $SYS_TCP_SYNCOOKIES
	echo -n "Check System tcp_max_syn_backlog: "
	test_proc_vs_variable /proc/sys/net/ipv4/tcp_max_syn_backlog $SYS_TCP_MAX_SYN_BACKLOG
	echo -n "Check System tcp_syn_retries:     "
	test_proc_vs_variable /proc/sys/net/ipv4/tcp_syn_retries $SYS_TCP_SYN_RETRIES
}

test_tomcat_running() {
	JPS_RUNNING_PID=`ps uxww|grep -v grep|grep org.apache.catalina|grep ${CATALINA_BASE}|awk '{print $2}'`
}

# getDbcfgPropertyValue (itemName propertyName)
function getDbcfgPropertyValue {
	# get item value line with all the property name and values
	local itemValue=`sed '/^\#/d' ${DB_PROPERTIES_FILE} | grep "^ *${1} *:" | tail -n 1 | cut -d ":" -f2- | sed 's/^[[:space:]]*//;s/[[:space:]]*$//'`
	# get value for a single property
	local propertyValue=`echo "${itemValue}" | sed -e 's/, /\n/g' | grep "^ *${2} *=" | tail -n 1 | cut -d "=" -f2-`
	# return value
	echo ${propertyValue}
}

start() {
	cd ${CATALINA_BASE}
	test_tomcat_running
	
	if [ -d "${HOME}/release/backend" ]; then {
		checkBackendDirs
	} fi

	if [ $JPS_RUNNING_PID ]; then {
		echo "Sorry, Tomcat is already running as PID $JPS_RUNNING_PID!"
		exit 2
	} else {
		# Generate and replace context.xml
		if [ -f ${DB_PROPERTIES_FILE} -a -f ${CONTEXT_TEMPLATE_FILE} ]; then {
			if [ "${DB_PROPERTIES_ITEM_NAME}" == "" ]; then {
				echo "Missing mandatory value for DB_PROPERTIES_ITEM_NAME"
				exit 2
			} fi
			
			driverClassName_emm_db=$(getDbcfgPropertyValue ${DB_PROPERTIES_ITEM_NAME} jdbc-driver)
			userName_emm_db=$(getDbcfgPropertyValue ${DB_PROPERTIES_ITEM_NAME} user)
			password_emm_db=$(getDbcfgPropertyValue ${DB_PROPERTIES_ITEM_NAME} password)
			url_emm_db=$(getDbcfgPropertyValue ${DB_PROPERTIES_ITEM_NAME} jdbc-connect)

			# replace '/' by '\/' (escape slashes) and '&' by '\&amp;' (escape ampersands) for correct sed action and xml syntax
			url_emm_db=${url_emm_db//\//\\\/}
			url_emm_db=${url_emm_db//&/\\&amp;}

			# Replace placeholders and generate new file
			password_emm_db_escaped=${password_emm_db//\//\\/}
			password_emm_db_escaped=${password_emm_db_escaped//\?/\\?}
			password_emm_db_escaped=${password_emm_db_escaped//\"/&quot;}
			password_emm_db_escaped=${password_emm_db_escaped//\'/&apos;}
			password_emm_db_escaped=${password_emm_db_escaped//\&/\\&amp;}
			sed -e "s/\${driverClassName_emm_db}/${driverClassName_emm_db}/g" ${CONTEXT_TEMPLATE_FILE} | \
			sed -e "s/\${userName_emm_db}/${userName_emm_db}/g" | \
			sed -e "s/\${password_emm_db}/${password_emm_db_escaped}/g" | \
			sed -e "s/\${url_emm_db}/${url_emm_db}/g" > ${CONTEXT_DESTINATION_FILE}

			echo "Generated and replaced \"${CONTEXT_DESTINATION_FILE}\"."
		} else {
			echo "File \"${CONTEXT_DESTINATION_FILE}\" was not generated, because source files were missing:"
			if [ ! -f ${DB_PROPERTIES_FILE} ]; then {
				echo "  ${DB_PROPERTIES_FILE} is missing"
			} fi
			if [ ! -f ${CONTEXT_TEMPLATE_FILE} ]; then {
				echo "  ${CONTEXT_TEMPLATE_FILE} is missing"
			} fi
			echo ""

			if [ -f ${CONTEXT_DESTINATION_FILE} ]; then {
				echo "Previous version will be used."
			} else {
				echo "Cannot start tomcat, because mandatory file \"${CONTEXT_DESTINATION_FILE}\" is missing."
				exit 2
			} fi
		} fi
		
		# Cleanup Derby DB
		if [ -d ${HOME}/var/db/report.db ]; then {
			rm -r ${HOME}/var/db/report.db
		} fi

		echo "######################"
		echo "# Starting up Tomcat #"
		echo "######################"
		if [ ${USER} == "rdir" ]; then {
			test_config_rdir
		} fi

		JAVA_OPTS="${JAVA_OPTS} -DHOME=${HOME} -Dhome=${HOME}"

		test_config_all
		
		if [ "${ORACLE_HOME}" != "" ]; then {
			echo "Using ORACLE_HOME:     ${ORACLE_HOME}"
		} fi
		
		if [ "${TNS_ADMIN}" != "" ]; then {
			echo "Using TNS_ADMIN:       ${TNS_ADMIN}"
		} fi
		
		if [ "${LD_LIBRARY_PATH}" != "" ]; then {
			echo "Using LD_LIBRARY_PATH: ${LD_LIBRARY_PATH}"
		} fi

		$CATALINA_HOME/bin/startup.sh
		
		# Write 25 empty lines to logfile to prevent detecting latest startup
		for i in {1..25}
		do
			echo -e "" >> "${LOG_DIRECTORY}/catalina.out"
		done
		
		echo "Waiting maximum 120 seconds for server startup"
		for i in {120..1}
		do
			latestLogContent=$(tail -n 25 "${LOG_DIRECTORY}/catalina.out" | grep "Server startup in")
			if [[ -n ${latestLogContent} ]]; then {
				echo "Server startup done"
				break
			} fi

			echo -ne "$i seconds left  \r"
			sleep 1
		done

		if [ "${NAGIOS_NOTIFICATION_CONTROL_SCRIPT_1}" != "" ] && [ -f ${NAGIOS_NOTIFICATION_CONTROL_SCRIPT_1} ]; then {
			echo "###################################"
			echo "# Starting up Nagios surveillance #"
			echo "###################################"

			sudo -u nagios ${NAGIOS_NOTIFICATION_CONTROL_SCRIPT_1} ${NAGIOS} ${NAG_CLIENT} enable
		} fi

		if [ "${NAGIOS_NOTIFICATION_CONTROL_SCRIPT_2}" != "" ] && [ -f ${NAGIOS_NOTIFICATION_CONTROL_SCRIPT_2} ]; then {
			echo "###################################"
			echo "# Starting up Nagios surveillance #"
			echo "###################################"
			echo "Waiting 120 seconds before nagios activation"
			sleep 120
			sudo -u nagios ${NAGIOS_NOTIFICATION_CONTROL_SCRIPT_2} ${D_NAGIOS} ${NAG_CLIENT} enable
		} fi

		if [ "${ICINGA_NOTIFICATION_CONTROL_SCRIPT_1}" != "" ] && [ -f ${ICINGA_NOTIFICATION_CONTROL_SCRIPT_1} ]; then {
			echo "###################################"
			echo "# Starting up Icinga surveillance #"
			echo "###################################"

			${ICINGA_NOTIFICATION_CONTROL_SCRIPT_1} enable
		} fi

		if [ "${ICINGA_NOTIFICATION_CONTROL_SCRIPT_2}" != "" ] && [ -f ${ICINGA_NOTIFICATION_CONTROL_SCRIPT_2} ]; then {
			echo "###################################"
			echo "# Starting up Icinga surveillance #"
			echo "###################################"
			echo "Waiting 120 seconds before icinga activation"
			sleep 120
			${ICINGA_NOTIFICATION_CONTROL_SCRIPT_2} enable
		} fi
		
		# Check for basic startup errors
		latestError=$(tail -n 100 "${LOG_DIRECTORY}/catalina.out" | grep "BeanCreationException")
		if [[ ${latestError} ]]; then {
			echo "###################################################"
			echo "###################################################"
			echo "## ERROR on Server startup found in catalina.out ##"
			echo "###################################################"
			echo "###################################################"
		} fi
	} fi
}

stop() {
	test_tomcat_running
	if [ $JPS_RUNNING_PID ]; then {
		if [ "${NAGIOS_NOTIFICATION_CONTROL_SCRIPT_1}" != "" ] && [ -f ${NAGIOS_NOTIFICATION_CONTROL_SCRIPT_1} ]; then {
			echo "######################################"
			echo "# Shutting down Nagios surveillance #"
			echo "######################################"
			sudo -u nagios ${NAGIOS_NOTIFICATION_CONTROL_SCRIPT_1} ${NAGIOS} ${NAG_CLIENT} disable
		} fi

		if [ "${NAGIOS_NOTIFICATION_CONTROL_SCRIPT_2}" != "" ] && [ -f ${NAGIOS_NOTIFICATION_CONTROL_SCRIPT_2} ]; then {
			echo "######################################"
			echo "# Shutting down Nagios surveillance #"
			echo "######################################"
			sudo -u nagios ${NAGIOS_NOTIFICATION_CONTROL_SCRIPT_2} ${D_NAGIOS} ${NAG_CLIENT} disable
		} fi
		
		if [ "${ICINGA_NOTIFICATION_CONTROL_SCRIPT_1}" != "" ] && [ -f ${ICINGA_NOTIFICATION_CONTROL_SCRIPT_1} ]; then {
			echo "######################################"
			echo "# Shutting down Icinga surveillance #"
			echo "######################################"
			${ICINGA_NOTIFICATION_CONTROL_SCRIPT_1} disable
		} fi

		if [ "${ICINGA_NOTIFICATION_CONTROL_SCRIPT_2}" != "" ] && [ -f ${ICINGA_NOTIFICATION_CONTROL_SCRIPT_2} ]; then {
			echo "######################################"
			echo "# Shutting down Icinga surveillance #"
			echo "######################################"
			${ICINGA_NOTIFICATION_CONTROL_SCRIPT_2} disable
		} fi

		echo "########################"
		echo "# Shutting down Tomcat #"
		echo "########################"
		$CATALINA_HOME/bin/shutdown.sh
		sleep 2
		n=0
		echo -n "Waiting for shutdown of Tomcat(MAX $MAX_SECONDS_KILL_9 Seconds):"
		test_tomcat_running
		while [ $JPS_RUNNING_PID ]; do
			if [ $n -gt $MAX_SECONDS_KILL_9 ]; then {
				kill -9 $JPS_RUNNING_PID
				echo "."
				echo -n "Terminating Tomcat the hard way with kill -9 $JPS_RUNNING_PID"
			} else {
				echo -n "."
			} fi
			sleep 1
			test_tomcat_running
			n=`expr $n + 1`
		done
		echo "."
		echo "Delete Working directory"
		rm $CATALINA_BASE/work/* -rf
	} else {
		echo "No Tomcat is running...."
		exit 2
	} fi
}

restart() {
	stop
	f=0
	echo -n "Waiting $MAX_SECONDS_FOR_RESTART Seconds, before starting Tomcat again:"
	while [ $f -lt $MAX_SECONDS_FOR_RESTART ]; do
		echo -n "."
		sleep 1
		f=`expr $f + 1`	
	done	
	echo "."
	start
}

forceRestart() {
	cd ${CATALINA_BASE}
	test_tomcat_running
	
	if [ -d "${HOME}/release/backend" ]; then {
		checkBackendDirs
	} fi

	if [ $JPS_RUNNING_PID ]; then {
		restart
	} else {
		start
	} fi
}

checkBackendDirs() {
	echo
	echo "Checking backend directories"

	beScripts="${EMM_BACKEND_HOME}/scripts"
	beBin="${EMM_BACKEND_HOME}/bin"
	beCurrent="${EMM_BACKEND_HOME}/release/backend/current"
	beCurrentScripts="$beCurrent/scripts"
	beCurrentBin="$beCurrent/bin"
	beSanity="$beBin/sanity.sh"

	if [ ! -d "$beScripts" ]; then {
		if [ -d "$beCurrentScripts" ]; then {
			ln -s "$beCurrent/scripts" "$beScripts"
			if [ $? -ne 0 ]; then {
				echo "** Failed to link backend $beCurrent/scripts to $beScripts" 1>&2
			} else {
				echo "Created backend link from $beCurrentScripts to $beScripts"	
			} fi
		} else {
			echo "** Missing backend scripts directory $beCurrentScripts" 1>&2
		} fi
	} fi

	if [ ! -d "$beBin" ]; then {
		mkdir "$beBin"
		if [ $? -ne 0 ]; then {
			echo "** Failed to create missing backend directory $beBin"
		} else {
			echo "Created backend directory $beBin"
		} fi
	} fi

	if [ -d "$beCurrentBin" ]; then {
		for path in $beCurrentBin/* ; do
			beBinFile="$beBin/`basename $path`"
			if [ ! -f "$beBinFile" ]; then {
				ln -s "$path" "$beBinFile"
				if [ $? -ne 0 ]; then {
					echo "** Failed to backend link $path to $beBinFile" 1>&2
				} else {
					echo "Created backend link from $path to $beBinFile"
				} fi
			} fi
		done
	} else {
		echo "** Missing backend scripts directory $beCurrentBin" 1>&2
	} fi

	if [ -x "$beSanity" ]; then {
		"$beSanity" "`whoami`"
	} else {
		echo "** Missing backend sanity check $beSanity" 1>&2
	} fi
	echo
}

export CATALINA_BASE CATALINA_HOME CATALINA_TMPDIR JAVA_HOME JAVA_OPTS LANG
if [ "${JRE_HOME}" != "" ]; then {
	JRE_HOME=${JAVA_HOME}
	export JRE_HOME
} fi

case "$1" in
	start)
		start
		;;
	status)
		test_tomcat_running
		if [ $JPS_RUNNING_PID ]; then {
			echo "Tomcat is running as PID $JPS_RUNNING_PID"
		} else {
			echo "No Tomcat process is running"
		} fi
		;;
	restart)
		restart
		;;
	force-restart)
		forceRestart
		;;
	version)
		echo "$PROGRAMM_NAME:           $VERSION"
		echo "Username: 	${USER}"
		echo "Hostname: 	$HOSTNAME"
		$JAVA_HOME/bin/java -classpath "$CATALINA_HOME/lib/catalina.jar" org.apache.catalina.util.ServerInfo
		;;
	stop)
		stop
		;;
	*)
		echo "Error, permitted parameters are:"
		echo "$PROGRAMM_NAME (start | stop | restart | force-restart| status | version)"
		exit 2
		;;
esac
exit 0
