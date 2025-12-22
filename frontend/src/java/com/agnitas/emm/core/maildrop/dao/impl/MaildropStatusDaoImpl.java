/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.maildrop.dao.impl;

import java.net.InetAddress;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.MaildropEntry;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.dao.impl.mapper.MaildropRowMapper;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.dao.MaildropStatusDao;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.CollectionUtils;

public class MaildropStatusDaoImpl extends BaseDaoImpl implements MaildropStatusDao {

	private static String origin;
	static {
		try {
			origin = InetAddress.getLocalHost ().getCanonicalHostName ();
		} catch (@SuppressWarnings("unused") Exception e) {
			origin = null;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean delete(int companyId, int id) {
		String sql = "SELECT status_id FROM maildrop_status_tbl WHERE company_id = ? AND status_id = ?";
		try {
			List<Integer> ids = select(sql, IntegerRowMapper.INSTANCE, companyId, id);
			return deleteByIds(ids) > 0;
		} catch (Exception e) {
			logger.error("Error: " + e.getMessage(), e);
		}
		return false;
	}
	
	@Override
	public boolean delete(int companyId, int mailingId, MaildropStatus status, MaildropGenerationStatus generationStatus) {
		final String sql = "SELECT status_id FROM maildrop_status_tbl WHERE company_id = ? AND mailing_id = ? AND status_field = ? AND genstatus = ?";
		List<Integer> ids = select(sql, IntegerRowMapper.INSTANCE, companyId, mailingId, status.getCodeString(), generationStatus.getCode());

		return deleteByIds(ids) > 0;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int deleteUnsentEntries(int mailingID) {
        try {
			List<Integer> ids = select(
                    "SELECT status_id FROM maildrop_status_tbl WHERE genstatus IN (?, ?) AND mailing_id = ?",
					IntegerRowMapper.INSTANCE,
					MaildropGenerationStatus.SCHEDULED.getCode(),
					MaildropGenerationStatus.NOW.getCode(),
					mailingID
			);

			return deleteByIds(ids);
		} catch (Exception e) {
			logger.error("Error: " + e.getMessage(), e);
		}
		return 0;
	}

	@Override
	public int cleanup(Collection<MaildropEntry> entries) {
		int entriesDeleted = 0;
		if (CollectionUtils.isNotEmpty(entries)) {
			Iterator<MaildropEntry> i = entries.iterator();

			while (i.hasNext()) {
				MaildropEntry entry = i.next();
				if (isDeletable(entry)) {
					if (delete(entry.getCompanyID(), entry.getId())) {
						entriesDeleted++;
					}
					i.remove();
				}
			}
		}
		return entriesDeleted;
	}

	// Check whether or not an entry is allowed for removal
	private boolean isDeletable(MaildropEntry entry) {
		if (entry.getStatus() == MaildropStatus.WORLD.getCode()) {
			MaildropGenerationStatus generationStatus = MaildropGenerationStatus.fromCodeOrNull(entry.getGenStatus());
			
			if(generationStatus != null) {
                return switch (generationStatus) {
                    case SCHEDULED, NOW -> true;
                    default -> false;
                };
			}

			return false;
		}

		return true;
	}

	@Override
	public MaildropEntry getMaildropEntry(int mailingID, int companyID, int statusID) {
		String sql = "SELECT * FROM maildrop_status_tbl WHERE mailing_id = ? AND company_id = ? AND status_id = ?";
		return selectObjectDefaultNull(sql, MaildropRowMapper.INSTANCE, mailingID, companyID, statusID);
	}
	
	@Override
	public List<Integer> getMaildropEntryIds(int mailingID, int companyID) {
		return select("SELECT DISTINCT status_id FROM maildrop_status_tbl WHERE mailing_id = ? AND company_id = ?",
				IntegerRowMapper.INSTANCE, mailingID, companyID);
	}

	@Override
	public MaildropEntry getEntryForStatus(int mailingID, int companyID, char status) {
		final String sql = "SELECT * FROM maildrop_status_tbl " +
			"WHERE mailing_id = ? AND company_id = ? AND status_field = ? " +
			"ORDER BY status_id DESC";

		List<MaildropEntry> entries = select(sql, MaildropRowMapper.INSTANCE, mailingID, companyID, Character.toString(status));

		return entries.isEmpty() ? null : entries.get(0);
	}

	@Override
	public void updateMaildropEntry(MaildropEntry entry) {
		final String sql = "UPDATE maildrop_status_tbl SET status_field = ?, senddate = ?, step = ?, blocksize = ?, gendate = ?, genstatus = ?, genchange = ?, max_recipients = ?, admin_test_target_id = ?, optimize_mail_generation = ? WHERE status_id = ? AND company_id = ? AND mailing_id = ?";
		
		this.update(sql,
				Character.toString(entry.getStatus()),
				entry.getSendDate(),
				entry.getStepping(),
				entry.getBlocksize(),
				entry.getGenDate(),
				entry.getGenStatus(),
				entry.getGenChangeDate(),
				entry.getMaxRecipients(),
				entry.getAdminTestTargetID(),
				entry.getMailGenerationOptimization(),
				entry.getId(),
				entry.getCompanyID(),
				entry.getMailingID());
	}

	@Override
	public int insertMaildropEntry(MaildropEntry entry) {
		int	overwriteTestRecipient = entry.getOverwriteTestRecipient();
		
		if (isOracleDB()) {
			int maildropStatusId = selectInt("SELECT maildrop_status_tbl_seq.NEXTVAL FROM DUAL");

			if (overwriteTestRecipient == 0) {
				update("INSERT INTO maildrop_status_tbl (status_id, company_id, status_field, mailing_id, senddate, step, blocksize, gendate, genstatus, genchange, max_recipients, admin_test_target_id, optimize_mail_generation, origin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
						maildropStatusId,
						entry.getCompanyID(),
						Character.toString(entry.getStatus()),
						entry.getMailingID(),
						entry.getSendDate(),
						entry.getStepping(),
						entry.getBlocksize(),
						entry.getGenDate(),
						entry.getGenStatus(),
						entry.getGenChangeDate(),
						entry.getMaxRecipients(),
						entry.getAdminTestTargetID(),
						entry.getMailGenerationOptimization(),
				      		origin);
			} else {
				update("INSERT INTO maildrop_status_tbl (status_id, company_id, status_field, mailing_id, senddate, step, blocksize, gendate, genstatus, genchange, max_recipients, admin_test_target_id, optimize_mail_generation, overwrite_test_recipient, origin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
						maildropStatusId,
						entry.getCompanyID(),
						Character.toString(entry.getStatus()),
						entry.getMailingID(),
						entry.getSendDate(),
						entry.getStepping(),
						entry.getBlocksize(),
						entry.getGenDate(),
						entry.getGenStatus(),
						entry.getGenChangeDate(),
						entry.getMaxRecipients(),
						entry.getAdminTestTargetID(),
						entry.getMailGenerationOptimization(),
						overwriteTestRecipient,
				      		origin);
			}
			
			entry.setId(maildropStatusId);
			
			return maildropStatusId;
		} else {
			final int maildropStatusId;
			
			if (overwriteTestRecipient == 0) {
				maildropStatusId = insert("status_id", "INSERT INTO maildrop_status_tbl (company_id, status_field, mailing_id, senddate, step, blocksize, gendate, genstatus, genchange, max_recipients, admin_test_target_id, optimize_mail_generation, origin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
										     entry.getCompanyID(),
										     Character.toString(entry.getStatus()),
										     entry.getMailingID(),
										     entry.getSendDate(),
										     entry.getStepping(),
										     entry.getBlocksize(),
										     entry.getGenDate(),
										     entry.getGenStatus(),
										     entry.getGenChangeDate(),
										     entry.getMaxRecipients(),
										     entry.getAdminTestTargetID(),
										     entry.getMailGenerationOptimization(),
										     origin);
			} else {
				maildropStatusId = insert("status_id", "INSERT INTO maildrop_status_tbl (company_id, status_field, mailing_id, senddate, step, blocksize, gendate, genstatus, genchange, max_recipients, admin_test_target_id, optimize_mail_generation, overwrite_test_recipient, origin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
										     entry.getCompanyID(),
										     Character.toString(entry.getStatus()),
										     entry.getMailingID(),
										     entry.getSendDate(),
										     entry.getStepping(),
										     entry.getBlocksize(),
										     entry.getGenDate(),
										     entry.getGenStatus(),
										     entry.getGenChangeDate(),
										     entry.getMaxRecipients(),
										     entry.getAdminTestTargetID(),
										     entry.getMailGenerationOptimization(),
										     overwriteTestRecipient,
										     origin);
			}
			entry.setId(maildropStatusId);

			return maildropStatusId;
		}
	}

	@Override
	public void batchInsertMaildropEntries(int companyId, int mailingId, List<MaildropEntry> entries) {
		if (entries.isEmpty()) {
			return;
		}
		
		try {
			if (isOracleDB()) {
				String sql = "INSERT INTO maildrop_status_tbl (status_id, company_id, status_field, mailing_id, senddate, step, blocksize, gendate, genstatus, genchange, max_recipients, admin_test_target_id, optimize_mail_generation, origin) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				List<Object[]> parameterList = new ArrayList<>();
				for (MaildropEntry entry : entries) {
					final int maildropStatusId = selectInt("SELECT maildrop_status_tbl_seq.NEXTVAL FROM DUAL");
					entry.setId(maildropStatusId);
					Object[] entryParam = {
							maildropStatusId,
							companyId,
							Character.toString(entry.getStatus()),
							mailingId,
							entry.getSendDate(),
							entry.getStepping(),
							entry.getBlocksize(),
							entry.getGenDate(),
							entry.getGenStatus(),
							entry.getGenChangeDate(),
							entry.getMaxRecipients(),
							entry.getAdminTestTargetID(),
							entry.getMailGenerationOptimization(),
							origin
					};
					parameterList.add(entryParam);
				}
				
				batchupdate(sql, parameterList);
			} else {
				String sql = "INSERT INTO maildrop_status_tbl (company_id, status_field, mailing_id, senddate, step, blocksize, gendate, genstatus, genchange, max_recipients, admin_test_target_id, optimize_mail_generation, origin) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				List<Object[]> parameterList = new ArrayList<>();
				for (MaildropEntry entry : entries) {
					Object[] entryParam = {
							companyId,
							Character.toString(entry.getStatus()),
							mailingId,
							entry.getSendDate(),
							entry.getStepping(),
							entry.getBlocksize(),
							entry.getGenDate(),
							entry.getGenStatus(),
							entry.getGenChangeDate(),
							entry.getMaxRecipients(),
							entry.getAdminTestTargetID(),
							entry.getMailGenerationOptimization(),
							origin
					};
					parameterList.add(entryParam);
				}
				
				int[] ids = batchInsertWithAutoincrement(sql, parameterList);
				for (int i = 0; i < ids.length; i++) {
					entries.get(i).setId(ids[i]);
				}

				if (!isPostgreSQL()) {
						// TODO: Bugfix: Some MariaDB versions (10.3 ?) cause time values '00:00' when batch inserting, so we update those values again afterwards
					for (MaildropEntry entry : entries) {
						updateMaildropEntry(entry);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Could not insert new mail drops", e);
		}
	}

	@Override
	public void batchUpdateMaildropEntries(int companyId, int mailingId, List<MaildropEntry> entries) {
		if (entries.isEmpty()) {
			return;
		}
		
		List<Object[]> parameterList = new ArrayList<>();
		for (MaildropEntry entry: entries) {
			Object[] entryParam = {
					Character.toString(entry.getStatus()),
					entry.getSendDate(),
					entry.getStepping(),
					entry.getBlocksize(),
					entry.getGenDate(),
					entry.getGenStatus(),
					entry.getGenChangeDate(),
					entry.getMaxRecipients(),
					entry.getAdminTestTargetID(),
					entry.getMailGenerationOptimization(),
					entry.getId(),
					companyId,
					mailingId
			};
			
			parameterList.add(entryParam);
		}
		
		batchupdate("UPDATE maildrop_status_tbl SET " +
						"status_field = ?, senddate = ?, step = ?, blocksize = ?, gendate = ?, genstatus = ?, genchange = ?, " +
						"max_recipients = ?, admin_test_target_id = ?, optimize_mail_generation = ? " +
						"WHERE status_id = ? AND company_id = ? AND mailing_id = ?", parameterList);
	}
	
	@Override
	public Collection<MaildropEntry> listMaildropStatus(int mailingID, int companyID) {
		final String sql = "SELECT * FROM maildrop_status_tbl WHERE mailing_id = ? AND company_id = ? AND status_field NOT LIKE '-%'";

		return select(sql, MaildropRowMapper.INSTANCE, mailingID, companyID);
	}

	@Override
	public List<MaildropEntry> getMaildropStatusEntriesForMailing(int companyID, int mailingID) {
		String maildropEntrySelect = "SELECT mailing_id, company_id, status_id, status_field, senddate, step, blocksize, gendate, genchange, genstatus, admin_test_target_id, optimize_mail_generation, max_recipients FROM maildrop_status_tbl WHERE company_id = ? AND mailing_id = ?";
		return select(maildropEntrySelect, MaildropRowMapper.INSTANCE, companyID, mailingID);
	}

	@Override
	public boolean setSelectedTestRecipients(int companyId, int maildropStatusId, boolean isSelected) {
		String sqlSetSelected = "UPDATE maildrop_status_tbl SET selected_test_recipients = ? WHERE company_id = ? AND status_id = ? AND status_field IN (?, ?)";
		String statusTest = Character.toString(MaildropStatus.TEST.getCode());
		String statusAdmin = Character.toString(MaildropStatus.ADMIN.getCode());

		return update(sqlSetSelected, isSelected ? 1 : 0, companyId, maildropStatusId, statusTest, statusAdmin) > 0;
	}

	@Override
	public void setTestRecipients(int maildropStatusId, List<Integer> customerIds) {
		String sqlClearRecipients = "DELETE FROM test_recipients_tbl WHERE maildrop_status_id = ?";
		String sqlStoreRecipients = "INSERT INTO test_recipients_tbl (maildrop_status_id, customer_id) VALUES (?, ?)";

		update(sqlClearRecipients, maildropStatusId);

		if (CollectionUtils.isNotEmpty(customerIds)) {
			List<Object[]> sqlParameters = customerIds.stream()
					.map(customerId -> new Object[] {maildropStatusId, customerId})
					.toList();

			batchupdate(sqlStoreRecipients, sqlParameters);
		}
	}
	
	@Override
	public void removeOutdatedFindLastNewsletterEntries(int companyID, ZonedDateTime olderThan) {
		final String selectSql = "SELECT mds2.status_id"
				+ " FROM maildrop_status_tbl mds1, maildrop_status_tbl mds2"
				+ " WHERE mds1.status_field = 'W'"
				+ " AND mds1.company_id > 1"
				+ " AND mds1.company_id = ?"
				+ " AND mds2.mailing_id = mds1.mailing_id"
				+ " AND mds2.senddate > mds1.senddate"
				+ " AND mds2.status_field = 'E'"
				+ " AND mds2.senddate < ?";
		
		final Date timestamp = Date.from(olderThan.toInstant());
		
		final List<Integer> idList = this.select(selectSql, IntegerRowMapper.INSTANCE, companyID, timestamp);
		deleteByIds(idList);
	}

	@Override
	public void writeMailingSendStatisticsEntry(int companyID, int mailingID, MaildropStatus maildropStatus, MediaTypes mediaType, MailType mailType, int amount, int dataSize, Date sendDate, String mailerHostname) {
		update("INSERT INTO mailing_account_tbl (company_id, mailing_id, status_field, mediatype, mailtype, no_of_mailings, no_of_bytes, timestamp, mailer) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
			companyID, mailingID, Character.toString(maildropStatus.getCode()), mediaType.getMediaCode(), mailType.getIntValue(), amount, dataSize, sendDate, mailerHostname
		);
	}

	@Override
	public List<Integer> getMailingsSentBetween(int companyID, Date startDateIncluded, Date endDateExcluded) {
		return select("SELECT mailing_id FROM maildrop_status_tbl WHERE company_id = ? AND senddate >= ? AND senddate < ? AND status_field NOT IN (?, ?)", IntegerRowMapper.INSTANCE,
				companyID, startDateIncluded, endDateExcluded, MaildropStatus.ADMIN.getCodeString(), MaildropStatus.TEST.getCodeString());
	}

	@Override
	public Map<Integer, List<Integer>> cleanupFailedTestDeliveries() {
		List<Map<String, Object>> result = select("SELECT status_id, company_id, mailing_id FROM maildrop_status_tbl WHERE genstatus IN (?, ?) AND status_field IN (?, ?) AND genchange < ?",
			MaildropGenerationStatus.NOW.getCode(), MaildropGenerationStatus.WORKING.getCode(),
			MaildropStatus.ADMIN.getCodeString(), MaildropStatus.TEST.getCodeString(),
			DateUtilities.getDateOfHoursAgo(1));
		Map<Integer, List<Integer>> returnMap = new HashMap<>();
		for (Map<String, Object> row : result) {
			int statusID = ((Number) row.get("status_id")).intValue();
			int companyID = ((Number) row.get("company_id")).intValue();
			int mailingID = ((Number) row.get("mailing_id")).intValue();
			if (!returnMap.containsKey(companyID)) {
				returnMap.put(companyID, new ArrayList<>());
			}
			update("UPDATE maildrop_status_tbl SET genstatus = ? WHERE status_id = ?", MaildropGenerationStatus.MANUALLY_SOLVED.getCode(), statusID);
			returnMap.get(companyID).add(mailingID);
		}
		return returnMap;
	}

	@Override
	public void cleanupOldEntriesByMailingID(int mailingID, int maximumAgeInDays) {
        List<Integer> ids = select(
				"SELECT status_id FROM maildrop_status_tbl WHERE mailing_id = ? AND senddate < ?",
				IntegerRowMapper.INSTANCE,
				mailingID,
				DateUtilities.getDateOfDaysAgo(maximumAgeInDays)
		);

		deleteByIds(ids);
	}

	@Override
	public void deleteByMailingId(int mailingID) {
		List<Integer> ids = select(
				"SELECT status_id FROM maildrop_status_tbl WHERE mailing_id = ?",
				IntegerRowMapper.INSTANCE,
				mailingID
		);

		if (!ids.isEmpty()) {
			update("DELETE FROM test_recipients_tbl WHERE " + makeBulkInClauseForInteger("maildrop_status_id", ids));
		}
		deleteByIds(ids);
	}

	protected int deleteByIds(Collection<Integer> ids) {
		if (ids.isEmpty()) {
			return 0;
		}

		return update("DELETE FROM maildrop_status_tbl WHERE "
				+ makeBulkInClauseForInteger("status_id", ids));
	}

}
