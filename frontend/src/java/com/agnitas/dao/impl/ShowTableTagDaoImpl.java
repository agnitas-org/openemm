/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.util.List;
import java.util.Map;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.dao.ShowTableTagDao;

public class ShowTableTagDaoImpl extends BaseDaoImpl implements ShowTableTagDao {

	private static final Logger logger = LogManager.getLogger(ShowTableTagDaoImpl.class);
	
	@Override
	public final List<Map<String, Object>> select(String sqlSelectStatement, int maxRows, int startOffset) {
		if (maxRows == 0) {
			return select(logger, sqlSelectStatement);
		} else {
			int rowcount = maxRows + startOffset;
			String row = "";
			if (isOracleDB()) {
				row = "WHERE ROWNUM <= " + Math.max(rowcount, 1000);
			} else {
				row = "LIMIT " + Math.max(rowcount, 1000);
			}
			return select(logger, "SELECT * FROM(" + sqlSelectStatement + ") subsel0 " + row);
		}
	}
}
