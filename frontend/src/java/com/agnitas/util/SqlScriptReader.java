/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;

public class SqlScriptReader extends BasicReader {
	public SqlScriptReader(InputStream inputStream) throws Exception {
		super(inputStream, (String) null);
	}
	
	public SqlScriptReader(InputStream inputStream, String encoding) throws Exception {
		super(inputStream, encoding);
	}
	
	public SqlScriptReader(InputStream inputStream, Charset encodingCharset) throws Exception {
		super(inputStream, encodingCharset);
	}
	
	public String readNextStatement() throws Exception {
		StringBuilder nextStatement = new StringBuilder();
		boolean withinString = false;
		boolean withinSingleLineComment = false;
		boolean withinMultiLineComment = false;
		
		Character nextCharacter;
		while ((nextCharacter = readNextCharacter()) != null) {
			if (withinString) {
				if (nextCharacter == '\'') {
					withinString = false;
				}
				nextStatement.append(nextCharacter);
			} else if (withinSingleLineComment) {
				if (nextCharacter == '\n' || nextCharacter == '\r') {
					withinSingleLineComment = false;
				}
			} else if (withinMultiLineComment) {
				if (nextCharacter == '*') {
					nextCharacter = readNextCharacter();
					if (nextCharacter == '/') {
						withinMultiLineComment = false;
					} else {
						reuseCurrentChar();
					}
				}
			} else if (nextCharacter == '\'') {
				withinString = true;
				nextStatement.append(nextCharacter);
			} else if (nextCharacter == '-') {
				nextCharacter = readNextCharacter();
				if (nextCharacter == '-') {
					withinSingleLineComment = true;
				} else {
					reuseCurrentChar();
					nextStatement.append('-');
				}
			} else if (nextCharacter == '/') {
				nextCharacter = readNextCharacter();
				if (nextCharacter == '*') {
					withinMultiLineComment = true;
				} else {
					reuseCurrentChar();
					nextStatement.append('/');
				}
			} else if (nextCharacter == ';') {
				break;
			} else {
				nextStatement.append(nextCharacter);
			}
		}
		
		if (withinString) {
			throw new Exception("Unclosed sql string");
		} else if (withinMultiLineComment) {
			throw new Exception("Unclosed multiline comment");
		} else if ("/".equals(nextStatement.toString().trim())) {
			// Skip empty Oracle statement: "/" is a code sign for finalizing Oracle SQL scripts
			return readNextStatement();
		} else if (StringUtils.isNotBlank(nextStatement.toString())) {
			return nextStatement.toString().trim();
		} else if (nextCharacter == null) {
			return null;
		} else {
			// Skip empty statement
			return readNextStatement();
		}
	}
}
