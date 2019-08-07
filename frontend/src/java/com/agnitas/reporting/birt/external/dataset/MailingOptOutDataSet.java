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

import javax.sql.DataSource;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DbUtilities;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.Recipient;
import com.agnitas.reporting.birt.external.beans.SendStatRow;

public class MailingOptOutDataSet extends BIRTDataSet {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MailingOptOutDataSet.class);
	
	public MailingOptOutDataSet() {
		super();
	}
	
	public MailingOptOutDataSet(DataSource dataSource) {
		super();
		setDataSource(dataSource);
	}
	
	int getTotalOptOut(Integer mailingId, Integer mailinglistId, @VelocityCheck Integer companyId, String targetSqlString) {
		String sql = "SELECT count(*) " +
			"FROM customer_"+companyId+"_binding_tbl bind join customer_"+companyId+"_tbl cust on (bind.customer_id = cust.customer_id) " +
			"WHERE bind.mailinglist_id=" + mailinglistId + " and bind.exit_mailing_id=" + mailingId + " and bind.user_status in (3, 4)";
		if (!StringUtils.isEmpty(targetSqlString)) {
			sql += " and " + targetSqlString;
		}
		JdbcTemplate jdbc = new JdbcTemplate(getDataSource());
		int optouts = jdbc.queryForObject(sql, Integer.class);
		return optouts;
	}
	
	/**
	 * 
	 * @param companyID
	 * @param mailingID
	 * @return [0] = optout , [1] percentage relative to totalsend 
	 */
	public List<SendStatRow> getTotalOptOut(@VelocityCheck int companyID, int mailingID,String useMailTrackingStr, String targetIDs, String language , Boolean includeAdminAndTestMails) {
		String query = getTotalOptOutsQuery(companyID, mailingID);
		
		if (StringUtils.isBlank(language)) {
			language = "EN";
		}

		List<Integer> targetIDList = com.agnitas.reporting.birt.external.utils.StringUtils.buildListFormCommaSeparatedValueString(targetIDs);
		if (StringUtils.isBlank(useMailTrackingStr)) {
			useMailTrackingStr= "false";
		}
		
		boolean useMailTracking= new Boolean(useMailTrackingStr.toLowerCase());
		
		if (useMailTracking  && !targetIDList.isEmpty()) {
			//TODO isn't List<Integer> more convenient ? 
			List<String> targetIDStringList = new ArrayList<>();
			for (Integer id:targetIDList) {
				targetIDStringList.add(id.toString());
			}
			List<LightTarget> targets= getTargets(targetIDStringList,companyID);
			StringBuffer queryBuffer = new StringBuffer(" UNION ");
			for (LightTarget target:targets ){
				if (!StringUtils.isBlank(target.getTargetSQL())) {
					String targetQuery = getTargetgroupOptOutsQuery(target.getName(), companyID, mailingID, target.getTargetSQL());
					queryBuffer.append(targetQuery);
					queryBuffer.append(" UNION ");
				}
			}
			String tempQuery = queryBuffer.delete(queryBuffer.lastIndexOf(" UNION ") , queryBuffer.length()).toString();
			query = query + tempQuery;
		}
		
		List<SendStatRow> statList = new ArrayList<>();
		
		Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
			int totalsend = new MailingSendDataSet(getDataSource()).getTotalSend(mailingID, includeAdminAndTestMails);
			int targetgroupindex = 1;
			connection = getDataSource().getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				SendStatRow row = new SendStatRow();
				
				String targetgroup = resultSet.getString("targetgroup");
				if ("<TOTAL>".equals(targetgroup)) {
					targetgroup = I18nString.getLocaleString("statistic.all_subscribers", language);
					row.setTargetgroupindex(0);
				} else {
					row.setTargetgroupindex(targetgroupindex);
					targetgroupindex++;
				}				
				row.setTargetgroup(targetgroup);
				row.setCount(resultSet.getInt("optout"));								
				if (totalsend != 0) {
					row.setRate((row.getCount() *1.0f) / totalsend );
				}		
				statList.add(row);
			}
		} catch (SQLException e) {
			logger.error("Could not get total optout with query " + query , e);
		} finally {
            DbUtilities.closeQuietly(connection, "Could not close DB-connection...");
            DbUtilities.closeQuietly(statement, "Could not close DB-statement !");
            DbUtilities.closeQuietly(resultSet, "Could not close result set !");
		}
		return statList;
	}

	public List<Recipient> getOptOutRecipients(@VelocityCheck int companyID, int mailingID, String language) { 
		String query = getOptOutsRecipientsQuery(companyID, mailingID);
		List<Recipient> recipientList = new ArrayList<>();
		
		if (StringUtils.isBlank(language)) {
			language = "EN";
		}
		
		Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
			connection = getDataSource().getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				Recipient recipient = new Recipient();
				
				String email = resultSet.getString("email");
				recipient.setEmail(  email != null ? resultSet.getString("email").trim(): "" );
				
				String firstname = resultSet.getString("firstname");
				recipient.setName(firstname != null ? resultSet.getString("firstname").trim(): "");
				
				String lastname = resultSet.getString("lastname");
				recipient.setLastname(lastname != null ? resultSet.getString("lastname").trim(): "");
				
				recipient.setGender(resultSet.getInt("gender"), language);
				recipientList.add(recipient);
			}
		} catch (SQLException e) {
			logger.error("SQL error while trying to execute query:" +query , e);
		} finally {
            DbUtilities.closeQuietly(connection, "Error while trying to close DB-connection");
            DbUtilities.closeQuietly(statement, "Could not close DB-statement !");
            DbUtilities.closeQuietly(resultSet, "Could not close result set !");
		}
		return recipientList;
	}
	
	private String getOptOutsRecipientsQuery(@VelocityCheck int companyID,int mailingID ) {
		String template = getOptOutRecipientsQueryTemplate();
		template = template.replace("<COMPANYID>", Integer.toString(companyID));
		template = template.replace("<MAILINGID>", Integer.toString(mailingID));
		return template;
	}
	
	private String getOptOutRecipientsQueryTemplate() {
		return "select cust.email as email, cust.firstname as firstname, cust.lastname as lastname, cust.gender as gender " 
				+" from customer_<COMPANYID>_binding_tbl bind, customer_<COMPANYID>_tbl cust "	
				+" where bind.customer_id=cust.customer_id and exit_mailing_id= <MAILINGID> "
				+" and user_status in(3,4) and mailinglist_id=(select mailinglist_id from mailing_tbl where mailing_id = <MAILINGID> )";
	}
	
	private String getTotalOptOutsQuery(@VelocityCheck int companyID, int mailingID) {
		String template = getTotalOptOutsQueryTemplate();
		template = template.replace("<COMPANYID>", Integer.toString(companyID));
		template = template.replace("<MAILINGID>", Integer.toString(mailingID));
		return template;
	}
	
	private String getTotalOptOutsQueryTemplate() {
			return "select '<TOTAL>' targetgroup, count(distinct (bind.customer_id)) as optout from customer_<COMPANYID>_binding_tbl bind "
			 	  +" where bind.exit_mailing_id=<MAILINGID> and bind.user_status in (3, 4) "
			 	  +" GROUP BY bind.user_status ";
	}
	
	private String getTargetgroupOptOutsQuery(String targetgroup, @VelocityCheck int companyID ,int mailingID , String targetSQL ) {
			String template = getTargetgroupOptOutsQueryTemplate();
			template = template.replace("<TARGETGROUP>", targetgroup);
			template = template.replace("<COMPANYID>", Integer.toString(companyID));
			template = template.replace("<MAILINGID>", Integer.toString(mailingID));
			template = template.replace("<TARGETSQL>", targetSQL);
			return template;
	}	
	
	private String getTargetgroupOptOutsQueryTemplate() {
			return "select '<TARGETGROUP>' targetgroup, count(distinct (bind.customer_id)) as optout from customer_<COMPANYID>_binding_tbl bind join customer_<COMPANYID>_tbl cust "
				+" on (bind.customer_id = cust.customer_id) "
				+"	where bind.exit_mailing_id=<MAILINGID> and bind.user_status in (3, 4) and " 
				+" (<TARGETSQL>) "
				+" GROUP BY bind.user_status";
	}
}
