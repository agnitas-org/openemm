/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.user;

import com.agnitas.beans.Admin;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.emm.rest.v2.infrastructure.security.auth.PermissionAllowed;
import com.agnitas.emm.rest.v2.infrastructure.security.xss.XssCheckLocation;
import com.agnitas.emm.rest.v2.infrastructure.security.xss.XssExclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/users")
public class UiUsersV2Controller {

    private final UiUserRestService usersRestService;

    public UiUsersV2Controller(UiUserRestService usersRestService) {
        this.usersRestService = usersRestService;
    }

    @GetMapping
    @PermissionAllowed("admin.show")
    public PageResponse<UiUserLightResponse> list(Pageable pageable, Admin admin) {
        return usersRestService.list(pageable, admin);
    }

    @GetMapping("/{id:\\d+}")
    @PermissionAllowed("admin.show")
    public UiUserResponse readById(@PathVariable @Positive int id, Admin admin) {
        return usersRestService.find(id, admin.getCompanyID());
    }

    @GetMapping("/by-name/{username}")
    @PermissionAllowed("admin.show")
    public UiUserResponse readByUserName(@PathVariable @NotBlank String username, Admin admin) {
        return usersRestService.find(username, admin.getCompanyID());
    }

    @PostMapping
    @PermissionAllowed("admin.new")
    @XssExclude(checkMethod = "isExcludedFromXssCheck")
    @ResponseStatus(HttpStatus.CREATED)
    public UiUserResponse create(@RequestBody @Valid CreateUiUserRequest request, Admin admin) {
        return usersRestService.create(request, admin);
    }

    @PatchMapping("/{id:\\d+}")
    @PermissionAllowed("admin.change")
    @XssExclude(checkMethod = "isExcludedFromXssCheck")
    public UiUserResponse patchById(
            @PathVariable @Positive int id,
            @RequestBody @Valid PatchUiUserRequest request,
            Admin admin
    ) {
        return usersRestService.patch(id, request, admin);
    }

    @PatchMapping("/by-name/{username}")
    @PermissionAllowed("admin.change")
    @XssExclude(checkMethod = "isExcludedFromXssCheck")
    public UiUserResponse patchByUsername(
            @PathVariable @NotBlank String username,
            @RequestBody @Valid PatchUiUserRequest request,
            Admin admin
    ) {
        return usersRestService.patch(username, request, admin);
    }

    @DeleteMapping("/{id:\\d+}")
    @PermissionAllowed("admin.delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable @Positive int id, Admin admin) {
        usersRestService.delete(id, admin.getCompanyID());
    }

    @DeleteMapping("/by-name/{username}")
    @PermissionAllowed("admin.delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByUsername(@PathVariable @NotBlank String username, Admin admin) {
        usersRestService.delete(username, admin.getCompanyID());
    }

    @SuppressWarnings("unused")
    public boolean isExcludedFromXssCheck(String name, XssCheckLocation location, Admin admin) {
        if (location != XssCheckLocation.BODY_FIELD) {
            return false;
        }
        return "password".equals(name);
    }
}
