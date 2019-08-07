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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DbUtilities;
import org.apache.log4j.Logger;

import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.OpenedMailsStatRow;
import com.agnitas.reporting.birt.external.beans.SendStatRow;
import com.agnitas.reporting.birt.external.utils.StringUtils;

public class MailingOpenedDataSet extends BIRTDataSet {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MailingOpenedDataSet.class);
	
	public MailingOpenedDataSet() {
		super();
	}
	
	public MailingOpenedDataSet(DataSource dataSource) {
		super();
		setDataSource(dataSource);
	}
	
	/**
	 * The total opened mails.
	 * @param companyID
	 * @param mailingID
	 * @return  [0] = total , [1] = percent ( base is total send ) 
	 */
	public List<SendStatRow> getTotalOpened(@VelocityCheck int companyID, int mailingID,String useMailTrackingStr, String targetIDs, String language ,Boolean includeAdminAndTestMails) {
		if (org.apache.commons.lang.StringUtils.isBlank(language)) {
			language = "EN";
		}
		
		String query = getTotalOpenedQuery(companyID, mailingID);
		List<Integer> targetIDList = StringUtils.buildListFormCommaSeparatedValueString(targetIDs);
		if (org.apache.commons.lang.StringUtils.isBlank(useMailTrackingStr)) {
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
				if (!org.apache.commons.lang.StringUtils.isBlank(target.getTargetSQL())) {
					String targetQuery = getTargetgroupOpenedQuery(target.getName(), companyID, mailingID, target.getTargetSQL());
					queryBuffer.append(targetQuery);
					queryBuffer.append(" UNION ");
				}
			}
			
			String tempQuery = queryBuffer.delete(queryBuffer.lastIndexOf(" UNION "), queryBuffer.length()).toString();
			query = query + tempQuery;
		}		
		
		Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<SendStatRow> statList = new ArrayList<>();
		
		try {
			connection = getDataSource().getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
			int totalsend = new MailingSendDataSet(getDataSource()).getTotalSend(mailingID, includeAdminAndTestMails);
			int targetgroupindex = 1;
			
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
				row.setCount(resultSet.getInt("total"));								
				if (totalsend != 0) {
					row.setRate( (row.getCount() *1.0f) / totalsend );
				}		
				statList.add(row);
			}			
		} catch (SQLException e) {
			logger.error("Error while trying to get total opened mails with query:" + query, e);
		} finally {
            DbUtilities.closeQuietly(connection, "Could not close DB-connection.");
            DbUtilities.closeQuietly(statement, "Could not close DB-statement !");
            DbUtilities.closeQuietly(resultSet, "Could not close result set !");
		}		
		return statList;
	}
	
	public List<OpenedMailsStatRow> getTopDomains(@VelocityCheck int companyID, int mailingID, int top ) {
		String query = getTopQuery(companyID, mailingID, top);
		List<OpenedMailsStatRow> openedList = new ArrayList<>();
		Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
			connection = getDataSource().getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				OpenedMailsStatRow row = new OpenedMailsStatRow();
				row.setCustomer(resultSet.getInt("customer"));
				row.setDomain(resultSet.getString("domain"));
				openedList.add(row);
			}
		} catch (SQLException e) {
			logger.error("Could not execute query for top domains :" + query, e);
		} finally {
            DbUtilities.closeQuietly(connection, "Could not close DB-connection.");
            DbUtilities.closeQuietly(statement, "Could not close DB-statement !");
            DbUtilities.closeQuietly(resultSet, "Could not close result set !");
		}
		return openedList;
	}
	
	public List<String> getDomains(List<OpenedMailsStatRow>  statList) {
		List<String> domainList = new ArrayList<>();
		for (OpenedMailsStatRow row:statList) {
			domainList.add(row.getDomain());
		}	
		return domainList;		
	}	
	
	public List<OpenedMailsStatRow>  getTopDomainsStat(@VelocityCheck int companyID,int mailingID, int top) {
		List<OpenedMailsStatRow> openedList = getTopDomains(companyID, mailingID, top);
		List<String> domainsList = getDomains(openedList);
		StringBuffer buffer = new StringBuffer();
		for (String domain:domainsList) {
			buffer.append("'");
			buffer.append(domain);
			buffer.append("',");			
		}
		String domains = buffer.delete(buffer.lastIndexOf(","),buffer.length()).toString();
		Map<String,Integer> totalOpenedDomains = getTotalForDomains(companyID, domains, mailingID);
		for (OpenedMailsStatRow row:openedList ) {
			row.setTotal(totalOpenedDomains.get(row.getDomain()));
		}
		return openedList;	
	}
	
	/**
	 * 
	 * @param companyID
	 * @param domains
	 * @param mailingID
	 * @return hash map with domain as key and count as value;
	 */
	public Map<String,Integer> getTotalForDomains(@VelocityCheck int companyID, String domains, int mailingID  ) {
		String query = getTotalForDomainsQuery(companyID, domains, mailingID);
		Map<String,Integer> domainsMap = new HashMap<>();
		
		Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
			connection = getDataSource().getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				domainsMap.put(resultSet.getString("domain"), resultSet.getInt("count"));
			}			
		} catch (SQLException e) {
			logger.error("Could not get totals for domains with query" +query ,e);
		} finally {
            DbUtilities.closeQuietly(connection, "Could not close DB-connection.");
            DbUtilities.closeQuietly(statement, "Could not close DB-statement !");
            DbUtilities.closeQuietly(resultSet, "Could not close result set !");
		}
		return domainsMap;
	}
	
	private String getTopQuery(@VelocityCheck int companyID, int mailingID, int top ) {
		String template = getTopQueryTemplate();
		template = template.replace("<COMPANYID>", Integer.toString(companyID));
		template = template.replace("<MAILINGID>", Integer.toString(mailingID));
		template = template.replace("<TOP>", Integer.toString(top));
		return template;
	}

    private String getTopQueryTemplate() {
        String query = "";
        query = "select * from (select count(*) as customer, substr(cust.email,instr(cust.email, '@')) as domain "
                + " from customer_<COMPANYID>_tbl cust "
                + " where cust.customer_id in (select onepix.customer_id from onepixellog_<COMPANYID>_tbl onepix where onepix.company_id = <COMPANYID> "
                + " and onepix.mailing_id = <MAILINGID> ) "
                + " group by substr(cust.email,instr(cust.email, '@')) order by count(*) desc) ";
        if (isOracleDB()) {
            query = query + " where rownum <= <TOP> ";
        } else {
            query = query + " cust_data limit <TOP> ";
        }
        return query;
    }
	
	private String getTotalForDomainsQuery(@VelocityCheck int companyID,String domains, int mailingID) {
		String template =getTotalForDomainsQueryTemplate();
		template = template.replace("<COMPANYID>", Integer.toString(companyID));
		template = template.replace("<DOMAINS>", domains );
		template = template.replace("<MAILINGID>", Integer.toString(mailingID));
		return template;
	}
	
	private String getTotalForDomainsQueryTemplate() {
		return "select count(cust.customer_id) as count, substr(cust.email, instr(cust.email, '@')) as domain from customer_<COMPANYID>_tbl cust, customer_<COMPANYID>_binding_tbl bind " 
			  +" where cust.customer_id = bind.customer_id and substr(cust.email, instr(cust.email, '@')) IN ( <DOMAINS>) and bind.mailinglist_id IN  (select mailinglist_id from mailing_tbl where mailing_id = <MAILINGID> ) "
			  +" group by  substr(cust.email, instr(cust.email, '@'))";
	}
	
	private String getTotalOpenedQuery(@VelocityCheck int companyID, int mailingID) {
		String template = getTotalOpenedQueryTemplate();
		template = template.replace("<COMPANYID>", Integer.toString(companyID));
		template = template.replace("<MAILINGID>", Integer.toString(mailingID));
		return template;
	}
	
	private String getTotalOpenedQueryTemplate() {
		return "select '<TOTAL>' as targetgroup, count(customer_id) as total from onepixellog_<COMPANYID>_tbl onepix where onepix.mailing_id=<MAILINGID> "
				+ " and onepix.company_id=<COMPANYID> ";
	}
	
	private String getTargetgroupOpenedQuery(String targetgroup, @VelocityCheck int companyID, int mailingID, String targetSQL) {
		String template = getTargetgroupOpenedQueryTemplate();
		template = template.replace("<TARGETGROUP>", targetgroup);
		template = template.replace("<COMPANYID>", Integer.toString(companyID));
		template = template.replace("<MAILINGID>",Integer.toString(mailingID));
		template = template.replace("<TARGETSQL>",targetSQL);
		return template;
	}
	
	private String getTargetgroupOpenedQueryTemplate() {
		return "select '<TARGETGROUP>' as targetgroup, count(cust.customer_id) as total from "
				+ " onepixellog_<COMPANYID>_tbl onepix  join customer_<COMPANYID>_tbl cust on (onepix.customer_id=cust.customer_id)"
			  +" where onepix.mailing_id=<MAILINGID> and onepix.company_id=<COMPANYID> and "
			  +	" (<TARGETSQL>) ";
	}
}
