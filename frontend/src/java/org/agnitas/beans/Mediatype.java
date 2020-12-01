/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.springframework.context.ApplicationContext;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public interface Mediatype {
    int STATUS_NOT_USED = 0;
    int STATUS_INACTIVE = 1;
    int STATUS_ACTIVE = 2;

    static boolean isActive(Mediatype mediaType) {
        return mediaType != null && STATUS_ACTIVE == mediaType.getStatus();
    }

    /**
     * Getter for property param.
     *
     * @return Value of property param.
     */
    String getParam() throws Exception;

    /**
     * Setter for property param.
     *
     * @param param New value of property param.
     */
    void setParam(String param) throws Exception;

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
    void setCompanyID( @VelocityCheck int companyID);

    String getTemplate();

    void setTemplate(String template);

    void syncTemplate(Mailing mailing, ApplicationContext con) throws Exception;
    
    /**
     * Makes a standalone copy of this mediatype without any references to this objects data 
     * 
     * @return
     * @throws Exception 
     */
    Mediatype copy() throws Exception;
    
    MediaTypes getMediaType();
}
