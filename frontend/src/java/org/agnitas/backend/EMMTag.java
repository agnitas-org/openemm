/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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
import org.agnitas.util.Log;
import org.agnitas.util.Title;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/** Class EMMTAG
 * - stores information about a single agnitas-tag
 * - constructor rectrieves selectvalues associated with tag-name from dbase
 * - after db query for a record set (user), EmmTag.mTagValue holds the value
 *	 for this tag
 */
public class EMMTag {
	/** This tag is taken from the database */
	public final static int	TAG_DBASE = 0;
	/** This tag is handled internally */
	public final static int	TAG_INTERNAL = 1;
	/** Internal tag, virtual Database column */
	public final static int	TI_DBV = 0;
	/** Internal tag, database column */
	public final static int	TI_DB = 1;
	/** Internal tag, image link */
	public final static int	TI_IMAGE = 2;
	/** Internal tag, email address */
	public final static int	TI_EMAIL = 3;
	/** Internal tag, number of subscriber for this mailing */
	public final static int	TI_SUBSCRIBERCOUNT = 4;
	/** Internal tag, current date */
	public final static int	TI_DATE = 5;
	/** Internal tag, system information created during final mail creation */
	public final static int	TI_SYSINFO = 6;
	/** Internal tag, dynamic condition */
	public final static int	TI_DYN = 7;
	/** Internal tag, dynamic content */
	public final static int	TI_DYNVALUE = 8;
	/** Handle title tags */
	public final static int	TI_TITLE = 9;
	/** Handle full title tags */
	public final static int	TI_TITLEFULL = 10;
	/** Handle title tags for first name only */
	public final static int	TI_TITLEFIRST = 11;
	/** Create image link tags */
	public final static int	TI_IMGLINK = 12;
	public final static int	TI_CALL = 13;
	public final static int	TI_SWYN = 14;
	public final static int	TI_SENDDATE = 15;
	public final static int	TI_GRIDPH = 16;
	/** Names of all internal tags */
	final static String[]	TAG_INTERNALS = {
		"agnDBV",
		"agnDB",
		"agnIMAGE",
		"agnEMAIL",
		"agnSUBSCRIBERCOUNT",
		"agnDATE",
		"agnSYSINFO",
		"agnDYN",
		"agnDVALUE",
		"agnTITLE",
		"agnTITLEFULL",
		"agnTITLEFIRST",
		"agnIMGLINK",
		null,
		"agnSWYN",
		"agnSENDDATE",
		"gridPH"
	};
	/** The full name of this tag including all parameters */
	public String		mTagFullname;
	/** The name of the tag */
	public String		mTagName;
	/** All parameters parsed into a hash */
	public Map <String, String>
				mTagParameters;
	/** Number of available parameter */
	private int		mNoOfParameters;
	/** invalid parameter */
	private StringBuffer	invalidParameters;
	/** Is this a complex, e.g. dynamic changeable tag */
	private boolean		isComplex;
	/** Howto select this tag from the database */
	public String		mSelectString;
	/** Howto interpret this string */
	public String		mSelectType;
	/** Result of this tag, is set for each customer, if not fixed or global */
	public String		mTagValue;
	/** The tag type */
	public int		 tagType;
	/** The tag type specification */
	public int		 tagSpec;
	/** If this tag is fixed, e.g. can be inserted already here */
	public boolean		fixedValue;
	/** If this tag is global, but will be inserted during final mail creation */
	public boolean		globalValue;

	/** Internal used to format agnDB values */
	public Column		dbColumn;
	public Format		dbFormat;
	/** Internal used value on how to code an email */
	private int		emailCode;
	/** Internal used column, if not current date */
	private Column		dateColumn;
	/** Internal used format, if this is a date tag */
	private SimpleDateFormat
				dateFormat;
	/** Internal used offset in seconds for date tag */
	private long		dateOffset;
	/** Internal used expression for modifying/calculating a date */
	private Expression[]	dateExpressions;
	private Calendar	dateCalendar;
	/** Internal used title type */
	private long		titleType;
	/** Internal used pre-/postfix for title generation */
	private String		titlePrefix, titlePostfix;
	/** Internal used title mode */
	private int		titleMode;
	/** Internal used reference to image component */
	private String		ilPrefix, ilPostfix;
	public String		ilURL;
	/** used as return value for anon preview */
	private String		anon;
	private boolean		punycodeValue;
	/** Variables to hold private data for functions */
	private String		func;
	private Code		code;
	
