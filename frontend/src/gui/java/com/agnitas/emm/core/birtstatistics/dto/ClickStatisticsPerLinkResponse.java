/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.dto;

import java.util.List;

import com.agnitas.emm.core.birtstatistics.enums.StatisticLinkCategory;
import com.agnitas.reporting.birt.external.beans.StatisticMetric;

public record ClickStatisticsPerLinkResponse(SummaryBlock summary, List<LinkCategoryBlock> categories) {

    public record SummaryBlock(List<SummaryCategoryBlock> categories) {

    }

    public record SummaryCategoryBlock(StatisticLinkCategory category, List<TargetGroupStats> targetGroups) {
    }

    public record LinkCategoryBlock(StatisticLinkCategory category, List<LinkStats> links) {

    }

    public record LinkStats(int id, String url, List<TargetGroupStats> targetGroups) {

    }

    public record TargetGroupStats(Integer id, ShareStats total, ShareStats mobile, ShareStats anonymous) {

    }

    public record ShareStats(StatisticMetric clicks, StatisticMetric clickers) {

    }

}
