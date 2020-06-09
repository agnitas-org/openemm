/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service;

import java.io.IOException;
import java.text.DateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.struts.upload.FormFile;

import com.agnitas.util.ImageUtils;

public interface ComMailingComponentsService {
	interface UploadStatistics {
		/**
		 * Get an overall count of found (see {@link ImageUtils#isValidImageFileExtension(String)}) image files
		 *
		 * @return files count
		 */
		int getFound();

		/**
		 * Get a count of valid (see {@link ImageUtils#isValidImage(byte[])}) image files
		 *
		 * @return files count
		 */
		int getStored();
	}

	MailingComponent getMailingTextTemplate(int mailingId, int companyID);
	
	/**
	 * Upload all valid images files from given ZIP stream. This method does not close the ZIP stream.
	 * <b>Mailing is neither saved (if successful) nor restored (in cases of errors).</b>
	 *
	 * @param mailing mailing to add the new components
	 * @param zipFile ZIP stream to read
	 * @return an instance of {@link UploadStatistics} filled with stats.
	 * @throws IOException on errors reading data
	 */
	UploadStatistics uploadZipArchive(Mailing mailing, FormFile zipFile) throws Exception;

	/**
	 * Upload valid image files (if matches {@code sftpFilePath}) from requested SFTP server.
	 *
	 * @param mailing mailing to add the new components
	 * @param fileServerAndAuthConfigString URL of the server (credentials required, base dir is optional)
	 * @param fileServerPrivateKeyString private key for SSH protocol
	 * @param sftpFilePath a remote directory and file mask (wildcards are supported)
	 * @return an instance of {@link UploadStatistics} filled with stats
	 * @throws Exception if something went wrong (unable to establish connection, network error, invalid file path requested)
	 */
	UploadStatistics uploadSFTP(Mailing mailing, String fileServerAndAuthConfigString, String fileServerPrivateKeyString, String sftpFilePath) throws Exception;

	Map<Integer, String> getImageSizes(@VelocityCheck int companyId, int mailingId);

	Map<String, Integer> getImageSizeMap(int companyId, int mailingId, boolean includeExternalImages);

	Map<Integer, String> getImageTimestamps(@VelocityCheck int companyId, int mailingId, DateFormat format);
	
	MailingComponent getComponent(int componentId, @VelocityCheck int companyID);
	
	/**
	 * Gets the mailing components by ids
	 *
	 * @param companyID the company id
	 * @param componentIds the mailing compnent id list
	 * @return the mailing components
	 */
	List<MailingComponent> getComponents(@VelocityCheck int companyID, int mailingId, Set<Integer> componentIds);

	List<MailingComponent> getComponentsByType(@VelocityCheck int companyID, int mailingId, List<Integer> types);
	
	void deleteComponent(MailingComponent component);

	void deleteComponents(@VelocityCheck int companyID, int mailingID, Set<Integer> bulkIds);
	
	void updateHostImage(int mailingID, @VelocityCheck int companyID, int componentID, byte[] imageBytes);

}
