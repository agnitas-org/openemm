/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.calendar.beans.CalendarComment;
import com.agnitas.emm.core.calendar.beans.CalendarCommentRecipient;
import com.agnitas.emm.core.calendar.dao.CalendarCommentDao;
import com.agnitas.emm.core.calendar.service.CalendarCommentService;
import com.agnitas.emm.core.reminder.service.ReminderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class CalendarCommentServiceImpl implements CalendarCommentService {

    private final CalendarCommentDao calendarCommentDao;
    private final ReminderService reminderService;

    public CalendarCommentServiceImpl(@Qualifier("calendarReminderService") ReminderService reminderService,
                                      CalendarCommentDao calendarCommentDao) {
        this.calendarCommentDao = calendarCommentDao;
        this.reminderService = reminderService;
    }

    @Override
    public List<CalendarComment> getComments(Date start, Date end, Admin admin) {
        return calendarCommentDao.getComments(start, end, admin.getCompanyID());
    }

    @Override
    public List<CalendarCommentRecipient> getRecipients(int notifyAdminId, String recipients) {
        List<CalendarCommentRecipient> result = new ArrayList<>();

        if (notifyAdminId > 0) {
            CalendarCommentRecipient recipient = calendarCommentDao.createCalendarCommentRecipient();
            recipient.setAdminId(notifyAdminId);

            result.add(recipient);
        }

        for (String address : getAddressesFromString(recipients)) {
            CalendarCommentRecipient recipient = calendarCommentDao.createCalendarCommentRecipient();
            recipient.setAddress(address);

            result.add(recipient);
        }

        return result;
    }

    private Set<String> getAddressesFromString(String stringRecipients) {
        if (StringUtils.isEmpty(stringRecipients)) {
            return Collections.emptySet();
        }

        Set<String> addresses = new HashSet<>();

        for (String address : stringRecipients.split("[;, \n]+")) {
            if (StringUtils.isNotBlank(address)) {
                addresses.add(address);
            }
        }

        return addresses;
    }

    @Override
    public int saveComment(CalendarComment comment) {
        int commentId = calendarCommentDao.saveComment(comment);

        if (comment.isSendNow()) {
            reminderService.send(commentId);
        }

        return commentId;
    }

    @Override
    public boolean removeComment(int commentId, int companyId) {
        return calendarCommentDao.deleteComment(commentId, companyId);
    }
}
