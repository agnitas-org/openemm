/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.mailing.beans;

import java.util.Optional;

import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingContentType;
import com.agnitas.emm.common.MailingType;

/**
 * Light-weight mailing containing only data used in lists, tables, etc.
 */
public class LightweightMailing {

	/** ID of mailing. */
	private final int mailingID;
	
	/** ID of company of mailing. */
	private final int companyID;
	
	/** Description of mailing. */
	private final String description;
	
	/** Name of mailing. */
	private final String shortname;
	
	/** Mailing type. */
	private final MailingType mailingType;
	
	private final Optional<String> workStatus;
	
	private final MailingContentType mailingContentType;
	
	public LightweightMailing(final int companyID, final int mailingID, final String shortname, final String description, final MailingType mailingType, final String workStatusOrNull, final MailingContentType mailingContentType) {
		this.companyID = companyID;
		this.mailingID = mailingID;
		this.shortname = shortname;
		this.description = description;
		this.mailingType = mailingType;
		this.workStatus = Optional.ofNullable(workStatusOrNull);
		this.mailingContentType = mailingContentType;
	}
	
	/**
	 * Transfer data from heavy-weight mailing object to the light-weight mailing
	 * object.
	 * 
	 * @param mailing heavy-weight mailing object
	 */
	public LightweightMailing(final Mailing mailing) {
		this(mailing.getCompanyID(), mailing.getId(), mailing.getShortname(), mailing.getDescription(), mailing.getMailingType(), null, mailing.getMailingContentType());
	}
	
	/**
	 * Returns ID of mailing.
	 * 
	 * @return ID of mailing
	 */
	public int getMailingID() {
		return this.mailingID;
	}
	
	/**
	 * Returns company ID of mailing.
	 * 
	 * @return company ID of mailing
	 */
	public int getCompanyID() {
		return this.companyID;
	}
	
	/**
	 * Return description of mailing.
	 * 
	 * @return description of mailing
	 */
	public String getMailingDescription() {
		return this.description;
	}
	
	/**
	 * Return name of mailing.
	 * 
	 * @return name of mailing
	 */
	public String getShortname() {
		return this.shortname;
	}

	/**
	 * Returns the mailing type.
	 * 
	 * @return mailing type
	 */
	public MailingType getMailingType() {
		return mailingType;
	}
	
	public Optional<String> getWorkStatus() {
		return workStatus;
	}

	public final MailingContentType getMailingContentType() {
		return mailingContentType;
	}
}
