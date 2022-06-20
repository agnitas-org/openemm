/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder.parser;

import java.util.Set;

import com.agnitas.emm.core.target.eql.ast.AbstractEqlNode;
import com.agnitas.emm.core.target.eql.ast.StartsWithRelationalEqlNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderGroupNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderOperator;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderRuleNode;

public class StartsWithRelationalConverter extends GenericEqlNodeParser<StartsWithRelationalEqlNode> {
    @Override
    protected QueryBuilderGroupNode parse(StartsWithRelationalEqlNode node, QueryBuilderGroupNode groupNode, Set<String> profileFields) throws EqlToQueryBuilderConversionException {
        AbstractEqlNode left = node.getLeft();
        EqlNodeParser<?> leftParser = configuration.getParserMapping().get(left.getClass());

        AbstractEqlNode right = node.getRight();
        EqlNodeParser<?> rightParser = configuration.getParserMapping().get(right.getClass());

        if (leftParser == null || rightParser == null) {
            throw new EqlToQueryBuilderConversionException("Unable to find suitable parser for node " + node);
        }

        QueryBuilderRuleNode ruleNode = new QueryBuilderRuleNode();
        ruleNode.setOperator(QueryBuilderOperator.STARTS_WITH.queryBuilderName());
        if (node.getNotFlag()) {
            ruleNode.setOperator(QueryBuilderOperator.NOT_STARTS_WITH.queryBuilderName());
        }
        groupNode.addRule(ruleNode);

        leftParser.parse(left, groupNode, profileFields);
        rightParser.parse(right, groupNode, profileFields);

        return groupNode;
    }
}
