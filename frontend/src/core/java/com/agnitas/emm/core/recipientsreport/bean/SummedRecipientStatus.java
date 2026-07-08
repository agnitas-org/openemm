/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.bean;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum SummedRecipientStatus {

    USER_OPT_OUT("User-Opt-Out", List.of("User-Opt-Out%")),
    OPT_IN_IP("Opt-In-IP", List.of("Opt-In-IP%")),
    BOUNCE_512("bounce:512", List.of("bounce:512%")),
    BOUNCE_511("bounce:511", List.of("bounce:511%")),
    BOUNCE_510("bounce:510", List.of("bounce:510%")),
    ADDED_TO_BLOCKLIST("Added to blocklist", List.of("Added to blocklist%")),
    OPT_OUT_BY_ADMIN("Opt-Out by ADMIN", List.of("Opt-Out by ADMIN%")),
    OPT_OUT_MAILING("Opt-Out-Mailing", List.of("Opt-Out-Mailing%")),
    BLACKLISTED_BY("Blacklisted by", List.of("Blacklisted by%")),
    OPT_IN_BY_ADMIN("Opt-In by ADMIN", List.of("Opt-In by ADMIN%")),
    IMPORT("Import", List.of("CSV File Upload%", "CSV Import%")),
    BOUNCE_CONVERSION("Bounce Conversion", List.of("bounce:soft%", "bounce:conversion%")),
    CSA_UNSUBSCRIBING("Unsubscribing via CSA Mandatory Link", List.of("%CSA%"));

    private final String name;
    private final List<String> remarks;

    SummedRecipientStatus(String name, List<String> remarks) {
        this.name = name;
        this.remarks = remarks;
    }

    public String getName() {
        return name;
    }

    public String getLikeSql() {
        String sql = remarks.stream()
            .map(remark -> "user_remark LIKE '" + remark + "'")
            .collect(Collectors.joining(" OR "));

        if (remarks.size() > 1) {
            sql = "(" + sql + ")";
        }
        return sql;
    }

    public String getNotLikeSql() {
        return remarks.stream()
            .map(remark -> "user_remark NOT LIKE '" + remark + "'")
            .collect(Collectors.joining(" AND "));
    }

    public static Set<String> getNames() {
        return Arrays.stream(SummedRecipientStatus.values())
                .map(SummedRecipientStatus::getName)
                .collect(Collectors.toSet());
    }
}
