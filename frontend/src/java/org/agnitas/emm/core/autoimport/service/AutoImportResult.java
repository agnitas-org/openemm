/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.autoimport.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoImportResult {
    private String message = null;
    private List<RemoteFile> importedFiles = new ArrayList<>();
    private Map<String, Integer> reportIdForFile = new HashMap<>();
	
	public void addImportedFile(RemoteFile importedFile) {
		importedFiles.add(importedFile);
	}
	
	public List<RemoteFile> getImportedFiles() {
		return importedFiles;
	}
	
	public void addReport(String fileName, Integer reportID) {
		reportIdForFile.put(fileName, reportID);
	}
	
	public Map<String, Integer> getReportIdForFileMap() {
		return reportIdForFile;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
