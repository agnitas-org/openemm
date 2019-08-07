/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend.dao;

import	java.sql.SQLException;
import	java.util.HashMap;
import	java.util.List;
import	java.util.Map;
import	java.util.regex.Matcher;
import	java.util.regex.Pattern;

import	org.agnitas.backend.DBase;
import	org.agnitas.backend.StringOps;

/**
 * Accesses all company relevant information from the database
 * from the tables company_tbl and company_info_tbl
 */
public class CompanyDAO {
	private long		companyID;
	private String		shortName;
	private boolean		mailTracking;
	private String		secretKey;
	private long		uidVersion;
	private String		rdirDomain;
	private String		mailloopDomain;
	private String		status;
	private String		mailsPerDay;
	private int		priorityCount;
	private Map <String, String>
				info;
	private long		companyBaseID;
		
	static private Pattern	searchBase = Pattern.compile ("from .*cust(omer_?)?([0-9]+)_(master_)?tbl", Pattern.CASE_INSENSITIVE);
	public CompanyDAO (DBase dbase, long forCompanyID) throws SQLException {
		List <Map <String, Object>>	rq;
		Map <String, Object>		row;

		try (DBase.With with = dbase.with ()) {
			row = dbase.querys (with.jdbc (),
					    "SELECT company_id, shortname, mailtracking, secret_key, enabled_uid_version, " +
					    "       rdir_domain, mailloop_domain, status, mails_per_day, priority_count " + 
					    "FROM company_tbl WHERE company_id = :companyID",
					    "companyID", forCompanyID);
			if (row != null) {
				companyID = dbase.asLong (row.get ("company_id"));
				shortName = dbase.asString (row.get ("shortname"));
				mailTracking = dbase.asInt (row.get ("mailtracking")) > 0;
				secretKey = dbase.asString (row.get ("secret_key"));
				uidVersion = dbase.asLong (row.get ("enabled_uid_version"));
				rdirDomain = dbase.asString (row.get ("rdir_domain"));
				mailloopDomain = dbase.asString (row.get ("mailloop_domain"));
				status = dbase.asString (row.get ("status"));
				mailsPerDay = dbase.asString (row.get ("mails_per_day"));
				priorityCount = dbase.asInt (row.get ("priority_count"));
				info = new HashMap <> ();
				rq = dbase.query (with.jdbc (),
						  "SELECT cname, cvalue FROM company_info_tbl " +
						  "WHERE company_id IN (0, :companyID) " + 
						  "ORDER BY company_id",
						  "companyID", companyID);
				for (int n = 0; n < rq.size (); ++n) {
					row = rq.get (n);
					String	name = dbase.asString (row.get ("cname"));
					String	value = dbase.asString (row.get ("cvalue"));
					if (name != null) {
						info.put (name, value != null ? value : "");
					}
				}
				companyBaseID = companyID;

				String	table = "customer_" + companyID + "_tbl";
				String	typ = null;
				
				if (dbase.isOracle ()) {
					rq = dbase.query (with.jdbc (),
							  "SELECT object_type FROM user_objects WHERE object_name = :tableName",
							  "tableName", table);
					for (int n = 0; n < rq.size (); ++n) {
						row = rq.get (n);
						typ = dbase.asString (row.get ("object_type"));
					}
				} else {
					if (dbase.queryInt (with.jdbc (),
							    "SELECT count(*) cnt FROM information_schema.views WHERE TABLE_SCHEMA = (select SCHEMA()) and table_name = :tableName",
							    "tableName", table) > 0) {
						typ = "VIEW";
					}
				}
				if ((typ != null) && typ.equals ("VIEW")) {
					String	query, text;

					if (dbase.isOracle ()) {
						query = "SELECT text FROM user_views WHERE view_name = :tableName";
					} else {
						query = "SELECT VIEW_DEFINITION FROM information_schema.VIEWS WHERE TABLE_SCHEMA = (select SCHEMA()) AND table_name = :tableName";
					}
					text = dbase.queryString (with.jdbc (),
								  query,
								  "tableName", table);
					if (text != null) {
						Matcher	m = searchBase.matcher (text.replaceAll ("[ \t\n\r\f]+", " "));
						
						if (m.find ()) {
							companyBaseID = StringOps.atol (m.group (2), companyBaseID);
						}
					}
				}
				if (companyBaseID != companyID) {
					row = dbase.querys (with.jdbc (),
							    "SELECT status " +
							    "FROM company_tbl " +
							    "WHERE company_id = :companyID",
							    "companyID", companyBaseID);
					if ((row == null) || (! "active".equals (dbase.asString (row.get ("status"))))) {
						companyBaseID = companyID;
					}
				}
			} else {
				companyID = 0;
			}
		}
	}
	public long companyID () {
		return companyID;
	}
	public String shortName () {
		return shortName;
	}
	public boolean mailTracking () {
		return mailTracking;
	}
	public String mailTrackingTable () {
		return "mailtrack_" + companyID + "_tbl";
	}
	public String secretKey () {
		return secretKey;
	}
	public long uidVersion () {
		return uidVersion;
	}
	public String rdirDomain () {
		return rdirDomain;
	}
	public String mailloopDomain () {
		return mailloopDomain;
	}
	public String status () {
		return status;
	}
	public String mailsPerDay () {
		return mailsPerDay;
	}
	public int priorityCount () {
		return priorityCount;
	}
	public Map <String, String> info () {
		return info;
	}
	public long companyBaseID () {
		return companyBaseID;
	}
	public CompanyDAO baseCompany (DBase dbase) throws SQLException {
		return companyBaseID == companyID ? this : new CompanyDAO (dbase, companyBaseID);
	}
}
