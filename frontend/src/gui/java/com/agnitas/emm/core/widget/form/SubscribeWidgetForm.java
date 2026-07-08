/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.widget.form;

import com.agnitas.util.importvalues.Gender;
import com.agnitas.util.importvalues.MailType;

public class SubscribeWidgetForm {

    private final String token;
    private Gender gender = Gender.UNKNOWN;
    private String firstName;
    private String lastName;
    private String smsNumber;
    private MailType mailType = MailType.HTML;
    private String email;
    private boolean trackingVeto;

    public SubscribeWidgetForm(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
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

    public String getSmsNumber() {
        return smsNumber;
    }

    public void setSmsNumber(String smsNumber) {
        this.smsNumber = smsNumber;
    }

    public MailType getMailType() {
        return mailType;
    }

    public void setMailType(MailType mailType) {
        this.mailType = mailType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isTrackingVeto() {
        return trackingVeto;
    }

    public void setTrackingVeto(boolean trackingVeto) {
        this.trackingVeto = trackingVeto;
    }
}
