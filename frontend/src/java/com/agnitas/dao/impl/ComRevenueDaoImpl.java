/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComTarget;
import com.agnitas.dao.ComRevenueDao;
import com.agnitas.dao.ComTargetDao;

/**
 * ComRevenueDao is used to provide an Interface for getting Revenue Data
 * from the Database. Revenue Data is only available if Deeptracking is permitted.
 * The revenue-Data are found in the RDIRLOG_<Company_ID>_VAL_NUM_TBL and called NUM_PARAMETER
 */
public class ComRevenueDaoImpl extends BaseDaoImpl implements ComRevenueDao {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ComRevenueDaoImpl.class);

	/** DAO accessing target groups. */
	private ComTargetDao targetDao;

	/**
	 * Sets DAO accessing target groups.
	 * @param targetDao DAO accessing target groups
	 */
	@Required
	public void setTargetDao(ComTargetDao targetDao) {
		this.targetDao = targetDao;
	}

	/**
	 * returns the Revenue for the given Company-ID, Mailing-ID and Target-ID
	 * 
	 * @param company_Id
	 * @param mailing_Id
	 * @param target_Id
	 */
	@Override
	public Double getRevenue(int companyId, int mailingId, int targetId) {
		// check if Deeptracking is allowed, if not return just 0.0
		if (revenueActivated(mailingId)) {
			// check, if there are any revenues, if not return 0.
			if (getRdirLogCount(companyId, mailingId) > 0) {
				String sql_target1 = "";
				String sql_target2 = "";

				// loop over all Target-ID's
				if (targetId > 0) {
					ComTarget aTarget = targetDao.getTarget(targetId, companyId);
					if (aTarget != null) {
						// construct first part of conditional sql-statement
						sql_target1 = ", customer_" + companyId + "_tbl cust";
						// construct second part of conditional sql-statement
						sql_target2 = " AND ((" + aTarget.getTargetSQL() + ") AND cust.customer_id = deep.customer_id)";
					}
				}

				// construct SQL-Statement, if no target ID is given, sql_target1 and sql_target2 are empty
				String sql = "SELECT SUM(deep.NUM_PARAMETER) as total FROM rdirlog_" + companyId + "_val_num_tbl deep";
				sql += sql_target1;
				sql += " WHERE deep.mailing_id = ? AND deep.page_tag = 'revenue'";
				sql += sql_target2;
				
				try {
					List<Map<String, Object>> result = select(logger, sql, mailingId);

					// check if we have values in our List.
					if (result.size() > 0) {
						Map<String, Object> map = result.get(0);
						if (map.get("total") != null) {
							return ((Number) map.get("total")).doubleValue();
						} else {
							return 0.0;
						}
					} else {
						return 0.0;
					}
				} catch (Exception e) {
					logger.error("Error in RevenueDao: " + e);
					logger.error("Query was: " + sql);
					return 0.0;
				}
			} else {
				return 0.0;
			}
		} else {
			return 0.0;
		}
	}

	/**
	 * Returns the Revenue for the given Company_ID and the given Mailing_ID.
	 * 
	 * @param company_Id
	 * @param mailing_Id
	 */
	@Override
	public Double getRevenue(int company_Id, int mailing_Id) {
		return getRevenue(company_Id, mailing_Id, 0); // just set target_Id to
														// 0.
	}

	/**
	 * returns the revenues as Hashtable for the given Mailing-IDs
	 */
	@Override
	public Map<Integer, Double> getRevenue(int company_Id, List<Integer> mailingIds, int target_Id) {
		Map<Integer, Double> returnTable = new Hashtable<>();
		for (int mailingId : mailingIds) {
			returnTable.put(mailingId, getRevenue(company_Id, mailingId, target_Id));
		}
		return returnTable;
	}

	/**
	 * returns true, if a DeepTracking (Shopmessung) is activated for the given mailingID
	 * 
	 * @param mailingID
	 * @return
	 */
	public boolean revenueActivated(int mailingID) {
		return selectInt(logger, "SELECT COUNT(*) FROM rdir_url_tbl WHERE mailing_id = ? AND deep_tracking != 0", mailingID) > 0;
	}

	/**
	 * returns the amount of entries in the RDIRLOG-Table with revenues
	 * 
	 * TODO for every MailingID, a single DB-Request is started.
	 * To Optimize this, it would be better, if the full Request is done in a single Request.
	 * 
	 * @param companyId
	 * @param mailingId
	 * @return revenues
	 */
	private int getRdirLogCount(int companyId, int mailingId) {
		return selectInt(logger, "SELECT COUNT(*) FROM rdirlog_" + companyId + "_val_num_tbl WHERE mailing_id = ? AND page_tag = 'revenue'", mailingId);
	}
}
