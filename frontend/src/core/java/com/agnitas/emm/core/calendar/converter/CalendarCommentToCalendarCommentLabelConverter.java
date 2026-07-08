/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.converter;

import java.util.List;

import com.agnitas.emm.core.calendar.beans.CalendarComment;
import com.agnitas.emm.core.calendar.beans.CalendarCommentLabel;
import com.agnitas.emm.core.calendar.beans.CalendarCommentRecipient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public final class CalendarCommentToCalendarCommentLabelConverter implements Converter<CalendarComment, CalendarCommentLabel> {

    @Override
    public CalendarCommentLabel convert(CalendarComment comment) {
        return new CalendarCommentLabel(
            comment.getCommentId(),
            getCommentAdminId(comment.getRecipients()),
            comment.isDeadline(),
            comment.getComment(),
            comment.getRecipientsString(),
            comment.getDate(),
            comment.getPlannedSendDate());
    }

    private static int getCommentAdminId(List<CalendarCommentRecipient> recipients) {
        if (CollectionUtils.size(recipients) != 1 || StringUtils.isNotEmpty(recipients.get(0).getAddress())) {
            return 0;
        }
        return recipients.get(0).getAdminId();
    }
}
