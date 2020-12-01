/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.preview;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.agnitas.backend.Mailgun;
import org.agnitas.backend.MailgunImpl;
import org.agnitas.util.Log;
import org.agnitas.util.PubID;
import org.apache.log4j.Logger;

public class PreviewImpl implements Preview {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(PreviewImpl.class);

	private static Pattern linkSearch = Pattern.compile("<[ \t\n\r]*([a-z][a-z0-9_-]*)([ \t\n\r]+[^>]*[a-z_][a-z0-9_-]*=)(\"http://[^\"]+\"|http://[^> \t\n\r]+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

	/**
	 * PCache (Page Cache)
	 * This class is used to cache full generated pages for a single
	 * customer
	 */
	static class PCache {
		static class PEntry {
			protected long timestamp;
			protected Page page;

			protected PEntry(long nTimestamp, Page nPage) {
				timestamp = nTimestamp;
				page = nPage;
			}
		}

		private int maxAge;
		private int maxEntries;
		private int size;
		private Map<String, PEntry> cache;
		private Log previewLogger;
		private String logid;

		protected PCache(int nMaxAge, int nMaxEntries) {
			maxAge = nMaxAge;
			maxEntries = nMaxEntries;
			size = 0;
			cache = new HashMap<>();
			previewLogger = null;
			logid = "cache";
		}

		protected void done() {
			cache.clear();
			size = 0;
		}

		protected void setLogger(Log nLogger, String nLogid) {
			previewLogger = nLogger;
			if (nLogid != null) {
				logid = nLogid;
			}
		}

		private void log(int level, String msg) {
			if (previewLogger != null) {
				previewLogger.out(level, logid, msg);
			}
		}

		protected Page find(long mailingID, long customerID, String selector, long now) {
			String key = mkKey(mailingID, customerID, selector);
			PEntry ent = cache.get(key);
			Page rc = null;

			if (ent != null) {
				log(Log.DEBUG, "Found entry for key \"" + key + "\"");
				if (ent.timestamp + maxAge < now) {
					cache.remove(key);
					--size;
					log(Log.DEBUG, "Entry is too old, remove it from cache, remaining cachesize is " + size);
				} else {
					rc = ent.page;
				}
			} else {
				log(Log.DEBUG, "No page in cache found for key \"" + key + "\"");
			}
			return rc;
		}

		protected void store(long mailingID, long customerID, String selector, long now, Page page) {
			String key = mkKey(mailingID, customerID, selector);
			PEntry ent;

			while (size + 1 > maxEntries) {
				PEntry match = null;
				String matchKey = null;

				for (Map.Entry<String, PEntry> chk : cache.entrySet()) {
					PEntry entry = chk.getValue();

					if ((match == null) || (match.timestamp > entry.timestamp)) {
						match = entry;
						matchKey = chk.getKey();
					}
				}
				if (matchKey != null) {
					log(Log.DEBUG, "Shrink cache as there are currently " + size + " of " + maxEntries + " possible cache elements");
					cache.remove(matchKey);
					--size;
				} else {
					log(Log.DEBUG, "Failed shrinking cache, even it has " + size + " of " + maxEntries + " elements");
					break;
				}
			}
			ent = new PEntry(now, page);
			cache.put(key, ent);
			++size;
			log(Log.DEBUG, "Store page for key \"" + key + "\" in cache, cache has now " + size + " elements");
		}

		protected int getSize() {
			return size;
		}

		private String mkKey(long mailingID, long customerID, String selector) {
			return "[" + mailingID + "/" + customerID + "]" + (selector == null ? "" : ":" + selector);
		}
	}

	/**
	 * limited list for caching mailings
	 */
	private Cache mhead, mtail;
	/**
	 * max age in seconds for an entry in the cache
	 */
	private int maxAge;
	/**
	 * max number of entries in the cache
	 */
	private int maxEntries;
	/**
	 * current number of entries
	 */
	private int msize;
	/**
	 * cache for generated pages
	 */
	private PCache pcache;
	/**
	 * cache for generated anon pages
	 */
	private PCache acache;
	/**
	 * last statistics report
	 */
	private long lastrep;
	/**
	 * logger
	 */
	protected Log log;

	private boolean scaSet;
	private boolean sca;

	/**
	 * converts a string to an interger, using a default value
	 * on errors or unset input
	 *
	 * @param s    the string to convert
	 * @param dflt the default, if string is unset or unparsable
	 * @return the integer for the input string
	 */
	protected int atoi(String s, int dflt) {
		int rc;

		if (s == null) {
			rc = dflt;
		} else {
			try {
				rc = Integer.parseInt(s);
			} catch (NumberFormatException e) {
				rc = dflt;
			}
		}
		return rc;
	}

	/**
	 * converts a string to a boolean, using a default value
	 * on unset input
	 *
	 * @param s    the string to convert
	 * @param dflt the default, if string is unset
	 * @return the integer for the input string
	 */
	protected boolean atob(String s, boolean dflt) {
		boolean rc;

		if (s == null) {
			rc = dflt;
		} else if ((s.length() == 0) || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("on")) {
			rc = true;
		} else {
			char ch = s.charAt(0);

			rc = ((ch == 'T') || (ch == 't') || (ch == 'Y') || (ch == 'y') || (ch == '1') || (ch == '+'));
		}
		return rc;
	}

	/**
	 * getRsc
	 * retrieves a value from resource bundle, if available
	 *
	 * @param rsc  the resource bundle
	 * @param keys the keys in this bundle
	 * @param key  the key of the value to retrieve
	 * @return the value, if available, otherwise null
	 */
	protected String getRsc(ResourceBundle rsc, Set<String> keys, String... keylist) {
		return Stream.of(keylist).filter((k) -> keys.contains(k)).findFirst().map((k) -> rsc.getString(k)).orElse(null);
	}

	protected void setFromResource(ResourceBundle rsc, Set<String> keys) {
		String val = getRsc(rsc, keys, "preview.frontend");
		if (val != null) {
			sca = atob(val, sca);
			scaSet = true;
		}
	}

	/**
	 * PreviewImpl
	 * the constructor reading the configuration
	 * from emm.properties
	 */
	public PreviewImpl(String mailoutCacheAge, String mailoutCacheSize, String pageCacheAge, String pageCacheSize, String logName, String logLevel) {
		String age = mailoutCacheAge;
		String size = mailoutCacheSize;
		String pcage = pageCacheAge;
		String pcsize = pageCacheSize;
		String acage = null;
		String acsize = null;
		String logname = logName;
		String loglevel = logLevel;
		String rscerror = null;
		try {
			ResourceBundle rsc;
			Set<String> keys;

			rsc = ResourceBundle.getBundle("emm");
			if (rsc != null) {
				keys = rsc.keySet();
				acage = getRsc(rsc, keys, "preview.anon.cache.age");
				acsize = getRsc(rsc, keys, "preview.anon.cache.size");
				setFromResource(rsc, keys);
			}
		} catch (Exception e) {
			rscerror = e.toString();
		}
		mhead = null;
		mtail = null;
		maxAge = atoi(age, 300);
		maxEntries = atoi(size, 20);
		msize = 0;
		pcache = new PCache(atoi(pcage, 120), atoi(pcsize, 50));
		acache = new PCache(atoi(acage, 120), atoi(acsize, 250));

		if (logname == null) {
			logname = "preview";
		}
		int level;
		if (loglevel == null) {
			level = Log.INFO;
		} else {
			try {
				level = Log.matchLevel(loglevel);
			} catch (NumberFormatException e) {
				level = Log.INFO;
			}
		}
		lastrep = 0;
		log = new Log(logname, level);
		if (rscerror != null) {
			log.out(Log.ERROR, "rsc", "Failed accessing resource bundle: " + rscerror);
		}
		pcache.setLogger(log, "view-cache");
		acache.setLogger(log, "anon-cache");
	}

	public boolean shallCreateAll() {
		if (!scaSet) {
			sca = System.getProperty("user.name", "unknown").equals("console");
			scaSet = true;
		}
		return sca;
	}

	/**
	 * done
	 * CLeanup code
	 */
	@Override
	public void done() {
		Cache temp;
		int count;

		count = 0;
		while (mhead != null) {
			temp = mhead;
			mhead = mhead.next;
			try {
				temp.release();
			} catch (Exception e) {
				log.out(Log.ERROR, "done", "Failed releasing cache: " + e.toString());
			}
			++count;
		}
		log.out(Log.DEBUG, "done", "Released " + count + " mailout cache entries of expected " + msize);
		mhead = null;
		mtail = null;
		msize = 0;
		pcache.done();
		acache.done();
	}

	@Override
	public int getMaxAge() {
		return maxAge;
	}

	@Override
	public void setMaxAge(int nMaxAge) {
		maxAge = nMaxAge;
	}

	@Override
	public synchronized int getMaxEntries() {
		return maxEntries;
	}

	@Override
	public synchronized void setMaxEntries(int nMaxEntries) {
		if (nMaxEntries >= 0) {
			maxEntries = nMaxEntries;
			while (msize > maxEntries) {
				Cache c = pop();

				log.out(Log.DEBUG, "max", "Reduce entries, currently " + msize + " in cache, new max value is " + maxEntries);
				try {
					c.release();
				} catch (Exception e) {
					log.out(Log.ERROR, "max", "Failed releasing cache: " + e.toString());
				}
			}
		}
	}

	/**
	 * create an ID for a optioanl given text
	 *
	 * @param text the text
	 * @return id part of the text
	 */
	private String makeTextID(String text) {
		String rc;

		if (text.length() < 32) {
			rc = text;
		} else {
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				byte[] digest;
				StringBuffer buf;
				String[] hd = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

				md.update(text.getBytes(StandardCharsets.UTF_8));
				digest = md.digest();
				buf = new StringBuffer(md.getDigestLength());
				for (int n = 0; n < digest.length; ++n) {
					buf.append(hd[(digest[n] >> 4) & 0xf]);
					buf.append(hd[digest[n] & 0xf]);
				}
				rc = buf.toString();
			} catch (Exception e) {
				rc = text;
			}
		}
		return rc;
	}

