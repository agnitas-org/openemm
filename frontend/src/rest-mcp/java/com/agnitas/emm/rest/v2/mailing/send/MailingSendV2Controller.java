/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.send;

import com.agnitas.beans.Admin;
import com.agnitas.emm.rest.v2.infrastructure.security.auth.PermissionAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mailings/{mailingId:\\d+}")
public class MailingSendV2Controller {

    private final MailingSendRestService mailingSendRestService;

    public MailingSendV2Controller(MailingSendRestService mailingSendRestService) {
        this.mailingSendRestService = mailingSendRestService;
    }

    @PostMapping("/send")
    @PermissionAllowed("mailing.send.world")
    public MailingStartResponse sendWorld(
            @PathVariable @Positive int mailingId,
            @RequestBody(required = false) @Valid StartSendMailingRequest request,
            Admin admin
    ) {
        return mailingSendRestService.sendWorld(mailingId, admin, request);
    }

    @PostMapping("/send-admin")
    public MailingStartResponse sendAdmin(
            @PathVariable @Positive int mailingId,
            @RequestBody(required = false) @Valid SendAdminMailingRequest request,
            Admin admin
    ) {
        return mailingSendRestService.sendAdmin(mailingId, admin, request);
    }

    @PostMapping("/send-test")
    public MailingStartResponse sendTest(
            @PathVariable @Positive int mailingId,
            @RequestBody(required = false) @Valid SendTestMailingRequest request,
            Admin admin
    ) {
        return mailingSendRestService.sendTest(mailingId, admin, request);
    }

    @PostMapping("/activate")
    @PermissionAllowed("mailing.send.world")
    public MailingStartResponse activate(
            @PathVariable @Positive int mailingId,
            @RequestBody(required = false) @Valid StartSendMailingRequest request,
            Admin admin
    ) {
        return mailingSendRestService.activate(mailingId, admin, request);
    }

    @PostMapping("/cancel")
    @PermissionAllowed("mailing.send.world")
    public MailingStopResponse cancel(@PathVariable @Positive int mailingId, Admin admin) {
        return mailingSendRestService.cancel(mailingId, admin.getCompanyID());
    }

    @PostMapping("/deactivate")
    @PermissionAllowed("mailing.send.world")
    public MailingStopResponse deactivate(@PathVariable @Positive int mailingId, Admin admin) {
        return mailingSendRestService.deactivate(mailingId, admin.getCompanyID());
    }
}
