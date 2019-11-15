#!/bin/bash
scriptDir=$(dirname "${0}")

hostname=$1
username=$2
password=$3
dbname=$4

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

if [ "${username}" == "" ]; then {
	#discard randomly earlier pressed keys by user
	read -t 1 -n 10000 discard
	
	read -p "Enter username: " username
} fi

# No password indicates interactive usage, so we ask for dbname, too
if [ "${password}" == "" ] && [ "${dbname}" == "" ]; then {
	#discard randomly earlier pressed keys by user
	read -t 1 -n 10000 discard
	
	read -p "Enter dbname (default: emm): " dbname
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
	local version=`echo "${1}" | grep -oE "[0-9]+\.[0-9]+\.[0-9]+(-hf[0-9]+)?" | tail -1`
	echo ${version}
}

echo "Droping database ${dbname}"
echo "DROP DATABASE IF EXISTS ${dbname};" | MYSQL_PWD=${password} mysql -h ${hostname} -P 3306 -u ${username} --default-character-set=utf8

echo "Creating database ${dbname}"
echo "CREATE DATABASE ${dbname} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" | MYSQL_PWD=${password} mysql -h ${hostname} -P 3306 -u ${username} --default-character-set=utf8

sqlScriptsToDo=""

for sqlfilename in `find ${scriptDir} -maxdepth 1 -name "emm-mysql-fulldb-*.sql" | sort`; do
	sqlScriptsToDo+=" ${sqlfilename}"
done
if [ -e "${scriptDir}/emm-mysql-test.sql" ]; then {
	sqlScriptsToDo+=" ${scriptDir}/emm-mysql-test.sql"
} fi

for sqlfilename in ${sqlScriptsToDo}; do
	echo "Executing ${sqlfilename}"
	
	MYSQL_PWD=${password} mysql -h ${hostname} -P 3306 -u ${username} --database=${dbname} --default-character-set=utf8 < ${sqlfilename}

	if [ $? != 0 ]; then {
		echo "Error in sql script"
		exit 1
	} fi
done

sqlScriptsToDo=""

for sqlfilename in `find ${scriptDir} -maxdepth 1 -name "emm-mysql-update-*.sql" | sort`;do
	updatefileVersion=$(getVersionNumber ${sqlfilename})
	
	versionAlreadyInDb=`echo "SELECT version_number FROM agn_dbversioninfo_tbl WHERE version_number = '${updatefileVersion}';" | MYSQL_PWD=${password} mysql -h ${hostname} -P ${port} --protocol=TCP -u ${username} --database=${dbname} --default-character-set=utf8`

    if [[ ${versionAlreadyInDb} == *${updatefileVersion}* ]]; then {
            echo "DB already contains version ${updatefileVersion}"
    } else {
           sqlScriptsToDo+=" ${sqlfilename}"
    } fi
done

sqlScriptsToDo+=" ${scriptDir}/../userrights.sql ${scriptDir}/emm-mysql-messages.sql"
for sqlfilename in `find ${scriptDir} -maxdepth 1 -name "emm-mysql-messages-*.sql" | sort`;do
	sqlScriptsToDo="${sqlScriptsToDo} ${sqlfilename}"
done
if [ -e "${scriptDir}/emm-mysql-test-post-update.sql" ]; then {
	sqlScriptsToDo+=" ${scriptDir}/emm-mysql-test-post-update.sql"
} fi

for sqlfilename in ${sqlScriptsToDo}; do
	echo "Executing ${sqlfilename}"
	
	MYSQL_PWD=${password} mysql -h ${hostname} -P 3306 -u ${username} --database=${dbname} --default-character-set=utf8 < ${sqlfilename}

	if [ $? != 0 ]; then {
		echo "Error in sql script"
		exit 1
	} fi
done
