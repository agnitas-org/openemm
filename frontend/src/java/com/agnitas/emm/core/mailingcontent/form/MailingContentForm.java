/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.form;

import com.agnitas.beans.ProfileField;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DynTagNameComparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MailingContentForm {

    private boolean showHTMLEditor;
    private int mailingID;
    private String shortname;
    private boolean isTemplate;
    private Map<String, DynTagDto> tags;
    private boolean isMailingUndoAvailable;
    private int gridTemplateId;
    private int workflowId;
    private List<TargetLight> availableTargetGroups;
    private List<ProfileField> availableInterestGroups;
    private List<String> dynTagNames = new ArrayList<>();

    private String customerExternalField = "";
    private String referenceExternalField = "";

    private Map<String, String[]> variabletypes = new HashMap<>();
    private String salutationTagType = "";
    private String salutationType = "";
    private String genderLanguage = "";

    public boolean isShowHTMLEditor() {
        return showHTMLEditor;
    }

    public void setShowHTMLEditor(boolean showHTMLEditor) {
        this.showHTMLEditor = showHTMLEditor;
    }

    public int getMailingID() {
        return mailingID;
    }

    public void setMailingID(int mailingID) {
        this.mailingID = mailingID;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public Map<String, DynTagDto> getTags() {
        return tags;
    }

    public void setTags(Map<String, DynTagDto> tags) {
        this.setTags(tags, false);
    }

    public void setTags(Map<String, DynTagDto> tags, boolean sortTags) {
        if (sortTags) {
            this.tags = AgnUtils.sortMap(tags, new DynTagNameComparator());
        } else {
            this.tags = tags;
        }
    }

    public List<String> getDynTagNames() {
        return dynTagNames;
    }

    public void setDynTagNames(List<String> dynTagNames) {
        this.dynTagNames = dynTagNames;
    }

    public boolean isIsTemplate() {
        return this.isTemplate;
    }

    public void setIsTemplate(boolean isTemplate) {
        this.isTemplate = isTemplate;
    }

    public boolean getIsMailingUndoAvailable() {
        return isMailingUndoAvailable;
    }

    public void setIsMailingUndoAvailable(boolean isMailingUndoAvailable) {
        this.isMailingUndoAvailable = isMailingUndoAvailable;
    }

    public int getGridTemplateId() {
        return gridTemplateId;
    }

    public void setGridTemplateId(int gridTemplateId) {
        this.gridTemplateId = gridTemplateId;
    }

    public int getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(int workflowId) {
        this.workflowId = workflowId;
    }

    public List<TargetLight> getAvailableTargetGroups() {
        return availableTargetGroups;
    }

    public void setAvailableTargetGroups(List<TargetLight> availableTargetGroups) {
        this.availableTargetGroups = availableTargetGroups;
    }

    public List<ProfileField> getAvailableInterestGroups() {
        return availableInterestGroups;
    }

    public void setAvailableInterestGroups(List<ProfileField> availableInterestGroups) {
        this.availableInterestGroups = availableInterestGroups;
    }

    public Map<String, String[]> getVariabletypes() {
        return variabletypes;
    }

    public void setVariabletypes(Map<String, String[]> variabletypes) {
        this.variabletypes = variabletypes;
    }

    public String getSalutationTagType() {
        return salutationTagType;
    }

    public void setSalutationTagType(String salutationTagType) {
        this.salutationTagType = salutationTagType;
    }

    public String getSalutationType() {
        return salutationType;
    }

    public void setSalutationType(String salutationType) {
        this.salutationType = salutationType;
    }

    public String getGenderLanguage() {
        return genderLanguage;
    }

    public void setGenderLanguage(String genderLanguage) {
        this.genderLanguage = genderLanguage;
    }

    public String getCustomerExternalField() {
        return customerExternalField;
    }

    public void setCustomerExternalField(String customerExternalField) {
        this.customerExternalField = customerExternalField;
    }

    public String getReferenceExternalField() {
        return referenceExternalField;
    }

    public void setReferenceExternalField(String referenceExternalField) {
        this.referenceExternalField = referenceExternalField;
    }
}
