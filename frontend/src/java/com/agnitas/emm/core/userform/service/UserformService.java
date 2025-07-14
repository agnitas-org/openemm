/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.service;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.userform.dto.UserFormDto;
import com.agnitas.emm.core.userform.form.UserFormForm;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.userform.bean.UserForm;
import org.json.JSONArray;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import org.agnitas.emm.core.velocity.scriptvalidator.ScriptValidationException;
import com.agnitas.exception.FormNotFoundException;
import com.agnitas.util.DbColumnType;
import com.agnitas.util.Tuple;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface UserformService {

	/**
	 * Checks, if there is another form with same name.
	 *
	 * @param formName  name of form
	 * @param companyId company ID
	 * @return true, if form name is already in use
	 */
	boolean isFormNameInUse(final String formName, final int companyId);

	List<Message> validateUserForm(Admin admin, UserFormForm form) throws ScriptValidationException;

	/**
	 * Load user form for given form name.
	 *
	 * @param companyID company ID
	 * @param formName  form name
	 * @return user form
	 * @throws FormNotFoundException if given form name is unknown
	 */
	UserForm getUserForm(int companyID, String formName) throws FormNotFoundException;

	List<Tuple<Integer, String>> getUserFormNamesByActionID(int companyID, int actionID);

	void copyUserForm(int id, int companyId, int newCompanyId, int mailinglistId, String rdirDomain, Map<Integer, Integer> actionIdReplacements) throws Exception;

	void restore(Set<Integer> bulkIds, int companyId);

	void deleteExpired(Date expireDate, int companyId);

    String getUserFormName(int formId, int companyId);

    List<UserForm> getUserForms(int companyId);

	// TODO: EMMGUI-714: Check usages and remove when removing old design
    UserAction setActiveness(int companyId, Map<Integer, Boolean> activeness);
    ServiceResult<List<UserForm>> setActiveness(Set<Integer> ids, int companyId, boolean activate);

	int updateActiveness(int companyId, Collection<Integer> formIds, boolean isActive);

	UserFormDto getUserForm(int companyId, int formId);

	ServiceResult<Integer> saveUserForm(Admin admin, UserFormDto userFormDto);

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

    List<String> getUserFormNames(Set<Integer> bulkIds, int companyID);

	Map<String, String> getMediapoolImages(Admin admin);

	Map<String, String> getProfileFields(Admin admin, DbColumnType.SimpleDataType... allowedTypes);

	boolean isActive(int formId);

	boolean isValidFormName(String formName);
}
