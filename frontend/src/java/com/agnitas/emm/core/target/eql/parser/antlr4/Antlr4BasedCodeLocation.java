/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.parser.antlr4;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import com.agnitas.emm.core.target.eql.codegen.CodeLocation;

/**
 * Implementation of {@link CodeLocation} reading location information from ANTLRs {@link ParserRuleContext}.
 */
public class Antlr4BasedCodeLocation extends CodeLocation {

	/**
	 * Create new code location.
	 * 
	 * @param line line number
	 * @param column column number
	 */
	private Antlr4BasedCodeLocation(final int line, final int column) {
		super(line, column);
	}
	
	/**
	 * Create code location from given {@link ParserRuleContext}.
	 * 
	 * @param ctxt ParserRuleContext providing location information
	 * 
	 * @return CodeLocation from {@link ParserRuleContext}
	 */
	public static Antlr4BasedCodeLocation fromParserRuleContext(ParserRuleContext ctxt) {
		Token start = ctxt.getStart();
		
		return new Antlr4BasedCodeLocation(start.getLine(), start.getCharPositionInLine());
	}

}
