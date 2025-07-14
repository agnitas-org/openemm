/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.bean;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/** Main Container for Actions. Allows managing and executing Actions with an easy interface */
public class EmmActionImpl implements EmmAction {

    private static final long serialVersionUID = -5500708140184014085L;

    protected int companyID;

    protected int id;
    protected String shortname;
    protected String description = "";

    private List<AbstractActionOperationParameters> actionOperations = new ArrayList<>();

    protected int type;

    /**
     * Names of forms , which use this Action
     */
    protected List<String> formNameList = new ArrayList<>();

    protected Timestamp changeDate;

    protected Timestamp creationDate;

    private boolean isActive = true;

    private boolean isAdvertising;

    private boolean deleted;

    @Override
    public int getCompanyID() {
        return companyID;
    }

    @Override
    public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int actionID) {
        this.id = actionID;
    }

    @Override
    public String getShortname() {
        return shortname;
    }

    @Override
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        if(description == null || description.length() < 1) {
            description = " ";
        }
        this.description = description;
    }

    @Override
    public List<AbstractActionOperationParameters> getActionOperations() {
        return actionOperations;
    }

    @Override
    public void setActionOperations(List<AbstractActionOperationParameters> actionOperations) {
        this.actionOperations = actionOperations;
    }

    @Override
    public int getType() {
        return this.type;
    }

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

    @Override
    public void setAdvertising(boolean advertising) {
        this.isAdvertising = advertising;
    }

    @Override
    public boolean isAdvertising() {
        return isAdvertising;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}

