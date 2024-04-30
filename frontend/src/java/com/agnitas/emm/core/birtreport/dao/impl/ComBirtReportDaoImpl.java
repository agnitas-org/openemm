/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.dao.impl;

import static org.agnitas.util.DbUtilities.joinForIn;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.birtreport.bean.BirtReportFactory;
import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.bean.ComLightweightBirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings;
import com.agnitas.emm.core.birtreport.bean.impl.ComLightweightBirtReportImpl;
import com.agnitas.emm.core.birtreport.dao.ComBirtReportDao;
import com.agnitas.emm.core.birtreport.dto.BirtReportType;
import com.agnitas.emm.core.birtreport.dto.FilterType;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;

public class ComBirtReportDaoImpl extends PaginatedBaseDaoImpl implements ComBirtReportDao {

	private static final Logger logger = LogManager.getLogger(ComBirtReportDaoImpl.class);
	
	private BirtReportFactory birtReportFactory;

	@Override
	@DaoUpdateReturnValueCheck
	public boolean insert(ComBirtReport report) throws Exception {
		if (report.getId() != 0) {
			logger.error("ReportID is invalid for insert of new report: {}", report.getId());
			return false;
		} else if (CollectionUtils.isEmpty(report.getEmailRecipientList())) {
			throw new Exception("Recipients for report are empty: " + report.getId());
		}
		
		report.calculateSendDate();
		
		if (isOracleDB()) {
			int newReportID = selectInt(logger, "SELECT birtreport_tbl_seq.NEXTVAL FROM DUAL");
			report.setId(newReportID);
			if (report.getId() == 0) {
				return false;
			}
			
			String sql = "INSERT INTO birtreport_tbl (report_id, company_id, shortname, description, active, report_type, format, email_subject, email_description, activation_date, end_date, active_tab, language, hidden, intervalpattern, nextstart, change_date)"
					+ " VALUES (" + AgnUtils.repeatString("?", 16, ", ") + ", CURRENT_TIMESTAMP)";
			try {
				update(logger, sql,
					report.getId(),
					report.getCompanyID(),
					report.getShortname(),
					report.getDescription(),
					report.isReportActive(),
					report.getReportType(),
					report.getFormat(),
					report.getEmailSubject(),
					report.getEmailDescription(),
					report.getActivationDate(),
					report.getEndDate(),
					report.getActiveTab(),
					report.getLanguage(),
					BooleanUtils.toInteger(report.isHidden()),
					report.getIntervalpattern(),
					report.getNextStart());
				
				storeBirtReportEmailRecipients(report);
			} catch (Exception e) {
				logger.error("Error inserting report", e);
				// logging is already done
				return false;
			}
		} else {
			String statement = "INSERT INTO birtreport_tbl (company_id, shortname, description, active, report_type, format, email_subject, email_description, activation_date, end_date, active_tab, language, hidden, intervalpattern, nextstart, change_date)"
					+ " VALUES (" + AgnUtils.repeatString("?", 15, ", ") + ", CURRENT_TIMESTAMP)";
			
			try {
				int reportID = insertIntoAutoincrementMysqlTable(logger, "report_id", statement,
					report.getCompanyID(),
					report.getShortname(),
					report.getDescription(),
					report.isReportActive(),
					report.getReportType(),
					report.getFormat(),
					report.getEmailSubject(),
					report.getEmailDescription(),
					report.getActivationDate(),
					report.getEndDate(),
					report.getActiveTab(),
					report.getLanguage(),
					BooleanUtils.toInteger(report.isHidden()),
					report.getIntervalpattern(),
					report.getNextStart()
				);

				report.setId(reportID);
				
				storeBirtReportEmailRecipients(report);
			} catch (RuntimeException e) {
				// logging is already done
	    		return false;
	    	}
		}

		try {
			report.getSettings().forEach(setting -> insertReportProperties(report.getId(), setting));
			return true;
		} catch (Exception e) {
			logger.error("error in insert: {}", e, e);
			return false;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean update(ComBirtReport report) throws Exception {
		return update(report, Collections.emptyList());
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public boolean update(ComBirtReport report, List<Integer> justDeactivateSettingTypes) throws Exception {
		if (CollectionUtils.isEmpty(report.getEmailRecipientList())) {
			throw new Exception("Recipients for report are empty: " + report.getId());
		}
	
		report.calculateSendDate();
		
		String sql = "UPDATE birtreport_tbl SET shortname = ?, description = ?, active = ?, report_type = ?, format = ?, email_subject = ?, email_description = ?, activation_date = ?, end_date = ?, active_tab = ?, language = ?, intervalpattern = ?, nextstart = ?, change_date = CURRENT_TIMESTAMP WHERE report_id = ? AND company_id = ?";

		try {
			update(logger, sql,
				report.getShortname(),
				report.getDescription(),
				report.isReportActive(),
				report.getReportType(),
				report.getFormat(),
				report.getEmailSubject(),
				report.getEmailDescription(),
				report.getActivationDate(),
				report.getEndDate(),
				report.getActiveTab(),
				report.getLanguage(),
				report.getIntervalpattern(),
				report.getNextStart(),
				report.getId(),
				report.getCompanyID());
			
			deleteReportParameters(report, justDeactivateSettingTypes);
			deactivateReportSettings(report.getId(), justDeactivateSettingTypes);
			report.getSettings().stream()
					.filter(setting -> !justDeactivateSettingTypes.contains(setting.getTypeId()))
					.forEach(setting -> insertReportProperties(report.getId(), setting));

			storeBirtReportEmailRecipients(report);
			
			return true;
		} catch (Exception e) {
			logger.error("Error updating report {}", report.getId(), e);
			// logging is already done
			return false;
		}
	}

	private void storeBirtReportEmailRecipients(ComBirtReport report) {
		update(logger, "DELETE FROM birtreport_recipient_tbl WHERE birtreport_id = ?", report.getId());
		if (report.getEmailRecipientList() != null) {
			for (String email : AgnUtils.removeObsoleteItemsFromList(report.getEmailRecipientList())) {
				update(logger, "INSERT INTO birtreport_recipient_tbl (birtreport_id, email) VALUES (?, ?)", report.getId(), email);
			}
		}
	}
	
	@Override
    public void deactivateReportSettings(int reportId, Collection<Integer> settingsType) {
        if (CollectionUtils.isNotEmpty(settingsType)) {
            update(logger, "UPDATE birtreport_parameter_tbl SET parameter_value = ? " +
					"WHERE report_id = ? AND parameter_name = ? AND report_type IN (" + joinForIn(settingsType) + ")",
                    "false", reportId, ComBirtReportSettings.ENABLED_KEY);
        }
    }

    @Override
    public void updateReportMailinglists(int reportId, int reportType, List<Integer> mailinglistIds) {
        String mailinglistIdsString = mailinglistIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        update(logger, "UPDATE birtreport_parameter_tbl SET parameter_value = ? " +
                        "WHERE report_id = ? AND report_type = ? AND parameter_name = ?",
                mailinglistIdsString.isEmpty() ? " " : mailinglistIdsString, reportId, reportType, ComBirtReportSettings.MAILINGLISTS_KEY);
    }

	@DaoUpdateReturnValueCheck
	public void deleteReportParameters(ComBirtReport report) {
		deleteReportParameters(report, Collections.emptyList());
	}

	public void deleteReportParameters(ComBirtReport report, List<Integer> skipReportSettings) {
		try {
			String deletionSql = "DELETE FROM birtreport_parameter_tbl WHERE report_id = ?";
			if (CollectionUtils.isNotEmpty(skipReportSettings)) {
				deletionSql += " AND report_type NOT IN (" + joinForIn(skipReportSettings) + ")" ;
			}
			update(logger, deletionSql, report.getId());
		} catch (Exception e) {
			// logging is already done
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void insertSentMailings(Integer reportId, Integer companyID, List<Integer> sentMailings) {
		if (sentMailings == null || sentMailings.isEmpty()) {
			update(logger, "INSERT INTO birtreport_sent_mailings_tbl (report_id, company_id, mailing_id, delivery_date) VALUES (?, ?, NULL, CURRENT_TIMESTAMP)", reportId, companyID);
		} else {
			String statement = "INSERT INTO birtreport_sent_mailings_tbl (report_id, company_id, mailing_id, delivery_date) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
			List<Object[]> batchArgs = new ArrayList<>();
	
			for (Integer mailingID : sentMailings) {
				batchArgs.add(new Object[] { reportId, companyID, mailingID });
			}
	
			batchupdate(logger, statement, batchArgs);
		}
	}

    @Override
    public List<Map<String, Object>> getReportParamValues(int companyID, String paramName) {
        String query = "SELECT DISTINCT param.report_id, param.parameter_value, param.report_type " +
                "FROM birtreport_parameter_tbl param " +
                "INNER JOIN birtreport_tbl rep " +
                "ON rep.report_id = param.report_id " +
                "WHERE rep.company_id = ? " +
                "AND param.parameter_name = ? " +
                "AND param.parameter_value NOT IN (' ') " +
                "ORDER BY param.report_id";
        return select(logger, query, companyID, paramName);
    }

	@Override
	public List<ComLightweightBirtReport> getLightweightBirtReportsBySelectedTarget(int companyID, int targetGroupID) {
		String query = "SELECT DISTINCT param.report_id, rep.shortname, rep.description, rep.hidden FROM birtreport_parameter_tbl param " +
				"INNER JOIN birtreport_tbl rep " +
				"ON rep.report_id = param.report_id " +
				"WHERE rep.company_id = ? " +
				"AND param.parameter_name = 'selectedTargets' " +
				"AND param.parameter_value NOT IN (' ') " +
				(isOracleDB() ?
					"AND INSTR(',' || parameter_value || ',', ',' || ? || ',') <> 0 "
					:
					"AND INSTR(CONCAT(',', parameter_value, ','), CONCAT(',', ?, ',')) <> 0 "
				) +
				"ORDER BY param.report_id";

		return select(logger, query, new ComLightweightBirtReportRowMapper(), companyID, targetGroupID);
	}

	@Override
	public List<ComLightweightBirtReport> getLightweightBirtReportList(int companyID) {
		String sqlGetReports = "SELECT report_id, shortname, description, hidden FROM birtreport_tbl " +
				"WHERE company_id = ? ORDER BY LOWER(shortname)";
		return select(logger, sqlGetReports, new ComLightweightBirtReportRowMapper(), companyID);
	}
	
    @DaoUpdateReturnValueCheck
	private void insertReportProperties(int reportId, ComBirtReportSettings comBirtReportSettings) {
		String statement = "INSERT INTO birtreport_parameter_tbl (report_id, report_type, parameter_name, parameter_value) VALUES (?, ?, ?, ?)";
		ReportSettingsType type = comBirtReportSettings.getReportSettingsType();
		Map<String, Object> settingsMap = comBirtReportSettings.getSettingsMap();
		
		List<Object[]> batchArgs = new ArrayList<>();
		
		settingsMap.entrySet().stream()
				.filter(pair -> Objects.nonNull(pair.getKey()) && Objects.nonNull(pair.getValue()))
				.filter(pair -> StringUtils.isNotEmpty(pair.getValue().toString()))
				.map(entry -> new Object[] {reportId, type.getKey(), entry.getKey(), entry.getValue().toString()})
				.forEach(batchArgs::add);
		
		batchupdate(logger, statement, batchArgs);
	}

	protected void getReportProperties(ComBirtReport report) {
		String query = "SELECT report_type, parameter_name, parameter_value FROM birtreport_parameter_tbl WHERE report_id = ?";
		List<Map<String, Object>> list = select(logger, query, report.getId());

		try {
			for (Map<String, Object> row : list) {
				String parameterName = (String) row.get("parameter_name");
				Object parameterValue = row.get("parameter_value");
				ReportSettingsType reportSettingsType = ReportSettingsType.getTypeByCode(((Number) row.get("report_type")).intValue());
				report.setSettingParameter(reportSettingsType, parameterName, parameterValue);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	protected class ComBirtReportRowMapper implements RowMapper<ComBirtReport> {
		@Override
		public ComBirtReport mapRow(ResultSet resultSet, int row) throws SQLException {
			try {
				ComBirtReport report = birtReportFactory.createReport();
				report.setId(resultSet.getInt("report_id"));
				report.setCompanyID(resultSet.getInt("company_id"));
				report.setShortname(resultSet.getString("shortname"));
				report.setDescription(resultSet.getString("description"));
				report.setReportActive(resultSet.getInt("active"));
				report.setReportType(resultSet.getInt("report_type"));
				report.setFormat(resultSet.getInt("format"));
				report.setEmailSubject(resultSet.getString("email_subject"));
				report.setEmailDescription(resultSet.getString("email_description"));
				report.setActivationDate(resultSet.getTimestamp("activation_date"));
				report.setEndDate(resultSet.getTimestamp("end_date"));
				report.setActiveTab(resultSet.getInt("active_tab"));
				report.setLanguage(resultSet.getString("language"));
				report.setIntervalpattern(resultSet.getString("intervalpattern"));
				report.setNextStart(resultSet.getTimestamp("nextstart"));
				report.setHidden(resultSet.getInt("hidden") == 1);
				
				report.setEmailRecipientList(select(logger, "SELECT email FROM birtreport_recipient_tbl WHERE birtreport_id = ?", StringRowMapper.INSTANCE, report.getId()));

				return report;
			} catch (Exception e) {
				throw new SQLException("Cannot read BIRT report", e);
			}
		}
	}

    protected class ComLightweightBirtReportRowMapper implements RowMapper<ComLightweightBirtReport> {
        @Override
        public ComLightweightBirtReport mapRow(ResultSet resultSet, int row) throws SQLException {
            try {
                ComLightweightBirtReport report = new ComLightweightBirtReportImpl();
                report.setId(resultSet.getBigDecimal("report_id").intValue());
                report.setShortname(resultSet.getString("shortname"));
                report.setDescription(resultSet.getString("description"));
				report.setHidden(resultSet.getInt("hidden") == 1);

                return report;
            } catch (Exception e) {
                throw new SQLException("Cannot read BIRT report", e);
            }
        }
    }
	
    @Required
	public void setBirtReportFactory(BirtReportFactory birtReportFactory) {
		this.birtReportFactory = birtReportFactory;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int resetBirtReportsForCurrentHost() {
		return update(logger,
			"UPDATE birtreport_tbl SET running = 0, nextstart = CURRENT_TIMESTAMP WHERE lasthostname = ? AND running = 1",
			AgnUtils.getHostName());
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	@Transactional
	public final boolean announceStart(final ComBirtReport birtReport) {
		if(birtReport.getId() <= 0) {
			return false;
		}
		
		try {
			// Lock rows against modification by other sessions
			final String updateJobStatusSql = "UPDATE birtreport_tbl SET running = 1, nextstart = ?, lasthostname = ?, laststart = CURRENT_TIMESTAMP WHERE report_id = ? AND running <= 0";
			final Timestamp nextStartTimestamp = birtReport.getNextStart() != null ? new Timestamp(birtReport.getNextStart().getTime()) : null;
			int touchedLines = update(logger, updateJobStatusSql, nextStartTimestamp, AgnUtils.getHostName(), birtReport.getId());
			return touchedLines == 1;
		} catch(final Exception e) {
			logger.error("Error while setting birtreport status", e);
			throw new RuntimeException("Error while setting birtreport status", e);
		}
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public void announceEnd(ComBirtReport birtReport) {
		update(logger, "UPDATE birtreport_tbl SET running = 0, lastresult = ? WHERE report_id = ?", AgnUtils.shortenStringToMaxLength(birtReport.getLastresult(), 512), birtReport.getId());
	}

	@Override
	public int getRunningReportsByHost(String hostname) {
		return selectInt(logger, "SELECT COUNT(*) FROM birtreport_tbl WHERE running = 1 AND lasthostname = ?", hostname);
	}

	@Override
	public List<ComBirtReport> getReportsToSend(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds) {
		// Check for parameter "predefineMailing" vs. mailinglist_tbl is for reports which should be sent after any mailing delivery was triggered for this mailinglist.
		// The parameter "predefineMailing" then contains a mailinglist_id, which is very inconvenient but was really implemented this way (mailingFilter = 2)
		String query =
			"SELECT * FROM birtreport_tbl WHERE"
				+ " ("
					+ "nextstart < CURRENT_TIMESTAMP"
					+ " OR "
						+ "(report_type IN (" +
					BirtReportType.TYPE_AFTER_MAILING_24HOURS.getKey() + ", " +
					BirtReportType.TYPE_AFTER_MAILING_48HOURS.getKey() + ", " +
					BirtReportType.TYPE_AFTER_MAILING_WEEK.getKey() + ")"
						+ " AND (nextstart IS NULL OR nextstart < CURRENT_TIMESTAMP)"
						+ " AND "
							+ "("
								+ "NOT EXISTS (SELECT 1 FROM birtreport_sent_mailings_tbl WHERE birtreport_tbl.report_id = birtreport_sent_mailings_tbl.report_id)"
								+ " OR "
								+ "NOT EXISTS (SELECT 1 FROM birtreport_parameter_tbl WHERE birtreport_tbl.report_id = birtreport_parameter_tbl.report_id AND parameter_name = '" + ComBirtReportSettings.PREDEFINED_ID_KEY + "')"
								+ " OR "
								+ "EXISTS (SELECT 1 FROM birtreport_parameter_tbl WHERE birtreport_tbl.report_id = birtreport_parameter_tbl.report_id AND parameter_name = 'mailingFilter' AND parameter_value = " + FilterType.FILTER_MAILINGLIST.getKey() + ")"
							+ ")"
						+ ")"
				+ ")"
				+ " AND active = 1"
				+ " AND running = 0"
				+ (includedCompanyIds != null && !includedCompanyIds.isEmpty() ? " AND company_id IN (" + StringUtils.join(includedCompanyIds, ", ") + ")" : "")
				+ (excludedCompanyIds != null && !excludedCompanyIds.isEmpty() ? " AND company_id NOT IN (" + StringUtils.join(excludedCompanyIds, ", ") + ")" : "")
				+ " AND (end_date IS NULL OR end_date > CURRENT_TIMESTAMP)"
				+ (isOracleDB() ? " ORDER BY nextstart" : " ORDER BY nextstart IS NULL, nextstart ASC");

		List<ComBirtReport> reportList = select(logger, query, new ComBirtReportRowMapper());
		
		for (ComBirtReport report : reportList) {
			if (report != null) {
				getReportProperties(report);
			}
		}
		
		return reportList;
	}

	@Override
	public void deactivateBirtReport(int reportID) {
		update(logger, "UPDATE birtreport_tbl SET active = 0 WHERE report_id = ?", reportID);
	}

	@Override
	public List<ComBirtReport> selectErroneousReports() {
		try {
			return select(logger, "SELECT * FROM birtreport_tbl WHERE active > 0 AND (end_date IS NULL OR end_date > CURRENT_TIMESTAMP) AND ((lastresult IS NOT NULL AND lastresult != 'OK') OR (nextstart IS NOT NULL AND nextstart < ?))", new ComBirtReportRowMapper(), DateUtilities.getDateOfHoursAgo(1));
		} catch (Exception e) {
			throw new RuntimeException("Error while reading erroneous reports from database", e);
		}
	}
}
