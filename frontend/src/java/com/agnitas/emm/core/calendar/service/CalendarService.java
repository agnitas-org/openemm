/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.emm.core.calendar.beans.CalendarMailingLabel;
import com.agnitas.emm.core.calendar.beans.CalendarUnsentMailing;
import com.agnitas.emm.core.calendar.beans.MailingPopoverInfo;
import com.agnitas.emm.core.calendar.form.DashboardCalendarForm;
import com.agnitas.emm.core.mailing.bean.MailingDto;
import com.agnitas.emm.core.mailing.dao.MailingDaoOptions;
import org.json.JSONArray;

public interface CalendarService {

    PaginatedListImpl<Map<String, Object>> getUnsentMailings(Admin admin, int listSize);

    List<CalendarUnsentMailing> getUnplannedMailings(Admin admin);

    List<CalendarUnsentMailing> getPlannedUnsentMailings(Admin admin);

    List<MailingDto> getUnsentUnplannedMailings(Admin admin);

    List<MailingDto> getUnsentPlannedMailings(Admin admin);

    PaginatedListImpl<Map<String, Object>> getPlannedMailings(Admin admin, int listSize);

    List<MailingPopoverInfo> mailingsPopoverInfo(Set<Integer> mailingIds, Admin admin);

    JSONArray getMailings(Admin admin, LocalDate startDate, LocalDate endDate, int limit);

    JSONArray getMailingsLight(Admin admin, LocalDate startDate, LocalDate endDate);

    List<MailingDto> getMailings(MailingDaoOptions opts, Admin admin);

    boolean moveMailing(Admin admin, int mailingId, LocalDate date);

    boolean clearMailingPlannedDate(int mailingId, int companyId);

    Map<String, List<?>> getLabels(DashboardCalendarForm form, Admin admin);

    List<CalendarMailingLabel> getMailingLabels(Date start, Date end, int limit, Admin admin);
}
