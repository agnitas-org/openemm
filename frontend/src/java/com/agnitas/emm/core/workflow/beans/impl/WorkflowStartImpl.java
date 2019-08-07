/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.agnitas.emm.core.workflow.beans.WorkflowDependency;
import com.agnitas.emm.core.workflow.beans.WorkflowDependencyType;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionType;
import com.agnitas.emm.core.workflow.beans.WorkflowStart;

public class WorkflowStartImpl extends WorkflowStartStopImpl implements WorkflowStart {
    private WorkflowStartType startType;

    public WorkflowStartImpl() {
        super();
        setType(WorkflowIconType.START.getId());
    }

    @Override
    public WorkflowStartType getStartType() {
        return startType;
    }

    @Override
    public void setStartType(WorkflowStartType startType) {
        this.startType = startType;
    }

    @Override
    public List<WorkflowDependency> getDependencies() {
        List<WorkflowDependency> dependencies = super.getDependencies();

        if (isFilled() && startType == WorkflowStartType.EVENT) {
            WorkflowStartEventType eventType = getEvent();

            if (eventType == WorkflowStartEventType.EVENT_REACTION) {
                WorkflowReactionType reaction = getReaction();
                int mailingId = getMailingId();
                int linkId = getLinkId();
                String column = getProfileField();

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
	                        if (StringUtils.isNotEmpty(column)) {
	                            dependencies.add(WorkflowDependencyType.PROFILE_FIELD_HISTORY.forName(column));
	                        }
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
            } else if (eventType == WorkflowStartEventType.EVENT_DATE) {
                String column = getDateProfileField();

                if (StringUtils.isNotEmpty(column)) {
                    dependencies.add(WorkflowDependencyType.PROFILE_FIELD.forName(column));
                }
            }
        }

        return dependencies;
    }
}
