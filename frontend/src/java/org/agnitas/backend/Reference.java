/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to hold the information for one reference table
 * definition
 */
public class Reference {
	/** a refernce table must have this unique column to be used for 1:n data access */
	public static String	multiID = "id";
	static private Pattern	parse = Pattern.compile ("^([a-z][a-z0-9_]*):([a-z][a-z0-9_]*);(.+)->([a-z][a-z0-9_]*)(<([a-z][a-z0-9_]*)>)?(\\{(.*)\\})?(/([^/]+)/)?$", Pattern.CASE_INSENSITIVE);
	static private Pattern	validNamePattern = Pattern.compile ("^[a-z][0-9a-z_]*$", Pattern.CASE_INSENSITIVE);
	static private Pattern	reservedNamePattern = Pattern.compile ("^(agn|sys)_.*$", Pattern.CASE_INSENSITIVE);
	/** the name of this reference table definition */
	private String		name;
	private String		table;
	private String		referenceExpression;
	private String		keyColumn;
	private String		backReference;
	private String		joinCondition;
	private String		orderBy;
	private boolean		isVoucher;
	private boolean		fullfilled;
	private boolean		multi;
	private int		idIndex;

	public Reference () {
		name (null);
		table = null;
		referenceExpression = null;
		keyColumn = null;
		backReference (null);
		joinCondition = null;
		orderBy = null;
		isVoucher = false;
		fullfilled = false;
		multi = false;
		idIndex = -1;
	}

	/**
	 * Constructor
	 * 
	 * @param nName                 the name of the reference table definition
	 * @param nTable                the name of the reference table in the database
	 * @param nReferenceExpression  the expression to reference the related table
	 * @param nKeyColumn            the key column of this reference table to be reference the related record
	 * @param nBackReference        for chained reference table, this is the name of the related one, if null, then the customer table is related
	 * @param nJoinCondition        the optional expression for the join on clause
	 * @param nOrderBy              for 1:n reference table, the optional expression for the ORDER BY statement (by default, this is the mulitID column)
	 * @param nIsVoucher            flag, if this reference table contains vouchers
	 */
	public Reference (String nName, String nTable,
			  String nReferenceExpression, String nKeyColumn,
			  String nBackReference, String nJoinCondition,
			  String nOrderBy, boolean nIsVoucher) {
		this ();
		name (nName);
		table = nTable;
		referenceExpression = nReferenceExpression;
		keyColumn = nKeyColumn;
		backReference (nBackReference);
		joinCondition = nJoinCondition;
		orderBy = nOrderBy;
		isVoucher = nIsVoucher;
	}

	/**
	 * Constructor which can parse a single textual line with
	 * a reference table definition in the form:
	 * name:table;cust-expr->key-column<backref>{join-condition}/order-by/
	 * e.g.:
	 * namesday:nameday_tbl;lower (cust.firstname)->firstname
	 */
	public Reference (String s) {
		this ();
		Matcher	m = parse.matcher (s);
		
		if (m.matches ()) {
			name (m.group (1));
			table = m.group (2);
			referenceExpression = m.group (3);
			keyColumn = m.group (4);
			backReference (m.group (6));
			joinCondition = m.group (8);
			orderBy = m.group (10);
		}
	}
	
	public String name () {
		return name;
	}
	public void name (String nName) {
		name = nName != null ?  nName.toLowerCase () : null;
	}
	public String table () {
		return table;
	}
	public String backReference () {
		return backReference;
	}
	public void backReference (String nBackReference) {
		backReference = nBackReference != null ? nBackReference.toLowerCase () : null;
	}

	/**
	 * Checks the name against some rules to check if
	 * it is considered valid. It may not clash with
	 * internaly used names.
	 */
	public boolean validName (String n) {
		return (n != null) &&
			(! n.equalsIgnoreCase ("cust")) &&
			(! n.equalsIgnoreCase ("bind")) &&
			(! reservedNamePattern.matcher (n).matches ()) &&
			validNamePattern.matcher (n).matches ();
	}

	/**
	 * Checks if at least the minium informations
	 * are available to use this definition
	 * 
	 * @return true, if definition is valid, false otherwise
	 */
	public boolean valid () {
		return (table != null) && (referenceExpression != null) && (keyColumn != null) && validName (name);
	}
	
	public boolean isFullfilled () { 
		return fullfilled;
	}

	public void fullfill () {
		fullfilled = true;
	}
	
	public boolean isMulti () {
		return multi;
	}
	
	public void multi () {
		multi = true;
	}

	/**
	 * Returns the join condition on clause for an sql statement.
	 * If this is not part of the definition, a default is created
	 * on the fly
	 * 
	 * @return the join condition on clause
	 */
	public String joinConditionClause () {
		if (joinCondition != null) {
			return joinCondition;
		}
		return referenceExpression + " = " + name + "." + keyColumn;
	}
	
	/**
	 * returns the join condition for the sql statement
	 * 
	 * @return the join condition
	 */
	public String joinConditionFrom () {
		return "INNER JOIN " + table () + " " + name () + " ON (" + joinConditionClause () + ")";
	}
	
	/**
	 * returns an exprssion to be used by an SQL ORDER BY
	 * statement
	 * 
	 * @return a string containing the order by expression
	 */
	public String orderBy () {
		return name + "." + (orderBy != null ? orderBy : multiID);
	}
	
	public boolean isVoucher () {
		return isVoucher;
	}
	
	/**
	 * Set the index for the column which is used for 1:n as a unique key
	 * 
	 * @param name  the name of the column
	 * @param index the index in the result set
	 */
	public void setIdIndex (String name, int index) {
		if (name.equals (multiID)) {
			idIndex = index;
		}
	}
	public int getIdIndex () {
		return idIndex;
	}
}
