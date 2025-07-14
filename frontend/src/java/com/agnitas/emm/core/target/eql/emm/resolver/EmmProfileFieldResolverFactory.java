/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.resolver;

import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldResolveException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderConfiguration;

public class EmmProfileFieldResolverFactory {

	/** {@link ProfileFieldDao}, that will be used by the {@link EmmProfileFieldResolver}. */
	private ProfileFieldDao profileFieldDao;

	private QueryBuilderConfiguration queryBuilderConfiguration;

	/**
	 * Creates a new {@link EmmProfileFieldResolver} for the given company ID.
	 * 
	 * @param companyId company ID
	 * 
	 * @return instance of {@link EmmProfileFieldResolver} for given company ID
	 * 
	 * @throws ProfileFieldResolveException on errors creating new instance
	 */
	public EmmProfileFieldResolver newInstance(int companyId) throws ProfileFieldResolveException {
		return new EmmProfileFieldResolverImpl(companyId, profileFieldDao, queryBuilderConfiguration);
	}
		
	
	public void setProfileFieldDao(ProfileFieldDao dao) {
		this.profileFieldDao = dao;
	}

	public void setQueryBuilderConfiguration(QueryBuilderConfiguration queryBuilderConfiguration) {
		this.queryBuilderConfiguration = queryBuilderConfiguration;
	}
}
