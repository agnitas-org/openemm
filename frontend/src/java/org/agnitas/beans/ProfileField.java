/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DbColumnType.SimpleDataType;

public interface ProfileField {
	int MODE_EDIT_EDITABLE = 0;
	int MODE_EDIT_READONLY = 1;
	int MODE_EDIT_NOT_VISIBLE = 2;
	
	int getCompanyID();

	void setCompanyID( @VelocityCheck int company);

	String getColumn();

	void setColumn(String column);

	int getAdminID();

	void setAdminID(int adminID);

	String getShortname();

	void setShortname(String desc);

	String getDescription();

	void setDescription(String desc);

	String getDataType();

	void setDataType(String dataType);

	int getDataTypeLength();

	void setDataTypeLength(int dataTypeLength);

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
	 * @return
	 */
	int getModeEdit();

	void setModeEdit(int edit);

	int getModeInsert();

	void setModeInsert(int insert);
	
	SimpleDataType getSimpleDataType();

	String getLabel();
}
