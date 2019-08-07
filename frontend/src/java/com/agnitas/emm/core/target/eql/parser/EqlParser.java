/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.parser;

import com.agnitas.emm.core.target.eql.ast.BooleanExpressionTargetRuleEqlNode;

/**
 * Interface for EQL parser.
 * 
 * Implementations of this interface must do:
 * <ul>
 *   <li>Parse EQL expressions</li>
 *   <li>Convert parse tree to a common syntax tree</li>
 *   <li>Throw proper EqlParserExceptions on errors during parsing</li>
 * </ul>
 */
public interface EqlParser {

	/**
	 * Parses given EQL expression. Encoding of EQL expression must be UTF-8.
	 * The parser uses default configuration.
	 * 
	 * @param eql EQL expression
	 * 
	 * @return Root node of syntax tree
	 * 
	 * @throws EqlParserException on errors during parsing EQL expression
	 */
	BooleanExpressionTargetRuleEqlNode parseEql(final String eql) throws EqlParserException;
	
	/**
	 * Parses given EQL expression. Encoding of EQL expression must be UTF-8.
	 * The parser uses given configuration.
	 * 
	 * @param eql EQL expression
	 * @param configuration configuration of parser
	 * 
	 * @return Root node of syntax tree
	 * 
	 * @throws EqlParserException on errors during parsing EQL expression
	 */
	BooleanExpressionTargetRuleEqlNode parseEql(final String eql, final EqlParserConfiguration configuration) throws EqlParserException;
	
}
