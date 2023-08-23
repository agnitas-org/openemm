/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.util.Locale;

import com.agnitas.messages.I18nString;

public class ExportException extends RuntimeException {
	private static final long serialVersionUID = 754385941659930987L;
	
	/**
	 * In case of e.g. network errors or auth credential erros, we may retry an autoexport, if set to do so
	 */
	private boolean retryable;
	
	public enum Reason {
		FileTransferError("error.filetransfer.createFile"),
		ConnectionError("error.connection.fileserver"),
		DefinitionError("error.definition"),
		ColumnNotExportableError("error.export.dbColumn.invisible");
		
		private String messageKey;
		
		Reason(String messageKey) {
			this.messageKey = messageKey;
		}
		
		public String getMessageKey() {
			return messageKey;
		}
	}
	
	private Reason reason;
	
	private Object[] additionalData;

	public Reason getReason() {
		return reason;
	}
	
	public Object[] getAdditionalData() {
		return additionalData;
	}

	public ExportException(boolean retryable, Reason reason, Object... additionalData) {
		super();
		this.retryable = retryable;
		this.reason = reason;
		this.additionalData = additionalData;
	}
	
	public String getMessage(Locale locale) {
		if (reason != null && reason.getMessageKey() != null) {
			return I18nString.getLocaleString(reason.getMessageKey(), locale, additionalData);
		} else {
			return super.getMessage();
		}
	}
	
	@Override
	public String getMessage() {
		if (reason != null && reason.getMessageKey() != null) {
			return I18nString.getLocaleString(reason.getMessageKey(), Locale.getDefault(), additionalData);
		} else {
			return super.getMessage();
		}
	}

	public boolean isRetryable() {
		return retryable;
	}
}
