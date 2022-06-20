/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing;

public class MailingDataException extends Exception {
	private static final long serialVersionUID = -9091751937620504749L;

	public MailingDataException() {
		super();
	}

	public MailingDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public MailingDataException(String message) {
		super(message);
	}

	public MailingDataException(Throwable cause) {
		super(cause);
	}

}
