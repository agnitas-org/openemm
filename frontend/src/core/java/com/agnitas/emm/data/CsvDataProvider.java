/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.data;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agnitas.util.CsvDataException;
import com.agnitas.util.CsvReader;
import org.apache.commons.io.IOUtils;

public class CsvDataProvider extends DataProvider {

	// Default optional parameters
	private final char separator;
	private final Character stringQuote;
	private final char escapeStringQuote;
	private final String nullValueText;
	private final boolean allowUnderfilledLines;
	private final boolean removeSurplusEmptyTrailingColumns;
	private final boolean noHeaders;
	private final boolean trimData = true;
	private CsvReader csvReader = null;
	private List<String> columnNames = null;
	private Long itemsAmount = null;
	private String itemsUnitSign = null;

	private final Charset encoding;

	public CsvDataProvider(File importFile, char[] zipPassword, String encoding, char separator, Character stringQuote, char escapeStringQuote,
						   boolean allowUnderfilledLines, boolean removeSurplusEmptyTrailingColumns, boolean noHeaders, String nullValueText) {
		super(importFile, zipPassword);
		this.encoding = Charset.forName(encoding);
		this.separator = separator;
		this.stringQuote = stringQuote;
		this.escapeStringQuote = escapeStringQuote;
		this.allowUnderfilledLines = allowUnderfilledLines;
		this.removeSurplusEmptyTrailingColumns = removeSurplusEmptyTrailingColumns;
		this.noHeaders = noHeaders;
		this.nullValueText = nullValueText;
	}

	@Override
	public String getConfigurationLogString() {
		return super.getConfigurationLogString()
			+ "Format: CSV" + "\n"
			+ "Encoding: " + encoding + "\n"
			+ "Separator: " + separator + "\n"
			+ "StringQuote: " + stringQuote + "\n"
			+ "EscapeStringQuote: " + escapeStringQuote + "\n"
			+ "AllowUnderfilledLines: " + allowUnderfilledLines + "\n"
			+ "RemoveSurplusEmptyTrailingColumns: " + removeSurplusEmptyTrailingColumns + "\n"
			+ "TrimData: " + trimData + "\n"
			+ "Null value text: " + (nullValueText == null ? "none" : "\"" + nullValueText + "\"") + "\n";
	}

	@Override
	public List<String> getAvailableDataPropertyNames() throws Exception {
		if (columnNames == null) {
			try (CsvReader scanCsvReader = new CsvReader(getInputStream(), encoding, separator, stringQuote)) {
				scanCsvReader.setStringQuoteEscapeCharacter(escapeStringQuote);
				scanCsvReader.setAlwaysTrim(trimData);
				scanCsvReader.setIgnoreEmptyLines(true);
				
				if (noHeaders) {
					final List<String> returnList = new ArrayList<>();
					if (allowUnderfilledLines) {
						// Scan all data for maximum
						List<String> values;
						int maxColumns = 0;
						while ((values = scanCsvReader.readNextCsvLine()) != null) {
							maxColumns = Math.max(maxColumns, values.size());
						}
						for (int i = 0; i < maxColumns; i++) {
							returnList.add(Integer.toString(i + 1));
						}
						columnNames = returnList;
					} else {
						// Only take first data as example for all other data
						final List<String> values = scanCsvReader.readNextCsvLine();
						for (int i = 0; i < values.size(); i++) {
							returnList.add("column_" + (i + 1));
						}
						columnNames = returnList;
					}
				} else {
					// Read headers from file
					columnNames = scanCsvReader.readNextCsvLine();
				}
			}
		}

		return columnNames;
	}

	@Override
	public long getItemsAmountToImport() throws Exception {
		if (itemsAmount == null) {
			if (getImportDataAmount() < 1024 * 1024 * 1024) {
				try (CsvReader scanCsvReader = new CsvReader(getInputStream(), encoding, separator, stringQuote)) {
					scanCsvReader.setStringQuoteEscapeCharacter(escapeStringQuote);
					scanCsvReader.setAlwaysTrim(trimData);
					scanCsvReader.setIgnoreEmptyLines(true);
					
					if (noHeaders) {
						itemsAmount = (long) scanCsvReader.getCsvLineCount();
						itemsUnitSign = null;
					} else {
						itemsAmount = (long) (scanCsvReader.getCsvLineCount() - 1);
						itemsUnitSign = null;
					}
				} catch (CsvDataException e) {
					throw new Exception(e.getMessage(), e);
				}
			} else {
				itemsAmount = getImportDataAmount();
				itemsUnitSign = "B";
			}
		}

		return itemsAmount;
	}

	@Override
	public String getItemsUnitSign() {
		return itemsUnitSign;
	}

	@Override
	public Map<String, Object> getNextItemData() throws Exception {
		if (csvReader == null) {
			openReader();
		}

		final List<String> values = csvReader.readNextCsvLine();
		if (values == null) {
			return null;
		}

		final Map<String, Object> returnMap = new HashMap<>();
		for (int i = 0; i < getAvailableDataPropertyNames().size(); i++) {
			final String columnName = getAvailableDataPropertyNames().get(i);
			if (values.size() > i) {
				if (nullValueText != null && nullValueText.equals(values.get(i))) {
					returnMap.put(columnName, null);
				} else {
					returnMap.put(columnName, values.get(i));
				}
			} else {
				returnMap.put(columnName, null);
			}
		}
		return returnMap;
	}

	@Override
	public void close() {
		IOUtils.closeQuietly(csvReader);
		csvReader = null;
		super.close();
	}

	private void openReader() throws Exception {
		if (csvReader != null) {
			throw new IllegalStateException("Reader was already opened before");
		}

		try {
			csvReader = new CsvReader(getInputStream(), encoding, separator, stringQuote);
			csvReader.setStringQuoteEscapeCharacter(escapeStringQuote);
			csvReader.setAlwaysTrim(trimData);
			csvReader.setIgnoreEmptyLines(true);

			if (!noHeaders) {
				// Skip headers
				csvReader.readNextCsvLine();
			}
		} catch (final Exception e) {
			IOUtils.closeQuietly(csvReader);
			throw e;
		}
	}
}
