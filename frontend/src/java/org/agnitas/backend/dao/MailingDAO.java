/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend.dao;

import	java.sql.SQLException;
import	java.sql.Timestamp;
import	java.util.ArrayList;
import	java.util.HashMap;
import	java.util.HashSet;
import	java.util.List;
import	java.util.Map;
import	java.util.Set;

import	org.agnitas.backend.DBase;
import	org.agnitas.backend.Media;
import	org.agnitas.util.Const;

/**
 * Accesses all mailing relevant information from the database
 * from the tables mailing_tbl, mailing_mt_tbl and mailing_info_tbl
 */
public class MailingDAO {
	private long		mailingID;
	private long		companyID;
	private long		mailinglistID;
	private long		mailtemplateID;
	private boolean		isTemplate;
	private boolean		deleted;
	private String		shortName;
	private Timestamp	creationDate;
	private String		targetExpression;
	private long		splitID;
	private int		mailingType;
	private String		workStatus;
	private int		priority;
	private Boolean		isWorkflowMailing;
	private List <Media>	media;
	private Map <String, String>
				info;
	private long		templateID;
	private int		templatePriority;
		
	public MailingDAO (DBase dbase, long forMailingID) throws SQLException {
		List <Map <String, Object>>	rq;
		Map <String, Object>		row;

		try (DBase.With with = dbase.with ()) {
			row = dbase.querys (with.jdbc (),
					    "SELECT mailing_id, company_id, mailinglist_id, mailtemplate_id, " +
					    "       is_template, deleted, shortname, creation_date, " +
					    "       target_expression, split_id, mailing_type, work_status, priority " +
					    "FROM mailing_tbl WHERE mailing_id = :mailingID",
					    "mailingID", forMailingID);
			if (row != null) {
				//
				// basic information from mailing_tbl
				mailingID = dbase.asLong (row.get ("mailing_id"));
				companyID = dbase.asLong (row.get ("company_id"));
				mailinglistID = dbase.asLong (row.get ("mailinglist_id"));
				mailtemplateID = dbase.asLong (row.get ("mailtemplate_id"));
				isTemplate = dbase.asInt (row.get ("is_template")) > 0;
				deleted = dbase.asInt (row.get ("deleted")) > 0;
				shortName = dbase.asString (row.get ("shortname"));
				creationDate = dbase.asTimestamp (row.get ("creation_date"));
				targetExpression = dbase.asString (row.get ("target_expression"));
				splitID = dbase.asLong (row.get ("split_id"));
				mailingType = dbase.asInt (row.get ("mailing_type"));
				workStatus = dbase.asString (row.get ("work_status"));
				priority = dbase.asInt (row.get ("priority"), -1);
				//
				// workflow related informations
				int	dependencyTypeMailing = Const.WorkflowDependencyType.MAILING_DELIVERY;
		
				row = dbase.querys (with.jdbc (),
						    "SELECT COUNT(*) cnt FROM workflow_dependency_tbl WHERE type = :type AND entity_id = :mailingID",
						    "type", dependencyTypeMailing,
						    "mailingID", mailingID);
				isWorkflowMailing = row != null ? (dbase.asInt (row.get ("cnt")) > 0) : false;
				//
				// media specific information from mailing_mt_tbl
				rq = dbase.query (with.jdbc (),
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
				media.sort ((e1, e2) -> e1.prio - e2.prio);
				//
				// mailing specific informations from mailing_info_tbl
				info = new HashMap <> ();
				rq = dbase.query (with.jdbc (),
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
						info.put (name, value != null ? value : "");
					}
				}
				//
				// find source template
				Set <Long>	seen = new HashSet <Long> ();
				long		scanID = mailtemplateID;
		
				while ((scanID > 0L) && (! seen.contains (scanID))) {
					seen.add (scanID);
					row = dbase.querys (with.jdbc (),
							    "SELECT mailtemplate_id, is_template, deleted, priority " +
							    "FROM mailing_tbl " +
							    "WHERE mailing_id = :mailingID",
							    "mailingID", scanID);
					if (row == null) {
						break;
					}
					if (dbase.asInt (row.get ("is_template")) > 0) {
						if (dbase.asInt (row.get ("deleted")) == 0) {
							templateID = scanID;
							templatePriority = dbase.asInt (row.get ("priority"));
						}
						break;
					}
					scanID = dbase.asLong (row.get ("mailtemplate_id"));
				}
			} else {
				mailingID = 0;
			}
		}
	}
	public long mailingID () {
		return mailingID;
	}
	public long companyID () {
		return companyID;
	}
	public long mailinglistID () {
		return mailinglistID;
	}
	public long mailtemplateID () {
		return mailtemplateID;
	}
	public boolean isTemplate () {
		return isTemplate;
	}
	public boolean deleted () {
		return deleted;
	}
	public String shortName () {
		return shortName;
	}
	public Timestamp creationDate () {
		return creationDate;
	}
	public String targetExpression () {
		return targetExpression;
	}
	public long splitID () {
		return splitID;
	}
	public int mailingType () {
		return mailingType;
	}
	public String workStatus () {
		return workStatus;
	}
	public int priority () {
		return priority;
	}
	public Boolean isWorkflowMailing () {
		return isWorkflowMailing;
	}
	public List <Media> media () {
		return media;
	}
	public Map <String, String> info () {
		return info;
	}
	public long sourceTemplateID () {
		return templateID;
	}
	public int sourceTemplatePriority () {
		return templatePriority;
	}

	/**
	 * set the working status for this mailing
	 */
	public boolean workStatus (DBase dbase, String newWorkStatus) throws SQLException {
		int	count = 0;
		
		try (DBase.With with = dbase.with ()) {
			count = dbase.update (with.jdbc (),
					      "UPDATE mailing_tbl " +
					      "SET work_status = :workStatus " +
					      "WHERE mailing_id = :mailingID",
					      "workStatus", newWorkStatus,
					      "mailingID", mailingID);
			if (count > 0) {
				workStatus = newWorkStatus;
			}
		}
		return count > 0;
	}
}
