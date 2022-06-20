/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import com.agnitas.beans.MaildropEntry;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.mailing.dao.MailingDeliveryLockDao;
import com.agnitas.emm.core.mailing.service.MaildropStatusService;
import com.agnitas.emm.core.mailing.service.MailingDeliveryLockService;
import org.agnitas.emm.core.autoimport.bean.MailingImportLock;
import org.springframework.beans.factory.annotation.Required;

public class MailingDeliveryLockServiceImpl implements MailingDeliveryLockService {

    private MailingDeliveryLockDao mailingDeliveryLockDao;
    private MaildropStatusService maildropStatusService;

    @Override
    public void blockIfNecessary(int autoImportId, int mailingId, int companyId) {
        int maildropStatusId = maildropStatusService.getLastMaildropEntryId(mailingId, companyId);

        MailingImportLock mailingImportLock = mailingDeliveryLockDao.getMailingImportLock(mailingId);

        if (mailingImportLock == null) {
            mailingDeliveryLockDao.saveMailingImportLock(mailingId, autoImportId, maildropStatusId);
        } else {
            mailingImportLock.setAutoImportId(autoImportId);
            mailingImportLock.setMaildropStatusId(maildropStatusId);

            mailingDeliveryLockDao.updateMailingImportLock(mailingImportLock);
        }
    }

    @Override
    public void cancelMailingImportLock(int mailingId) {
        mailingDeliveryLockDao.updateMaildropStatusForImportLock(0, mailingId);
    }

    @Override
    public void resumeExistingLocksIfNeeded(int mailingId, int companyId) {
        int maildropStatusId = maildropStatusService.getLastMaildropEntryId(mailingId, companyId);

        MailingImportLock mailingImportLock = mailingDeliveryLockDao.getMailingImportLock(mailingId);

        if (mailingImportLock != null) {
            MaildropEntry maildropEntry = maildropStatusService.getMaildropEntry(mailingId, companyId, maildropStatusId);

            if (maildropEntry.getGenStatus() < MaildropGenerationStatus.WORKING.getCode()) {
                mailingDeliveryLockDao.updateMaildropStatusForImportLock(maildropStatusId, mailingId);
            }
        }
    }

    @Override
    public MailingImportLock getMailingImportLock(int mailingId) {
        MailingImportLock mailingImportLock = mailingDeliveryLockDao.getMailingImportLock(mailingId);

        if (mailingImportLock != null) {
            return mailingImportLock;
        }

        return new MailingImportLock();
    }

    @Required
    public void setMailingDeliveryLockDao(MailingDeliveryLockDao mailingDeliveryLockDao) {
        this.mailingDeliveryLockDao = mailingDeliveryLockDao;
    }

    @Required
    public void setMaildropStatusService(MaildropStatusService maildropStatusService) {
        this.maildropStatusService = maildropStatusService;
    }
}
