/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.form.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.target.form.TargetEditForm;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.web.mvc.Popups;

@Component
public class TargetEditFormValidator {

    private final ComTargetService targetService;

    public TargetEditFormValidator(ComTargetService targetService) {
        this.targetService = targetService;
    }

    public boolean validate(int companyId, TargetEditForm form, Popups popups) {
        boolean isValid = true;

        if(StringUtils.isBlank(form.getShortname())) {
            isValid = false;
            popups.alert("error.name.is.empty");
        } else if (!targetService.checkIfTargetNameIsValid(form.getShortname())) {
            isValid = false;
            popups.alert("error.target.namenotallowed");
        } else if (targetService.checkIfTargetNameAlreadyExists(companyId, form.getShortname(), form.getTargetId())) {
            popups.alert("error.target.namealreadyexists");
            isValid = false;
        }

        return isValid;
    }
}
