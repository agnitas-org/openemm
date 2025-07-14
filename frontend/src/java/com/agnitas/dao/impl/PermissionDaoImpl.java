/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.dao.PermissionDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.PermissionInfo;
import com.agnitas.dao.impl.mapper.StringRowMapper;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * DAO handler for Admin-Objects
 * This class is compatible with oracle and mysql datasources and databases
 */
public class PermissionDaoImpl extends BaseDaoImpl implements PermissionDao {

    @Override
	public List<Permission> getAllPermissions() {
    	List<Permission> list = select("SELECT permission_name FROM permission_tbl ORDER BY category, sub_category, sort_order, permission_name", new Permission_RowMapper())
				.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		// Read display order of categories
		if (Permission.CATEGORY_DISPLAY_ORDER.length == 0) {
			List<String> newCategoriesOrder = select("SELECT category_name FROM permission_category_tbl ORDER BY sort_order, category_name", StringRowMapper.INSTANCE);
			Permission.CATEGORY_DISPLAY_ORDER = newCategoriesOrder.toArray(new String[0]);
		}

		// Read display order of subcategories
		if (Permission.SUBCATEGORY_DISPLAY_ORDER.size() == 0) {
			for (String category : Permission.CATEGORY_DISPLAY_ORDER) {
				List<String> newSubCategoriesOrder = select("SELECT subcategory_name FROM permission_subcategory_tbl WHERE category_name = ? ORDER BY sort_order, subcategory_name", StringRowMapper.INSTANCE, category);
				Permission.SUBCATEGORY_DISPLAY_ORDER.put(category, newSubCategoriesOrder.toArray(new String[0]));
			}
		}

		return list;
    }

    @Override
	public LinkedHashMap<String, PermissionInfo> getPermissionInfos() {
		String sortDirectionPart;
		if (isOracleDB()) {
			sortDirectionPart = "ASC NULLS FIRST";
		} else {
			sortDirectionPart = "ASC";
		}
    	List<Map<String, Object>> result = select("SELECT permission_name, category, sub_category, sort_order, feature_package, creation_date FROM permission_tbl ORDER BY category " + sortDirectionPart + ", sub_category " + sortDirectionPart + ", sort_order " + sortDirectionPart + ", permission_name");
    	LinkedHashMap<String, PermissionInfo> returnMap = new LinkedHashMap<>();
    	for (Map<String, Object> row : result) {
    		String permissionName = (String) row.get("permission_name");
    		PermissionInfo permissionInfo = new PermissionInfo((String) row.get("category"), (String) row.get("sub_category"), ((Number) row.get("sort_order")).intValue(), (String) row.get("feature_package"), (Date) row.get("creation_date"));
    		returnMap.put(permissionName, permissionInfo);
    	}
		return returnMap;
    }

    protected static class Permission_RowMapper implements RowMapper<Permission> {
		@Override
		public Permission mapRow(ResultSet resultSet, int row) throws SQLException {
			return Permission.getPermissionByToken(resultSet.getString("permission_name"));
		}
	}

	@Override
	public List<String> getAllCategoriesOrdered() {
		String sql = "SELECT DISTINCT category, sub_category FROM permission_tbl ORDER BY category, sub_category";
		List<String> categoriesFromDB = select(sql, StringRowMapper.INSTANCE);
		List<String> allCategoriesOrdered = new ArrayList<>();
		for (String orderedCategory : Permission.CATEGORY_DISPLAY_ORDER) {
			if (categoriesFromDB.contains(orderedCategory)) {
				allCategoriesOrdered.add(orderedCategory);
				categoriesFromDB.remove(orderedCategory);
			}
		}

		boolean addSystemAtEnd = false;
		if (categoriesFromDB.contains(Permission.CATEGORY_KEY_SYSTEM)) {
			addSystemAtEnd = true;
			categoriesFromDB.remove(Permission.CATEGORY_KEY_SYSTEM);
		}

		// add all remaining categories which do not have a special sort order
		allCategoriesOrdered.addAll(categoriesFromDB);

		if (addSystemAtEnd) {
			allCategoriesOrdered.add(Permission.CATEGORY_KEY_SYSTEM);
		}

		return allCategoriesOrdered;
	}
}
