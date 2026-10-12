/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.binding;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.binding.BindingIdentifier;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.rest.v2.infrastructure.bulk.BulkDeleteResult;
import com.agnitas.emm.rest.v2.infrastructure.bulk.BulkOperationResponse;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.emm.rest.v2.infrastructure.security.auth.PermissionAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/bindings")
public class BindingsV2Controller {

    private final BindingRestService bindingRestService;

    public BindingsV2Controller(BindingRestService bindingRestService) {
        this.bindingRestService = bindingRestService;
    }

    @GetMapping
    @PermissionAllowed("recipient.show")
    public PageResponse<BindingResponse> list(@Valid ListBindingsRequest request, Pageable pageable, Admin admin) {
        return bindingRestService.find(request, pageable, admin.getCompanyID());
    }

    @PostMapping
    @PermissionAllowed("recipient.change")
    @ResponseStatus(HttpStatus.CREATED)
    public BindingResponse create(@RequestBody @Valid CreateBindingRequest request, Admin admin) {
        return bindingRestService.create(request, admin.getCompanyID());
    }

    @GetMapping("/{recipientId:\\d+}/{mailinglistId:\\d+}/{mediatype}")
    @PermissionAllowed("recipient.show")
    public BindingResponse get(
            @PathVariable @Positive int recipientId,
            @PathVariable @Positive int mailinglistId,
            @PathVariable @NotNull MediaTypes mediatype,
            Admin admin
    ) {
        return bindingRestService.find(new BindingIdentifier(
                recipientId,
                mailinglistId,
                mediatype
        ), admin.getCompanyID());
    }

    @PatchMapping("/{recipientId:\\d+}/{mailinglistId:\\d+}/{mediatype}")
    @PermissionAllowed("recipient.change")
    public BindingResponse updatePartially(
            @PathVariable @Positive int recipientId,
            @PathVariable @Positive int mailinglistId,
            @PathVariable @NotNull MediaTypes mediatype,
            @RequestBody @Valid PatchBindingRequest request,
            Admin admin
    ) {
        return bindingRestService.update(
                new BindingIdentifier(recipientId, mailinglistId, mediatype),
                request,
                admin.getCompanyID()
        );
    }

    @PostMapping("/bulk")
    @PermissionAllowed("recipient.change")
    public BulkUpsertBindingResponse bulkUpsert(@RequestBody @Valid BulkUpsertBindingRequest request, Admin admin) {
        return bindingRestService.upsert(request, admin);
    }

    @DeleteMapping({
            "/{recipientId:\\d+}/{mailinglistId:\\d+}",
            "/{recipientId:\\d+}/{mailinglistId:\\d+}/{mediatype}"
    })
    @PermissionAllowed("recipient.change")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable @Positive int recipientId,
            @PathVariable @Positive int mailinglistId,
            @PathVariable(required = false) MediaTypes mediatype,
            Admin admin
    ) {
        bindingRestService.delete(new BindingIdentifier(
                recipientId,
                mailinglistId,
                mediatype
        ), admin.getCompanyID());
    }

    @DeleteMapping("/bulk")
    @PermissionAllowed("recipient.change")
    public BulkOperationResponse<BulkDeleteResult> delete(
            @RequestBody @Valid BulkDeleteBindingRequest request,
            Admin admin
    ) {
        return bindingRestService.delete(request, admin.getCompanyID());
    }
}
