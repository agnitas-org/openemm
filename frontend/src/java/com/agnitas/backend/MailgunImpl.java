/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.backend.exceptions.CancelException;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.util.Bit;
import com.agnitas.util.Log;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * Central control class for generating mails
 */
public class MailgunImpl implements Mailgun {
	/**
	 * Reference to configuration
	 */
	protected Data data;
	/**
	 * All content blocks
	 */
	protected BlockCollection allBlocks = null;
	/**
	 * All tags for this mailing
	 */
	protected Map<String, EMMTag> tagNames = null;
	protected MediaMap mmap;
	/**
	 * The blocklist information for this mailing
	 */
	protected Blocklist blist = null;
	/**
	 * Query for normal selection
	 */
	protected String selectQuery = null;
	/**
	 * Query for the world part selection
	 */
	protected String wSelectQuery = null;

	/**
	 * Constructor
	 * must be followed by initialize ()
	 */
	public MailgunImpl() {
		data = null;
	}

	/**
	 * Cleanup
	 */
	@Override
	public void done() throws Exception {
		if (data != null) {
			try {
				data.done();
			} catch (Exception e) {
				data = null;
				throw e;
			}
			data = null;
		}
	}

	/**
	 * Initialize internal data
	 *
	 * @param status_id the string version of the statusID to use
	 */
	@Override
	public void initialize(String status_id, Map <String, Object> opts) throws Exception {
		data = new Data ("mailout");
		try {
			data.setup (status_id, opts);
		} catch (CancelException e) {
			throw e;
		} catch (Exception e) {
			done();
			throw e;
		} finally {
			if (data != null) {
				data.suspend();
			}
		}
	}
	@Override
	public void initialize(String status_id) throws Exception {
		initialize(status_id, null);
	}

	/**
	 * Setup a mailout without starting generation
	 *
	 * @param opts options to control the setup beyond DB information
	 */
	@Override
	public void prepare(Map<String, Object> opts) throws Exception {
		try {
			doPrepare(opts);
		} catch (Exception e) {
			if (data != null) {
				data.suspend();
			}
			throw e;
		}
	}

	/**
	 * Execute an already setup mailout
	 *
	 * @param opts options to control the execution beyond DB information
	 */
	@Override
	public void execute(Map<String, Object> opts) throws Exception {
		synchronized (this) {
			try {
				doExecute(opts);
			} catch (Exception e) {
				if (data != null) {
					data.suspend();
				}
				throw e;
			}
		}
	}

	/**
	 * Full execution of a mail generation
	 *
	 * @param custid optional customer id
	 * @return Status string
	 */
	@Override
	public String fire(String status_id, Map<String, Object> opts) throws Exception {
		String str;

		str = null;
		try {
			initialize (status_id, opts);
			data.logging(Log.INFO, "mailout", "Starting up");
			doPrepare(opts);
			doExecute(opts);
			str = "Success: Mailout fired.";
		} catch (CancelException e) {
			str = e.message ();
			data.logging (Log.WARNING, "mailout", "Creation has canceled due to " + str);
			data.mailing.setWorkStatus (e.getMailingStatus ());
			data.updateGenerateionStateForCancel ();
		} catch (Exception e) {
			if (data != null) {
				data.logging(Log.ERROR, "mailout", "Creation failed: " + e.toString(), e);
				if (data.mailing.id() > 0) {
					try (Destroyer d = new Destroyer (data.mailing.id ())) {
						data.logging(Log.INFO, "mailout", "Try to remove failed mailing: " + e);
						data.logging(Log.INFO, "mailout", "Remove already generated files lead to " + d.destroy ());
					}
				}
			}
			try {
				done();
			} catch (Exception temp) {
				if (data != null) {
					data.logging(Log.ERROR, "mailout", "Failed to finalize mailout (after failure): " + temp.toString(), temp);
				}
			}
			throw e;
		}
		data.logging(Log.INFO, "mailout", "Execution done: " + str);
		try {
			done();
		} catch (Exception e) {
			data.logging(Log.ERROR, "mailout", "Failed to finalize mailout: " + e.toString(), e);
		}
		return str;
	}

