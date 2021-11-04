/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.form;

import org.agnitas.web.forms.FormSearchParams;

public class AdminListFormSearchParams implements FormSearchParams<AdminListForm>{
    private String firstName;
    private String lastName;
    private String email;
    private Integer companyId;
    private Integer mailinglistId;
    private Integer adminGroupId;
    private String language;

    @Override
    public void storeParams(AdminListForm form) {
        this.firstName = form.getSearchFirstName();
        this.lastName = form.getSearchLastName();
        this.email = form.getSearchEmail();
        this.companyId = form.getFilterCompanyId();
        this.mailinglistId = form.getFilterMailinglistId();
        this.adminGroupId = form.getFilterAdminGroupId();
        this.language = form.getFilterLanguage();
    }

    @Override
    public void restoreParams(AdminListForm form) {
        form.setSearchFirstName(this.firstName);
        form.setSearchLastName(this.lastName);
        form.setSearchEmail(this.email);
        form.setFilterCompanyId(this.companyId);
        form.setFilterMailinglistId(this.mailinglistId);
        form.setFilterAdminGroupId(this.adminGroupId);
        form.setFilterLanguage(this.language);
    }

    @Override
    public void resetParams() {
        firstName = null;
        lastName = null;
        email = null;
        companyId = null;
        mailinglistId = null;
        adminGroupId = null;
        language = null;
    }
}
