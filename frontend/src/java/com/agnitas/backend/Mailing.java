/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agnitas.backend.dao.MailingDAO;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.util.Log;
import com.agnitas.util.Str;
import com.agnitas.util.Systemconfig;

/**
 * Thes class keeps track of all mailing related
 * configurations
 */
public class Mailing {
	/**
	 * refrence to global configuration
	 */
	private Data data;
	/**
	 * the mailing_id
	 */
	private long id;
	/**
	 * database representtion for mailing
	 */
	private MailingDAO mailing;
	/**
	 * the name of the mailing
	 */
	@SuppressWarnings("unused")
	private String name;
	/**
	 * default encoding for all blocks
	 */
	private String defaultEncoding;
	/**
	 * default character set for all blocks
	 */
	private String defaultCharset;
	/**
	 * used block size
	 */
	private int blockSize;
	/**
	 * minimal/maximum dynamic blocksize and divisor
	 */
	private int dynBlockSizeMin;
	private int dynBlockSizeMax;
	private int dynBlockDivisor;
	/**
	 * steps in seconds to delay sending of each blocksize
	 */
	private int stepping;
	/**
	 * number of blocks per entity
	 */
	private int blocksPerStep;
	/**
	 * start stepping at this block
	 */
	private int startBlockForStep;
	/**
	 * maximum number of bytes per output file
	 */
	private long maxBytesPerOutputFile;
	/**
	 * the subject for this mailing
	 */
	private String subject;

	private String preHeader;

	/**
	 * the sender address for this mailing
	 */
	private EMail fromEmail;
	/**
	 * the optional reply-to address for this mailing
	 */
	private EMail replyTo;
	/**
	 * the envelope address
	 */
	private EMail envelopeFrom;
	private boolean envelopeForced;
	/**
	 * the encoding for this mailing
	 */
	private String encoding;
	/**
	 * the charachter set for this mailing
	 */
	private String charset;
	/**
	 * domain used to build message-ids
	 */
	private String domain;
	/**
	 * boundary part to build multipart messages
	 */
	private String boundary;
	/**
	 * content of the X-Mailer: header
	 */
	private String mailer;
	/**
	 * output directories for admin and test sendings
	 */
	private Map<String, String> outputDirectories;
	/**
	 * base path to spool directory to build dynamically not configured paths
	 */
	private String spoolDirectory;
	/**
	 * path to accounting logfile
	 */
	private String accountLogfile;
	/**
	 * path to bounce logfile
	 */
	private String bounceLogfile;
	/**
	 * path to mailtrack logfile
	 */
	private String mailtrackLogfile;
	/**
	 * name of system MTA
	 */
	private String messageTransferAgent;
	/**
	 * number of receivers for dynamic blocksize calculation
	 */
	private long receiverCount;

	public Mailing(Data nData) {
		data = nData;
		defaultEncoding = "quoted-printable";
		defaultCharset = "UTF-8";
		blockSize = 0;
		dynBlockSizeMin = 50;
		dynBlockSizeMax = 1000;
		dynBlockDivisor = 500;
		stepping = 0;
		blocksPerStep = 1;
		startBlockForStep = 1;
		domain = Systemconfig.fqdn;
		boundary = "AGNITAS";
		mailer = "Agnitas AG";
		outputDirectories = new HashMap<>();
		spoolDirectory = Str.makePath ("$home", "var", "spool");
		accountLogfile = Str.makePath ("$home", "log", "account.log");
		bounceLogfile = Str.makePath ("$home", "log", "extbounce.log");
		mailtrackLogfile = Str.makePath ("$home", "log", "mailtrack.log");
		outputDirectories.put ("meta", spool ("META"));
		outputDirectories.put ("direct", spool ("DIRECT", "META"));
		outputDirectories.put ("mail", spool ("mailer"));
		outputDirectories.put ("deleted", spool ("DELETED"));
	}
	
	public Mailing done() {
		return null;
	}

	public boolean exists() {
		return mailing != null;
	}

	public long id() {
		return id;
	}

	public void id(long nId) throws SQLException {
		id = nId;
		mailing = new MailingDAO(data.dbase, id);
		if (mailing.mailingID() == 0L) {
			mailing = null;
		} else {
			if (Data.emm) {
				mailing.retrieveItems (data.dbase);
			}
		}
	}
	
