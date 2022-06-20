/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.agnitas.backend.DBase;
import org.agnitas.util.Title;

/**
 * Accesses title relevant data from the database, these are the
 * tables title_tbl and title_gender_tbl.
 */
public class TitleDAO {
	private Map <Long, Title>	titles;
	
	public TitleDAO (DBase dbase, long forCompanyID) throws SQLException {
		List <Map <String, Object>>	rq;
			
		titles = new HashMap <> ();
		try (DBase.With with = dbase.with ()) {
			rq = dbase.query (with.jdbc (),
					  "SELECT title_id, title, gender FROM title_gender_tbl " +
					  "WHERE title_id IN (SELECT title_id FROM title_tbl WHERE company_id = :companyID OR company_id = 0 OR company_id IS null)",
					  "companyID", forCompanyID);
			for (int n = 0; n < rq.size (); ++n) {
				Map <String, Object>	row = rq.get (n);
				Long			id = dbase.asLong (row.get ("title_id"));
				String			title = dbase.asString (row.get ("title"));
				int			gender = dbase.asInt (row.get ("gender"));
				Title			cur = null;

				if ((cur = titles.get(id)) == null) {
					cur = new Title(id);
					titles.put(id, cur);
				}
				cur.setTitle(gender, title);
			}
		}
	}

	public Map<Long, Title> titles() {
		return titles;
	}
}
