/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.trackablelinks.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.dao.TrackableLinkStatisticsDao;
import com.agnitas.dao.TrackableLinkStatisticsDao.ClickFilter;
import com.agnitas.dao.TrackableLinkStatisticsDao.LinkClickStatisticData;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkClickStatsService;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class TrackableLinkClickStatsServiceImpl implements TrackableLinkClickStatsService {

    private final TrackableLinkStatisticsDao trackableLinkStatisticsDao;

    public TrackableLinkClickStatsServiceImpl(TrackableLinkStatisticsDao trackableLinkStatisticsDao) {
        this.trackableLinkStatisticsDao = trackableLinkStatisticsDao;
    }

    @Override
    public Map<Integer, TargetClickStats> getClickCountsByTarget(
            int mailingId, int companyId, List<LightTarget> targets, DateRange dateRange
    ) {
        List<ClickFilter> filters = new ArrayList<>();
        for (LightTarget target : targets) {
            for (ShareType share : ShareType.values()) {
                filters.add(new ClickFilter(
                        filterKey(target.getId(), share),
                        combineConditions(target.getTargetSQL(), share.sqlCondition)
                ));
            }
        }

        Map<String, Map<Integer, LinkClickStatisticData>> statsByFilterKey = trackableLinkStatisticsDao.getClickStatisticsPerLink(
                mailingId, companyId, dateRange, filters
        );

        Map<Integer, TargetClickStats> result = new HashMap<>();
        for (LightTarget target : targets) {
            Map<ShareType, Map<Integer, ClickCounts>> countsByShare = new EnumMap<>(ShareType.class);
            for (ShareType share : ShareType.values()) {
                countsByShare.put(share, toClickCounts(statsByFilterKey.get(filterKey(target.getId(), share))));
            }
            result.put(target.getId(), new TargetClickStats(countsByShare));
        }

        return result;
    }

    private static Map<Integer, ClickCounts> toClickCounts(Map<Integer, LinkClickStatisticData> statsByLinkId) {
        if (statsByLinkId == null) {
            return Map.of();
        }

        return statsByLinkId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new ClickCounts(e.getValue().clicksCount(), e.getValue().clickersCount())
                ));
    }

    private static String filterKey(int targetId, ShareType share) {
        return targetId + "_" + share.name();
    }

    private static String combineConditions(String... conditions) {
        return Arrays.stream(conditions)
                .filter(StringUtils::isNotBlank)
                .map(condition -> "(" + condition + ")")
                .collect(Collectors.joining(" AND "));
    }

}
