/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.util.quota.dao;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.dao.impl.mapper.StringRowMapper;

import java.util.List;
import java.util.Optional;

public final class RestfulQuotaDaoImpl extends BaseDaoImpl implements RestfulQuotaDao {

	@Override
	public final Optional<String> readQuotaSpecs(final String username, final int companyId) {
		final String sql = "SELECT quota FROM restful_quota_tbl q, admin_tbl a WHERE a.company_id=? AND a.username=? AND q.admin_id=a.admin_id";
		
		final List<String> list = select(sql, StringRowMapper.INSTANCE, companyId, username);

		return list.isEmpty()
				? Optional.empty()
				: Optional.of(list.get(0));
	}

}
