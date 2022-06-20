/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.importvalues.MailType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.emm.core.birtstatistics.monthly.MonthlyStatType;
import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.MonthCounterStatRow;
import com.agnitas.reporting.birt.external.beans.MonthDetailStatRow;
import com.agnitas.reporting.birt.external.beans.MonthTotalStatRow;

public class MonthStatDataSet extends BIRTDataSet {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(MonthStatDataSet.class);

	public List<MonthCounterStatRow> getMailingCounts(@VelocityCheck int companyID, int adminId, String startDateString, String endDateString) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date startDate = dateFormat.parse(startDateString);
		Date endDate = DateUtilities.addDaysToDate(dateFormat.parse(endDateString), 1);
		List<Object> params = new ArrayList<>();
		
		String query = "SELECT COUNT(DISTINCT a.mailing_id) mailing_count,"
			+ " SUM(a.no_of_mailings) email_count,"
			+ " SUM(a.no_of_bytes) / SUM(a.no_of_mailings) / 1024 kbPerMail"
			+ " FROM mailing_account_tbl a"
            + " JOIN mailing_tbl m ON a.mailing_id = m.mailing_id AND m.deleted = 0 ";
		
			if (adminId > 0 && isDisabledMailingListsSupported()) {
				query += " AND m.mailinglist_id NOT IN (SELECT mailinglist_id FROM disabled_mailinglist_tbl WHERE admin_id = ?)";
				params.add(adminId);
			}
			
			query += " WHERE a.company_id = ? "
			+ " AND a.timestamp >= ?"
			+ " AND a.timestamp < ?"
			+ " AND a.status_field NOT IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') ";
		
			params.add(companyID);
			params.add(startDate);
			params.add(endDate);
		
