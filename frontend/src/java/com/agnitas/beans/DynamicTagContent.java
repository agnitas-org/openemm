/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;


public interface DynamicTagContent extends Cloneable {
/**
     * Getter for property dynContent.
     *
     * @return Value of property dynContent.
     */
    String getDynContent();

    /**
     * Getter for property id.
     *
     * @return Value of property id.
     */
    int getId();

    /**
     * Getter for property dynName.
     *
     * @return Value of property dynName.
     */
    String getDynName();

    /**
     * Getter for property dynNameID.
     *
     * @return Value of property dynNameID.
     */
    int getDynNameID();

    /**
     * Getter for property dynOrder.
     *
     * @return Value of property dynOrder.
     */
    int getDynOrder();

    /**
     * Getter for property targetID.
     *
     * @return Value of property targetID.
     */
    int getTargetID();

    /**
     * Setter for property companyID.
     * 
     * @param id New value of property companyID.
     */
    void setCompanyID( int id);

    /**
     * Setter for property dynContent.
     * 
     * @param content New value of property dynContent.
     */
    void setDynContent(String content);

    /**
     * Setter for property id.
     * 
     * @param id New value of property id.
     */
    void setId(int id);

    /**
     * Setter for property dynName.
     * 
     * @param name New value of property dynName.
     */
    void setDynName(String name);

    /**
     * Setter for property dynNameID.
     * 
     * @param id New value of property dynNameID.
     */
    void setDynNameID(int id);

    /**
     * Setter for property dynOrder.
     * 
     * @param id New value of property dynOrder.
     */
    void setDynOrder(int id);

    /**
     * Setter for property mailingID.
     * 
     * @param id New value of property mailingID.
     */
    void setMailingID(int id);

    /**
     * Setter for property targetID.
     * 
     * @param id New value of property targetID.
     */
    void setTargetID(int id);

    /**
     * Getter for property mailingID.
     *
     * @return Value of property mailingID.
     */
    int getMailingID();

    /**
     * Getter for property companyID.
     *
     * @return Value of property companyID.
     */
    int getCompanyID();


    DynamicTagContent clone();
}
