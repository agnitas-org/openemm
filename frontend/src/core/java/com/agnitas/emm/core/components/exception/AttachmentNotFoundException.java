/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.components.exception;

import com.agnitas.emm.core.commons.exception.EntityNotFoundException;

public class AttachmentNotFoundException extends EntityNotFoundException {

	public AttachmentNotFoundException(String filename, int mailingId) {
		super("Mailing attachment", "filename = %s, mailing ID = %d".formatted(filename, mailingId));
	}

	public AttachmentNotFoundException(int attachmentId, int mailingId) {
		super("Mailing attachment", "attachment ID = %d, mailing ID = %d".formatted(attachmentId, mailingId));
	}
}
