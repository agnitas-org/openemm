/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agnitas.backend.dao.TagDAO;
import org.agnitas.backend.exceptions.EMMTagException;
import org.agnitas.backend.tags.Tag;
import org.agnitas.util.Log;
import org.agnitas.util.Str;
import org.agnitas.util.Title;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * Class EMMTAG
 * - stores information about a single agnitas-tag
 * - constructor rectrieves selectvalues associated with tag-name from dbase
 * - after db query for a record set (user), EmmTag.mTagValue holds the value
 * for this tag
 */
public class EMMTag {
	/**
	 * This tag is taken from the database
	 */
	public final static int TAG_DBASE = 0;
	/**
	 * This tag is handled internally
	 */
	public final static int TAG_INTERNAL = 1;
	/**
	 * Internal tag, virtual Database column
	 */
	public final static int TI_DBV = 0;
	/**
	 * Internal tag, database column
	 */
	public final static int TI_DB = 1;
	/**
	 * Internal tag, image link
	 */
	public final static int TI_IMAGE = 2;
	/**
	 * Internal tag, email address
	 */
	public final static int TI_EMAIL = 3;
	/**
	 * Internal tag, number of subscriber for this mailing
	 */
	public final static int TI_SUBSCRIBERCOUNT = 4;
	/**
	 * Internal tag, current date
	 */
	public final static int TI_DATE = 5;
	/**
	 * Internal tag, system information created during final mail creation
	 */
	public final static int TI_SYSINFO = 6;
	/**
	 * Internal tag, dynamic condition
	 */
	public final static int TI_DYN = 7;
	/**
	 * Internal tag, dynamic content
	 */
	public final static int TI_DYNVALUE = 8;
	/**
	 * Handle title tags
	 */
	public final static int TI_TITLE = 9;
	/**
	 * Handle full title tags
	 */
	public final static int TI_TITLEFULL = 10;
	/**
	 * Handle title tags for first name only
	 */
	public final static int TI_TITLEFIRST = 11;
	/**
	 * Create image link tags
	 */
	public final static int TI_IMGLINK = 12;
	public final static int TI_CALL = 13;
	public final static int TI_SWYN = 14;
	public final static int TI_SENDDATE = 15;
	public final static int TI_GRIDPH = 16;
	public final static int TI_MODULE = 17;
	/**
	 * Names of all internal tags
	 */
	protected final static String[] TAG_INTERNALS = { null, "agnDB", "agnIMAGE", "agnEMAIL", "agnSUBSCRIBERCOUNT", "agnDATE", "agnSYSINFO", "agnDYN", "agnDVALUE", "agnTITLE", "agnTITLEFULL", "agnTITLEFIRST", "agnIMGLINK", null, "agnSWYN", "agnSENDDATE", "gridPH", null };
	/**
	 * The generic tag reference for an extrernal implementation
	 */
	private Tag tag;
	/**
	 * The full name of this tag including all parameters
	 */
	public String mTagFullname;
	/**
	 * The name of the tag
	 */
	public String mTagName;
	/**
	 * All parameters parsed into a hash
	 */
	public Map<String, String> mTagParameters;
	/**
	 * All Attributes parsed into a list
	 */
	public List<String> mTagAttributes;
	/**
	 * Is this a complex, e.g. dynamic changeable tag
	 */
	private boolean isComplex;
	/**
	 * Howto select this tag from the database
	 */
	public String mSelectString;
	/**
	 * Howto interpret this string
	 */
	public String mSelectType;
	/**
	 * Result of this tag, is set for each customer, if not global
	 */
	protected String mTagValue;
	/**
	 * The tag type
	 */
	public int tagType;
	/**
	 * The tag type specification
	 */
	public int tagSpec;
	/**
	 * If this tag is global, but will be inserted during final mail creation
	 */
	public boolean globalValue;

	/**
	 * Internal used to format agnDB values
	 */
	public Column dbColumn;
	public Format dbFormat;
	public Expression dbExpression;
	/**
	 * Internal used for dynamic image names
	 */
	private String imageSource;
	private String imagePatternName;
	private Column imagePatternColumn;
	/**
	 * Internal used value on how to code an email
	 */
	protected int emailCode;
	/**
	 * Internal used column, if not current date
	 */
	protected Column dateColumn;
	/**
	 * Internal used base, if not current date
	 */
	protected String dateBase;
	/**
	 * Internal used date, if not column is specified, defaults to current date
	 */
	protected Date dateValue;
	/**
	 * Internal used format, if this is a date tag
	 */
	protected SimpleDateFormat dateFormat;
	/**
	 * Internal used offset in seconds for date tag
	 */
	protected long dateOffset;
	/**
	 * Internal used expression for modifying/calculating a date
	 */
	protected Expression[] dateExpressions;
	protected Calendar dateCalendar;
	/**
	 * Internal used title type
	 */
	protected long titleType;
	protected StringBuffer titleError;
	/**
	 * Internal used pre-/postfix for title generation
	 */
	protected String titlePrefix;
	protected String titlePostfix;
	/**
	 * Internal used title mode
	 */
	protected int titleMode;
	/**
	 * Internal used reference to image component
	 */
	protected String ilPrefix, ilPostfix;
	public String ilURL;
	/**
	 * used as return value for anon preview
	 */
	private String anon;
	protected boolean punycodeValue;
	/**
	 * Variables to hold private data for functions
	 */
	protected String func;
	protected Code code;

