/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Date;

import org.agnitas.beans.LightProfileField;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DbColumnType.SimpleDataType;

public interface ProfileField extends LightProfileField {
	int MODE_EDIT_EDITABLE = 0;
	int MODE_EDIT_READONLY = 1;
	int MODE_EDIT_NOT_VISIBLE = 2;
	
	int getCompanyID();

	void setCompanyID( @VelocityCheck int company);

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
	 * 
	 * Allowed values:
	 * 	MODE_EDIT_NOT_VISIBLE:
	 * 		This field is not intended to be shown in the GUI
	 * 	MODE_EDIT_READONLY:
	 * 		This field is shown but can not be edited by the user
	 * 	MODE_EDIT_EDITABLE and all others:
	 * 		Show this field in GUI for edit
	 */
	int getModeEdit();

	void setModeEdit(int edit);

	int getModeInsert();

	void setModeInsert(int insert);
	
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
