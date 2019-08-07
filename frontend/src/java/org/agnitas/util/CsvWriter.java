/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * The Class CsvWriter.
 */
public class CsvWriter implements Closeable {

	/** Default output encoding. */
	public static final String DEFAULT_ENCODING = "UTF-8";

	/** Default output separator. */
	public static final char DEFAULT_SEPARATOR = ',';

	/** Default output stringquote. */
	public static final char DEFAULT_STRING_QUOTE = '"';

	/** Default output linebreak. */
	public static final String DEFAULT_LINEBREAK = "\n";

	/** Current output separator. */
	private char separator;

	/** Current output separator as string. */
	private String separatorString;

	/** Current output string quote. */
	private char stringQuote;

	/**
	 * Character to escape the stringquote character within quoted strings. By default this is the stringquote character itself, so it is doubled in quoted string, but may also be a backslash '\'.
	 */
	private char stringQuoteEscapeCharacter;

	/** Current output string quote as string for internal use. */
	private String stringQuoteString;

	/** Current output string quote two times for internal use. */
	private String escapedStringQuoteString;

	/** Currently use the string quote. */
	private boolean useStringQuote;

	/** Current output linebreak. */
	private String lineBreak;

	/** Output stream. */
	private OutputStream outputStream;

	/** Output encoding. */
	private Charset encoding;

	/** Always quote all data entries. */
	private boolean alwaysQuote = false;

	/** Always quote texts or only quote them when they contain linebreaks or the separator character. */
	private boolean quoteAllStrings = false;

	/** Lines written until now. */
	private int writtenLines = 0;

	/** Number of columns to write, set by first line written. */
	private int numberOfColumns = -1;

	/** Output writer. */
	private BufferedWriter outputWriter = null;

	/** Minimum sizes of columns for beautification */
	private int[] minimumColumnSizes = null;

	/** Padding locations of columns for beautification (true = right padding = left aligned) */
	private boolean[] columnPaddings = null;

	/**
	 * CSV Writer derived constructor.
	 *
	 * @param outputStream
	 *            the output stream
	 */
	public CsvWriter(OutputStream outputStream) {
		this(outputStream, Charset.forName(DEFAULT_ENCODING), DEFAULT_SEPARATOR, DEFAULT_STRING_QUOTE, DEFAULT_LINEBREAK);
	}

	/**
	 * CSV Writer derived constructor.
	 *
	 * @param outputStream
	 *            the output stream
	 * @param encoding
	 *            the encoding
	 */
	public CsvWriter(OutputStream outputStream, String encoding) {
		this(outputStream, Charset.forName(encoding), DEFAULT_SEPARATOR, DEFAULT_STRING_QUOTE, DEFAULT_LINEBREAK);
	}

	/**
	 * CSV Writer derived constructor.
	 *
	 * @param outputStream
	 *            the output stream
	 * @param encoding
	 *            the encoding
	 */
	public CsvWriter(OutputStream outputStream, Charset encoding) {
		this(outputStream, encoding, DEFAULT_SEPARATOR, DEFAULT_STRING_QUOTE, DEFAULT_LINEBREAK);
	}

	/**
	 * CSV Writer derived constructor.
	 *
	 * @param outputStream
	 *            the output stream
	 * @param separator
	 *            the separator
	 */
	public CsvWriter(OutputStream outputStream, char separator) {
		this(outputStream, Charset.forName(DEFAULT_ENCODING), separator, DEFAULT_STRING_QUOTE, DEFAULT_LINEBREAK);
	}

	/**
	 * CSV Writer derived constructor.
	 *
	 * @param outputStream
	 *            the output stream
	 * @param encoding
	 *            the encoding
	 * @param separator
	 *            the separator
	 */
	public CsvWriter(OutputStream outputStream, String encoding, char separator) {
		this(outputStream, Charset.forName(encoding), separator, DEFAULT_STRING_QUOTE, DEFAULT_LINEBREAK);
	}

	/**
	 * CSV Writer derived constructor.
	 *
	 * @param outputStream
	 *            the output stream
	 * @param encoding
	 *            the encoding
	 * @param separator
	 *            the separator
	 */
	public CsvWriter(OutputStream outputStream, Charset encoding, char separator) {
		this(outputStream, encoding, separator, DEFAULT_STRING_QUOTE, DEFAULT_LINEBREAK);
	}

