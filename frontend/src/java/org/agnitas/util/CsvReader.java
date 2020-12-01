/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * The Class CsvReader.
 */
public class CsvReader extends BasicReader {
	/** The Constant DEFAULT_SEPARATOR. */
	public static final char DEFAULT_SEPARATOR = ',';

	/** The Constant DEFAULT_STRING_QUOTE. */
	public static final char DEFAULT_STRING_QUOTE = '"';

	/** Mandatory separating charactor. */
	private char separator;

	/** Character for stringquotes: if set to null, no quoting will be done. */
	private char stringQuote;

	/**
	 * Character to escape the stringquote character within quoted strings. By default this is the stringquote character itself, so it is doubled in quoted string, but may also be a backslash '\'.
	 */
	private char stringQuoteEscapeCharacter;

	/** Since stringQuote is a simple char this activates or deactivates the quoting. */
	private boolean useStringQuote;

	/** Allow linebreaks in data texts without the effect of a new data set line. */
	private boolean lineBreakInDataAllowed = true;

	/** Allow double stringquotes to use it as a character in data text. */
	private boolean escapedStringQuoteInDataAllowed = true;

	/** If a single read was done, it is impossible to make a full read at once with readAll(). */
	private boolean singleReadStarted = false;

	/** Number of columns expected (set by first read line). */
	private int numberOfColumns = -1;

	/** Number of lines read until now. */
	private int readCsvLines = 0;

	/** Allow lines with less than the expected number of data entries per line. */
	private boolean fillMissingTrailingColumnsWithNull = false;
	
	/** Trim all data values */
	private boolean alwaysTrim = false;
	
	/** Ignore empty lines */
	private boolean ignoreEmptyLines = false;

	/**
	 * CSV Reader derived constructor.
	 *
	 * @param inputStream
	 *            the input stream
	 */
	public CsvReader(InputStream inputStream) throws Exception {
		this(inputStream, Charset.forName(DEFAULT_ENCODING), DEFAULT_SEPARATOR, DEFAULT_STRING_QUOTE);
	}

	/**
	 * CSV Reader derived constructor.
	 *
	 * @param inputStream
	 *            the input stream
	 * @param encoding
	 *            the encoding
	 */
	public CsvReader(InputStream inputStream, String encoding) throws Exception {
		this(inputStream, Charset.forName(encoding), DEFAULT_SEPARATOR, DEFAULT_STRING_QUOTE);
	}

	/**
	 * CSV Reader derived constructor.
	 *
	 * @param inputStream
	 *            the input stream
	 * @param encoding
	 *            the encoding
	 */
	public CsvReader(InputStream inputStream, Charset encoding) throws Exception {
		this(inputStream, encoding, DEFAULT_SEPARATOR, DEFAULT_STRING_QUOTE);
	}

	/**
	 * CSV Reader derived constructor.
	 *
	 * @param inputStream
	 *            the input stream
	 * @param separator
	 *            the separator
	 */
	public CsvReader(InputStream inputStream, char separator) throws Exception {
		this(inputStream, Charset.forName(DEFAULT_ENCODING), separator, DEFAULT_STRING_QUOTE);
	}

	/**
	 * CSV Reader derived constructor.
	 *
	 * @param inputStream
	 *            the input stream
	 * @param encoding
	 *            the encoding
	 * @param separator
	 *            the separator
	 */
	public CsvReader(InputStream inputStream, String encoding, char separator) throws Exception {
		this(inputStream, Charset.forName(encoding), separator, DEFAULT_STRING_QUOTE);
	}

	/**
	 * CSV Reader derived constructor.
	 *
	 * @param inputStream
	 *            the input stream
	 * @param encoding
	 *            the encoding
	 * @param separator
	 *            the separator
	 */
	public CsvReader(InputStream inputStream, Charset encoding, char separator) throws Exception {
		this(inputStream, encoding, separator, DEFAULT_STRING_QUOTE);
	}

	/**
	 * CSV Reader derived constructor.
	 *
	 * @param inputStream
	 *            the input stream
	 * @param separator
	 *            the separator
	 * @param stringQuote
	 *            the string quote
	 */
	public CsvReader(InputStream inputStream, char separator, Character stringQuote) throws Exception {
		this(inputStream, Charset.forName(DEFAULT_ENCODING), separator, stringQuote);
	}

	/**
	 * CSV Reader derived constructor.
	 *
	 * @param inputStream
	 *            the input stream
	 * @param encoding
	 *            the encoding
	 * @param separator
	 *            the separator
	 * @param stringQuote
	 *            the string quote
	 */
	public CsvReader(InputStream inputStream, String encoding, char separator, Character stringQuote) throws Exception {
		this(inputStream, Charset.forName(encoding), separator, stringQuote);
	}

