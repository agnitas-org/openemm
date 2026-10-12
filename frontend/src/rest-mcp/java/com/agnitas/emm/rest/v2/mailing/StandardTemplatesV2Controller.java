/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing;

import com.agnitas.beans.Admin;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.emm.rest.v2.infrastructure.security.auth.PermissionAllowed;
import com.agnitas.emm.rest.v2.infrastructure.security.xss.XssCheckLocation;
import com.agnitas.emm.rest.v2.infrastructure.security.xss.XssExclude;
import com.agnitas.emm.rest.v2.mailing.dto.CreateMailingRequest;
import com.agnitas.emm.rest.v2.mailing.dto.ListTemplatesRequest;
import com.agnitas.emm.rest.v2.mailing.dto.MailingFullResponse;
import com.agnitas.emm.rest.v2.mailing.dto.MailingLightResponse;
import com.agnitas.emm.rest.v2.mailing.dto.MailingResponse;
import com.agnitas.emm.rest.v2.mailing.dto.PatchMailingRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/templates")
public class StandardTemplatesV2Controller {

    private final MailingRestService mailingRestService;

    public StandardTemplatesV2Controller(MailingRestService mailingRestService) {
        this.mailingRestService = mailingRestService;
    }

    @GetMapping
    @PermissionAllowed("template.show")
    public PageResponse<MailingLightResponse> list(@Valid ListTemplatesRequest query, Pageable pageable, Admin admin) {
        return mailingRestService.listTemplates(query, pageable, admin);
    }

    @PermissionAllowed("template.show")
    @GetMapping(value = "/{id:\\d+}")
    public MailingResponse find(@PathVariable @Positive int id, Admin admin) {
        return mailingRestService.findTemplate(id, admin.getCompanyID());
    }

    @PermissionAllowed("mailing.show")
    @GetMapping(value = "/{id:\\d+}", params = "view=full")
    public MailingFullResponse findFull(@PathVariable @Positive int id, Admin admin) {
        return mailingRestService.findTemplateFull(id, admin.getCompanyID());
    }

    @PermissionAllowed("mailing.show")
    @GetMapping(value = "/{id:\\d+}", params = "view=light")
    public MailingLightResponse findLight(@PathVariable @Positive int id, Admin admin) {
        return mailingRestService.findTemplateLight(id, admin.getCompanyID());
    }

    @PostMapping
    @PermissionAllowed("template.change")
    @XssExclude(checkMethod = "isExcludedFromXssCheck")
    @ResponseStatus(HttpStatus.CREATED)
    public MailingResponse create(@Validated @RequestBody CreateMailingRequest request, Admin admin) {
        return mailingRestService.createTemplate(request, admin);
    }

    @PostMapping("/{id:\\d+}/copy")
    @PermissionAllowed("template.change")
    @ResponseStatus(HttpStatus.CREATED)
    public MailingLightResponse copy(@PathVariable @Positive int id, Admin admin) {
        return mailingRestService.copyTemplate(id, admin.getCompanyID());
    }

    @PatchMapping("/{id}")
    @PermissionAllowed("template.change")
    @XssExclude(checkMethod = "isExcludedFromXssCheck")
    public MailingResponse patch(
            @Positive @PathVariable int id,
            @Validated @RequestBody PatchMailingRequest request,
            Admin admin
    ) {
        return mailingRestService.patchTemplate(id, request, admin);
    }

    @DeleteMapping("/{id}")
    @PermissionAllowed("template.delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive int id, Admin admin) {
        mailingRestService.deleteTemplate(id, admin);
    }

    @SuppressWarnings("unused")
    public boolean isExcludedFromXssCheck(String name, XssCheckLocation location, Admin admin) {
        if (location != XssCheckLocation.BODY_FIELD) {
            return false;
        }
        name = StringUtils.defaultString(name);
        return name.matches("htmlTemplate")
                || name.matches("textTemplate");
    }
}
