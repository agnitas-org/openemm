/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.service.impl;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.calendar.beans.ComCalendarComment;
import com.agnitas.emm.core.calendar.beans.ComCalendarCommentRecipient;
import com.agnitas.emm.core.calendar.dao.ComCalendarCommentDao;
import com.agnitas.emm.core.calendar.service.CalendarCommentService;
import com.agnitas.emm.core.reminder.service.ComReminderService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CalendarCommentServiceImpl implements CalendarCommentService {
    private static final String DD_MM_YYYY = "dd-MM-yyyy";
    private static final String DD_MM_YYYY_HH_MM = "dd-MM-yyyy HH:mm";

    private ComCalendarCommentDao calendarCommentDao;
    private ComReminderService reminderService;
    private AdminService adminService;

    @Override
    public JSONArray getComments(Admin admin, LocalDate startDate, LocalDate endDate) {
        ZoneId zoneId = AgnUtils.getZoneId(admin);
        Date start = DateUtilities.toDate(startDate, zoneId);
        Date end = DateUtilities.toDate(endDate, zoneId);

        return commentsAsJson(calendarCommentDao.getComments(start, end, admin.getCompanyID()), admin);
    }

    private JSONArray commentsAsJson(List<ComCalendarComment> comments, Admin admin) {
        JSONArray array = new JSONArray();

        TimeZone timezone = AgnUtils.getTimeZone(admin);
        DateFormat dateFormat = DateUtilities.getFormat(DD_MM_YYYY, timezone);
        DateFormat dateTimeFormat = DateUtilities.getFormat(DD_MM_YYYY_HH_MM, timezone);

        Map<Integer, String> adminNameMap = adminService.getAdminsNamesMap(admin.getCompanyID());

        for (ComCalendarComment comment : comments) {
            JSONObject object = new JSONObject();

            object.element("recipients", admin.isRedesignedUiUsed() ? comment.getRecipientsString() : getRecipients(comment));
            if (admin.isRedesignedUiUsed()) {
                object.element("notifyAdminId", getCommentAdminId(comment.getRecipients()));
            }
            object.element("commentId", comment.getCommentId());
            object.element("comment", comment.getComment());
            object.element("adminId", comment.getAdminId());
            object.element("adminName", adminNameMap.get(comment.getAdminId()));
            object.element("deadline", comment.isDeadline());
            object.element("notified", comment.isNotified());
            object.element("date", DateUtilities.format(comment.getDate(), dateFormat));
            object.element(admin.isRedesignedUiUsed() ? "plannedSendDate" : "plannedSendDates", DateUtilities.format(comment.getPlannedSendDate(), dateTimeFormat));

            array.add(object);
        }

        return array;
    }

    private static int getCommentAdminId(List<ComCalendarCommentRecipient> recipients) {
        if (recipients.size() != 1 || StringUtils.isNotEmpty(recipients.get(0).getAddress())) {
            return 0;
        }
        return recipients.get(0).getAdminId();
    }

    private JSONObject getRecipients(ComCalendarComment comment) {
        List<ComCalendarCommentRecipient> recipients = comment.getRecipients();
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

        result.element("value", recipientValue);
        result.element("adminId", adminId);

        return result;
    }

    @Override
    public List<ComCalendarCommentRecipient> getRecipients(int notifyAdminId, String recipients) {
        List<ComCalendarCommentRecipient> result = new ArrayList<>();

        if (notifyAdminId > 0) {
            ComCalendarCommentRecipient recipient = calendarCommentDao.createCalendarCommentRecipient();
            recipient.setAdminId(notifyAdminId);

            result.add(recipient);
        }

        for (String address : getAddressesFromString(recipients)) {
            ComCalendarCommentRecipient recipient = calendarCommentDao.createCalendarCommentRecipient();
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
    public int saveComment(ComCalendarComment comment) {
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

    @Required
    public void setCalendarCommentDao(ComCalendarCommentDao calendarCommentDao) {
        this.calendarCommentDao = calendarCommentDao;
    }

    @Required
    public void setReminderService(ComReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Required
    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }
}
