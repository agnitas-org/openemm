/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.action.dto;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;

public class EmmActionDto {
    private int id;
    private String shortname;
    private String description;
    private List<String> formNames;
    private Date creationDate;
    private Date changeDate;
    private boolean active;
    private boolean isAdvertising;
    private int type;
    private List<? extends AbstractActionOperationParameters> operations;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getShortname() {
        return shortname;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isUseForms() {
        return CollectionUtils.isNotEmpty(formNames);
    }

    public void setFormNames(List<String> formNames) {
        this.formNames = formNames;
    }

    public List<String> getFormNames() {
        return formNames;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setOperations(List<? extends AbstractActionOperationParameters> operations) {
        this.operations = operations;
    }

    public List<? extends AbstractActionOperationParameters> getOperations() {
        return operations;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isAdvertising() {
        return isAdvertising;
    }

    public void setAdvertising(boolean advertising) {
        isAdvertising = advertising;
    }
}
