/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.agnitas.util.ParameterParser;

/**
 * Class to collect all information for emailing
 */
public class Media {
	/**
	 * Unspecified media type
	 */
	static public final int TYPE_UNRELATED = -1;
	/**
	 * Mediatype email
	 */
	static public final int TYPE_EMAIL = 0;
	static public final int TYPE_FAX = 1;
	static public final int TYPE_PRINT = 2;
	static public final int TYPE_SMS = 4;
	/**
	 * Entry is not used
	 */
	static public final int STAT_UNUSED = 0;
	/**
	 * Entry marked as inactive
	 */
	static public final int STAT_INACTIVE = 1;
	/**
	 * Entry marked as active
	 */
	static public final int STAT_ACTIVE = 2;
	/**
	 * parameter name for profile field
	 */
	static private final String PARAMETER_PROFILEFIELD = "profilefield";

	/**
	 * The media type itself
	 */
	public int type;
	/**
	 * Its usage priority
	 */
	public int prio;
	/**
	 * The status if active
	 */
	public int stat;
	/**
	 * Assigned parameter as found in database
	 */
	private String parm;
	/**
	 * Parsed version of parameters
	 */
	private Map<String, String> ptab;
	/**
	 * Profile field
	 */
	private String profileField;

	/**
	 * Returns the string for a media type
	 *
	 * @param t the mediatype
	 * @return its string representation
	 */
	static public String typeName(int t) {
		switch (t) {
			case TYPE_EMAIL:
				return "email";
			case TYPE_FAX:
				return "fax";
			case TYPE_PRINT:
				return "print";
			case TYPE_SMS:
				return "sms";
			default:
				return Integer.toString(t);
		}
	}

	/**
	 * Returns the string of the name of the
	 * default profile field for this media type
	 * (if applicated)
	 *
	 * @param t the mediatype
	 * @return the default profile field name
	 */
	static public String defaultProfileField(int t) {
		switch (t) {
			case TYPE_EMAIL:
				return "email";
			case TYPE_FAX:
				return "faxnumber";
			case TYPE_PRINT:
				return null;
			case TYPE_SMS:
				return "smsnumber";
			default:
				return null;
		}
	}

	/**
	 * Return the priority as string
	 *
	 * @param p the numeric priority
	 * @return its string representation
	 */
	static public String priorityName(int p) {
		return Integer.toString(p);
	}

	/**
	 * Return the status as string
	 *
	 * @param s the numeric status
	 * @return its string representation
	 */
	static public String statusName(int s) {
		switch (s) {
			case STAT_UNUSED:
				return "unused";
			case STAT_INACTIVE:
				return "inactive";
			case STAT_ACTIVE:
				return "active";
			default:
				return Integer.toString(s);
		}
	}

	/**
	 * The constructor
	 *
	 * @param mediatype the type for this entry
	 * @param priority  its priority
	 * @param status    its activity status
	 * @param parameter the parameter as found in the database
	 */
	public Media(int mediatype, int priority, int status, String parameter) {
		type = mediatype;
		prio = priority;
		stat = status;
		parm = parameter;
		ptab = parm != null ? (new ParameterParser(parm)).parse() : new HashMap<>();
		profileField = ptab.getOrDefault(PARAMETER_PROFILEFIELD, defaultProfileField(type));
	}

	/**
	 * Return own type as string
	 *
	 * @return its string representation
	 */
	public String typeName() {
		return typeName(type);
	}

	/**
	 * Return own priority as string
	 *
	 * @return its string representation
	 */
	public String priorityName() {
		return priorityName(prio);
	}

	/**
	 * Return own status as string
	 *
	 * @return its string representation
	 */
	public String statusName() {
		return statusName(stat);
	}

	public String profileField() {
		return profileField;
	}
	
	public boolean containsKey (String key) {
		return ptab.containsKey (key);
	}

	/**
	 * Get a list of all variable names
	 *
	 * @return List containing all variable names
	 */
	public List<String> getParameterVariables() {
		return new ArrayList<>(ptab.keySet());
	}

	/**
	 * Find all values to a variable
	 *
	 * @param id the name of the variable
	 * @return value of parameter
	 */
	public String findParameterValue(String id) {
		return ptab.get(id);
	}

	public String findParameterValue(String id, String dflt) {
		return ptab.getOrDefault(id, dflt);
	}

	/**
	 * Set values for a variable
	 *
	 * @param id  the name of the variable
	 * @param val the value
	 */
	public void setParameter(String id, String val) {
		ptab.put(id, val);
	}

	/**
	 * Find a parameter as string
	 *
	 * @param id   Variable to look for
	 * @param dflt default, if variable is not found
	 * @return the value, if available or the default
	 */
	public String findString(String id, String dflt) {
		return ptab.getOrDefault(id, dflt);
	}

	/**
	 * Find a parameter as interger value
	 *
	 * @param id   Variable to look for
	 * @param dflt default, if variable is not found
	 * @return the value, if available and parsable or the default
	 */
	public int findInteger(String id, int dflt) {
		String tmp = findString(id, null);

		if (tmp != null) {
			try {
				return Integer.parseInt(tmp);
			} catch (NumberFormatException e) {
				// do nothing
			}
		}
		return dflt;
	}
}
