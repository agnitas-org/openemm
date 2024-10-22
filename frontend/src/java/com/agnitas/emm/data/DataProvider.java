/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.data;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.agnitas.emm.common.exceptions.TooManyFilesInZipToImportException;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.InputStreamWithOtherItemsToClose;
import org.agnitas.util.TarGzUtilities;
import org.agnitas.util.Tuple;
import org.agnitas.util.ZipUtilities;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.json.schema.validator.NumberUtilities;

public abstract class DataProvider implements Closeable {
	abstract public List<String> getAvailableDataPropertyNames() throws Exception;
	abstract public long getItemsAmountToImport() throws Exception;
	abstract public Map<String, Object> getNextItemData() throws Exception;
	abstract public Map<String, DbColumnType> scanDataPropertyTypes(Map<String, Tuple<String, String>> mapping) throws Exception;
	abstract public File filterDataItems(List<Integer> indexList, String fileSuffix) throws Exception;
	
	private File importFile;
	private char[] zipPassword = null;
	private InputStream inputStream = null;
	
	public DataProvider(File importFile, char[] zipPassword) {
		this.importFile = importFile;
		this.zipPassword = zipPassword;
	}

	public static void detectNextDataType(final Map<String, Tuple<String, String>> mapping, final Map<String, DbColumnType> dataTypes, final String propertyKey, final String currentValue) {
		String formatInfo = null;
		if (mapping != null) {
			for (final Tuple<String, String> mappingValue : mapping.values()) {
				if (mappingValue.getFirst().equals(propertyKey)) {
					if (StringUtils.isNotBlank(mappingValue.getSecond())) {
						formatInfo = mappingValue.getSecond();
						break;
					}
				}
			}
		}

		final SimpleDataType currentType = dataTypes.get(propertyKey) == null ? null : dataTypes.get(propertyKey).getSimpleDataType();
		if (currentType != SimpleDataType.Blob) {
			if (StringUtils.isEmpty(currentValue)) {
				if (!dataTypes.containsKey(propertyKey)) {
					dataTypes.put(propertyKey, null);
				}
			} else if ("file".equalsIgnoreCase(formatInfo) || currentValue.length() > 4000) {
				dataTypes.put(propertyKey, new DbColumnType("BLOB", -1, -1, -1, true));
			} else if (currentType != SimpleDataType.Characters && currentType != SimpleDataType.Numeric && currentType != SimpleDataType.Float && currentType != SimpleDataType.Blob && StringUtils.isNotBlank(formatInfo) && !".".equals(formatInfo) && !",".equals(formatInfo) && !"file".equalsIgnoreCase(formatInfo) && !"lc".equalsIgnoreCase(formatInfo) && !"uc".equalsIgnoreCase(formatInfo)) {
				try {
					DateUtilities.parseLocalDateTime(formatInfo, currentValue.trim());
					if (formatInfo != null && (formatInfo.toLowerCase().contains("h") || formatInfo.contains("m") || formatInfo.toLowerCase().contains("s"))) {
						dataTypes.put(propertyKey, new DbColumnType("TIMESTAMP", -1, -1, -1, true));
					} else {
						dataTypes.put(propertyKey, new DbColumnType("DATE", -1, -1, -1, true));
					}
				} catch (@SuppressWarnings("unused") final Exception e) {
					if (NumberUtilities.isInteger(currentValue) && currentValue.trim().length() <= 10) {
						dataTypes.put(propertyKey, new DbColumnType("INTEGER", -1, -1, -1, true));
					} else if (NumberUtilities.isDouble(currentValue) && currentValue.trim().length() <= 20) {
						dataTypes.put(propertyKey, new DbColumnType("DOUBLE", -1, -1, -1, true));
					} else {
						dataTypes.put(propertyKey, new DbColumnType("VARCHAR", Math.max(dataTypes.get(propertyKey) == null ? 0 : dataTypes.get(propertyKey).getCharacterLength(), currentValue.getBytes(StandardCharsets.UTF_8).length), -1, -1, true));
					}
				}
			} else if (currentType != SimpleDataType.Characters && currentType != SimpleDataType.Numeric && currentType != SimpleDataType.Float && currentType != SimpleDataType.Blob && currentType != SimpleDataType.Date && StringUtils.isBlank(formatInfo)) {
				try {
					DateUtilities.parseLocalDateTime(DateUtilities.getDateTimeFormatWithSecondsPattern(Locale.getDefault()), currentValue.trim());
					dataTypes.put(propertyKey, new DbColumnType("TIMESTAMP", -1, -1, -1, true));
				} catch (@SuppressWarnings("unused") final Exception e) {
					try {
						DateUtilities.parseLocalDate(DateUtilities.getDateFormatPattern(Locale.getDefault()), currentValue.trim());
						dataTypes.put(propertyKey, new DbColumnType("DATE", -1, -1, -1, true));
					} catch (@SuppressWarnings("unused") final Exception e1) {
						if (NumberUtilities.isInteger(currentValue) && currentValue.trim().length() <= 10) {
							dataTypes.put(propertyKey, new DbColumnType("INTEGER", -1, -1, -1, true));
						} else if (NumberUtilities.isDouble(currentValue) && currentValue.trim().length() <= 20) {
							dataTypes.put(propertyKey, new DbColumnType("DOUBLE", -1, -1, -1, true));
						} else {
							dataTypes.put(propertyKey, new DbColumnType("VARCHAR", Math.max(dataTypes.get(propertyKey) == null ? 0 : dataTypes.get(propertyKey).getCharacterLength(), currentValue.getBytes(StandardCharsets.UTF_8).length), -1, -1, true));
						}
					}
				}
			} else if (currentType != SimpleDataType.Characters && currentType != SimpleDataType.Numeric && currentType != SimpleDataType.Float && currentType != SimpleDataType.Blob && currentType != SimpleDataType.DateTime && StringUtils.isBlank(formatInfo)) {
				try {
					DateUtilities.parseLocalDate(DateUtilities.getDateFormatPattern(Locale.getDefault()), currentValue.trim());
					dataTypes.put(propertyKey, new DbColumnType("DATE", -1, -1, -1, true));
				} catch (@SuppressWarnings("unused") final Exception e) {
					if (NumberUtilities.isInteger(currentValue) && currentValue.trim().length() <= 10) {
						dataTypes.put(propertyKey, new DbColumnType("INTEGER", -1, -1, -1, true));
					} else if (NumberUtilities.isDouble(currentValue) && currentValue.trim().length() <= 20) {
						dataTypes.put(propertyKey, new DbColumnType("DOUBLE", -1, -1, -1, true));
					} else {
						dataTypes.put(propertyKey, new DbColumnType("VARCHAR", Math.max(dataTypes.get(propertyKey) == null ? 0 : dataTypes.get(propertyKey).getCharacterLength(), currentValue.getBytes(StandardCharsets.UTF_8).length), -1, -1, true));
					}
				}
			} else if (currentType != SimpleDataType.Characters && currentType != SimpleDataType.Date && currentType != SimpleDataType.DateTime && currentType != SimpleDataType.Float && NumberUtilities.isInteger(currentValue) && currentValue.trim().length() <= 10) {
				dataTypes.put(propertyKey, new DbColumnType("INTEGER", -1, -1, -1, true));
			} else if (currentType != SimpleDataType.Characters && currentType != SimpleDataType.Date && currentType != SimpleDataType.DateTime && NumberUtilities.isDouble(currentValue) && currentValue.trim().length() <= 20) {
				dataTypes.put(propertyKey, new DbColumnType("DOUBLE", -1, -1, -1, true));
			} else {
				dataTypes.put(propertyKey, new DbColumnType("VARCHAR", Math.max(dataTypes.get(propertyKey) == null ? 0 : dataTypes.get(propertyKey).getCharacterLength(), currentValue.getBytes(StandardCharsets.UTF_8).length), -1, -1, true));
			}
		}
	}
	
