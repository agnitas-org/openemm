/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the collection of blacklisted emails and
 * email pattern.
 */
public class Blacklist {
	/** used to detect and avoud double tnries */
	private Set <String>		seen;
	/** contains all non wildcard entries */
	private Map <String, Blackdata>	exact;
	/** contains a list of all wildcard records */
	private List <Blackdata>	wildcards;
	/** number of entries from global blacklist */
	private int			globalCount;
	/** number of entries from local blacklist */
	private int			localCount;
	/** number of entries in wildcards */
	private int			wcount;
	/** path to bouncelog file */
	private String			bouncelog;
	/** optional logger */
	private Log			log;

	/** Constructor for the class
	 * 
	 * @param nLog a optional instance of Log for logging
	 */
	public Blacklist (Log nLog) {
		seen = new HashSet <> ();
		exact = new HashMap <> ();
		wildcards = new ArrayList <> ();
		localCount = 0;
		globalCount = 0;
		wcount = 0;
		bouncelog = Str.makePath ("$home",  "log", "extbounce.log");
		log = nLog;
	}
	public Blacklist () {
		this (null);
	}

	/** sets the path to the bouncelog file
	 * @param nBouncelog the path to the file
	 */
	public void setBouncelog (String nBouncelog) {
		bouncelog = nBouncelog;
	}

	/** add a email or pattern to the blacklist
	 * @param email the email or pattern
	 * @param global true, if this entry is on the global blacklist
	 */
	public void add (String email, boolean global) {
		if (! seen.contains (email)) {
			seen.add (email);

			Blackdata	bd = new Blackdata (email, global);

			if (bd.isWildcard ()) {
				wildcards.add (bd);
				++wcount;
			} else {
				exact.put (bd.getEmail (), bd);
			}
			if (global) {
				++globalCount;
			} else {
				++localCount;
			}
		}
	}

	/** Returns wether an email is on the blacklist or not
	 * @param email the email to check
	 * @return the entry, if the email is blacklisted, null otherwise
	 */
	public Blackdata isBlackListed (String email) {
		Blackdata rc = null;

		email = email.toLowerCase ().trim ();
		rc = exact.get (email);
		for (int n = 0; (rc == null) && (n < wcount); ++n) {
			Blackdata	e = wildcards.get (n);

			if (e.matches (email)) {
				rc = e;
			}
		}
		return rc;
	}

	/** returns the number of entries on the global blacklist
	 * @return count
	 */
	public int globalCount () {
		return globalCount;
	}

	/** returns the number of entries on the local blacklist
	 * @return count
	 */
	public int localCount () {
		return localCount;
	}

	/** Write blacklisted entry to bounce log file
	 * @param mailingID the mailingID
	 * @param customerID the customerID to mark as blacklisted
	 */
	public void writeBounce (long mailingID, long customerID) {
		if (bouncelog != null) {
			String	entry = "5.9.9;0;" + mailingID + ";0;" + customerID + ";admin=auto opt-out due to blacklist\tstatus=blacklist\n";
			try (FileOutputStream file = new FileOutputStream (bouncelog, true)) {
				file.write (entry.getBytes ("ISO-8859-1"));
			} catch (Exception e) {
				if (log != null) {
					log.out (Log.ERROR, "blacklist", "Failed to write \"" + entry + "\" to " + bouncelog + ": " + e.toString ());
				}
			}
		}
	}
}
