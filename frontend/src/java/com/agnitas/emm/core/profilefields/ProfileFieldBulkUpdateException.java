/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields;

public class ProfileFieldBulkUpdateException extends ProfileFieldException {
	private static final long serialVersionUID = 1180906126880768002L;
	
	private final String profileFieldName;
	private final int companyID;
	private final Object invalidValue;
    
    public ProfileFieldBulkUpdateException(String profileFieldName, Object invalidValue, int companyID, Throwable cause) {
        super(cause);
		
		this.profileFieldName = profileFieldName;
		this.invalidValue = invalidValue;
		this.companyID = companyID;
    }
    
    public String getProfileFieldName() {
        return profileFieldName;
    }
    
    public int getCompanyID() {
        return companyID;
    }
    
    public Object getInvalidValue() {
        return invalidValue;
    }
}
