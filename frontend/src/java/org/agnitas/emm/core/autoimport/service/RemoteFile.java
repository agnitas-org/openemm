/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.autoimport.service;

import java.io.File;

public class RemoteFile {
	private RemoteFileData data = new RemoteFileData(-1);
    private ImportFileStatus status;

	public RemoteFile(String remoteFilePath, File localFile, long downloadDurationMillis) {
		this.data.remoteFilePath = remoteFilePath;
		this.data.localFile = localFile;
		this.data.downloadDurationMillis = downloadDurationMillis;
		this.status = ImportFileStatus.UPLOADED;
	}
	
	public String getRemoteFilePath() {
		// FormFile seems to escape double-quotes
		if (data == null || data.remoteFilePath == null) {
			return null;
		} else {
			return data.remoteFilePath.replace("\\\"", "\"");
		}
	}
	
	public File getLocalFile() {
		return data.localFile;
	}
	
    public long getDownloadDurationMillis() {
		return data.downloadDurationMillis;
	}

    public ImportFileStatus getStatus() {
        return status;
    }

    public void setStatus(ImportFileStatus status) {
        this.status = status;
    }

}
