/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.util.AgnUtils;
import org.agnitas.web.forms.StrutsFormBase;
import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.list.LazyList;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.agnitas.emm.core.Permission;
import com.agnitas.web.ComAdminAction;

/**
 * Implementation of <strong>Form</strong> that holds data for user forms.
 */
public class UserFormEditForm extends StrutsFormBase{
    
    private static final long serialVersionUID = 5344970502954958422L;

	/** Holds value of property action. */
    private int action;
    
    /** Holds value of property formID. */
    private int formID;
    
    /** Holds value of property formName. */
    private String formName;
    
    /** Holds value of property description. */
    private String description;
    
    /** Holds value of property startActionID. */
    private int startActionID;
    
    /** Holds value of property endActionID. */
    private int endActionID;
    
    /** Holds value of property successTemplate. */
    private String successTemplate;
    
    /** Holds value of property errorTemplate. */
    private String errorTemplate;

    protected boolean fromListPage;

    private String successUrl;
    private String errorUrl;
    private boolean successUseUrl;
    private boolean errorUseUrl;

    /** Is success template contains Velocity statements. */
    private boolean successUseVelocity;
    /** Is error template contains Velocity statements. */
    private boolean errorUseVelocity;

    private boolean isActive;

    public UserFormEditForm() {
        isActive = true;
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
     * @return messages for errors, that occured. 
     */
    @Override
	public ActionErrors formSpecificValidate(ActionMapping mapping,
                                             HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if (action == ComAdminAction.ACTION_SAVE) {
            if (AgnUtils.allowed(request, Permission.FORMS_CHANGE)) {
                if (getFormName() == null || getFormName().trim().equals("") || getFormName().trim().length() < 3) {
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.name.too.short"));
                } else {
                    if (getFormName().length() > 50) {
                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.form.nameTooLong"));
                    }
                }
            } else {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.permissionDenied"));
            }
        }

        return errors;
    }

    @Override
    protected ActionMessages checkForHtmlTags(HttpServletRequest request) {
        if (action != UserFormEditAction.ACTION_VIEW_WITHOUT_LOAD) {
            return super.checkForHtmlTags(request);
        }
        return new ActionErrors();
    }
    
    @Override
	protected boolean isParameterExcludedForUnsafeHtmlTagCheck( String parameterName, HttpServletRequest request) {
    	return parameterName.equals( "errorTemplate") || parameterName.equals( "successTemplate");
	}

	/** Getter for property action.
     * @return Value of property action.
     */
    public int getAction() {
        return this.action;
    }
    
    /** Setter for property action.
     * @param action New value of property action.
     */
    public void setAction(int action) {
        this.action = action;
    }
    
    /** Getter for property fontID.
     * @return Value of property fontID.
     *
     */
    public int getFormID() {
        return this.formID;
    }
    
    /**
     * Setter for property fontID.
     * 
     * @param formID 
     */
    public void setFormID(int formID) {
        this.formID = formID;
    }
    
    /** Getter for property fontName.
     * @return Value of property fontName.
     *
     */
    public String getFormName() {
        return this.formName;
    }
    
    /**
     * Setter for property fontName.
     * 
     * @param formName 
     */
    public void setFormName(String formName) {
        this.formName = formName.trim();
    }
    
    /**
     * Getter for property description.
     * @return Value of property description.
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Setter for property description.
     * @param description New value of property description.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Getter for property startActionID.
     * @return Value of property startActionID.
     */
    public int getStartActionID() {
        return this.startActionID;
    }
    
    /**
     * Setter for property startActionID.
     * @param startActionID New value of property startActionID.
     */
    public void setStartActionID(int startActionID) {
        this.startActionID = startActionID;
    }
    
    /**
     * Getter for property endActionID.
     * @return Value of property endActionID.
     */
    public int getEndActionID() {
        return this.endActionID;
    }
    
    /**
     * Setter for property endActionID.
     * @param endActionID New value of property endActionID.
     */
    public void setEndActionID(int endActionID) {
        this.endActionID = endActionID;
    }
    
    /**
     * Getter for property successTemplate.
     * @return Value of property successTemplate.
     */
    public String getSuccessTemplate() {
        return this.successTemplate;
    }
    
    /**
     * Setter for property successTemplate.
     * @param successTemplate New value of property successTemplate.
     */
    public void setSuccessTemplate(String successTemplate) {
        this.successTemplate = successTemplate;
    }
    
    /**
     * Getter for property errorTemplate.
     * @return Value of property errorTemplate.
     */
    public String getErrorTemplate() {
        return this.errorTemplate;
    }
    
    /**
     * Setter for property errorTemplate.
     * @param errorTemplate New value of property errorTemplate.
     */
    public void setErrorTemplate(String errorTemplate) {
        this.errorTemplate = errorTemplate;
    }


    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getErrorUrl() {
        return errorUrl;
    }

    public void setErrorUrl(String errorUrl) {
        this.errorUrl = errorUrl;
    }

    public boolean isSuccessUseUrl() {
        return successUseUrl;
    }

    public void setSuccessUseUrl(boolean successUseUrl) {
        this.successUseUrl = successUseUrl;
    }

    public boolean isErrorUseUrl() {
        return errorUseUrl;
    }

    public void setErrorUseUrl(boolean errorUseUrl) {
        this.errorUseUrl = errorUseUrl;
    }

    @Override
    public void reset(ActionMapping map, HttpServletRequest request) {
        super.reset(map, request);
        Factory<String> factory = () -> Integer.toString(0);
        columnwidthsList = LazyList.lazyList(new ArrayList<>(), factory);
    }

    public boolean getFromListPage() {
        return fromListPage;
    }

    public void setFromListPage(boolean fromListPage) {
        this.fromListPage = fromListPage;
    }

    public boolean isSuccessUseVelocity() {
        return successUseVelocity;
    }

    public void setSuccessUseVelocity(boolean successUseVelocity) {
        this.successUseVelocity = successUseVelocity;
    }

    public boolean isErrorUseVelocity() {
        return errorUseVelocity;
    }

    public void setErrorUseVelocity(boolean errorUseVelocity) {
        this.errorUseVelocity = errorUseVelocity;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean active) {
        isActive = active;
    }
}