	/**
	 * makePreview
	 * The main entrance for this class, a preview for all
	 * parts of the mail is generated into a hashtable for
	 * the given mailing and customer. If cachable is set
	 * to true, the result is cached for speed up future
	 * access.
	 *
	 * @param mailingID       the mailing-id to create the preview for
	 * @param customerID      the customer-id to create the preview for
	 * @param selector        optional selector for selecting different version of cached page
	 * @param anon            if we should anonymize the result
	 * @param convertEntities replace non ascii characters by ther HTML entity representation
	 * @param ecsUIDs         if set we should use ecs (extended click statistics) style UIDs
	 * @param createAll       if set create all displayable parts of the mailing
	 * @param cachable        if the result should be cached
	 * @param each            targetID is considered as true during text block creation for previewing
	 * @return the preview
	 */
	@Override
	public Page makePreview(long mailingID, long customerID, String selector, String text, boolean anon, boolean convertEntities, boolean ecsUIDs, boolean createAll, boolean cachable, long[] targetIDs) {
		long now;
		String lid;
		String error;
		PCache pc;
		Cache c;
		Page rc;

		now = System.currentTimeMillis () / 1000;
		lid = "[" + mailingID + "/" + customerID +
				(convertEntities ? "&" : "") +
				(ecsUIDs ? "^" : "") +
				(createAll ? "*" : "") +
				(selector == null ? "" : ":" + selector) +
				(targetIDs == null || targetIDs.length == 0 ? "" : ">" + targetIDs) +
			  "]" + (text == null ? "" : ", " + makeTextID (text));
		error = null;
		pc = anon ? acache : pcache;
		if (cachable) {
			synchronized (pc) {
				if (lastrep + 3600 < now) {
					log.out(Log.INFO, "stat", "Mailing cache: " + msize + ", Page cache: " + pcache.getSize() + ", Anon cache: " + acache.getSize());
					lastrep = now;
				}
				rc = pc.find(mailingID, customerID, selector, now);
				if (rc == null) {
					if (text == null) {
						for (c = mhead; c != null; c = c.next) {
							if (c.mailingID == mailingID) {
								break;
							}
						}
						if (c != null) {
							pop(c);
							if (c.ctime + maxAge < now) {
								log.out(Log.DEBUG, "create", "Found entry for " + mailingID + "/" + customerID + " in mailout cache, but it is expired");
								try {
									c.release();
									c = null;
								} catch (Exception e) {
									log.out(Log.ERROR, "create", "Failed releasing cache: " + e.toString());
								}
							} else {
								log.out(Log.DEBUG, "create", "Found entry for " + mailingID + "/" + customerID + " in mailout cache");
								push(c);
							}
						}
						if (c == null) {
							try {
								c = new Cache(mailingID, now, null, createAll, cachable);
								push(c);
								log.out(Log.DEBUG, "create", "Created new mailout cache entry for " + mailingID + "/" + customerID);
							} catch (Exception e) {
								c = null;
								error = getErrorMessage(e);
								log.out(Log.ERROR, "create", "Failed to create new mailout cache entry for " + mailingID + "/" + customerID + ": " + error);
							}
						}
						if (c != null) {
							try {
								rc = c.makePreview(customerID, selector, anon, convertEntities, ecsUIDs, cachable, targetIDs);
								log.out(Log.DEBUG, "create", "Created new page for " + lid);
							} catch (Exception e) {
								error = getErrorMessage(e);
								log.out(Log.ERROR, "create", "Failed to create preview for " + lid + ": " + error);
							}
						}
					} else {
						c = null;
						try {
							c = new Cache(mailingID, now, text, createAll, cachable);
							rc = c.makePreview(customerID, selector, anon, convertEntities, ecsUIDs, cachable, targetIDs);
							c.release();
						} catch (Exception e) {
							error = getErrorMessage(e);
							log.out(Log.ERROR, "create", "Failed to create custom text preview for " + lid + ": " + error);
						}
					}
					if ((error == null) && (rc != null)) {
						pc.store(mailingID, customerID, selector, now, rc);
					}
				} else {
					log.out(Log.DEBUG, "create", "Found page in page cache for " + lid);
				}
			}
		} else {
			rc = null;
			try {
				c = new Cache(mailingID, now, text, createAll, cachable);
				rc = c.makePreview(customerID, selector, anon, convertEntities, ecsUIDs, cachable, targetIDs);
				c.release();
				log.out(Log.DEBUG, "create", "Created uncached preview for " + lid);
			} catch (Exception e) {
				error = getErrorMessage(e);
				log.out(Log.ERROR, "create", "Failed to create uncached preview for " + lid + ": " + error);
			}
		}
		if (error != null) {
			if (rc == null) {
				rc = new PageImpl();
			}
			rc.setError(error);
		}

		if (rc != null && rc.getError() != null) {
			log.out(Log.INFO, "create", "Found error for " + mailingID + "/" + customerID + ": " + rc.getError());
		}

		return rc;
	}

