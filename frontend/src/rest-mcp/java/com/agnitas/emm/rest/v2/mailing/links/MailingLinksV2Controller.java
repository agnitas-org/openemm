/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.links;

import com.agnitas.beans.Admin;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.emm.rest.v2.infrastructure.security.auth.PermissionAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mailings/{mailingId:\\d+}/links")
class MailingLinksV2Controller {

    private final MailingLinkRestService linkRestService;

    MailingLinksV2Controller(MailingLinkRestService linkRestService) {
        this.linkRestService = linkRestService;
    }

    @GetMapping
    @PermissionAllowed("mailing.content.show")
    PageResponse<LinkResponse> list(@PathVariable @Positive int mailingId, Pageable pageable, Admin admin) {
        return linkRestService.list(mailingId, pageable, admin);
    }

    @PermissionAllowed("mailing.content.show")
    @GetMapping(value = "/{linkId:\\d+}")
    public LinkResponse find(
            @PathVariable @Positive int mailingId,
            @PathVariable @Positive int linkId,
            Admin admin
    ) {
        return linkRestService.find(linkId, mailingId, admin);
    }

    @PatchMapping("/{linkId:\\d+}")
    @PermissionAllowed("mailing.content.show")
    public LinkResponse patch(
            @PathVariable @Positive int mailingId,
            @PathVariable @Positive int linkId,
            @Valid @RequestBody PatchLinkRequest request,
            Admin admin
    ) {
        return linkRestService.patch(linkId, mailingId, request, admin);
    }
}
