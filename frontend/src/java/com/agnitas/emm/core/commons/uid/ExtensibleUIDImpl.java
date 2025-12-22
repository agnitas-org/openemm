/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.uid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implementation of {@link ExtensibleUID} interface.
 */
final class ExtensibleUIDImpl implements ExtensibleUID {
	
	/** The logger. */
	private static final Logger logger = LogManager.getLogger(ExtensibleUIDImpl.class);

	/** License ID. */
	private final int licenseID;

	/** Prefix for UID. */
	private final String prefix;

	/** Company ID. */
	private final int companyID;

	/** Customer ID. */
	private final int customerID;

	/** Mailing ID. */
	private final int mailingID;

	/** URL ID. */
	private final int urlID;
	
	/** position of URL */
	private final int position;

	/** The bit field. */
	private final long bitField;
	
	/** The senddate in seconds */
	private long sendDate;
	
	/** the maildrop_status_tbl.status_field value if applicated */
	private char statusField;

	/**
	 * Create and initialize new UID instance.
	 *
	 * <b>Do never instantiate a UID directly, use {@link UIDFactory} methods instead.</b>
	 *
	 * @see UIDFactory
	 */
 	ExtensibleUIDImpl(final String prefix, final int licenseID, final int companyID, final int customerID, final int mailingID, final int urlID, final int position, final long bitfield) {
 		this.prefix = prefix;
 		this.licenseID = licenseID;
 		this.companyID = warnIfZero(companyID, "Company ID is 0");
 		this.customerID = warnIfZero(customerID, "Customer ID is 0");
 		this.mailingID = urlID == 0 ? mailingID : warnIfZero(mailingID, "Mailing ID is 0, but URL ID given"); // Warn, if mailing ID is 0, but URL ID is <> 0
 		this.urlID = urlID;
		this.position = position < 1 ? 1 : position;
 		this.bitField = bitfield;
		this.sendDate = 0L;
		this.statusField = '\0';
	}

 	/**
 	 * Checks,if given value is zero. If so, a warning is logged.
 	 *
 	 * @param value value to check
 	 * @param warning warning if value is 0
 	 *
 	 * @return given value
 	 */
 	private static int warnIfZero(final int value, final String warning) {
 		if(value == 0) {
 			logger.warn(String.format("Warning when creating new UID: %s", warning), new Exception(warning));
 		}
 		
 		return value;
 	}

	/**
	 * Copy the values of the given UID.
	 *
	 * @param uid UID to copy
	 */
	public ExtensibleUIDImpl(final ExtensibleUID uid) {
		this.companyID = uid.getCompanyID();
		this.customerID = uid.getCustomerID();
		this.licenseID = uid.getLicenseID();
		this.mailingID = uid.getMailingID();
		this.prefix = uid.getPrefix();
		this.urlID = uid.getUrlID();
		this.position = uid.getPosition();
		this.bitField = uid.getBitField();
		this.sendDate = uid.getSendDate ();
		this.statusField = uid.getStatusField ();
	}

	@Override
	public final int getLicenseID() {
		return this.licenseID;
	}

	@Override
	public final String getPrefix() {
		return this.prefix;
	}

	@Override
	public final int getCompanyID() {
		return this.companyID;
	}

	@Override
	public final int getCustomerID() {
		return this.customerID;
	}

	@Override
	public final int getMailingID() {
		return this.mailingID;
	}

	@Override
	public final int getUrlID() {
		return this.urlID;
	}
	
	@Override
	public final int getPosition() {
		return this.position;
	}

	@Override
	public final long getBitField() {
		return this.bitField;
	}
	
	@Override
	public long getSendDate () {
		return sendDate;
	}
	
	@Override
	public ExtensibleUID setSendDate (long sendDate) {
		this.sendDate = sendDate;
		return this;
	}
	
	@Override
	public char getStatusField () {
		return statusField;
	}
	
	@Override
	public ExtensibleUID setStatusField (char statusField) {
		this.statusField = statusField;
		return this;
	}

	@Override
	public String toString() {
		return "ExtensibleUIDImpl{" +
				"licenseID=" + licenseID +
				", prefix='" + prefix + '\'' +
				", companyID=" + companyID +
				", customerID=" + customerID +
				", mailingID=" + mailingID +
				", urlID=" + urlID +
				", position=" + position +
				", bitField=" + bitField +
				", sendDate=" + sendDate +
				", statusField=" + statusField +
				'}';
	}
}