	/**
	 * Constructor
	 *
	 * @param data Reference to configuration
	 * @param tagSpecification  the tag itself
	 */
	public EMMTag(Data data, String tagSpecification) throws EMMTagException, SQLException {
		tag = null;
		mTagFullname = tagSpecification;
		mTagParameters = new HashMap<>();
		mTagAttributes = new ArrayList<>();

		// parse the tag
		List<String> parsed = splitTag(data);
		int pcnt;

		if ((parsed == null) || ((pcnt = parsed.size()) == 0)) {
			throw new EMMTagException(data, this, "failed in parsing (empty?) tag", "error.agnTag.parsing");
		}

		mTagName = parsed.get(0);
		for (int n = 1; n < pcnt; ++n) {
			String parm = parsed.get(n);
			int pos = parm.indexOf('=');

			if (pos != -1) {
				String variable = parm.substring(0, pos);
				String value = parm.substring(pos + 1);

				mTagParameters.put(variable, value);
			} else {
				mTagAttributes.add(parm);
			}
		}
		// check for special URL Tags

		// return if tag is a url tag, otherwise get tag info from database
		if (check_tags(data) == TAG_DBASE) {
			TagDAO.Entry entry = data.getTag(mTagName);

			if (entry != null) {
				mSelectString = entry.selectValue();
				mSelectType = entry.type();
				if (mSelectString == null) {
					throw new EMMTagException(data, this, "unknown empty tag", "error.agnTag.unknownEmpty");
				}
				interpretTagType(data);
			} else if (!dynamicTag(data)) {
				throw new EMMTagException(data, this, "unknown tag", "error.agnTag.unknown");
			}

			if (tagType == TAG_DBASE) {
				int pos, end;

				pos = 0;
				while ((pos = mSelectString.indexOf("[", pos)) != -1) {
					if ((end = mSelectString.indexOf("]", pos + 1)) != -1) {
						String id = mSelectString.substring(pos + 1, end);
						String rplc = substitute(data, id);

						if (rplc == null) {
							rplc = "[" + id + "]";
						}
						mSelectString = (pos > 0 ? mSelectString.substring(0, pos) : "") + (rplc == null ? "" : rplc) + (end < mSelectString.length() - 1 ? mSelectString.substring(end + 1) : "");
						pos += rplc.length() - (id.length() + 2) + 1;
					} else {
						break;
					}
				}

				// replace arguments of complex tags (in curly braces)
				//
				if (isComplex) {
					for (String e : mTagParameters.keySet()) {
						String alias = "{" + e + "}";
						if (mSelectString.indexOf(alias) == -1) {
							throw new EMMTagException(data, this, "parameter '" + alias + "' not found in tag entry", "error.agnTag.param.notFound", alias);
						}
						mSelectString = StringOps.replace(mSelectString, alias, mTagParameters.get(alias.substring(1, alias.length() - 1)));
					}

					if (mSelectString.indexOf("{") != -1) {
						String param = this.mSelectString.substring(mSelectString.indexOf("{") + 1, this.mSelectString.indexOf("}"));
						throw new EMMTagException(data, this, "missing required parameter '" + param + "'", "error.agnTag.param.required", param);
					}
				} else if (mTagParameters.size() > 0) {
					throw new EMMTagException(data, this, "no parameters supported in simple tag", "error.agnTag.simple.params.notSupported");
				}

				if (isPureData(mSelectString)) {
					mTagValue = StringOps.unSqlString(mSelectString);
					globalValue = true;
				}
			}
		}
		anon = null;
		punycodeValue = false;
		func = null;
		code = null;
	}

	/**
	 * String representation of ourself
	 *
	 * @return our representation
	 */
	@Override
	public String toString() {
		return mTagFullname + " (" + (isComplex ? "complex," : "") + tagType + "," + tagSpec + ")" + " = " + (mSelectString == null ? "" : "[" + mSelectString + "]") + (getTagValue() == null ? "*unset*" : "\"" + getTagValue() + "\"");
	}

	public void setTagValue(String value) {
		if (tag != null) {
			tag.value(value);
		} else {
			mTagValue = value;
		}
	}

	public String getTagValue() {
		return tag != null ? tag.value() : mTagValue;
	}

	/**
	 * Initialize for interal tags, throws an exception, if strict is True
	 * for validation of tags
	 *
	 * @param data   the global configuration
	 * @param strict if true, an exception is thrown when parsing
	 * @throws EMMTagException
	 */
	public <T extends Data> void initialize(T data, boolean strict) throws EMMTagException {
		switch (tagType) {
			case TAG_INTERNAL:
				initializeInternalTag(data, strict);
				break;
			default:
				break;
		}

		if (strict) {
			if (mTagParameters.size() > 0) {
				List<String> allowedParameters = new ArrayList<>();
				StringBuffer notAllowed = null;

				collectAllowedParameters(allowedParameters);
				for (String name : mTagParameters.keySet()) {
					boolean found = false;

					for (int n = 0; n < allowedParameters.size(); ++n) {
						String parm = allowedParameters.get(n);

						if (parm.equals(name) || parm.equals("*")) {
							found = true;
							break;
						} else if (parm.equals("")) {
							break;
						}
					}
					if (!found) {
						if (notAllowed == null) {
							notAllowed = new StringBuffer();
						} else {
							notAllowed.append(", ");
						}
						notAllowed.append(name);
					}
				}
				if (notAllowed != null) {
					throw new EMMTagException(data, this, "tag does not support these parameter: " + notAllowed, "error.agnTag.param.notAllowed", notAllowed.toString());
				}
			}
		}
		if (data.previewAnon) {
			anon = mTagParameters.get("anon");
			if (anon != null) {
				globalValue = true;
				setTagValue(anon);
			}
		}
	}

