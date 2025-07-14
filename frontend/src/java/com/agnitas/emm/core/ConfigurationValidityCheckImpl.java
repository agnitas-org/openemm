/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;

import javax.sql.DataSource;

import org.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.util.ServerCommand.Server;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.ConfigTableDao;
import com.agnitas.dao.LayoutDao;
import com.agnitas.emm.core.target.service.TargetService;

/**
 * ATTENTION: Before changing something in Database affecting all companies, please rethink.
 * Try if it is possible to make a soft rollout and activate the changes for single companyIDs first. If not, you HAVE TO talk to AGNITAS developer first.
 */
public class ConfigurationValidityCheckImpl implements ConfigurationValidityCheck {

	private static final Logger logger = LogManager.getLogger(ConfigurationValidityCheckImpl.class);

	protected DataSource dataSource;
	protected JdbcTemplate jdbcTemplate;

	protected ConfigService configService;

	protected ConfigTableDao configTableDao;

	protected CompanyDao companyDao;

	protected TargetService targetService;
	
	private LayoutDao layoutDao;
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public void setConfigTableDao(ConfigTableDao configTableDao) {
		this.configTableDao = configTableDao;
	}

	public void setCompanyDao(CompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	public void setTargetService(TargetService targetService) {
		this.targetService = targetService;
	}

	public void setLayoutDao(LayoutDao layoutDao) {
		this.layoutDao = layoutDao;
	}

	@Override
	public void checkValidity(WebApplicationContext webApplicationContext) {
		if (configService.getApplicationType() == Server.EMM) {
	    	try {
				/**
				 * ATTENTION: Before changing something in Database affecting all companies, please rethink.
				 * Try if it is possible to make a soft rollout and activate the changes for single companyIDs first. If not, you HAVE TO talk to AGNITAS developer first.
				 */
	
	    		migrateLogosAndImages(webApplicationContext);
			} catch (Exception e) {
				logger.error(MessageFormat.format("Cannot check installation validity: {0}", e.getMessage()), e);
			}
		}
    }
    
	/**
	 * This AGNITAS logo and image files can be replaced by using the EMT/OMT, but they have to be initialized here at the first startup of EMM/OpenEMM after installation
	 */
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

		if (!layoutDao.getLayoutData().containsKey("report_logo.png")) {
			File reportLogoFile = new File(webApplicationContext.getServletContext().getRealPath("/assets/core/images/facelift/report_logo.png"));
			if (reportLogoFile.exists()) {
				try (InputStream reportLogoInputStream = new FileInputStream(reportLogoFile)) {
					layoutDao.saveLayoutData(0, "report_logo.png", IOUtils.toByteArray(reportLogoInputStream));
				} catch (Exception e) {
					logger.error("Cannot find reportLogoFile: " + reportLogoFile.getAbsolutePath(), e);
				}
			} else {
				logger.error("Cannot find reportLogoFile: " + reportLogoFile.getAbsolutePath());
			}
		}
    }
}
