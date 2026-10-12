/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.trigger;

import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.emm.rest.v2.infrastructure.security.auth.PermissionAllowed;
import com.agnitas.emm.rest.v2.infrastructure.security.xss.XssCheckLocation;
import com.agnitas.emm.rest.v2.infrastructure.security.xss.XssExclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.apache.commons.lang3.StringUtils;
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
@RequestMapping("/triggers")
public class TriggersV2Controller {

    public final TriggerRestService triggerRestService;

    public TriggersV2Controller(TriggerRestService triggerRestService) {
        this.triggerRestService = triggerRestService;
    }

    @GetMapping
    @PermissionAllowed("actions.show")
    public PageResponse<TriggerLightResponse> list(Pageable pageable, Admin admin) {
        return triggerRestService.find(pageable, admin.getCompanyID());
    }

    @GetMapping("/{id:\\d+}")
    @PermissionAllowed("actions.show")
    public TriggerResponse getById(@PathVariable @Positive int id, Admin admin) {
        return triggerRestService.find(id, admin);
    }

    @PostMapping
    @PermissionAllowed("actions.change")
    @XssExclude(checkMethod = "isExcludedFromXssCheck")
    @ResponseStatus(HttpStatus.CREATED)
    public TriggerResponse create(@RequestBody @Valid CreateTriggerRequest request, Admin admin) {
        return triggerRestService.create(request, admin);
    }

    @PatchMapping("/{id:\\d+}")
    @XssExclude(checkMethod = "isExcludedFromXssCheck")
    @PermissionAllowed("actions.change")
    public TriggerResponse patch(
            @PathVariable int id,
            @Valid @RequestBody PatchTriggerRequest request,
            Admin admin
    ) {
        return triggerRestService.patch(id, request, admin);
    }

    @PostMapping("/{id:\\d+}/execute")
    @PermissionAllowed("actions.show")
    public void execute(
            @PathVariable @Positive int id,
            @RequestBody Map<String, Object> fields,
            Admin admin
    ) {
        triggerRestService.execute(id, admin.getCompanyID(), fields);
    }

    @DeleteMapping("/{id:\\d+}")
    @PermissionAllowed("actions.delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id, Admin admin) {
        triggerRestService.delete(id, admin.getCompanyID());
    }

    @SuppressWarnings("unused")
    public boolean isExcludedFromXssCheck(String name, XssCheckLocation location, Admin admin) {
        if (location != XssCheckLocation.BODY_FIELD) {
            return false;
        }
        name = StringUtils.defaultString(name);
        return name.matches("operations\\[\\d+].script") || name.matches("operations\\[\\d+].htmlMail");
    }
}
