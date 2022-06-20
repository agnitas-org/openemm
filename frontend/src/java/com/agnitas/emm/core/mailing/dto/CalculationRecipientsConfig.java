/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.dto;

import java.util.Collection;

public class CalculationRecipientsConfig {
    private int companyId;
    private int splitId;
    private int mailingId;
    private int mailingListId;
    private int followUpMailing;
    private String followUpType;
    private boolean conjunction;
    private boolean assignTargetGroups;
    private boolean changeMailing;
    private Collection<Integer> targetGroupIds;
    private Collection<Integer> altgIds;

    public boolean isAssignTargetGroups() {
        return assignTargetGroups;
    }

    public void setAssignTargetGroups(boolean assignTargetGroups) {
        this.assignTargetGroups = assignTargetGroups;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getSplitId() {
        return splitId;
    }

    public void setSplitId(int splitId) {
        this.splitId = splitId;
    }

    public int getMailingListId() {
        return mailingListId;
    }

    public void setMailingListId(int mailingListId) {
        this.mailingListId = mailingListId;
    }

    public int getFollowUpMailing() {
        return followUpMailing;
    }

    public void setFollowUpMailing(int followUpMailing) {
        this.followUpMailing = followUpMailing;
    }

    public String getFollowUpType() {
        return followUpType;
    }

    public void setFollowUpType(String followUpType) {
        this.followUpType = followUpType;
    }

    public boolean isConjunction() {
        return conjunction;
    }

    public void setConjunction(boolean conjunction) {
        this.conjunction = conjunction;
    }

    public Collection<Integer> getTargetGroupIds() {
        return targetGroupIds;
    }

    public void setTargetGroupIds(Collection<Integer> targetGroupIds) {
        this.targetGroupIds = targetGroupIds;
    }

    public Collection<Integer> getAltgIds() {
        return altgIds;
    }

    public void setAltgIds(Collection<Integer> altgIds) {
        this.altgIds = altgIds;
    }

    public int getMailingId() {
        return mailingId;
    }

    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }

    public boolean isChangeMailing() {
        return changeMailing;
    }

    public void setChangeMailing(boolean changeMailing) {
        this.changeMailing = changeMailing;
    }
}
