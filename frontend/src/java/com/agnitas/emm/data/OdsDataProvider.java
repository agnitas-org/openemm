/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.data;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import com.agnitas.util.DateUtilities;
import org.apache.commons.lang3.StringUtils;

public class OdsDataProvider extends DataProvider {

	private final String sheetname;
	private InputStream contentXmlZipInputStream = null;
	private XMLStreamReader xmlReader = null;

	private final String nullValueText;
	private final boolean allowUnderfilledLines;
	private final boolean noHeaders;
	private final boolean trimData;

	private Integer itemsAmount = null;

	private List<String> columnNames = null;
	private int currentRowNumber;

	public OdsDataProvider(File importFile, char[] zipPassword, boolean allowUnderfilledLines, boolean noHeaders, String nullValueText,
						   boolean trimData, String sheetname) {
		super(importFile, zipPassword);
		this.allowUnderfilledLines = allowUnderfilledLines;
		this.noHeaders = noHeaders;
		this.nullValueText = nullValueText;
		this.trimData = trimData;
		this.sheetname = sheetname;
	}

	@Override
	public String getConfigurationLogString() {
		return super.getConfigurationLogString()
			+ "Format: ODS" + "\n"
			+ "AllowUnderfilledLines: " + allowUnderfilledLines + "\n"
			+ "NoHeaders: " + noHeaders + "\n"
			+ "TrimData: " + trimData + "\n"
			+ (StringUtils.isNotBlank(sheetname) ? "Sheetname: " + sheetname + "\n" : "")
			+ "Null value text: " + (nullValueText == null ? "none" : "\"" + nullValueText + "\"") + "\n";
	}

	@Override
	public List<String> getAvailableDataPropertyNames() throws Exception {
		if (columnNames != null) {
			return columnNames;
		}

		if (xmlReader == null) {
			openReader();
		}
		currentRowNumber = 0;

		columnNames = new ArrayList<>();
		if (noHeaders) {
			final List<String> returnList = new ArrayList<>();
			if (allowUnderfilledLines) {
				// Scan all data for maximum
				List<Object> values;
				int maxColumns = 0;
				while ((values = readNextRow()) != null) {
					maxColumns = Math.max(maxColumns, values.size());
				}
				for (int i = 0; i < maxColumns; i++) {
					returnList.add(Integer.toString(i + 1));
				}
			} else {
				// Only take first data as example for all other data
				final List<Object> values = readNextRow();
				for (int i = 0; i < values.size(); i++) {
					returnList.add("column_" + (i + 1));
				}
			}
			columnNames = returnList;
		} else {
			// Read headers from file
			columnNames = readNextRow().stream()
					.map(Object::toString)
					.collect(Collectors.toList());

			// Remove empty trailing headernames
			while (columnNames.size() > 0 && StringUtils.isBlank(columnNames.get(columnNames.size() - 1))) {
				columnNames.remove(columnNames.get(columnNames.size() - 1));
			}
		}

		close();

	return columnNames;
	}

	private List<Object> readNextRow() throws Exception {
		List<Object> returnList = null;
		while (xmlReader.hasNext() && xmlReader.next() > 0) {
			if (xmlReader.isStartElement() && xmlReader.getLocalName().equals("table-row")) {
				currentRowNumber++;
				returnList = new ArrayList<>();

				while (xmlReader.next() > 0) {
					if (xmlReader.isStartElement() && xmlReader.getLocalName().equals("table-cell")) {
						if (xmlReader.getAttributeValue("xxxurn:oasis:names:tc:opendocument:xmlns:table:1.0", "number-columns-repeated") != null) {
							// TODO
							System.out.println("TODO");

							while (xmlReader.next() > 0) {
								if (!xmlReader.isEndElement() || !xmlReader.getLocalName().equals("table-cell")) {
									break;
								}
							}
						} else if (xmlReader.getAttributeValue("urn:oasis:names:tc:opendocument:xmlns:office:1.0", "value-type") != null && !"string".equalsIgnoreCase(xmlReader.getAttributeValue("urn:oasis:names:tc:opendocument:xmlns:office:1.0", "value-type"))) {
							final String dataType = xmlReader.getAttributeValue("urn:oasis:names:tc:opendocument:xmlns:office:1.0", "value-type");
							if ("float".equalsIgnoreCase(dataType)) {
								final String floatValue = xmlReader.getAttributeValue("urn:oasis:names:tc:opendocument:xmlns:office:1.0", "value");
								returnList.add(Double.parseDouble(floatValue));
							} else if ("date".equalsIgnoreCase(dataType)) {
								String dateAttributeValue = xmlReader.getAttributeValue("urn:oasis:names:tc:opendocument:xmlns:office:1.0", "date-value");
								Date dateValue = DateUtilities.parseIso8601DateTimeString(dateAttributeValue);
								returnList.add(dateValue);
							} else {
								throw new Exception("Unsupported datatype: " + dataType);
							}

							while (xmlReader.next() > 0) {
								if (!xmlReader.isEndElement() || !xmlReader.getLocalName().equals("table-cell")) {
									break;
								}
							}
						} else {
							boolean isEmptyCell = true;
							while (xmlReader.next() > 0) {
								if (xmlReader.isStartElement() && xmlReader.getLocalName().equals("p")) {
									String value = "";
									while (true) {
										xmlReader.next();
										if (xmlReader.isCharacters()) {
											value += xmlReader.getText().trim();
											isEmptyCell = false;
										} else if (xmlReader.isStartElement() && xmlReader.getLocalName().equals("s")) {
											value += " ";
											xmlReader.next();
										} else {
											if (!xmlReader.isStartElement() || !(xmlReader.getLocalName().equals("a") || xmlReader.getLocalName().equals("span"))) {
												break;
											}
										}
									}

									if (nullValueText != null && nullValueText.equals(value)) {
										value = null;
									}
									returnList.add(value);

									while (xmlReader.isEndElement() && (xmlReader.getLocalName().equals("a") || xmlReader.getLocalName().equals("span"))) {
										xmlReader.next();
									}

									if (xmlReader.isEndElement() && !xmlReader.getLocalName().equals("p")) {
										throw new Exception(
												String.format("Invalid xml data. Expected closing tag 'p', but got '%s' (end: %b) at line %d, column %d.", 
														xmlReader.getName(), 
														xmlReader.isEndElement(),
														xmlReader.getLocation().getLineNumber(), 
														xmlReader.getLocation().getColumnNumber()));
									}
								} else if (xmlReader.isEndElement() && xmlReader.getLocalName().equals("table-cell")) {
									if (isEmptyCell) {
										returnList.add("");
									}
									break;
								}
							}
						}
					} else if (xmlReader.isEndElement() && xmlReader.getLocalName().equals("table-row")) {
						break;
					}
				}
				break;
			}
		}
		if (returnList != null && returnList.size() == 1 && returnList.get(0).equals("")) {
			return null;
		}
		return returnList;
	}

