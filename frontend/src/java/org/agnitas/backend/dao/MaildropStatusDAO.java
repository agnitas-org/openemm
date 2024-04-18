/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend.dao;

import	java.sql.SQLException;
import	java.sql.Timestamp;
import	java.util.Date;
import	java.util.HashMap;
import	java.util.List;
import	java.util.Map;

import	org.agnitas.backend.DBase;
import	org.agnitas.util.Log;
import	org.agnitas.util.Systemconfig;

/**
 * Accesses all maildrop status relevant information from the database
 * from the table maildrop_status_tbl
 * 
 * No caching here as we have to ensure to always access the real data
 */
public class MaildropStatusDAO {
	private long		statusID;
	private long		companyID;
	private long		mailingID;
	private String		statusField;
	private Timestamp	sendDate;
	private int		step;
	private int		blockSize;
	private int		genStatus;
	private long		maxRecipients;
	private long		adminTestTargetID;
	private String		optimizeMailGeneration;
	private boolean		selectedTestRecipients;
	private long		overwriteTestRecipient;
	private boolean		dependsOnAutoImportID;
	private boolean		autoImportOK;
	private Date		realSendDate;
		
	public MaildropStatusDAO (DBase dbase, long forStatusID, long forMailingID) throws SQLException {
		List <Map <String, Object>>	rq;
		Map <String, Object>		row;
	
		try (DBase.With with = dbase.with ()) {
			row = dbase.querys (with.cursor (),
					    /*
					    "SELECT status_id, company_id, mailing_id, status_field, senddate, " +
					    "       step, blocksize, genstatus, max_recipients, " +
					    "       admin_test_target_id, optimize_mail_generation, selected_test_recipients, overwrite_test_recipient " +
					     */
					    "SELECT * " +
					    "FROM maildrop_status_tbl " +
					    "WHERE status_id = :statusID",
					    "statusID", forStatusID);
			if (row != null) {
				statusID = dbase.asLong (row.get ("status_id"));
				companyID = dbase.asLong (row.get ("company_id"));
				mailingID = dbase.asLong (row.get ("mailing_id"));
				statusField = dbase.asString (row.get ("status_field"));
				if ("C".equals (statusField)) {
					statusField = "E";
				}
				sendDate = dbase.asTimestamp (row.get ("senddate"));
				step = dbase.asInt (row.get ("step"));
				blockSize = dbase.asInt (row.get ("blocksize"));
				genStatus = dbase.asInt (row.get ("genstatus"));
				maxRecipients = dbase.asLong (row.get ("max_recipients"));
				adminTestTargetID = dbase.asLong (row.get ("admin_test_target_id"));
				optimizeMailGeneration = dbase.asString (row.get ("optimize_mail_generation"));
				selectedTestRecipients = dbase.asInt (row.get ("selected_test_recipients")) == 1;
				overwriteTestRecipient = dbase.asLong (row.get ("overwrite_test_recipient"));
				dependsOnAutoImportID = false;
				autoImportOK = false;
				if ((! "A".equals (statusField)) && (! "T".equals (statusField))) {
					rq = dbase.query (with.cursor (),
							  "SELECT auto_import_id, status_ok " +
							  "FROM mailing_import_lock_tbl " +
							  "WHERE mailing_id = :mailingID",
							  "mailingID", mailingID);
					if (rq.size () > 0) {
						dependsOnAutoImportID = true;
						autoImportOK = rq.stream ().filter (r -> dbase.asInt (r.get ("status_ok")) == 1).count () == rq.size ();
					}
				}
			} else {
				statusID = 0;
				if (forMailingID > 0) {
					MailingDAO	mailing = new MailingDAO (dbase, forMailingID);
					
					companyID = mailing.companyID ();
					mailingID = mailing.mailingID ();
				}
			}
			if (mailingID > 0) {
				determinateRealSendDate (dbase);
			}
		}
	}
	public MaildropStatusDAO (DBase dbase, long forStatusID) throws SQLException {
		this (dbase, forStatusID, 0);
	}

