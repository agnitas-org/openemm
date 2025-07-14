/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

public class CsvDataInvalidItemCountException extends CsvDataException {
	private static final long serialVersionUID = -8783673456414186072L;

	private int expected;
	private int actual;

	public CsvDataInvalidItemCountException(String message, int errorLineNumber, int expected, int actual) {
		super(message, errorLineNumber);
		this.expected = expected;
		this.actual = actual;
	}

	public int getExpected() {
		return expected;
	}

	public int getActual() {
		return actual;
	}
}