	/**
	 * CSV Writer derived constructor.
	 *
	 * @param outputStream
	 *            the output stream
	 * @param separator
	 *            the separator
	 * @param stringQuote
	 *            the string quote
	 */
	public CsvWriter(OutputStream outputStream, char separator, Character stringQuote) {
		this(outputStream, Charset.forName(DEFAULT_ENCODING), separator, stringQuote, DEFAULT_LINEBREAK);
	}

	/**
	 * CSV Writer derived constructor.
	 *
	 * @param outputStream
	 *            the output stream
	 * @param separator
	 *            the separator
	 * @param stringQuote
	 *            the string quote
	 * @param lineBreak
	 *            the line break
	 */
	public CsvWriter(OutputStream outputStream, char separator, Character stringQuote, String lineBreak) {
		this(outputStream, Charset.forName(DEFAULT_ENCODING), separator, stringQuote, lineBreak);
	}

	/**
	 * CSV Writer derived constructor.
	 *
	 * @param outputStream
	 *            the output stream
	 * @param encoding
	 *            the encoding
	 * @param separator
	 *            the separator
	 * @param stringQuote
	 *            the string quote
	 */
	public CsvWriter(OutputStream outputStream, String encoding, char separator, Character stringQuote) {
		this(outputStream, isBlank(encoding) ? Charset.forName(DEFAULT_ENCODING) : Charset.forName(encoding), separator, stringQuote == null ? DEFAULT_STRING_QUOTE : stringQuote, DEFAULT_LINEBREAK);
	}

	/**
	 * CSV Writer main constructor.
	 *
	 * @param outputStream
	 *            the output stream
	 * @param encoding
	 *            the encoding
	 * @param separator
	 *            the separator
	 * @param stringQuote
	 *            the string quote
	 * @param lineBreak
	 *            the line break
	 */
	public CsvWriter(OutputStream outputStream, Charset encoding, char separator, Character stringQuote, String lineBreak) {
		this.outputStream = outputStream;
		this.encoding = encoding;
		this.separator = separator;
		separatorString = Character.toString(separator);
		this.lineBreak = lineBreak;
		if (stringQuote != null) {
			this.stringQuote = stringQuote;
			stringQuoteEscapeCharacter = stringQuote;
			stringQuoteString = Character.toString(stringQuote);
			escapedStringQuoteString = stringQuoteEscapeCharacter + stringQuoteString;
			useStringQuote = true;
		} else {
			useStringQuote = false;
		}

		if (this.encoding == null) {
			throw new IllegalArgumentException("Encoding is null");
		} else if (this.outputStream == null) {
			throw new IllegalArgumentException("OutputStream is null");
		} else if (anyCharsAreEqual(this.separator, '\r', '\n')) {
			throw new IllegalArgumentException("Separator '" + this.separator + "' is invalid");
		} else if (useStringQuote && anyCharsAreEqual(this.separator, this.stringQuote, '\r', '\n')) {
			throw new IllegalArgumentException("Stringquote '" + this.stringQuote + "' is invalid");
		} else if (!this.lineBreak.equals("\r") && !this.lineBreak.equals("\n") && !this.lineBreak.equals("\r\n")) {
			throw new IllegalArgumentException("Given linebreak is invalid");
		}
	}

	/**
	 * Getter for property alwaysQuote.
	 *
	 * @return true, if is always quote
	 */
	public boolean isAlwaysQuote() {
		return alwaysQuote;
	}

	/**
	 * Setter for property alwaysQuote.
	 *
	 * @param alwaysQuote
	 *            the new always quote
	 */
	public void setAlwaysQuote(boolean alwaysQuote) {
		this.alwaysQuote = alwaysQuote;
		if (alwaysQuote) {
			quoteAllStrings = false;
		}
	}

	/**
	 * Getter for property quoteAllStrings.
	 *
	 * @return true, if is quote all strings
	 */
	public boolean isQuoteAllStrings() {
		return quoteAllStrings;
	}

