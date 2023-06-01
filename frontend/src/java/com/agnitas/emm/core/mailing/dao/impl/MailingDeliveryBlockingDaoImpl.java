/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.dao.impl;

import com.agnitas.emm.core.mailing.dao.MailingDeliveryBlockingDao;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MailingDeliveryBlockingDaoImpl extends BaseDaoImpl implements MailingDeliveryBlockingDao {

    private static final Logger LOGGER = LogManager.getLogger(MailingDeliveryBlockingDaoImpl.class);

    @Override
    public int findBlockingAutoImportId(int mailingId) {
        return selectIntWithDefaultValue(LOGGER, "SELECT auto_import_id FROM mailing_import_lock_tbl WHERE mailing_id = ?", 0, mailingId);
    }

    @Override
    public boolean isAutoImportBlockingEntryExists(int mailingId) {
        return selectInt(LOGGER, "SELECT COUNT(*) FROM mailing_import_lock_tbl WHERE mailing_id = ?", mailingId) > 0;
    }

    @Override
    public void createBlocking(int mailingId, int autoImportId, int maildropStatusId) {
        if (autoImportId > 0) {
            String sqlQuery = "INSERT INTO mailing_import_lock_tbl (mailing_id, auto_import_id, maildrop_status_id) VALUES (?, ?, ?)";
            update(LOGGER, sqlQuery, mailingId, autoImportId, maildropStatusId);
        }
    }

    @Override
    public void updateBlockingData(int mailingId, int autoImportId, int maildropStatusId) {
        String sqlQuery = "UPDATE mailing_import_lock_tbl SET auto_import_id = ?, maildrop_status_id = ?, change_date = CURRENT_TIMESTAMP WHERE mailing_id = ?";
        update(LOGGER, sqlQuery, autoImportId, maildropStatusId, mailingId);
    }

    @Override
    public void updateMaildropStatus(int maildropStatusId, int mailingId) {
        if (mailingId > 0) {
            String sqlQuery = "UPDATE mailing_import_lock_tbl SET maildrop_status_id = ?, change_date = CURRENT_TIMESTAMP WHERE mailing_id = ?";
            update(LOGGER, sqlQuery, maildropStatusId, mailingId);
        }
    }
}
