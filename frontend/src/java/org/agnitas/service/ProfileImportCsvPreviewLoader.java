/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.service.impl.CSVColumnState;
import org.agnitas.service.impl.FieldsFactory;
import org.agnitas.service.impl.ImportWizardContentParseException;
import org.agnitas.util.CaseInsensitiveSet;
import org.agnitas.util.CsvColInfo;
import org.agnitas.util.CsvDataException;
import org.agnitas.util.CsvDataInvalidItemCountException;
import org.agnitas.util.CsvDataInvalidTextAfterQuoteException;
import org.agnitas.util.CsvReader;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.TempFileInputStream;
import org.agnitas.util.ZipDataException;
import org.agnitas.util.ZipUtilities;
import org.agnitas.util.importvalues.Charset;
import org.agnitas.util.importvalues.ImportMode;
import org.agnitas.util.importvalues.Separator;
import org.agnitas.util.importvalues.TextRecognitionChar;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.agnitas.dao.ComRecipientDao;
import com.agnitas.json.Json5Reader;
import com.agnitas.json.JsonObject;
import com.agnitas.json.JsonReader.JsonToken;

public class ProfileImportCsvPreviewLoader {
    @SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(ProfileImportCsvPreviewLoader.class);

	private ComRecipientDao recipientDao;
	
	private ImportProfile importProfile;
	
	private ImportRecipientsDao importRecipientsDao;

	private File importFile;
	
	private CSVColumnState[] columns;

	private FieldsFactory fieldsFactory = new FieldsFactory();
	
	public CSVColumnState[] getColumns() {
		return columns;
	}
	
	public ProfileImportCsvPreviewLoader(ComRecipientDao recipientDao, ImportRecipientsDao importRecipientsDao, ImportProfile importProfile, File importFile) {
		this.recipientDao = recipientDao;
		this.importProfile = importProfile;
		this.importRecipientsDao = importRecipientsDao;
		this.importFile = importFile;
	}
	
