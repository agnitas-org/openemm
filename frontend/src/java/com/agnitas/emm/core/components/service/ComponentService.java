/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service;

import com.agnitas.beans.Admin;
import com.agnitas.beans.FormComponent;
import com.agnitas.emm.core.components.dto.FormComponentDto;
import com.agnitas.emm.core.components.dto.FormUploadComponentDto;
import com.agnitas.emm.core.userform.form.UserFormImagesOverviewFilter;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.component.service.ComponentModel;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ComponentService {

	List<MailingComponent> getComponents(ComponentModel model);

	MailingComponent getComponent(ComponentModel model);

	int addComponent(ComponentModel model) throws Exception;

	void updateComponent(ComponentModel model) throws Exception;

	void deleteComponent(ComponentModel model);

	boolean deleteFormComponent(int formID, int companyID, String componentName);

	/**
	 * Gets the form component by name.
	 *
	 * @param formID the form id
	 * @param companyID the company id
	 * @param imageFileName the image file name
	 * @return the form component by name
	 */
	FormComponent getFormComponent(int formID, int companyID, String imageFileName, FormComponent.FormComponentType componentType);
    
    void updateMailingContent(ComponentModel model) throws Exception;

    int addMailingComponent(MailingComponent mailingComponent) throws Exception;

	PaginatedListImpl<FormComponentDto> getFormImageComponents(UserFormImagesOverviewFilter filter, int companyID, int formId);

	Map<String, byte[]> getImageComponentsData(UserFormImagesOverviewFilter filter, Set<Integer> ids, int companyId, int formId);

	File getComponentArchive(String zipName, Map<String, byte[]> formComponentsData);

	SimpleServiceResult saveFormComponents(Admin admin, int formId, List<FormComponent> components, List<UserAction> userActions);

	SimpleServiceResult saveComponentsFromZipFile(Admin admin, int formId, MultipartFile zipFile, List<UserAction> userActions, boolean overwriteExisting);

	ServiceResult<List<FormUploadComponentDto>> readComponentsFromZipFile(MultipartFile zipFile);

	List<Message> validateComponents(List<FormUploadComponentDto> components, boolean checkDuplicate);

    List<String> getComponentFileNames(Set<Integer> bulkIds, int formId, int companyID);

    void delete(Set<Integer> bulkIds, int formId, Admin admin);
}
