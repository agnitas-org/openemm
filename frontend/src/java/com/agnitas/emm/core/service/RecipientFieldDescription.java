/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agnitas.util.DbColumnType.SimpleDataType;

import com.agnitas.beans.ProfileFieldMode;

public class RecipientFieldDescription {
	private String columnName;
	private String shortName;
	private String description;
	private SimpleDataType simpleDataType;
	private String databaseDataType;
	private long characterLength; // only for alphanumeric types
	private int numericPrecision; // only for numeric types
	private int numericScale; // only for numeric types
	private boolean nullable;
	
	/** List of admin IDs and their permission, admin ID 0 is the fallback for all admins not mentioned in the list */
	private Map<Integer, ProfileFieldMode> permissions = new HashMap<>();
	
	private boolean historized;
	
	private Date creationDate;
	private Date changeDate;
	
	private int sortOrder = 1000;
	private int line;
	private int interest;
	private String defaultValue;
	private List<String> allowedValues;
	
	public RecipientFieldDescription() {
	}
	
	public RecipientFieldDescription(String columnName, String shortName, String description, SimpleDataType simpleDataType, String databaseDataType, long characterLength, int numericPrecision, int numericScale, boolean nullable, Map<Integer, ProfileFieldMode> permissions, boolean historized) {
		this.columnName = columnName;
		this.shortName = shortName;
		this.description = description;
		this.simpleDataType = simpleDataType;
		this.databaseDataType = databaseDataType;
		this.characterLength = characterLength;
		this.numericPrecision = numericPrecision;
		this.numericScale = numericScale;
		this.nullable = nullable;
		this.permissions = permissions;
		this.historized = historized;
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	public RecipientFieldDescription setColumnName(String columnName) {
		if (columnName == null) {
			throw new RuntimeException("Invalid empty column name for recipient field");
		}
		this.columnName = columnName.toLowerCase();
		return this;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public RecipientFieldDescription setShortName(String shortName) {
		this.shortName = shortName;
		return this;
	}
	
	public String getDescription() {
		return description;
	}
	
	public RecipientFieldDescription setDescription(String description) {
		this.description = description;
		return this;
	}
	
	public SimpleDataType getSimpleDataType() {
		return simpleDataType;
	}
	
	public RecipientFieldDescription setSimpleDataType(SimpleDataType simpleDataType) {
		this.simpleDataType = simpleDataType;
		return this;
	}
	
	public String getDatabaseDataType() {
		return databaseDataType;
	}
	
	public RecipientFieldDescription setDatabaseDataType(String databaseDataType) {
		if (databaseDataType == null) {
			throw new RuntimeException("Invalid empty databaseDataType for recipient field");
		}
		this.databaseDataType = databaseDataType.toUpperCase();
		return this;
	}
	
	public long getCharacterLength() {
		return characterLength;
	}
	public RecipientFieldDescription setCharacterLength(long characterLength) {
		this.characterLength = characterLength;
		return this;
	}
	
	public int getNumericPrecision() {
		return numericPrecision;
	}
	
	public RecipientFieldDescription setNumericPrecision(int numericPrecision) {
		this.numericPrecision = numericPrecision;
		return this;
	}
	
	public int getNumericScale() {
		return numericScale;
	}
	
	public RecipientFieldDescription setNumericScale(int numericScale) {
		this.numericScale = numericScale;
		return this;
	}
	
	public boolean isNullable() {
		return nullable;
	}
	
	public RecipientFieldDescription setNullable(boolean nullable) {
		this.nullable = nullable;
		return this;
	}
	
	public ProfileFieldMode getDefaultPermission() {
		if (permissions != null && permissions.containsKey(0)) {
			return permissions.get(0);
		} else {
			return ProfileFieldMode.Editable;
		}
	}
	
	public ProfileFieldMode getAdminPermission(int adminID) {
		if (permissions == null || permissions.size() == 0) {
			return ProfileFieldMode.Editable;
		} else if (permissions.containsKey(adminID)) {
			return permissions.get(adminID);
		} else if (permissions.containsKey(0)) {
			return permissions.get(0);
		} else {
			return ProfileFieldMode.Editable;
		}
	}

	public RecipientFieldDescription setDefaultPermission(ProfileFieldMode permission) {
		permissions.put(0, permission);
		return this;
	}

	public Map<Integer, ProfileFieldMode> getPermissions() {
		return permissions;
	}

	public RecipientFieldDescription setPermissions(Map<Integer, ProfileFieldMode> permissions) {
		this.permissions = permissions;
		return this;
	}

	public boolean isHistorized() {
		return historized;
	}

	public RecipientFieldDescription setHistorized(boolean historized) {
		this.historized = historized;
		return this;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public RecipientFieldDescription setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
		return this;
	}

	public Date getChangeDate() {
		return changeDate;
	}

	public RecipientFieldDescription setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
		return this;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public RecipientFieldDescription setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
		return this;
	}

	public int getLine() {
		return line;
	}

	public RecipientFieldDescription setLine(int line) {
		this.line = line;
		return this;
	}

	public int getInterest() {
		return interest;
	}

	public RecipientFieldDescription setInterest(int interest) {
		this.interest = interest;
		return this;
	}

	public List<String> getAllowedValues() {
		return allowedValues;
	}

	public RecipientFieldDescription setAllowedValues(List<String> allowedValues) {
		this.allowedValues = allowedValues;
		return this;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public RecipientFieldDescription setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}
	
	public boolean isStandardField() {
		return RecipientStandardField.getAllRecipientStandardFieldColumnNames().contains(columnName);
	}
}
