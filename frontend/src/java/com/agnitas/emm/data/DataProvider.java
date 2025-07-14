/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

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
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.agnitas.emm.common.exceptions.TooManyFilesInZipToImportException;
import com.agnitas.util.InputStreamWithOtherItemsToClose;
import com.agnitas.util.TarGzUtilities;
import com.agnitas.util.ZipUtilities;
import org.apache.commons.lang3.StringUtils;

public abstract class DataProvider implements Closeable {

	public abstract List<String> getAvailableDataPropertyNames() throws Exception;
	public abstract long getItemsAmountToImport() throws Exception;
	public abstract Map<String, Object> getNextItemData() throws Exception;

	private File importFile;
	private char[] zipPassword = null;
	private InputStream inputStream = null;
	
	public DataProvider(File importFile, char[] zipPassword) {
		this.importFile = importFile;
		this.zipPassword = zipPassword;
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
	public String getItemsUnitSign() {
		return null;
	}
}
