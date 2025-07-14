/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder;

import java.util.Map;

import com.agnitas.emm.core.target.eql.emm.querybuilder.converter.RuleConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class QueryBuilderToEqlConverter {

	private static final Logger logger = LogManager.getLogger(QueryBuilderToEqlConverter.class);

	private QueryBuilderConfiguration queryBuilderConfiguration;
	private final ObjectMapper objectMapper;

	public QueryBuilderToEqlConverter() {
		objectMapper = new ObjectMapper();
		configureObjectMapper();
	}

	private void configureObjectMapper() {
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		SimpleModule module = new SimpleModule();
		module.addDeserializer(QueryBuilderBaseNode.class, new QueryBuilderNodeDeserializer());
		objectMapper.registerModule(module);
	}

	public String convertQueryBuilderJsonToEql(String queryBuilderRules, int companyID) throws QueryBuilderToEqlConversionException {
        try {
            QueryBuilderBaseNode node = objectMapper.readValue(queryBuilderRules, QueryBuilderBaseNode.class);
			if (node instanceof QueryBuilderGroupNode groupNode) {
				return convertGroupNodeToEql(groupNode, companyID);
			}

			if (node instanceof QueryBuilderRuleNode ruleNode) {
				return convertRuleNodeToEql(ruleNode, companyID);
			}
        } catch (JsonProcessingException e) {
			logger.error("Error occurred when convert QB JSON to EQL. JSON: %n%s".formatted(queryBuilderRules), e);
        }

		return "";
	}
	
	private String convertGroupNodeToEql(QueryBuilderGroupNode groupNode, int companyID) throws QueryBuilderToEqlConversionException {
		StringBuilder buffer = new StringBuilder();
		QueryBuilderCondition condition = QueryBuilderHelper.conditionOfGroup(groupNode);
		String eqlOperator = QueryBuilderHelper.booleanEqlOperator(condition);
		
		boolean first = true;
		for (QueryBuilderBaseNode subNode : groupNode.getRules()) {
			if (!first) {
				buffer.append(" ").append(eqlOperator).append(" ");
			}
			if (subNode instanceof QueryBuilderRuleNode ruleNode) {
				buffer.append(convertRuleNodeToEql(ruleNode, companyID));
			} else if (subNode instanceof QueryBuilderGroupNode subGroupNode) {
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

	private String convertRuleNodeToEql(QueryBuilderRuleNode ruleNode, int companyID) throws QueryBuilderToEqlConversionException {
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

	public void setQueryBuilderConfiguration(QueryBuilderConfiguration queryBuilderConfiguration) {
		this.queryBuilderConfiguration = queryBuilderConfiguration;
	}
}