	public long findMailingByName (String mailingName, long companyID) throws SQLException {
		return exists () ? mailing.findMailingByName (data.dbase, mailingName, companyID) : 0L;
	}

	public long mailingID() {
		return exists() ? mailing.mailingID() : 0L;
	}

	public long companyID() {
		return exists() ? mailing.companyID() : 0L;
	}

	public long mailinglistID() {
		return exists() ? mailing.mailinglistID() : 0L;
	}

	public long mailtemplateID() {
		return exists() ? mailing.mailtemplateID() : 0L;
	}

	public String name() {
		return exists() ? mailing.shortName() : null;
	}
	
	public String description() {
		return exists() ? mailing.description() : null;
	}

	public Date creationDate() {
		return exists() ? mailing.creationDate() : null;
	}

	public boolean deleted() {
		return exists() && mailing.deleted();
	}

	public String targetExpression() {
		return exists() ? mailing.targetExpression() : null;
	}

	public long splitID() {
		return exists() ? mailing.splitID() : 0L;
	}
	
	public long deliveryRestrictID () {
		return exists () ? mailing.deliveryRestrictID () : 0L;
	}

	public int mailingType() {
		return exists() ? mailing.mailingType() : 0;
	}

	public String workStatus() {
		return exists() ? mailing.workStatus() : null;
	}

	public void setWorkStatus(MailingStatus newWorkStatus, MailingStatus ... oldWorkStatuses) {
		if (exists ()) {
			String currentWorkStatus = workStatus();
			try {
				if (mailing.workStatus(data.dbase, newWorkStatus, oldWorkStatuses)) {
					data.logging(Log.INFO, "workstatus", "Updated working status from " + currentWorkStatus + " to " + newWorkStatus.getDbKey ());
				}
			} catch (Exception e) {
				data.logging(Log.ERROR, "workstatus", "Failed to update working status from " + currentWorkStatus + " to " + newWorkStatus.getDbKey () + " due to " + e.toString(), e);
			}
		}
	}

	public Date planDate () {
		return exists () ? mailing.planDate () : null;
	}

	public int priority() {
		return exists() ? mailing.priority() : -1;
	}
	
	public String contentType () {
		return exists () ? mailing.contentType () : null;
	}
	
	public boolean frequencyCounterDisabled() {
		return exists() && mailing.frequencyCounterDisabled();
	}

	public boolean isWorkflowMailing() {
		return exists() && mailing.isWorkflowMailing();
	}

	public List<Media> media() {
		return exists() ? mailing.media() : null;
	}

	public Map<String, String> info() {
		return exists() ? mailing.info() : null;
	}

	public Map<String, String> item() {
		return exists() ? mailing.item() : null;
	}

	public long sourceTemplateID() {
		return exists() ? mailing.sourceTemplateID() : 0L;
	}

	public int sourceTemplatePriority() {
		return exists() ? mailing.sourceTemplatePriority() : 0;
	}

	public String defaultEncoding() {
		return defaultEncoding;
	}

	public String defaultCharset() {
		return defaultCharset;
	}

	public int blockSize() {
		return blockSize > 0 ? blockSize : (int) Long.min (dynBlockSizeMax, Long.max (dynBlockSizeMin, receiverCount / dynBlockDivisor));
	}

	public void blockSize(int nBlockSize) {
		if ((nBlockSize > 0) && (nBlockSize != blockSize)) {
			blockSize = nBlockSize;
			blocksPerStep = 1;
		}
	}
	
	public void dynBlockSizeMin (int newSize) {
		if (newSize > 0) {
			dynBlockSizeMin = newSize;
		}
	}
	
	public void dynBlockSizeMax (int newSize) {
		if (newSize > 0) {
			dynBlockSizeMax = newSize;
		}
	}
	
	public void dynBlockDivisor (int newSize) {
		if (newSize > 0) {
			dynBlockDivisor = newSize;
		}
	}

	public int stepping() {
		return stepping;
	}

	public void stepping(int nStepping) {
		if ((nStepping >= 0) && (nStepping != stepping)) {
			stepping = nStepping;
		}
	}

	public int blocksPerStep() {
		return blocksPerStep;
	}

	public int startBlockForStep() {
		return startBlockForStep;
	}

	public long maxBytesPerOutputFile() {
		return maxBytesPerOutputFile;
	}

	public String subject() {
		return subject;
	}

	public void subject(String nSubject) {
		subject = nSubject;
	}

