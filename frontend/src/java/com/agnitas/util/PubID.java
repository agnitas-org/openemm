/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides the generation and decoding of an agnPUBID,
 * an ID like the agnUID, but used for public accessable information.
 * At this state, this is used to access an anonymous fullview as
 * posted in a social network. The ID provdies the information of
 * the recipient of whom the data should be accessed and is not
 * intended to be a secure piece of information.
 * 
 * The pubID transports the mailingID of the related mailing and
 * the customerID of the recipient. An optional source (free text)
 * to indicate from where the ID is used (e.g. a short name of the
 * social network) and on optional parameter which can be used by
 * the code which processes this ID and depends on the context it
 * is used in.
 */
public class PubID {
	private final static String	cl = "w5KMCHOXE_PTuLcfF6D1ZI3BydeplQaztVAnUj0bqos7k49YgWhxiS-RrGJm8N2v";
	private final static Pattern	srcInvalid = Pattern.compile ("[^0-9a-zA-Z_-]");
	private long			mailingID;
	private long			customerID;
	private String			source;
	private String			parm;
	
	public PubID () {
		mailingID = -1;
		customerID = -1;
		source = null;
		parm = null;
	}
	
	public long getMailingID () {
		return mailingID;
	}
	public void setMailingID (long nMailingID) {
		mailingID = nMailingID;
	}
	public long getCustomerID () {
		return customerID;
	}
	public void setCustomerID (long nCustomerID) {
		customerID = nCustomerID;
	}
	public String getSource () {
		return source;
	}
	public void setSource (String nSource) {
		if (nSource == null) {
			source = nSource;
		} else {
			Matcher	m = srcInvalid.matcher (nSource);
		
			if (! m.find ()) {
				source = nSource;
			} else {
				StringBuffer	scratch = new StringBuffer (nSource.length ());
				int		last, pos;
			
				last = 0;
				do {
					pos = m.start ();
					if (last < pos)
						scratch.append (nSource.substring (last, pos));
					scratch.append ('_');
					last = ++pos;
				}	while (m.find (pos));
				if (pos < nSource.length ())
					scratch.append (nSource.substring (pos));
				source = scratch.toString ();
			}
			if ((source != null) && (source.length () > 20)) {
				source = source.substring (0, 20);
			}
		}
	}
	public String getParm () {
		return parm;
	}
	public void setParm (String nParm) {
		parm = nParm;
	}

	public String createID () {
		String	src = mailingID + ";" + customerID + ";" +
			      (source == null ? "" : source) + (parm == null || parm.length () == 0 ? "" : ";" + parm);
		return encode (src);
	}
	
	public String createID (long cMailingID, long cCustomerID, String cSource, String cParm) {
		setMailingID (cMailingID);
		setCustomerID (cCustomerID);
		setSource (cSource);
		setParm (cParm);
		return createID ();
	}

	public String createID (long cMailingID, long cCustomerID, String cSource) {
		return createID (cMailingID, cCustomerID, cSource, null);
	}

	/**
	 * parses a uid and populate its instance variables
	 * with the content of the ID, if parsing had been
	 * successful.
	 * 
	 * @param pid the pubID to be parsed
	 * @return    true, if parsing had been successful, false otherwise
	 */
	public boolean parseID (String pid) {
		boolean	rc;
		String	dst = decode (pid);

		rc = false;
		if (dst != null) {
			String[]	parts = dst.split (";", 4);
		
			if ((parts.length == 3) || (parts.length == 4)) {
				long	tMailingID;
				long	tCustomerID;
				String	tSource;
				String	tParm;
				
				try {
					tMailingID = Long.parseLong (parts[0]);
					tCustomerID = Long.parseLong (parts[1]);
					tSource = parts[2];
					tParm = parts.length == 4 ? parts[3] : null;
					if ((tMailingID > 0) && (tCustomerID > 0)) {
						setMailingID (tMailingID);
						setCustomerID (tCustomerID);
						setSource (tSource);
						setParm (tParm);
						rc = true;
					}
				} catch (NumberFormatException e) {
					// do nothing
				}
			}
		}
		return rc;
	}

	private char checksum (String s) {
		int	cs;
		int	slen;
		
		cs = 12;
		slen = s.length ();
		for (int n = 0; n < slen; ++n) {
			cs += s.charAt (n);
			
		}
		return cl.charAt (cs & 0x3f);
	}
	
	private String encode (String s) {
		String	rc;
		
		try {
			byte[]		dump = s.getBytes ("UTF8");
			StringBuffer	temp = new StringBuffer (((dump.length + 3) * 4) / 3);

			for (int n = 0; n < dump.length; n += 3) {
				int	d = ((dump[n] & 0xff) << 16) |
					    ((n + 1 < dump.length ? dump[n + 1] & 0xff : 0) << 8) |
					    (n + 2 < dump.length ? dump[n + 2] & 0xff : 0);
				int	c1 = (d >> 18) & 0x3f,
					c2 = (d >> 12) & 0x3f,
					c3 = (d >> 6) & 0x3f,
					c4 = d & 0x3f;
				temp.append (cl.charAt (c1));
				temp.append (cl.charAt (c2));
				temp.append (cl.charAt (c3));
				temp.append (cl.charAt (c4));
			}
			temp.insert (5, checksum (temp.toString ()));
			rc = temp.toString ();
		} catch (java.io.UnsupportedEncodingException e) {
			rc = null;
		}
		return rc;
	}
	
	private String decode (String s) {
		String	rc;
		int	slen;
		
		rc = null;
		slen = s.length ();
		if ((slen > 5) && (((slen - 1) & 3) == 0)) {
			boolean	st = true;
			char	check = s.charAt (5);
			byte[]	collect;
			int	pos;
			
			s = s.substring (0, 5) + s.substring (6);
			if (check == checksum (s)) {
				--slen;
				collect = new byte[slen];
				pos = 0;
				for (int n = 0; st && (n < slen); n += 4) {
					int	v1 = cl.indexOf (s.charAt (n)),
						v2 = cl.indexOf (s.charAt (n + 1)),
						v3 = cl.indexOf (s.charAt (n + 2)),
						v4 = cl.indexOf (s.charAt (n + 3));

					if ((v1 == -1) || (v2 == -1) || (v3 == -1) || (v4 == -1))
						st = false;
					else {
						int	v = (v1 << 18) | (v2 << 12) | (v3 << 6) | v4;

						collect[pos++] = (byte) ((v >> 16) & 0xff);
						collect[pos++] = (byte) ((v >> 8) & 0xff);
						collect[pos++] = (byte) (v & 0xff);
					}
				}
				if (st) {
					while ((pos > 0) && (collect[pos - 1] == 0))
						--pos;
					try {
						rc = new String (collect, 0, pos, "UTF8");
					} catch (java.io.UnsupportedEncodingException e) {
						rc = null;
					}
				}
			}
		}
		return rc;
	}
}
