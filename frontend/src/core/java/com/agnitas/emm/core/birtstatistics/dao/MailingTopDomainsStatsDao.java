/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.dao;

import java.util.Map;

public interface MailingTopDomainsStatsDao {

    record TopDomainsFilter(
            int mailingId,
            int companyId,
            int limit,
            boolean isTopLevel
    ) {
    }

    Map<String, Integer> getSentEmails(TopDomainsFilter opts, String targetSql);

    Map<String, Integer> getHardBounces(TopDomainsFilter opts, String targetSql);

    Map<String, Integer> getSoftBounces(TopDomainsFilter opts, String targetSql);

    int getHardBouncesTotal(TopDomainsFilter opts, String targetSql);

    int getSoftBouncesTotal(TopDomainsFilter opts, String targetSql);

    int getOpenersTotal(int mailingId, int companyId, String targetSql);

    int getClickersTotal(int mailingId, int companyId, String targetSql);

    Map<String, Integer> getOpeners(TopDomainsFilter opts, String targetSql);

    Map<String, Integer> getClickers(TopDomainsFilter opts, String targetSql);
}
