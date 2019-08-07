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

import com.agnitas.emm.core.target.eql.ast.NumericConstantAtomEqlNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderGroupNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderRuleNode;

public class NumericConstantParser extends GenericValueExtractorEqlNodeParser<NumericConstantAtomEqlNode> {

    @Override
    protected QueryBuilderGroupNode parse(NumericConstantAtomEqlNode node, QueryBuilderRuleNode ruleNode, QueryBuilderGroupNode groupNode) {
        String value = node.getValue();
        Object ruleValue = ruleNode.getValue();

        if (ruleValue instanceof Object[]) {
            List<Object> values = new LinkedList<>((Arrays.asList((Object[]) ruleValue)));
            values.add(value);
            ruleNode.setValue(values.toArray());
        } else {
            ruleNode.setValue(value);
        }
        return groupNode;
    }
}