	public long statusID () {
		return statusID;
	}
	public long companyID () {
		return companyID;
	}
	public long mailingID () {
		return mailingID;
	}
	public String statusField () {
		return statusField;
	}
	public Timestamp sendDate () {
		return sendDate;
	}
	public int step () {
		return step;
	}
	public int blockSize () {
		return blockSize;
	}
	public int genStatus () {
		return genStatus;
	}
	public long maxRecipients () {
		return maxRecipients;
	}
	public long adminTestTargetID () {
		return adminTestTargetID;
	}
	public String optimizeMailGeneration () {
		return optimizeMailGeneration;
	}
	public boolean selectedTestRecipients () {
		return selectedTestRecipients;
	}
	public long overwriteTestRecipient () {
		return overwriteTestRecipient;
	}
	public boolean dependsOnAutoImportID () {
		return dependsOnAutoImportID;
	}
	public boolean autoImportOK () {
		return autoImportOK;
	}
	public Date realSendDate () {
		return realSendDate;
	}

	private void determinateRealSendDate (DBase dbase) throws SQLException {
		char				currentStatus = '\0';
		List <Map <String, Object>>	rq;
		Map <String, Object>		row;

		realSendDate = null;
		try (DBase.With with = dbase.with ()) {
			rq = dbase.query (with.cursor (),
					  "SELECT status_id, status_field, senddate " +
					  "FROM maildrop_status_tbl " +
					  "WHERE mailing_id = :mailingID",
					  "mailingID", mailingID);
			for (int n = 0; n < rq.size (); ++n) {
				row = rq.get (n);
			
				String	checkStatusField = dbase.asString (row.get ("status_field"));
			
				if ((checkStatusField != null) && (checkStatusField.length () > 0)) {
					char	status = checkStatusField.charAt (0);
					boolean	hit = false;
					
					switch (status) {
						case 'W':
							hit = true;
							break;
						case 'R':
						case 'D':
							hit = currentStatus != 'W';
							break;
						case 'E':
							hit = currentStatus != 'W' && currentStatus != 'R' && currentStatus != 'D';
							break;
						case 'A':
						case 'T':
							hit = currentStatus == 'T' || currentStatus == 'A' || currentStatus == '\0';
							break;
						default:
							break;
					}
					if (hit) {
						currentStatus = status;
						realSendDate = dbase.asDate (row.get ("senddate"));
					}
				}
			}
		}
		if (realSendDate == null) {
			realSendDate = new Date ();
		}
	}

