/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.response_inbox.forms;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.response_inbox.enums.MailloopReplyStatus;
import com.agnitas.web.forms.FormSearchParams;

public class ResponseInboxFormSearchParams implements FormSearchParams<ResponseInboxOverviewFilter> {

    private String subject;
    private String sender;
    private String responseAddress;
    private DateRange timestamp = new DateRange();
    private MailloopReplyStatus status;

    @Override
    public void storeParams(ResponseInboxOverviewFilter filter) {
        this.subject = filter.getSubject();
        this.sender = filter.getSender();
        this.responseAddress = filter.getResponseAddress();
        this.timestamp = filter.getTimestamp();
        this.status = filter.getStatus();
    }

    @Override
    public void restoreParams(ResponseInboxOverviewFilter filter) {
        filter.setSubject(subject);
        filter.setSender(sender);
        filter.setResponseAddress(responseAddress);
        filter.setTimestamp(timestamp);
        filter.setStatus(status);
    }

}
