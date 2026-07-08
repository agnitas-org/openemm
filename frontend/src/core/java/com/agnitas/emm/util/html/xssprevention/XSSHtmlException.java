/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.html.xssprevention;

import java.util.Collection;
import java.util.Collections;

public class XSSHtmlException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = -1447753156652715853L;
	
	private final Collection<HtmlCheckError> errors;

	public XSSHtmlException(final Collection<HtmlCheckError> errors) {
		super();
		
		this.errors = Collections.unmodifiableCollection(errors);
	}

	public XSSHtmlException(final String message, final Throwable cause, final Collection<HtmlCheckError> errors) {
		super(message, cause);
		
		this.errors = Collections.unmodifiableCollection(errors);
	}

	public XSSHtmlException(final String message, final Collection<HtmlCheckError> errors) {
		super(message);
		
		this.errors = Collections.unmodifiableCollection(errors);
	}

	public XSSHtmlException(final Throwable cause, final Collection<HtmlCheckError> errors) {
		super(cause);
		
		this.errors = Collections.unmodifiableCollection(errors);
	}
	
	public final Collection<HtmlCheckError> getErrors() {
		return this.errors;
	}

}
