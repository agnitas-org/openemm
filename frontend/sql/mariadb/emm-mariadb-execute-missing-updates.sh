#!/bin/bash
scriptDir=$(dirname "${0}")

if [ "$1" = "dbcfg" ]; then {
	if [ -f "/opt/agnitas.com/etc/dbcfg" ]; then {
		dbcfgFile="/opt/agnitas.com/etc/dbcfg"
	} elif [ -f "${HOME}/etc/dbcfg" ]; then {
		dbcfgFile="${HOME}/etc/dbcfg"
	} fi
	if [ -e "${dbcfgFile}" ]; then {
		# Remove comment lines and empty lines
		dbcfgData=`sed '/^\#/d' ${dbcfgFile} | sed '/^[[:space:]]*$/d'`
		
		if [[ "${dbcfgData}" == *"host"* ]]; then {
			# Cut data before search text
			hostname=`echo "${dbcfgData}" | sed -e 's/.*host *=//g'`
			# Cut data after ", "
			hostname="${hostname%%, *}"
			# Trim
			hostname=`echo "${hostname}" | sed 's/^ *//g' | sed 's/ *$//g'`
		} fi
		
		if [[ "${dbcfgData}" == *"user"* ]]; then {
			# Cut data before search text
			username=`echo "${dbcfgData}" | sed -e 's/.*user *=//g'`
			# Cut data after ", "
			username="${username%%, *}"
			# Trim
			username=`echo "${username}" | sed 's/^ *//g' | sed 's/ *$//g'`
		} fi
		
		if [[ "${dbcfgData}" == *"password"* ]]; then {
			# Cut data before search text
			password=`echo "${dbcfgData}" | sed -e 's/.*password *=//g'`
			# Cut data after ", "
			password="${password%%, *}"
			# Trim
			password=`echo "${password}" | sed 's/^ *//g' | sed 's/ *$//g'`
		} fi
		
		if [[ "${dbcfgData}" == *"name"* ]]; then {
			# Cut data before search text
			dbname=`echo "${dbcfgData}" | sed -e 's/.*name *=//g'`
			# Cut data after ", "
			dbname="${dbname%%, *}"
			# Trim
			dbname=`echo "${dbname}" | sed 's/^ *//g' | sed 's/ *$//g'`
		} fi
		
		if [[ "${dbcfgData}" == *"secure"* ]]; then {
			# Cut data before search text
			secure=`echo "${dbcfgData}" | sed -e 's/.*secure *=//g'`
			# Cut data after ", "
			secure="${secure%%, *}"
			# Trim
			secure=`echo "${secure}" | sed 's/^ *//g' | sed 's/ *$//g'`
		} fi
	} fi
} elif [ -f "$1" ]; then {
	dbcfgFile="$1"
	if [ -e "${dbcfgFile}" ]; then {
		# Remove comment lines and empty lines
		dbcfgData=`sed '/^\#/d' ${dbcfgFile} | sed '/^[[:space:]]*$/d'`
		
		if [[ "${dbcfgData}" == *"host"* ]]; then {
			# Cut data before search text
			hostname=`echo "${dbcfgData}" | sed -e 's/.*host *=//g'`
			# Cut data after ", "
			hostname="${hostname%%, *}"
			# Trim
			hostname=`echo "${hostname}" | sed 's/^ *//g' | sed 's/ *$//g'`
		} fi
		
		if [[ "${dbcfgData}" == *"user"* ]]; then {
			# Cut data before search text
			username=`echo "${dbcfgData}" | sed -e 's/.*user *=//g'`
			# Cut data after ", "
			username="${username%%, *}"
			# Trim
			username=`echo "${username}" | sed 's/^ *//g' | sed 's/ *$//g'`
		} fi
		
		if [[ "${dbcfgData}" == *"password"* ]]; then {
			# Cut data before search text
			password=`echo "${dbcfgData}" | sed -e 's/.*password *=//g'`
			# Cut data after ", "
			password="${password%%, *}"
			# Trim
			password=`echo "${password}" | sed 's/^ *//g' | sed 's/ *$//g'`
		} fi
		
		if [[ "${dbcfgData}" == *"name"* ]]; then {
			# Cut data before search text
			dbname=`echo "${dbcfgData}" | sed -e 's/.*name *=//g'`
			# Cut data after ", "
			dbname="${dbname%%, *}"
			# Trim
			dbname=`echo "${dbname}" | sed 's/^ *//g' | sed 's/ *$//g'`
		} fi
		
		if [[ "${dbcfgData}" == *"secure"* ]]; then {
			# Cut data before search text
			secure=`echo "${dbcfgData}" | sed -e 's/.*secure *=//g'`
			# Cut data after ", "
			secure="${secure%%, *}"
			# Trim
			secure=`echo "${secure}" | sed 's/^ *//g' | sed 's/ *$//g'`
		} fi
	} fi
} else {
	hostname=$1
	username=$2
	password=$3
	dbname=$4
	secure=$5
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

if [ "${secure}" = "yes" ] || [ "${secure}" = "true" ] || [ "${secure}" = "y" ] || [ "${secure}" = "j" ] || [ "${secure}" = "YES" ] || [ "${secure}" = "TRUE" ] || [ "${secure}" = "Y" ] || [ "${secure}" = "J" ] || [ "${secure}" = "" ]; then {
	optionalSslParameter=" --ssl"
} else {
	optionalSslParameter=""
} fi

echo

# getVersionNumber (textWithVersion)
function getVersionNumber {
	local version=`echo "${1}" | grep -oE "[0-9]+\.[0-9]+\.[0-9]+((\.|-hf)[0-9]+)?" | tail -1`
	echo ${version}
}

updatefiles="${updatefiles} ${scriptDir}/emm-mariadb-messages.sql"

for sqlfilename in `find ${scriptDir} -maxdepth 1 -name "emm-mariadb-messages-*.sql" | sort`;do
	updatefiles="${updatefiles} ${sqlfilename}"
done

for sqlfilename in `find ${scriptDir} -maxdepth 1 -name "emm-mariadb-update-*.sql" | sort`;do
	updatefileVersion=$(getVersionNumber ${sqlfilename})
	
	versionAlreadyInDb=`echo "SELECT version_number FROM agn_dbversioninfo_tbl WHERE version_number = '${updatefileVersion}';" | MYSQL_PWD=${password} mysql -h ${hostname} -P ${port} --protocol=TCP ${optionalSslParameter} -u ${username} --database=${dbname} --default-character-set=utf8`

	if [[ ${versionAlreadyInDb} == *${updatefileVersion}* ]]; then {
		if [ "${updatefileVersion}" == "" ]; then {
			echo "Skipping file ${sqlfilename}"
		} else {
			echo "DB already contains version ${updatefileVersion}"
		} fi
	} else {
		updatefiles="${updatefiles} ${sqlfilename}"
	} fi
done

updatefiles="${updatefiles} ${scriptDir}/../userrights.sql"

updatefiles="${updatefiles} ${scriptDir}/emm-mariadb-migration.sql"

echo

for sqlfilename in ${updatefiles};do
	echo "Executing ${sqlfilename}"
	MYSQL_PWD=${password} mysql -h ${hostname} -P ${port} --protocol=TCP ${optionalSslParameter} -u ${username} --database=${dbname} --default-character-set=utf8 < ${sqlfilename}
	if [ $? != 0 ]; then {
		echo "Error while executing ${sqlfilename}"
		exit 1
	} fi
done
