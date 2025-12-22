/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.forms;

import java.util.List;

import com.agnitas.emm.core.mailing.enums.MailingRecipientType;
import com.agnitas.web.forms.FormSearchParams;

public class MailingRecipientsFormSearchParams implements FormSearchParams<MailingRecipientsOverviewFilter> {

    private String firstname;
    private String lastname;
    private String email;
    private List<MailingRecipientType> types;

    @Override
    public void storeParams(MailingRecipientsOverviewFilter filter) {
        firstname = filter.getFirstname();
        lastname = filter.getLastname();
        email = filter.getEmail();
        types = filter.getTypes();
    }

    @Override
    public void restoreParams(MailingRecipientsOverviewFilter filter) {
        filter.setFirstname(firstname);
        filter.setLastname(lastname);
        filter.setEmail(email);
        filter.setTypes(types);
    }

}
