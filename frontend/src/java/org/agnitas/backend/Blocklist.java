/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.util.Log;
import org.agnitas.util.Str;

/**
 * This is the collection of block listed emails and
 * email pattern.
 */
public class Blocklist {
	/** used to detect and avoud double tnries */
	private Set <String>		seen;
	/** contains all non wildcard entries */
	private Map <String, Entry>	exact;
	/** contains a list of all wildcard records */
	private List <Entry>		wildcards;
	/** number of entries from global blocklist */
	private int			globalCount;
	/** number of entries from local blocklist */
	private int			localCount;
	/** number of entries in wildcards */
	private int			wcount;
	/** path to bouncelog file */
	private String			bouncelog;
	/** optional logger */
	private Log			log;
	/** allow case sensitive mailes */
	private boolean			allowUnnormalizedEmails;

	/**
	 * This class holds one email or pattern for blocklist
	 * checking
	 */
	static class Entry {
		/** the email or the email pattern */
		private String	  email;
		/** true, if the entry is part on the global blocklist */
		private boolean	  global;
		/** true, if email contains any wildcard characters */
		private boolean	  iswildcard;

		/**
		 * Constructor for the class
		 * 
		 * The email is converted to lowercase for further
		 * processing and if it contains the speical character
		 * "%" or "_" it is considered to be a wildcard pattern
		 * instead of a plain email address and wildcard pattern
		 * matching is applied instead during matching.
		 *
		 * @param nEmail the email or the pattern
		 * @param nGlobal sets the source for this entry, true it comes from a global blocklist, otherwise from a local one
		 */
		private Entry (String nEmail, boolean nGlobal) {
			email = nEmail;
			global = nGlobal;
			iswildcard = ((email.indexOf ('*') != -1) || (email.indexOf ('%') != -1));
		}

		/**
		 * Matches the given string against email.
		 *
		 * @param check the email to check
		 * @return       true, if check matches email
		 */
		private boolean matches (String check) {
			if (! iswildcard) {
				return email.equals (check);
			}
			return compare (email, 0, email.length (), check, 0, check.length ());
		}

		/** returns a static string where this email comes from.
		 * 
		 * If the email came from a global blocklist, the string
		 * "global" is returned, "local" otherwise. This is used
		 * for logging the source if a blocklisted address is
		 * encountered.
		 * 
		 * @returns "global", if email is on a global blocklist else "local"
		 */
		public String where () {
			return global ? "global" : "local";
		}

		/** returns if the entry is a wildcard expression
		 *
		 * @return true, if its a wildcard
		 */
		private  boolean isWildcard () {
			return iswildcard;
		}
	
		/* compares a string against a SQL wildcard pattern (recrusive) */
		private boolean compare (String mask, int mpos, int mlen, String str, int spos, int slen) {
			char	cur;

			while ((mpos < mlen) && (spos < slen)) {
				cur = mask.charAt (mpos++);
				if (isMatchMulti (cur)) {
					while ((mpos < mlen) && isMatchMulti (mask.charAt (mpos))) {
						mpos++;
					}
					if (mpos == mlen) {
						return true;
					}
					while (spos < slen) {
						if (compare (mask, mpos, mlen, str, spos, slen)) {
							return true;
						} else {
							++spos;
						}
					}
				} else {
					if (cur != str.charAt (spos)) {
						return false;
					}
					spos++;
				}
			}
			if ((spos == slen) && (mpos < mlen)) {
				while ((mpos < mlen) && isMatchMulti (mask.charAt (mpos))) {
					++mpos;
				}
			}
			return (mpos == mlen) && (spos == slen);
		}
	
		private boolean isMatchMulti (char ch) {
			return (ch == '%') || (ch == '*');
		}
	}

	/** Constructor for the class
	 * 
	 * @param nLog a optional instance of Log for logging
	 */
	public Blocklist (Log nLog, boolean nAllowUnnormalizedEmails) {
		seen = new HashSet <> ();
		exact = new HashMap <> ();
		wildcards = new ArrayList <> ();
		localCount = 0;
		globalCount = 0;
		wcount = 0;
		bouncelog = Str.makePath ("$home",  "log", "extbounce.log");
		log = nLog;
		allowUnnormalizedEmails = nAllowUnnormalizedEmails;
	}

	/** sets the path to the bouncelog file
	 * @param nBouncelog the path to the file
	 */
	public void setBouncelog (String nBouncelog) {
		bouncelog = nBouncelog;
	}

	/** add a email or pattern to the blocklist
	 * @param email the email or pattern
	 * @param global true, if this entry is on the global blocklist
	 */
	public void add (String email, boolean global) {
		email = Str.normalizeEMail (email, allowUnnormalizedEmails);
		if ((email != null) && (! seen.contains (email))) {
			seen.add (email);

			Entry	entry = new Entry (email, global);

			if (entry.isWildcard ()) {
				wildcards.add (entry);
				++wcount;
			} else {
				exact.put (email, entry);
			}
			if (global) {
				++globalCount;
			} else {
				++localCount;
			}
		}
	}

	/** Returns wether an email is on the blocklist or not
	 * @param email the email to check
	 * @return the entry, if the email is blocklisted, null otherwise
	 */
	public Entry isBlockListed (String email) {
		Entry rc = null;

		email = Str.normalizeEMail (email, allowUnnormalizedEmails);
		if (email != null) {
			rc = exact.get (email);
			if (rc == null) {
				for (int n = 0; n < wcount; ++n) {
					Entry	e = wildcards.get (n);

					if (e.matches (email)) {
						rc = e;
						break;
					}
				}
			}
		}
		return rc;
	}

	/** returns the number of entries on the global blocklist
	 * @return count
	 */
	public int globalCount () {
		return globalCount;
	}

	/** returns the number of entries on the local blocklist
	 * @return count
	 */
	public int localCount () {
		return localCount;
	}

	/** Write blocklisted entry to bounce log file
	 * @param mailingID the mailingID
	 * @param customerID the customerID to mark as blocklisted
	 */
	public void writeBounce (long mailingID, long customerID) {
		if (bouncelog != null) {
			String	entry = "5.9.9;0;" + mailingID + ";0;" + customerID + ";admin=auto opt-out due to blocklist\tstatus=blocklist\n";
			try (FileOutputStream file = new FileOutputStream (bouncelog, true)) {
				file.write (entry.getBytes ("UTF-8"));
			} catch (Exception e) {
				if (log != null) {
					log.out (Log.ERROR, "blocklist", "Failed to write \"" + entry + "\" to " + bouncelog + ": " + e.toString ());
				}
			}
		}
	}
}
