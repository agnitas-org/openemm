/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.agnitas.target.TargetFactory;
import org.agnitas.util.DbUtilities;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.ComTarget;

/**
 * Implementation of {@link RowMapper} reading complete target groups.
 */
public class TargetRowMapper implements RowMapper<ComTarget> {

    final TargetFactory targetFactory;

    public TargetRowMapper(final TargetFactory targetFactory) {
        this.targetFactory = targetFactory;
    }

    @Override
    public ComTarget mapRow(final ResultSet resultSet, final int row) throws SQLException {
        try {
            final ComTarget readTarget = targetFactory.newTarget();
            readTarget.setId(resultSet.getInt("target_id"));
            readTarget.setCompanyID(resultSet.getInt("company_id"));
            readTarget.setTargetDescription(resultSet.getString("target_description"));
            readTarget.setTargetName(resultSet.getString("target_shortname"));
            readTarget.setTargetSQL(resultSet.getString("target_sql"));
            readTarget.setDeleted(resultSet.getInt("deleted"));
            readTarget.setCreationDate(resultSet.getTimestamp("creation_date"));
            readTarget.setChangeDate(resultSet.getTimestamp("change_date"));
            readTarget.setAdminTestDelivery(resultSet.getBoolean("admin_test_delivery"));
            readTarget.setLocked(resultSet.getBoolean("locked"));
            readTarget.setComplexityIndex(resultSet.getInt("complexity"));
            readTarget.setValid(!resultSet.getBoolean("invalid"));
            readTarget.setComponentHide(resultSet.getBoolean("component_hide"));
            if (DbUtilities.resultsetHasColumn(resultSet, "favorite")) {
                readTarget.setFavorite(resultSet.getBoolean("favorite"));
            }

            final String eql = readEql(resultSet);

            readTarget.setEQL(eql != null ? eql : "");

            return readTarget;
        } catch (Exception e) {
            throw new SQLException("Cannot create Target item from ResultSet row", e);
        }
    }

    /**
     * Reads EQL from database, if EQL is available for target group.
     *
     * @param resultSet {@link ResultSet} to read EQL from
     *
     * @return EQL code
     *
     * @throws SQLException on errors accessing database data
     */
    private String readEql(final ResultSet resultSet) throws SQLException {
        return resultSet.getString("eql");
    }

}
