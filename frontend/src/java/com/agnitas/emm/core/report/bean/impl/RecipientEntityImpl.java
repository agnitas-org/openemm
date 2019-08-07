/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.bean.impl;

import java.util.Map;

import com.agnitas.emm.core.report.bean.RecipientEntity;

public class RecipientEntityImpl implements RecipientEntity {

    private int id;

    private int salutation;

    private String title;

    private String firstName;

    private String lastName;

    private String email;

    private boolean isTrackingVeto;

    private int mailFormat;

    private Map<String, Object> otherRecipientData;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getSalutation() {
        return salutation;
    }

    @Override
    public void setSalutation(int salutation) {
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
    public int getMailFormat() {
        return mailFormat;
    }

    @Override
    public void setMailFormat(int mailFormat) {
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
