/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.target.exception;

import com.agnitas.emm.core.commons.exception.EntityNotFoundException;

public class TargetGroupNotFoundException extends EntityNotFoundException {

	/** Serial version UID. */
	private static final long serialVersionUID = -7078505106762900608L;
	
	private final int targetId;
	
	public TargetGroupNotFoundException(int id) {
		super("Target group", id);
		this.targetId = id;
	}

	public TargetGroupNotFoundException(String message) {
		super(message);
		this.targetId = 0;
	}
	
	public int getTargetGroupId() {
		return this.targetId;
	}

}
