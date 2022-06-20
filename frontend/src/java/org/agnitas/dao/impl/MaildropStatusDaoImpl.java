/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import java.net.InetAddress;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.dao.MaildropStatusDao;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.dao.impl.mapper.MaildropRowMapper;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.beans.MaildropEntry;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.maildrop.MaildropStatus;

public class MaildropStatusDaoImpl extends BaseDaoImpl implements MaildropStatusDao {
	private static final transient Logger logger = LogManager.getLogger( MaildropStatusDaoImpl.class);
	private static String origin;
	static {
		try {
			origin = InetAddress.getLocalHost ().getCanonicalHostName ();
		} catch (Exception e) {
			origin = null;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean delete(@VelocityCheck int companyId, int id) {
		String sql = "DELETE FROM maildrop_status_tbl WHERE company_id = ? AND status_id = ?";
		try {
			return update(logger, sql, companyId, id) > 0;
		} catch (Exception e) {
			logger.error("Error: " + e.getMessage(), e);
		}
		return false;
	}
	
	@Override
	public boolean delete(int companyId, int mailingId, MaildropStatus status, MaildropGenerationStatus generationStatus) {
		final String sql = "DELETE FROM maildrop_status_tbl WHERE company_id=? AND mailing_id=? AND status_field=? AND genstatus=?";

		final int deleted = update(logger, sql, companyId, mailingId, status.getCodeString(), generationStatus.getCode());

		return deleted > 0;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int deleteUnsentWorldMailingEntries(int mailingID) {
		String sql = "DELETE FROM maildrop_status_tbl WHERE genstatus IN (?, ?) AND status_field = ? AND mailing_id = ?";
		try {
			return update(logger, sql, MaildropGenerationStatus.SCHEDULED.getCode(), MaildropGenerationStatus.NOW.getCode(), MaildropStatus.WORLD.getCode(), mailingID);
		} catch (Exception e) {
			logger.error("Error: " + e.getMessage(), e);
		}
		return 0;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int deleteUnsentEntries(int mailingID) {
		String sql = "DELETE FROM maildrop_status_tbl WHERE genstatus IN (?, ?) AND mailing_id = ?";
		try {
			return update(logger, sql, MaildropGenerationStatus.SCHEDULED.getCode(), MaildropGenerationStatus.NOW.getCode(), mailingID);
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
			final MaildropGenerationStatus generationStatusOrNull = MaildropGenerationStatus.fromCodeOrNull(entry.getGenStatus());
			
			if(generationStatusOrNull != null) {
				switch(generationStatusOrNull) {
				case SCHEDULED:
				case NOW:
					return true;
				default:
					return false;
				}
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	@Override
	public final MaildropEntry getMaildropEntry(final int mailingID, final int companyID, final int statusID) {
		final String sql = "SELECT * FROM maildrop_status_tbl WHERE mailing_id = ? AND company_id = ? AND status_id = ?";
		
		final List<MaildropEntry> result = select(logger, sql, MaildropRowMapper.INSTANCE, mailingID, companyID, statusID);
		
		if(result.size() == 0) {
			return null;
		} else {
			return result.get(0);	// According to specification of maildrop_status_tbl, a status ID is unique
		}
	}
	
	@Override
	public final List<Integer> getMaildropEntryIds(final int mailingID, final int companyID) {
		return select(logger, "SELECT DISTINCT status_id FROM maildrop_status_tbl WHERE mailing_id = ? AND company_id = ?",
				IntegerRowMapper.INSTANCE, mailingID, companyID);
	}
	
	@Override
	public final List<Integer> getMaildropEntryIds(final int mailingID, final int companyID, MaildropStatus maildropStatus) {
		return select(logger, "SELECT DISTINCT status_id FROM maildrop_status_tbl WHERE mailing_id = ? AND company_id = ? AND status_field = ?",
				IntegerRowMapper.INSTANCE, mailingID, companyID, maildropStatus.getCodeString());
	}

	@Override
	public MaildropEntry getEntryForStatus(final int mailingID, final int companyID, final char status) {
		final String sql = "SELECT * FROM maildrop_status_tbl " +
			"WHERE mailing_id = ? AND company_id = ? AND status_field = ? " +
			"ORDER BY status_id DESC";

		List<MaildropEntry> entries = select(logger, sql, MaildropRowMapper.INSTANCE, mailingID, companyID, Character.toString(status));

		if (entries.size() == 0) {
			return null;
		} else {
			return entries.get(0);
		}
	}

	@Override
	public final int saveMaildropEntry(final MaildropEntry entry) throws Exception {
		MailingType mailingType;
		try {
			mailingType = MailingType.fromCode(selectInt(logger, "SELECT mailing_type FROM mailing_tbl WHERE mailing_id = ?", entry.getMailingID()));
	    } catch (final Exception e) {
	        throw new Exception("Error getting mailingType for mailing " + entry.getMailingID(), e);
	    }
		
		if (mailingType == MailingType.ACTION_BASED || mailingType == MailingType.DATE_BASED) {
			MaildropEntry existingEntry = getEntryForStatus(entry.getMailingID(), entry.getCompanyID(), entry.getStatus());
			if (existingEntry != null) {
				entry.setId(existingEntry.getId());
				logger.error("Trying to activate mailing multiple times: " + entry.getMailingID());
			}
		}
		
		if (entry.getId() == 0) {
			return insertMaildropEntry(entry);
		} else {
			final MaildropEntry existingEntry = getMaildropEntry(entry.getMailingID(), entry.getCompanyID(), entry.getId());

			if (existingEntry == null) {
				return insertMaildropEntry(entry);
			} else {
				updateMaildropEntry(entry);

				return entry.getId();
			}
		}
	}
	
	private void updateMaildropEntry(final MaildropEntry entry) {
		final String sql = "UPDATE maildrop_status_tbl SET status_field = ?, senddate = ?, step = ?, blocksize = ?, gendate = ?, genstatus = ?, genchange = ?, max_recipients = ?, admin_test_target_id = ?, optimize_mail_generation = ? WHERE status_id = ? AND company_id = ? AND mailing_id = ?";
		
		this.update(logger, sql,
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
	
	private int insertMaildropEntry(final MaildropEntry entry) {
		if (isOracleDB()) {
			final int maildropStatusId = selectInt(logger, "SELECT maildrop_status_tbl_seq.NEXTVAL FROM DUAL");
			
			update(logger, "INSERT INTO maildrop_status_tbl (status_id, company_id, status_field, mailing_id, senddate, step, blocksize, gendate, genstatus, genchange, max_recipients, admin_test_target_id, optimize_mail_generation, origin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
			
			entry.setId(maildropStatusId);
			
			return maildropStatusId;
		} else {
			final int maildropStatusId = insertIntoAutoincrementMysqlTable(logger, "status_id", "INSERT INTO maildrop_status_tbl (company_id, status_field, mailing_id, senddate, step, blocksize, gendate, genstatus, genchange, max_recipients, admin_test_target_id, optimize_mail_generation, origin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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

			entry.setId(maildropStatusId);

			return maildropStatusId;
		}
	}

	private void batchInsertMaildropEntries(int companyId, int mailingId, List<MaildropEntry> entries) {
		if (entries.isEmpty()) {
			return;
		}
		
		try {
			if (isOracleDB()) {
				String sql = "INSERT INTO maildrop_status_tbl (status_id, company_id, status_field, mailing_id, senddate, step, blocksize, gendate, genstatus, genchange, max_recipients, admin_test_target_id, optimize_mail_generation, origin) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				List<Object[]> parameterList = new ArrayList<>();
				for (MaildropEntry entry : entries) {
					final int maildropStatusId = selectInt(logger, "SELECT maildrop_status_tbl_seq.NEXTVAL FROM DUAL");
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
				
				batchupdate(logger, sql, parameterList);
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
				
				int[] ids = batchInsertIntoAutoincrementMysqlTable(logger, sql, parameterList);
				for (int i = 0; i < ids.length; i++) {
					entries.get(i).setId(ids[i]);
				}
				
			}
		} catch (Exception e) {
			logger.error("Could not insert new mail drops", e);
		}
	}
	
	private void batchUpdateMaildropEntries(int companyId, int mailingId, List<MaildropEntry> entries) {
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
		
		batchupdate(logger, "UPDATE maildrop_status_tbl SET " +
						"status_field = ?, senddate = ?, step = ?, blocksize = ?, gendate = ?, genstatus = ?, genchange = ?, " +
						"max_recipients = ?, admin_test_target_id = ?, optimize_mail_generation = ? " +
						"WHERE status_id = ? AND company_id = ? AND mailing_id = ?", parameterList);
	}
	
	@Override
	public final Collection<MaildropEntry> listMaildropStatus(final int mailingID, final int companyID) {
		final String sql = "SELECT * FROM maildrop_status_tbl WHERE mailing_id = ? AND company_id = ? AND status_field NOT LIKE '-%'";

		return select(logger, sql, MaildropRowMapper.INSTANCE, mailingID, companyID);
	}

	@Override
	public List<MaildropEntry> getMaildropStatusEntriesForMailing(int companyID, int mailingID) {
		String maildropEntrySelect = "SELECT mailing_id, company_id, status_id, status_field, senddate, step, blocksize, gendate, genchange, genstatus, admin_test_target_id, optimize_mail_generation, max_recipients FROM maildrop_status_tbl WHERE company_id = ? AND mailing_id = ?";
		return select(logger, maildropEntrySelect, MaildropRowMapper.INSTANCE, companyID, mailingID);
	}

	@Override
	public boolean setSelectedTestRecipients(@VelocityCheck int companyId, int maildropStatusId, boolean isSelected) {
		String sqlSetSelected = "UPDATE maildrop_status_tbl SET selected_test_recipients = ? WHERE company_id = ? AND status_id = ? AND status_field IN (?, ?)";
		String statusTest = Character.toString(MaildropStatus.TEST.getCode());
		String statusAdmin = Character.toString(MaildropStatus.ADMIN.getCode());

		return update(logger, sqlSetSelected, isSelected ? 1 : 0, companyId, maildropStatusId, statusTest, statusAdmin) > 0;
	}

	@Override
	public void setTestRecipients(int maildropStatusId, List<Integer> customerIds) {
		String sqlClearRecipients = "DELETE FROM test_recipients_tbl WHERE maildrop_status_id = ?";
		String sqlStoreRecipients = "INSERT INTO test_recipients_tbl (maildrop_status_id, customer_id) VALUES (?, ?)";

		update(logger, sqlClearRecipients, maildropStatusId);

		if (CollectionUtils.isNotEmpty(customerIds)) {
			List<Object[]> sqlParameters = customerIds.stream()
					.map(customerId -> new Object[] {maildropStatusId, customerId})
					.collect(Collectors.toList());

			batchupdate(logger, sqlStoreRecipients, sqlParameters);
		}
	}
	
	@Override
	public void saveMaildropEntries(int companyId, int mailingId, Set<MaildropEntry> maildropStatusList) {
		List<MaildropEntry> update = new ArrayList<>();
		List<MaildropEntry> create = new ArrayList<>();
		
		List<Integer> existingMaildropIds = getMaildropEntryIds(mailingId, companyId);
		
		maildropStatusList.forEach(entry -> {
			entry.setCompanyID(companyId);
			entry.setMailingID(mailingId);
			if (existingMaildropIds.contains(entry.getId())) {
				update.add(entry);
			} else {
				create.add(entry);
			}
		});

		batchInsertMaildropEntries(companyId, mailingId, create);
		batchUpdateMaildropEntries(companyId, mailingId, update);
	}

	@Override
	public boolean reactivateMaildropStatusEntry(int maildropStatusID) {
		return update(logger, "UPDATE maildrop_status_tbl SET genstatus = 1, genchange = CURRENT_TIMESTAMP, gendate = CURRENT_TIMESTAMP WHERE status_id = ? AND genstatus != 2", maildropStatusID) > 0;
	}

	@Override
	public final void removeOutdatedFindLastNewsletterEntries(final int companyID, final ZonedDateTime olderThan) {
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
		
		final List<Integer> idList = this.select(logger, selectSql, IntegerRowMapper.INSTANCE, companyID, timestamp);
		final List<Object[]> idArgList = idList
				.stream()
				.map(id -> new Object[] { id })
				.collect(Collectors.toList());
		
		
		final String deleteSql = "DELETE FROM maildrop_status_tbl WHERE status_id=?";
		this.batchupdate(logger, deleteSql, idArgList);
	}
	
}
