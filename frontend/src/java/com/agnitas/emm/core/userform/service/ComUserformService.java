/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.service;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.userforms.UserformService;
import org.agnitas.util.DbColumnType;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.userform.dto.UserFormDto;
import com.agnitas.service.ServiceResult;
import com.agnitas.userform.bean.UserForm;

import net.sf.json.JSONArray;

public interface ComUserformService extends UserformService {

    String getUserFormName(int formId, int companyId);

    List<UserForm> getUserForms(int companyId);

    UserAction setActiveness(int companyId, Map<Integer, Boolean> activeness);

	UserFormDto getUserForm(int companyId, int formId);

	boolean isFormNameUnique(String formName, int formId, int companyId);

	ServiceResult<Integer> saveUserForm(Admin admin, UserFormDto userFormDto) throws Exception;

	List<UserFormDto> bulkDeleteUserForm(List<Integer> bulkIds, int companyId);

	boolean deleteUserForm(int formId, int companyId);

	String getCloneUserFormName(String name, int companyId, Locale locale);

	ServiceResult<Integer> cloneUserForm(Admin admin, int userFormId);

	File exportUserForm(Admin admin, int userFormId, String userFormName);
	
	String getUserFormUrlPattern(final Admin admin, final String formName, final boolean resolveUID, final Optional<String> companyToken);
	List<UserFormTestUrl> getUserFormUrlForAllAdminAndTestRecipients(final Admin admin, final String formName, final Optional<String> companyToken);
	String getUserFormUrlWithoutUID(Admin admin, String formName, Optional<String> companyToken);

    JSONArray getUserFormsJson(Admin admin);

    List<String> getUserFormNames(int companyId);

	Map<String, String> getMediapoolImages(Admin admin);

	Map<String, String> getProfileFields(Admin admin, DbColumnType.SimpleDataType... allowedTypes);

}