	/** Constructor
	 * @param data Reference to configuration
	 * @param tag the tag itself
	 */
	public EMMTag (Data data, String tag) throws EMMTagException, SQLException {
		mTagFullname = tag;
		mTagParameters = new HashMap <> ();
		invalidParameters = null;

		// parse the tag
		List <String>	parsed = splitTag (data);
		int		pcnt;

		if ((parsed == null) || ((pcnt = parsed.size ()) == 0)) {
			throw new EMMTagException (data, this, "failed in parsing (empty?) tag");
		}

		mTagName = parsed.get (0);
		for (int n = 1; n < pcnt; ++n) {
			String	parm = parsed.get (n);
			int pos = parm.indexOf ('=');

			if (pos != -1) {
				String	variable = parm.substring (0, pos);
				String	value = parm.substring (pos + 1);

				mTagParameters.put (variable, value);
			} else {
				if (invalidParameters == null) {
					invalidParameters = new StringBuffer ();
				} else {
					invalidParameters.append (", ");
				}
				invalidParameters.append (parm);
			}
		}
		mNoOfParameters = mTagParameters.size ();

		// check for special URL Tags

		// return if tag is a url tag, otherwise get tag info from database
		if (check_tags(data) == TAG_DBASE) {
			TagDAO.Entry	entry = data.getTag (mTagName);
			
			if (entry != null) {
				mSelectString = entry.selectValue ();
				mSelectType = entry.type ();
				if (mSelectString == null) {
					throw new EMMTagException (data, this, "unknown empty tag");
				}
				interpretTagType (data);
			} else if (! dynamicTag (data)) {
				throw new EMMTagException (data, this, "unknown tag");
			}

			if (tagType == TAG_DBASE) {
				int pos, end;

				pos = 0;
				while ((pos = mSelectString.indexOf ("[", pos)) != -1)
					if ((end = mSelectString.indexOf ("]", pos + 1)) != -1) {
						String	id = mSelectString.substring (pos + 1, end);
						String	rplc = substitute (data, id);

						if (rplc == null) {
							rplc = "[" + id + "]";
						}
						mSelectString = (pos > 0 ? mSelectString.substring (0, pos) : "") +
								(rplc == null ? "" : rplc) +
								(end < mSelectString.length () - 1 ? mSelectString.substring (end + 1) : "");
						pos += rplc.length () - (id.length () + 2) + 1;
					} else
						break;

				// replace arguments of complex tags (in curly braces)
				//
				if (isComplex) {
					for (String e : mTagParameters.keySet ()) {
						String alias = "{" + e + "}";
						if (mSelectString.indexOf(alias) == -1) {
							throw new EMMTagException (data, this, "parameter '" + alias +"' not found in tag entry");
						}
						mSelectString = StringOps.replace( mSelectString,
							alias, mTagParameters.get (alias.substring(1, alias.length() - 1)) );
					}

					if (mSelectString.indexOf("{") != -1) {
						throw new EMMTagException (data, this, "missing required parameter '" + this.mSelectString.substring(mSelectString.indexOf("{") + 1, this.mSelectString.indexOf("}")) + "'");
					}
				} else if (mNoOfParameters > 0) {
					throw new EMMTagException (data, this, "no parameters supported in simple tag");
				}

				if (isPureData (mSelectString)) {
					mTagValue = StringOps.unSqlString (mSelectString);
					fixedValue = true;
				}
			}
		}
		anon = null;
		punycodeValue = false;
		func = null;
		code = null;
	}

	/** String representation of outself
	 * @return our representation
	 */
	@Override
	public String toString () {
		return mTagFullname +
			" (" + (isComplex ? "complex," : "") + tagType + "," + tagSpec + ")" +
			" = " +
			(mSelectString == null ? "" : "[" + mSelectString + "]") +
			(mTagValue == null ? "*unset*" : "\"" + mTagValue + "\"");
	}

	public void setmTagValue(String mTagValue) {
		this.mTagValue = mTagValue;
	}

	public String getmTagValue() {
		return mTagValue;
	}

	/**
	 * Initialize for interal tags, throws an exception, if strict is True
	 * for validation of tags
	 * 
	 * @param data the global configuration
	 * @param strict if true, an exception is thrown when parsing
	 * @throws EMMTagException
	 */
	public void initialize (Data data, boolean strict) throws EMMTagException {
		if (invalidParameters != null) {
			data.logging (Log.WARNING, "emmtag", "Invalid parameters: " + invalidParameters);
			if (strict) {
				throw new EMMTagException (data, this, "invalid parameters: \"" + invalidParameters + "\"");
			}
		}
		if (strict) {
			if (mTagParameters.size () > 0) {
				List <String> allowedParameters = new ArrayList <> ();
				StringBuffer	notAllowed = null;

				collectAllowedParameters (allowedParameters);
				for (String name : mTagParameters.keySet ()) {
					boolean found = false;

					for (int n = 0; n < allowedParameters.size (); ++n) {
						String	parm = allowedParameters.get (n);

						if (parm.equals (name) || parm.equals ("*")) {
							found = true;
							break;
						} else if (parm.equals ("")) {
							break;
						}
					}
					if (! found) {
						if (notAllowed == null) {
							notAllowed = new StringBuffer ();
						} else {
							notAllowed.append (", ");
						}
						notAllowed.append (name);
					}
				}
				if (notAllowed != null) {
					throw new EMMTagException (data, this, "tag does not support these parameter: " + notAllowed);
				}
			}
		}
		switch (tagType) {
		case TAG_INTERNAL:
			initializeInternalTag (data, strict);
			break;
		}
		
		if (data.previewAnon) {
			anon = mTagParameters.get ("anon");
			if (anon != null) {
				fixedValue = true;
				mTagValue = anon;
			}
		}
	}

