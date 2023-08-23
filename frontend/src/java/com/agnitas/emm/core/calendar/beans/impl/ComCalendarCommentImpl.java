/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.beans.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.emm.core.calendar.beans.ComCalendarComment;
import com.agnitas.emm.core.calendar.beans.ComCalendarCommentRecipient;


public class ComCalendarCommentImpl implements ComCalendarComment {

    private int commentId;
    private int companyId;
    private int adminId;
    private List<ComCalendarCommentRecipient> recipients;
    private String comment;
    private Date date;
    private boolean deadline;
    private boolean sendNow;
    private Date plannedSendDate;

    @Override
	public int getCommentId() {
        return commentId;
    }

    @Override
	public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    @Override
	public String getComment() {
        return comment;
    }

    @Override
	public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
	public Date getDate() {
        return date;
    }

    @Override
	public void setDate(Date date) {
        this.date = date;
    }

    @Override
	public boolean isDeadline() {
        return deadline;
    }

    @Override
	public void setDeadline(boolean deadline) {
        this.deadline = deadline;
    }

    @Override
	public int getCompanyId() {
        return companyId;
    }

    @Override
	public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    @Override
	public int getAdminId() {
        return adminId;
    }

    @Override
	public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    @Override
	public boolean isNotified() {
        if (CollectionUtils.isEmpty(recipients)) {
            return false;
        }

        for (ComCalendarCommentRecipient recipient : recipients) {
            if (!recipient.isNotified()) {
                return false;
            }
        }
        return true;
    }

    @Override
	public List<ComCalendarCommentRecipient> getRecipients() {
        return recipients;
    }

    @Override
	public void setRecipients(List<ComCalendarCommentRecipient> recipients) {
        this.recipients = recipients;
    }

    @Override
	public String getRecipientsString() {
        if (CollectionUtils.isEmpty(recipients)) {
            return "";
        }

        List<String> addresses = recipients.stream()
                .map(ComCalendarCommentRecipient::getAddress)
                .collect(Collectors.toList());

        return StringUtils.join(addresses, ", ");
    }

    @Override
    public boolean isSendNow() {
        return sendNow;
    }

    @Override
    public void setSendNow(boolean sendNow) {
        this.sendNow = sendNow;
    }

    @Override
    public Date getPlannedSendDate() {
        return plannedSendDate;
    }

    @Override
    public void setPlannedSendDate(Date plannedSendDate) {
        this.plannedSendDate = plannedSendDate;
    }
}
