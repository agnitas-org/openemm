/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.List;


import com.agnitas.beans.FormComponent;
import com.agnitas.beans.FormComponent.FormComponentType;

/**
 * The Interface FormComponentDao.
 */
public interface FormComponentDao {
	
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
	 * Exists.
	 *
	 * @param formID the form id
	 * @param companyID the company id
	 * @param componentID the component id
	 * @return true, if successful
	 */
	boolean exists(int formID, int companyID, int componentID);
	boolean exists(int formId, int companyID, String componentName);

	/**
	 * Save.
	 *
	 * @param formComponent the form component
	 */
	boolean saveFormComponent(FormComponent formComponent);

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
	 * Delete form component.
	 *
	 * @param companyID the company id
	 * @param formID the form id
	 * @param componentName the name
	 * @param componentType the componentType
	 */
	boolean deleteFormComponent(int companyID, int formID, String componentName, FormComponentType componentType);
	
	boolean deleteFormComponentByCompany(int companyID);

	/**
	 * Gets the form components.
	 *
	 * @param companyID the company id
	 * @param formID the form id
	 * @return the form components
	 */
	List<FormComponent> getFormComponents(int companyID, int formID);

	List<FormComponent> getFormComponents(int companyId, int formId, List<FormComponentType> types);

	boolean saveFormComponent(int companyId, int formId, FormComponent components, FormComponent componentThumbnail) throws Exception;
}
