/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Date;

import com.agnitas.beans.LightProfileField;
import com.agnitas.util.DbColumnType.SimpleDataType;

/**
 * @deprecated Use RecipientFieldDescription instead
 */
@Deprecated
public interface ProfileField extends LightProfileField {
	int getCompanyID();

	void setCompanyID( int company);

	int getAdminID();

	void setAdminID(int adminID);

	String getDescription();

	void setDescription(String desc);

	String getDataType();

	void setDataType(String dataType);

	long getDataTypeLength();

	void setDataTypeLength(long dataTypeLength);

	String getDefaultValue();

	void setDefaultValue(String value);
	
	boolean getNullable();
	
	void setNullable(boolean nullable);

	/**
	 * ModeEdit
	 * Usage mode of this field in the GUI when creating or changing a customer
	 */
	ProfileFieldMode getModeEdit();

	void setModeEdit(ProfileFieldMode edit);
	
	SimpleDataType getSimpleDataType();

	String getLabel();
	
	void setLabel(String label);

    int getSort();

    void setSort(int sort);

    int getLine();

    void setLine(int line);

    boolean isInterest();

    int getInterest();

    void setInterest(int interest);

    void setInterest(boolean interest);

    Date getCreationDate();

    void setCreationDate(Date creationDate);

    Date getChangeDate();

    void setChangeDate(Date changeDate);

    boolean getHistorize();

    void setHistorize(boolean historize);

    String[] getAllowedValues();

    void setAllowedValues(String[] allowedValues);

    boolean isHiddenField();

    void setHiddenField(boolean hiddenField);
    
    void setNumericPrecision(int numericPrecision);
    
    int getNumericPrecision();
    
    void setNumericScale(int numericScale);
    
    int getNumericScale();

    long getMaxDataSize();

	void setMaxDataSize(long maxDataSize);

	SimpleDataType getOverrideSimpleDataType();

	void setOverrideSimpleDataType(SimpleDataType overrideSimpleDataType);
}
