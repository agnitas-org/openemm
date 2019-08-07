/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import org.apache.commons.lang.StringUtils;

public class DbColumnType {
	public static final String GENERIC_TYPE_INTEGER = "INTEGER";
	public static final String GENERIC_TYPE_DOUBLE = "DOUBLE";
	public static final String GENERIC_TYPE_CHAR = "CHAR";
	public static final String GENERIC_TYPE_VARCHAR = "VARCHAR";
	public static final String GENERIC_TYPE_VARCHAR2 = "VARCHAR2";
	public static final String GENERIC_TYPE_DATE = "DATE";

	private String typeName;
	private int characterLength; // only for VARCHAR and VARCHAR2 types
	private int numericPrecision; // only for numeric types
	private int numericScale; // only for numeric types
	private boolean nullable;
	
	public enum SimpleDataType {
		Characters("settings.fieldType.Characters"),
		Numeric("settings.fieldType.Numeric"),
		Date("settings.fieldType.DATE"),
		Blob("settings.fieldType.Blob");

		private String messageKey;
		
		private SimpleDataType(String messageKey) {
			this.messageKey = messageKey;
		}
		
		public String getMessageKey() {
			return messageKey;
		}
		
		public static SimpleDataType getFromString(String dataTypeString) throws Exception {
			for(SimpleDataType simpleDataType : SimpleDataType.values()) {
				if (simpleDataType.name().equalsIgnoreCase(dataTypeString)) {
					return simpleDataType;
				}
			}
			throw new Exception("Invalid SimpleDataType: " + dataTypeString);
		}
	}
	
	public DbColumnType(String typeName, int characterLength, int numericPrecision, int numericScale, boolean nullable) {
		this.typeName = typeName;
		this.characterLength = characterLength;
		this.numericPrecision = numericPrecision;
		this.numericScale = numericScale;
		this.nullable = nullable;
	}
	
	public String getTypeName() {
		return typeName;
	}
	
	public int getCharacterLength() {
		return characterLength;
	}
	
	public int getNumericPrecision() {
		return numericPrecision;
	}
	
	public int getNumericScale() {
		return numericScale;
	}
	
	public boolean isNullable() {
		return nullable;
	}
	
	public SimpleDataType getSimpleDataType() {
		return getSimpleDataType(typeName);
	}
	
	public static SimpleDataType getSimpleDataType(String typeName) {
		if (typeName.toUpperCase().startsWith("VARCHAR") || typeName.toUpperCase().startsWith("CHAR") || typeName.equalsIgnoreCase("CLOB") || typeName.toUpperCase().contains("TEXT")) {
			return SimpleDataType.Characters;
		} else if (typeName.toUpperCase().contains("DATE") || typeName.toUpperCase().contains("TIME")) {
			return SimpleDataType.Date;
		} else if (typeName.toLowerCase().equals("blob") || typeName.toLowerCase().equals("bytea")) {
			return SimpleDataType.Blob;
		} else {
			return SimpleDataType.Numeric;
		}
	}
	
	/**
	 * Return a simple dbFieldType which can be interpreted by a bean:message-Tag
	 * @param typeName
	 * @return
	 */
	public static String dbType2String(String typeName) {
		if (StringUtils.isBlank(typeName)) {
			return null;
		} else if (typeName.equalsIgnoreCase("BIGINT")
				|| typeName.equalsIgnoreCase("INT")
				|| typeName.equalsIgnoreCase("INTEGER")
				|| typeName.equalsIgnoreCase("NUMBER")
				|| typeName.equalsIgnoreCase("SMALLINT")) {
			return GENERIC_TYPE_INTEGER;
		} else if (typeName.equalsIgnoreCase("DECIMAL")
				|| typeName.equalsIgnoreCase("DOUBLE")
				|| typeName.equalsIgnoreCase("FLOAT")
				|| typeName.equalsIgnoreCase("NUMERIC")
				|| typeName.equalsIgnoreCase("REAL")) {
			return GENERIC_TYPE_DOUBLE;
		} else if (typeName.equalsIgnoreCase("CHAR")) {
			return GENERIC_TYPE_CHAR;
		} else if (typeName.equalsIgnoreCase("VARCHAR")
				|| typeName.equalsIgnoreCase("VARCHAR2")
				|| typeName.equalsIgnoreCase("LONGVARCHAR")
				|| typeName.equalsIgnoreCase("CLOB")) {
			return GENERIC_TYPE_VARCHAR;
		} else if (typeName.equalsIgnoreCase("DATE")
				|| typeName.equalsIgnoreCase("TIMESTAMP")
				|| typeName.equalsIgnoreCase("TIME")) {
			return GENERIC_TYPE_DATE;
		} else {
			return "UNKNOWN(" + typeName + ")";
		}
	}
	
	/**
	 * Read a bean:message-Tag text to a db data type
	 * @param typeName
	 * @return
	 */
	public static String string2DbType(String beanTypeString) throws Exception {
		if (StringUtils.isBlank(beanTypeString)) {
			throw new Exception("Invalid dbtype");
		}
		
		beanTypeString = beanTypeString.toUpperCase();
			
		switch (beanTypeString) {
			case "INTEGER":
				return "NUMBER";
			case "DOUBLE":
				return "DOUBLE";
			case "CHAR":
				return "CHAR";
			case "VARCHAR":
				return "VARCHAR";
			case "STRING":
				return "VARCHAR";
			case "DATE":
				return "TIMESTAMP";
			default:
				throw new Exception("Invalid dbtype");
		}
	}
}
