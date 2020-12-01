/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.util.HashMap;
import java.util.Map;

import org.agnitas.util.Bit;
import org.agnitas.util.Log;

import gnu.trove.map.TLongLongMap;
import gnu.trove.map.hash.TLongLongHashMap;

/**
 * A memory efficinet storing for all customer to keep
 * track of the accepted media for each customer
 */
public class MediaMap {
	private Data data;
	private long mcount;
	private TLongLongMap mapping;
	private Map<Long, String> names;
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
		names = new HashMap<>();
		unset = mapping.getNoEntryValue();
	}

	/**
	 * Add a mediatype for a customer
	 *
	 * @param cust_id the customerID to add the mediatype to
	 * @param mtype   the mediatype accepted by this customer
	 */
	public void add(long cust_id, int mtype) {
		long mid;

		if ((mid = mapping.get(cust_id)) != unset) {
			mid = Bit.set(mid, mtype);
		} else {
			mid = Bit.bitmask(mtype);
			++mcount;
			if ((mcount % 50000) == 0)
				data.logging(Log.VERBOSE, "mmap", "Currently read " + mcount + " bindings");
		}
		mapping.put(cust_id, mid);
	}

	/**
	 * Create a list of accepted media types for the given customer
	 * in the order they are definied in the mailing_mt_tbl
	 *
	 * @param cust_id the customerID of the customer to retrieve the information for
	 * @return a string of a comma separated list of accepted media types
	 */
	public String get(long cust_id) {
		String rc = null;
		long media_id;

		if ((media_id = mapping.get(cust_id)) != unset) {
			if ((rc = names.get(media_id)) == null) {
				String str = null;

				for (Media m : data.media()) {
					if ((m.stat == Media.STAT_ACTIVE) && Bit.isset(media_id, m.type)) {
						if (str != null) {
							str += "," + m.typeName();
						} else {
							str = m.typeName();
						}
					}
				}
				if (str != null) {
					names.put(media_id, str);
					rc = str;
				}
			}
		}
		return rc != null ? rc : data.getDefaultMediaType();
	}

	/**
	 * Return a list of all active media types fro this mailing
	 *
	 * @return a string of a comma separated list of active media types
	 */
	public String getActive() {
		String rc = null;

		for (Media m : data.media()) {
			if (m.stat == Media.STAT_ACTIVE)
				if (rc != null)
					rc += "," + m.typeName();
				else
					rc = m.typeName();
		}
		return rc;
	}
}
