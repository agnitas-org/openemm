/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.PermissionDao;
import com.agnitas.emm.core.Permission;

/**
 * DAO handler for ComAdmin-Objects
 * This class is compatible with oracle and mysql datasources and databases
 */
public class PermissionDaoImpl extends BaseDaoImpl implements PermissionDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(PermissionDaoImpl.class);
	
    @Override
	public List<Permission> getAllPermissions() {
    	List<Permission> list = select(logger, "SELECT permission_name, category, sub_category, sort_order, feature_package FROM permission_tbl", new Permission_RowMapper());
    	
		while (list.remove(null)) {
			// do nothing here, just remove null entries
		}
		
		return list;
    }
    
    @Override
	public List<Permission> getPermissionsByCategory(String categrory) {
    	String sql = "SELECT permission_name, category, sub_category, sort_order, feature_package FROM permission_tbl WHERE category = ?";
		return select(logger, sql, new Permission_RowMapper(), categrory);
    }

    protected class Permission_RowMapper implements RowMapper<Permission> {
		@Override
		public Permission mapRow(ResultSet resultSet, int row) throws SQLException {
			Permission readPermission = Permission.getPermissionByToken(resultSet.getString("permission_name"));

			if (readPermission != null) {
				readPermission.setCategory(resultSet.getString("category"));
				readPermission.setSubCategory(resultSet.getString("sub_category"));
				readPermission.setSortOrder(resultSet.getInt("sort_order"));
				readPermission.setFeaturePackage(resultSet.getString("feature_package"));

				return readPermission;
			} else {
				return null;
			}
		}
	}
}
