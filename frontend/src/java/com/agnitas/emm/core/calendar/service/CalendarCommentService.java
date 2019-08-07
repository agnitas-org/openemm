/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.service;

import java.time.LocalDate;
import java.util.List;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.calendar.beans.ComCalendarComment;
import com.agnitas.emm.core.calendar.beans.ComCalendarCommentRecipient;

import net.sf.json.JSONArray;

public interface CalendarCommentService {

    JSONArray getComments(ComAdmin admin, LocalDate startDate, LocalDate endDate);

    List<ComCalendarCommentRecipient> getRecipients(int notifyAdminId, String recipients);

    int saveComment(ComCalendarComment comment);

    boolean removeComment(int commentId, @VelocityCheck int companyId);
}
