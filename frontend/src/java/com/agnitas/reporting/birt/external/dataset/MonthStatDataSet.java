/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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
import org.apache.log4j.Logger;

import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.MonthCounterStatRow;
import com.agnitas.reporting.birt.external.beans.MonthDetailStatRow;
import com.agnitas.reporting.birt.external.beans.MonthTotalStatRow;

public class MonthStatDataSet extends BIRTDataSet {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MonthStatDataSet.class);

	public static final int TOP_10_BY_RECIPIENT_NUM = 0;
	public static final int TOP_10_BY_OPEN_NUM = 1;
	public static final int TOP_10_BY_RECIPIENT_CLICKS = 2;

	public List<MonthCounterStatRow> getMailingCounts (@VelocityCheck int companyID, String startDateString, String endDateString) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date startDate = dateFormat.parse(startDateString);
		Date endDate = DateUtilities.addDaysToDate(dateFormat.parse(endDateString), 1);
		String query = "SELECT COUNT(DISTINCT mailing_id) mailing_count,"
			+ " SUM(no_of_mailings) email_count,"
			+ " SUM(no_of_bytes) / SUM(no_of_mailings) / 1024 kbPerMail"
			+ " FROM mailing_account_tbl"
			+ " WHERE company_id = ?"
			+ " AND timestamp >= ?"
			+ " AND timestamp < ?"
			+ " AND status_field NOT IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') ";
		return select(logger, query, (resultSet, rowNum) -> {
			MonthCounterStatRow monthCounterRow = new MonthCounterStatRow();
			monthCounterRow.setMailingCount(resultSet.getInt("mailing_count"));
			monthCounterRow.setEMailCount(resultSet.getInt("email_count"));
			monthCounterRow.setKilobyte(new DecimalFormat("0.0").format(resultSet.getDouble("kbPerMail")));
			return monthCounterRow;
		}, companyID, startDate, endDate);
	}

	public List<MonthDetailStatRow> getMonthDetails(@VelocityCheck int companyID, String startDateString, String endDateString) throws Exception {
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

		queryBuilder.append(", (SELECT COUNT(distinct o.customer_id) FROM onepixellog_device_" + companyID + "_tbl o WHERE m.mailing_id = o.mailing_id  AND o.creation >= ? AND o.creation < ?) open_count");
		queryParameters.add(startDate);
		queryParameters.add(endDate);
		
		queryBuilder.append(", (SELECT COUNT(distinct r.customer_id) click_recipients FROM rdirlog_" + companyID + "_tbl r WHERE r.mailing_id = m.mailing_id  AND r.timestamp >= ? AND r.timestamp < ?) click_recipients");
		queryParameters.add(startDate);
		queryParameters.add(endDate);

		queryBuilder.append(" FROM mailing_tbl m, mailing_account_tbl a")
			.append(" WHERE m.company_id = ?")
			.append(" AND m.mailing_id = a.mailing_id")
			.append(" AND a.no_of_mailings <> 0 AND a.status_field NOT IN ('")
			.append(UserType.Admin.getTypeCode()).append("', '")
			.append(UserType.TestUser.getTypeCode()).append("', '")
			.append(UserType.TestVIP.getTypeCode()).append("')");
		queryParameters.add(companyID);

		queryBuilder.append(" AND a.timestamp >= ? AND a.timestamp < ?");
		queryParameters.add(startDate);
		queryParameters.add(endDate);
		queryBuilder.append(" GROUP BY m.mailing_id, m.shortname, m.description, a.mailtype");
		if (isOracleDB()) {
			queryBuilder.append(", TO_CHAR(a.timestamp, 'DD.MM.YYYY')");
		} else {
			queryBuilder.append(", DATE_FORMAT(a.timestamp, '%d.%m.%Y')");
		}
		queryBuilder.append(" ORDER BY datum");
		
		return select(logger, queryBuilder.toString(), (resultSet, rowNum) -> {
			MonthDetailStatRow monthDetailRow = new MonthDetailStatRow();
			monthDetailRow.setMailingId(resultSet.getInt("mailing_id"));
			monthDetailRow.setDate(resultSet.getString("datum"));
			monthDetailRow.setShortName(resultSet.getString("shortname"));
			monthDetailRow.setDescription(resultSet.getString("description"));
			monthDetailRow.setKiloByte(new DecimalFormat("0.0").format(resultSet.getDouble("kbPerMail")));
			monthDetailRow.setMailingCount(resultSet.getInt("mailing_count"));
			int mailTypeCode = resultSet.getInt("mailtype");
			try {
				monthDetailRow.setMailtype(I18nString.getLocaleString(MailType.getFromInt(mailTypeCode).getMessageKey(), "en"));
			} catch (Exception e) {
				monthDetailRow.setMailtype(I18nString.getLocaleString("MailType.unknown", "en") + ": " + mailTypeCode);
			}
			monthDetailRow.setOpenings(resultSet.getInt("open_count"));
			monthDetailRow.setClickRecipients(resultSet.getInt("click_recipients"));
			return monthDetailRow;
		}, queryParameters.toArray(new Object[0]));
	}

	public List<MonthTotalStatRow> getMonthTotals(@VelocityCheck int companyID, String startDateString, String endDateString, int top10Metric) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date startDate = dateFormat.parse(startDateString);
		Date endDate = DateUtilities.addDaysToDate(dateFormat.parse(endDateString), 1);
		
		List<Object> queryParameters = new ArrayList<>();
		StringBuilder queryBuilder = new StringBuilder("SELECT m.mailing_id, m.shortname");
		
		switch (top10Metric) {
			case TOP_10_BY_RECIPIENT_NUM:
				queryBuilder.append(", SUM(a.no_of_mailings) total");
				break;
			case TOP_10_BY_OPEN_NUM:
				queryBuilder.append(", (SELECT COUNT(distinct o.customer_id) FROM onepixellog_device_" + companyID + "_tbl o WHERE m.mailing_id = o.mailing_id  AND o.creation >= ? AND o.creation < ?) total");
				queryParameters.add(startDate);
				queryParameters.add(endDate);
				break;
			case TOP_10_BY_RECIPIENT_CLICKS:
				queryBuilder.append(", (SELECT COUNT(distinct r.customer_id) click_recipients FROM rdirlog_" + companyID + "_tbl r WHERE r.mailing_id = m.mailing_id  AND r.timestamp >= ? AND r.timestamp < ?) total");
				queryParameters.add(startDate);
				queryParameters.add(endDate);
				break;
			default:
				throw new Exception("Invalid top10Metric type: " + top10Metric);
		}

		queryBuilder.append(" FROM mailing_tbl m, mailing_account_tbl a")
			.append(" WHERE m.company_id = ?")
			.append(" AND m.mailing_id = a.mailing_id")
			.append(" AND a.no_of_mailings <> 0 AND a.status_field NOT IN ('")
			.append(UserType.Admin.getTypeCode()).append("', '")
			.append(UserType.TestUser.getTypeCode()).append("', '")
			.append(UserType.TestVIP.getTypeCode()).append("')");
		queryParameters.add(companyID);

		queryBuilder.append(" AND a.timestamp >= ? AND a.timestamp < ?");
		queryParameters.add(startDate);
		queryParameters.add(endDate);
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
