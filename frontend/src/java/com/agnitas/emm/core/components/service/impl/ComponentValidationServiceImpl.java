/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service.impl;

import static org.agnitas.beans.impl.MailingComponentImpl.COMPONENT_NAME_MAX_LENGTH;

import java.io.InputStream;
import java.util.List;

import com.agnitas.emm.validator.ApacheTikaUtils;
import org.agnitas.beans.MailingComponent;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.agnitas.emm.core.components.dto.UploadMailingAttachmentDto;
import com.agnitas.emm.core.components.form.AttachmentType;
import com.agnitas.emm.core.components.service.ComMailingComponentsService;
import com.agnitas.emm.core.components.service.ComponentValidationService;
import com.agnitas.emm.core.mimetypes.service.MimeTypeWhitelistService;
import com.agnitas.emm.core.upload.dao.ComUploadDao;
import com.agnitas.messages.Message;

@Service("ComponentValidationService")
public class ComponentValidationServiceImpl implements ComponentValidationService {

    private final ComUploadDao uploadDao;
    private final MimeTypeWhitelistService mimetypeWhitelistService;
    private final ComMailingComponentsService mailingComponentsService;
	private final ConfigService configService;

    public ComponentValidationServiceImpl(ComUploadDao uploadDao,
                                          MimeTypeWhitelistService mimetypeWhitelistService,
                                          ComMailingComponentsService mailingComponentsService,
                                          ConfigService configService) {
        this.uploadDao = uploadDao;
        this.mimetypeWhitelistService = mimetypeWhitelistService;
        this.mailingComponentsService = mailingComponentsService;
        this.configService = configService;
    }

    @Override
    public boolean validateAttachment(int companyId, int mailingId, UploadMailingAttachmentDto attachment, List<Message> errors, List<Message> warnings) {
        boolean valid;
        if (attachment.isUsePdfUpload()) {
        	valid = validatePdfUploadFields(attachment, errors);
		} else {
			valid = validateAttachmentFields(attachment, errors, warnings);
        }

        if (valid) {
            valid = validatePersonalizedAttachment(attachment, errors);
            valid &= validateUniqueName(companyId, mailingId, attachment, errors);
        }

        return valid;
	}

	private boolean validatePdfUploadFields(UploadMailingAttachmentDto attachment, List<Message> errors) {
        if (attachment.getUploadId() <= 0 || !uploadDao.exists(attachment.getUploadId())) {
            errors.add(Message.of("mailing.errors.no_attachment_pdf_file"));
            return false;
        }

        return true;
    }

	private boolean validateAttachmentFields(UploadMailingAttachmentDto attachment, List<Message> errors, List<Message> warnings) {
		MultipartFile file = attachment.getAttachmentFile();
		String attachmentName = attachment.getName();
		if (file == null || file.isEmpty()) {
            errors.add(Message.of("mailing.errors.no_attachment_file"));
            return false;
        }

        if (StringUtils.isBlank(attachmentName)) {
            errors.add(Message.of("mailing.errors.no_attachment_name"));
        }        
        
        if (attachmentName.length() > COMPONENT_NAME_MAX_LENGTH) {
            errors.add(Message.of("error.compname.too.long", attachmentName));
            return false;
        }

        if (!validateAttachmentSize(file, errors, warnings)) {
        	return false;
		}

        if (attachment.getType() == AttachmentType.PERSONALIZED) {
            if (!StringUtils.equals("text/xml", file.getContentType()) &&
                    !StringUtils.equals("text/x-xslfo", file.getContentType())) {
                errors.add(Message.of("GWUA.mailing.errors.personalized.attachment.invalidMimeType"));
                return false;
            }
        } else if (!mimetypeWhitelistService.isMimeTypeWhitelisted(file.getContentType())) {
            errors.add(Message.of("mailing.errors.attachment.invalidMimeType", file.getContentType()));
            return false;
        } else if (configService.getBooleanValue(ConfigValue.UseAdvancedFileContentTypeDetection)) {
        	// Detect mimetype by file content (not file name extension)
        	String fileContentType;
        	try (InputStream stream = file.getInputStream()) {
    	        fileContentType = ApacheTikaUtils.getContentType(stream);
    		} catch (Exception e) {
    			errors.add(Message.of("upload.file.mimetyp.detection.error", file.getOriginalFilename()));
                return false;
    		}
        	
        	if (!fileContentType.equalsIgnoreCase(file.getContentType())) {
                errors.add(Message.of("mailing.errors.attachment.invalidMimeType", file.getContentType()));
                return false;
        	}
        }

        return true;
    }

    private boolean validateAttachmentSize(MultipartFile file, List<Message> errors, List<Message> warnings) {
        int maxErrorSize = configService.getIntegerValue(ConfigValue.MaximumUploadAttachmentSize);
        if (file.getSize() > maxErrorSize) {
			errors.add(Message.of("error.component.size", FileUtils.byteCountToDisplaySize(maxErrorSize)));
			return false;
		}

        int maxWarningSize = configService.getIntegerValue(ConfigValue.MaximumWarningAttachmentSize);
        if (file.getSize() > maxWarningSize) {
            warnings.add(Message.of("warning.component.size", FileUtils.byteCountToDisplaySize(maxErrorSize)));
		}

        return true;
    }

	private boolean validateUniqueName(int companyId, int mailingId, UploadMailingAttachmentDto attachment, List<Message> errors) {
        List<MailingComponent> attachments = mailingComponentsService.getPreviewHeaderComponents(companyId, mailingId);

        boolean isNotUnique = attachments
                .stream()
                .anyMatch(component ->
                        component.getComponentName().equals(attachment.getName())
                                && component.getTargetID() != attachment.getTargetId());

        if (isNotUnique) {
            errors.add(Message.of("error.mailing.attachment.unique"));
            return false;
        }

        return true;
    }

	private boolean validatePersonalizedAttachment(UploadMailingAttachmentDto attachment, List<Message> errors) {
        if (attachment.getType() != AttachmentType.PERSONALIZED) {
            return true;
        }

        if (attachment.getBackgroundFile() == null || attachment.getBackgroundFile().isEmpty()) {
            errors.add(Message.of("GWUA.mailing.errors.no_bg_attachment_file"));
            return false;
        }

        if (!StringUtils.equals("application/pdf", attachment.getBackgroundFile().getContentType())) {
            errors.add(Message.of("error.mailing.attachment.type.invalid"));
            return false;
        }

        return true;
    }

}
