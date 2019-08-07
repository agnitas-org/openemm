/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.preview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageImpl implements Page {
	private Map <String, String>	content;
	private Map <String, String[]>	header;
	private StringBuffer		error;
	private Map <String, Object>	compat;

	public PageImpl () {
		content = new HashMap <> ();
		header = null;
		error = null;
		compat = null;
	}

	@Override
	public void addContent (String key, String value) {
		content.put (key, value);
	}

	@Override
	public void setError (String msg) {
		if (error == null) {
			error = new StringBuffer ();
		}
		error.append (msg);
		error.append ('\n');
	}

	@Override
	public String getError () {
		return error != null ? error.toString () : null;
	}

	@Override
	public Map <String, Object> compatibilityRepresentation () {
		if (compat == null) {
			compat = new HashMap <> ();
			if (content != null) {
				for (String key : content.keySet ()) {
					compat.put (key, content.get (key));
				}
			}
			if (error != null) {
				compat.put (Preview.ID_ERROR, error.toString ());
			}
		}
		return compat;
	}

	/** Pattern to find entities to escape */
	static private Pattern		textReplace = Pattern.compile ("[&<>'\"]");
	/** Values to escape found entities */
	static private Map <String, String>
					textReplacement = new HashMap <> ();
	static {
		textReplacement.put ("&", "&amp;");
		textReplacement.put ("<", "&lt;");
		textReplacement.put (">", "&gt;");
		textReplacement.put ("'", "&apos;");
		textReplacement.put ("\"", "&quot;");
	}
	/** escapeEntities
	 * This method escapes the HTML entities to be displayed
	 * in a HTML context
	 * @param s the input string
	 * @return null, if input string had been null,
	 *		   the escaped version of s otherwise
	 */
	private String escapeEntities (String s) {
		if (s != null) {
			int		slen = s.length ();
			Matcher		m = textReplace.matcher (s);
			StringBuffer	buf = new StringBuffer (slen + 128);
			int		pos = 0;

			while (m.find (pos)) {
				int next = m.start ();
				String	ch = m.group ();

				if (pos < next)
					buf.append (s.substring (pos, next));
				buf.append (textReplacement.get (ch));
				pos = m.end ();
			}
			if (pos != 0) {
				if (pos < slen)
					buf.append (s.substring (pos));
				s = buf.toString ();
			}
		}
		return s;
	}

	/** encode
	 * Encodes a string to a byte stream using the given character set,
	 * if escape is true, HTML entities are escaped prior to encoding
	 * @param s the string to encode
	 * @param charset the character set to convert the string to
	 * @param escape if HTML entities should be escaped
	 * @return the coded string as a byte stream
	 */
	private byte[] encode (String s, String charset, boolean escape) {
		if (escape && (s != null)) {
			s = "<pre>\n" + escapeEntities (s) + "</pre>\n";
		}
		try {
			return s == null ? null : s.getBytes (charset);
		} catch (java.io.UnsupportedEncodingException e) {
			return null;
		}
	}

	/** get
	 * a null input save conversion variant
	 * @param s the input string
	 * @param escape to escape HTML entities
	 * @return the converted string
	 */
	private String convert (String s, boolean escape) {
		if (escape && (s != null)) {
			return escapeEntities (s);
		}
		return s;
	}

	static private Pattern	searchHTML = Pattern.compile ("<[^>]+>");
	static private Pattern	searchLink = Pattern.compile ("=\"?(https?://[^ \t\"]+)\"?", Pattern.CASE_INSENSITIVE);
	private String stripper (String s) {
		Matcher		m = searchLink.matcher (s);
		int		slen = s.length ();
		StringBuffer	buf = null;
		int		pos = 0;
			
		while (m.find (pos)) {
			if (buf == null) {
				buf = new StringBuffer (slen);
			}
			if (pos < m.start ())
				buf.append (s.substring (pos, m.start ()));
			buf.append (s.substring (m.start (), m.start (1)));
			buf.append ('#');
			buf.append (s.substring (m.end (1), m.end ()));
			pos = m.end ();
		}
		if (buf != null) {
			if (pos < slen)
				buf.append (s.substring (pos));
			s = buf.toString ();
		}
		return s;
	}
	
	protected String strip (String html) {
		if (html != null) {
			Matcher		m = searchHTML.matcher (html);
			int		htmlLength = html.length ();
			StringBuffer	buf = new StringBuffer (htmlLength);
			int		pos = 0;
			
			while (m.find (pos)) {
				if (pos < m.start ())
					buf.append (html.substring (pos, m.start ()));
				buf.append (stripper (html.substring (m.start (), m.end ())));
				pos = m.end ();
			}
			if (pos != 0) {
				if (pos < htmlLength)
					buf.append (html.substring (pos));
				html = buf.toString ();
			}
		}
		return html;
	}

	/**
	 * Get header-, text- or HTML-part from hashtable created by
	 * createPreview as byte stream
	 */
	@Override
	public byte[] getPartByID (String id, String charset, boolean escape) {
		return encode (content.get (id), charset, escape);
	}
	@Override
	public byte[] getHeaderPart (String charset, boolean escape) {
		return getPartByID (Preview.ID_HEAD, charset, escape);
	}
	@Override
	public byte[] getHeaderPart (String charset) {
		return getHeaderPart (charset, false);
	}
	@Override
	public byte[] getTextPart (String charset, boolean escape) {
		return getPartByID (Preview.ID_TEXT, charset, escape);
	}
	@Override
	public byte[] getTextPart (String charset) {
		return getTextPart (charset, false);
	}
	@Override
	public byte[] getHTMLPart (String charset, boolean escape) {
		return getPartByID (Preview.ID_HTML, charset, escape);
	}
	@Override
	public byte[] getHTMLPart (String charset) {
		return getHTMLPart (charset, false);
	}
	/**
	 * Get header-, text- or HTML-part as strings
	 */
	@Override
	public String getByID (String id, boolean escape) {
		return convert (content.get (id), escape);
	}
	@Override
	public String getStrippedByID (String id, boolean escape) {
		return convert (strip (content.get (id)), escape);
	}

	@Override
	public String getHeader (boolean escape) {
		return getByID (Preview.ID_HEAD, escape);
	}
	@Override
	public String getHeader () {
		return getHeader (false);
	}

	@Override
	public String getText (boolean escape) {
		return getByID (Preview.ID_TEXT, escape);
	}
	@Override
	public String getText () {
		return getText (false);
	}
	@Override
	public String getHTML (boolean escape) {
		return getByID (Preview.ID_HTML, escape);
	}
	@Override
	public String getHTML () {
		return getHTML (false);
	}
	@Override
	public String getStrippedHTML (boolean escape) {
		return getStrippedByID (Preview.ID_HTML, escape);
	}
	@Override
	public String getStrippedHTML () {
		return getStrippedHTML (false);
	}

	/**
	 * Get attachment names and content
	 */
	private boolean isID (String name) {
		return name.startsWith ("__") && name.endsWith ("__");
	}
	private String[] getList (boolean asAttachemnts) {
		ArrayList <String>	collect = new ArrayList<>();

		for (String name : content.keySet ()) {
			if (isID (name) != asAttachemnts) {
				collect.add (name);
			}
		}
		return collect.toArray (new String[collect.size ()]);
	}
	@Override
	public String[] getIDs () {
		return getList (false);
	}
	@Override
	public String[] getAttachmentNames () {
		return getList (true);
	}

	@Override
	public byte[] getAttachment (String name) {
		if (isID (name) || (! content.containsKey (name))) {
			return null;
		}

		byte[]	rc = null;
		String	coded = content.get (name);

		if (coded != null) {
			String	valid = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
			byte[]	temp = new byte[coded.length ()];
			int tlen = 0;
			long	val;
			int count;
			int pad;
			byte	pos;

			val = 0;
			count = 0;
			pad = 0;
			for (int n = 0; n < coded.length (); ++n) {
				char	ch = coded.charAt (n);

				if (ch == '=') {
					++pad;
					++count;
				} else if ((pos = (byte) valid.indexOf (ch)) != -1) {
					switch (count++) {
					case 0:
						val = pos << 18;
						break;
					case 1:
						val |= pos << 12;
						break;
					case 2:
						val |= pos << 6;
						break;
					case 3:
						val |= pos;
						break;
					}
				}
				if (count == 4) {
					temp[tlen] = (byte) ((val >> 16) & 0xff);
					temp[tlen + 1] = (byte) ((val >> 8) & 0xff);
					temp[tlen + 2] = (byte) (val & 0xff);
					tlen += 3 - pad;
					count = 0;
					if (pad > 0)
						break;
				}
			}
			rc = Arrays.copyOf (temp, tlen);
		}
		return rc;
	}

	private synchronized void parseHeader () {
		if (header == null) {
			String	head = content.get (Preview.ID_HEAD);

			header = new HashMap <> ();
			if (head != null) {
				String[]	lines = head.split ("\r?\n");
				String		cur = null;

				for (int n = 0; n <= lines.length; ++n) {
					String	line = (n < lines.length ? lines[n] : null);

					if ((line == null) || ((line.indexOf (' ') != 0) && (line.indexOf ('\t') != 0))) {
						if (cur != null) {
							String[]	parsed = cur.split (": +", 2);

							if (parsed.length == 2) {
								String		key = parsed[0].toLowerCase ();
								String[]	field = header.get (key);
								int		nlen = (field == null ? 1 : field.length + 1);
								String[]	nfield = new String[nlen];

								if (field != null)
									for (int m = 0; m < field.length; ++m)
										nfield[m] = field[m];
								nfield[nlen - 1] = parsed[1];
								header.put (key, nfield);
							}
						}
						cur = line;
					} else if (cur != null) {
						cur += '\n' + line;
					}
				}
			}
		}
	}

	/**
	 * Get individual lines from the header
	 */
	@Override
	public String[] getHeaderFields (String field) {
		parseHeader ();
		return	header.get (field.toLowerCase ());
	}

	@Override
	public String getHeaderField (String field, boolean escape) {
		String		rc = null;
		String[]	head = getHeaderFields (field);

		if ((head != null) && (head.length > 0)) {
			rc = escape ? escapeEntities (head[0]) : head[0];
		}
		return rc;
	}

	@Override
	public String getHeaderField (String field) {
		return getHeaderField (field, false);
	}
}
