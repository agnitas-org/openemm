/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.enums;

public enum StatisticType {
    SUMMARY("mailing_summary.rptdesign"),
    CLICK_STATISTICS_PER_LINK("mailing_linkclicks.rptdesign"),
    PROGRESS_OF_DELIVERY("mailing_delivery_progress.rptdesign"),
    PROGRESS_OF_OPENINGS("mailing_net_and_gross_openings_progress.rptdesign"),
    PROGRESS_OF_CLICKS("mailing_linkclicks_progress.rptdesign"),
    TOP_DOMAINS("top_domains.rptdesign"),
    BOUNCES("mailing_bounces.rptdesign");

    private final String code;

    StatisticType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static StatisticType getByCode(final String code){
        for (StatisticType type: StatisticType.values()) {
            if(type.getCode().equals(code)){
                return type;
            }
        }
        return null;
    }
}
