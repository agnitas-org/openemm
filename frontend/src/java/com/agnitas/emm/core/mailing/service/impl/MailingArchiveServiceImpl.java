/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import java.util.List;
import java.util.Objects;

import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.action.bean.ArchiveOverviewActionLimitType;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.mailing.bean.MailingArchiveEntry;
import com.agnitas.emm.core.mailing.service.MailingArchiveService;
import com.agnitas.util.DateUtilities;

public final class MailingArchiveServiceImpl implements MailingArchiveService {

    private final MailingDao mailingDao;

    public MailingArchiveServiceImpl(MailingDao mailingDao) {
        this.mailingDao = Objects.requireNonNull(mailingDao, "mailing DAO");
    }

    @Override
    public List<MailingArchiveEntry> listMailingArchive(int archiveId, ArchiveOverviewActionLimitType limitType, Integer limitValue, int companyId) {
        DateRange sendDate = null;
        Integer countLimit = null;

        if (limitType != null && limitValue != null) {
            if (ArchiveOverviewActionLimitType.DAYS.equals(limitType)) {
                sendDate = new DateRange(DateUtilities.getDateOfDaysAgo(limitValue), null);
            } else if (ArchiveOverviewActionLimitType.MAILINGS.equals(limitType)) {
                countLimit = limitValue;
            }
        }

        return this.mailingDao.listMailingArchive(archiveId, sendDate, countLimit, companyId);
    }
}
