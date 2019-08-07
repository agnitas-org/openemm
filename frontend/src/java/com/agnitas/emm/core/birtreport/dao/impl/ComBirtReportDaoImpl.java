/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.bean.ComLightweightBirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportComparisonSettings;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportImpl;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportRecipientSettings;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings;
import com.agnitas.emm.core.birtreport.bean.impl.ComLightweightBirtReportImpl;
import com.agnitas.emm.core.birtreport.dao.ComBirtReportDao;

public class ComBirtReportDaoImpl extends PaginatedBaseDaoImpl implements ComBirtReportDao {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComBirtReportDaoImpl.class);

	@Override
	@DaoUpdateReturnValueCheck
	public boolean insert(ComBirtReport report) {
		if (report.getId() != 0) {
			logger.error("ReportID is invalid for insert of new report: " + report.getId());
			return false;
		}
		
		report.calculateSendDate();
		
		if (isOracleDB()) {
			int newReportID = selectInt(logger, "SELECT birtreport_tbl_seq.NEXTVAL FROM DUAL");
			report.setId(newReportID);
			if (report.getId() == 0) {
				return false;
			}
			
			String sql = "INSERT INTO birtreport_tbl (report_id, company_id, shortname, description, active, report_type, send_days, format, email, email_subject, email_description, send_time, send_date, activation_date, end_date, active_tab, language, hidden, change_date)"
					+ " VALUES (" + AgnUtils.repeatString("?", 18, ", ") + ", CURRENT_TIMESTAMP)";
			try {
				update(logger, sql,
					report.getId(),
					report.getCompanyID(),
					report.getShortname(),
					report.getDescription(),
					report.isReportActive(),
					report.getReportType(),
					report.buildSendDate(),
					report.getFormat(),
					report.getSendEmail(),
					report.getEmailSubject(),
					report.getEmailDescription(),
					report.getSendTime(),
					report.getSendDate(),
					report.getActivationDate(),
					report.getEndDate(),
					report.getActiveTab(),
					report.getLanguage(),
					BooleanUtils.toInteger(report.isHidden()));
			} catch (Exception e) {
				logger.error("Error inserting report", e);
				// logging is already done
				return false;
			}
		} else {
			String statement = "INSERT INTO birtreport_tbl (company_id, shortname, description, active, report_type, send_days, format, email, email_subject, email_description, send_time, send_date, activation_date, end_date, active_tab, language, hidden)"
					+ " VALUES (" + AgnUtils.repeatString("?", 17, ", ") + ")";
			
			try {
				int reportID = insertIntoAutoincrementMysqlTable(logger, "report_id", statement,
					report.getCompanyID(),
					report.getShortname(),
					report.getDescription(),
					report.isReportActive(),
					report.getReportType(),
					report.buildSendDate(),
					report.getFormat(),
					report.getSendEmail(),
					report.getEmailSubject(),
					report.getEmailDescription(),
					report.getSendTime(),
					report.getSendDate(),
					report.getActivationDate(),
					report.getEndDate(),
					report.getActiveTab(),
					report.getLanguage(),
					BooleanUtils.toInteger(report.isHidden())
				);

				report.setId(reportID);
			} catch (RuntimeException e) {
				// logging is already done
	    		return false;
	    	}
		}

		try {
			insertReportProperties(report.getId(), report.getReportComparisonSettings());
			insertReportProperties(report.getId(), report.getReportMailingSettings());
			insertReportProperties(report.getId(), report.getReportRecipientSettings());
			return true;
		} catch (Exception e) {
			logger.error("error in insert: " + e, e);
			return false;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean update(ComBirtReport report) {
		report.calculateSendDate();
		
		String sql = "UPDATE birtreport_tbl SET shortname = ?, description = ?, active = ?, report_type = ?, send_days = ?, format = ?, email = ?, email_subject = ?, email_description = ?, send_time = ?, send_date = ?, activation_date = ?, end_date = ?, active_tab = ?, language = ?, change_date = CURRENT_TIMESTAMP, delivery_date = ? WHERE report_id = ? AND company_id = ?";

		try {
			update(logger, sql,
				report.getShortname(),
				report.getDescription(),
				report.isReportActive(),
				report.getReportType(),
				report.buildSendDate(),
				report.getFormat(),
				report.getSendEmail(),
				report.getEmailSubject(),
				report.getEmailDescription(),
				report.getSendTime(),
				report.getSendDate(),
				report.getActivationDate(),
				report.getEndDate(),
				report.getActiveTab(),
				report.getLanguage(),
				report.getDeliveryDate(),
				report.getId(),
				report.getCompanyID());
			
			deleteReportParameters(report);
			insertReportProperties(report.getId(), report.getReportComparisonSettings());
			insertReportProperties(report.getId(), report.getReportMailingSettings());
			insertReportProperties(report.getId(), report.getReportRecipientSettings());
			
			return true;
		} catch (Exception e) {
			logger.error("Error updating report " + report.getId(), e);
			// logging is already done
			return false;
		}
	}

	@DaoUpdateReturnValueCheck
	public void deleteReportParameters(ComBirtReport report) {
		try {
			update(logger, "DELETE FROM birtreport_parameter_tbl WHERE report_id = ?", report.getId());
		} catch (Exception e) {
			// logging is already done
		}
	}
    
    @Override
	public List<ComBirtReport> getAllReportToSend(Date date) {
		String query = "SELECT report_id, company_id, shortname, description, active, report_type, send_days, format, email, email_subject, email_description, send_time, send_date, activation_date, end_date, active_tab, language, hidden"
				+ " FROM birtreport_tbl WHERE active = 1 AND (send_date <= ? OR report_type IN (6, 7, 8)) AND (end_date IS NULL OR end_date >= ?)";

		List<ComBirtReport> list = select(logger, query, new ComBirtReportRowMapper(), date, date);
		
		for (ComBirtReport report : list) {
			getReportProperties(report);
		}
		
		return list;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void insertSentMailings(Integer reportId, Integer companyID, List<Integer> sentMailings) {
		String statement = "INSERT INTO birtreport_sent_mailings_tbl (report_id, company_id, mailing_id) VALUES (?, ?, ?)";
		
		List<Object[]> batchArgs = new ArrayList<>();
		
		for (Integer mailingID : sentMailings) {
			batchArgs.add(new Object[] { reportId, companyID, mailingID });
		}
		
		batchupdate(logger, statement, batchArgs);
	}

    @Override
    public List<Map<String, Object>> getReportParamValues(@VelocityCheck int companyID, String paramName) {
        String query = "SELECT DISTINCT param.report_id, param.parameter_value FROM birtreport_parameter_tbl param " +
                "INNER JOIN birtreport_tbl rep " +
                "ON rep.report_id = param.report_id " +
                "WHERE rep.company_id = ? " +
                "AND param.parameter_name = ? " +
                "AND param.parameter_value NOT IN (' ') " +
                "ORDER BY param.report_id";
        return select(logger, query, companyID, paramName);
    }

	@Override
	public List<ComLightweightBirtReport> getLightweightBirtReportsBySelectedTarget(@VelocityCheck int companyID, int targetGroupID) {
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
	public List<ComLightweightBirtReport> getLightweightBirtReportList(@VelocityCheck int companyID) {
		String sqlGetReports = "SELECT report_id, shortname, description, hidden FROM birtreport_tbl " +
				"WHERE company_id = ? ORDER BY LOWER(shortname)";
		return select(logger, sqlGetReports, new ComLightweightBirtReportRowMapper(), companyID);
	}
    @DaoUpdateReturnValueCheck
	private void insertReportProperties(Integer reportId, ComBirtReportSettings birtReportSettings) {
		String statement = "INSERT INTO birtreport_parameter_tbl (report_id, report_type, parameter_name, parameter_value) VALUES (?, ?, ?, ?)";
		int reportSettingsType = birtReportSettings.getReportSettingsType().getKey();
		Map<String, Object> settingsMap = birtReportSettings.getSettingsMap();
		
		List<Object[]> batchArgs = new ArrayList<>();
		
		settingsMap.entrySet().stream()
				.filter(pair -> Objects.nonNull(pair.getKey()) && Objects.nonNull(pair.getValue()))
				.filter(pair -> StringUtils.isNotEmpty(pair.getValue().toString()))
				.map(entry -> new Object[] {reportId, reportSettingsType, entry.getKey(), entry.getValue().toString()})
				.forEach(batchArgs::add);
		
		batchupdate(logger, statement, batchArgs);
	}

	protected void getReportProperties(ComBirtReport report) {
		String query = "SELECT report_type, parameter_name, parameter_value FROM birtreport_parameter_tbl WHERE report_id = ?";
		List<Map<String, Object>> list = select(logger, query, report.getId());
		ComBirtReportComparisonSettings comparisonSettings = report.getReportComparisonSettings();
		ComBirtReportMailingSettings mailingSettings = report.getReportMailingSettings();
		ComBirtReportRecipientSettings recipientSettings = report.getReportRecipientSettings();
		try {
			for (Map<String, Object> row : list) {
				String parameterName = (String) row.get("parameter_name");
				Object parameterValue = row.get("parameter_value");
				ReportSettingsType reportType = ReportSettingsType.getTypeByCode(((Number) row.get("report_type")).intValue());
				switch (reportType) {
					case COMPARISON:
						comparisonSettings.setReportSetting(parameterName, parameterValue);
						break;
					case MAILING:
						mailingSettings.setReportSetting(parameterName, parameterValue);
						break;
					case RECIPIENT:
						recipientSettings.setReportSetting(parameterName, parameterValue);
						break;
					default:
						//nothing do
						break;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	protected class ComBirtReportRowMapper implements RowMapper<ComBirtReport> {
		@Override
		public ComBirtReport mapRow(ResultSet resultSet, int row) throws SQLException {
			try {
				ComBirtReport report = new ComBirtReportImpl();
				report.setId(resultSet.getBigDecimal("report_id").intValue());
				report.setCompanyID(resultSet.getBigDecimal("company_id").intValue());
				report.setShortname(resultSet.getString("shortname"));
				report.setDescription(resultSet.getString("description"));
				report.setReportActive(resultSet.getBigDecimal("active").intValue());
				report.setReportType(resultSet.getBigDecimal("report_type").intValue());
				report.parseSendDays(resultSet.getString("send_days"));
				report.setFormat(resultSet.getBigDecimal("format").intValue());
				report.setSendEmail(resultSet.getString("email"));
				report.setEmailSubject(resultSet.getString("email_subject"));
				report.setEmailDescription(resultSet.getString("email_description"));
				report.setSendTime(resultSet.getTimestamp("send_time"));
				report.setSendDate(resultSet.getTimestamp("send_date"));
				report.setActivationDate(resultSet.getTimestamp("activation_date"));
				report.setEndDate(resultSet.getTimestamp("end_date"));
				report.setActiveTab(resultSet.getBigDecimal("active_tab").intValue());
				report.setLanguage(resultSet.getString("language"));
				report.setHidden(resultSet.getInt("hidden") == 1);

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
}