	/**
	 * Handle special cases on internal tags
	 *
	 * @param data Reference to configuration
	 */
	public String makeInternalValue(Data data, Custinfo cinfo) throws Exception {
		if (tagType != TAG_INTERNAL) {
			throw new Exception("Call makeInternalValue with tag type " + tagType);
		}
		switch (tagSpec) {
			case TI_DBV:         // is set before in Mailout.realFire ()
				break;
			case TI_DB:
				if (dbColumn != null) {
					mTagValue = dbColumn.get(dbFormat, dbExpression);
				}
				if (punycodeValue && (mTagValue != null)) {
					mTagValue = Str.punycodeEMail(mTagValue);
				}
				break;
			case TI_IMAGE:
				if ((imagePatternName != null) && (imagePatternColumn != null) && (! imagePatternColumn.getIsnull ())) {
					mTagValue = data.defaultImageLink(imagePatternName.replace("*", imagePatternColumn.get()), imageSource, false);
				}
				break;
			case TI_EMAIL:
				switch (emailCode) {
					case 1:
						if (mTagValue != null) {
							mTagValue = Str.punycodeEMail(mTagValue.trim());
						}
						break;
					default:
						break;
				}
				break;
			case TI_SUBSCRIBERCOUNT: {
				long cnt = data.totalSubscribers;
				String format = null;
				String str;

				if (((format = mTagParameters.get("format")) == null) && ((str = mTagParameters.get("type")) != null)) {
					str = str.toLowerCase();
					if (str.equals("us")) {
						format = "#,###,###";
					} else if (str.equals("de")) {
						format = "#.###.###";
					}
				}
				if ((str = mTagParameters.get("round")) != null) {
					try {
						int round = Integer.parseInt(str);

						if (round > 0) {
							cnt = (cnt + round - 1) / round;
						}
					} catch (NumberFormatException e) {
						// do nothing
					}
				}
				if (format != null) {
					int len = format.length();
					boolean first = true;
					int last = -1;
					mTagValue = "";

					for (int n = len - 1; n >= 0; --n) {
						if (format.charAt(n) == '#') {
							last = n;
						}
					}
					for (int n = len - 1; n >= 0; --n) {
						if (format.charAt(n) == '#') {
							if (first || (cnt != 0)) {
								if (n == last) {
									str = Long.toString(cnt);
									cnt = 0;
								} else {
									str = Long.toString(cnt % 10);
									cnt /= 10;
								}
								mTagValue = str + mTagValue;
								first = false;
							}
						} else if ((n < last) || (cnt != 0)) {
							mTagValue = format.substring(n, n + 1) + mTagValue;
						}
					}
				} else {
					mTagValue = Long.toString(cnt);
				}
			}
			break;
			case TI_DATE:			// is prepared here in initializeInternalTag () from check_tags
				if (dateFormat != null) {
					Date	value;
				
					if (dateColumn == null) {
						if (dateBase == null) {
							value = dateValue;
						} else {
							switch (dateBase) {
							case "now":
								value = new Date();
								break;
							case "senddate":
								value = data.genericSendDate();
								break;
							default:
								value = dateValue;
								break;
							}
						}
					} else if (dateColumn.getTypeID () == Column.DATE) {
						value = (Date) dateColumn.getValue ();
					} else {
						value = null;
					}
					if (value != null) {
						if (dateOffset != 0) {
							value = new Date (value.getTime () + (dateOffset * 1000));
						}
						if (dateExpressions != null) {
							for (int n = dateExpressions.length - 1; n >= 0; --n) {
								if (dateExpressions[n] != null) {
									dateCalendar.setTime (value);
									dateExpressions[n]
										.setVariable ("year", dateCalendar.get (Calendar.YEAR))
										.setVariable ("month", dateCalendar.get (Calendar.MONTH) + 1)
										.setVariable ("day", dateCalendar.get (Calendar.DAY_OF_MONTH))
										.setVariable ("hour", dateCalendar.get (Calendar.HOUR_OF_DAY))
										.setVariable ("minute", dateCalendar.get (Calendar.MINUTE))
										.setVariable ("second", dateCalendar.get (Calendar.SECOND));

									double result = dateExpressions[n].evaluate();
									if (n > 0) {
										int offset = (int) result;
										int index;

										if (n == 2) {
											--offset;
										}
										switch (n) {
											default:
											case 1:
												index = Calendar.YEAR;
												break;
											case 2:
												index = Calendar.MONTH;
												break;
											case 3:
												index = Calendar.DAY_OF_MONTH;
												break;
											case 4:
												index = Calendar.HOUR_OF_DAY;
												break;
											case 5:
												index = Calendar.MINUTE;
												break;
											case 6:
												index = Calendar.SECOND;
												break;
										}
										dateCalendar.set(index, offset);
										value = dateCalendar.getTime();
									} else {
										if (result == Math.floor(result)) {
											mTagValue = Long.toString((long) result);
										} else {
											mTagValue = Double.toString(result);
										}
									}
								}
							}
						}
						if ((dateExpressions == null) || (dateExpressions[0] == null)) {
							mTagValue = dateFormat.format(value);
						}
					}
				}
				break;
			case TI_SYSINFO:        // is set here in initializeInternalTag () from check_tags
				break;
			case TI_DYN:            // is handled in xml backend
			case TI_DYNVALUE:        // dito
				break;
			case TI_TITLE:
			case TI_TITLEFULL:
			case TI_TITLEFIRST: {
				Title title = data.getTitle(titleType);

				if (title != null) {
					titleError.setLength(0);
					mTagValue = title.makeTitle(titleMode, cinfo.getGender(), cinfo.getTitle(), cinfo.getFirstname(), cinfo.getLastname(), cinfo.getColumns(), titleError);
					if ((mTagValue.length() > 0) && ((titlePrefix != null) || (titlePostfix != null))) {
						mTagValue = (titlePrefix == null ? "" : titlePrefix) + mTagValue + (titlePostfix == null ? "" : titlePostfix);
					}
					if (titleError.length() > 0) {
						for (String error : titleError.toString().split("\n")) {
							data.logging(Log.WARNING, "emmtag", mTagFullname + ": " + error);
						}
					}
				} else {
					mTagValue = "";
				}
			}
			break;
			case TI_IMGLINK:        // is set in imageLinkReference
				break;
			case TI_CALL:
				if (code != null) {
					mTagValue = code.getCode();
				}
				break;
			default:
				if (tag != null) {
					tag.makeValue(cinfo);
				}
				break;
		}
		return getTagValue();
	}

	/**
	 * Set link reference for image link tag
	 *
	 * @param data  Reference to configuration
	 * @param urlID id of referenced URL
	 */
	public void imageLinkReference(Data data, long urlID) {
		String destination = ilURL;

		for (int n = 0; n < data.urlcount; ++n) {
			URL url = data.URLlist.get(n);

			if (url.getId() == urlID) {
				destination = url.getUrl();
				break;
			}
		}
		mTagValue = ilPrefix + destination + ilPostfix;
		globalValue = true;
	}

