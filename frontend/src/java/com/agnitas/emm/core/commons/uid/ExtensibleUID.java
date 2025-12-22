/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.uid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Recipient;

/**
 * UID interface with EMM extensions.
 */
public interface ExtensibleUID {
	
	/**
	 * Enum to name bits of the bit field.
	 */
	enum NamedUidBit {

		/**
		 * Was previously <i>Do not track</i> bit.
		 *
		 * Do not use this bit for links in mailings.
		 * The tracking veto state for recipients is determined from DB.
		 *
		 * This bit is currently for internal purpose only to propagate the tracking veto state
		 * in a performant way. This should be not longer done, too.
		 *
		 * @see Recipient#isDoNotTrackMe()
		 * @see Recipient#setDoNotTrackMe(boolean)
		 */
		@Deprecated
		RESERVED(0),
		
		/** <i>No link extension</i> bit. */
		NO_LINK_EXTENSION(1);
		
		/** Position of the bit. */
		private final int bit;
		
		/**
		 * Creates named bit.
		 *
		 * @param bitNumber number of the bit
		 */
		NamedUidBit(final int bitNumber) {
			this.bit = bitNumber;
		}
		
		/**
		 * Returns the number of the named bit.
		 *
		 * @return position
		 */
		public final int getBitPosition() {
			return this.bit;
		}
		
		/**
		 * Converts a list of {@link NamedUidBit}s to a corresponding long value.
		 *
		 * @param bits list of {@link NamedUidBit}s (can contain <code>null</code> values, which are ignored)
		 *
		 * @return corresponding long value
		 *
		 * @throws NullPointerException if one of the given {@link NamedUidBit}s is <code>null</code>
		 */
		public static final long namedBitsToLong(final NamedUidBit...bits) {
			long l = 0L;
		 		
	 		for(final NamedUidBit b : bits) {
	 			if(b != null) {
	 				l = setBit(l, b);
	 			}
	 		}
	 		
	 		return l;
		}

		public static final Set<NamedUidBit> longToSet(final long value) {
			return Arrays.asList(values())
					.stream()
					.filter(bit -> isBitSet(value, bit))
					.collect(Collectors.toSet());
		}
		
		public static final boolean isBitSet(final long value, final NamedUidBit bit) {
			return (value & (1L << bit.getBitPosition())) != 0L;
		}
		
		public static final long setBit(final long value, final NamedUidBit bit) {
			return value | (1L << bit.getBitPosition());
		}
		
		public static final Collection<NamedUidBit> bitsFromLong(final long value) {
			final List<NamedUidBit> list = new ArrayList<>();
			
			for(final NamedUidBit bit : values()) {
				if(isBitSet(value, bit)) {
					list.add(bit);
				}
			}
			
			return list;
		}
		
	}
	
	/**
	 * Returns the prefix.
	 *
	 * @return prefix
	 */
	String getPrefix();

	/**
	 * Returns the company ID.
	 *
	 * @return company ID
	 */
	int getCompanyID();

	/**
	 * Returns the customer ID.
	 *
	 * @return customer ID
	 */
	int getCustomerID();

	/**
	 * Returns the mailing ID.
	 *
	 * @return mailing ID
	 */
	int getMailingID();

	/**
	 * Returns the URL ID.
	 *
	 * @return URL ID
	 */
	int getUrlID();
	
	/**
	 * Returns the position for this URL in the related content block, starting by 1
	 * 
	 * @return position
	 */
	int getPosition();
	
	/**
	 * Returns the license ID.
	 *
	 * @return license ID
	 */
	int getLicenseID();
	
	/**
	 * Returns the value of the bit field.
	 *
	 * @return value of the bit field
	 */
	long getBitField();
	
	/**
	 * Returns the senddate in seconds(!) since 1.1.1970 (the unix epoch)
	 * 
	 * @return the senddate or 0, if not set
	 */
	long getSendDate ();
	
	/**
	 * Update UID seting a send date
	 * 
	 * @return the instance of the ExtensibleUID for daisy chaining
	 */
	ExtensibleUID setSendDate (long sendDate);
	
	/**
	 * Returns the maildrop_status_tbl.status_field from sent mailing
	 * 
	 * @return the status field or '\0', if not set
	 */
	char getStatusField ();
	
	/**
	 * Update UID seting a status field
	 * 
	 * @return the instance of the ExtensibleUID for daisy chaining
	 */
	ExtensibleUID setStatusField (char statusField);
}
