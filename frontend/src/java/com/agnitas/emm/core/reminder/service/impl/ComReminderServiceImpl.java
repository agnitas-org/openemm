/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.reminder.service.impl;

import java.util.Date;
import java.util.List;

import org.agnitas.beans.CompaniesConstraints;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.reminder.beans.ComReminder;
import com.agnitas.emm.core.reminder.dao.ComReminderDao;
import com.agnitas.emm.core.reminder.service.ComReminderService;

public class ComReminderServiceImpl implements ComReminderService {
    private JavaMailService javaMailService;
    private ComReminderDao reminderDao;

    @Override
    public void send() {
        List<ComReminder> reminders = reminderDao.getReminders(new Date());
        for (ComReminder reminder : reminders) {
            reminder.setSent(send(reminder));
        }
        reminderDao.setNotified(reminders);
    }

    @Override
    public void send(CompaniesConstraints constraints) {
        List<ComReminder> reminders = reminderDao.getReminders(new Date(), constraints);
        for (ComReminder reminder : reminders) {
            reminder.setSent(send(reminder));
        }
        reminderDao.setNotified(reminders);
    }

    @Override
    public void send(int reminderId) {
        reminderDao.getReminders(reminderId).forEach(this::send);
    }

    private boolean send(ComReminder reminder) {
        String message = getMessage(reminder);
        return javaMailService.sendEmail(reminder.getCompanyId(), reminder.getRecipientEmail(), reminder.getTitle(), message, message);
    }

    private String getMessage(ComReminder reminder) {
        return reminder.getMessage() + "\n\n- " + reminder.getSenderName();
    }

    @Required
    public void setJavaMailService(JavaMailService javaMailService) {
        this.javaMailService = javaMailService;
    }

    @Required
    public void setReminderDao(ComReminderDao reminderDao) {
        this.reminderDao = reminderDao;
    }
}
