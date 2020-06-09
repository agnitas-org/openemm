/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import	java.sql.Connection;
import	java.sql.PreparedStatement;
import	java.sql.ResultSet;
import	java.sql.SQLException;
import	java.util.ArrayList;
import	java.util.Calendar;
import	java.util.Date;
import	java.util.HashMap;
import	java.util.HashSet;
import	java.util.List;
import	java.util.Map;
import	java.util.TimeZone;

import org.agnitas.dao.UserStatus;
import org.agnitas.util.Log;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.agnitas.dao.DaoUpdateReturnValueCheck;

/**
 * BC, the Big Clause, creates the main queries to prepare and retrieve
 * the recipients for a mailing.
 */
public class BC {
	/** local reference to context */
	private Data		data = null;
	private String		customerTable = null;
	private String		bindingTable = null;
	/** temporary table for storing customerIDs */
	private String		table = null;
	/** if the table had been successful created */
	private boolean		tableCreated = false;
	/** sorted references for proper seolving of joins */
	private List <Reference>
				sortedReferences = null;
	/* parts of final where clause */
	private String		partFrom = null;
	private String		partUserstatus = null;
	private String		partUserstatusBounce = null;
	private String		partUsertype = null;
	private String		partMailinglist = null;
	private String		partSubselect = null;
	private String		partCounter = null;
	private String		partReactivate = null;
	/** number of receivers for this mailing */
	private long		subscriberCount = 0;
	/** number of real receiver for this run */
	private long		receiverCount = 0;

	public BC (Data nData) {
		data = nData;
		customerTable = "customer_" + data.company.id () + "_tbl";
		bindingTable = "customer_" + data.company.id () + "_binding_tbl";
	}

	/**
	 * Cleanup code to remove all created temporary tables
	 */
	public BC done () {
		if (tableCreated && removeTable (table)) {
			tableCreated = false;
		}
		return null;
	}

	/*
	 * Depending on the mailing type and its configuration,
	 * prepare all relevant parts to create the main queries
	 * later.
	 * 
	 * @return true, if preparation had been successful, false otherwise
	 */
	public boolean prepareQueryParts () {
		boolean rc = false;
		String	partFromPrepare;

		partFromPrepare = partExtend (customerTable + " cust INNER JOIN " + bindingTable + " bind ON (cust.customer_id = bind.customer_id)");
		if ((data.maildropStatus.isAdminMailing () || data.maildropStatus.isTestMailing ()) && data.maildropStatus.selectedTestRecipients ()) {
			partUserstatus = "bind.user_status IN (" + UserStatus.Active.getStatusCode() + ", " + UserStatus.Suspend.getStatusCode() + ")";
		} else if ((data.defaultUserStatus != UserStatus.Active.getStatusCode()) && (data.maildropStatus.isAdminMailing () || data.maildropStatus.isTestMailing ())) {
			partUserstatus = "bind.user_status IN (" + data.defaultUserStatus + ", " + UserStatus.Active.getStatusCode() + ")";
		} else {
			partUserstatus = "bind.user_status = " + data.defaultUserStatus;
			if (data.defaultUserStatus == UserStatus.Active.getStatusCode()) {
				partUserstatusBounce = "bind.user_status = " + UserStatus.Bounce.getStatusCode();
			}
		}
		partMailinglist = "bind.mailinglist_id = " + data.mailinglist.id ();
		if (data.maildropStatus.isAdminMailing () ||
		    data.maildropStatus.isTestMailing () ||
		    data.maildropStatus.isRuleMailing () ||
		    data.maildropStatus.isOnDemandMailing () ||
		    data.maildropStatus.isWorldMailing ()) {
			partSubselect = data.targetExpression.subselect ();
			rc = collectRecipientsToTemporaryTable (partFromPrepare);
			if (rc) {
				String	subscriberQuery, receiverQuery;

				receiverQuery = "SELECT count(distinct customer_id) FROM " + table + " WHERE user_type IN ('A', 'T', 'W')";
				if (data.maildropStatus.isAdminMailing () || data.maildropStatus.isTestMailing ()) {
					subscriberQuery = "SELECT count(distinct cust.customer_id) FROM " + partFromPrepare + " WHERE " + partCounter;
				} else {
					subscriberQuery = receiverQuery;
				}
				try {
					subscriberCount = data.dbase.queryLong (subscriberQuery);
					if (! receiverQuery.equals (subscriberQuery)) {
						receiverCount = data.dbase.queryLong (receiverQuery);
					} else {
						receiverCount = subscriberCount;
					}
				} catch (Exception e) {
					data.logging (Log.ERROR, "bc", "Failed to count " + table + ": " + e.toString (), e);
				}
			}
			partFrom = partExtend (customerTable + " cust INNER JOIN " + table + " bind ON (cust.customer_id = bind.customer_id)");
		} else if (data.maildropStatus.isCampaignMailing () || data.maildropStatus.isVerificationMailing () || data.maildropStatus.isPreviewMailing ()) {
			String	receiverQuery;

			if (data.campaignForceSending || data.maildropStatus.isPreviewMailing ()) {
				partFrom = partExtend (customerTable + " cust LEFT OUTER JOIN " + bindingTable + " bind ON (cust.customer_id = bind.customer_id AND " + partMailinglist + ")");
			} else {
				partFrom = partFromPrepare;
			}
			if (data.maildropStatus.isCampaignMailing ()) {
				if (data.campaignEnableTargetGroups) {
					partSubselect = data.targetExpression.subselect ();
				}
				if ((data.campaignUserStatus != null) && (data.campaignUserStatus.length > 0)) {
					if (data.campaignUserStatus.length == 1) {
						partUserstatus = "bind.user_status = " + data.campaignUserStatus[0];
					} else {
						partUserstatus = "bind.user_status IN (";
						String	sep = "";
						for (long userStatus : data.campaignUserStatus) {
							partUserstatus += sep + userStatus;
							sep = ", ";
						}
						partUserstatus += ")";
					}
				}
			}
			partCounter = partCustomer (true);
			
			long	customerID;
			
			if (data.maildropStatus.isCampaignMailing () || data.maildropStatus.isVerificationMailing ()) {
				customerID = data.campaignCustomerID;
			} else if (data.maildropStatus.isPreviewMailing ()) {
				customerID = data.previewCustomerID;
			} else {
				customerID = 0;
			}
			
			rc = setMissingVoucherCode (customerID);
			if (rc) {
				subscriberCount = 1;
				if (data.maildropStatus.isCampaignMailing ()) {
					receiverQuery = "SELECT count(distinct cust.customer_id) FROM " + partFrom + " WHERE " + partCounter;
					try {
						receiverCount = data.dbase.queryLong (receiverQuery);
					} catch (Exception e) {
						data.logging (Log.ERROR, "bc", "Failed to count receiver using \"" + receiverQuery + "\", setting to " + subscriberCount +": " + e.toString (), e);
						receiverCount = subscriberCount;
					}
				} else {
					receiverCount = subscriberCount;
				}
			}
		} else {
			data.logging (Log.ERROR, "bc", "Unsupported mailing type detected!");
			partFrom = partFromPrepare;
		}
		if (data.targetExpression.resolveByDatabase ().size () > 0) {
			partFrom +=
				data.targetExpression.resolveByDatabase ().stream ()
				.map (t -> " LEFT OUTER JOIN agn_tg_" + t.getID () + " ON agn_tg_" + t.getID () + ".customer_id = cust.customer_id")
				.reduce ((s, e) -> s + " " + e).orElse (null);
		}
		partFrom = "FROM " + partFrom;
		return rc;
	}

