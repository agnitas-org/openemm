/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.response_inbox.service.impl;

import com.agnitas.emm.core.response_inbox.bean.MailloopReplyEntry;
import com.agnitas.emm.core.response_inbox.dao.MailloopReplyDao;
import com.agnitas.emm.core.response_inbox.forms.ResponseInboxOverviewFilter;
import com.agnitas.emm.core.response_inbox.service.MailloopReplyService;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.recipient.service.RecipientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service("mailloopReplyService")
public class MailloopReplyServiceImpl implements MailloopReplyService {

    private final RecipientService recipientService;
    private final ConfigService configService;
    private final MailloopReplyDao replyDao;

    @Autowired
    public MailloopReplyServiceImpl(RecipientService recipientService, ConfigService configService, MailloopReplyDao replyDao) {
        this.recipientService = recipientService;
        this.configService = configService;
        this.replyDao = replyDao;
    }

    @Override
    public PaginatedList<MailloopReplyEntry> getOverviewList(ResponseInboxOverviewFilter filter, int companyId) {
        return replyDao.getOverviewList(filter, companyId);
    }

    @Override
    public MailloopReplyEntry getReply(int id, int companyID) {
        if (id <= 0) {
            return null;
        }

        return replyDao.getReply(id, companyID);
    }

    @Override
    public List<MailloopReplyEntry> getReplies(Set<Integer> ids, int companyID) {
        return replyDao.getReplies(ids, companyID);
    }

    @Override
    public void delete(Set<Integer> ids, int companyID) {
        replyDao.delete(ids, companyID);
    }

    @Override
    public boolean deleteByCompany(int companyId) {
        return replyDao.deleteByCompany(companyId);
    }

    @Override
    public void markAsRead(int id, int companyID) {
        replyDao.markAsRead(id, companyID);
    }

    @Override
    public boolean existsForMailloop(int mailloopId) {
        return replyDao.existsForMailloop(mailloopId);
    }

    @Override
    public void deleteForMailloop(int mailloopId, int companyId) {
        replyDao.deleteForMailloop(mailloopId, companyId);
    }

    @Override
    public void deleteExpired(int companyID) {
        ZonedDateTime expiredDate = ZonedDateTime.now()
                .minusDays(configService.getIntegerValue(ConfigValue.ResponseInbox_RetentionDays, companyID));

        replyDao.deleteExpired(companyID, Date.from(expiredDate.toInstant()));
    }

    @Override
    public List<Integer> findSendersIds(MailloopReplyEntry replyEntry, int companyId) {
        Integer customerId = replyEntry.getCustomerId();
        if (customerId != null && recipientService.recipientExists(companyId, customerId)) {
            return List.of(customerId);
        }

        return recipientService.findIdsByEmail(replyEntry.getSenderEmail(), companyId);
    }
}
