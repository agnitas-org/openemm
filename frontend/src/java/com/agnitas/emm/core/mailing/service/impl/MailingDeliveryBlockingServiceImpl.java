/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import com.agnitas.beans.MaildropEntry;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.dao.MailingDeliveryBlockingDao;
import com.agnitas.emm.core.mailing.service.MailingDeliveryBlockingService;
import org.springframework.beans.factory.annotation.Autowired;

public class MailingDeliveryBlockingServiceImpl implements MailingDeliveryBlockingService {

    private final MailingDeliveryBlockingDao mailingDeliveryBlockingDao;
    private final MaildropService maildropService;

    @Autowired
    public MailingDeliveryBlockingServiceImpl(MailingDeliveryBlockingDao mailingDeliveryBlockingDao, MaildropService maildropService) {
        this.mailingDeliveryBlockingDao = mailingDeliveryBlockingDao;
        this.maildropService = maildropService;
    }

    @Override
    public void blockDeliveryByAutoImport(int autoImportId, int mailingId, int companyId) {
        int maildropStatusId = maildropService.getLastMaildropEntryId(mailingId, companyId);
        blockByAutoImport(mailingId, autoImportId, maildropStatusId);
    }

    @Override
    public void blockByAutoImport(int mailingId, int autoImportId, int maildropStatusId) {
        if (!mailingDeliveryBlockingDao.isAutoImportBlockingEntryExists(mailingId)) {
            mailingDeliveryBlockingDao.createBlocking(mailingId, autoImportId, maildropStatusId);
        } else {
            mailingDeliveryBlockingDao.updateBlockingData(mailingId, autoImportId, maildropStatusId);
        }
    }

    @Override
    public void unblock(int mailingId) {
        mailingDeliveryBlockingDao.updateMaildropStatus(0, mailingId);
    }

    @Override
    public void resumeBlockingIfNeeded(int mailingId, int companyId) {
        if (mailingDeliveryBlockingDao.isAutoImportBlockingEntryExists(mailingId)) {
            int maildropStatusId = maildropService.getLastMaildropEntryId(mailingId, companyId);
            MaildropEntry maildropEntry = maildropService.getMaildropEntry(mailingId, companyId, maildropStatusId);

            if (maildropEntry.getGenStatus() < MaildropGenerationStatus.WORKING.getCode()) {
                mailingDeliveryBlockingDao.updateMaildropStatus(maildropStatusId, mailingId);
            }
        }
    }

    @Override
    public int findBlockingAutoImportId(int mailingId) {
        return mailingDeliveryBlockingDao.findBlockingAutoImportId(mailingId);
    }
}
