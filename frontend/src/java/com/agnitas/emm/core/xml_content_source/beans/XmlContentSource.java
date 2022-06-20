/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.xml_content_source.beans;

import org.agnitas.emm.core.velocity.VelocityCheck;

public interface XmlContentSource {
    /**
     * Setter for property id.
     *
     * @param id New value of property id.
     */
    void setId(int id);

    /**
     * Setter for property companyID.
     *
     * @param id New value of property companyID.
     */
    void setCompanyID(@VelocityCheck int id);

    /**
     * Setter for property description.
     *
     * @param desc New value of property description.
     */
    void setDescription(String desc);

    /**
     * Setter for property shortname.
     *
     * @param shortname New value of property shortname.
     */
    void setShortname(String shortname);

    /**
     * Setter for property url.
     *
     * @param url New value of property url.
     */
    void setUrl(String url);

    /**
     * Getter for property id.
     *
     * @return Value of property id.
     */
    int getId();

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
     * Getter for property url.
     *
     * @return Value of property url.
     */
    String getUrl();
}
