/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.reminder.service.impl;

import java.util.Date;
import java.util.List;

import com.agnitas.beans.CompaniesConstraints;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.reminder.beans.Reminder;
import com.agnitas.emm.core.reminder.dao.ReminderDao;
import com.agnitas.emm.core.reminder.service.ReminderService;

public class ReminderServiceImpl implements ReminderService {
    private JavaMailService javaMailService;
    private ReminderDao reminderDao;

    @Override
    public void send() {
        List<Reminder> reminders = reminderDao.getReminders(new Date());
        for (Reminder reminder : reminders) {
            reminder.setSent(send(reminder));
        }
        reminderDao.setNotified(reminders);
    }

    @Override
    public void send(CompaniesConstraints constraints) {
        List<Reminder> reminders = reminderDao.getReminders(new Date(), constraints);
        for (Reminder reminder : reminders) {
            reminder.setSent(send(reminder));
        }
        reminderDao.setNotified(reminders);
    }

    @Override
    public void send(int reminderId) {
        reminderDao.getReminders(reminderId).forEach(this::send);
    }

    private boolean send(Reminder reminder) {
        String message = getMessage(reminder);
        return javaMailService.sendEmail(reminder.getCompanyId(), reminder.getRecipientEmail(), reminder.getTitle(), message, message);
    }

    private String getMessage(Reminder reminder) {
        return reminder.getMessage() + "\n\n- " + reminder.getSenderName();
    }

    public void setJavaMailService(JavaMailService javaMailService) {
        this.javaMailService = javaMailService;
    }

    public void setReminderDao(ReminderDao reminderDao) {
        this.reminderDao = reminderDao;
    }
}
