/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.DomainStatRow;
import com.agnitas.reporting.birt.external.beans.LightTarget;

public class DomainStatDataSet extends BIRTDataSet {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(DomainStatDataSet.class);
	
	private static final int BIND_USER_STATUS = 1;
	
	private static String getCustomerTableName(int companyId) {
		return "customer_" + companyId + "_tbl";
	}
	
	private static String getCustomerBindingTableName(int companyId) {
		return "customer_" + companyId + "_binding_tbl";
	}
	
	public List<DomainStatRow> getDomainStat(@VelocityCheck int companyID, String targetID, Integer mailinglistId, int limit, String language, Boolean topLevelDomains) {
		language = StringUtils.defaultIfEmpty(language, "EN");
  
		String targetSql = "";
		if (!StringUtils.isEmpty(targetID)) {
			LightTarget target =  getTarget(Integer.parseInt(targetID), companyID);
			if (!StringUtils.isEmpty(target.getTargetSQL())) {
				targetSql = "AND (" + target.getTargetSQL()+ ")";
			}
		}
		
		String mailingListSql = "";
		if (null != mailinglistId) {
			mailingListSql = "AND bind.mailinglist_id = " + mailinglistId;
		}
			
		int totalCount = getDomainsTotalCount(companyID, mailingListSql, targetSql);
		List<DomainStatRow> domainList = getTopDomainList(companyID, mailingListSql, targetSql, limit, totalCount, language,  topLevelDomains);
  
		convertCountsToRate(domainList, totalCount);

		return domainList;
	}
	
	private int getDomainsTotalCount(int companyId, String mailingListSql, String targetSql) {
		String sql = "SELECT COUNT(cust.customer_id) as domain_count" +
				" FROM " + getCustomerTableName(companyId) + " cust, " + getCustomerBindingTableName(companyId) + " bind" +
				" WHERE cust.customer_id = bind.customer_id" +
				" AND bind.user_status = " + BIND_USER_STATUS +
				" " + mailingListSql +
				" " + targetSql;
		
		return select(logger, sql, Integer.class);
	}
	
	private void convertCountsToRate(List<DomainStatRow> domainList, int all) {
		for (DomainStatRow dsr : domainList) {
			dsr.setDomainCount(dsr.getDomainCount() / all);
		}
	}
	
	private List<DomainStatRow> getTopDomainList(int companyId, String mailingListSql, String targetSql, int limit, int totalCount, String language, boolean topLevelDomains) {
		String instrEmail;
        if (isOracleDB()) {
        	instrEmail = topLevelDomains ? "INSTR(email, '.', -1)" : "INSTR(email, '@')";
        } else {
            instrEmail = topLevelDomains ? "LENGTH(email) - INSTR(REVERSE(email), '.')" : "INSTR(email, '@')";
        }
		
		String sql = "SELECT COUNT(cust.customer_id) as domain_count, SUBSTR(email, " + instrEmail + " + 1) AS domain_name" +
				" FROM " + getCustomerTableName(companyId) + " cust, " + getCustomerBindingTableName(companyId) + " bind " +
				" WHERE cust.customer_id = bind.customer_id" +
				" AND bind.user_status = " + BIND_USER_STATUS +
				" " + mailingListSql +
				" " + targetSql +
				" GROUP BY SUBSTR(email, " + instrEmail + " + 1)" +
				" ORDER BY domain_count DESC";
		
		DomainRowMapper rowMapper = new DomainRowMapper();
		String sqlLimited = "SELECT * FROM (" + sql + ")";
		
		if (isOracleDB()) {
			sqlLimited += " WHERE ROWNUM <= " + limit;
		} else {
			sqlLimited += " cust_bind_data LIMIT " + limit;
		}
		
		List<DomainStatRow> domainList = select(logger, sqlLimited, rowMapper);
		
		// calculate "others"
		String othersText = I18nString.getLocaleString("statistic.Other", language);

		int othersTotal = totalCount - rowMapper.getTotalValue();
		domainList.add(new DomainStatRow(othersText, othersTotal));
		
		return domainList;
	}
	
	private static class DomainRowMapper implements RowMapper<DomainStatRow> {
		
		private AtomicInteger totalValue = new AtomicInteger();
		
		private int getTotalValue() {
			return totalValue.get();
		}
		
		@Override
		public DomainStatRow mapRow(ResultSet resultSet, int i) throws SQLException {
			int dc = resultSet.getInt("domain_count");
            totalValue.addAndGet(dc);
            return new DomainStatRow(resultSet.getString("domain_name"), dc);
		}
	}
}
