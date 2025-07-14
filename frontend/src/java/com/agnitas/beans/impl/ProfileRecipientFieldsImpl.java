/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.ProfileRecipientFields;
import com.agnitas.util.ImportUtils;

public class ProfileRecipientFieldsImpl implements ProfileRecipientFields {
	private static final long serialVersionUID = -7473225210668299536L;
	
	private String email;
    private String gender;
    private String mailtype;
    private String firstname;
    private String lastname;
    private String creation_date;
    private String change_date;
    private String title;
    private String temporaryId;
    private List<Integer> updatedIds;
    private Map<String, String> customFields = new HashMap<>();
    private String mailtypeDefined = ImportUtils.MAIL_TYPE_UNDEFINED;

    @Override
	public String getEmail() {
        return email;
    }

    @Override
	public void setEmail(String email) {
        this.email = email;
    }

    @Override
	public String getGender() {
        return gender;
    }

    @Override
	public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
	public String getMailtype() {
        return mailtype;
    }

    @Override
	public void setMailtype(String mailtype) {
        this.mailtype = mailtype;
    }

    @Override
	public String getFirstname() {
        return firstname;
    }

    @Override
	public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    @Override
	public String getLastname() {
        return lastname;
    }

    @Override
	public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @Override
	public String getCreation_date() {
        return creation_date;
    }

    @Override
	public void setCreation_date(String creation_date) {
        this.creation_date = creation_date;
    }

    @Override
	public String getChange_date() {
        return change_date;
    }

    @Override
	public void setChange_date(String change_date) {
        this.change_date = change_date;
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
	public String getTemporaryId() {
        return temporaryId;
    }

    @Override
	public void setTemporaryId(String temporaryId) {
        this.temporaryId = temporaryId;
    }

    @Override
	public List<Integer> getUpdatedIds() {
        return updatedIds;
    }

    @Override
	public void addUpdatedIds(Integer updatedId) {
        if (this.updatedIds == null) {
            this.updatedIds = new ArrayList<>();
        }
        this.updatedIds.add(updatedId);
    }

    @Override
	public Map<String, String> getCustomFields() {
        return customFields;
    }

    @Override
	public void setCustomFields(Map<String, String> customFields) {
        this.customFields = customFields;
    }

    @Override
	public String getMailtypeDefined() {
        return mailtypeDefined;
    }

    @Override
	public void setMailtypeDefined(String mailtypeDefined) {
        this.mailtypeDefined = mailtypeDefined;
    }
}
