/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtreport.dto;

import java.time.Instant;
import java.util.List;

import com.agnitas.emm.core.birtreport.bean.ReportFormat;
import jakarta.validation.constraints.NotNull;

public record TopDomainsReportRequest(
        List<Integer> targetGroups,
        List<Integer> mailingLists,
        Instant startDate,
        Instant stopDate,
        String figures,
        int maxDomains,
        boolean topLevelDomain,
        @NotNull ReportFormat format,
        boolean darkmode,
        String language,
        String reportName
) implements StatisticsReportRequest {
}
