/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.service;

import java.time.LocalDate;
import java.util.Map;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComAdmin;

import net.sf.json.JSONArray;

public interface CalendarService {

    PaginatedListImpl<Map<String, Object>> getUnsentMailings(@VelocityCheck int companyId, int listSize);

    PaginatedListImpl<Map<String, Object>> getPlannedMailings(@VelocityCheck int companyId, int listSize);

    JSONArray getMailings(ComAdmin admin, LocalDate startDate, LocalDate endDate);

    boolean moveMailing(ComAdmin admin, int mailingId, LocalDate date);
}
