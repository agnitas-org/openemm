/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.bean;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class RestfulUserActivityAction {

    private Date timestamp;
    private String endpoint;
    private String description;
    private String requestMethod;
    private String username;
    private String supervisorName;

    public RestfulUserActivityAction(String username, String supervisorName, String endpoint, String description, String requestMethod, Date timestamp) {
        this.timestamp = timestamp;
        this.endpoint = endpoint;
        this.description = description;
        this.requestMethod = requestMethod;
        this.username = username;
        this.supervisorName = supervisorName;
    }

    public RestfulUserActivityAction() {
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    public String getDisplayName() {
        if (StringUtils.isBlank(supervisorName)) {
            return getUsername();
        }

        return getUsername() + " (" + supervisorName + ")";
    }
}
