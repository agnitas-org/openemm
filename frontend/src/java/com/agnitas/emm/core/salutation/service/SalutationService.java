/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.salutation.service;

import java.util.List;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Title;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.salutation.form.SalutationOverviewFilter;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.ServiceResult;

public interface SalutationService {

    PaginatedList<Title> overview(SalutationOverviewFilter filter);

    List<Title> getAll(int companyId, boolean includeGenders);

    Title get(int salutationId, int companyId);

    void save(Title title);

    ServiceResult<List<Title>> getAllowedForDeletion(Set<Integer> ids, int companyId);

    ServiceResult<UserAction> bulkDelete(Set<Integer> ids, int companyId);

    String resolve(int salutationId, int recipientId, int type, Admin admin);

}
