/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.LayoutDao;

public class LayoutDaoImpl extends BaseDaoImpl implements LayoutDao {

	@Override
	public Map<String, Map<Integer, byte[]>> getLayoutData() {
		String sql = "SELECT company_id, item_name, data FROM layout_tbl";
		List<Map<String, Object>> result = select(sql);
		Map<String, Map<Integer, byte[]>> returnMap = new HashMap<>();
		for (Map<String, Object> row : result) {
			int companyID = ((Number) row.get("company_id")).intValue();
			String itemName = (String) row.get("item_name");
			byte[] data = (byte[]) row.get("data");
			if (!returnMap.containsKey(itemName)) {
				returnMap.put(itemName, new HashMap<>());
			}
			returnMap.get(itemName).put(companyID, data);
		}
		return returnMap;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void saveLayoutData(int companyID, String itemName, byte[] data) {
		int existsCount = selectInt("SELECT COUNT(*) FROM layout_tbl WHERE company_id = ? AND item_name = ?", companyID, itemName);
		if (existsCount == 0) {
			update("INSERT INTO layout_tbl (company_id, item_name) VALUES (?, ?)", companyID, itemName);
		}
		updateBlob("UPDATE layout_tbl SET data = ? WHERE company_id = ? AND item_name = ?", data, companyID, itemName);
	}

}
