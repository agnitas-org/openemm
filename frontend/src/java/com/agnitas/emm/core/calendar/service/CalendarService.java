/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.service;

import java.time.LocalDate;
import java.util.Map;

import org.agnitas.beans.impl.PaginatedListImpl;

import com.agnitas.beans.Admin;

import net.sf.json.JSONArray;

public interface CalendarService {

    PaginatedListImpl<Map<String, Object>> getUnsentMailings(Admin admin, int listSize);

    PaginatedListImpl<Map<String, Object>> getPlannedMailings(Admin admin, int listSize);

    JSONArray getMailings(Admin admin, LocalDate startDate, LocalDate endDate);

    boolean moveMailing(Admin admin, int mailingId, LocalDate date);
}
