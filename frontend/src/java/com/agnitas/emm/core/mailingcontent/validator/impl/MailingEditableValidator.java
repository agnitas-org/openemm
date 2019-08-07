/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.validator.impl;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.util.AgnUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.emm.core.mailingcontent.validator.DynTagValidator;
import com.agnitas.web.mvc.Popups;

@Component
@Order(0)
public class MailingEditableValidator implements DynTagValidator {

    private MaildropService maildropService;
    private HttpServletRequest request;

    public MailingEditableValidator(MaildropService maildropService,
                                    HttpServletRequest request) {
        this.maildropService = maildropService;
        this.request = request;
    }

    @Override
    public boolean validate(DynTagDto dynTagDto, Popups popups) {
        ComAdmin admin = AgnUtils.getAdmin(request);
        if (admin == null) {
            return false;
        }


        if (!isMailingGridEditable(dynTagDto.getMailingId(), admin)) {
            popups.alert("status_changed");
            return false;
        }

        return true;
    }

    private boolean isMailingGridEditable(int mailingID, ComAdmin admin) {
        if (maildropService.isActiveMailing(mailingID, admin.getCompanyID())) {
            return admin.permissionAllowed(Permission.MAILING_CONTENT_CHANGE_ALWAYS);
        } else {
            return true;
        }
    }
}
