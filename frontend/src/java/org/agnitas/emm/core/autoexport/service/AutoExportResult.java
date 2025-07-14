/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.autoexport.service;

import org.agnitas.emm.core.autoimport.service.RemoteFile;

public final class AutoExportResult {
    private String message = null;
    private RemoteFile exportedFile = null;
    private int reportId;
	
	public void setExportedFile(final RemoteFile exportedFile) {
		this.exportedFile = exportedFile;
	}
	
	public RemoteFile getExportedFile() {
		return exportedFile;
	}
	
	public void setReportId(final int reportId) {
		this.reportId = reportId;
	}

	public final int getReportId() {
		return this.reportId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

}
