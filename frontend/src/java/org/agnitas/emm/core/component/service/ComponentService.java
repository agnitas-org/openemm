/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.component.service;

import java.util.List;

import com.agnitas.dao.FormComponent;
import com.agnitas.dao.FormComponent.FormComponentType;
import org.agnitas.beans.MailingComponent;

public interface ComponentService {
	List<MailingComponent> getComponents(ComponentModel model);

	MailingComponent getComponent(ComponentModel model);

	int addComponent(ComponentModel model) throws Exception;

	void updateComponent(ComponentModel model) throws Exception;

	void deleteComponent(ComponentModel model);

	/**
	 * Save form component.
	 *
	 * @param formComponent the form component
	 */
	boolean saveFormComponent(FormComponent formComponent);
	
	boolean deleteFormComponent(int formID, int companyID, String componentName);

	/**
	 * Gets the form component by name.
	 *
	 * @param formID the form id
	 * @param companyID the company id
	 * @param imageFileName the image file name
	 * @return the form component by name
	 */
	FormComponent getFormComponent(int formID, int companyID, String imageFileName, FormComponentType componentType);
	
	/**
	 * Gets the component descriptions.
	 * This returns FormComponent items with all fields filled except for the data byte[]
	 *
	 * @param companyID the company id
	 * @param componentType the component type
	 * @return the component descriptions
	 */
	List<FormComponent> getFormComponentDescriptions(int companyID, int formID, FormComponentType componentType);

	/**
	 * Gets the form components.
	 *
	 * @param companyID the company id
	 * @param formID the form id
	 * @return the form components
	 */
	List<FormComponent> getFormComponents(int companyID, int formID);
}
