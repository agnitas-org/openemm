/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.forms;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.agnitas.util.importvalues.Gender;
import org.agnitas.util.importvalues.MailType;

import com.agnitas.beans.ProfileField;
import com.agnitas.emm.core.recipient.dto.FrequencyCounter;

public class RecipientForm {

    private int id;
    private Gender gender = Gender.UNKNOWN;
    private String title;
    private String firstname;
    private String lastname;
    private String email;
    private MailType mailtype = MailType.HTML;
    private boolean trackingVeto;
    private boolean encryptedSend;
    private int latestDataSourceId;
    private int dataSourceId;

    private Map<String, String> additionalColumns = new HashMap<>();

    private Set<ProfileField> sortedFields = new LinkedHashSet<>();

    private FrequencyCounter counter;

    private RecipientBindingListForm bindingsListForm = new RecipientBindingListForm();

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Gender getGender() {
        return gender;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setCounter(FrequencyCounter counter) {
        this.counter = counter;
    }

    public FrequencyCounter getCounter() {
        return counter;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setMailtype(MailType mailtype) {
        this.mailtype = mailtype;
    }

    public MailType getMailtype() {
        return mailtype;
    }

    public void setAdditionalColumns(Map<String, String> additionalColumns) {
        this.additionalColumns = additionalColumns;
    }

    public Map<String, String> getAdditionalColumns() {
        return additionalColumns;
    }

    public boolean isTrackingVeto() {
        return trackingVeto;
    }

    public void setTrackingVeto(boolean trackingVeto) {
        this.trackingVeto = trackingVeto;
    }

    public boolean isEncryptedSend() {
        return encryptedSend;
    }

    public void setEncryptedSend(boolean encryptedSend) {
        this.encryptedSend = encryptedSend;
    }

    public Set<ProfileField> getSortedFields() {
        return sortedFields;
    }

    public void setSortedFields(Set<ProfileField> sortedFields) {
        this.sortedFields = sortedFields;
    }

    public RecipientBindingListForm getBindingsListForm() {
        return bindingsListForm;
    }

    public void setLatestDataSourceId(int latestDataSourceId) {
        this.latestDataSourceId = latestDataSourceId;
    }

    public int getLatestDataSourceId() {
        return latestDataSourceId;
    }

    public void setDataSourceId(int dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public int getDataSourceId() {
        return dataSourceId;
    }
}
