/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.userform.service.UserFormFilter;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.Tuple;

import com.agnitas.beans.LinkProperty;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;

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

	int updateActiveness(@VelocityCheck int companyId, Collection<Integer> formIds, boolean isActive);

	List<UserForm> getByIds(@VelocityCheck int companyId, Collection<Integer> formIds);

    /**
     * Deletes user form identified by form name and company id.
     *
	 * @param formID
	 *            The id of the user form that should be deleted.
	 * @param companyID
	 *            The companyID for the user form that should be deleted.
     * @return true on success.
     */
    boolean deleteUserForm(int formID, @VelocityCheck int companyID);

    boolean deleteUserFormByCompany(@VelocityCheck int companyID);

    /**
     * Load all user forms for company id.
     *
     * @param companyID
     *          The id of the company for user forms.
     * @return List of UserForm or empty list.
     */
    List<UserForm> getUserForms(@VelocityCheck int companyID);

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

    UserForm getUserForm(int formID, @VelocityCheck int companyID) throws Exception;

	UserForm getUserFormByName(String name, @VelocityCheck int companyID) throws Exception;

	String getUserFormName(int formId, @VelocityCheck int companyId);

	Map<String, ComTrackableUserFormLink> getUserFormTrackableLinks(int formID, @VelocityCheck int companyID);
	
    /**
     * Deletes a trackable link.
     *
     * @return true==success
     *         false==error
     */
	boolean deleteUserFormTrackableLink(int linkID, @VelocityCheck int companyID);

    /**
     * Getter for property trackableLink by link id and company id.
     * @param linkID - id of the link
     */
    ComTrackableUserFormLink getUserFormTrackableLink(int linkID) throws Exception;

   /**
    * Saves a trackableLink.
    * @param link - id of link
    */
    int storeUserFormTrackableLink(ComTrackableUserFormLink link);

	/**
	 * Logs a click for trackable link in rdir_log_userform_tbl
	 *
	 * @param link the link which was clicked.
	 * @param customerID the id of the recipient who clicked the link.
	 * @param remoteAddr the ip address of the recipient. 
	 * @return True on success.
	 */
	boolean logUserFormTrackableLinkClickInDB(ComTrackableUserFormLink link, Integer customerID, Integer mailingID, String remoteAddr, DeviceClass deviceClass, int deviceID, int clientID);
	
	boolean logUserFormCallInDB(@VelocityCheck int companyID, int formID, int linkID, Integer mailingID, Integer customerID, String remoteAddr, DeviceClass deviceClass, int deviceID, int clientID);
	
	List<LinkProperty> getUserFormTrackableLinkProperties(ComTrackableUserFormLink link);

	void deleteUserFormTrackableLinkProperties(int linkID);

	void storeUserFormTrackableLinkProperties(ComTrackableUserFormLink link);
	
	ComTrackableUserFormLink getDummyUserFormTrackableLinkForStatisticCount(@VelocityCheck int companyID, int formID) throws Exception;

	/**
	 * The same as {@link UserFormDao#getUserForms(int)} but also
	 * fills {@link UserForm#getStartActionID()} and {@link UserForm#getEndActionID()}
	 */
	PaginatedListImpl<UserForm> getUserFormsWithActionIDs(String sortColumn, String sortDirection, int pageNumber,
														  int pageSize, Boolean activenessFilter, @VelocityCheck int companyID);
	
	PaginatedListImpl<UserForm> getUserFormsWithActionIdsNew(String sortColumn, String sortDirection, int pageNumber,
			int pageSize, UserFormFilter filter, @VelocityCheck int companyID);
}
