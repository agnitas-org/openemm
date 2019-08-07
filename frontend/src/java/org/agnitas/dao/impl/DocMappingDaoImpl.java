/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.agnitas.dao.DocMappingDao;
import org.apache.log4j.Logger;

public class DocMappingDaoImpl extends BaseDaoImpl implements DocMappingDao{
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(DocMappingDaoImpl.class);
	
    @Override
	public String getDocMapping(String pagekey) {
    	return select(logger, "SELECT filename FROM doc_mapping_tbl WHERE pagekey = ?", String.class, pagekey);
	}

	@Override
	public Map<String, String> getDocMapping() {
		List<Map<String, Object>> result = select(logger, "SELECT pagekey, filename FROM doc_mapping_tbl");
		Map<String, String> returnMap = new HashMap<>();
		for (Map<String, Object> row: result) {
			returnMap.put((String) row.get("pagekey"), (String) row.get("filename"));
		}
		return returnMap;
	}
}
