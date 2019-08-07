/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.dto;

import javax.validation.constraints.Size;

import org.agnitas.emm.core.validator.annotation.Password;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

public class CompanyAdminDto {

    @Size.List({
            @Size(min = 2, message = "error.name.too.short"),
            @Size(max = 30, message = "error.name.too.long")
    })
    @NotEmpty(message = "error.name.is.empty")
    private String firstName;

    @Size.List({
            @Size(min = 2, message = "error.name.too.short"),
            @Size(max = 30, message = "error.name.too.long")
    })
    @NotEmpty(message = "error.name.is.empty")
    private String lastName;

    @Size.List({
            @Size(min = 3, message = "error.username.tooShort"),
            @Size(max = 100, message = "error.username.tooLong")
    })
    @NotEmpty(message = "error.username.required")
    private String username;

    @Password()
    @NotEmpty(message = "error.password.missing")
    private String password;

    @Email(message = "error.email.invalid")
    @NotEmpty(message = "error.email.empty")
    private String email;

    @Email(message = "error.email.invalid")
    private String statisticEmail;

    private int salutation;
    private String title;
    private Boolean hasDisposablePassword;
    private String language;
    private String timeZone;

    public int getSalutation() {
        return salutation;
    }

    public void setSalutation(int salutation) {
        this.salutation = salutation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getHasDisposablePassword() {
        return hasDisposablePassword;
    }

    public void setHasDisposablePassword(Boolean hasDisposablePassword) {
        this.hasDisposablePassword = hasDisposablePassword;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatisticEmail() {
        return statisticEmail;
    }

    public void setStatisticEmail(String statisticEmail) {
        this.statisticEmail = statisticEmail;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
