/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.agnitas.backend.dao.BackendLogDAO;
import org.agnitas.util.Log;

/**
 * General parent class for all types of creating mail output
 */
abstract public class MailWriter {
	/**
	 * Reference to configuration
	 */
	protected Data data;
	/**
	 * Collection of all available blocks
	 */
	protected BlockCollection allBlocks;
	/**
	 * Save backend log data
	 */
	protected BackendLogDAO backendLog;
	/**
	 * Boundary to separate text from html part
	 */
	protected String innerBoundary;
	/**
	 * Boundary to separate content from images
	 */
	protected String outerBoundary;
	/**
	 * Boundary to separate attachmantes from rest of mail
	 */
	protected String attachBoundary;
	/**
	 * Start time of writing mail
	 */
	public Date startExecutionTime;
	/**
	 * End time of writing mail
	 */
	public Date endExecutionTime;
	/**
	 * Start time of writing current block
	 */
	public Date startBlockTime;
	/**
	 * End time of writing current block
	 */
	public Date endBlockTime;

	/**
	 * number of mails written
	 */
	public long mailCount;
	/**
	 * max. number of receiver of a single block
	 */
	public long blockSize;
	/**
	 * number of blocks written
	 */
	public long blockCount;
	/**
	 * number of mails written in current block
	 */
	public long inBlockCount;
	/**
	 * number of records written in current block
	 */
	protected long inRecordCount;
	/**
	 * pattern for creating filenames for writing blocks
	 */
	protected String filenamePattern;

	/**
	 * mailtype for the current receiver
	 */
	protected int mailType;
	/**
	 * count real written mails
	 */
	protected long totalMails;

	/**
	 * internal used, if set we need to increase number of created mails
	 */
	protected boolean pending;
	/**
	 * to create unique filenamnes
	 */
	private static long uniqts = 0;
	/**
	 * to create unique filenamnes
	 */
	private static long uniqnr = 0;

	/**
	 * Constructor
	 *
	 * @param data      reference to configuration
	 * @param allBlocks reference to all content blocks
	 */
	public MailWriter(Data data, BlockCollection allBlocks) throws Exception {
		this.data = data;
		this.allBlocks = allBlocks;

		// setup billing interface
		if (data.maildropStatus.isAdminMailing() || data.maildropStatus.isTestMailing() || data.maildropStatus.isWorldMailing()) {
			backendLog = new BackendLogDAO(data.dbase, data.maildropStatus.id(), data.mailing.id(), data.maildropStatus.isWorldMailing());
		} else {
			backendLog = null;
		}
		// create boundaries
		innerBoundary = "-==" + data.boundary() + "INNERB%(sys$boundary)==";
		outerBoundary = "-==" + data.boundary() + "OUTER%(sys$boundary)==";
		attachBoundary = "-==" + data.boundary() + "ATTACH%(sys$boundary)==";
		startExecutionTime = new Date();
		endExecutionTime = null;
		startBlockTime = null;
		endBlockTime = null;

		// set counters
		totalMails = 0;
		mailCount = 0;
		blockSize = 0;
		blockCount = 0;
		inBlockCount = 0;
		inRecordCount = 0;
		filenamePattern = null;
		pending = false;
	}

	/**
	 * Cleanup
	 */
	public void done() throws Exception {
		endBlock();

		if (backendLog != null) {
			data.logging(Log.DEBUG, "writer", "Finalize backend log counter with " + totalMails);
			backendLog.freeze(data.dbase, totalMails);
			backendLog = null;
			data.logging(Log.DEBUG, "writer", "Finalize billing counter done");
		}
		endExecutionTime = new Date();
	}

