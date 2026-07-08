/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.mailinglists;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.restful.v2.mailinglists.dto.CreateMailinglistRequest;
import com.agnitas.emm.restful.v2.mailinglists.dto.MailinglistResponse;
import com.agnitas.emm.restful.v2.mailinglists.dto.MailinglistsPage;
import com.agnitas.emm.restful.v2.mailinglists.dto.UpdateMailinglistRequest;
import com.agnitas.emm.restful.v2.mailinglists.service.MailinglistRestService;
import com.agnitas.emm.restful.v2.infrastructure.security.auth.PermissionAllowed;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mailinglists")
public class MailinglistsV2Controller {

    private final MailinglistRestService mailinglistRestService;

    public MailinglistsV2Controller(MailinglistRestService mailinglistRestService) {
        this.mailinglistRestService = mailinglistRestService;
    }

    @GetMapping
    @PermissionAllowed("mailinglist.show")
    public PaginatedList<MailinglistResponse> paginatedList(@Valid MailinglistsPage pageReq, Admin admin) {
        return mailinglistRestService.findAll(pageReq, admin.getCompanyID());
    }

    @GetMapping("/{id:\\d+}")
    @PermissionAllowed("mailinglist.show")
    public MailinglistResponse getById(@PathVariable @Positive int id, Admin admin) {
        return mailinglistRestService.getById(id, admin.getCompanyID());
    }

    @PostMapping
    @PermissionAllowed("mailinglist.change")
    @ResponseStatus(HttpStatus.CREATED)
    public MailinglistResponse create(@Valid @RequestBody CreateMailinglistRequest request, Admin admin) {
        return mailinglistRestService.create(request, admin.getCompanyID());
    }

    @PatchMapping("/{id}")
    @PermissionAllowed("mailinglist.change")
    public MailinglistResponse patch(@PathVariable int id, Admin admin,
                                     @Valid @RequestBody UpdateMailinglistRequest updateRequest) {
        return mailinglistRestService.updatePartially(id, admin.getCompanyID(), updateRequest);
    }

    @DeleteMapping("/{id}")
    @PermissionAllowed("mailinglist.delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id, Admin admin) {
        mailinglistRestService.delete(id, admin);
    }
}
