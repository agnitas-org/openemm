/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.bounce.service;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.bounce.dto.BounceFilterDto;
import com.agnitas.emm.core.bounce.form.BounceFilterListForm;
import com.agnitas.service.ServiceResult;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;

import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public interface BounceFilterService {

    int saveBounceFilter(Admin admin, BounceFilterDto bounceFilter, boolean isNew) throws Exception;

    // TODO: EMMGUI-714: Remove when deleting old design
    PaginatedListImpl<BounceFilterDto> getPaginatedBounceFilterList(Admin admin, String sort, String direction, int page, int rownums);

    PaginatedListImpl<BounceFilterDto> overview(BounceFilterListForm filter);

	int saveBounceFilter(int companyId, TimeZone adminTimeZone, BounceFilterDto bounceFilter, boolean isNew) throws Exception;

    BounceFilterDto getBounceFilter(int companyId, int filterId);

    // TODO: EMMGUI-714: Check usages and remove when removing old design
    boolean deleteBounceFilter(int filterId, int companyId);

    ServiceResult<UserAction> delete(Set<Integer> ids, int companyId);

    boolean isMailingUsedInBounceFilterWithActiveAutoResponder(int companyId, int mailingId);
    
    List<BounceFilterDto> getDependentBounceFiltersWithActiveAutoResponder(int companyId, int mailingId);

    String getBounceFilterNames(List<BounceFilterDto> filters);

    ServiceResult<List<BounceFilterDto>> getAllowedForDeletion(Set<Integer> ids, int companyId);

    boolean containsReply(int filterId);
}
