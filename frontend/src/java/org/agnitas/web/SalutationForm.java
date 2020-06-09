/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.util.ArrayList;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.util.AgnUtils;
import org.agnitas.web.forms.StrutsFormBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;

public class SalutationForm extends StrutsFormBase {
    
    private static final long serialVersionUID = 6582709937144587542L;

	/** 
     * Holds value of property mailinglistID. 
     */
    private int salutationID;
    
    /**
     * Holds value of property description. 
     */
    private String description;
    
    /**
     * Holds value of property action. 
     */
    private int action;
    
    private int salutationCompanyID;
    
    /**
     * Holds value of property shortname. 
     */
    private String shortname;
    
    /**
     * Holds value of property salMale. 
     */
    private String salMale;
    
    /**
     * Holds value of property salFemale.
     */
    private String salFemale;
    
    /**
     * Holds value of property salUnknown. 
     */
    private String salUnknown;
    
    /**
     * Holds value of property salCompany. 
     */
    private String salCompany;
    
    /**
     * Holds value of property salMiss. 
     */
    private String salMiss;
    
    /**
     * Holds value of property salPractice. 
     */
    private String salPractice;

    /**
     * Holds value of property previousAction.
     */
    private int previousAction;

    private ActionMessages messages;

    /**
     * Creates a new instance of MailinglistForm 
     */
    public SalutationForm() {
        super();
        if (this.columnwidthsList == null) {
            this.columnwidthsList = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                columnwidthsList.add("-1");
            }
        }
    }
    
    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    @Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {
    	super.reset(mapping, request);
       
    }
    
    /**
     * Validate the properties that have been set from this HTTP request,
     * and return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * <code>null</code> or an <code>ActionErrors</code> object with no
     * recorded error messages.
     * 
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     * @return errors
     */
    @Override
	public ActionErrors formSpecificValidate(ActionMapping mapping, HttpServletRequest request) {
        
        ActionErrors errors = new ActionErrors();
        if (action == SalutationAction.ACTION_SAVE || action == SalutationAction.ACTION_NEW) {
            if (StringUtils.isEmpty(shortname) || shortname.length() < 3) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.name.too.short"));
            }
        }
        if (action == SalutationAction.ACTION_VIEW && salutationID == 0) {
        	Locale aLoc = AgnUtils.getLocale(request);
            MessageResources text=(MessageResources)this.getServlet().getServletContext().getAttribute(org.apache.struts.Globals.MESSAGES_KEY);
          
        	this.shortname=text.getMessage(aLoc, "default.salutation.shortname");
            this.description=text.getMessage(aLoc, "default.salutation.description");
        }
        
        return errors;
    }
        
    /** 
     * Getter for property description.
     *
     * @return Value of property description.
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Setter for property description.
     *
     * @param description New value of property description.
     */
    public void setDescription(String description) {
        this.description = description;
    }
        
    /**
     * Getter for property action.
     *
     * @return Value of property action.
     */
    public int getAction() {
        return this.action;
    }
    
    /**
     * Setter for property action.
     *
     * @param action New value of property action.
     */
    public void setAction(int action) {
        this.action = action;
    }
    
    /**
     * Getter for property shortname.
     *
     * @return Value of property shortname.
     */
    public String getShortname() {
        return this.shortname;
    }
    
    /**
     * Setter for property shortname.
     *
     * @param shortname New value of property shortname.
     */
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }
    
    /**
     * Getter for property salutationID.
     *
     * @return Value of property salutationID.
     */
    public int getSalutationID() {
        return this.salutationID;
    }
    
    /**
     * Setter for property salutationID.
     *
     * @param salutationID New value of property salutationID.
     */
    public void setSalutationID(int salutationID) {
        this.salutationID = salutationID;
    }
    
    /**
     * Getter for property salMale.
     *
     * @return Value of property salMale.
     */
    public String getSalMale() {
        return this.salMale;
    }
    
    /**
     * Setter for property salMale.
     *
     * @param salMale New value of property salMale.
     */
    public void setSalMale(String salMale) {
        this.salMale = salMale;
    }
    
    /**
     * Getter for property salFemale.
     *
     * @return Value of property salFemale.
     */
    public String getSalFemale() {
        return this.salFemale;
    }
    
    /**
     * Setter for property salFemale.
     *
     * @param salFemale New value of property salFemale.
     */
    public void setSalFemale(String salFemale) {
        this.salFemale = salFemale;
    }
    
    /**
     * Getter for property salUnknown.
     *
     * @return Value of property salUnknown.
     */
    public String getSalUnknown() {
        return this.salUnknown;
    }
    
    /**
     * Setter for property salUnknown.
     *
     * @param salUnknown New value of property salUnknown.
     */
    public void setSalUnknown(String salUnknown) {
        this.salUnknown = salUnknown;
    }
    
    /**
     * Getter for property salCompany.
     *
     * @return Value of property salCompany.
     */
    public String getSalCompany() {
        return this.salCompany;
    }
    
    /**
     * Setter for property salCompany.
     *
     * @param salCompany New value of property salCompany.
     */
    public void setSalCompany(String salCompany) {
        this.salCompany = salCompany;
    }
    
    /**
     * Getter for property salMiss.
     *
     * @return Value of property salMiss.
     */
    public String getSalMiss() {
        return this.salMiss;
    }
    
    /**
     * Setter for property salMiss.
     *
     * @param salMiss New value of property salMiss.
     */
    public void setSalMiss(String salMiss) {
        this.salMiss = salMiss;
    }
    
    /**
     * Getter for property salPractice.
     *
     * @return Value of property salPractice.
     */
    public String getSalPractice() {
        return this.salPractice;
    }
    
    /**
     * Setter for property salPractice.
     *
     * @param salPractice New value of property salPractice.
     */
    public void setSalPractice(String salPractice) {
        this.salPractice = salPractice;
    }

    /**
     * Getter for property previousAction.
     *
     * @return Value of property previousAction.
     */
    public int getPreviousAction() {
        return previousAction;
    }

    /**
     * Setter for property previousAction.
     *
     * @param previousAction New value of property previousAction.
     */
    public void setPreviousAction(int previousAction) {
        this.previousAction = previousAction;
    }
    
    public int getSalutationCompanyID() {
		return salutationCompanyID;
	}

	public void setSalutationCompanyID(int salutationCompanyID) {
		this.salutationCompanyID = salutationCompanyID;
	}

	public ActionMessages getMessages() {
        return messages;
    }

    public void setMessages(ActionMessages messages) {
        this.messages = messages;
    }
}
