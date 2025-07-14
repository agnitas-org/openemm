/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.ImportStatus;
import com.agnitas.service.GenericImportException.ReasonCode;
import com.agnitas.util.ImportUtils.ImportErrorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public class ImportStatusImpl implements ImportStatus {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ImportStatusImpl.class);

	protected int id;

	protected int company;

	protected int admin;

	protected int datasource;

	protected int mode;

	protected int doubleCheck;

	protected int ignoreNull = 1;

	protected char separator = ';';

	protected String delimiter = "";

	protected String keycolumn;

	protected String charset = "";

	protected int recordsBefore;

	protected long fileSize;

	protected int csvLines;

	protected int blacklisted;

	protected int invalidNullValues;
	
	protected int fields;

	protected int inserted;

	protected int updated;
	
	protected String fatalError = null;

	protected Map<ImportErrorType, Integer> errors = new HashMap<>();
	
	private List<Integer> firstErrorLineNumbers = new ArrayList<>();

	private List<String> firstErrorTypes = new ArrayList<>();
	
	private List<String> firstErrorColumns = new ArrayList<>();

	private int duplicatesInImportData;
	
	private int alreadyInDb;
	
	private Date changeDate;

	private Date creationDate;

	private int deletedEntries;

	private File importedRecipientsCsvFile;

	private File invalidRecipientsCsvFile;

	private File fixedByUserRecipientsCsvFile;

	private File duplicateInCsvOrDbRecipientsCsvFile;
	
	private Set<String> errorColumns = new HashSet<>();

	private Map<MediaTypes, Map<Integer, Integer>> mailinglistStatistics = new HashMap<>();
	
	private boolean nearLimit = false;

	public ImportStatusImpl() {
	}

	// * * * * *
	// SETTER:
	// * * * * *
	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void setCompanyID( int company) {
		this.company = company;
	}

	@Override
	public void setAdminID(int admin) {
		this.admin = admin;
	}

	@Override
	public void setDatasourceID(int datasource) {
		this.datasource = datasource;
	}

	@Override
	public void setMode(int mode) {
		this.mode = mode;
	}

	@Override
	public void setDoubleCheck(int doubleCheck) {
		this.doubleCheck = doubleCheck;
	}

	@Override
	public void setIgnoreNull(int ignoreNull) {
		this.ignoreNull = ignoreNull;
	}

	@Override
	public void setSeparator(char separator) {
		if (separator == 't') {
			this.separator = '\t';
		} else {
			this.separator = separator;
		}
	}

	@Override
	public void setDelimiter(String delimiter) {
		if (delimiter != null) {
			this.delimiter = delimiter;
		} else {
			this.delimiter = "";
		}
	}

	@Override
	public void setKeycolumn(String keycolumn) {
		if (keycolumn == null) {
			this.keycolumn = null;
		} else {
			this.keycolumn = keycolumn.toUpperCase();
		}
	}

	@Override
	public void setCharset(String charset) {
		this.charset = charset;
	}

	@Override
	public void setRecordsBefore(int recordsBefore) {
		this.recordsBefore = recordsBefore;
	}

	@Override
	public void setFields(int fields) {
		this.fields = fields;
	}

	@Override
	public void setInserted(int inserted) {
		this.inserted = inserted;
	}

	@Override
	public void setUpdated(int updated) {
		this.updated = updated;
	}

	@Override
	public void setErrors(Map<ImportErrorType, Integer> errors) {
		this.errors = errors;
	}

	@Override
	public void setError(ImportErrorType id, Integer value) {
		errors.put(id, value);
	}

	// * * * * *
	// GETTER:
	// * * * * *
	@Override
	public int getId() {
		return id;
	}

	@Override
	public int getCompanyID() {
		return company;
	}

	@Override
	public int getAdminID() {
		return admin;
	}

	@Override
	public int getDatasourceID() {
		return datasource;
	}

	@Override
	public int getMode() {
		return mode;
	}

	@Override
	public int getDoubleCheck() {
		return doubleCheck;
	}

	@Override
	public int getIgnoreNull() {
		return ignoreNull;
	}

	@Override
	public char getSeparator() {
		return separator;
	}

	@Override
	public String getDelimiter() {
		return delimiter;
	}

	@Override
	public String getKeycolumn() {
		return keycolumn;
	}

	@Override
	public String getCharset() {
		if (charset == null || charset.trim().equals("")) {
			charset = "ISO-8859-1";
		}
		return charset;
	}

	@Override
	public int getRecordsBefore() {
		return recordsBefore;
	}

	@Override
	public int getFields() {
		return fields;
	}

	@Override
	public int getInserted() {
		return inserted;
	}

	@Override
	public int getUpdated() {
		return updated;
	}

	@Override
	public Map<ImportErrorType, Integer> getErrors() {
		return errors;
	}

	@Override
	public Object getError(ImportErrorType importErrorType) {
		Object ret = errors.get(importErrorType);

		if (ret == null) {
			return 0;
		} else {
			return ret;
		}
	}
	
	/**
	 * Getter Method used only within JSPs
	 * @throws Exception
	 */
	@Override
	public Object getError(String idString) {
		try {
			ImportErrorType importErrorType = ImportErrorType.fromString(idString);
			return getError(importErrorType);
		} catch (Exception e) {
			logger.error("Cannot get error data: " + e.getMessage(), e);
			return 0;
		}
	}

	@Override
	public void addError(ImportErrorType importErrorType) {
		Integer old = null;

		old = errors.get(importErrorType);
		if (old != null) {
			errors.put(importErrorType, old.intValue() + 1);
		} else {
			errors.put(importErrorType, 1);
		}
	}

	@Override
	public int getAlreadyInDb() {
		return alreadyInDb;
	}

	@Override
	public void setAlreadyInDb(int alreadyInDb) {
		this.alreadyInDb = alreadyInDb;
	}

	@Override
	public Date getChangeDate() {
		return changeDate;
	}

	@Override
	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public String getFatalError() {
		return fatalError;
	}

	@Override
	public void setFatalError(String fatalError) {
		this.fatalError = fatalError;
	}

	@Override
	public void addError(ImportErrorType importErrorType, int numberOfErrors) {
		Integer old = errors.get(importErrorType);
		if (old != null) {
			errors.put(importErrorType, old + numberOfErrors);
		} else {
			errors.put(importErrorType, numberOfErrors);
		}
	}

	@Override
	public int getCsvLines() {
		return csvLines;
	}

	@Override
	public void setCsvLines(int csvLines) {
		this.csvLines = csvLines;
	}

	@Override
	public int getBlacklisted() {
		return blacklisted;
	}

	@Override
	public void setBlacklisted(int blacklisted) {
		this.blacklisted = blacklisted;
	}

	@Override
	public int getInvalidNullValues() {
		return invalidNullValues;
	}

	@Override
	public void setInvalidNullValues(int invalidNullValues) {
		this.invalidNullValues = invalidNullValues;
	}

	@Override
	public long getFileSize() {
		return fileSize;
	}

	@Override
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	@Override
	public void setDeletedEntries(int deletedEntries) {
		this.deletedEntries = deletedEntries;
	}

	@Override
	public int getDeletedEntries() {
		return deletedEntries;
	}

	@Override
	public void addToFirstErrors(int lineNumber, ReasonCode importErrorType, String columnName) {
		firstErrorLineNumbers.add(lineNumber);
		firstErrorTypes.add(importErrorType.toString());
		firstErrorColumns.add(columnName);
	}

	@Override
	public List<Integer> getFirstErrorLineNumbers() {
		return firstErrorLineNumbers;
	}

	@Override
	public List<String> getFirstErrorTypes() {
		return firstErrorTypes;
	}

	@Override
	public List<String> getFirstErrorColumns() {
		return firstErrorColumns;
	}

	@Override
	public void clearFirstErrors() {
		firstErrorLineNumbers.clear();
		firstErrorTypes.clear();
		firstErrorColumns.clear();
	}

	@Override
	public void setImportedRecipientsCsv(File importedRecipientsCsvFile) {
		this.importedRecipientsCsvFile = importedRecipientsCsvFile;
	}

	@Override
	public File getImportedRecipientsCsv() {
		return importedRecipientsCsvFile;
	}

	@Override
	public void setInvalidRecipientsCsv(File invalidRecipientsCsvFile) {
		this.invalidRecipientsCsvFile = invalidRecipientsCsvFile;
	}

	@Override
	public File getInvalidRecipientsCsv() {
		return invalidRecipientsCsvFile;
	}

	@Override
	public void setFixedByUserRecipientsCsv(File fixedByUserRecipientsCsvFile) {
		this.fixedByUserRecipientsCsvFile = fixedByUserRecipientsCsvFile;
	}

	@Override
	public File getFixedByUserRecipientsCsv() {
		return fixedByUserRecipientsCsvFile;
	}

	@Override
	public void setDuplicateInCsvOrDbRecipientsCsv(File duplicateInCsvOrDbRecipientsCsvFile) {
		this.duplicateInCsvOrDbRecipientsCsvFile = duplicateInCsvOrDbRecipientsCsvFile;
	}

	@Override
	public File getDuplicateInCsvOrDbRecipientsCsv() {
		return duplicateInCsvOrDbRecipientsCsvFile;
	}

	@Override
	public void addErrorColumn(String columnName) {
		errorColumns.add(columnName);
	}

	@Override
	public Set<String> getErrorColumns() {
		return errorColumns;
	}
	
	@Override
	public Map<MediaTypes, Map<Integer, Integer>> getMailinglistStatistics() {
		return mailinglistStatistics;
	}
	
	@Override
	public void setMailinglistStatistics(Map<MediaTypes, Map<Integer, Integer>> mailinglistStatistics) {
		this.mailinglistStatistics = mailinglistStatistics;
	}

	@Override
	public void setNearLimit(boolean nearLimit) {
		this.nearLimit = nearLimit;
	}

	@Override
	public boolean isNearLimit() {
		return nearLimit;
	}

	@Override
	public int getDuplicateInImportData() {
		return duplicatesInImportData;
	}

	@Override
	public void setDuplicatesInImportData(int duplicatesInImportData) {
		this.duplicatesInImportData = duplicatesInImportData;
	}
}
