/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.service.job;

import java.sql.Timestamp;
import java.util.List;
import javax.sql.DataSource;

import com.agnitas.service.JobWorkerBase;
import com.agnitas.util.quartz.JobWorker;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * This JobWorker
 * - refreshes last login values of GUI users in db
 * - refreshes last login values of SOAP webservice users in db
 * - refreshes last login values of Restful webservice users in db
 * 
 * Example Insert in DB:
 *  INSERT INTO job_queue_tbl (id, description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, job_name, deleted)
 *     VALUES ((SELECT MAX(id) + 1 FROM job_queue_tbl), 'RefreshUserLastLogin', CURRENT_TIMESTAMP, NULL, 0, 'OK', 0, 0, '**00', CURRENT_TIMESTAMP, NULL, 'RefreshUserLastLogin', 0);
 */
@JobWorker("RefreshUserLastLogin")
public class RefreshUserLastLoginJobWorker extends JobWorkerBase {

	private record LastLogin(String username, Timestamp loginDate) {
	}

	@Override
	public String runJob() {
		DataSource dataSource = daoLookupFactory.getBeanDataSource();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		refreshLastLogin(jdbcTemplate, "login_track_tbl", "admin_tbl");
		refreshLastLogin(jdbcTemplate, "ws_login_track_tbl", "webservice_user_tbl");

		return "OK";
	}

	private void refreshLastLogin(JdbcTemplate jdbcTemplate, String loginTrackTable, String userTable) {
		List<LastLogin> lastLogins = findLastLogins(jdbcTemplate, loginTrackTable);
		if (lastLogins.isEmpty()) {
			return;
		}

		jdbcTemplate.batchUpdate("UPDATE " + userTable + " SET last_login_date = ? WHERE username = ?", lastLogins, 100,
			(ps, lastLogin) -> {
				ps.setTimestamp(1, lastLogin.loginDate());
				ps.setString(2, lastLogin.username());
			});
	}

	private List<LastLogin> findLastLogins(JdbcTemplate jdbcTemplate, String loginTrackTable) {
		RowMapper<LastLogin> rowMapper = (rs, i) -> new LastLogin(
				rs.getString("username"),
				rs.getTimestamp("last_login")
		);

		String sql = "SELECT username, MAX(creation_date) AS last_login FROM " + loginTrackTable + " WHERE username IS NOT NULL";

		if (previousJobStart == null) {
			return jdbcTemplate.query(sql + " GROUP BY username", rowMapper);
		}

		return jdbcTemplate.query(sql + " AND creation_date >= ? GROUP BY username", rowMapper, new Timestamp(previousJobStart.getTime()));
	}
}