	@Override
	public Page makePreview(long mailingID, long customerID, String selector, String text, boolean anon, boolean convertEntities, boolean ecsUIDs, boolean createAll, boolean cachable) {
		return makePreview(mailingID, customerID, selector, text, anon, convertEntities, ecsUIDs, createAll, cachable, null);
	}

	@Override
	public Page makePreview(long mailingID, long customerID, String selector, String text, boolean anon, boolean convertEntities, boolean ecsUIDs, boolean cachable) {
		return makePreview(mailingID, customerID, selector, text, anon, convertEntities, ecsUIDs, shallCreateAll(), cachable, null);
	}

	@Override
	public Page makePreview(long mailingID, long customerID, String selector, String text, boolean anon, boolean cachable) {
		return makePreview(mailingID, customerID, selector, text, anon, false, false, false, cachable, null);
	}

	@Override
	public Page makePreview(long mailingID, long customerID, String selector, boolean anon, boolean cachable) {
		return makePreview(mailingID, customerID, selector, null, anon, false, false, false, cachable, null);
	}

	@Override
	public Page makePreview(long mailingID, long customerID, boolean cachable) {
		return makePreview(mailingID, customerID, null, null, false, false, false, false, cachable, null);
	}

	@Override
	public Page makePreview(long mailingID, long customerID, long targetID) {
		long[] targetIDs = { targetID };
		return makePreview(mailingID, customerID, null, null, false, false, false, false, false, targetIDs);
	}

