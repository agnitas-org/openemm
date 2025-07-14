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
import com.agnitas.backend.DynCont;
import com.agnitas.backend.DynName;
import com.agnitas.backend.StringOps;
import com.agnitas.util.Log;

public class ContentDAO {
	private long companyID;
	private long mailingID;

	public ContentDAO(long forCompanyID, long forMailingID) {
		companyID = forCompanyID;
		mailingID = forMailingID;
	}
	
	public Map <Long, DynName> getDynamicContent (DBase dbase) throws SQLException {
		Map <Long, DynName>	names = new HashMap <> ();
		
		try (DBase.With with = dbase.with ()) {
			List <Map <String, Object>>	rq;
	
			rq = dbase.query (with.cursor (),
					  "SELECT dyn_name_id, dyn_name, interest_group, no_link_extension " +
					  "FROM dyn_name_tbl " +
					  "WHERE mailing_id = :mailingID AND company_id = :companyID AND (deleted IS NULL OR deleted = 0)",
					  "mailingID", mailingID,
					  "companyID", companyID);
			for (int n = 0; n < rq.size (); ++n) {
				Map <String, Object>	row = rq.get (n);
				long			nameID = dbase.asLong (row.get ("dyn_name_id"));
				String			name = dbase.asString (row.get ("dyn_name"));

				if (!names.containsKey(nameID)) {
					DynName dno = new DynName(name, nameID);

					dno.setInterest(dbase.asString(row.get("interest_group")));
					dno.setDisableLinkExtension(dbase.asInt(row.get("no_link_extension")) == 1);
					names.put(nameID, dno);
					dbase.logging(Log.DEBUG, "content", "Added dynamic name " + name);
				} else
					dbase.logging(Log.DEBUG, "content", "Skip already recorded name " + name);
			}
			
			rq = dbase.query (with.cursor (),
					  "SELECT dyn_content_id, dyn_name_id, target_id, dyn_order, dyn_content FROM dyn_content_tbl " +
					  "WHERE dyn_name_id IN (" +
					  "      SELECT dyn_name_id FROM dyn_name_tbl WHERE mailing_id = :mailingID AND company_id = :companyID AND (deleted IS NULL OR deleted = 0)" +
					  ")",
					  "mailingID", mailingID,
					  "companyID", companyID);
			for (int n = 0; n < rq.size (); ++n) {
				Map <String, Object>	row = rq.get (n);
				long			dyncontID = dbase.asLong (row.get ("dyn_content_id"));
				long			nameID = dbase.asLong (row.get ("dyn_name_id"));
				long			targetID = dbase.asLong (row.get ("target_id"));
				long			order = dbase.asLong (row.get ("dyn_order"));
				String			content = dbase.asClob (row.get ("dyn_content"));
				DynName			name;

				if ((name = names.get(nameID)) != null) {
					name.add(new DynCont(dyncontID, targetID, order, content != null ? StringOps.convertOld2New(content) : null));
				} else {
					dbase.logging(Log.WARNING, "content", "Found content for name-ID " + nameID + " without an entry in dyn_name_tbl");
				}
			}
		}
		return names;
	}
}
