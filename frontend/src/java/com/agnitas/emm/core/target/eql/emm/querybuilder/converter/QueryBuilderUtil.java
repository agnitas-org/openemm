/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder.converter;

import java.util.Arrays;
import java.util.Collection;

import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderRuleNode;
import org.apache.commons.lang3.StringUtils;

public class QueryBuilderUtil {
    
    public static String getRuleNodeValueAsString(QueryBuilderRuleNode ruleNode) {
        if (ruleNode.getValue() != null) {
            return ruleNode.getValue().toString();
        }
        return "";
    }
    
    public static Object[] getRuleNodeValueAsArray(QueryBuilderRuleNode ruleNode) {
        Object value = ruleNode.getValue();
        if (value != null) {
            if (value instanceof Collection<?> values) {
                return values.toArray();
            }

            if (value instanceof Object[] values) {
                return values;
            }

            return new Object[]{value};
        }
        return new Object[0];
    }
    
    public static boolean containsAnyEmptyValue(Object[] values) {
        return Arrays.stream(values)
                .map(value -> value == null ? "" : value.toString())
                .anyMatch(StringUtils::isBlank);
    }
}
