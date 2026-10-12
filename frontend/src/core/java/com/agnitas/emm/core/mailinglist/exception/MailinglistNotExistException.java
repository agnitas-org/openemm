/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.mailinglist.exception;

import com.agnitas.emm.core.commons.exception.EntityNotFoundException;

public class MailinglistNotExistException extends EntityNotFoundException {

	private static final long serialVersionUID = -1182381885122181120L;

	private final int mailinglistID;
	private final int companyID;

	public MailinglistNotExistException(final int mailinglistId, final int companyId) {
		super("Unknown mailinglist ID %d (company ID %d)".formatted(mailinglistId, companyId));
		this.mailinglistID = mailinglistId;
		this.companyID = companyId;
	}

	public int getMailinglistID() {
		return mailinglistID;
	}

	public int getCompanyID() {
		return companyID;
	}
}