	/**
	 * return number of subscriber for this newsletter,
	 * i.e. the number of recipients the mail would
	 * receive, this may differ from receiver when sending
	 * admin or test mailings
	 * 
	 * @return the number of estimated subscriber for this mailing
	 */
	public long subscriber () {
		return subscriberCount;
	}

	/**
	 * return the real number of receiver for this invokation,
	 * this may differ from subscriber when sending admin or
	 * test mailings
	 * 
	 * @return the numer of recevier
	 */
	public long receiver () {
		return receiverCount;
	}

	/**
	 * Create the initial part of the final query, this is either
	 * a WITH statement to cover all database evaluated target
	 * groups or just a simple SELECT.
	 * 
	 * @param fullExpression if true, target groups are evaulated, if neccessary
	 * @return               the string used as the start of the select statement
	 */
	
	public String createSelect (boolean fullExpression) {
		if (fullExpression && (data.targetExpression.resolveByDatabase ().size () > 0)) {
			return
				"WITH\n    " + data.targetExpression.resolveByDatabase ().stream ()
					.map (t -> "agn_tg_" + t.getID () + " (customer_id, value) AS (SELECT cust.customer_id, 1 FROM " + customerTable + " cust WHERE " + t.getSQL (true) + ")")
					.reduce ((s, e) -> s + ",\n    " + e).orElse (null) + "\n" +
				"SELECT";
		}
		return "SELECT";
	}

	/**
	 * Creates a list of SQL WHERE clauses to be used for selecting
	 * the receivers
	 * 
	 * @return a list of strings containing the clauses to be used to select all recipients
	 */
	public List <String> createClauses () {
		List <String> rc = new ArrayList <> ();

		if (data.maildropStatus.isWorldMailing () || data.maildropStatus.isRuleMailing () || data.maildropStatus.isOnDemandMailing ()) {
			String	order = null;
			
			rc.add (partClause ("bind.user_type IN ('A', 'T', 'w')"));
			if (data.omgEnabled ()) {
				order = "NVL (bind.omg, 0)";
			}
			rc.add (partClause ("bind.user_type = 'W'", order));
		} else if (data.maildropStatus.isAdminMailing () || data.maildropStatus.isTestMailing ()) {
			rc.add (partClause (null));
		} else if (data.maildropStatus.isCampaignMailing () || data.maildropStatus.isVerificationMailing () || data.maildropStatus.isPreviewMailing ()) {
			rc.add (partClause (partCustomer (true)));
		}
		return rc;
	}

	/**
	 * Removes a recipient from the selected recipient collection, e.g. for blacklisted
	 * 
	 * @param cid the customerID of the recipient
	 */
	public void removeReceiver (Long cid) {
		receiverCount -= 1;
		if (table != null) {
			try {
				data.dbase.update ("DELETE FROM " + table + " WHERE customer_id = :customerID", "customerID", cid);
			} catch (Exception e) {
				data.logging (Log.ERROR, "bc", "Failed to remove " + cid + " from " + table + ": " + e.toString (), e);
			}
		}
	}

	/**
	 * Creates a simple selection clause to access the recipients of a mailing for
	 * further processing, e.g. for setting the lastsend_date
	 * 
	 * @param tableSelector the selector for the related table, e.g. "cust" for the customer table
	 * @param reduction     an optional query to reduce the selected recipients, e.g. for limiting the updates for one query
	 * @return              the SQL WHERE claus to be used for the final query
	 */
	public String createSimpleClause (String tableSelector, String reduction) {
		if (data.maildropStatus.isCampaignMailing () || data.maildropStatus.isVerificationMailing ()) {
			if (receiverCount > 0) {
				return tableSelector + ".customer_id = " + data.campaignCustomerID + (partSubselect != null ? " AND (" + partSubselect + ")" : "");
			}
		} else {
			return "EXISTS (SELECT 1 FROM " + table + " bind WHERE " + tableSelector + ".customer_id = bind.customer_id" + (reduction != null ? " AND (" + reduction + ")" : "") + ")";
		}
		return null;
	}

	/**
	 * Cretes the statement to write the recipients to the mailtrack table
	 * 
	 * @param destination the name of the mailtrack table to use
	 * @return            the final statement to write the recipients to the mailtrack table
	 */
	public String mailtrackStatement (String destination) {
		if (! data.maildropStatus.isVerificationMailing ()) {
			String	prefix = "INSERT INTO " + destination + " (maildrop_status_id, mailing_id, customer_id, timestamp) ";
			
			if (data.maildropStatus.isCampaignMailing () || data.maildropStatus.isVerificationMailing ()) {
				if (receiverCount > 0) {
					return prefix + "VALUES (" + data.maildropStatus.id () + ", " + data.mailing.id () + ", " + data.campaignCustomerID + ", CURRENT_TIMESTAMP)";
				}
			} else {
				return prefix + "SELECT " + data.maildropStatus.id () + ", " + data.mailing.id () + ", customer_id, CURRENT_TIMESTAMP FROM " + table;
			}
		}
		return null;
	}
	
	/**
	 * Returns the name which had been used for mail generation. This may be
	 * the original customer_xx_binding_tbl or the temporary created table
	 * where all recipient are collected to.
	 * 
	 * @return the name of the used binding table
	 */
	public String getBindingTable () {
		return tableCreated ? table : bindingTable;
	}

	/**
	 * Return a statement to select the customer_id and mediatype for all
	 * recipients
	 * 
	 * @return the query to select these binding values
	 */
	public String getBindingQuery () {
		String	query;
		
		if (tableCreated) {
			query = "SELECT customer_id, mediatype FROM " + table;
		} else {
			query = "SELECT customer_id, mediatype FROM customer_" + data.company.id () + "_binding_tbl bind WHERE " + partCustomer (false);
		}
		return query;
	}
	
	/**
	 * Returns the statement to isnert the recipients for an interval
	 * mailing
	 * 
	 * @param destination the name of the interval tracking table
	 * @return            the statement to use to write the tracking infromation
	 */
	public String intervalStatement (String destination) {
		return "INSERT INTO " + destination + " (customer_id, mailing_id, send_date) SELECT customer_id, :mailingID, :sendDate FROM " + table;
	}

	private boolean removeTable (String tname) {
		boolean rc = false;

		try {
			data.dbase.execute ("TRUNCATE TABLE " + tname);
		} catch (Exception e) {
			data.logging (Log.WARNING, "bc", "Failed to truncate table " + tname + ": " + e.toString ());
		}
		try {
			data.dbase.execute ("DROP TABLE " + tname);
			rc = true;
		} catch (Exception e) {
			data.logging (Log.ERROR, "bc", "Failed to drop table " + tname + ": " + e.toString ());
		}
		return rc;
	}