	/**
	 * Prepare the mailout
	 *
	 * @param opts options to control the setup beyond DB information
	 */
	private void doPrepare(Map<String, Object> opts) throws Exception {
		data.resume();
		data.options(opts, 1);
		
		data.logging(Log.DEBUG, "prepare", "Starting firing");
		// create new Block collection and store in member var if not already done by subclass
		allBlocks = new BlockCollection();
		data.setBlocks(allBlocks);
		allBlocks.setupBlockCollection(data, data.previewInput);
		
		data.logging(Log.DEBUG, "prepare", "Parse blocks");
		// read all tag names contained in the blocks into Hashtable
                // - read selectvalues and store in EMMTag associated with tag name in Hashtable
		tagNames = allBlocks.parseBlocks();
		data.setUsedFieldsInLayout(allBlocks, tagNames);

		readBlocklist();
		data.suspend();
	}

	/**
	 * Execute a prepared mailout
	 *
	 * @param opts options to control the execution beyond DB information
	 */
	protected void doExecute(Map<String, Object> opts) throws Exception {
		data.resume();
		data.options(opts, 2);
		data.sanityCheck(blist);

		// get constructed selectvalue based on tag names in Hashtable
		data.startExecution();
		selectQuery = getSelectvalue(tagNames, false);
		wSelectQuery = getSelectvalue(tagNames, true);

		MailWriter mailer = new MailWriterMeta(data, allBlocks, tagNames);
		List<EMMTag> email_tags = new ArrayList<>();
		int email_count = 0;
		boolean hasOverwriteData = false;
		boolean hasVirtualData = false;

		for (EMMTag tag : tagNames.values()) {
			if (!tag.globalValue) {
				if ((tag.tagType == EMMTag.TAG_INTERNAL) && (tag.tagSpec == EMMTag.TI_EMAIL)) {
					email_tags.add(tag);
					email_count++;
				} else if ((tag.tagType == EMMTag.TAG_INTERNAL) && (tag.tagSpec == EMMTag.TI_DBV)) {
					hasVirtualData = true;
					data.initializeVirtualData(tag.mSelectString);
				}
			}
		}

		hasOverwriteData = data.overwriteData();

		try {
			data.logging(Log.INFO, "execute", "Start creation of mails");

			boolean needSamples = Bit.isset(data.availableMedias, Media.TYPE_EMAIL) && (data.maildropStatus.isTestMailing() || data.maildropStatus.isWorldMailing());
			List<String> clist = data.generationClauses();
			Set<Long> seen = prepareCollection();
			boolean multi = data.useMultipleRecords();
			Custinfo cinfo;
			Extractor ex;

			data.prefillRecipients(seen);
			data.logging(Log.DEBUG, "execute", (multi ? "Multi" : "Single") + " record mode selected");
			cinfo = new Custinfo(data);
			if (data.providerEmail != null) {
				cinfo.setProviderEmail(data.providerEmail);
			}
			ex = new Extractor (tagNames, mmap, data, cinfo,
					    blist, mailer,
					    needSamples, seen, multi,
					    hasOverwriteData, hasVirtualData,
					    email_tags, email_count);

			for (int state = 0; state < clist.size(); ++state) {
				String clause = clist.get(state);
				if (clause == null) {
					continue;
				}

				String query = (state == 0 ? selectQuery : wSelectQuery) + " " + clause;

				if ((state == 1) && (seen.size() > 0)) {
					data.mailing.increaseStartblockForStepping();
				}
				mailer.checkBlock((mailer.blockSize > 0) && (mailer.inBlockCount > 0));

				data.dbase.cursor(query).query(query, null, ex);

				mailer.writeMailDone();
			}
			data.correctReceiver();
		} catch (CancelException e) {
			throw e;
		} catch (Exception e) {
			data.updateGenerationState(4);
			data.suspend();
			throw new Exception("Error during main query or mail generation:" + e.toString(), e);
		}
		mailer.done();
		if (!data.maildropStatus.isPreviewMailing()) {
			finalizeMailingToDatabase(mailer);
		}
		data.endExecution();
		data.updateGenerationState();

		data.logging(Log.DEBUG, "execute", "Successful end");
		data.suspend();
	}

