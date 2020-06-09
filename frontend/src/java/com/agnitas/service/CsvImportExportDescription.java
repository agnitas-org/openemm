/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.impl.ColumnMappingImpl;
import org.agnitas.service.ImportException;
import org.agnitas.service.ImportMethod;
import org.agnitas.service.UpdateMethod;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.CsvReader;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.TempFileInputStream;
import org.agnitas.util.ZipUtilities;
import org.agnitas.util.importvalues.CheckForDuplicates;
import org.agnitas.util.importvalues.TextRecognitionChar;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.emm.core.referencetable.beans.ComReferenceTable;
import com.agnitas.service.ImportError.ImportErrorKey;

public class CsvImportExportDescription {
	private int id = -1;
	private String name;
	private String tableName;
	private int companyID;
	private List<ColumnMapping> columnMapping;
	private String encoding;
	private char delimiter;
	private boolean alwaysQuote;
	private Character stringQuote;
	private boolean fullImportOnly;
    private ImportMethod importMethod = ImportMethod.UpdateAndInsert;
    private UpdateMethod updateMethod = UpdateMethod.UpdateAll;
	private boolean forImport;
	private CheckForDuplicates checkForDuplicates = CheckForDuplicates.COMPLETE;
	private boolean zipped = false;
    private String zipPassword = null;
	private boolean autoMapping = false;

	/**
	 * If column with name "creation_date" and type "DATE" exists only rows with creation_date >= (export date + dateCreationDaysFrom) will be exported.
	 */
	private Integer dateCreationDaysFrom;

	/**
	 * If column with name "creation_date" and type "DATE" exists only rows with creation_date <= (export date + dateCreationDaysTill) will be exported.
	 */
	private Integer dateCreationDaysTill;

	/**
	 * If column with name "change_date" and type "DATE" exists only rows with change_date >= (export date + dateChangeDaysFrom) will be exported.
	 */
	private Integer dateChangeDaysFrom;

	/**
	 * If column with name "change_date" and type "DATE" exists only rows with change_date <= (export date + dateChangeDaysTill) will be exported.
	 */
	private Integer dateChangeDaysTill;

	private boolean noHeaders = false;
	
	private String mailForError;
	
	private String mailForReport;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public int getCompanyID() {
		return companyID;
	}
	
	public void setCompanyID(int companyID) {
		this.companyID = companyID;
	}
	
	public List<ColumnMapping> getColumnMapping() {
		return columnMapping;
	}
	
	public void setColumnMapping(List<ColumnMapping> columnMapping) {
		this.columnMapping = columnMapping;
	}
	
	public String getEncoding() {
		return encoding;
	}
	
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	public char getDelimiter() {
		return delimiter;
	}
	
