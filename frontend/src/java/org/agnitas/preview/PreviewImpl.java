/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.preview;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.agnitas.backend.Mailgun;
import org.agnitas.backend.MailgunImpl;
import org.agnitas.util.Log;
import org.agnitas.util.PubID;

public class PreviewImpl implements Preview {
	private static Pattern linkSearch = Pattern.compile("<[ \t\n\r]*([a-z][a-z0-9_-]*)([ \t\n\r]+[^>]*[a-z_][a-z0-9_-]*=)(\"http://[^\"]+\"|http://[^> \t\n\r]+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

	private static final transient Pattern URL_PATTERN = Pattern.compile("(.+?)://(.*)$");
	
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
		log = new Log(logname, level, 0);
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
				MessageDigest sha512Digest = MessageDigest.getInstance("SHA-512");
				byte[] digest;
				StringBuffer buf;
				String[] hd = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

				sha512Digest.update(text.getBytes(StandardCharsets.UTF_8));
				digest = sha512Digest.digest();
				buf = new StringBuffer(sha512Digest.getDigestLength());
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
	 * @param targetIDs            targetID is considered as true during text block creation for previewing
	 * @return the preview
	 */
	@Override
	public Page makePreview(long mailingID, long customerID, String selector, String text, boolean anon, boolean convertEntities, boolean ecsUIDs, boolean createAll, boolean cachable, long[] targetIDs, boolean isMobile) {
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
								c = new Cache(mailingID, now, null, createAll, cachable, isMobile);
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
							c = new Cache(mailingID, now, text, createAll, cachable, isMobile);
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
				c = new Cache(mailingID, now, text, createAll, cachable, isMobile);
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
		return makePreview(mailingID, customerID, selector, text, anon, convertEntities, ecsUIDs, createAll, cachable, null, false);
	}

	@Override
	public Page makePreview(long mailingID, long customerID, String selector, String text, boolean anon, boolean convertEntities, boolean ecsUIDs, boolean cachable) {
		return makePreview(mailingID, customerID, selector, text, anon, convertEntities, ecsUIDs, shallCreateAll(), cachable, null, false);
	}

	@Override
	public Page makePreview(long mailingID, long customerID, String selector, String text, boolean anon, boolean cachable) {
		return makePreview(mailingID, customerID, selector, text, anon, false, false, false, cachable, null, false);
	}

	@Override
	public Page makePreview(long mailingID, long customerID, String selector, boolean anon, boolean cachable) {
		return makePreview(mailingID, customerID, selector, null, anon, false, false, false, cachable, null, false);
	}

	@Override
	public Page makePreview(long mailingID, long customerID, boolean cachable) {
		return makePreview(mailingID, customerID, null, null, false, false, false, false, cachable, null, false);
	}

	@Override
	public Page makePreview(long mailingID, long customerID, boolean cachable, boolean isMobile) {
		return makePreview(mailingID, customerID, null, null, false, false, false, false, cachable, null, isMobile);
	}

	@Override
	public Page makePreview(long mailingID, long customerID, long targetID) {
		long[] targetIDs = { targetID };
		return makePreview(mailingID, customerID, null, null, false, false, false, false, false, targetIDs, false);
	}

	@Override
	public Page makePreview(long mailingID, long customerID, long targetID, boolean isMobile) {
		long[] targetIDs = { targetID };
		return makePreview(mailingID, customerID, null, null, false, false, false, false, false, targetIDs, isMobile);
	}

	@Override
	public String makePreview(long mailingID, long customerID, String text, boolean cachable) {
		if (text.indexOf("[agn") == -1) {
			return text;
		}

		Page temp = makePreview(mailingID, customerID, null, text, false, false, false, false, cachable);

		return temp != null ? temp.getText() : null;
	}

	@Override
	public String makePreview(long mailingID, long customerID, String text) {
		return makePreview(mailingID, customerID, text, true);
	}

	@Override
	public String makePreview(long mailingID, long customerID, String text, String proxy, boolean encode) {
		return insertProxy(makePreview(mailingID, customerID, text), proxy, encode);
	}

	@Override
	public String makePreview(long mailingID, long customerID, String text, String proxy) {
		return makePreview(mailingID, customerID, text, proxy, false);
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
		Page page = makePreview(mailingID, customerID, null, null, false, false, true, false, false, null, false);

		return page != null ? page.getHTML() : null;
	}

	@Override
	public String makePreviewForHeatmap(long mailingID, long customerID, String proxy, boolean encode) {
		return insertProxy(makePreviewForHeatmap(mailingID, customerID), proxy, encode);
	}

	@Override
	public String makePreviewForHeatmap(long mailingID, long customerID, String proxy) {
		return makePreviewForHeatmap(mailingID, customerID, proxy, false);
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
	@Deprecated(forRemoval = true)
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
	public Map<String, Object> createPreview(long mailingID, long customerID, String selector, boolean anon, boolean cachable) {
		return createPreview(mailingID, customerID, selector, null, anon, false, false, cachable);
	}

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

					temp.append(createProxiedUrl(proxy, url, encode));

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
	
	private final String createProxiedUrl(final String proxy, final String url, final boolean encode) {
		final StringBuffer temp = new StringBuffer(proxy);

		final Matcher matcher = URL_PATTERN.matcher(url);
		final String proxyPath = matcher.matches()
				? String.format("%s/%s", matcher.group(1), matcher.group(2))
				: url;
		
		if (encode) {
			try {
				temp.append(URLEncoder.encode(proxyPath, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				temp.append(proxyPath);
			}
		} else {
			temp.append(proxyPath);
		}

		return temp.toString();
	}
}
