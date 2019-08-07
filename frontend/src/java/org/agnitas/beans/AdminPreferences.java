/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.io.Serializable;

public interface AdminPreferences extends Serializable {
    int MAILING_CONTENT_HTML_EDITOR = 1;
    int MAILING_CONTENT_HTML_CODE = 0;

    /**
     * Getter for property adminID.
     *
     * @return Value of property id of this Admin.
     */
    int getAdminID();

    /**
     * Setter for property adminID.
     *
     * @param adminID the new value for the adminID.
     */
    void setAdminID(int adminID);

    /**
     * Getter for the preferred mailing content view type
     */
    int getMailingContentView();

    /**
     * Setter for the preferred mailing content view type
     *
     * @param mailingContentView the new value of mailingContentViewType
     */
    void setMailingContentView(int mailingContentView);
}
