/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.TimeoutLRUMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

// ComDeliveryStatImpl sucks ... quite complicated , why ? 
public class MailingSendDataSet extends BIRTDataSet {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MailingSendDataSet.class);
	
	private static TimeoutLRUMap<Integer, Map<String, Integer>> mailingTotalSendMapNew = new TimeoutLRUMap<>(1000, 45 * 60 * 1000); // cache the totalsend about 45 minutes
	
	public MailingSendDataSet() {
		super();
	}
	
	public MailingSendDataSet(DataSource dataSource) {
		super();
		setDataSource(dataSource);
	}
	
	/**
	 * return the total count of sent mails beloning to this mailing. Includes all types of mails (test, admin..)
	 * if selectedTargets is empty, all targets are summed up. Otherwise, only mails to the selected targets are counted.
	 * 
	 * @param mailingID
	 * @param companyId
	 * @param selectedTargets
	 * @return
	 */
	public Integer getTotalSend(Integer mailingID, @VelocityCheck Integer companyId, String selectedTargets) {
		Integer result = readFromCache(mailingID, selectedTargets);
		if (null == result) {
			String targetSql = getTargetSqlString(selectedTargets, companyId);
			if (StringUtils.isEmpty(targetSql)) {
				result = selectInt(logger, "SELECT SUM(no_of_mailings) FROM mailing_account_tbl WHERE mailing_id = ?", mailingID);
			} else {
				String sql = "SELECT SUM(acc.no_of_mailings) FROM mailing_account_tbl acc"
					+ " JOIN mailing_tbl mailing ON acc.mailing_id = mailing.mailing_id" 
					+ " JOIN customer_" + companyId + "_binding_tbl bind ON mailing.mailinglist_id = bind.mailinglist_id" 
					+ " JOIN customer_" + companyId + "_tbl cust ON bind.customer_id = cust.customer_id" 
					+ " WHERE acc.mailing_id = ?"
					+ " AND " + targetSql;
				result = selectInt(logger, sql, mailingID);
			}
			
			writeToCache(mailingID, selectedTargets, result);
			return result;
		} else {
			return result;
		}
	}
	
	/**
	 * Try to read the result from the cache (TimeoutLRUMap)
	 * @param mailingID
	 * @param selectedTargets
	 * @return
	 */
	private Integer readFromCache(Integer mailingID, String selectedTargets) {
		Map<String, Integer> tmpMailingMap = mailingTotalSendMapNew.get(mailingID);
		if (null != tmpMailingMap && tmpMailingMap.containsKey(selectedTargets)) {
			return tmpMailingMap.get(selectedTargets);
		} else {
			return null;
		}
		
	}
	
	/**
	 * Cache the result in a TimeoutLRUMap
	 * 
	 * @param mailingID
	 * @param selectedTargets
	 * @param result
	 */
	private void writeToCache(Integer mailingID, String selectedTargets, Integer result) {
		Map<String, Integer> tmpMailingMap = mailingTotalSendMapNew.get(mailingID);
		if (null == tmpMailingMap) {
			tmpMailingMap = new HashMap<>();
			mailingTotalSendMapNew.put(mailingID, tmpMailingMap);
		}
		tmpMailingMap.put(selectedTargets,result);
	}
	
	public Integer getTotalSend(int mailingID, Boolean includeAdminAndTestMails) {
		StringBuilder queryBuilder = new StringBuilder("SELECT SUM(no_of_mailings) FROM mailing_account_tbl WHERE mailing_id = ?");
	    if (!includeAdminAndTestMails) {
		    queryBuilder.append(" AND status_field NOT IN ('A', 'T')");
	    }
	    return selectInt(logger, queryBuilder.toString(), mailingID);
	}
	
    /**
     * Get basic mailing statistic data
     * 
     * @param mailingID
     * @return Data Map
     */
	public Map<String, Object> getMailingStats(Integer mailingID) {
		String sql = "SELECT"
			+ " " + getIfNull() + "(SUM(no_of_mailings), 0) AS MAILS,"
			+ " MIN(timestamp) AS MINTIME,"
			+ " MAX(timestamp) AS MAXTIME,"
			+ " " + getIfNull() + "(SUM(no_of_bytes), 0) AS BYTES"
			+ " FROM mailing_account_tbl"
			+ " WHERE mailing_id = ? AND status_field NOT IN ('A', 'T')";
		return selectSingleRow(logger, sql, mailingID);
	}
}
