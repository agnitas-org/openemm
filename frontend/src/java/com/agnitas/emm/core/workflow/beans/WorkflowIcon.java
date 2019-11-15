/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;

import java.util.ArrayList;
import java.util.List;

import com.agnitas.emm.core.workflow.service.WorkflowIconTypeSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.agnitas.emm.core.workflow.beans.impl.WorkflowActionBasedMailingImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowArchiveImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowDateBasedMailingImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowDeadlineImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowDecisionImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowExportImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowFollowupMailingImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowFormImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowImportImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowMailingImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowParameterImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowRecipientImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowReportImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowStartImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowStopImpl;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = WorkflowStartImpl.class, name = WorkflowIconType.Constants.START_VALUE),
        @JsonSubTypes.Type(value = WorkflowStopImpl.class, name = WorkflowIconType.Constants.STOP_VALUE),
        @JsonSubTypes.Type(value = WorkflowDecisionImpl.class, name = WorkflowIconType.Constants.DECISION_VALUE),
        @JsonSubTypes.Type(value = WorkflowDeadlineImpl.class, name = WorkflowIconType.Constants.DEADLINE_VALUE),
        @JsonSubTypes.Type(value = WorkflowParameterImpl.class, name = WorkflowIconType.Constants.PARAMETER_VALUE),
		@JsonSubTypes.Type(value = WorkflowReportImpl.class, name = WorkflowIconType.Constants.REPORT_VALUE),
		@JsonSubTypes.Type(value = WorkflowRecipientImpl.class, name = WorkflowIconType.Constants.RECIPIENT_VALUE),
        @JsonSubTypes.Type(value = WorkflowArchiveImpl.class, name = WorkflowIconType.Constants.ARCHIVE_VALUE),
        @JsonSubTypes.Type(value = WorkflowFormImpl.class, name = WorkflowIconType.Constants.FORM_VALUE),
        @JsonSubTypes.Type(value = WorkflowMailingImpl.class, name = WorkflowIconType.Constants.MAILING_VALUE),
		@JsonSubTypes.Type(value = WorkflowActionBasedMailingImpl.class, name = WorkflowIconType.Constants.ACTION_BASED_MAILING_VALUE),
		@JsonSubTypes.Type(value = WorkflowDateBasedMailingImpl.class, name = WorkflowIconType.Constants.DATE_BASED_MAILING_VALUE),
		@JsonSubTypes.Type(value = WorkflowFollowupMailingImpl.class, name = WorkflowIconType.Constants.FOLLOWUP_MAILING_VALUE),
		@JsonSubTypes.Type(value = WorkflowImportImpl.class, name = WorkflowIconType.Constants.IMPORT_VALUE),
		@JsonSubTypes.Type(value = WorkflowExportImpl.class, name = WorkflowIconType.Constants.EXPORT_VALUE)
})
public interface WorkflowIcon {
    int getId();

    void setId(int id);

    @JsonSerialize(using = WorkflowIconTypeSerializer.class)
    int getType();

    void setType(int type);

    int getX();

    void setX(int x);

    int getY();

    void setY(int y);

    boolean isFilled();

    void setFilled(boolean filled);

    String getIconTitle();

    void setIconTitle(String iconTitle);

    boolean isEditable();

    void setEditable(boolean editable);

    void setConnections(List<WorkflowConnection> connections);

    List<WorkflowConnection> getConnections();
    
    String getIconComment();
    
    void setIconComment(String comment);

    @JsonIgnore
    default List<WorkflowDependency> getDependencies() {
        return new ArrayList<>();
    }
}
