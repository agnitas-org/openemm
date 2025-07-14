/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.service;

import com.agnitas.service.JobWorker;

import com.agnitas.emm.core.reminder.service.ReminderService;

/**
 * Example Insert in DB:
 *  INSERT INTO job_queue_tbl (id, description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted)
 *    VALUES ((SELECT MAX(id) + 1 FROM job_queue_tbl), 'CalendarCommentMailingService', CURRENT_TIMESTAMP, NULL, 0, 'OK', 0, 0, '**00', CURRENT_TIMESTAMP, NULL, 'com.agnitas.emm.core.calendar.service.CalendarCommentMailingServiceJobWorker', 1);
 */
public class CalendarCommentMailingServiceJobWorker extends JobWorker {
		
	@Override
	public String runJob() {
        ReminderService reminderService = serviceLookupFactory.getBeanCalendarReminderService(); // (ReminderService) applicationContext.getBean("calendarReminderService");
        reminderService.send();
		
		return null;
    }
}
