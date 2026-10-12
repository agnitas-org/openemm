/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.forms;

import com.agnitas.beans.Admin;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.emm.rest.v2.infrastructure.security.auth.PermissionAllowed;
import com.agnitas.emm.rest.v2.infrastructure.security.xss.XssCheckLocation;
import com.agnitas.emm.rest.v2.infrastructure.security.xss.XssExclude;
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
@RequestMapping("/forms")
public class FormsV2Controller {

    private final FormRestService formRestService;

    public FormsV2Controller(FormRestService formRestService) {
        this.formRestService = formRestService;
    }

    @GetMapping
    @PermissionAllowed("forms.show")
    public PageResponse<FormLightResponse> listForms(Pageable pageable, Admin admin) {
        return formRestService.list(pageable, admin.getCompanyID());
    }

    @GetMapping("/{formId:\\d+}")
    @PermissionAllowed("forms.show")
    public FormResponse getForm(@PathVariable @Positive int formId, Admin admin) {
        return formRestService.find(formId, admin.getCompanyID());
    }

    @PostMapping
    @PermissionAllowed("forms.change")
    @XssExclude(checkMethod = "isExcludedFromXssCheck")
    @ResponseStatus(HttpStatus.CREATED)
    public FormResponse createForm(@RequestBody @Valid CreateFormRequest request, Admin admin) {
        return formRestService.create(request, admin);
    }

    @PatchMapping("/{formId:\\d+}")
    @PermissionAllowed("forms.change")
    @XssExclude(checkMethod = "isExcludedFromXssCheck")
    public FormResponse patchForm(
            @PathVariable @Positive int formId,
            @RequestBody @Valid PatchFormRequest request,
            Admin admin
    ) {
        return formRestService.patch(formId, request, admin);
    }

    @DeleteMapping("/{formId:\\d+}")
    @PermissionAllowed("forms.delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteForm(@PathVariable @Positive int formId, Admin admin) {
        formRestService.delete(formId, admin.getCompanyID());
    }

    @PostMapping("/{id:\\d+}/activate")
    @PermissionAllowed("forms.change")
    public FormResponse activate(@PathVariable @Positive int id, Admin admin) {
        return formRestService.activate(id, admin);
    }

    @PostMapping("/{id:\\d+}/deactivate")
    @PermissionAllowed("forms.change")
    public FormResponse deactivate(@PathVariable @Positive int id, Admin admin) {
        return formRestService.deactivate(id, admin);
    }

    @SuppressWarnings("unused")
    public boolean isExcludedFromXssCheck(String name, XssCheckLocation location, Admin admin) {
        if (location != XssCheckLocation.BODY_FIELD) {
            return false;
        }
        return "successSettings.template".equals(name) || "errorSettings.template".equals(name);
    }
}
