/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.util.Date;

import org.agnitas.beans.impl.LightProfileFieldImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.beans.ProfileField;

public class ProfileFieldImpl extends LightProfileFieldImpl implements ProfileField {
	protected int companyID = -1;
	protected int adminID = 0;
	protected String description = "";
	protected String dataType;
	protected int dataTypeLength;
	private int maxDataSize;
	protected String defaultValue = "";
	protected boolean nullable = true;
	protected int modeEdit = 0;
	protected int modeInsert = 0;
	protected String label;
	
	protected int line = 0;
	protected int sort = 1000;
	protected boolean interest = false;
	
	protected Date creationDate;
	protected Date changeDate;
	
	private boolean historize;
	private String[] allowedValues;
	private boolean isHiddenField;
	
	private int numericPrecision;
	private int numericScale;
	
	@Override
	public int getCompanyID() {
		return companyID;
	}
	
	@Override
	public void setCompanyID( @VelocityCheck int company) {
		this.companyID = company;
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
	public boolean getNullable() {
		return nullable;
	}
	
	@Override
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}
	
	@Override
	public SimpleDataType getSimpleDataType() {
		return new DbColumnType(dataType, 0, 0, 0, false).getSimpleDataType();
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}
	
	@Override
	public int getLine() {
		return line;
	}

	@Override
	public void setLine(int line) {
		this.line = line;
	}

	@Override
	public int getSort() {
		return sort;
	}

	@Override
	public void setSort(int sort) {
		this.sort = sort;
	}

	@Override
	public boolean isInterest() {
		return interest;
	}

	@Override
	public void setInterest(boolean interest) {
		this.interest = interest;
	}

	@Override
	public int getInterest() {
		return interest ? 1:0;
	}

	@Override
	public void setInterest(int interest) {
		this.interest = ( interest == 0 ? false :true );
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getChangeDate() {
		return changeDate;
	}

	@Override
	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}

	@Override
	public boolean getHistorize() {
		return historize;
	}

	@Override
	public void setHistorize(boolean historize) {
		this.historize = historize;
	}

	@Override
	public String[] getAllowedValues() {
		return allowedValues;
	}

	@Override
	public void setAllowedValues(String[] allowedValues) {
		this.allowedValues = allowedValues;
	}

	@Override
	public boolean isHiddenField() {
		return isHiddenField;
	}

	@Override
	public void setHiddenField(boolean hiddenField) {
		isHiddenField = hiddenField;
	}

	public boolean getIsHiddenField() {
		return isHiddenField;
	}
	
	@Override
	public int getNumericPrecision() {
		return numericPrecision;
	}
	
	@Override
	public void setNumericPrecision(int numericPrecision) {
		this.numericPrecision = numericPrecision;
	}
	
	@Override
	public int getNumericScale() {
		return numericScale;
	}
	
	@Override
	public void setNumericScale(int numericScale) {
		this.numericScale = numericScale;
	}

	@Override
	public String getDataType() {
		return DbColumnType.dbType2String(dataType, numericScale);
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	@Override
	public int getDataTypeLength() {
		if (DbColumnType.GENERIC_TYPE_VARCHAR.equals(getDataType())) {
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
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ProfileFieldImpl other = (ProfileFieldImpl) obj;
		if (companyID != other.companyID) {
			return false;
		}
		if (column == null) {
			if (other.column != null) {
				return false;
			}
		} else if (!column.equals(other.column)) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + companyID;
		result = prime * result + ((column == null) ? 0 : column.hashCode());
		return result;
	}

	@Override
	public final int getMaxDataSize() {
		return maxDataSize;
	}

	@Override
	public final void setMaxDataSize(final int maxDataSize) {
		this.maxDataSize = maxDataSize;
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
}
