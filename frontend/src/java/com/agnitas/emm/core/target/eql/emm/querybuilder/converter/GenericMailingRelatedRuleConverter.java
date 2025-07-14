/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder.converter;

import org.apache.commons.lang3.StringUtils;

import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderRuleNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConversionException;

public abstract class GenericMailingRelatedRuleConverter implements RuleConverter {

    @Override
    public String convert(QueryBuilderRuleNode ruleNode, int companyId) throws QueryBuilderToEqlConversionException {
        validate(ruleNode);
        return negateIfRequired(ruleNode, convertMailingRule(ruleNode, companyId));
    }

    private String negateIfRequired(QueryBuilderRuleNode ruleNode, String rule) {
        return ruleNode.isNegated() ? String.format("NOT (%s)", rule) : rule;
    }

    protected abstract String convertMailingRule(QueryBuilderRuleNode ruleNode, int companyId);

    //Default validation checks if value is present and not blank.
    protected void validate(QueryBuilderRuleNode ruleNode) throws QueryBuilderToEqlConversionException {
        String value = QueryBuilderUtil.getRuleNodeValueAsString(ruleNode);
        if (StringUtils.isBlank(value)) {
            throw new QueryBuilderToEqlConversionException("Value is empty for node " + ruleNode);
        }
    }
}
