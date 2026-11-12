/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtreport.dto;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public enum ReportSettingsType {

    COMPARISON(1, "mailing.comparison", false, false),
    MAILING(2, "mailing.statistics", false, false),
    RECIPIENT(3, "statistic.Recipient", false, true),
    TOP_DOMAIN(4, "statistic.TopDomains", true, true);

    private final int key;
    private final String typeMsgKey;
    private final boolean mailTrackingRequired;
    private final boolean dateRanged;
    
    ReportSettingsType(int key, String typeMsgKey, boolean mailTrackingRequired, boolean dateRanged) {
        this.key = key;
        this.typeMsgKey = typeMsgKey;
        this.mailTrackingRequired = mailTrackingRequired;
        this.dateRanged = dateRanged;
    }
    
    public int getKey() {
        return key;
    }
    
    public String getTypeMsgKey() {
        return typeMsgKey;
    }
    public boolean isMailTrackingRequired() {
        return mailTrackingRequired;
    }
    
    public boolean isDateRanged() {
        return dateRanged;
    }
    
    public static ReportSettingsType getTypeByCode(int code) {
        return findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Unknown report settings type: " + code));
    }

    public static Optional<ReportSettingsType> findByCode(int code) {
        return Stream.of(values())
                .filter(t -> t.getKey() == code)
                .findFirst();
    }

    public static List<Integer> keys() {
        return Arrays.stream(ReportSettingsType.values())
                .map(ReportSettingsType::getKey)
                .toList();
    }
}
