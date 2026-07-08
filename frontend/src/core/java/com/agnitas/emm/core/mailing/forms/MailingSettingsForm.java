/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.forms;

import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingContentType;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.mailing.bean.MailingParameter;
import com.agnitas.emm.core.mailing.enums.MailingSettingsViewType;
import com.agnitas.emm.core.mailing.forms.mediatype.EmailMediatypeForm;
import com.agnitas.emm.core.mailing.forms.mediatype.MediatypeForm;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MailingSettingsForm {

    private int parentId;
    private int archiveId;
    private int targetMode = Mailing.TARGET_MODE_AND;
    private int mailinglistId;
    private String planDate;
    private String shortname;
    private String description;
    private String targetExpression;
    private boolean archived;
    private boolean needsTarget;
    private boolean useDynamicTemplate;
    private boolean assignTargetGroups;
    private MailingType mailingType = MailingType.NORMAL;
    private Collection<Integer> targetGroupIds;
    private MailingContentType mailingContentType;
    private List<MailingParameter> params = new ArrayList<>();
    protected Map<Integer, MediatypeForm> mediatypes = new HashMap<>();
    private MailingSettingsViewType viewType;
    private SplitSettings splitSettings = new SplitSettings();

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public boolean isUseDynamicTemplate() {
        return useDynamicTemplate;
    }

    public void setUseDynamicTemplate(boolean useDynamicTemplate) {
        this.useDynamicTemplate = useDynamicTemplate;
    }

    public boolean isNeedsTarget() {
        return needsTarget;
    }

    public void setNeedsTarget(boolean needsTarget) {
        this.needsTarget = needsTarget;
    }

    public int getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(int archiveId) {
        this.archiveId = archiveId;
    }

    public int getTargetMode() {
        return targetMode;
    }

    public void setTargetMode(int targetMode) {
        this.targetMode = targetMode;
    }

    public int getMailinglistId() {
        return mailinglistId;
    }

    public void setMailinglistId(int mailinglistId) {
        this.mailinglistId = mailinglistId;
    }

    public String getPlanDate() {
        return planDate;
    }

    public void setPlanDate(String planDate) {
        this.planDate = planDate;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public SplitSettings getSplitSettings() {
        return splitSettings;
    }

    public void setSplitSettings(SplitSettings splitSettings) {
        this.splitSettings = splitSettings;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTargetExpression() {
        return targetExpression;
    }

    public void setTargetExpression(String targetExpression) {
        this.targetExpression = targetExpression;
    }

    public Collection<Integer> getTargetGroupIds() {
        return targetGroupIds;
    }

    public void setTargetGroupIds(Collection<Integer> targetGroupIds) {
        this.targetGroupIds = targetGroupIds;
    }
    
    public MailingContentType getMailingContentType() {
   		return mailingContentType;
   	}
   
   	public void setMailingContentType(MailingContentType mailingContentType) {
   		this.mailingContentType = mailingContentType;
   	}
   
   	public boolean isMailingContentTypeAdvertising() {
   		return mailingContentType == null || mailingContentType == MailingContentType.advertising;
   	}
   
   	public void setMailingContentTypeAdvertising(boolean mailingContentTypeAdvertising) {
   		mailingContentType = mailingContentTypeAdvertising ? MailingContentType.advertising : MailingContentType.transaction;
   	}

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isAssignTargetGroups() {
        return assignTargetGroups;
    }

    public void setAssignTargetGroups(boolean assignTargetGroups) {
        this.assignTargetGroups = assignTargetGroups;
    }

    public MailingType getMailingType() {
        return mailingType;
    }

    public void setMailingType(MailingType mailingType) {
        this.mailingType = mailingType;
    }

    public Map<Integer, MediatypeForm> getMediatypes() {
        return mediatypes;
    }

    public void setMediatypes(Map<Integer, MediatypeForm> mediatypes) {
        this.mediatypes = mediatypes;
    }

    public MailingSettingsViewType getViewType() {
        return viewType;
    }

    public void setViewType(MailingSettingsViewType viewType) {
        this.viewType = viewType;
    }

    public EmailMediatypeForm getEmailMediatype() {
        return (EmailMediatypeForm) mediatypes.computeIfAbsent(MediaTypes.EMAIL.getMediaCode(), mt -> {
            EmailMediatypeForm form = new EmailMediatypeForm();
            form.setPriority(getMediatypePriority());
            return form; 
        });    
    }

    public Optional<MediatypeForm> getMediatypeForm(MediaTypes mediaType) {
        if (MediaTypes.EMAIL.equals(mediaType)) {
            return Optional.of(getEmailMediatype());
        }

        return Optional.empty();
    }

    protected int getMediatypePriority() {
        Set<Integer> keys = getMediatypes().keySet();
        return CollectionUtils.isEmpty(keys) ? 0 : Collections.max(keys) + 1;
    }

    public List<MailingParameter> getParams() {
        return params;
    }

    public void setParams(List<MailingParameter> params) {
        this.params = params;
    }

    public void clearTargetsData() {
        setTargetGroupIds(Collections.emptyList());
        splitSettings.clear();
    }

    public boolean isMediaTypeActive(MediaTypes mediaType) {
        return getMediatypeForm(mediaType)
                .map(MediatypeForm::isActive)
                .orElse(false);
    }
}
