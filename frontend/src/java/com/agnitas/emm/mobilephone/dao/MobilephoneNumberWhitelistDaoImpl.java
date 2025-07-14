/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.mobilephone.dao;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.dao.impl.mapper.StringRowMapper;

import java.util.List;

/**
 * Implementation of {@link MobilephoneNumberWhitelistDao} interface.
 */
public final class MobilephoneNumberWhitelistDaoImpl extends BaseDaoImpl implements MobilephoneNumberWhitelistDao {

	@Override
	public final List<String> readWhitelistPatterns(final int companyID) {
		final String sql = "SELECT phone_pattern FROM mobilephone_whitelist_tbl WHERE company_id=?";

		return select(sql, StringRowMapper.INSTANCE, companyID);
	}

}
