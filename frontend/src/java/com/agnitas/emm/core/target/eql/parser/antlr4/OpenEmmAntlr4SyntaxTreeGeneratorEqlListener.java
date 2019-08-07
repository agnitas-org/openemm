package com.agnitas.emm.core.target.eql.parser.antlr4;

import com.agnitas.emm.core.target.eql.ast.ClickedInMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.OpenedMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.devicequery.AbstractDeviceQueryNode;
import com.agnitas.emm.core.target.eql.parser.EqlParserConfiguration;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.ClickedInMailingRelationalExpressionContext;
import com.agnitas.emm.core.target.eql.parser.antlr4.autogen.EqlGrammarParser.OpenedMailingRelationalExpressionContext;

/**
 * Intermediary class required to make {@link AbstractAntlr4SyntaxTreeGeneratorEqlListener} 
 * non-abstract.
 */
public class OpenEmmAntlr4SyntaxTreeGeneratorEqlListener extends AbstractAntlr4SyntaxTreeGeneratorEqlListener {

	public OpenEmmAntlr4SyntaxTreeGeneratorEqlListener(EqlParserConfiguration configuration) {
		super(configuration);
	}
	@Override
	public final void exitClickedInMailingRelationalExpression(final ClickedInMailingRelationalExpressionContext ctx) {
		assert nodeStack.size() == 0;
		
		final AbstractDeviceQueryNode deviceQueryOrNull = null;
		
		if(ctx.LINK() == null) { // "LINK" token not present -> clicked arbitrary link in mailing
			// Due to grammar, this is guaranteed to succeed.
			final int mailingId = Integer.parseInt(ctx.NUMERIC_ID().get(0).getText());
			
			nodeStack.push(new ClickedInMailingRelationalEqlNode(mailingId, deviceQueryOrNull, Antlr4BasedCodeLocation.fromParserRuleContext(ctx)));
		} else {
			// Due to grammar, this is guaranteed to succeed.
			final int linkId = Integer.parseInt(ctx.NUMERIC_ID().get(0).getText());
			final int mailingId = Integer.parseInt(ctx.NUMERIC_ID().get(1).getText());
			
			nodeStack.push(new ClickedInMailingRelationalEqlNode(mailingId, linkId, deviceQueryOrNull, Antlr4BasedCodeLocation.fromParserRuleContext(ctx)));
		}
	}
	
	
	@Override
	public final void exitOpenedMailingRelationalExpression(final OpenedMailingRelationalExpressionContext ctx) {
		// Due to grammar, this is guaranteed to succeed.
		final int mailingId = Integer.parseInt(ctx.NUMERIC_ID().getText());
		
		nodeStack.push(new OpenedMailingRelationalEqlNode(mailingId, Antlr4BasedCodeLocation.fromParserRuleContext(ctx)));
	}

}
