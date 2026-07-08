/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.response_inbox.bean;

import com.agnitas.emm.core.response_inbox.enums.MailloopReplyStatus;
import com.agnitas.emm.core.response_inbox.enums.MailloopReplyContentType;

import java.util.Date;

public class MailloopReplyEntry {

    private int id;
    private MailloopReplyStatus status = MailloopReplyStatus.UNREAD;
    private String subject;
    private String senderFullName;
    private String senderEmail;
    private String responseEmail;
    private Date timestamp;
    private String content;
    private MailloopReplyContentType contentType;
    private Integer customerId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MailloopReplyStatus getStatus() {
        return status;
    }

    public void setStatus(MailloopReplyStatus status) {
        this.status = status;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSenderFullName() {
        return senderFullName;
    }

    public void setSenderFullName(String senderFullName) {
        this.senderFullName = senderFullName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getResponseEmail() {
        return responseEmail;
    }

    public void setResponseEmail(String responseEmail) {
        this.responseEmail = responseEmail;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MailloopReplyContentType getContentType() {
        return contentType;
    }

    public void setContentType(MailloopReplyContentType contentType) {
        this.contentType = contentType;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getSender() {
        return senderFullName + " <" + senderEmail + ">";
    }
}
