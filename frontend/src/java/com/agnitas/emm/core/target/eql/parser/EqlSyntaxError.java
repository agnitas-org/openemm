/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.parser;

public class EqlSyntaxError {

	private final int line;
	private final int column;
	private final String symbol;
	
	public EqlSyntaxError(int line, int column, String symbol) {
		this.line = line;
		this.column = column;
		this.symbol = symbol;
	}
	
	public int getLine() {
		return this.line;
	}
	
	public int getColumn() {
		return this.column;
	}
	
	public String getSymbol() {
		return this.symbol;
	}
	
	@Override
	public String toString() {
		return String.format("EqlSyntaxError[line=%d, column=%d, symbol=%s]", line, column, symbol != null ? symbol : "<unknown>");
	}
}
