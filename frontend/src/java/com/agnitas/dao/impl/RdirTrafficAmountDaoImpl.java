/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.beans.impl.CompanyStatus;
import com.agnitas.dao.RdirTrafficAmountDao;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.DbUtilities;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class RdirTrafficAmountDaoImpl extends BaseDaoImpl implements RdirTrafficAmountDao {

	@Override
    public void save(int companyID, int mailingID, String contentName, int contentSize) {
		// Insert new entry
		update("INSERT INTO rdir_traffic_amount_" + companyID + "_tbl (mailing_id, content_name, content_size, demand_date) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",
			mailingID,
			contentName,
			contentSize
		);
	}

	@Override
	public void aggregateExistingTrafficAmountEntries(int companyID, Date dateToAggregate) {
		Date dateToAggregateStart = DateUtilities.removeTime(dateToAggregate, TimeZone.getDefault());
		Date dateToAggregateEnd = DateUtilities.addDaysToDate(dateToAggregateStart, 1);
		
		String aggregatedRdirTrafficTableName = "rdir_traffic_agr_" + companyID + "_tbl";

		update(
			"INSERT INTO " + aggregatedRdirTrafficTableName + " (mailing_id, content_name, content_size, demand_date, amount) ("
			    + " SELECT mailing_id, content_name, content_size, ?, COUNT(*) FROM rdir_traffic_amount_" + companyID + "_tbl"
			    + " WHERE demand_date >= ? AND demand_date < ?"
			    + " GROUP BY mailing_id, content_name, content_size"
		    + " )", dateToAggregateStart, dateToAggregateStart, dateToAggregateEnd);
		
		update("DELETE FROM rdir_traffic_amount_" + companyID + "_tbl WHERE demand_date >= ? AND demand_date < ?", dateToAggregateStart, dateToAggregateEnd);
    }
	
	@Override
	public List<Integer> getCompaniesForAggregation(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds) {
		return select("SELECT company_id FROM company_tbl WHERE status = '" + CompanyStatus.ACTIVE.getDbValue() + "'"
			+ (includedCompanyIds != null && !includedCompanyIds.isEmpty() ? " AND company_id IN (" + StringUtils.join(includedCompanyIds, ", ") + ")" : "")
			+ (excludedCompanyIds != null && !excludedCompanyIds.isEmpty() ? " AND company_id NOT IN (" + StringUtils.join(excludedCompanyIds, ", ") + ")" : "")
			+ " ORDER BY company_id",
		IntegerRowMapper.INSTANCE);
	}
	/**
	 * called for reseting company tables
	 */
	@Override
	public boolean emptyTrafficTables(int companyID) {
		try {
			update("TRUNCATE TABLE rdir_traffic_agr_"+ companyID + "_tbl");
			update("TRUNCATE TABLE rdir_traffic_amount_"+ companyID + "_tbl");
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	/**
	 * Called when company table have to be deleted
	 */
	@Override
	public boolean dropTrafficTables(int companyID) {
		try {
			String trafficAgrTable = "rdir_traffic_agr_" + companyID + "_tbl";
			String trafficAmountTable = "rdir_traffic_amount_" + companyID + "_tbl";
			
			if (DbUtilities.checkIfTableExists(getDataSource(), trafficAgrTable)) {
				DbUtilities.dropTable(getDataSource(), trafficAgrTable);
			}
			if (DbUtilities.checkIfTableExists(getDataSource(), trafficAmountTable)) {
				DbUtilities.dropTable(getDataSource(), trafficAmountTable);
			}
			return true;
		} catch(Exception e) {
			return false;
		}
	}
}
