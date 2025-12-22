/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.agnitas.emm.core.workflow.beans.WorkflowDependencyType;
import com.agnitas.backend.DBase;
import com.agnitas.backend.Media;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.util.Log;
import com.agnitas.util.ParameterParser;

/**
 * Accesses all mailing relevant information from the database
 * from the tables mailing_tbl, mailing_mt_tbl and mailing_info_tbl
 */
public class MailingDAO {
	private long mailingID;
	private long companyID;
	private long mailinglistID;
	private long mailtemplateID;
	private boolean isTemplate;
	private boolean deleted;
	private String shortName;
	private String description;
	private Date creationDate;
	private String targetExpression;
	private long splitID;
	private long deliveryRestrictID;
	private int mailingType;
	private String workStatus;
	private Date planDate;
	private int priority;
	private String contentType;
	private String mailerset;
	private boolean frequencyCounterDisabled;
	private boolean isWorkflowMailing;
	private List<Media> media;
	private Map<String, String> info;
	private Map<String, String> item;
	private long templateID;
	private int templatePriority;

	public MailingDAO(DBase dbase, long forMailingID) throws SQLException {
		List<Map<String, Object>> rq;
		Map<String, Object> row;

		try (DBase.With with = dbase.with ()) {
			row = dbase.querys (with.cursor (),
					    "SELECT * " +
					    "FROM mailing_tbl WHERE mailing_id = :mailingID",
					    "mailingID", forMailingID);
			if (row != null) {
				//
				// basic information from mailing_tbl
				mailingID = dbase.asLong(row.get("mailing_id"));
				companyID = dbase.asLong(row.get("company_id"));
				mailinglistID = dbase.asLong(row.get("mailinglist_id"));
				mailtemplateID = dbase.asLong(row.get("mailtemplate_id"));
				isTemplate = dbase.asInt(row.get("is_template")) > 0;
				deleted = dbase.asInt(row.get("deleted")) > 0;
				shortName = dbase.asString(row.get("shortname"));
				description = dbase.asString(row.get("description"));
				creationDate = dbase.asDate(row.get("creation_date"));
				targetExpression = dbase.asString(row.get("target_expression"));
				splitID = dbase.asLong(row.get("split_id"));
				mailingType = dbase.asInt(row.get("mailing_type"));
				workStatus = dbase.asString(row.get("work_status"));
				if (row.containsKey("plan_date")) {
					planDate = dbase.asDate(row.get ("plan_date"));
				}
				priority = dbase.asInt(row.get("priority"), -1);
				contentType = dbase.asString (row.get ("content_type"));
				if (row.containsKey("mailerset")) {
					mailerset = dbase.asString(row.get ("mailerset"));
				}
				if (row.containsKey("freq_counter_disabled")) {
					frequencyCounterDisabled = dbase.asInt(row.get("freq_counter_disabled")) == 1;
				}
				if (row.containsKey ("delivery_restrict_tg_id")) {
					deliveryRestrictID = dbase.asLong (row.get ("delivery_restrict_tg_id"));
				}
				//
				// workflow related informations
				int	dependencyTypeMailing = WorkflowDependencyType.MAILING_DELIVERY.getId();
		
				row = dbase.querys (with.cursor (),
						    "SELECT COUNT(*) cnt FROM workflow_dependency_tbl WHERE type = :type AND entity_id = :mailingID",
						    "type", dependencyTypeMailing,
						    "mailingID", mailingID);
				isWorkflowMailing = row != null ? (dbase.asInt (row.get ("cnt")) > 0) : false;
				//
				// media specific information from mailing_mt_tbl
				rq = dbase.query (with.cursor (),
						  "SELECT mediatype, param, priority, status FROM mailing_mt_tbl " +
						  "WHERE mailing_id = :mailingID AND status = " + Media.STAT_ACTIVE,
						  "mailingID", mailingID);
				media = new ArrayList <> (rq.size ());
				for (int n = 0; n < rq.size (); ++n) {
					row = rq.get (n);
					media.add (
						   new Media (
							      dbase.asInt (row.get ("mediatype")),
							      dbase.asInt (row.get ("priority")),
							      dbase.asInt (row.get ("status")),
							      dbase.asString (row.get ("param"))
						   )
					);
				}
				media.sort((e1, e2) -> e1.prio - e2.prio);
				//
				// mailing specific informations from mailing_info_tbl
				info = new HashMap <> ();
				rq = dbase.query (with.cursor (),
						  "SELECT name, value FROM mailing_info_tbl " +
						  "WHERE mailing_id = :mailingID OR (mailing_id = 0 AND company_id = :companyID) " +
						  "ORDER BY mailing_id",
						  "mailingID", mailingID,
						  "companyID", companyID);
				for (int n = 0; n < rq.size (); ++n) {
					row = rq.get (n);
					String	name = dbase.asString (row.get ("name"));
					String	value = dbase.asString (row.get ("value"));
					if (name != null) {
						info.put(name, value != null ? value : "");
					}
				}
				//
				// mailing specific item definitions
				item = null;
				//
				// find source template
				Set <Long>	seen = new HashSet <> ();
				long		scanID = mailtemplateID;
		
				while ((scanID > 0L) && (! seen.contains (scanID))) {
					seen.add (scanID);
					row = dbase.querys (with.cursor (),
							    "SELECT mailtemplate_id, is_template, deleted, priority " +
							    "FROM mailing_tbl " +
							    "WHERE mailing_id = :mailingID",
							    "mailingID", scanID);
					if (row == null) {
						break;
					}
					if (dbase.asInt(row.get("is_template")) > 0) {
						if (dbase.asInt(row.get("deleted")) == 0) {
							templateID = scanID;
							templatePriority = dbase.asInt(row.get("priority"));
						}
						break;
					}
					scanID = dbase.asLong(row.get("mailtemplate_id"));
				}
			} else {
				mailingID = 0;
			}
		}
	}
	
