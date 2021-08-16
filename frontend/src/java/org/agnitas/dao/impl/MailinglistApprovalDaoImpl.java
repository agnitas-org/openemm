/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.agnitas.beans.Mailinglist;
import org.agnitas.dao.MailinglistApprovalDao;
import org.agnitas.dao.impl.mapper.MailinglistRowMapper;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.log4j.Logger;

public class MailinglistApprovalDaoImpl extends PaginatedBaseDaoImpl implements MailinglistApprovalDao {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MailinglistApprovalDaoImpl.class);

	public static final Set<String> SORTABLE_FIELDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("mailinglist_id", "shortname", "description", "creation_date", "change_date")));

	public static final MailinglistRowMapper MAILINGLIST_ROW_MAPPER = new MailinglistRowMapper();

	@Override
	public List<Mailinglist> getEnabledMailinglistsNamesForAdmin(@VelocityCheck int companyId, int adminId){
		final String sql = "SELECT m.mailinglist_id, m.company_id, m.shortname, m.description, m.creation_date, m.change_date " +
				"FROM mailinglist_tbl m " +
				"WHERE m.deleted = 0 AND m.company_id = ? " +
				"ORDER BY LOWER(m.shortname) ASC";
				
		return select(logger, sql, new MailinglistDaoImpl.MailingListNames_RowMapper(), companyId);
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
	public boolean hasAnyDisabledRecipientBindingsForAdmin(@VelocityCheck int companyId, int adminId, int recipientId) {
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

	@Override
	public List<Integer> getMailinglistsWithMailinglistApproval(int companyId) {
		return new ArrayList<>();
	}
}
