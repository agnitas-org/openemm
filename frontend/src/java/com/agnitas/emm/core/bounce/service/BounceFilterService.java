/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.bounce.service;

import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.bounce.dto.BounceFilterDto;
import com.agnitas.emm.core.bounce.form.BounceFilterListForm;
import org.agnitas.beans.impl.PaginatedListImpl;

public interface BounceFilterService {
    int saveBounceFilter(Admin admin, BounceFilterDto bounceFilter, boolean isNew) throws Exception;

    PaginatedListImpl<BounceFilterDto> getPaginatedBounceFilterList(Admin admin, String sort, String direction, int page, int rownums);

    PaginatedListImpl<BounceFilterDto> overview(BounceFilterListForm filter);

	int saveBounceFilter(int companyId, TimeZone adminTimeZone, BounceFilterDto bounceFilter, boolean isNew) throws Exception;

    BounceFilterDto getBounceFilter(int companyId, int filterId);

    boolean deleteBounceFilter(int filterId, int companyId);

    void delete(Set<Integer> ids, int companyId);

    void validateDeletion(Set<Integer> ids);

    boolean isMailingUsedInBounceFilterWithActiveAutoResponder(int companyId, int mailingId);
    
    List<BounceFilterDto> getDependentBounceFiltersWithActiveAutoResponder(int companyId, int mailingId);

    String getBounceFilterNames(List<BounceFilterDto> filters);

    List<String> getBounceFilterNames(Set<Integer> ids, int companyId);
}
