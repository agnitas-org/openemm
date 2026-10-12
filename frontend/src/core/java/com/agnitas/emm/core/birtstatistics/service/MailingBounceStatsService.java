/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.service;

import static org.apache.tomcat.jakartaee.commons.lang3.StringUtils.isBlank;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtstatistics.dto.EmailBounceDetail;
import com.agnitas.emm.core.birtstatistics.dto.StatisticMetric;
import com.agnitas.reporting.birt.external.beans.LightTarget;

public interface MailingBounceStatsService {

    record BounceStatsOpts(
            int mailingId,
            int companyId,
            String altgSql,
            List<LightTarget> targets,
            Locale locale
    ) {
        public String withAltg(String targetSql) {
            return targetSql + (isBlank(altgSql) ? "" : " AND (%s)".formatted(altgSql));
        }
    }

    record BounceStats(
            Map<Integer, Map<String, StatisticMetric>> softBounces,
            Map<Integer, Map<String, StatisticMetric>> hardBounces,
            Map<Integer, StatisticMetric> undelivered
    ) {}

    BounceStats getStats(int mailingId, List<Integer> targetIds, Admin admin);

    BounceStats getStats(int mailingId, int companyId, List<LightTarget> targets, Locale locale);

    List<EmailBounceDetail> getSoftBouncesWithDetailAndEmail(int mailingId, int companyId, List<Integer> targetIds);

    List<EmailBounceDetail> getHardBouncesWithDetailAndEmail(int mailingId, int companyId, List<Integer> targetIds);

}
