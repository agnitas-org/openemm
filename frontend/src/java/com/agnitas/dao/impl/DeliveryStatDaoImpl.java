/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.beans.impl.MailingBackendLog;
import com.agnitas.dao.DeliveryStatDao;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class DeliveryStatDaoImpl extends BaseDaoImpl implements DeliveryStatDao {

	@Override
	public int getSentMails(int maildropId) {
		return selectInt("SELECT SUM(no_of_mailings) FROM mailing_account_tbl WHERE maildrop_id = ?", maildropId);
	}

	@Override
	public Date getSendStartTime(int maildropId) {
		return select("SELECT MIN(timestamp) FROM mailing_account_tbl WHERE maildrop_id = ?", Date.class, maildropId);
	}

	@Override
	public Date getSendEndTime(int maildropId) {
		return select("SELECT MAX(timestamp) FROM mailing_account_tbl WHERE maildrop_id = ?", Date.class, maildropId);
	}

	@Override
	public Date getSendDateFoStatus(int statusId) {
		if (isOracleDB()) {
			return selectWithDefaultValue("SELECT senddate FROM maildrop_status_tbl WHERE status_id = ? AND ROWNUM < 2", Date.class, null, statusId);
		} else {
			return selectWithDefaultValue("SELECT senddate FROM maildrop_status_tbl WHERE status_id = ? LIMIT 1", Date.class, null, statusId);
		}
	}

	@Override
	public MaildropEntry getLastMaildropStatus(int mailingId) {
		String query;
		if (isOracleDB()) {
			query = "SELECT * FROM (SELECT status_field, status_id, genstatus FROM maildrop_status_tbl WHERE mailing_id = ? ORDER BY genchange DESC) WHERE rownum = 1";
		} else {
			query = "SELECT status_field, status_id, genstatus FROM maildrop_status_tbl WHERE mailing_id = ? ORDER BY genchange DESC LIMIT 1";
		}

		return selectObjectDefaultNull(query, new MailingDropMapper(), mailingId);
	}

	@Override
	public MailingBackendLog getLastMailingBackendLog(int statusId) {
		String query;
		if (isOracleDB()) {
			query = "SELECT * FROM (SELECT current_mails, total_mails, timestamp, creation_date FROM mailing_backend_log_tbl WHERE status_id = ? ORDER BY creation_date DESC) WHERE rownum = 1";
		} else {
			query = "SELECT current_mails, total_mails, `timestamp`, creation_date FROM mailing_backend_log_tbl WHERE status_id = ? ORDER BY creation_date DESC LIMIT 1";
		}
		return selectObjectDefaultNull(query, new MailingBackendLogRowMapper(), statusId);
	}	
    
    @Override
	public MailingBackendLog getLastWorldMailingBackendLog(int mailingId) {
        String sql = isOracleDB()
                ? "SELECT * FROM (SELECT current_mails, total_mails, timestamp, creation_date FROM world_mailing_backend_log_tbl WHERE mailing_id = ? ORDER BY creation_date DESC) WHERE rownum = 1"
                : "SELECT current_mails, total_mails, `timestamp`, creation_date FROM world_mailing_backend_log_tbl WHERE mailing_id = ? ORDER BY creation_date DESC LIMIT 1";
		return selectObjectDefaultNull(sql, new MailingBackendLogRowMapper(), mailingId);
	}

	@Override
	public MaildropEntry getFirstMaildropGenerationStatus(int companyId, int mailingId, String statusField) {
		String query;
		if (isOracleDB()) {
			query = "SELECT genstatus, gendate, senddate, status_id, optimize_mail_generation FROM maildrop_status_tbl WHERE company_id = ? AND mailing_id = ? AND status_field = ? AND ROWNUM < 2";
		} else {
			query = "SELECT genstatus, gendate, senddate, status_id, optimize_mail_generation FROM maildrop_status_tbl WHERE company_id = ? AND mailing_id = ? AND status_field = ? LIMIT 1";
		}
		return selectObjectDefaultNull(query, new MaildropGenerationRowMapper(), companyId, mailingId, statusField);
	}

	@Override
	public int getTotalMails(int mailingID) {
		String totalMailsQuery = "SELECT SUM(no_of_mailings) FROM mailing_account_tbl WHERE status_field IN ('W','E','R') AND mailing_id = ?";
		int totalMails = selectInt(totalMailsQuery, mailingID);

		if (totalMails == 0) {
			totalMailsQuery = "SELECT SUM(no_of_mailings) FROM mailing_account_tbl WHERE status_field IN ('A','T') AND mailing_id = ?";
			totalMails = selectInt(totalMailsQuery, mailingID);
		}
		return totalMails;
	}
	
	@Override
	public boolean deleteMaildropStatusByCompany(int companyID) {
		update("DELETE FROM mailing_account_tbl WHERE company_id = ?", companyID);
		update("DELETE FROM mailing_backend_log_tbl WHERE status_id IN (SELECT status_id FROM maildrop_status_tbl WHERE company_id = ?)", companyID);
		update("DELETE FROM mailtrack_" + companyID + "_tbl");
		update("DELETE FROM test_recipients_tbl WHERE maildrop_status_id IN (SELECT status_id FROM maildrop_status_tbl WHERE company_id = ?)", companyID);
		update("DELETE FROM mailing_import_lock_tbl WHERE maildrop_status_id IN (SELECT status_id FROM maildrop_status_tbl WHERE company_id = ?)", companyID);
		update("DELETE FROM maildrop_status_tbl WHERE company_id = ?", companyID);
		return selectInt("SELECT COUNT(*) FROM maildrop_status_tbl WHERE company_id = ?", companyID) == 0;
	}

	@Override
	public List<Integer> findTargetDependentMaildropEntries(int targetGroupId, int companyId) {
		String query = "SELECT status_id FROM maildrop_status_tbl WHERE company_id = ? AND genstatus <> ? AND admin_test_target_id = ?";
		return select(query, IntegerRowMapper.INSTANCE, companyId, MaildropGenerationStatus.FINISHED.getCode(), targetGroupId);
	}

	private static class MailingBackendLogRowMapper implements RowMapper<MailingBackendLog> {

		@Override
		public MailingBackendLog mapRow(ResultSet rs, int i) throws SQLException {
			MailingBackendLog mailingBackendLog = new MailingBackendLog();
			mailingBackendLog.setCurrentMails(rs.getInt("current_mails"));
			mailingBackendLog.setTotalMails(rs.getInt("total_mails"));
			mailingBackendLog.setTimestamp(rs.getTimestamp("timestamp"));
			mailingBackendLog.setCreationDate(rs.getTimestamp("creation_date"));
			return mailingBackendLog;
		}
	}

	private static class MaildropGenerationRowMapper implements RowMapper<MaildropEntry> {

		@Override
		public MaildropEntry mapRow(ResultSet rs, int i) throws SQLException {
			MaildropEntry maildropEntry = new MaildropEntryImpl();
			maildropEntry.setGenStatus(rs.getInt("genstatus"));
			maildropEntry.setGenDate(rs.getTimestamp("gendate"));
			maildropEntry.setSendDate(rs.getTimestamp("senddate"));
			maildropEntry.setId(rs.getInt("status_id"));
			maildropEntry.setMailGenerationOptimization(rs.getString("optimize_mail_generation"));
			return maildropEntry;
		}
	}

	private static class MailingDropMapper implements RowMapper<MaildropEntry> {

		@Override
		public MaildropEntry mapRow(ResultSet rs, int i) throws SQLException {
			MaildropEntry maildropEntry = new MaildropEntryImpl();
			maildropEntry.setStatus(rs.getString("status_field").charAt(0));
			maildropEntry.setId(rs.getInt("status_id"));
			maildropEntry.setGenStatus(rs.getInt("genstatus"));
			return maildropEntry;
		}
	}
}
