/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.preview;

import java.util.List;

// Throw it when the TagCheckImpl decides that tag or content contains errors
public class AgnTagException extends RuntimeException {

	private static final long serialVersionUID = -4720583899796412192L;
	private List<String[]> report; // each element of the report is an array with 3 elements :  [0]=the block which contains the error(s), [1]= the tag which is wrong, [2] = an error description
	private List<String> failures;

	public AgnTagException(String message, List<String[]> report, List<String> failures) {
		super(message);
		this.report = report;
		this.failures = failures;
	}

	public AgnTagException(String message, List<String[]> report) {
		super(message);
		this.report = report;
	}

	public List<String[]> getReport() {
		return report;
	}

	public List<String> getFailures() {
		return failures;
	}

}
