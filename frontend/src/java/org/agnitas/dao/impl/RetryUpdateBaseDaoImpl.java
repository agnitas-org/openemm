/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import java.io.InputStream;
import java.util.List;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.jdbc.support.KeyHolder;

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
	@SuppressWarnings("unused")
	private static final transient Logger retryUpdateBaseDaoImplLogger = Logger.getLogger(RetryUpdateBaseDaoImpl.class);
	
	protected ConfigService configService;
	
	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	protected int retryableUpdate(int companyID, Logger logger, String statement, Object... parameter) {
		int retryCount = 0;
		while (true) {
			try {
				return super.update(logger, statement, parameter);
			} catch (ConcurrencyFailureException e) {
				int maxRetryCount = configService.getIntegerValue(ConfigValue.DbMaximumDeadlockRetries, companyID);
				
				if (retryCount <= maxRetryCount) {
					retryCount++;
					logSqlRetry(e, retryCount, maxRetryCount, logger, statement, parameter);
					int retryWaitSeconds = configService.getIntegerValue(ConfigValue.DbDeadlockRetriesWaitSeconds, companyID);
					try {
						Thread.sleep(retryWaitSeconds * 1000);
					} catch (InterruptedException e1) {
						// do nothing
					}
				} else {
					throw e;
				}
			}
		}
	}

	protected int retryableUpdate(int companyID, Logger logger, String statement, KeyHolder keys, String[] keyColumns, Object... parameter) {
		int retryCount = 0;
		while (true) {
			try {
				return super.update(logger, statement, keys, keyColumns, parameter);
			} catch (ConcurrencyFailureException e) {
				int maxRetryCount = configService.getIntegerValue(ConfigValue.DbMaximumDeadlockRetries, companyID);
				
				if (retryCount <= maxRetryCount) {
					retryCount++;
					logSqlRetry(e, retryCount, maxRetryCount, logger, statement, parameter);
					int retryWaitSeconds = configService.getIntegerValue(ConfigValue.DbDeadlockRetriesWaitSeconds, companyID);
					try {
						Thread.sleep(retryWaitSeconds * 1000);
					} catch (InterruptedException e1) {
						// do nothing
					}
				} else {
					throw e;
				}
			}
		}
	}

	protected void retryableUpdateClob(int companyID, Logger logger, String statement, String clobData, Object... parameter) throws Exception {
		int retryCount = 0;
		while (true) {
			try {
				super.updateClob(logger, statement, clobData, parameter);
				return;
			} catch (ConcurrencyFailureException e) {
				int maxRetryCount = configService.getIntegerValue(ConfigValue.DbMaximumDeadlockRetries, companyID);
				
				if (retryCount <= maxRetryCount) {
					retryCount++;
					logSqlRetry(e, retryCount, maxRetryCount, logger, statement, parameter);
					int retryWaitSeconds = configService.getIntegerValue(ConfigValue.DbDeadlockRetriesWaitSeconds, companyID);
					try {
						Thread.sleep(retryWaitSeconds * 1000);
					} catch (InterruptedException e1) {
						// do nothing
					}
				} else {
					throw e;
				}
			}
		}
	}

	protected void retryableUpdateBlob(int companyID, Logger logger, String statement, byte[] blobData, Object... parameter) throws Exception {
		int retryCount = 0;
		while (true) {
			try {
				super.updateBlob(logger, statement, blobData, parameter);
				return;
			} catch (ConcurrencyFailureException e) {
				int maxRetryCount = configService.getIntegerValue(ConfigValue.DbMaximumDeadlockRetries, companyID);
				
				if (retryCount <= maxRetryCount) {
					retryCount++;
					logSqlRetry(e, retryCount, maxRetryCount, logger, statement, parameter);
					int retryWaitSeconds = configService.getIntegerValue(ConfigValue.DbDeadlockRetriesWaitSeconds, companyID);
					try {
						Thread.sleep(retryWaitSeconds * 1000);
					} catch (InterruptedException e1) {
						// do nothing
					}
				} else {
					throw e;
				}
			}
		}
	}

	protected void retryableUpdateBlob(int companyID, Logger logger, String statement, InputStream blobDataInputStream, Object... parameter) throws Exception {
		int retryCount = 0;
		while (true) {
			try {
				super.updateBlob(logger, statement, blobDataInputStream, parameter);
				return;
			} catch (ConcurrencyFailureException e) {
				int maxRetryCount = configService.getIntegerValue(ConfigValue.DbMaximumDeadlockRetries, companyID);
				
				if (retryCount <= maxRetryCount) {
					retryCount++;
					logSqlRetry(e, retryCount, maxRetryCount, logger, statement, parameter);
					int retryWaitSeconds = configService.getIntegerValue(ConfigValue.DbDeadlockRetriesWaitSeconds, companyID);
					try {
						Thread.sleep(retryWaitSeconds * 1000);
					} catch (InterruptedException e1) {
						// do nothing
					}
				} else {
					throw e;
				}
			}
		}
	}

	protected int[] retryableBatchupdate(int companyID, Logger logger, String statement, List<Object[]> values) {
		int retryCount = 0;
		while (true) {
			try {
				return super.batchupdate(logger, statement, values);
			} catch (ConcurrencyFailureException e) {
				int maxRetryCount = configService.getIntegerValue(ConfigValue.DbMaximumDeadlockRetries, companyID);
				
				if (retryCount <= maxRetryCount) {
					retryCount++;
					logSqlRetry(e, retryCount, maxRetryCount, logger, statement);
					int retryWaitSeconds = configService.getIntegerValue(ConfigValue.DbDeadlockRetriesWaitSeconds, companyID);
					try {
						Thread.sleep(retryWaitSeconds * 1000);
					} catch (InterruptedException e1) {
						// do nothing
					}
				} else {
					throw e;
				}
			}
		}
	}

	protected int[] retryableBatchInsertIntoAutoincrementMysqlTable(int companyID, Logger logger, String statement, List<Object[]> listOfValueArrays) throws Exception {
		int retryCount = 0;
		while (true) {
			try {
				return super.batchInsertIntoAutoincrementMysqlTable(logger, statement, listOfValueArrays);
			} catch (ConcurrencyFailureException e) {
				int maxRetryCount = configService.getIntegerValue(ConfigValue.DbMaximumDeadlockRetries, companyID);
				
				if (retryCount <= maxRetryCount) {
					retryCount++;
					logSqlRetry(e, retryCount, maxRetryCount, logger, statement);
					int retryWaitSeconds = configService.getIntegerValue(ConfigValue.DbDeadlockRetriesWaitSeconds, companyID);
					try {
						Thread.sleep(retryWaitSeconds * 1000);
					} catch (InterruptedException e1) {
						// do nothing
					}
				} else {
					throw e;
				}
			}
		}
	}

	protected int[] retryableBatchInsertIntoAutoincrementMysqlTable(int companyID, Logger logger, String autoincrementColumn, String statement, List<Object[]> parametersList) throws Exception {
		int retryCount = 0;
		while (true) {
			try {
				return super.batchInsertIntoAutoincrementMysqlTable(logger, autoincrementColumn, statement, parametersList);
			} catch (ConcurrencyFailureException e) {
				int maxRetryCount = configService.getIntegerValue(ConfigValue.DbMaximumDeadlockRetries, companyID);
				
				if (retryCount <= maxRetryCount) {
					retryCount++;
					logSqlRetry(e, retryCount, maxRetryCount, logger, statement);
					int retryWaitSeconds = configService.getIntegerValue(ConfigValue.DbDeadlockRetriesWaitSeconds, companyID);
					try {
						Thread.sleep(retryWaitSeconds * 1000);
					} catch (InterruptedException e1) {
						// do nothing
					}
				} else {
					throw e;
				}
			}
		}
	}

	protected int retryableInsertIntoAutoincrementMysqlTable(int companyID, Logger logger, String autoincrementColumn, String statement, Object... parameter) {
		int retryCount = 0;
		while (true) {
			try {
				return super.insertIntoAutoincrementMysqlTable(logger, autoincrementColumn, statement, parameter);
			} catch (ConcurrencyFailureException e) {
				int maxRetryCount = configService.getIntegerValue(ConfigValue.DbMaximumDeadlockRetries, companyID);
				
				if (retryCount <= maxRetryCount) {
					retryCount++;
					logSqlRetry(e, retryCount, maxRetryCount, logger, statement, parameter);
					int retryWaitSeconds = configService.getIntegerValue(ConfigValue.DbDeadlockRetriesWaitSeconds, companyID);
					try {
						Thread.sleep(retryWaitSeconds * 1000);
					} catch (InterruptedException e1) {
						// do nothing
					}
				} else {
					throw e;
				}
			}
		}
	}

	protected int retryableInsertMultipleIntoAutoincrementMysqlTable(int companyID, Logger logger, String autoincrementColumn, String statement, Object... parameter) {
		int retryCount = 0;
		while (true) {
			try {
				return super.insertMultipleIntoAutoincrementMysqlTable(logger, autoincrementColumn, statement, parameter);
			} catch (ConcurrencyFailureException e) {
				int maxRetryCount = configService.getIntegerValue(ConfigValue.DbMaximumDeadlockRetries, companyID);
				
				if (retryCount <= maxRetryCount) {
					retryCount++;
					logSqlRetry(e, retryCount, maxRetryCount, logger, statement, parameter);
					int retryWaitSeconds = configService.getIntegerValue(ConfigValue.DbDeadlockRetriesWaitSeconds, companyID);
					try {
						Thread.sleep(retryWaitSeconds * 1000);
					} catch (InterruptedException e1) {
						// do nothing
					}
				} else {
					throw e;
				}
			}
		}
	}
	
	protected void logSqlRetry(Exception e, int retryCount, int maxRetryCount, Logger logger, String statement, Object... parameter) {
		if (parameter != null && parameter.length > 0) {
			logger.error("Deadlock detected\nRetrying statement: " + e.getMessage() + "\nRetry " + retryCount + "/" + maxRetryCount + "\nSQL: " + statement + "\nParameter: " + getParameterStringList(parameter), e);
		} else {
			logger.error("Deadlock detected\nRetrying statement: " + e.getMessage() + "\nRetry " + retryCount + "/" + maxRetryCount + "\nSQL: " + statement, e);
		}
		if (javaMailService != null) {
			javaMailService.sendExceptionMail(0, "Deadlock detected\nRetrying statement SQL: " + statement + "\nParameter: " + getParameterStringList(parameter), e);
		} else {
			logger.error("Missing javaMailService. So no erroremail was sent.");
		}
	}
}
