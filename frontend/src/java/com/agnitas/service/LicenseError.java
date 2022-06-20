/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.util.Locale;

import com.agnitas.messages.I18nString;

public class LicenseError extends RuntimeException {
	private static final long serialVersionUID = 7201359459490150186L;
	
	private String errorMessageKey;
	private Object[] additionalErrorData;

	public LicenseError(String message, Throwable cause) {
		super(LicenseError.class.getName() + ": " + message, cause);
	}
	
	public LicenseError(String message, String allowedValue, String currentValue, Throwable cause) {
		super(LicenseError.class.getName() + ": " + message + " allowedValue: " + allowedValue + " currentValue: " + currentValue, cause);
	}
	
	public LicenseError(String message, int allowedValue, int currentValue, Throwable cause) {
		super(LicenseError.class.getName() + ": " + message + " allowedValue: " + allowedValue + " currentValue: " + currentValue, cause);
	}

	public LicenseError(String message) {
		super(LicenseError.class.getName() + ": " + message);
	}

	public LicenseError(String message, String allowedValue, String currentValue) {
		super(LicenseError.class.getName() + ": " + message + " allowedValue: " + allowedValue + " currentValue: " + currentValue);
	}

	public LicenseError(String message, int allowedValue, int currentValue) {
		super(LicenseError.class.getName() + ": " + message + " allowedValue: " + allowedValue + " currentValue: " + currentValue);
	}

	public LicenseError(String errorMessageKey, Object... additionalErrorData) {
		super(errorMessageKey);
		this.errorMessageKey = errorMessageKey;
		this.additionalErrorData = additionalErrorData;
	}

	public String getErrorKey() {
		return errorMessageKey;
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
