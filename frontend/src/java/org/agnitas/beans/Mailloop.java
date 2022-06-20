/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.util.Date;

import org.agnitas.emm.core.velocity.VelocityCheck;

public interface Mailloop {
    /**
     * Getter for property companyID.
     *
     * @return Value of property companyID.
     */
    int getCompanyID();

    /**
     * Getter for property description.
     *
     * @return Value of property description.
     */
    String getDescription();

    /**
     * Getter for property forwardEmail.
     *
     * @return Value of property forwardEmail.
     */
    String getForwardEmail();

    /**
     * Getter for property mailloopID.
     *
     * @return Value of property mailloopID.
     */
    int getId();

    /**
     * Getter for property shortname.
     *
     * @return Value of property shortname.
     */
    String getShortname();

    /**
     * Getter for property doAutoresponder.
     *
     * @return Value of property doAutoresponder.
     */
    boolean isDoAutoresponder();

    /**
     * Getter for property doForward.
     *
     * @return Value of property doForward.
     */
    boolean isDoForward();

    /**
     * Setter for property companyID.
     *
     * @param companyID New value of property companyID.
     */
    void setCompanyID( @VelocityCheck int companyID);

    /**
     * Setter for property description.
     *
     * @param description New value of property description.
     */
    void setDescription(String description);

    /**
     * Setter for property doAutoresponder.
     *
     * @param doAutoresponder New value of property doAutoresponder.
     */
    void setDoAutoresponder(boolean doAutoresponder);

    /**
     * Setter for property doForward.
     *
     * @param doForward New value of property doForward.
     */
    void setDoForward(boolean doForward);

    /**
     * Setter for property forwardEmail.
     *
     * @param forwardEmail New value of property forwardEmail.
     */
    void setForwardEmail(String forwardEmail);

    /**
     * Setter for property mailloopID.
     *
     * @param mailloopID New value of property mailloopID.
     */
    void setId(int mailloopID);

    /**
     * Setter for property shortname.
     *
     * @param shortname New value of property shortname.
     */
    void setShortname(String shortname);

    /**
     * Getter for property changedate.
     *
     * @return Value of property changedate.
     */
    Date getChangedate();

    /**
     * Setter for property changedate.
     *
     * @param changedate New value of property changedate.
     */
    void setChangedate(Date changedate);
    
    /**
     * Getter for property mailinglistID.
     *
     * @return Value of property mailinglistID.
     */
    int getMailinglistID();
    
    /**
     * Setter for property mailinglistID.
     *
     * @param mailinglistID New value of property mailinglistID.
     */
    void setMailinglistID(int mailinglistID);
    
    /**
     * Getter for property userformID.
     *
     * @return Value of property userformID.
     */
    int getUserformID();
    
    /**
     * Setter for property userformID.
     *
     * @param userformID New value of property userformID.
     */
    void setUserformID(int userformID);
    
    /**
     * Getter for property doSubscribe.
     *
     * @return Value of property doSubscribe.
     */
    boolean isDoSubscribe();
    
    /**
     * Setter for property doSubscribe.
     *
     * @param doSubscribe New value of property doSubscribe.
     */
    void setDoSubscribe(boolean doSubscribe);

	/**
	 * Getter for property filterEmail.
	 * 
	 * @return Value of property filterEmail.
	 */
    String getFilterEmail();

	/**
	 * Setter for property filterEmail.
	 * 
	 * @param filterEmail
	 *            New value of property filterEmail.
	 */
	void setFilterEmail(String filterEmail);
	
	/**
	 * Returns the mailing ID of the auto-responder mailing.
	 * If no auto-responder mailing is defined, 0 is returned. In this case,
	 * use {@link #getArHtml()}, {@link #getArSender()}, {@link #getArSubject()} and {@link #getArText()}.
	 * 
	 * @return mailing ID of auto-responder or 0
	 */
	int getAutoresponderMailingId();
	
	/**
	 * Set ID of auto-responder mailing. If no auto-responder mailing is defined, set to 0.
	 * 
	 * @param mailingID mailing ID of auto-responder mailing or 0.
	 */
	void setAutoresonderMailingId(final int mailingID);

	/**
	 * Returns the security token defined for the mailloop.
	 * 
	 * @return security token for mailloop
	 */
	String getSecurityToken();
	
	/**
	 * Set new security token for mailloop.
	 * 
	 * @param securityToken security token for mailloop
	 */
	void setSecurityToken(final String securityToken);
}
