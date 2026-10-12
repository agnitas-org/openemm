/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.target;

import com.agnitas.beans.Admin;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.emm.rest.v2.infrastructure.security.auth.PermissionAllowed;
import jakarta.validation.Valid;
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
@RequestMapping("/targets")
public class TargetsV2Controller {

    public final TargetRestService targetService;

    public TargetsV2Controller(TargetRestService targetService) {
        this.targetService = targetService;
    }

    @GetMapping
    @PermissionAllowed("targets.show")
    public PageResponse<TargetLightResponse> paginatedList(Pageable pageable, Admin admin) {
        return targetService.find(pageable, admin);
    }

    @GetMapping("/{id:\\d+}")
    @PermissionAllowed("targets.show")
    public TargetResponse getById(@PathVariable @Positive int id, Admin admin) {
        return targetService.find(id, admin.getCompanyID());
    }

    @PostMapping
    @PermissionAllowed("targets.change")
    @ResponseStatus(HttpStatus.CREATED)
    public TargetResponse create(@RequestBody @Valid CreateTargetRequest request, Admin admin) {
        return targetService.create(request, admin.getCompanyID());
    }

    @PatchMapping("/{id:\\d+}")
    @PermissionAllowed("targets.change")
    public TargetResponse patch(
            @PathVariable int id,
            @Valid @RequestBody PatchTargetRequest request,
            Admin admin
    ) {
        return targetService.patch(id, request, admin.getCompanyID());
    }

    @DeleteMapping("/{id:\\d+}")
    @PermissionAllowed("targets.delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id, Admin admin) {
        targetService.delete(id, admin);
    }
}
