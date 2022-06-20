/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import com.agnitas.beans.MaildropEntry;
import com.agnitas.emm.core.mailing.service.MaildropStatusService;
import org.agnitas.dao.MaildropStatusDao;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

public class MaildropStatusServiceImpl implements MaildropStatusService {

    private MaildropStatusDao maildropStatusDao;

    @Override
    public int getLastMaildropEntryId(int mailingId, int companyId) {
        List<Integer> maildropEntryIds = maildropStatusDao.getMaildropEntryIds(mailingId, companyId);

        if (maildropEntryIds.isEmpty()) {
            return 0;
        }

        return maildropEntryIds.get(maildropEntryIds.size() - 1);
    }

    @Override
    public MaildropEntry getMaildropEntry(int mailingId, int companyId, int statusId) {
        return maildropStatusDao.getMaildropEntry(mailingId, companyId, statusId);
    }

    @Required
    public void setMaildropStatusDao(MaildropStatusDao maildropStatusDao) {
        this.maildropStatusDao = maildropStatusDao;
    }
}
