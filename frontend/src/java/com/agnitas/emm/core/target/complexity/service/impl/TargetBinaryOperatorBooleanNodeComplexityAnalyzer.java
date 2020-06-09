/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.complexity.service.impl;

import com.agnitas.emm.core.target.complexity.service.AbstractTargetComplexityAnalyzer;
import com.agnitas.emm.core.target.complexity.service.TargetComplexityCriterion;
import com.agnitas.emm.core.target.complexity.bean.TargetComplexityEvaluationState;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorBooleanEqlNode;
import org.springframework.stereotype.Component;

@Component
public final class TargetBinaryOperatorBooleanNodeComplexityAnalyzer extends AbstractTargetComplexityAnalyzer<BinaryOperatorBooleanEqlNode> {
    private static final int MANY_CONDITIONS_CRITERIA_THRESHOLD = 5;

    public TargetBinaryOperatorBooleanNodeComplexityAnalyzer() {
        super(BinaryOperatorBooleanEqlNode.class);
    }

    @Override
    public void analyze(BinaryOperatorBooleanEqlNode node, boolean negative, TargetComplexityEvaluationState state) {
        analyze(state);

        analyzeAbstract(node.getLeft(), negative, state);
        analyzeAbstract(node.getRight(), negative, state);
    }

    private void analyze(TargetComplexityEvaluationState state) {
        int count = state.getLogicOperatorCount() + 1;

        state.setLogicOperatorCount(count);

        // Number of conditions is always a number of operators decreased by 1.
        if (count + 1 == MANY_CONDITIONS_CRITERIA_THRESHOLD) {
            add(state, TargetComplexityCriterion.MANY_CONDITIONS);
        }
    }
}
