/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.forms;

import org.agnitas.web.forms.FormSearchParams;

public class RecipientsFormSearchParams implements FormSearchParams<RecipientListForm> {
    private int mailinglistId;
    private int targetId;
    private int userStatus;
    private String userType;
    private String firstName;
    private String lastName;
    private String email;
    private String queryBuilderRules;

    @Override
    public void storeParams(RecipientListForm form) {
        this.mailinglistId = form.getFilterMailinglistId();
        this.targetId = form.getFilterTargetId();
        this.userStatus = form.getFilterUserStatus();
        this.userType = form.getFilterUserType();
        this.firstName = form.getSearchFirstName();
        this.lastName = form.getSearchLastName();
        this.email = form.getSearchEmail();
        this.queryBuilderRules = form.getSearchQueryBuilderRules();
    }

    @Override
    public void restoreParams(RecipientListForm form) {
        form.setFilterMailinglistId(mailinglistId);
        form.setFilterTargetId(targetId);
        form.setFilterUserStatus(userStatus);
        form.setFilterUserType(userType);
        form.setSearchFirstName(firstName);
        form.setSearchLastName(lastName);
        form.setSearchEmail(email);
        form.setSearchQueryBuilderRules(queryBuilderRules);
    }

    @Override
    public void resetParams() {
        mailinglistId = 0;
        targetId = 0;
        userStatus = 0;
        userType = "";
        firstName = "";
        lastName = "";
        email = "";
        queryBuilderRules = "[]";
    }

    public void updateTargetId(int newTargetId) {
        this.targetId = newTargetId;
    }

    public int getMailinglistId() {
        return mailinglistId;
    }

    public int getUserStatus() {
        return userStatus;
    }

    public String getUserType() {
        return userType;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }
}
