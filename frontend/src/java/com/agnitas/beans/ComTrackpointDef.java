/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

public interface ComTrackpointDef {

    int TYPE_SIMPLE = 0;
    int TYPE_NUM = 1;
    int TYPE_ALPHA = 2;

    /**
     * Getter for property id.
     *
     * @return Value of property id.
     */
    int getId();

    /**
     * Getter for property shortname.
     *
     * @return Value of property shortname.
     */
    String getShortname();

    /**
     * Getter for property description.
     *
     * @return Value of property description.
     */
    String getDescription();

    /**
     * Getter for property pagetag.
     *
     * @return Value of property pagetag.
     */
    String getPagetag();

    /**
     * Getter for property type.
     *
     * @return Value of property type.
     */
    int getType();

    /**
     * Getter for property currency.
     *
     * @return Value of property curreny.
     */
    String getCurrency();

    /**
     * Getter for property format.
     *
     * @return Value of property format.
     */
    int getFormat();

    int getMailingID();

    int getActionID();

    int getCompanyID();

    void setId(int id);

    void setShortname(String name);

    void setDescription(String desc);

    void setPagetag(String pagetag);

    void setType(int type);

    void setCurrency(String currency);

    void setFormat(int format);

    void setMailingID(int mailingID);

    void setActionID(int actionID);

    void setCompanyID(int companyID);
}
