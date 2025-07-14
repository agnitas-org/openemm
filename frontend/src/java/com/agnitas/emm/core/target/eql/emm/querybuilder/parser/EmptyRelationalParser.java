/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder.parser;

import java.util.Set;

import com.agnitas.emm.core.target.eql.ast.AbstractEqlNode;
import com.agnitas.emm.core.target.eql.ast.EmptyRelationalEqlNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderGroupNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderRuleNode;

public class EmptyRelationalParser extends GenericEqlNodeParser<EmptyRelationalEqlNode> {

    @Override
    protected QueryBuilderGroupNode parse(EmptyRelationalEqlNode node, QueryBuilderGroupNode groupNode, Set<String> profileFields) throws EqlToQueryBuilderConversionException {
        QueryBuilderRuleNode ruleNode = new QueryBuilderRuleNode();
        ruleNode.setOperator(node.getNotFlag() ? "is_not_empty" : "is_empty");
        groupNode.addRule(ruleNode);
        AbstractEqlNode child = node.getChild();
        EqlNodeParser<?> parser = configuration.getParserMapping().get(child.getClass());
        if (parser != null) {
            parser.parse(child, groupNode, profileFields);
            return groupNode;
        }
        throw new EqlToQueryBuilderConversionException("Unable to find suitable parser.");
    }
}
