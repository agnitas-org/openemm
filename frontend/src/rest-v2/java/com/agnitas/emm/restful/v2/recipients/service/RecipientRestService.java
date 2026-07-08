/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.recipients.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.restful.v2.infrastructure.bulk.BulkDeleteResult;
import com.agnitas.emm.restful.v2.infrastructure.search.dto.PageForm;
import com.agnitas.emm.restful.v2.recipients.dto.BulkUpsertResponse;
import com.agnitas.emm.restful.v2.recipients.dto.RecipientSubscribeDto;
import com.agnitas.service.SimpleServiceResult;
import jakarta.validation.Valid;

public interface RecipientRestService {

    Map<String, Object> getById(int recipientId, Admin admin);

    SimpleServiceResult delete(int id, Admin admin);

    List<BulkDeleteResult> delete(List<Integer> ids, Admin admin);

    Map<String, Object> create(
        Map<String, Object> fields,
        RecipientSubscribeDto recipientSubscribeDto,
        Admin admin
    );

    Map<String, Object> updatePartially(
        int id,
        Map<String, Object> fields,
        RecipientSubscribeDto subscribeDto,
        Admin admin
    );

    boolean isAllowedToUseHtml(String columnName, Admin admin);

    BulkUpsertResponse create(
            List<Map<String, Object>> recipients,
            RecipientSubscribeDto subscribeDto,
            Admin admin
    ) throws IOException;

    BulkUpsertResponse insertOrUpdatePartially(
            List<Map<String, Object>> recipients,
            RecipientSubscribeDto subscribeDto,
            Admin admin
    ) throws IOException;

    PaginatedList<Map<String, Object>> list(@Valid PageForm pageReq, Map<String, String> fieldFilters, Admin admin);
}