	public void setPreHeader(String preHeader) {
		this.preHeader = preHeader;
	}

	public String getPreHeader() {
		return preHeader;
	}

	public EMail fromEmail() {
		return fromEmail;
	}

	public void fromEmail(EMail nFromEmail) {
		fromEmail = nFromEmail;
	}

	public EMail replyTo() {
		return replyTo;
	}

	public void replyTo(EMail nReplyTo) {
		replyTo = nReplyTo;
	}

	public EMail envelopeFrom() {
		return envelopeFrom;
	}
	
	public boolean envelopeForced () {
		return envelopeForced;
	}

	public void envelopeFrom(EMail nEnvelopeFrom) {
		envelopeFrom = nEnvelopeFrom;
		envelopeForced = envelopeFrom != null;
	}

	public String encoding() {
		return encoding;
	}

	public void encoding(String nEncoding) {
		encoding = nEncoding;
	}

	public String charset() {
		return charset;
	}

	public void charset(String nCharset) {
		charset = nCharset;
	}

	public String domain() {
		return domain;
	}

	public void domain(String nDomain) {
		domain = nDomain;
	}

	public String boundary() {
		return boundary;
	}

	public void boundary(String nBoundary) {
		boundary = nBoundary;
	}

	public String mailer() {
		return mailer;
	}

	public void mailer(String nMailer) {
		mailer = nMailer;
	}

	public String accountLogfile() {
		return accountLogfile;
	}

	public String bounceLogfile() {
		return bounceLogfile;
	}
	
	public String mailtrackLogfile() {
		return mailtrackLogfile;
	}
	
	public String messageTransferAgent() {
		return messageTransferAgent;
	}
	
	public void receiverCount (long newReceiverCount) {
		receiverCount = newReceiverCount;
	}

	/**
	 * increase starting block
	 */
	public void increaseStartblockForStepping() {
		++startBlockForStep;
	}

	/**
	 * checks if sender email is valid
	 */
	public boolean fromEmailIsValid() {
		return fromEmail != null && fromEmail.valid(false);
	}

	/**
	 * Get full sender email expression, if set, null otherwise
	 *
	 * @return the senders email addresse, if set
	 */
	public String getFromEmailFull() {
		return fromEmail != null ? fromEmail.full : null;
	}

	/**
	 * Get sender email suitable for header
	 *
	 * @return the header version fo the sender
	 */
	public String getFromEmailForHeader() {
		if (fromEmail == null) {
			return null;
		} else if (!fromEmail.full.equals(fromEmail.pure)) {
			return fromEmail.full_puny;
		} else {
			return "<" + fromEmail.full_puny + ">";
		}
	}

	/**
	 * Get reply to for header, if different from sender email
	 *
	 * @return the header version for reply to
	 */
	public String getReplyToForHeader() {
		if (replyTo != null && replyTo.valid(true) && ((fromEmail == null) || (!fromEmail.full.equals(replyTo.full)))) {
			return replyTo.full_puny;
		}
		return null;
	}

	/**
	 * Set envelope from, if not already defined to fromEmail
	 */
	public void setEnvelopeFrom() {
		if (envelopeFrom == null) {
			envelopeFrom = fromEmail;
		}
	}

	/**
	 * Get envelope address
	 *
	 * @return the punycoded envelope address
	 */
	public String getEnvelopeFrom() {
		String temp, env;

		env = null;
		if ((temp = data.company.info("envelope-from", id)) != null) {
			env = (new EMail (temp)).pure_puny;
		}
		if ((env == null) && (envelopeFrom != null)) {
			env = envelopeFrom.pure_puny;
		}
		if (env == null) {
			env = fromEmail != null ? fromEmail.pure_puny : null;
		}
		return env;
	}

	/**
	 * Set encoding, if not already definied
	 */
	public void setEncoding() {
		if ((encoding == null) || (encoding.length() == 0)) {
			encoding = defaultEncoding;
		}
	}

	/**
	 * Set charset, if not already definied
	 */
	public void setCharset() {
		if ((charset == null) || (charset.length() == 0)) {
			charset = defaultCharset;
		}
	}

