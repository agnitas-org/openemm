/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.agnitas.target.ConditionalOperator;

import com.agnitas.beans.IntEnum;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;

public interface WorkflowDecision extends WorkflowMailingAware {

    ConditionalOperator[] DECISION_OPERATORS = {
			ConditionalOperator.EQ,
			ConditionalOperator.NEQ,
			ConditionalOperator.GT,
			ConditionalOperator.LT,
			ConditionalOperator.LEQ,
			ConditionalOperator.GEQ,
			ConditionalOperator.IS,
			ConditionalOperator.LIKE,
			ConditionalOperator.NOT_LIKE,
			ConditionalOperator.CONTAINS,
			ConditionalOperator.NOT_CONTAINS,
			ConditionalOperator.STARTS_WITH,
			ConditionalOperator.NOT_STARTS_WITH
	};

    Map<ConditionalOperator, String> OPERATOR_TYPE_SUPPORT_MAP = WorkflowUtils.getOperatorTypeSupportMap();

    WorkflowDecisionType getDecisionType();

    void setDecisionType(WorkflowDecisionType decisionType);

    WorkflowDecisionCriteria getDecisionCriteria();

    void setDecisionCriteria(WorkflowDecisionCriteria decisionCriteria);

    WorkflowReactionType getReaction();

    void setReaction(WorkflowReactionType reaction);

    int getLinkId();

    void setLinkId(int linkId);

    String getProfileField();

    void setProfileField(String profileField);

    WorkflowAutoOptimizationCriteria getAoDecisionCriteria();

    void setAoDecisionCriteria(WorkflowAutoOptimizationCriteria aoDecisionCriteria);

    String getThreshold();

    void setThreshold(String threshold);

    List<WorkflowRule> getRules();

    void setRules(List<WorkflowRule> rules);

	Date getDecisionDate();

	void setDecisionDate(Date decisionDate);

	void setDateFormat(String dateFormat);

	String getDateFormat();

    boolean isIncludeVetoed();

    void setIncludeVetoed(boolean includeVetoed);
    
    boolean hasReactionCriteria();

    enum WorkflowDecisionType implements IntEnum {
        TYPE_DECISION(1),
        TYPE_AUTO_OPTIMIZATION(2);

        private final int id;

        public static WorkflowDecisionType fromId(int id) {
            return IntEnum.fromId(WorkflowDecisionType.class, id);
        }

        public static WorkflowDecisionType fromId(int id, boolean safe) {
            return IntEnum.fromId(WorkflowDecisionType.class, id, safe);
        }

        WorkflowDecisionType(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }
    }

    enum WorkflowDecisionCriteria implements IntEnum {
        DECISION_REACTION(1),
        DECISION_PROFILE_FIELD(2);

        private final int id;

        public static WorkflowDecisionCriteria fromId(int id) {
            return IntEnum.fromId(WorkflowDecisionCriteria.class, id);
        }

        public static WorkflowDecisionCriteria fromId(int id, boolean safe) {
            return IntEnum.fromId(WorkflowDecisionCriteria.class, id, safe);
        }

        WorkflowDecisionCriteria(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }
    }

    enum WorkflowAutoOptimizationCriteria implements IntEnum {
        AO_CRITERIA_CLICKRATE(1),
        AO_CRITERIA_OPENRATE(2),
        AO_CRITERIA_REVENUE(3);

        private final int id;

        public static WorkflowAutoOptimizationCriteria fromId(int id) {
            return IntEnum.fromId(WorkflowAutoOptimizationCriteria.class, id);
        }

        public static WorkflowAutoOptimizationCriteria fromId(int id, boolean safe) {
            return IntEnum.fromId(WorkflowAutoOptimizationCriteria.class, id, safe);
        }

        WorkflowAutoOptimizationCriteria(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }
    }
}
