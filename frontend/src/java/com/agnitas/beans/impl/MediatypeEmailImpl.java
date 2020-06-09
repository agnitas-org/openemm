/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.mail.internet.InternetAddress;

import com.agnitas.beans.MediatypeEmail;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.impl.MediatypeImpl;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.ParameterParser;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import static org.agnitas.emm.core.mailing.service.MailingModel.Format.OFFLINE_HTML;

public class MediatypeEmailImpl extends MediatypeImpl implements MediatypeEmail {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MediatypeEmailImpl.class);

	public final String DEFAULT_CHARSET = "UTF-8";

	/** Holds value of property subject. */
	protected String subject = "";

	/** Holds value of property linefeed. */
	protected int linefeed;

	/** Holds value of property mailFormat. */
	protected int mailFormat = 2;

	/** Holds value of property charset. */
	protected String charset = DEFAULT_CHARSET;

	/** Holds value of property fromAdr. */
	protected String fromEmail = "";

	/** Holds value of property fromAdr. */
	protected String fromFullname = "";

	/** Complete Reply-To Address. */
	protected String replyEmail = "";

	/** Holds value of property replyFullname. */
	protected String replyFullname = "";

	/** Holds value of property envelopeAdr. */
	protected String envelopeEmail = "";

	/** Holds value of property envelopeAdr. */
	protected String followupFor = "";

	/** holds the follow-Up Method, eg. non-opener, opener, non-clicker, clicker */
	protected String followUpMethod = "";

	protected boolean doublechecking = false;

	protected boolean skipempty = false;

	/** Flag, if IntelliAd link tracking is enabled. */
	protected boolean intelliAdEnabled;

	/** ID string for IntelliAd. */
	protected String intelliAdIdString;

	protected String htmlTemplate;

	/** Holds value of property onepixel. */
	protected String onepixel = MediatypeEmailImpl.ONEPIXEL_TOP;

	/** Holds value of property mailingID. */
	protected int mailingID;
	
	protected String bccRecipients;

	/** Creates a new instance of MediatypeEmailImpl */
	public MediatypeEmailImpl() {
		template = "[agnDYN name=\"Text\" /]";
		htmlTemplate = "[agnDYN name=\"HTML-Version\" /]";
	}

	/**
	 * Getter for property envelopeAdr.
	 * 
	 * @return Value of property envelopeAdr.
	 */
	@Override
	public String getEnvelopeEmail() {
		return envelopeEmail;
	}

	/**
	 * Setter for property envelopeAdr.
	 * 
	 * @param envelopeEmail
	 *            New value of property envelopeAdr.
	 */
	@Override
	public void setEnvelopeEmail(String envelopeEmail) {
		this.envelopeEmail = envelopeEmail;
	}

	@Override
	public String getFollowupFor() {
		return followupFor;
	}

	@Override
	public void setFollowupFor(String followupFor) {
		this.followupFor = followupFor;
	}

	@Override
	public boolean isDoublechecking() {
		return doublechecking;
	}

	@Override
	public void setDoublechecking(boolean doublechecking) {
		this.doublechecking = doublechecking;
	}

	/**
	 * Setter for property onepixel.
	 * 
	 * @param onepixel
	 *            New value of property onepixel.
	 */
	@Override
	public void setOnepixel(String onepixel) {
		this.onepixel = onepixel;
	}

	/**
	 * Getter for property onepixel.
	 * 
	 * @return Value of property onepixel.
	 */
	@Override
	public String getOnepixel() {
		return onepixel;
	}

	/**
	 * Getter for property subject.
	 * 
	 * @return Value of property subject.
	 *
	 */
	@Override
	public String getSubject() {
		return subject;
	}

	/**
	 * Setter for property subject.
	 * 
	 * @param subject
	 *            New value of property subject.
	 *
	 */
	@Override
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * Getter for property fromAdr.
	 * 
	 * @return Value of property fromAdr.
	 *
	 */
	@Override
	public String getFromEmail() {
		return fromEmail;
	}

	/**
	 * Setter for property fromAdr.
	 * 
	 * @param fromEmail
	 *            New value of property fromAdr.
	 *
	 */
	@Override
	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}

	/**
	 * Getter for property fromAdr.
	 * 
	 * @return Value of property fromAdr.
	 *
	 */
	@Override
	public String getFromFullname() {
		return fromFullname;
	}

	/**
	 * Setter for property fromAdr.
	 * 
	 * @param fromFullname
	 *            New value of property fromAdr.
	 *
	 */
	@Override
	public void setFromFullname(String fromFullname) {
		this.fromFullname = fromFullname;
	}

	@Override
	public String getFromAdr() throws Exception {
		InternetAddress tmpFrom = new InternetAddress(fromEmail, fromFullname, charset);
		if (StringUtils.isNotBlank(tmpFrom.getPersonal())) {
			return tmpFrom.getPersonal() + " <" + tmpFrom.getAddress() + ">";
		} else {
			return tmpFrom.getAddress();
		}
	}

	/**
	 * Getter for property linefeed.
	 * 
	 * @return Value of property linefeed.
	 *
	 */
	@Override
	public int getLinefeed() {
		return linefeed;
	}

	/**
	 * Setter for property linefeed.
	 * 
	 * @param linefeed
	 *            New value of property linefeed.
	 *
	 */
	@Override
	public void setLinefeed(int linefeed) {
		this.linefeed = linefeed;
	}

	/**
	 * Getter for property mailFormat.
	 * 
	 * @return Value of property mailFormat.
	 *
	 */
	@Override
	public int getMailFormat() {
		return mailFormat;
	}

	/**
	 * Setter for property mailFormat.
	 * 
	 * @param mailFormat
	 *            New value of property mailFormat.
	 *
	 */
	@Override
	public void setMailFormat(int mailFormat) {
		this.mailFormat = mailFormat;
	}
	
	@Override
	public void setMailFormat(MailType mailType) {
		this.mailFormat = mailType.getIntValue();
	}
	
	@Override
	public void deleteDateBasedParameters() {
		setBccRecipients("");
	}
	
	/**
	 * Getter for property charset.
	 * 
	 * @return Value of property charset.
	 *
	 */
	@Override
	public String getCharset() {
		return charset;
	}

	/**
	 * Setter for property charset.
	 * 
	 * @param charset
	 *            New value of property charset.
	 *
	 */
	@Override
	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * Getter for property replyAdr.
	 * 
	 * @return Value of property replyAdr.
	 */
	@Override
	public String getReplyAdr() throws Exception {
		InternetAddress tmpReply = new InternetAddress(replyEmail, replyFullname, charset);
		if (StringUtils.isNotBlank(tmpReply.getPersonal())) {
			return tmpReply.getPersonal() + " <" + tmpReply.getAddress() + ">";
		} else {
			return tmpReply.getAddress();
		}
	}

	/**
	 * Getter for property replyEmail.
	 * 
	 * @return Value of property replyEmail.
	 */
	@Override
	public String getReplyEmail() {
		return replyEmail;
	}

	/**
	 * Setter for property replyAdr.
	 * 
	 * @param replyEmail
	 *            New value of property replyAdr.
	 */
	@Override
	public void setReplyEmail(String replyEmail) {
		this.replyEmail = replyEmail;
	}

	/**
	 * Getter for property replyFullname.
	 * 
	 * @return Value of property replyFullname.
	 */
	@Override
	public String getReplyFullname() {
		return replyFullname;
	}

	/**
	 * Setter for property replyFullname.
	 * 
	 * @param replyFullname
	 *            New value of property replyFullname.
	 */
	@Override
	public void setReplyFullname(String replyFullname) {
		this.replyFullname = replyFullname;
	}

	/**
	 * Getter for property mailingID.
	 * 
	 * @return Value of property mailingID.
	 */
	@Override
	public int getMailingID() {
		return mailingID;
	}

	/**
	 * Setter for property mailingID.
	 * 
	 * @param mailingID
	 *            New value of property mailingID.
	 */
	@Override
	public void setMailingID(int mailingID) {
		this.mailingID = mailingID;
	}

	/**
	 * Setter for property onepixel.
	 * 
	 * @param htmlTemplate
	 *            New value of property onepixel.
	 */
	@Override
	public void setHtmlTemplate(String htmlTemplate) {
		this.htmlTemplate = htmlTemplate;
	}

	@Override
	public void setParam(String param) throws Exception {
		Map<String, String> parameters = new ParameterParser(param).parse();

		String from = parameters.get("from");
		fromEmail = "";
		fromFullname = "";
		if (StringUtils.isNotEmpty(from)) {
			try {
				InternetAddress adr = new InternetAddress(from);

				fromEmail = adr.getAddress();
				fromFullname = adr.getPersonal();
			} catch (Exception e) {
				// Cannot check from address
			}
		}

		String reply = parameters.get("reply");
		replyEmail = "";
		replyFullname = "";
		if (StringUtils.isNotEmpty(reply)) {
			try {
				InternetAddress adr = new InternetAddress(reply);
				
				replyEmail = adr.getAddress();
				replyFullname = adr.getPersonal();
			} catch (Exception e) {
				// Cannot check reply address
			}
		} else {
			replyEmail = fromFullname;
			replyFullname = fromFullname;
		}

		String envelope = parameters.get("envelope");
		envelopeEmail = "";
		if (StringUtils.isNotBlank(envelope)) {
			InternetAddress adr = new InternetAddress(envelope);
			envelopeEmail = adr.getAddress();
		}

		charset = parameters.get("charset");
		if (StringUtils.isBlank(charset)) {
			charset = "ISO-8859-1";
		}

		subject = parameters.get("subject");

		mailFormat = NumberUtils.toInt(parameters.get("mailformat"), OFFLINE_HTML.getValue());

		linefeed = NumberUtils.toInt(parameters.get("linefeed"), 72);

		onepixel = StringUtils.defaultIfEmpty(parameters.get("onepixlog"), MediatypeEmail.ONEPIXEL_TOP);

		followupFor = StringUtils.defaultString(parameters.get("followup_for"));

		// set the follow-up method
		// we have 4 different types:
		// "non-opener" ,"opener", "non-clicker", "clicker"
		// they all have to be lowercase!
		followUpMethod = StringUtils.defaultString(parameters.get("followup_method"));

		doublechecking = StringUtils.isNotEmpty(parameters.get("remove_dups"));

		String skip = parameters.get("skipempty");
		skipempty = BooleanUtils.toBoolean(skip);

		String intelliAdEnableParam = parameters.get(INTELLIAD_ENABLE_PARAM);
		setIntelliAdEnabled(BooleanUtils.toBoolean(intelliAdEnableParam));

		String intelliAdStringParam = parameters.get(INTELLIAD_STRING_PARAM);
		setIntelliAdString(StringUtils.trimToEmpty(intelliAdStringParam));
		
		String bcc = parameters.get(BCC_STRING_PARAM);
		setBccRecipients(bcc);
	}

	@Override
	public String getParam() throws Exception {
		StringBuffer result = new StringBuffer();
		InternetAddress tmpFrom = new InternetAddress(fromEmail, fromFullname, charset);
		if (StringUtils.isEmpty(replyEmail)) {
			replyEmail = fromEmail;
		}
		if (StringUtils.isEmpty(replyFullname)) {
			replyFullname = fromFullname;
		}
		InternetAddress tmpReply = new InternetAddress(replyEmail, replyFullname, charset);

		result.append("from=\"");
		result.append(ParameterParser.escapeValue(tmpFrom.toString()));
		result.append("\"");

		result.append(", subject=\"");
		result.append(ParameterParser.escapeValue(subject));
		result.append("\"");

		result.append(", charset=\"");
		result.append(ParameterParser.escapeValue(charset));
		result.append("\"");

		result.append(", linefeed=\"");
		result.append(ParameterParser.escapeValue(Integer.toString(linefeed)));
		result.append("\"");

		result.append(", mailformat=\"");
		result.append(ParameterParser.escapeValue(Integer.toString(mailFormat)));
		result.append("\"");

		result.append(", reply=\"");
		result.append(ParameterParser.escapeValue(tmpReply.toString()));
		result.append("\"");

		result.append(", onepixlog=\"");
		result.append(ParameterParser.escapeValue(onepixel));
		result.append("\"");

		// we have to check if we have a follow up. If not, we dont even write the
		// parameters
		// to the DB-Field. With that nothing wrong can happen if the backend checks
		// that
		// parameters.
		if (StringUtils.isNotBlank(followupFor) && StringUtils.isNotBlank(followUpMethod) && !followupFor.equals("0")) {
			result.append(", followup_for=\"");
			result.append(ParameterParser.escapeValue(followupFor));
			result.append("\"");

			result.append(", followup_method=\"");
			result.append(ParameterParser.escapeValue(followUpMethod));
			result.append("\"");
		}

		if (StringUtils.isNotEmpty(envelopeEmail)) {
			InternetAddress tmpEnvelope = new InternetAddress(envelopeEmail);

			result.append(", envelope=\"");
			result.append(tmpEnvelope.toString());
			result.append("\"");
		}

		if (doublechecking) {
			result.append(", remove_dups=\"true\"");
		}

		result.append(", skipempty=");
		if (skipempty) {
			result.append("\"true\"");
		} else {
			result.append("\"false\"");
		}

		if (isIntelliAdEnabled()) {
			result.append(", ");
			result.append(INTELLIAD_ENABLE_PARAM);
			result.append("=\"true\"");
		}

		if (StringUtils.isNotBlank(getIntelliAdString())) {
			result.append(", ");
			result.append(INTELLIAD_STRING_PARAM);
			result.append("=\"");
			result.append(getIntelliAdString());
			result.append("\"");
		}
		
		if (StringUtils.isNotBlank(getBccRecipients())) {
			result.append(", ");
			result.append(BCC_STRING_PARAM);
			result.append("=\"");
			result.append(getBccRecipients());
			result.append("\"");
		}

		super.setParam(result.toString());
		return result.toString();
	}

	// getter and setter for the followUpMethod (see declaration of String
	// followUpMethod)
	@Override
	public String getFollowUpMethod() {
		return followUpMethod;
	}

	@Override
	public void setFollowUpMethod(String followUpMethod) {
		this.followUpMethod = followUpMethod;
	}

	@Override
	public boolean isSkipempty() {
		return skipempty;
	}

	@Override
	public void setSkipempty(boolean skipempty) {
		this.skipempty = skipempty;
	}

	// removes the parameters for a follow-up mailing.
	@Override
	public void deleteFollowupParameters() {
		String params = "";
		String paramsWithoutFollowup = "";

		// first, get original parameters
		try {
			params = getParam();
		} catch (Exception e) {
			logger.error("Error in getting parameters from mailing: " + e);
		}

		// now remove followup
		paramsWithoutFollowup = removeFollowUp(params);

		// set the new parameters
		try {
			setParam(paramsWithoutFollowup);
		} catch (Exception e) {
			logger.error("Error setting new parameters in mailing: " + e);
		}
	}

	// removes everthing for a followup from the given String.
	private String removeFollowUp(String original) {
		// search for ', followup_for="12345"' with "" but without ''
		String pattern = "\\p{Punct}\\p{Blank}followup_for=\\p{Punct}\\d*\\p{Punct}";
		// search for ', followup_method="abcd"' or ', followup_method="abcd-efgh"'
		// eg. "followup_method="non-opener" or "followup_method="opener".
		String pattern2 = "\\p{Punct}\\p{Blank}followup_method=\\p{Punct}\\w*\\p{Punct}?\\w*\\p{Punct}";
		String returnString = original.replaceAll(pattern, "");
		returnString = returnString.replaceAll(pattern2, "");
		return returnString;
	}

	@Override
	public boolean isIntelliAdEnabled() {
		return intelliAdEnabled;
	}

	@Override
	public void setIntelliAdEnabled(boolean enabled) {
		this.intelliAdEnabled = enabled;
	}

	@Override
	public String getIntelliAdString() {
		return intelliAdIdString;
	}

	@Override
	public void setIntelliAdString(String intelliAdString) {
		this.intelliAdIdString = intelliAdString;
	}

	/**
	 * Getter for property onepixel.
	 * 
	 * @return Value of property onepixel.
	 */
	@Override
	public String getHtmlTemplate() {
		return htmlTemplate;
	}
	
	@Override
	public String getBccRecipients() throws Exception {
		return Stream.of(AgnUtils.getEmailAddressesFromList(bccRecipients))
					.map(InternetAddress::toString).collect(Collectors.joining(", "));
	}
	
	@Override
	public void setBccRecipients(String bccRecipients) {
		this.bccRecipients = bccRecipients;
	}
	
	/**
	 * Makes a standalone copy of this mediatype without any references to this
	 * objects data
	 * 
	 * @return
	 * @throws Exception
	 */
	@Override
	public MediatypeEmail copy() throws Exception {
		MediatypeEmail mediatypeEmail = new MediatypeEmailImpl();
		mediatypeEmail.setParam(getParam());
		mediatypeEmail.setStatus(getStatus());
		mediatypeEmail.setPriority(getPriority());
		mediatypeEmail.setCompanyID(getCompanyID());
		mediatypeEmail.setTemplate(getTemplate());
		return mediatypeEmail;
	}

	@Override
	public void syncTemplate(Mailing mailing, ApplicationContext con) {
		MailingComponent component = mailing.getTextTemplate();
		if (component != null) {
			component.setEmmBlock(template, "text/plain");
		}

		component = mailing.getHtmlTemplate();
		if (component != null) {
			component.setEmmBlock(htmlTemplate, "text/html");
		}
	}

	@Override
	public MediaTypes getMediaType() {
		return MediaTypes.EMAIL;
	}
}