	/**
	 * Retrieve blocklist from database
	 */
	private void retrieveBlocklist () throws Exception {
		blist.setBouncelog (data.mailing.bounceLogfile ());

		List<String> blocklistTables = new ArrayList<>();
		int isLocal;

		if (Data.emm) {
			blocklistTables.add("cust_ban_tbl");
		}
		blocklistTables.add("cust" + data.company.id() + "_ban_tbl");
		isLocal = blocklistTables.size() - 1;

		data.logging (Log.INFO, "readblist", "Using simplified blocklist wildcard matching");
		for (int blocklistIndex = 0; blocklistIndex < blocklistTables.size(); ++blocklistIndex) {
			String table = blocklistTables.get(blocklistIndex);
			List<Map<String, Object>> rq;
			Map<String, Object> row;

			try {
				rq = data.dbase.query("SELECT email FROM " + table);
				for (int n = 0; n < rq.size(); ++n) {
					row = rq.get(n);

					String email = data.dbase.asString(row.get("email"));
					if (email != null) {
						blist.add(email, blocklistIndex != isLocal);
					}
				}
			} catch (Exception e) {
				data.logging(Log.FATAL, "readblist", "Failed reading blocklist table " + table + ": " + e);
				throw new Exception("Failed reading blocklist table " + table + " (" + e.toString() + ")", e);
			}
		}
	}

	/**
	 * Read in the global and local blocklist
	 */
	protected void readBlocklist() throws Exception {
		blist = new Blocklist(data.log, data.company.allowUnnormalizedEmails ());
		if (!data.maildropStatus.isPreviewMailing()) {
			try {
				retrieveBlocklist();
			} catch (Exception e) {
				data.logging (Log.FATAL, "readblist", "Unable to get blocklist: " + e.toString (), e);
				throw new Exception ("Unable to get blocklist: " + e.toString (), e);
			}

			data.logging (Log.INFO, "readblist", "Found " + blist.globalCount () + " entr" + Log.exty (blist.globalCount ()) + " in global blocklist, " +
				      blist.localCount () + " entr" + Log.exty (blist.localCount ()) + " in local blocklist");
		}
	}

	protected static class ExtractorMediaMap implements ResultSetExtractor<Object> {
		private MediaMap mmap;

		public ExtractorMediaMap(MediaMap mmap) {
			this.mmap = mmap;
		}

		@Override
		public Object extractData(ResultSet rset) throws SQLException {
			while (rset.next()) {
				mmap.add(rset.getLong(1), rset.getInt (2), rset.getInt(3));
			}
			return null;
		}
	}

	/**
	 * Prepare collection of customers
	 *
	 * @return a hashset for already seen customers
	 */
	protected Set<Long> prepareCollection() throws Exception {
		String bindQuery = data.getBindingQuery();

		mmap = new MediaMap(data);
		data.dbase.cursor(bindQuery).query(bindQuery, null, new ExtractorMediaMap(mmap));
		return new HashSet<>();
	}

	/**
	 * Write final data to database
	 */
	private static Map<Long, Object> postlock = new HashMap<>();

