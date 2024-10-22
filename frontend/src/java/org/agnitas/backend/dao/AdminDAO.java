/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend.dao;

import	java.sql.SQLException;
import	java.util.ArrayList;
import	java.util.List;
import	java.util.Map;
import	org.agnitas.backend.DBase;

public class AdminDAO {
	public static class Admin {
		private long		adminID;
		private String		email;
		private String		firstname;
		private String		lastname;
		private String		language;
		private List <String>	roles;
		public Admin (long adminID, String email, String firstname, String lastname, String language) {
			this.adminID = adminID;
			this.email = email;
			this.firstname = firstname;
			this.lastname = lastname;
			this.language = language;
			this.roles = new ArrayList <> ();
		}
		public long adminID () {
			return adminID;
		}
		public String email () {
			return email;
		}
		public String firstname () {
			return firstname;
		}
		public String lastname () {
			return lastname;
		}
		public String language () {
			return language;
		}
		public void addRole (String role) {
			roles.add (role);
		}
		public boolean hasRole (String role) {
			return roles.contains (role);
		}
	}
	public AdminDAO () {
	}
	public List <AdminDAO.Admin> getAdmins (DBase dbase, long companyID) throws SQLException {
		List <AdminDAO.Admin>		rc;
		List <Map <String, Object>>	rq;
		Map <String, Object>		row;

		rc = new ArrayList <> ();
		try (DBase.With with = dbase.with ()) {
			rq = dbase.query (with.cursor (),
					  "SELECT adt.admin_id, adt.email, adt.firstname, adt.fullname, adt.admin_lang, ag.shortname " +
					  "FROM admin_tbl adt " +
					  "     INNER JOIN admin_to_group_tbl atg ON atg.admin_id = adt.admin_id " + 
					  "     INNER JOIN admin_group_tbl ag ON ag.admin_group_id = atg.admin_group_id " +
					  "WHERE adt.company_id = :companyID AND (ag.deleted IS NULL OR ag.deleted = 0) " +
					  "ORDER BY adt.admin_id ",
					  "companyID", companyID);
			if (rq != null) {
				AdminDAO.Admin	cur = null;
				
				for (int n = 0; n < rq.size (); ++n) {
					row = rq.get (n);
					long	adminID = dbase.asLong (row.get ("admin_id"));
					String	email = dbase.asString (row.get ("email"));
					String	firstname = dbase.asString (row.get ("firstname"));
					String	lastname = dbase.asString (row.get ("fullname"));
					String	language = dbase.asString (row.get ("admin_lang"));
					String	role = dbase.asString (row.get ("shortname"));
					
					if (role != null) {
						if ((cur == null) || (cur.adminID () != adminID)) {
							cur = new AdminDAO.Admin (adminID, email, firstname, lastname, language);
							rc.add (cur);
						}
						cur.addRole (role);
					}
				}
			}
		}
		return rc;
	}
}
