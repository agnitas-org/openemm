/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.recipient.service;

import java.util.Collections;
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
import com.agnitas.emm.core.recipient.exception.RecipientNotExistException;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.core.validator.ValidationException;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.recipient.dto.BulkDeleteRecipientsRequest;
import com.agnitas.emm.mcp.v1.recipient.dto.BulkDeleteRecipientsResponse;
import com.agnitas.emm.mcp.v1.recipient.dto.BulkRecipientSubscriptionRequest;
import com.agnitas.emm.mcp.v1.recipient.dto.BulkRecipientsRequest;
import com.agnitas.emm.mcp.v1.recipient.dto.BulkUpsertRecipientsResponse;
import com.agnitas.emm.mcp.v1.recipient.dto.CreateRecipientRequest;
import com.agnitas.emm.mcp.v1.recipient.dto.ListRecipientsRequest;
import com.agnitas.emm.mcp.v1.recipient.dto.RecipientDeleteResult;
import com.agnitas.emm.mcp.v1.recipient.dto.RecipientSubscriptionRequest;
import com.agnitas.emm.mcp.v1.recipient.dto.UpdateRecipientRequest;
import com.agnitas.emm.mcp.v1.recipient.exception.RecipientDeletionException;
import com.agnitas.emm.mcp.v1.recipient.mapper.RecipientsMcpMapper;
import org.springframework.stereotype.Service;

@Service
public class RecipientMcpServiceImpl implements RecipientMcpService {

    private final RecipientService recipientService;
    private final MailinglistService mailinglistService;
    private final ConfigService configService;
    private final RecipientsMcpMapper mapper;

    public RecipientMcpServiceImpl(
            RecipientService recipientService,
            MailinglistService mailinglistService,
            ConfigService configService,
            RecipientsMcpMapper mapper
    ) {
        this.recipientService = recipientService;
        this.mailinglistService = mailinglistService;
        this.configService = configService;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<Map<String, Object>> list(ListRecipientsRequest request, McpPageable pageable, Admin admin) {
        RecipientSearchParamsDto searchParams = mapper.toDto(orEmpty(request));
        PaginatedFilter<String> pagination = mapper.toPagination(pageable);
        PaginatedList<RecipientDto> list = recipientService.list(searchParams, pagination, admin);
        return mapper.toPageResponse(list, RecipientDto::getParameters);
    }

    @Override
    public List<Integer> listIds(ListRecipientsRequest request, Admin admin) {
        RecipientSearchParamsDto searchParams = mapper.toDto(orEmpty(request));
        return recipientService.listIds(searchParams, admin);
    }

    private static ListRecipientsRequest orEmpty(ListRecipientsRequest request) {
        return request != null ? request : new ListRecipientsRequest(null, null, null, null, null);
    }

    @Override
    public Map<String, Object> get(Integer recipientId, String email, Admin admin) {
        requireExactlyOneIdentifier(recipientId, email);
        return recipientId != null
                ? recipientService.getVisibleFieldValues(recipientId, admin)
                : recipientService.getVisibleFieldValues(email, admin);
    }

    @Override
    public Map<String, Object> create(CreateRecipientRequest request, Admin admin) {
        Subscription subscription = toSubscription(request.subscription(), admin);
        Recipient recipient = recipientService.create(new CreateRecipientCommand(request.fields(), admin, subscription));
        return recipientService.getVisibleFieldValues(recipient.getCustomerID(), admin);
    }

    @Override
    public Map<String, Object> update(Integer recipientId, String email, UpdateRecipientRequest request, Admin admin) {
        requireExactlyOneIdentifier(recipientId, email);
        if (recipientId == null) {
            recipientId = recipientService.getRecipientIdByEmail(email, admin);
        }
        Subscription subscription = toSubscription(request.subscription(), admin);
        recipientService.updatePartially(new UpdateRecipientCommand(recipientId, request.fields(), admin, subscription));
        return recipientService.getVisibleFieldValues(recipientId, admin);
    }

    @Override
    public void delete(Integer recipientId, String email, Admin admin) {
        requireExactlyOneIdentifier(recipientId, email);
        DeleteResult result = recipientId != null
                ? recipientService.delete(recipientId, admin)
                : recipientService.delete(email, admin);
        if (result.isDeleted()) {
            return;
        }
        if (result.isNotFound()) {
            throw recipientId != null
                    ? new RecipientNotExistException(recipientId.longValue())
                    : new RecipientNotExistException(email, admin.getCompanyID());
        }
        throw new RecipientDeletionException(result.errors());
    }

    @Override
    public BulkUpsertRecipientsResponse createBulk(BulkRecipientsRequest request, Admin admin) {
        ImportStatus status = recipientService.create(toBulkUpsertCommand(request, admin));
        return toBulkUpsertResponse(request.recipients().size(), status);
    }

    @Override
    public BulkUpsertRecipientsResponse addOrUpdateBulk(BulkRecipientsRequest request, Admin admin) {
        ImportStatus status = recipientService.addOrUpdate(toBulkUpsertCommand(request, admin));
        return toBulkUpsertResponse(request.recipients().size(), status);
    }

    @Override
    public BulkDeleteRecipientsResponse deleteBulk(BulkDeleteRecipientsRequest request, Admin admin) {
        List<DeleteResult> results = recipientService.delete(new LinkedHashSet<>(request.ids()), admin);

        List<RecipientDeleteResult> mappedResults = results.stream()
                .map(result -> new RecipientDeleteResult(result.id(), result.status().name()))
                .toList();

        long succeeded = results.stream()
                .filter(DeleteResult::isDeleted)
                .count();

        return new BulkDeleteRecipientsResponse(results.size(), succeeded, results.size() - succeeded, mappedResults);
    }

    private BulkUpsertRecipientCommand toBulkUpsertCommand(BulkRecipientsRequest request, Admin admin) {
        return new BulkUpsertRecipientCommand(
                request.recipients(),
                admin,
                toBulkSubscription(request.subscription(), admin)
        );
    }

    private static BulkUpsertRecipientsResponse toBulkUpsertResponse(int total, ImportStatus status) {
        Map<String, Integer> errors = status.getErrors()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().name(),
                        Map.Entry::getValue
                ));

