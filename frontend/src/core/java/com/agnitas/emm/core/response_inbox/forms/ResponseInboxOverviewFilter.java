/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.response_inbox.forms;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.response_inbox.enums.MailloopReplyStatus;
import com.agnitas.web.forms.PaginationForm;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ResponseInboxOverviewFilter extends PaginationForm {

    private String subject;
    private String sender;
    private String responseAddress;
    private DateRange timestamp = new DateRange();
    private MailloopReplyStatus status;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getResponseAddress() {
        return responseAddress;
    }

    public void setResponseAddress(String responseAddress) {
        this.responseAddress = responseAddress;
    }

    public DateRange getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateRange timestamp) {
        this.timestamp = timestamp;
    }

    public MailloopReplyStatus getStatus() {
        return status;
    }

    public void setStatus(MailloopReplyStatus status) {
        this.status = status;
    }

    public boolean isUiFiltersSet() {
        return isNotBlank(subject) || isNotBlank(sender) || isNotBlank(responseAddress)
                || timestamp.isPresent() || status != null;
    }
}