		return select(logger, query, (resultSet, rowNum) -> {
			MonthCounterStatRow monthCounterRow = new MonthCounterStatRow();
			monthCounterRow.setMailingCount(resultSet.getInt("mailing_count"));
			monthCounterRow.setEMailCount(resultSet.getInt("email_count"));
			monthCounterRow.setKilobyte(new DecimalFormat("0.0").format(resultSet.getDouble("kbPerMail")));
			return monthCounterRow;
		}, params.toArray());
	}

	public List<MonthDetailStatRow> getMonthDetails(@VelocityCheck int companyID, int adminId, String startDateString, String endDateString, int top10Metric) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date startDate = dateFormat.parse(startDateString);
		Date endDate = DateUtilities.addDaysToDate(dateFormat.parse(endDateString), 1);
		
		List<Object> queryParameters = new ArrayList<>();
		StringBuilder queryBuilder = new StringBuilder("SELECT m.mailing_id, m.shortname, m.description, a.mailtype, SUM(a.no_of_mailings) mailing_count, SUM(a.no_of_bytes) / SUM(a.no_of_mailings) / 1024 kbPerMail");
		if (isOracleDB()) {
			queryBuilder.append(", TO_CHAR(a.timestamp, 'DD.MM.YYYY') datum");
		} else {
			queryBuilder.append(", DATE_FORMAT(a.timestamp, '%d.%m.%Y') datum");
		}

		MonthlyStatType metric = MonthlyStatType.get(top10Metric);
		if (metric == null) {
			throw new Exception("Invalid top10Metric type: " + top10Metric);
		}

		switch (metric) {
			case RECIPIENT_NUM:
				queryBuilder.append(", SUM(a.no_of_mailings) AS total");
				break;
			case OPENERS:
				queryBuilder.append(", (SELECT COUNT(distinct o.customer_id) FROM ")
                        .append(getOnePixelLogDeviceTableName(companyID))
                        .append(" o WHERE m.mailing_id = o.mailing_id  AND o.creation >= ? AND o.creation < ?) total");
				queryParameters.add(startDate);
				queryParameters.add(endDate);
				break;
			case ANONYMOUS_OPENINGS:
				queryBuilder.append(", (SELECT COUNT(o.customer_id) FROM ")
                        .append(getOnePixelLogDeviceTableName(companyID))
                        .append(" o WHERE m.mailing_id = o.mailing_id AND o.customer_id = 0 AND o.creation >= ? AND o.creation < ?) total");
				queryParameters.add(startDate);
				queryParameters.add(endDate);
				break;
			case CLICKERS:
				queryBuilder.append(", (SELECT COUNT(distinct r.customer_id) click_recipients FROM ")
                        .append(getRdirLogTableName(companyID))
                        .append(" r WHERE r.mailing_id = m.mailing_id  AND r.timestamp >= ? AND r.timestamp < ?) total");
				queryParameters.add(startDate);
				queryParameters.add(endDate);
				break;
			case ANONYMOUS_CLICKS:
				queryBuilder.append(", (SELECT COUNT(r.customer_id) click_recipients FROM ")
                        .append(getRdirLogTableName(companyID))
                        .append(" r WHERE r.mailing_id = m.mailing_id AND r.customer_id = 0 AND r.timestamp >= ? AND r.timestamp < ?) total");
				queryParameters.add(startDate);
				queryParameters.add(endDate);
				break;
			default:
				throw new Exception("Invalid top10Metric type: " + top10Metric);
		}
		
		queryBuilder.append(" FROM mailing_tbl m, mailing_account_tbl a")
			.append(" WHERE m.company_id = ? AND m.deleted = 0")
			.append(" AND m.mailing_id = a.mailing_id")
			.append(" AND a.no_of_mailings <> 0 AND a.status_field NOT IN ('")
			.append(UserType.Admin.getTypeCode()).append("', '")
			.append(UserType.TestUser.getTypeCode()).append("', '")
			.append(UserType.TestVIP.getTypeCode()).append("')");
		queryParameters.add(companyID);

		queryBuilder.append(" AND a.timestamp >= ? AND a.timestamp < ?");
		queryParameters.add(startDate);
		queryParameters.add(endDate);
		
		if (adminId > 0 && isDisabledMailingListsSupported()) {
			queryBuilder.append(" AND a.mailinglist_id NOT IN (SELECT mailinglist_id FROM disabled_mailinglist_tbl WHERE admin_id = ?) ");
			queryParameters.add(adminId);
		}

		queryBuilder.append(" GROUP BY m.mailing_id, m.shortname, m.description, a.mailtype");
		if (isOracleDB()) {
			queryBuilder.append(", TO_CHAR(a.timestamp, 'DD.MM.YYYY')");
		} else {
			queryBuilder.append(", DATE_FORMAT(a.timestamp, '%d.%m.%Y')");
		}
		queryBuilder.append(" ORDER BY datum, total");
		
		return select(logger, queryBuilder.toString(), (resultSet, rowNum) -> {
			MonthDetailStatRow monthDetailRow = new MonthDetailStatRow();
			monthDetailRow.setMailingId(resultSet.getInt("mailing_id"));
			monthDetailRow.setDate(resultSet.getString("datum"));
			monthDetailRow.setShortName(resultSet.getString("shortname"));
			monthDetailRow.setDescription(resultSet.getString("description"));
			monthDetailRow.setKiloByte(new DecimalFormat("0.0").format(resultSet.getDouble("kbPerMail")));
			monthDetailRow.setMailingCount(resultSet.getInt("mailing_count"));
			monthDetailRow.setTotal(resultSet.getInt("total"));
			int mailTypeCode = resultSet.getInt("mailtype");
			try {
				monthDetailRow.setMailtype(I18nString.getLocaleString(MailType.getFromInt(mailTypeCode).getMessageKey(), "en"));
			} catch (Exception e) {
				monthDetailRow.setMailtype(I18nString.getLocaleString("MailType.unknown", "en") + ": " + mailTypeCode);
			}
			return monthDetailRow;
		}, queryParameters.toArray(new Object[0]));
	}

	public List<MonthTotalStatRow> getMonthTotals(@VelocityCheck int companyID, int adminId, String startDateString, String endDateString, int top10Metric) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date startDate = dateFormat.parse(startDateString);
		Date endDate = DateUtilities.addDaysToDate(dateFormat.parse(endDateString), 1);
		
		List<Object> queryParameters = new ArrayList<>();
		StringBuilder queryBuilder = new StringBuilder("SELECT m.mailing_id, m.shortname");

		MonthlyStatType metric = MonthlyStatType.get(top10Metric);
		if (metric == null) {
			throw new Exception("Invalid top10Metric type: " + top10Metric);
		}

		switch (metric) {
			case RECIPIENT_NUM:
				queryBuilder.append(", SUM(a.no_of_mailings) total");
				break;
			case OPENERS:
				queryBuilder.append(", (SELECT COUNT(distinct o.customer_id) FROM ")
                        .append(getOnePixelLogDeviceTableName(companyID))
                        .append(" o WHERE m.mailing_id = o.mailing_id  AND o.creation >= ? AND o.creation < ?) total");
				queryParameters.add(startDate);
				queryParameters.add(endDate);
				break;
			case ANONYMOUS_OPENINGS:
				queryBuilder.append(", (SELECT COUNT(o.customer_id) FROM ")
                        .append(getOnePixelLogDeviceTableName(companyID))
                        .append(" o WHERE m.mailing_id = o.mailing_id AND o.customer_id = 0 AND o.creation >= ? AND o.creation < ?) total");
				queryParameters.add(startDate);
				queryParameters.add(endDate);
				break;
			case CLICKERS:
				queryBuilder.append(", (SELECT COUNT(distinct r.customer_id) click_recipients FROM ")
                        .append(getRdirLogTableName(companyID))
                        .append(" r WHERE r.mailing_id = m.mailing_id  AND r.timestamp >= ? AND r.timestamp < ?) total");
				queryParameters.add(startDate);
				queryParameters.add(endDate);
				break;
			case ANONYMOUS_CLICKS:
				queryBuilder.append(", (SELECT COUNT(r.customer_id) click_recipients FROM ")
                        .append(getRdirLogTableName(companyID))
                        .append(" r WHERE r.mailing_id = m.mailing_id AND r.customer_id = 0 AND r.timestamp >= ? AND r.timestamp < ?) total");
				queryParameters.add(startDate);
				queryParameters.add(endDate);
				break;
			default:
				throw new Exception("Invalid top10Metric type: " + top10Metric);
		}

		queryBuilder.append(" FROM mailing_tbl m, mailing_account_tbl a")
			.append(" WHERE m.company_id = ? AND m.deleted = 0")
			.append(" AND m.mailing_id = a.mailing_id")
			.append(" AND a.no_of_mailings <> 0 AND a.status_field NOT IN ('")
			.append(UserType.Admin.getTypeCode()).append("', '")
			.append(UserType.TestUser.getTypeCode()).append("', '")
			.append(UserType.TestVIP.getTypeCode()).append("')");
		queryParameters.add(companyID);

		queryBuilder.append(" AND a.timestamp >= ? AND a.timestamp < ?");
		queryParameters.add(startDate);
		queryParameters.add(endDate);
		
		if (adminId > 0 && isDisabledMailingListsSupported()) {
			queryBuilder.append(" AND a.mailinglist_id NOT IN (SELECT mailinglist_id FROM disabled_mailinglist_tbl WHERE admin_id = ?) ");
			queryParameters.add(adminId);
		}
		
		queryBuilder.append(" GROUP BY m.mailing_id, m.shortname");

		return select(logger, queryBuilder.toString(), (resultSet, rowNum) -> {
			MonthTotalStatRow row = new MonthTotalStatRow();
			row.setMailingId(resultSet.getInt("mailing_id"));
			row.setShortName(resultSet.getString("shortname"));
			row.setTotal(resultSet.getInt("total"));
			return row;
		}, queryParameters.toArray(new Object[0]));
	}
}