	private static Pattern findColumn = Pattern.compile("\\$(([a-z][a-z0-9_]*\\.)?[a-z][a-z0-9_]*)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	private void findColumns(String expression, Set<String> predef) {
		if (expression != null) {
			Matcher m = findColumn.matcher(expression);

			while (m.find()) {
				String column = m.group(1).toLowerCase();

				if (column.startsWith("cust.")) {
					column = column.substring(5);
				}
				predef.add(column);
			}
		}
	}

	public void requestFields(Data data, Set<String> predef) throws Exception {
		findColumns(mTagParameters.get("filter"), predef);
		if (tagType == TAG_INTERNAL) {
			switch (tagSpec) {
				case TI_DB:
					if (dbColumn != null) {
						predef.add(dbColumn.getQname());
					}
					break;
				case TI_IMAGE:
					if (imagePatternColumn != null) {
						predef.add(imagePatternColumn.getQname());
					}
					break;
				case TI_DATE:
					if (dateColumn != null) {
						predef.add(dateColumn.getQname());
					}
					break;
				case TI_DYN: {
					String type = mTagParameters.get("type");

					if (type != null) {
						String[] tdetail = type.split(":", 2);

						if ((tdetail.length == 2) && tdetail[0].equalsIgnoreCase("multi")) {
							String[] refs = tdetail[1].toLowerCase().split(", *");

							for (String ref : refs) {
								if ((data.references == null) || (!data.references.containsKey(ref))) {
									data.logging(Log.WARNING, "emmtag", mTagFullname + ": agnDYN using multi for non existing reference table " + ref);
								} else {
									Reference r = data.references.get(ref);

									if (r.isMulti()) {
										predef.add(r.name() + "." + Reference.multiID);
									}
								}
							}
						}
					}
				}
				break;
				case TI_TITLE:
				case TI_TITLEFULL:
				case TI_TITLEFIRST: {
					Title title = data.getTitle(titleType);

					if (title != null) {
						title.requestFields(predef, titleMode);
					}
				}
				break;
				case TI_CALL:
					if (code != null) {
						code.requestFields(predef, mTagParameters);
					}
					break;
				default:
					if (tag != null) {
						tag.requestFields(predef);
					}
					break;
			}
		}
	}

	/**
	 * Retruns the type of this tag
	 *
	 * @return the type of the tag
	 */
	public String getType() {
		String rc = mSelectType;

		if (tagType == TAG_INTERNAL) {
			switch (tagSpec) {
				case TI_CALL:
					if ((func != null) && (code != null)) {
						String lang = code.getLanguage();
						String name = code.getName();

						rc += ":" + (lang == null ? "" : lang) + ":" + (name == null ? "" : name) + ":" + func;
					}
					break;
				default:
					break;
			}
		}
		return rc;
	}

	/**
	 * Is this character a whitespace?
	 *
	 * @param ch the character to inspect
	 * @return true, if character is whitespace
	 */
	private boolean isspace(char ch) {
		return ((ch == ' ') || (ch == '\t') || (ch == '\n') || (ch == '\r') || (ch == '\f'));
	}

	protected String clearify(String tagString) {
		return StringOps.removeEntities(tagString).replaceAll("[„“‚‘“”‘’«»‹›]", "\"").trim();
	}

	/**
	 * Split a tag into its elements
	 *
	 * @return Vector of all elements
	 */
	private enum State {
		SearchForName,
		InName,
		SearchForEqualOrName,
		SearchForValue,
		EscapedValue,
		InQuotedValue,
		EscapedInQuotedValue,
		InValue,
		EscapedInValue,
		InEscapedQuotedValue,
		EscapedInEscapedQuotedValue
	}

	private List<String> splitTag(Data data) {
		int tlen;
		String tagString;

		tlen = mTagFullname.length();
		if ((tlen > 0) && (mTagFullname.charAt(0) == '[')) {
			tagString = mTagFullname.substring(1);
			--tlen;
		} else {
			tagString = mTagFullname;
		}
		if ((tlen > 0) && (tagString.charAt(tlen - 1) == ']')) {
			if ((tlen > 1) && (tagString.charAt(tlen - 2) == '/')) {
				tagString = tagString.substring(0, tlen - 2);
			} else {
				tagString = tagString.substring(0, tlen - 1);
			}
		}
		tagString = clearify(tagString);
		tlen = tagString.length();

		List<String> rc = new ArrayList<>();

		State state = State.SearchForName;
		char quote = '\0';
		StringBuffer scratch = new StringBuffer(tlen);

		for (char ch : tagString.toCharArray()) {
			switch (state) {
				case SearchForName:
					if (!isspace(ch)) {
						scratch.setLength(0);
						scratch.append(ch);
						state = State.InName;
					}
					break;
				case InName:
					if (!isspace(ch)) {
						scratch.append(ch);
						if (ch == '=') {
							state = State.SearchForValue;
						}
					} else {
						state = State.SearchForEqualOrName;
					}
					break;
				case SearchForEqualOrName:
					if (!isspace(ch)) {
						if (ch == '=') {
							scratch.append(ch);
							state = State.SearchForValue;
						} else {
							rc.add(scratch.toString());
							scratch.setLength(0);
							scratch.append(ch);
							state = State.InName;
						}
					}
					break;
				case SearchForValue:
					if (!isspace(ch)) {
						if (ch == '\\') {
							state = State.EscapedValue;
						} else if ((ch == '"') || (ch == '\'')) {
							quote = ch;
							state = State.InQuotedValue;
						} else {
							scratch.append(ch);
							state = State.InValue;
						}
					}
					break;
				case EscapedValue:
					if ((ch == '"') || (ch == '\'')) {
						quote = ch;
						state = State.InEscapedQuotedValue;
					} else {
						scratch.append(ch);
						state = State.InValue;
					}
					break;
				case InQuotedValue:
					if (ch == '\\') {
						state = State.EscapedInQuotedValue;
					} else if (ch == quote) {
						rc.add(scratch.toString());
						state = State.SearchForName;
					} else {
						scratch.append(ch);
					}
					break;
				case EscapedInQuotedValue:
					scratch.append(ch);
					state = State.InQuotedValue;
					break;
				case InValue:
					if (isspace(ch)) {
						rc.add(scratch.toString());
						state = State.SearchForName;
					} else if (ch == '\\') {
						state = State.EscapedInValue;
					} else {
						scratch.append(ch);
					}
					break;
				case EscapedInValue:
					scratch.append(ch);
					state = State.InValue;
					break;
				case InEscapedQuotedValue:
					if (ch == '\\') {
						state = State.EscapedInEscapedQuotedValue;
					} else {
						scratch.append(ch);
					}
					break;
				case EscapedInEscapedQuotedValue:
					if (ch == quote) {
						rc.add(scratch.toString());
						state = State.SearchForName;
					} else {
						scratch.append(ch);
					}
					break;
				default:
					break;
			}
		}
		if (scratch.length() > 0) {
			switch (state) {
				case InName:
				case SearchForEqualOrName:
				case InValue:
					rc.add(scratch.toString());
					break;
				case EscapedInEscapedQuotedValue:
					break;
				case EscapedInQuotedValue:
					break;
				case EscapedInValue:
					break;
				case EscapedValue:
					break;
				case InEscapedQuotedValue:
					break;
				case InQuotedValue:
					break;
				case SearchForName:
					break;
				case SearchForValue:
					break;
				default:
					break;
			}
		}
		return rc;
	}

