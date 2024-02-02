/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.forms;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.agnitas.emm.core.recipient.forms.RecipientListBaseForm;

public class MailingRecipientsForm extends RecipientListBaseForm {

    private static final Set<String> DEFAULT_COLUMNS = new HashSet<>(Arrays.asList(
            "email", "firstname", "lastname", "customer_id"
    ));

    private boolean loadRecipients;
    private int recipientsFilter;

    @Override
    public boolean isDefaultColumn(String column) {
        return DEFAULT_COLUMNS.stream().anyMatch(column::equalsIgnoreCase);
    }

    @Override
    public boolean isSelectedColumn(String column) {
        return selectedFields.contains(column);
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
}
