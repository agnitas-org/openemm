/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.bean;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum SummedRecipientRemark {

    USER_OPT_OUT("User-Opt-Out"),
    OPT_IN_IP("Opt-In-IP"),
    BOUNCE_512("bounce:512"),
    BOUNCE_511("bounce:511"),
    BOUNCE_510("bounce:510"),
    ADDED_TO_BLOCKLIST("Added to blocklist"),
    OPT_OUT_BY_ADMIN("Opt-Out by ADMIN"),
    OPT_OUT_MAILING("Opt-Out-Mailing"),
    BLACKLISTED_BY("Blacklisted by"),
    OPT_IN_BY_ADMIN("Opt-In by ADMIN");

    private final String name;

    SummedRecipientRemark(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Set<String> getNames() {
        return Arrays.stream(SummedRecipientRemark.values())
                .map(SummedRecipientRemark::getName)
                .collect(Collectors.toSet());
    }
}