	static private long findStatusIDForWorldMailing (DBase dbase, long mailingID, String direction) throws SQLException {
		List <Map <String, Object>>	rq;

		try (DBase.With with = dbase.with ()) {
			rq = dbase.query (with.cursor (),
					  "SELECT status_id " +
					  "FROM maildrop_status_tbl " +
					  "WHERE mailing_id = :mailingID AND status_field = :statusField " +
					  "ORDER BY status_id " + direction,
					  "mailingID", mailingID,
					  "statusField", "W");
			if (rq.size () > 0) {
				return dbase.asLong (rq.get (0).get ("status_id"));
			}
		}
		return 0L;
	}
	/**
	 * find the largest status id for a world mailing
	 */
	static public long findLargestStatusIDForWorldMailing (DBase dbase, long mailingID) throws SQLException {
		return findStatusIDForWorldMailing (dbase, mailingID, "DESC");
	}
	/**
	 * find the smallest status id for a world mailing
	 */
	static public long findSmallestStatusIDForWorldMailing (DBase dbase, long mailingID) throws SQLException {
		return findStatusIDForWorldMailing (dbase, mailingID, "ASC");
	}
	/**
	 * update genchange for statusID
	 */
	public boolean updateGenChange (DBase dbase, Date genchange) throws SQLException {
		try (DBase.With with = dbase.with ()) {
			DBase.Retry <Integer>	r = dbase.new Retry <> ("genchange", dbase, with.cursor ()) {
				@Override
				public void execute () throws SQLException {
					if (genchange == null) {
						priv = dbase.update (cursor,
								     "UPDATE maildrop_status_tbl " +
								     "SET genchange = CURRENT_TIMESTAMP " + 
								     "WHERE status_id = :statusID",
								     "statusID", statusID);
					} else {
						priv = dbase.update (cursor,
								     "UPDATE maildrop_status_tbl " +
								     "SET genchange = :genchange " +
								     "WHERE status_id = :statusID",
								     "genchange", genchange,
								     "statusID", statusID);
					}
				}
			};
			if (dbase.retry (r)) {
				dbase.logging (Log.INFO, "genchange", "Update genchange to " + (genchange != null ? genchange.toString () : "now") + " had been " + (r.priv == 1 ? "" : "NOT ") + "successful");
				return r.priv == 1;
			}
			throw r.error;
		}
	}
	public boolean updateGenChange (DBase dbase) throws SQLException {
		return updateGenChange (dbase, null);
	}
	/**
	 * update genstatus for statusID
	 */
	public boolean updateGenStatus (DBase dbase, int fromStatus, int toStatus, boolean forceSetProcessedBy) throws SQLException {
		try (DBase.With with = dbase.with ()) {
			int	count;
		
			DBase.Retry <Integer>	r = dbase.new Retry <> ("genstatus", dbase, with.cursor ()) {
				@Override
				public void execute () throws SQLException {
					Map <String, Object>	parameter = new HashMap <> ();
					String			query =
					     "UPDATE maildrop_status_tbl " +
					     "SET genchange = CURRENT_TIMESTAMP, genstatus = :toStatus";
					String			clause =
					     "WHERE status_id = :statusID";
					
					parameter.put ("toStatus", toStatus);
					parameter.put ("statusID", statusID);
					if (forceSetProcessedBy || (toStatus == 2)) {
						query += ", processed_by = :processedBy";
					} else {
						clause += " AND processed_by = :processedBy";
					}
					parameter.put ("processedBy", Systemconfig.fqdn);
					if (fromStatus > 0) {
						clause += " AND genstatus = :fromStatus";
						parameter.put ("fromStatus", fromStatus);
					}
					query += " " + clause;
					priv = dbase.update (cursor, query, parameter);
					if (priv == 1) {
						Map <String, Object>	rq;
						
						rq = dbase.querys (cursor,
								   "SELECT genstatus " +
								   "FROM maildrop_status_tbl " +
								   "WHERE status_id = :statusID",
								   "statusID", statusID);
						genStatus = -1;
						if (rq == null) {
							dbase.logging (Log.ERROR, "genstatus", "Failed to query maildrop_status_tbl.status_id = " + statusID);
						} else {
							Object	obj = rq.get ("genstatus");

							if (obj == null) {
								dbase.logging (Log.ERROR, "genstatus", "genstatus IS NULL for status_id " + statusID + " but should be " + toStatus);
							} else {
								genStatus = dbase.asInt (obj);
								if (genStatus != toStatus) {
									dbase.logging (Log.ERROR, "genstatus", "genStatus = " + genStatus + " but should be " + toStatus + " for status_id " + statusID);
								} else {
									dbase.logging (Log.INFO, "genstatus", "genStatus = " + genStatus + " as expected for status_id " + statusID);
									if (toStatus == 1) {
										dbase.update (cursor,
											      "UPDATE mailing_import_lock_tbl " +
											      "SET status_ok = 0, change_date = CURRENT_TIMESTAMP " +
											      "WHERE maildrop_status_id = :statusID AND status_ok = 1",
											      "statusID", statusID);
									}
								}
							}
						}
					} else {
						dbase.logging (Log.ERROR, "genstatus", "Primary update of genstatus failed, expected 1 row, but " + priv + " rows are affected");
					}
				}
			};
			if (dbase.retry (r)) {
				count = r.priv;
				if (count == 1) {
					dbase.logging (Log.INFO, "genstatus", "Updated genstatus " + (fromStatus > 0 ? "from " + fromStatus + " " : "") + "to " + toStatus + " for statusID " + statusID);
				} else {
					dbase.logging (Log.INFO, "genstatus", "Failed to update genstatus " + (fromStatus > 0 ? "from " + fromStatus + " " : "") + "to " + toStatus + " for statusID " + statusID + ", affected " + count + " rows");
				}
				return count == 1;
			}
			throw r.error;
		}
	}

	/**
	 * remove an entry in the table
	 */
	public boolean remove (DBase dbase) throws SQLException {
		try (DBase.With with = dbase.with ()) {
			return dbase.update (with.cursor (),
					     "DELETE FROM maildrop_status_tbl " +
					     "WHERE status_id = :statusID",
					     "statusID", statusID) == 1;
		}
	}
}
