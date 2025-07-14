/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
//
// One should not access runtime directly, I know :-(

/**
 * This class provides the mechanism to use a VelocityScript in title
 * configuration to be used by the agnTITLE tag family.
 */
public class CustomTitle {
	/** The magic character sequence to recognize a custom title */
	public static final String	  SCRIPT_ID = "#<>#";
	/** The provided titleText itself, e.g. the velocity script */
	private String			titleText;

	/**
	 * Constructor for a custom title
	 * 
	 * The provied title text is saved, removing the optional magic SCRIPT_ID
	 * @param nTitleText the velocity script optional prefixed by SCRIPT_ID
	 */
	public CustomTitle (String nTitleText) {
		titleText = cleanup (nTitleText.startsWith (SCRIPT_ID) ? nTitleText.substring (SCRIPT_ID.length ()) : nTitleText);
	}

	private static Pattern	searcher = Pattern.compile ("cust\\.([a-z_][a-z0-9_]*)");
	/**
	 * Add all found references to a database column from the velocity
	 * script into the passed Set.
	 * 
	 * @param predef the set to store each found database column
	 */
	public void requestFields (Set <String> predef) {
		Matcher m = searcher.matcher (titleText);
		int pos = 0;

		while (m.find (pos)) {
			predef.add (titleText.substring (m.start (1), m.end (1)));
			pos = m.end ();
		}
	}

	/**
	 * Runs the velocity engine on the script with the provided
	 * content for one recipient.
	 * 
	 * @param titleType one of the valid constants TITLE_* from com.agnitas.util.Title
	 * @param gender    the gender of the recipient
	 * @param title     his title
	 * @param firstname his firstname
	 * @param lastname  his lastname
	 * @param columns   and all other known database columns for this recipient
	 * @param error     optional Buffer to collect error message
	 * @return          the output of the velocity script, i.e. the generated title for this recipient
	 */
	public String makeTitle (int titleType, int gender, String title, String firstname, String lastname, Map <String, String> columns, StringBuffer error) {
		String	rc = null;
		
		try {
			VelocityEngine	ve = new VelocityEngine ();
			VelocityContext	vc = new VelocityContext ();
		
//			ve.setProperty (VelocityEngine.RUNTIME_LOG_LOGSYSTEM, this);
			ve.setProperty (VelocityEngine.VM_LIBRARY, "");
			ve.init ();

			StringWriter	out = new StringWriter ();

			vc.put ("type", titleType);
			vc.put ("gender", gender);
			vc.put ("title", (title == null || titleType == Title.TITLE_FIRST ? "" : title));
			vc.put ("firstname", (firstname == null || titleType == Title.TITLE_DEFAULT ? "" : firstname));
			vc.put ("lastname", (lastname == null || titleType == Title.TITLE_FIRST ? "" : lastname));
			vc.put ("cust", columns);
			ve.evaluate (vc, out, "title", titleText);
			rc = shrink (out.toString ());
		} catch (Exception e) {
			if (error != null) {
				error.append ("CustomTitle: \"" + titleText + "\" leads to " + e.toString () + "\n");
			}
		}

		return rc;
	}

	private static Pattern	cleaner = Pattern.compile ("#(include|parse)(\\([^)]*\\))?");
	private String cleanup (String template) {
		Matcher m = cleaner.matcher (template);

		return m.replaceAll ("");
	}

	private static Pattern	shrinker = Pattern.compile ("[ \t\n\r]+");
	private String shrink (String output) {
		Matcher m = shrinker.matcher (output);

		return m.replaceAll (" ").trim ();
	}
}
