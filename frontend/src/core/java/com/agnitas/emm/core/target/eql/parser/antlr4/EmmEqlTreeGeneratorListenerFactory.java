package com.agnitas.emm.core.target.eql.parser.antlr4;

import com.agnitas.emm.core.target.eql.parser.EqlParserConfiguration;

public final class EmmEqlTreeGeneratorListenerFactory implements EqlTreeGeneratorListenerFactory {

	@Override
	public final EqlTreeGeneratorListener newListener(final EqlParserConfiguration configuration) {
		return new OpenEmmAntlr4SyntaxTreeGeneratorEqlListener(configuration);
	}

}
