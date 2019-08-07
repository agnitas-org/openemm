/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.List;
import java.util.Map;

import org.agnitas.emm.core.velocity.VelocityCheck;

/**
 * ComRevenueDao is used to provide an Interface for getting Revenue Data
 * from the Database. Revenue Data is only available if Deeptracking is permitted.
 * The revenue-Data are found in the RDIRLOG_<Company_ID>_VAL_NUM_TBL and called NUM_PARAMETER
 */
public interface ComRevenueDao {
	/**
	 *  returns the revenue as Double-Value
	 */
	public Double getRevenue(@VelocityCheck int company_Id, int mailing_Id, int target_Id);
	
	/**
	 * returns the revenue as Double-Value
	 */	
	public Double getRevenue(@VelocityCheck int company_Id, int mailing_Id);
	
	/**
	 * returns the revenues as Map with key=MailID and value=revenue.
	 * 
	 */
	public Map<Integer, Double> getRevenue(@VelocityCheck int company_Id, List<Integer> mailingIds, int target_Id);
}
