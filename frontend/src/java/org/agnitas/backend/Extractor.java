/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.util.Log;
import org.agnitas.util.importvalues.MailType;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class Extractor implements ResultSetExtractor<Object> {
	private final int reservedColumns = 4;
	private Map<String, EMMTag> tagNames = null;
	private MediaMap mmap;
	private Data data;
	private Blocklist blist;
	private MailWriter mailer;
	private boolean needSamples;
	private Set<Long> seen;
	private boolean multi;
	private boolean hasOverwriteData;
	private boolean hasVirtualData;
	private List<EMMTag> emailTags;
	private int emailCount;
	private ResultSetMetaData meta;
	private int metacount;
	private int usecount;
	private Column[] rmap;
	private Custinfo cinfo;
	private long lastCid;
	private boolean skip;

	public Extractor(Map<String, EMMTag> tagNames, MediaMap mmap, Data nData, Custinfo nCinfo, Blocklist nBlist, MailWriter nMailer, boolean nNeedSamples, Set<Long> nSeen, boolean nMulti, boolean nHasOverwriteData, boolean nHasVirtualData, List<EMMTag> nEmailTags, int nEmailCount) {
		this.tagNames = tagNames;
		this.mmap = mmap;
		data = nData;
		cinfo = nCinfo;
		blist = nBlist;
		mailer = nMailer;
		needSamples = nNeedSamples;
		seen = nSeen;
		multi = nMulti;
		hasOverwriteData = nHasOverwriteData;
		hasVirtualData = nHasVirtualData;
		emailTags = nEmailTags;
		emailCount = nEmailCount;
		meta = null;
		metacount = 0;
		usecount = 0;
		rmap = null;
		lastCid = -1;
		skip = false;
	}

	private void extractRecord(ResultSet rset) throws SQLException, DataAccessException {
		if (meta == null) {
			meta = rset.getMetaData();
			metacount = meta.getColumnCount();
			rmap = new Column[metacount];
			usecount = reservedColumns;
			for (EMMTag current_tag : tagNames.values()) {
				if ((!current_tag.globalValue) && (current_tag.tagType == EMMTag.TAG_DBASE)) {
					++usecount;
				}
			}

			int lindex = 0;

			for (int n = 0; n < metacount; ++n) {
				Column c = null;

				if ((n >= usecount) && (lindex < data.lcount)) {
					while (lindex < data.lcount) {
						if (data.columnUse(lindex)) {
							c = data.columnByIndex(lindex++);
							++usecount;
							break;
						}
						lindex++;
					}
				}
				if (c == null) {
					String cname = meta.getColumnName(n + 1).toLowerCase ();
					int ctype = meta.getColumnType(n + 1);

					if (Column.typeStr(ctype) != null) {
						c = new Column(cname, ctype);
					}
				}
				rmap[n] = c;
				if (c != null) {
					data.columnUpdateQueryIndex(c, n);

					if (hasOverwriteData) {
						String overwritten = data.overwriteData(c.getQname());

						if (overwritten != null) {
							c.setOverwrite(overwritten.equals("") ? null : overwritten);
						} else {
							c.clearOverwrite();
						}
					}
				}
			}
		}

		long cid = rset.getLong(1);

		if (hasVirtualData && (!data.useRecord(cid))) {
			return;
		}

		if (data.maildropStatus.isPreviewMailing() && data.previewClearData) {
			cid = 0;
			for (int n = 0; n < metacount; ++n) {
				if (rmap[n] != null) {
					rmap[n].clr();
				}
			}
		} else {
			for (int n = 0; n < metacount; ++n) {
				if (rmap[n] != null) {
					rmap[n].set(rset, n + 1);
				}
			}
		}

		int count = reservedColumns;

		for (EMMTag tmpTag : tagNames.values()) {
			if ((!tmpTag.globalValue) && (tmpTag.tagType == EMMTag.TAG_DBASE)) {
				if ((rmap[count] != null) && (!rmap[count].getIsnull())) {
					tmpTag.setTagValue(rmap[count].get());
				} else {
					tmpTag.setTagValue(null);
				}
				++count;
			}
		}
		if (hasVirtualData) {
			for (EMMTag tmpTag : tagNames.values()) {
				if (hasVirtualData && (tmpTag.tagType == EMMTag.TAG_INTERNAL) && (tmpTag.tagSpec == EMMTag.TI_DBV)) {
					tmpTag.setTagValue(data.virtualData(tmpTag.mSelectString.toLowerCase()));
				}
			}
		}

		if (!seen.contains(cid)) {
			seen.add(cid);
			skip = false;
			lastCid = cid;

			String userType = rmap[2] != null ? rmap[2].get() : null;
			String mailtype = rmap[3] != null ? rmap[3].get() : null;
			int mtype;

			if ((userType == null) || userType.isEmpty()) {
				userType = "W";
			}
			if (data.maildropStatus.isPreviewMailing()) {
				mtype = MailType.HTML.getIntValue();
			} else {
				if (mailtype == null) {
					data.logging(Log.WARNING, "mailout", "Unset mailtype for customer_id " + cid + ", using default");
					mtype = MailType.HTML.getIntValue();
				} else {
					mtype = Integer.parseInt(mailtype);
					if (mtype > MailType.HTML_OFFLINE.getIntValue()) {
						mtype = MailType.HTML_OFFLINE.getIntValue();
					}
					if (mtype > data.masterMailtype) {
						mtype = data.masterMailtype;
					}
				}
			}

			cinfo.newCustomer(cid, userType, rmap);

			for (int n = 0; n < emailCount; ++n) {
				EMMTag etag = emailTags.get(n);

				etag.setTagValue(cinfo.getMediaFieldContent(data.mediaEMail));
			}

			if (!data.maildropStatus.isPreviewMailing()) {
				boolean isBlocklisted = false;

				for (Media m : data.media()) {
					String check = cinfo.getMediaFieldContent(m);

					if (check != null) {
						String what = m.typeName();
						Blocklist.Entry bl = blist.isBlockListed(check);

						if (bl != null) {
							data.logging(Log.WARNING, "mailout", "Found " + what + ": " + check + " (" + cid + ") in " + bl.where() + " blocklist, ignored");
							blist.writeBounce(data.mailing.id(), cid);
							isBlocklisted = true;
						}
					}
				}
				if (isBlocklisted) {
					data.markBlocklisted(cid);
					skip = true;
					return;
				}
			}

			MediaMap.MMEntry	mediatypes = getMediaTypes (cid);
			if (mediatypes == null) {
				skip = true;
				return;
			}
			try {
				int targetGroupValuesStartIndex = data.useControlColumns(cinfo, rset, usecount + 1) - 1;
				int pos = 0;

				for (@SuppressWarnings("unused") Target t : data.targetExpression.resolveByDatabase()) {
					Column c = null;

					if (targetGroupValuesStartIndex < metacount) {
						c = rmap[targetGroupValuesStartIndex++];
					}
					cinfo.setTargetGroupValue(pos, c != null && c.get().equals("1"));
					++pos;
				}
			} catch (Exception e) {
				data.logging(Log.ERROR, "mailout", "Failed to use control columns: " + e.toString(), e);
			}

			if (needSamples) {
				Set <String>	seen = new HashSet <> ();
				
				for (int state = 0; state < 2; ++state) {
					String	source;
					
					switch (state) {
					default:
						source = null;
						break;
					case 0:
						source = data.sampleEmails ();
						break;
					case 1:
						source = data.deliveryCheckEmails ();
						break;
					}
					if (source != null) {
						List<String> v = StringOps.splitString(source);

						for (int mcount = 0; mcount < v.size(); ++mcount) {
							String email = validateSampleEmail(v.get(mcount));

							if ((email != null) && (email.length() > 3) && (email.indexOf('@') != -1) && (! seen.contains (email))) {
								seen.add (email);
								cinfo.setSampleEmail(email);
								for (int n = 0; n < emailCount; ++n) {
									emailTags.get(n).setTagValue(email);
								}
								for (int n = 0; (n < MailType.HTML_OFFLINE.getIntValue()) && (n <= data.masterMailtype); ++n) {
									try {
										mailer.writeMail(cinfo, mcount + 1, n, 0, Media.typeName(Media.TYPE_EMAIL), "1", tagNames);
										mailer.writeContent(cinfo, 0, tagNames, rmap);
									} catch (Exception e) {
										data.logging(Log.ERROR, "mailout", "Failed to write sample mail: " + e.toString(), e);
									}
								}
							}
						}
					}
				}
				cinfo.setSampleEmail(null);
				for (int n = 0; n < emailCount; ++n) {
					emailTags.get(n).setTagValue(cinfo.getMediaFieldContent(data.mediaEMail));
				}
				needSamples = false;
			}

			try {
				mailer.writeMail(cinfo, 0, mtype, cid, mediatypes.name, mediatypes.status, tagNames);
				mailer.writeContent(cinfo, cid, tagNames, rmap);
			} catch (Exception e) {
				data.logging(Log.ERROR, "mailout", "Failed to write mail: " + e.toString(), e);
			}
		} else if (multi) {
			if (cid != lastCid) {
				data.logging(Log.ERROR, "mailout", "ExceptionTrigger: expecting data for " + lastCid + ", but got for " + cid);
				return;
			}
			if (!skip) {
				try {
					mailer.writeContent(cinfo, cid, tagNames, rmap);
				} catch (Exception e) {
					data.logging(Log.ERROR, "mailout", "Failed to write content: " + e.toString(), e);
				}
			}
		}
	}

	@Override
	public Object extractData(ResultSet rset) throws SQLException, DataAccessException {
		while (rset.next()) {
			extractRecord(rset);
		}
		return null;
	}

	/**
	 * Return used mediatypes (currently only email)
	 *
	 * @param customerID the customerID to get types for
	 * @return mediatypes
	 */
	private MediaMap.MMEntry getMediaTypes (long cid) {
		if (data.maildropStatus.isPreviewMailing()) {
			return mmap.getActive();
		}
		return mmap.get(cid);
	}

	/**
	 * Check the sample email receiver if they should receive
	 * the sample for this mailing
	 *
	 * @param inp the expression to validate
	 * @return null, if no mail should be sent, otherwise the email address
	 */
	private String validateSampleEmail(String inp) {
		String email;
		int n = inp.indexOf(':');

		if (n != -1) {
			String minsub;

			minsub = inp.substring(0, n);
			try {
				long minsubscriber = Long.parseLong(minsub);

				if (minsubscriber < data.totalSubscribers) {
					email = inp.substring(n + 1);
				} else {
					email = null;
				}
			} catch (NumberFormatException e) {
				data.logging(Log.WARNING, "sample", "Invalid sample email entry: " + inp);
				email = null;
			}
		} else {
			email = inp;
		}
		return email;
	}
}
