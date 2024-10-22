/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.FormComponent;
import com.agnitas.beans.FormComponent.FormComponentType;
import com.agnitas.dao.FormComponentDao;
import com.agnitas.dao.MailingComponentDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.components.dto.FormComponentDto;
import com.agnitas.emm.core.components.dto.FormUploadComponentDto;
import com.agnitas.emm.core.components.service.ComponentService;
import com.agnitas.emm.core.userform.form.UserFormImagesOverviewFilter;
import com.agnitas.messages.Message;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.ImageUtils;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.factory.MailingComponentFactory;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.MailingStatus;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.component.service.ComponentAlreadyExistException;
import org.agnitas.emm.core.component.service.ComponentModel;
import org.agnitas.emm.core.component.service.ComponentNotExistException;
import org.agnitas.emm.core.component.service.validation.ComponentModelValidator;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.FileUtils;
import org.agnitas.util.ZipUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ComponentServiceImpl implements ComponentService {
	
	private static final Logger logger = LogManager.getLogger(ComponentServiceImpl.class);

	private static final int THUMBNAIL_WIDTH = 56;
	private static final int THUMBNAIL_HEIGHT = 79;

	private ExtendedConversionService conversionService;
	private ConfigService configService;
	private MailingComponentDao mailingComponentDao;
	private ComponentModelValidator modelValidator;
	private MailingComponentFactory mailingComponentFactory;
	private FormComponentDao formComponentDao;
	private MailingDao mailingDao;

	@Override
	@Transactional
	public List<MailingComponent> getComponents(ComponentModel model) {
		modelValidator.assertIsValidToGetOrDelete(model);
		if (!mailingDao.exist(model.getMailingId(), model.getCompanyId())) {
			throw new MailingNotExistException(model.getCompanyId(), model.getMailingId());
		}
		return mailingComponentDao.getMailingComponents(model.getMailingId(), model.getCompanyId(), model.getComponentType());
	}

	@Override
	@Transactional
	public MailingComponent getComponent(ComponentModel model) {
		modelValidator.assertIsValidToGetOrDelete(model);
		MailingComponent component = mailingComponentDao.getMailingComponent(model.getComponentId(), model.getCompanyId());
		if (component == null || component.getType() != model.getComponentType()) {
			throw new ComponentNotExistException();
		}
		return component;
	}

	protected int addComponentImpl(ComponentModel model) throws Exception {
		MailingComponent component = mailingComponentFactory.newMailingComponent();
		if (!mailingDao.exist(model.getMailingId(), model.getCompanyId())) {
			throw new MailingNotExistException(model.getCompanyId(), model.getMailingId());
		}
		if (null != mailingComponentDao.getMailingComponentByName(model.getMailingId(), model.getCompanyId(), model.getComponentName())) {
			throw new ComponentAlreadyExistException();
		}

		component.setCompanyID(model.getCompanyId());
		component.setMailingID(model.getMailingId());
		component.setType(model.getComponentType());
		component.setComponentName(model.getComponentName());
		component.setBinaryBlock(model.getData(), model.getMimeType());

		mailingComponentDao.saveMailingComponent(component);
		return component.getId();
	}

	@Override
	@Transactional
	public void updateComponent(ComponentModel model) throws Exception {
		modelValidator.assertIsValidToUpdateGroup(model);
		MailingComponent component = mailingComponentDao.getMailingComponent(model.getComponentId(), model.getCompanyId());
		if (component == null || component.getType() != model.getComponentType()) {
			throw new ComponentNotExistException();
		}
		if (!component.getComponentName().equals(model.getComponentName())
				&& null != mailingComponentDao.getMailingComponentByName(component.getMailingID(), component.getCompanyID(), model.getComponentName())) {
			throw new ComponentAlreadyExistException();
		}
		component.setComponentName(model.getComponentName());
		if (model.getData() != null) {
			component.setBinaryBlock(model.getData(), model.getMimeType());
		}

		mailingComponentDao.saveMailingComponent(component);
	}

	@Override
	public boolean deleteFormComponent(int formID, int companyID, String componentName) {
		return formComponentDao.deleteFormComponent(companyID, formID, componentName, null);
	}

	@Override
	public FormComponent getFormComponent(int formID, int companyID, String imageFileName, FormComponent.FormComponentType componentType) {
		return formComponentDao.getFormComponent(formID, companyID, imageFileName, componentType);
	}

	protected void deleteComponentImpl(ComponentModel model) {
		MailingComponent component = mailingComponentDao.getMailingComponent(model.getComponentId(), model.getCompanyId());
		if (component == null || component.getType() != model.getComponentType()) {
			throw new ComponentNotExistException();
		}
		mailingComponentDao.deleteMailingComponent(component);
	}

	@Override
	@Transactional
	public int addComponent(ComponentModel model) throws Exception {
		modelValidator.assertIsValidToAdd(model);
		int res = addComponentImpl(model);
        mailingDao.updateStatus(model.getCompanyId(), model.getMailingId(), MailingStatus.EDIT, null);
        return res;
	}

	@Override
	@Transactional
	public void deleteComponent(ComponentModel model) {
		modelValidator.assertIsValidToGetOrDelete(model);
		deleteComponentImpl(model);
	    mailingDao.updateStatus(model.getCompanyId(), model.getMailingId(), MailingStatus.EDIT, null);
	}

	@Override
	@Transactional
	public void updateMailingContent(ComponentModel model) throws Exception {
		modelValidator.assertIsValidToUpdateMailingContentGroup(model);
		if (!mailingDao.exist(model.getMailingId(), model.getCompanyId())) {
			throw new MailingNotExistException(model.getCompanyId(), model.getMailingId());
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
		if (!mailingDao.exist(mailingComponent.getMailingID(), mailingComponent.getCompanyID())) {
			throw new MailingNotExistException(mailingComponent.getCompanyID(), mailingComponent.getMailingID());
		}
		if (null != mailingComponentDao.getMailingComponentByName(mailingComponent.getMailingID(), mailingComponent.getCompanyID(), mailingComponent.getComponentName())) {
			throw new ComponentAlreadyExistException();
		}

		mailingComponentDao.saveMailingComponent(mailingComponent);
		return mailingComponent.getId();
	}

	@Override
	public PaginatedListImpl<FormComponentDto> getFormImageComponents(UserFormImagesOverviewFilter filter, int companyID, int formId) {
		initFilterData(filter, companyID, formId);

		PaginatedListImpl<FormComponent> components = formComponentDao.getFormComponentOverview(filter);
		return conversionService.convertPaginatedList(components, FormComponent.class, FormComponentDto.class);
	}

	@Override
	public Map<String, byte[]> getImageComponentsData(UserFormImagesOverviewFilter filter, Set<Integer> ids, int companyId, int formId) {
		initFilterData(filter, companyId, formId);
		List<FormComponent> components = formComponentDao.getFormComponents(ids, filter);
		return components.stream().collect(Collectors.toMap(FormComponent::getName, FormComponent::getData));
	}

	private void initFilterData(UserFormImagesOverviewFilter filter, int companyID, int formId) {
		filter.setCompanyId(companyID);
		filter.setFormId(formId);
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
	public List<String> getComponentFileNames(Set<Integer> bulkIds, int formId, int companyID) {
		return formComponentDao.getComponentFileNames(bulkIds, formId, companyID);
	}

	@Override
	public void delete(Set<Integer> bulkIds, int formId, Admin admin) {
		formComponentDao.delete(bulkIds, formId, admin.getCompanyID());
	}

	@Override
	public SimpleServiceResult saveFormComponents(Admin admin, int formId, List<FormComponent> components, List<UserAction> userActions) {
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
				if (imageComponent.isOverwriteExisting()) {
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
			} else if (!ImageUtils.isValidImage(component.getData(), configService.getBooleanValue(ConfigValue.UseAdvancedFileContentTypeDetection))) {
				invalidFormat.add(name);
			}
		}

		List<Message> errors = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(invalidNames)) {
			errors.add(Message.of("FilenameNotValid", StringUtils.join(invalidNames, ", ")));
		}

		if (CollectionUtils.isNotEmpty(invalidFormat)) {
			errors.add(Message.of("FileformatNotValidWithAllowedTypes", StringUtils.join(invalidFormat, ", "), StringUtils.join(ImageUtils.availableImageExtensions, ", ")));
		}

		if (CollectionUtils.isNotEmpty(duplicateNames)) {
			errors.add(Message.of("FilenameDuplicate", StringUtils.join(duplicateNames, ", ")));
		}

		return errors;
	}

	@Override
	// TODO: remove after EMMGUI-714 will be finished and old design will be removed
	public SimpleServiceResult saveComponentsFromZipFile(Admin admin, int formId, MultipartFile zipFile, List<UserAction> userActions, boolean overwriteExisting) {
		try {
			ServiceResult<List<FormUploadComponentDto>> readResult = readComponentsFromZipFile(zipFile);
			if (!readResult.isSuccess()) {
				return new SimpleServiceResult(false, readResult.getErrorMessages());
			}

			List<FormUploadComponentDto> components = readResult.getResult();
			components.forEach(c -> c.setOverwriteExisting(overwriteExisting));

			List<Message> errors = validateComponents(components, false);
			if (errors.isEmpty()) {
				List<FormComponent> componentList = conversionService.convert(components, FormUploadComponentDto.class, FormComponent.class);
				return saveFormComponents(admin, formId, componentList, userActions);
			}

			return new SimpleServiceResult(false, errors);
		} catch (Exception e) {
			logger.error("Error occurred: " + e.getMessage(), e);
			return new SimpleServiceResult( false, Message.of("Error"));
		}
	}

	@Override
	public ServiceResult<List<FormUploadComponentDto>> readComponentsFromZipFile(MultipartFile zipFile) {
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
		} catch (IOException e) {
			return ServiceResult.error(Message.of("error.unzip"));
		}

		return ServiceResult.success(components);
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

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setMailingComponentDao(MailingComponentDao mailingComponentDao) {
		this.mailingComponentDao = mailingComponentDao;
	}

	@Required
	public void setModelValidator(final ComponentModelValidator modelValidator) {
		this.modelValidator = modelValidator;
	}

	@Required
	public void setMailingComponentFactory(MailingComponentFactory mailingComponentFactory) {
		this.mailingComponentFactory = mailingComponentFactory;
	}

	@Required
	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	@Required
	public void setFormComponentDao(FormComponentDao formComponentDao) {
		this.formComponentDao = formComponentDao;
	}
}
