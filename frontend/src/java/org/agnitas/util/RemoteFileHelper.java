/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.Closeable;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

public interface RemoteFileHelper extends Closeable {
	public void connect() throws Exception;
	public void cd(String path) throws Exception;
	public List<String> ls(String path) throws Exception;
	public boolean directoryExists(String directoryPath) throws Exception;
	public boolean fileExists(String filePath) throws Exception;
	public void put(InputStream inputStream, String destination, boolean useTempFileNameWhileUploading) throws Exception;
	public InputStream get(String name) throws Exception;
	public Date getModifyDate(String filePathAndName) throws Exception;
	public long getFileSize(String filePathAndName) throws Exception;
}
