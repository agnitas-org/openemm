/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.TargetLight;
import com.agnitas.beans.impl.TargetLightImpl;

/**
 * Row mapper for target group data without target representation.
 */
public class TargetLightRowMapper implements RowMapper<TargetLight> {
	
	/** Singleton instance for use in persistence layer. */
	public static final transient TargetLightRowMapper INSTANCE = new TargetLightRowMapper();
	
    @Override
    public TargetLight mapRow(ResultSet resultSet, int row) throws SQLException {
        try {
            TargetLightImpl readTarget = new TargetLightImpl();
            readTarget.setId(resultSet.getInt("target_id"));
            readTarget.setCompanyID(resultSet.getInt("company_id"));
            readTarget.setTargetDescription(resultSet.getString("target_description"));
            readTarget.setTargetName(resultSet.getString("target_shortname"));
            readTarget.setLocked(resultSet.getBoolean("locked"));
            readTarget.setCreationDate(resultSet.getTimestamp("creation_date"));
            readTarget.setChangeDate(resultSet.getTimestamp("change_date"));
            readTarget.setDeleted(resultSet.getInt("deleted"));
            readTarget.setComponentHide(resultSet.getBoolean("component_hide"));
            readTarget.setComplexityIndex(resultSet.getInt("complexity"));
            readTarget.setValid(!resultSet.getBoolean("invalid"));
            readTarget.setFavorite(resultSet.getBoolean("favorite"));

            return readTarget;
        } catch (Exception e) {
            throw new SQLException("Cannot create TargetLight item from ResultSet row", e);
        }
    }
}
