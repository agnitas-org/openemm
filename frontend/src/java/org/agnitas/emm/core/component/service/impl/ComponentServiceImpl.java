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
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.emm.core.validator.annotation.Validate;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.dao.FormComponentDao;

public abstract class ComponentServiceImpl implements ComponentService {

	protected MailingComponentDao mailingComponentDao;
	
	@Required
	public void setMailingComponentDao(MailingComponentDao mailingComponentDao) {
		this.mailingComponentDao = mailingComponentDao;
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
	@Validate("getComponents")
	public List<MailingComponent> getComponents(ComponentModel model) {
		if (!mailingDao.exist(model.getMailingId(), model.getCompanyId())) {
			throw new MailingNotExistException();
		}
		return mailingComponentDao.getMailingComponents(model.getMailingId(), model.getCompanyId(), model.getComponentType());
	}

	@Override
	@Transactional
	@Validate("getComponent")
	public MailingComponent getComponent(ComponentModel model) {
		MailingComponent component = mailingComponentDao.getMailingComponent(model.getComponentId(), model.getCompanyId());
		if (component == null || component.getType() != model.getComponentType()) {
			throw new ComponentNotExistException();
		}
		return component;
	}

	@Override
	@Transactional
	@Validate("addComponent")
	public int addComponent(ComponentModel model) throws Exception {
		return addComponentImpl(model);
	}
		
	protected int addComponentImpl(ComponentModel model) throws Exception {
		MailingComponent component = getMailingComponent();
		if (!mailingDao.exist(model.getMailingId(), model.getCompanyId())) {
			throw new MailingNotExistException();
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
	@Validate("updateComponent")
	public void updateComponent(ComponentModel model) throws Exception {
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
	@Validate("getComponent")
	public void deleteComponent(ComponentModel model) {
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
