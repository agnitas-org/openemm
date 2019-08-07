/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import org.agnitas.emm.core.velocity.VelocityCheck;

public interface ComContentSource {
    /**
     * Setter for property id.
     *
     * @param id New value of property id.
     */
    public void setId(int id);

    /**
     * Setter for property companyID.
     *
     * @param id New value of property companyID.
     */
    public void setCompanyID(@VelocityCheck int id);

    /**
     * Setter for property description.
     *
     * @param desc New value of property description.
     */
    public void setDescription(String desc);

    /**
     * Setter for property shortname.
     *
     * @param shortname New value of property shortname.
     */
    public void setShortname(String shortname);

    /**
     * Setter for property url.
     *
     * @param url New value of property url.
     */
    public void setUrl(String url);

    /**
     * Getter for property id.
     *
     * @return Value of property id.
     */
    public int getId();

    /**
     * Getter for property companyID.
     *
     * @return Value of property companyID.
     */
    public int getCompanyID();

    /**
     * Getter for property description.
     *
     * @return Value of property description.
     */
    public String getDescription();

    /**
     * Getter for property shortname.
     *
     * @return Value of property shortname.
     */
    public String getShortname();

    /**
     * Getter for property url.
     *
     * @return Value of property url.
     */
    public String getUrl();
}
