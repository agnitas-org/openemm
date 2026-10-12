/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.stats.recipient;

import java.util.List;
import java.util.Map;

import com.agnitas.emm.common.UserStatus;
import com.agnitas.util.importvalues.MailType;

interface RecipientStatsDao {

    List<BindStatusDetails> getRecipientStatusCounts(RecipientStatsFilter filter);

    Map<MailType, Long> getMailtypeCounts(RecipientStatsFilter filter);

    Map<UserStatus, Long> getUserStatusCounts(RecipientStatsFilter filter);

    long getTotalDoiCount(RecipientStatsFilter filter);

    long getNotConfirmedDoiCount(RecipientStatsFilter filter);

    long getNotConfirmedAndDeletedDoiCount(RecipientStatsFilter filter);

    long getConfirmedDoiCount(RecipientStatsFilter filter);

    long getConfirmedAndNotActiveDoiCount(RecipientStatsFilter filter);
}