	/** Handle special cases on internal tags
	 * @param data Reference to configuration
	 */
	public String makeInternalValue (Data data, Custinfo cinfo) throws Exception {
		if (tagType != TAG_INTERNAL) {
			throw new Exception ("Call makeInternalValue with tag type " + tagType);
		}
		switch (tagSpec) {
		case TI_DBV:		 // is set before in Mailout.realFire ()
			break;
		case TI_DB:
			if (dbColumn != null) {
				mTagValue = dbColumn.get (dbFormat);
			}
			if (punycodeValue && (mTagValue != null)) {
				mTagValue = StringOps.punycodeEMail (mTagValue);
			}
			break;
		case TI_IMAGE:
			break;
		case TI_EMAIL:
			switch (emailCode) {
			case 1:
				if (mTagValue != null)
					mTagValue = StringOps.punycodeEMail (mTagValue.trim ());
				break;
			}
			break;
		case TI_SUBSCRIBERCOUNT:
			{
				long	cnt = data.totalSubscribers;
				String	format = null;
				String	str;

				if (((format = mTagParameters.get ("format")) == null) &&
					((str = mTagParameters.get ("type")) != null)) {
					str = str.toLowerCase ();
					if (str.equals ("us"))
						format = "#,###,###";
					else if (str.equals ("de"))
						format = "#.###.###";
				}
				if ((str = mTagParameters.get ("round")) != null) {
					try {
						int round = Integer.parseInt (str);

						if (round > 0)
							cnt = (cnt + round - 1) / round;
					} catch (NumberFormatException e) {
						// do nothing
					}
				}
				if (format != null) {
					int len = format.length ();
					boolean first = true;
					int last = -1;
					mTagValue = "";

					for (int n = len - 1; n >= 0; --n)
						if (format.charAt (n) == '#')
							last = n;
					for (int n = len - 1; n >= 0; --n)
						if (format.charAt (n) == '#') {
							if (first || (cnt != 0)) {
								if (n == last) {
									str = Long.toString (cnt);
									cnt = 0;
								} else {
									str = Long.toString (cnt % 10);
									cnt /= 10;
								}
								mTagValue = str + mTagValue;
								first = false;
							}
						} else if ((n < last) || (cnt != 0))
							mTagValue = format.substring (n, n + 1) + mTagValue;
				} else
					mTagValue = Long.toString (cnt);
			}
			break;
		case TI_DATE:			// is prepared here in initializeInternalTag () from check_tags
			if (dateFormat != null) {
				Date	value;
				
				if (dateColumn == null) {
					value = data.currentSendDate;
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

								double	result = dateExpressions[n].evaluate ();
								if (n > 0) {
									int	offset = (int) result;
									int	index;
									
									if (n == 2) {
										--offset;
									}
									switch (n) {
									default:
									case 1:	index = Calendar.YEAR;		break;
									case 2:	index = Calendar.MONTH;		break;
									case 3:	index = Calendar.DAY_OF_MONTH;	break;
									case 4:	index = Calendar.HOUR_OF_DAY;	break;
									case 5:	index = Calendar.MINUTE;	break;
									case 6:	index = Calendar.SECOND;	break;
									}
									dateCalendar.set (index, offset);
									value = dateCalendar.getTime ();
								} else {
									if (result == Math.floor (result)) {
										mTagValue = Long.toString ((long) result);
									} else {
										mTagValue = Double.toString (result);
									}
								}
							}
						}
					}
					if ((dateExpressions == null) || (dateExpressions[0] == null)) {
						mTagValue = dateFormat.format (value);
					}
				}
			}
			break;
		case TI_SYSINFO:		// is set here in initializeInternalTag () from check_tags
			break;
		case TI_DYN:			// is handled in xml backend
		case TI_DYNVALUE:		// dito
			break;
		case TI_TITLE:
		case TI_TITLEFULL:
		case TI_TITLEFIRST:
			{
				Title	title = data.getTitle (titleType);

				if (title != null) {
					mTagValue = title.makeTitle (titleMode, cinfo.getGender (), cinfo.getTitle (), cinfo.getFirstname (), cinfo.getLastname (), cinfo.getColumns (), null);
					if ((mTagValue.length () > 0) && ((titlePrefix != null) || (titlePostfix != null))) {
						mTagValue = (titlePrefix == null ? "" : titlePrefix) + mTagValue + (titlePostfix == null ? "" : titlePostfix);
					}
				} else {
					mTagValue = "";
				}
			}
			break;
		case TI_IMGLINK:		// is set in imageLinkReference
			break;
		case TI_CALL:
			mTagValue = code.getCode ();
			break;
		}
		return mTagValue;
	}

	/** Set link reference for image link tag
	 * @param data Reference to configuration
	 * @param urlID id of referenced URL
	 */
	public void imageLinkReference(Data data, long urlID) {
		String	destination = ilURL;

		for (int n = 0; n < data.urlcount; ++n) {
			URL url = data.URLlist.get (n);

			if (url.getId () == urlID) {
				destination = url.getUrl ();
				break;
			}
		}
		mTagValue = ilPrefix + destination + ilPostfix;
		fixedValue = true;
	}

	private static Pattern	findColumn = Pattern.compile ("\\$(([a-z][a-z0-9_]*\\.)?[a-z][a-z0-9_]*)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	public void requestFields (Data data, Set <String> predef) throws Exception {
		if (tagType == TAG_INTERNAL) {
			switch (tagSpec) {
			case TI_DB:
				if (dbColumn != null) {
					predef.add (dbColumn.getQname ());
				}
				break;
			case TI_DATE:
				if (dateColumn != null) {
					predef.add (dateColumn.getQname ());
				}
				break;
			case TI_DYN:
				{
					String	type = mTagParameters.get ("type");
			
					if (type != null) {
						String[]	tdetail = type.split (":", 2);
				
						if ((tdetail.length == 2) && tdetail[0].equalsIgnoreCase ("multi")) {
							String[]	refs = tdetail[1].split (", *");
					
							for (String ref : refs) {
								if ((data.references == null) || (! data.references.containsKey (ref.toLowerCase ()))) {
									data.logging (Log.WARNING, "emmtag", "agnDYN using multi for non existing reference table " + ref);
								} else {
									Reference	r = data.references.get (ref);
								
									if (r.isMulti ()) {
										predef.add (r.name () + "." + Reference.multiID);
									}
								}
							}
						}
					}
				
					String	filter = mTagParameters.get ("filter");
				
					if (filter != null) {
						Matcher	m = findColumn.matcher (filter);
					
						while (m.find ()) {
							String	column = m.group (1).toLowerCase ();
					
							if (column.startsWith ("cust.")) {
								column = column.substring (5);
							}
							predef.add (column);
						}
					}
				}
				break;
			case TI_TITLE:
			case TI_TITLEFULL:
			case TI_TITLEFIRST:
				{
					Title	title = data.getTitle (titleType);

					if (title != null) {
						title.requestFields (predef, titleMode);
					}
				}
				break;
			case TI_CALL:
				if (code != null)
					code.requestFields (predef, mTagParameters);
				break;
			}
		}
	}

	/**
	 * Retruns the type of this tag
	 * 
	 * @return the type of the tag
	 */
	public String getType () {
		String	rc = mSelectType;
		
		if (tagType == TAG_INTERNAL)
			switch (tagSpec) {
			case TI_CALL:
				if ((func != null) && (code != null)) {
					String	lang = code.getLanguage ();
					String	name = code.getName ();
					
					rc += ":" + (lang == null ? "" : lang) + ":" + (name == null ? "" : name) + ":" + func;
				}
				break;
			}
		return rc;
	}

	/** Is this character a whitespace?
	 * @param ch the character to inspect
	 * @return true, if character is whitespace
	 */
	private boolean isspace (char ch) {
		return ((ch == ' ') || (ch == '\t') || (ch == '\n') || (ch == '\r') || (ch == '\f'));
	}

	private String clearify (String tag) {
		return StringOps.removeEntities (tag).replaceAll ("[„“‚‘“”‘’«»‹›]", "\"");
	}

	/** Split a tag into its elements
	 * @return Vector of all elements
	 */
	private List <String> splitTag (Data data) throws EMMTagException {
		int tlen;
		String	tag;

		tlen = mTagFullname.length ();
		if ((tlen > 0) && (mTagFullname.charAt (0) == '[')) {
			tag = mTagFullname.substring (1);
			--tlen;
		} else
			tag = mTagFullname;
		if ((tlen > 0) && (tag.charAt (tlen - 1) == ']'))
			if ((tlen > 1) && (tag.charAt (tlen - 2) == '/'))
				tag = tag.substring (0, tlen - 2);
			else
				tag = tag.substring (0, tlen - 1);
		tag = clearify (tag);
		tlen = tag.length ();

		List <String> rc = new ArrayList <> ();
		int		rccnt = 0;
		int		state = 0;
		char		quote = '\0';
		StringBuffer	scratch = new StringBuffer (tlen);

		for (int n = 0; n <= tlen; ) {
			char	ch;

			if (n < tlen)
				ch = tag.charAt (n);
			else {
				ch = '\0';
				state = 99;
				++n;
			}
			switch (state) {
			default:
				throw new EMMTagException (data, this, "invalid state " + state);
			case 0:
				if (! isspace (ch)) {
					scratch.setLength (0);
					state = 1;
				} else {
					++n;
				}
				break;
			case 1:
				if (isspace (ch)) {
					state = 99;
				} else {
					scratch.append (ch);
					if (ch == '=')
						state = 2;
				}
				++n;
				break;
			case 2:
				if (isspace (ch)) {
					state = 99;
				} else if (ch == '\\') {
					state = 3;
				} else {
					if ((ch == '"') || (ch == '\'')) {
						quote = ch;
						state = 10;
					} else {
						scratch.append (ch);
						state = 20;
					}
				}
				++n;
				break;
			case 3:
				if ((ch == '"') || (ch == '\'')) {
					quote = ch;
					state = 30;
				} else {
					scratch.append (ch);
					state = 20;
				}
				++n;
				break;
			case 10:
				if (ch == '\\') {
					state = 11;
				} else {
					if (ch == quote) {
						state = 99;
					} else {
						scratch.append (ch);
					}
				}
				++n;
				break;
			case 11:
				scratch.append (ch);
				state = 10;
				++n;
				break;
			case 20:
				if (isspace (ch)) {
					state = 99;
				} else if (ch == '\\') {
					state = 21;
				} else {
					scratch.append (ch);
				}
				++n;
				break;
			case 21:
				scratch.append (ch);
				state = 20;
				++n;
				break;
			case 30:
				if (ch == '\\') {
					state = 31;
				} else {
					scratch.append (ch);
				}
				++n;
				break;
			case 31:
				if (ch == quote) {
					state = 99;
				} else {
					scratch.append (ch);
				}
				++n;
				break;
			case 99:
				if (scratch.length () > 0) {
					rc.add (scratch.toString ());
					rccnt++;
				}
				state = 0;
				break;
			}
		}
		return rccnt > 0 ? rc : null;
	}

	/** Checks a select value if its just a pure
	 * (string or numeric) data for marking it
	 * as fixed value to avoid including it in
	 * the global select call
	 * @param str the string to check
	 * @return true, if its pure data, false otherwise
	 */
	private boolean isPureData (String str) {
		int slen = str.length ();

		if (slen > 0) {
			if ((slen >= 2) && (str.charAt (0) == '\'') && (str.charAt (slen - 1) == '\''))
				return true;
			if (slen > 0) {
				int n;
				char	ch;

				n = 0;
				while (n < slen) {
					ch = str.charAt (n);
					if (((n == 0) && (ch == '-')) || Character.isDigit (ch))
						++n;
					else
						break;
				}
				if (n == slen)
					return true;
			}
		}
		return false;
	}

	private String substitute (Data data, String id) {
		if (id.equals ("licence-id"))
			return Integer.toString (data.licenceID ());
		if (id.equals ("company-id"))
			return Long.toString (data.company.id ());
		if (id.equals ("mailinglist-id"))
			return Long.toString (data.mailinglist.id ());
		if (id.equals ("mailing-id"))
			return Long.toString (data.mailing.id ());
		if (id.equals ("rdir-domain"))
			return data.rdirDomain;
		return null;
	}

	/** parse specific tag type found in database
	 */
	private void interpretTagType (Data data) {
		if ((tagType == TAG_DBASE) && (mSelectType != null) && mSelectType.equals ("FUNCTION")) {
			tagType = TAG_INTERNAL;
			tagSpec = TI_CALL;
		} else if ((tagType == TAG_DBASE) && (mSelectType != null)) {
			if (mSelectType.equals ("COMPLEX"))
				isComplex = true;
		}
	}

	/** Try to interpret an unknown tag in a dynamic way,
	 * guessing what the user tries to use
	 * @return True, if we interpreted it someway
	 */
	private boolean dynamicTag (Data data) {
		boolean rc = false;

		if (mTagName.length () > 3) {
			String	column = mTagName.substring (3);

			if (data.columnByName (column) != null) {
				tagType = TAG_INTERNAL;
				tagSpec = TI_DB;
				mTagParameters.put ("column", column);
				mNoOfParameters = mTagParameters.size ();
				rc = true;
			}
		}
		return rc;
	}

	/** Determinate the type of the tag
	 * @param data Reference to configuration
	 */
	private int check_tags(Data data) {
		fixedValue = false;
		globalValue = false;

		int n;

		for (n = 0; n < TAG_INTERNALS.length; ++n)
			if (mTagName.equals (TAG_INTERNALS[n]))
				break;
		if (n < TAG_INTERNALS.length) {
			tagType = TAG_INTERNAL;
			tagSpec = n;
		} else {
			tagType = TAG_DBASE;
			tagSpec = 0;
		}
		return tagType;
	}

	/** Collect allowed parameter by this tag for
	 * more strict tag checking
	 * @param collect the vector to collect the allowed names
	 */
	private void collectAllowedParameters (List <String> collect) {
		collect.add ("anon");
		switch (tagType) {
		case TAG_DBASE:
			collect.add ("*");
			break;
		case TAG_INTERNAL:
			switch (tagSpec) {
			case TI_DBV:
			case TI_DB:
				collect.add ("column");
				collect.add ("format");
				collect.add ("encode");
				collect.add ("language");
				collect.add ("country");
				collect.add ("timezone");
				collect.add ("code");
				break;
			case TI_IMAGE:
				collect.add ("name");
				collect.add ("source");
				break;
			case TI_EMAIL:
				collect.add ("code");
				break;
			case TI_SUBSCRIBERCOUNT:
				collect.add ("");
				break;
			case TI_DATE:
				collect.add ("type");
				collect.add ("column");
				collect.add ("language");
				collect.add ("country");
				collect.add ("timezone");
				collect.add ("offset");
				collect.add ("format");
				collect.add ("expression");
				break;
			case TI_SYSINFO:
				collect.add ("default");
				break;
			case TI_DYN:
				collect.add ("name");
				collect.add ("type");
				collect.add ("filter");
				collect.add ("onerror");
				break;
			case TI_DYNVALUE:
				collect.add ("name");
				break;
			case TI_TITLE:
			case TI_TITLEFULL:
			case TI_TITLEFIRST:
				collect.add ("type");
				collect.add ("prefix");
				collect.add ("postfix");
				break;
			case TI_IMGLINK:
				collect.add ("name");
				break;
			case TI_CALL:
				collect.add ("*");
				break;
			case TI_SWYN:
				collect.add ("networks");
				collect.add ("title");
				collect.add ("link");
				collect.add ("selector");
				collect.add ("bare");
				collect.add ("size");
				break;
			case TI_SENDDATE:
				collect.add ("format");
				break;
			case TI_GRIDPH:
				collect.add ("name");
				break;
			}
			break;
		}
	}

	private Column findColumn (Data data, boolean strict, String name) throws EMMTagException {
		Column	rc = null;
		
		if (name != null) {
			name = name.trim ();

			Column	col = data.columnByName (name);

			if (col == null) {
				String	orig = name;
				Column	alias = data.columnByAlias (name);
				int	len = name.length ();
				int	n;

				for (n = 0; n < len; ++n) {
					char	ch = name.charAt (n);

					if (((n == 0) && (! Character.isLetter (ch)) && (ch != '_')) ||
					    ((n > 0) && (! Character.isLetterOrDigit (ch)) && (ch != '_')))
						break;
				}
				if (n < len) {
					name = name.substring (0, n);
					col = data.columnByName (name);
				}
				if ((col == null) && (alias != null)) {
					name = alias.getQname ();
					col = alias;
				} else {
					data.logging (Log.WARNING, "emmtag", "Unknown column referenced for " + internalTagName () + ": " + orig);
					if (strict) {
						throw new EMMTagException (data, this, "paramter \"column\" tries to referemce unknown database column " + orig);
					}
				}
			}
			rc = col;
		}
		return rc;
	}
	private String internalTagName () {
		if (tagSpec == TI_CALL) {
		    return "-function-";
		}
		return TAG_INTERNALS[tagSpec];
	}
	
	/** Initialize the tag, if its an internal one
	 * @param data Reference to configuration
	 */
	private void initializeInternalTag (Data data, boolean strict) throws EMMTagException {
		switch (tagSpec) {
		case TI_DBV:
		case TI_DB:
			mSelectString = mTagParameters.get ("column");
			dbColumn = null;
			dbFormat = new Format (mTagParameters.get ("format"), mTagParameters.get ("encode"),
					       data.getLocale (mTagParameters.get ("language"), mTagParameters.get ("country")),
					       data.getTimeZone (mTagParameters.get ("timezone")));
			if (dbFormat.error () != null) {
				data.logging (Log.WARNING, "emmtag", "Error in formating: " + dbFormat.error ());
				if (strict) {
					throw new EMMTagException (data, this, "invalid formating: " + dbFormat.error ());
				}
			}
			if (tagSpec == TI_DBV) {
				if (mSelectString != null)
					mSelectString = mSelectString.trim ().toLowerCase ();
				else {
					data.logging (Log.WARNING, "emmtag", "Missing virtual column");
					if (strict) {
						throw new EMMTagException (data, this, "missing parameter \"column\"");
					}
				}
			} else if (tagSpec == TI_DB) {
				if (mSelectString == null) {
					data.logging (Log.WARNING, "emmtag", "Missing column parameter for " + TAG_INTERNALS[TI_DB]);
					if (strict) {
						throw new EMMTagException (data, this, "missing parameter \"column\"");
					}
				} else {
					Column	col = findColumn (data, strict, mSelectString);

					if (col != null) {
						String	err = col.validate (dbFormat);

						if (err != null) {
							data.logging (Log.WARNING, "emmtag", "Invalid format: " + err);
							if (strict) {
								throw new EMMTagException (data, this, "invalid formating using parameter \"format\"");
							}
						}
						mSelectString = col.getQname ();
					}
					dbColumn = col;
				}
			}
			String	encode = mTagParameters.get ("code");
				
			if (encode != null) {
				if (encode.equals ("punycode")) {
					punycodeValue = true;
				} else {
					data.logging (Log.WARNING, "emmtag", "Invalid code for " + mTagFullname);
					if (strict) {
						throw new EMMTagException (data, this, "invalid value for paramter \"code\"");
					}
				}
			}
			break;
		case TI_IMAGE:
			{
				String	name = mTagParameters.get ("name");
				String	source = mTagParameters.get ("source");

				if (name == null) {
					data.logging (Log.WARNING, "emmtag", "Missing name");
					if (strict) {
						throw new EMMTagException (data, this, "missing parameter \"name\"");
					}
				}
				mTagValue = data.defaultImageLink (name, source, true);
				fixedValue = true;
			}
			break;
		case TI_EMAIL:
			emailCode = 0;
			{
				String	code = mTagParameters.get ("code");

				if (code != null) {
					if (code.equals ("punycode")) {
						emailCode = 1;
					} else {
						data.logging (Log.WARNING, "emmtag", "Unknown coding for email found: " + code);
						if (strict) {
							throw new EMMTagException (data, this, "invalid value for parameter \"code\"");
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
				String	temp;
				int	type;
				String	lang;
				String	country;
				String	typestr;

				if ((temp = mTagParameters.get ("type")) != null)
					type = Integer.parseInt (temp);
				else
					type = 0;
				if ((temp = mTagParameters.get ("column")) != null) {
					dateColumn = findColumn (data, strict, temp);
					if (dateColumn != null) {
						if (dateColumn.getTypeID () != Column.DATE) {
							data.logging (Log.WARNING, "emmtag", "Invalid column type for " + temp);
							if (strict) {
								throw new EMMTagException (data, this, "invalid column data type (expect date) using parameter \"column\"");
							}
						}
					}
				} else {
					dateColumn = null;
				}
				lang = mTagParameters.get ("language");
				country = mTagParameters.get ("country");
				dateOffset = 0;
				if ((temp = mTagParameters.get ("offset")) != null) {
					try {
						dateOffset = (long) (Double.parseDouble (temp) * (24 * 60 * 60));
					} catch (NumberFormatException e) {
						data.logging (Log.WARNING, "emmtag", "Invalid offset " + temp + " for date found: " + e.toString ());
						if (strict) {
							throw new EMMTagException (data, this, "invalid offset \"" + temp + "\"");
						}
					}
				}
				if ((temp = mTagParameters.get ("format")) != null) {
					typestr = temp;
				} else {
					NamedParameterJdbcTemplate	jdbc = null;
					String				query = "SELECT format FROM date_tbl WHERE type = :type";

					typestr = "d.M.yyyy";
					try {
						Map <String, Object>	row;

						jdbc = data.dbase.request (query);
						row = data.dbase.querys (jdbc, query, "type", type);
						if (row != null) {
							typestr = data.dbase.asString (row.get ("format"));
						} else {
							data.logging (Log.WARNING, "emmtag", "No format in date_tbl found for " + mTagFullname);
							if (strict) {
								throw new EMMTagException (data, this, "No format in database found for paramter \"type\" " + type);
							}
						}
					} catch (Exception e) {
						data.logging (Log.WARNING, "emmtag", "Query failed for data_tbl: " + e);
						if (strict) {
							throw new EMMTagException (data, this, e.toString ());
						}
					} finally {
						data.dbase.release (jdbc, query);
					}
				}
				if ((temp = mTagParameters.get ("expression")) != null) {
					Pattern	modifier = Pattern.compile ("^(year|month|day|hour|minute|second): *(.*)$", Pattern.MULTILINE | Pattern.DOTALL);
					
					dateExpressions = new Expression[7];
					for (String s : temp.split ("; *")) {
						Matcher	m = modifier.matcher (s);
						int	index = 0;
						
						if (m.matches ()) {
							s = m.group (2);
							switch (m.group (1)) {
							case "year":	index = 1;	break;
							case "month":	index = 2;	break;
							case "day":	index = 3;	break;
							case "hour":	index = 4;	break;
							case "minute":	index = 5;	break;
							case "second":	index = 6;	break;
							}
						}
						ExpressionBuilder	builder = new ExpressionBuilder (s);
					
						builder.variables ("year", "month", "day", "hour", "minute", "second");
						dateExpressions[index] = builder.build ();
					}
				} else {
					dateExpressions = null;
				}
				dateCalendar = null;
				
				Locale	l = data.getLocale (lang, country);

				if (l == null) {
					dateFormat = new SimpleDateFormat (typestr);
					if (dateExpressions != null) {
						dateCalendar = Calendar.getInstance ();
					}
				} else {
					dateFormat = new SimpleDateFormat (typestr, l);
					if (dateExpressions != null) {
						dateCalendar = Calendar.getInstance (l);
					}
				}
				
				TimeZone	timeZone = data.getTimeZone (mTagParameters.get ("timezone"));

				if (timeZone != null) {
					dateFormat.setTimeZone (timeZone);
				}
			} catch (Exception e) {
				data.logging (Log.WARNING, "emmtag", "Failed parsing tag " + mTagFullname + " (" + e.toString () + ")");
				if (strict) {
					throw new EMMTagException (data, this, e.toString ());
				}
			}
			if (dateColumn == null) {
				globalValue = true;
			}
			break;
		case TI_SYSINFO:
			{
				String	dflt = mTagParameters.get ("default");

				mTagValue = dflt == null ? "" : dflt;
			}
			globalValue = true;
			break;
		case TI_DYN:
		case TI_DYNVALUE:
			if (strict) {
				if (mTagParameters.get ("name") == null) {
					throw new EMMTagException (data, this, "missing parameter \"name\"");
				}
			}
			break;
		case TI_TITLE:
		case TI_TITLEFULL:
		case TI_TITLEFIRST:
			{
				String	temp;

				if ((temp = mTagParameters.get ("type")) != null) {
					try {
						titleType = Long.parseLong (temp);
					} catch (java.lang.NumberFormatException e) {
						data.logging (Log.WARNING, "emmtag", "Invalid type string type=\"" + temp + "\", using default 0");
						titleType = 1;
						if (strict) {
							throw new EMMTagException (data, this, "invalid value for parameter \"type\"");
						}
					}
				} else {
					titleType = 1;
				}
				titlePrefix = mTagParameters.get ("prefix");
				titlePostfix = mTagParameters.get ("postfix");
				if (tagSpec == TI_TITLE) {
					titleMode = Title.TITLE_DEFAULT;
				} else if (tagSpec == TI_TITLEFULL) {
					titleMode = Title.TITLE_FULL;
				} else if (tagSpec == TI_TITLEFIRST) {
					titleMode = Title.TITLE_FIRST;
				}
				if (strict) {
					Title			title = null;
					HashSet <String>	cols = new HashSet<>();
					
					try {
						title = data.getTitle (titleType);
					} catch (SQLException e) {
						throw new EMMTagException (data, this, "failed to query for title type=\"" + temp + "\": " + e.toString ());
					}
					if (title == null) {
						throw new EMMTagException (data, this, "no title for title type=\"" + temp + "\" found");
					}
					title.requestFields (cols, titleMode);
					for (String col : cols) {
						if (data.columnByName (col) == null) {
							throw new EMMTagException (data, this, "column \"" + col + "\" required by title type=\"" + temp + "\" not found in customer table");
						}
					}
				}
			}
			break;
		case TI_IMGLINK:
			{
				String	name = mTagParameters.get ("name");
				String	source = Imagepool.MAILING;

				ilURL = null;
				ilPrefix = null;
				ilPostfix = null;
				if (name != null) {
					ilURL = data.defaultImageLink (name, source, true);
					ilPrefix = "<a href=\"";
					ilPostfix = "\"><img src=\"" + ilURL + "\" border=\"0\"></a>";
				} else {
					data.logging (Log.WARNING, "emmtag", "Missing name");
					if (strict) {
						throw new EMMTagException (data, this, "missing parameter \"name\"");
					}
				}
			}
			break;
		case TI_CALL:
			int	n;
			String	cname;
			
			cname = mSelectString;
			if ((n = cname.indexOf (':')) != -1) {
				func = cname.substring (n + 1);
				cname = cname.substring (0, n);
			} else if (mTagName.length () > 3)
				func = mTagName.substring (3).toLowerCase ();
			else
				func = cname;
			if ((cname == null) || ((code = data.findCode (cname)) == null)) {
				data.logging (Log.WARNING, "emmtag", "No code \"" + cname + "\" for function '" + func + "' found");
				if (strict) {
					throw new EMMTagException (data, this, "function \"" + func + "\" not found");
				}
			}
			mTagValue = "";
			globalValue = true;
			break;
		case TI_SWYN:
			SWYN		swyn = data.getSWYN ();
			List <String>	networks = null;
			String		pNetwork = mTagParameters.get ("networks");
			String		pTitle = mTagParameters.get ("title");
			String		pLink = mTagParameters.get ("link");
			String		pSelector = mTagParameters.get ("selector");
			String		pBare = mTagParameters.get ("bare");
			String		pSize = mTagParameters.get ("size");
			
			if (pNetwork != null) {
				String	nwlist[] = pNetwork.split (",");
				
				for (n = 0; n < nwlist.length; ++n) {
					String	nw = nwlist[n].trim ();
					
					if (nw.length () > 0) {
						if (networks == null)
							networks = new ArrayList <> ();
						networks.add (nw);
					}
				}
			}
			mTagValue = swyn.build (StringOps.atob (pBare, false), (pSize == null ? "default" : pSize), networks, pTitle, pLink, pSelector);
			fixedValue = true;
			break;
		case TI_SENDDATE:
			String	format = mTagParameters.get ("format");
			if (format == null) {
				if (data.dbase.isOracle ()) {
					format = "YYYYMMDD";
				} else {
					format = "%Y%m%d";
				}
			}
			mTagValue = data.maildropStatus.genericSendDate (format);
			fixedValue = true;
			break;
		case TI_GRIDPH:
			if (strict) {
				if (mTagParameters.get ("name") == null) {
					throw new EMMTagException (data, this, "Missing parameter \"name\"");
				}
			}
			break;
		}
	}
}
