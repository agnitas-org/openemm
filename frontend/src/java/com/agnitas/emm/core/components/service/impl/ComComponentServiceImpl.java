/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service.impl;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.agnitas.beans.MailingComponent;
import org.agnitas.emm.core.component.service.ComponentModel;
import org.agnitas.emm.core.component.service.ComponentNotExistException;
import org.agnitas.emm.core.component.service.impl.ComponentServiceImpl;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.FileUtils;
import org.agnitas.util.ZipUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.FormComponent;
import com.agnitas.beans.FormComponent.FormComponentType;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.components.dto.FormComponentDto;
import com.agnitas.emm.core.components.dto.FormUploadComponentDto;
import com.agnitas.emm.core.components.service.ComComponentService;
import com.agnitas.messages.Message;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.ImageUtils;

public abstract class ComComponentServiceImpl extends ComponentServiceImpl implements ComComponentService {
	private static final transient Logger logger = Logger.getLogger(ComComponentServiceImpl.class);
	
	public static final int THUMBNAIL_WIDTH = 56;
	public static final int THUMBNAIL_HEIGHT = 79;

	private ExtendedConversionService conversionService;

	@Override
	@Transactional
	public int addComponent(ComponentModel model) throws Exception {
		modelValidator.assertIsValidToAdd(model);
		int res = addComponentImpl(model);
        ((ComMailingDao)mailingDao).updateStatus(model.getMailingId(), "edit");
        return res;
	}

	@Override
	@Transactional
	public void deleteComponent(ComponentModel model) {
		modelValidator.assertIsValidToGetOrDelete(model);
		deleteComponentImpl(model);
	    ((ComMailingDao)mailingDao).updateStatus(model.getMailingId(), "edit");
	}

	@Override
	@Transactional
	public void updateMailingContent(ComponentModel model) throws Exception {
		modelValidator.assertIsValidToUpdateMailingContentGroup(model);
		if (!mailingDao.exist(model.getMailingId(), model.getCompanyId())) {
			throw new MailingNotExistException();
		}

		MailingComponent component = mailingComponentDao.getMailingComponentByName(model.getMailingId(), model.getCompanyId(), model.getComponentName());
		if (component == null) {
			throw new ComponentNotExistException();
		}

		// Change mime type if and only if mime type is set in model. Otherwise, take mime type of component.
		final String mimeType = model.getMimeType() != null ? model.getMimeType() : component.getMimeType();
		
		component.setEmmBlock(new String(model.getData(), "UTF-8"), mimeType);

		mailingComponentDao.saveMailingComponent(component);
	}

	@Override
	public int addMailingComponent(MailingComponent mailingComponent) throws Exception {
		mailingComponentDao.saveMailingComponent(mailingComponent);
		return mailingComponent.getId();
	}