	private boolean createTable (String tname, String stmt, List <String> adds) {
		boolean rc = false;

		try {
			if (data.dbase.tableExists (tname)) {
				data.logging (Log.INFO, "bc", "Try to remove stale table " + tname);
				removeTable (tname);
			}
		} catch (Exception e) {
			data.logging (Log.WARNING, "bc", "Failed to remove stale table " + tname + ": " + e.toString ());
		}
		for (int n = 0; (! rc) && (n < 2); ++n) {
			try {
				data.dbase.execute (stmt);
				rc = true;
			} catch (Exception e) {
				if (n == 0) {
					data.logging (Log.WARNING, "bc", "Failed to create table " + tname + ", try to remove stale one");
					removeTable (tname);
				} else {
					data.logging (Log.ERROR, "bc", "Failed to create table " + tname + ": " + e.toString (), e);
				}
			}
		}
		if (rc && (adds != null)) {
			for (int n = 0; n < adds.size (); ++n) {
				String	add = adds.get (n);

				if (add != null) {
					try {
						int	count = data.dbase.update (add);
						
						receiverCount += count;
						data.logging (Log.DEBUG, "bc", "Added " + count + " receiver using: " + add);
					} catch (Exception e) {
						data.logging (Log.ERROR, "bc", "Failed to add \"" + add + "\": " + e.toString (), e);
						rc = false;
					}
				}
			}
		}
		return rc;
	}
	
	private void getColumns (List <String> collect, Map <String, String> cmap) {
		collect.add ("customer_id");
		cmap.put ("customer_id", "cust.customer_id");
		collect.add ("user_type");
		cmap.put ("user_type", "bind.user_type");
		collect.add ("mediatype");
		cmap.put ("mediatype", "bind.mediatype");
		collect.add ("timestamp");
		cmap.put ("timestamp", "bind.timestamp");
		if (data.shouldRemoveDuplicateEMails ()) {
			if (data.mediaEMail != null) {
				String	column = data.mediaEMail.profileField ();
				
				if (column != null) {
					collect.add (column);
					cmap.put (column, "cust." + column);
				}
			}
		}
		if (data.omgEnabled ()) {
			collect.add ("omg");
			cmap.put ("omg", "agn_omg.openhour");
		}
	}

	private void getRestrictions (List <String> collect) {
		collect.add (partSubselect);
		if (! data.maildropStatus.isPreviewMailing ()) {
			collect.add (data.getMediaSubselect ());
		}
		if (data.maildropStatus.isWorldMailing () || data.maildropStatus.isRuleMailing () || data.maildropStatus.isOnDemandMailing ()) {
			collect.add (data.getFollowupSubselect ());
		}
	}

	private void getReduction (List <String> collect) {
		for (String red : data.getReduction ()) {
			collect.add (red);
		}
	}

	private void getExtensions (List <String> collect) {
		if (StringOps.atob (data.company.info ("use-extended-usertypes"), true)) {
			String	userType = null;
			
			if (data.maildropStatus.isWorldMailing ()) {
				userType = "w";
			} else if (data.maildropStatus.isTestMailing ()) {
				userType = "t";
			}
			if (userType != null) {
				String	mss = data.getMediaSubselect ();
				String	stmt;
				
				stmt = "(bind.user_type = '" + userType + "' AND bind.mailinglist_id IN (0, " + data.mailinglist.id () + "))";
				if (mss != null) {
					stmt += " AND " + mss;
				}
				collect.add (stmt);
			}
		}
	}

	private String getPartUsertype () {
		if (data.maildropStatus.isWorldMailing ()) {
			return "bind.user_type IN ('A', 'T', 'W')";
		} else if (data.maildropStatus.isAdminMailing () || data.maildropStatus.isTestMailing ()) {
			if (data.maildropStatus.selectedTestRecipients ()) {
				return "EXISTS (SELECT 1 FROM test_recipients_tbl test WHERE test.maildrop_status_id = " + data.maildropStatus.id () + " AND test.customer_id = bind.customer_id)";
			}
			if (data.maildropStatus.isAdminMailing ()) {
				return "bind.user_type = 'A'";
			}
			return "bind.user_type IN ('A', 'T')";
		}
		return null;
	}

	private String getTemporary (String tableName) {
		if (data.dbase.isOracle ()) {
			String	ts = data.tempTablespace ();
			
			if (ts != null) {
				return "TABLE " + tableName + " TABLESPACE " + ts;
			}
		}
		return "TABLE " + tableName;
	}

	
	private long countTable (String tableName, String what, String limit) {
		long	cnt;
		
		try {
			cnt = data.dbase.queryLong ("SELECT count(" + (what == null ? "*" : what) + ") FROM " + tableName + (limit == null ? "" : " WHERE " + limit));
		} catch (Exception e) {
			data.logging (Log.ERROR, "bc", "Failed to count table " + tableName + " using " + (what == null ? "(*)" : what + ": " + e.toString ()), e);
			cnt = -1;
		}
		return cnt;
	}
	
	private boolean hasDuplicateEntries () {
		boolean	rc = true;
		
		try {
			Map <String, Object>	row = data.dbase.querys ("SELECT count(distinct email) uemail, count(email) aemail FROM " + table + " WHERE user_type IN ('A', 'T', 'W')");

			if (row != null) {
				long	uniqueEmails = data.dbase.asLong (row.get ("uemail")),
					availableEmails = data.dbase.asLong (row.get ("aemail"));
				
				rc = uniqueEmails != availableEmails;
				data.logging (Log.INFO, "bc", "Duplicate check will " + (rc ? "" : "not ") + "be executed (" + uniqueEmails + " unique of " + availableEmails + " available email addresses)");
			} else {
				data.logging (Log.ERROR, "bc", "Failed to determinate if removal of duplicate entries is required");
			}
		} catch (Exception e) {
			data.logging (Log.ERROR, "bc", "Failed to check dups, assume true: " + e.toString (), e);
		}
		return rc;
	}