	/**
	 * Wrapper for heatmap generation
	 *
	 * @param mailingID  the mailing to generate the heatmap for
	 * @param customerID the customerID to generate the heatmap for
	 * @return the preview
	 */

	@Override
	public String makePreviewForHeatmap(long mailingID, long customerID) {
		Page page = makePreview(mailingID, customerID, null, null, false, false, true, false, false, null);

		return page != null ? page.getHTML() : null;
	}

	private String getErrorMessage(Exception e) {
		StringBuffer sb = new StringBuffer(e.toString());
		Throwable t = e;
		while (t != null) {
			StackTraceElement[] stackTrace = t.getStackTrace();
			if (stackTrace != null) {
				for (int i = 0; i < stackTrace.length; i++) {
					sb.append("\n\tat ");
					sb.append(stackTrace[i].toString());
				}
			}
			t = t.getCause();
			if (t != null) {
				sb.append("\nCaused by: " + t + "\n");
			}
		}
		return sb.toString();
	}

	private Cache pop(Cache c) {
		if (c != null) {
			if (c.next != null) {
				c.next.prev = c.prev;
			} else {
				mtail = c.prev;
			}
			if (c.prev != null) {
				c.prev.next = c.next;
			} else {
				mhead = c.next;
			}
			c.next = null;
			c.prev = null;
			--msize;
		}
		return c;
	}

