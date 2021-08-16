/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

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

import com.agnitas.web.ComTargetAction;

public class TargetForm extends StrutsFormBase {

    @Deprecated
	public static final int COLUMN_TYPE_STRING = 0;
    @Deprecated
	public static final int COLUMN_TYPE_NUMERIC = 1;
    @Deprecated
	public static final int COLUMN_TYPE_DATE = 2;
	@Deprecated
    public static final int COLUMN_TYPE_INTERVAL_MAILING = 3;
	@Deprecated
	public static final int COLUMN_TYPE_MAILING_RECEIVED = 4;
	@Deprecated
	public static final int COLUMN_TYPE_MAILING_OPENED = 5;
	@Deprecated
	public static final int COLUMN_TYPE_MAILING_CLICKED = 6;
	
    private static final long serialVersionUID = 45877020863407141L;
	private String shortname;
    private String description;
    private int targetID;
    private int action;
    private int numOfRecipients;
    
    /**
     * Last action we came from.
     */
    private int previousAction;
    
    /**
     * The list size a user prefers while viewing a table
     */
    private int preferredListSize = 20;
    
    /**
     * The list size has been loaded from the admin's properties
     */
    private boolean preferredListSizeLoaded = true;

    private boolean isShowStatistic = false;
    
    public TargetForm() {
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

        this.targetID = 0;
		this.action = StrutsActionBase.ACTION_SAVE;
        Locale aLoc = AgnUtils.getLocale(request);
        
        MessageResources text=(MessageResources)this.getServlet().getServletContext().getAttribute(org.apache.struts.Globals.MESSAGES_KEY);
        //MessageResources text=this.getServlet().getResources();

        this.shortname = text.getMessage(aLoc, "default.Name");
        this.description = text.getMessage(aLoc, "default.description");
        this.isShowStatistic = false;
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

        if (getAction() == ComTargetAction.ACTION_SAVE) {
            if (StringUtils.isEmpty(shortname) || shortname.length()<3) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.name.too.short"));
            }
        }

        return errors;
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
     * Getter for property targetID.
     *
     * @return Value of property targetID.
     */
    public int getTargetID() {
        return this.targetID;
    }
    
    /**
     * Setter for property targetID.
     *
     * @param targetID New value of property targetID.
     */
    public void setTargetID(int targetID) {
        this.targetID = targetID;
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
     * Getter for property numOfRecipients.
     *
     * @return Value of property numOfRecipients.
     */
	public int getNumOfRecipients() {
		return numOfRecipients;
	}

	/**
     * Setter for property numOfRecipients.
     *
     * @param numOfRecipients New value of property numOfRecipients.
     */
	public void setNumOfRecipients(int numOfRecipients) {
		this.numOfRecipients = numOfRecipients;
	}

	public int getPreferredListSize() {
		return preferredListSize;
	}

	public void setPreferredListSize(int preferredListSize) {
		this.preferredListSize = preferredListSize;
	}

	public boolean isPreferredListSizeLoaded() {
		return preferredListSizeLoaded;
	}

	public void setPreferredListSizeLoaded(boolean preferredListSizeLoaded) {
		this.preferredListSizeLoaded = preferredListSizeLoaded;
	}

	public int getPreviousAction() {
		return previousAction;
	}

	public void setPreviousAction(int previousAction) {
		this.previousAction = previousAction;
	}

    public boolean isShowStatistic() {
        return isShowStatistic;
    }

    public void setIsShowStatistic(boolean isShowStatistic) {
        this.isShowStatistic = isShowStatistic;
    }

}