	/**
	 * Setter for property quoteAllStrings.
	 *
	 * @param quoteAllStrings
	 *            the new quote all strings
	 */
	public void setQuoteAllStrings(boolean quoteAllStrings) {
		this.quoteAllStrings = quoteAllStrings;
		if (quoteAllStrings) {
			alwaysQuote = false;
		}
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
		if (useStringQuote && anyCharsAreEqual(separator, stringQuote, '\r', '\n', stringQuoteEscapeCharacter)) {
			throw new IllegalArgumentException("Stringquote escape character '" + this.stringQuoteEscapeCharacter + "' is invalid");
		}
		escapedStringQuoteString = stringQuoteEscapeCharacter + stringQuoteString;
	}

	/**
	 * Write a single line of data entries.
	 *
	 * @param values
	 *            the values
	 * @throws Exception
	 *             the exception
	 */
	public void writeValues(Object... values) throws Exception {
		writeValues(Arrays.asList(values));
	}

	/**
	 * Write a single line of data entries.
	 *
	 * @param values
	 *            the values
	 * @throws CsvDataException
	 *             the csv data exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void writeValues(List<? extends Object> values) throws CsvDataException, IOException {
		if (numberOfColumns != -1 && (values == null || numberOfColumns != values.size())) {
			throw new CsvDataInvalidItemCountException("Inconsistent number of values (expected: " + numberOfColumns + " was: " + (values == null ? "null" : values.size()) + ")", writtenLines, numberOfColumns, values == null ? 0 : values.size());
		}

		if (outputWriter == null) {
			if (outputStream == null) {
				throw new IllegalStateException("CsvWriter is already closed");
			}
			outputWriter = new BufferedWriter(new OutputStreamWriter(outputStream, encoding));
		}

		for (int i = 0; i < values.size(); i++) {
			if (i > 0) {
				outputWriter.write(separator);
			}

			String escapedValue = escapeValue(values.get(i));

			if (minimumColumnSizes != null && minimumColumnSizes.length > i) {
				if (columnPaddings != null && columnPaddings.length > i && columnPaddings[i]) {
					escapedValue = rightPad(escapedValue, minimumColumnSizes[i]);
				} else {
					escapedValue = leftPad(escapedValue, minimumColumnSizes[i]);
				}
			}

			outputWriter.write(escapedValue);
		}
		outputWriter.write(lineBreak);

		writtenLines++;
		numberOfColumns = values.size();
	}

	/**
	 * Write a full set of lines of data entries.
	 *
	 * @param valueLines
	 *            the value lines
	 * @throws Exception
	 *             the exception
	 */
	public void writeAll(List<List<? extends Object>> valueLines) throws Exception {
		for (List<? extends Object> valuesOfLine : valueLines) {
			writeValues(valuesOfLine);
		}
	}

	/**
	 * Escape a single data entry using stringquotes as configured.
	 *
	 * @param value
	 *            the value
	 * @return the string
	 * @throws CsvDataException
	 *             the csv data exception
	 */
	private String escapeValue(Object value) throws CsvDataException {
		String valueString = "";
		if (value != null) {
			valueString = value.toString();
		}

		if (alwaysQuote || (quoteAllStrings && value instanceof String) || valueString.contains(separatorString) || valueString.contains("\r") || valueString.contains("\n")
				|| (useStringQuote && valueString.contains(stringQuoteString))) {
			if (!useStringQuote) {
				throw new CsvDataException("StringQuote was deactivated but is needed for csv-value", writtenLines);
			} else {
				StringBuilder escapedValue = new StringBuilder();
				escapedValue.append(stringQuote);
				escapedValue.append(valueString.replace(stringQuoteString, escapedStringQuoteString));
				escapedValue.append(stringQuote);
				return escapedValue.toString();
			}
		} else {
			return valueString;
		}
	}

	/**
	 * Calculate column value output sizes for beautification of csv output.
	 *
	 * @param values
	 *            the values
	 * @throws CsvDataException
	 *             the csv data exception
	 */
	public int[] calculateOutputSizesOfValues(List<? extends Object> values) throws CsvDataException {
		int[] returnArray = new int[values.size()];
		for (int i = 0; i < values.size(); i++) {
			returnArray[i] = escapeValue(values.get(i)).length();
		}
		return returnArray;
	}

