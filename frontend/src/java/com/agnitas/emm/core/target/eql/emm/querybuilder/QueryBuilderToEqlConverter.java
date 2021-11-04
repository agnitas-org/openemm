/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.target.eql.emm.querybuilder.converter.RuleConverter;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;

public final class QueryBuilderToEqlConverter {

	private static final transient Logger logger = Logger.getLogger(QueryBuilderToEqlConverter.class);
	
	/** Configuration for JSON parser to deal with data from QueryBuilder. */
	private static final JsonConfig JSON_CONFIG = new DefaultQueryBuilderJsonConfig();

	private QueryBuilderConfiguration queryBuilderConfiguration;
	
	public final String convertQueryBuilderJsonToEql(String queryBuilderRules, int companyID) throws QueryBuilderToEqlConversionException {
		//final JSONObject jsonObject = JSONObject.fromObject(queryBuilderRules);
		final JSON jsonObject = JSONSerializer.toJSON(queryBuilderRules, JSON_CONFIG);
		
		final Object resultNode = JSONSerializer.toJava(jsonObject, JSON_CONFIG);
		if(resultNode instanceof QueryBuilderGroupNode) { 
			final QueryBuilderGroupNode groupNode = (QueryBuilderGroupNode) resultNode;
			return convertGroupNodeToEql(groupNode, companyID);
		} else if(resultNode instanceof QueryBuilderRuleNode) {
			final QueryBuilderRuleNode ruleNode = (QueryBuilderRuleNode) resultNode;
			return convertRuleNodeToEql(ruleNode, companyID);
		} else {
			return "";
		}
	}
	
	private String convertGroupNodeToEql(final QueryBuilderGroupNode groupNode, int companyID) throws QueryBuilderToEqlConversionException {
		StringBuilder buffer = new StringBuilder();
		QueryBuilderCondition condition = QueryBuilderHelper.conditionOfGroup(groupNode);
		String eqlOperator = QueryBuilderHelper.booleanEqlOperator(condition);
		
		boolean first = true;
		for (QueryBuilderBaseNode subNode : groupNode.getRules()) {
			if (!first) {
				buffer.append(" ").append(eqlOperator).append(" ");
			}
			if (subNode instanceof QueryBuilderRuleNode) {
				buffer.append(convertRuleNodeToEql((QueryBuilderRuleNode) subNode, companyID));
			} else if (subNode instanceof QueryBuilderGroupNode) {
				QueryBuilderGroupNode subGroupNode = (QueryBuilderGroupNode) subNode;
				String subGroup = convertGroupNodeToEql(subGroupNode, companyID);
				
				// TODO: Optimize parenthesis by evaluation of operators of groupNode and subGroupNode
				buffer.append("(").append(subGroup).append(")");
			} else {
				String msg = String.format("Unhandled QueryBuilder node type '%s'", subNode.getClass().getCanonicalName());
				logger.error(msg);
				throw new QueryBuilderToEqlConversionException(msg);
			}
				
			first = false;
		}
		
		return buffer.toString();
	}

	private String convertRuleNodeToEql(final QueryBuilderRuleNode ruleNode, int companyID) throws QueryBuilderToEqlConversionException {
		Map<String, RuleConverter> filterConvertersByName = queryBuilderConfiguration.getFilterConvertersByName();
		Map<String, RuleConverter> filterConvertersByOperator = queryBuilderConfiguration.getFilterConvertersByOperator();
		Map<String, RuleConverter> filterConvertersByType = queryBuilderConfiguration.getFilterConvertersByType();
		RuleConverter defaultRuleConverter = queryBuilderConfiguration.getDefaultRuleConverter(),
				targetRuleConverter = filterConvertersByName.get(ruleNode.getId());
		targetRuleConverter = targetRuleConverter == null ? filterConvertersByOperator.get(ruleNode.getOperator()) : targetRuleConverter;
		targetRuleConverter = targetRuleConverter == null ? filterConvertersByType.getOrDefault(ruleNode.getType(), defaultRuleConverter) : targetRuleConverter;
		return targetRuleConverter.convert(ruleNode, companyID);
	}

	// ---------------------------------------------------------------------------------------------------------------------------------- Dependency Injection

	@Required
	public void setQueryBuilderConfiguration(QueryBuilderConfiguration queryBuilderConfiguration) {
		this.queryBuilderConfiguration = queryBuilderConfiguration;
	}
}
