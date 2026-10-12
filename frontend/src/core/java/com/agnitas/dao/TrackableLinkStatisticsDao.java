/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.dao;

import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.commons.dto.DateRange;

public interface TrackableLinkStatisticsDao {

    /**
     * One pivoted click/clicker column-group, keyed by an opaque caller-chosen {@code key} used to
     * look the corresponding result back up in the returned map. {@code sqlCondition} is ANDed into
     * a {@code CASE WHEN} for that column-group; blank means "no filter" (unconditional count).
     */
    record ClickFilter(String key, String sqlCondition) {
    }

    record LinkClickStatisticData(int id, int clicksCount, int clickersCount) {
    }

    Map<String, Map<Integer, LinkClickStatisticData>> getClickStatisticsPerLink(
            int mailingId,
            int companyId,
            DateRange timestamp,
            List<ClickFilter> filters
    );

}
