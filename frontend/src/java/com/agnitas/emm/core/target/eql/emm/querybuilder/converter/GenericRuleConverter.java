/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder.converter;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.agnitas.emm.core.target.eql.codegen.DataType;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldResolveException;
import com.agnitas.emm.core.target.eql.codegen.util.StringUtil;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderHelper;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderOperator;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderRuleNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConversionException;
import com.agnitas.emm.core.target.eql.emm.resolver.EmmProfileFieldResolver;
import com.agnitas.emm.core.target.eql.emm.resolver.EmmProfileFieldResolverFactory;

public abstract class GenericRuleConverter implements RuleConverter {

    private static final Logger logger = LogManager.getLogger(GenericRuleConverter.class);

    private EmmProfileFieldResolverFactory profileFieldResolverFactory;
    
    @Override
    public String convert(QueryBuilderRuleNode ruleNode, int companyId) throws QueryBuilderToEqlConversionException {
        try {
            EmmProfileFieldResolver profileFieldResolver = profileFieldResolverFactory.newInstance(companyId);
            DataType dataType = profileFieldResolver.resolveProfileFieldType(ruleNode.getId());
            QueryBuilderOperator operatorObject = QueryBuilderHelper.operatorOfRule(ruleNode, dataType);
            String operator = QueryBuilderHelper.relationalEqlOperator(operatorObject);
            validate(ruleNode, dataType, operator);
            return convert(ruleNode, dataType, operator);
        } catch (ProfileFieldResolveException e) {
            throw new QueryBuilderToEqlConversionException(e.getMessage(), e.getCause());
        }
    }

    public abstract String convert(QueryBuilderRuleNode node, DataType dataType, String operator) throws QueryBuilderToEqlConversionException;

    /**
     * Derived classes may throw
	 * @throws QueryBuilderToEqlConversionException
	 */
    protected void validate(QueryBuilderRuleNode node, DataType dataType, String operator) throws QueryBuilderToEqlConversionException {
        //Default behaviour does not imply any validation.
    }

    protected String valueOfRule (QueryBuilderRuleNode node, DataType dataType) throws QueryBuilderToEqlConversionException {
        switch (dataType) {
            case NUMERIC:
                String numericValue = QueryBuilderUtil.getRuleNodeValueAsString(node);
                if (NumberUtils.isCreatable(numericValue)) {
                    return numericValue;
                } else {
                	 String message = String.format("Data type '%s' not handled'", dataType);
                     logger.error(message);
                     throw new QueryBuilderToEqlConversionException(message);
                }
		case TEXT:
			return StringUtil.makeEqlStringConstant(node.getValue().toString());
            
            default:
                String message = String.format("Data type '%s' not handled'", dataType);
                logger.error(message);
                throw new QueryBuilderToEqlConversionException(message);
        }
    }

    public void setProfileFieldResolverFactory(EmmProfileFieldResolverFactory profileFieldResolverFactory) {
        this.profileFieldResolverFactory = profileFieldResolverFactory;
    }
}
