/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agnitas.backend.DBase;
import com.agnitas.backend.EMail;
import com.agnitas.util.DNS;
import com.agnitas.util.Log;
import com.agnitas.util.Str;

/**
 * Accesses all information for DKIM generation
 */
public class DkimDAO {
	static class Entry {
		private boolean	exists;
		private long timestamp;
		public Entry (boolean nExists, long nTimestamp) {
			exists = nExists;
			timestamp = nTimestamp;
		}
		public boolean exists () {
			return exists;
		}
	}

	private static Map <String, Entry> dkimReportCache = new HashMap <> ();

	public class DKIM {
		private long id;
		private boolean local;
		private String key;
		private String domain;
		private String selector;
		private boolean reportEnabled;
		private boolean ident;

		private DKIM(long nId, boolean nLocal, String nKey, String nDomain, String nSelector) {
			id = nId;
			local = nLocal;
			key = nKey;
			domain = nDomain;
			selector = nSelector;
			reportEnabled = false;
			ident = false;
		}

		private boolean valid() {
			return (key != null) && (domain != null) && (selector != null);
		}

		private boolean match(EMail email) {
			if ((email != null) && (email.domain != null)) {
				return domain.equalsIgnoreCase(email.domain);
			}
			return false;
		}

		private boolean matchSubdomain(EMail email) {
			if ((email != null) && (email.domain != null)) {
				if ((email.pure_puny.length() > domain.length()) && (email.pure_puny.charAt(email.pure_puny.length() - domain.length() - 1) == '.')) {
					return email.pure_puny.endsWith(domain);
				}
			}
			return false;
		}
		
		private void reportEnabled(boolean nReportEnabled) {
			reportEnabled = nReportEnabled;
		}

		private void ident(boolean nIdent) {
			ident = nIdent;
		}
		
		@Override
		public String toString () {
			return "DKIM (" + selector + "._domainkey." + domain + ")" + (ident ? "" : " (default)");
		}

		public long id() {
			return id;
		}
		
		public String key() {
			return key;
		}

		public String domain() {
			return domain;
		}

		public String selector() {
			return selector;
		}
		
		public boolean local() {
			return local;
		}

		public boolean reportEnabled() {
			return reportEnabled;
		}
		
		public boolean ident() {
			return ident;
		}
	}

	private long companyID;
	private List<DKIM> dkims;

	public DkimDAO(DBase dbase, long forCompanyID) throws SQLException {
		List<Map<String, Object>> rq;

		companyID = forCompanyID;
		dkims = new ArrayList <> ();
		try (DBase.With with = dbase.with ()) {
			rq = dbase.query (with.cursor (),
					  "SELECT dkim_id, domain, company_id, selector, domain_key FROM dkim_key_tbl " +
					  "WHERE company_id IN (0, :companyID) AND " +
					  "      ((valid_start IS NULL) OR (valid_start <= CURRENT_TIMESTAMP)) AND " +
					  "      ((valid_end IS NULL) OR (valid_end >= CURRENT_TIMESTAMP)) " +
					  "ORDER BY timestamp",
					  "companyID", companyID);
			for (int n = 0; n < rq.size (); ++n) {
				Map <String, Object>	row = rq.get (n);
				boolean			hasPlain = row.containsKey ("domain_key");
				long			dkimID = dbase.asLong (row.get ("dkim_id"));
				String			dkimKey = hasPlain ? dbase.asString (row.get ("domain_key")) : null;
				DKIM			dkim;
				
				String	domain = Str.punycodeDomain(dbase.asString(row.get("domain")));
				dkim = new DKIM(
						dkimID,
						dbase.asLong(row.get("company_id")) == companyID,
						dkimKey,
						domain,
						dbase.asString(row.get("selector"))
					       );
				if (dkim.valid()) {
					dkims.add(dkim);
				}
			}
		}
		dkimReportEnabled (dbase);
	}
	
	public List <DKIM> dkims () {
		return dkims;
	}

	public DKIM find(EMail email, boolean useLocal, boolean useGlobal) {
		DKIM local = null;
		DKIM global = null;

		for (DKIM dkim : dkims) {
			if (dkim.local() && dkim.match(email)) {
				dkim.ident(true);
				return dkim;
			}
		}
		for (DKIM dkim : dkims) {
			if (dkim.local()) {
				if (dkim.matchSubdomain(email)) {
					dkim.ident(true);
					return dkim;
				}
				if (useLocal && (local == null)) {
					local = dkim;
				}
			} else {
				if (useGlobal && (global == null)) {
					global = dkim;
				}
			}
		}

		DKIM fallback = local != null ? local : global;

		if (fallback != null) {
			fallback.ident(false);
		}
		return fallback;
	}

	private void dkimReportEnabled(DBase dbase) {
		if (!dkims.isEmpty()) synchronized (dkimReportCache) {
			DNS dns = null;
			
			for (DKIM dkim : dkims) {
				String		domain = dkim.domain ().toLowerCase ();
				DkimDAO.Entry	entry = dkimReportCache.get (domain);
				long		now = System.currentTimeMillis ();
				
				if ((entry != null) && (entry.timestamp + 30 * 60 * 1000 < now)) {
					entry = null;
				}
				if (entry == null) {
					try {
						if (dns == null) {
							dns = new DNS(2, dbase.getLogger());
						}
						String check = "_report._domainkey." + domain;
						String content = dns.queryText(check);

						if (content == null) {
							dbase.logging(Log.DEBUG, "dkim", "No report for dkim domain " + domain + " found");
						} else {
							dbase.logging(Log.DEBUG, "dkim", "For dkim domain " + domain + " we found in " + check + " this content: \"" + content + "\"");
						}
						entry = new DkimDAO.Entry (content != null, now);
						dkimReportCache.put (domain, entry);
					} catch (Exception e) {
						dbase.logging (Log.ERROR, "dkim", "Failed to query \"" + domain + "\": " + e.toString (), e);
					}
				}
				if (entry != null) {
					dkim.reportEnabled (entry.exists ());
				}
			}
		}
	}
}
