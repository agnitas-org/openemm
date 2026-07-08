/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.form;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

public class CommentForm {
    private int commentId;
    private String startDate;
    private String endDate;
    private String date;
    private String plannedSendDate;
    private String comment;
    private boolean deadline;
    private boolean sendNow;
    private int notifyAdminId;
    private String recipients;

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = Objects.isNull(commentId) ? 0 : commentId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPlannedSendDate() {
        return plannedSendDate;
    }

    public void setPlannedSendDate(String plannedSendDate) {
        this.plannedSendDate = plannedSendDate;
    }

    public String getComment() {
        return Objects.isNull(comment) ? StringUtils.EMPTY : comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isDeadline() {
        return deadline;
    }

    public void setDeadline(boolean deadline) {
        this.deadline = deadline;
    }

    public boolean isSendNow() {
        return sendNow;
    }

    public void setSendNow(boolean sendNow) {
        this.sendNow = sendNow;
    }

    public int getNotifyAdminId() {
        return notifyAdminId;
    }

    public void setNotifyAdminId(int notifyAdminId) {
        this.notifyAdminId = notifyAdminId;
    }

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }
}