	@Override
	public boolean saveFormComponent(FormComponent formComponent) {
		if (formComponent.getType() == FormComponentType.IMAGE) {
			// Create thumbnail for this image
			FormComponent thumbnailComponent = new FormComponent();
			thumbnailComponent.setType(FormComponentType.THUMBNAIL);
			thumbnailComponent.setCompanyID(formComponent.getCompanyID());
			thumbnailComponent.setFormID(formComponent.getFormID());
			thumbnailComponent.setName(formComponent.getName());
			thumbnailComponent.setDescription("Thumbnail for " + formComponent.getName());
			thumbnailComponent.setMimeType(formComponent.getMimeType());
			thumbnailComponent.setCompanyID(formComponent.getCompanyID());
			thumbnailComponent.setCompanyID(formComponent.getCompanyID());
			
			ByteArrayInputStream inputStream = null;
			ByteArrayOutputStream outputStream = null;
			try {
				inputStream = new ByteArrayInputStream(formComponent.getData());
				outputStream = new ByteArrayOutputStream();
				BufferedImage imageFullSize = ImageIO.read(inputStream);
				formComponent.setWidth(imageFullSize.getWidth());
				formComponent.setHeight(imageFullSize.getHeight());
				
				// Keep aspect ratio of original image in thumbnail
				int thumbnailWidth = formComponent.getWidth();
				int thumbnailHeight = formComponent.getHeight();
				double aspectRatio = thumbnailWidth / (double) thumbnailHeight;
				if (thumbnailWidth > THUMBNAIL_WIDTH) {
					thumbnailWidth = THUMBNAIL_WIDTH;
					thumbnailHeight = (int) Math.round(thumbnailWidth / aspectRatio);
				}
				if (thumbnailHeight > THUMBNAIL_HEIGHT) {
					thumbnailWidth = (int) Math.round(thumbnailHeight * aspectRatio);
					thumbnailHeight = THUMBNAIL_HEIGHT;
				}
				
				BufferedImage imageThumbnail = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
				imageThumbnail.createGraphics().drawImage(imageFullSize.getScaledInstance(thumbnailWidth, thumbnailHeight, BufferedImage.SCALE_SMOOTH), 0, 0, null);
				ImageIO.write(imageThumbnail, AgnUtils.getFileExtension(formComponent.getName()), outputStream);
				thumbnailComponent.setData(outputStream.toByteArray());
				thumbnailComponent.setWidth(thumbnailWidth);
				thumbnailComponent.setHeight(thumbnailHeight);
			} catch (Exception e) {
				logger.error("Cannot create thumbnail for image", e);
				return false;
			} finally {
				IOUtils.closeQuietly(outputStream);
				IOUtils.closeQuietly(inputStream);
			}

			boolean success = formComponentDao.saveFormComponent(formComponent);
			
			if (success) {
				// Delete old thumbnail for this image by name
				formComponentDao.deleteFormComponent(formComponent.getCompanyID(), formComponent.getFormID(), formComponent.getName(), FormComponentType.THUMBNAIL);
				
				success = formComponentDao.saveFormComponent(thumbnailComponent);
			}
			return success;
		} else {
			return formComponentDao.saveFormComponent(formComponent);
		}
	}

	@Override
	public FormComponent getFormComponent(int formID, int companyID, String imageFileName, FormComponentType componentType) {
		return formComponentDao.getFormComponent(formID, companyID, imageFileName, componentType);
	}

	@Override
	public List<FormComponent> getFormComponentDescriptions(int companyID, int formID, FormComponentType componentType) {
		return formComponentDao.getFormComponentDescriptions(companyID, formID, componentType);
	}

	@Override
	public boolean deleteFormComponent(int formID, int companyID, String componentName) {
		return formComponentDao.deleteFormComponent(companyID, formID, componentName, null);
	}

	@Override
	public List<FormComponent> getFormComponents(int companyID, int formID) {
		return formComponentDao.getFormComponents(companyID, formID);
	}

	@Override
	public List<FormComponentDto> getFormImageComponents(@VelocityCheck int companyID, int formId) {
		List<FormComponent> components = getFormComponentDescriptions(companyID, formId, FormComponentType.IMAGE);
		return conversionService.convert(components, FormComponent.class, FormComponentDto.class);
	}

	@Override
	public Map<String, byte[]> getImageComponentsData(int companyId, int formId) {
		List<FormComponent> components = formComponentDao.getFormComponents(companyId, formId, Collections.singletonList(FormComponentType.IMAGE));
		return components.stream().collect(Collectors.toMap(FormComponent::getName, FormComponent::getData));
	}

