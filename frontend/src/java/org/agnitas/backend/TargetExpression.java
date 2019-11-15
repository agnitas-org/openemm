/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package	org.agnitas.backend;

import	java.sql.SQLException;
import	java.util.ArrayList;
import	java.util.HashMap;
import	java.util.HashSet;
import	java.util.List;
import	java.util.Map;
import	java.util.Set;

import	org.agnitas.util.Config;
import	org.agnitas.util.Log;

public class TargetExpression {
	/** refrence to global configuration				*/
	private Data			data;
	/** for subselect, the target expression of the mailing itself	*/
	public String			expression;
	/** id for list splits						*/
	private long			splitID;
	/** the computed subselection for the receiver of this mailing	*/
	private String			subselect;
	/** keep track of collected targets				*/
	private Map <Long, Target>	targets;
	/**
	 * collect a list of targets that must be resolved using 
	 * the database
	 */
	private List <Target>		resolveByDatabase;
	
	public TargetExpression (Data nData) {
		data = nData;
	}
	public void expression (String nExpression) {
		expression = nExpression;
	}
	public void splitID (long nSplitID) {
		splitID = nSplitID;
	}
	public String subselect () {
		return subselect;
	}

	/**
	 * Clear all set target relevant values
	 */
	public void clear () {
		expression = null;
		splitID = 0;
		subselect = null;
	}
	
	/**
	 * Cleanup open resources etc.
	 */
	public TargetExpression done () {
		return null;
	}

	/**
	 * Write all target expression related settings to logfile
	 */
	public void logSettings () {
		data.logging (Log.DEBUG, "init", "\ttargetExpression.expression = " + (expression == null ? "*not set*" : expression));
		data.logging (Log.DEBUG, "init", "\ttargetExpression.splitID = " + splitID);
		data.logging (Log.DEBUG, "init", "\ttargetExpression.subselect = " + (subselect == null ? "*not set*" : subselect));
	}

	/**
	 * Configure from external resource
	 * 
	 * @param cfg the configuration
	 */
	public void configure (Config cfg) {
	}

	/**
	 * Retrieves all target expression realted information from available resources
	 */
	public void retrieveInformation () throws Exception {
		String		combinedExpression = expression;
		
		subselect = null;
		if (splitID > 0) {
			if (! data.shouldRemoveDuplicateEMails ()) {
				if (expression == null) {
					combinedExpression = Long.toString (splitID);
				} else {
					combinedExpression = "(" + expression + ") & " + Long.toString (splitID);
				}
			} else {
				getTarget (splitID, false);
			}
		}
		if (combinedExpression != null) {
			StringBuffer	buf = new StringBuffer ();
			int		elen = combinedExpression.length ();

			for (int n = 0; n < elen; ++n) {
				char	ch = combinedExpression.charAt (n);

				if ((ch == '(') || (ch == ')')) {
					buf.append (ch);
				} else if ((ch == '&') || (ch == '|')) {
					if (ch == '&')
						buf.append (" AND");
					else
						buf.append (" OR");
					while (((n + 1) < elen) && (combinedExpression.charAt (n + 1) == ch))
						++n;
				} else if (ch == '!') {
					buf.append (" NOT");
				} else if ("0123456789".indexOf (ch) != -1) {
					int	newn = n;
					long	tid = 0;
					int	pos;
					Target	temp;

					while ((n < elen) && ((pos = "0123456789".indexOf (ch)) != -1)) {
						newn = n;
						tid *= 10;
						tid += pos;
						++n;
						if (n < elen)
							ch = combinedExpression.charAt (n);
						else
							ch = '\0';
					}
					n = newn;
					temp = getTarget (tid, false);
					if ((temp != null) && temp.valid ())
						buf.append (" (" + temp.getSQL (true) + ")");
				}
			}
			if (buf.length () >= 3)
				subselect = buf.toString ();
		}
	}

	/*
	 * handle a missing target expression, give up, if the mailing type
	 * requires one
	 */
	public void handleMissingTargetExpression () throws Exception {
		if (expression == null) {
			if (data.maildropStatus.isRuleMailing ()) {
				try {
					data.maildropStatus.removeEntry ();
					data.logging (Log.INFO, "init", "Removed entry from maildrop status table due to missing target expression");
				} catch (SQLException e) {
					data.logging (Log.ERROR, "init", "Failed to disable rule based mailing: " + e.toString (), e);
				}
				throw new Exception ("Missing target: Rule based mailing generation aborted and disabled");
			} else if (data.maildropStatus.isOnDemandMailing ()) {
				try {
					data.maildropStatus.setGenerationStatus (0, 4);
				} catch (Exception e) {
					data.logging (Log.ERROR, "init", "Failed to set genreation status: " + e.toString (), e);
				}
				throw new Exception ("Missing target: On Demand mailing generation aborted and left in undefined condition");
			}
		}
	}

