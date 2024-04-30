/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.enums;

import com.agnitas.emm.core.birtstatistics.DateMode;

public enum StatisticType {
    
    SUMMARY(DateMode.SELECT_MONTH),
    SUMMARY_AUTO_OPT(DateMode.NONE),
    CLICK_STATISTICS_PER_LINK,
    PROGRESS_OF_DELIVERY(DateMode.LAST_TENHOURS),
    PROGRESS_OF_OPENINGS(DateMode.LAST_TENHOURS),
    PROGRESS_OF_CLICKS(DateMode.LAST_TENHOURS),
    PROGRESS_OF_SINGLE_LINK_CLICKS(DateMode.LAST_TENHOURS),
    TOP_DOMAINS,
    BOUNCES,
    BENCHMARK,
    DEVICES_OVERVIEW,
    TRACKING_POINT_WEEK_OVERVIEW(DateMode.LAST_TENHOURS),
    SOCIAL_NETWORKS,
    SIMPLE_TRACKING_POINT,
    NUM_TRACKING_POINT_WEEK_OVERVIEW(DateMode.LAST_TENHOURS),
    ALPHA_TRACKING_POINT;

    private final DateMode dateMode;

    StatisticType() {
        this(DateMode.NONE);
    }

    StatisticType(DateMode dateMode) {
        this.dateMode = dateMode;
    }

    public DateMode getDateMode() {
        return dateMode;
    }
}
