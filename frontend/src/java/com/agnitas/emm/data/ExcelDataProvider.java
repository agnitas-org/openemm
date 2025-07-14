/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.data;

import java.io.File;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.agnitas.service.ImportException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelDataProvider extends DataProvider {

	// Default optional parameters
	private final String nullValueText;
	private final boolean allowUnderfilledLines;
	private boolean ignoreDataWithoutHeader = true;
	private final boolean noHeaders;
	private final boolean trimData;

	private final String dataPath;
	private HSSFWorkbook hssfWorkbook;
	private HSSFSheet hssfSheet;
	private XSSFWorkbook xssfWorkbook;
	private XSSFSheet xssfSheet;
	private int currentRowNumber;
	private int maxRowNumber;

	private List<String> columnNames = null;
	private Integer itemsAmount = null;

	public ExcelDataProvider(File importFile, char[] zipPassword, boolean allowUnderfilledLines, boolean noHeaders, String nullValueText,
							 boolean trimData, String dataPath) throws Exception {
		super(importFile, zipPassword);
		this.allowUnderfilledLines = allowUnderfilledLines;
		this.noHeaders = noHeaders;
		this.nullValueText = nullValueText;
		this.trimData = trimData;
		this.dataPath = dataPath;
		
		openReader();
	}

	public void setIgnoreDataWithoutHeader(boolean ignoreDataWithoutHeader) {
		this.ignoreDataWithoutHeader = ignoreDataWithoutHeader;
	}

	@Override
	public String getConfigurationLogString() {
		return getConfigurationLogString()
			+ "Format: EXCEL" + "\n"
			+ "AllowUnderfilledLines: " + allowUnderfilledLines + "\n"
			+ "IgnoreDataWithoutHeader: " + ignoreDataWithoutHeader + "\n"
			+ "TrimData: " + trimData + "\n"
			+ "Null value text: " + (nullValueText == null ? "none" : "\"" + nullValueText + "\"") + "\n";
	}

	@Override
	public List<String> getAvailableDataPropertyNames() {
		return columnNames.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	@Override
	public long getItemsAmountToImport() {
		if (itemsAmount == null) {
			if (hssfSheet != null) {
				itemsAmount = maxRowNumber - hssfSheet.getFirstRowNum() + 1;
			} else {
				itemsAmount = maxRowNumber - xssfSheet.getFirstRowNum() + 1;
			}
			if (!noHeaders) {
				// Skip header row
				itemsAmount--;
			}
		}
		return itemsAmount;
	}

	@Override
	public Map<String, Object> getNextItemData() {
		if (currentRowNumber > maxRowNumber) {
			return null;
		}

		final List<Object> values = new ArrayList<>();
		if (hssfSheet != null) {
			for (int i = hssfSheet.getRow(hssfSheet.getFirstRowNum()).getFirstCellNum(); i < hssfSheet.getRow(hssfSheet.getFirstRowNum()).getLastCellNum(); i++) {
				final HSSFCell cell = hssfSheet.getRow(currentRowNumber).getCell(i);
				
				if (cell == null) {
					values.add(null);
				} else if (cell.getCellType() == CellType.NUMERIC) {
					if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
						values.add(cell.getDateCellValue());
					} else {
						values.add(cell.getNumericCellValue());
					}
				} else if (cell.getCellType() == CellType.STRING) {
					values.add(trimData ? StringUtils.trim(cell.getStringCellValue()) : cell.getStringCellValue());
				} else if (cell.getCellType() == CellType.BLANK) {
					values.add(null);
				} else {
					values.add(trimData ? StringUtils.trim(cell.getStringCellValue()) : cell.getStringCellValue());
				}
			}
		} else {
			for (int i = xssfSheet.getRow(xssfSheet.getFirstRowNum()).getFirstCellNum(); i < xssfSheet.getRow(xssfSheet.getFirstRowNum()).getLastCellNum(); i++) {
				final XSSFCell cell = xssfSheet.getRow(currentRowNumber).getCell(i);
				if (cell == null) {
					values.add(null);
				} else if (cell.getCellType() == CellType.NUMERIC) {
					if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
						values.add(cell.getDateCellValue());
					} else {
						values.add(cell.getNumericCellValue());
					}
				} else if (cell.getCellType() == CellType.STRING) {
					values.add(trimData ? StringUtils.trim(cell.getStringCellValue()) : cell.getStringCellValue());
				} else if (cell.getCellType() == CellType.BLANK) {
					values.add(null);
				} else {
					values.add(trimData ? StringUtils.trim(cell.getStringCellValue()) : cell.getStringCellValue());
				}
			}
		}

		final Map<String, Object> returnMap = new HashMap<>();
		for (int i = 0; i < columnNames.size(); i++) {
			final String columnName = columnNames.get(i);
			if (StringUtils.isNotBlank(columnName)) {
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
		}

		currentRowNumber++;

		return returnMap;
	}

	@Override
	public void close() {
		if (hssfWorkbook != null) {
			IOUtils.closeQuietly(hssfWorkbook);
			hssfWorkbook = null;
			hssfSheet = null;
		}
		if (xssfWorkbook != null) {
			IOUtils.closeQuietly(xssfWorkbook);
			xssfWorkbook = null;
			xssfSheet = null;
		}
		super.close();
	}

	private void openReader() throws Exception {
		try {
			if (xssfSheet == null && hssfSheet == null) {
				PushbackInputStream excelInputStream = new PushbackInputStream(getInputStream(), 8);
				boolean isXlsFormatFile = isXlsFile(excelInputStream);
				
				if (isXlsFormatFile) {
					hssfWorkbook = new HSSFWorkbook(excelInputStream);
					if (StringUtils.isNotBlank(dataPath)) {
						hssfSheet = hssfWorkbook.getSheet(dataPath);
					} else {
						hssfSheet = hssfWorkbook.getSheetAt(0);
					}
					
					if (hssfSheet != null) {
						currentRowNumber = hssfSheet.getFirstRowNum();
						maxRowNumber = hssfSheet.getLastRowNum();
						// Microsoft Excel shows ~1.046.000 available lines, but
						// they are not used at all. So detect the last used line
						for (int rowIndex = maxRowNumber; rowIndex >= currentRowNumber; rowIndex--) {
							final HSSFRow row = hssfSheet.getRow(rowIndex);
							if (row != null) {
								boolean rowHasValue = false;
								for (int columnIndex = row.getFirstCellNum(); columnIndex < row.getLastCellNum(); columnIndex++) {
									if (row.getCell(columnIndex) != null) {
										rowHasValue = true;
										break;
									}
								}
								if (rowHasValue) {
									maxRowNumber = rowIndex;
									break;
								}
							}
						}
					} else {
						throw new Exception("Cannot find data sheet");
					}
				} else {
					xssfWorkbook = new XSSFWorkbook(excelInputStream);
					if (StringUtils.isNotBlank(dataPath)) {
						xssfSheet = xssfWorkbook.getSheet(dataPath);
					} else {
						xssfSheet = xssfWorkbook.getSheetAt(0);
					}
					
					if (xssfSheet != null) {
						currentRowNumber = xssfSheet.getFirstRowNum();
						maxRowNumber = xssfSheet.getLastRowNum();
						// Microsoft Excel shows ~1.046.000 available lines, but
						// they are not used at all. So detect the last used line
						for (int rowIndex = maxRowNumber; rowIndex >= currentRowNumber; rowIndex--) {
							final XSSFRow row = xssfSheet.getRow(rowIndex);
							if (row != null) {
								boolean rowHasValue = false;
								for (int columnIndex = row.getFirstCellNum(); columnIndex < row.getLastCellNum(); columnIndex++) {
									if (row.getCell(columnIndex) != null) {
										rowHasValue = true;
										break;
									}
								}
								if (rowHasValue) {
									maxRowNumber = rowIndex;
									break;
								}
							}
						}
					} else {
						throw new Exception("Cannot find data sheet");
					}
				}

				if (!noHeaders) {
					currentRowNumber++;
				}
			}
			
			columnNames = new ArrayList<>();
			if (noHeaders) {
				final List<String> returnList = new ArrayList<>();
				if (allowUnderfilledLines) {
					// Scan all data for maximum
					int maxColumns = 0;
					if (hssfSheet != null) {
						for (int i = hssfSheet.getFirstRowNum(); i <= maxRowNumber; i++) {
							maxColumns = Math.max(maxColumns, hssfSheet.getRow(i).getLastCellNum() - hssfSheet.getRow(i).getFirstCellNum());
						}
					} else {
						for (int i = xssfSheet.getFirstRowNum(); i <= maxRowNumber; i++) {
							maxColumns = Math.max(maxColumns, xssfSheet.getRow(i).getLastCellNum() - xssfSheet.getRow(i).getFirstCellNum());
						}
					}
					for (int i = 0; i < maxColumns; i++) {
						returnList.add(Integer.toString(i + 1));
					}
					columnNames = returnList;
				} else {
					// Only take first data as example for all other data
					int maxColumns = 0;
					if (hssfSheet != null) {
						maxColumns = hssfSheet.getRow(hssfSheet.getFirstRowNum()).getLastCellNum() - hssfSheet.getRow(hssfSheet.getFirstRowNum()).getFirstCellNum();
					} else {
						maxColumns = xssfSheet.getRow(xssfSheet.getFirstRowNum()).getLastCellNum() - xssfSheet.getRow(xssfSheet.getFirstRowNum()).getFirstCellNum();
					}
					for (int i = 0; i < maxColumns; i++) {
						returnList.add("column_" + (i + 1));
					}
					columnNames = returnList;
				}
			} else {
				// Read headers from file
				if (hssfSheet != null) {
					for (int i = hssfSheet.getRow(hssfSheet.getFirstRowNum()).getFirstCellNum(); i < hssfSheet.getRow(hssfSheet.getFirstRowNum()).getLastCellNum(); i++) {
						final HSSFCell cell = hssfSheet.getRow(hssfSheet.getFirstRowNum()).getCell(i);
						final String cellValue = cell == null ? null : cell.getStringCellValue();
						String nextColumnName = trimData ? StringUtils.trim(cellValue) : cellValue;
						if (StringUtils.isNotBlank(nextColumnName)) {
							columnNames.add(nextColumnName);
						} else {
							columnNames.add(null);
						}
					}

					// Check for data in columns without Header
					if (!ignoreDataWithoutHeader) {
						for (int columnIndex = 0; columnIndex < columnNames.size(); columnIndex++) {
							if (StringUtils.isBlank(columnNames.get(columnIndex))) {
								for (int rowIndex = hssfSheet.getFirstRowNum(); rowIndex <= maxRowNumber; rowIndex++) {
									int maxRowColumn = hssfSheet.getRow(rowIndex).getLastCellNum() - hssfSheet.getRow(rowIndex).getFirstCellNum();
									if (columnIndex < maxRowColumn) {
										final HSSFCell cell = hssfSheet.getRow(rowIndex).getCell(columnIndex);
										final String cellValue = cell == null ? null : cell.getStringCellValue();
										if (StringUtils.isNotBlank(cellValue)) {
											throw new ImportException(false, "error.import.data.header", columnIndexToLetters(columnIndex), (rowIndex + 1));
										}
									}
								}
							}
						}
					}
				} else {
					for (int i = xssfSheet.getRow(xssfSheet.getFirstRowNum()).getFirstCellNum(); i < xssfSheet.getRow(xssfSheet.getFirstRowNum()).getLastCellNum(); i++) {
						final XSSFCell cell = xssfSheet.getRow(xssfSheet.getFirstRowNum()).getCell(i);
						final String cellValue = cell == null ? null : cell.getStringCellValue();
						String nextColumnName = trimData ? StringUtils.trim(cellValue) : cellValue;
						if (StringUtils.isNotBlank(nextColumnName)) {
							columnNames.add(nextColumnName);
						} else {
							columnNames.add(null);
						}
					}

					// Check for data in columns without Header
					if (!ignoreDataWithoutHeader) {
						for (int columnIndex = 0; columnIndex < columnNames.size(); columnIndex++) {
							if (StringUtils.isBlank(columnNames.get(columnIndex))) {
								for (int rowIndex = xssfSheet.getFirstRowNum(); rowIndex <= maxRowNumber; rowIndex++) {
									int maxRowColumn = xssfSheet.getRow(rowIndex).getLastCellNum() - xssfSheet.getRow(rowIndex).getFirstCellNum();
									if (columnIndex < maxRowColumn) {
										final XSSFCell cell = xssfSheet.getRow(rowIndex).getCell(columnIndex);
										final String cellValue = cell == null ? null : cell.getStringCellValue();
										if (StringUtils.isNotBlank(cellValue)) {
											throw new ImportException(false, "error.import.data.header", columnIndexToLetters(columnIndex), (rowIndex + 1));
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (final Exception e) {
			close();
			throw e;
		}
	}

	/**
	 * XLS = Excel 2003 format
	 */
	public static boolean isXlsFile(PushbackInputStream inputStream) throws IOException {
		final byte[] magicBytes = new byte[8];
		final int readBytes = inputStream.read(magicBytes);
		inputStream.unread(magicBytes, 0, readBytes);
		return readBytes == 8 && Byte.toUnsignedInt(magicBytes[0]) == 0xD0 && Byte.toUnsignedInt(magicBytes[1]) == 0xCF && Byte.toUnsignedInt(magicBytes[2]) == 0x11
				&& Byte.toUnsignedInt(magicBytes[3]) == 0xE0 && Byte.toUnsignedInt(magicBytes[4]) == 0xA1 && Byte.toUnsignedInt(magicBytes[5]) == 0xB1
				&& Byte.toUnsignedInt(magicBytes[6]) == 0x1A && Byte.toUnsignedInt(magicBytes[7]) == 0xE1;
	}
	
	public static String columnIndexToLetters(int columnIndex) {
		char[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		String columnLetters = "";
		columnLetters = letters[columnIndex % letters.length] + columnLetters;
		int columnIndexTemp = columnIndex / letters.length;
		while (columnIndexTemp > 0) {
			columnLetters = letters[(columnIndexTemp % letters.length) - 1] + columnLetters;
			columnIndexTemp = columnIndexTemp / letters.length;
		}
		return columnLetters;
	}
}