	@DaoUpdateReturnValueCheck
	private boolean collectRecipientsToTemporaryTable (String partFromPrepare) {
		List <String>		 columns = new ArrayList <> ();
		Map <String, String>	cmap = new HashMap <> ();
		
		getColumns (columns, cmap);
		String	queryFields = columns.stream ()
			.reduce ((s, e) -> s + ", " + e)
			.orElse ("");
		String	selectFields = columns.stream ()
			.map (c -> (cmap.get (c) != null ? cmap.get (c) + " AS " : "") + c)
			.reduce ((s, e) -> s + ", " + e)
			.orElse ("");

		table = "TMP_CRT_" + data.maildropStatus.statusField () + "_" + data.mailing.id () + "_" + data.maildropStatus.id () + "_TBL";
		
		String	partSelect;
		
		partSelect = partUserstatus + " AND " + partMailinglist;
		partCounter = partUserstatus + " AND " + partMailinglist;
		if ((partUserstatusBounce != null) && data.ahvEnabled ()) {
			partReactivate = partUserstatusBounce + " AND " + partMailinglist;
			if (data.ahvOldestEntryToReactivate () != null) {
				partReactivate += " AND agn_ahv.reactivate >= :oldest AND bind.timestamp >= :oldest";
			}
		}
		partUsertype = getPartUsertype ();
		if (partUsertype != null) {
			partSelect += " AND " + partUsertype;
		}
		List <String> collect = new ArrayList <> ();
		boolean limitSelect = data.maildropStatus.isWorldMailing () ||
				      data.maildropStatus.isOnDemandMailing () ||
				      data.maildropStatus.isRuleMailing () ||
				      data.isDryRun ();

		getRestrictions (collect);
		for (String rest : collect) {
			if (rest != null) {
				if (limitSelect) {
					partSelect += " AND (" + rest + ")";
				}
				partCounter += " AND (" + rest + ")";
				if (partReactivate != null) {
					partReactivate += " AND (" + rest + ")";
				}
			}
		}
		collect.clear ();
		getReduction (collect);
		for (String rest : collect) {
			if (rest != null) {
				partSelect += " AND (" + rest + ")";
				if (partReactivate != null) {
					partReactivate += " AND (" + rest + ")";
				}
			}
		}
		if (partReactivate != null) {
			processReactivation (partFromPrepare);
		}
		String	partFromSource = partSource ();
		String	partFromCreate = partFromPrepare + (partFromSource != null ? " " + partFromSource : "");
		String	customerSelect =
			"SELECT distinct " + selectFields + "\n" +
			"FROM " + partFromCreate;
		String	stmt =
			"CREATE " + getTemporary (table) + " AS\n" + customerSelect + "\n" +
			"WHERE " + partSelect;
		List <String> adds = new ArrayList <> ();
		
		collect.clear ();
		getExtensions (collect);
		for (String ext : collect) {
			if (ext != null) {
				String	add =
					"INSERT INTO " + table + " (" + queryFields + ")\n" + customerSelect + "\n" +
					"WHERE " + ext;
				adds.add (add);
			}
		}
		tableCreated = createTable (table, stmt, adds);
		if (tableCreated) {
			String	query = "CREATE INDEX TMP_CUID_" + data.maildropStatus.id () + "_IDX ON " + table + " (customer_id)";
			
			if (data.dbase.isOracle ()) {
				String	ts = data.tempTablespace ();
				
				if (ts != null) {
					query += " TABLESPACE " + ts;
				}
			}
			try {
				data.dbase.execute (query);
			} catch (Exception e) {
				data.logging (Log.ERROR, "bc", "Failed to create index using \"" + query + "\": " + e.toString (), e);
			}
		}
		String	ctable = "TMP_CRT_" + data.maildropStatus.id () + "_TBL";
		
		boolean	fail = false;
		if ((! tableCreated) ||
		    (! handleDuplicateAddreses (ctable)) ||
		    (! prepareDailyLimits ()) ||
		    (! removePrioritizedReceiver ()) ||
		    (! removeReceiversReachedDailyLimit (ctable)) ||
		    (! updateOptimizedMailGenerationTimestamp ()) ||
		    (! fillMissingVoucherCodes ())) {
			fail = true;
		}
		if (fail && tableCreated) {
			removeTable (table);
			tableCreated = false;
		}
		return tableCreated;
	}
	
	static class Filler implements ResultSetExtractor <Object> {
		private String	fquery;
		private long	count;
		
		public Filler (String nFquery) {
			fquery = nFquery;
			count = 0;
		}
		
		public long getCount () {
			return count;
		}
		
		@Override
		public Object extractData (ResultSet rset) throws SQLException, DataAccessException {
			try (Connection conn = DBase.DATASOURCE.getConnection ()) {
				boolean			autoCommit = conn.getAutoCommit ();
				
				try {
					conn.setAutoCommit (false);
					try (PreparedStatement prep = conn.prepareStatement (fquery)) {
						HashSet	<String> 		seen = new HashSet<>();
			
						count = 0;
						while (rset.next ()) {
							long	customerID = rset.getLong (1);
							String	email = rset.getString (2);

							if (email != null) {
								if (seen.contains (email)) {
									prep.setLong (1, customerID);
									count += prep.executeUpdate ();
									if (count % 1000 == 0) {
										conn.commit ();
									}
								} else {
									seen.add (email);
								}
							}
						}
						conn.commit ();
					}
				} finally {
					conn.setAutoCommit (autoCommit);
				}
			}
			return null;
		}
	}
	private boolean handleDuplicateAddreses (String ctable) {
		boolean	ok = true;
		
		if (data.shouldRemoveDuplicateEMails () && hasDuplicateEntries ()) {
			boolean	ctableCreated = false;
			try {
				String	stmt;
				
				if (data.dbase.isOracle ()) {
					stmt = "CREATE TABLE " + ctable + " (customer_id NUMBER)";
				} else {
					stmt = "CREATE TABLE " + ctable + " (customer_id INTEGER UNSIGNED)";
				}

				ctableCreated = createTable (ctable, stmt, null);
				if (ctableCreated) {
					try {
						String	fquery = "INSERT INTO " + ctable + " (customer_id) VALUES (?)";
						Filler	filler = new Filler (fquery);
						String	query = "SELECT customer_id, email FROM " + table + " WHERE user_type IN ('A', 'T', 'W') ORDER BY customer_id";
						long	cnt;
						
						DBase.Retry <Long>	r = data.dbase.new Retry <Long> ("filler", data.dbase, data.dbase.jdbc (query)) {
							@Override
							public void execute () throws SQLException {
								jdbc.query (query, filler);
								priv = filler.getCount ();
							}
						};
						if (data.dbase.retry (r)) {
							cnt = r.priv != null ? r.priv : 0L;
							if (cnt > 0) {
								int	updcnt;
						
								updcnt = data.dbase.update ("DELETE FROM " + table + " WHERE customer_id IN (SELECT customer_id FROM " + ctable + ")");
								data.logging (Log.INFO, "bc", "Found " + cnt + " duplicate entries, removed " + updcnt + " entries");
							}
						} else {
							throw r.error;
						}
					} catch (Exception e) {
						data.logging (Log.ERROR, "bc", "Failed to removed duplicates: " + e.toString (), e);
						ok = false;
					}
					removeTable (ctable);
					ctableCreated = false;
				} else {
					data.logging (Log.ERROR, "bc", "Failed to create temp. table for finding duplicates");
					ok = false;
				}
			} finally {
				if (ctableCreated) {
					removeTable (ctable);
				}
			}
		}

		String	splitExpression = data.targetExpression.getSplitExpression ();
		
		if (ok && data.shouldRemoveDuplicateEMails () && (splitExpression != null)) {
			String	query;
			
			if (data.dbase.isOracle ()) {
				query = "DELETE FROM " + table + " self " +
					"WHERE NOT EXISTS (SELECT 1 FROM " + table + " cust WHERE self.customer_id = cust.customer_id AND (" + splitExpression + "))";
			} else {
				query = "DELETE FROM " + table + " " +
					"WHERE customer_id NOT IN (SELECT customer_id FROM " + table + " cust WHERE " + splitExpression + ")";
			}
			try {
				int	count = data.dbase.update (query);
				
				data.logging (Log.INFO, "bc", "Removed " + count + " for listsplit");
			} catch (Exception e) {
				data.logging (Log.ERROR, "bc", "Failed to select listsplit: " + e.toString (), e);
				ok = false;
			}
		}
		return ok;
	}
	