	/**
	 * Calculate column value output size for beautification of csv output.
	 *
	 * @param value
	 *            the value
	 * @throws CsvDataException
	 *             the csv data exception
	 */
	public int calculateOutputSizesOfValue(Object value) throws CsvDataException {
		return escapeValue(value).length();
	}

	/**
	 * Close this writer and its underlying stream.
	 */
	@Override
	public void close() {
		closeQuietly(outputWriter);
		outputWriter = null;
		closeQuietly(outputStream);
		outputStream = null;
	}

	/**
	 * Get number of lines written until now.
	 *
	 * @return the written lines
	 */
	public int getWrittenLines() {
		return writtenLines;
	}

	/**
	 * Flush buffered data.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void flush() throws IOException {
		if (outputWriter != null) {
			outputWriter.flush();
		}
	}

	/**
	 * Create a single csv line.
	 *
	 * @param separator
	 *            the separator
	 * @param stringQuote
	 *            the string quote
	 * @param values
	 *            the values
	 * @return the csv line
	 */
	public static String getCsvLine(char separator, Character stringQuote, List<? extends Object> values) {
		return getCsvLine(separator, stringQuote, values.toArray());
	}

	/**
	 * Create a single csv line.
	 *
	 * @param separator
	 *            the separator
	 * @param stringQuote
	 *            the string quote
	 * @param values
	 *            the values
	 * @return the csv line
	 */
	public static String getCsvLine(char separator, Character stringQuote, Object... values) {
		StringBuilder returnValue = new StringBuilder();
		String separatorString = Character.toString(separator);
		String stringQuoteString = stringQuote == null ? "" : Character.toString(stringQuote);
		String doubleStringQuoteString = stringQuoteString + stringQuoteString;
		if (values != null) {
			for (Object value : values) {
				if (returnValue.length() > 0) {
					returnValue.append(separator);
				}
				if (value != null) {
					String valueString = value.toString();
					if (valueString.contains(separatorString) || valueString.contains("\r") || valueString.contains("\n") || valueString.contains(stringQuoteString)) {
						returnValue.append(stringQuoteString);
						returnValue.append(valueString.replace(stringQuoteString, doubleStringQuoteString));
						returnValue.append(stringQuoteString);
					} else {
						returnValue.append(valueString);
					}
				}
			}
		}
		return returnValue.toString();
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
	 * Check if String value is null or contains only whitespace characters.
	 *
	 * @param value
	 *            the value
	 * @return true, if is blank
	 */
	private static boolean isBlank(String value) {
		return value == null || value.trim().length() == 0;
	}

	/**
	 * Close a Closable item and ignore any Exception thrown by its close method.
	 *
	 * @param closeableItem
	 *            the closeable item
	 */
	private static void closeQuietly(Closeable closeableItem) {
		if (closeableItem != null) {
			try {
				closeableItem.close();
			} catch (IOException e) {
				// Do nothing
			}
		}
	}

	/**
	 * Set minimumColumnSizes for beautification
	 *
	 * @param minimumColumnSizes
	 */
	public void setMinimumColumnSizes(int[] minimumColumnSizes) {
		this.minimumColumnSizes = minimumColumnSizes;
	}

	/**
	 * Set columnPaddings for beautification (true = right padding = left aligned)
	 *
	 * @param columnPaddings
	 */
	public void setColumnPaddings(boolean[] columnPaddings) {
		this.columnPaddings = columnPaddings;
	}

	/**
	 * Append blanks at the left of a string to make if fit the given minimum
	 *
	 * @param escapedValue
	 * @param i
	 * @return
	 */
	private String leftPad(String value, int minimumLength) {
		try {
			return String.format("%1$" + minimumLength + "s", value);
		} catch (Exception e) {
			return value;
		}
	}

	/**
	 * Append blanks at the right of a string to make if fit the given minimum
	 *
	 * @param escapedValue
	 * @param i
	 * @return
	 */
	private String rightPad(String value, int minimumLength) {
		try {
			return String.format("%1$-" + minimumLength + "s", value);
		} catch (Exception e) {
			return value;
		}
	}
}
