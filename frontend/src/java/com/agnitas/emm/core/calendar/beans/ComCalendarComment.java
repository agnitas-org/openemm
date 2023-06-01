/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.beans;


import java.util.Date;
import java.util.List;

import org.agnitas.emm.core.velocity.VelocityCheck;

public interface ComCalendarComment {

    int getCommentId();

    void setCommentId(int commentId);

    String getComment();

    void setComment(String comment);

    Date getDate();

    void setDate(Date date);

    boolean isDeadline();

    void setDeadline(boolean deadline);

    int getCompanyId();

    void setCompanyId(@VelocityCheck int companyId);

    int getAdminId();

    void setAdminId(int adminId);

    boolean isNotified();

    List<ComCalendarCommentRecipient> getRecipients();

    void setRecipients(List<ComCalendarCommentRecipient> recipients);

    String getRecipientsString();

    boolean isSendNow();

    void setSendNow(boolean sendNow);

    Date getPlannedSendDate();

    void setPlannedSendDate(Date plannedSendDate);
}
