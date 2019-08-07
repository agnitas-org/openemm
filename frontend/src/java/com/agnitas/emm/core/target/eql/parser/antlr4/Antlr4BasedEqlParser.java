/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.parser.antlr4;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Objects;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.log4j.Logger;

import com.agnitas.emm.core.target.eql.ast.BooleanExpressionTargetRuleEqlNode;
import com.agnitas.emm.core.target.eql.parser.EqlParser;
import com.agnitas.emm.core.target.eql.parser.EqlParserConfiguration;
import com.agnitas.emm.core.target.eql.parser.EqlParserException;
import com.agnitas.emm.core.target.eql.parser.EqlSyntaxError;
import com.agnitas.emm.core.target.eql.parser.EqlSyntaxErrorException;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarLexer;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser;

/**
 * EQL parser based on ANTLR 4.
 * 
 * This parser takes the parse tree from the ANTLR4 based parser and returns a normalized syntax tree used
 * by code generators.
 */
public class Antlr4BasedEqlParser implements EqlParser {

	private final EqlParserConfiguration defaultConfiguration;
	
	/** The logger. */
	private static final Logger logger = Logger.getLogger(Antlr4BasedEqlParser.class);

	private final EqlTreeGeneratorListenerFactory treeGeneratorFactory;
	
	public Antlr4BasedEqlParser(final EqlTreeGeneratorListenerFactory treeGeneratorFactory) {
		this.defaultConfiguration = new EqlParserConfiguration().setCreateBooleanAnnotationNodes(false);
		this.treeGeneratorFactory = Objects.requireNonNull(treeGeneratorFactory, "Tree generator factory is null");
	}
	
	@Override
	public final BooleanExpressionTargetRuleEqlNode parseEql(final String eql) throws EqlParserException {
		return parseEql(eql, this.defaultConfiguration);
	}
	
	@Override
	public final BooleanExpressionTargetRuleEqlNode parseEql(final String eql, final EqlParserConfiguration configuration) throws EqlParserException {
		if(logger.isInfoEnabled()) {
			logger.info("Parsing EQL statment: \n" + eql + "\n--- END OF CODE ---");
		}
	
		try {
			try(final Reader reader = new StringReader(eql)) {
				final ANTLRInputStream charInput = new ANTLRInputStream(reader);
				final EqlGrammarLexer lexer = new EqlGrammarLexer(charInput);
				final CommonTokenStream tokens = new CommonTokenStream(lexer);
				final EqlGrammarParser parser = new EqlGrammarParser(tokens);
				
				final Antlr4EqlSyntaxErrorListener syntaxErrorListener = new Antlr4EqlSyntaxErrorListener();
				parser.addErrorListener(syntaxErrorListener);
				
				final ParseTree parseTree = parser.target_expression();
				
				if(!syntaxErrorListener.hasErrors()) {
					final ParseTreeWalker walker = new ParseTreeWalker();
					final EqlTreeGeneratorListener listener = this.treeGeneratorFactory.newListener(configuration);
					walker.walk(listener, parseTree);
					
					return listener.getSyntaxTreeRootNode();
				} else {
					if(logger.isInfoEnabled()) {
						logger.info("Found " + syntaxErrorListener.getErrors().size() + " errors in EQL");
						
						for(EqlSyntaxError error : syntaxErrorListener.getErrors()) {
							logger.info("  - at line " + error.getLine() + ", column " + error.getColumn() + ": " + error.getSymbol());
						}
					}
					throw new EqlSyntaxErrorException(syntaxErrorListener.getErrors());
				}
			}						 
		} catch(final IOException e) {
			throw new EqlParserException("Error reading EQL statement", e);
		}
	}

}