	public void retrieveItems (DBase dbase) throws SQLException {
		try (DBase.With with = dbase.with ()) {
			List<Map<String, Object>> rq = dbase.query (with.cursor (),
								    "SELECT param FROM mailing_item_tbl WHERE mailing_id = :mailingID",
								    "mailingID", mailingID);
			for (int n = 0; n < rq.size (); ++n) {
				Map<String, Object> row = rq.get (n);
			
				String	param = dbase.asString (row.get ("param"));
				if (param != null) {
					ParameterParser parsed = new ParameterParser(param);
					if (item == null) {
						item = parsed.parse();
					} else {
						item.putAll(parsed.parse());
					}
				}
			}
		}
	}

	public long mailingID() {
		return mailingID;
	}

	public long companyID() {
		return companyID;
	}

	public long mailinglistID() {
		return mailinglistID;
	}

	public long mailtemplateID() {
		return mailtemplateID;
	}

	public boolean isTemplate() {
		return isTemplate;
	}

	public boolean deleted() {
		return deleted;
	}

	public String shortName() {
		return shortName;
	}
	
	public String description() {
		return description;
	}

	public Date creationDate() {
		return creationDate;
	}

	public String targetExpression() {
		return targetExpression;
	}

	public long splitID() {
		return splitID;
	}
	
	public long deliveryRestrictID () {
		return deliveryRestrictID;
	}

	public int mailingType() {
		return mailingType;
	}

	public String workStatus() {
		return workStatus;
	}

	public Date planDate () {
		return planDate;
	}

	public int priority() {
		return priority;
	}
	
	public String contentType () {
		return contentType;
	}
	
	public String mailerset () {
		return mailerset;
	}
	
	public boolean frequencyCounterDisabled() {
		return frequencyCounterDisabled;
	}

	public boolean isWorkflowMailing() {
		return isWorkflowMailing;
	}

	public List<Media> media() {
		return media;
	}

	public Map<String, String> info() {
		return info;
	}

	public Map<String, String> item() {
		return item;
	}

	public long sourceTemplateID() {
		return templateID;
	}

