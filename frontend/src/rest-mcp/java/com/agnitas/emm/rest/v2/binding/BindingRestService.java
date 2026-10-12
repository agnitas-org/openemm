/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.binding;

import com.agnitas.emm.core.binding.BindingIdentifier;
import com.agnitas.beans.Admin;
import com.agnitas.emm.rest.v2.infrastructure.bulk.BulkDeleteResult;
import com.agnitas.emm.rest.v2.infrastructure.bulk.BulkOperationResponse;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import org.springframework.data.domain.Pageable;

public interface BindingRestService {

    PageResponse<BindingResponse> find(ListBindingsRequest request, Pageable pageable, int companyId);

    BindingResponse find(BindingIdentifier request, int companyId);

    BindingResponse create(CreateBindingRequest request, int companyId);

    void delete(BindingIdentifier identifier, int companyId);

    BulkOperationResponse<BulkDeleteResult> delete(BulkDeleteBindingRequest request, int companyId);

    BindingResponse update(BindingIdentifier identifier, PatchBindingRequest request, int companyId);

    BulkUpsertBindingResponse upsert(BulkUpsertBindingRequest request, Admin admin);
}