	public void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}
	
	public Character getStringQuote() {
		return stringQuote;
	}
	
	public void setStringQuote(Character stringQuote) {
		this.stringQuote = stringQuote;
	}

	public boolean isFullImportOnly() {
		return fullImportOnly;
	}

	public void setFullImportOnly(boolean fullimportonly) {
		this.fullImportOnly = fullimportonly;
	}

	public ImportMethod getImportMethod() {
		return importMethod;
	}

	public void setImportMethod(ImportMethod importMethod) {
		if (importMethod != null) {
			this.importMethod = importMethod;
		} else {
			this.importMethod = ImportMethod.UpdateAndInsert;
		}
	}

	public UpdateMethod getUpdateMethod() {
		return updateMethod;
	}

	public void setUpdateMethod(UpdateMethod updateMethod) {
		if (updateMethod != null) {
			this.updateMethod = updateMethod;
		} else {
			this.updateMethod = UpdateMethod.UpdateAll;
		}
	}

	public boolean isForImport() {
		return forImport;
	}

	public void setForImport(boolean forImport) {
		this.forImport = forImport;
	}

	/**
	 * If column with name "creation_date" and type "DATE" exists only rows with creation_date >= (export date + dateCreationDaysFrom) will be exported.
	 */
	public Integer getDateCreationDaysFrom() {
		return dateCreationDaysFrom;
	}

	public void setDateCreationDaysFrom(Integer dateCreationDaysFrom) {
		this.dateCreationDaysFrom = dateCreationDaysFrom;
	}

	/**
	 * If column with name "creation_date" and type "DATE" exists only rows with creation_date <= (export date + dateCreationDaysTill) will be exported.
	 */
	public Integer getDateCreationDaysTill() {
		return dateCreationDaysTill;
	}

	public void setDateCreationDaysTill(Integer dateCreationDaysTill) {
		this.dateCreationDaysTill = dateCreationDaysTill;
	}

	/**
	 * If column with name "change_date" and type "DATE" exists only rows with change_date >= (export date + dateChangeDaysFrom) will be exported.
	 */
	public Integer getDateChangeDaysFrom() {
		return dateChangeDaysFrom;
	}

	public void setDateChangeDaysFrom(Integer dateChangeDaysFrom) {
		this.dateChangeDaysFrom = dateChangeDaysFrom;
	}

	/**
	 * If column with name "change_date" and type "DATE" exists only rows with change_date <= (export date + dateChangeDaysTill) will be exported.
	 */
	public Integer getDateChangeDaysTill() {
		return dateChangeDaysTill;
	}

	public void setDateChangeDaysTill(Integer dateChangeDaysTill) {
		this.dateChangeDaysTill = dateChangeDaysTill;
	}

	public void setCheckForDuplicates(CheckForDuplicates checkForDuplicates) {
		this.checkForDuplicates = checkForDuplicates;
	}

	public CheckForDuplicates getCheckForDuplicates() {
		return checkForDuplicates;
	}

	public void setZipped(boolean zipped) {
		this.zipped = zipped;
	}

	public boolean isZipped() {
		return zipped;
	}

	public void setZipPassword(String zipPassword) {
		this.zipPassword = zipPassword;
	}

	public String getZipPassword() {
		return zipPassword;
	}

	public void setAutoMapping(boolean autoMapping) {
		this.autoMapping = autoMapping;
	}

	public boolean isAutoMapping() {
		return autoMapping;
	}
	
	public void setNoHeaders(boolean noHeaders) {
		this.noHeaders = noHeaders;
	}
	
	public boolean isNoHeaders() {
		return noHeaders;
	}

	public String getMailForReport() {
		return mailForReport;
	}

	public void setMailForReport(String mailForReport) {
		this.mailForReport = mailForReport;
	}

	public String getMailForError() {
		return mailForError;
	}

	public void setMailForError(String mailForError) {
		this.mailForError = mailForError;
	}

	public boolean isAlwaysQuote() {
		return alwaysQuote;
	}

	public void setAlwaysQuote(boolean alwaysQuote) {
		this.alwaysQuote = alwaysQuote;
	}

	// TODO: move to service layer
	public void prepareColumnMapping(ComReferenceTable table, CaseInsensitiveMap<String, DbColumnType> structure, File importFile) throws Exception {
		List<String> csvColumns;
		try (CsvReader readerForAnalyse = new CsvReader(getImportInputStream(importFile), getEncoding(), getDelimiter(), getStringQuote())) {
			readerForAnalyse.setAlwaysTrim(true);
			csvColumns = readerForAnalyse.readNextCsvLine();
		} catch (Exception e) {
			throw new ImportError(ImportErrorKey.cannotReadImportFile);
		}
		
		if (csvColumns == null) {
			throw new ImportError(ImportErrorKey.emptyImportFile);
		}
			
		String duplicateCsvColumn = CsvReader.checkForDuplicateCsvHeader(csvColumns, autoMapping);
		if (duplicateCsvColumn != null) {
			throw new ImportError(ImportErrorKey.csvContainsInvalidColumn, duplicateCsvColumn);
		}

		String keyColumn = table.getKeyColumn().toLowerCase();

		if (autoMapping || CollectionUtils.isEmpty(columnMapping)) {
			List<ColumnMapping> mappings = new ArrayList<>();

			for (String csvColumn : csvColumns) {
				if (structure.containsKey(csvColumn)) {
					mappings.add(createMapping(csvColumn, csvColumn.toLowerCase(), csvColumn.equalsIgnoreCase(keyColumn)));
				} else {
					throw exceptionCsvInvalidColumn(csvColumn);
				}
			}

			columnMapping = mappings;
		}

		validateColumnMapping(table.isVoucher(), structure, keyColumn, csvColumns);
	}

	private InputStream getImportInputStream(File importFile) throws FileNotFoundException {
		if (isZipped()) {
			try {
				if (getZipPassword() == null) {
					InputStream dataInputStream = ZipUtilities.openZipInputStream(new FileInputStream(importFile));
					ZipEntry zipEntry = ((ZipInputStream) dataInputStream).getNextEntry();
					if (zipEntry == null) {
						throw new ImportException(false, "error.unzip.noEntry");
					}
					return dataInputStream;
				} else {
					File unzipPath = new File(importFile.getAbsolutePath() + ".unzipped");
					unzipPath.mkdir();
					ZipUtilities.decompressFromEncryptedZipFile(importFile, unzipPath, getZipPassword());
					
					// Check if there was only one file within the zip file and use it for import
					String[] filesToImport = unzipPath.list();
					if (filesToImport.length != 1) {
						throw new Exception("Invalid number of files included in zip file");
					}
					InputStream dataInputStream = new FileInputStream(unzipPath.getAbsolutePath() + "/" + filesToImport[0]);
					return new TempFileInputStream(dataInputStream, unzipPath);
				}
			} catch (ImportException e) {
				throw e;
			} catch (Exception e) {
				throw new ImportException(false, "error.unzip", e.getMessage());
			}
		} else {
			return new FileInputStream(importFile);
		}
	}

	private void validateColumnMapping(boolean isVoucherTable, CaseInsensitiveMap<String, DbColumnType> structure, String keyColumn, List<String> csvColumns) {
		// Check if mapping only contains csv-file-column indexes
		boolean columnIndexKeyMapping = true;
		for (ColumnMapping mapping : getColumnMapping()) {
			if (!AgnUtils.isNumber(mapping.getFileColumn())) {
				columnIndexKeyMapping = false;
				break;
			}
		}

		if (columnIndexKeyMapping) {
			// Mapping only contains csv-file-column indexes
			if (getMappingByDbColumn(keyColumn) == null) {
				throw new ImportError(ImportErrorKey.mappingMustContainKeyColumn, keyColumn);
			}

			for (int csvColumnIndex = 1; csvColumnIndex <= csvColumns.size(); csvColumnIndex++) {
				validateMapping(Integer.toString(csvColumnIndex), structure);
			}
		} else {
			if (isVoucherTable) {
				if (getMappingByDbColumn("voucher_code") == null) {
					throw new ImportError(ImportErrorKey.mappingMustContainKeyColumn, "voucher_code");
				}
			} else {
				if (getMappingByDbColumn(keyColumn) == null) {
					throw new ImportError(ImportErrorKey.mappingMustContainKeyColumn, keyColumn);
				}
			}

			for (String csvColumn : csvColumns) {
				validateMapping(csvColumn, structure);
			}
		}
	}

	private void validateMapping(String csvColumn, CaseInsensitiveMap<String, DbColumnType> structure) {
		ColumnMapping mapping = getMappingByFileColumn(csvColumn);

		if (mapping != null && !ColumnMapping.DO_NOT_IMPORT.equalsIgnoreCase(mapping.getDatabaseColumn())) {
			if (!structure.containsKey(mapping.getDatabaseColumn())) {
				throw new ImportError(ImportErrorKey.csvContainsInvalidColumn, csvColumn);
			}
		}
	}

	private ColumnMapping createMapping(String source, String target, boolean isKeyColumn) {
		ColumnMapping mapping = new ColumnMappingImpl();

		mapping.setFileColumn(source);
		mapping.setDatabaseColumn(target);
		mapping.setKeyColumn(isKeyColumn);

		return mapping;
	}

	private Exception exceptionCsvInvalidColumn(String column) {
		if (StringUtils.isBlank(column)) {
			return new Exception("Invalid empty csvfile header for import automapping");
		} else {
			return new ImportError(ImportErrorKey.csvContainsInvalidColumn, column);
		}
	}

	public ColumnMapping getMappingByDbColumn(String dbColumn) {
		for (ColumnMapping mapping : columnMapping) {
			if (mapping.getDatabaseColumn().equalsIgnoreCase(dbColumn)) {
				return mapping;
			}
		}
		return null;
	}

	public ColumnMapping getMappingByFileColumn(String fileColumn) {
		for (ColumnMapping mapping : columnMapping) {
			if (mapping.getFileColumn().equals(fileColumn)) {
				return mapping;
			}
		}
		return null;
	}

	public List<String> getKeyColumns() {
		List<String> keyColumns = new ArrayList<>();
		for (ColumnMapping mapping : columnMapping) {
			if (mapping.isKeyColumn()) {
				keyColumns.add(mapping.getDatabaseColumn());
			}
		}
		return keyColumns;
	}

	@Override
	public String toString() {
		StringBuilder output = new StringBuilder("\"" + name + "\" (CID " + companyID + " / ID " + id + ")\n");
		try {
			output.append("Charset: " + encoding + "\n");
		} catch (Exception e) {
			output.append("Charset: Invalid (\"" + e.getMessage() + "\")\n");
		}
		output.append("NoHeaders: " + noHeaders + "\n");
		output.append("Zipped: " + zipped + "\n");
		output.append("EncryptedZip: " + (zipPassword != null) + "\n");
		output.append("Separator: " + delimiter + "\n");
		try {
			output.append("TextRecognitionChar: " + TextRecognitionChar.getTextRecognitionCharByChar(stringQuote).name() + "\n");
		} catch (Exception e) {
			output.append("TextRecognitionChar: Invalid (\"" + e.getMessage() + "\")\n");
		}
		try {
			output.append("ImportMethod: " + importMethod.name() + "\n");
		} catch (Exception e) {
			output.append("ImportMethod: Invalid (\"" + e.getMessage() + "\")\n");
		}
		try {
			output.append("UpdateMethod: " + updateMethod.name() + "\n");
		} catch (Exception e) {
			output.append("UpdateMethod: Invalid (\"" + e.getMessage() + "\")\n");
		}
		try {
			output.append("CheckForDuplicates: " + checkForDuplicates.name() + "\n");
		} catch (Exception e) {
			output.append("CheckForDuplicates: Invalid (\"" + e.getMessage() + "\")\n");
		}
		
		output.append("ColumnMapping: \n");
		for (ColumnMapping mapping : columnMapping) {
			output.append("\t" + mapping.getDatabaseColumn() + " = \"" + mapping.getFileColumn() + "\""
				+ (mapping.isKeyColumn() ? " keycolumn" : "")
				+ (mapping.isEncrypted() ? " encrypted" : "")
				+ (mapping.isMandatory() ? " mandatory" : "")
				+ (StringUtils.isNotEmpty(mapping.getFormat()) ? " Format: \"" + mapping.getFormat() + "\"" : "")
				+ (StringUtils.isNotEmpty(mapping.getDefaultValue()) ? " Default: \"" + mapping.getDefaultValue() + "\"" : "")
				+ "\n");
		}
		
		return output.toString();
	}
}