	/**
	 * CSV Reader main constructor.
	 *
	 * @param inputStream
	 *            the input stream
	 * @param encoding
	 *            the encoding
	 * @param separator
	 *            the separator
	 * @param stringQuote
	 *            the string quote
	 * @throws Exception
	 */
	public CsvReader(InputStream inputStream, Charset encoding, char separator, Character stringQuote) throws Exception {
		super(inputStream, encoding);
		
		this.separator = separator;
		if (stringQuote != null) {
			this.stringQuote = stringQuote;
			stringQuoteEscapeCharacter = stringQuote;
			useStringQuote = true;
		} else {
			useStringQuote = false;
		}

		if (anyCharsAreEqual(this.separator, '\r', '\n')) {
			throw new IllegalArgumentException("Separator '" + this.separator + "' is invalid");
		} else if (useStringQuote && anyCharsAreEqual(this.separator, this.stringQuote, '\r', '\n')) {
			throw new IllegalArgumentException("Stringquote '" + this.stringQuote + "' is invalid");
		}
	}

	/**
	 * Getter for property fillMissingTrailingColumnsWithNull.
	 *
	 * @return true, if is fill missing trailing columns with null
	 */
	public boolean isFillMissingTrailingColumnsWithNull() {
		return fillMissingTrailingColumnsWithNull;
	}

	/**
	 * Setter for property fillMissingTrailingColumnsWithNull.
	 *
	 * @param fillMissingTrailingColumnsWithNull
	 *            the new fill missing trailing columns with null
	 */
	public void setFillMissingTrailingColumnsWithNull(boolean fillMissingTrailingColumnsWithNull) {
		this.fillMissingTrailingColumnsWithNull = fillMissingTrailingColumnsWithNull;
	}

	/**
	 * Getter for property alwaysTrim.
	 *
	 * @return true, if is alwaysTrim
	 */
	public boolean isAlwaysTrim() {
		return alwaysTrim;
	}

	/**
	 * Setter for property alwaysTrim.
	 *
	 * @param alwaysTrim
	 *            trim all values
	 */
	public void setAlwaysTrim(boolean alwaysTrim) {
		this.alwaysTrim = alwaysTrim;
	}

	/**
	 * Getter for property ingoreEmptyLines.
	 *
	 * @return true, if is ingoreEmptyLines
	 */
	public boolean isIgnoreEmptyLines() {
		return ignoreEmptyLines;
	}

	/**
	 * Setter for property ingoreEmptyLines.
	 *
	 * @param ignoreEmptyLines
	 *            ignore empty lines
	 */
	public void setIgnoreEmptyLines(boolean ignoreEmptyLines) {
		this.ignoreEmptyLines = ignoreEmptyLines;
	}

	/**
	 * Setter for property stringQuoteEscapeCharacter. Character to escape the stringquote character within quoted strings. By default this is the stringquote character itself, so it is doubled in
	 * quoted string, but may also be a backslash '\'.
	 *
	 * @param stringQuoteEscapeCharacter
	 *            the new fill missing trailing columns with null
	 */
	public void setStringQuoteEscapeCharacter(char stringQuoteEscapeCharacter) {
		this.stringQuoteEscapeCharacter = stringQuoteEscapeCharacter;
		if (useStringQuote && anyCharsAreEqual(separator, '\r', '\n', stringQuoteEscapeCharacter)) {
			throw new IllegalArgumentException("Stringquote escape character '" + this.stringQuoteEscapeCharacter + "' is invalid");
		}
	}

	/**
	 * Get lines read until now.
	 *
	 * @return the read lines
	 */
	public int getReadCsvLines() {
		return readCsvLines;
	}

	/**
	 * Getter for property lineBreakInDataAllowed.
	 *
	 * @return true, if is line break in data allowed
	 */
	public boolean isLineBreakInDataAllowed() {
		return lineBreakInDataAllowed;
	}

	/**
	 * Setter for property lineBreakInDataAllowed.
	 *
	 * @param lineBreakInDataAllowed
	 *            the new line break in data allowed
	 */
	public void setLineBreakInDataAllowed(boolean lineBreakInDataAllowed) {
		this.lineBreakInDataAllowed = lineBreakInDataAllowed;
	}

	/**
	 * Getter for property escapedStringQuoteInDataAllowed.
	 *
	 * @return true, if is escaped string quote in data allowed
	 */
	public boolean isEscapedStringQuoteInDataAllowed() {
		return escapedStringQuoteInDataAllowed;
	}

	/**
	 * Setter for property escapedStringQuoteInDataAllowed.
	 *
	 * @param escapedStringQuoteInDataAllowed
	 *            the new escaped string quote in data allowed
	 */
	public void setEscapedStringQuoteInDataAllowed(boolean escapedStringQuoteInDataAllowed) {
		this.escapedStringQuoteInDataAllowed = escapedStringQuoteInDataAllowed;
	}