	private boolean prepareDailyLimits () {
		boolean	ok = true;

		if (data.company.limitMailsPerDay () && (data.maildropStatus.isWorldMailing () || data.maildropStatus.isRuleMailing () || data.maildropStatus.isOnDemandMailing ())) {
			Calendar	temp;
			int		timestamp;
			String		upd = "unset";
			long		companyID = data.company.baseID ();
			String		limitTable = "RECVLIMIT_" + companyID + "_TBL";
			int		mpdl = data.company.mailsPerDayLocal ();
			int		mpdg = data.company.mailsPerDayGlobal ();
			String		tz = data.company.mailsPerDayTZ ();
			
			if (tz == null) {
				temp = Calendar.getInstance ();
			} else {
				temp = Calendar.getInstance (TimeZone.getTimeZone (tz));
			}
			temp.setTime (data.currentSendDate);
			timestamp = temp.get (Calendar.YEAR) * 10000 + temp.get (Calendar.MONTH) * 100 + temp.get (Calendar.DAY_OF_MONTH);
			try {
				if (! data.dbase.tableExists (limitTable)) {
					String	mktable;
				
					if (data.dbase.isOracle ()) {
						mktable = "CREATE TABLE " + limitTable + "(\n" +
							  "\tCOMPANY_ID NUMBER NOT NULL,\n" +
							  "\tCUSTOMER_ID NUMBER NOT NULL,\n" +
							  "\tSENDDATE NUMBER NOT NULL,\n" +
							  "\tSTATUS_FIELD VARCHAR2(1) NOT NULL,\n" +
							  "\tMAILCOUNT NUMBER NOT NULL\n" +
							  ")";
					} else {
						mktable = "CREATE TABLE " + limitTable + "(\n" +
							  "\tCOMPANY_ID int(11) NOT NULL,\n" +
							  "\tCUSTOMER_ID INTEGER UNSIGNED NOT NULL,\n" +
							  "\tSENDDATE int(11) NOT NULL,\n" +
							  "\tSTATUS_FIELD VARCHAR(1) NOT NULL,\n" +
							  "\tMAILCOUNT int(11) NOT NULL\n" +
							  ")";
					}
					data.dbase.execute (mktable);
				}
				upd = "UPDATE " + limitTable + " SET mailcount = mailcount + 1 " +
				      "WHERE company_id = :companyID AND senddate = :senddate" +
					   " AND status_field = :statusField" +
					   " AND customer_id IN (SELECT customer_id FROM " + table + ")";
				data.dbase.update (upd, "companyID", companyID, "senddate", timestamp, "statusField", data.maildropStatus.statusField ());
				upd = "INSERT INTO " + limitTable + " (company_id, customer_id, senddate, status_field, mailcount) " +
				      "SELECT :companyID, customer_id, :senddate, :statusField, 1 FROM " + table + " bind " +
				      "WHERE NOT EXISTS (SELECT 1 FROM " + limitTable + " recv WHERE bind.customer_id = recv.customer_id AND company_id = :companyID AND senddate = :senddate AND status_field = :statusField)";
				data.dbase.update (upd, "companyID", companyID, "senddate", timestamp, "statusField", data.maildropStatus.statusField ());
				
				long	count = 0;
				
				for (int state = 0; state < 2; ++state) {
					int	limit = state == 0 ? mpdl : mpdg;
					
					if (limit > 0) {
						upd = "DELETE FROM " + table + " WHERE customer_id IN (";
						switch (state) {
						case 0:
							upd += "SELECT customer_id FROM " + limitTable + " " +
								"WHERE company_id = " + companyID +
								" AND senddate = " + timestamp +
								" AND status_field = '" + data.maildropStatus.statusField () + "'" +
								" AND mailcount > " + limit;
							break;
						case 1:
							upd += "SELECT customer_id FROM " + limitTable +" r1 " +
								"WHERE company_id = " + companyID +
								" AND senddate = " + timestamp +
								" AND (" +
								      "SELECT sum(mailcount) FROM " + limitTable + " r2 " +
								       "WHERE r1.customer_id = r2.customer_id " +
								       " AND company_id = " + companyID +
								       " AND senddate = " + timestamp +
								") > " + limit;
							break;
						default:
							throw new Exception("Unexpected state");
						}
						upd += ")";
						count += data.dbase.update (upd);
					}
				}
				if (count > 0) {
					data.logging (Log.WARNING, "bc", "-=[mailsperday]=- statusID:" + data.maildropStatus.id () + " count:" + count);
				}
			} catch (Exception e) {
				data.logging (Log.ERROR, "bc", "Failed to apply day limit in \"" + upd + "\": " + e.toString (), e);
				ok = false;
			}
		}
		return ok;
	}
	
	private boolean removeReceiversReachedDailyLimit (String ctable) {
		boolean	ok = true;
		
		if ((data.maildropStatus.limitReceivers () > 0) &&
		    (data.maildropStatus.isWorldMailing () || data.maildropStatus.isRuleMailing () || data.maildropStatus.isOnDemandMailing ())) {
			long	cnt;
			
			cnt = countTable (table, "distinct customer_id", "user_type = 'W'");
			if ((cnt == -1) || (cnt > data.maildropStatus.limitReceivers ())) {
				boolean	ctableCreated = false;

				try {
					String	stmt;
					
					if (data.dbase.isOracle ()) {
						stmt = "CREATE TABLE " + ctable + " (customer_id) AS SELECT distinct customer_id FROM " + table + " WHERE user_type = 'W' ORDER BY dbms_random.value";
					} else {
						stmt = "CREATE TABLE " + ctable + " (customer_id INTEGER UNSIGNED) AS SELECT distinct customer_id FROM " + table + " WHERE user_type = 'W' ORDER BY rand()";
					}
					ctableCreated = createTable (ctable, stmt, null);
					if (ctableCreated) {
						cnt = countTable (ctable, null, null);
						if ((cnt == -1) || (cnt > data.maildropStatus.limitReceivers ())) {
							try {
								String	query;
								if (data.dbase.isOracle ()) {
									query = "DELETE FROM " + ctable + " WHERE rownum <= :limitRecv";
								} else {
									query = "DELETE FROM " + ctable + " LIMIT :limitRecv";
								}
								data.dbase.update (query, "limitRecv", data.maildropStatus.limitReceivers ());
								cnt = data.dbase.update ("DELETE FROM " + table + " WHERE customer_id IN (SELECT customer_id FROM " + ctable + ")");
								data.logging (Log.INFO, "bc", "Removed " + cnt + " due to receiver limitation to " + data.maildropStatus.limitReceivers ());
							} catch (Exception e) {
								data.logging (Log.ERROR, "bc", "Failed to apply limitation: " + e.toString (), e);
								ok = false;
							}
						}
					} else {
						data.logging (Log.ERROR, "bc", "Failed to create temp. table for limiting recipients");
						ok = false;
					}
				} finally {
					if (ctableCreated) {
						removeTable (ctable);
					}
				}
			}
		}
		return ok;
	}

