/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import	java.util.HashMap;
import	java.util.List;
import	java.util.Map;

/**
 * This class provides a simple templating engine which
 * supports a basic set of known variables during instanciation
 * and an optional further bunch of variables during
 * replacement operation
 */
public class Substitute {
	/** basic information for string substutition */
	private Map <String, String>	basic = null;

	/**
	 * Constructor, accepts a list of pairs of variables/values to
	 * inititalze the basic known variables
	 * 
	 * @param config a list of pairs of known variables
	 */
	public Substitute (Object ... config) {
		basic = new HashMap <> ();
		for (int n = 0; n + 1 < config.length; n += 2) {
			put (config[n], config[n + 1]);
		}
	}
	
	/**
	 * Extend the list of known variables by given parameter.
	 * Replaces a null value with the empty string.
	 * 
	 * @param variable the variable to assign the value to
	 * @param value    its value
	 */
	public void put (Object variable, Object value) {
		basic.put (variable.toString (), value != null ? value.toString () : "");
	}
	
	/**
	 * replace all placeholder in input string with the values
	 * of the variables, if found. If a placeholder could not
	 * be resolved, the name is added to the optional missing
	 * paramter.
	 * 
	 * @param str     the input string to replace the placeholder
	 * @param extra   an optional extra bunch of known variables
	 * @param missing an optional list which will be filled with unresolved variable names
	 * @return        the newly generated string
	 */
	public String replace (String str, Map <String, String> extra, List <String> missing) {
		if (str == null) {
			return str;
		}

		int	pos, start, end, opos, npos, maxlen, bracketCount;
		String	name, pre, replace, post;
		boolean	optional;
		String	prefix, postfix, alt;

		pos = 0;
		while ((start = str.indexOf ("%(", pos)) != -1) {
			end = -1;
			bracketCount = 1;
			for (int n = start + 2; (end == -1) && (n < str.length ()); ++n) {
				char	ch = str.charAt (n);

				if (ch == '(') {
					++bracketCount;
				} else if (ch == ')') {
					--bracketCount;
					if (bracketCount == 0) {
						end = n;
					}
				}
			}
			if (end == -1) {
				end = str.length ();
			}
			name = str.substring (start + 2, end);
			if (((opos = name.indexOf ('?')) != -1) && ((npos = name.indexOf ('?', opos + 1)) != -1)) {
				optional = true;
				prefix = name.substring (0, opos);
				postfix = name.substring (npos + 1);
				name = name.substring (opos + 1, npos);
				if ((npos = postfix.indexOf ('?')) != -1) {
					alt = postfix.substring (npos + 1);
					postfix = postfix.substring (0, npos);
				} else {
					alt = null;
				}
			} else {
				optional = false;
				prefix = null;
				postfix = null;
				alt = null;
			}
			maxlen = -1;
			if ((opos = name.indexOf (':')) > 0) {
				try {
					maxlen = Integer.parseInt (name.substring (opos + 1));
				} catch (NumberFormatException e) {
					// do nothing
				}
				name = name.substring (0, opos);
			}
			if ((extra != null) && extra.containsKey (name)) {
				replace = extra.get (name);
			} else {
				replace = basic.get (name);
			}
			if ((replace == null) && (! optional) && (missing != null)) {
				missing.add (name);
			}
			if (optional) {
				if (replace == null) {
					replace = replace (alt, extra, missing);
				} else {
					replace = prefix + replace + postfix;
				}
			}
			if ((maxlen >= 0) && (replace != null) && (maxlen < replace.length ())) {
				replace = replace.substring (0, maxlen);
			}
			pre = (start > 0 ? str.substring (0, start) : "");
			post = (end + 1 < str.length () ? str.substring (end + 1) : "");
			pos = start + (replace == null ? 0 : replace.length ());
			str = pre + (replace ==null ? "" : replace) + post;
		}
		return str;
	}

	public String replace (String str, Map <String, String> extra) {
		return replace (str, extra, null);
	}
	
	public String replace (String str) {
		return replace (str, null, null);
	}
}
