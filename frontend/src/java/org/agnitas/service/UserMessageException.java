/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.util.Locale;

import com.agnitas.messages.I18nString;

public class UserMessageException extends Exception {
	private static final long serialVersionUID = -5251330971021954637L;
	
	private String errorMessageKey;
	private Object[] additionalErrorData;

	public UserMessageException(String errorMessageKey, Throwable cause) {
		super(cause);
		this.errorMessageKey = errorMessageKey;
	}

	public UserMessageException(String errorMessageKey, Object... additionalErrorData) {
		super();
		this.errorMessageKey = errorMessageKey;
		this.additionalErrorData = additionalErrorData;
	}

	public String getErrorMessageKey() {
		return errorMessageKey;
	}

	public void setErrorMessageKey(String errorMessageKey) {
		this.errorMessageKey = errorMessageKey;
	}

	public Object[] getAdditionalErrorData() {
		return additionalErrorData;
	}
	
	public String getMessage(Locale locale) {
		if (errorMessageKey != null) {
			return I18nString.getLocaleString(errorMessageKey, locale, additionalErrorData);
		} else {
			return super.getMessage();
		}
	}
	
	@Override
	public String getMessage() {
		if (errorMessageKey != null) {
			return I18nString.getLocaleString(errorMessageKey, Locale.getDefault(), additionalErrorData);
		} else {
			return super.getMessage();
		}
	}
}
