/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map.Entry;

import org.agnitas.emm.core.commons.util.ConfigKey;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ConfigTableDao;
import com.agnitas.reporting.birt.util.RSACryptUtil;

/**
 * ATTENTION: Before changing something in Database affecting all companies, please rethink.
 * Try if it is possible to make a soft rollout and activate the changes for single companyIDs first. If not, you HAVE TO talk to AGNITAS developer first.
 */
public class ConfigurationValidityCheckBasicImpl implements ConfigurationValidityCheck {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ConfigurationValidityCheckBasicImpl.class);

	protected ConfigService configService;
	
	protected ConfigTableDao configTableDao;
	
	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setConfigTableDao(ConfigTableDao configTableDao) {
		this.configTableDao = configTableDao;
	}

	@Override
	public void checkValidity() {
    	try {
			migrateBirtKeys();
			
			/**
			 * ATTENTION: Before changing something in Database affecting all companies, please rethink.
			 * Try if it is possible to make a soft rollout and activate the changes for single companyIDs first. If not, you HAVE TO talk to AGNITAS developer first.
			 */
		} catch (Exception e) {
			logger.error("Cannot check installation validity: " + e.getMessage(), e);
		}
    }

	private void migrateBirtKeys() throws IOException, SQLException {
		// Migrate Birt configuration to db (birt.privatekey)
		boolean foundThisBirtPrivateKeyValue = false;
		boolean foundOtherBirtPrivateKeyValue = false;
		String birtPrivateKeyFile = configService.getValue(AgnUtils.getHostName(), ConfigValue.BirtPrivateKeyFile);
		if (StringUtils.isNotBlank(birtPrivateKeyFile) ) {
			String birtPrivateKey = RSACryptUtil.getPrivateKey(birtPrivateKeyFile);
			for (Entry<ConfigKey, String> entry : configTableDao.getAllEntries().entrySet()) {
				if (entry.getKey().getName().equals(ConfigValue.BirtPrivateKey.toString())) {
					if (StringUtils.isNotBlank(birtPrivateKey) && birtPrivateKey.equals(entry.getValue()) && (StringUtils.isBlank(entry.getKey().getHostName()) || AgnUtils.getHostName().toLowerCase().equals(entry.getKey().getHostName()))) {
						foundThisBirtPrivateKeyValue = true;
					} else if (StringUtils.isNotBlank(entry.getValue()) && !entry.getValue().equals("[to be defined]")) {
						foundOtherBirtPrivateKeyValue = true;
					}
				}
			}
			if (!foundThisBirtPrivateKeyValue) {
				if (foundOtherBirtPrivateKeyValue) {
					configTableDao.storeEntry("birt", "privatekey", AgnUtils.getHostName(), birtPrivateKey);
				} else {
					configTableDao.storeEntry("birt", "privatekey", birtPrivateKey);
				}
				logger.info("Added new birt.privatekey to config_tbl");
				configService.enforceExpiration();
			}
		}

		// Migrate Birt configuration to db (birt.publickey)
		boolean foundThisBirtPublicKeyValue = false;
		boolean foundOtherBirtPublicKeyValue = false;
		String birtPublicKeyFile = configService.getValue(AgnUtils.getHostName(), ConfigValue.BirtPublicKeyFile);
		if (StringUtils.isNotBlank(birtPublicKeyFile) ) {
			String birtPublicKey = RSACryptUtil.getPublicKey(birtPublicKeyFile);
			for (Entry<ConfigKey, String> entry : configTableDao.getAllEntries().entrySet()) {
				if (entry.getKey().getName().equals(ConfigValue.BirtPublicKey.toString())) {
					if (StringUtils.isNotBlank(birtPublicKey) && birtPublicKey.equals(entry.getValue()) && (StringUtils.isBlank(entry.getKey().getHostName()) || AgnUtils.getHostName().toLowerCase().equals(entry.getKey().getHostName()))) {
						foundThisBirtPublicKeyValue = true;
					} else if (StringUtils.isNotBlank(entry.getValue()) && !entry.getValue().equals("[to be defined]")) {
						foundOtherBirtPublicKeyValue = true;
					}
				}
			}
			if (!foundThisBirtPublicKeyValue) {
				if (foundOtherBirtPublicKeyValue) {
					configTableDao.storeEntry("birt", "publickey", AgnUtils.getHostName(), birtPublicKey);
				} else {
					configTableDao.storeEntry("birt", "publickey", birtPublicKey);
				}
				logger.info("Added new birt.publickey to config_tbl");
				configService.enforceExpiration();
			}
		}
	}
}