	protected void finalizeMailingToDatabase(MailWriter mailer) {
		//
		// EMM-4150 & EMM-7151
		if (data.maildropStatus.isAdminMailing () || data.maildropStatus.isTestMailing () || data.maildropStatus.isWorldMailing ()) {
			data.mailing.setWorkStatus (data.totalReceivers == 0 ? MailingStatus.NORECIPIENTS : MailingStatus.GENERATION_FINISHED, MailingStatus.IN_GENERATION);
		} else if (data.maildropStatus.isRuleMailing () || data.maildropStatus.isOnDemandMailing () || data.maildropStatus.isCampaignMailing ()) {
			data.mailing.setWorkStatus (MailingStatus.ACTIVE, MailingStatus.CANCELED, MailingStatus.INSUFFICIENT_VOUCHERS);
		}
		data.toMailtrack();
		//
		// EMM-2952 & BAUR-790
		String	profileUpdateBaseStatement = null;
		if ((! data.company.mailtracking ()) || (! data.company.mailtrackingExtended ())) {
			//
			// 1.) AGNEMM-2952: Set last sent date, if applicated
			List<String> profileUpdates = new ArrayList<>();

			String lastsendDateColumn = "lastsend_date";

			if (data.columnByName(lastsendDateColumn) != null) {
				profileUpdates.add("cust." + lastsendDateColumn + " = CURRENT_TIMESTAMP");
			} else {
				data.logging(Log.INFO, "execute", "Column " + lastsendDateColumn + " not set as not present in table");
			}
			//
			// 2.) BAUR-790: Update frequency counter
			if (data.mailinglist.frequencyCounterEnabled () && (! data.mailing.frequencyCounterDisabled ())) {
				for (String column : new String[] { "freq_count_day", "freq_count_week", "freq_count_month" }) {
					if (data.columnByName (column) != null) {
						profileUpdates.add ("cust." + column + " = COALESCE(cust." + column + " + 1, 1)");
					}
				}
			}
			//
			// X.) Add statement, if there are customer profile columns to update
			if (profileUpdates.size () > 0) {
				profileUpdateBaseStatement = 
					"UPDATE customer_" + data.company.id () + "_tbl cust " +
					"SET " + profileUpdates.stream ().reduce ((s, e) -> s + ", " + e).orElse (null);
			}
		}
		
		Map<String, String> extra = new HashMap<>();
		long chunks = data.limitBlockChunks();

		for (long chunk = 0; chunk < chunks; ++chunk) {
			String where;

			if (chunks == 1) {
				where = data.createSimpleClause();
			} else {
				where = data.createSimpleClause("mod(cust.customer_id, " + chunks + ") = " + chunk);
			}
			if (where == null) {
				if (chunks == 1) {
					data.logging(Log.INFO, "execute", "No post execution as no recipients found");
				}
				continue;
			}

			extra.put("where", where);

			List<String> postexecs = new ArrayList<>();

			if (data.maildropStatus.isCampaignMailing () ||
			    data.maildropStatus.isRuleMailing () ||
			    data.maildropStatus.isOnDemandMailing () ||
			    data.maildropStatus.isWorldMailing ()) {
				//
				// 1.) profile updates, if available
				if (profileUpdateBaseStatement != null) {
					postexecs.add (profileUpdateBaseStatement + " WHERE " + where);
				}
				//
				// 2.) If optional post execution is set in database
				String postexec = data.company.infoSubstituted("post-execute", extra);

				if ((postexec != null) && (postexec.length() > 0)) {
					postexecs.add(postexec);
				}
			}

			synchronized (postlock) {
				if (!postlock.containsKey(data.company.id())) {
					postlock.put(data.company.id(), new Object());
				}
			}
			for (String stmt : postexecs) {
				boolean success = false;
				int retry = 3;

				while ((!success) && (retry-- > 0)) {
					try {
						synchronized (postlock.get(data.company.id())) {
							try (DBase.With with = data.dbase.with (stmt)) {
								if (stmt.toLowerCase().startsWith("update ")) {
									data.dbase.update(with.cursor (), stmt);
								} else {
									data.dbase.execute(with.cursor (), stmt);
								}
							}
						}
						success = true;
					} catch (Exception e) {
						data.logging(Log.ERROR, "execute", "Failed to execute post-execute \"" + stmt + "\": " + e.toString() + (retry > 0 ? ", retry execution" : ""), e);
					}
				}
			}
		}
	}

