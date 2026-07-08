/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.auto_import.bean;

import java.io.File;

import com.agnitas.emm.core.auto_import.enums.ImportFileStatus;

public class RemoteFile {

	private final String remoteFilePath;
	private final File localFile;
	private final long downloadDurationMillis;
    private ImportFileStatus status;

	public RemoteFile(String remoteFilePath, File localFile, long downloadDurationMillis) {
		this.remoteFilePath = remoteFilePath;
		this.localFile = localFile;
		this.downloadDurationMillis = downloadDurationMillis;
		this.status = ImportFileStatus.UPLOADED;
	}
	
	public String getRemoteFilePath() {
		// FormFile seems to escape double-quotes
		if (remoteFilePath == null) {
			return null;
		} else {
			return remoteFilePath.replace("\\\"", "\"");
		}
	}
	
	public File getLocalFile() {
		return localFile;
	}
	
    public long getDownloadDurationMillis() {
		return downloadDurationMillis;
	}

    public ImportFileStatus getStatus() {
        return status;
    }

    public void setStatus(ImportFileStatus status) {
        this.status = status;
    }

}
