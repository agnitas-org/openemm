/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.forms;

import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.web.forms.StrutsFormBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

public class ComMailingParameterForm extends StrutsFormBase {
	private static final long serialVersionUID = 6617476371226706266L;

	private static final transient Logger logger = Logger.getLogger(ComMailingParameterForm.class);

	protected String action;
	private int mailingInfoID = 0;				//increasing number
	private int mailingID = 0;					//mailing on which the parameter is set, 0 for default values
	private int companyID = 0;					//need companyID for default values
	private String parameterName;				//name of the parameter
	private String value;						//value of the parameter
	private String description; 				//description of the parameter
	private Date creation_date; 
	private Date change_date;
	private int creationAdminID = 0; 			//who created the parameter
	private int changeAdminID = 0; 				//who changed the values
	private int previousAction;
	private String parameterSearchQuery = "";
	private String mailingSearchQuery = "";

	
	public int getMailingInfoID() {
		return mailingInfoID;
	}

	public void setMailingInfoID(int mailingInfoID) {
		this.mailingInfoID = mailingInfoID;
	}

	public int getMailingID() {
		return mailingID;
	}

	public void setMailingID(int mailingID) {
		this.mailingID = mailingID;
	}

	public int getCompanyID() {
		return companyID;
	}

	public void setCompanyID(@VelocityCheck int companyID) {
		this.companyID = companyID;
	}

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreation_date() {
		return creation_date;
	}

	public void setCreation_date(Date creation_date) {
		this.creation_date = creation_date;
	}

	public Date getChange_date() {
		return change_date;
	}

	public void setChange_date(Date change_date) {
		this.change_date = change_date;
	}

	public int getCreation_admin_id() {
		return creationAdminID;
	}

	public void setCreation_admin_id(int creation_admin_id) {
		this.creationAdminID = creation_admin_id;
	}

	public int getChange_admin_id() {
		return changeAdminID;
	}

	public void setChange_admin_id(int change_admin_id) {
		this.changeAdminID = change_admin_id;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

    public int getPreviousAction() {
        return previousAction;
    }

    public void setPreviousAction(int previousAction) {
        this.previousAction = previousAction;
    }

	public String getParameterSearchQuery() {
		return parameterSearchQuery;
	}

	public void setParameterSearchQuery(String parameterSearchQuery) {
		this.parameterSearchQuery = parameterSearchQuery;
	}

	public String getMailingSearchQuery() {
		return mailingSearchQuery;
	}

	public void setMailingSearchQuery(String mailingSearchQuery) {
		this.mailingSearchQuery = mailingSearchQuery;
	}

	/**
	 * The reset form from StrutsFormBase is not used, because tomcat would call
	 * it every time before populating the values...
	 */
	public void resetFormValues() {
		setMailingInfoID(0);
		setMailingID(0);
		setCompanyID(0);
		setParameterName("");
		setValue("");
		setDescription("");
		setCreation_date(null);
		setChange_date(null);
		setCreation_admin_id(0);
		setChange_admin_id(0);
		setParameterSearchQuery("");
		setMailingSearchQuery("");
	}
	
	/**
	 * this method returns the ActionErrors
	 */
	@Override
	public ActionErrors formSpecificValidate(ActionMapping mapping, HttpServletRequest request) {		
		ActionErrors actionErrors = new ActionErrors();	// hold the errors. This will be returned.
		
		// Check only if we have a new or edited form. Not at the list-view!
		//String method = mapping.getParameter();
		//String methodCall = request.getParameter(method);
		if(getAction().equals("save")) {
			if(StringUtils.isBlank(getParameterName())) {
				actionErrors.add("mailingParameter", new ActionMessage("error.mailing.parameter.name"));
				if (logger.isDebugEnabled()) {
					logger.debug("Error in MailingParameter - name is empty");
				}
			}	
		
			if(StringUtils.isBlank(getValue())) {
				actionErrors.add("mailingParameter", new ActionMessage("error.mailing.parameter.value"));
				if (logger.isDebugEnabled()) {
					logger.debug("Error in MailingParameter - value is empty");
				}
			}
		}
		return actionErrors;
	}		
}