	/**
	 * Check if the we need a dedicated mailerset for this mailer
	 */
	public void checkMailerset () {
		if (exists ()) {
			String	mailerset = data.sendEncrypted ? "crypt" : null;
			try {
				mailing.mailerset (data.dbase, mailerset);
			} catch (SQLException e) {
				data.logging (Log.ERROR, "mailing", "Failed to set new mailerset " + (mailerset != null ? mailerset : "*default*") + " for mailing " + id + ": " + e);
			}
		}
	}
	
	/**
	 * returns the X-Mailer: header content
	 *
	 * @return mailer name
	 */
	public String makeMailer() {
		if ((mailer != null) && (data.company.name() != null)) {
			return StringOps.replace(mailer, "[agnMANDANT]", data.company.name());
		}
		return mailer;
	}

	/**
	 * returns an output directory for a given name
	 *
	 * @param lookupName the name to lookup
	 * @return the path to the output directory or null
	 */
	public String outputDirectory(String lookupName) {
		return outputDirectories.getOrDefault (lookupName, spool (lookupName));
	}

	/**
	 * return an output directory for company specific
	 * output writing
	 *
	 * @param lookupName the name to lookup
	 * @return the path to the output directory or null
	 */
	public String outputDirectoryForCompany(String lookupName) {
		String path = outputDirectory(lookupName);

		if (path != null) {
			String companyPath = path + data.company.id();
			try {
				if (Data.isDirectory (companyPath)) {
					return companyPath;
				}
			} catch (Exception e) {
				// do nothing
			}
			return path + "0";
		}
		return path;
	}

	/**
	 * Write all mailing related settings to logfile
	 */
	public void logSettings() {
		data.logging(Log.DEBUG, "init", "\tmailing.id = " + id);
		if (exists()) {
			data.logging(Log.DEBUG, "init", "\tmailing.mailingID = " + mailing.mailingID());
			data.logging(Log.DEBUG, "init", "\tmailing.companyID = " + mailing.companyID());
			data.logging(Log.DEBUG, "init", "\tmailing.mailinglistID = " + mailing.mailinglistID());
			data.logging(Log.DEBUG, "init", "\tmailing.mailtemplateID = " + mailing.mailtemplateID());
			data.logging(Log.DEBUG, "init", "\tmailing.isTemplate = " + mailing.isTemplate());
			data.logging(Log.DEBUG, "init", "\tmailing.deleted = " + mailing.deleted());
			data.logging(Log.DEBUG, "init", "\tmailing.name = " + mailing.shortName());
			data.logging(Log.DEBUG, "init", "\tmailing.description = " + mailing.description());
			data.logging(Log.DEBUG, "init", "\tmailing.creationDate = " + mailing.creationDate());
			data.logging(Log.DEBUG, "init", "\tmailing.targetExpression = " + mailing.targetExpression());
			data.logging(Log.DEBUG, "init", "\tmailing.splitID = " + mailing.splitID());
			data.logging(Log.DEBUG, "init", "\tmailing.deliveryRestrictID = " + mailing.deliveryRestrictID());
			data.logging(Log.DEBUG, "init", "\tmailing.mailingType = " + mailing.mailingType());
			data.logging(Log.DEBUG, "init", "\tmailing.workStatus = " + mailing.workStatus());
			data.logging(Log.DEBUG, "init", "\tmailing.planDate = " + mailing.planDate());
			data.logging(Log.DEBUG, "init", "\tmailing.priority = " + mailing.priority());
			data.logging(Log.DEBUG, "init", "\tmailing.contentType = " + mailing.contentType());
			data.logging(Log.DEBUG, "init", "\tmailing.mailerset = " + mailing.mailerset());
			data.logging(Log.DEBUG, "init", "\tmailing.frequencyCounterDisabled = " + mailing.frequencyCounterDisabled());
			data.logging(Log.DEBUG, "init", "\tmailing.isWorkflowMailing = " + mailing.isWorkflowMailing());
			data.logging(Log.DEBUG, "init", "\tmailing.sourceTemplateID = " + mailing.sourceTemplateID());
			data.logging(Log.DEBUG, "init", "\tmailing.sourceTemplatePriority = " + mailing.sourceTemplatePriority());
		}
		data.logging(Log.DEBUG, "init", "\tmailing.defaultEncoding = " + defaultEncoding);
		data.logging(Log.DEBUG, "init", "\tmailing.defaultCharset = " + defaultCharset);
		data.logging(Log.DEBUG, "init", "\tmailing.blockSize = " + blockSize);
		data.logging(Log.DEBUG, "init", "\tmailing.dynBlockSizeMin = " + dynBlockSizeMin);
		data.logging(Log.DEBUG, "init", "\tmailing.dynBlockSizeMax = " + dynBlockSizeMax);
		data.logging(Log.DEBUG, "init", "\tmailing.dynBlockDivisor = " + dynBlockDivisor);
		data.logging(Log.DEBUG, "init", "\tmailing.realBlocksize = " + blockSize ());
		data.logging(Log.DEBUG, "init", "\tmailing.stepping = " + stepping);
		data.logging(Log.DEBUG, "init", "\tmailing.blocksPerStep = " + blocksPerStep);
		data.logging(Log.DEBUG, "init", "\tmailing.startBlockForStep = " + startBlockForStep);
		data.logging(Log.DEBUG, "init", "\tmailing.maxBytesPerOutputFile = " + maxBytesPerOutputFile);
		data.logging(Log.DEBUG, "init", "\tmailing.subject = " + (subject == null ? "*not set*" : subject));
		data.logging(Log.DEBUG, "init", "\tmailing.preHeader = " + (preHeader == null ? "*not set*" : preHeader));
		data.logging(Log.DEBUG, "init", "\tmailing.fromEmail = " + (fromEmail == null ? "*not set*" : fromEmail.toString()));
		data.logging(Log.DEBUG, "init", "\tmailing.replyTo = " + (replyTo == null ? "*not set*" : replyTo.toString()));
		data.logging(Log.DEBUG, "init", "\tmailing.envelopeFrom = " + (envelopeFrom == null ? "*not set*" : envelopeFrom.toString()));
		data.logging(Log.DEBUG, "init", "\tmailing.envelopeForced = " + envelopeForced);
		data.logging(Log.DEBUG, "init", "\tmailing.encoding = " + encoding);
		data.logging(Log.DEBUG, "init", "\tmailing.charset = " + charset);
		data.logging(Log.DEBUG, "init", "\tmailing.domain = " + domain);
		data.logging(Log.DEBUG, "init", "\tmailing.boundary = " + boundary);
		data.logging(Log.DEBUG, "init", "\tmailing.mailer = " + mailer);
		outputDirectories.entrySet().stream()
				.forEach((e) -> data.logging(Log.DEBUG, "init", "\tmailing.outputDirectory[" + e.getKey() + "] = " + e.getValue()));
		data.logging(Log.DEBUG, "init", "\tmailing.accountLogfile = " + accountLogfile);
		data.logging(Log.DEBUG, "init", "\tmailing.bounceLogfile = " + bounceLogfile);
		data.logging(Log.DEBUG, "init", "\tmailing.mailtrackLogfile = " + mailtrackLogfile);
		data.logging(Log.DEBUG, "init", "\tmailing.messageTransferAgent = " + (messageTransferAgent == null ? "*not set*" : messageTransferAgent));
	}

