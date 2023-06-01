#!/bin/bash
scriptDirectory="$(cd "$(dirname "${scriptPath}")" && pwd)"

cd ~/emm/webapps/css/WEB-INF/classes
java com.agnitas.emm.core.admin.encrypt.InitialPasswordEncryptor ~/emm/conf/keys/emm.salt ${scriptDirectory}/initialPassword.sql
