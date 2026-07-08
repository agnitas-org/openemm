/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder.converter;

import java.util.Arrays;
import java.util.LinkedList;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.emm.core.target.eql.codegen.DataType;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderOperator;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderRuleNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConversionException;

public class DateRuleConverter extends GenericRuleConverter {

    public static final int MIN_EXPECTED_SIZE = 1;

    private static final String SINGLE_QUOTE = "'";

    private static final String TODAY = "TODAY";

    private static final String PLUS = "+";

    private static final String MINUS = "-";

    @Override
    public String convert(QueryBuilderRuleNode node, DataType dataType, String operator) throws QueryBuilderToEqlConversionException {
        Object[] values = QueryBuilderUtil.getRuleNodeValueAsArray(node);
        LinkedList<Object> valuesList = new LinkedList<>(Arrays.asList(values));
        String value = (String) valuesList.pop();

        if (!TODAY.equalsIgnoreCase(value)) {
            value = SINGLE_QUOTE + value + SINGLE_QUOTE;
        } else {
            value = TODAY;
            if (valuesList.size() > MIN_EXPECTED_SIZE) {
                String rawValue = (String) valuesList.pop();
                if (StringUtils.isNotBlank(rawValue)) {
                    int offset = parseOffset(node, rawValue);
                    String sign = valuesList.pop().equals("sub") ? MINUS : PLUS;
                    value += sign;
                    value += Math.abs(offset);
                }

            }
        }

        boolean addEmptyFieldCheck = node.isIncludeEmpty() &&
                QueryBuilderOperator.NEQ.queryBuilderName().equals(node.getOperator());

        String ruleClause = String.format("`%s` %s %s DATEFORMAT '%s'", node.getId(), operator, value, valuesList.pollLast());

        if (addEmptyFieldCheck) {
            return String.format("(%s OR `%s` IS EMPTY)",
                    ruleClause,
                    node.getId());
        } else {
            return ruleClause;
        }

    }

    @Override
    protected void validate(QueryBuilderRuleNode node, DataType dataType, String operator) throws QueryBuilderToEqlConversionException {
        Object[] values = QueryBuilderUtil.getRuleNodeValueAsArray(node);

        if (ArrayUtils.getLength(values) < 2) {
            throw new QueryBuilderToEqlConversionException("Invalid rule value for node " + node);
        }

        if (TODAY.equalsIgnoreCase((String) values[0]) && ArrayUtils.getLength(values) > 2) {
            // validate if values contains ['TODAY', '%operator%', '%offset%', '%dateformat%'] data
            parseOffset(node, (String) values[1]);
        }
    }

    private int parseOffset(QueryBuilderRuleNode node, String rawValue) throws QueryBuilderToEqlConversionException {
        if (StringUtils.isBlank(rawValue)) {
            return 0;
        }

        try {
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException e) {
            throw new QueryBuilderToEqlConversionException("Invalid rule value for node " + node, e);
        }
    }
}
