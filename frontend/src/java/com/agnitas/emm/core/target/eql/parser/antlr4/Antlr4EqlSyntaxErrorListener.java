/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.parser.antlr4;

import java.util.BitSet;
import java.util.List;
import java.util.Vector;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import com.agnitas.emm.core.target.eql.parser.EqlSyntaxError;

class Antlr4EqlSyntaxErrorListener implements ANTLRErrorListener {
	
	private final List<EqlSyntaxError> errors;
	
	public Antlr4EqlSyntaxErrorListener() {
		this.errors = new Vector<>();
	}

	public boolean hasErrors() {
		return this.errors.size() > 0;
	}
	
	public List<EqlSyntaxError> getErrors() {
		return this.errors;
	}
	
	@Override
	public void syntaxError(Recognizer<?, ?> parser, Object offendingSymbol, int line, int column, String msg, RecognitionException e) {
		String symbol = null;

		if(e instanceof NoViableAltException) {
			NoViableAltException nvae = (NoViableAltException) e;
			symbol = nvae.getStartToken().getText();
		}
		
		this.errors.add(new EqlSyntaxError(line, column, symbol));
	}
	
	@Override
	public void reportContextSensitivity(Parser parser, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
		// Unused
	}
	
	@Override
	public void reportAttemptingFullContext(Parser parser, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
		// Unused
	}
	
	@Override
	public void reportAmbiguity(Parser parser, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
		// Unused
	}

}
