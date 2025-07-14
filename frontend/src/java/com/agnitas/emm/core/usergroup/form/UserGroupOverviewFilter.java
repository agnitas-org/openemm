/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.usergroup.form;

import com.agnitas.web.forms.PaginationForm;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class UserGroupOverviewFilter extends PaginationForm {

    private int currentAdminCompanyId;
    private int currentAdminId;
    private Integer clientId;
    private String description;
    private String clientName;
    private String groupName;
    private boolean showDeleted;

    public int getCurrentAdminId() {
        return currentAdminId;
    }

    public void setCurrentAdminId(int currentAdminId) {
        this.currentAdminId = currentAdminId;
    }

    public int getCurrentAdminCompanyId() {
        return currentAdminCompanyId;
    }

    public void setCurrentAdminCompanyId(int currentAdminCompanyId) {
        this.currentAdminCompanyId = currentAdminCompanyId;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isUiFiltersSet() {
        return isNotBlank(description) || isNotBlank(clientName) || isNotBlank(groupName) || clientId != null;
    }

    public boolean isShowDeleted() {
        return showDeleted;
    }

    public void setShowDeleted(boolean showDeleted) {
        this.showDeleted = showDeleted;
    }
}
