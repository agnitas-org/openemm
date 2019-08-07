/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder.parser;

import java.util.Set;

import com.agnitas.emm.core.target.eql.ast.AbstractEqlNode;
import com.agnitas.emm.core.target.eql.ast.ContainsRelationalEqlNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderGroupNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderOperator;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderRuleNode;

public class ContainsRelationalConverter extends GenericEqlNodeParser<ContainsRelationalEqlNode> {
    @Override
    protected QueryBuilderGroupNode parse(ContainsRelationalEqlNode node, QueryBuilderGroupNode groupNode, Set<String> unknownProfileFields) throws EqlToQueryBuilderConversionException {
        AbstractEqlNode left = node.getLeft();
        EqlNodeParser<?> leftParser = configuration.getParserMapping().get(left.getClass());

        AbstractEqlNode right = node.getRight();
        EqlNodeParser<?> rightParser = configuration.getParserMapping().get(right.getClass());

        if (leftParser == null || rightParser == null) {
            throw new EqlToQueryBuilderConversionException("Unable to find suitable parser for node " + node);
        }

        QueryBuilderRuleNode ruleNode = new QueryBuilderRuleNode();
        ruleNode.setOperator(QueryBuilderOperator.CONTAINS.queryBuilderName());
        if (node.getNotFlag()) {
            ruleNode.setOperator(QueryBuilderOperator.NOT_CONTAINS.queryBuilderName());
        }
        groupNode.addRule(ruleNode);

        leftParser.parse(left, groupNode, unknownProfileFields);
        rightParser.parse(right, groupNode, unknownProfileFields);

        return groupNode;
    }
}
