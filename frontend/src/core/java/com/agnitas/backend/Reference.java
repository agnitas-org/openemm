package com.agnitas.backend;

import java.util.Map;

public class Reference {
	public static String multiID = "-";

	public Reference(Data nData) {
		/* empty by intention */
	}

	public Reference(Data nData, String nName, String nTable, String nReferenceExpression, String nKeyColumn, String nBackReference, String nJoinCondition, String nOrderBy,
			boolean nIsVoucher, boolean nVoucherRenew) {
		/* empty by intention */
	}

	public Reference(Data nData, String s) {
		/* empty by intention */
	}

	public String voucherName () {
		return name ();
	}
	
	public String name() {
		return null;
	}

	public void name(String nName) {
		/* empty by intention */
	}

	public String table() {
		return null;
	}

	public String backReference() {
		return null;
	}

	public void backReference(String nBackReference) {
		/* empty by intention */
	}

	public boolean validName(String n) {
		return false;
	}

	public boolean valid() {
		return false;
	}

	public boolean isFullfilled() {
		return false;
	}

	public void fullfill() {
		/* empty by intention */
	}

	public boolean isMulti() {
		return false;
	}

	public void multi() {
		/* empty by intention */
	}

	public String joinConditionClause() {
		return null;
	}

	public String joinConditionFrom() {
		return null;
	}

	public String orderBy() {
		return null;
	}

	public boolean isVoucher() {
		return false;
	}

	public boolean voucherRenew() {
		return false;
	}

	public void setIdIndex(String name, int index) {
		/* empty by intention */
	}

	public int getIdIndex() {
		return -1;
	}

	public Map<String, Column> get(String selectColumn, String selectValue) {
		return null;
	}
}