	public int sourceTemplatePriority() {
		return templatePriority;
	}

	/**
	 * set the working status for this mailing
	 */
	public boolean workStatus (DBase dbase, MailingStatus newWorkStatus, MailingStatus[] oldWorkStatuses) throws SQLException {
		int	count = 0;
		
		try (DBase.With with = dbase.with ()) {
			String	query =
			      "UPDATE mailing_tbl " +
			      "SET work_status = :workStatus " +
			      "WHERE mailing_id = :mailingID";
			if ((oldWorkStatuses != null) && (oldWorkStatuses.length > 0)) {
				query += " AND (work_status IS NULL OR work_status ";
				if (oldWorkStatuses.length == 1) {
					query += "= '" + oldWorkStatuses[0].getDbKey () + "'";
				} else {
					query += "IN (" + Stream.of (oldWorkStatuses).map (ws -> "'" + ws.getDbKey () + "'").reduce ((s, e) -> s + ", " + e).orElse (null) + ")";
				}
				query += ")";
			}
			count = dbase.update (with.cursor (), query, "mailingID", mailingID, "workStatus", newWorkStatus.getDbKey ());
			if (count > 0) {
				workStatus = newWorkStatus.getDbKey ();
			} else {
				Map<String, Object> row = dbase.querys (with.cursor (),
									"SELECT work_status " + 
									"FROM mailing_tbl " +
									"WHERE mailing_id = :mailingID",
									"mailingID", mailingID);
				String reason;
				
				if (row == null) {
					reason = "mailing not existing";
				} else {
					workStatus = dbase.asString (row.get ("work_status"));
					
					reason = "current work status is " + (workStatus == null ? "unset" : "\"" + workStatus + "\"");
				}
				if ((oldWorkStatuses == null) ||
				    (oldWorkStatuses.length == 0) ||
				    (workStatus == null) ||
				    (Stream.of (oldWorkStatuses).filter (ws -> workStatus.equals (ws.getDbKey ())).count () > 0)) {
					dbase.logging (Log.WARNING, "mailing", "failed to update workstatus for mailing " + mailingID + ": " + reason);
				} else if (workStatus.equals (newWorkStatus.getDbKey ())) {
					dbase.logging (Log.DEBUG, "mailling", "work status not changed as it is already \"" + workStatus + "\"");
				} else {
					dbase.logging (Log.DEBUG, "mailing", "work status not changed from \"" + workStatus + "\" to \"" + newWorkStatus.getDbKey () + "\" as old work status did not match any of " + Stream.of (oldWorkStatuses).map (ws -> "\"" + ws.getDbKey () + "\"").reduce ((s, e) -> s + ", " + e).orElse ("-none-"));
				}
			}
		}
		return count > 0;
	}

	/**
	 * set the mailerset for this mailing
	 */
	public boolean mailerset (DBase dbase, String newMailerset) throws SQLException {
		int	count = 0;
		
		try (DBase.With with = dbase.with ()) {
			count = dbase.update (with.cursor (),
					      "UPDATE mailing_tbl " +
					      "SET mailerset = :mailerset " +
					      "WHERE mailing_id = :mailingID",
					      "mailerset", newMailerset,
					      "mailingID", mailingID);
			if (count > 0) {
				mailerset = newMailerset;
			}
		}
		return count > 0;
	}
	
	/**
	 * retrieve a MailingID by name from the database
	 */
	public long findMailingByName (DBase dbase, String mailingName, long companyID) throws SQLException {
		long	mailingID = 0;
		
		try (DBase.With with = dbase.with ()) {
			Map <String, Object>	row = dbase.querys (with.cursor (),
								    "SELECT mailing_id " +
								    "FROM mailing_tbl " +
								    "WHERE company_id = :companyID AND shortname = :mailingName AND (deleted IS NULL OR deleted = 0)",
								    "companyID", companyID, "mailingName", mailingName);
			
			if (row != null) {
				mailingID = dbase.asLong (row.get ("mailing_id"));
			}
		}
		return mailingID;
	}
}
