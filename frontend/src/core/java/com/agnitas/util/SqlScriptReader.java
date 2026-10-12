/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class SqlScriptReader extends BasicReader {

	private static final Set<String> PLSQL_BLOCK_STARTER_WORDS = Set.of("DECLARE", "BEGIN");
	private static final Set<String> PLSQL_CREATE_OBJECT_TYPES = Set.of("PROCEDURE", "FUNCTION", "PACKAGE", "TRIGGER", "TYPE");

	/** Characters pushed back by {@link #peekWord()}/{@link #peekWords(int)} to be re-read in original order. */
	private final Deque<Character> pending = new ArrayDeque<>();

	/**
	 * Current statement terminator. Defaults to ";" and can be changed by a "DELIMITER &lt;token&gt;"
	 */
	private String delimiter = ";";

	public SqlScriptReader(InputStream inputStream) {
		super(inputStream, (String) null);
	}

	public SqlScriptReader(InputStream inputStream, String encoding) {
		super(inputStream, encoding);
	}

	public SqlScriptReader(InputStream inputStream, Charset encodingCharset) {
		super(inputStream, encodingCharset);
	}

	public String readNextStatement() throws Exception {
		skipLeadingWhitespaceAndComments();

		if ("DELIMITER".equalsIgnoreCase(peekWord())) {
			String newDelimiter = readDelimiterCommandValue();
			if (StringUtils.isNotBlank(newDelimiter)) {
				delimiter = newDelimiter;
			}
			return readNextStatement();
		}

		if (";".equals(delimiter) && isPlSqlBlockStart()) {
			String block = readPlSqlBlock();
			return StringUtils.isBlank(block) ? readNextStatement() : block;
		}

		StringBuilder nextStatement = new StringBuilder();
		boolean withinString = false;
		boolean withinSingleLineComment = false;
		boolean withinMultiLineComment = false;

		Character nextCharacter;
		while ((nextCharacter = nextChar()) != null) {
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
					nextCharacter = nextChar();
					if (nextCharacter == '/') {
						withinMultiLineComment = false;
					} else {
						pushBack(nextCharacter);
					}
				}
			} else if (nextCharacter == '\'') {
				withinString = true;
				nextStatement.append(nextCharacter);
			} else if (nextCharacter == '-') {
				nextCharacter = nextChar();
				if (nextCharacter == '-') {
					withinSingleLineComment = true;
				} else {
					pushBack(nextCharacter);
					nextStatement.append('-');
				}
			} else if (nextCharacter == '/') {
				nextCharacter = nextChar();
				if (nextCharacter == '*') {
					withinMultiLineComment = true;
				} else {
					pushBack(nextCharacter);
					nextStatement.append('/');
				}
			} else if (nextCharacter == '$' && ";".equals(delimiter) && tryReadDollarQuoteTag() instanceof String tag) {
				nextStatement.append('$').append(tag).append('$');
				appendUntilClosingDollarQuote(nextStatement, tag);
			} else {
				nextStatement.append(nextCharacter);
				if (endsWithDelimiter(nextStatement)) {
					nextStatement.setLength(nextStatement.length() - delimiter.length());
					break;
				}
			}
		}

		if (withinString) {
			throw new IllegalStateException("Unclosed sql string");
		} else if (withinMultiLineComment) {
			throw new IllegalStateException("Unclosed multiline comment");
		} else if ("/".equals(nextStatement.toString().trim())) {
			// Skip stray Oracle SQL*Plus script-termination sign "/" that is not part of a detected PL/SQL block
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

	private String tryReadDollarQuoteTag() throws IOException {
		List<Character> consumed = new ArrayList<>();
		StringBuilder tag = new StringBuilder();

		Character c = nextChar();
		while (c != null && (Character.isLetterOrDigit(c) || c == '_')) {
			consumed.add(c);
			tag.append(c);
			c = nextChar();
		}

		if (c != null && c == '$') {
			return tag.toString();
		}

		if (c != null) {
			consumed.add(c);
		}
		for (int i = consumed.size() - 1; i >= 0; i--) {
			pushBack(consumed.get(i));
		}
		return null;
	}

	private void appendUntilClosingDollarQuote(StringBuilder target, String tag) throws IOException {
		String closingTag = "$" + tag + "$";
		StringBuilder window = new StringBuilder();

		Character c;
		while ((c = nextChar()) != null) {
			target.append(c);
			window.append(c);
			if (window.length() > closingTag.length()) {
				window.deleteCharAt(0);
			}
			if (window.length() == closingTag.length() && window.toString().equals(closingTag)) {
				return;
			}
		}

		throw new IllegalStateException("Unclosed dollar-quoted string ($" + tag + "$)");
	}

	private void skipLeadingWhitespaceAndComments() throws IOException {
		while (true) {
			Character c = nextChar();
			if (c == null) {
				return;
			}

			if (Character.isWhitespace(c)) {
				continue;
			}

			if (c == '-') {
				Character c2 = nextChar();
				if (c2 != null && c2 == '-') {
					Character c3;
					do {
						c3 = nextChar();
					} while (c3 != null && c3 != '\n' && c3 != '\r');
					continue;
				}
				pushBack(c2);
				pushBack(c);
				return;
			}

			if (c == '/') {
				Character c2 = nextChar();
				if (c2 != null && c2 == '*') {
					Character c3 = nextChar();
					while (c3 != null) {
						if (c3 == '*') {
							Character c4 = nextChar();
							if (c4 != null && c4 == '/') {
								break;
							}
							pushBack(c4);
						}
						c3 = nextChar();
					}
					continue;
				}
				pushBack(c2);
				pushBack(c);
				return;
			}

			pushBack(c);
			return;
		}
	}

	private boolean endsWithDelimiter(StringBuilder statement) {
		int delimiterLength = delimiter.length();
		return statement.length() >= delimiterLength
				&& statement.substring(statement.length() - delimiterLength).equals(delimiter);
	}

	/**
	 * Reads the rest of a "DELIMITER &lt;token&gt;" line and returns the new delimiter token.
	 * Only called once {@link #peekWord()} has confirmed the line starts with "DELIMITER".
	 */
	private String readDelimiterCommandValue() throws IOException {
		Character c = nextChar();
		while (c != null && Character.isWhitespace(c)) {
			c = nextChar();
		}
		while (c != null && Character.isLetter(c)) {
			c = nextChar();
		}

		StringBuilder rest = new StringBuilder();
		while (c != null && c != '\n' && c != '\r') {
			rest.append(c);
			c = nextChar();
		}
		return rest.toString().trim();
	}

	/**
	 * Detects an Oracle anonymous PL/SQL block ("DECLARE"/"BEGIN") or a named PL/SQL object
	 * ("CREATE [OR REPLACE] PROCEDURE|FUNCTION|PACKAGE|TRIGGER|TYPE ...") at the current read
	 * position, without consuming any input.
	 */
	private boolean isPlSqlBlockStart() throws IOException {
		List<String> words = peekWords(4);
		if (words.isEmpty()) {
			return false;
		}

		String first = words.getFirst();
		if (PLSQL_BLOCK_STARTER_WORDS.contains(first)) {
			return true;
		}
		if (!"CREATE".equals(first)) {
			return false;
		}

		int index = 1;
		if (index < words.size() && "OR".equals(words.get(index))) {
			index++;
			if (index < words.size() && "REPLACE".equals(words.get(index))) {
				index++;
			}
		}
		return index < words.size() && PLSQL_CREATE_OBJECT_TYPES.contains(words.get(index));
	}

	private String readPlSqlBlock() throws IOException {
		StringBuilder block = new StringBuilder();
		String line;
		boolean firstLine = true;
		while ((line = readLine()) != null) {
			if ("/".equals(line.trim())) {
				break;
			}
			if (!firstLine) {
				block.append('\n');
			}
			block.append(line);
			firstLine = false;
		}
		return block.toString().trim();
	}

	private String readLine() throws IOException {
		Character c = nextChar();
		if (c == null) {
			return null;
		}

		StringBuilder line = new StringBuilder();
		while (c != null && c != '\n' && c != '\r') {
			line.append(c);
			c = nextChar();
		}
		if (c != null && c == '\r') {
			Character next = nextChar();
			if (next != null && next != '\n') {
				pushBack(next);
			}
		}
		return line.toString();
	}

	private String peekWord() throws IOException {
		List<String> words = peekWords(1);
		return words.isEmpty() ? "" : words.getFirst();
	}

	private List<String> peekWords(int maxWords) throws IOException {
		List<Character> consumed = new ArrayList<>();
		List<String> words = new ArrayList<>();

		for (int i = 0; i < maxWords; i++) {
			Character c = nextChar();
			while (c != null && Character.isWhitespace(c)) {
				consumed.add(c);
				c = nextChar();
			}

			StringBuilder word = new StringBuilder();
			while (c != null && Character.isLetter(c)) {
				consumed.add(c);
				word.append(c);
				c = nextChar();
			}
			if (c != null) {
				consumed.add(c);
			}

			if (word.isEmpty()) {
				break;
			}
			words.add(word.toString().toUpperCase());

			if (c == null) {
				break;
			}
		}

		for (int i = consumed.size() - 1; i >= 0; i--) {
			pushBack(consumed.get(i));
		}

		return words;
	}

	private Character nextChar() throws IOException {
		return pending.isEmpty() ? readNextCharacter() : pending.pollFirst();
	}

	private void pushBack(Character character) {
		if (character != null) {
			pending.addFirst(character);
		}
	}

}
