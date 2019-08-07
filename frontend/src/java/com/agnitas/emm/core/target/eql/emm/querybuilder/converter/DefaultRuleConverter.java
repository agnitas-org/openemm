/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder.converter;

import com.agnitas.emm.core.target.eql.codegen.DataType;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderRuleNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConversionException;

public class DefaultRuleConverter extends GenericRuleConverter {

    private static final char GRAVE_ACCENT = '`';

    private static final String WHITESPACE = " ";

    @Override
    public String convert(QueryBuilderRuleNode ruleNode, DataType dataType, String operator) throws QueryBuilderToEqlConversionException {
        return String.valueOf(GRAVE_ACCENT) + ruleNode.getId() +
                GRAVE_ACCENT + WHITESPACE +
                operator + WHITESPACE + valueOfRule(ruleNode, dataType);
    }

}
