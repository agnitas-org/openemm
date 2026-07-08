/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.service;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.emm.core.birtstatistics.dto.EmailBounceDetail;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.StatisticMetric;

public interface MailingBounceStatsService {

    record BounceStatsRequest(
            int mailingId,
            int companyId,
            String altgSql,
            List<LightTarget> targets,
            Locale locale
    ) {
        public String withAltg(String targetSql) {
            return targetSql + (isBlank(altgSql) ? "" : " AND (%s)".formatted(altgSql));
        }

        public String getTargetsSqlCombinedWithOr() {
            return targets.stream().map(LightTarget::getTargetSQL).collect(Collectors.joining(" OR "));
        }
    }

    record BounceStats(
            Map<Integer, Map<String, StatisticMetric>> softBounces,
            Map<Integer, Map<String, StatisticMetric>> hardBounces,
            Map<Integer, StatisticMetric> undelivered
    ) {}

    BounceStats getStats(BounceStatsRequest req);

    List<EmailBounceDetail> getSoftBouncesWithDetailAndEmail(int mailingId, int companyId, List<Integer> targetIds);

    List<EmailBounceDetail> getHardBouncesWithDetailAndEmail(int mailingId, int companyId, List<Integer> targetIds);

}
