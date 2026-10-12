/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.trackablelinks.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.reporting.birt.external.beans.LightTarget;

public interface TrackableLinkClickStatsService {

    enum ShareType {
        TOTAL("", "report.total"),
        MOBILE("rlog.device_class_id = " + DeviceClass.MOBILE.getId(), "report.mobile"),
        ANONYMOUS("rlog.customer_id = 0", "statistics.share.anonymous");

        public final String sqlCondition;
        public final String messageKey;

        ShareType(String sqlCondition, String messageKey) {
            this.sqlCondition = sqlCondition;
            this.messageKey = messageKey;
        }
    }

    record ClickCounts(long clicks, long clickers) {
        public static final ClickCounts EMPTY = new ClickCounts(0, 0);
    }

    record TargetClickStats(Map<ShareType, Map<Integer, ClickCounts>> countsByShareAndLink) {

        public ClickCounts forLink(ShareType share, int linkId) {
            return countsByShareAndLink.getOrDefault(share, Map.of()).getOrDefault(linkId, ClickCounts.EMPTY);
        }

        public ClickCounts aggregate(ShareType share, Collection<Integer> linkIds) {
            Map<Integer, ClickCounts> countsByLink = countsByShareAndLink.getOrDefault(share, Map.of());

            long clicks = 0;
            long clickers = 0;
            for (Integer linkId : linkIds) {
                ClickCounts counts = countsByLink.getOrDefault(linkId, ClickCounts.EMPTY);
                clicks += counts.clicks();
                clickers += counts.clickers();
            }

            return new ClickCounts(clicks, clickers);
        }
    }

    Map<Integer, TargetClickStats> getClickCountsByTarget(
            int mailingId,
            int companyId,
            List<LightTarget> targets,
            DateRange dateRange
    );

}