	/**
	 * Read the next line of csv data.
	 *
	 * @return the list
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws CsvDataException
	 *             the csv data exception
	 */
	public List<String> readNextCsvLine() throws IOException, CsvDataException {
		readCsvLines++;
		singleReadStarted = true;
		List<String> returnList = new ArrayList<>();
		StringBuilder nextValue = new StringBuilder();
		boolean insideString = false;
		boolean isQuotedString = false;
		Character nextCharacter;
		char previousCharacter = (char) -1;
		
		while ((nextCharacter = readNextCharacter()) != null) {
			char nextChar = nextCharacter;
			if (useStringQuote && nextChar == stringQuote) {
				if (stringQuoteEscapeCharacter != stringQuote) {
					if (previousCharacter != stringQuoteEscapeCharacter) {
						insideString = !insideString;
					}
				} else {
					insideString = !insideString;
				}
				nextValue.append(nextChar);
				isQuotedString = true;
			} else if (!insideString) {
				if (nextChar == '\r' || nextChar == '\n') {
					if (nextValue.length() > 0 || previousCharacter == separator) {
						returnList.add(parseValue(nextValue.toString()));
						nextValue = new StringBuilder();
						isQuotedString = false;
					}
					
					if (ignoreEmptyLines && isBlank(returnList)) {
						returnList = new ArrayList<>();
					} else if (returnList.size() > 0) {
						if (numberOfColumns == -1) {
							numberOfColumns = returnList.size();
							return returnList;
						} else if (numberOfColumns == returnList.size()) {
							return returnList;
						} else if (numberOfColumns > returnList.size() && fillMissingTrailingColumnsWithNull) {
							while (returnList.size() < numberOfColumns) {
								returnList.add(null);
							}
							return returnList;
						} else {
							// Too many values found, so check if the trailing values are only empty items
							while (returnList.size() > numberOfColumns) {
								String lastItem = returnList.remove(returnList.size() - 1);
								if (!"".equals(lastItem)) {
									throw new CsvDataInvalidItemCountException("Inconsistent number of values (expected: " + numberOfColumns + " actually: " + returnList.size() + ")", readCsvLines, numberOfColumns, returnList.size());
								}
							}
							return returnList;
						}
					}
				} else if (nextChar == separator) {
					returnList.add(parseValue(nextValue.toString()));
					nextValue = new StringBuilder();
					isQuotedString = false;
				} else if (isQuotedString) {
					if (!Character.isWhitespace(nextChar)) {
						throw new CsvDataInvalidTextAfterQuoteException("Not allowed textdata after quoted text in data", readCsvLines);
					}
				} else {
					nextValue.append(nextChar);
				}
			} else { // insideString
				if ((nextChar == '\r' || nextChar == '\n') && !lineBreakInDataAllowed) {
					throw new CsvDataException("Not allowed linebreak in data", readCsvLines);
				} else {
					nextValue.append(nextChar);
				}
			}

			previousCharacter = nextCharacter;
		}

		if (insideString) {
			close();
			throw new CsvDataException("Unexpected end of data after quoted csv-value was started", readCsvLines);
		} else {
			if (nextValue.length() > 0 || previousCharacter == separator) {
				returnList.add(parseValue(nextValue.toString()));
			}
			
			if (ignoreEmptyLines && isBlank(returnList)) {
				return null;
			} else if (returnList.size() > 0) {
				if (numberOfColumns == -1) {
					numberOfColumns = returnList.size();
					return returnList;
				} else if (numberOfColumns == returnList.size()) {
					return returnList;
				} else if (numberOfColumns > returnList.size() && fillMissingTrailingColumnsWithNull) {
					while (returnList.size() < numberOfColumns) {
						returnList.add(null);
					}
					return returnList;
				} else {
					throw new CsvDataInvalidItemCountException("Inconsistent number of values (expected: " + numberOfColumns + " actually: " + returnList.size() + ")", readCsvLines, numberOfColumns, returnList.size());
				}
			} else {
				close();
				return null;
			}
		}
	}

