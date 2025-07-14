/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import com.agnitas.beans.MediaTypeStatus;
import org.springframework.context.ApplicationContext;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public interface Mediatype {

    static boolean isActive(Mediatype mediaType) {
        return mediaType != null && MediaTypeStatus.Active.getCode() == mediaType.getStatus();
    }

    /**
     * Getter for property param.
     *
     * @return Value of property param.
     */
    String getParam();

    /**
     * Setter for property param.
     *
     * @param param New value of property param.
     */
    void setParam(String param);

    /**
     * Getter for property priority.
     *
     * @return Value of property priority.
     */
    int getPriority();

    /**
     * Setter for property priority.
     *
     * @param priority New value of property priority.
     */
    void setPriority(int priority);

    /**
     * Getter for property status.
     *
     * @return Value of property status.
     */
    int getStatus();

    /**
     * Setter for property status.
     *
     * @param status New value of property status.
     */
    void setStatus(int status);

    /** Getter for property companyID.
     * @return Value of property companyID.
     *
     */
    int getCompanyID();

    /** Setter for property companyID.
     * @param companyID New value of property companyID.
     *
     */
    void setCompanyID(int companyID);

    String getTemplate();

    void setTemplate(String template);

    void syncTemplate(Mailing mailing, ApplicationContext con);
    
    /**
     * Makes a standalone copy of this mediatype without any references to this objects data
     */
    Mediatype copy();
    
    MediaTypes getMediaType();
}