	/**
	 * Scan the SQL expression of all targets for referenced profile or
	 * reference columns to retrieve them from the database so the
	 * xmlback has access to them to validate the sql expression off
	 * database
	 * 
	 * @param use the set to add all found columns to
	 */
	public void requestFields (Set <String> predef) {
		if (targets != null) {
			for (Target t : targets.values ()) {
				t.requestFields (predef);
			}
		}
	}

	/**
	 * Get a target representation
	 * 
	 * @param tid               the dyn_target_tbl.target_di
	 * @param requireEvaluation if this target should be pre evaluated 
	 * @return                  an instance for the retrieved target
	 * @throws Exception
	 */
	public Target getTarget (long tid, boolean requireEvaluation) throws Exception {
		if (targets == null) {
			targets = new HashMap <> ();
		}

		Target	rc = targets.get (tid);
		String	reason = "unspecified";

		if (rc == null) {
			String		sql = null;
			boolean		databaseOnly = false;

			try {
				Map <String, Object>	row = data.dbase.querys (
					"SELECT target_sql, component_hide, eql, deleted, invalid "+
					"FROM dyn_target_tbl "+ 
					"WHERE target_id = :targetID",
					"targetID", tid
				);
				
				if (row != null) {
					int	deleted = data.dbase.asInt (row.get ("deleted"));
					int	invalid = data.dbase.asInt (row.get ("invalid"));
					
					if (deleted != 0) {
						data.logging (Log.ERROR, "targets", "TargetID " + tid + " is marked as deleted");
						sql = null;
						reason = "deleted";
					} else if (invalid > 0) {
						data.logging (Log.ERROR, "targets", "TargetID " + tid + " is marked as invalid");
						sql = null;
						reason = "invalid";
					} else {
						sql = data.dbase.asString (row.get ("target_sql"), 3);
						if (sql == null) {
							reason = "empty";
						} else if (sql.trim ().equals ("1=0")) {
							data.logging (Log.ERROR, "target", "TargetID " + tid + " has invalid SQL expression \"" + sql + "\"");
							sql = null;
							reason = "invalid sql";
						}
						databaseOnly = (data.dbase.asInt (row.get ("component_hide")) == 1) && (data.dbase.asString (row.get ("eql")) != null);
					}
				} else {
					data.logging (Log.ERROR, "targets", "No target with ID " + tid + " found in dyn_target_tbl");
					sql = null;
					reason = "non existing";
				}
			} catch (SQLException e) {
				data.logging (Log.ERROR, "targets", "Failed to query for target ID " + tid + " in dyn_target_tbl: " + e.toString (), e);
				sql = null;
				reason = "failure";
			}
			rc = new Target (tid, sql, databaseOnly);
			targets.put (tid, rc);
		}
		if (! rc.valid ()) {
			throw new Exception ("TargetID " + tid + ": " + reason + " target found");
		}
		if (requireEvaluation) {
			rc.setNeedEvaluation ();
		}
		return rc;
	}

	/*
	 * Get the sql fragment for a split list expression
	 * 
	 * @return the sql fragment
	 */
	public String getSplitExpression () {
		if (splitID > 0) {
			try {
				Target	se = getTarget (splitID, false);
			
				if (se != null) {
					return se.getSQL (true);
				}
			} catch (Exception e) {
				data.logging (Log.ERROR, "targets", "Failed to retrieve already requested target for list split for id " + splitID);
			}
		}
		return null;
	}
	
	/**
	 * return a list of target groups that must be resolved
	 * by accessing the database and cannot resolved in
	 * later processing
	 * 
	 * @return the list of targets
	 */
	public List <Target> resolveByDatabase () {
		if (resolveByDatabase == null) {
			resolveByDatabase = new ArrayList <> ();
			if (targets != null) {
				targets
					.values ()
					.stream ()
					.filter ((t) -> t.databaseOnly () && t.needEvaluation ())
					.forEach ((t) -> resolveByDatabase.add (t));
			}
		}
		return resolveByDatabase;
	}

	public void setEvaluatedValues (String targetInformation) {
		Set <Long>	targetIDs = new HashSet <> ();
		String[]	targets = targetInformation.split (", *");
		
		for (int n = 0; n < targets.length; ++n) {
			if (! targets[n].equals ("")) {
				try {
					long	targetID = Long.parseLong (targets[n]);
			
					if (targetID > 0) {
						targetIDs.add (targetID);
					}
				} catch (Exception e) {
					data.logging (Log.INFO, "_tg", "Got unparsable input: " + targetInformation);
				}
			}
		}
		resolveByDatabase ().stream ().forEach (t -> t.setEvaluateValue (targetIDs.contains (t.getID ())));
	}
	
	public void clearEvaluatedValues () {
		resolveByDatabase ().stream ().forEach (t -> t.clearEvaluateValue ());
	}
}
