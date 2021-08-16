/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import javax.sql.DataSource;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ConfigTableDao;
import com.agnitas.dao.LayoutDao;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.reporting.birt.util.RSACryptUtil;

/**
 * ATTENTION: Before changing something in Database affecting all companies, please rethink.
 * Try if it is possible to make a soft rollout and activate the changes for single companyIDs first. If not, you HAVE TO talk to AGNITAS developer first.
 */
public class ConfigurationValidityCheckBasicImpl implements ConfigurationValidityCheck {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ConfigurationValidityCheckBasicImpl.class);

	protected DataSource dataSource;
	protected JdbcTemplate jdbcTemplate;

	protected ConfigService configService;

	protected ConfigTableDao configTableDao;

	protected ComCompanyDao companyDao;

	protected ComTargetService targetService;
	
	private LayoutDao layoutDao;
	
	@Required
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setConfigTableDao(ConfigTableDao configTableDao) {
		this.configTableDao = configTableDao;
	}

	@Required
	public void setCompanyDao(ComCompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	@Required
	public void setTargetService(ComTargetService targetService) {
		this.targetService = targetService;
	}

	@Required
	public void setLayoutDao(LayoutDao layoutDao) {
		this.layoutDao = layoutDao;
	}

	@Override
	public void checkValidity(WebApplicationContext webApplicationContext) {
    	try {
			/**
			 * ATTENTION: Before changing something in Database affecting all companies, please rethink.
			 * Try if it is possible to make a soft rollout and activate the changes for single companyIDs first. If not, you HAVE TO talk to AGNITAS developer first.
			 */

    		initiallyConfigureBirtKeys();
    		migrateLogosAndImages(webApplicationContext);
		} catch (Exception e) {
			logger.error("Cannot check installation validity: " + e.getMessage(), e);
		}
    }

	/**
	 * Insert Birt RSA keys from PEM files into db
	 */
	private void initiallyConfigureBirtKeys() throws Exception {
		// birt.privatekey
		boolean foundThisBirtPrivateKeyValue = false;
		boolean foundOtherBirtPrivateKeyValue = false;
		String birtPrivateKeyFile = AgnUtils.getUserHomeDir() + "/conf/keys/birt_private.pem";
		if (new File(birtPrivateKeyFile).exists() && (StringUtils.isBlank(configService.getValue(ConfigValue.BirtPrivateKey)) || "[to be defined]".equals(configService.getValue(ConfigValue.BirtPrivateKey)))) {
			String birtPrivateKey = RSACryptUtil.getPrivateKey(birtPrivateKeyFile);
			for (Map<String, Object> row : jdbcTemplate.queryForList("SELECT hostname, value FROM config_tbl WHERE class = ? AND name = ? AND (hostname IS NULL OR hostname = '')", "birt", "privatekey")) {
				String hostname = (String) row.get("hostname");
				String birtPrivateKeyFromDB = (String) row.get("value");
				
				if (StringUtils.isNotBlank(birtPrivateKey) && birtPrivateKey.equals(birtPrivateKeyFromDB) && (StringUtils.isBlank(hostname) || AgnUtils.getHostName().equalsIgnoreCase(hostname))) {
					foundThisBirtPrivateKeyValue = true;
				} else if (StringUtils.isNotBlank(birtPrivateKeyFromDB) && !birtPrivateKeyFromDB.equals("[to be defined]")) {
					foundOtherBirtPrivateKeyValue = true;
				}
			}
			if (!foundThisBirtPrivateKeyValue) {
				if (foundOtherBirtPrivateKeyValue) {
					configTableDao.storeEntry("birt", "privatekey", AgnUtils.getHostName(), birtPrivateKey, "migrated BIRT key to DB");
				} else {
					configTableDao.storeEntry("birt", "privatekey", null, birtPrivateKey, "migrated BIRT key to DB");
				}
				logger.info("Added new birt.privatekey to config_tbl");
				configService.enforceExpiration();
			}
		}

		// birt.publickey
		boolean foundThisBirtPublicKeyValue = false;
		boolean foundOtherBirtPublicKeyValue = false;
		String birtPublicKeyFile = AgnUtils.getUserHomeDir() + "/conf/keys/birt_public.pem";
		if (new File(birtPublicKeyFile).exists() && (StringUtils.isBlank(configService.getValue(ConfigValue.BirtPublicKey)) || "[to be defined]".equals(configService.getValue(ConfigValue.BirtPublicKey)))) {
			String birtPublicKey = RSACryptUtil.getPublicKey(birtPublicKeyFile);
			for (Map<String, Object> row : jdbcTemplate.queryForList("SELECT hostname, value FROM config_tbl WHERE class = ? AND name = ? AND (hostname IS NULL OR hostname = '')", "birt", "publickey")) {
				String hostname = (String) row.get("hostname");
				String birtPublicKeyFromDB = (String) row.get("value");
				
				if (StringUtils.isNotBlank(birtPublicKey) && birtPublicKey.equals(birtPublicKeyFromDB) && (StringUtils.isBlank(hostname) || AgnUtils.getHostName().equalsIgnoreCase(hostname))) {
					foundThisBirtPublicKeyValue = true;
				} else if (StringUtils.isNotBlank(birtPublicKeyFromDB) && !birtPublicKeyFromDB.equals("[to be defined]")) {
					foundOtherBirtPublicKeyValue = true;
				}
			}
			if (!foundThisBirtPublicKeyValue) {
				if (foundOtherBirtPublicKeyValue) {
					configTableDao.storeEntry("birt", "publickey", AgnUtils.getHostName(), birtPublicKey, "migrated public key to DB");
				} else {
					configTableDao.storeEntry("birt", "publickey", null, birtPublicKey, "migrated public key to DB");
				}
				logger.info("Added new birt.publickey to config_tbl");
				configService.enforceExpiration();
			}
		}
	}
    
    public void migrateLogosAndImages(WebApplicationContext webApplicationContext) {
		if (layoutDao.getLayoutData().size() == 0) {
			File faviconFile = new File(webApplicationContext.getServletContext().getRealPath("favicon.ico"));
			if (faviconFile.exists()) {
				try (InputStream faviconInputStream = new FileInputStream(faviconFile)) {
					layoutDao.saveLayoutData(0, "favicon.ico", IOUtils.toByteArray(faviconInputStream));
				} catch (Exception e) {
					logger.error("Cannot find faviconFile: " + faviconFile.getAbsolutePath(), e);
				}
			} else {
				logger.error("Cannot find faviconFile: " + faviconFile.getAbsolutePath());
			}

			File emmLogoSvgFile = new File(webApplicationContext.getServletContext().getRealPath("/assets/core/images/facelift/agnitas-emm-logo.svg"));
			if (emmLogoSvgFile.exists()) {
				try (InputStream logoSvgInputStream = new FileInputStream(emmLogoSvgFile)) {
					layoutDao.saveLayoutData(0, "logo.svg", IOUtils.toByteArray(logoSvgInputStream));
				} catch (Exception e) {
					logger.error("Cannot find emmLogoSvgFile: " + emmLogoSvgFile.getAbsolutePath(), e);
				}
			} else {
				logger.error("Cannot find emmLogoSvgFile: " + emmLogoSvgFile.getAbsolutePath());
			}

			File emmLogoPngFile = new File(webApplicationContext.getServletContext().getRealPath("/assets/core/images/facelift/agnitas-emm-logo.png"));
			if (emmLogoPngFile.exists()) {
				try (InputStream logoPngInputStream = new FileInputStream(emmLogoPngFile)) {
					layoutDao.saveLayoutData(0, "logo.png", IOUtils.toByteArray(logoPngInputStream));
				} catch (Exception e) {
					logger.error("Cannot find emmLogoPngFile: " + emmLogoPngFile.getAbsolutePath(), e);
				}
			} else {
				logger.error("Cannot find emmLogoPngFile: " + emmLogoPngFile.getAbsolutePath());
			}

			File editionLogoFile = new File(webApplicationContext.getServletContext().getRealPath("/assets/core/images/facelift/edition_logo.png"));
			if (editionLogoFile.exists()) {
				try (InputStream editionLogoInputStream = new FileInputStream(editionLogoFile)) {
					layoutDao.saveLayoutData(0, "edition_logo.png", IOUtils.toByteArray(editionLogoInputStream));
				} catch (Exception e) {
					logger.error("Cannot find editionLogoFile: " + editionLogoFile.getAbsolutePath(), e);
				}
			} else {
				logger.error("Cannot find editionLogoFile: " + editionLogoFile.getAbsolutePath());
			}
		}
    }
}
