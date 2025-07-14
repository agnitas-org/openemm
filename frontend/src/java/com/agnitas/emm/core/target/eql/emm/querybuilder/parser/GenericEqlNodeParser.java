/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder.parser;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import com.agnitas.emm.core.target.eql.ast.AbstractEqlNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderBaseNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderGroupNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderRuleNode;

public abstract class GenericEqlNodeParser<T> implements EqlNodeParser<T> {

    protected EqlToQueryBuilderParserConfiguration configuration;

    @Override
    public QueryBuilderGroupNode parse(AbstractEqlNode node, QueryBuilderGroupNode groupNode, Set<String> profileFields) throws EqlToQueryBuilderConversionException {
        T eqlNode = getEqlNode(node);
        final QueryBuilderGroupNode resultNode = parse(eqlNode, groupNode, profileFields);
        processNodeAfterParse(resultNode);
        return resultNode;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getEqlNode(AbstractEqlNode node) throws EqlToQueryBuilderConversionException {
        try {
            return (T) node;
        } catch (ClassCastException e) {
            throw new EqlToQueryBuilderConversionException("Cannot cast class, probably a configuration error.", e);
        }
    }

    protected abstract QueryBuilderGroupNode parse(T node, QueryBuilderGroupNode groupNode, Set<String> profileFields) throws EqlToQueryBuilderConversionException;

    protected void processNodeAfterParse(final QueryBuilderGroupNode groupNode) {
        final List<QueryBuilderBaseNode> rules = groupNode.getRules();
        if(CollectionUtils.isEmpty(rules)) {
            return;
        }
        QueryBuilderBaseNode lastNode = rules.get(rules.size() - 1);
        if(lastNode instanceof QueryBuilderGroupNode) {
            lastNode = refactorIfContainsIncludingEmptyRule((QueryBuilderGroupNode) lastNode);
            rules.set(rules.size() - 1, lastNode);
        }
    }

    private QueryBuilderBaseNode refactorIfContainsIncludingEmptyRule(final QueryBuilderGroupNode node) {
        final List<QueryBuilderBaseNode> rules = node.getRules();
        if (rules.size() != 2) {
            return node;
        }
        final QueryBuilderBaseNode leftNode = rules.get(0),
                rightNode = rules.get(1);
        if (!(leftNode instanceof QueryBuilderRuleNode && rightNode instanceof QueryBuilderRuleNode)) {
            return node;
        }

        final QueryBuilderRuleNode leftRule = (QueryBuilderRuleNode) leftNode,
                rightRule = (QueryBuilderRuleNode) rightNode;

        if (!leftRule.getId().equals(rightRule.getId())) {
            return node;
        }

        if (!("not_equal".equals(leftRule.getOperator()) && "is_empty".equals(rightRule.getOperator()) && "OR".equals(node.getCondition()))) {
            return node;
        }

        leftRule.setIncludeEmpty(true);
        return leftRule;
    }

    public void setConfiguration(EqlToQueryBuilderParserConfiguration configuration) {
        this.configuration = configuration;
    }
}
