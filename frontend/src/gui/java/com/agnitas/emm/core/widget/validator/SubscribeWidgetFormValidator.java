/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.widget.validator;

import com.agnitas.emm.core.widget.form.SubscribeWidgetForm;
import com.agnitas.web.mvc.Popups;
import com.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class SubscribeWidgetFormValidator {

    public boolean validate(SubscribeWidgetForm form, Popups popups) {
        if (StringUtils.isBlank(form.getEmail()) || !AgnUtils.isEmailValid(form.getEmail())) {
            popups.alert("error.invalid.email");
        }

        if (StringUtils.length(form.getFirstName()) > 100) {
            popups.alert("error.recipient.firstname.tooLong");
        }
        if (StringUtils.length(form.getLastName()) > 100) {
            popups.alert("error.recipient.lastname.tooLong");
        }

        return !popups.hasAlertPopups();
    }
}
