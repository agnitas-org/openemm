/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.Date;
import java.util.List;


public interface RdirTrafficAmountDao	{
	void save(int companyID, int mailingID, String contentName, int contentSize);

	void aggregateExistingTrafficAmountEntries(int companyID, Date dateToAggregate);

	List<Integer> getCompaniesForAggregation(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds);
	
	boolean emtpyTrafficTables(int companyID);
	
	boolean dropTrafficTables(int companyID);
}