	private boolean isBlank(List<String> list) {
		if (list != null) {
			for (String item : list) {
				if (StringUtils.isNotBlank(item)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Read all csv data at once. This can only be done before readNextCsvLine() was called for the first time
	 *
	 * @return the list
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws CsvDataException
	 *             the csv data exception
	 */
	public List<List<String>> readAll() throws IOException, CsvDataException {
		if (singleReadStarted) {
			throw new IllegalStateException("Single readNextCsvLine was called before readAll");
		}

		try {
			List<List<String>> csvValues = new ArrayList<>();
			List<String> lineValues;
			while ((lineValues = readNextCsvLine()) != null) {
				csvValues.add(lineValues);
			}
			return csvValues;
		} finally {
			close();
		}
	}

	/**
	 * Parse a single value to applicate allowed double stringquotes.
	 *
	 * @param rawValue
	 *            the raw value
	 * @return the string
	 * @throws CsvDataException
	 *             the csv data exception
	 */
	private String parseValue(String rawValue) throws CsvDataException {
		String returnValue = rawValue;

		if (isNotEmpty(returnValue)) {
			if (useStringQuote) {
				String stringQuoteString = Character.toString(stringQuote);
				if (returnValue.contains(stringQuoteString)) {
					returnValue = returnValue.trim();
				}
				if (returnValue.charAt(0) == stringQuote && returnValue.charAt(returnValue.length() - 1) == stringQuote) {
					returnValue = returnValue.substring(1, returnValue.length() - 1);
					returnValue = returnValue.replace(stringQuoteEscapeCharacter + stringQuoteString, stringQuoteString);
				}
			}
			returnValue = returnValue.replace("\r\n", "\n").replace('\r', '\n');

			if (!escapedStringQuoteInDataAllowed && returnValue.indexOf(stringQuote) >= 0) {
				throw new CsvDataException("Not allowed stringquote in data", readCsvLines);
			}
			
			if (alwaysTrim) {
				returnValue = returnValue.trim();
			}
		}

		return returnValue;
	}

	/**
	 * This method reads the stream to the end and counts all csv value lines, which can be less than the absolute linebreak count of the stream for the reason of quoted linebreaks. The result also
	 * contains the first line, which may consist of columnheaders.
	 *
	 * @return the csv line count
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws CsvDataException
	 *             the csv data exception
	 */
	public int getCsvLineCount() throws IOException, CsvDataException {
		if (singleReadStarted) {
			throw new IllegalStateException("Single readNextCsvLine was called before getCsvLineCount");
		}

		try {
			int csvLineCount = 0;
			while (readNextCsvLine() != null) {
				csvLineCount++;
			}
			return csvLineCount;
		} finally {
			close();
		}
	}

	/**
	 * Parse a single csv data line for data entries.
	 *
	 * @param separator
	 *            the separator
	 * @param stringQuote
	 *            the string quote
	 * @param csvLine
	 *            the csv line
	 * @return the list
	 * @throws Exception
	 *             the exception
	 */
	public static List<String> parseCsvLine(char separator, Character stringQuote, String csvLine) throws Exception {
		CsvReader reader = null;
		try {
			reader = new CsvReader(new ByteArrayInputStream(csvLine.getBytes("UTF-8")), "UTF-8", separator, stringQuote);
			List<List<String>> fullData = reader.readAll();
			if (fullData.size() != 1) {
				throw new Exception("Too many csv lines in data");
			} else {
				return fullData.get(0);
			}
		} catch (CsvDataException e) {
			throw e;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * Parse a single csv data line for data entries.
	 *
	 * @param separator
	 *            the separator
	 * @param stringQuote
	 *            the string quote
	 * @param csvLine
	 *            the csv line
	 * @return the list
	 * @throws Exception
	 *             the exception
	 */
	public static List<String> parseCsvLine(char separator, Character stringQuote, Character escapeStringQuote, String csvLine) throws Exception {
		CsvReader reader = null;
		try {
			reader = new CsvReader(new ByteArrayInputStream(csvLine.getBytes("UTF-8")), "UTF-8", separator, stringQuote);
			reader.setStringQuoteEscapeCharacter(escapeStringQuote);
			List<List<String>> fullData = reader.readAll();
			if (fullData.size() != 1) {
				throw new Exception("Too many csv lines in data");
			} else {
				return fullData.get(0);
			}
		} catch (CsvDataException e) {
			throw e;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * Check if any characters in a list are equal.
	 *
	 * @param values
	 *            the values
	 * @return true, if successful
	 */
	private static boolean anyCharsAreEqual(char... values) {
		for (int i = 0; i < values.length; i++) {
			for (int j = i + 1; j < values.length; j++) {
				if (values[i] == values[j]) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the first duplicate csv file header or null if there is no duplicate.
	 * Leading and trailing whitespaces in csv file headers are omitted.
	 * Csv file headers are case-sensitive.
	 * 
	 * @param csvFileHeaders
	 * @return
	 */
	public static String checkForDuplicateCsvHeader(List<String> csvFileHeaders, boolean caseInsensitive) {
		Set<String> foundHeaders = new HashSet<>();
		for (String nextHeader : csvFileHeaders) {
			if (nextHeader != null) {
				nextHeader = nextHeader.trim();
				if (caseInsensitive) {
					nextHeader = nextHeader.toLowerCase();
				}
				if (nextHeader.length() > 0) {
					if (foundHeaders.contains(nextHeader)) {
						return nextHeader;
					} else {
						foundHeaders.add(nextHeader);
					}
				}
			}
		}
		return null;
	}
}
