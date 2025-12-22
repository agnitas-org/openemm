/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.bounce.service;

import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.bounce.dto.BounceFilterDto;
import com.agnitas.emm.core.bounce.form.BounceFilterListForm;
import com.agnitas.emm.core.bounce.service.impl.BlacklistedFilterEmailException;
import com.agnitas.emm.core.bounce.service.impl.BlacklistedForwardEmailException;
import com.agnitas.emm.core.bounce.service.impl.EmailInUseException;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.ServiceResult;

public interface BounceFilterService {

    int saveBounceFilter(Admin admin, BounceFilterDto bounceFilter, boolean isNew)
            throws BlacklistedFilterEmailException, EmailInUseException, BlacklistedForwardEmailException;

    PaginatedList<BounceFilterDto> overview(BounceFilterListForm filter);

	int saveBounceFilter(int companyId, TimeZone adminTimeZone, BounceFilterDto bounceFilter, boolean isNew)
            throws BlacklistedFilterEmailException, EmailInUseException, BlacklistedForwardEmailException;

    BounceFilterDto getBounceFilter(int companyId, int filterId);

    ServiceResult<UserAction> delete(Set<Integer> ids, int companyId);

    boolean isMailingUsedInBounceFilterWithActiveAutoResponder(int companyId, int mailingId);
    
    List<BounceFilterDto> getDependentBounceFiltersWithActiveAutoResponder(int companyId, int mailingId);

    String getBounceFilterNames(List<BounceFilterDto> filters);

    ServiceResult<List<BounceFilterDto>> getAllowedForDeletion(Set<Integer> ids, int companyId);

    boolean containsReply(int filterId);
}
