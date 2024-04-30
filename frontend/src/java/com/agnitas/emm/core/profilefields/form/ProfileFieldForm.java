/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.form;

import org.agnitas.web.forms.PaginationForm;
import org.apache.commons.lang3.StringUtils;

public class ProfileFieldForm extends PaginationForm {
    private boolean fieldVisible = true;
    private int fieldSort = 1000;
    private boolean line;
    private boolean isInterest;
    private boolean includeInHistory;
    private boolean useAllowedValues;
    private String[] allowedValues;
    private boolean[] allowedValuesValidationResults;
    private long fieldLength;
    private String fieldname;
    private String description = StringUtils.EMPTY;
    private String fieldType;
    private String fieldDefault = StringUtils.EMPTY;
    private boolean fieldNull = true;
    private String shortname = StringUtils.EMPTY;
    private String dependentWorkflows; // TODO check usage after ProfileFieldsControllerOld.java has been removed
    private String dependentWorkflowName; // TODO delete after ProfileFieldsControllerOld.java has been removed

    public boolean isFieldVisible() {
        return fieldVisible;
    }

    public void setFieldVisible(boolean fieldVisible) {
        this.fieldVisible = fieldVisible;
    }

    public int getFieldSort() {
        return fieldSort;
    }

    public void setFieldSort(int fieldSort) {
        this.fieldSort = fieldSort;
    }

    public boolean getLine() {
        return line;
    }

    public void setLine(boolean line) {
        this.line = line;
    }

    public boolean isInterest() {
        return isInterest;
    }

    public void setInterest(boolean interest) {
        isInterest = interest;
    }

    public boolean isIncludeInHistory() {
        return includeInHistory;
    }

    public void setIncludeInHistory(boolean includeInHistory) {
        this.includeInHistory = includeInHistory;
    }

    public boolean isUseAllowedValues() {
        return useAllowedValues;
    }

    public void setUseAllowedValues(boolean useAllowedValues) {
        this.useAllowedValues = useAllowedValues;
    }

    public String[] getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(String[] allowedValues) {
        this.allowedValues = allowedValues;
    }

    public boolean[] getAllowedValuesValidationResults() {
        return allowedValuesValidationResults;
    }

    public void setAllowedValuesValidationResults(boolean[] allowedValuesValidationResults) {
        this.allowedValuesValidationResults = allowedValuesValidationResults;
    }

    public long getFieldLength() {
        return fieldLength;
    }

    public void setFieldLength(long fieldLength) {
        this.fieldLength = fieldLength;
    }

    public String getFieldname() {
        return fieldname;
    }

    public void setFieldname(String fieldname) {
        this.fieldname = fieldname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldDefault() {
        return fieldDefault;
    }

    public void setFieldDefault(String fieldDefault) {
        this.fieldDefault = fieldDefault;
    }

    public boolean isFieldNull() {
        return fieldNull;
    }

    public void setFieldNull(boolean fieldNull) {
        this.fieldNull = fieldNull;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getDependentWorkflows() {
        return dependentWorkflows;
    }

    public void setDependentWorkflows(String dependentWorkflows) {
        this.dependentWorkflows = dependentWorkflows;
    }

    public String getDependentWorkflowName() {
        return dependentWorkflowName;
    }

    public void setDependentWorkflowName(String dependentWorkflowName) {
        this.dependentWorkflowName = dependentWorkflowName;
    }
}
