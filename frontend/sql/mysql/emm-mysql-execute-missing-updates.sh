#!/bin/bash
scriptDir=$(dirname "${0}")

if [ "$1" = "dbcfg" ]; then {
	if [ -f "/opt/agnitas.com/etc/dbcfg" ]; then {
		dbcfgFile="/opt/agnitas.com/etc/dbcfg"
	} elif [ -f "${HOME}/etc/dbcfg" ]; then {
		dbcfgFile="${HOME}/etc/dbcfg"
	} fi
	if [ -e "${dbcfgFile}" ]; then {
		dbcfgData=`sed '/^\#/d' ${dbcfgFile}`
		
		hostname=`echo "${dbcfgData}" | sed -e 's/.*host *=//g'`
		hostname="${hostname%%,*}"
		
		username=`echo "${dbcfgData}" | sed -e 's/.*user *=//g'`
		username="${username%%,*}"
		
		password=`echo "${dbcfgData}" | sed -e 's/.*password *=//g'`
		password="${password%%,*}"
		
		dbname=`echo "${dbcfgData}" | sed -e 's/.*name *=//g'`
		dbname="${dbname%%,*}"
	} fi
} elif [ -f "$1" ]; then {
	dbcfgFile="$1"
	dbcfgData=`sed '/^\#/d' ${dbcfgFile}`
	
	hostname=`echo "${dbcfgData}" | sed -e 's/.*host *=//g'`
	hostname="${hostname%%,*}"
	
	username=`echo "${dbcfgData}" | sed -e 's/.*user *=//g'`
	username="${username%%,*}"
	
	password=`echo "${dbcfgData}" | sed -e 's/.*password *=//g'`
	password="${password%%,*}"
	
	dbname=`echo "${dbcfgData}" | sed -e 's/.*name *=//g'`
	dbname="${dbname%%,*}"
} else {
	hostname=$1
	username=$2
	password=$3
	dbname=$4
} fi

if [ "${hostname}" == "" ]; then {
	#discard randomly earlier pressed keys by user
	read -t 1 -n 10000 discard
	
	read -p "Enter hostname: " hostname
} fi

port=3306
if [[ "${hostname}" == *:* ]]; then {
	delimiter=`expr index "${hostname}" :`
	port=${hostname:${delimiter}}
	hostname=${hostname:0:${delimiter} - 1}
} fi

if [ "${dbname}" == "" ]; then {
	#discard randomly earlier pressed keys by user
	read -t 1 -n 10000 discard
	
	read -p "Enter dbname (default: emm): " dbname
} fi

if [ "${username}" == "" ]; then {
	#discard randomly earlier pressed keys by user
	read -t 1 -n 10000 discard
	
	read -p "Enter username: " username
} fi

if [ "${password}" == "" ]; then {
	#discard randomly earlier pressed keys by user
	read -t 1 -n 10000 discard
	
	read -s -p "Enter password: " password
} fi

if [ "${dbname}" == "" ]; then {
	#default database name
	dbname=emm
} fi

echo

# getVersionNumber (textWithVersion)
function getVersionNumber {
	local version=`echo "${1}" | grep -oE "[0-9]+\.[0-9]+\.[0-9]+((\.|-hf)[0-9]+)?" | tail -1`
	echo ${version}
}

updatefiles="${updatefiles} ${scriptDir}/emm-mysql-messages.sql"

for sqlfilename in `find ${scriptDir} -maxdepth 1 -name "emm-mysql-messages-*.sql" | sort`;do
	updatefiles="${updatefiles} ${sqlfilename}"
done

for sqlfilename in `find ${scriptDir} -maxdepth 1 -name "emm-mysql-update-*.sql" | sort`;do
	updatefileVersion=$(getVersionNumber ${sqlfilename})
	
	versionAlreadyInDb=`echo "SELECT version_number FROM agn_dbversioninfo_tbl WHERE version_number = '${updatefileVersion}';" | MYSQL_PWD=${password} mysql -h ${hostname} -P ${port} --protocol=TCP -u ${username} --database=${dbname} --default-character-set=utf8`

	if [[ ${versionAlreadyInDb} == *${updatefileVersion}* ]]; then {
			echo "DB already contains version ${updatefileVersion}"
	} else {
			updatefiles="${updatefiles} ${sqlfilename}"
	} fi
done

updatefiles="${updatefiles} ${scriptDir}/../userrights.sql"

echo

for sqlfilename in ${updatefiles};do
	echo "Executing ${sqlfilename}"
	MYSQL_PWD=${password} mysql -h ${hostname} -P ${port} --protocol=TCP -u ${username} --database=${dbname} --default-character-set=utf8 < ${sqlfilename}
	if [ $? != 0 ]; then {
		echo "Error while executing ${sqlfilename}"
		exit 1
	} fi
done
