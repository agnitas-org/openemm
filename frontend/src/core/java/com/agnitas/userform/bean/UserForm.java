/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.userform.bean;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.action.bean.EmmAction;

import org.springframework.context.ApplicationContext;

import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.userform.trackablelinks.bean.TrackableUserFormLink;

/**
 * EMM user form interface
 */
public interface UserForm {
	String TEMP_REDIRECT_PARAM = "REDIRECT_TEMP";

	boolean evaluateEndAction(ApplicationContext con, Map<String, Object> params, final EmmActionOperationErrors errors);

	String evaluateForm(ApplicationContext con, Map<String, Object> params, final EmmActionOperationErrors errors);

	boolean evaluateStartAction(ApplicationContext con, Map<String, Object> params, final EmmActionOperationErrors errors);

	/**
	 * Getter for property companyID.
	 * 
	 * @return Value of property companyID.
	 */
	int getCompanyID();

	/**
	 * Getter for property description.
	 * 
	 * @return Value of property description.
	 */
	String getDescription();

	/**
	 * Getter for property endAction.
	 * 
	 * @return Value of property endAction.
	 */
	EmmAction getEndAction();

	/**
	 * Getter for property endActionID.
	 * 
	 * @return Value of property endActionID.
	 */
	int getEndActionID();

	/**
	 * Getter for property errorTemplate.
	 * 
	 * @return Value of property errorTemplate.
	 */
	String getErrorTemplate();

	/**
	 * Getter for property formID.
	 * 
	 * @return Value of property formID.
	 */
	int getId();

	/**
	 * Getter for property formName.
	 * 
	 * @return Value of property formName.
	 */
	String getFormName();

	/**
	 * Getter for property startAction.
	 * 
	 * @return Value of property startAction.
	 */
	EmmAction getStartAction();

	/**
	 * Getter for property startActionID.
	 * 
	 * @return Value of property startActionID.
	 */
	int getStartActionID();

	/**
	 * Getter for property successTemplate.
	 * 
	 * @return Value of property successTemplate.
	 */
	String getSuccessTemplate();

	/**
	 * Setter for property companyID.
	 * 
	 * @param companyID
	 *            New value of property companyID.
	 */
	void setCompanyID( int companyID);

	/**
	 * Setter for property description.
	 * 
	 * @param description
	 *            New value of property description.
	 */
	void setDescription(String description);

	/**
	 * Setter for property endAction.
	 * 
	 * @param endAction
	 *            New value of property endAction.
	 */
	void setEndAction(EmmAction endAction);

	/**
	 * Setter for property endActionID.
	 * 
	 * @param endActionID
	 *            New value of property endActionID.
	 */
	void setEndActionID(int endActionID);

	/**
	 * Setter for property errorTemplate.
	 * 
	 * @param errorTemplate
	 *            New value of property errorTemplate.
	 */
	void setErrorTemplate(String errorTemplate);

	/**
	 * Setter for property formID.
	 * 
	 * @param formID
	 *            New value of property formID.
	 */
	void setId(int formID);

	/**
	 * Setter for property formName.
	 * 
	 * @param formName
	 *            New value of property formName.
	 */
	void setFormName(String formName);

	/**
	 * Setter for property startAction.
	 * 
	 * @param startAction
	 *            New value of property startAction.
	 */
	void setStartAction(EmmAction startAction);

	/**
	 * Setter for property startActionID.
	 * 
	 * @param startActionID
	 *            New value of property startActionID.
	 */
	void setStartActionID(int startActionID);

	/**
	 * Setter for property successTemplate.
	 * 
	 * @param successTemplate
	 */
	void setSuccessTemplate(String successTemplate);

	String getSuccessUrl();

	void setSuccessUrl(String successUrl);

	String getErrorUrl();

	void setErrorUrl(String errorUrl);

	boolean isSuccessUseUrl();

	void setSuccessUseUrl(boolean successUseUrl);

	boolean isErrorUseUrl();

	void setErrorUseUrl(boolean errorUseUrl);

	List<Integer> getUsedActionIds();

	/**
	 * getStartActionID() > 0 && getStartActionID() > 0
	 */
	boolean isUsesActions();
	
	/**
     * Getter for property trackableLinks.
     *
     * @return Value of property trackableLinks.
     */
    Map<String, TrackableUserFormLink> getTrackableLinks();
    
    /**
     * Setter for property trackableLinks.
     *
     * @param trackableLinks New value of property trackableLinks.
     */
    void setTrackableLinks(Map<String, TrackableUserFormLink> trackableLinks);

	Date getCreationDate();

	void setCreationDate(Date creationDate);

	Date getChangeDate();

	void setChangeDate(Date changeDate);

	void setSuccessMimetype(String string);

	String getSuccessMimetype();

	void setErrorMimetype(String string);

	String getErrorMimetype();

	boolean isActive();

	void setActive(boolean active);

	boolean isDeleted();

	void setDeleted(boolean deleted);

	boolean getIsActive();

	void setIsActive(boolean active);

	String getSuccessFormBuilderJson();

	void setSuccessFormBuilderJson(String json);

	String getErrorFormBuilderJson();

	void setErrorFormBuilderJson(String json);

	String evaluateErrorForm(ApplicationContext con, Map<String, Object> params, EmmActionOperationErrors errors);
}
