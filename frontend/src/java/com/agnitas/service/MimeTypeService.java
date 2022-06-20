/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.io.IOException;
import java.io.InputStream;

import jakarta.activation.MimetypesFileTypeMap;

public class MimeTypeService {
	private static final String MIMETYPES_RESOURCE_FILE = "mimetypes";
	
	private static MimetypesFileTypeMap MIMETYPEMAP = null;
	
	public MimeTypeService() throws IOException {
		synchronized(MimeTypeService.class) {
			if(MIMETYPEMAP == null) {
				MIMETYPEMAP = parseMimetypeResourceFile(MIMETYPES_RESOURCE_FILE);
			}
		}
	}
	
	private static MimetypesFileTypeMap parseMimetypeResourceFile(String resource) throws IOException {
		try (InputStream inputStream = MimeTypeService.class.getClassLoader().getResourceAsStream(resource)) {
			return new MimetypesFileTypeMap(inputStream);
		}
	}
	
	public String getMimetypeForFile(String filename) { 
		return MIMETYPEMAP.getContentType(filename);
	}
}
