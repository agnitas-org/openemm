/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import java.io.Serializable;

import org.agnitas.beans.ProfileField;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.apache.commons.lang3.StringUtils;

public class ProfileFieldImpl implements ProfileField, Serializable {
	private static final long serialVersionUID = -6125451198749198856L;

	protected int companyID = -1;
	protected String column;
	protected int adminID = 0;
	protected String shortname = "";
	protected String description = "";
	protected String dataType;
	protected int dataTypeLength;
	protected String defaultValue = "";
	protected boolean nullable = true;
	protected int modeEdit = 0;
	protected int modeInsert = 0;
	protected String label;

	@Override
	public int getCompanyID() {
		return companyID;
	}
	
	@Override
	public void setCompanyID( @VelocityCheck int company) {
		this.companyID = company;
	}

	@Override
	public String getColumn() {
		return column;
	}
	
	@Override
	public void setColumn(String column) {
		if (column != null) {
			this.column = column.toLowerCase();
		} else {
			this.column = null;
		}
		
		// Fallback for special cases in which the shortname is not set
		if (StringUtils.isEmpty(shortname)) {
			if (column != null) {
				shortname = column.toUpperCase();
			} else {
				shortname = null;
			}
		}
	}

	@Override
	public int getAdminID() {
		return adminID;
	}
	
	@Override
	public void setAdminID(int adminID) {
		this.adminID = adminID;
	}

	@Override
	public String getShortname() {
		return shortname;
	}
	
	@Override
	public void setShortname(String shortname) {
		if (shortname == null) {
			this.shortname = "";
		} else {
			this.shortname = shortname;
		}
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public void setDescription(String desc) {
		if (desc == null) {
			desc = "";
		}
		this.description = desc;
	}

	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dbType2String(dataType);
	}

	@Override
	public int getDataTypeLength() {
		if ("VARCHAR".equals(dataType) || "CHAR".equals(dataType)) {
			return dataTypeLength;
		} else {
			return 0;
		}
	}

	@Override
	public void setDataTypeLength(int dataTypeLength) {
		this.dataTypeLength = dataTypeLength;
	}
	
	@Override
	public String getDefaultValue() {
		return defaultValue;
	}
	
	@Override
	public void setDefaultValue(String value) {
		if (value == null) {
			value = "";
		}
		this.defaultValue = value;
	}

	@Override
	public int getModeEdit() {
		return modeEdit;
	}
	
	@Override
	public void setModeEdit(int modeEdit) {
		this.modeEdit = modeEdit;
	}

	@Override
	public int getModeInsert() {
		return modeInsert;
	}
	
	@Override
	public void setModeInsert(int modeInsert) {
		this.modeInsert = modeInsert;
	}

	@Override
	public boolean equals(Object o) {
		if (!getClass().isInstance(o)) {
			return false;
		}

		ProfileField f = (ProfileField) o;

		if (f.getCompanyID() != companyID)
			return false;

		if (!f.getColumn().equalsIgnoreCase(column))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		Integer i = new Integer(companyID);

		return i.hashCode() * column.hashCode();
	}
	
	@Override
	public boolean getNullable() {
		return nullable;
	}
	
	@Override
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}
	
	/**
	 * String representation for easier debugging
	 */
	@Override
	public String toString() {
		int length = getDataTypeLength() ;
		return "(" + companyID + ") " + column + " shortname:" + shortname + " " 
				+ dataType + (length > 0 ? "(" + length + ")" : "") 
				+ (StringUtils.isNotEmpty(defaultValue) ? " default:" + defaultValue : "")
				+ " nullable:" + nullable;
	}
	
	@Override
	public SimpleDataType getSimpleDataType() {
		return new DbColumnType(dataType, 0, 0, 0, false).getSimpleDataType();
	}
	
	protected static String dbType2String(int type) {
		switch (type) {
			case java.sql.Types.BIGINT:
			case java.sql.Types.INTEGER:
			case java.sql.Types.SMALLINT:
				return "INTEGER";
	
			case java.sql.Types.DECIMAL:
			case java.sql.Types.DOUBLE:
			case java.sql.Types.FLOAT:
			case java.sql.Types.NUMERIC:
			case java.sql.Types.REAL:
				return "DOUBLE";
	
			case java.sql.Types.CHAR:
				return "CHAR";
	
			case java.sql.Types.VARCHAR:
			case java.sql.Types.LONGVARCHAR:
			case java.sql.Types.CLOB:
				return "VARCHAR";
	
			case java.sql.Types.DATE:
			case java.sql.Types.TIMESTAMP:
			case java.sql.Types.TIME:
				return "DATE";
				
			default:
				return "UNKNOWN(" + type + ")";
		}
	}
	
	protected static String dbType2String(String typeName) {
		if (StringUtils.isBlank(typeName)) {
			return null;
		} else if (typeName.equalsIgnoreCase("BIGINT")
				|| typeName.equalsIgnoreCase("INT")
				|| typeName.equalsIgnoreCase("INTEGER")
				|| typeName.equalsIgnoreCase("SMALLINT")) {
			return DbColumnType.GENERIC_TYPE_INTEGER;
		} else if (typeName.equalsIgnoreCase("DECIMAL")
				|| typeName.equalsIgnoreCase("NUMBER")
				|| typeName.equalsIgnoreCase("DOUBLE")
				|| typeName.equalsIgnoreCase("FLOAT")
				|| typeName.equalsIgnoreCase("NUMERIC")
				|| typeName.equalsIgnoreCase("REAL")) {
			return DbColumnType.GENERIC_TYPE_DOUBLE;
		} else if (typeName.equalsIgnoreCase("CHAR")) {
			return DbColumnType.GENERIC_TYPE_CHAR;
		} else if (typeName.equalsIgnoreCase("VARCHAR")
				|| typeName.equalsIgnoreCase("VARCHAR2")
				|| typeName.equalsIgnoreCase("LONGVARCHAR")
				|| typeName.equalsIgnoreCase("CLOB")) {
			return DbColumnType.GENERIC_TYPE_VARCHAR;
		} else if (typeName.equalsIgnoreCase("DATE")
				|| typeName.equalsIgnoreCase("TIMESTAMP")
				|| typeName.equalsIgnoreCase("TIME")) {
			return DbColumnType.GENERIC_TYPE_DATE;
		} else {
			return "UNKNOWN(" + typeName + ")";
		}
	}

	@Override
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
