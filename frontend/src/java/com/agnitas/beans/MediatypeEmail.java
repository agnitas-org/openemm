/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import org.agnitas.beans.Mediatype;
import org.agnitas.util.importvalues.MailType;

public interface MediatypeEmail extends Mediatype {
    String ONEPIXEL_BOTTOM = "bottom";
    String ONEPIXEL_NONE = "none";
    String ONEPIXEL_TOP = "top";
    
	String INTELLIAD_ENABLE_PARAM = "intelliad_enabled";
	String INTELLIAD_STRING_PARAM = "intelliad_string";
    
    /**
     * Getter for property charset.
     * 
     * @return Value of property charset.
     */
    String getCharset();

    /**
     * Getter for property fromAdr.
     * 
     * @return Value of property fromAdr.
     */
    String getFromEmail();

    /**
     * Getter for property fromAdr.
     * 
     * @return Value of property fromAdr.
     */
    String getFromFullname();

    /**
     * Getter for property fromAdr.
     * 
     * @return Value of property fromAdr.
     */
    String getReplyEmail();

    /**
     * Getter for property fromAdr.
     * 
     * @return Value of property fromAdr.
     */
    String getReplyFullname();

    /**
     * Getter for property fromAdr.
     * 
     * @return Value of property fromAdr.
     */
    String getFromAdr() throws Exception;

    /**
     * Getter for property linefeed.
     * 
     * @return Value of property linefeed.
     */
    int getLinefeed();

    /**
     * Getter for property mailFormat.
     * 
     * @return Value of property mailFormat.
     */
    int getMailFormat();

    /**
     * Getter for property onepixel.
     * 
     * @return Value of property onepixel.
     */
    String getOnepixel();

    /**
     * Getter for property replyAdr.
     * 
     * @return Value of property replyAdr.
     */
    String getReplyAdr() throws Exception;

    /**
     * Getter for property subject.
     * 
     * @return Value of property subject.
     */
    String getSubject();

    /**
     * Getter for property subject.
     * 
     * @return Value of property subject.
     */
    String getHtmlTemplate();

    /**
     * Setter for property charset.
     * 
     * @param charset New value of property charset.
     */
    void setCharset(String charset);

    /**
     * Setter for property fromAdr.
     * 
     * @param fromAdr New value of property fromAdr.
     */
    void setFromEmail(String fromEmail);

    /**
     * Setter for property fromAdr.
     * 
     * @param fromAdr New value of property fromAdr.
     */
    void setFromFullname(String fromFullname);

    /**
     * Setter for property fromAdr.
     * 
     * @param fromAdr New value of property fromAdr.
     */
    void setReplyEmail(String replyEmail);

    /**
     * Setter for property fromAdr.
     * 
     * @param fromAdr New value of property fromAdr.
     */
    void setReplyFullname(String replyFullname);

    /**
     * Setter for property linefeed.
     * 
     * @param linefeed New value of property linefeed.
     */
    void setLinefeed(int linefeed);

    /**
     * Setter for property mailFormat.
     * 
     * @param mailFormat New value of property mailFormat.
     */
    void setMailFormat(int mailFormat);

    /**
     * Setter for property onepixel.
     * 
     * @param onepixel New value of property onepixel.
     */
    void setOnepixel(String onepixel);

    /**
     * Setter for property subject.
     * 
     * @param subject New value of property subject.
     */
    void setSubject(String subject);

    /**
     * Setter for property subject.
     * 
     * @param subject New value of property subject.
     */
    void setHtmlTemplate(String htmlTemplate);

    /**
     * Getter for property mailingID.
     *
     * @return Value of property mailingID.
     */
    int getMailingID();
    
     /**
     * Setter for property mailingID.
     * 
     * @param mailingID New value of property mailingID.
     */
    void setMailingID(int mailingID);
    
	/**
     * Getter for property envelopeAdr.
     * 
     * @return Value of property envelopeAdr.
     */
    String getEnvelopeEmail();
    
    /**
     * Setter for property envelopeAdr.
     * 
     * @param envelopeAdr New value of property envelopeAdr.
     */
    void setEnvelopeEmail(String envelopeEmail);

	/**
	 * Gets the id of the mailing this one is referencing.
	 * This field is only used in followup mails for non-openers.
	 * 
	 * @return Value of property followupFor.
	 */
	String	getFollowupFor();
    
	/**
	 * Setter for property followupFor.
	 * 
	 * @param followupFor	The mailing_id of the referenced mailing.
	 */
	void setFollowupFor(String followupFor);
	
	boolean isDoublechecking();

	void setDoublechecking(boolean doublechecking);

	// for getting and setting the Follow-Up Method (for clickers, non-clickers and so on).
	String getFollowUpMethod();

	void setFollowUpMethod(String followUpMethod); 
	
	boolean isSkipempty();

	void setSkipempty(boolean skipempty);
	
	// removes the follow-up parameters (and only that) from the mailing.
	void deleteFollowupParameters();

    /**
     * Returns if IntelliAd is enabled for mailing. 
     * 
     * @return true if IntelliAd is enabled
     */
    boolean isIntelliAdEnabled();
    
    /**
     * Set if IntelliAd is enabled for mailing.
     * 
     * @param enabled true if IntelliAd is enabled
     */
    void setIntelliAdEnabled( boolean enabled);
    
    /**
     * Returns the IntelliAd ID string if set.
     * 
     * @return IntelliAd ID string or null
     */
    String getIntelliAdString();
    
    /**
     * Set the IntelliAd ID string if set.
     * 
     * @param intelliAdString IntelliAd ID string or null
     */
    void setIntelliAdString( String intelliAdString);

	void setMailFormat(MailType mailType);
}