	private boolean removePrioritizedReceiver () {
		boolean	ok = true;
		
		if (data.isPriorityMailing) {
			long	cnt, remain;
			String	stmt;
			
			if (data.dbase.isOracle ()) {
				stmt =	"DELETE FROM " + table + " recv " +
					"WHERE user_type = 'W' AND NOT EXISTS (" +
					"	SELECT 1 FROM " + data.priorityTable + " prio WHERE prio.customer_id = recv.customer_id AND prio.mailing_id = :mailingID AND prio.status_id = :statusID AND time_id = :timeID" +
					")";
			} else {
				stmt =	"DELETE FROM " + table + " " +
					"WHERE user_type = 'W' AND customer_id NOT IN (" +
					"      SELECT customer_id FROM " + data.priorityTable + " WHERE mailing_id = :mailingID AND status_id = :statusID AND time_id = :timeID" +
					")";
			}
			data.logging (Log.DEBUG, "bc", "Deleting recipient due to priority");
			try {
				cnt = data.dbase.update (stmt, "mailingID", data.mailing.id (), "statusID", data.maildropStatus.id (), "timeID", data.priorityTimeID);
				remain = countTable (table, "distinct customer_id", "user_type = 'W'");
				data.logging (Log.INFO, "bc", "Removed " + cnt + " recpients due to priority, remaining " + remain + " world recipients");
			} catch (Exception e) {
				data.logging (Log.ERROR, "bc", "Failed to remove recipients for priority mailings: " + e.toString (), e);
				ok = false;
			}
		}
		return ok;
	}
	
	private boolean updateOptimizedMailGenerationTimestamp () {
		boolean	ok = true;
		
		if (data.omgEnabled ()) {
			String	stmt = "";
			try {
				long		cnt;
				Calendar	now = Calendar.getInstance ();
				int		genstart;
				
				now.setTime (data.currentSendDate);
				genstart = now.get (Calendar.HOUR_OF_DAY) * 60 + now.get (Calendar.MINUTE);
				stmt = "UPDATE " + table + " SET omg = omg - :genstart WHERE omg IS NOT NULL";
				cnt = data.dbase.update (stmt, "genstart", genstart);
				data.logging (Log.INFO, "bc", "Set " + cnt + " recipients optimized offset");
				if (data.maildropStatus.optimizeFor24h ()) {
					int	offset = 0;
					
					if (data.maildropStatus.sendDate () != null) {
						now.setTime (data.maildropStatus.sendDate ());
						offset = now.get (Calendar.HOUR_OF_DAY) * 60 + now.get (Calendar.MINUTE) - genstart;
					}
					stmt = "UPDATE " + table + " SET omg = omg + 24 * 60 WHERE omg < :offset";
					cnt = data.dbase.update (stmt, "offset", offset);
					data.logging (Log.INFO, "bc", "Set " + cnt + " recipients to additional 24h");
				}
			} catch (Exception e) {
				data.logging (Log.ERROR, "bc", "Failed to set direct sending: " + e.toString () + " using " + stmt, e);
				ok = false;
			}
		}
		return ok;
	}
	
	static class Voucher implements ResultSetExtractor <Object> {
		private Data	data;
		private String	retrieveQuery;
		private String	assignQuery;
		private String	currentQuery;
		private String	cleanupQuery;
		private String	name;
		private long	count;
		
		public Voucher (Data nData, String nName, String nRetrieveQuery, String nAssignQuery, String nCurrentQuery, String nCleanupQuery) {
			data = nData;
			name = nName;
			retrieveQuery = nRetrieveQuery;
			assignQuery = nAssignQuery;
			currentQuery = nCurrentQuery;
			cleanupQuery = nCleanupQuery;
			count = 0;
		}
		public String query () {
			return retrieveQuery;
		}
		public long getCount () {
			return count;
		}
		
		@Override
		public Object extractData (ResultSet rset) throws SQLException, DataAccessException {
			try (Connection conn = DBase.DATASOURCE.getConnection ()) {
				boolean			autoCommit = conn.getAutoCommit ();
				
				try {
					conn.setAutoCommit (false);
					try (PreparedStatement assignPrep = conn.prepareStatement (assignQuery);
					     PreparedStatement currentPrep = currentQuery != null ? conn.prepareStatement (currentQuery) : null;
					     PreparedStatement cleanupPrep = cleanupQuery != null ? conn.prepareStatement (cleanupQuery) : null) {
						while (rset.next ()) {
							long	customerID = rset.getLong (1);
							String	current = null;

							if (currentPrep != null) {
								currentPrep.setLong (1, customerID);
								try (ResultSet	temp = currentPrep.executeQuery ()) {
									while (temp.next ()) {
										current = temp.getString (1);
									}
								}
							}
							assignPrep.setLong (1, customerID);
							long	lcount = assignPrep.executeUpdate ();
						
							if ((lcount > 0) && (cleanupPrep != null) && (current != null)) {
								cleanupPrep.setString (1, current);
								cleanupPrep.executeUpdate ();
							}
							count += lcount;
							if (count % 1000 == 0) {
								data.logging (Log.INFO, "bc", "Assigned " + count + " vouchers for " + name);
								conn.commit ();
							}
						}
						data.logging (Log.INFO, "bc", "Finally assigned " + count + " vouchers for " + name);
						conn.commit ();
					}
				} finally {
					conn.setAutoCommit (autoCommit);
				}
			}
			return null;
		}
	}
	private boolean fillMissingVoucherCodes () {
		boolean	ok = true;
		
		if (data.references != null) {
			String	stmt = "";
			try {
				for (Reference r : getReferences ()) {
					if (r.isFullfilled () && r.isVoucher ()) {
						Voucher	voucher;
						String	assignQuery =
							"UPDATE " + r.table () + " " +
							"SET customer_id = ?, assign_date = CURRENT_TIMESTAMP, mailing_id = " + data.mailing.id () + " " +
							"WHERE customer_id IS NULL " + (data.dbase.isOracle () ? "AND rownum = 1" : "LIMIT 1");
						
						if (voucherRenew (r)) {
							voucher = new Voucher (data, r.name (),
									       "SELECT customer_id FROM " + table,
									       assignQuery,
									       "SELECT voucher_code FROM " + r.table () + " WHERE customer_id = ?",
									       "DELETE FROM " + r.table () + " WHERE voucher_code = ?");
						} else {
							voucher = new Voucher (data, r.name (),
									       "SELECT bind.customer_id " +
									       "FROM " + table + " bind " +
									       "WHERE NOT EXISTS (SELECT 1 FROM " + r.table () + " vc WHERE vc.customer_id = bind.customer_id)",
									       assignQuery,
									       null,
									       null);
						}
									       
						long	cnt;
						DBase.Retry <Long>	rt = data.dbase.new Retry <Long> ("voucher", data.dbase, data.dbase.jdbc (voucher.query ())) {
							@Override
							public void execute () throws SQLException {
								jdbc.query (voucher.query (), voucher);
								priv = voucher.getCount ();
							}
						};
						if (data.dbase.retry (rt)) {
							cnt = rt.priv != null ? rt.priv : 0L;
						} else {
							throw rt.error;
						}
						data.logging (Log.INFO, "bc", r.name () + ": set " + cnt + " new vouchers in " + r.table ());
						stmt = "SELECT count(*) FROM " + table + " cust " + r.joinConditionFrom ();
						cnt = data.dbase.queryLong (stmt);
						data.logging (Log.INFO, "bc", r.name () + ": now " + cnt + " vouchers are active in " + r.table () + " for this mailing");
					}
				}
			} catch (Exception e) {
				data.logging (Log.ERROR, "bc", "Failed in filling vouchers: " + e.toString () + " using " + stmt, e);
				ok = false;
			}
		}
		return ok;
	}
	