        int errorsCount = status.getErrors()
                .values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();

        return new BulkUpsertRecipientsResponse(total, total - errorsCount, errorsCount, errors);
    }

    private Subscription toBulkSubscription(BulkRecipientSubscriptionRequest request, Admin admin) {
        if (request == null) {
            return new Subscription(false, Collections.emptyList(), null, null, "Set by %s via MCP".formatted(admin.getUsername()));
        }

        boolean forAllMailinglists = Boolean.TRUE.equals(request.subscribeToAllMailinglists());
        List<Integer> mailinglistIds;
        if (forAllMailinglists) {
            mailinglistIds = mailinglistService.getMailinglistIds(admin.getCompanyID());
        } else if (request.mailinglistId() != null) {
            mailinglistIds = List.of(request.mailinglistId());
        } else {
            mailinglistIds = List.of();
        }

        return new Subscription(
                forAllMailinglists,
                mailinglistIds,
                request.mediaType(),
                null,
                "Set by %s via MCP".formatted(admin.getUsername())
        );
    }

    private Subscription toSubscription(RecipientSubscriptionRequest request, Admin admin) {
        if (request == null) {
            return null;
        }

        boolean forAllMailinglists = Boolean.TRUE.equals(request.subscribeToAllMailinglists());
        List<Integer> mailinglistIds;
        if (forAllMailinglists) {
            mailinglistIds = mailinglistService.getMailinglistIds(admin.getCompanyID());
        } else if (request.mailinglistId() != null) {
            mailinglistIds = List.of(request.mailinglistId());
        } else {
            mailinglistIds = List.of();
        }

        return new Subscription(
                forAllMailinglists,
                mailinglistIds,
                request.mediaType(),
                request.status(),
                "Set by %s via MCP".formatted(admin.getUsername())
        );
    }

    @Override
    public boolean isAllowedToUseHtml(String columnName, Admin admin) {
        if (recipientService.getEditableColumns(admin).get(columnName) != null) {
            return configService.allowHtmlInReferenceAndProfileFields(admin.getCompanyID());
        }
        return true;
    }

    private static PageResponse<Map<String, Object>> toPageResponse(List<Map<String, Object>> fullList, McpPageable pageable) {
        int totalCount = fullList.size();
        int start = Math.min((pageable.page() - 1) * pageable.size(), totalCount);
        int end = Math.min(start + pageable.size(), totalCount);

        return new PageResponse<>(
                fullList.subList(start, end),
                new PageResponse.PageMeta(pageable.page(), pageable.size(), totalCount)
        );
    }

    private void requireExactlyOneIdentifier(Integer recipientId, String email) {
        if ((recipientId == null) == (email == null)) {
            throw ValidationException.exact("Exactly one of recipientId or email must be provided");
        }
    }
}
