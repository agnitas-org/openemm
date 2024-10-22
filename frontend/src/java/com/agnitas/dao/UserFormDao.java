/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;


import org.agnitas.util.Tuple;

import com.agnitas.userform.bean.UserForm;

/**
 * interface for user form functions
 */
public interface UserFormDao {

    /**
     * Saves or updates userForm.
     *
     * @param form
     *          The userForm that should be saved.
     * @return Saved userForm id.
     * @throws Exception
     */
    int storeUserForm(UserForm form) throws Exception;
    
	int createUserForm(int companyId, UserForm userForm);
	
	void updateUserForm(int companyId, UserForm form);

	int updateActiveness(int companyId, Collection<Integer> formIds, boolean isActive);

	List<UserForm> getByIds(int companyId, Collection<Integer> formIds);

    /**
     * Deletes user form identified by form name and company id.
     *
	 * @param formID
	 *            The id of the user form that should be deleted.
	 * @param companyID
	 *            The companyID for the user form that should be deleted.
     * @return true on success.
     */
    boolean deleteUserForm(int formID, int companyID);

    void markDeleted(int formId, int companyId);

    void restore(Set<Integer> ids, int companyId);

    List<Integer> getMarkedAsDeletedBefore(Date date, int companyId);

    boolean deleteUserFormByCompany(int companyID);

	List<UserForm> overview(int companyID);

    /**
     * Load all user forms for company id.
     *
     * @param companyID
     *          The id of the company for user forms.
     * @return List of UserForm or empty list.
     */
    List<UserForm> getUserForms(int companyID);

	/**
	 * Checks, if there is another form with same name.
	 * 
	 * @param formName name of form
	 * @param formId ID of current form
	 * @param companyId company ID
	 * 
	 * @return true, if form name is already in use
	 */
	boolean isFormNameInUse(String formName, int formId, int companyId);

	/**
	 * Ids and shortnames of all {@link UserForm} which uses action
	 */
	List<Tuple<Integer, String>> getUserFormNamesByActionID(int companyID, int actionID);

	UserForm getUserForm(int formID, int companyID);

	UserForm getUserFormByName(String name, int companyID) throws Exception;

	String getUserFormName(int formId, int companyId);

	boolean existsUserForm(int companyId, int userFormId);

    boolean isActive(int formId);
}
