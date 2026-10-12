/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.mapper;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatisticForm;
import com.agnitas.emm.core.birtstatistics.service.MailingTopDomainsStatsService.TopDomainsStatsQuery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MailingStatsUiMapper {

    @Mapping(target = "locale", source = "admin.locale")
    @Mapping(target = "companyId", source = "admin.companyID")
    @Mapping(target = "mailingId", source = "mailingId")
    @Mapping(target = "limit", source = "form.maxDomains")
    @Mapping(target = "isTopLevel", source = "form.topLevelDomain")
    TopDomainsStatsQuery toTopDomainsQuery(
            int mailingId,
            Admin admin,
            MailingStatisticForm form
    );
}
