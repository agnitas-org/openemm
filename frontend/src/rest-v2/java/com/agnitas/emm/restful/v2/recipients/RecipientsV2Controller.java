/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.recipients;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.restful.v2.infrastructure.bulk.BulkDeleteRequest;
import com.agnitas.emm.restful.v2.infrastructure.bulk.BulkDeleteResult;
import com.agnitas.emm.restful.v2.infrastructure.bulk.BulkOperationResponse;
import com.agnitas.emm.restful.v2.infrastructure.search.dto.PageForm;
import com.agnitas.emm.restful.v2.infrastructure.security.auth.PermissionAllowed;
import com.agnitas.emm.restful.v2.infrastructure.security.xss.XssCheckLocation;
import com.agnitas.emm.restful.v2.infrastructure.security.xss.XssExclude;
import com.agnitas.emm.restful.v2.recipients.dto.BulkUpsertResponse;
import com.agnitas.emm.restful.v2.recipients.dto.Groups;
import com.agnitas.emm.restful.v2.recipients.dto.RecipientSubscribeDto;
import com.agnitas.emm.restful.v2.recipients.service.RecipientRestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.groups.Default;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/recipients")
public class RecipientsV2Controller {

    private final RecipientRestService recipientRestService;

    public RecipientsV2Controller(RecipientRestService recipientRestService) {
        this.recipientRestService = recipientRestService;
    }

    @GetMapping
    @PermissionAllowed("recipient.show")
    public PaginatedList<Map<String, Object>> list(
            @Valid PageForm pageReq,
            @RequestParam Map<String, String> allParams,
            Admin admin
    ) {
        Set<String> dtoFields = Arrays.stream(pageReq.getClass().getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
        Map<String, String> fieldFilters = allParams.entrySet().stream()
                .filter(entry -> !dtoFields.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return recipientRestService.list(pageReq, fieldFilters, admin);
    }

    @GetMapping("/{id:\\d+}")
    @PermissionAllowed("recipient.show")
    public Map<String, Object> getById(@PathVariable @Positive int id, Admin admin) {
        return recipientRestService.getById(id, admin);
    }

    @PostMapping
    @XssExclude(checkMethod = "isExcludedFromXssCheck")
    @PermissionAllowed("recipient.create")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(
        @RequestBody Map<String, Object> fields,
        @Validated({ Groups.Single.class, Default.class }) RecipientSubscribeDto subscribeDto,
        Admin admin
    ) {
        return recipientRestService.create(fields, subscribeDto, admin);
    }

    @PostMapping("/bulk")
    @XssExclude(checkMethod = "isExcludedFromXssCheck")
    @PermissionAllowed("recipient.create;import.mode.add")
    public BulkUpsertResponse bulkCreate(
        @RequestBody List<Map<String, Object>> recipients,
        @Validated(Groups.Bulk.class) RecipientSubscribeDto subscribeDto,
        Admin admin
    ) throws IOException {
        return recipientRestService.create(recipients, subscribeDto, admin);
    }

    @PatchMapping("/{id}")
    @XssExclude(checkMethod = "isExcludedFromXssCheck")
    @PermissionAllowed("recipient.change")
    public Map<String, Object> updatePartially(
        @PathVariable int id,
        @RequestBody Map<String, Object> fields,
        @Valid RecipientSubscribeDto subscribeDto,
        Admin admin
    ) {
        return recipientRestService.updatePartially(id, fields, subscribeDto, admin);
    }

    @PatchMapping("/bulk")
    @XssExclude(checkMethod = "isExcludedFromXssCheck")
    @PermissionAllowed("recipient.change;import.mode.add_update")
    public BulkUpsertResponse bulkInsertOrUpdatePartially(
            @RequestBody List<Map<String, Object>> recipients,
            @Validated(Groups.Bulk.class) RecipientSubscribeDto subscribeDto,
            Admin admin
    ) throws IOException {
        return recipientRestService.insertOrUpdatePartially(recipients, subscribeDto, admin);
    }

    @DeleteMapping("/{id}")
    @PermissionAllowed("recipient.delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id, Admin admin) {
        recipientRestService.delete(id, admin);
    }

    @DeleteMapping("/bulk")
    @PermissionAllowed("recipient.delete")
    public BulkOperationResponse<BulkDeleteResult> bulkDelete(
            @RequestBody @Valid BulkDeleteRequest request,
            Admin admin
    ) {
        return BulkOperationResponse.from(recipientRestService.delete(new ArrayList<>(request.ids()), admin));
    }

    @SuppressWarnings("unused")
    public boolean isExcludedFromXssCheck(String name, XssCheckLocation location, Admin admin) {
        if (location != XssCheckLocation.BODY_FIELD) {
            return false;
        }
        return recipientRestService.isAllowedToUseHtml(name, admin);
    }
}