	protected InputStream getInputStream() throws Exception {
		if (!importFile.exists()) {
			throw new Exception("Import file does not exist: " + importFile.getAbsolutePath());
		} else if (importFile.isDirectory()) {
			throw new Exception("Import path is a directory: " + importFile.getAbsolutePath());
		} else if (importFile.length() == 0) {
			throw new Exception("Import file is empty: " + importFile.getAbsolutePath());
		} else {
			try {
				if (StringUtils.endsWithIgnoreCase(importFile.getAbsolutePath(), ".zip")) {
					if (zipPassword != null) {
						if (ZipUtilities.getZipFileEntries(importFile, zipPassword).size() != 1) {
							throw new TooManyFilesInZipToImportException("Compressed import file does not contain a single compressed file: " + importFile.getAbsolutePath());
						} else {
							inputStream = ZipUtilities.openPasswordSecuredZipFile(importFile.getAbsolutePath(), zipPassword);
						}
					} else {
						if (ZipUtilities.getZipFileEntries(importFile).size() != 1) {
							throw new TooManyFilesInZipToImportException("Compressed import file does not contain a single compressed file: " + importFile.getAbsolutePath());
						} else {
							inputStream = ZipUtilities.openZipFile(importFile.getAbsolutePath());
						}
					}
				} else if (StringUtils.endsWithIgnoreCase(importFile.getAbsolutePath(), ".tar.gz")) {
					if (TarGzUtilities.getFilesCount(importFile) != 1) {
						throw new TooManyFilesInZipToImportException("Compressed import file does not contain a single compressed file: " + importFile.getAbsolutePath());
					} else {
						inputStream = TarGzUtilities.openCompressedFile(importFile);
					}
				} else if (StringUtils.endsWithIgnoreCase(importFile.getAbsolutePath(), ".tgz")) {
					if (TarGzUtilities.getFilesCount(importFile) != 1) {
						throw new TooManyFilesInZipToImportException("Compressed import file does not contain a single compressed file: " + importFile.getAbsolutePath());
					} else {
						inputStream = TarGzUtilities.openCompressedFile(importFile);
					}
				} else if (StringUtils.endsWithIgnoreCase(importFile.getAbsolutePath(), ".gz")) {
					inputStream = new GZIPInputStream(new FileInputStream(importFile));
				} else {
					inputStream = new InputStreamWithOtherItemsToClose(new FileInputStream(importFile), importFile.getAbsolutePath());
				}
				return inputStream;
			} catch (final Exception e) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (@SuppressWarnings("unused") final IOException e1) {
						// do nothing
					}
				}
				throw e;
			}
		}
	}
	
	public String getConfigurationLogString() {
		String configurationLogString =  "File: " + importFile.getAbsolutePath() + "\n";
		if (StringUtils.endsWithIgnoreCase(importFile.getAbsolutePath(), ".zip")) {
			configurationLogString += "Zip: true\n";
			if (zipPassword != null) {
				configurationLogString += "ZipPassword: true\n";
			}
		}
		return configurationLogString;
	}

	@Override
	public void close() {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
			inputStream = null;
		}
	}
	
	public String getImportFilePath() {
		return importFile.getAbsolutePath();
	}
	
	/**
	 * Raw import data size.<br/>
	 * This means, the data might be compressed and this is the size of the <b>compressed</b> data.
	 */
	public long getImportDataSize() {
		return importFile.length();
	}

	/**
	 * Real import data size.<br/>
	 * This means, the data might be compressed and this is the size of the <b>uncompressed</b> data.
	 */
	public long getImportDataAmount() throws Exception {
		if (StringUtils.endsWithIgnoreCase(importFile.getAbsolutePath(), ".zip")) {
			if (zipPassword != null)  {
				return ZipUtilities.getUncompressedSize(importFile, zipPassword);
			} else {
				return ZipUtilities.getDataSizeUncompressed(importFile);
			}
		} else {
			return importFile.length();
		}
	}

	@SuppressWarnings("unused")
	public String getItemsUnitSign() throws Exception {
		return null;
	}
}
