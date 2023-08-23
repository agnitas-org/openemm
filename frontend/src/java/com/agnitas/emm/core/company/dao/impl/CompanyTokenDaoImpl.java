/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.dao.impl;

import java.util.List;
import java.util.Optional;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.emm.core.company.dao.CompanyTokenDao;
import com.agnitas.emm.core.company.service.UnknownCompanyTokenException;
import com.agnitas.emm.core.servicemail.UnknownCompanyIdException;

public final class CompanyTokenDaoImpl extends BaseDaoImpl implements CompanyTokenDao {

	private static final Logger LOGGER = LogManager.getLogger(CompanyTokenDaoImpl.class);
	
	@Override
	public final int getCompanyIdByToken(final String token) throws UnknownCompanyTokenException {
		final List<Integer> list = select(LOGGER, "SELECT company_id FROM company_tbl WHERE company_token=?", IntegerRowMapper.INSTANCE, token);

		// For security reasons we also report a UnknownCompanyTokenException when token is assigned to more than 1 company
		if(list.size() != 1) {
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format("Found company token '%s' in %d companies", token, list.size()));
			}
			
			throw new UnknownCompanyTokenException(token);
		}
		
		return list.get(0);
	}

	@Override
	public final Optional<String> getCompanyToken(final int companyID) throws UnknownCompanyIdException {
		final List<String> list = select(LOGGER, "SELECT company_token FROM company_tbl WHERE company_id=?", StringRowMapper.INSTANCE, companyID);
		
		if(list.isEmpty()) {
			throw new UnknownCompanyIdException(companyID);
		}
		
		return Optional.ofNullable(list.get(0));
	}

	@Override
	public final void assignToken(final int companyID, final String token) {
		update(LOGGER, "UPDATE company_tbl SET company_token=? WHERE company_id=?", token, companyID);
	}
}