	public void validateImportProfileMatchGivenCSVFile() throws ImportWizardContentParseException, ImportException {
		try {
			final List<ColumnMapping> mappingList = importProfile.getColumnMapping();
			if (importProfile.isAutoMapping() && importProfile.isNoHeaders()) {
				throw new ImportWizardContentParseException("error.import.automapping.missing.header");
			} else if (mappingList.isEmpty() && !importProfile.isAutoMapping()) {
				throw new ImportWizardContentParseException("error.import.no_columns_maped");
			}
			if ("CSV".equalsIgnoreCase(importProfile.getDatatype())) {
				char separator = Separator.getSeparatorById(importProfile.getSeparator()).getValueChar();
				Character stringQuote = TextRecognitionChar.getTextRecognitionCharById(importProfile.getTextRecognitionChar()).getValueCharacter();
				
				try (CsvReader csvReader = new CsvReader(getImportInputStream(), Charset.getCharsetById(importProfile.getCharset()).getCharsetName(), separator, stringQuote)) {
					csvReader.setAlwaysTrim(true);
				
					List<String> fileHeaders;
					if (!importProfile.isNoHeaders()) {
						fileHeaders = csvReader.readNextCsvLine();
						
			            // Check for duplicate csv file columns
			            Set<String> csvFileColumnsForDuplicateCheck = new CaseInsensitiveSet();
			            for (String csvColumns : fileHeaders) {
			            	if (StringUtils.isBlank(csvColumns)) {
			            		throw new ImportWizardContentParseException("error.import.column.name.empty");
			            	} else if (csvFileColumnsForDuplicateCheck.contains(csvColumns)) {
			            		throw new ImportWizardContentParseException("error.import.column.csv.duplicate");
			            	}
			            	csvFileColumnsForDuplicateCheck.add(csvColumns);
			            }
					} else {
						fileHeaders = new ArrayList<>();
						int i = 1;
						for (ColumnMapping columnMapping : mappingList) {
							if (!columnMapping.getDatabaseColumn().equals(ColumnMapping.DO_NOT_IMPORT)) {
								fileHeaders.add("column_" + i);
							}
							i++;
						}
					}
						
					for (ColumnMapping columnMapping : mappingList) {
						if (!columnMapping.getDatabaseColumn().equals(ColumnMapping.DO_NOT_IMPORT) && StringUtils.isNotEmpty(columnMapping.getFileColumn()) && !fileHeaders.contains(columnMapping.getFileColumn())) {
							throw new ImportWizardContentParseException("error.import.no_keycolumn_mapping_found_in_file", columnMapping.getFileColumn());
						}
					}
				}
			} else if ("JSON".equalsIgnoreCase(importProfile.getDatatype())) {
				try (Json5Reader jsonReader = new Json5Reader(getImportInputStream(), Charset.getCharsetById(importProfile.getCharset()).getCharsetName())) {
					jsonReader.readNextToken();
					if (jsonReader.getCurrentToken() != JsonToken.JsonArray_Open) {
						throw new Exception("Json data does not contain expected JsonArray");
					}

					// Check for duplicate json property keys
					while (jsonReader.readNextJsonNode()) {
			            Set<String> jsonFilePropertiesForDuplicateCheck = new CaseInsensitiveSet();
						Object currentObject = jsonReader.getCurrentObject();
						if (!(currentObject instanceof JsonObject)) {
							throw new Exception("Json data does not contain expected JsonArray of JsonObjects");
						}
						JsonObject currentJsonObject = (JsonObject) currentObject;
					    for (String jsonPropertyKey : currentJsonObject.keySet()) {
			            	if (StringUtils.isBlank(jsonPropertyKey)) {
			            		throw new ImportWizardContentParseException("error.import.column.name.empty");
			            	} else if (jsonFilePropertiesForDuplicateCheck.contains(jsonPropertyKey)) {
			            		throw new ImportWizardContentParseException("error.import.column.csv.duplicate");
			            	}
			            	jsonFilePropertiesForDuplicateCheck.add(jsonPropertyKey);
					    }
						
						for (ColumnMapping columnMapping : mappingList) {
							if (!columnMapping.getDatabaseColumn().equals(ColumnMapping.DO_NOT_IMPORT) && StringUtils.isNotEmpty(columnMapping.getFileColumn()) && !currentJsonObject.keySet().contains(columnMapping.getFileColumn())) {
								throw new ImportWizardContentParseException("error.import.no_keycolumn_mapping_found_in_file", columnMapping.getFileColumn());
							}
						}
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			throw new ImportWizardContentParseException("error.import.charset", e);
		} catch (ZipDataException e) {
			throw new ImportWizardContentParseException("error.import.zip", e);
		} catch (IOException e) {
			throw new ImportException(false, "error.import.file", e.getMessage(), e);
		} catch (Exception e) {
			if (e instanceof ImportWizardContentParseException) {
				throw (ImportWizardContentParseException) e;
			} else {
				throw new ImportException(false, "error.import.file", e.getMessage(), e);
			}
		}
	}
	
	private InputStream getImportInputStream() throws FileNotFoundException {
		if (importProfile.isZipped()) {
			try {
				if (importProfile.getZipPassword() == null) {
					InputStream dataInputStream = ZipUtilities.openZipInputStream(new FileInputStream(importFile));
					ZipEntry zipEntry = ((ZipInputStream) dataInputStream).getNextEntry();
					if (zipEntry == null) {
						throw new ImportException(false, "error.unzip.noEntry");
					}
					return dataInputStream;
				} else {
					File unzipPath = new File(importFile.getAbsolutePath() + ".unzipped");
					unzipPath.mkdir();
					ZipUtilities.decompressFromEncryptedZipFile(importFile, unzipPath, importProfile.getZipPassword());
					
					// Check if there was only one file within the zip file and use it for import
					String[] filesToImport = unzipPath.list(); // Returns null if no files found
					if (filesToImport == null || filesToImport.length != 1) {
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

	/**
	 * Read the first 20 items of the import data file
	 */
	public List<List<String>> getPreviewParsedContent(ActionMessages errors) throws Exception {
		int lineNumber = 0;
		List<List<String>> previewParsedContent = new LinkedList<>();
		try {
			if ("CSV".equalsIgnoreCase(importProfile.getDatatype())) {
				char separator = Separator.getSeparatorById(importProfile.getSeparator()).getValueChar();
				Character stringQuote = TextRecognitionChar.getTextRecognitionCharById(importProfile.getTextRecognitionChar()).getValueCharacter();
				
				try (CsvReader csvReader = new CsvReader(getImportInputStream(), Charset.getCharsetById(importProfile.getCharset()).getCharsetName(), separator, stringQuote)) {
					csvReader.setAlwaysTrim(true);
					
					columns = null;
					while (lineNumber <= 20) {
						List<String> csvLineData = csvReader.readNextCsvLine();
		
						if (csvLineData == null) {
							break;
						}
						
						lineNumber++;
						// If we haven't been sent the header data yet then we store
						// them (but don't process them)
						if (columns == null) {
							if (!importProfile.isNoHeaders()) {
								columns = new CSVColumnState[csvLineData.size()];
								if (importProfile.isAutoMapping()) {
									CaseInsensitiveMap<String, DbColumnType> customerDbFields = importRecipientsDao.getCustomerDbFields(importProfile.getCompanyId());
									for (int i = 0; i < csvLineData.size(); i++) {
										String headerName = csvLineData.get(i);
										if (StringUtils.isBlank(headerName)) {
											throw new Exception("Invalid empty csvfile header for import automapping");
										} else if (customerDbFields.containsKey(headerName)) {
											columns[i] = new CSVColumnState();
											columns[i].setColName(headerName.toLowerCase());
											columns[i].setImportedColumn(true);
										} else {
											columns[i] = new CSVColumnState();
											columns[i].setColName(headerName);
											columns[i].setImportedColumn(false);
										}
									}
								} else {
									for (int i = 0; i < csvLineData.size(); i++) {
										String headerName = csvLineData.get(i);
										final String columnNameByCvsFileName = fieldsFactory.getDBColumnNameByCsvFileName(headerName, importProfile);
										if (columnNameByCvsFileName != null) {
											columns[i] = new CSVColumnState();
											columns[i].setColName(columnNameByCvsFileName);
											columns[i].setImportedColumn(true);
										} else {
											columns[i] = new CSVColumnState();
											columns[i].setColName(headerName);
											columns[i].setImportedColumn(false);
										}
									}
								}
							} else {
								int csvColumnsExpected = 0;
								for (ColumnMapping columnMapping : importProfile.getColumnMapping()) {
									if (StringUtils.isNotBlank(columnMapping.getFileColumn())) {
										csvColumnsExpected++;
									}
								}
								
								if (csvLineData.size() != csvColumnsExpected) {
									throw new CsvDataException("Number of import file columns does not fit mapped columns", csvReader.getReadCsvLines());
								}
								columns = new CSVColumnState[Math.min(csvLineData.size(), importProfile.getColumnMapping().size())];
								
								for (int i = 0; i < columns.length; i++) {
									final String columnName = importProfile.getColumnMapping().get(i).getDatabaseColumn();
									columns[i] = new CSVColumnState();
									columns[i].setColName("column_" + (i + 1));
									if (columnName != null && !columnName.equals(ColumnMapping.DO_NOT_IMPORT)) {
										columns[i].setImportedColumn(true);
									} else {
										columns[i].setImportedColumn(false);
									}
								}
								
								// Add dummy column names to preview data
								final LinkedList<String> columnsList = new LinkedList<>();
								for (int idx = 0; (idx < columns.length) && (idx < csvLineData.size()); idx++) {
									if (!columns[idx].getImportedColumn()) {
										continue;
									}
									columnsList.add(columns[idx].getColName());
								}
								previewParsedContent.add(columnsList);
							}
							initColumnsNullableCheck(columns);
						}
	
						final LinkedList<String> linelinkedList = new LinkedList<>();
						for (int idx = 0; (idx < columns.length) && (idx < csvLineData.size()); idx++) {
							if (!columns[idx].getImportedColumn()) {
								continue;
							}
							String value = csvLineData.get(idx);
		
							linelinkedList.add(value);
						}
						previewParsedContent.add(linelinkedList);
					}
				}
			} else if ("JSON".equalsIgnoreCase(importProfile.getDatatype())) {
				try (Json5Reader jsonReader = new Json5Reader(getImportInputStream(), Charset.getCharsetById(importProfile.getCharset()).getCharsetName())) {
					jsonReader.readNextToken();
					if (jsonReader.getCurrentToken() != JsonToken.JsonArray_Open) {
						throw new Exception("Json data does not contain expected JsonArray");
					}
					
					columns = null;
					List<CSVColumnState> columnsList = new ArrayList<>();

					while (jsonReader.readNextJsonNode() && lineNumber <= 20) {
						lineNumber++;
						Object currentObject = jsonReader.getCurrentObject();
						if (!(currentObject instanceof JsonObject)) {
							throw new Exception("Json data does not contain expected JsonArray of JsonObjects");
						}
						JsonObject currentJsonObject = (JsonObject) currentObject;
						
						// Check if this jsonObject contains a new property
						for (String propertyKey : currentJsonObject.keySet()) {
							if (StringUtils.isBlank(propertyKey)) {
								throw new Exception("Invalid empty jsonfile property key");
							}
								
							CSVColumnState propertyKeyAlreadyListed = null;
							for (CSVColumnState column : columnsList) {
								if (column.getColName().equals(propertyKey)) {
									propertyKeyAlreadyListed = column;
									break;
								}
							}
							if (propertyKeyAlreadyListed == null) {
								if (importProfile.isAutoMapping()) {
									CaseInsensitiveMap<String, DbColumnType> customerDbFields = importRecipientsDao.getCustomerDbFields(importProfile.getCompanyId());
									if (customerDbFields.containsKey(propertyKey)) {
										columnsList.add(new CSVColumnState(propertyKey, true, -1));
									} else {
										columnsList.add(new CSVColumnState(propertyKey, false, -1));
									}
								} else {
									if (fieldsFactory.getDBColumnNameByCsvFileName(propertyKey, importProfile) != null) {
										columnsList.add(new CSVColumnState(propertyKey, true, -1));
									} else {
										columnsList.add(new CSVColumnState(propertyKey, false, -1));
									}
								}
								
								// Fill all other items, which do not contain a value for this new property, with an empty value for it
								for (List<String> previewParsedContentItem : previewParsedContent) {
									previewParsedContentItem.add("");
								}
							}
						}
						
						// Collect the property values of this jsonObject
						final List<String> contentListItem = new LinkedList<>();
						for (CSVColumnState column : columnsList) {
							if (column.getImportedColumn()) {
								if (currentJsonObject.containsPropertyKey(column.getColName())) {
									contentListItem.add(currentJsonObject.get(column.getColName()).toString());
								} else {
									contentListItem.add("");
								}
							}
						}
						previewParsedContent.add(contentListItem);
					}
					
					// Add headers
					final LinkedList<String> headersList = new LinkedList<>();
					for (CSVColumnState column : columnsList) {
						headersList.add(column.getColName());
					}
					previewParsedContent.add(0, headersList);
					
					columns = columnsList.toArray(new CSVColumnState[0]);
				}
			}
		} catch (CsvDataInvalidItemCountException e) {
	        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.import.data.itemcount", e.getExpected(), e.getActual(), e.getErrorLineNumber()));
		} catch (CsvDataInvalidTextAfterQuoteException e) {
	        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.import.data.invalidTextAfterQuote", e.getErrorLineNumber()));
		} catch (CsvDataException e) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("import.csv_errors_linestructure", e.getMessage() + " in line " + e.getErrorLineNumber()));
		} catch (Exception e) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("import.csv_errors_linestructure", lineNumber));
		}
		return previewParsedContent;
	}

	/**
	 * Method creates message about assigning recipients to mailing lists
	 * according to import mode for displaying in result page and in report
	 * email
	 *
	 * @return mailing list add message
	 */
	public String createMailinglistAddMessage() {
		int importMode = importProfile.getImportMode();
		if (importMode == ImportMode.ADD.getIntValue() ||
			importMode == ImportMode.ADD_AND_UPDATE.getIntValue() ||
			importMode == ImportMode.UPDATE.getIntValue()) {
			return "import.result.subscribersAdded";
		} else if (importMode == ImportMode.MARK_OPT_OUT.getIntValue() ||
			importMode == ImportMode.TO_BLACKLIST.getIntValue()) {
			return "import.result.subscribersUnsubscribed";
		} else if (importMode == ImportMode.MARK_BOUNCED.getIntValue()) {
			return "import.result.subscribersBounced";
		} else if (importMode == ImportMode.MARK_SUSPENDED.getIntValue()) {
			return "import.result.subscribersSuspended";
		} else if (importMode == ImportMode.REACTIVATE_BOUNCED.getIntValue()) {
			return "import.result.bouncedSubscribersReactivated";
		} else if (importMode == ImportMode.REACTIVATE_SUSPENDED.getIntValue()) {
			return "import.result.subscribersReactivated";
		} else {
			return "import.result.subscribersAdded";
		}
	}

	private void initColumnsNullableCheck(CSVColumnState[] cols) {
		Map<String, CsvColInfo> columnsInfo = recipientDao.readDBColumns(importProfile.getCompanyId());
		for (CSVColumnState columnState : cols) {
			CsvColInfo columnInfo = columnsInfo.get(columnState.getColName());
			if (columnInfo != null) {
				columnState.setNullable(columnInfo.isNullable());
			}
		}
	}
}
