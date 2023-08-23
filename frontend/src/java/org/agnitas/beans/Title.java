/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.util.Map;


public interface Title {
    
    /**
     * Constants
     */
    int GENDER_MALE = 0;

    int GENDER_FEMALE = 1;

    int GENDER_UNKNOWN = 2;

    int GENDER_MISS = 3;

    int GENDER_PRACTICE = 4;

    int GENDER_COMPANY = 5;
 
    /**
     * Setter for property companyID.
     * 
     * @param company New value of property companyID.
     */
    void setCompanyID(int company);
    
    /**
     * Setter for property id.
     * 
     * @param title New value of property id.
     */
    void setId(int title);

    /**
     * Setter for property description.
     * 
     * @param desc New value of property description.
     */
    void setDescription(String desc);
    
    /**
     * Setter for property titleGender.
     * 
     * @param titleGender New value of property titleGender.
     */
    void setTitleGender(Map<Integer, String> titleGender);

   /**
     * Getter for property companyID.
     * 
     * @return Value of property companyID.
     */
   int getCompanyID();
    
    /**
     * Getter for property id.
     * 
     * @return Value of property id.
     */
    int getId();

    /**
     * Getter for property description.
     * 
     * @return Value of property description.
     */
    String getDescription();

    /**
     * Getter for property titleGender.
     * 
     * @return Value of property titleGender.
     */
    Map<Integer, String> getTitleGender();
    
}
