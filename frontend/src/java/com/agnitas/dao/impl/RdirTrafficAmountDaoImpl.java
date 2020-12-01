/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.agnitas.beans.impl.CompanyStatus;
import org.agnitas.dao.RdirTrafficAmountDao;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class RdirTrafficAmountDaoImpl extends BaseDaoImpl implements RdirTrafficAmountDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(RdirTrafficAmountDaoImpl.class);

	@Override
    public void save(@VelocityCheck int companyID, int mailingID, String contentName, int contentSize) {
		// Insert new entry
		update(logger, "INSERT INTO rdir_traffic_amount_" + companyID + "_tbl (mailing_id, content_name, content_size, demand_date) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",
			mailingID,
			contentName,
			contentSize
		);
	}

	@Override
	public void aggregateExistingTrafficAmountEntries(@VelocityCheck int companyID, Date dateToAggregate) {
		Date dateToAggregateStart = DateUtilities.removeTime(dateToAggregate, TimeZone.getDefault());
		Date dateToAggregateEnd = DateUtilities.addDaysToDate(dateToAggregateStart, 1);
		
		String aggregatedRdirTrafficTableName = "rdir_traffic_agr_" + companyID + "_tbl";
		
		update (logger,
			"INSERT INTO " + aggregatedRdirTrafficTableName + " (mailing_id, content_name, content_size, demand_date, amount) ("
			    + " SELECT mailing_id, content_name, content_size, ?, COUNT(*) FROM rdir_traffic_amount_" + companyID + "_tbl"
			    + " WHERE demand_date >= ? AND demand_date < ?"
			    + " GROUP BY mailing_id, content_name, content_size"
		    + " )", dateToAggregateStart, dateToAggregateStart, dateToAggregateEnd);
		
		update(logger, "DELETE FROM rdir_traffic_amount_" + companyID + "_tbl WHERE demand_date >= ? AND demand_date < ?", dateToAggregateStart, dateToAggregateEnd);
    }
	
	@Override
	public List<Integer> getCompaniesForAggregation(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds) {
		return select(logger, "SELECT company_id FROM company_tbl WHERE status = '" + CompanyStatus.ACTIVE.getDbValue() + "'"
			+ (includedCompanyIds != null && !includedCompanyIds.isEmpty() ? " AND company_id IN (" + StringUtils.join(includedCompanyIds, ", ") + ")" : "")
			+ (excludedCompanyIds != null && !excludedCompanyIds.isEmpty() ? " AND company_id NOT IN (" + StringUtils.join(excludedCompanyIds, ", ") + ")" : "")
			+ " ORDER BY company_id",
		new IntegerRowMapper());
	}
	/**
	 * called for reseting company tables
	 */
	@Override
	public boolean emtpyTrafficTables(@VelocityCheck int companyID) {
		try {
			update(logger, "TRUNCATE TABLE rdir_traffic_agr_"+ companyID + "_tbl");
			update(logger, "TRUNCATE TABLE rdir_traffic_amount_"+ companyID + "_tbl");
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	/**
	 * Called when company table have to be deleted
	 */
	@Override
	public boolean dropTrafficTables(@VelocityCheck int companyID) {
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
