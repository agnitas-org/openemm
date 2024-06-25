/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.html;

import java.util.Collection;
import java.util.Collections;

public class HtmlCheckerException extends Exception {
	private static final long serialVersionUID = 5275806646474636474L;
	
	private final Collection<HtmlCheckerError> errors;

	public HtmlCheckerException(final Collection<HtmlCheckerError> errors) {
		super();
		
		this.errors = Collections.unmodifiableCollection(errors);
	}

	public HtmlCheckerException(final String message, final Throwable cause, final Collection<HtmlCheckerError> errors) {
		super(message, cause);
		
		this.errors = Collections.unmodifiableCollection(errors);
	}

	public HtmlCheckerException(final String message, final Collection<HtmlCheckerError> errors) {
		super(message);
		
		this.errors = Collections.unmodifiableCollection(errors);
	}

	public HtmlCheckerException(final Throwable cause, final Collection<HtmlCheckerError> errors) {
		super(cause);
		
		this.errors = Collections.unmodifiableCollection(errors);
	}
	
	public final Collection<HtmlCheckerError> getErrors() {
		return this.errors;
	}
}
