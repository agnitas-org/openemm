/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dao.impl;

import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.dao.LightTargetDao;
import com.agnitas.dao.impl.BaseDaoImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class LightTargetDaoImpl extends BaseDaoImpl implements LightTargetDao {

	@Override
	public LightTarget getTarget(int targetID, int companyID) {
		String query = "SELECT target_id, target_description, target_shortname, target_sql FROM dyn_target_tbl WHERE target_id = ? AND company_id = ?";
		return selectObjectDefaultNull(query, new LightTarget_RowMapper(), targetID, companyID);
	}

	@Override
	public List<LightTarget> getTargets(List<String> targetIDs, int companyID) {
		if (targetIDs == null || targetIDs.isEmpty()) {
			return null;
		}

		String query = "SELECT target_id, target_description, target_shortname, target_sql FROM dyn_target_tbl WHERE target_id IN (" + StringUtils.join(targetIDs, ", ") + ") AND company_id = ? ORDER By target_id";
		return select(query, new LightTarget_RowMapper(), companyID);
	}
	
    protected static class LightTarget_RowMapper implements RowMapper<LightTarget> {
		@Override
		public LightTarget mapRow(ResultSet resultSet, int row) throws SQLException {
			LightTarget target = new LightTarget();
			target.setId(resultSet.getInt("target_id"));
			target.setName(resultSet.getString("target_shortname"));
			target.setDescription(resultSet.getString("target_description"));
			target.setTargetSQL(resultSet.getString("target_sql"));
			return target;
		}
	}
}