	/**
	 * Optional add database hint
	 *
	 * @return the hint
	 */
	private String getHint() {
		String rc = null;

		if (data.dbase.isOracle()) {
			rc = data.company.infoSubstituted("oracle-hint");
			if (rc == null) {
				rc = "FULL(" + data.getBindingTable() + ")";
			}
			if (rc != null) {
				if (rc.length() > 0) {
					rc = "/* " + rc + " */ ";
				}
			}
		}
		return rc != null ? rc : "";
	}

	/**
	 * Build the complete big query
	 *
	 * @param tagNamesParameter the tags
	 * @return the created query
	 */
	protected String getSelectvalue(Map<String, EMMTag> tagNamesParameter, boolean hint) {
		StringBuffer selectString = new StringBuffer();

		selectString.append(data.getSelectExpression(tagNamesParameter != null));
		selectString.append(" ");

		if (hint) {
			String hstr = getHint();

			if (hstr.length() > 0) {
				selectString.append(hstr);
			}
		}
		if (tagNamesParameter != null) {
			// if changing this list, ensure to set "Extractor.reservedColumns" to the amount of
			// columns which are here directly referenced
			selectString.append("cust.customer_id, bind.user_status, bind.user_type, cust.mailtype");
			// append all select string values of all tags
			for (EMMTag current_tag : tagNamesParameter.values()) {
				if ((!current_tag.globalValue) && (current_tag.tagType == EMMTag.TAG_DBASE)) {
					selectString.append(", " + current_tag.mSelectString);
				}
			}
			if (data.lusecount > 0) {
				for (int n = 0; n < data.lcount; ++n) {
					Column c = data.columnByIndex(n);

					if (c.getInuse()) {
						selectString.append(", ");
						selectString.append(c.getRef() == null ? "cust" : c.getRef());
						selectString.append(".");
						selectString.append(c.getName());
					}
				}
			}
			data.getControlColumns(selectString);
		} else {
			selectString.append("count(distinct customer_id)");
		}
		// turn stringbuffer into string
		String result = selectString.toString();

		data.logging(Log.DEBUG, "selectvalue", "SQL-String: " + result);

		return result;
	}

	/* DO NOT REMOVE THIS METHOD */
	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			throw new Exception("Missing statusID");
		}

		String status_id = args[0];
		Map<String, Object> opts = new HashMap <> ();

		if (args.length > 1) {
			/*
			Map <String, Object>	stc = new HashMap <> ();
			stc.put ("_tg", "35421,1234");
			opts.put ("static", stc);
			 */

			for (int n = 1; n < args.length; ++n) {
				int pos = args[n].indexOf('=');

				if (pos > 0) {
					String var = args[n].substring(0, pos).trim();
					String val = args[n].substring(pos + 1).trim();

					if (val.indexOf(',') != -1) {
						String[] parts = val.split(", *");
						List<String> nval = new ArrayList<>(parts.length);

						for (String part : parts) {
							nval.add(part);
						}
						opts.put(var, nval);
					} else if (var.equals("overwrite") || var.equals("virtual")) {
						Map<String, String> nval = new HashMap<>();

						for (String pair : val.split("; *")) {
							String[] element = pair.split(": *", 2);

							if (element.length == 2) {
								nval.put(element[0], element[1]);
							}
						}
						opts.put(var, nval);
					} else {
						opts.put(var, val);
					}
				}
			}
		}
		MailgunImpl mailout = null;
		try {
			mailout = new MailgunImpl();
			mailout.initialize(status_id, opts);
			mailout.prepare(opts);
			mailout.execute(opts);
			mailout.done();
		} finally {
			if (mailout != null) {
				mailout.done();
			}
		}
	}
}
