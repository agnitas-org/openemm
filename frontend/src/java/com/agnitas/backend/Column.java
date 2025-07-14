/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

import com.agnitas.backend.Format;
import net.objecthunter.exp4j.Expression;

/**
 * Representation of a single column
 */
public class Column {
	static final public int UNSET = -1;
	static final public int NUMERIC = 0;
	static final public int STRING = 1;
	static final public int DATE = 2;

	/**
	 * Name of this column
	 */
	private String name;
	/**
	 * An optional alias name for this column
	 */
	private String alias;
	/**
	 * Reference to table to access
	 */
	private String ref;
	/**
	 * Qualified name
	 */
	private String qname;
	/**
	 * Data type of this column
	 */
	private int type;
	/**
	 * True if DB has NULL value
	 */
	private boolean isnull;
	/**
	 * True if column is in use
	 */
	private boolean inuse;
	/**
	 * raw object
	 */
	private Object value;
	/**
	 * Its numeric version
	 */
	private long ival;
	/**
	 * its float version
	 */
	private double fval;
	/**
	 * Its string version
	 */
	private String sval;
	/**
	 * Its date version
	 */
	private Date dval;
	/**
	 * an overwritten value
	 */
	private boolean isOverwritten;
	private String overwritten;

	/**
	 * Returns the type of the given type as simple
	 * string representation, either "i" for intergers,
	 * "s" for strings and "d" for date types
	 *
	 * @param cType the column type
	 * @return the simple type string represenation
	 */
	static public int getTypeID(int cType) {
		switch (cType) {
			case Types.DECIMAL:
			case Types.NUMERIC:
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.REAL:
			case Types.BIGINT:
			case Types.INTEGER:
			case Types.SMALLINT:
			case Types.TINYINT:
				return NUMERIC;
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.BLOB:
			case Types.CLOB:
				return STRING;
			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP:
				return DATE;
			default:
				return UNSET;
		}
	}

	/**
	 * Returns a mapping from data type to a one character
	 * representation. These informations are used in the
	 * xmlback process to determinate the data type for
	 * each column value
	 *
	 * @param cType the columns data type
	 * @return a one character string for known data types, null otherwise
	 */
	static public String typeStr(int cType) {
		switch (getTypeID(cType)) {
			case NUMERIC:
				return "n";
			case STRING:
				return "s";
			case DATE:
				return "d";
			default:
				return null;
		}
	}

	/**
	 * Constructor
	 */
	public Column() {
		name = null;
		alias = null;
		ref = null;
		qname = null;
		type = -1;
		isnull = false;
		inuse = true;
		ival = -1;
		fval = -1.0;
		sval = null;
		dval = null;
		isOverwritten = false;
		overwritten = null;
	}

	/**
	 * Constructor setting name and type
	 *
	 * @param cName name of column
	 * @param cType type of column
	 */
	public Column(String cName, int cType) {
		this();
		setName(cName);
		qname = name;
		type = cType;
	}

	public void setName(String nName) {
		name = nName != null ? nName.toLowerCase() : null;
		makeQname();
	}

	public String getName() {
		return name;
	}

	public void setAlias(String nAlias) {
		alias = nAlias != null ? nAlias.toLowerCase() : null;
	}

	public String getAlias() {
		return alias;
	}

	public void setRef(String nRef) {
		ref = nRef != null ? nRef.toLowerCase() : null;
		makeQname();
	}

	public String getRef() {
		return ref;
	}

	public String getQname() {
		return qname;
	}

	public void isnull(boolean nIsnull) {
		isnull = nIsnull;
	}

	public boolean isnull() {
		return isOverwritten ? overwritten == null : isnull;
	}

	public void setInuse(boolean nInuse) {
		inuse = nInuse;
	}

	public boolean getInuse() {
		return inuse;
	}

	public Object getValue() {
		return value;
	}

	public int getTypeID() {
		return getTypeID(type);
	}

