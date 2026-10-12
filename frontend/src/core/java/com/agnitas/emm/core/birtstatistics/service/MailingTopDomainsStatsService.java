/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.agnitas.emm.core.birtstatistics.dto.StatisticMetric;

public interface MailingTopDomainsStatsService {

    record TopDomainsStatsQuery(
            int mailingId,
            int companyId,
            int limit,
            boolean isTopLevel,
            Locale locale,
            List<Integer> targetIds
    ) {
    }

    record TopDomainStats(
            Map<Integer, Map<String, StatisticMetric>> sentEmails,
            Map<Integer, Map<String, StatisticMetric>> hardBounces,
            Map<Integer, Map<String, StatisticMetric>> softBounces,
            Map<Integer, Map<String, StatisticMetric>> openers,
            Map<Integer, Map<String, StatisticMetric>> clickers
    ) {}

    TopDomainStats getStats(TopDomainsStatsQuery req);
}