	@Override
	public File getComponentArchive(String zipName, Map<String, byte[]> formComponentsData) {
		try {
			File componentsZip = File.createTempFile(zipName, ".zip", AgnUtils.createDirectory(AgnUtils.getTempDir()));
			if (MapUtils.isNotEmpty(formComponentsData)) {
				try (OutputStream stream = new FileOutputStream(componentsZip);
					 ZipOutputStream zipStream = ZipUtilities.openNewZipOutputStream(stream)) {
					zipStream.setLevel(ZipOutputStream.STORED);

					for (Map.Entry<String, byte[]> entry : formComponentsData.entrySet()) {
						String name = entry.getKey();
						byte[] data = entry.getValue();
						ZipUtilities.addFileDataToOpenZipFileStream(data, name, zipStream);
					}
				}

			}
			return componentsZip;
		} catch (Exception e) {
			logger.error("Write component to ZIP failed", e);
		}
		return null;
	}

	@Override
	public SimpleServiceResult saveFormComponents(ComAdmin admin, int formId, List<FormComponent> components, List<UserAction> userActions) {
		return saveFormComponents(admin, formId, components, userActions, true);
	}

	@Override
	public SimpleServiceResult saveFormComponents(ComAdmin admin, int formId, List<FormComponent> components, List<UserAction> userActions, boolean overwriteExisting) {
		if (formId == 0) {
			logger.error("Cannot save or change globally used images (formID = 0)");
			return new SimpleServiceResult(false, Message.of("Error"));
		}

		int companyId = admin.getCompanyID();

		try {
			List<String> erroneousFiles = new ArrayList<>();
			List<String> duplicateNames = new ArrayList<>();
			List<String> actionDescriptions = new ArrayList<>();
			for (FormComponent imageComponent : components) {
				String name = imageComponent.getName();
				if (overwriteExisting) {
					formComponentDao.deleteFormComponent(companyId, formId, name, null);
				} else if (formComponentDao.exists(formId, companyId, name)) {
					duplicateNames.add(name);
					continue;
				}

				FormComponent thumbnail = makeComponentThumbnail(imageComponent);
				boolean success = formComponentDao.saveFormComponent(companyId, formId, imageComponent, thumbnail);

				if (success) {
					actionDescriptions.add(String.format("Form Component ID: %d, Compnent Name: %s", imageComponent.getId(), name));
				} else {
					erroneousFiles.add(name);
				}
			}

			actionDescriptions.stream()
					.map(des -> new UserAction("upload form component", des))
					.forEach(userActions::add);

			List<Message> warnings = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(erroneousFiles)) {
				warnings.add(Message.of("FilesWithError", StringUtils.join(erroneousFiles, ", ")));
			}

			if (CollectionUtils.isNotEmpty(duplicateNames)) {
				warnings.add(Message.of("FilenameDuplicate", StringUtils.join(duplicateNames, ", ")));
			}

			return new SimpleServiceResult(true,
					Collections.emptyList(),
					warnings,
					Collections.emptyList());
		} catch (Exception e) {
			logger.error("Could not save form components", e);
		}

