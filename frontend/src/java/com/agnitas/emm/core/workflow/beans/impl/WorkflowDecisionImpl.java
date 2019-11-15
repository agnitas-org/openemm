/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;

import com.agnitas.emm.core.workflow.beans.WorkflowDecision;
import com.agnitas.emm.core.workflow.beans.WorkflowDependency;
import com.agnitas.emm.core.workflow.beans.WorkflowDependencyType;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionType;
import com.agnitas.emm.core.workflow.beans.WorkflowRule;

public class WorkflowDecisionImpl extends BaseWorkflowIcon implements WorkflowDecision {
    private WorkflowDecisionType decisionType;
    private WorkflowDecisionCriteria decisionCriteria;
    private WorkflowReactionType reaction;
    private int mailingId;
    private int linkId;
    private String profileField;
	private String dateFormat;
    private WorkflowAutoOptimizationCriteria aoDecisionCriteria;
    private String threshold;
	private Date decisionDate;
    private List<WorkflowRule> rules = new ArrayList<>();
    private boolean includeVetoed = true;

    public WorkflowDecisionImpl() {
        super();
        setType(WorkflowIconType.DECISION.getId());
    }

    @Override
    public WorkflowDecisionType getDecisionType() {
        return decisionType;
    }

    @Override
    public void setDecisionType(WorkflowDecisionType decisionType) {
        this.decisionType = decisionType;
    }

    @Override
    public WorkflowDecisionCriteria getDecisionCriteria() {
        return decisionCriteria;
    }

    @Override
    public void setDecisionCriteria(WorkflowDecisionCriteria decisionCriteria) {
        this.decisionCriteria = decisionCriteria;
    }

    @Override
    public WorkflowReactionType getReaction() {
        return reaction;
    }

    @Override
    public void setReaction(WorkflowReactionType reaction) {
        this.reaction = reaction;
    }

    @Override
    public int getMailingId() {
        return mailingId;
    }

    @Override
    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }

    @Override
    public int getLinkId() {
        return linkId;
    }

    @Override
    public void setLinkId(int linkId) {
        this.linkId = linkId;
    }

    @Override
    public String getProfileField() {
        return profileField;
    }

    @Override
    public void setProfileField(String profileField) {
        this.profileField = profileField;
    }

    @Override
    public WorkflowAutoOptimizationCriteria getAoDecisionCriteria() {
        return aoDecisionCriteria;
    }

    @Override
    public void setAoDecisionCriteria(WorkflowAutoOptimizationCriteria aoDecisionCriteria) {
        this.aoDecisionCriteria = aoDecisionCriteria;
    }

    @Override
    public String getThreshold() {
        return threshold;
    }

    @Override
    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    @Override
    public List<WorkflowRule> getRules() {
        return rules;
    }

    @Override
    public void setRules(List<WorkflowRule> rules) {
        this.rules = rules;
    }

	@Override
	public Date getDecisionDate() {
		return decisionDate;
	}

	@Override
	public void setDecisionDate(Date decisionDate) {
		this.decisionDate = decisionDate;
	}

	@Override
	public String getDateFormat() {
		return dateFormat;
	}

	@Override
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

    @Override
    public boolean isIncludeVetoed() {
        return includeVetoed;
    }

    @Override
    public void setIncludeVetoed(boolean includeVetoed) {
        this.includeVetoed = includeVetoed;
    }

    @Override
    public List<WorkflowDependency> getDependencies() {
        List<WorkflowDependency> dependencies = super.getDependencies();

        if (isFilled() && decisionType == WorkflowDecisionType.TYPE_DECISION) {
            if (decisionCriteria == WorkflowDecisionCriteria.DECISION_REACTION) {
                if (reaction != null) {
                    switch (reaction) {
	                    case OPENED:
	                    case NOT_OPENED:
	                    case CLICKED:
	                    case NOT_CLICKED:
	                    case BOUGHT:
	                    case NOT_BOUGHT:
	                    case OPENED_AND_CLICKED:
	                    case OPENED_OR_CLICKED:
	                        if (mailingId > 0) {
	                            dependencies.add(WorkflowDependencyType.MAILING_REFERENCE.forId(mailingId));
	                        }
	                        break;
	                    case CLICKED_LINK:
	                        if (mailingId > 0) {
	                            dependencies.add(WorkflowDependencyType.MAILING_REFERENCE.forId(mailingId));
	                            if (linkId > 0) {
	                                dependencies.add(WorkflowDependencyType.MAILING_LINK.forId(linkId));
	                            }
	                        }
	                        break;
						case CHANGE_OF_PROFILE:
							break;
						case CONFIRMED_OPT_IN:
							break;
						case DOWNLOAD:
							break;
						case OPT_IN:
							break;
						case OPT_OUT:
							break;
						case WAITING_FOR_CONFIRM:
							break;
						default:
							break;
                    }
                }
            } else if (decisionCriteria == WorkflowDecisionCriteria.DECISION_PROFILE_FIELD) {
                if (StringUtils.isNotEmpty(profileField)) {
                    dependencies.add(WorkflowDependencyType.PROFILE_FIELD.forName(profileField));
                }
            }
        }

        return dependencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WorkflowDecisionImpl that = (WorkflowDecisionImpl) o;
        return mailingId == that.mailingId &&
                linkId == that.linkId &&
                includeVetoed == that.includeVetoed &&
                decisionType == that.decisionType &&
                decisionCriteria == that.decisionCriteria &&
                reaction == that.reaction &&
                Objects.equals(profileField, that.profileField) &&
                Objects.equals(dateFormat, that.dateFormat) &&
                aoDecisionCriteria == that.aoDecisionCriteria &&
                Objects.equals(threshold, that.threshold) &&
                Objects.equals(decisionDate, that.decisionDate) &&
                Objects.equals(rules, that.rules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), decisionType, decisionCriteria, reaction, mailingId, linkId, profileField, dateFormat, aoDecisionCriteria, threshold, decisionDate, rules, includeVetoed);
    }
}