	private boolean setMissingVoucherCode (long customerID) {
		boolean	ok = true;
		
		if (data.references != null) {
			String	stmt = "";
			
			try {
				for (Reference r : getReferences ()) {
					if (r.isFullfilled () && r.isVoucher ()) {
						long	cnt;
						boolean	assign;
						String	current = null;
						
						if (voucherRenew (r)) {
							stmt = "SELECT voucher_code FROM " + r.table () + " WHERE customer_id = :customerID";
							current = data.dbase.queryString (stmt, "customerID", customerID);
							assign = true;
						} else {
							stmt = "SELECT count(*) FROM " + r.table () + " WHERE customer_id = :customerID";
							cnt = data.dbase.queryLong (stmt, "customerID", customerID);
							assign = cnt == 0;
						}
						if (assign) {
							stmt =
								"UPDATE " + r.table () + " " +
								"SET customer_id = :customerID, assign_date = CURRENT_TIMESTAMP, mailing_id = :mailingID " +
								"WHERE customer_id IS NULL " + (data.dbase.isOracle () ? " AND rownum = 1" : "LIMIT 1");
							cnt = data.dbase.update (stmt, "customerID", customerID, "mailingID", data.mailing.id ());
							if (cnt != 1) {
								data.logging (Log.WARNING, "bc", r.name () + ": no new voucher found (" + cnt + ") for " + r.table ());
							} else {
								data.logging (Log.DEBUG, "bc", r.name () + ": new voucher assigned");
								if (current != null) {
									stmt = "DELETE FROM " + r.table () + " WHERE voucher_code = :voucherCode";
									cnt = data.dbase.update (stmt, "voucherCode", current);
									if (cnt != 1) {
										data.logging (Log.WARNING, "bc", r.name () + ": expected to remove one entry for voucher \"" + current + "\" but removed " + cnt + " entries");
									} else {
										data.logging (Log.DEBUG, "bc", r.name () + ": removed previous voucher \"" + current + "\"");
									}
								}
							}
						} else {
							data.logging (Log.DEBUG, "bc", r.name () + ": voucher already assigned");
						}
					}
				}
			} catch (Exception e) {
				data.logging (Log.ERROR, "bc", "Failed to assing single voucher: " + e.toString () + " using " + stmt, e);
				ok = false;
			}
		}
		return ok;
	}

	private boolean voucherRenew (Reference r) {
		return r.voucherRenew () && (data.maildropStatus.isCampaignMailing () || data.maildropStatus.isRuleMailing () || data.maildropStatus.isOnDemandMailing () || data.maildropStatus.isWorldMailing ());
	}

	private boolean hasReactivationCandidates () {
		Date	oldest = data.ahvOldestEntryToReactivate ();
		String	query = "SELECT count(*) FROM " + data.ahvTable () + " WHERE reactivate IS NOT NULL AND reactivate < :reactivation";
		
		if (oldest != null) {
			query += " AND reactivate >= :oldest";
		}
		try {
			long	count;
			
			if (oldest != null) {
				count = data.dbase.queryLong (query, "reactivation", data.currentSendDate, "oldest", oldest);
			} else {
				count = data.dbase.queryLong (query, "reactivation", data.currentSendDate);
			}
			return count > 0;
		} catch (Exception e) {
			data.logging (Log.ERROR, "ahv", "Failed to query database using \"" + query + "\": " + e.toString (), e);
		}
		return false;
	}
	
