/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.importquota.common;

import java.time.ZonedDateTime;
import java.util.Objects;

public final class ImportSize {
	
	private final int companyID;
	private final int importID;
	private final ImportType type;
	private final ZonedDateTime timestamp;
	private final int lineCount;
	
	public ImportSize(final int companyID, final int importID, final ImportType type, final ZonedDateTime timestamp, final int lineCount) {
		this.companyID = companyID;
		this.importID = importID;
		this.type = type; // Can be null for unknown import types
		this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
		this.lineCount = lineCount;
	}

	public final int getCompanyID() {
		return companyID;
	}

	public final int getImportID() {
		return importID;
	}

	public final ImportType getType() {
		return type;
	}

	public final ZonedDateTime getTimestamp() {
		return timestamp;
	}

	public final int getLineCount() {
		return lineCount;
	}

}
