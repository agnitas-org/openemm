/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import org.apache.commons.lang3.StringUtils;

public class DbColumnType {

	public static final String GENERIC_TYPE_INTEGER = "INTEGER";
	public static final String GENERIC_TYPE_FLOAT = "FLOAT";
	public static final String GENERIC_TYPE_DOUBLE = "DOUBLE";
	public static final String GENERIC_TYPE_VARCHAR = "VARCHAR";
	public static final String GENERIC_TYPE_VARCHAR2 = "VARCHAR2";
	public static final String GENERIC_TYPE_DATE = "DATE";
	public static final String GENERIC_TYPE_DATETIME = "DATETIME";
	public static final String GENERIC_TYPE_BLOB = "BLOB";
	public static final String GENERIC_TYPE_TIMESTAMP = "TIMESTAMP";

	private final String typeName;
	private final long characterLength; // only for VARCHAR and VARCHAR2 types
	private final int numericPrecision; // only for numeric types
	private final int numericScale; // only for numeric types
	private final boolean nullable;
	
	public enum SimpleDataType {

		Characters("settings.fieldType.VARCHAR", GENERIC_TYPE_VARCHAR),
		Numeric("settings.fieldType.INTEGER", GENERIC_TYPE_INTEGER),
		Float("settings.fieldType.DOUBLE", GENERIC_TYPE_DOUBLE),
		Date("settings.fieldType.DATE", GENERIC_TYPE_DATE),
		DateTime("settings.fieldType.DATETIME", GENERIC_TYPE_DATETIME),
		Blob("settings.fieldType.Blob", GENERIC_TYPE_BLOB);

		private final String messageKey;
		private final String genericDbDataTypeName;

		SimpleDataType(String messageKey, String genericDbDataTypeName) {
			this.messageKey = messageKey;
			this.genericDbDataTypeName = genericDbDataTypeName;
		}
		
		public String getMessageKey() {
			return messageKey;
		}
		
		public String getGenericDbDataTypeName() {
			return genericDbDataTypeName;
		}
		
		public static SimpleDataType getFromString(String dataTypeString) {
			if ("integer".equalsIgnoreCase(dataTypeString)) {
				return SimpleDataType.Numeric;
			}
			for(SimpleDataType simpleDataType : SimpleDataType.values()) {
				if (simpleDataType.name().equalsIgnoreCase(dataTypeString)) {
					return simpleDataType;
				}
			}
			throw new IllegalArgumentException("Invalid SimpleDataType: " + dataTypeString);
		}
	}
	
	public DbColumnType(String typeName, long characterLength, int numericPrecision, int numericScale, boolean nullable) {
		this.typeName = typeName;
		this.characterLength = characterLength;
		this.numericPrecision = numericPrecision;
		this.numericScale = numericScale;
		this.nullable = nullable;
	}
	
	public String getTypeName() {
		return typeName;
	}
	
	public long getCharacterLength() {
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
		return getSimpleDataType(typeName, numericScale);
	}
	
	public static SimpleDataType getSimpleDataType(String typeName, int scale) {
		if (typeName.toUpperCase().startsWith("VARCHAR") || typeName.toUpperCase().startsWith("CHAR") || typeName.equalsIgnoreCase("CLOB") || typeName.toUpperCase().contains("TEXT")) {
			return SimpleDataType.Characters;
		} else if (typeName.toUpperCase().contains("TIME")) {
			return SimpleDataType.DateTime;
		} else if (typeName.toUpperCase().contains("DATE")) {
			return SimpleDataType.Date;
		} else if (typeName.equalsIgnoreCase("blob") || typeName.equalsIgnoreCase("tinyblob") || typeName.equalsIgnoreCase("mediumblob") || typeName.equalsIgnoreCase("longblob") || typeName.equalsIgnoreCase("bytea")) {
			return SimpleDataType.Blob;
		} else if (typeName.equalsIgnoreCase("FLOAT") || typeName.equalsIgnoreCase("DOUBLE") || (typeName.equalsIgnoreCase("NUMBER") && scale != 0)) {
			return SimpleDataType.Float;
		} else {
			return SimpleDataType.Numeric;
		}
	}
	
	/**
	 * Return a simple dbFieldType which can be interpreted by a bean:message-Tag
	 */
	public static String dbType2String(String typeName, int scale) {
		if (StringUtils.isBlank(typeName)) {
			return null;
		} else if (typeName.equalsIgnoreCase("BIGINT")
				|| typeName.equalsIgnoreCase("INT")
				|| typeName.equalsIgnoreCase("INTEGER")
				|| typeName.equalsIgnoreCase("SMALLINT")
				|| (typeName.equalsIgnoreCase("NUMBER") && scale == 0)) {
			return GENERIC_TYPE_INTEGER;
		} else if (typeName.equalsIgnoreCase("DECIMAL")
				|| (typeName.equalsIgnoreCase("NUMBER") && scale != 0)
				|| typeName.equalsIgnoreCase("DOUBLE")
				|| typeName.equalsIgnoreCase("FLOAT")
				|| typeName.equalsIgnoreCase("NUMERIC")
				|| typeName.equalsIgnoreCase("REAL")) {
			return GENERIC_TYPE_FLOAT;
		} else if (typeName.equalsIgnoreCase("CHAR")
				|| typeName.equalsIgnoreCase("VARCHAR")
				|| typeName.equalsIgnoreCase("VARCHAR2")
				|| typeName.equalsIgnoreCase("LONGVARCHAR")
				|| typeName.equalsIgnoreCase("CLOB")
				|| typeName.equalsIgnoreCase("TINYTEXT")
				|| typeName.equalsIgnoreCase("TEXT")
				|| typeName.equalsIgnoreCase("MEDIUMTEXT")
				|| typeName.equalsIgnoreCase("LONGTEXT")) {
			return GENERIC_TYPE_VARCHAR;
		} else if (typeName.equalsIgnoreCase("DATE")) {
			return GENERIC_TYPE_DATE;
		} else if (typeName.equalsIgnoreCase("DATETIME")
				|| typeName.equalsIgnoreCase("TIMESTAMP")
				|| typeName.equalsIgnoreCase("TIME")) {
			return GENERIC_TYPE_DATETIME;
		} else if (typeName.equalsIgnoreCase("BLOB")
				|| typeName.equalsIgnoreCase("TINYBLOB")
				|| typeName.equalsIgnoreCase("MEDIUMBLOB")
				|| typeName.equalsIgnoreCase("LONGBLOB")) {
			return GENERIC_TYPE_BLOB;
		} else {
			return "UNKNOWN(" + typeName + ")";
		}
	}
	
	/**
	 * Read a bean:message-Tag text to a db data type
	 */
	public static String string2DbType(String beanTypeString) {
		if (StringUtils.isBlank(beanTypeString)) {
			throw new IllegalArgumentException("Invalid dbtype: " + beanTypeString);
		}
		
		beanTypeString = beanTypeString.toUpperCase();
			
		switch (beanTypeString) {
			case GENERIC_TYPE_INTEGER:
				return "NUMBER";
			case GENERIC_TYPE_FLOAT:
				return "DOUBLE";
			case GENERIC_TYPE_VARCHAR:
				return "VARCHAR";
			case GENERIC_TYPE_DATE:
				return "DATE";
			case GENERIC_TYPE_DATETIME:
				return "TIMESTAMP";
			default:
				throw new IllegalArgumentException("Invalid dbtype:" + beanTypeString);
		}
	}
}