	/**
	 * Checks a select value if its just a pure
	 * (string or numeric) data for marking it
	 * as global value to avoid including it in
	 * the global select call
	 *
	 * @param str the string to check
	 * @return true, if its pure data, false otherwise
	 */
	private boolean isPureData(String str) {
		int slen = str.length();

		if (slen > 0) {
			if ((slen >= 2) && (str.charAt(0) == '\'') && (str.charAt(slen - 1) == '\'')) {
				return true;
			}
			if (slen > 0) {
				int n;
				char ch;

				n = 0;
				while (n < slen) {
					ch = str.charAt(n);
					if (((n == 0) && (ch == '-')) || Character.isDigit(ch)) {
						++n;
					} else {
						break;
					}
				}
				return n == slen;
			}
		}
		return false;
	}

	private String substitute(Data data, String id) {
		switch (id) {
			case "licence-id":	return Integer.toString(data.licenceID());
			case "company-id":	return Long.toString(data.company.id());
			case "company-token":	return data.company.token ();
			case "mailinglist-id":	return Long.toString(data.mailinglist.id());
			case "mailing-id":	return Long.toString(data.mailing.id());
			case "rdir-domain":	return data.rdirDomain;
			default:		return null;
		}
	}

	/**
	 * parse specific tag type found in database
	 */
	protected void interpretTagType(Data data) {
		if ((tagType == TAG_DBASE) && (mSelectType != null) && mSelectType.equals("FUNCTION")) {
			tagType = TAG_INTERNAL;
			tagSpec = TI_CALL;
		} else if ((tagType == TAG_DBASE) && (mSelectType != null)) {
			if (mSelectType.equals("COMPLEX")) {
				isComplex = true;
			}
		}
	}

	/**
	 * Try to interpret an unknown tag in a dynamic way,
	 * guessing what the user tries to use
	 *
	 * @return True, if we interpreted it someway
	 */
	protected boolean dynamicTag(Data data) {
		boolean rc = false;

		if (mTagName.length() > 3) {
			String column = mTagName.substring(3);

			if (data.columnByName(column) != null) {
				tagType = TAG_INTERNAL;
				tagSpec = TI_DB;
				mTagParameters.put("column", column);
				rc = true;
			}
		}
		return rc;
	}

	/**
	 * Determinate the type of the tag
	 *
	 * @param data Reference to configuration
	 */
	private int check_tags(Data data) {
		globalValue = false;

		int n;

		for (n = 0; n < TAG_INTERNALS.length; ++n) {
			if (mTagName.equals(TAG_INTERNALS[n])) {
				break;
			}
		}
		if (n < TAG_INTERNALS.length) {
			tagType = TAG_INTERNAL;
			tagSpec = n;
		} else if (Tag.has(mTagName)) {
			tagType = TAG_INTERNAL;
			tagSpec = TI_MODULE;
		} else {
			tagType = TAG_DBASE;
			tagSpec = 0;
		}
		return tagType;
	}

	/**
	 * Collect allowed parameter by this tag for
	 * more strict tag checking
	 *
	 * @param collect the vector to collect the allowed names
	 */
	private void collectAllowedParameters(List<String> collect) {
		collect.add("anon");
		collect.add("filter");
		collect.add("onerror");
		switch (tagType) {
			case TAG_DBASE:
				collect.add("*");
				break;
			case TAG_INTERNAL:
				switch (tagSpec) {
					case TI_DBV:
					case TI_DB:
						collect.add("column");
						collect.add("format");
						collect.add("expression");
						collect.add("encode");
						collect.add("language");
						collect.add("country");
						collect.add("timezone");
						collect.add("code");
						break;
					case TI_IMAGE:
						collect.add("name");
						collect.add("source");
						collect.add("namepattern");
						collect.add("patternsource");
						break;
					case TI_EMAIL:
						collect.add("code");
						break;
					case TI_SUBSCRIBERCOUNT:
						collect.add("");
						break;
					case TI_DATE:
						collect.add("type");
						collect.add("column");
						collect.add("language");
						collect.add("base");
						collect.add("country");
						collect.add("timezone");
						collect.add("offset");
						collect.add("format");
						collect.add("expression");
						break;
					case TI_SYSINFO:
						collect.add("name");
						collect.add("default");
						break;
					case TI_DYN:
						collect.add("name");
						collect.add("type");
						break;
					case TI_DYNVALUE:
						collect.add("name");
						break;
					case TI_TITLE:
					case TI_TITLEFULL:
					case TI_TITLEFIRST:
						collect.add("type");
						collect.add("prefix");
						collect.add("postfix");
						break;
					case TI_IMGLINK:
						collect.add("name");
						break;
					case TI_CALL:
						collect.add("*");
						break;
					case TI_SWYN:
						collect.add("networks");
						collect.add("title");
						collect.add("link");
						collect.add("selector");
						collect.add("bare");
						collect.add("size");
						break;
					case TI_SENDDATE:
						collect.add("format");
						break;
					case TI_GRIDPH:
						collect.add("name");
						break;
					default:
						if (tag != null) {
							tag.collectAllowedParameters(collect);
						}
						break;
				}
				break;
			default:
				break;
		}
	}

