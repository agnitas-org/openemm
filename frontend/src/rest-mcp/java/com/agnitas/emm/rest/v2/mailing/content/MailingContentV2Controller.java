/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.content;

import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.emm.rest.v2.infrastructure.security.auth.PermissionAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mailings/{mailingId:\\d+}/contents")
class MailingContentV2Controller {

    private final MailingContentRestService contentService;

    MailingContentV2Controller(MailingContentRestService contentService) {
        this.contentService = contentService;
    }

    @GetMapping
    @PermissionAllowed("mailing.content.show")
    public List<MailingContentResponse> list(@PathVariable @Positive int mailingId, Admin admin) {
        return contentService.list(mailingId, admin.getCompanyID());
    }

    @GetMapping("/{contentId:\\d+}")
    @PermissionAllowed("mailing.content.show")
    public MailingContentResponse getById(
            @PathVariable @Positive int mailingId,
            @PathVariable @Positive int contentId,
            Admin admin
    ) {
        return contentService.getById(contentId, mailingId, admin.getCompanyID());
    }

    @GetMapping("/by-name/{contentName}")
    @PermissionAllowed("mailing.content.show")
    public MailingContentResponse getByName(
            @PathVariable @Positive int mailingId,
            @PathVariable @NotBlank String contentName,
            Admin admin
    ) {
        return contentService.getByName(contentName, mailingId, admin.getCompanyID());
    }

    @PatchMapping("/{contentId:\\d+}")
    @PermissionAllowed("mailing.change")
    public MailingContentResponse patch(
            @PathVariable @Positive int mailingId,
            @PathVariable @Positive int contentId,
            @RequestBody @Valid PatchMailingContentRequest request,
            Admin admin
    ) {
        return contentService.patch(contentId, mailingId, request, admin);
    }

    @PatchMapping("/by-name/{contentName}")
    @PermissionAllowed("mailing.change")
    public MailingContentResponse patch(
            @PathVariable @Positive int mailingId,
            @PathVariable @NotBlank String contentName,
            @RequestBody @Valid PatchMailingContentRequest request,
            Admin admin
    ) {
        return contentService.patch(contentName, mailingId, request, admin);
    }

    @DeleteMapping("/{contentId:\\d+}")
    @PermissionAllowed("mailing.change")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable @Positive int mailingId,
            @PathVariable @Positive int contentId,
            Admin admin
    ) {
        contentService.delete(contentId, mailingId, admin.getCompanyID());
    }

    @DeleteMapping("/by-name/{contentName}")
    @PermissionAllowed("mailing.change")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable @Positive int mailingId,
            @PathVariable @NotBlank String contentName,
            Admin admin
    ) {
        contentService.delete(contentName, mailingId, admin.getCompanyID());
    }
}
