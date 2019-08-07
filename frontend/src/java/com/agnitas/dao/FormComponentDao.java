/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.List;

import com.agnitas.dao.FormComponent.FormComponentType;

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
	public FormComponent getFormComponent(int formID, int companyID, String imageFileName, FormComponentType componentType);

	/**
	 * Exists.
	 *
	 * @param formID the form id
	 * @param companyID the company id
	 * @param componentID the component id
	 * @return true, if successful
	 */
	public boolean exists(int formID, int companyID, int componentID);

	/**
	 * Save.
	 *
	 * @param formComponent the form component
	 */
	public boolean saveFormComponent(FormComponent formComponent);

	/**
	 * Gets the component descriptions.
	 * This returns FormComponent items with all fields filled except for the data byte[]
	 *
	 * @param companyID the company id
	 * @param componentType the component type
	 * @return the component descriptions
	 */
	public List<FormComponent> getFormComponentDescriptions(int companyID, int formID, FormComponentType componentType);

	/**
	 * Delete form component.
	 *
	 * @param companyID the company id
	 * @param formID the form id
	 * @param componentName the name
	 * @param componentType the componentType
	 */
	public boolean deleteFormComponent(int companyID, int formID, String componentName, FormComponentType componentType);
	
	public boolean deleteFormComponentByCompany(int companyID);

	/**
	 * Gets the form components.
	 *
	 * @param companyID the company id
	 * @param formID the form id
	 * @return the form components
	 */
	public List<FormComponent> getFormComponents(int companyID, int formID);
}
