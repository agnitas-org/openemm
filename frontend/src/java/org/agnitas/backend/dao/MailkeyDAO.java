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
import	org.agnitas.util.ParameterParser;

public class MailkeyDAO {
	static private final String	mailkeyTable = "mail_key_tbl";

	public class Mailkey {
		private long	id;
		private boolean	local;
		private String	method;
		private Map <String, String>
				parameter;
		private String	key;
		public Mailkey (long id, boolean local, String method, String parameter, String key) {
			this.id = id;
			this.local = local;
			this.method = method;
			this.parameter = (new ParameterParser (parameter)).parse ();
			this.key = key;
		}
		public Mailkey (long id, boolean local, String method, Map <String, String> parameter, String key) {
			this.id = id;
			this.local = local;
			this.method = method;
			this.parameter = parameter;
			this.key = key;
		}
		
		public long id () {
			return id;
		}
		public boolean local () {
			return local;
		}
		public String method () {
			return method;
		}
		public Map <String, String> parameter () {
			return parameter;
		}
		public String key () {
			return key;
		}
	}
	
	private List <Mailkey>	mailkeys;
	
	public MailkeyDAO (DBase dbase, long companyID, List <DkimDAO.DKIM> dkims) throws SQLException {
		mailkeys = new ArrayList <> ();
		
		try (DBase.With with = dbase.with ()) {
			List<Map<String, Object>> rq;
			
			if (dbase.exists (mailkeyTable)) {
				rq = dbase.query (with.cursor (),
						  "SELECT mail_key_id, company_id, method, parameter, mail_key " +
						  "FROM " + mailkeyTable + " " +
						  "WHERE company_id IN (0, :companyID) AND " +
						  "      ((valid_start IS NULL) OR (valid_start <= CURRENT_TIMESTAMP)) AND " +
						  "      ((valid_end IS NULL) OR (valid_end >= CURRENT_TIMESTAMP)) " +
						  "ORDER BY mail_key_id",
						  "companyID", companyID);
				for (int n = 0; n < rq.size (); ++n) {
					Map <String, Object>	row = rq.get (n);
					
					mailkeys.add (new Mailkey (
								   dbase.asLong (row.get ("mail_key_id")),
								   dbase.asLong (row.get ("company_id")) == companyID,
								   dbase.asString (row.get ("method")),
								   dbase.asString (row.get ("parameter")),
								   dbase.asString (row.get ("mail_key"))
					));
				}
			}
			if (dkims != null) {
				for (DkimDAO.DKIM dkim : dkims) {
					Map <String, String>	parameter = new HashMap <> ();
					
					parameter.put ("selector", dkim.selector ());
					parameter.put ("domain", dkim.domain ());
					mailkeys.add (new Mailkey (dkim.id (), dkim.local (), "dkim", parameter, dkim.key ()));
				}
			}
		}
	}
	public List <Mailkey> mailkeys () {
		return mailkeys;
	}
}
