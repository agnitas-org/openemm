/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.agnitas.util.CsvDataException;
import org.agnitas.util.CsvReader;
import org.agnitas.util.CsvWriter;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.Tuple;
import org.agnitas.util.ZipUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class CsvDataProvider extends DataProvider {
	// Default optional parameters
	private char separator = ';';
	private Character stringQuote = '"';
	private char escapeStringQuote = '"';
	private String nullValueText = null;
	private boolean allowUnderfilledLines = false;
	private boolean removeSurplusEmptyTrailingColumns = false;
	private boolean noHeaders = false;
	private boolean trimData = true;
	private boolean useExtendedCheck;

	private CsvReader csvReader = null;
	private List<String> columnNames = null;
	private Map<String, DbColumnType> dataTypes = null;
	private Long itemsAmount = null;
	private String itemsUnitSign = null;

	private Charset encoding = StandardCharsets.UTF_8;

	public CsvDataProvider(final File importFile, final char[] zipPassword, String encoding, final char separator, final Character stringQuote, final char escapeStringQuote, final boolean allowUnderfilledLines, final boolean removeSurplusEmptyTrailingColumns, final boolean noHeaders, final String nullValueText) {
		this(importFile, zipPassword, encoding, separator, stringQuote, escapeStringQuote, allowUnderfilledLines, removeSurplusEmptyTrailingColumns, noHeaders, nullValueText, false);
	}

	public CsvDataProvider(final File importFile, final char[] zipPassword, String encoding, final char separator, final Character stringQuote, final char escapeStringQuote, final boolean allowUnderfilledLines, final boolean removeSurplusEmptyTrailingColumns, final boolean noHeaders, final String nullValueText, boolean useExtendedCheck) {
		super(importFile, zipPassword);
		this.encoding = Charset.forName(encoding);
		this.separator = separator;
		this.stringQuote = stringQuote;
		this.escapeStringQuote = escapeStringQuote;
		this.allowUnderfilledLines = allowUnderfilledLines;
		this.removeSurplusEmptyTrailingColumns = removeSurplusEmptyTrailingColumns;
		this.noHeaders = noHeaders;
		this.nullValueText = nullValueText;
		this.useExtendedCheck = useExtendedCheck;
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
	public Map<String, DbColumnType> scanDataPropertyTypes(final Map<String, Tuple<String, String>> mapping) throws Exception {
		if (dataTypes == null) {
			try (CsvReader scanCsvReader = new CsvReader(getInputStream(), encoding, separator, stringQuote, useExtendedCheck)) {
				scanCsvReader.setStringQuoteEscapeCharacter(escapeStringQuote);
				scanCsvReader.setAlwaysTrim(trimData);
				scanCsvReader.setIgnoreEmptyLines(true);
				
				if (!noHeaders) {
					// Read headers from file
					columnNames = scanCsvReader.readNextCsvLine();
				}

				dataTypes = new HashMap<>();

				// Scan all data for maximum
				List<String> values;
				while ((values = scanCsvReader.readNextCsvLine()) != null) {
					for (int i = 0; i < values.size(); i++) {
						final String columnName = (columnNames == null || columnNames.size() <= i ? "column_" + Integer.toString(i + 1) : columnNames.get(i));
						final String currentValue = values.get(i);
						detectNextDataType(mapping, dataTypes, columnName, currentValue);
					}
				}
			} catch (final Exception e) {
				throw e;
			}
		}

		return dataTypes;
	}

	@Override
	public List<String> getAvailableDataPropertyNames() throws Exception {
		if (columnNames == null) {
			try (CsvReader scanCsvReader = new CsvReader(getInputStream(), encoding, separator, stringQuote, useExtendedCheck)) {
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
							returnList.add("column_" + Integer.toString(i + 1));
						}
						columnNames = returnList;
					}
				} else {
					// Read headers from file
					columnNames = scanCsvReader.readNextCsvLine();
				}
			} catch (final Exception e) {
				throw e;
			}
		}

		return columnNames;
	}

	@Override
	public long getItemsAmountToImport() throws Exception {
		if (itemsAmount == null) {
			if (getImportDataAmount() < 1024 * 1024 * 1024) {
				try (CsvReader scanCsvReader = new CsvReader(getInputStream(), encoding, separator, stringQuote, useExtendedCheck)) {
					scanCsvReader.setStringQuoteEscapeCharacter(escapeStringQuote);
					scanCsvReader.setAlwaysTrim(trimData);
					scanCsvReader.setIgnoreEmptyLines(true);
					
					if (noHeaders) {
						itemsAmount = Long.valueOf(scanCsvReader.getCsvLineCount());
						itemsUnitSign = null;
					} else {
						itemsAmount = Long.valueOf(scanCsvReader.getCsvLineCount() - 1);
						itemsUnitSign = null;
					}
				} catch (final CsvDataException e) {
					throw new Exception(e.getMessage(), e);
				} catch (final Exception e) {
					throw e;
				}
			} else {
				itemsAmount = getImportDataAmount();
				itemsUnitSign = "B";
			}
		}

		return itemsAmount;
	}

	@Override
	public String getItemsUnitSign() throws Exception {
		return itemsUnitSign;
	}

	@Override
	public Map<String, Object> getNextItemData() throws Exception {
		if (csvReader == null) {
			openReader();
		}

		final List<String> values = csvReader.readNextCsvLine();
		if (values != null) {
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
		} else {
			return null;
		}
	}

	@Override
	public void close() {
		IOUtils.closeQuietly(csvReader);
		csvReader = null;
		super.close();
	}

	@Override
	public File filterDataItems(final List<Integer> indexList, final String fileSuffix) throws Exception {
		OutputStream outputStream = null;
		try {
			openReader();

			File filteredDataFile;
			if (StringUtils.endsWithIgnoreCase(getImportFilePath(), ".zip")) {
				filteredDataFile = new File(getImportFilePath() + "." + fileSuffix + ".csv.zip");
				outputStream = ZipUtilities.openNewZipOutputStream(filteredDataFile);
				((ZipOutputStream) outputStream).putNextEntry(new ZipEntry(new File(getImportFilePath() + "." + fileSuffix + ".csv").getName()));
			} else {
				filteredDataFile = new File(getImportFilePath() + "." + fileSuffix + ".csv");
				outputStream = new FileOutputStream(filteredDataFile);
			}

			try (CsvWriter csvWriter = new CsvWriter(outputStream, encoding.toString(), separator, stringQuote)) {
				csvWriter.setStringQuoteEscapeCharacter(escapeStringQuote);

				csvWriter.writeValues(columnNames);

				Map<String, Object> item;
				int itemIndex = 0;
				while ((item = getNextItemData()) != null) {
					itemIndex++;
					if (indexList.contains(itemIndex)) {
						final List<String> values = new ArrayList<>();
						for (final String columnName : columnNames) {
							if (item.get(columnName) == null) {
								values.add(nullValueText);
							} else if (item.get(columnName) instanceof String) {
								values.add((String) item.get(columnName));
							} else if (item.get(columnName) instanceof Date) {
								values.add(DateUtilities.formatDate(DateUtilities.YYYY_MM_DD_HH_MM_SS, (Date) item.get(columnName)));
							} else if (item.get(columnName) instanceof Number) {
								values.add(item.get(columnName).toString());
							} else {
								values.add(item.get(columnName).toString());
							}
						}
						csvWriter.writeValues(values);
					}
				}

				return filteredDataFile;
			}
		} finally {
			close();
			IOUtils.closeQuietly(outputStream);
		}
	}

	private void openReader() throws Exception {
		if (csvReader != null) {
			throw new Exception("Reader was already opened before");
		}

		try {
			csvReader = new CsvReader(getInputStream(), encoding, separator, stringQuote, useExtendedCheck);
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