	public String typeStr() {
		return typeStr(type);
	}

	public void setOverwrite(String overwrite) {
		isOverwritten = true;
		overwritten = overwrite;
	}

	public void clearOverwrite() {
		isOverwritten = false;
	}

	/**
	 * check if reference and column name matches this column
	 *
	 * @param matchRef  name of reference table to check (use null for standard profile table)
	 * @param matchName name of column to match
	 * @return true, if matches, false otherwise
	 */
	public boolean match (String matchRef, String matchName) {
		return name.equalsIgnoreCase (matchName) &&
		       (((ref == null) && (matchRef == null)) ||
			((ref != null) && (matchRef != null) && ref.equalsIgnoreCase (matchRef)));
	}

	/**
	 * Clear the value (set to an empty or zero value)
	 */
	public void clr() {
		value = null;
		isnull (true);
		switch (type) {
			default:
				break;
			case Types.DECIMAL:
			case Types.NUMERIC:
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.REAL:
				ival = 0;
				fval = 0.0;
				break;
			case Types.BIGINT:
			case Types.INTEGER:
			case Types.SMALLINT:
			case Types.TINYINT:
				ival = 0;
				break;
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.BLOB:
			case Types.CLOB:
				sval = null;
				break;
			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP:
				dval = null;
				break;
		}
	}

	/**
	 * Set value from a result set. If the value
	 * could not be parsed to the desired data
	 * type an internal default value is used:
	 * -1.0 for real
	 * -1 for for int
	 * null for all others
	 *
	 * @param rset  the result set to use
	 * @param index the index into the result set
	 */
	public void set(ResultSet rset, int index) {
		value = null;
		switch (type) {
			default:
				break;
			case Types.DECIMAL:
			case Types.NUMERIC:
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.REAL:
				try {
					fval = rset.getDouble(index);
					value = fval;
				} catch (SQLException e) {
					fval = -1.0;
				}
				ival = (long) fval;
				break;
			case Types.BIGINT:
			case Types.INTEGER:
			case Types.SMALLINT:
			case Types.TINYINT:
				try {
					ival = rset.getLong(index);
					value = ival;
				} catch (SQLException e) {
					ival = -1;
				}
				break;
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.BLOB:
			case Types.CLOB:
				try {
					if ((type == Types.CHAR) || (type == Types.VARCHAR)) {
						sval = rset.getString(index);
					} else if (type == Types.BLOB) {
						Blob blob = rset.getBlob(index);

						try {
							sval = blob == null ? null : new String(blob.getBytes(1, (int) blob.length()), "UTF-8");
						} catch (java.io.UnsupportedEncodingException e) {
							sval = null;
						} finally {
							if (blob != null) {
								blob.free ();
							}
						}
					} else if (type == Types.CLOB) {
						Clob clob = rset.getClob(index);

						try {
							sval = clob == null ? null : clob.getSubString(1, (int) clob.length());
						} finally {
							if (clob != null) {
								clob.free ();
							}
						}
					}
					value = sval;
				} catch (SQLException e) {
					sval = null;
				}
				break;
			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP:
				try {
					if (type == Types.TIME) {
						dval = rset.getTime(index);
					} else if (type == Types.TIMESTAMP) {
						dval = rset.getTimestamp(index);
					} else {
						dval = rset.getDate(index);
					}
					value = dval;
				} catch (SQLException e) {
					dval = null;
				}
				break;
		}
		try {
			isnull (rset.wasNull());
			if (isnull () && (value != null)) {
				value = null;
			}
		} catch (SQLException e) {
			isnull (false);
		}
	}

	/**
	 * Get a column value as string
	 *
	 * @return string version of column content
	 */
	public String get() {
		return get(null);
	}

