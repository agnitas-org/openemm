/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.agnitas.messages.I18nString;

/**
 * This class descripes an error for a localized message texts.
 */
public class ImportError extends RuntimeException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8437787275630740539L;

	/**
	 * The Enum ImportErrorKey.
	 */
	public enum ImportErrorKey {
		mappingMustContainKeyColumn("error.mappingMustContainKeyColumn"),
		invalidMapping("error.invalidMapping"),
		csvContainsInvalidColumn("error.csvContainsInvalidColumn"),
		cannotReadImportFile("error.cannotReadImportFile");
		
		/**
		  * Instantiates a new error key.
		  *
		  * @param messageKey the message key
		  */
		private ImportErrorKey(String messageKey) {
			this.messageKey = messageKey;
		}
		
		/** The message key. */
		private final String messageKey;
		
		/**
		 * Gets the message key.
		 *
		 * @return the message key
		 */
		public String getMessageKey() {
			return messageKey;
		}
	}
	
	/** The error key. */
	private ImportErrorKey errorKey;
	
	/** The additional error data. */
	private Object[] additionalErrorData;

	/**
	 * Instantiates a new error.
	 * Used for interim data of paramereter errors
	 *
	 * @param errorKey the error key
	 * @param additionalErrorData the additional error data
	 */
	public ImportError(ImportErrorKey errorKey, Object... additionalErrorData) {
		this.additionalErrorData = additionalErrorData;
		this.errorKey = errorKey;
	}
	
	/**
	 * Gets the localized message.
	 *
	 * @param locale the locale
	 * @return the localized message
	 */
	public String getLocalizedMessage(Locale locale) {
		return I18nString.getLocaleString(errorKey.getMessageKey(), locale, getAdditionalErrorData());
	}
	
	@Override
	public String getMessage() {
		return I18nString.getLocaleString(errorKey.getMessageKey(), Locale.getDefault(), getAdditionalErrorData());
	}

	/**
	 * Gets the error key.
	 *
	 * @return the error key
	 */
	public ImportErrorKey getErrorKey() {
		return errorKey;
	}

	/**
	 * Gets the additional error data.
	 *
	 * @return the additional error data
	 */
	public Object[] getAdditionalErrorData() {
		return additionalErrorData;
	}

	public void setAdditionalErrorData(String[] additionalErrorData) {
		this.additionalErrorData = additionalErrorData;
	}
	
	@Override
	public String toString() {
		return "ImportErrorKey: " + errorKey + (additionalErrorData != null && additionalErrorData.length > 0 ? " AdditionalErrorData : " + StringUtils.join(additionalErrorData, ", ") : "");
	}
}
