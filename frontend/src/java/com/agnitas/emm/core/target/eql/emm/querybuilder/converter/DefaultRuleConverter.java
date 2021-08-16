/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder.converter;

import com.agnitas.emm.core.target.eql.codegen.DataType;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderCondition;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderOperator;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderRuleNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConversionException;

public class DefaultRuleConverter extends GenericRuleConverter {

    private static final char GRAVE_ACCENT = '`';

    private static final String WHITESPACE = " ";

    private static final String IS_EMPTY = "IS EMPTY";

    private static final char OPEN_PARENTHESIS = '(';

    private static final char CLOSE_PARENTHESIS = ')';

    @Override
    public String convert(QueryBuilderRuleNode ruleNode, DataType dataType, String operator) throws QueryBuilderToEqlConversionException {
        final boolean includeEmptyField = ruleNode.isIncludeEmpty(),
                addEmptyFieldCheck = includeEmptyField && QueryBuilderOperator.NEQ.queryBuilderName().equals(ruleNode.getOperator());
        final StringBuilder builder = new StringBuilder();
        if(addEmptyFieldCheck) {
            builder.append(OPEN_PARENTHESIS);
        }

        builder.append(GRAVE_ACCENT).append(ruleNode.getId()).append(GRAVE_ACCENT);
        builder.append(WHITESPACE);
        builder.append(operator);
        builder.append(WHITESPACE);
        builder.append(valueOfRule(ruleNode, dataType));

        if(addEmptyFieldCheck) {
            builder.append(WHITESPACE);
            builder.append(QueryBuilderCondition.OR.queryBuilderName());
            builder.append(WHITESPACE);
            builder.append(GRAVE_ACCENT).append(ruleNode.getId()).append(GRAVE_ACCENT);
            builder.append(WHITESPACE);
            builder.append(IS_EMPTY);
            builder.append(CLOSE_PARENTHESIS);
        }
        return builder.toString();
    }

}
