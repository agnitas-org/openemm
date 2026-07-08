/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agnitas.backend.DBase;

/**
 * Accesses all tag relevant tables (tag_tbl, tag_function_tbl)
 */
public class TagDAO {
	public static class Entry {
		private String selectValue;
		private String type;

		public Entry(String nSelectValue, String nType) {
			selectValue = nSelectValue;
			type = nType;
		}

		public String selectValue() {
			return selectValue;
		}

		public String type() {
			return type;
		}
	}

	public static class Function {
		private String lang;
		private String code;

		public Function(String nLang, String nCode) {
			lang = nLang;
			code = nCode;
		}

		public String lang() {
			return lang;
		}

		public String code() {
			return code;
		}
	}

	private long companyID;
	private Map<String, Entry> tags;
	private Map<String, Function> functionTags;

	public TagDAO(DBase dbase, long forCompanyID) throws SQLException {
		companyID = forCompanyID;
		tags = new HashMap<>();
		functionTags = new HashMap<>();
		try (DBase.With with = dbase.with()) {
			List<Map<String, Object>> rq;

			rq = dbase.query(with.cursor(),
					 "SELECT tagname, selectvalue, type " +
					"FROM tag_tbl " +
					"WHERE company_id IN (0, :companyID) ORDER BY company_id", "companyID", companyID);
			for (int n = 0; n < rq.size(); ++n) {
				Map<String, Object> row = rq.get(n);

				tags.put(dbase.asString(row.get("tagname")), new Entry(dbase.asString(row.get("selectvalue")), dbase.asString(row.get("type"))));
			}
		}
	}

	public Entry get(String tagName) {
		return tags.get(tagName);
	}

	public Function getFunction(DBase dbase, String functionName) throws SQLException {
		Function rc;

		if (functionTags.containsKey(functionName)) {
			rc = functionTags.get(functionName);
		} else {
			rc = null;
			try (DBase.With with = dbase.with()) {
				List<Map<String, Object>> rq;

				rq = dbase.query(with.cursor(),
						 "SELECT lang, code " +
						"FROM tag_function_tbl " +
						"WHERE name = :name AND company_id IN (0, :companyID) ORDER BY company_id DESC",
						"name", functionName, "companyID", companyID);
				for (int n = 0; (n < rq.size()) && (rc == null); ++n) {
					Map<String, Object> row = rq.get(n);
					String lang = dbase.asString(row.get("lang"));
					String code = dbase.asClob(row.get("code"));

					if ((lang != null) && (code != null)) {
						rc = new Function(lang, code);
					}
				}
				functionTags.put(functionName, rc);
			}
		}
		return rc;
	}
}
