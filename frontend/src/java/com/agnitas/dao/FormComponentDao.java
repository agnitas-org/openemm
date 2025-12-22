/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.List;
import java.util.Set;

import com.agnitas.beans.FormComponent;
import com.agnitas.beans.FormComponent.FormComponentType;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.userform.form.UserFormImagesOverviewFilter;

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

	boolean exists(int formId, int companyID, String componentName);

	PaginatedList<FormComponent> getFormComponentOverview(UserFormImagesOverviewFilter filter);

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

	List<FormComponent> getFormComponents(Set<Integer> ids, UserFormImagesOverviewFilter filter);

	boolean saveFormComponent(int companyId, int formId, FormComponent components, FormComponent componentThumbnail);

	boolean updateDimension(int width, int height, int componentId);

	List<String> getComponentFileNames(Set<Integer> bulkIds, int formId, int companyID);

	void delete(Set<Integer> bulkIds, int formId, int companyID);
}
