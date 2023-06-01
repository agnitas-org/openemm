/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.validator.impl;

import java.util.Objects;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.mailing.service.MailingPropertiesRules;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.emm.core.mailingcontent.validator.DynTagValidator;
import com.agnitas.web.mvc.Popups;

@Component
@Order(0)
public class MailingEditableValidator implements DynTagValidator {

    private final MailingPropertiesRules mailingPropertiesRules;

    public MailingEditableValidator(final MailingPropertiesRules rules) {
    	this.mailingPropertiesRules = Objects.requireNonNull(rules, "MailingPropertiesRules is null");
    }

    @Override
    public final boolean validate(final DynTagDto dynTagDto, final Popups popups, final Admin admin) {
        if (!isMailingGridEditable(dynTagDto.getMailingId(), admin)) {
            popups.alert("status_changed");
            return false;
        }

        return true;
    }

    private boolean isMailingGridEditable(final int mailingID, final Admin admin) {
        return mailingID <= 0 || this.mailingPropertiesRules.isMailingContentEditable(mailingID, admin);
    }
}
