/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.util.quota.dao;

import com.agnitas.dao.impl.BaseDaoImpl;

public final class RestfulApiInvocationCostsDaoImpl extends BaseDaoImpl implements RestfulApiInvocationCostsDao {

	/** Default costs. */
	public static final transient int DEFAULT_INVOCATION_COSTS = 1;
	
	@Override
	public final int invocationCostsForApi(final int companyID, final String endpointName) {
		int costs = queryCosts(companyID, endpointName);
		
		if(costs == 0) {
			costs = queryCosts(0, endpointName);
		}
		
		if(costs == 0) {
			costs = DEFAULT_INVOCATION_COSTS;
		}
		
		return costs;
	}

	private final int queryCosts(final int companyId, final String apiName) {
		final String sql = "SELECT costs FROM restful_api_costs_tbl WHERE name=? AND company_id=?";

		return selectInt(sql, apiName, companyId);
	}

}
