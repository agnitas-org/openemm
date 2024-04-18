/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.forms;

import com.agnitas.emm.core.mailing.enums.MailingRecipientType;
import com.agnitas.emm.core.recipient.forms.RecipientListBaseForm;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MailingRecipientsOverviewFilter extends RecipientListBaseForm {

    private static final Set<String> DEFAULT_COLUMNS = Set.of("email", "firstname", "lastname", "customer_id");

    private boolean loadRecipients;
    private List<MailingRecipientType> types;
    private String firstname;
    private String lastname;
    private String email;
    private int recipientsFilter; // TODO: EMMGUI-714: remove when old design will be removed

    @Override
    public boolean isDefaultColumn(String column) {
        return DEFAULT_COLUMNS.stream().anyMatch(column::equalsIgnoreCase);
    }

    @Override
    public boolean isSelectedColumn(String column) {
        return selectedFields.contains(column);
    }

    public List<MailingRecipientType> getTypes() {
        return types;
    }

    public void setTypes(List<MailingRecipientType> types) {
        this.types = types;
    }

    public int getRecipientsFilter() {
        return recipientsFilter;
    }

    public void setRecipientsFilter(int recipientsFilter) {
        this.recipientsFilter = recipientsFilter;
    }

    public boolean isLoadRecipients() {
        return loadRecipients;
    }

    public void setLoadRecipients(boolean loadRecipients) {
        this.loadRecipients = loadRecipients;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public Object[] toArray() {
        return ArrayUtils.addAll(Arrays.asList(
                recipientsFilter,
                selectedFields,
                loadRecipients,
                types,
                firstname,
                lastname,
                email
        ).toArray(), super.toArray());
    }
}