	@Override
	public long getItemsAmountToImport() throws Exception {
		if (itemsAmount == null) {
			if (xmlReader == null) {
				openReader();
			}
			currentRowNumber = 0;

			int numberOfRows = 0;
			while (readNextRow() != null) {
				numberOfRows++;
			}

			if (noHeaders) {
				itemsAmount = numberOfRows;
			} else {
				itemsAmount = numberOfRows - 1;
			}

			close();
		}
		return itemsAmount;
	}

	private void openReader() throws Exception {
		if (xmlReader != null) {
			throw new IllegalStateException("Reader was already opened before");
		}

		@SuppressWarnings("resource")
		ZipInputStream zipInputStream = null;
		try {
			zipInputStream = new ZipInputStream(getInputStream());
			ZipEntry nextEntry;
			while ((nextEntry = zipInputStream.getNextEntry()) != null) {
				if (nextEntry.getName().equals("content.xml")) {
					contentXmlZipInputStream = zipInputStream;
					break;
				}
			}

			if (contentXmlZipInputStream == null) {
				throw new Exception("Cannot find ods data");
			}

			final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
			xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

			xmlReader = xmlInputFactory.createXMLStreamReader(contentXmlZipInputStream);

			final String dataPath = "document-content/body/spreadsheet/table";
			final Stack<String> currentPath = new Stack<>();
			String currentSheetname = null;
			xmlReader.next();
			currentPath.push(xmlReader.getLocalName());
			while (!currentPath.isEmpty() && !StringUtils.join(currentPath, "/").equals(dataPath) && !(sheetname == null || sheetname.equals(currentSheetname))) {
				xmlReader.next();
				if (xmlReader.isStartElement()) {
					currentPath.push(xmlReader.getLocalName());

					if ("table".equals(xmlReader.getLocalName())) {
						currentSheetname = null;
						for (int i = 0; i < xmlReader.getAttributeCount(); i++) {
							if (xmlReader.getAttributeLocalName(i).equals("name")) {
								currentSheetname = xmlReader.getAttributeValue(i);
								break;
							}
						}
					} else {
						currentSheetname = null;
					}
				} else if (xmlReader.isEndElement()) {
					currentPath.pop();
				}
			}
			if (currentPath.isEmpty()) {
				throw new Exception("Path '" + dataPath + "' is not part of the xml data");
			}

			currentRowNumber = 0;
		} catch (Exception e) {
			if (zipInputStream != null) {
				zipInputStream.close();
			}
			close();
			throw e;
		}
	}

	@Override
	public Map<String, Object> getNextItemData() throws Exception {
		if (columnNames == null) {
			getAvailableDataPropertyNames();
		}

		if (xmlReader == null) {
			openReader();
		}

		if (currentRowNumber == 0 && !noHeaders) {
			// Skip Header line
			readNextRow();
		}

		final List<Object> values = readNextRow();

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
		if (contentXmlZipInputStream != null) {
			try {
				contentXmlZipInputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			contentXmlZipInputStream = null;
		}
		if (xmlReader != null) {
			try {
				xmlReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			xmlReader = null;
		}
		super.close();
	}
}
