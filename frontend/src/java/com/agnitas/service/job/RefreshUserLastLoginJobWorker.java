/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.job;

import javax.sql.DataSource;

import org.agnitas.service.JobWorker;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * This JobWorker
 * - refreshes last login values of GUI users in db
 * - refreshes last login values of SOAP webservice users in db
 * - refreshes last login values of Restful webservice users in db
 * 
 * Example Insert in DB:
 *  INSERT INTO job_queue_tbl (id, description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted)
 *     VALUES ((SELECT MAX(id) + 1 FROM job_queue_tbl), 'RefreshUserLastLogin', CURRENT_TIMESTAMP, NULL, 0, 'OK', 0, 0, '**00', CURRENT_TIMESTAMP, NULL, 'com.agnitas.service.job.RefreshUserLastLoginJobWorker', 0);
 */
public class RefreshUserLastLoginJobWorker extends JobWorker {
	@Override
	public String runJob() throws Exception {
		DataSource dataSource = daoLookupFactory.getBeanDataSource();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		refreshGuiAndRestfulUserLastLogin(jdbcTemplate);
		refreshWsUserLastLogin(jdbcTemplate);
		
		return "OK";
	}

	private void refreshGuiAndRestfulUserLastLogin(JdbcTemplate jdbcTemplate) {
		if (previousJobStart == null) {
			jdbcTemplate.update("UPDATE admin_tbl SET last_login_date = (SELECT MAX(creation_date) FROM login_track_tbl WHERE login_track_tbl.username = admin_tbl.username)"
				+ " WHERE username IN (SELECT DISTINCT username FROM login_track_tbl)");
		} else {
			jdbcTemplate.update("UPDATE admin_tbl SET last_login_date = (SELECT MAX(creation_date) FROM login_track_tbl WHERE login_track_tbl.username = admin_tbl.username)"
				+ " WHERE username IN (SELECT DISTINCT username FROM login_track_tbl WHERE creation_date >= ?)", previousJobStart);
		}
	}

	private void refreshWsUserLastLogin(JdbcTemplate jdbcTemplate) {
		if (previousJobStart == null) {
			jdbcTemplate.update("UPDATE webservice_user_tbl SET last_login_date = (SELECT MAX(creation_date) FROM ws_login_track_tbl WHERE ws_login_track_tbl.username = webservice_user_tbl.username)"
				+ " WHERE username IN (SELECT DISTINCT username FROM ws_login_track_tbl)");
		} else {
			jdbcTemplate.update("UPDATE webservice_user_tbl SET last_login_date = (SELECT MAX(creation_date) FROM ws_login_track_tbl WHERE ws_login_track_tbl.username = webservice_user_tbl.username)"
				+ " WHERE username IN (SELECT DISTINCT username FROM ws_login_track_tbl WHERE creation_date >= ?)", previousJobStart);
		}
	}
}
