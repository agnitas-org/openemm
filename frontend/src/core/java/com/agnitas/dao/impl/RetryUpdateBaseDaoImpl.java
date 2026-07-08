/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * Helper class which hides the dependency injection variables and eases some select and update actions and logging.
 * But still the datasource or the JdbcTemplate can be used directly if needed.
 * 
 * The logger of this class is not used for db actions to log, because it would hide the calling from the derived classes.
 * Therefore every simplified update and select method demands an logger delivered as parameter.
 * 
 * This class especially retries updates and inserts in tables, if they where temporarily blocked by other processes
 */
public abstract class RetryUpdateBaseDaoImpl extends BaseDaoImpl {
	
	protected ConfigService configService;
	
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	protected int retryableUpdate(int companyID, String statement, Object... parameter) {
		int retryCount = 0;
		while (true) {
			try {
				return super.update(statement, parameter);
			} catch (ConcurrencyFailureException e) {
				int maxRetryCount = configService.getIntegerValue(ConfigValue.DbMaximumDeadlockRetries, companyID);
				
				if (retryCount < maxRetryCount) {
					retryCount++;
					logSqlRetry(e, retryCount, maxRetryCount, statement, parameter);
					int retryWaitSeconds = configService.getIntegerValue(ConfigValue.DbDeadlockRetriesWaitSeconds, companyID);
					try {
						Thread.sleep(retryWaitSeconds * 1000L);
					} catch (InterruptedException e1) {
						// do nothing
					}
				} else {
					throw e;
				}
			}
		}
	}

	protected void logSqlRetry(Exception e, int retryCount, int maxRetryCount, String statement, Object... parameter) {
		if (parameter != null && parameter.length > 0) {
            logger.error("Deadlock detected\nRetrying statement: {}\nRetry {}/{}\nSQL: {}\nParameter: {}",
					e.getMessage(), retryCount, maxRetryCount, statement, getParameterStringList(parameter), e);
		} else {
            logger.error("Deadlock detected\nRetrying statement: {}\nRetry {}/{}\nSQL: {}",
					e.getMessage(), retryCount, maxRetryCount, statement, e);
		}
		if (javaMailService != null) {
			javaMailService.sendExceptionMail(0, "Deadlock detected\nRetrying statement SQL: " + statement + "\nParameter: " + getParameterStringList(parameter), e);
		} else {
			logger.error("Missing javaMailService. So no erroremail was sent.");
		}
	}
}
