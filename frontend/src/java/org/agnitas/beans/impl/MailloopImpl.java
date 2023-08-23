/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import java.util.Date;

import org.agnitas.beans.Mailloop;

public class MailloopImpl implements Mailloop {
	/**
	 * Holds value of property id.
	 */
	private int id;

	/**
	 * Holds value of property shortname.
	 */
	private String shortname;

	/**
	 * Holds value of property description.
	 */
	private String description;

	/**
	 * Holds value of property companyID.
	 */
	private int companyID;

	/**
	 * Holds value of property forwardEmail.
	 */
	private String forwardEmail;

	/**
	 * Holds value of property doForward.
	 */
	private boolean doForward;

	/**
	 * Holds value of property doSubscribe.
	 */
	private boolean doSubscribe;

	/**
	 * Holds value of property changedate.
	 */
	private Date changedate;

	/**
	 * Holds value of property doAutoresponder.
	 */
	private boolean doAutoresponder;

	/**
	 * Holds value of property mailinglistID.
	 */
	private int mailinglistID;

	/**
	 * Holds value of property userformID.
	 */
	private int userformID;
	
	/** ID of auto-responder mailing. */
	private int autoresponderMailingId;
	
	/** Security token defined for the mailloop. */
	private String securityToken;

	/**
	 * Holds value of property filterEmail.
	 */
	private String filterEmail = "";

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String getShortname() {
		return shortname;
	}

	@Override
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int getCompanyID() {
		return companyID;
	}

	@Override
	public void setCompanyID(int companyID) {
		this.companyID = companyID;
	}

	@Override
	public String getForwardEmail() {
		return forwardEmail;
	}

	@Override
	public void setForwardEmail(String forwardEmail) {
		this.forwardEmail = forwardEmail;
	}

	@Override
	public boolean isDoForward() {
		return doForward;
	}

	@Override
	public void setDoForward(boolean doForward) {
		this.doForward = doForward;
	}

	@Override
	public boolean isDoAutoresponder() {
		return doAutoresponder;
	}

	@Override
	public void setDoAutoresponder(boolean doAutoresponder) {
		this.doAutoresponder = doAutoresponder;
	}

	@Override
	public Date getChangedate() {
		return changedate;
	}

	@Override
	public void setChangedate(Date changedate) {
		this.changedate = changedate;
	}

	@Override
	public boolean isDoSubscribe() {
		return doSubscribe;
	}

	@Override
	public void setDoSubscribe(boolean doSubscribe) {
		this.doSubscribe = doSubscribe;
	}

	@Override
	public int getMailinglistID() {
		return mailinglistID;
	}

	@Override
	public void setMailinglistID(int mailinglistID) {
		this.mailinglistID = mailinglistID;
	}

	@Override
	public int getUserformID() {
		return userformID;
	}

	@Override
	public void setUserformID(int userformID) {
		this.userformID = userformID;
	}

	@Override
	public String getFilterEmail() {
		return filterEmail;
	}

	@Override
	public void setFilterEmail(String filterEmail) {
		this.filterEmail = filterEmail;
	}

	@Override
	public int getAutoresponderMailingId() {
		return this.autoresponderMailingId;
	}

	@Override
	public void setAutoresonderMailingId(int mailingID) {
		this.autoresponderMailingId = mailingID;
	}
	
	@Override
	public String getSecurityToken() {
		return this.securityToken;
	}
	
	@Override
	public void setSecurityToken(final String securityToken) {
		this.securityToken = securityToken;
	}
}
