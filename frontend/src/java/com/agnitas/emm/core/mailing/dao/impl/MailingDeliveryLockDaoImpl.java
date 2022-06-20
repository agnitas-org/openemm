/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.dao.impl;

import com.agnitas.emm.core.mailing.dao.MailingDeliveryLockDao;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.autoimport.bean.MailingImportLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MailingDeliveryLockDaoImpl extends BaseDaoImpl implements MailingDeliveryLockDao {

    private static final transient Logger LOGGER = LogManager.getLogger(MailingDeliveryLockDaoImpl.class);

    @Override
    public MailingImportLock getMailingImportLock(int mailingId) {
        if (mailingId <= 0) {
            return null;
        } else {
            String selectQuery = "SELECT mailing_id, auto_import_id, maildrop_status_id FROM mailing_import_lock_tbl WHERE mailing_id = ?";
            return selectObjectDefaultNull(LOGGER, selectQuery, new MailingImportLockRowMapper(), mailingId);
        }
    }

    @Override
    public void saveMailingImportLock(int mailingId, int autoImportId, int maildropStatusId) {
        if (autoImportId > 0) {
            String sqlQuery = "INSERT INTO mailing_import_lock_tbl (mailing_id, auto_import_id, maildrop_status_id) VALUES (?, ?, ?)";
            update(LOGGER, sqlQuery, mailingId, autoImportId, maildropStatusId);
        }
    }

    @Override
    public void updateMailingImportLock(MailingImportLock mailingImportLock) {
        String sqlQuery = "UPDATE mailing_import_lock_tbl SET auto_import_id = ?, maildrop_status_id = ?, change_date = CURRENT_TIMESTAMP WHERE mailing_id = ?";
        update(LOGGER, sqlQuery, mailingImportLock.getAutoImportId(), mailingImportLock.getMaildropStatusId(), mailingImportLock.getMailingId());
    }

    @Override
    public void updateMaildropStatusForImportLock(int maildropStatusId, int mailingId) {
        if (mailingId > 0) {
            String sqlQuery = "UPDATE mailing_import_lock_tbl SET maildrop_status_id = ?, change_date = CURRENT_TIMESTAMP WHERE mailing_id = ?";
            update(LOGGER, sqlQuery, maildropStatusId, mailingId);
        }
    }

    private static class MailingImportLockRowMapper implements RowMapper<MailingImportLock> {
        @Override
        public MailingImportLock mapRow(ResultSet resultSet, int i) throws SQLException {
            MailingImportLock mailingImportLock = new MailingImportLock();

            mailingImportLock.setMailingId(resultSet.getInt("mailing_id"));
            mailingImportLock.setAutoImportId(resultSet.getInt("auto_import_id"));
            mailingImportLock.setMaildropStatusId(resultSet.getInt("maildrop_status_id"));

            return mailingImportLock;
        }
    }
}
