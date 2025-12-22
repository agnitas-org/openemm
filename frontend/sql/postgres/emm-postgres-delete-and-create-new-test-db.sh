#!/bin/bash
scriptDir=$(dirname "${0}")

hostname=$1
username=$2
password=$3
dbname=$4

if [ -z "${hostname}" ]; then
	read -t 1 -n 10000 discard
	read -p "Enter hostname: " hostname
fi

port=5432
if [[ "${hostname}" == *:* ]]; then
	delimiter=$(expr index "${hostname}" :)
	port=${hostname:${delimiter}}
	hostname=${hostname:0:$((${delimiter} - 1))}
fi

if [ -z "${username}" ]; then
	read -t 1 -n 10000 discard
	read -p "Enter username: " username
fi

# No password indicates interactive usage, so we ask for dbname too
if [ -z "${password}" ] && [ -z "${dbname}" ]; then
	read -t 1 -n 10000 discard
	read -p "Enter dbname (default: emm): " dbname
fi

if [ -z "${password}" ]; then
	read -t 1 -n 10000 discard
	read -s -p "Enter password: " password
	echo
fi

if [ -z "${dbname}" ]; then
	dbname=emm
fi

echo

# Set environment variable for PostgreSQL authentication
export PGPASSWORD="${password}"

# getVersionNumber (textWithVersion)
function getVersionNumber {
	local version=$(echo "${1}" | grep -oE "[0-9]+\.[0-9]+\.[0-9]+((\.|-hf)[0-9]+)?" | tail -1)
	echo ${version}
}

echo "Dropping database ${dbname}"
psql -h ${hostname} -p ${port} -U ${username} -d postgres -c "DROP DATABASE IF EXISTS ${dbname};"

echo "Creating database ${dbname}"
psql -h ${hostname} -p ${port} -U ${username} -d postgres -c "CREATE DATABASE ${dbname} WITH ENCODING 'UTF8';"

sqlScriptsToDo="emm-postgres-create-tablespaces.sql"

for sqlfilename in $(find ${scriptDir} -maxdepth 1 -name "emm-postgres-fulldb-*.sql" | sort); do
	sqlScriptsToDo+=" ${sqlfilename}"
done

if [ -e "${scriptDir}/emm-postgres-test.sql" ]; then
	sqlScriptsToDo+=" ${scriptDir}/emm-postgres-test.sql"
fi

for sqlfilename in ${sqlScriptsToDo}; do
	echo "Executing ${sqlfilename}"
	psql -h ${hostname} -p ${port} -U ${username} -d ${dbname} -f ${sqlfilename} > /dev/null
	if [ $? -ne 0 ]; then
		echo "Error in sql script ${sqlfilename}"
		exit 1
	fi
done

sqlScriptsToDo=""

for sqlfilename in $(find ${scriptDir} -maxdepth 1 -name "emm-postgres-update-*.sql" | sort); do
	updatefileVersion=$(getVersionNumber ${sqlfilename})

	versionAlreadyInDb=$(psql -h ${hostname} -p ${port} -U ${username} -d ${dbname} -t -c "SELECT version_number FROM agn_dbversioninfo_tbl WHERE version_number = '${updatefileVersion}';" | xargs)

	if [[ "${versionAlreadyInDb}" == "${updatefileVersion}" ]]; then
		if [ -z "${updatefileVersion}" ]; then
			echo "Skipping file ${sqlfilename}"
		else
			echo "DB already contains version ${updatefileVersion}"
		fi
	else
		sqlScriptsToDo+=" ${sqlfilename}"
	fi
done

sqlScriptsToDo+=" ${scriptDir}/../userrights.sql ${scriptDir}/emm-postgres-messages.sql"

for sqlfilename in $(find ${scriptDir} -maxdepth 1 -name "emm-postgres-messages-*.sql" | sort); do
	sqlScriptsToDo+=" ${sqlfilename}"
done

if [ -e "${scriptDir}/emm-postgres-test-post-update.sql" ]; then
	sqlScriptsToDo+=" ${scriptDir}/emm-postgres-test-post-update.sql"
fi

for sqlfilename in ${sqlScriptsToDo}; do
	echo "Executing ${sqlfilename}"
	psql -h ${hostname} -p ${port} -U ${username} -d ${dbname} -f ${sqlfilename} > /dev/null
	if [ $? -ne 0 ]; then
		echo "Error in sql script ${sqlfilename}"
		exit 1
	fi
done
