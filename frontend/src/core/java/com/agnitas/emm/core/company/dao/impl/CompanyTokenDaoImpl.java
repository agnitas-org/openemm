/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.dao.impl;

import java.util.Optional;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.core.company.dao.CompanyTokenDao;

public final class CompanyTokenDaoImpl extends BaseDaoImpl implements CompanyTokenDao {

	@Override
	public Optional<Integer> getCompanyIdByToken(String token) {
		Integer companyId = selectIntDefaultNull("SELECT company_id FROM company_tbl WHERE company_token = ?", token);
		return Optional.ofNullable(companyId);
	}

	@Override
	public Optional<String> getCompanyToken(int companyID) {
		String token = selectStringDefaultNull("SELECT company_token FROM company_tbl WHERE company_id = ?", companyID);
		return Optional.ofNullable(token);
	}

	@Override
	public void assignToken(int companyID, String token) {
		update("UPDATE company_tbl SET company_token = ? WHERE company_id = ?", token, companyID);
	}
}
