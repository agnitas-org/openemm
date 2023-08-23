/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.agnitas.beans.CompaniesConstraints;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;

import org.agnitas.util.DbUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.birtreport.bean.BirtReportFactory;
import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.workflow.dao.ComWorkflowReportScheduleDao;

public class ComWorkflowReportScheduleDaoImpl extends BaseDaoImpl implements ComWorkflowReportScheduleDao {

	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ComWorkflowReportScheduleDaoImpl.class);
	
	private BirtReportFactory birtReportFactory;

	@Override
	@DaoUpdateReturnValueCheck
	public void scheduleWorkflowReport(int reportId, int companyId, int workflowId, Date sendTime) {
		String sql = "INSERT INTO workflow_report_schedule_tbl (report_id, company_id, workflow_id, send_date) VALUES (?, ?, ?, ?)";
		update(logger, sql, reportId, companyId, workflowId, sendTime);
	}

	@Override
    @Deprecated
	public List<Integer> getAllWorkflowReportsToSend() {
        String query = "SELECT report_id FROM workflow_report_schedule_tbl WHERE send_date < CURRENT_TIMESTAMP AND sent = 0";
        return select(logger, query, IntegerRowMapper.INSTANCE);
	}

    @Override
    @Deprecated
    public List<ComBirtReport> getAllWorkflowBirtReportsToSend() {
        String query = "SELECT report_id, company_id FROM workflow_report_schedule_tbl WHERE send_date < CURRENT_TIMESTAMP AND sent = 0";
        return select(logger, query, new ScheduledBirtReportRowMapper());
    }

    @Override
    public List<Integer> getAllWorkflowBirtReportIdsToSend(int maximumNumberOfReports, CompaniesConstraints constraints) {
        String query = "SELECT wrs.report_id " +
                "       FROM workflow_report_schedule_tbl wrs " +
                "         LEFT JOIN birtreport_tbl br " +
                "           ON wrs.report_id = br.report_id AND wrs.company_id = br.company_id" +
                "       WHERE wrs.send_date < CURRENT_TIMESTAMP " +
                "         AND wrs.sent = 0 " +
				"         AND running = 0" +
                DbUtilities.asCondition(" AND %s", constraints, "wrs.company_id");

        String selectStatement;
        if (isOracleDB()) {
			selectStatement = "SELECT * FROM (SELECT selection.*, rownum AS r FROM (" + query + ") selection) WHERE r <= ?";
		} else {
			selectStatement = query + " LIMIT ?";
		}
        
        return select(logger, selectStatement, IntegerRowMapper.INSTANCE, maximumNumberOfReports);
    }

	@Override
	@DaoUpdateReturnValueCheck
	public void markWorkflowReportsSent(List<Integer> reportIds) {
		ArrayList<Object[]> updateList = new ArrayList<>();
		for (Integer reportId : reportIds) {
			updateList.add(new Object[] {reportId} );
		}
		String sql = "UPDATE workflow_report_schedule_tbl SET sent = 1 WHERE report_id = ?";
		batchupdate(logger, sql, updateList);
	}

    protected class ScheduledBirtReportRowMapper implements RowMapper<ComBirtReport> {
        @Override
        public ComBirtReport mapRow(ResultSet resultSet, int i) throws SQLException {
            ComBirtReport report = birtReportFactory.createReport();
            report.setCompanyID(resultSet.getInt("company_id"));
            report.setId(resultSet.getInt("report_id"));
            return report;
        }
    }
    
    @Required
    public void setBirtReportFactory(BirtReportFactory birtReportFactory) {
        this.birtReportFactory = birtReportFactory;
    }
}