	private Column findColumn(Data data, boolean strict, String name) throws EMMTagException {
		Column rc = null;

		if (name != null) {
			name = name.trim();

			Column col = data.columnByName(name);

			if (col == null) {
				String orig = name;
				Column alias = data.columnByAlias(name);
				int len = name.length();
				int n;

				for (n = 0; n < len; ++n) {
					char ch = name.charAt(n);

					if (((n == 0) && (!Character.isLetter(ch)) && (ch != '_')) || ((n > 0) && (!Character.isLetterOrDigit(ch)) && (ch != '_'))) {
						break;
					}
				}
				if (n < len) {
					name = name.substring(0, n);
					col = data.columnByName(name);
				}
				if ((col == null) && (alias != null)) {
					name = alias.getQname();
					col = alias;
				} else {
					data.logging(Log.WARNING, "emmtag", mTagFullname + ": unknown column referenced for " + internalTagName() + ": " + orig);
					if (strict) {
						throw new EMMTagException(data, this, "paramater \"column\" tries to reference unknown database column " + orig, "error.mailing.tag.column.unknown", orig);
					}
				}
			}
			rc = col;
		}
		return rc;
	}

	private String internalTagName() {
		if (tagSpec == TI_CALL) {
			return "-function-";
		} else if (tag != null) {
			return tag.name();
		}
		return TAG_INTERNALS[tagSpec];
	}

