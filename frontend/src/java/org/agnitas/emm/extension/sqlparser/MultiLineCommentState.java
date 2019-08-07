/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.sqlparser;

/**
 * Implementation of the ParserState interface.
 * 
 * This state indicates, that the parser is now inside a muli-line comment.
 * 
 * @see ParserState
 */
class MultiLineCommentState implements ParserState {

	/** Successor state when "*" is read. */
	private ParserState possibleMultiLineCommentEndState;

	/**
	 * Set successor state.
	 * 
	 * @param possibleMultiLineCommentEndState PossibleMultiLineCommentEndState 
	 */
	public void setReachableStates( PossibleMultiLineCommentEndState possibleMultiLineCommentEndState) {
		this.possibleMultiLineCommentEndState = possibleMultiLineCommentEndState;
	}
	
	@Override
	public ParserState processChar(int c, StatementBuffer sb) {
		if( c == -1)
			return null;
		else if( c == '*')
			return possibleMultiLineCommentEndState;
		else
			return this;
	}

}
