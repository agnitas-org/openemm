/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

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
import com.agnitas.reporting.birt.external.beans.MailingClickStatsPerTargetAndHourRow;
import com.agnitas.reporting.birt.external.utils.StringUtils;

/**
 * DataSet for mailing_url_clicks_day_overview.rptdesign
 */
public class MailingURLClicksDayStatDataSet extends BIRTDataSet {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MailingURLClicksDayStatDataSet.class);

	public static final String DATE_PATTERN = "YYYYMMDD";

	public List<MailingClickStatsPerTargetAndHourRow> getStatPerDay(
			@VelocityCheck int companyID, int mailingID, int urlID, String startdate) {

		String query = getTotalClicksQuery(companyID, mailingID, urlID, startdate);
		List<MailingClickStatsPerTargetAndHourRow> statList = new ArrayList<>();
		Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
			connection = getDataSource().getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				MailingClickStatsPerTargetAndHourRow row = getRowFromResultSet(resultSet);
				statList.add(row);
			}
		} catch (SQLException e) {
			logger.error("SQL error while trying to execute query : " + query, e);
		} finally {
            DbUtilities.closeQuietly(connection, "Could not close connection !");
            DbUtilities.closeQuietly(statement, "Could not close DB-statement !");
            DbUtilities.closeQuietly(resultSet, "Could not close result set !");
		}
		return statList;
	}

	private MailingClickStatsPerTargetAndHourRow getRowFromResultSet(ResultSet resultSet) throws SQLException {
		MailingClickStatsPerTargetAndHourRow row = new MailingClickStatsPerTargetAndHourRow();
		row.setHour(resultSet.getInt("hh"));
		row.setClicks_gross(resultSet.getInt("clicks_gros"));
		row.setClicks_net(resultSet.getInt("clicks_net"));
		row.setTargetgroup(resultSet.getString("targetgroup"));

		return row;
	}

	private String getTotalClicksQuery(@VelocityCheck int companyID, int mailingID, int urlID, String startdate) {
		String template = getTotalClicksQueryTemplate();
		template = template.replace("<COMPANYID>", Integer.toString(companyID));
		template = template.replace("<MAILINGID>", Integer.toString(mailingID));
		template = template.replace("<URLID>", Integer.toString(urlID));
		template = template.replace("<STARTDATE>", startdate);
		return template;
	}

	private String getTotalClicksQueryTemplate() {
        String ifNull = getIfNull();
        return "select hh, "+ifNull+"(clicks_gros ,0) as clicks_gros, "+ifNull+"(clicks_net,0) as clicks_net , '<TOTAL>' as targetgroup from "
				+ " (SELECT LEVEL-1  hh "
				+ " FROM DUAL "
				+ " CONNECT BY LEVEL <= 24 ) hours "
				+ " left join ( "
				+ " select to_char(rdir.timestamp, 'HH24') time "
				+ " , count(rdir.customer_id) as clicks_gros, count(distinct rdir.customer_id) as clicks_net from rdirlog_<COMPANYID>_tbl rdir "
				+ " where rdir.mailing_id= <MAILINGID> "
				+ " and rdir.url_id=<URLID> "
				+ " and to_char( rdir.timestamp, 'yyyymmdd') = '<STARTDATE>' group by to_char(rdir.timestamp, 'HH24')) as clicks "
				+ " on (hours.hh = clicks.time) " + " order by hh";
	}

	public List<MailingClickStatsPerTargetAndHourRow> getStatPerDayAndTarget(String commaSeparatedTargetIDs, @VelocityCheck int companyID, int mailingID,
			int urlID, String startdate) {
		List<MailingClickStatsPerTargetAndHourRow> statList = new ArrayList<>();
		List<Integer> targetIDs = StringUtils.buildListFormCommaSeparatedValueString(commaSeparatedTargetIDs);

		if (!targetIDs.isEmpty()) {
			String query = getSubSelectsClickPerHourAndTargets(targetIDs, companyID, mailingID, urlID, startdate);
			Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;
            try {
				connection = getDataSource().getConnection();
				statement = connection.createStatement();
				resultSet = statement.executeQuery(query);
				while (resultSet.next()) {
					MailingClickStatsPerTargetAndHourRow row = getRowFromResultSet(resultSet);
					statList.add(row);
				}
			} catch (SQLException e) {
				logger.error("SQL error while trying to execute query : " + query,	e);
			} finally {
                DbUtilities.closeQuietly(connection, "Could not close connection !");
                DbUtilities.closeQuietly(statement, "Could not close DB-statement !");
                DbUtilities.closeQuietly(resultSet, "Could not close result set !");
			}
		}
		return statList;
	}

	public String getSubSelectsClickPerHourAndTargets(List<Integer> targetIDs,
			@VelocityCheck int companyID, int mailingID, int urlID, String startdate) {
		if (targetIDs.size() > 0) {
			StringBuffer selectBuffer = new StringBuffer();
			for (Integer targetID : targetIDs) {
				String tmpQuery = getSubSelectClicksPerHourAndTarget(targetID, companyID, mailingID, urlID, startdate, DATE_PATTERN);
				if (org.apache.commons.lang.StringUtils.isNotBlank(tmpQuery)) {
					selectBuffer.append(tmpQuery);
					selectBuffer.append(" UNION ");
				}
			}
			String unionSelect = selectBuffer.delete(selectBuffer.lastIndexOf("UNION"), selectBuffer.length()).toString();
			return unionSelect;
		}
		return "";
	}

	// TODO optimize that !!
	public String getSubSelectClicksPerHourAndTarget(Integer targetID, @VelocityCheck int companyID, int mailingID, int urlID, String startdate, String pattern) {
		LightTarget target = getTarget(targetID, companyID);
		String template = getSubSelectTemplateClicksPerHourAndTarget();
		template = template.replaceAll("<TARGETGROUP>", target.getName());
		template = template.replaceAll("<COMPANYID>", Integer.toString(companyID));
		template = template.replaceAll("<TARGETSQL>", target.getTargetSQL());
		template = template.replaceAll("<MAILINGID>", Integer.toString(mailingID));
		template = template.replaceAll("<PATTERN>", pattern);
		template = template.replaceAll("<STARTDATE>", startdate);
		template = template.replaceAll("<URLID>", Integer.toString(urlID));
		return template;
	}

	private String getSubSelectTemplateClicksPerHourAndTarget() {
		return "select to_char(rdir.timestamp, 'HH24') as hh, count(rdir.customer_id) as clicks_gros, count(distinct rdir.customer_id) as clicks_net, '<TARGETGROUP>' as targetgroup "
				+ " from rdirlog_<COMPANYID>_tbl rdir join customer_<COMPANYID>_tbl cust ON ((<TARGETSQL>) and cust.customer_id = rdir.customer_id) "
				+ " where rdir.mailing_id= <MAILINGID>  and rdir.url_id=<URLID>  and to_char( rdir.timestamp, 'YYYYMMDD') = '<STARTDATE>' "
				+ " group by  to_char(rdir.timestamp, 'HH24')";
	}
}
