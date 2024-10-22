/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.exception;

import com.agnitas.exception.RequestErrorException;
import com.agnitas.messages.Message;

import java.util.Set;

public class AttachmentDownloadException extends RequestErrorException {
    private static final long serialVersionUID = 6429920494944339556L;

	private int mailingId;

    public AttachmentDownloadException(Set<Message> errors, int mailingId) {
        super(errors);
        this.mailingId = mailingId;
    }

    public int getMailingId() {
        return mailingId;
    }
}
