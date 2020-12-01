/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util.quartz;

import org.agnitas.emm.core.logintracking.dao.LoginTrackDao;
import org.apache.log4j.Logger;

public class WebserviceLoginTrackTableCleanerJobWorker extends AbstractLoginTrackTableCleanerJobWorker {
	
	/**
	 * Default value of retention time (in hours) for old records.
	 */
	public static final int DEFAULT_RETENTION_TIME_HOURS = 24;
	
	/**
	 * Number of records deleted with one statement.
	 */
	public static final int DEFAULT_DELETE_BLOCK_SIZE = 250_000;

	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(WebserviceLoginTrackTableCleanerJobWorker.class);
	
	@Override
	public LoginTrackDao getLoginTrackDao() {
		return daoLookupFactory.getBeanWsLoginTrackDao();
	}

	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	@Override
	public int getDefaultRetentionTimeHours() {
		return DEFAULT_RETENTION_TIME_HOURS;
	}

	@Override
	public int getDefaultBlockSize() {
		return DEFAULT_DELETE_BLOCK_SIZE;
	}
}
