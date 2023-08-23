/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.complexity.service.impl;

import com.agnitas.emm.core.target.complexity.service.TargetComplexityAnalyzer;
import com.agnitas.emm.core.target.complexity.service.TargetComplexityAnalyzerMap;
import com.agnitas.emm.core.target.complexity.bean.TargetComplexityEvaluationCache;
import com.agnitas.emm.core.target.complexity.bean.TargetComplexityEvaluationState;
import com.agnitas.emm.core.target.complexity.service.TargetComplexityEvaluator;
import com.agnitas.emm.core.target.complexity.bean.impl.TargetComplexityEvaluationStateImpl;
import com.agnitas.emm.core.target.eql.ast.AbstractBooleanEqlNode;
import com.agnitas.emm.core.target.eql.parser.EqlParser;
import com.agnitas.emm.core.target.eql.parser.EqlParserConfiguration;
import com.agnitas.emm.core.target.eql.parser.EqlParserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public final class TargetComplexityEvaluatorImpl implements TargetComplexityEvaluator {
    private static final Logger logger = LogManager.getLogger(TargetComplexityEvaluatorImpl.class);

    private EqlParserConfiguration parserConfiguration;
    private EqlParser eqlParser;
    private TargetComplexityAnalyzerMap analyzerMap;

    public TargetComplexityEvaluatorImpl() {
        this.parserConfiguration = new EqlParserConfiguration().setCreateBooleanAnnotationNodes(true);
    }

    @Override
    public int evaluate(String eql, int companyId, TargetComplexityEvaluationCache cache) throws EqlParserException {
        return eqlParser.parseEql(eql, parserConfiguration)
            .getChild()
            .map(node -> evaluate(node, companyId, cache))
            .orElse(0);
    }

    protected int evaluate(AbstractBooleanEqlNode node, int companyId, TargetComplexityEvaluationCache cache) {
        Class<? extends AbstractBooleanEqlNode> nodeType = node.getClass();
        TargetComplexityAnalyzer<?> analyzer = analyzerMap.get(nodeType);

        if (analyzer == null) {
            logger.debug("No complexity analyzer defined for " + nodeType.getSimpleName());
            return 0;
        }

        try {
            TargetComplexityEvaluationState state = new TargetComplexityEvaluationStateImpl(companyId, cache);
            analyzer.analyzeAbstract(node, false, state);
            return state.getComplexityIndex();
        } catch (Exception e) {
            logger.error("Error occurred: " + e.getMessage(), e);
            return 0;
        }
    }

    @Required
    public void setEqlParser(EqlParser eqlParser) {
        this.eqlParser = eqlParser;
    }

    @Required
    public void setTargetComplexityAnalyzerMap(TargetComplexityAnalyzerMap analyzerMap) {
        this.analyzerMap = analyzerMap;
    }
}