	/**
	 * Initialize the tag, if its an internal one
	 *
	 * @param data Reference to configuration
	 */
	protected void initializeInternalTag(Data data, boolean strict) throws EMMTagException {
		switch (tagSpec) {
			case TI_DBV:
			case TI_DB:
				mSelectString = mTagParameters.get("column");
				dbColumn = null;
				dbFormat = new Format(mTagParameters.get("format"), mTagParameters.get("encode"), data.getLocale(mTagParameters.get("language"), mTagParameters.get("country")), data.getTimeZone(mTagParameters.get("timezone")));
				if (dbFormat.error() != null) {
					data.logging(Log.WARNING, "emmtag", mTagFullname + ": error in formatting: " + dbFormat.error());
					if (strict) {
						throw new EMMTagException(data, this, "invalid formatting: " + dbFormat.error(), "error.agnTag.format.invalid", dbFormat.error());
					}
				}
				if (tagSpec == TI_DBV) {
					if (mSelectString != null) {
						mSelectString = mSelectString.trim().toLowerCase();
					} else {
						data.logging(Log.WARNING, "emmtag", mTagFullname + ": missing virtual column");
						if (strict) {
							throw new EMMTagException(data, this, "missing parameter \"column\"", "error.default.missing.param", "column");
						}
					}
				} else if (tagSpec == TI_DB) {
					if (mSelectString == null) {
						data.logging(Log.WARNING, "emmtag", mTagFullname + ": missing column parameter for " + TAG_INTERNALS[TI_DB]);
						if (strict) {
							throw new EMMTagException(data, this, "missing parameter \"column\"", "error.default.missing.param", "column");
						}
					} else {
						Column col = findColumn(data, strict, mSelectString);

						if (col != null) {
							String err = col.validate(dbFormat);

							if (err != null) {
								data.logging(Log.WARNING, "emmtag", mTagFullname + ": invalid format: " + err);
								if (strict) {
									throw new EMMTagException(data, this, "invalid formatting using parameter \"format\"", "error.agnTag.param.format.invalid", "format");
								}
							}
							mSelectString = col.getQname();
						}
						dbColumn = col;
					}
				}
				String expression = mTagParameters.get("expression");

				if (expression != null) {
					try {
						ExpressionBuilder build = new ExpressionBuilder(expression.toLowerCase());

						build.variable("value");
						if (dbColumn != null) {
							build.variables(dbColumn.getName(), dbColumn.getQname());
						}
						dbExpression = build.build();
					} catch (Exception e) {
						data.logging(Log.WARNING, "emmtag", mTagFullname + ": invalid expression: " + e);
						if (strict) {
							throw new EMMTagException(data, this, "invalid \"expression\"", "error.agnTag.expression.invalid", expression);
						}
					}
				} else {
					dbExpression = null;
				}
				String encode = mTagParameters.get("code");

				if (encode != null) {
					if (encode.equals("punycode")) {
						punycodeValue = true;
					} else {
						data.logging(Log.WARNING, "emmtag", mTagFullname + ": invalid code: " + encode);
						if (strict) {
							throw new EMMTagException(data, this, "invalid value for parameter \"code\"", "error.agnTag.value.invalid", "code");
						}
					}
				}
				break;
			case TI_IMAGE: {
				String name = mTagParameters.get("name");
				imageSource = mTagParameters.get("source");
				imagePatternName = mTagParameters.get("namepattern");
				String patternSource = mTagParameters.get("patternsource");

				if (imagePatternName != null) {
					if (imagePatternName.indexOf('*') == -1) {
						data.logging(Log.WARNING, "emmtag", mTagFullname + ": no placeholder found");
						if (strict) {
							throw new EMMTagException(data, this, "missing placeholder \"*\" in \"namepattern\"", "error.agnTag.placeholder.missing", "namepattern");
						}
					}
					if (patternSource == null) {
						data.logging(Log.WARNING, "emmtag", mTagFullname + ": no pattrern source supplied");
						if (strict) {
							throw new EMMTagException(data, this, "missing parameter \"patternsource\"", "error.default.missing.param", "patternsource");
						}
					} else {
						imagePatternColumn = findColumn(data, strict, patternSource);
					}
				} else {
					if (name == null) {
						data.logging(Log.WARNING, "emmtag", mTagFullname + ": missing name");
						if (strict) {
							throw new EMMTagException(data, this, "missing parameter \"name\"", "error.default.missing.param", "name");
						}
					}
					mTagValue = data.defaultImageLink(name, imageSource, true);
					globalValue = true;
				}
			}
			break;
			case TI_EMAIL: {
				emailCode = 0;
				String tagCode = mTagParameters.get("code");

				if (tagCode != null) {
					if (tagCode.equals("punycode")) {
						emailCode = 1;
					} else {
						data.logging(Log.WARNING, "emmtag", mTagFullname + ": unknown coding for email found: " + tagCode);
						if (strict) {
							throw new EMMTagException(data, this, "invalid value for parameter \"code\"", "error.agnTag.value.invalid", "code");
						}
					}
				}
			}
			break;
			case TI_SUBSCRIBERCOUNT:
				globalValue = true;
				break;
			case TI_DATE:
				try {
					String temp;
					int type;
					String lang;
					String country;
					String typestr;

					if ((temp = mTagParameters.get("type")) != null) {
						type = Integer.parseInt(temp);
					} else {
						type = 0;
					}
					dateBase = null;
					if ((temp = mTagParameters.get("column")) != null) {
						dateColumn = findColumn(data, strict, temp);
						if (dateColumn != null) {
							if (dateColumn.getTypeID() != Column.DATE) {
								data.logging(Log.WARNING, "emmtag", mTagFullname + ": invalid column type for " + temp);
								if (strict) {
									throw new EMMTagException(data, this, "invalid column data type (expect date) using parameter \"column\"", "error.agnTag.column.type.invalid", "date", "column");
								}
							}
						}
					} else {
						dateColumn = null;
						if (data.currentSendDate == null) {
							dateValue = new Date();
						} else {
							dateValue = data.currentSendDate;
						}
						if ((temp = mTagParameters.get("base")) != null) {
							switch (temp) {
							default:
								data.logging(Log.WARNING, "emmtag", mTagFullname + ": unknown base date value: " + temp);
								if (strict) {
									throw new EMMTagException(data, this, "unknown base date value " + temp + " for parameter \"base\"", "error.agnTag.baseDate.unknown", temp, "base");
								}
								break;
							case "now":
							case "senddate":
								dateBase = temp;
								break;
							}
						}
					}
					lang = mTagParameters.get("language");
					country = mTagParameters.get("country");
					dateOffset = 0;
					if ((temp = mTagParameters.get("offset")) != null) {
						try {
							dateOffset = (long) (Double.parseDouble(temp) * (24 * 60 * 60));
						} catch (NumberFormatException e) {
							data.logging(Log.WARNING, "emmtag", mTagFullname + ": invalid offset " + temp + " for date found");
							if (strict) {
								throw new EMMTagException(data, this, "invalid offset \"" + temp + "\"", "error.agnTag.offset.invalid", temp);
							}
						}
					}
					if ((temp = mTagParameters.get("format")) != null) {
						typestr = temp;
					} else {
						String query = "SELECT format FROM date_tbl WHERE type = :type";

						typestr = "d.M.yyyy";
						try (DBase.With with = data.dbase.with ()) {
							Map<String, Object> row;

							row = data.dbase.querys(with.cursor (), query, "type", type);
							if (row != null) {
								typestr = data.dbase.asString(row.get("format"));
							} else {
								data.logging(Log.WARNING, "emmtag", mTagFullname + ": no format in date_tbl found for " + mTagFullname);
								if (strict) {
									throw new EMMTagException(data, this, "No format in database found for parameter \"type\" " + type, "error.agnTag.format.notFound", "type");
								}
							}
						} catch (Exception e) {
							data.logging(Log.WARNING, "emmtag", mTagFullname + ": query failed for data_tbl: " + e);
							if (strict) {
								throw new EMMTagException(data, this, e.toString(), "Error");
							}
						}
					}
					if ((temp = mTagParameters.get("expression")) != null) {
						Pattern modifier = Pattern.compile("^(year|month|day|hour|minute|second): *(.*)$", Pattern.MULTILINE | Pattern.DOTALL);

						dateExpressions = new Expression[7];
						for (String s : temp.split("; *")) {
							Matcher m = modifier.matcher(s);
							int index = 0;

							if (m.matches()) {
								s = m.group(2);
								switch (m.group(1)) {
									case "year":
										index = 1;
										break;
									case "month":
										index = 2;
										break;
									case "day":
										index = 3;
										break;
									case "hour":
										index = 4;
										break;
									case "minute":
										index = 5;
										break;
									case "second":
										index = 6;
										break;
									default:
										break;
								}
							}
							try {
								ExpressionBuilder builder = new ExpressionBuilder(s);

								builder.variables("year", "month", "day", "hour", "minute", "second");
								dateExpressions[index] = builder.build();
							} catch (Exception e) {
								data.logging(Log.WARNING, "emmtag", mTagFullname + ": invalid expression: " + e);
								if (strict) {
									throw new EMMTagException(data, this, "invalid \"expression\"", "error.agnTag.expression.invalid", s);
								}
								dateExpressions = null;
								break;
							}
						}
					} else {
						dateExpressions = null;
					}
					dateCalendar = null;

					Locale l = data.getLocale(lang, country);

					if (l == null) {
						dateFormat = new SimpleDateFormat(typestr);
						if (dateExpressions != null) {
							dateCalendar = Calendar.getInstance();
						}
					} else {
						dateFormat = new SimpleDateFormat(typestr, l);
						if (dateExpressions != null) {
							dateCalendar = Calendar.getInstance(l);
						}
					}

					TimeZone timeZone = data.getTimeZone(mTagParameters.get("timezone"));

					if (timeZone != null) {
						dateFormat.setTimeZone(timeZone);
					}
				} catch (Exception e) {
					data.logging(Log.WARNING, "emmtag", mTagFullname + ": failed parsing tag: " + e.toString());
					if (strict) {
						throw new EMMTagException(data, this, e.toString(), "Error");
					}
				}
				if ((dateColumn == null) && (dateBase == null)) {
					globalValue = true;
				}
				break;
			case TI_SYSINFO: {
				String dflt = mTagParameters.get("default");

				mTagValue = dflt == null ? "" : dflt;
			}
			globalValue = true;
			break;
			case TI_DYN:
			case TI_DYNVALUE:
				if (strict) {
					if (mTagParameters.get("name") == null) {
						throw new EMMTagException(data, this, "missing parameter \"name\"", "error.default.missing.param", "name");
					}
				}
				break;
			case TI_TITLE:
			case TI_TITLEFULL:
			case TI_TITLEFIRST: {
				String temp;

				if ((temp = mTagParameters.get("type")) != null) {
					try {
						titleType = Long.parseLong(temp);
					} catch (java.lang.NumberFormatException e) {
						data.logging(Log.WARNING, "emmtag", mTagFullname + ": invalid type string type=\"" + temp + "\", using default 0");
						titleType = 1;
						if (strict) {
							throw new EMMTagException(data, this, "invalid value for parameter \"type\"", "error.agnTag.invalidParamValue", "type");
						}
					}
				} else {
					titleType = 1;
				}
				titleError = new StringBuffer();
				titlePrefix = mTagParameters.get("prefix");
				titlePostfix = mTagParameters.get("postfix");
				if (tagSpec == TI_TITLE) {
					titleMode = Title.TITLE_DEFAULT;
				} else if (tagSpec == TI_TITLEFULL) {
					titleMode = Title.TITLE_FULL;
				} else if (tagSpec == TI_TITLEFIRST) {
					titleMode = Title.TITLE_FIRST;
				}
				if (strict) {
					Title title = null;
					HashSet<String> cols = new HashSet<>();

					try {
						title = data.getTitle(titleType);
					} catch (SQLException e) {
						throw new EMMTagException(data, this, "failed to query for title type=\"" + temp + "\": " + e.toString(), "error.agnTag.loadTitle.failed", temp);
					}
					if (title == null) {
						throw new EMMTagException(data, this, "no title for title type=\"" + temp + "\" found", "error.agnTag.title.type.unknown", temp);
					}
					title.requestFields(cols, titleMode);
					for (String col : cols) {
						if (data.columnByName(col) == null) {
							throw new EMMTagException(data, this, "column \"" + col + "\" required by title type=\"" + temp + "\" not found in customer table", "error.agnTag.error.agnTag.requiredTitleData.notFound", col, temp);
						}
					}
				}
			}
			break;
			case TI_IMGLINK: {
				String name = mTagParameters.get("name");
				String source = Imagepool.MAILING;

				ilURL = null;
				ilPrefix = null;
				ilPostfix = null;
				if (name != null) {
					ilURL = data.defaultImageLink(name, source, true);
					ilPrefix = "<a href=\"";
					ilPostfix = "\"><img src=\"" + ilURL + "\" border=\"0\"></a>";
				} else {
					data.logging(Log.WARNING, "emmtag", mTagFullname + ": missing name");
					if (strict) {
						throw new EMMTagException(data, this, "missing parameter \"name\"", "error.default.missing.param", "name");
					}
				}
			}
			break;
			case TI_CALL:
				int n;
				String cname;

				cname = mSelectString;
				if ((n = cname.indexOf(':')) != -1) {
					func = cname.substring(n + 1);
					cname = cname.substring(0, n);
				} else if (mTagName.length() > 3) {
					func = mTagName.substring(3).toLowerCase();
				} else {
					func = cname;
				}
				if ((cname == null) || ((code = data.findCode(cname)) == null)) {
					data.logging(Log.WARNING, "emmtag", mTagFullname + ": no code \"" + cname + "\" for function '" + func + "' found");
					if (strict) {
						throw new EMMTagException(data, this, "function \"" + func + "\" not found", "error.agnTag.function.notFound", func);
					}
				}
				mTagValue = "";
				globalValue = true;
				break;
			case TI_SWYN:
				SWYN swyn = data.getSWYN();
				List<String> networks = null;
				String pNetwork = mTagParameters.get("networks");
				String pTitle = mTagParameters.get("title");
				String pLink = mTagParameters.get("link");
				String pSelector = mTagParameters.get("selector");
				String pBare = mTagParameters.get("bare");
				String pSize = mTagParameters.get("size");

				if (pNetwork != null) {
					String[] nwlist = pNetwork.split(",");

					for (n = 0; n < nwlist.length; ++n) {
						String nw = nwlist[n].trim();

						if (nw.length() > 0) {
							if (networks == null) {
								networks = new ArrayList<>();
							}
							networks.add(nw);
						}
					}
				}
				mTagValue = swyn.build(Str.atob(pBare, false), (pSize == null ? "default" : pSize), networks, pTitle, pLink, pSelector);
				globalValue = true;
				break;
			case TI_SENDDATE:
				String format = mTagParameters.get("format");
				mTagValue = (new SimpleDateFormat (format != null ? format : "yyyyMMdd")).format(data.genericSendDate ());
				globalValue = true;
				break;
			case TI_GRIDPH:
				if (strict) {
					if (mTagParameters.get("name") == null) {
						throw new EMMTagException(data, this, "Missing parameter \"name\"", "error.default.missing.param", "name");
					}
				}
				break;
			default:
				tag = Tag.get(mTagName);
				if (tag != null) {
					tag.setup(data, this, mTagFullname, mTagName, mTagParameters);
					tag.initialize();
					globalValue = tag.isGlobalValue();
				}
				break;
		}
	}
}
