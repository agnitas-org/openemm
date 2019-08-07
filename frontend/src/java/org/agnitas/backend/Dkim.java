/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;

import org.agnitas.backend.dao.DkimDAO;
import org.agnitas.util.DNS;
import org.agnitas.util.Log;
import org.agnitas.util.TimeoutLRUMap;

/**
 * Read DKIM information from database to forward them to
 * xmlback for mail generation
 */
public class Dkim {
	static private TimeoutLRUMap <String, Boolean>	dkimReportCache;
	static private byte[]				secretKey;
	static {
		dkimReportCache = new TimeoutLRUMap<>(5000, 30 * 60 * 1000); // max. 5000 entries for 30 Minutes

		String	salt = 	"C'est la vie";	
		String	path = StringOps.makePath ("${home}", "lib", "dkim.key");
		File	fd = new File (path);
		
		if (fd.exists ()) {
			try (FileInputStream rd = new FileInputStream (fd)) {
				byte[]	rc = new byte[(int) fd.length ()];
				int	n = rd.read (rc);
				if (n != rc.length) {
					throw new IOException ("Incomplete read, expected " + rc.length + " bytes, got " + n);
				}
				salt = (new String (rc)).trim ();
			} catch (IOException e) {
				try {
					(new Log ("dkim-key", Log.ERROR))
						.out (Log.ERROR, "read", "Failed to read key from existing file " + path + ": " + e.toString (), e);
				} catch (Exception e2) {
					// do nothing
				}
			}
		}
		secretKey = new byte[16];
		if (salt != null) {
			try {
				byte[]	b = salt.getBytes ("UTF-8");
			
				for (int n = 0; n < secretKey.length; ++n)
					secretKey[n] = n < b.length ? b[n] : 0;
			} catch (java.io.UnsupportedEncodingException e) {
				// do nothing
			}
		}
	}

	private Data	data;
	/**
	 * Constructor
	 * 
	 * @param data the global configuration
	 */
	public Dkim (Data nData) {
		data = nData;
	}
	
	/**
	 * scan the stored DKIM keys to match the domain part
	 * of the given email and store the related information
	 * as part of the company info data.
	 * 
	 * @param email the email to lookup the dkim key for
	 * @return      true if a dkim key was found, false otherwise
	 */
	public boolean check (EMail email) throws SQLException {
		DkimDAO		dkimDAO = new DkimDAO (data.dbase, data.company.id (), secretKey);
		DkimDAO.DKIM	dkim = dkimDAO.find (email,
						     StringOps.atob (data.company.info ("dkim-local-key"), false),
						     StringOps.atob (data.company.info ("dkim-global-key"), false));
		if (dkim == null) {
			return false;
		}

		data.company.infoAdd ("_dkim_domain", dkim.domain ());
		data.company.infoAdd ("_dkim_selector", dkim.selector ());
		data.company.infoAdd ("_dkim_key", dkim.key ());
		if (dkim.ident ()) {
			data.company.infoAdd ("_dkim_ident", email.pure_puny);
		}
		String	dkimDebug = data.company.info ("dkim-debug", data.mailing.id ());
		if (dkimDebug != null) {
			data.company.infoAdd ("_dkim_z", dkimDebug);
		}
		if (dkimReport (data, dkim.domain ())) {
			data.company.infoAdd ("_dkim_report", "true");
		}
		return true;
	}

	private boolean dkimReport (Data data, String domain) {
		boolean	rc = false;
		
		synchronized (domain) {
			Boolean	use = dkimReportCache.get (domain);
			
			if (use == null) {
				DNS	dns = new DNS (2, data.getLogger ());
				String	check = "_report._domainkey." + domain;
				String	content = dns.queryText (check);
				
				if (content == null) {
					data.logging (Log.DEBUG, "dkim", "No report for dkim domain " + domain + " found");
				} else {
					data.logging (Log.DEBUG, "dkim", "For dkim domain " + domain + " we found in " + check + " this content: \"" + content + "\"");
				}
				use = content != null;
				dkimReportCache.put (domain, use);
			}
			rc = use;
		}
		return rc;
	}
}
