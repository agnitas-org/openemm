/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.uid;

import org.agnitas.beans.Recipient;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.emm.core.commons.uid.ComExtensibleUID.NamedUidBit;

public final class UIDFactory {
	
 	/**
 	 * Creates a UID used to identify a customer.
 	 * All other properties than given ones are set to 0.
 	 * 
 	 * @param licenseID license ID (must be <> 0)
 	 * @param companyID company ID (must be <> 0)
 	 * @param customerID customer ID (must be <> 0)
 	 * @param bits named control bits
 	 * 
 	 * @return UID
 	 */
 	public static final ComExtensibleUID from(final int licenseID, @VelocityCheck final int companyID, final int customerID, final NamedUidBit...bits) {
 		return new ComExtensibleUIDImpl(
 				null, 
 				licenseID, 
 				companyID, 
 				customerID,
 				0, 
 				0, 
 				NamedUidBit.namedBitsToLong(bits));
 	}
 	
 	/**
 	 * Creates a UID used to identify customer and mailing.
 	 * All other properties than given ones are set to 0.
 	 * 
 	 * @param licenseID license ID (must be <> 0)
 	 * @param companyID company ID (must be <> 0)
 	 * @param customerID customer ID (must be <> 0)
 	 * @param mailingID mailing ID
 	 * @param bits named control bits
 	 * 
 	 * @return UID
 	 */
 	public static final ComExtensibleUID from(final int licenseID, @VelocityCheck final int companyID, final int customerID, final int mailingID, final NamedUidBit...bits) {
 		return new ComExtensibleUIDImpl(
 				null, 
 				licenseID, 
 				companyID, 
 				customerID,
 				mailingID, 
 				0, 
 				NamedUidBit.namedBitsToLong(bits));
 	}
	
 	/**
 	 * Creates a UID used to identify customer, mailing and link.
 	 * All other properties than given ones are set to 0.
 	 * 
 	 * @param licenseID license ID (must be <> 0)
 	 * @param companyID company ID (must be <> 0)
 	 * @param customerID customer ID (must be <> 0)
 	 * @param mailingID mailing ID (must be <> 0, if link ID <> 0)
 	 * @param urlID URL ID
 	 * @param bits named control bits
 	 * 
 	 * @return UID
 	 */
 	public static final ComExtensibleUID from(final int licenseID, @VelocityCheck final int companyID, final int customerID, final int mailingID, final int urlID, final NamedUidBit...bits) {
 		return new ComExtensibleUIDImpl(
 				null, 
 				licenseID, 
 				companyID, 
 				customerID,
 				mailingID, 
 				urlID, 
 				NamedUidBit.namedBitsToLong(bits));
 	}
	
 	/**
 	 * Creates a UID used to identify a recipient.
 	 * All other properties than given ones are set to 0.
 	 * 
 	 * @param licenseID license ID (must be <> 0)
 	 * @param recipient recipient
 	 * @param additionalBits additional named control bits
 	 * 
 	 * @return UID
 	 */
 	public static final ComExtensibleUID from(final int licenseID, final Recipient recipient, final NamedUidBit...additionalBits) {
 		long bits = NamedUidBit.namedBitsToLong(additionalBits);
 		
 		return new ComExtensibleUIDImpl(
 				null, 
 				licenseID, 
 				recipient.getCompanyID(), 
 				recipient.getCustomerID(),
 				0, 
 				0, 
 				bits);
 	}
	
