/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agnitas.util.Log;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Container class to keep track of all used social networks and the
 * used icons for this mailing
 */
public class SWYN {
	private static Pattern hostPattern = Pattern.compile("https?://([^:/]+)");

	static class Network implements Comparable<Network> {
		String name;
		String source;
		String charset;
		int order;
		String image;
		byte[] icon;
		String target;
		String code;

		protected Network(String nName, String nSource, String nCharset, int nOrder, String nImage, byte[] nIcon, String nTarget, String nCode) {
			name = nName;
			source = nSource;
			charset = nCharset;
			order = nOrder;
			image = nImage;
			icon = nIcon;
			target = nTarget;
			code = nCode;
		}

		@Override
		public int compareTo(Network other) {
			return order - other.order;
		}
	}

	static class Size {
		Map<String, Network> networks;
		Network[] sorted;

		protected Size() {
			networks = new HashMap<>();
			sorted = null;
		}

		protected void add(Network nw) {
			networks.put(nw.name.toLowerCase(), nw);
		}

		protected void sort() {
			sorted = new Network[networks.size()];

			int n = 0;
			for (Network nw : networks.values()) {
				sorted[n++] = nw;
			}
			Arrays.sort(sorted);
		}
	}

	private Data data;
	private String prefix, infix, postfix;
	private Map<String, Size> sizes;

	/**
	 * Constructor
	 *
	 * @param nData the global configuration
	 */
	public SWYN(Data nData) {
		data = nData;
		prefix = null;
		infix = null;
		postfix = null;
		sizes = new HashMap<>();
	}

	/**
	 * Read in all available social network informations for this company
	 * and collects them in a sorted list (by swyn_tbl.ordering) for the
	 * sequence to use them
	 */
	public void setup() {
		NamedParameterJdbcTemplate jdbc;
		List<Map<String, Object>> rq;
		Map<String, Object> row;
		String query = "SELECT name, source, isize, charset, ordering, image, icon, target, code " + "FROM swyn_tbl " + "WHERE company_id IN (0, :companyID) " + "ORDER BY company_id";

		jdbc = null;
		try {
			jdbc = data.dbase.request(query);
			rq = data.dbase.query(jdbc, query, "companyID", data.company.id());
			for (int n = 0; n < rq.size(); ++n) {
				row = rq.get(n);

				String name = data.dbase.asString(row.get("name"));
				String source = data.dbase.asString(row.get("source"));
				String size = data.dbase.asString(row.get("isize"));
				String charset = data.dbase.asString(row.get("charset"));
				int order = data.dbase.asInt(row.get("ordering"));
				String image = data.dbase.asString(row.get("image"));
				byte[] icon = data.dbase.asBlob(row.get("icon"));
				String target = data.dbase.asString(row.get("target"));
				String code = data.dbase.asString(row.get("code"));

				if ((name != null) && (code != null))
					if (name.startsWith("_")) {
						if (name.equals("__prefix__")) {
							prefix = code;
						} else if (name.equals("__infix__")) {
							infix = code;
						} else if (name.equals("__postfix__")) {
							postfix = code;
						} else {
							data.logging(Log.WARNING, "swyn", "Found unknown control entry " + name);
						}
					} else {
						Network nw = new Network(name, source, charset, order, image, icon, target, code);
						Size sz;

						if (size == null) {
							size = "default";
						}
						sz = sizes.get(size);
						if (sz == null) {
							sz = new Size();
							sizes.put(size, sz);
						}
						sz.add(nw);
						data.logging(Log.DEBUG, "swyn", "Found entry for " + name);
					}
			}
		} catch (Exception e) {
			data.logging(Log.WARNING, "swyn", "Failed to read swyn_tbl: " + e.toString(), e);
		} finally {
			data.dbase.release(jdbc, query);
		}
		for (Size sz : sizes.values()) {
			sz.sort();
		}
	}

