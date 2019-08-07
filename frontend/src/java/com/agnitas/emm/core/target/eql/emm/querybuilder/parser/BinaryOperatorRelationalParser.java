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
import java.util.Set;

import com.agnitas.emm.core.target.eql.ast.AbstractEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorRelationalEqlNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderBaseNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderGroupNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderHelper;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderRuleNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.converter.DateRuleConverter;

public class BinaryOperatorRelationalParser extends GenericEqlNodeParser<BinaryOperatorRelationalEqlNode> {

    @Override
    public QueryBuilderGroupNode parse(BinaryOperatorRelationalEqlNode node, QueryBuilderGroupNode groupNode, Set<String> unknownProfileFields) throws EqlToQueryBuilderConversionException {
        QueryBuilderRuleNode rule = new QueryBuilderRuleNode();
        rule.setOperator(QueryBuilderHelper.relationalEqlOperatorToQueryBuilderString(node.getOperator()));
        groupNode.addRule(rule);
        AbstractEqlNode left = node.getLeft(),
                right = node.getRight();
        EqlNodeParser<?> leftParser = configuration.getParserMapping().get(left.getClass()),
                rightParser = configuration.getParserMapping().get(right.getClass());
        if (leftParser != null && rightParser != null) {
            leftParser.parse(left, groupNode, unknownProfileFields);
            rightParser.parse(right, groupNode, unknownProfileFields);
            updateDateNode(node, groupNode);
            return groupNode;
        }
        throw new EqlToQueryBuilderConversionException("Unable to find suitable parser for node " + node);
    }

    private void updateDateNode(BinaryOperatorRelationalEqlNode node, QueryBuilderGroupNode groupNode) {
        String dateFormat = node.getDateFormat();
        if (dateFormat != null) {
            List<QueryBuilderBaseNode> rules = groupNode.getRules();
            QueryBuilderRuleNode ruleNode = (QueryBuilderRuleNode) rules.get(rules.size() - 1);
            Object value = ruleNode.getValue();
            LinkedList<Object> values = new LinkedList<>();
            if (value instanceof Object[]){
                values.addAll(Arrays.asList((Object[]) value));
            }else {
                values.add(value);
            }
            if (values.size() > DateRuleConverter.MIN_EXPECTED_SIZE) {
                String operator = ruleNode.getOperator();
                ruleNode.setOperator(values.removeLast().toString());
                values.add(operator);
            }
            values.add(dateFormat);
            ruleNode.setValue(values.toArray());
            ruleNode.setType("date");
        }
    }

}
