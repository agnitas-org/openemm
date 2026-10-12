/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.dao;

import java.util.List;
import java.util.Map;

public interface MailingTopDomainsStatsDao {

    record Filter(int mailingId, int companyId, boolean isTopLevel) {
    }

    record TargetFilter(int targetId, String targetSql) {
    }

    Map<Integer, Map<String, Integer>> getSentEmails(Filter filter, List<TargetFilter> targets);

    Map<Integer, Map<String, Integer>> getHardBounces(Filter filter, List<TargetFilter> targets);

    Map<Integer, Map<String, Integer>> getSoftBounces(Filter filter, List<TargetFilter> targets);

    Map<Integer, Map<String, Integer>> getOpeners(Filter filter, List<TargetFilter> targets);

    Map<Integer, Map<String, Integer>> getClickers(Filter filter, List<TargetFilter> targets);
}