	private Cache pop() {
		Cache rc;

		rc = mtail;
		if (rc != null) {
			mtail = mtail.prev;
			if (mtail != null) {
				mtail.next = null;
			} else {
				mhead = null;
			}
			--msize;
			rc.next = null;
			rc.prev = null;
		}
		return rc;
	}

	private void push(Cache c) {
		if (msize >= maxEntries) {
			Cache tmp = pop();

			if (tmp != null) {
				if (tmp == c) {
					log.out(Log.ERROR, "push", "Try to release pushed cache");
				} else {
					try {
						tmp.release();
					} catch (Exception e) {
						log.out(Log.ERROR, "push", "Failed releasing cache: " + e.toString());
					}
				}
				--msize;
			}
		}
		c.next = mhead;
		c.prev = null;
		if (mhead != null) {
			mhead.prev = c;
		}
		mhead = c;
		++msize;
	}

	/******************** deprecated part ********************/
	@Override
	@Deprecated
	public Map<String, Object> createPreview(long mailingID, long customerID, String selector, String text, boolean anon, boolean convertEntities, boolean ecsUIDs, boolean createAll, boolean cachable) {
		Page p = makePreview(mailingID, customerID, selector, text, anon, convertEntities, ecsUIDs, createAll, cachable);

		return p != null ? p.compatibilityRepresentation() : null;
	}

	@Override
	@Deprecated
	public Map<String, Object> createPreview(long mailingID, long customerID, String selector, String text, boolean anon, boolean convertEntities, boolean ecsUIDs, boolean cachable) {
		return createPreview(mailingID, customerID, selector, text, anon, convertEntities, ecsUIDs, shallCreateAll(), cachable);
	}

	@Override
	@Deprecated
	public Map<String, Object> createPreview(long mailingID, long customerID, String selector, String text, boolean anon, boolean cachable) {
		return createPreview(mailingID, customerID, selector, text, anon, false, false, cachable);
	}

	@Override
	@Deprecated
	public Map<String, Object> createPreview(long mailingID, long customerID, String selector, boolean anon, boolean cachable) {
		return createPreview(mailingID, customerID, selector, null, anon, false, false, cachable);
	}

	@Override
	@Deprecated
	public Map<String, Object> createPreview(long mailingID, long customerID, boolean cachable) {
		return createPreview(mailingID, customerID, null, null, false, false, false, cachable);
	}

	/**
	 * Pattern to find entities to escape
	 */
	static private Pattern textReplace = Pattern.compile("[&<>'\"]");
	/**
	 * Values to escape found entities
	 */
	static private Map<String, String> textReplacement = new HashMap<>();

	static {
		textReplacement.put("&", "&amp;");
		textReplacement.put("<", "&lt;");
		textReplacement.put(">", "&gt;");
		textReplacement.put("'", "&apos;");
		textReplacement.put("\"", "&quot;");
	}

