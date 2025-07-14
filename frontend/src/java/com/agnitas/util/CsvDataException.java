/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

public class CsvDataException extends Exception {
	private static final long serialVersionUID = 5128483227215628395L;
	
	private int errorLineNumber = -1;
	
	public CsvDataException(int errorLineNumber) {
		super();
		this.errorLineNumber = errorLineNumber;
	}

	public CsvDataException(String message, int errorLineNumber, Throwable cause) {
		super(message, cause);
		this.errorLineNumber = errorLineNumber;
	}

	public CsvDataException(String message, int errorLineNumber) {
		super(message);
		this.errorLineNumber = errorLineNumber;
	}

	public CsvDataException(int errorLineNumber, Throwable cause) {
		super(cause);
		this.errorLineNumber = errorLineNumber;
	}

	public int getErrorLineNumber() {
		return errorLineNumber;
	}
}
