/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.agnitas.emm.core.target.eql.ast.AbstractBooleanEqlNode;
import com.agnitas.emm.core.target.eql.codegen.DataType;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldResolveException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.parser.EqlNodeParser;
import com.agnitas.emm.core.target.eql.emm.querybuilder.parser.EqlToQueryBuilderParserConfiguration;
import com.agnitas.emm.core.target.eql.emm.resolver.EmmProfileFieldResolver;
import com.agnitas.emm.core.target.eql.emm.resolver.EmmProfileFieldResolverFactory;
import com.agnitas.emm.core.target.eql.parser.EqlParser;
import com.agnitas.emm.core.target.eql.parser.EqlParserConfiguration;
import com.agnitas.emm.core.target.eql.parser.EqlParserException;
import net.sf.json.JSONSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

/**
 * Converts EQL code to QueryBuilder rules in JSON format.
 */
public final class EqlToQueryBuilderConverter {

	private static final transient Logger logger = Logger.getLogger(EqlToQueryBuilderConverter.class);

	private final EqlParser parser;

	private EqlToQueryBuilderParserConfiguration configuration;

	private final EqlParserConfiguration parserConfiguration;

	private EmmProfileFieldResolverFactory emmProfileFieldResolverFactory;

	/**
	 * Creates a new converter instance.
	 */
	public EqlToQueryBuilderConverter(final EqlParser parser) {
		this.parser = Objects.requireNonNull(parser, "EqlParser is null");
		
		this.parserConfiguration = new EqlParserConfiguration().setCreateBooleanAnnotationNodes(true);
	}

	/**
	 * Converts given EQL code to QueryBuilder rules in JSON format.
	 *
	 * @param eql EQL code to be converted
	 *
	 * @return rules as JSON string
	 *
	 * @throws EqlParserException
	 * @throws EqlToQueryBuilderConversionException
	 */
	public final String convertEqlToQueryBuilderJson(final String eql, int companyId) throws EqlParserException, EqlToQueryBuilderConversionException {
		try {
			// Get default configuration and enable generation of annotation nodes
			Optional<AbstractBooleanEqlNode> child = parser.parseEql(eql, parserConfiguration).getChild();

			if (child.isPresent()) {
				AbstractBooleanEqlNode node = child.get();

				EqlNodeParser<?> nodeParser = configuration.getParserMapping().get(node.getClass());
				Set<String> profileFields = new HashSet<>();
				QueryBuilderGroupNode groupNode = nodeParser.parse(node, new QueryBuilderGroupNode(), profileFields);

				if (StringUtils.isEmpty(groupNode.getCondition())) {
					groupNode.setCondition("AND");
				}

				if (profileFields.size() > 0) {
					validateProfileFields(groupNode, profileFields, emmProfileFieldResolverFactory.newInstance(companyId));
				}

				return JSONSerializer.toJSON(groupNode).toString();
			} else {
				return "{\"condition\":\"AND\",\"rules\":[]}";
			}
		} catch (final ProfileFieldResolveException e) {
			if (logger.isInfoEnabled()) {
				logger.info("Error converting EQL to QueryBuilder", e);
			}

			throw new EqlToQueryBuilderConversionException(e);
		} catch (final Exception e) {
			if (logger.isInfoEnabled()) {
				logger.info("Error converting EQL to QueryBuilder", e);
			}

			throw e;
		}
	}

	private void validateProfileFields(QueryBuilderGroupNode group, Set<String> profileFields, EmmProfileFieldResolver resolver) throws ProfileFieldResolveException, EqlToQueryBuilderConversionException {
		for (QueryBuilderBaseNode node : group.getRules()) {
			if (node instanceof QueryBuilderGroupNode) {
				validateProfileFields((QueryBuilderGroupNode) node, profileFields, resolver);
			} else if (node instanceof QueryBuilderRuleNode) {
				validateProfileFields((QueryBuilderRuleNode) node, profileFields, resolver);
			} else {
				throw new RuntimeException("Unsupported QB node type: " + node.getClass());
			}
		}
	}

	private void validateProfileFields(QueryBuilderRuleNode rule, Set<String> profileFields, EmmProfileFieldResolver resolver) throws ProfileFieldResolveException, EqlToQueryBuilderConversionException {
		if (profileFields.contains(rule.getId())) {
			DataType type = resolver.resolveProfileFieldType(rule.getId());
			QueryBuilderOperator operator = QueryBuilderOperator.findByQueryBuilderName(rule.getOperator());

			if (operator != null && !isOperatorApplicable(type, operator)) {
				throw new EqlToQueryBuilderConversionException("The `" + operator.queryBuilderName() + "` operator is not applicable to profile field of type `" + type + "`");
			}
		}
	}

	private boolean isOperatorApplicable(DataType type, QueryBuilderOperator operator) {
		switch (type) {
			case NUMERIC:
				return operator.isNumericOperator();

			case TEXT:
				return operator.isTextOperator();

			case DATE:
				return operator.isDateOperator();

			default:
				// Rest of the types are not expected here.
				return false;
		}
	}

	@Required
	public void setConfiguration(EqlToQueryBuilderParserConfiguration configuration) {
		this.configuration = configuration;
	}

	@Required
	public void setEmmProfileFieldResolverFactory(EmmProfileFieldResolverFactory emmProfileFieldResolverFactory) {
		this.emmProfileFieldResolverFactory = emmProfileFieldResolverFactory;
	}
}
