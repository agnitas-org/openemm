/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * When this InputStream is closed, the temporary file or directory is deleted.
 * The extra InputStream is needed because it may be some ZipInputStream etc.
 */
public class TempFileInputStream extends FilterInputStream {
	private File tempFile;
	
	public TempFileInputStream(InputStream inputStream, File tempFile) {
		super(inputStream);
		this.tempFile = tempFile;
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		if (tempFile != null && tempFile.exists()) {
			FileUtils.removeRecursively(tempFile);
		}
	}
}
