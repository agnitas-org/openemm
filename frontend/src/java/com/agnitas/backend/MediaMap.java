/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

import java.util.HashMap;
import java.util.Map;

import com.agnitas.util.Log;

import gnu.trove.map.TLongLongMap;
import gnu.trove.map.hash.TLongLongHashMap;

/**
 * A memory efficinet storing for all customer to keep
 * track of the accepted media for each customer
 * 
 * As the whole information for all recipients is hold in memory, this
 * is optimized for storage size. So for each customer an entry in the
 * private mapping is created where the customerID is the key to
 * reference the mapping. The value should be viewd as sequence of
 * "userStatusBits" bit blocks for each mediatype. So mediatype 0 is
 * using bit 0 to userStatusBits - 1, mediatype 1 is using bit
 * userStatusBits to userStatusBits * 2 - 1 and so on. Each bit block is
 * considered as valid (i.e. the customer has an entry for this
 * media) if the value is not zero and this also represents the
 * current user_status for this recipient.
 */
public class MediaMap {
	static private final int	userStatusBits = 8;
	static private final int	userStatusMask = (1 << userStatusBits) - 1;
	static class MMEntry {
		String	name;
		String	status;
		public MMEntry (String nName, String nStatus) {
			name = nName;
			status = nStatus;
		}
	}
	private Data data;
	private long mcount;
	private TLongLongMap mapping;
	private Map<Long, MMEntry> entries;
	private MMEntry defaultEntry;
	private long unset;

	/**
	 * Constructor
	 *
	 * @param ndata the global configuration
	 */
	public MediaMap(Data ndata) {
		data = ndata;
		mcount = 0;
		mapping = new TLongLongHashMap();
		entries = new HashMap<>();
		defaultEntry = new MMEntry (data.getDefaultMediaType(), "1");
		unset = mapping.getNoEntryValue();
	}
	
	/**
	 * Add a mediatype for a customer
	 *
	 * @param customerID     the customerID to add the mediatype to
	 * @param userStatus  the user status for this media
	 * @param mediaType   the mediatype accepted by this customer
	 */
	public void add(long customerID, int userStatus, int mediaType) {
		long mid;

		if ((mid = mapping.get(customerID)) != unset) {
			mid |= token_encode (userStatus, mediaType);
		} else {
			mid = token_encode (userStatus, mediaType);
			++mcount;
			if ((mcount % 50000) == 0)
				data.logging(Log.VERBOSE, "mmap", "Currently read " + mcount + " bindings");
		}
		mapping.put(customerID, mid);
	}

	/**
	 * Create a list of accepted media types for the given customer
	 * in the order they are definied in the mailing_mt_tbl
	 *
	 * @param customerID the customerID of the customer to retrieve the information for
	 * @return a string of a comma separated list of accepted media types
	 */
	public MMEntry get(long customerID) {
		MMEntry rc = null;
		long token;

		if ((token = mapping.get(customerID)) != unset) {
			if ((rc = entries.get(token)) == null) {
				String names = null;
				String status = null;

				for (Media m : data.media()) {
					if (m.stat == Media.STAT_ACTIVE) {
						int	userStatus = token_decode (token, m.type);
						
						if (userStatus != 0) {
							if (names != null) {
								names += "," + m.typeName();
								status += "," + userStatus;
							} else {
								names = m.typeName();
								status = Integer.toString (userStatus);
							}
						}
					}
				}
				if ((names != null) && (status != null)) {
					rc = new MMEntry (names, status);
					entries.put (token, rc);
				}
			}
		}
		return rc != null ? rc : defaultEntry;
	}

	/**
	 * Return a list of all active media types fro this mailing
	 *
	 * @return a string of a comma separated list of active media types
	 */
	public MMEntry getActive() {
		String	names = null;
		String	status = null;

		for (Media m : data.media()) {
			if (m.stat == Media.STAT_ACTIVE)
				if (names != null) {
					names += "," + m.typeName ();
					status += ",1";
				} else {
					names = m.typeName ();
					status = "1";
				}
		}
		return names != null ? new MMEntry (names, status) : null;
	}

	private long token_encode (int status, int mediaType) {
		return ((long) (status & userStatusMask)) << (mediaType * userStatusBits);
	}
	private int token_decode (long token, int mediaType) {
		return (int) ((token >> (mediaType * userStatusBits)) & userStatusMask);
	}
	
	public String verify (int status, int mediaType) {
		long	token = token_encode (status, mediaType);
		String	rc = "Token is " + token + "\n";
		
		for (int mt = 0; mt < 8; ++mt) {
			rc += "Mediatype:" + mt + (mt == mediaType ? " (selected)" : "") + " status is " + token_decode (token, mt) + "\n";
		}
		return rc;
	}
}
