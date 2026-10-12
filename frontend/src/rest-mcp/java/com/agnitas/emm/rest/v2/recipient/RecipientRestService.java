/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.recipient;

import java.util.List;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.emm.rest.v2.infrastructure.bulk.BulkDeleteResult;
import com.agnitas.emm.rest.v2.infrastructure.bulk.BulkOperationResponse;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.emm.rest.v2.recipient.dto.BulkUpsertResponse;
import com.agnitas.emm.rest.v2.recipient.dto.RecipientSubscribeRequest;
import org.springframework.data.domain.Pageable;

public interface RecipientRestService {

    Map<String, Object> getById(int recipientId, Admin admin);

    void delete(int id, Admin admin);

    BulkOperationResponse<BulkDeleteResult> delete(List<Integer> ids, Admin admin);

    Map<String, Object> create(
        Map<String, Object> fields,
        RecipientSubscribeRequest subscribeRequest,
        Admin admin
    );

    Map<String, Object> updatePartially(
        int id,
        Map<String, Object> fields,
        RecipientSubscribeRequest subscribeRequest,
        Admin admin
    );

    boolean isAllowedToUseHtml(String columnName, Admin admin);

    BulkUpsertResponse create(
            List<Map<String, Object>> recipients,
            RecipientSubscribeRequest subscribeRequest,
            Admin admin
    );

    BulkUpsertResponse addOrUpdatePartially(
            List<Map<String, Object>> recipients,
            RecipientSubscribeRequest subscribeRequest,
            Admin admin
    );

    PageResponse<Map<String, Object>> list(Pageable pageReq, Map<String, String> fieldFilters, Admin admin);
}
