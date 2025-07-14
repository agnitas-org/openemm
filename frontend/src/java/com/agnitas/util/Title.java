/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class holds all relevant variants for every known gender
 * for one title. It uses either the hard coded rules to create
 * the known variants for titles or using a velocity script
 * based {@link CustomTitle} to create titles not supported
 * by the internal logic.
 */
public class Title {
	static class Entry {
		String		title;
		CustomTitle	custom;
		protected Entry (String t) {
			if (t.startsWith (CustomTitle.SCRIPT_ID)) {
				title = null;
				custom = new CustomTitle (t);
			} else {
				title = t;
				custom = null;
			}
		}
	}

	public final static int TITLE_ALL = -1;
	public final static int TITLE_DEFAULT = 0;
	public final static int TITLE_FULL = 1;
	public final static int TITLE_FIRST = 2;
	public final static int TITLE_MAX = 3;
	/** the unique ID of this title */
	protected Long		id;
	/** The titles for each gender */
	private List <Entry>	titles;

	/* Constructor
	 * @param nID new unique id
	 */
	public Title (Long nID) {
		id = nID;
		titles = new ArrayList <> ();
	}

	/* Constructor without id, use null in this case */
	public Title () {
		this (null);
	}

	/** Set/Add a title for a gender
	 * @param gender numeric representation for the gender
	 * @param title title for this gender
	 */
	public void setTitle (int gender, String title) {
		if (gender >= 0) {
			if ((title != null) && title.endsWith (" ")) {
				title = title.substring (0, title.length () - 1);
			}

			int size = titles.size ();

			if (size <= gender) {
				while (size < gender) {
					titles.add (size, null);
					++size;
				}
				titles.add (gender, title == null ? null : new Entry (title));
			} else {
				titles.set (gender, title == null ? null : new Entry (title));
			}
		}
	}

	/** Set required database fields
	 * @param datap context
	 * @param predef collection of required db names
	 * @param ttype title type
	 */
	public void requestFields (Set <String> predef, int ttype) {
		predef.add ("gender");
		if ((ttype == TITLE_ALL) || (ttype == TITLE_FIRST) || (ttype == TITLE_FULL)) {
			predef.add ("firstname");
		}
		if ((ttype == TITLE_ALL) || (ttype == TITLE_DEFAULT) || (ttype == TITLE_FULL)) {
			predef.add ("title");
			predef.add ("lastname");
		}
		for (int n = 0; n < titles.size (); ++n) {
			Entry	e = titles.get (n);

			if ((e != null) && (e.custom != null)) {
				e.custom.requestFields (predef);
			}
		}
	}

	/** Check for valid input strings
	 * @param s the input string
	 * @return true if string is not empty, false otherwise
	 */
	private boolean isValid (String s) {
		return (s != null) && (s.length () > 0);
	}

	/** Create the title string using customer related data
	 * @param titleType type of title to create
	 * @param gender the gender from the db
	 * @param title the title from the db
	 * @param firstname you guess it
	 * @param lastname again
	 * @param columns other required columns from the database
	 * @param error optional buffer to put error messages to
	 * @return the title string
	 */
	public String makeTitle (int titleType, int gender, String title, String firstname, String lastname, Map <String, String> columns, StringBuffer error) {
		String	s = "";
		Entry	e;
		String	name = null;

		if ((gender < 0) || (gender >= titles.size ())) {
			gender = 2;
		}
		if (gender < titles.size ()) {
			e = titles.get (gender);
		} else {
			e = null;
		}
		if ((e == null) && (gender != 2) && (2 < titles.size ())) {
			gender = 2;
			e = titles.get (gender);
		}
		if (e != null) {
			if (e.custom == null) {
				if (gender != 2) {
					switch (titleType) {
						case TITLE_DEFAULT:
						case TITLE_FULL:
							String	custtitle = "";
	
							if (isValid (title)) {
								custtitle = title + " ";
							}
							if (titleType == TITLE_FULL) {
								if (isValid (firstname)) {
									if (isValid (lastname)) {
										name = firstname + " " + lastname;
									}
								} else if (isValid (lastname)) {
									name = lastname;
								}
								if (name != null) {
									name = custtitle + name;
								}
							} else if (isValid (lastname)) {
								name = custtitle + lastname;
							}
							break;
						case TITLE_FIRST:
							if (isValid (firstname)) {
								name = firstname;
							}
							break;
						default:
							break;
					}
					if (name == null) {
						gender = 2;
						if (gender < titles.size ()) {
							e = titles.get (gender);
						} else {
							e = null;
						}
					}
				}
			}
			if (e != null) {
				if (e.custom == null) {
					if (e.title != null) {
						if (name != null) {
							s = e.title + " " + name;
						} else {
							s = e.title;
						}
					}
				} else {
					s = e.custom.makeTitle (titleType, gender, title, firstname, lastname, columns, error);
					if (s == null) {
						s = "";
					}
				}
			}
		}
		return s;
	}

	/** Create all title strings using customer related data
	 * Beware, use this only for single cases as this is slow
	 * @param columns all required columns from the database
	 * @param error optional buffer to put error messages to
	 * @return all title strings
	 */
	public String[][] makeTitles (Map <String, String> columns) {
		int		genders = titles.size () < 3 ? 2 : titles.size () - 1;
		String[][]	rc = new String[genders + 1][TITLE_MAX];
		String		title, firstname, lastname;
		StringBuffer	error = new StringBuffer ();
		Map <String, String>
				lcColumns = new HashMap <> (columns.size ());

		for (Map.Entry <String, String> kv : columns.entrySet ()) {
			lcColumns.put (kv.getKey ().toLowerCase (), kv.getValue ());
		}

		title = lcColumns.get ("title");
		firstname = lcColumns.get ("firstname");
		lastname = lcColumns.get ("lastname");
		for (int gender = 0; gender <= genders; ++gender) {
			for (int n = 0; n < TITLE_MAX; ++n) {
				rc[gender][n] = makeTitle (n, gender, title, firstname, lastname, lcColumns, error);
				if (error.length () > 0) {
					error.insert (0, "**: ");
					rc[gender][n] = error.toString ();
					error.setLength (0);
				}
			}
		}
		return rc;
	}
}
