/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.components.exception;

import com.agnitas.emm.core.commons.exception.EntityNotFoundException;

public class ComponentNotExistException extends EntityNotFoundException {

	private static final long serialVersionUID = 6092682233580400555L;

	public ComponentNotExistException() {
	}

	public ComponentNotExistException(String filename, int mailingId) {
		super("Mailing component", "filename = %s, mailing ID = %d".formatted(filename, mailingId));
	}

	public ComponentNotExistException(int componentId, int mailingId) {
		super("Mailing component", "component ID = %d, mailing ID = %d".formatted(componentId, mailingId));
	}
}
