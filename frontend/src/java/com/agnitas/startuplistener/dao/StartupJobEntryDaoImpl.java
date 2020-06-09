/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.startuplistener.dao;

import java.util.List;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.log4j.Logger;

import com.agnitas.startuplistener.common.JobState;
import com.agnitas.startuplistener.common.StartupJobEntry;

public final class StartupJobEntryDaoImpl extends BaseDaoImpl implements StartupJobEntryDao {

	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(StartupJobEntryDaoImpl.class);
	
	@Override
	public final List<StartupJobEntry> listActiveAndPendingJobs() {
		final String sql = "SELECT * FROM startup_job_tbl WHERE enabled = 1 AND state = ?";
		
		return select(LOGGER, sql, new StartupJobEntryRowMapper(), JobState.PENDING.getCode());
	}

	@Override
	public final void updateJobState(final int id, final JobState newState) {
		final String sql = "UPDATE startup_job_tbl SET state = ?, change_date = CURRENT_TIMESTAMP WHERE id = ?";
		
		update(LOGGER, sql, newState.getCode(), id);
	}

}
