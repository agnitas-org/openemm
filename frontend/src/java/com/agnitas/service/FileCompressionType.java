/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

/**
 * The Enum CompressionType.
 */
public enum FileCompressionType {
	ZIP ("zip"),
	TARGZ ("tar.gz"),
	TGZ ("tgz"),
	GZ ("gz");

	private final String defaultFileExtension;

	public String getDefaultFileExtension() {
		return defaultFileExtension;
	}

	FileCompressionType(final String defaultFileExtension) {
		this.defaultFileExtension = defaultFileExtension;
	}

	public static FileCompressionType getFromString(final String compressionTypeString) {
		for (final FileCompressionType compressionType : FileCompressionType.values()) {
			if (compressionType.name().equalsIgnoreCase(compressionTypeString)) {
				return compressionType;
			}
		}
		throw new RuntimeException("Invalid compression type: " + compressionTypeString);
	}
	
	public static FileCompressionType getFileCompressionTypeFromFileName(final String fileName) {
		if (fileName != null && fileName.toLowerCase().contains(".")) {
			for (FileCompressionType fileCompressionType : values()) {
				if (fileName.toLowerCase().endsWith("." + fileCompressionType.getDefaultFileExtension())) {
					return fileCompressionType;
				}
			}
		}
		return null;
	}
}
