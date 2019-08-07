/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.agnitas.beans.MailingComponent;
import org.agnitas.emm.core.component.service.ComponentModel;
import org.agnitas.emm.core.component.service.ComponentNotExistException;
import org.agnitas.emm.core.component.service.impl.ComponentServiceImpl;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.emm.core.validator.annotation.Validate;
import org.agnitas.util.AgnUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.FormComponent;
import com.agnitas.dao.FormComponent.FormComponentType;
import com.agnitas.emm.core.components.service.ComComponentService;

public abstract class ComComponentServiceImpl extends ComponentServiceImpl implements ComComponentService {
	private static final transient Logger logger = Logger.getLogger(ComComponentServiceImpl.class);
	
	public static final int THUMBNAIL_WIDTH = 56;
	public static final int THUMBNAIL_HEIGHT = 79;
	
	@Override
	@Transactional
	@Validate("addComponent")
	public int addComponent(ComponentModel model) throws Exception {
		int res = addComponentImpl(model);
        ((ComMailingDao)mailingDao).updateStatus(model.getMailingId(), "edit");
        return res;
	}

	@Override
	@Transactional
	@Validate("getComponent")
	public void deleteComponent(ComponentModel model) {
		deleteComponentImpl(model);
	    ((ComMailingDao)mailingDao).updateStatus(model.getMailingId(), "edit");
	}

	@Override
	@Transactional
	@Validate("updateMailingContent")
	public void updateMailingContent(ComponentModel model) throws Exception {
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
	public void updateHostImage(int mailingID, int companyID, int componentID, byte[] imageBytes) {
		mailingComponentDao.updateHostImage(mailingID, companyID, componentID, imageBytes);
	}
}