 	/**
 	 * Creates a UID used to identify a recipient and mailing.
 	 * All other properties than given ones are set to 0.
 	 * 
 	 * @param licenseID license ID (must be <> 0)
 	 * @param recipient recipient
 	 * @param mailingID mailing ID (must be <> 0)
 	 * @param additionalBits additional named control bits
 	 * 
 	 * @return UID
 	 */
 	public static final ComExtensibleUID from(final int licenseID, final Recipient recipient, final int mailingID, final NamedUidBit...additionalBits) {
 		long bits = NamedUidBit.namedBitsToLong(additionalBits);
 		
 		return new ComExtensibleUIDImpl(
 				null, 
 				licenseID, 
 				recipient.getCompanyID(), 
 				recipient.getCustomerID(),
 				mailingID, 
 				0, 
 				bits);
 	}

 	
 	/**
 	 * Copies given UID but assigns new mailing ID.
 	 * 
 	 * These method is used by UID string builder to fix a design flaw in some UID versions.
 	 * 
 	 * @param uid UID to copy
 	 * @param mailingId new mailing ID
 	 * 
 	 * @return new UID
 	 */
	public static final ComExtensibleUID copyWithNewMailingID(final ComExtensibleUID uid, final int mailingId) {
		return new ComExtensibleUIDImpl(
 				uid.getPrefix(), 
 				uid.getLicenseID(), 
 				uid.getCompanyID(), 
 				uid.getCustomerID(),
 				mailingId, 
 				uid.getUrlID(), 
 				uid.getBitField());
	}

 	/**
 	 * Creates a UID used to identify a recipient.
 	 * All other properties than given ones are set to 0.
 	 * This method respects the Do-Not-Track flag of the recipient.
 	 * 
 	 * @param prefix UID prefix 
 	 * @param licenseID license ID (must be <> 0)
 	 * @param companyID company ID (must be <> 0) 
 	 * @param customerID customer ID (must be <> 0)
 	 * @param mailingID mailing ID (must be <> 0, if urlID is <> 0)
 	 * @param urlID url ID 
 	 * @param bits list of bits set in bitfield
 	 * 
 	 * @return UID
 	 */
	
	public static final ComExtensibleUID from(final String prefix, final int licenseID, final int companyID, final int customerID, final int mailingID, final int urlID, final NamedUidBit...bits) {
		return new ComExtensibleUIDImpl(
				prefix,
 				licenseID, 
 				companyID, 
 				customerID,
 				mailingID, 
 				urlID, 
 				NamedUidBit.namedBitsToLong(bits));
	}
	
	/**
 	 * Creates a UID used to identify a recipient.
 	 * All other properties than given ones are set to 0.
 	 * This method respects the Do-Not-Track flag of the recipient.
 	 * 
 	 * @param prefix UID prefix 
 	 * @param licenseID license ID (must be <> 0)
 	 * @param companyID company ID (must be <> 0) 
 	 * @param customerID customer ID (must be <> 0)
 	 * @param mailingID mailing ID (must be <> 0, if urlID is <> 0)
 	 * @param urlID url ID 
 	 * @param bitfield bit field
 	 * 
 	 * @return UID
 	 */
	public static final ComExtensibleUID from(final String prefix, final int licenseID, final int companyID, final int customerID, final int mailingID, final int urlID, final long bitfield) {
		return new ComExtensibleUIDImpl(
				prefix,
 				licenseID, 
 				companyID, 
 				customerID,
 				mailingID, 
 				urlID, 
 				bitfield);
	}
	
	/**
 	 * Creates a UID for heatmap used to identify a link in a mailing.
 	 * All other properties (expect customer ID) than given ones are set to 0.
 	 * This method respects the Do-Not-Track flag of the recipient.
	 *
	 * <b>Because customer ID is set to -1 here, do not use this method for other features than Heatmap!</b> 
     *
 	 * @param prefix UID prefix 
 	 * @param licenseID license ID (must be <> 0)
 	 * @param companyID company ID (must be <> 0) 
 	 * @param mailingID mailing ID (must be <> 0, if urlID is <> 0)
 	 * @param urlID url ID 
 	 * 
 	 * @return UID
 	 */
	public static final ComExtensibleUID forHeatmapFrom(final String prefix, final int licenseID, final int companyID, final int mailingID, final int urlID) {
		return new ComExtensibleUIDImpl(
				prefix,
 				licenseID, 
 				companyID, 
 				-1,			// The Heatmap is the only exception where customer ID -1 is allowed!
 				mailingID, 
 				urlID, 
 				0L);
	}

}
