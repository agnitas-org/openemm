/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;


import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;

public interface EmmAction extends Serializable {
    int TYPE_LINK = 0;
    int TYPE_FORM = 1;
    int TYPE_ALL = 9;

    /**
     * Getter for property actionID.
     *
     * @return Value of property actionID.
     */
    int getId();

    /**
     * Getter for property actionOperations.
     * @return the actionOperations
     */
    List<AbstractActionOperationParameters> getActionOperations();

    /**
     * Getter for property companyID.
     *
     * @return Value of property companyID.
     */
    int getCompanyID();

    /**
     * Getter for property description.
     *
     * @return Value of property description.
     */
    String getDescription();

    /**
     * Getter for property shortname.
     *
     * @return Value of property shortname.
     */
    String getShortname();

    /**
     * Getter for property type.
     *
     * @return Value of property type.
     */
    int getType();

    /**
     * Setter for property actionID.
     *
     * @param actionID New value of property actionID.
     */
    void setId(int actionID);

    /**
     * Setter for property actionOperations.
     *
     * @param actionOperations the actionOperations to set
     */
    void setActionOperations(List<AbstractActionOperationParameters> actionOperations);

    /**
     * Setter for property companyID.
     *
     * @param companyID New value of property companyID.
     */
    void setCompanyID( int companyID);

    /**
     * Setter for property description.
     *
     * @param description New value of property description.
     */
    void setDescription(String description);

    /**
     * Setter for property shortname.
     *
     * @param shortname New value of property shortname.
     */
    void setShortname(String shortname);

    /**
     * Setter for property type.
     *
     * @param type New value of property type.
     */
    void setType(int type);

    /**
     * This will return 0 , if the action will be loaded by hibernate.
     * @return number of forms, which use this action
     */
    int getUsed();

    String getFormNames();

    List<String> getFormNameList();

    void setFormNameList(List<String> formNameList);

    Timestamp getChangeDate();

    void setChangeDate(Timestamp changeDate);

    Timestamp getCreationDate();

    void setCreationDate(Timestamp creationDate);

    boolean getIsActive();

    void setIsActive(boolean active);

    void setAdvertising(boolean advertising);

    boolean isAdvertising();

    boolean isDeleted();

    void setDeleted(boolean deleted);
}

