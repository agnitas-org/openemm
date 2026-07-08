/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen.resolver;

public class ReferenceTableResolveException extends Exception {
	private static final long serialVersionUID = -3318782301586996652L;

	public ReferenceTableResolveException() {
		// Do nothing
	}

	public ReferenceTableResolveException(String message) {
		super(message);
	}

	public ReferenceTableResolveException(Throwable cause) {
		super(cause);
	}

	public ReferenceTableResolveException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReferenceTableResolveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