	private void processReactivation (String partFromPrepare) {
		if ((partReactivate != null) && (partReactivate.length () > 0) && hasReactivationCandidates ()) {
			String	query = "SELECT cust.customer_id, agn_ahv.bouncecount " +
					"FROM " + partFromPrepare + " INNER JOIN " + data.ahvTable () + " agn_ahv ON (cust.customer_id = agn_ahv.customer_id) " +
					"WHERE agn_ahv.reactivate IS NOT NULL AND agn_ahv.reactivate < :reactivation AND (" + partReactivate + ") " +
					"ORDER BY agn_ahv.reactivate";
			Date	oldest = data.ahvOldestEntryToReactivate ();
			NamedParameterJdbcTemplate
				jdbc = null;
			boolean	dryrun = StringOps.atob (data.company.info ("ahv:dryrun"), false);
			String	logid = dryrun ? "ahv-dryrun" : "ahv";
			
			try {
				List <Map <String, Object>>	rq;
				Map <String, Object>		row;
				String				updateAHV, updateBinding;
				int				reactivated = 0;
				int				limit = -1;

				for (int n = 0; n < 2; ++n) {
					String	temp;
					int	value = -1;
					
					if (n == 0) {
						temp = data.company.info ("ahv:limit-absolute");
						if (temp != null) {
							value = StringOps.atoi (temp, -1);
							if (value == -1) {
								data.logging (Log.ERROR, logid, "Failed to parse absolute limit: " + temp);
							} else {
								data.logging (Log.DEBUG, logid, "Absolute limit: " + value);
							}
						}
					} else if (n == 1) {
						temp = data.company.info ("ahv:limit-percent");
						if (temp != null) {
							double	percent = StringOps.atof (temp, -1.0);
							
							if (percent == 0.0) {
								value = 0;
							} else if (percent > 0.0) {
								double	dvalue;
								long	subscriber;

								if (data.maildropStatus.isCampaignMailing () || data.maildropStatus.isVerificationMailing ()) {
									subscriber = 1;
								} else {
									subscriber = data.dbase.queryLong ("SELECT count(distinct cust.customer_id) FROM " + partFromPrepare + " WHERE " + partCounter);
								}
								dvalue = (subscriber * percent) / 100.0;
								value = (int) dvalue;
								data.logging (Log.DEBUG, logid, "Percent limit of " + percent + "% of " + subscriber + " leads to a reactivation limit of " + dvalue + " (rounded to " + value + (value == 0 ? ", but set to 1" : "") + ")");
								if (value == 0) {
									value = 1;
								}
							} else {
								data.logging (Log.ERROR, logid, "Failed to parse percent limit: " + temp);
							}
						}
					}
					if ((value != -1) && ((limit == -1) || (value < limit))) {
						limit = value;
					}
				}
				if (limit != -1) {
					data.logging (Log.DEBUG, logid, "Limit reactivation to maximum of " + limit + " receiver" + Log.exts (limit));
				} else {
					data.logging (Log.INFO, logid, "No limitation for reactivation set, try to reactivate all ready records");
				}

				if (dryrun) {
					updateAHV = "SELECT count(*) " +
						    "FROM " + data.ahvTable () + " " +
						    "WHERE customer_id = :customerID AND reactivate IS NOT NULL";
					updateBinding = "SELECT count(*) " +
							"FROM " + bindingTable + " " +
							"WHERE customer_id = :customerID AND user_status = " + UserStatus.Bounce.getStatusCode();
				} else {
					updateAHV = "UPDATE " + data.ahvTable () + " " +
						    "SET reactivate = NULL " +
						    "WHERE customer_id = :customerID AND reactivate IS NOT NULL";
					updateBinding = "UPDATE " + bindingTable + " " +
							"SET user_status = " + UserStatus.Active.getStatusCode() + ", user_remark = :userRemark, timestamp = CURRENT_TIMESTAMP " +
							"WHERE customer_id = :customerID AND user_status = " + UserStatus.Bounce.getStatusCode();
				}
				jdbc = data.dbase.request (query);
				if (oldest != null) {
					rq = data.dbase.query (jdbc, query, "reactivation", data.currentSendDate, "oldest", oldest);
				} else {
					rq = data.dbase.query (jdbc, query, "reactivation", data.currentSendDate);
				}
				for (int n = 0; (n < rq.size ()) && ((limit == -1) || (n < limit)); ++n) {
					row = rq.get (n);
					
					long	customerID = data.dbase.asLong (row.get ("customer_id"));
					int	bounceCount = data.dbase.asInt (row.get ("bouncecount"));
					String	userRemark = data.company.info ("ahv:user-remark", bounceCount);
					int	count;
					
					if (dryrun) {
						count = data.dbase.queryInt (updateAHV, "customerID", customerID);
					} else {
						count = data.dbase.update (updateAHV, "customerID", customerID);
					}
					if (count == 1) {
						if (userRemark == null) {
							userRemark = Integer.toString (bounceCount) + ". Hardbounce Validation";
						}
						if (dryrun) {
							count = data.dbase.queryInt (updateBinding, "customerID", customerID);
						} else {
							count = data.dbase.update (updateBinding, "userRemark", userRemark, "customerID", customerID);
						}
						if (count > 0) {
							data.logging (Log.DEBUG, logid, "CustomerID = " + customerID + " reactivated after " + bounceCount + " bounce" + Log.exts (bounceCount));
							++reactivated;
						} else {
							data.logging (Log.WARNING, logid, "Even as customerID = " + customerID + " should be reactivated, updated leads to " + count + " results");
						}
					} else {
						data.logging (Log.WARNING, logid, "Update of AHV for customerID = " + customerID + " leads to " + count + " updates, not one as expected");
					}
				}
				data.logging (Log.INFO, logid, "Reactivated " + reactivated + " bounce" + Log.exts (reactivated));
			} catch (Exception e) {
				data.logging (Log.ERROR, logid, "Failed to reactive bounces: " + e.toString (), e);
			} finally {
				data.dbase.release (jdbc, query);
			}
		}
	}
	
	private String partCustomer (boolean complexQuery) {
		String	rc = null;
		String	prefix = complexQuery ? "cust." : "";

		if (data.maildropStatus.isCampaignMailing () || data.maildropStatus.isVerificationMailing ()) {
			rc = prefix + "customer_id = " + data.campaignCustomerID;
			if (complexQuery && data.maildropStatus.isCampaignMailing () && (partSubselect != null)) {
				rc += " AND (" + partSubselect + ")";
			}
		} else if (data.maildropStatus.isPreviewMailing ()) {
			rc = prefix + "customer_id = " + data.previewCustomerID;
		}
		if ((! data.maildropStatus.isPreviewMailing ()) && (! data.campaignForceSending)) {
			String	more = partUserstatus + " AND " + partMailinglist;

			if (rc != null) {
				rc += " AND " + more;
			} else {
				rc = more;
			}
		}
		return rc;
	}

	private List <Reference> getReferences () {
		if (sortedReferences == null) {
			sortedReferences = new ArrayList<>(data.references.size ());
			
			List <Reference> unsortedReferences = new ArrayList<>(data.references.size ());
			HashSet <String> seen = new HashSet<>();
			for (Reference r : data.references.values ()) {
				if ((r.backReference () == null) || seen.contains (r.backReference ())) {
					sortedReferences.add (r);
					seen.add (r.name ());
				} else {
					unsortedReferences.add (r);
				}
			}
			List <Reference> toRemove = new ArrayList<>();
			
			while (! unsortedReferences.isEmpty ()) {
				toRemove.clear ();
				for (Reference r : unsortedReferences) {
					if (seen.contains (r.backReference ())) {
						sortedReferences.add (r);
						seen.add (r.name ());
						toRemove.add (r);
					}
				}
				for (Reference r : toRemove) {
					unsortedReferences.remove (r);
				}
				
				if (toRemove.isEmpty ()) {
					String[]	unsorted = new String[unsortedReferences.size ()];
					
					for (int n = 0; n < unsortedReferences.size (); ++n) {
						unsorted[n] = unsortedReferences.get (n).name ();
					}
					data.logging (Log.ERROR, "bc", "Failed to resolve all references: " + String.join (", ", unsorted));
					break;
				}
			}
		}
		return sortedReferences;
	}

	private String partClause (String query, String order) {
		String	newOrder = "cust.customer_id";
		
		if (order != null) {
			newOrder = order + ", " + newOrder;
		}
		if (data.references != null) {
			for (Reference r : getReferences ()) {
				if (r.isFullfilled () && r.isMulti ()) {
					String orderBy = r.orderBy ();
					
					if (orderBy != null) {
						newOrder += ", " + orderBy;
					}
				}
			}
		}

		String	rc = "\n" + partFrom;
		
		if (query != null) {
			rc += "\nWHERE " + query;
		}
		rc += "\nORDER BY " + newOrder;
		return rc;
	}
	private String partClause (String query) {
		return partClause (query, null);
	}

	private String partExtend (String source) {
		String	extended = source;
		
		if (data.references != null) {
			for (Reference r : getReferences ()) {
				if (r.isFullfilled ()) {
					extended = "(" + extended + ") LEFT OUTER JOIN " + r.table () + " " + r.name () + " ON (" + r.joinConditionClause () + ")";
				}
			}
		}
		return extended;
	}

	private String partSource () {
		if (data.omgEnabled ()) {
			return "LEFT OUTER JOIN " + data.omgTable () + " agn_omg ON (cust.customer_id = agn_omg.customer_id)";
		}
		return null;
	}
}