	/**
	 * Configure from external resource
	 *
	 * @param cfg the configuration
	 */
	public void configure(Config cfg) {
		defaultEncoding = cfg.cget("default_encoding", defaultEncoding);
		defaultCharset = cfg.cget("default_charset", defaultCharset);
		maxBytesPerOutputFile = cfg.cget("max_bytes_per_output_file", maxBytesPerOutputFile);
		domain = cfg.cget("domain", domain);
		boundary = cfg.cget("boundary", boundary);
		mailer = cfg.cget("mailer", mailer);
		for (String key : cfg.getKeys()) {
			if (key.endsWith("dir") && (key.length() > 3)) {
				String	directory = cfg.cget (key);
				
				if (Data.isDirectory (directory)) {
					outputDirectories.put(key.substring(0, key.length() - 3), directory);
				}
			}
		}
		accountLogfile = cfg.cget("account_logfile", accountLogfile);
		bounceLogfile = cfg.cget("bounce_logfile", bounceLogfile);
		mailtrackLogfile = cfg.cget("mailtrack_logfile", mailtrackLogfile);
		messageTransferAgent = Data.syscfg.get ("mta", System.getenv("MTA"));
	}
	
	private String spool (String directory, String alternate) {
		String	path = Str.makePath (spoolDirectory, directory);

		if ((alternate != null) && (! Data.isDirectory (path))) {
			path = spool (alternate);
		}
		return path;
	}
	private String spool (String directory) {
		return spool (directory, null);
	}
}
