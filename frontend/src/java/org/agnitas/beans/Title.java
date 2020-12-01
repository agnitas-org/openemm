/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.util.Map;

import org.agnitas.emm.core.velocity.VelocityCheck;

public interface Title {
    
    /**
     * Constants
     */
    public static final int GENDER_MALE = 0;

    public static final int GENDER_FEMALE = 1;

    public static final int GENDER_UNKNOWN = 2;

    public static final int GENDER_MISS = 3;

    public static final int GENDER_PRACTICE = 4;

    public static final int GENDER_COMPANY = 5;
 
    /**
     * Setter for property companyID.
     * 
     * @param company New value of property companyID.
     */
    public void setCompanyID( @VelocityCheck int company);
    
    /**
     * Setter for property id.
     * 
     * @param title New value of property id.
     */
    public void setId(int title);

    /**
     * Setter for property description.
     * 
     * @param desc New value of property description.
     */
    public void setDescription(String desc);
    
    /**
     * Setter for property titleGender.
     * 
     * @param titleGender New value of property titleGender.
     */
    public void setTitleGender(Map<Integer, String> titleGender);

   /**
     * Getter for property companyID.
     * 
     * @return Value of property companyID.
     */
    public int getCompanyID();
    
    /**
     * Getter for property id.
     * 
     * @return Value of property id.
     */
    public int getId();

    /**
     * Getter for property description.
     * 
     * @return Value of property description.
     */
    public String getDescription();

    /**
     * Getter for property titleGender.
     * 
     * @return Value of property titleGender.
     */
    public Map<Integer, String> getTitleGender();
    
}
