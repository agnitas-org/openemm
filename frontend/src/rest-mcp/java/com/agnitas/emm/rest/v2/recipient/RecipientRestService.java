/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.recipient;

import static java.util.Collections.emptyList;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ImportStatus;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.Recipient;
import com.agnitas.emm.core.commons.pagination.PaginatedFilter;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.validation.DeleteResult;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.recipient.dto.BulkUpsertRecipientCommand;
import com.agnitas.emm.core.recipient.dto.CreateRecipientCommand;
import com.agnitas.emm.core.recipient.dto.RecipientDto;
import com.agnitas.emm.core.recipient.dto.RecipientSearchParamsDto;
import com.agnitas.emm.core.recipient.dto.Subscription;
import com.agnitas.emm.core.recipient.dto.UpdateRecipientCommand;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.rest.v2.infrastructure.bulk.BulkDeleteResult;
import com.agnitas.emm.rest.v2.infrastructure.bulk.BulkOperationResponse;
import com.agnitas.emm.rest.v2.infrastructure.exception.HttpStatusException;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.util.AgnUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
class RecipientRestService {

    private final RecipientService recipientService;
    private final ConfigService configService;
    private final MailinglistService mailinglistService;
    private final RecipientsRestMapper mapper;

    public RecipientRestService(
            RecipientService recipientService,
            ConfigService configService,
            MailinglistService mailinglistService,
            RecipientsRestMapper mapper
    ) {
        this.recipientService = recipientService;
        this.configService = configService;
        this.mailinglistService = mailinglistService;
        this.mapper = mapper;
    }

    public Map<String, Object> find(String email, Admin admin) {
        return recipientService.getVisibleFieldValues(email, admin);
    }

    public Map<String, Object> find(int recipientId, Admin admin) {
        return recipientService.getVisibleFieldValues(recipientId, admin);
    }

    public void delete(int id, Admin admin) {
        DeleteResult result = recipientService.delete(id, admin);
        HttpStatusException.throwIfFailed(result);
    }

    public void delete(String email, Admin admin) {
        DeleteResult result = recipientService.delete(email, admin);
        HttpStatusException.throwIfFailed(result);
    }

    public BulkOperationResponse<BulkDeleteResult> delete(List<Integer> ids, Admin admin) {
        List<DeleteResult> results = recipientService.delete(new LinkedHashSet<>(ids), admin);
        return BulkOperationResponse.fromDeleteResult(results);
    }

    public Map<String, Object> create(
            Map<String, Object> fields,
            RecipientSubscribeRequest subscribeRequest,
            Admin admin
    ) {
        Subscription subscription = toSubscription(subscribeRequest, admin, true);
        Recipient recipient = recipientService.create(new CreateRecipientCommand(fields, admin, subscription));
        return recipientService.getVisibleFieldValues(recipient.getCustomerID(), admin);
    }

    public Map<String, Object> updatePartially(
            String email,
            Map<String, Object> fields,
            RecipientSubscribeRequest subscribeRequest,
            Admin admin
    ) {
        int id = recipientService.getRecipientIdByEmail(email, admin);
        return updatePartially(id, fields, subscribeRequest, admin);
    }

    public Map<String, Object> updatePartially(
            int id,
            Map<String, Object> fields,
            RecipientSubscribeRequest subscribeRequest,
            Admin admin
    ) {
        Subscription subscription = toSubscription(subscribeRequest, admin, true);
        recipientService.updatePartially(new UpdateRecipientCommand(id, fields, admin, subscription));
        return recipientService.getVisibleFieldValues(id, admin);
    }

    public boolean isAllowedToUseHtml(String columnName, Admin admin) {
        if (recipientService.getEditableColumns(admin).get(columnName) != null) {
            return configService.allowHtmlInReferenceAndProfileFields(admin.getCompanyID());
        }
        return true;
    }

    public BulkUpsertResponse create(
            List<Map<String, Object>> recipients,
            RecipientSubscribeRequest subscribeRequest,
            Admin admin
    ) {
        return getBulkUpsertResponse(recipients, recipientService.create(new BulkUpsertRecipientCommand(
                recipients,
                admin,
                toSubscription(subscribeRequest, admin, false)
        )));
    }

    private static BulkUpsertResponse getBulkUpsertResponse(List<Map<String, Object>> recipients, ImportStatus status) {
        Map<String, Integer> errors = status.getErrors().entrySet().stream().collect(Collectors.toMap(
                s -> s.getKey().name(),
                Map.Entry::getValue
        ));
        int errorsCount = status.getErrors().values().stream().mapToInt(i -> i).sum();
        return new BulkUpsertResponse(recipients.size(), recipients.size() - errorsCount, errorsCount, errors);
    }

    public BulkUpsertResponse addOrUpdatePartially(
            List<Map<String, Object>> recipients,
            RecipientSubscribeRequest subscribeRequest,
            Admin admin
    ) {
        return getBulkUpsertResponse(recipients, recipientService.addOrUpdate(new BulkUpsertRecipientCommand(
                recipients,
                admin,
                toSubscription(subscribeRequest, admin, false)
        )));
    }

    public PageResponse<Map<String, Object>> list(ListRecipientsRequest request, Pageable pageable, Admin admin) {
        RecipientSearchParamsDto searchParams = mapper.toDto(request);
        PaginatedFilter<String> pagination = mapper.toPagination(pageable);
        PaginatedList<RecipientDto> list = recipientService.list(searchParams, pagination, admin);
        return mapper.toPageResponse(list, RecipientDto::getParameters);
    }

    public List<Integer> listIds(ListRecipientsRequest request, Admin admin) {
        RecipientSearchParamsDto searchParams = mapper.toDto(request);
        return recipientService.listIds(searchParams, admin);
    }

    private List<Integer> detectMailinglistsToSubscribe(RecipientSubscribeRequest request, Admin admin) {
        if (request.subscribeForAllMailinglists()) {
                return mailinglistService.getMailinglistIds(admin.getCompanyID());
        }
        if (AgnUtils.isNumber(request.mailinglist())) {
            return List.of(Integer.parseInt(request.mailinglist()));
        }
        return emptyList();
    }

    private Subscription toSubscription(RecipientSubscribeRequest request, Admin admin, boolean includeStatus) {
        return new Subscription(
                request.subscribeForAllMailinglists(),
                detectMailinglistsToSubscribe(request, admin),
                request.mediaType(),
                includeStatus ? request.status() : null,
                "Set by %s via restful".formatted(admin.getUsername())
        );
    }
}