	/**
	 * escapeEntities
	 * This method escapes the HTML entities to be displayed
	 * in a HTML context
	 *
	 * @param s the input string
	 * @return null, if input string had been null,
	 * the escaped version of s otherwise
	 */
	private String escapeEntities(String s) {
		if (s != null) {
			int slen = s.length();
			Matcher m = textReplace.matcher(s);
			StringBuffer buf = new StringBuffer(slen + 128);
			int pos = 0;

			while (m.find(pos)) {
				int next = m.start();
				String ch = m.group();

				if (pos < next) {
					buf.append(s, pos, next);
				}
				buf.append(textReplacement.get(ch));
				pos = m.end();
			}
			if (pos != 0) {
				if (pos < slen) {
					buf.append(s.substring(pos));
				}
				s = buf.toString();
			}
		}
		return s;
	}

	/**
	 * encode
	 * Encodes a string to a byte stream using the given character set,
	 * if escape is true, HTML entities are escaped prior to encoding
	 *
	 * @param s       the string to encode
	 * @param charset the character set to convert the string to
	 * @param escape  if HTML entities should be escaped
	 * @return the coded string as a byte stream
	 */
	private byte[] encode(String s, String charset, boolean escape) {
		if (escape && (s != null)) {
			s = "<pre>\n" + escapeEntities(s) + "</pre>\n";
		}
		try {
			return s == null ? null : s.getBytes(charset);
		} catch (java.io.UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * get
	 * a null input save conversion variant
	 *
	 * @param s      the input string
	 * @param escape to escape HTML entities
	 * @return the converted string
	 */
	private String convert(String s, boolean escape) {
		if (escape && (s != null)) {
			return escapeEntities(s);
		}
		return s;
	}

	/**
	 * Get header-, text- or HTML-part from hashtable created by
	 * createPreview as byte stream
	 */
	@Override
	@Deprecated
	public byte[] getHeaderPart(Map<String, Object> output, String charset, boolean escape) {
		return encode((String) output.get(ID_HEAD), charset, escape);
	}

	@Override
	@Deprecated
	public byte[] getHeaderPart(Map<String, Object> output, String charset) {
		return getHeaderPart(output, charset, false);
	}

	@Override
	@Deprecated
	public byte[] getTextPart(Map<String, Object> output, String charset, boolean escape) {
		return encode((String) output.get(ID_TEXT), charset, escape);
	}

	@Override
	@Deprecated
	public byte[] getTextPart(Map<String, Object> output, String charset) {
		return getTextPart(output, charset, false);
	}

	@Override
	@Deprecated
	public byte[] getHTMLPart(Map<String, Object> output, String charset, boolean escape) {
		return encode((String) output.get(ID_HTML), charset, escape);
	}

	@Override
	@Deprecated
	public byte[] getHTMLPart(Map<String, Object> output, String charset) {
		return getHTMLPart(output, charset, false);
	}

	/**
	 * Get header-, text- or HTML-part as strings
	 */
	@Override
	@Deprecated
	public String getHeader(Map<String, Object> output, boolean escape) {
		return convert((String) output.get(ID_HEAD), escape);
	}

	@Override
	@Deprecated
	public String getHeader(Map<String, Object> output) {
		return getHeader(output, false);
	}

	@Override
	@Deprecated
	public String getText(Map<String, Object> output, boolean escape) {
		return convert((String) output.get(ID_TEXT), escape);
	}

	@Override
	@Deprecated
	public String getText(Map<String, Object> output) {
		return getText(output, false);
	}

	@Override
	@Deprecated
	public String getHTML(Map<String, Object> output, boolean escape) {
		return convert((String) output.get(ID_HTML), escape);
	}

	@Override
	@Deprecated
	public String getHTML(Map<String, Object> output) {
		return getHTML(output, false);
	}

	/**
	 * Get attachment names and content
	 */
	private boolean isAttachment(String name) {
		return (!name.startsWith("__")) && (!name.endsWith("__"));
	}

	@Override
	@Deprecated
	public String[] getAttachmentNames(Map<String, Object> output) {
		ArrayList<String> collect = new ArrayList<>();

		for (String name : output.keySet()) {
			if (isAttachment(name)) {
				collect.add(name);
			}
		}
		return collect.toArray(new String[collect.size()]);
	}

	@Override
	@Deprecated
	public byte[] getAttachment(Map<String, Object> output, String name) {
		if ((!isAttachment(name)) || (!output.containsKey(name))) {
			return null;
		}

		byte[] rc = null;
		String coded = (String) output.get(name);

		if (coded != null) {
			String valid = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
			byte[] temp = new byte[coded.length()];
			int tlen = 0;
			long val;
			int count;
			int pad;
			byte pos;

			val = 0;
			count = 0;
			pad = 0;
			for (int n = 0; n < coded.length(); ++n) {
				char ch = coded.charAt(n);

				if (ch == '=') {
					++pad;
					++count;
				} else if ((pos = (byte) valid.indexOf(ch)) != -1) {
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
						default:
							break;
					}
				}
				if (count == 4) {
					temp[tlen] = (byte) ((val >> 16) & 0xff);
					temp[tlen + 1] = (byte) ((val >> 8) & 0xff);
					temp[tlen + 2] = (byte) (val & 0xff);
					tlen += 3 - pad;
					count = 0;
					if (pad > 0) {
						break;
					}
				}
			}
			rc = Arrays.copyOf(temp, tlen);
		}
		return rc;
	}

	/**
	 * Get individual lines from the header
	 */
	@Override
	@Deprecated
	@SuppressWarnings("unchecked")
	public String[] getHeaderField(Map<String, Object> output, String field) {
		String[] rc = null;

		synchronized (output) {
			Map<String, String[]> header = (Map<String, String[]>) output.get(ID_HDETAIL);

			if (header == null) {
				String head = (String) output.get(ID_HEAD);

				header = new HashMap<>();
				if (head != null) {
					String[] lines = head.split("\r?\n");
					String cur = null;

					for (int n = 0; n <= lines.length; ++n) {
						String line = (n < lines.length ? lines[n] : null);

						if ((line == null) || ((line.indexOf(' ') != 0) && (line.indexOf('\t') != 0))) {
							if (cur != null) {
								String[] parsed = cur.split(": +", 2);

								if (parsed.length == 2) {
									String key = parsed[0].toLowerCase();
									String[] content = header.get(key);
									int nlen = (content == null ? 1 : content.length + 1);
									String[] ncontent = new String[nlen];

									if (content != null) {
										for (int m = 0; m < content.length; ++m) {
											ncontent[m] = content[m];
										}
									}
									ncontent[nlen - 1] = parsed[1];
									header.put(key, ncontent);
								}
							}
							cur = line;
						} else if (cur != null) {
							cur += '\n' + line;
						}
					}
				}
				output.put(ID_HDETAIL, header);
			}
			rc = header.get(field.toLowerCase());
		}
		return rc;
	}

	@Override
	@Deprecated
	public String getPartOfHeader(Map<String, Object> output, boolean escape, String headerKeyword) {
		String rc = null;
		String[] head = getHeaderField(output, headerKeyword);

		if ((head != null) && (head.length > 0)) {
			rc = escape ? escapeEntities(head[0]) : head[0];
		}
		return rc;
	}

	// well, we could create a global Hashmap containing all the values for this preview
	// but the part-Method is called not very often, so its more efficient to parse
	// the header if we need it.
	// As parameter give the "Keyword" you will get then the appropriate return String.
	// Possible Values for the Header are:
	// "Return-Path", "Received", "Message-ID", "Date", "From", "To", "Subject", "X-Mailer", "MIME-Version"
	// warning! We do a "startswith" comparison, that means, if you give "Re" as parameter, you will
	// get either "Return-Path" or "Received", depending on what comes at last.
	@Override
	@Deprecated
	public String getPartOfHeader(Map<String, Object> output, String charset, boolean forHTML, String headerKeyword) throws Exception {
		String returnString = null;
		String tmpLine = null;
		// use just \n as line delimiter. Warning, if you use Windows, that will not work...
		StringTokenizer st = new StringTokenizer(new String(getHeaderPart(output, charset, forHTML), StandardCharsets.UTF_8), "\n");
		while (st.hasMoreElements()) {
			// get next line and cut the leading and trailing whitespaces of.
			tmpLine = ((String) st.nextElement()).trim();
			// convert Header String to lower and compare with lower-case given String
			if (tmpLine.toLowerCase().startsWith(headerKeyword.toLowerCase())) {
				// get index of first :
				int endOfHeaderKeyword = tmpLine.indexOf(':') + 1;
				// return everything from first ":" and remove trailing whitespaces..
				returnString = tmpLine.substring(endOfHeaderKeyword).trim();
			}
		}
		return returnString;
	}

	public String makePreview(long mailingID, long customerID, String text, boolean cachable) {
		if (text.indexOf("[agn") == -1) {
			return text;
		}

		Page temp = makePreview(mailingID, customerID, null, text, false, false, false, false, cachable);

		return temp != null ? temp.getText() : null;
	}

	public String makePreview(long mailingID, long customerID, String text) {
		return makePreview(mailingID, customerID, text, true);
	}

	public String makePreview(long mailingID, long customerID, String text, String proxy, boolean encode) {
		return insertProxy(makePreview(mailingID, customerID, text), proxy, encode);
	}

	public String makePreview(long mailingID, long customerID, String text, String proxy) {
		return makePreview(mailingID, customerID, text, proxy, false);
	}

	public String makePreviewForHeatmap(long mailingID, long customerID, String proxy, boolean encode) {
		return insertProxy(makePreviewForHeatmap(mailingID, customerID), proxy, encode);
	}

	public String makePreviewForHeatmap(long mailingID, long customerID, String proxy) {
		return makePreviewForHeatmap(mailingID, customerID, proxy, false);
	}

	@Deprecated
	public Map<String, Object> createPreview(PubID pid, boolean cachable) {
		return createPreview(pid.getMailingID(), pid.getCustomerID(), pid.getParm(), true, cachable);
	}

	@Deprecated
	public String createPreview(long mailingID, long customerID, String text, boolean cachable) {
		return makePreview(mailingID, customerID, text, cachable);
	}

	@Deprecated
	public String createPreview(long mailingID, long customerID, String text) {
		return createPreview(mailingID, customerID, text, true);
	}

	public String providerGetSubject(long mailingID, long customerID) {
		String rc;

		try {
			Page page = makePreview(mailingID, customerID, false);

			rc = page != null ? page.getHeaderField("subject") : null;
		} catch (Exception e) {
			log.out(Log.ERROR, "provider", "Failed to get subject line for provider: " + e.toString());
			rc = null;
		}
		return rc;
	}

	public boolean providerSendPreview(long mailingID, long customerID, String email) {
		boolean rc;

		try {
			Mailgun mailout = new MailgunImpl();
			Map<String, Object> opts = new HashMap<>();

			mailout.initialize("provider:" + mailingID);
			mailout.prepare(null);
			opts.put("customer-id", customerID);
			opts.put("provider-email", email);
			mailout.execute(opts);
			mailout.done();
			rc = true;
		} catch (Exception e) {
			log.out(Log.ERROR, "provider", "Failed to execute mailout for provider: " + e.toString());
			rc = false;
		}
		return rc;
	}

	private String insertProxy(String html, String proxy, boolean encode) {
		if (html == null || proxy == null) {
			return html;
		} else {
			StringBuffer temp = new StringBuffer(html.length() * 2);
			Matcher m = linkSearch.matcher(html);
			int pos = 0;
			while (m.find()) {
				int start = m.start();
				String entity = m.group(1);

				// Insert interim characters, which do not contain a link, into temp buffer
				if (pos < start) {
					temp.append(html, pos, start);
				}

				// Only insert proxy, if this is not a "<a href="-link
				if (!entity.equals("a")) {
					String url = m.group(3);

					// Trim leading and trailing string quotes, because they are always added later on
					if (url.startsWith("\"") && url.endsWith("\"")) {
						url = url.substring(1, url.length() - 1);
					} else if (url.startsWith("'") && url.endsWith("'")) {
						url = url.substring(1, url.length() - 1);
					}

					temp.append("<");
					temp.append(entity);
					temp.append(m.group(2)); // group 2 contains leading whitespaces and trailing equal sign
					temp.append("\"");
					temp.append(proxy);

					if (encode) {
						try {
							temp.append(URLEncoder.encode(url, "UTF-8"));
						} catch (UnsupportedEncodingException e) {
							temp.append(url);
						}
					} else {
						temp.append(url);
					}

					temp.append("\"");
				} else {
					// Keep a "<a href="-links unchanged (I guess they might be replaced by rdir links, which contain the proxy?)
					temp.append(m.group());
				}

				pos = m.end();
			}

			if (pos < html.length()) {
				// Handle the trailing part, which does not contain a link
				temp.append(html.substring(pos));
			}

			return temp.toString();
		}
	}
}
