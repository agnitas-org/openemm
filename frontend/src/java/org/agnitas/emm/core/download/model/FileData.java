/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.download.model;

import jakarta.activation.MimeType;

/**
 * Data structure for download informations.
 */
// TODO: GWUA-5759: remove in case if org.agnitas.emm.core.download.web.FileDownloadServlet will be removed
public class FileData {
	
	/** Name of temporary file. */
	private final String tempFileName;
	
	/** MIME type of content. */
	private final MimeType mimeType;
	
	/** Name of file used by the client. */
	private final String downloadName;
	
	/**
	 * Creates a new FileData instance. 
	 * 
	 * @param tempFileName name of temporary file
	 * @param mimeType MIME type of content
	 * @param downloadName name of downloaded file
	 */
	public FileData( String tempFileName, MimeType mimeType, String downloadName) {
		this.tempFileName = tempFileName;
		this.mimeType = mimeType;
		this.downloadName = downloadName;
	}
	
	/**
	 * Returns the name of the temporary file.
	 * 
	 * @return name of temporary file
	 */
	public String getTempFileName() {
		return this.tempFileName;
	}
	
	/**
	 * Returns the MIME type of the content.
	 * 
	 * @return MIME type of content
	 */
	public MimeType getMimeType() {
		return this.mimeType;
	}
	
	/**
	 * Returns the filename used by the client.
	 * 
	 * @return filename of downloaded file
	 */
	public String getDownloadName() {
		return this.downloadName;
	}
}
