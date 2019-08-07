/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder.parser;

import java.util.Set;

import com.agnitas.emm.core.target.eql.ast.AbstractEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorBooleanEqlNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderGroupNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderHelper;

public class BinaryOperatorBooleanParser extends GenericEqlNodeParser<BinaryOperatorBooleanEqlNode> {

    @Override
    public QueryBuilderGroupNode parse(BinaryOperatorBooleanEqlNode node, QueryBuilderGroupNode groupNode, Set<String> unknownProfileFields) throws EqlToQueryBuilderConversionException {
        String condition = QueryBuilderHelper.booleanEqlOperatorToQueryBuilderString(node.getOperator());
        QueryBuilderGroupNode newGroupNode = groupNode;
        if (groupNode.getCondition() == null) {
            groupNode.setCondition(condition);
        }
        if (!condition.equals(groupNode.getCondition())) {
            newGroupNode = new QueryBuilderGroupNode(condition);
            groupNode.addRule(newGroupNode);
        }
        AbstractEqlNode left = node.getLeft(),
                right = node.getRight();
        EqlNodeParser<?> leftParser = configuration.getParserMapping().get(left.getClass()),
                rightParser = configuration.getParserMapping().get(right.getClass());
        if (leftParser != null && rightParser != null) {
            leftParser.parse(left, newGroupNode, unknownProfileFields);
            rightParser.parse(right, newGroupNode, unknownProfileFields);
            return groupNode;
        }
        throw new EqlToQueryBuilderConversionException("Unable to find suitable parser for node " + node);
    }

}