		return new SimpleServiceResult(false, Message.of("Error"));
	}

	@Override
	public List<Message> validateComponents(List<FormUploadComponentDto> components, boolean checkDuplicate) {
		List<String> existingNames = new ArrayList<>();
		List<String> invalidNames = new ArrayList<>();
		List<String> duplicateNames = new ArrayList<>();
		List<String> invalidFormat = new ArrayList<>();
		for (FormUploadComponentDto component : components) {
			String name = component.getFileName();
			if (StringUtils.isBlank(name) || !FileUtils.isValidFileName(name)) {
				invalidNames.add(name);
			} else if (checkDuplicate && existingNames.contains(name)) {
				duplicateNames.add(name);
			} else {
				existingNames.add(name);
			}

			if (!ImageUtils.isValidImageFileExtension(AgnUtils.getFileExtension(name))) {
				invalidFormat.add(name);
			}
		}

		List<Message> errors = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(invalidNames)) {
			errors.add(Message.of("FilenameNotValid", StringUtils.join(invalidNames, ", ")));
		}

		if (CollectionUtils.isNotEmpty(invalidFormat)) {
			errors.add(Message.of("FilenameNotValid", StringUtils.join(invalidFormat, ", ")));
		}

		if (CollectionUtils.isNotEmpty(duplicateNames)) {
			errors.add(Message.of("FilenameDuplicate", StringUtils.join(duplicateNames, ", ")));
		}

		return errors;
	}

	@Override
	public SimpleServiceResult saveComponentsFromZipFile(ComAdmin admin, int formId, MultipartFile zipFile, List<UserAction> userActions, boolean overwriteExisting) {

		try {
			List<FormUploadComponentDto> components = new ArrayList<>();
			try (InputStream stream = zipFile.getInputStream();
				 ZipInputStream zipStream = ZipUtilities.openZipInputStream(stream)) {

				ZipEntry entry;

				while ((entry = zipStream.getNextEntry()) != null) {
					String name = entry.getName();
					byte[] data = IOUtils.toByteArray(zipStream);

					FormUploadComponentDto componentDto = new FormUploadComponentDto();
					componentDto.setData(data);
					componentDto.setFileName(name);
					componentDto.setDescription("Zip upload");
					components.add(componentDto);
				}
			}

			List<Message> errors = validateComponents(components, false);
			if (errors.isEmpty()) {
				List<FormComponent> componentList = conversionService.convert(components, FormUploadComponentDto.class, FormComponent.class);
				return saveFormComponents(admin, formId, componentList, userActions, overwriteExisting);
			}

			return new SimpleServiceResult(false, errors);
		} catch (IOException e) {
			return new SimpleServiceResult(false, Message.of("error.unzip"));
		} catch (Exception e) {
			logger.error("Error occurred: " + e.getMessage(), e);
			return new SimpleServiceResult( false, Message.of("Error"));
		}
	}

	private FormComponent makeComponentThumbnail(FormComponent imageComponent) {
		FormComponent thumbnail = new FormComponent();
		thumbnail.setName(imageComponent.getName());
		thumbnail.setDescription("Thumbnail for " + imageComponent.getName());
		thumbnail.setType(FormComponentType.THUMBNAIL);
		thumbnail.setMimeType(imageComponent.getMimeType());
		thumbnail.setCreationDate(imageComponent.getCreationDate());
		thumbnail.setChangeDate(imageComponent.getChangeDate());

		byte[] thumbnailData = new byte[0];
		int width = 0, height = 0;

		byte[] origin =  imageComponent.getData();
		int originWidth = imageComponent.getWidth(), originHeight = imageComponent.getHeight();

		if (originWidth <= THUMBNAIL_WIDTH && originHeight <= THUMBNAIL_HEIGHT) {
			thumbnailData = origin;
			width = originWidth;
			height = originHeight;
		} else {
			try (InputStream originStream = new ByteArrayInputStream(origin)) {
				BufferedImage originImage = ImageIO.read(originStream);

				double factorX = THUMBNAIL_WIDTH / (double) originWidth;
				double factorY = THUMBNAIL_HEIGHT / (double) originHeight;

				width = THUMBNAIL_WIDTH;
				height = THUMBNAIL_HEIGHT;

				if (factorX < factorY) {
					height = (int) Math.round(originHeight * factorX);
				} else if (factorX > factorY) {
					width = (int) Math.round(originWidth * factorY);
				}

				try (ByteArrayOutputStream scaledStream = new ByteArrayOutputStream()) {
					Image image = originImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
					BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

					Graphics2D graphics = scaledImage.createGraphics();
					graphics.drawImage(image, 0, 0, null);
					graphics.dispose();

					ImageIO.write(scaledImage, AgnUtils.getFileExtension(imageComponent.getName()), scaledStream);

					thumbnailData = scaledStream.toByteArray();
				}
			} catch (Exception e) {
				logger.error("Cannot create thumbnail for image", e);
			}
		}

		thumbnail.setData(thumbnailData);
		thumbnail.setWidth(width);
		thumbnail.setHeight(height);

		return thumbnail;
	}

	@Required
	public void setConversionService(ExtendedConversionService conversionService) {
		this.conversionService = conversionService;
	}
}
