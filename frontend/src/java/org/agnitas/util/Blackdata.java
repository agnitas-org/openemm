/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

/**
 * This class holds one email or pattern for blacklist
 * checking
 */
public class Blackdata {
	/** the email or the email pattern */
	private String	  email;
	/** true, if the entry is part on the global blacklist */
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
	 * @param nGlobal sets the source for this entry, true it comes from a global blacklist, otherwise from a local one
	 */
	public Blackdata (String nEmail, boolean nGlobal) {
		email = nEmail != null ? nEmail.toLowerCase ().trim () : null;
		global = nGlobal;
		iswildcard = (email != null) && ((email.indexOf ('_') != -1) || (email.indexOf ('%') != -1));
	}

	/**
	 * Matches the given string against email.
	 *
	 * @param check the email to check
	 * @return       true, if check matches email
	 */
	public boolean matches (String check) {
		if (! iswildcard) {
			return email.equals (check);
		}
		return sqllike (email, 0, email.length (),
				check, 0, check.length ());
	}

	/** returns a static string where this email comes from.
	 * 
	 * If the email came from a global blacklist, the string
	 * "global" is returned, "local" otherwise. This is used
	 * for logging the source if a blacklisted address is
	 * encountered.
	 * 
	 * @returns "global", if email is on a global blacklist else "local"
	 */
	public String where () {
		if (global) {
			return "global";
		}
		return "local";
	}

	/** returns the pure email address
	 *
	 * @return email
	 */
	protected String getEmail () {
		return email;
	}
	/** returns the source
	 *
	 * @return true, if entry is on global blacklist
	 */
	protected boolean isGlobal () {
		return global;
	}
	/** returns if the entry is a wildcard expression
	 *
	 * @return true, if its a wildcard
	 */
	protected boolean isWildcard () {
		return iswildcard;
	}
	
	/* compares a string against a SQL wildcard pattern (recrusive) */
	private boolean sqllike (String mask, int mpos, int mlen, String str, int spos, int slen) {
		char	cur;

		while ((mpos < mlen) && (spos < slen)) {
			cur = mask.charAt (mpos++);
			if (cur == '_') {
				spos++;
			} else if (cur == '%') {
				while ((mpos < mlen) && (mask.charAt (mpos) == '%')) {
					mpos++;
				}
				if (mpos == mlen) {
					return true;
				}
				while (spos < slen) {
					if (sqllike (mask, mpos, mlen, str, spos, slen)) {
						return true;
					} else {
						++spos;
					}
				}
			} else {
				if ((cur == '\\') && (mpos < mlen)) {
					cur = mask.charAt (mpos++);
				}
				if (cur != str.charAt (spos)) {
					return false;
				}
				spos++;
			}
		}
		if ((spos == slen) && (mpos < mlen)) {
			while ((mpos < mlen) && (mask.charAt (mpos) == '%')) {
				++mpos;
			}
		}
		return (mpos == mlen) && (spos == slen);
	}
}
