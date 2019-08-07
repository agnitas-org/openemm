/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web.forms;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.beans.Campaign;
import org.agnitas.beans.MailingBase;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.SafeString;
import org.agnitas.web.EmmActionAction;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;

public class EmmActionForm extends StrutsFormBase {
    private static final long serialVersionUID = -6049830951608775632L;
    
	private String shortname;
    private String description;
    private int actionID;
    private int action;
    private List<AbstractActionOperationParameters> actions;
    private Map<?, ?> used;
    private List<Campaign> campaigns;
    private List<MailingBase> mailings;
    protected boolean fromListPage;
    private int type;
    private int deleteModule;
    private String newModule;
    private boolean isActive = true;

	public EmmActionForm() {
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

        switch (action) {
            case EmmActionAction.ACTION_NEW:
                action = EmmActionAction.ACTION_VIEW;
                shortname = SafeString.getLocaleString("default.Name", AgnUtils.getLocale(request));
                description = SafeString.getLocaleString("default.description", AgnUtils.getLocale(request));
                actionID = 0;
                actions = null;
                deleteModule = 0;
                type = 0;
                break;

            case EmmActionAction.ACTION_SAVE:
                if (StringUtils.isEmpty(shortname) || shortname.length() < 3) {
                    errors.add("shortname", new ActionMessage("error.name.too.short"));
                } else if (shortname.length() > 50) {
                    errors.add("shortname", new ActionMessage("error.action.nameTooLong"));
                }

                if (actions != null) {
                    for (AbstractActionOperationParameters operation : actions) {
                        if (operation.getCompanyId() == 0) {
                            operation.setCompanyId(AgnUtils.getCompanyID(request));
                        }
                        operation.validate(errors, getWebApplicationContext());
                    }
                }
                break;
        }

        return errors;
    }

    /**
     * Getter for property shortname.
     *
     * @return Value of property shortname.
     */
    public String getShortname() {
        return shortname;
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
     * Getter for property description.
     *
     * @return Value of property description.
     */
    public String getDescription() {
        return description;
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
     * Getter for property actionID.
     *
     * @return Value of property actionID.
     */
    public int getActionID() {
        return actionID;
    }

    /**
     * Setter for property actionID.
     *
     * @param actionID New value of property actionID.
     */
    public void setActionID(int actionID) {
        this.actionID = actionID;
    }

    /**
     * Getter for property action.
     *
     * @return Value of property action.
     */
    public int getAction() {
        return action;
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
     * Getter for property actions.
     *
     * @return Value of property actions.
     */
    public List<AbstractActionOperationParameters> getActions() {
        return actions;
    }

    /**
     * Setter for property actions.
     *
     * @param actions New value of property actions.
     */
    public void setActions(List<AbstractActionOperationParameters> actions) {
        this.actions = actions;
    }

    /**
     * Getter for property type.
     *
     * @return Value of property type.
     */
    public int getType() {
        return type;
    }

    /**
     * Setter for property type.
     *
     * @param type New value of property type.
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Getter for property deleteModule.
     *
     * @return Value of property deleteModule.
     */
    public int getDeleteModule() {
        return deleteModule;
    }

    /**
     * Setter for property deleteModule.
     *
     * @param deleteModule New value of property deleteModule.
     */
    public void setDeleteModule(int deleteModule) {
        this.deleteModule = deleteModule;
    }

    /**
     * Getter for property newModule.
     *
     * @return Value of property newModule.
     */
    public String getNewModule() {
        return newModule;
    }

    /**
     * Setter for property newModule.
     *
     * @param newModule New value of property newModule.
     */
    public void setNewModule(String newModule) {
        this.newModule = newModule;
    }

	public Map<?, ?> getUsed() {
		return used;
	}

	public void setUsed(Map<?, ?> used) {
		this.used = used;
	}

    public List<Campaign> getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(List<Campaign> campaigns) {
        this.campaigns = campaigns;
    }

    public List<MailingBase> getMailings() {
        return mailings;
    }

    public void setMailings(List<MailingBase> mailings) {
        this.mailings = mailings;
    }

    public boolean getFromListPage() {
        return fromListPage;
    }

    public void setFromListPage(boolean fromListPage) {
        this.fromListPage = fromListPage;
    }

	@Override
	protected boolean isParameterExcludedForUnsafeHtmlTagCheck(String parameterName, HttpServletRequest request) {
		return parameterName.endsWith(".script") || parameterName.endsWith(".textMail") || parameterName.endsWith(".htmlMail");
	}

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
