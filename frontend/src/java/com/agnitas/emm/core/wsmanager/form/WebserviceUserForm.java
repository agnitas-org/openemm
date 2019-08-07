/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.wsmanager.form;

import javax.validation.constraints.Min;

import org.agnitas.emm.core.validator.annotation.FieldsValueMatch;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

@FieldsValueMatch(field = "password", fieldMatch = "passwordRepeat", message = "error.webserviceuser.mismatching_passwords",
        groups = {WebserviceUserForm.ValidationStepTwo.class, WebserviceUserForm.ValidationEditUser.class})
public class WebserviceUserForm {

    public interface ValidationStepOne {
    	// Do nothing
    }

    public interface ValidationStepTwo {
    	// Do nothing
    }

    public interface ValidationEditUser {
    	// Do nothing
    }

    @NotEmpty(message = "error.webserviceuser.no_username", groups = {ValidationStepOne.class})
    private String userName;

    @Email(message = "error.invalid.email", groups = {ValidationStepOne.class, ValidationEditUser.class})
    private String email;

    @NotEmpty(message = "error.password.missing", groups = {ValidationStepOne.class})
    private String password;

    private String passwordRepeat;

    private int companyId;

    private String contactInfo;

    private boolean active;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordRepeat() {
        return passwordRepeat;
    }

    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
