/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import	java.util.HashSet;
import	java.util.List;
import	java.util.Map;
import	java.util.Set;

import	org.agnitas.util.Log;
import	org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Collects all permission from the database and stores them for further
 * inspection
 */
public class Permissions {
	private Set <String>	group = new HashSet<>(),
				admin = new HashSet<>(),
				company = new HashSet<>();

	/**
	 * Constructor
	 * 
	 * @param data the global configuration
	 */
	public Permissions (Data data) {
		String				query = null;
		NamedParameterJdbcTemplate	jdbc = null;
		
		try {
			List <Map <String, Object>>	rq;
			
			jdbc = data.dbase.request ();
			for (int state = 0; state < 3; ++state) {
				String		table = null;
				Set <String>	target = null;
				
				switch (state) {
				case 0:
					query = "SELECT security_token FROM admin_group_permission_tbl " +
						"WHERE admin_group_id IN (SELECT admin_group_id FROM admin_group_tbl WHERE company_id IN (0, :companyID))";
					table = "admin_group_permission_tbl";
					target = group;
					break;
				case 1:
					query = "SELECT security_token FROM admin_permission_tbl " +
						"WHERE admin_id IN (SELECT admin_id FROM admin_tbl WHERE company_id IN (0, :companyID))";
					table = "admin_permission_tbl";
					target = admin;
					break;
				case 2:
					query = "SELECT security_token FROM company_permission_tbl " +
						"WHERE company_id IN (0, :companyID)";
					table = "company_permission_tbl";
					target = company;
					break;
				}
				if ((target != null) && (table != null) && data.dbase.tableExists (table)) {
					rq = data.dbase.query (jdbc, query, "companyID", data.company.id ());
					for (int n = 0; n < rq.size (); ++n) {
						Map <String, Object>	row = rq.get (n);
						String			token = data.dbase.asString (row.get ("security_token"));
					
						if (token != null) {
							target.add (token);
						}
					}
				}
			}
		} catch (Exception e) {
			data.logging (Log.ERROR, "perm", "Failed to read permissions: " + e.toString (), e);
		} finally {
			data.dbase.release (jdbc);
		}
		data.logging (Log.DEBUG, "perm", "Found " + group.size () + " admin group, " + admin.size () + " admin and " + company.size () + " company permissions for " + data.company.id ());
	}
	
	/**
	 * Checks if a token is in any permission source available
	 * 
	 * @param token the security token to check for
	 * @return      true, if the token is found, false otherwise
	 */
	public boolean allowed (String token) {
		return group.contains (token) || admin.contains (token) || company.contains (token);
	}
}