	/**
	 * Build the HTML fragment for all networks to be used in this
	 * case. As there can be more than one reference to these
	 * networks (agnSWYN), these are constructed on-the-fly.
	 *
	 * @param bare          if true, do not add extra HTML code (e.g. for building a table) to the output. Useful if the output is embedded in own layout
	 * @param size          the icon size to be used as found in swyn_tbl.isize
	 * @param networksToUse an optional list of network names to be used. If this is null, all available networks are used
	 * @param title         the title to be displayed on the social network, if supported
	 * @param selector      a wildcard pattern to limit the resulting view of the mail to the text blocks which names matching this pattern
	 */
	public String build(boolean bare, String size, List<String> networksToUse, String title, String link, String selector) {
		StringBuffer rc = new StringBuffer(512);
		Size sz = sizes.get(size);
		Network[] use = null;
		int count = 0;
		Map<String, String> extra = new HashMap<>();

		if (sz != null) {
			if (networksToUse != null) {
				use = new Network[networksToUse.size()];
				count = 0;
				for (int n = 0; n < networksToUse.size(); ++n) {
					String name = networksToUse.get(n).toLowerCase();

					if (name != null) {
						Network nw = sz.networks.get(name);

						if (nw != null)
							use[count++] = nw;
					}
				}
			} else {
				use = sz.sorted;
				count = sz.sorted.length;
			}
		}
		if ((count > 0) && (!bare) && (prefix != null)) {
			rc.append(prefix);
		}
		if (use != null) {
			for (int n = 0; n < count; ++n) {
				Network nw = use[n];
				String tmpLink;
				String tmpTarget;
				StringBuffer name = new StringBuffer("SWYN");

				extra.clear();
				if (nw.name != null) {
					name.append(": ");
					name.append(nw.name);
				} else if (nw.source != null) {
					name.append(": ");
					name.append(nw.source);
				}
				name.append("/");
				name.append(size);
				if (link != null) {
					name.append(" (");
					name.append(link);
					name.append(")");
				} else if (selector != null) {
					name.append(" [");
					name.append(selector);
					name.append("]");
				}
				if ((!bare) && (n != 0) && (infix != null))
					rc.append(infix);
				if (title != null) {
					extra.put("title", title);
					extra.put("urltitle", encode(title, nw.charset));
				}
				if (link != null) {
					extra.put("link", link);
					extra.put("urllink", encode(link, "UTF8"));
					tmpLink = link;
				} else {
					String urlLink, append;

					tmpLink = data.anonURL + "uid=";
					urlLink = encode(tmpLink, nw.charset);
					append = "##PUBID";
					if ((nw.source != null) || (selector != null)) {
						append += ":" + (nw.source != null ? nw.source : "");
						if (selector != null)
							append += ":" + selector;
					}
					append += "##";
					extra.put("link", tmpLink + append);
					extra.put("urllink", urlLink + append);
				}
				if (tmpLink != null) {
					Matcher m = hostPattern.matcher(tmpLink);

					if (m.lookingAt()) {
						String host = m.group(1);

						extra.put("host", host);
						extra.put("urlhost", encode(host, nw.charset));
					}
				}
				if (data.company.name() != null) {
					extra.put("urlcompany-name", encode(data.company.name(), nw.charset));
				}
				if (nw.target != null) {
					tmpTarget = data.substituteString(nw.target, extra);
				} else {
					tmpTarget = tmpLink;
				}
				data.requestURL(tmpTarget, name.toString(), false);
				extra.put("target", tmpTarget);
				if (nw.image != null) {
					extra.put("image", nw.image);
					extra.put("urlimage", encode(nw.image, nw.charset));
					if (nw.icon != null) {
						data.requestImage(nw.image, nw.icon);
					}
					extra.put("imagelink", data.defaultImageLink(nw.image, Imagepool.MAILING, true));
				}
				rc.append(data.substituteString(nw.code, extra));
			}
		}
		if ((count > 0) && (!bare) && (postfix != null)) {
			rc.append(postfix);
		}
		return rc.toString();
	}

	private String encode(String s, String charset) {
		String rc;

		if (charset != null) {
			try {
				rc = URLEncoder.encode(s, charset);
			} catch (java.io.UnsupportedEncodingException e) {
				rc = s;
			}
		} else {
			rc = s;
		}
		return rc;
	}
}
