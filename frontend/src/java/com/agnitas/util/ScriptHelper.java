/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.util;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.DatasourceDescription;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.Recipient;
import org.agnitas.beans.impl.BindingEntryImpl;
import org.agnitas.dao.MaildropStatusDao;
import org.agnitas.dao.SourceGroupType;
import org.agnitas.dao.UserStatus;
import org.agnitas.dao.exception.UnknownUserStatusException;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.uid.parser.exception.DeprecatedUIDVersionException;
import org.agnitas.emm.core.commons.uid.parser.exception.InvalidUIDException;
import org.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.velocity.emmapi.VelocityRecipientWrapper;
import org.agnitas.preview.Preview;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.TimeoutLRUMap;
import org.agnitas.util.XmlUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.dao.ComBindingEntryDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.DatasourceDescriptionDao;
import com.agnitas.dao.ScripthelperEmailLogDao;
import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.commons.encoder.Sha512Encoder;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mailing.service.MailgunOptions;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingService;
import com.agnitas.emm.core.mailing.service.impl.UnableToSendActionbasedMailingException;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.scripthelper.service.ScriptHelperService;
import com.agnitas.json.Json5Reader;
import com.agnitas.json.JsonObject;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ScriptHelper {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ScriptHelper.class);

	/** Maximum length for valid email. This is a workaround for AGNEMM-2306. */
	private static final int MAXIMUM_EMAIL_ADDRESS_LENGTH = 100;	// 100 is same size as of column CUSTOMER_?_TBL.email

	/** DAO accessing mailing data. */
	private ComMailingDao mailingDao;

	private MaildropStatusDao maildropStatusDao;
	
	private MailinglistService mailinglistService;

	/** DAO accessing ScripthelperEmailLog. */
	private ScripthelperEmailLogDao scripthelperEmailLogDao;

	/** DAO accessing subscriber data. */
	protected ComRecipientDao recipientDao;
	
	/** DAO accessing recipient binding data. */
	protected ComBindingEntryDao bindingEntryDao;

	private ScriptHelperService helperService;

	private ConfigService configService;
	
	private DatasourceDescriptionDao datasourceDescriptionDao;

	private HttpServletResponse res = null;

	private JavaMailService javaMailService;

	private SendActionbasedMailingService sendActionbasedMailingService;

	/** SHA512 encoder for Velocity. */
	private final transient Sha512Encoder sha512Encoder;

	long timers[] = new long[10];

	private RecipientService recipientService;

	/** Service for handling UIDs. */
	private ExtensibleUIDService extensibleUIDService;

	protected int companyID;
	
    protected TimeoutLRUMap<Integer, Integer> datasourceIdCache = new TimeoutLRUMap<>(100, 300000);

	/**
	 * Caution: mailingID may be null
	 */
	private Integer mailingID;

	/**
	 * Caution: formID may be null
	 */
	private Integer formID;

    public ScriptHelper() {
        this(null, null, null);
    }

	/**
	 * This constructor has to stay unmodified.
	 * DON'T touch it.
	 * It will crash at loading some script action.
	 */
	public ScriptHelper(final ApplicationContext con, final HttpServletRequest request, final HttpServletResponse res) {
//		super(con);
		this.res = res;
		this.sha512Encoder = new Sha512Encoder();
	}

	/**
	 * We have to use this method, because the constructor is not allowed to be
	 * modified.
	 */
	public void setScriptHelper(final ScriptHelperService helperService) {
		this.helperService = helperService;
	}

	public void startTimer(final int timerID) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		if (timerID < 10) {
			timers[timerID] = System.currentTimeMillis();
		}
	}

	public long stopTimer(final int timerID) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		if (timerID < 10) {
			return System.currentTimeMillis() - timers[timerID];
		} else {
			return 0;
		}
	}

	public String toString(final long i) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		return Long.toString(i);
	}

	/**
	 * @deprecated use #set($params = {}) in Velocity-script instead where possible
	 * Because the put-method will print an error when called with Strings instead of Objects.
	 */
	@Deprecated
	public Map<Object, Object> newHashtable() {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		return new Hashtable<>();
	}

	public Date newDate() {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		return new Date();
	}

	public GregorianCalendar newCalendar() {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		final GregorianCalendar calendar = new GregorianCalendar();

		// Using ISO-8601 for correct german WEEK_OF_YEAR
		DateUtilities.makeWeekOfYearISO8601Compliant(calendar);

		return calendar;
	}

	public List<Object> newLinkedList() {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		return new LinkedList<>();
	}

	public String getNewline() {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		return "\n";
	}

	public String getDoubleQuote() {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		return "\"";
	}

	public Map<String, String> parseCsvTokens(final String input) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		final Map<String, String> result = new HashMap<>();
		int posA = 0;
		int posB = input.indexOf(": ");
		while (posB != -1) {
			final String token = input.substring(posA, posB).trim();
			posA = posB + 2;
			posB = input.indexOf(";", posA);
			if (posB != -1) {
				final String value = input.substring(posA, posB).trim();
				result.put(token, value);
				posA = posB + 1;
				posB = input.indexOf(": ", posA);
			}
		}

		return result;
	}

	public String urlEncode(final String input) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		try {
			return URLEncoder.encode(input, "UTF-8");
		} catch (final Exception e) {
			return "";
		}
	}

	public String formatDate(final Date aDate, final String format, final String lang, final String country) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		try {
			final Locale aLocale = new Locale(lang, country);
			final SimpleDateFormat aFormat = new SimpleDateFormat(format, aLocale);
			return aFormat.format(aDate);
		} catch (final Exception e) {
			logger.error("Problem in dateformat: " + e, e);
			return "error in Format-String!";
		}
	}

	public String encodeSessionID(final String input) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		return res.encodeURL(input);
	}

	/**
	 * Send email (with cc-address) and log this event
	 *
	 * @param from_adr
	 * @param to_adr
	 * @param to_cc_adr
	 * @param subject
	 * @param body_text
	 * @param body_html
	 * @param mailtype
	 * @param charset
	 * @return
	 */
	public boolean sendEmail(final String from_adr, final String to_adr, final String to_cc_adr, final String subject, final String body_text, final String body_html, final String charset) {
		final boolean success = javaMailService.sendEmail(companyID, from_adr, null, null, null, null, to_adr, to_cc_adr, configService.getValue(ConfigValue.DefaultBccEmail, companyID), subject, body_text, body_html, charset);
		if (success) {
			// Create log Entry
			scripthelperEmailLogDao.writeLogEntry(companyID, mailingID, formID, from_adr, to_adr, to_cc_adr, subject);
		}
		return success;

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */
	}

	/**
	 * Send email (without cc-address) and log this event
	 * 
	 * @Deprecated: This method is there only for legacy compatibility reasons.
	 * 				Use "sendEmail(final String from_adr, final String to_adr, final String to_cc_adr, final String subject, final String body_text, final String body_html, final String charset)" instead.
	 *
	 * @param from_adr
	 * @param to_adr
	 * @param subject
	 * @param body_text
	 * @param body_html
	 * @param mailtype
	 * @param charset
	 * @return
	 */
	@Deprecated
	public boolean sendEmail(final String from_adr, final String to_adr, final String subject, final String body_text, final String body_html, final int mailtype, final String charset) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		return sendEmail(from_adr, to_adr, null, subject, body_text, body_html, charset);
	}

	/**
	 * Send email (with cc-address) and log this event
	 * 
	 * @Deprecated: This method is there only for legacy compatibility reasons.
	 * 				Use "sendEmail(final String from_adr, final String to_adr, final String to_cc_adr, final String subject, final String body_text, final String body_html, final String charset)" instead.
	 *
	 * @param from_adr
	 * @param to_adr
	 * @param to_cc_adr
	 * @param subject
	 * @param body_text
	 * @param body_html
	 * @param mailtype
	 * @param charset
	 * @return
	 */
	@Deprecated
	public boolean sendEmail(final String from_adr, final String to_adr, final String to_cc_adr, final String subject, final String body_text, final String body_html, final int mailtype, final String charset) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		return sendEmail(from_adr, to_adr, to_cc_adr, subject, body_text, body_html, charset);
	}

	public int parseInt(final String input) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		try {
			return Integer.parseInt(input);
		} catch (final Exception e) {
			return 0;
		}
	}

	public Integer newInteger(final int i) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		return i;
	}

	public String padROrTrim(final String input, final int size) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		String result = "";
		if (input != null) {
			result = input;
		}
		if (result.length() > size) {
			result = result.substring(0, size);
		}

		while (result.length() < size) {
			result = result + " ";
		}

		return result;
	}

	public String padLOrTrim(final String input, final int size) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		String result = input;
		if (result.length() > size) {
			result = result.substring(0, size);
		}

		while (result.length() < size) {
			result = " " + result;
		}

		return result;
	}

	public List<String> getForLoop(final int size) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		final List<String> result = new LinkedList<>();
		for (int i = 1; i <= size; i++) {
			result.add(Integer.toString(i));
		}

		return result;
	}

	/**
	 * this method returns the last sent mailing for the given customer.
	 *
	 * @param companyIdToCheck
	 * @param customerID
	 * @return
	 * @throws Exception
	 */
	public int getLastSentMailingID(final int companyIdToCheck, final int customerID) throws Exception {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		if (helperService == null) {
			throw new Exception("No ScriptHelperService is set. You must call setHelperService first!");
		} else {
			return helperService.getLastSentMailingID(companyIdToCheck, customerID);
		}
	}

	/**
	 * This method returns the mailingID for the given company which is a
	 * world-mailing and was sent to the given mailinglist. If the given
	 * mailinglistID is null or "0" it will be ignored.
	 *
	 * @param companyIdToCheck
	 * @return
	 * @throws Exception
	 */
	public int getLastSentWorldMailingIDByCompanyAndMailinglist(final int companyIdToCheck, final int mailingListID) throws Exception {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		if (helperService == null) {
			throw new Exception("No ScriptHelperService is set. You must call setHelperService first!");
		} else {
			int lastNewsletterMailingID = helperService.getLastSentWorldMailingByCompanyAndMailinglist(companyIdToCheck, mailingListID);
			
			if (lastNewsletterMailingID <= 0) {
				// No such mailing found
				return 0;
			} else {
				// Check if event-mailing entry already exists. Only one single event-mailing maildropStatus entry should exist.
				final List<MaildropEntry> maildropStatusList = maildropStatusDao.getMaildropStatusEntriesForMailing(companyIdToCheck, lastNewsletterMailingID);
				for (final MaildropEntry entry : maildropStatusList) {
					if (entry.getStatus() == MaildropStatus.ACTION_BASED.getCode()) {
						return lastNewsletterMailingID;
					}
				}

				// Create new maildrop entry for event-mailing
				final MaildropEntry drop = new MaildropEntryImpl();

				drop.setStatus(MaildropStatus.ACTION_BASED.getCode());
				drop.setSendDate(new java.util.Date());
				drop.setGenDate(new java.util.Date());
				drop.setGenStatus(1);
				drop.setGenChangeDate(new java.util.Date());
				drop.setMailingID(lastNewsletterMailingID);
				drop.setCompanyID(companyIdToCheck);

				maildropStatusDao.saveMaildropEntry(drop);

				return lastNewsletterMailingID;
			}
		}
	}

	public Object getAnonymousLastSentMailing(final int companyIdToCheck, final int customerID) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		try {
			final Map <String, Object> returnTable = helperService.getAnonLastSentMailing(companyIdToCheck, customerID);
			return returnTable.get(Preview.ID_HTML);
		} catch (final Exception e) {
			logger.error("Anonymous Preview failed. CompanyID: " + companyIdToCheck + " customerID: " + customerID + e, e);
			return null;
		}
	}

	/**
	 * this method returns the HTML part for the given mailingID with the given
	 * customerID.
	 *
	 * @param mailingIdToCheck
	 * @param customerID
	 * @return
	 */
	public Object getAnonymousLastSentMailingByMailingID(final int mailingIdToCheck, final int customerID) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		Object returnObject = null;
		try {
			returnObject = helperService.getAnonLastSentMailingByMailingID(mailingIdToCheck, customerID);
		} catch (final Exception e) {
			logger.error("ComScriptHelper:getAnonymousLastSentMailingByMailingID -> Error creating Fullview: \n" + e);
		}
		return returnObject;
	}

	public boolean validateEmail(final String email) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		// Check if email address does not exceed maximum allowed length. This is a workaround for AGNEMM-2306.
		if (email.length() > MAXIMUM_EMAIL_ADDRESS_LENGTH) {
			logger.warn("Email address to long (" + email.length() + " exceeds " + MAXIMUM_EMAIL_ADDRESS_LENGTH + ")");
			return false;
		}

		try {
			return AgnUtils.isEmailValid(email);
		} catch( final Exception e) {
			logger.error( "Error validating email address", e);
			return false;
		}
	}

	/*
	 * TODO When EMM-8506 is rolled out completely:
	 * 
	 * 1. Change type of method parameter to VelocityRecipient
	 * 2. Remove "recipient instanceof Recipient" path
	 */
	public boolean storeRecipient(final Object recipient) {
		/*
		 * **************************************************
		 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
		 * **************************************************
		 *
		 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
		 */
		
		final Recipient rcp = recipientFrom(recipient);
		
		if (rcp == null) {
			logger.error(String.format("Error in storeRecipient - Unsupported type of recipient: %s", recipient.getClass().getCanonicalName()));
			return false;
		}
		
		// Set velocity specific datasource id
		try {
			Integer datasourceDescriptionId = datasourceIdCache.get(companyID);
			if (datasourceDescriptionId == null) {
				DatasourceDescription datasourceDescription = datasourceDescriptionDao.getByDescription(SourceGroupType.Velocity, companyID, "Velocity");
				if (datasourceDescription == null) {
					datasourceDescription = datasourceDescriptionDao.getByDescription(SourceGroupType.Velocity, 0, "Velocity");
				}
				if (datasourceDescription != null) {
					datasourceDescriptionId = datasourceDescription.getId();
					datasourceIdCache.put(companyID, datasourceDescriptionId);
				}
			}
			
			if (datasourceDescriptionId != null) {
				rcp.getCustParameters().put(ComCompanyDaoImpl.STANDARD_FIELD_LATEST_DATASOURCE_ID, datasourceDescriptionId);
			}
		} catch (Exception e) {
			logger.error("Cannot set velocity datasource_id in recipient for company " + companyID, e);
		}
		
		try {
			return recipientDao.updateInDbWithException(rcp, false);
		} catch (Exception e) {
			logger.error("Error in storeRecipient", e);
			return false;
		}
	}

	/*
	 * TODO When EMM-8506 is rolled out completely:
	 * 
	 * 1. Change type of method parameter to VelocityRecipient
	 * 2. Remove "recipient instanceof Recipient" path
	 */
	public boolean updateDatasourceID(final Object cust) {
    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */
		
		final Recipient rcp = recipientFrom(cust);
		
		if(rcp == null) {
			logger.error(String.format("Error in updateDatasourceID - Unsupported type of recipient: %s", cust.getClass().getCanonicalName()));
			return false;
		}

		return recipientDao.updateDataSource(rcp);
	}

	/**
	 * Computes SHA512 hash for given String. UTF-8 encoding is assumed.
	 * Result is sequence of hex digits.
	 *
	 * @param s String to encode
	 *
	 * @return sequence of hex digits
	 *
	 * @throws UnsupportedEncodingException if UTF-8 is not supported.
	 */
	public final String sha512(final String s) throws UnsupportedEncodingException {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		return this.sha512WithEncoding(s, "UTF-8");
	}

	/**
	 * Computes SHA512 hash for given String. Encoding is to be specified.
	 * Result is sequence of hex digits.
	 *
	 * @param s String to encode
	 * @param encoding character encoding of input String
	 *
	 * @return sequence of hex digits
	 *
	 * @throws UnsupportedEncodingException if given encoding is not supported.
	 */
	public final String sha512WithEncoding(final String s, final String encoding) throws UnsupportedEncodingException {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		return this.sha512Encoder.encodeToHex(s, encoding);
	}

	/**
	 * Computes a mod b.
	 *
	 * @param a first operand
	 * @param b second operand
	 *
	 * @return modulo
	 */
	public int mod( final int a, final int b) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		return a % b;
	}

	public Map<String, Object> decodeUID(final String uidString) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		try {
			final ComExtensibleUID uid = extensibleUIDService.parse(uidString);

			final Map<String, Object> map = new HashMap<>();

			map.put("companyID", uid.getCompanyID());
			map.put("customerID", uid.getCustomerID());
			map.put("mailingID", uid.getMailingID());
			map.put("prefix", uid.getPrefix());
			map.put("urlID", uid.getUrlID());

			return map;
		} catch( final Exception e) {
			logger.error( "Error parsing UID: " + uidString, e);
			return null;
		}
	}

    private Map<String, String> buildRecipient(final NodeList allMessageChilds) throws Exception {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

    	final Map<String, String> result = new HashMap<>();
		Node aNode = null;
		String nodeName = null;
		NodeList recipientNodes = null;
		Node recipientNode = null;
		String recipientNodeName = null;
		NamedNodeMap allAttr = null;

		for (int i = 0; i < allMessageChilds.getLength(); i++) {
			aNode = allMessageChilds.item(i);
			nodeName = aNode.getNodeName();

			if (nodeName.equals("recipient")) {
				recipientNodes = aNode.getChildNodes();
				for (int j = 0; j < recipientNodes.getLength(); j++) {
					recipientNode = recipientNodes.item(j);
					recipientNodeName = recipientNode.getNodeName();
					if (recipientNodeName.equals("gender")
							|| recipientNodeName.equals("firstname")
							|| recipientNodeName.equals("lastname")
							|| recipientNodeName.equals("mailtype")
							|| recipientNodeName.equals("email")) {
						try {
							result.put(recipientNodeName.toUpperCase(), recipientNode.getFirstChild().getNodeValue());
						} catch (final Exception e) {
							// do nothing
						}
					}
					if (recipientNodeName.equals("extracol")) {
						allAttr = recipientNode.getAttributes();
						try {
							result.put(allAttr.getNamedItem("name").getNodeValue().toUpperCase(), recipientNode.getFirstChild().getNodeValue());
						} catch (final Exception e) {
							// do nothing
						}
					}
				}
			}
			if (nodeName.equals("content")) {
				allAttr = aNode.getAttributes();
				try {
					result.put(allAttr.getNamedItem("name").getNodeValue().toUpperCase(), aNode.getFirstChild().getNodeValue());
				} catch (final Exception e) {
					// do nothing
				}
			}
		}

		return result;
	}

	/*
	 * TODO When EMM-8506 is rolled out completely:
	 * 
	 * 1. Change type of method parameter to VelocityRecipient
	 * 2. Remove "recipient instanceof Recipient" path
	 */
    public final int updateRecipientWithEmailChangeConfirmation(final Object recipient, final int mailingIdToSet, final String profileFieldForConfirmationCode) {
    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

    	Recipient rcp;
    	
    	if(recipient instanceof Recipient) {
    		rcp = (Recipient) recipient;
    	} else if(recipient instanceof VelocityRecipientWrapper) {
    		rcp = ((VelocityRecipientWrapper) recipient).getWrappedRecipient();
    	} else {
			logger.error(String.format("Error in updateRecipientWithEmailChangeConfirmation - Unsupported type of recipient: %s", recipient.getClass().getCanonicalName()));
			return -1;
    	}

    	try {
    		this.recipientService.updateRecipientWithEmailChangeConfiguration(rcp, mailingIdToSet, profileFieldForConfirmationCode);

    		return rcp.getCustomerID();
    	} catch(final Exception e) {
    		final String msg = String.format("Error updating recipient %d with email change confirmation (mailing ID %d)", rcp.getCustomerID(), mailingIdToSet);
    		logger.error(msg, e);

    		return -1;
    	}
    }

    public final int confirmEmailAddressChange(final String uidString, final String confirmationCode) {
    	try {
    		final ComExtensibleUID uid = extensibleUIDService.parse(uidString);

    		try {
	    		this.recipientService.confirmEmailAddressChange(uid, confirmationCode);

	    		return 0;
    		} catch(final Exception e) {
    			logger.error(String.format("Error confirming email address change for customer %d of company %d", uid.getCustomerID(), uid.getCompanyID()), e);

    			return -1;
    		}
    	} catch(final UIDParseException e) {
    		if(logger.isInfoEnabled()) {
    			logger.info(String.format("Error parsing uid '%s'", uidString), e);
    		}

    		return -1;
    	} catch (final DeprecatedUIDVersionException e) {
    		if(logger.isInfoEnabled()) {
    			logger.info(String.format("UID '%s' of deprected version", uidString), e);
    		}
    		
    		return -1;
		} catch (final InvalidUIDException e) {
    		if(logger.isInfoEnabled()) {
    			logger.info(String.format("Invalid UID '%s'", uidString), e);
    		}
    		
    		return -1;
		}
    }

	public List<Map<String, Object>> parseTransactionMailXml(final String xmlInput) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		List<Map<String, Object>> result = new LinkedList<>();
		final boolean validation = false;
		final boolean ignoreWhitespace = true;
		final boolean ignoreComments = true;
		final boolean putCDATAIntoText = true;
		final boolean createEntityRefs = false;

		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			dbf.setFeature(XmlUtilities.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
			dbf.setFeature(XmlUtilities.LOAD_EXTERNAL_DTD_FEATURE, false);
			dbf.setFeature(XmlUtilities.EXTENRAL_PARAMETER_ENTITIES_FEATURE, false);
		} catch(final ParserConfigurationException e) {
			logger.fatal("Cannot enable XML parser feature DISALLOW_DOCTYPE_DECL_FEATURE! Parser is currently vulnerable to External XML Entity processing!");
		}

		// set the configuration options
		dbf.setValidating(validation);
		dbf.setIgnoringComments(ignoreComments);
		dbf.setIgnoringElementContentWhitespace(ignoreWhitespace);
		dbf.setCoalescing(putCDATAIntoText);
		// The opposite of creating entity ref nodes is expanding them inline
		dbf.setExpandEntityReferences(!createEntityRefs);

		DocumentBuilder db = null;
		Document doc = null;
		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(new InputSource(new StringReader(xmlInput)));
			final Element base = doc.getDocumentElement();
			final NodeList allMessages = base.getChildNodes();
			NodeList allMessageChilds = null;
			Node aMessage = null;
			NamedNodeMap allAttr = null;
			String nodeName = null;
			int messageType = 0;
			Map<String, Object> messageEntry = null;

			for (int i = 0; i < allMessages.getLength(); i++) {
				aMessage = allMessages.item(i);
				nodeName = aMessage.getNodeName();

				if (nodeName.equals("message")) {
					messageEntry = new HashMap<>();
					allAttr = aMessage.getAttributes();
					messageType = Integer.parseInt(allAttr.getNamedItem("type").getNodeValue());
					messageEntry.put("messageType", messageType);
					allMessageChilds = aMessage.getChildNodes();
					messageEntry.put("recipient", buildRecipient(allMessageChilds));
					result.add(messageEntry);
				}
			}

		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
			result = null;
		}

		return result;
	}

	/**
	 * @deprecated use #set($params = {}) in Velocity-script instead where possible.
	 * Because the put-method will print an error when called with Strings instead of Objects.
	 */
	@Deprecated
	public Map<Object, Object> newHashMap() {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		return new HashMap<>();
	}

	public void println(final String output) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		logger.error(output);
	}

	/**
	 * Finds the last newsletter that would have been sent to the given
	 * customer. The newsletter also gets a new entry maildrop_status_tbl to
	 * allow it to be sent as action mail.
	 *
	 * @param customerID
	 *            Id of the recipient for the newsletter.
	 * @param companyIdToCheck
	 *            the company to look in.
	 * @return The mailingID of the last newsletter that would have been sent to
	 *         this recipient.
	 * @throws Exception
	 */
	public int findLastNewsletter(final int customerID, final int companyIdToCheck, final int mailinglist) throws Exception {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		final int lastNewsletterMailingID = mailingDao.findLastNewsletter(customerID, companyIdToCheck, mailinglist);

		if (lastNewsletterMailingID <= 0) {
			// No such mailing found
			return 0;
		} else {
			// Check if event-mailing entry already exists. Only one single event-mailing maildropStatus entry should exist.
			final List<MaildropEntry> maildropStatusList = maildropStatusDao.getMaildropStatusEntriesForMailing(companyIdToCheck, lastNewsletterMailingID);
			for (final MaildropEntry entry : maildropStatusList) {
				if (entry.getStatus() == MaildropStatus.ACTION_BASED.getCode()) {
					return lastNewsletterMailingID;
				}
			}

			// Create new maildrop entry for event-mailing
			final MaildropEntry drop = new MaildropEntryImpl();

			drop.setStatus(MaildropStatus.ACTION_BASED.getCode());
			drop.setSendDate(new java.util.Date());
			drop.setGenDate(new java.util.Date());
			drop.setGenStatus(1);
			drop.setGenChangeDate(new java.util.Date());
			drop.setMailingID(lastNewsletterMailingID);
			drop.setCompanyID(companyIdToCheck);

			maildropStatusDao.saveMaildropEntry(drop);

			return lastNewsletterMailingID;
		}
	}

	/**
	 * Parse a given date string to a GregorianCalendar
	 *
	 * @param dateValue
	 * @param datePattern
	 * @return
	 */
	public GregorianCalendar parseDate(final String dateValue, final String datePattern) {

		/*
		 * **************************************************
		 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
		 * **************************************************
		 *
		 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
		 */

		try {
			final GregorianCalendar returnValue = new GregorianCalendar();
			final Date parsedDate = new SimpleDateFormat(datePattern).parse(dateValue);
			returnValue.setTime(parsedDate);
			return returnValue;
		} catch (final ParseException e) {
			return null;
		}
	}

	/**
	 * Create a UID for a customer
	 *
	 * @param companyId
	 * @param customerKeyColumnName
	 * @param customerKeyColumnValue
	 * @return
	 */
	public String createUidForCustomer(final int companyId, final String customerKeyColumnName, final String customerKeyColumnValue) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		try {
			// Search for customer
			final int customerID = recipientDao.findByColumn(companyId, customerKeyColumnName, customerKeyColumnValue);

			// Create UID
			if (customerID > 0) {
				final ComExtensibleUID extensibleUID = UIDFactory.from(this.configService.getLicenseID(), companyId, customerID);

				return extensibleUIDService.buildUIDString(extensibleUID);
			} else {
				return null;
			}
		} catch (final Exception e) {
			return null;
		}
	}

	public String formatNumber(final String numberString, final String decimalSeparatorCharacter, final String formatPattern, final String language) {
		return formatNumber(AgnUtils.normalizeNumber(decimalSeparatorCharacter, numberString), formatPattern, language);
	}

	public String formatNumber(final String numberString, final String formatPattern, final String language) {
		return formatNumber(Double.parseDouble(numberString), formatPattern, language);
	}

	public String formatNumber(final Number numberValue, final String formatPattern, final String language) {
		final Locale locale = Locale.forLanguageTag(language);
		final DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(locale);
		format.applyPattern(formatPattern);
		return format.format(numberValue);
	}

	public boolean sendEventMailing(final Mailing mailing, final int customerID, final int delayMinutes, final String userStatus, final Map<String, String> overwrite) {
		return sendEventMailing(mailing, customerID, delayMinutes, null, userStatus, overwrite);
	}

	public boolean sendEventMailing(final Mailing mailing, final int customerID, final int delayMinutes, final List<Integer> userStatusList, final Map<String, String> overwrite) {
		return sendEventMailing(mailing, customerID, delayMinutes, null, userStatusList, overwrite);
	}

	public boolean sendEventMailing(final Mailing mailing, final int customerID, final int delayMinutes, final String bccEmails, final String userStatus, final Map<String, String> overwrite) {
		List<Integer> userStatusList = null;
		if (userStatus != null) {
			userStatusList = new Vector<>();

			final int status = Integer.parseInt(userStatus);
			if (status == UserStatus.Active.getStatusCode() || status == UserStatus.WaitForConfirm.getStatusCode()) {
				// This block is for backward compatibility only!
				userStatusList.add(UserStatus.Active.getStatusCode());
				userStatusList.add(UserStatus.WaitForConfirm.getStatusCode());
			} else {
				userStatusList.add(status);
			}
		}
		return sendEventMailing(mailing, customerID, delayMinutes, bccEmails, userStatusList, overwrite);
	}

	public boolean sendEventMailing(final Mailing mailing, final int customerID, final int delayMinutes, final String bccEmails, final List<Integer> userStatusList, final Map<String, String> overwrite) {
		try {
			final MailgunOptions mailgunOptions = new MailgunOptions();
			if (userStatusList != null) {
				final List<UserStatus> statusList = new ArrayList<>();
				
				for(final int statusCode : userStatusList) {
					try {
						final UserStatus status = UserStatus.getUserStatusByID(statusCode);
						statusList.add(status);
					} catch(final UnknownUserStatusException e) {
						/*
						 * When an unknown status code is encountered we
						 * can ignore it. Even as int-typed code, if wouldn't have any effect.
						 */
						logger.error(String.format("Skipping unknown user status code %d", statusCode));
					}
				}

				mailgunOptions.withAllowedUserStatus(statusList);
			}
			if (overwrite != null) {
				mailgunOptions.withProfileFieldValues(overwrite);
			}

			try {
				if (StringUtils.isNotBlank(bccEmails)) {
					mailgunOptions.withBccEmails(bccEmails);
				}

				sendActionbasedMailingService.sendActionbasedMailing(mailing.getCompanyID(), mailing.getId(), customerID, delayMinutes, mailgunOptions);
			} catch(final Exception e) {
				logger.error("Cannot fire campaign-/event-mail", e);

				throw new UnableToSendActionbasedMailingException(mailing.getId(), customerID, e);
			}
			return true;
		} catch(final Exception e) {
			logger.error("Error sending action-based mailing", e);

			return false;
		}
	}
	
	public boolean validateReCaptcha(String apiKey, String captchaResponseToken) {
		try {
			Map<String, Object> httpPostParameter = new HashMap<>();
			httpPostParameter.put("secret", apiKey);
			httpPostParameter.put("response", captchaResponseToken);
			String response = HttpUtils.executeHttpPostRequest("https://www.google.com/recaptcha/api/siteverify", httpPostParameter, configuredSecureTransportLayerProtocol(companyID));
			JsonObject jsonResponse = (JsonObject) Json5Reader.readJsonItemString(response).getValue();
			Boolean success = (Boolean) jsonResponse.get("success");
			return success != null && success;
		} catch (Exception e) {
			logger.error("Recaptcha validate error: " + e.getMessage(), e);
			return false;
		}
	}
	
	public int random(int startLimitInclusive, int endLimitExclusive) {
		return ((int) (Math.random() * (endLimitExclusive - startLimitInclusive)) + startLimitInclusive);
	}
	
	public boolean checkEmailExcludes(String emailAddress, List<String> excludeEmailPatterns) {
		return checkEmailIncludesExcludes(emailAddress, null, excludeEmailPatterns);
	}
	
	public boolean checkEmailIncludes(String emailAddress, List<String> includeEmailPatterns) {
		return checkEmailIncludesExcludes(emailAddress, includeEmailPatterns, null);
	}
	
	public boolean checkEmailIncludesExcludes(String emailAddress, List<String> includeEmailPatterns, List<String> excludeEmailPatterns) {
		try {
			emailAddress = AgnUtils.checkAndNormalizeEmail(emailAddress);
		} catch (Exception e) {
			return false;
		}

		if (includeEmailPatterns != null) {
			boolean foundMatchingIncludePattern = false;
			for (String includeEmailPattern : includeEmailPatterns) {
				if (emailMatchesPattern(emailAddress, includeEmailPattern)) {
					foundMatchingIncludePattern = true;
					break;
				}
			}
			if (!foundMatchingIncludePattern) {
				return false;
			}
		}
		
		if (excludeEmailPatterns != null) {
			for (String excludeEmailPattern : excludeEmailPatterns) {
				if (emailMatchesPattern(emailAddress, excludeEmailPattern)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	private boolean emailMatchesPattern(String emailAddress, String emailPattern) {
		return Pattern.matches(emailPattern.replace(".", "\\.").replace("*", ".*").replace("?", "."), emailAddress);
	}
	
	/**
	 * <b>For internal use only!!!!</b>
	 * 
	 * Returns the recipient object from given object:
	 * 
	 * <ul>
	 *   <li><code>obj</code> is of type {@link Recipient}, then the given object is returned</li>
	 *   <li><code>obj</code> is of type {@link VelocityRecipientWrapper} then the wrapped recipient is returned</li>
	 *   <li>In any other case, <code>null</code> is returned.
	 * </ul>
	 * 
	 * @param obj object representating a recipient
	 * 
	 * @return recipient
	 */
	private Recipient recipientFrom(final Object obj) {
    	if(obj instanceof Recipient) {
    		return (Recipient) obj;
    	} else if(obj instanceof VelocityRecipientWrapper) {
    		return ((VelocityRecipientWrapper) obj).getWrappedRecipient();
    	} else {
			logger.error(String.format("Unsupported type of recipient: %s", obj.getClass().getCanonicalName()));
			return null;
    	}

	}
	
	private final String configuredSecureTransportLayerProtocol(final int companyIdParam) {
		return this.configService.getValue(ConfigValue.SecureTransportLayerProtocol, companyIdParam);
	}
	
	/**
	 * Check for existing binding and insert new one or update an existing one
	 * 
	 * @param customerID
	 * @param mailinglistID
	 * @param mediaType
	 * @param userType
	 * @param userStatus
	 * @param userRemark
	 * @param referrer
	 * @param entryMailingID
	 * @param exitMailingID
	 */
	public void saveBinding(int customerID, int mailinglistID, int mediaType, String userType, int userStatus, String userRemark, String referrer, int entryMailingID, int exitMailingID) {
		BindingEntry bindingEntry = new BindingEntryImpl();
		
		bindingEntry.setCustomerID(customerID);
		bindingEntry.setMailinglistID(mailinglistID);
		bindingEntry.setMediaType(mediaType);
		bindingEntry.setUserType(userType);
		bindingEntry.setUserStatus(userStatus);
		bindingEntry.setUserRemark(userRemark);
		bindingEntry.setReferrer(referrer);
		bindingEntry.setEntryMailingID(entryMailingID);
		bindingEntry.setExitMailingID(exitMailingID);
		
		bindingEntryDao.save(companyID, bindingEntry);
	}
	
	public Mailinglist getMailinglist(int mailinglistID) {
		return mailinglistService.getMailinglist(mailinglistID, companyID);
	}

	public void logFile(final int companyIdToLogFor, final String logFileName, final String content) {
    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		try {
			File velocityLogDirPath = new File(new File(AgnUtils.getTempDir(), "Velocity"), Integer.toString(companyIdToLogFor));
			if (!velocityLogDirPath.exists()) {
				velocityLogDirPath.mkdirs();
			}
			String requestUUID = AgnUtils.generateNewUUID().toString().replace("-", "").toUpperCase();
			File tmpFile = new File(velocityLogDirPath, "Velocity_" + (StringUtils.isNotEmpty(logFileName) ? logFileName + "_" : "") + requestUUID + ".log");
			try (FileWriter aWriter = new FileWriter(tmpFile)) {
				if (content != null) {
					aWriter.write(content);
				}
			}
		} catch (final Exception e) {
			logger.error("could not log script: " + e + "\n" + companyIdToLogFor + " " + logFileName + " " + content, e);
		}
	}

	public void setMailingDao(final ComMailingDao mailingDao) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		this.mailingDao = mailingDao;
	}

	public void setMaildropStatusDao(final MaildropStatusDao maildropStatusDao) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		this.maildropStatusDao = maildropStatusDao;
	}

	public void setMailinglistService(final MailinglistService mailinglistService) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		this.mailinglistService = mailinglistService;
	}

	public void setRecipientDao(final ComRecipientDao recipientDao) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		this.recipientDao = recipientDao;
	}

	public void setBindingEntryDao(final ComBindingEntryDao bindingEntryDao) {

    	/*
    	 * **************************************************
    	 *   IMPORTANT  IMPORTANT    IMPORTANT    IMPORTANT
    	 * **************************************************
    	 *
    	 * DO NOT REMOVE METHOD OR CHANGE SIGNATURE!!!
    	 */

		this.bindingEntryDao = bindingEntryDao;
	}

	public void setCompanyID(final int companyID) {
		this.companyID = companyID;
	}

	/**
	 * Caution: mailingID may be null
	 *
	 * @param mailingID
	 */
	public void setMailingID(final Integer mailingID) {
		this.mailingID = mailingID;
	}

	/**
	 * Caution: formID may be null
	 *
	 * @param formID
	 */
	public void setFormID(final Integer formID) {
		this.formID = formID;
	}

	// --------------------------------------------------------------------------------- Dependency Injection
	public void setConfigService(final ConfigService configService) {
		this.configService = configService;
	}

	public void setHelperService(final ScriptHelperService helperService) {
		this.helperService = helperService;
	}
	
	public Date getMailingSendDate(final int companyIdToCheck, final int mailingIdToCheck) {
		return mailingDao.getMailingSendDate(companyIdToCheck, mailingIdToCheck);
	}
	
	public String getMailingSubject(final int mailingIdParameter) throws Exception {
		return mailingDao.getEmailSubject(companyID, mailingIdParameter);
	}
	
	public String getMailingName(final int mailingIdParameter) {
		return mailingDao.getLightweightMailing(companyID, mailingIdParameter).getShortname();
	}

	@Required
	public void setScripthelperEmailLogDao(final ScripthelperEmailLogDao scripthelperEmailLogDao) {
		this.scripthelperEmailLogDao = scripthelperEmailLogDao;
	}

	@Required
	public void setJavaMailService(final JavaMailService javaMailService) {
		this.javaMailService = javaMailService;
	}

	@Required
	public void setSendActionbasedMailingService(final SendActionbasedMailingService sendActionbasedMailingService) {
		this.sendActionbasedMailingService = sendActionbasedMailingService;
	}

	@Required
	public final void setRecipientService(final RecipientService service) {
		this.recipientService = Objects.requireNonNull(service, "Recipient service is null");
	}

	@Required
	public final void setExtensibleUIDService(final ExtensibleUIDService extensibleUIDService) {
		this.extensibleUIDService = extensibleUIDService;
	}
	
	@Required
	public final void setDatasourceDescriptionDao(final DatasourceDescriptionDao datasourceDescriptionDao) {
		this.datasourceDescriptionDao = Objects.requireNonNull(datasourceDescriptionDao, "datasourceDescriptionDao is null");
	}
}
