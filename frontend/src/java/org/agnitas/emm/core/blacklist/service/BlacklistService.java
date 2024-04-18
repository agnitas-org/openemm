/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.blacklist.service;

import java.util.List;
import java.util.Set;

import com.agnitas.emm.core.globalblacklist.forms.BlacklistOverviewFilter;
import org.agnitas.beans.BlackListEntry;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.UserStatus;


import com.agnitas.emm.core.globalblacklist.beans.BlacklistDto;
import com.agnitas.emm.core.globalblacklist.beans.GlobalBlacklistDto;


public interface BlacklistService {

    boolean insertBlacklist(BlacklistModel model);

    boolean deleteBlacklist(BlacklistModel model);

    boolean checkBlacklist(BlacklistModel model);

    List<String> getEmailList(int companyID) throws Exception;

    List<BlackListEntry> getRecipientList(int companyID) throws Exception;

    List<Mailinglist> getMailinglistsWithBlacklistedBindings(BlacklistModel model);
    
    void updateBlacklistedBindings(BlacklistModel bm, List<Integer> mailinglists, UserStatus userStatus);

    boolean isAlreadyExist(int companyId, String email);

    boolean add(int companyId, int adminId, String email, String reason) throws Exception;

    void add(GlobalBlacklistDto globalBlacklistDto);

    boolean update(int companyId, String email, String reason);

    PaginatedListImpl<BlacklistDto> getAll(BlacklistOverviewFilter filter, int companyId);

    List<BlacklistDto> getAll(int companyId) throws Exception;

    List<Mailinglist> getBindedMailingLists(int companyId, String email);

    boolean delete(int companyId, String email, Set<Integer> mailinglistIds);
    
    boolean blacklistCheck(String email, int companyId);
    
    boolean blacklistCheckCompanyOnly(String email, int companyId);
    
    Set<String> loadBlackList(int companyId) throws Exception;
}
