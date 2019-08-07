/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

/**
 * Handles
 * 
 */
/**
 * Service class for creation of SQL-snippets used in the mailing_stat.rptdesign
 */
package com.agnitas.reporting.birt.external.dataset;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DbUtilities;
import org.apache.log4j.Logger;

import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.MailingClickStatsPerTargetAndDayRow;
import com.agnitas.reporting.birt.external.utils.StringUtils;

// clicks per week( clicks_week_overview.rptdesign )
public class MailingClicksWeekStatDataSet extends BIRTDataSet {
	private static final transient Logger logger = Logger.getLogger(MailingClicksWeekStatDataSet.class);

	public static final String DATE_PATTERN = "YYYYMMDD";

	public List<MailingClickStatsPerTargetAndDayRow> getStatPerWeek(
			@VelocityCheck int companyID, int mailingID, String startdate, String enddate) {
		List<MailingClickStatsPerTargetAndDayRow> statList = new ArrayList<>();
		String query = getSelectClicksPerWeek(mailingID, companyID, startdate,
				enddate);
		Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
			connection = getDataSource().getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				MailingClickStatsPerTargetAndDayRow row = getRowFromResultSet(resultSet);
				statList.add(row);
			}

		} catch (SQLException e) {
			logger.error(
					"SQL error while trying to get mailing week stat with query: "
							+ query, e);
		} finally {
            DbUtilities.closeQuietly(connection, "Could not close DB connection !");
            DbUtilities.closeQuietly(statement, "Could not close DB-statement !");
            DbUtilities.closeQuietly(resultSet, "Could not close result set !");
		}
		return statList;

	}

	/**
	 * Produces the 'DataSet' for the clicks_week overview, grouped by the
	 * targets.
	 * 
	 * @param commaSeparetedTargetIDs -
	 *            targets you want to compare, separeted by comma e.g. '23,34'
	 * @param companyID -
	 *            companyID ( always have to be provided for security reasons )
	 * @param mailingID
	 * @param startdate -
	 * @param enddate
	 * @return - list of stat values. Have a look at
	 *         MailingClickStatsPerTargetAndDayRow
	 */
	public List<MailingClickStatsPerTargetAndDayRow> getStatPerWeekAndTargets(
			String commaSeparetedTargetIDs, @VelocityCheck int companyID, int mailingID,
			String startdate, String enddate) {

		List<MailingClickStatsPerTargetAndDayRow> statList = new ArrayList<>();
		List<Integer> targetIDs = StringUtils
				.buildListFormCommaSeparatedValueString(commaSeparetedTargetIDs);
		if (!targetIDs.isEmpty()) {
			String query = getSubSelectsClicksPerWeekAndTargetsQuery(targetIDs,
					companyID, mailingID, startdate, enddate, DATE_PATTERN);
			Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;
            try {
				connection = getDataSource().getConnection();
				statement = connection.createStatement();
				resultSet = statement.executeQuery(query);
				while (resultSet.next()) {
					MailingClickStatsPerTargetAndDayRow row = getRowFromResultSet(resultSet);
					statList.add(row);
				}

			} catch (SQLException e) {
				logger.error(
						"SQL-Error while trying to get click stat grouped by week and target, query : "
								+ query, e);
			} finally {
                DbUtilities.closeQuietly(connection, "Could not close DB connection ");
                DbUtilities.closeQuietly(statement, "Could not close DB-statement !");
                DbUtilities.closeQuietly(resultSet, "Could not close result set !");
			}
		}
		return statList;
	}

	private MailingClickStatsPerTargetAndDayRow getRowFromResultSet(
			ResultSet resultSet) throws SQLException {
		MailingClickStatsPerTargetAndDayRow row = new MailingClickStatsPerTargetAndDayRow();
		row.setDay(resultSet.getTimestamp("myday"));
		row.setTargetgroup(resultSet.getString("targetgroup"));
		row.setClicks_gross(resultSet.getInt("clicks_gros"));
		row.setClicks_net(resultSet.getInt("clicks_net"));
		return row;
	}

	public String getSubSelectsClicksPerWeekAndTargetsQuery(
			List<Integer> targetIDs, @VelocityCheck int companyID, int mailingID,
			String startdate, String enddate, String pattern) {
		if (targetIDs.size() > 0) {
			StringBuffer selectBuffer = new StringBuffer();
			for (Integer targetID : targetIDs) {
				String tmpQuery = getSubSelectClicksPerTargetWeekQuery(
						targetID, companyID, mailingID, startdate, enddate,
						pattern);
				if (!"".equals(tmpQuery.trim())) {
					selectBuffer.append(tmpQuery);
					selectBuffer.append(" UNION ");
				}
			}
			String unionSelect = selectBuffer.delete(
					selectBuffer.lastIndexOf("UNION"), selectBuffer.length())
					.toString();
			return unionSelect;
		}
		return "";
	}

	public String getSubSelectClicksPerTargetWeekQuery(int targetID,
			@VelocityCheck int companyID, int mailingID, String startdate, String enddate,
			String pattern) {
		LightTarget target = getTarget(targetID, companyID);
		if (target != null) {
			String targetgroup = target.getName();
			String targetSQL = target.getTargetSQL();
			StringBuffer templateBuffer = new StringBuffer();
            String ifNull = getIfNull();
            templateBuffer
					.append("(select myday,  "+ifNull+"( clicksperday.clicks_net,0) clicks_net, "+ifNull+"( clicksperday.clicks_gros,0) clicks_gros, '<TARGETGROUP>' targetgroup from ");
			templateBuffer.append(getSelect7Days());
			templateBuffer.append("left join  ( ");
			templateBuffer
					.append(getSubSelectTemplateClicksPerWeekAndTarget());
			templateBuffer
					.append(" ON ( to_char( dummydays.myday,'<PATTERN>') = clicksperday.mydate ) )");

			String targetSubSelect = templateBuffer.toString();
			if (target.getTargetSQL() != null
					&& !"".equals(target.getTargetSQL().trim())) {
				targetSubSelect = targetSubSelect.replaceAll("<PATTERN>",
						pattern);
				targetSubSelect = targetSubSelect.replaceAll(
						"<TARGETGROUP>", targetgroup);
				targetSubSelect = targetSubSelect.replaceAll("<COMPANYID>",
						Integer.toString(companyID));
				targetSubSelect = targetSubSelect.replaceAll("<TARGETSQL>",
						targetSQL);
				targetSubSelect = targetSubSelect.replaceAll("<MAILINGID>",
						Integer.toString(mailingID));

				targetSubSelect = targetSubSelect.replaceAll("<DATE>",
						startdate);
				targetSubSelect = targetSubSelect.replaceAll("<STARTDATE>",
						startdate);
				targetSubSelect = targetSubSelect.replaceAll("<ENDDATE>",
						enddate);
			}
			return targetSubSelect;
		}
		return "";

	}

	private String getSubSelectTemplateClicksPerWeekAndTarget() {
		return "select to_char(rdir.timestamp, '<PATTERN>') as mydate "
				+ ",count(rdir.customer_id ) as clicks_gros, count(distinct rdir.customer_id ) as clicks_net from rdirlog_<COMPANYID>_tbl rdir "
				+ "join customer_<COMPANYID>_tbl cust on ( rdir.customer_id = cust.customer_id and (<TARGETSQL>) ) "
				+ "where rdir.company_id=<COMPANYID>  and rdir.mailing_id=<MAILINGID> "
				+ "and (rdir.timestamp >= TO_DATE('<STARTDATE>', '<PATTERN>')   and rdir.timestamp <= TO_DATE('<ENDDATE>', '<PATTERN>') ) "
				+ "group by to_char(rdir.timestamp, '<PATTERN>') ) clicksperday";
	}

	private String getSelectClicksPerWeek(int mailingID, @VelocityCheck int companyID,
			String startdate, String enddate) {
		String template = getSelectTemplateClicksPerWeek();
		template = template.replace("<MAILINGID>", Integer.toString(mailingID));
		template = template.replace("<COMPANYID>", Integer.toString(companyID));
		template = template.replace("<STARTDATE>", startdate);
		template = template.replace("<ENDDATE>", enddate);
		return template;
	}

	private String getSelectTemplateClicksPerWeek() {
        String ifNull = getIfNull();
        return "(select myday,  "+ifNull+"( clicksperday.clicks_net,0) clicks_net, "+ifNull+"( clicksperday.clicks_gros,0) clicks_gros, '<TOTAL>' targetgroup from "
				+ "( SELECT ( to_date('<STARTDATE>','YYYYMMDD')  + LEVEL  -1 ) myday  FROM    DUAL  CONNECT BY LEVEL <= 7 ) dummydays left join  ( select to_char(rdir.timestamp, 'YYYYMMDD') as mydate ,count(rdir.customer_id ) as clicks_gros, count(distinct rdir.customer_id ) as clicks_net from rdirlog_<COMPANYID>_tbl rdir "
				+ " where rdir.company_id=<COMPANYID>  and rdir.mailing_id=<MAILINGID> and (rdir.timestamp >= TO_DATE('<STARTDATE>', 'YYYYMMDD')   and rdir.timestamp <= TO_DATE('<ENDDATE>', 'YYYYMMDD') ) group by to_char(rdir.timestamp, 'YYYYMMDD') ) clicksperday ON ( to_char( dummydays.myday,'YYYYMMDD') = clicksperday.mydate ) ) ";
	}

	private String getSelect7Days() {
		return " ( SELECT   ( to_date('<DATE>','<PATTERN>')  + LEVEL  -1 ) myday "
				+ " FROM    DUAL " + " CONNECT BY LEVEL <= 7 ) dummydays ";

	}
}
