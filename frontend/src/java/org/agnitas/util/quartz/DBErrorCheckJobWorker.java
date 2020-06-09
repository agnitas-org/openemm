/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util.quartz;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.JobWorker;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Example Insert in DB:
 *  INSERT INTO job_queue_tbl (id, description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted)
 *    VALUES ((SELECT MAX(id) + 1 FROM job_queue_tbl), 'DBErrorCheck', CURRENT_TIMESTAMP, NULL, 0, 'OK', 0, 0, '**00', CURRENT_TIMESTAMP, NULL, 'org.agnitas.util.quartz.DBErrorCheckJobWorker', 1);
 */
public class DBErrorCheckJobWorker extends JobWorker {
	private static final transient Logger logger = Logger.getLogger(DBErrorCheckJobWorker.class);
	
	@Override
	public String runJob() throws Exception {
		DataSource datasource = daoLookupFactory.getBeanDataSource();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource);
		List<Map<String, Object>> result;
		if (previousJobStart != null) {
			result = jdbcTemplate.queryForList("SELECT co.company_id, co.shortname, el.errortext, el.module_name, client_info, count(*) cnt"
			+ " FROM emm_db_errorlog_tbl el"
			+ " LEFT JOIN company_tbl co ON co.company_id = el.company_id"
			+ " WHERE el.creation_date > ?"
			+ " GROUP BY co.company_id, co.shortname, el.errortext, el.module_name, client_info"
			+ " ORDER BY cnt DESC",
			previousJobStart);
		} else {
			result = jdbcTemplate.queryForList("SELECT co.company_id, co.shortname, el.errortext, el.module_name, client_info, count(*) cnt"
			+ " FROM emm_db_errorlog_tbl el"
			+ " LEFT JOIN company_tbl co ON co.company_id = el.company_id"
			+ " WHERE co.company_id = el.company_id"
			+ " GROUP BY co.company_id, co.shortname, el.errortext, el.module_name, client_info"
			+ " ORDER BY cnt DESC");
		}
		
		if (result.size() > 0) {
			logger.error("Found " + result.size() + " db errors in emm_db_errorlog_tbl");
			
			String infoMailAddress = job.getParameters().get("infoMailAddress");
			if (StringUtils.isBlank(infoMailAddress)) {
				infoMailAddress = configService.getValue(ConfigValue.Mailaddress_Error);
			}
			
			String infoMailSubject = job.getParameters().get("infoMailSubject");
			if (StringUtils.isBlank(infoMailSubject)) {
				infoMailSubject = "[CRITICAL] EMM DB Errorlog";
			}
			infoMailSubject += " (Host: " + AgnUtils.getHostName() + ")";

			StringBuilder infoMailContent = new StringBuilder();
			infoMailContent.append("CompanyID;Shortname;Errortext;ModuleName;ClientInfo;Count\n");
			for (Map<String, Object> row : result) {
				infoMailContent.append(row.get("cnt") + ": " + row.get("shortname") + " (" + row.get("company_id") + ") " + row.get("module_name") + ": " + row.get("client_info") + "\n\t-> " + row.get("errortext") + "\n");
			}
			
			if (StringUtils.isNotBlank(infoMailAddress)) {
				serviceLookupFactory.getBeanJavaMailService().sendEmail(infoMailAddress, infoMailSubject, infoMailContent.toString(), infoMailContent.toString());
			}
		}
		
		return null;
	}
}
