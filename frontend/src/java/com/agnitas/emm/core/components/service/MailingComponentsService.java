/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.MailingComponentType;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.components.dto.MailingAttachmentDto;
import com.agnitas.emm.core.components.dto.MailingImageDto;
import com.agnitas.emm.core.components.dto.UpdateMailingAttachmentDto;
import com.agnitas.emm.core.components.dto.UploadMailingAttachmentDto;
import com.agnitas.emm.core.components.dto.UploadMailingImageDto;
import com.agnitas.emm.core.components.form.MailingImagesOverviewFilter;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.ImageUtils;
import com.agnitas.web.forms.PaginationForm;

public interface MailingComponentsService {

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

	Map<String, Integer> getImageSizeMap(int companyId, int mailingId, boolean includeExternalImages);

	SimpleServiceResult uploadMailingAttachment(Admin admin, int mailingId, UploadMailingAttachmentDto attachment);

	SimpleServiceResult updateMailingAttachments(Admin admin, int mailingId, Map<Integer, UpdateMailingAttachmentDto> attachments);

	/**
	 * Gets the mailing components by ids
	 *
	 * @param companyID the company id
	 * @param componentIds the mailing component id list
	 * @return the mailing components
	 */
	List<MailingComponent> getComponents(int companyID, int mailingId, Set<Integer> componentIds);

    List<String> getMailingImagesNamesForMobileAlternative(int mailingId, int companyId);

    PaginatedList<MailingImageDto> getMailingImagesOverview(int companyId, int mailingId, MailingImagesOverviewFilter filter);

    ServiceResult<ImportStatistics> uploadImages(Admin admin, int mailingId, List<UploadMailingImageDto> images, List<UserAction> userActions);
    
	List<MailingComponent> getPreviewHeaderComponents(int companyId, int mailingId);

	PaginatedList<MailingAttachmentDto> getAttachmentsOverview(PaginationForm form, int mailingId, int companyId);

	Map<String, String> getUrlsByNamesForEmmImages(Admin admin, int mailingId);

    List<MailingComponent> getMailingComponents(int mailingId, int companyId, MailingComponentType componentType, boolean includeContent);

	boolean deleteImages(int companyId, int mailingId, Set<Integer> bulkIds);

	ServiceResult<Boolean> reloadImage(Admin admin, int mailingId, int componentId);

	boolean updateHostImage(int mailingID, int companyID, int componentID, byte[] imageBytes);

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
	ServiceResult<ImportStatistics> importImagesFromSftp(Admin admin, int mailingId, String sftpServerAndAuthConfigString, String sftpPrivateKeyString, String sftpFilePath, List<UserAction> userActions);

    Map<Integer, String> getUploadsByExtension(Admin admin);

    boolean validatePdfUploadFields(UploadMailingAttachmentDto attachment, List<Message> errors);

    void updateMailingMediapoolImagesReferences(int mailingId, int companyId, Set<String> mediapoolImagesNames);

	List<String> getImagesNames(int mailingId, Set<Integer> ids, Admin admin);

	List<String> getImagesNames(int mailingId, int companyId);

	List<String> getNames(Set<Integer> ids, int mailingId, Admin admin);

	ServiceResult<UserAction> delete(Set<Integer> ids, int mailingId, Admin admin);

	File getZipToDownload(Set<Integer> ids, int mailingId, Admin admin);

}
