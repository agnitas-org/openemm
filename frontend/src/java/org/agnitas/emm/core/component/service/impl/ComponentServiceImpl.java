/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.component.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import com.agnitas.beans.FormComponent;
import com.agnitas.emm.core.userform.form.UserFormImagesOverviewFilter;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.factory.MailingComponentFactory;
import org.agnitas.dao.MailingComponentDao;
import org.agnitas.dao.MailingDao;
import org.agnitas.emm.core.component.service.ComponentAlreadyExistException;
import org.agnitas.emm.core.component.service.ComponentModel;
import org.agnitas.emm.core.component.service.ComponentNotExistException;
import org.agnitas.emm.core.component.service.ComponentService;
import org.agnitas.emm.core.component.service.validation.ComponentModelValidator;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.util.AgnUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.dao.FormComponentDao;

import javax.imageio.ImageIO;

public class ComponentServiceImpl implements ComponentService {
	private static final Logger logger = LogManager.getLogger(ComponentServiceImpl.class);

	public static final int THUMBNAIL_WIDTH = 56;
	public static final int THUMBNAIL_HEIGHT = 79;

	protected MailingComponentDao mailingComponentDao;
	protected ComponentModelValidator modelValidator;

	protected MailingComponentFactory mailingComponentFactory;

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

	protected MailingDao mailingDao;
	
	@Required
	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
	
	protected FormComponentDao formComponentDao;
	
	@Required
	public void setFormComponentDao(FormComponentDao formComponentDao) {
		this.formComponentDao = formComponentDao;
	}
	
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

	@Override
	@Transactional
	public int addComponent(ComponentModel model) throws Exception {
		modelValidator.assertIsValidToAdd(model);
		return addComponentImpl(model);
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
	@Transactional
	public void deleteComponent(ComponentModel model) {
		modelValidator.assertIsValidToGetOrDelete(model);
		deleteComponentImpl(model);
	}

	@Override
	public boolean saveFormComponent(FormComponent formComponent) {
		if (formComponent.getType() == FormComponent.FormComponentType.IMAGE) {
			// Create thumbnail for this image
			FormComponent thumbnailComponent = new FormComponent();
			thumbnailComponent.setType(FormComponent.FormComponentType.THUMBNAIL);
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
				formComponentDao.deleteFormComponent(formComponent.getCompanyID(), formComponent.getFormID(), formComponent.getName(), FormComponent.FormComponentType.THUMBNAIL);

				success = formComponentDao.saveFormComponent(thumbnailComponent);
			}
			return success;
		} else {
			return formComponentDao.saveFormComponent(formComponent);
		}
	}

	@Override
	public boolean deleteFormComponent(int formID, int companyID, String componentName) {
		return formComponentDao.deleteFormComponent(companyID, formID, componentName, null);
	}

	@Override
	public FormComponent getFormComponent(int formID, int companyID, String imageFileName, FormComponent.FormComponentType componentType) {
		return formComponentDao.getFormComponent(formID, companyID, imageFileName, componentType);
	}

	@Override
	public List<FormComponent> getFormComponentDescriptions(UserFormImagesOverviewFilter filter) {
		return formComponentDao.getFormComponentDescriptions(filter);
	}

	protected void deleteComponentImpl(ComponentModel model) {
		MailingComponent component = mailingComponentDao.getMailingComponent(model.getComponentId(), model.getCompanyId());
		if (component == null || component.getType() != model.getComponentType()) {
			throw new ComponentNotExistException();
		}
		mailingComponentDao.deleteMailingComponent(component);
	}

}
