/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder.converter;

import com.agnitas.emm.core.target.eql.codegen.DataType;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderOperator;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderRuleNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConversionException;

public class DefaultRuleConverter extends GenericRuleConverter {

    @Override
    public String convert(QueryBuilderRuleNode ruleNode, DataType dataType, String operator) throws QueryBuilderToEqlConversionException {
        boolean addEmptyFieldCheck = ruleNode.isIncludeEmpty() &&
                QueryBuilderOperator.NEQ.queryBuilderName().equals(ruleNode.getOperator());

        String ruleClause = String.format("`%s` %s %s", ruleNode.getId(), operator, valueOfRule(ruleNode, dataType));

        if (addEmptyFieldCheck) {
            return String.format("(%s OR `%s` IS EMPTY)",
                    ruleClause,
                    ruleNode.getId());
        } else {
            return ruleClause;
        }
    }
}
