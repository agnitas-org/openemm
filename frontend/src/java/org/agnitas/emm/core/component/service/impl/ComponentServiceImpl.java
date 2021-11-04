/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.component.service.impl;

import java.util.List;

import org.agnitas.beans.MailingComponent;
import org.agnitas.dao.MailingComponentDao;
import org.agnitas.dao.MailingDao;
import org.agnitas.emm.core.component.service.ComponentAlreadyExistException;
import org.agnitas.emm.core.component.service.ComponentModel;
import org.agnitas.emm.core.component.service.ComponentNotExistException;
import org.agnitas.emm.core.component.service.ComponentService;
import org.agnitas.emm.core.component.service.validation.ComponentModelValidator;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.dao.FormComponentDao;

public abstract class ComponentServiceImpl implements ComponentService {

	protected MailingComponentDao mailingComponentDao;
	protected ComponentModelValidator modelValidator;
	
	@Required
	public void setMailingComponentDao(MailingComponentDao mailingComponentDao) {
		this.mailingComponentDao = mailingComponentDao;
	}

	@Required
	public void setModelValidator(final ComponentModelValidator modelValidator) {
		this.modelValidator = modelValidator;
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
	
	protected abstract MailingComponent getMailingComponent();
	
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
		MailingComponent component = getMailingComponent();
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
		
	protected void deleteComponentImpl(ComponentModel model) {
		MailingComponent component = mailingComponentDao.getMailingComponent(model.getComponentId(), model.getCompanyId());
		if (component == null || component.getType() != model.getComponentType()) {
			throw new ComponentNotExistException();
		}
		mailingComponentDao.deleteMailingComponent(component);
	}

}
