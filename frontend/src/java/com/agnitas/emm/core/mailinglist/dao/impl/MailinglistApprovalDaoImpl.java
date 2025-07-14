/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailinglist.dao.impl;

import com.agnitas.beans.Mailinglist;
import com.agnitas.emm.core.mailinglist.dao.MailinglistApprovalDao;
import com.agnitas.dao.impl.PaginatedBaseDaoImpl;
import com.agnitas.dao.impl.mapper.MailinglistRowMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MailinglistApprovalDaoImpl extends PaginatedBaseDaoImpl implements MailinglistApprovalDao {
	
	public static final MailinglistRowMapper MAILINGLIST_ROW_MAPPER = new MailinglistRowMapper();

	@Override
	public List<Mailinglist> getEnabledMailinglistsNamesForAdmin(int companyId, int adminId){
		final String sql = "SELECT m.mailinglist_id, m.company_id, m.shortname, m.description, m.creation_date, m.change_date " +
				"FROM mailinglist_tbl m " +
				"WHERE m.deleted = 0 AND m.company_id = ? " +
				"ORDER BY LOWER(m.shortname) ASC";
				
		return select(sql, new MailinglistDaoImpl.MailingListNames_RowMapper(), companyId);
	}

	@Override
	public List<Mailinglist> getEnabledMailinglistsForAdmin(int companyId, int adminId) {
		final String sql = "SELECT m.mailinglist_id, m.company_id, m.shortname, m.description, m.creation_date, m.change_date " +
				"FROM mailinglist_tbl m " +
				"WHERE m.deleted = 0 AND m.company_id = ? " +
				"ORDER BY LOWER(m.shortname) ASC";
		
		return select(sql, MAILINGLIST_ROW_MAPPER, companyId);
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
	public boolean isAdminHaveAccess(int companyId, int adminId, int mailingListId) {
		return true;
	}

	@Override
	public boolean hasAnyDisabledMailingListsForAdmin(int companyId, int adminId) {
		return false;
	}

	@Override
	public boolean hasAnyDisabledRecipientBindingsForAdmin(int companyId, int adminId, int recipientId) {
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
	public boolean deleteDisabledMailinglistsByCompany(int companyId){
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Integer> getMailinglistsWithMailinglistApproval(int companyId) {
		return new ArrayList<>();
	}
}
