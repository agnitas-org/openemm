/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.mailing;

import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.restful.v2.infrastructure.security.auth.PermissionAllowed;
import com.agnitas.emm.restful.v2.mailing.dto.MailingLightResponse;
import com.agnitas.emm.restful.v2.mailing.dto.MailingResponse;
import com.agnitas.emm.restful.v2.mailing.dto.MailingsPage;
import com.agnitas.emm.restful.v2.mailing.dto.UpdateMailingRequest;
import com.agnitas.emm.restful.v2.mailing.service.MailingRestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mailings")
public class MailingsV2Controller {

    private final MailingRestService mailingRestService;

    public MailingsV2Controller(MailingRestService mailingRestService) {
        this.mailingRestService = mailingRestService;
    }

    @GetMapping
    @PermissionAllowed("mailing.show")
    public PaginatedList<MailingLightResponse> list(@Valid MailingsPage pageReq, Admin admin) {
        return mailingRestService.list(pageReq, admin);
    }
    @PermissionAllowed("mailing.show")
    @GetMapping("/{id:\\d+}")
    public MailingResponse getFull(@Positive @PathVariable int id, Admin admin) {
        return mailingRestService.getFull(id, admin.getCompanyID());
    }

    @PermissionAllowed("mailing.show")
    @GetMapping(value = "/{id:\\d+}", params = "view=light")
    public MailingLightResponse getLight(@Positive @PathVariable int id, Admin admin) {
        return mailingRestService.getLight(id, admin.getCompanyID());
    }

    @PostMapping
    @PermissionAllowed("mailing.import")
    @ResponseStatus(HttpStatus.CREATED)
    public MailingLightResponse create(
            @Valid @RequestBody Map<String, Object> body,
            Admin admin
    ) throws JsonProcessingException {
        return mailingRestService.create(body, admin.getCompanyID());
    }

    @PostMapping(params = "templateId")
    @PermissionAllowed("mailing.import")
    @ResponseStatus(HttpStatus.CREATED)
    public MailingLightResponse createFromTemplate(@Positive @RequestParam int templateId, Admin admin) {
        return mailingRestService.createFromTemplate(templateId, admin.getCompanyID());
    }

    @PostMapping(params = "copyFromId")
    @PermissionAllowed("mailing.import")
    @ResponseStatus(HttpStatus.CREATED)
    public MailingLightResponse copy(@RequestParam @Positive int copyFromId, Admin admin) {
        return mailingRestService.copy(copyFromId, admin.getCompanyID());
    }

    @PatchMapping("/{id}")
    @PermissionAllowed("mailing.change")
    public MailingResponse patch(
            @Positive @PathVariable int id,
            @Valid @RequestBody UpdateMailingRequest updateRequest,
            Admin admin
    ) {
        return mailingRestService.patch(id, updateRequest, admin);
    }

    @DeleteMapping("/{id}")
    @PermissionAllowed("mailing.delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive int id, Admin admin) {
        mailingRestService.delete(id, admin);
    }
}
