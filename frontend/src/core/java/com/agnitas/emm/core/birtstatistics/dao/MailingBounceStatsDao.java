/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.dao;

import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.birtstatistics.dto.EmailBounceDetail;
import com.agnitas.emm.core.bounce.Bounce;

public interface MailingBounceStatsDao {

    String BOUNCE_REMARK_SIGN = "bounce:";

    Map<Bounce, Integer> getSoftBounces(int mailingId, int companyId, String targetSql);

    Map<Bounce, Integer> getHardBounces(int mailingId, int companyId, String targetSql);

    List<EmailBounceDetail> getSoftBouncesWithDetailAndEmail(int mailingId, int companyId, String targetSql);

    List<EmailBounceDetail> getHardBouncesWithDetailAndEmail(int mailingId, int companyId, String targetSql);
}
