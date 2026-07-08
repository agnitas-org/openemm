/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.response_inbox.service;

import com.agnitas.emm.core.response_inbox.bean.MailloopReplyEntry;
import com.agnitas.emm.core.response_inbox.forms.ResponseInboxOverviewFilter;
import com.agnitas.beans.PaginatedList;

import java.util.List;
import java.util.Set;

public interface MailloopReplyService {

    PaginatedList<MailloopReplyEntry> getOverviewList(ResponseInboxOverviewFilter filter, int companyId);

    MailloopReplyEntry getReply(int id, int companyID);

    void markAsRead(int id, int companyID);

    List<Integer> findSendersIds(MailloopReplyEntry replyEntry, int companyId);

    List<MailloopReplyEntry> getReplies(Set<Integer> ids, int companyID);

    void delete(Set<Integer> ids, int companyID);

    boolean deleteByCompany(int companyId);

    boolean existsForMailloop(int mailloopId);

    void deleteForMailloop(int mailloopId, int companyId);

    void deleteExpired(int companyID);
}
