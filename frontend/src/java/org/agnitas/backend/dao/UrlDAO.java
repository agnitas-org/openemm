/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend.dao;

import	java.sql.SQLException;
import	java.util.ArrayList;
import	java.util.HashMap;
import	java.util.List;
import	java.util.Map;

import	org.agnitas.backend.DBase;
import	org.agnitas.backend.URL;
import	org.agnitas.backend.URLExtension;

/**
 * Accesses all tag relevant tables (rdir_url_tbl, rdir_url_param_tbl
 */
public class UrlDAO {
	private long	companyID;
	private long	mailingID;
	
	public UrlDAO (long forCompanyID, long forMailingID) {
		companyID = forCompanyID;
		mailingID = forMailingID;
	}
	
	public List <URL> retrieve (DBase dbase, URLExtension urlExtension) throws SQLException {
		try (DBase.With with = dbase.with ()) {
			List <Map <String, Object>>	rq;
			String 				escape = dbase.isOracle () ? "" : "`";
			Map <Long, URL>			urls = new HashMap <> ();
			
			rq = dbase.query (with.cursor(),
					  "SELECT ut.url_id, ut.full_url, " + escape + "usage" + escape + ", ut.admin_link, ut.original_url, ut.static_value, up.param_key, up.param_value " +
					  "FROM rdir_url_tbl ut LEFT OUTER JOIN rdir_url_param_tbl up ON up.url_id = ut.url_id " +
					  "WHERE ut.company_id = :companyID AND ut.mailing_id = :mailingID AND (ut.deleted IS NULL OR ut.deleted = 0) AND (up.param_type IS NULL OR up.param_type = :paramType)",
					  "companyID", companyID, "mailingID", mailingID, "paramType", "LinkExtension");
			
			for (int n = 0; n < rq.size (); ++n) {
				Map <String, Object>	
					row = rq.get (n);
				long	urlID = dbase.asLong (row.get ("url_id"));
				URL	url = urls.get (urlID);
				long	usage = dbase.asLong (row.get ("usage"));
				String	paramKey = dbase.asString (row.get ("param_key"));
				String	paramValue = dbase.asString (row.get ("param_value"));

				if ((url == null) && ((usage > 0) || ((paramKey != null) && (paramValue != null)))) {
					url = new URL (urlID, dbase.asString (row.get ("full_url")), usage);
					url.setAdminLink (dbase.asInt (row.get ("admin_link")) > 0);
					url.setOriginalURL (dbase.asString (row.get ("original_url")));
					url.setStaticValue (dbase.asInt (row.get ("static_value")) == 1);
					urls.put (urlID, url);
				}
				if ((url != null) && (usage == 0) && (paramKey != null) && (paramValue != null)) {
					urlExtension.add (urlID, paramKey, paramValue);
				}
			}
			return new ArrayList<>(urls.values ());
		}
	}
}


