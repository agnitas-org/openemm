/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder.converter;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderRuleNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConversionException;

public class ClickedInMailingRuleConverter extends GenericMailingRelatedRuleConverter {

    private static final String ANY_LINK_VALUE = "-1";

    @Override
    protected String convertMailingRule(QueryBuilderRuleNode ruleNode, int companyId) throws QueryBuilderToEqlConversionException {
        Object[] values = (Object[]) ruleNode.getValue();
        String eql = "CLICKED %s IN MAILING %s";
        if (values[1].equals(ANY_LINK_VALUE)) {
            return String.format(eql, StringUtils.EMPTY, values[0]);
        }
        return "CLICKED LINK " + values[1]  + " IN MAILING " + values[0];
    }

    @Override
    protected void validate(QueryBuilderRuleNode ruleNode) throws QueryBuilderToEqlConversionException {
        Object[] values = (Object[]) ruleNode.getValue();
        if (values.length != 2 || Arrays.stream(values).map(Object::toString).anyMatch(StringUtils::isBlank)) {
            throw new QueryBuilderToEqlConversionException("Invalid value for node " + ruleNode);
        }
    }
}
