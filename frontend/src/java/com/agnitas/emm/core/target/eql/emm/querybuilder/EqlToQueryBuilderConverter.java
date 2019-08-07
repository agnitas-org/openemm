/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.target.eql.ast.AbstractBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.BooleanExpressionTargetRuleEqlNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.parser.EqlToQueryBuilderParserConfiguration;
import com.agnitas.emm.core.target.eql.parser.EqlParser;
import com.agnitas.emm.core.target.eql.parser.EqlParserConfiguration;
import com.agnitas.emm.core.target.eql.parser.EqlParserException;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

/**
 * Converts EQL code to QueryBuilder rules in JSON format.
 */
public final class EqlToQueryBuilderConverter {

	private static final transient Logger logger = Logger.getLogger(EqlToQueryBuilderConverter.class);

	private final EqlParser parser;

	private EqlToQueryBuilderParserConfiguration configuration;
	
	private final EqlParserConfiguration parserConfiguration;
 

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
	 * @return QueryBuilder rules as JSON string
	 *
	 * @throws EqlParserException
	 * @throws EqlToQueryBuilderConversionException
	 */
	public final QueryBuilderData convertEqlToQueryBuilderJson(final String eql) throws EqlParserException, EqlToQueryBuilderConversionException {
		try {
			// Get default configuration and enable generation of annotation nodes
			BooleanExpressionTargetRuleEqlNode targetRuleNode = this.parser.parseEql(eql, this.parserConfiguration);
			if(targetRuleNode.getChild().isPresent()) {
				AbstractBooleanEqlNode node = targetRuleNode.getChild().get();
				Set<String> unknownProfileFields = new HashSet<>();
				QueryBuilderGroupNode groupNode = new QueryBuilderGroupNode();
				groupNode = configuration.getParserMapping().get(node.getClass()).parse(node, groupNode, unknownProfileFields);
				if (StringUtils.isEmpty(groupNode.getCondition())) {
					groupNode.setCondition("AND");
				}
				JSON json = JSONSerializer.toJSON(groupNode);
				return new QueryBuilderData(json.toString(), unknownProfileFields);
			} else {
				return new QueryBuilderData("{\"condition\":\"AND\",\"rules\":[]}", Collections.emptySet());
			}
		} catch(final Exception e) {
			if(logger.isInfoEnabled()) {
				logger.info("Error converting EQL to QueryBuilder", e);
			}

			throw e;
		}
	}

	@Required
	public void setConfiguration(EqlToQueryBuilderParserConfiguration configuration) {
		this.configuration = configuration;
	}
}
