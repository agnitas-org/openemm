/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.dto.impl;

import java.util.Map;

import com.agnitas.emm.core.report.dto.RecipientEntityDto;

public class RecipientEntityDtoImpl implements RecipientEntityDto {

    private String salutation;

    private String title;

    private String firstName;

    private String lastName;

    private String email;

    private boolean isTrackingVeto;

    private String mailFormat;

    private Map<String, Object> otherRecipientData;

    @Override
    public String getSalutation() {
        return salutation;
    }

    @Override
    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean isTrackingVeto() {
        return isTrackingVeto;
    }

    @Override
    public void setTrackingVeto(boolean trackingVeto) {
        isTrackingVeto = trackingVeto;
    }

    @Override
    public String getMailFormat() {
        return mailFormat;
    }

    @Override
    public void setMailFormat(String mailFormat) {
        this.mailFormat = mailFormat;
    }

    @Override
    public Map<String, Object> getOtherRecipientData() {
        return otherRecipientData;
    }

    @Override
    public void setOtherRecipientData(Map<String, Object> otherRecipientData) {
        this.otherRecipientData = otherRecipientData;
    }
}
