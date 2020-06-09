/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.complexity.service;

import java.util.Objects;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.agnitas.emm.core.target.complexity.bean.TargetComplexityEvaluationState;
import com.agnitas.emm.core.target.eql.ast.AbstractEqlNode;

public abstract class AbstractTargetComplexityAnalyzer<T extends AbstractEqlNode> implements TargetComplexityAnalyzer<T> {
    private static final Logger logger = Logger.getLogger(AbstractTargetComplexityAnalyzer.class);

    @Lazy
    @Autowired
    private TargetComplexityAnalyzerMap analyzerMap;
    private Class<T> nodeType;

    public AbstractTargetComplexityAnalyzer(Class<T> nodeType) {
        this.nodeType = Objects.requireNonNull(nodeType);
    }

    @Override
    public final void analyzeAbstract(AbstractEqlNode node, boolean negative, TargetComplexityEvaluationState state) {
        if (nodeType.isInstance(node)) {
            analyze(nodeType.cast(node), negative, state);
        } else {
            Class<? extends AbstractEqlNode> customNodeType = node.getClass();
            TargetComplexityAnalyzer<?> analyzer = analyzerMap.get(customNodeType);

            if (analyzer == null) {
                logger.debug("No complexity analyzer defined for " + customNodeType.getSimpleName());
            } else {
                analyzer.analyzeAbstract(node, negative, state);
            }
        }
    }

    @Override
	public final Class<T> getNodeType() {
        return nodeType;
    }

    protected final void add(TargetComplexityEvaluationState state, TargetComplexityCriterion criterion) {
        state.setComplexityIndex(state.getComplexityIndex() + criterion.getComplexity());
    }
}
