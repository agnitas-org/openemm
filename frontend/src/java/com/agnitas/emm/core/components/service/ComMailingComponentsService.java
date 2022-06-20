/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service;

import java.text.DateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingComponentType;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.components.dto.MailingImageDto;
import com.agnitas.emm.core.components.dto.NewFileDto;
import com.agnitas.emm.core.components.dto.UpdateMailingAttachmentDto;
import com.agnitas.emm.core.components.dto.UploadMailingAttachmentDto;
import com.agnitas.emm.core.components.dto.UploadMailingImageDto;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.ImageUtils;

public interface ComMailingComponentsService {
	interface ImportStatistics {

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

	Map<Integer, String> getImageSizes(@VelocityCheck int companyId, int mailingId);

	Map<String, Integer> getImageSizeMap(int companyId, int mailingId, boolean includeExternalImages);

	long getAttachmentsSize(int companyId, int mailingId);

	Map<Integer, String> getImageTimestamps(@VelocityCheck int companyId, int mailingId, DateFormat format);

	MailingComponent getComponent(int componentId, @VelocityCheck int companyID);

	MailingComponent getComponent(@VelocityCheck int companyId, int mailingId, int componentId);

	SimpleServiceResult uploadMailingAttachment(ComAdmin admin, int mailingId, UploadMailingAttachmentDto attachment);

	SimpleServiceResult updateMailingAttachments(ComAdmin admin, int mailingId, Map<Integer, UpdateMailingAttachmentDto> attachments);

	/**
	 * Gets the mailing components by ids
	 *
	 * @param companyID the company id
	 * @param componentIds the mailing component id list
	 * @return the mailing components
	 */
	List<MailingComponent> getComponents(@VelocityCheck int companyID, int mailingId, Set<Integer> componentIds);

	List<MailingComponent> getComponents(@VelocityCheck int companyId, int mailingId, boolean includeContent);

    List<MailingImageDto> getMailingImages(int companyId, int mailingId);

    ServiceResult<ImportStatistics> uploadImages(ComAdmin admin, int mailingId, List<UploadMailingImageDto> images, List<UserAction> userActions);
    
	List<MailingComponent> getComponentsByType(@VelocityCheck int companyID, int mailingId, List<MailingComponentType> types);

	List<MailingComponent> getPreviewHeaderComponents(@VelocityCheck int companyId, int mailingId);

	Map<String, String> getUrlsByNamesForEmmImages(ComAdmin admin, int mailingId);

	void deleteComponent(int companyId, int mailingId, int componentId);

	void deleteComponent(MailingComponent component);

	boolean deleteImages(@VelocityCheck int companyId, int mailingId, Set<Integer> bulkIds);

	ServiceResult<Boolean> reloadImage(ComAdmin admin, int mailingId, int componentId);

	boolean updateHostImage(int mailingID, @VelocityCheck int companyID, int componentID, byte[] imageBytes);

	/**
	 * Import valid image files (also from and ZIP-archives) to mailing components.
	 *
	 * @param admin current user.
	 * @param mailingId an identifier of a mailing to import images to.
	 * @param newFiles a list of files to be imported (may contain image files and ZIP-archives).
	 * @param userActions a list of user actions to store one if succeeded (for UAL).
	 * @return an instance of {@link ServiceResult}.
	 */
	ServiceResult<ImportStatistics> importImagesBulk(ComAdmin admin, int mailingId, List<NewFileDto> newFiles, List<UserAction> userActions);

	/**
	 * Import valid image files (if matches {@code sftpFilePath}) from requested SFTP server to mailing components.
	 *
	 * @param admin current user.
	 * @param mailingId an identifier of a mailing to import images to.
	 * @param sftpServerAndAuthConfigString URL of the server (credentials required, base dir is optional).
	 * @param sftpPrivateKeyString private key for SSH protocol.
	 * @param sftpFilePath a remote directory and file mask (wildcards are supported).
	 * @param userActions a list of user actions to store one if succeeded (for UAL).
	 * @return an instance of {@link ServiceResult}.
	 */
	ServiceResult<ImportStatistics> importImagesFromSftp(ComAdmin admin, int mailingId, String sftpServerAndAuthConfigString, String sftpPrivateKeyString, String sftpFilePath, List<UserAction> userActions);
}