	/**
	 * Setup everything to start a new block
	 */
	public void startBlock() throws Exception {
		long timestamp, now;

		++blockCount;
		inBlockCount = 0;
		inRecordCount = 0;
		timestamp = data.sendSeconds;
		if (data.mailing.stepping() > 0) {
			if (data.maildropStatus.isCampaignMailing())
				timestamp += data.mailing.stepping() * 60;
			else if (blockCount > data.mailing.startBlockForStep())
				timestamp += (data.mailing.stepping() * 60) * ((blockCount - data.mailing.startBlockForStep()) / data.mailing.blocksPerStep());
		}
		timestamp = data.modifyNextBlockTimestamp(timestamp);
		now = System.currentTimeMillis() / 1000;

		if (timestamp < now)
			timestamp = now;
		data.currentSendDate = new Date(timestamp * 1000);

		String tsstr;
		SimpleDateFormat tmp;

		tmp = new SimpleDateFormat("'D'yyyyMMddHHmmss");
		tsstr = tmp.format(data.currentSendDate);

		String unique;

		if (data.maildropStatus.isCampaignMailing () || data.maildropStatus.isVerificationMailing ()) {
			unique = getUniqueNr (timestamp, 1) +
				"C" + (data.maildropStatus.statusField () != null ? data.maildropStatus.statusField (): "") +
				StringOps.format_number (data.campaignCustomerID, 8);
		} else if (data.maildropStatus.isAdminMailing () || data.maildropStatus.isTestMailing () || data.maildropStatus.isPreviewMailing ()) {
			unique = getUniqueNr (timestamp, 5);
		} else {
			unique = StringOps.format_number(Long.toString(blockCount), 4);
		}
		filenamePattern = "AgnMail" + data.getFilenameDetail () +
				  "=" + tsstr +
				  "=" + data.getFilenameCompanyID () +
				  "=" + data.getFilenameMailingID () +
				  "=" + unique +
				  "=liaMngA";
		data.logging (Log.INFO, "writer", "Start block " + blockCount + " using blocksize " + blockSize);
		startBlockTime = new Date ();
	}

	/**
	 * Mark everything for ending the block
	 */
	public void endBlock() throws Exception {
		endBlockTime = new Date();
		data.logging(Log.INFO, "writer", "End block " + blockCount);
	}

	/**
	 * Check if we need to create a new block
	 *
	 * @param force force start of a new block
	 */
	public void checkBlock(boolean force) throws Exception {
		if (data.forceNewBlock) {
			force = true;
			data.forceNewBlock = false;
		}
		if (blockCount == 0) {
			startBlock();
		} else if ((blockSize > 0) && (force || (inBlockCount >= blockSize) || (inRecordCount >= blockSize))) {
			endBlock();
			startBlock();
		}
	}

	/**
	 * When a mail is written, increase counter
	 */
	public void writeMailDone() throws Exception {
		if (pending) {
			pending = false;
			++mailCount;
			++inBlockCount;
		}
	}

	/**
	 * Write information for a single receiver
	 *
	 * @param cinfo        Information about the customer
	 * @param mcount       if more than one mail is written for this receiver
	 * @param mailtype     the mailtype for this receiver
	 * @param icustomer_id the customer ID
	 * @param tagNames     the available tags
	 */
	public void writeMail (Custinfo cinfo,
			       int mcount, int mailtype, long icustomer_id,
			       String mediatypes, String userStatuses,
			       Map <String, EMMTag> tagNames) throws Exception {
		writeMailDone ();
		mailType = mailtype;
		checkBlock(false);
		pending = true;
	}

	public void writeContent(Custinfo cinfo, long icustomer_id, Map<String, EMMTag> tagNames, Column[] rmap) throws Exception {
		if (!pending) {
			throw new Exception("Got content request even if no receiver active");
		}
	}

	public long getInBlockCount() {
		return inBlockCount;
	}

	/**
	 * Create a unique number based on given timestamp
	 *
	 * @param ts current timestamp
	 * @return unique number as string
	 */
	private synchronized String getUniqueNr(long ts, int minLength) {
		if (uniqts == ts)
			++uniqnr;
		else {
			uniqts = ts;
			uniqnr = 1;
		}
		return StringOps.format_number(Long.toString(uniqnr), minLength);
	}
}