	public String get(Expression expression) {
		String str;

		if (isnull ()) {
			return "";
		} else if (isOverwritten) {
			return overwritten == null ? "" : overwritten;
		}
		switch (type) {
			case Types.DECIMAL:
			case Types.NUMERIC:
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.REAL:
				if (ival != fval) {
					return Double.toString(calc(expression, fval));
				}
				return Long.toString(calc(expression, ival));
			case Types.BIGINT:
			case Types.INTEGER:
			case Types.SMALLINT:
			case Types.TINYINT:
				return Long.toString(calc(expression, ival));
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.BLOB:
			case Types.CLOB:
				return sval != null ? calc(expression, sval) : "";
			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP:
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("en"));

				if (dval != null) {
					str = fmt.format(calc(expression, dval));
				} else {
					str = "0000-00-00 00:00:00";
				}
				return str;

			default:
				return null;
		}
	}

	/**
	 * Get a column value as string using the passed
	 * Fromat instance for formatting the output
	 *
	 * @param format the instance to format the output
	 * @return string version of column content
	 */
	public String get(Format format, Expression expression) {
		String rc = null;

		if (isnull ()) {
			rc = "";
		} else if (isOverwritten) {
			rc = format.format(overwritten == null ? "" : overwritten);
		} else {
			switch (type) {
				case Types.DECIMAL:
				case Types.NUMERIC:
				case Types.DOUBLE:
				case Types.FLOAT:
				case Types.REAL:
					rc = format.format(calc(expression, fval));
					break;
				case Types.BIGINT:
				case Types.INTEGER:
				case Types.SMALLINT:
				case Types.TINYINT:
					rc = format.format(calc(expression, ival));
					break;
				case Types.CHAR:
				case Types.VARCHAR:
				case Types.BLOB:
				case Types.CLOB:
					rc = format.format(sval != null ? calc(expression, sval) : "");
					break;
				case Types.DATE:
				case Types.TIME:
				case Types.TIMESTAMP:
					rc = format.format(calc(expression, dval));
					break;
				default:
					break;
			}
		}
		return format.encode(rc != null ? rc : get(expression));
	}

	/**
	 * Validates the value, if it is suitable for the given format
	 *
	 * @param format the instance to format the output
	 * @return null, if format is usable, otherwise an error text with the reason of the failure
	 */
	public String validate(Format format) {
		get(format, null);
		return format.error();
	}

	private void makeQname() {
		if (name != null) {
			if (ref == null) {
				qname = name;
			} else {
				qname = ref + "." + name;
			}
		} else {
			qname = null;
		}
	}

	private double calc(Expression expression, double valueParameter) {
		try {
			return expression == null || isnull () ? valueParameter : expression.setVariable("value", valueParameter).setVariable(name, valueParameter).setVariable(qname, valueParameter).evaluate();
		} catch (Exception e) {
			return valueParameter;
		}
	}

	private long calc(Expression expression, long valueParameter) {
		return (long) calc(expression, (double) valueParameter);
	}

	private String calc(Expression expression, String valueParameter) {
		if ((expression != null) && (valueParameter != null) && (!isnull ())) {
			String tempvalue = valueParameter.trim();

			try {
				valueParameter = Long.valueOf(calc(expression, Long.parseLong(tempvalue))).toString();
			} catch (NumberFormatException e1) {
				try {
					valueParameter = Double.valueOf(calc(expression, Double.parseDouble(tempvalue))).toString();
				} catch (NumberFormatException e2) {
					// do nothing
				}
			}
		}
		return valueParameter;
	}

	private Date calc(Expression expression, Date valueParameter) {
		if ((expression != null) && (valueParameter != null) && (!isnull ())) {
			Instant i = valueParameter.toInstant();
			double day = 24 * 60 * 60;
			double epoch = i.getEpochSecond() / day;
			double result = calc(expression, epoch);

			if (result != epoch) {
				i = Instant.ofEpochSecond((long) (result * day));
				valueParameter = Date.from(i);
			}
		}
		return valueParameter;
	}
}
