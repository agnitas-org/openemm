/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons;

public class TranslatableMessageException extends Exception {
	private static final long serialVersionUID = -3955695277647392879L;

	private static final String DEFAULT_ERROR_MSG_KEY = "Error";
	
	private String errorMsgKey = DEFAULT_ERROR_MSG_KEY;
	
	public TranslatableMessageException() {
		super();
	}
	
	public TranslatableMessageException(String errorMsgKey) {
		this.errorMsgKey = errorMsgKey;
	}
	
	public TranslatableMessageException(String errorMsgKey, String message) {
		super(message);
		this.errorMsgKey = errorMsgKey;
	}
	
	public TranslatableMessageException(Throwable cause) {
		super(cause);
	}
	
	public TranslatableMessageException(String errorMsgKey, Throwable cause) {
		super(cause);
		this.errorMsgKey = errorMsgKey;
	}
	
	public String getErrorMsgKey() {
		return errorMsgKey;
	}
}
