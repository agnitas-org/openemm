/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.actions.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.agnitas.actions.EmmAction;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;

/** Main Container for Actions. Allows managing and executing Actions with an easy interface
 */
public class EmmActionImpl implements EmmAction {
    
    /**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = -5500708140184014085L;

	/** Holds value of property companyID. */
    protected int companyID;
    
    /**
     * Holds value of property id.
     */
    protected int id;
    
    /** Holds value of property shortname. */
    protected String shortname;
    
    /** Holds value of property description. */
    protected String description = "";

    private List<AbstractActionOperationParameters> actionOperations = new ArrayList<>();
    
    /**
     * Holds value of property type.
     */
    protected int type;
    
    /**
     * Names of forms , which use this Action
     */
    protected List<String> formNameList = new ArrayList<>();

    protected Timestamp changeDate;
    
    protected Timestamp creationDate;

    private boolean isActive = true;

    /** Getter for property companyID.
     *
     * @return Value of property companyID.
     */
    @Override
    public int getCompanyID() {
        return companyID;
    }
    
    /** Setter for property companyID.
     *
     * @param companyID New value of property companyID.
     */
    @Override
    public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }
    
    /**
     * Getter for property id.
     * 
     * @return Value of property id.
     */
    @Override
    public int getId() {
        return id;
    }
    
    /**
     * Setter for property id.
     * 
     * @param actionID 
     */
    @Override
    public void setId(int actionID) {
        this.id = actionID;
    }
    
    /** Getter for property shortname.
     *
     * @return Value of property shortname.
     */
    @Override
    public String getShortname() {
        return shortname;
    }
    
    /** Setter for property shortname.
     *
     * @param shortname New value of property shortname.
     */
    @Override
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }
    
    /** Getter for property description.
     *
     * @return Value of property description.
     */
    @Override
    public String getDescription() {
        return description;
    }
    
    /** Setter for property description.
     *
     * @param description New value of property description.
     */
    @Override
    public void setDescription(String description) {
        if(description == null || description.length() < 1) {
            description = " ";
        }
        this.description = description;
    }
    
    /**
	 * @return the actionOperations
	 */
    @Override
	public List<AbstractActionOperationParameters> getActionOperations() {
		return actionOperations;
	}

	/**
	 * @param actionOperations the actionOperations to set
	 */
    @Override
	public void setActionOperations(List<AbstractActionOperationParameters> actionOperations) {
		this.actionOperations = actionOperations;
	}

	/**
     * Getter for property type.
     *
     * @return Value of property type.
     */
    @Override
    public int getType() {
        return this.type;
    }
    
    /**
     * Setter for property type.
     *
     * @param type New value of property type.
     */
    @Override
    public void setType(int type) {
        this.type = type;
    }

    @Override
	public int getUsed() {
		return CollectionUtils.size(formNameList);
	}

    @Override
    public String getFormNames() {
        return StringUtils.join(formNameList, "; ");
    }

    @Override
    public List<String> getFormNameList() {
        return formNameList;
    }

    @Override
    public void setFormNameList(List<String> formNameList) {
        this.formNameList = formNameList;
    }

    @Override
	public Timestamp getChangeDate() {
		return changeDate;
	}

	@Override
	public void setChangeDate(Timestamp changeDate) {
		this.changeDate = changeDate;
	}

	@Override
	public Timestamp getCreationDate() {
		return creationDate;
	}

	@Override
	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}

	@Override
    public boolean getIsActive() {
        return isActive;
    }

    @Override
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
