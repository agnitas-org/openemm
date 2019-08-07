#!/bin/bash
scriptDirectory="$(cd "$(dirname "${scriptPath}")" && pwd)"

cd ~/webapps/emm/WEB-INF/classes
java com.agnitas.emm.core.admin.encrypt.InitialPasswordEncryptor ~/conf/keys/emm.salt ${scriptDirectory}/initialPassword.sql
