/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.MailinglistImpl;
import org.agnitas.dao.MailinglistApprovalDao;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.emm.core.mailinglist.bean.MailinglistEntry;

public class MailinglistApprovalDaoImpl extends PaginatedBaseDaoImpl implements MailinglistApprovalDao {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MailinglistApprovalDaoImpl.class);

	public static final Set<String> SORTABLE_FIELDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("mailinglist_id", "shortname", "description", "creation_date", "change_date")));

	public static final MailinglistEntryRowMapper MAILING_LIST_ENTRY_ROW_MAPPER = new MailinglistEntryRowMapper();
	
	public static final Mailinglist_RowMapper MAILINGLIST_ROW_MAPPER = new Mailinglist_RowMapper();

	@Override
	public List<Mailinglist> getEnabledMailinglistsNamesForAdmin(@VelocityCheck int companyId, int adminId){
		final String sql = "SELECT m.mailinglist_id, m.company_id, m.shortname, m.description, m.creation_date, m.change_date " +
				"FROM mailinglist_tbl m " +
				"WHERE m.deleted = 0 AND m.company_id = ? " +
				"ORDER BY LOWER(m.shortname) ASC";
				
		return select(logger, sql, new MailingListNames_RowMapper(), companyId);
	}

	@Override
	public List<Mailinglist> getEnabledMailinglistsForAdmin(@VelocityCheck int companyId, int adminId) {
		final String sql = "SELECT m.mailinglist_id, m.company_id, m.shortname, m.description, m.creation_date, m.change_date " +
				"FROM mailinglist_tbl m " +
				"WHERE m.deleted = 0 AND m.company_id = ? " +
				"ORDER BY LOWER(m.shortname) ASC";
		
		return select(logger, sql, MAILINGLIST_ROW_MAPPER, companyId);
	}

	@Override
	public List<Integer> getDisabledMailinglistsForAdmin(int companyId, int adminId) {
		return new ArrayList<>();
	}

	@Override
	public List<Integer> getAdminsDisallowedToUseMailinglist(int companyId, int mailinglistId) {
		return new ArrayList<>();
	}

	@Override
	public boolean isAdminHaveAccess(@VelocityCheck int companyId, int adminId, int mailingListId) {
		return true;
	}

	@Override
	public boolean hasAnyDisabledMailingListsForAdmin(@VelocityCheck int companyId, int adminId) {
		return false;
	}

	@Override
	public boolean disallowAdminToUseMailinglists(int companyId, int adminID, Collection<Integer> mailinglistIds) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean disallowAdminsToUseMailinglist(int companyId, int mailinglistID, Collection<Integer> adminIds){
		throw new UnsupportedOperationException();
	}

	@Override
	public void allowAdminToUseMailinglists(int companyId, int adminId, Collection<Integer> mailinglistIds){
		throw new UnsupportedOperationException();
	}

	@Override
	public void allowAdminsToUseMailinglist(int companyId, int mailinglistId, Collection<Integer> adminIds){
		throw new UnsupportedOperationException();
	}

	@Override
	public void allowAdminToUseAllMailinglists(int companyId, int adminId){
		throw new UnsupportedOperationException();
	}

	@Override
	public void allowAllAdminsToUseMailinglist(int companyId, int mailinglistId){
		throw new UnsupportedOperationException();
	}

	public static class MailinglistRowMapper implements RowMapper<Mailinglist> {

		private final String columnPrefix;

		public MailinglistRowMapper() {
			this.columnPrefix = StringUtils.EMPTY;
		}

		public MailinglistRowMapper(String columnPrefix) {
			this.columnPrefix = StringUtils.defaultString(columnPrefix, StringUtils.EMPTY);
		}

		@Override
		public Mailinglist mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			Mailinglist mailinglist = new MailinglistImpl();

			mailinglist.setId(resultSet.getInt(columnPrefix + "mailinglist_id"));
			mailinglist.setCompanyID(resultSet.getInt(columnPrefix + "company_id"));
			mailinglist.setShortname(resultSet.getString(columnPrefix + "shortname"));
			mailinglist.setDescription(resultSet.getString(columnPrefix + "description"));
			mailinglist.setCreationDate(resultSet.getTimestamp(columnPrefix + "creation_date"));
			mailinglist.setChangeDate(resultSet.getTimestamp(columnPrefix + "change_date"));
			mailinglist.setRemoved(resultSet.getBoolean(columnPrefix + "deleted"));

			return mailinglist;
		}
	}

	public static class Mailinglist_RowMapper implements RowMapper<Mailinglist> {
		@Override
		public Mailinglist mapRow(ResultSet resultSet, int row) throws SQLException {
			Mailinglist readItem = new MailinglistImpl();
			
			readItem.setId(resultSet.getInt("mailinglist_id"));
			readItem.setCompanyID(resultSet.getInt("company_id"));
			readItem.setShortname(resultSet.getString("shortname"));
			readItem.setDescription(resultSet.getString("description"));
			readItem.setCreationDate(resultSet.getTimestamp("creation_date"));
			readItem.setChangeDate(resultSet.getTimestamp("change_date"));
			
			return readItem;
		}
	}

	public static class MailingListNames_RowMapper implements RowMapper<Mailinglist> {
		@Override
		public Mailinglist mapRow(ResultSet resultSet, int i) throws SQLException {
			Mailinglist mailing = new MailinglistImpl();

			mailing.setId(resultSet.getInt("mailinglist_id"));
			mailing.setShortname(resultSet.getString("shortname"));

			return mailing;
		}
	}

	public static class MailinglistEntryRowMapper implements RowMapper<MailinglistEntry> {

		@Override
		public MailinglistEntry mapRow(ResultSet resultSet, int i) throws SQLException {
			MailinglistEntry mailinglistEntry = new MailinglistEntry();
			mailinglistEntry.setId(resultSet.getInt("mailinglist_id"));
			mailinglistEntry.setShortname(resultSet.getString("shortname"));
			mailinglistEntry.setDescription(resultSet.getString("description"));
			mailinglistEntry.setCreationDate(resultSet.getDate("creation_date"));
			mailinglistEntry.setChangeDate(resultSet.getDate("change_date"));

			return mailinglistEntry;
		}
	}

}
