/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.dao.CssDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CssDaoImpl extends BaseDaoImpl implements CssDao {

	/** Generate a map from all names and values of the css_tbl dependent on the companyID*/
	@Override
	public Map<String, String> getCssParameterData(int companyID) {
		// Sort by companyid, so that the more specific value that fits for the given companyid overrides the companyid 0 values
		String sql = "SELECT name, value FROM css_tbl WHERE company_id = 0 OR company_id = " + companyID + " ORDER BY company_id ASC";
		List<Map<String, Object>> result = select(sql);
		Map<String, String> returnMap = new HashMap<>();
		for (Map<String, Object> row : result) {
			String name = (String) row.get("name");
			String value = (String) row.get("value");
			returnMap.put(name, value);
		}
		return returnMap;
	}
}
