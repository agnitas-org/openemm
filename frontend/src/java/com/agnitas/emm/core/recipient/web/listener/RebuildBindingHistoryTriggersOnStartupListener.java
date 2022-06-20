/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.web.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.agnitas.beans.impl.CompanyStatus;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.recipient.dao.BindingHistoryDao;

/**
 * Rebuilds the binding history trigger for all marked companies, with triggers not updated by previous run.
 * 
 * To mark a company, set key
 * "recipient.binding_history.rebuild_trigger_on_startup" in company_info_tbl to
 * "true".
 */
public final class RebuildBindingHistoryTriggersOnStartupListener implements ServletContextListener {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(RebuildBindingHistoryTriggersOnStartupListener.class);
	
	private WebApplicationContext webApplicationContext;
	private DataSource dataSource;
	private ComCompanyDao companyDao;
	private BindingHistoryDao bindingHistoryDao;
	private ConfigService configService;

	@Override
	public final void contextInitialized(final ServletContextEvent servletContextEvent) {
		final ServletContext servletContext = servletContextEvent.getServletContext();
		webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);

		updateMarkedCompanies();
	}

	@Override
	public final void contextDestroyed(final ServletContextEvent servletContextEvent) {
		// Nothing to be done here
	}

	public final void updateMarkedCompanies() {
		try {
			final JdbcTemplate template = new JdbcTemplate(getDataSource());
			List<Map<String, Object>> result = template.queryForList("SELECT company_id, cvalue FROM company_info_tbl WHERE cname = ? ORDER BY company_id", ConfigValue.RecipientBindingFieldHistoryRebuildOnStartup.toString());
			List<Integer> markedCompanies = new ArrayList<>();
			for (Map<String, Object> row : result) {
				if (AgnUtils.interpretAsBoolean((String) row.get("cvalue"))) {
					markedCompanies.add(((Number) row.get("company_id")).intValue());
				}
			}

			if (markedCompanies.size() > 0) {
				logger.warn(String.format("Found %d companies to rebuild binding history triggers", markedCompanies.size()));
			}

			for (final int companyID : markedCompanies) {
				if (CompanyStatus.ACTIVE == getCompanyDao().getCompany(companyID).getStatus()) {
					try {
						logger.warn(String.format("Rebuilding binding history triggers for company %d", companyID));
					
						getBindingHistoryDao().recreateBindingHistoryTrigger(companyID);
						final JdbcTemplate template1 = new JdbcTemplate(getDataSource());
						
						getConfigService().getBooleanValue(ConfigValue.RecipientBindingFieldHistoryRebuildOnStartup, companyID);
						template1.update("UPDATE company_info_tbl SET cvalue = 'false', timestamp = CURRENT_TIMESTAMP, description = 'History binding triggers rebuilt' WHERE company_id = ? and cname = ?", companyID, ConfigValue.RecipientBindingFieldHistoryRebuildOnStartup.toString());
					} catch (final Exception e) {
						logger.error(String.format("Could not rebuild binding history trigger for company %d", companyID), e);
					}
				}
			}
		} catch (Exception e) {
			logger.error(String.format("Could not rebuild binding history triggers: " + e.getMessage()), e);
		}
	}

	private DataSource getDataSource() {
		if (dataSource == null) {
			dataSource = webApplicationContext.getBean("dataSource", DataSource.class);
		}
		
		return this.dataSource;
	}
	
	private BindingHistoryDao getBindingHistoryDao() {
		if (bindingHistoryDao == null) {
			bindingHistoryDao = webApplicationContext.getBean("BindingHistoryDao", BindingHistoryDao.class);
		}
		
		return bindingHistoryDao;
	}
	
	private ComCompanyDao getCompanyDao() {
		if (companyDao == null) {
			companyDao = webApplicationContext.getBean("CompanyDao", ComCompanyDao.class);
		}
		
		return companyDao;
	}
	
	private final ConfigService getConfigService() {
		if(this.configService == null) {
			this.configService = webApplicationContext.getBean("ConfigService", ConfigService.class);
		}
		
		return this.configService;
	}
}
