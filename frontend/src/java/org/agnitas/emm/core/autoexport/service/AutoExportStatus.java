/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.autoexport.service;

import org.agnitas.emm.core.autoimport.service.RemoteFile;

public class AutoExportStatus {
	private int fields;
	private int exportedCount;
	private long fileSize;
	private RemoteFile remoteFile;
	
	public AutoExportStatus(int fields, int exportedCount, long fileSize, RemoteFile remoteFile) {
		this.fields = fields;
		this.exportedCount = exportedCount;
		this.fileSize = fileSize;
		this.remoteFile = remoteFile;
	}

	public int getFields() {
		return fields;
	}
	
	public void setFields(int fields) {
		this.fields = fields;
	}
	
	public int getExportedCount() {
		return exportedCount;
	}
	
	public void setExportedCount(int exportedCount) {
		this.exportedCount = exportedCount;
	}
	
	public long getFileSize() {
		return fileSize;
	}
	
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	
	public RemoteFile getRemoteFile() {
		return remoteFile;
	}
	
	public void setRemoteFile(RemoteFile remoteFile) {
		this.remoteFile = remoteFile;
	}
}
