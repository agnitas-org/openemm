/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.forms;

import com.agnitas.web.forms.FormDate;
import com.agnitas.web.forms.PaginationForm;

// TODO: EMMGUI-714. check usages and remove if redundant after move of old design
public class UserActivityLogForm extends PaginationForm {

    private FormDate dateFrom = new FormDate();
    private FormDate dateTo = new FormDate();
    private String description;
    private int userAction;
    private String username;

    public FormDate getDateFrom() {
        return dateFrom;
    }

    public FormDate getDateTo() {
        return dateTo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getUserAction() {
        return userAction;
    }

    public void setUserAction(int userAction) {
        this.userAction = userAction;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public void setDateFrom(FormDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public void setDateTo(FormDate dateTo) {
        this.dateTo = dateTo;
    }
}
