/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.xbill.DNS.InvalidTypeException;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import org.xbill.DNS.Name;
/**
 * This class provieds an easy way to access DNS records
 */
public class DNS {
	private SimpleResolver	resolv;
	private Log		log;
	
	/**
	 * Constructor
	 * 
	 * @param timeoutInSeconds the timeout for queries in seconds, 0 means infinite
	 * @param nLog             optional Log instance for logging
	 */
	public DNS (int timeoutInSeconds, Log nLog) {
		log = nLog;
		if (timeoutInSeconds > 0) {
			try {
				resolv = new SimpleResolver ();
				resolv.setTimeout (Duration.ofSeconds (timeoutInSeconds));
			} catch (UnknownHostException e) {
				if (log != null) {
					log.out (Log.WARNING, "dns", "Failed to find proper DNS host: " + e.toString ());
				}
				resolv = null;
			}
		} else {
			resolv = null;
		}
	}
	/**
	 * Constructor
	 * 
	 * @param timeoutInSeconds the timeout for queries in seconds
	 */
	public DNS (int timeoutInSeconds) {
		this (timeoutInSeconds, null);
	}
	/**
	 * Constructor
	 * 
	 */
	public DNS () {
		this (0);
	}

	/**
	 * Query a text record for the given domain from DNS
	 * 
	 * @param domain the domain to lookup the DNS for a text record
	 * @return       the text record content, if found, null otherwise
	 */
	public String queryText (String domain) {
		String		rc = null;
		Record[]	r = query (domain, Type.TXT);
		
		if (r != null) {
			rc = "";
			for (int n = 0; n < r.length; ++n) {
				List <String>	answ = ((TXTRecord) r[n]).getStrings ();
				
				for (int m = 0; m < answ.size (); ++m) {
					String	s = answ.get (m);
					
					if ((s != null) && (s.length () > 0)) {
						rc += s;
					}
				}
			}
		}
		return rc;
	}

	public Map<Integer,String> queryMxLegacy(String domain) {
		Map<Integer, String> mapResult = new LinkedHashMap<>();

		List<Name> result = new ArrayList<>();
		Record [] records =  query(domain, Type.MX);

		if(Objects.nonNull(records)) {
			for (Record rec : records) {
				MXRecord mx = (MXRecord) rec;
				mapResult.put(mx.getPriority(), mx.getTarget().toString());
				result.add(mx.getTarget());
			}
		}
		return mapResult;
	}

	private String typeString (int type) {
		try {
			return Type.string (type);
		} catch (InvalidTypeException e) {
			return "" + type;
		}
	}
	private Record[] query (String q, int type) {
		Lookup	l;
		
		try {
			l = new Lookup (q, type);
			if (resolv != null) {
				l.setResolver (resolv);
			}
		} catch (TextParseException e) {
			if (log != null) {
				log.out (Log.INFO, "dns", "Failed to parse query \"" + q + "\" (" + typeString (type) + "): " + e.toString ());
			}
			l = null;
		}
		return l != null ? l.run () : null;
	}
}
