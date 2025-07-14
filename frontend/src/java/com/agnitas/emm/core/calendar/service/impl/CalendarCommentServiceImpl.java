/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.service.impl;

import java.text.DateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.calendar.beans.CalendarComment;
import com.agnitas.emm.core.calendar.beans.CalendarCommentRecipient;
import com.agnitas.emm.core.calendar.dao.CalendarCommentDao;
import com.agnitas.emm.core.calendar.service.CalendarCommentService;
import com.agnitas.emm.core.reminder.service.ReminderService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class CalendarCommentServiceImpl implements CalendarCommentService {
    private static final String DD_MM_YYYY = "dd-MM-yyyy";
    private static final String DD_MM_YYYY_HH_MM = "dd-MM-yyyy HH:mm";

    private final CalendarCommentDao calendarCommentDao;
    private final ReminderService reminderService;
    private final AdminService adminService;

    public CalendarCommentServiceImpl(@Qualifier("calendarReminderService") ReminderService reminderService,
                                      CalendarCommentDao calendarCommentDao, AdminService adminService) {
        this.calendarCommentDao = calendarCommentDao;
        this.reminderService = reminderService;
        this.adminService = adminService;
    }

    @Override
    public JSONArray getComments(Admin admin, LocalDate startDate, LocalDate endDate) {
        Date start = DateUtilities.toDate(startDate, admin.getZoneId());
        Date end = DateUtilities.toDate(endDate, admin.getZoneId());

        return commentsAsJson(calendarCommentDao.getComments(start, end, admin.getCompanyID()), admin);
    }

    @Override
    public List<CalendarComment> getComments(Date start, Date end, Admin admin) {
        return calendarCommentDao.getComments(start, end, admin.getCompanyID());
    }

    private JSONArray commentsAsJson(List<CalendarComment> comments, Admin admin) {
        JSONArray array = new JSONArray();

        TimeZone timezone = AgnUtils.getTimeZone(admin);
        DateFormat dateFormat = DateUtilities.getFormat(DD_MM_YYYY, timezone);
        DateFormat dateTimeFormat = DateUtilities.getFormat(DD_MM_YYYY_HH_MM, timezone);

        Map<Integer, String> adminNameMap = adminService.getAdminsNamesMap(admin.getCompanyID());

        for (CalendarComment comment : comments) {
            JSONObject object = new JSONObject();

            object.put("recipients", admin.isRedesignedUiUsed() ? comment.getRecipientsString() : getRecipients(comment));
            if (admin.isRedesignedUiUsed()) {
                object.put("notifyAdminId", getCommentAdminId(comment.getRecipients()));
            }
            object.put("commentId", comment.getCommentId());
            object.put("comment", comment.getComment());
            object.put("adminId", comment.getAdminId());
            object.put("adminName", adminNameMap.get(comment.getAdminId()));
            object.put("deadline", comment.isDeadline());
            object.put("notified", comment.isNotified());
            object.put("date", DateUtilities.format(comment.getDate(), dateFormat));
            if (admin.isRedesignedUiUsed()) {
                object.put("plannedSendDate", DateUtilities.format(comment.getPlannedSendDate(), admin.getDateTimeFormat()));
            } else {
                object.put("plannedSendDates", DateUtilities.format(comment.getPlannedSendDate(), dateTimeFormat));
            }
            array.put(object);
        }

        return array;
    }

    private static int getCommentAdminId(List<CalendarCommentRecipient> recipients) {
        if (CollectionUtils.size(recipients) != 1 || StringUtils.isNotEmpty(recipients.get(0).getAddress())) {
            return 0;
        }
        return recipients.get(0).getAdminId();
    }

    private JSONObject getRecipients(CalendarComment comment) {
        List<CalendarCommentRecipient> recipients = comment.getRecipients();
        JSONObject result = new JSONObject();
        String recipientValue = StringUtils.EMPTY;
        int adminId = 0;

        if (CollectionUtils.isNotEmpty(recipients)) {
            if (recipients.size() == 1 && StringUtils.isEmpty(recipients.get(0).getAddress())) {
                adminId = recipients.get(0).getAdminId();
            } else {
                recipientValue = comment.getRecipientsString();
            }
        }

        result.put("value", recipientValue);
        result.put("adminId", adminId);

        return result;
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
