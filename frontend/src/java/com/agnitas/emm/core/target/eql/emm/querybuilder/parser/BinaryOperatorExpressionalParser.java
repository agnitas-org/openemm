/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder.parser;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.emm.core.target.eql.ast.AbstractEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderBaseNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderGroupNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderRuleNode;


public class BinaryOperatorExpressionalParser extends GenericEqlNodeParser<BinaryOperatorExpressionalEqlNode> {

    @Override
    protected QueryBuilderGroupNode parse(BinaryOperatorExpressionalEqlNode node, QueryBuilderGroupNode groupNode, Set<String> unknownProfileFields) throws EqlToQueryBuilderConversionException {
        AbstractEqlNode left = node.getLeft(),
                right = node.getRight();
        Map<Class<?>, EqlNodeParser<?>> map = configuration.getParserMapping();
        EqlNodeParser<?> leftParser = map.get(left.getClass()),
                rightParser = map.get(right.getClass());
        if (leftParser != null && rightParser != null) {
            leftParser.parse(left, groupNode, unknownProfileFields);
            rightParser.parse(right, groupNode, unknownProfileFields);
            List<QueryBuilderBaseNode> rules = groupNode.getRules();
            QueryBuilderRuleNode rule = (QueryBuilderRuleNode) rules.get(rules.size() - 1);
            rules.remove(rule);
            QueryBuilderRuleNode newRuleNode = new QueryBuilderRuleNode();
            newRuleNode.setOperator(node.getOperator().name().toLowerCase());
            newRuleNode.setId(rule.getId());
            List<Object> newRuleValue = new LinkedList<>();
            if (rule.getValue() instanceof Object[]) {
                newRuleValue.addAll(Arrays.asList((Object[])rule.getValue()));
            }else {
                newRuleValue.add(rule.getValue());
            }
            newRuleValue.add(rule.getOperator());
            newRuleNode.setValue(newRuleValue.toArray());
            rules.add(newRuleNode);
            return groupNode;
        }
        throw new EqlToQueryBuilderConversionException("Unable to find suitable parser for node " + node);
    }
}
