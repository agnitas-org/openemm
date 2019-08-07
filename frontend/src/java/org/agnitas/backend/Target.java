/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Keeps track of the relevant data of one element
 * from dyn_target_tbl
 */
public class Target {
	private long	id;
	private String	sql;
	private boolean	databaseOnly;
	private boolean needEvaluation;
	private boolean	isEvaluated;
	private boolean	evaluatedValue;

	/**
	 * Constructor
	 * 
	 * @param nId  the dyn_target_tbl.target_id value
	 * @param nSql the dyn_target_tbl.target_sql value
	 */
	public Target (long nId, String nSql, boolean nDatabaseOnly) {
		id = nId;
		sql = nSql;
		databaseOnly = nDatabaseOnly;
	}
	
	public long getID() {
		return id;
	}
	
	public String getSQL (boolean forDatabaseAccess) {
		return forDatabaseAccess || (! databaseOnly) ? sql : null;
	}
	
	public boolean databaseOnly () {
		return databaseOnly;
	}

	/**
	 * Checks if this target expression is valid (very simple check,
	 * it is only checked, if the SQL is not empty)
	 * 
	 * @return true, if the sql is considered as valid, false otherwise
	 */
	public boolean valid () {
		return sql != null && sql.length () > 0;
	}

	static private Pattern	cleaner = Pattern.compile ("'[^']*'");
	static private Pattern	checker = Pattern.compile ("[a-z][a-z0-9_]*\\.[a-z][a-z0-9_]*", Pattern.CASE_INSENSITIVE);
	/**
	 * Scan the SQL expression for referenced profile or reference columns
	 * to retrieve them from the database so the xmlback has access to them
	 * to validate the sql expression off database
	 * 
	 * @param use the set to add all found columns to
	 */
	public void requestFields (Set <String> use) {
		if ((sql != null) && (! databaseOnly)) {
			String[]	parts = cleaner.split (sql);
			
			for (int n = 0; n <parts.length; ++n) {
				Matcher	m = checker.matcher (parts[n]);
		
				while (m.find ()) {
					String	c = m.group ().toLowerCase ();
				
					if (c.startsWith ("cust.")) {
						c = c.substring (5);
					}
					use.add (c);
				}
			}
		}
	}

	/**
	 * If the target is used in xmlback, this must be called to mark it for
	 * pre evaluation
	 */
	public void setNeedEvaluation () {
		needEvaluation = true;
	}
	
	/**
	 * Returns if the target is marked for pre evaluation
	 */
	public boolean needEvaluation () {
		return needEvaluation;
	}

	/**
	 * returns true, if an evaluation of this target group already has taken
	 * place
	 * 
	 * @return true, if evaluated, false otherwise
	 */
	public boolean hasEvaluatedValue () {
		return isEvaluated;
	}
	
	/**
	 * return the evaluated value of this target group
	 * 
	 * @return true, if target group matches, false otherwise
	 */
	public boolean evaluatedValue () {
		return evaluatedValue;
	}

	/**
	 * Set the pre evalutated value for this target group
	 * 
	 * @param value true, if this target group matches, false otherwise
	 */
	public void setEvaluateValue (boolean value) {
		isEvaluated = true;
		evaluatedValue = value;
	}
	
	/**
	 * Clear the state of being already evaluated
	 */
	public void clearEvaluateValue () {
		isEvaluated = false;
		evaluatedValue = false;
	}
}
