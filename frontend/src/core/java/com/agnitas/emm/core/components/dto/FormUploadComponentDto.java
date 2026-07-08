/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.dto;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public class FormUploadComponentDto {

	private MultipartFile file;

	private String fileName;

	private String description;

	private byte[] data = new byte[0];
	private boolean overwriteExisting = true;

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isOverwriteExisting() {
		return overwriteExisting;
	}

	public void setOverwriteExisting(boolean overwriteExisting) {
		this.overwriteExisting = overwriteExisting;
	}

	public byte[] getData() {
		byte[] bytes = data;

		try {
			if (file != null && !file.isEmpty()) {
				bytes = file.getBytes();
			}
		} catch (IOException e) {
			//nothing do
		}

		return bytes;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
