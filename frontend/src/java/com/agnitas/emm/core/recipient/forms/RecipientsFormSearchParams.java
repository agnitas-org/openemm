/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.forms;

import com.agnitas.web.forms.FormSearchParams;

import java.util.List;

public class RecipientsFormSearchParams implements FormSearchParams<RecipientListForm> {

    private final int defaultUserStatus;

    public RecipientsFormSearchParams(int defaultUserStatus) {
        this.defaultUserStatus = defaultUserStatus;
    }

    private int mailinglistId;
    private int targetId;
    private int userStatus;
    private String firstName;
    private Integer gender;
    private String lastName;
    private String email;
    private String queryBuilderRules;
    private List<String> userTypes;

    @Override
    public void storeParams(RecipientListForm form) {
        this.mailinglistId = form.getFilterMailinglistId();
        this.targetId = form.getFilterTargetId();
        this.userStatus = form.getFilterUserStatus();
        this.firstName = form.getSearchFirstName();
        this.lastName = form.getSearchLastName();
        this.gender = form.getFilterGender();
        this.email = form.getSearchEmail();
        this.queryBuilderRules = form.getSearchQueryBuilderRules();
        this.userTypes = form.getFilterUserTypes();
    }

    @Override
    public void restoreParams(RecipientListForm form) {
        form.setFilterMailinglistId(mailinglistId);
        form.setFilterTargetId(targetId);
        form.setFilterUserStatus(userStatus);
        form.setFilterUserTypes(userTypes);
        form.setSearchFirstName(firstName);
        form.setSearchLastName(lastName);
        form.setSearchEmail(email);
        form.setSearchQueryBuilderRules(queryBuilderRules);
        form.setFilterGender(gender);
    }

    @Override
    public void resetParams() {
        mailinglistId = 0;
        targetId = 0;
        userStatus = defaultUserStatus;
        firstName = "";
        lastName = "";
        email = "";
        queryBuilderRules = "[]";
        gender = null;
        userTypes = null;
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
