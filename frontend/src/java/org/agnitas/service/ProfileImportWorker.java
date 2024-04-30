/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.ImportStatus;
import org.agnitas.beans.impl.ColumnMappingImpl;
import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.emm.core.autoimport.bean.AutoImport;
import org.agnitas.emm.core.autoimport.service.ImportFileStatus;
import org.agnitas.emm.core.autoimport.service.RemoteFile;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.ProfileImportCsvException.ReasonCode;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.CsvDataException;
import org.agnitas.util.CsvDataInvalidItemCountException;
import org.agnitas.util.CsvDataInvalidTextAfterQuoteException;
import org.agnitas.util.CsvReader;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.FileUtils;
import org.agnitas.util.ImportUtils;
import org.agnitas.util.ImportUtils.ImportErrorType;
import org.agnitas.util.TempFileInputStream;
import org.agnitas.util.ZipUtilities;
import org.agnitas.util.importvalues.Charset;
import org.agnitas.util.importvalues.CheckForDuplicates;
import org.agnitas.util.importvalues.DateFormat;
import org.agnitas.util.importvalues.Gender;
import org.agnitas.util.importvalues.ImportMode;
import org.agnitas.util.importvalues.ImportModeHandler;
import org.agnitas.util.importvalues.NullValuesAction;
import org.agnitas.util.importvalues.Separator;
import org.agnitas.util.importvalues.TextRecognitionChar;
import org.agnitas.web.ProfileImportReporter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.dao.ImportProcessActionDao;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.commons.encrypt.ProfileFieldEncryptor;
import com.agnitas.emm.core.commons.validation.AgnitasEmailValidator;
import com.agnitas.emm.core.commons.validation.AgnitasEmailValidatorWithWhitespace;
import com.agnitas.emm.core.commons.validation.EmailValidator;
import com.agnitas.emm.core.imports.beans.ImportItemizedProgress;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.json.Json5Reader;
import com.agnitas.json.JsonObject;
import com.agnitas.json.JsonReader;
import com.agnitas.json.JsonReader.JsonToken;
import com.agnitas.service.ColumnInfoService;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

public class ProfileImportWorker implements Callable<ProfileImportWorker> {
    private static final transient Logger logger = LogManager.getLogger(ProfileImportWorker.class);

	private static final String IMPORT_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "RecipientImport";
	
	/**
	 * Cache variable for the dataSource vendor, so it must not be recalculated everytime.
	 * This variable may be uninitialized before the first execution of the isOracleDB method
	 */
	private static Boolean IS_ORACLE_DB = null;

	private boolean interactiveMode = false;
	private boolean ignoreErrors = false;
	private boolean waitingForInteraction = false;
	private boolean done = true;

	private ConfigService configService;
    private ImportRecipientsDao importRecipientsDao;
    private ColumnInfoService columnInfoService;
    private ImportProcessActionDao importProcessActionDao = null;
    private EmmActionService emmActionService = null;
	private ProfileFieldEncryptor profileFieldEncryptor = null;
	private ProfileImportReporter profileImportReporter;
	private ImportModeHandlerFactory importModeHandlerFactory;

	private RemoteFile importFile;
	private AutoImport autoImport = null;
	private File resultFile;
	private ImportProfile importProfile;
	private SimpleDateFormat importDateFormat = new SimpleDateFormat(DateUtilities.DD_MM_YYYY);
	private TimeZone importTimeZone = TimeZone.getDefault();
	private Admin admin;
	private int datasourceId;
	private boolean normalizeEmails = true;
	private ImportStatus status;
	private List<Integer> mailingListIdsToAssign;
	private String sessionId;
	private int maxGenderValue;
	private Date startTime;
	private Date endTime;

	private long fileSize;
	private int linesInFile;
	private String temporaryImportTableName;
	private String insertIntoTemporaryImportTableSqlString;
	private String importIndexColumn;
	private String duplicateIndexColumn;
	private String temporaryErrorTableName = null;
	private int completedPercent;
    protected Exception error;
	private int duplicatesInCsvData = 0;
	private int duplicatesInDatabase = 0;
	private int blacklistedEmails = 0;
	private Map<MediaTypes, Map<Integer, Integer>> mailinglistAssignStatistics;
	private EmailValidator emailValidator = null;

	/** All available CSV columnnames included in the CSV import file **/
	private List<String> csvFileHeaders;

	/** CSV columnnames that are used for this import **/
	private List<String> importedDataFileColumns;
	
	/** CSV column indexes that are used for this import **/
	private List<Integer> importedCsvFileColumnIndexes;
	
	private List<ColumnMapping> columnMappingsByCsvIndex;
	
	/** DB columnnames that the csv data columns are imported in **/
	private List<String> importedDBColumns;
	
	/** DB columnnames that csv data values and additional values are imported in **/
	private List<String> transferDbColumns;
	
	private int reportID = 0;

	private ImportItemizedProgress currentProgressStatus = ImportItemizedProgress.PREPARING;

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	public void setImportRecipientsDao(ImportRecipientsDao importRecipientsDao) {
		this.importRecipientsDao = importRecipientsDao;
	}
	
	public void setColumnInfoService(ColumnInfoService columnInfoService) {
		this.columnInfoService = columnInfoService;
	}

	public void setImportProcessActionDao(ImportProcessActionDao importProcessActionDao) {
		this.importProcessActionDao = importProcessActionDao;
	}

	public void setEmmActionService(EmmActionService emmActionService) {
		this.emmActionService = emmActionService;
	}

	public void setProfileFieldEncryptor(ProfileFieldEncryptor profileFieldEncryptor) {
		this.profileFieldEncryptor = profileFieldEncryptor;
	}

	public void setProfileImportReporter(ProfileImportReporter profileImportReporter) {
		this.profileImportReporter = profileImportReporter;
	}

	public void setImportModeHandlerFactory(ImportModeHandlerFactory importModeHandlerFactory) {
		this.importModeHandlerFactory = importModeHandlerFactory;
	}

	public String getUsername() {
		return admin.getUsername() + (admin.getSupervisor() != null ? "/" + admin.getSupervisor().getSupervisorName() : "");
	}

	public void setImportFile(RemoteFile importFile) {
		this.importFile = importFile;
	}

	public RemoteFile getImportFile() {
		return importFile;
	}

	public AutoImport getAutoImport() {
		return autoImport;
	}

	public void setAutoImport(AutoImport autoImport) {
		this.autoImport = autoImport;
	}

	public File getResultFile() {
		return resultFile;
	}

	public void setImportProfile(ImportProfile importProfile) throws Exception {
		this.importProfile = importProfile;
		if (importProfile != null) {
			importDateFormat = new SimpleDateFormat(DateFormat.getDateFormatById(importProfile.getDateFormat()).getValue());
			importDateFormat.setLenient(false);
			
			normalizeEmails = !configService.getBooleanValue(ConfigValue.AllowUnnormalizedEmails, importProfile.getCompanyId());
		}
		
		if (importTimeZone != null) {
			importDateFormat.setTimeZone(importTimeZone);
			importDateFormat.setLenient(false);
		}
	}

	public void setAdmin(Admin admin) {
		this.admin = admin;
		if (admin != null) {
			importTimeZone = TimeZone.getTimeZone(admin.getAdminTimezone());
			importDateFormat.setTimeZone(importTimeZone);
			importDateFormat.setLenient(false);
		}
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setDatasourceId(int datasourceId) {
		this.datasourceId = datasourceId;
	}

	public int getDatasourceId() {
		return datasourceId;
	}

	public void setCustomerImportStatus(ImportStatus status) {
		this.status = status;
	}

	public void setMailingListIdsToAssign(List<Integer> mailingListIdsToAssign) {
		this.mailingListIdsToAssign = mailingListIdsToAssign;
	}

    public void setInteractiveMode(boolean interactiveMode) {
		this.interactiveMode = interactiveMode;
	}

	public void setMaxGenderValue(int maxGenderValue) {
		this.maxGenderValue = maxGenderValue;
	}

	public boolean isWaitingForInteraction() {
		return waitingForInteraction;
	}

	public boolean isDone() {
		return done;
	}

	public ImportProfile getImportProfile() {
		return importProfile;
	}

	public int getImportProfileId() {
		if (importProfile == null) {
			return 0;
		}

		return importProfile.getId();
	}

	public ImportStatus getStatus() {
		return status;
	}

	public List<Integer> getMailingListIdsToAssign() {
		return mailingListIdsToAssign;
	}

	public Map<MediaTypes, Map<Integer, Integer>> getMailinglistAssignStatistics() {
		return mailinglistAssignStatistics;
	}

    public Exception getError() {
		return error;
	}

	public boolean isNearLimit() {
		return status.isNearLimit();
	}

	public int getCompletedPercent() {
		return completedPercent;
	}
	
	public String getTemporaryErrorTableName() {
		return temporaryErrorTableName;
	}

	public List<String> getCsvFileHeaders() {
		return csvFileHeaders;
	}

	public List<String> getImportedDataFileColumns() {
		return importedDataFileColumns;
	}

	public void setCompletedPercent(int completedPercent) {
		if (completedPercent > 100) {
			this.completedPercent = 100;
		} else if (completedPercent < 0) {
			this.completedPercent = 0;
		} else {
			this.completedPercent = completedPercent;
		}
	}

	public ImportItemizedProgress getCurrentProgressStatus() {
		return currentProgressStatus;
	}

	@Override
	public ProfileImportWorker call() throws Exception {
		try {
			importFile.setStatus(ImportFileStatus.IMPORTING);
			startTime = new Date();
			done = false;
			waitingForInteraction = false;
			
			if (!ImportUtils.checkIfImportFileHasData(importFile.getLocalFile(), importProfile.getZipPassword())) {
				handleFileIsEmpty();
				return this;
			}

			if (importProfile.isAutoMapping()) {
				prepareAutoMapping();
			}

			if (configService.getBooleanValue(ConfigValue.AllowEmailWithWhitespace, importProfile.getCompanyId())) {
				emailValidator = AgnitasEmailValidatorWithWhitespace.getInstance();
			} else {
				emailValidator = AgnitasEmailValidator.getInstance();
			}

			importValidationCheck();

			if (temporaryImportTableName == null) {
				try {
					// Loading and parsing import data
					prepareImportData();
				} catch (Exception e) {
					setStatusValues();
					endTime = new Date();
					logger.error("Error during profile prepareImportData: " + e.getMessage(), e);
					cleanUp();
					throw e;
				}
			}

			if (error != null) {
				// Some error occurred
				importFile.setStatus(ImportFileStatus.FAILED);
				waitingForInteraction = false;
				endTime = new Date();
				try {
					// Write logs and reports
					resultFile = profileImportReporter.writeProfileImportResultFile(this, importProfile.getCompanyId(), admin == null ? 0 : admin.getAdminID());
					profileImportReporter.sendProfileImportErrorMail(this, importProfile.getCompanyId(), importProfile.getReportLocale(), importProfile.getReportTimezone(), importProfile.getReportDateTimeFormatter());
					reportID = profileImportReporter.writeProfileImportReport(this, importProfile.getCompanyId(), admin == null ? 0 : admin.getAdminID(), importProfile.getReportLocale(), importProfile.getReportTimezone(), importProfile.getReportDateTimeFormatter(), true);
				} catch (Exception e) {
					logger.error("Error during profile importData: " + e.getMessage(), e);
					throw e;
				} finally {
					cleanUp();
				}
			} else if (!interactiveMode || temporaryErrorTableName == null || ignoreErrors || !importRecipientsDao.hasRepairableErrors(temporaryErrorTableName)) {
				waitingForInteraction = false;
				try {
					// Transferring the loaded and maybe corrected data into the live tables
					importData();

					endTime = new Date();

					// Check if any valid data was imported
					if ((ImportMode.getFromInt(importProfile.getImportMode()) == ImportMode.ADD || ImportMode.getFromInt(importProfile.getImportMode()) == ImportMode.ADD_AND_UPDATE || ImportMode.getFromInt(importProfile.getImportMode()) == ImportMode.UPDATE)
							&& ((importProfile.isNoHeaders() && linesInFile > 0) || (!importProfile.isNoHeaders() && linesInFile > 1))
							&& status.getInserted() == 0
							&& status.getUpdated() == 0
							&& status.getErrors().size() > 0) {
						error = new ImportException(false, "error.import.data.invalid");
						status.setFatalError("All import data was invalid");
					}


					// Write logs and reports
					currentProgressStatus = ImportItemizedProgress.REPORTING_TO_RESULT_FILE;
					resultFile = profileImportReporter.writeProfileImportResultFile(this, importProfile.getCompanyId(), admin == null ? 0 : admin.getAdminID());


					// Create downloadable report files
					currentProgressStatus = ImportItemizedProgress.REPORTING_SUCCESS_RECIPIENTS;
					status.setImportedRecipientsCsv(createImportedRecipients(getImportFile().getLocalFile().getName() + "_valid_recipients", "CSV".equalsIgnoreCase(importProfile.getDatatype())));
					currentProgressStatus = ImportItemizedProgress.REPORTING_INVALID_RECIPIENTS;
					status.setInvalidRecipientsCsv(createInvalidRecipients(getImportFile().getLocalFile().getName() + "_invalid_recipients", importedDataFileColumns, "CSV".equalsIgnoreCase(importProfile.getDatatype())));
					currentProgressStatus = ImportItemizedProgress.REPORTING_FIXED_RECIPIENTS;
					status.setFixedByUserRecipientsCsv(createFixedByUserRecipients(getImportFile().getLocalFile().getName() + "_fixed_recipients", importedDataFileColumns, "CSV".equalsIgnoreCase(importProfile.getDatatype())));
					currentProgressStatus = ImportItemizedProgress.REPORTING_DUPLICATED_RECIPIENTS;
					status.setDuplicateInCsvOrDbRecipientsCsv(createDuplicateInCsvOrDbRecipients(getImportFile().getLocalFile().getName() + "_duplicate_recipients", "CSV".equalsIgnoreCase(importProfile.getDatatype())));

					currentProgressStatus = ImportItemizedProgress.SENDING_REPORT_MAIL;
					profileImportReporter.sendProfileImportReportMail(this, importProfile.getCompanyId(), importProfile.getReportLocale(), importProfile.getReportTimezone(), importProfile.getReportDateTimeFormatter());
					reportID = profileImportReporter.writeProfileImportReport(this, importProfile.getCompanyId(), admin == null ? 0 : admin.getAdminID(), importProfile.getReportLocale(), importProfile.getReportTimezone(), importProfile.getReportDateTimeFormatter(), false);
				} finally {
					cleanUp();
				}
			} else {
				waitingForInteraction = true;
			}
		} catch (DataAccessException | SQLException e) {
			logger.error("Error during profile importData: " + e.getMessage(), e);
			status.setFatalError("Internal db error");
        	error = e;

			if (endTime == null) {
				endTime = new Date();
	        }
			importFile.setStatus(ImportFileStatus.FAILED);
	        resultFile = profileImportReporter.writeProfileImportResultFile(this, importProfile.getCompanyId(), admin == null ? 0 : admin.getAdminID());
	        profileImportReporter.sendProfileImportErrorMail(this, importProfile.getCompanyId(), importProfile.getReportLocale(), importProfile.getReportTimezone(), importProfile.getReportDateTimeFormatter());
	        reportID = profileImportReporter.writeProfileImportReport(this, importProfile.getCompanyId(), admin == null ? 0 : admin.getAdminID(), importProfile.getReportLocale(), importProfile.getReportTimezone(), importProfile.getReportDateTimeFormatter(), true);
			waitingForInteraction = false;
		} catch (CsvDataInvalidItemCountException e) {
			logger.error("Error during profile importData: " + e.getMessage(), e);
			status.setFatalError(e.getMessage() + " in line " + e.getErrorLineNumber());
        	error = new ImportException(false, "error.import.data.itemcount", e.getExpected(), e.getActual(), e.getErrorLineNumber());

			if (endTime == null) {
				endTime = new Date();
	        }
			importFile.setStatus(ImportFileStatus.FAILED);
	        resultFile = profileImportReporter.writeProfileImportResultFile(this, importProfile.getCompanyId(), admin == null ? 0 : admin.getAdminID());
	        profileImportReporter.sendProfileImportErrorMail(this, importProfile.getCompanyId(), importProfile.getReportLocale(), importProfile.getReportTimezone(), importProfile.getReportDateTimeFormatter());
	        reportID = profileImportReporter.writeProfileImportReport(this, importProfile.getCompanyId(), admin == null ? 0 : admin.getAdminID(), importProfile.getReportLocale(), importProfile.getReportTimezone(), importProfile.getReportDateTimeFormatter(), true);
			waitingForInteraction = false;
		} catch (CsvDataInvalidTextAfterQuoteException e) {
			logger.error("Error during profile importData: " + e.getMessage(), e);
			status.setFatalError(e.getMessage() + " in line " + e.getErrorLineNumber());
        	error = new ImportException(false, "error.import.data.invalidTextAfterQuote", e.getErrorLineNumber());

			if (endTime == null) {
				endTime = new Date();
	        }
			importFile.setStatus(ImportFileStatus.FAILED);
	        resultFile = profileImportReporter.writeProfileImportResultFile(this, importProfile.getCompanyId(), admin == null ? 0 : admin.getAdminID());
	        profileImportReporter.sendProfileImportErrorMail(this, importProfile.getCompanyId(), importProfile.getReportLocale(), importProfile.getReportTimezone(), importProfile.getReportDateTimeFormatter());
	        reportID = profileImportReporter.writeProfileImportReport(this, importProfile.getCompanyId(), admin == null ? 0 : admin.getAdminID(), importProfile.getReportLocale(), importProfile.getReportTimezone(), importProfile.getReportDateTimeFormatter(), true);
			waitingForInteraction = false;
		} catch (CsvDataException e) {
			logger.error("Error during profile importData: " + e.getMessage(), e);
			status.setFatalError(e.getMessage() + " in line " + e.getErrorLineNumber());
        	error = new ImportException(false, "import.csv_errors_linestructure", e.getMessage() + " in line " + e.getErrorLineNumber());

			if (endTime == null) {
				endTime = new Date();
	        }
			importFile.setStatus(ImportFileStatus.FAILED);
	        resultFile = profileImportReporter.writeProfileImportResultFile(this, importProfile.getCompanyId(), admin == null ? 0 : admin.getAdminID());
	        profileImportReporter.sendProfileImportErrorMail(this, importProfile.getCompanyId(), importProfile.getReportLocale(), importProfile.getReportTimezone(), importProfile.getReportDateTimeFormatter());
	        reportID = profileImportReporter.writeProfileImportReport(this, importProfile.getCompanyId(), admin == null ? 0 : admin.getAdminID(), importProfile.getReportLocale(), importProfile.getReportTimezone(), importProfile.getReportDateTimeFormatter(), true);
			waitingForInteraction = false;
		} catch (Exception e) {
			logger.error("Error during profile importData: " + e.getMessage(), e);
			status.setFatalError(e.getMessage());
        	error = e;

			if (endTime == null) {
				endTime = new Date();
	        }
			importFile.setStatus(ImportFileStatus.FAILED);
	        resultFile = profileImportReporter.writeProfileImportResultFile(this, importProfile.getCompanyId(), admin == null ? 0 : admin.getAdminID());
	        profileImportReporter.sendProfileImportErrorMail(this, importProfile.getCompanyId(), importProfile.getReportLocale(), importProfile.getReportTimezone(), importProfile.getReportDateTimeFormatter());
	        reportID = profileImportReporter.writeProfileImportReport(this, importProfile.getCompanyId(), admin == null ? 0 : admin.getAdminID(), importProfile.getReportLocale(), importProfile.getReportTimezone(), importProfile.getReportDateTimeFormatter(), true);
			waitingForInteraction = false;
		} finally {
	        done = true;
		}
        importFile.setStatus(ImportFileStatus.IMPORTED);
        return this;
    }

	private void prepareAutoMapping() throws Exception {
		if (importProfile.isNoHeaders()) {
			throw new ImportException(false, "error.import.automapping.missing.header");
		}
		
		char separator = Separator.getSeparatorById(importProfile.getSeparator()).getValueChar();
		Character stringQuote = TextRecognitionChar.getTextRecognitionCharById(importProfile.getTextRecognitionChar()).getValueCharacter();

		CaseInsensitiveMap<String, DbColumnType> customerDbFields = importRecipientsDao.getCustomerDbFields(importProfile.getCompanyId());
		List<ColumnMapping> autoMapping = new ArrayList<>();
		if ("CSV".equalsIgnoreCase(importProfile.getDatatype())) {
			try (CsvReader csvReader = new CsvReader(getImportInputStream(), Charset.getCharsetById(importProfile.getCharset()).getCharsetName(), separator, stringQuote)) {
				csvReader.setAlwaysTrim(true);
				List<String> csvHeaders = csvReader.readNextCsvLine();
				for (String csvHeader : csvHeaders) {
					if (StringUtils.isBlank(csvHeader)) {
						throw new Exception("Invalid empty csvfile header for import automapping");
					} else if (customerDbFields.containsKey(csvHeader)) {
						ColumnMapping columnMapping = new ColumnMappingImpl();
						columnMapping.setFileColumn(csvHeader);
						columnMapping.setDatabaseColumn(csvHeader.toLowerCase());
						autoMapping.add(columnMapping);
					}
				}
			}
		} else {
			try (Json5Reader jsonReader = new Json5Reader(getImportInputStream(), Charset.getCharsetById(importProfile.getCharset()).getCharsetName())) {
				jsonReader.readNextToken();
				
				while (jsonReader.getCurrentToken() != null && jsonReader.getCurrentToken() != JsonToken.JsonArray_Open) {
					jsonReader.readNextToken();
				}
				
				if (jsonReader.getCurrentToken() != JsonToken.JsonArray_Open) {
					throw new Exception("Json data does not contain expected JsonArray");
				}
				
				Set<String> jsonObjectAttributes = new HashSet<>();
				while (jsonReader.readNextJsonNode()) {
					Object currentObject = jsonReader.getCurrentObject();
					if (currentObject == null || !(currentObject instanceof JsonObject)) {
						throw new Exception("Json data does not contain expected JsonArray of JsonObjects");
					}
					jsonObjectAttributes.addAll(((JsonObject) currentObject).keySet());
				}
				
				for (String jsonObjectAttribute : jsonObjectAttributes) {
					if (StringUtils.isBlank(jsonObjectAttribute)) {
						throw new Exception("Invalid empty json attribute for import");
					} else if (customerDbFields.containsKey(jsonObjectAttribute)) {
						ColumnMapping columnMapping = new ColumnMappingImpl();
						columnMapping.setFileColumn(jsonObjectAttribute);
						columnMapping.setDatabaseColumn(jsonObjectAttribute.toLowerCase());
						autoMapping.add(columnMapping);
					}
				}
			}
		}
		importProfile.setColumnMapping(autoMapping);
	}
	
	private InputStream getImportInputStream() throws Exception {
		if (AgnUtils.isZipArchiveFile(importFile.getLocalFile())) {
			try {
				if (importProfile.getZipPassword() == null) {
					InputStream dataInputStream = ZipUtilities.openSingleFileZipInputStream(importFile.getLocalFile());
					if (dataInputStream == null) {
						throw new ImportException(false, "error.unzip.noEntry");
					} else {
						return dataInputStream;
					}
				} else {
					File tempImportFile = new File(importFile.getLocalFile().getAbsolutePath() + ".tmp");
					try (ZipFile zipFile = new ZipFile(importFile.getLocalFile())) {
						zipFile.setPassword(importProfile.getZipPassword().toCharArray());
						List<FileHeader> fileHeaders = zipFile.getFileHeaders();
						// Check if there is only one file within the zip file
						if (fileHeaders == null || fileHeaders.size() != 1) {
							throw new Exception("Invalid number of files included in zip file");
						} else {
							try (FileOutputStream tempImportFileOutputStream = new FileOutputStream(tempImportFile)) {
								try(final InputStream zipInput = zipFile.getInputStream(fileHeaders.get(0))) {
									IOUtils.copy(zipInput, tempImportFileOutputStream);
								}
							}
							return new TempFileInputStream(tempImportFile);
						}
					}
				}
			} catch (ImportException e) {
				throw e;
			} catch (Exception e) {
				throw new ImportException(false, "error.unzip", e.getMessage());
			}
		} else {
			return new FileInputStream(importFile.getLocalFile());
		}
	}

	private void importValidationCheck() throws Exception {
		CaseInsensitiveMap<String, ProfileField> profilefields = columnInfoService.getColumnInfoMap(importProfile.getCompanyId(), admin.getAdminID());
		
		Set<String> mappedDbColumns = new HashSet<>();
		for (ColumnMapping mapping : importProfile.getColumnMapping()) {
			if (!ColumnMapping.DO_NOT_IMPORT.equals(mapping.getDatabaseColumn())) {
				if (!profilefields.containsKey(mapping.getDatabaseColumn())) {
					throw new ImportException(false, "error.import.dbColumnUnknown", mapping.getDatabaseColumn());
				} else if (profilefields.get(mapping.getDatabaseColumn()).getModeEdit() == ProfileFieldMode.NotVisible) {
					throw new ImportException(false, "error.import.dbColumn.invisible", mapping.getDatabaseColumn());
				} else if (profilefields.get(mapping.getDatabaseColumn()).getModeEdit() == ProfileFieldMode.ReadOnly && !importProfile.getKeyColumns().contains(mapping.getDatabaseColumn())) {
					throw new ImportException(false, "error.import.dbColumnNotEditable", mapping.getDatabaseColumn());
				} else if (!mappedDbColumns.add(mapping.getDatabaseColumn())) {
					throw new ImportException(false, "error.import.dbColumnMappedMultiple", mapping.getDatabaseColumn());
				}
			}
		}

		ImportModeHandler importModeHandler = importModeHandlerFactory.getImportModeHandler(ImportMode.getFromInt(importProfile.getImportMode()).getImportModeHandlerName());
		
		List<String> columnsToCheck = importProfile.getKeyColumns();
		if (CollectionUtils.isNotEmpty(columnsToCheck)) {
			if (!importRecipientsDao.isKeyColumnIndexed(importProfile.getCompanyId(), columnsToCheck)) {
				int unindexedLimit = configService.getIntegerValue(ConfigValue.MaximumContentLinesForUnindexedImport, importProfile.getCompanyId());
				if (unindexedLimit >= 0 && importRecipientsDao.getResultEntriesCount("SELECT COUNT(*) FROM customer_" + importProfile.getCompanyId() + "_tbl") > unindexedLimit) {
					throw new ImportException(false, "error.import.keyColumn.index");
				}
			}
		}
		
		importModeHandler.checkPreconditions(importProfile);
	}

	private void importData() throws Exception {
		ImportModeHandler importModeHandler = importModeHandlerFactory.getImportModeHandler(ImportMode.getFromInt(importProfile.getImportMode()).getImportModeHandlerName());

		currentProgressStatus = ImportItemizedProgress.HANDLING_BLACKLIST;
		// Remove blacklisted entries (global blacklist is ignored to allow import of global blacklisted entries to companies blacklist)
		blacklistedEmails = importModeHandler.handleBlacklist(importProfile, temporaryImportTableName);
		
		// Create synchronization infos
		if (importProfile.getKeyColumns() != null && !importProfile.getKeyColumns().isEmpty() && importProfile.getCheckForDuplicates() == CheckForDuplicates.COMPLETE.getIntValue()) {
			currentProgressStatus = ImportItemizedProgress.HANDLING_BLACKLIST;
			duplicatesInCsvData = importRecipientsDao.markDuplicatesEntriesSingleTable(importProfile.getCompanyId(), temporaryImportTableName, importProfile.getKeyColumns(), importIndexColumn, duplicateIndexColumn);
			duplicatesInDatabase = importRecipientsDao.markDuplicatesEntriesCrossTable(importProfile.getCompanyId(), temporaryImportTableName, "customer_" + importProfile.getCompanyId() + "_tbl", importProfile.getKeyColumns(), "customer_id");
		}
		
		// Execute ImportProcessAction
		if (importProcessActionDao != null && importProfile.getImportProcessActionID() > 0) {
			try {
				currentProgressStatus = ImportItemizedProgress.EXECUTING_IMPORT_ACTION;
				importProcessActionDao.executeImportProcessAction(admin.getCompanyID(), importProfile.getImportProcessActionID(), temporaryImportTableName, mailingListIdsToAssign);
			} catch (Exception e) {
				logger.error("Error in ImportProcessAction: " + e.getMessage());
				status.addError(ImportErrorType.STRUCTURE_ERROR);
				throw e;
			}
		}

		currentProgressStatus = ImportItemizedProgress.HANDLING_NEW_RECIPIENTS;
		importModeHandler.handleNewCustomers(status, importProfile, temporaryImportTableName, duplicateIndexColumn, transferDbColumns, datasourceId);

		currentProgressStatus = ImportItemizedProgress.HANDLING_EXISTING_RECIPIENTS;
		importModeHandler.handleExistingCustomers(status, importProfile, temporaryImportTableName, importIndexColumn, transferDbColumns, datasourceId);

		currentProgressStatus = ImportItemizedProgress.HANDLING_POSTPROCESSING;
		mailinglistAssignStatistics = importModeHandler.handlePostProcessing(emmActionService, status, importProfile, temporaryImportTableName, datasourceId, mailingListIdsToAssign, importProfile.getMediatypes());

		setStatusValues();
	}

	private void prepareImportData() throws Exception {
		currentProgressStatus = ImportItemizedProgress.PREPARING;
		// Create temporary import table
		String description = "Import by " + getUsername();
		temporaryImportTableName = importRecipientsDao.createTemporaryCustomerImportTable(admin.getCompanyID(), "customer_" + importProfile.getCompanyId() + "_tbl", admin.getAdminID(), datasourceId, importProfile.getKeyColumns(), sessionId, description);
		importIndexColumn = importRecipientsDao.addIndexedIntegerColumn(importProfile.getCompanyId(), temporaryImportTableName, "csvindex", temporaryImportTableName + "csvix");
		duplicateIndexColumn = importRecipientsDao.addIndexedIntegerColumn(importProfile.getCompanyId(), temporaryImportTableName, "dbl", temporaryImportTableName + "dblix");
		
		if (!isOracleDB() && (ImportMode.getFromInt(importProfile.getImportMode()) == ImportMode.BLACKLIST_EXCLUSIVE || ImportMode.getFromInt(importProfile.getImportMode()) == ImportMode.TO_BLACKLIST)) {
			// Change collation of email column for blacklist import on mysql and mariadb
			importRecipientsDao.changeEmailColumnCollation(temporaryImportTableName, "utf8mb4_bin");
		}
		
		fileSize = importFile.getLocalFile().length();
				
		// This counts csv lines (see also: escaped linebreaks in csv format) for the progressbar 100% value
		char separator = Separator.getSeparatorById(importProfile.getSeparator()).getValueChar();
		Character stringQuote = TextRecognitionChar.getTextRecognitionCharById(importProfile.getTextRecognitionChar()).getValueCharacter();
		
		if ("CSV".equalsIgnoreCase(importProfile.getDatatype())) {
			try (CsvReader csvReader = new CsvReader(getImportInputStream(), Charset.getCharsetById(importProfile.getCharset()).getCharsetName(), separator, stringQuote)) {
				csvReader.setAlwaysTrim(true);
				linesInFile = csvReader.getCsvLineCount();
				if (!importProfile.isNoHeaders()) {
					linesInFile = linesInFile - 1;
				}
			}
		} else if ("JSON".equalsIgnoreCase(importProfile.getDatatype())) {
			try (Json5Reader jsonReader = new Json5Reader(getImportInputStream(), Charset.getCharsetById(importProfile.getCharset()).getCharsetName())) {
				jsonReader.readNextToken();
				
				while (jsonReader.getCurrentToken() != null && jsonReader.getCurrentToken() != JsonToken.JsonArray_Open) {
					jsonReader.readNextToken();
				}
				
				if (jsonReader.getCurrentToken() != JsonToken.JsonArray_Open) {
					throw new Exception("Json data does not contain expected JsonArray");
				}
				
				int itemCount = 0;
				while (jsonReader.readNextJsonNode()) {
					Object currentObject = jsonReader.getCurrentObject();
					if (currentObject == null || !(currentObject instanceof JsonObject)) {
						throw new Exception("Json data does not contain expected JsonArray of JsonObjects");
					}
					itemCount++;
				}
				
				linesInFile = itemCount;
			}
		} else {
			throw new RuntimeException("Invalid datatype: " + importProfile.getDatatype());
		}

		int maximumNumberOfRowsInImportFile = configService.getIntegerValue(ConfigValue.ProfileRecipientImportMaxRows, admin.getCompanyID());
		if (maximumNumberOfRowsInImportFile > 0 && linesInFile > maximumNumberOfRowsInImportFile) {
			throw new ImportException(false, "error.import.maxlinesexceeded", linesInFile, maximumNumberOfRowsInImportFile);
		}
		
		// Import csv data in temporary import table and set the correct number of lines found
		try {
			if ("CSV".equalsIgnoreCase(importProfile.getDatatype())) {
				linesInFile = importCsvDataInTemporaryTable();
			} else {
				linesInFile = importJsonDataInTemporaryTable();
			}
		} catch (Exception e) {
			logger.error("Error in structure: " + e.getMessage());
			status.addError(ImportErrorType.STRUCTURE_ERROR);
			throw e;
		}
		
		importRecipientsDao.gatherTableStats(temporaryImportTableName);
		
		setStatusValues();
	}

	private void setStatusValues() {
		status.setFileSize(fileSize);
		status.setCsvLines(linesInFile);
		
		if (temporaryErrorTableName != null) {
			status.setErrors(importRecipientsDao.getReasonStatistics(temporaryErrorTableName));
		} else {
			status.setErrors(new HashMap<>());
		}
		status.addError(ImportErrorType.BLACKLIST_ERROR, blacklistedEmails);
		status.setDoubleCheck(duplicatesInCsvData);
		status.setAlreadyInDb(duplicatesInDatabase);
		status.setFields(columnMappingsByCsvIndex != null ? columnMappingsByCsvIndex.size() : 0);
		status.setDatasourceID(datasourceId);

		// Collect error status statistics
		if (temporaryErrorTableName != null) {
			String csvFieldForEmail = null;
			String csvFieldForMailType = null;
			String csvFieldForGender = null;
			for (ColumnMapping columnMapping : importProfile.getColumnMapping()) {
				if ("email".equalsIgnoreCase(columnMapping.getDatabaseColumn())) {
					csvFieldForEmail = columnMapping.getFileColumn();
				} else if ("mailtype".equalsIgnoreCase(columnMapping.getDatabaseColumn())) {
					csvFieldForMailType = columnMapping.getFileColumn();
				} else if ("gender".equalsIgnoreCase(columnMapping.getDatabaseColumn())) {
					csvFieldForGender = columnMapping.getFileColumn();
				}
			}
			
			status.setError(ImportErrorType.EMAIL_ERROR, importRecipientsDao.getResultEntriesCount(
				"SELECT COUNT(*) FROM " + temporaryErrorTableName + " WHERE reason = '" + ReasonCode.InvalidEmail.toString() + "' AND errorfixed = 0"
			+ (csvFieldForEmail == null ? "" : " OR errorfield = '" + csvFieldForEmail + "'")));
			
			status.setError(ImportErrorType.MAILTYPE_ERROR, importRecipientsDao.getResultEntriesCount(
				"SELECT COUNT(*) FROM " + temporaryErrorTableName + " WHERE reason = '" + ReasonCode.InvalidMailtype.toString() + "' AND errorfixed = 0"
			+ (csvFieldForMailType == null ? "" : " OR errorfield = '" + csvFieldForMailType + "'")));
			
			status.setError(ImportErrorType.GENDER_ERROR, importRecipientsDao.getResultEntriesCount(
				"SELECT COUNT(*) FROM " + temporaryErrorTableName + " WHERE reason = '" + ReasonCode.InvalidGender.toString() + "' AND errorfixed = 0"
			+ (csvFieldForGender == null ? "" : " OR errorfield = '" + csvFieldForGender + "'")));
			
			status.setError(ImportErrorType.NUMERIC_ERROR, importRecipientsDao.getResultEntriesCount(
				"SELECT COUNT(*) FROM " + temporaryErrorTableName + " WHERE reason = '" + ReasonCode.InvalidNumber.toString() + "' AND errorfixed = 0"));
			status.setError(ImportErrorType.DATE_ERROR, importRecipientsDao.getResultEntriesCount(
				"SELECT COUNT(*) FROM " + temporaryErrorTableName + " WHERE reason = '" + ReasonCode.InvalidDate.toString() + "' AND errorfixed = 0"));
			status.setError(ImportErrorType.ENCRYPTION_ERROR, importRecipientsDao.getResultEntriesCount(
				"SELECT COUNT(*) FROM " + temporaryErrorTableName + " WHERE reason = '" + ReasonCode.InvalidEncryption.toString() + "' AND errorfixed = 0"));
			status.setError(ImportErrorType.VALUE_TOO_LARGE_ERROR, importRecipientsDao.getResultEntriesCount(
				"SELECT COUNT(*) FROM " + temporaryErrorTableName + " WHERE reason = '" + ReasonCode.ValueTooLarge.toString() + "' AND errorfixed = 0"));
			status.setError(ImportErrorType.NUMBER_TOO_LARGE_ERROR, importRecipientsDao.getResultEntriesCount(
				"SELECT COUNT(*) FROM " + temporaryErrorTableName + " WHERE reason = '" + ReasonCode.NumberTooLarge.toString() + "' AND errorfixed = 0"));
			status.setError(ImportErrorType.DBINSERT_ERROR, importRecipientsDao.getResultEntriesCount(
				"SELECT COUNT(*) FROM " + temporaryErrorTableName + " WHERE reason IS NULL AND errorfixed = 0"));
		} else {
			status.setError(ImportErrorType.EMAIL_ERROR, 0);
			status.setError(ImportErrorType.MAILTYPE_ERROR, 0);
			status.setError(ImportErrorType.GENDER_ERROR, 0);
			status.setError(ImportErrorType.NUMERIC_ERROR, 0);
			status.setError(ImportErrorType.DATE_ERROR, 0);
			status.setError(ImportErrorType.ENCRYPTION_ERROR, 0);
			status.setError(ImportErrorType.VALUE_TOO_LARGE_ERROR, 0);
			status.setError(ImportErrorType.DBINSERT_ERROR, 0);
		}
		
		if (temporaryImportTableName != null) {
			status.setError(ImportErrorType.KEYDOUBLE_ERROR, importRecipientsDao.getResultEntriesCount(
				"SELECT COUNT(*) FROM " + temporaryImportTableName + " WHERE " + duplicateIndexColumn + " IS NOT NULL"));
		}
		
		status.setError(ImportErrorType.BLACKLIST_ERROR, blacklistedEmails);
		
		status.setMailinglistStatistics(mailinglistAssignStatistics);
	}

	private void createTemporaryErrorTable(int amountOfDataFields) throws Exception {
		if (temporaryErrorTableName == null) {
			// Create temporary import error table
			List<String> columns = new ArrayList<>();
			for (int i = 1; i <= amountOfDataFields; i++) {
				columns.add("data_" + i);
			}
			temporaryErrorTableName = importRecipientsDao.createTemporaryCustomerErrorTable(admin.getCompanyID(), admin.getAdminID(), datasourceId, columns, sessionId);
		}
	}

	private final int importCsvDataInTemporaryTable() throws Exception {
		setCompletedPercent(0);
		currentProgressStatus = ImportItemizedProgress.IMPORTING_DATA_TO_TMP_TABLE;
		
		try (final Connection connection = importRecipientsDao.getDataSource().getConnection()) {
			final boolean previousAutoCommit = connection.getAutoCommit();
			
			try {
				connection.setAutoCommit(false);

				try (InputStream inputStream = getImportInputStream()) {
					return importCsvDataInTemporaryTable(inputStream, connection);
				} finally {
					connection.commit();
				}
			} finally {
				connection.setAutoCommit(previousAutoCommit);
			}
		}
	}
	
	private final int importCsvDataInTemporaryTable(final InputStream stream, final Connection connection) throws Exception {
		final char separator = Separator.getSeparatorById(importProfile.getSeparator()).getValueChar();
		final Character stringQuote = TextRecognitionChar.getTextRecognitionCharById(importProfile.getTextRecognitionChar()).getValueCharacter();
		
		try (CsvReader csvReader = new CsvReader(stream, Charset.getCharsetById(importProfile.getCharset()).getCharsetName(), separator, stringQuote)) {
			csvReader.setAlwaysTrim(true);

			final List<String> additionalDbColumns = new ArrayList<>();
			final List<String> additionalDbValues = new ArrayList<>();

			processCsvHeaderData(csvReader, additionalDbColumns, additionalDbValues);
			
			// Check if keycolumns are part of the imported data
			for (String keyColumnName : importProfile.getKeyColumns()) {
				if (!importedDBColumns.contains(keyColumnName.toLowerCase())) {
					throw new ImportException(false, "error.import.missing.keyColumn", keyColumnName);
				}
			}
			
			checkCsvHeaderForRestrictedColumns(additionalDbColumns);
			checkCsvHeaderForDuplicates();
			csvDetermineDbColumnsToTransfer(additionalDbColumns);
			return importCsvDataInTemporaryTable(csvReader, connection, additionalDbColumns, additionalDbValues);
		}
	}
	
	private final void processCsvHeaderData(final CsvReader csvReader, final List<String> additionalDbColumns, final List<String> additionalDbValues) throws Exception {
		if (importProfile.isNoHeaders()) {
			// CSV file has no column headers in first line
			
			importedDataFileColumns = new ArrayList<>();
			
			// Contains the index within the csv data entries for each sql parameter
			importedCsvFileColumnIndexes = new ArrayList<>();
			
			importedDBColumns = new ArrayList<>();
			
			csvFileHeaders = new ArrayList<>();
			
			for (ColumnMapping columnMapping : importProfile.getColumnMapping()) {
				csvFileHeaders.add(columnMapping.getFileColumn());
				if (StringUtils.isNotBlank(columnMapping.getDatabaseColumn()) && !ColumnMapping.DO_NOT_IMPORT.equalsIgnoreCase(columnMapping.getDatabaseColumn())) {
					if (StringUtils.isNotBlank(columnMapping.getFileColumn())) {
						importedDBColumns.add(columnMapping.getDatabaseColumn().toLowerCase());
						if (!columnMapping.getFileColumn().startsWith("column_") || !AgnUtils.isDigit(columnMapping.getFileColumn().substring(7))) {
							throw new Exception("Invalid csv mapping column: " + columnMapping.getFileColumn());
						}
						int csvIndex = Integer.parseInt(columnMapping.getFileColumn().substring(7)) - 1;
						if (csvIndex < 0) {
							throw new Exception("Invalid csv mapping column: " + columnMapping.getFileColumn());
						}
						importedDataFileColumns.add(columnMapping.getFileColumn());
						importedCsvFileColumnIndexes.add(csvIndex);
					} else {
                        tryImportIntoDbColumnWithoutDataInCsvFile(additionalDbColumns, additionalDbValues, columnMapping);
					}
				}
			}
		} else {
			final List<String> headerData = csvReader.readNextCsvLine();
			if (headerData == null) {
				throw new ImportException(true, "error.import.no_file");
			} else if (headerData.isEmpty()) {
				throw new ImportException(false, "error.invalid.csvfile.noheaders");
			}
			
			// First line contains the csv headers
			csvFileHeaders = AgnUtils.makeListTrim(headerData);
			
			importedDataFileColumns = new ArrayList<>();
			// Contains the index within the csv data entries for each sql parameter
			importedCsvFileColumnIndexes = new ArrayList<>();
			
			importedDBColumns = new ArrayList<>();
			for (ColumnMapping columnMapping : importProfile.getColumnMapping()) {
				if (StringUtils.isNotBlank(columnMapping.getDatabaseColumn()) && !ColumnMapping.DO_NOT_IMPORT.equalsIgnoreCase(columnMapping.getDatabaseColumn())) {
					if (StringUtils.isNotBlank(columnMapping.getFileColumn())) {
						importedDBColumns.add(columnMapping.getDatabaseColumn().toLowerCase());
						int csvIndex = csvFileHeaders.indexOf(columnMapping.getFileColumn());
						if (csvIndex < 0) {
							throw new CsvDataException("CSV file doesn't contain expected column (is headerline missing?): " + columnMapping.getFileColumn(), 1);
						}
						importedDataFileColumns.add(columnMapping.getFileColumn());
						importedCsvFileColumnIndexes.add(csvIndex);
					} else {
                        tryImportIntoDbColumnWithoutDataInCsvFile(additionalDbColumns, additionalDbValues, columnMapping);
					}
				}
			}
		}

	}
	
    private void tryImportIntoDbColumnWithoutDataInCsvFile(List<String> additionalDbColumns, List<String> additionalDbValues, ColumnMapping columnMapping) {
        String databaseColumn = columnMapping.getDatabaseColumn();
        try {
            additionalDbColumns.add(databaseColumn);
            additionalDbValues.add(getDefValForDB(columnMapping));
        } catch (Exception e) {
            throw new ImportException(false, "error.import.invalidDataForField", databaseColumn);
        }
    }
    
    private String getDefValForDB(ColumnMapping columnMapping) throws Exception {
    	SimpleDataType dataType = DbUtilities.getColumnDataType(importRecipientsDao.getDataSource(), "customer_" + importProfile.getCompanyId() + "_tbl", columnMapping.getDatabaseColumn()).getSimpleDataType();
        String defaultValue = columnMapping.getDefaultValue();
        
        if (StringUtils.isBlank(defaultValue)) {
            return "NULL";
        } else if (dataType == SimpleDataType.Date || dataType == SimpleDataType.DateTime) {
            return getDateDefValForDB(columnMapping);
        } else {
            return defaultValue;
        }
	}

    private String getDateDefValForDB(ColumnMapping columnMapping) throws Exception {
        String defaultValue = columnMapping.getDefaultValue().trim();
        if (defaultValue.startsWith("'") && defaultValue.endsWith("'")) {
            defaultValue = defaultValue.substring(1, defaultValue.length() - 1).trim();
        }
        if (StringUtils.isBlank(defaultValue)) {
            return "";
        } else if (isDbDateFunction(defaultValue)) {
            return getAddDateDbFunction(defaultValue);
        } else {
            String reformattedDate = new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM_SS).format(DateUtilities.parseUnknownDateFormat(defaultValue));
            if (isOracleDB()) {
                return "TO_DATE('" + reformattedDate + "', 'DD.MM.YYYY HH24:MI:SS')";
            } else {
                return "STR_TO_DATE('" + reformattedDate + "', '%d.%m.%Y %H:%i:%s')";
            }
        }
    }
    
    private boolean isDbDateFunction(String val) {
        return val.matches("(?i)^(now|now\\(\\)|sysdate|sysdate\\(\\)|current_timestamp|current_timestamp\\(\\)|today)\\s*([+-]\\s*\\d+\\s*)?$");
    }
    
    private String getAddDateDbFunction(String val) {
	    if (isOracleDB()) {
	        return val.replace(val.split("[+\\-\\s]+", 2)[0], "CURRENT_TIMESTAMP");
        } else {
	        Matcher matcher = Pattern.compile("[+-]\\s*\\d+").matcher(val);
            return matcher.find() ? "DATE_ADD(NOW(), INTERVAL " + matcher.group() + " DAY)" : "NOW()";
        }
    }

	private void checkCsvHeaderForRestrictedColumns(final List<String> additionalDbColumns) throws Exception {
		// Some fields that may not be imported by users via csv data, but must be set by the system
		for (String dbColumnName : ImportUtils.getHiddenColumns(admin)) {
			if (importedDBColumns.contains(dbColumnName.toLowerCase()) || additionalDbColumns.contains(dbColumnName.toLowerCase())) {
				throw new Exception("Invalid not allowed dbcolumn to import: " + dbColumnName);
			}
		}
	}
	
	private final void checkCsvHeaderForDuplicates() throws Exception {
		final String duplicateCsvColumn = CsvReader.checkForDuplicateCsvHeader(csvFileHeaders, importProfile.isAutoMapping());
		
		if (duplicateCsvColumn != null) {
			throw new Exception("Invalid duplicate csvcolumn: " + duplicateCsvColumn);
		}
	}
	
	private final void csvDetermineDbColumnsToTransfer(final List<String> additionalDbColumns) {
		// Determine db columns to be transfered from temporary table to live table

		transferDbColumns = new ArrayList<>();
		transferDbColumns.addAll(importedDBColumns);
		transferDbColumns.addAll(additionalDbColumns);
	}
	
	private final int importCsvDataInTemporaryTable(final CsvReader csvReader, final Connection connection, final List<String> additionalDbColumns, List<String> additionalDbValues) throws Exception {
		final CaseInsensitiveMap<String, DbColumnType> columnDataTypes = DbUtilities.getColumnDataTypes(importRecipientsDao.getDataSource(), "customer_" + importProfile.getCompanyId() + "_tbl");
		
		for (int i = 0; i < additionalDbValues.size(); i++) {
			// Pure db function values, indicated by no quotes, cannot be checked by now
			String additionalValue = additionalDbValues.get(i).trim();
			if (StringUtils.isBlank(additionalValue)) {
				additionalDbValues.set(i, "NULL");
			} else if (DbUtilities.isNowKeyword(additionalValue)) {
				additionalDbValues.set(i, "CURRENT_TIMESTAMP");
			} else if (additionalValue.startsWith("'") && additionalValue.endsWith("'")) {
				String textValue = additionalValue.substring(1, additionalValue.length() - 1);
				if (DbUtilities.isNowKeyword(textValue)) {
					additionalDbValues.set(i, "CURRENT_TIMESTAMP");
				} else {
					if (!DbUtilities.isAllowedValueForDataType(isOracleDB(), columnDataTypes.get(additionalDbColumns.get(i)), textValue)) {
						throw new Exception("Additional value '" + textValue + "' is not allowed for column '" + additionalDbColumns.get(i) + "'");
					}
				}
			} else if (additionalValue.startsWith("\"") && additionalValue.endsWith("\"")) {
				// Allow and fix invalid textvalue quotes
				String textValue = additionalValue.substring(1, additionalValue.length() - 1);
				if (DbUtilities.isNowKeyword(textValue)) {
					additionalDbValues.set(i, "CURRENT_TIMESTAMP");
				} else {
					if (!DbUtilities.isAllowedValueForDataType(isOracleDB(), columnDataTypes.get(additionalDbColumns.get(i)), textValue)) {
						throw new Exception("Additional value '" + textValue + "' is not allowed for column '" + additionalDbColumns.get(i) + "'");
					} else {
						additionalDbValues.set(i, "'" + textValue + "'");
					}
				}
			}
		}
		
		insertIntoTemporaryImportTableSqlString = "INSERT INTO " + temporaryImportTableName + " ("
				+ StringUtils.join(importedDBColumns, ", ")
				+ (importedDBColumns.size() > 0 && additionalDbColumns.size() > 0 ? ", " : "")
				+ StringUtils.join(additionalDbColumns, ", ")
				+ (importedDBColumns.size() > 0 || additionalDbColumns.size() > 0 ? ", " : "")
				+ importIndexColumn
				+ ") VALUES ("
				+ AgnUtils.repeatString("?", importedDBColumns.size(), ", ")
				+ (importedDBColumns.size() > 0 && additionalDbValues.size() > 0 ? ", " : "")
				+ StringUtils.join(additionalDbValues, ", ")
				+ (importedDBColumns.size() > 0 || additionalDbValues.size() > 0 ? ", " : "")
				+ "?"
				+ ")";
		
		try(final PreparedStatement preparedStatement = connection.prepareStatement(insertIntoTemporaryImportTableSqlString)) {
			List<List<Object>> batchValues = new ArrayList<>();
			final int batchBlockSize = 1000;
			boolean hasUnexecutedData = false;

			ImportModeHandler importModeHandler = importModeHandlerFactory.getImportModeHandler(ImportMode.getFromInt(importProfile.getImportMode()).getImportModeHandlerName());
			columnMappingsByCsvIndex = new ArrayList<>();
			for (int i = 0; i < importedCsvFileColumnIndexes.size(); i++) {
				columnMappingsByCsvIndex.add(importProfile.getMappingByDbColumn(importedDBColumns.get(i)));
			}

			// This csvDataLine contains all the csv data values, also the not imported ones
			List<String> csvDataLine;
			while ((csvDataLine = csvReader.readNextCsvLine()) != null) {
				List<Object> batchValueEntry = new ArrayList<>();
				batchValues.add(batchValueEntry);
				
				if (importProfile.isNoHeaders()) {
					int csvColumnsExpected = 0;
					for (ColumnMapping columnMapping : importProfile.getColumnMapping()) {
						if (StringUtils.isNotBlank(columnMapping.getFileColumn())) {
							csvColumnsExpected++;
						}
					}
					if (csvDataLine.size() != csvColumnsExpected) {
						throw new Exception("Number of import file columns does not fit mapped columns in line " + csvReader.getReadCsvLines());
					}
				}
				
				try {
					for (int columnIndex = 0; columnIndex < importedCsvFileColumnIndexes.size(); columnIndex++) {
						String importedDB = importedDBColumns.get(columnIndex);
						if (StringUtils.isNotBlank(importedDB) && !ColumnMapping.DO_NOT_IMPORT.equalsIgnoreCase(importedDB)) {
							String csvValue = csvDataLine.get(importedCsvFileColumnIndexes.get(columnIndex));
				        	validateAndSetInsertParameter(importModeHandler, preparedStatement, columnIndex, columnMappingsByCsvIndex.get(columnIndex), csvValue, columnDataTypes, batchValueEntry);
						}
					}

					// Add additional integer value to identify csv data item index
					preparedStatement.setInt(importedCsvFileColumnIndexes.size() + 1, csvReader.getReadCsvLines());
					
					preparedStatement.addBatch();
				} catch (ProfileImportCsvException e) {
					preparedStatement.clearParameters();

					if (temporaryErrorTableName == null) {
						createTemporaryErrorTable(importedDataFileColumns.size());
					}
		        	
					importRecipientsDao.addErroneousCsvEntry(importProfile.getCompanyId(), temporaryErrorTableName, importedCsvFileColumnIndexes, csvDataLine, csvReader.getReadCsvLines(), e.getReasonCode(), e.getCsvFieldName());
				} catch (Exception e) {
					preparedStatement.clearParameters();

					if (temporaryErrorTableName == null) {
						createTemporaryErrorTable(importedDataFileColumns.size());
					}
		        	
					importRecipientsDao.addErroneousCsvEntry(importProfile.getCompanyId(), temporaryErrorTableName, importedCsvFileColumnIndexes, csvDataLine, csvReader.getReadCsvLines(), null, null);
				}
				
				if (csvReader.getReadCsvLines() % batchBlockSize == 0) {
					try {
						int[] results = preparedStatement.executeBatch();
						connection.commit();
						for (int i = 0; i < results.length; i++) {
							if (results[i] != 1 && results[i] != Statement.SUCCESS_NO_INFO) {
								int lineIndex = (csvReader.getReadCsvLines() - batchBlockSize) + i;
								throw new Exception("Line could not be imported: " + lineIndex);
							}
						}
					} catch (BatchUpdateException e) {
						connection.rollback();
						executeSingleStepUpdates(preparedStatement, batchValues, (csvReader.getReadCsvLines() - (csvReader.getReadCsvLines() % batchBlockSize)));
						connection.commit();
					}
					
					batchValues.clear();
					
					hasUnexecutedData = false;
					setCompletedPercent(Math.round(csvReader.getReadCsvLines() * 100f / linesInFile));
				} else {
					hasUnexecutedData = true;
				}
			}
			
			if (hasUnexecutedData) {
				// Execute the last open sql batch block
				try {
					int[] results = preparedStatement.executeBatch();
					connection.commit();
					for (int i = 0; i < results.length; i++) {
						if (results[i] != 1 && results[i] != Statement.SUCCESS_NO_INFO) {
							int lineIndex = (csvReader.getReadCsvLines() - (csvReader.getReadCsvLines() % batchBlockSize)) + i;
							throw new Exception("Line could not be imported: " + lineIndex);
						}
					}
				} catch (BatchUpdateException e) {
					connection.rollback();
					executeSingleStepUpdates(preparedStatement, batchValues, (csvReader.getReadCsvLines() - (csvReader.getReadCsvLines() % batchBlockSize)));
					connection.commit();
				}
				
				batchValues.clear();
			}

			return csvReader.getReadCsvLines() - 1;
		}
	}
	
	private void executeSingleStepUpdates(PreparedStatement preparedStatement, List<List<Object>> batchValues, int startingDataEntryIndex) throws Exception {
		preparedStatement.clearBatch();
		for (int entryIndex = 0; entryIndex < batchValues.size(); entryIndex++) {
			List<Object> batchValueEntry = batchValues.get(entryIndex);
			List<String> stringRepresentations = new ArrayList<>();
			try {
				for (int i = 0; i < batchValueEntry.size(); i++) {
					preparedStatement.setObject(i + 1, batchValueEntry.get(i));
					if (batchValueEntry.get(i) == null) {
						stringRepresentations.add("");
					} else if (batchValueEntry.get(i) instanceof Date) {
						stringRepresentations.add(importDateFormat.format(batchValueEntry.get(i)));
					} else if (batchValueEntry.get(i) instanceof Integer || batchValueEntry.get(i) instanceof Long) {
						stringRepresentations.add(batchValueEntry.get(i).toString());
					} else if (batchValueEntry.get(i) instanceof Double || batchValueEntry.get(i) instanceof Float) {
						if (importProfile.getDecimalSeparator() != '.') {
							stringRepresentations.add((batchValueEntry.get(i).toString()).replace(".", "").replace(Character.toString(importProfile.getDecimalSeparator()), "."));
						} else {
							stringRepresentations.add((String) batchValueEntry.get(i));
						}
					} else if (batchValueEntry.get(i) instanceof String) {
						stringRepresentations.add((String) batchValueEntry.get(i));
					} else {
						throw new Exception("Unexpected datatype: " + batchValueEntry.get(i).getClass().getSimpleName());
					}
				}
				preparedStatement.setObject(batchValueEntry.size() + 1, Integer.valueOf(startingDataEntryIndex + entryIndex));
				preparedStatement.execute();
			} catch (SQLException e) {
				if (temporaryErrorTableName == null) {
					createTemporaryErrorTable(importedDataFileColumns.size());
				}
				
				// Bring error data back into datafiles order
				List<String> stringRepresentationsInDatafileOrder = new ArrayList<>();
				for (int i = 0; i < stringRepresentations.size(); i++) {
					stringRepresentationsInDatafileOrder.add(null);
				}
				for (int i = 0; i < importedCsvFileColumnIndexes.size(); i++) {
					stringRepresentationsInDatafileOrder.set(importedCsvFileColumnIndexes.get(i), stringRepresentations.get(i));
				}
	        	
				importRecipientsDao.addErroneousCsvEntry(importProfile.getCompanyId(), temporaryErrorTableName, importedCsvFileColumnIndexes, stringRepresentationsInDatafileOrder, startingDataEntryIndex + entryIndex, ReasonCode.Unknown, null);
			}
		}
	}

	private final int importJsonDataInTemporaryTable() throws Exception {
		setCompletedPercent(0);
		currentProgressStatus = ImportItemizedProgress.IMPORTING_DATA_TO_TMP_TABLE;

		try(final Connection connection = importRecipientsDao.getDataSource().getConnection()) {
			final boolean previousAutoCommit = connection.getAutoCommit();
			
			try {
				connection.setAutoCommit(false);
				
				try {
					try (final InputStream inputStream = getImportInputStream()) {
						return importJsonDataInTemporaryTable(inputStream, connection);
					}
				} finally {
					connection.commit();
				}
			} finally {
				connection.setAutoCommit(previousAutoCommit);
			}
		}
	}
	
	private final int importJsonDataInTemporaryTable(final InputStream stream, final Connection connection) throws Exception {
		try(final JsonReader jsonReader = new Json5Reader(stream, Charset.getCharsetById(importProfile.getCharset()).getCharsetName())) {
			jsonReader.readNextToken();
			
			while (jsonReader.getCurrentToken() != null && jsonReader.getCurrentToken() != JsonToken.JsonArray_Open) {
				jsonReader.readNextToken();
			}
			
			if (jsonReader.getCurrentToken() != JsonToken.JsonArray_Open) {
				throw new Exception("Json data does not contain expected JsonArray");
			}
			
			final List<String> additionalDbColumns = new ArrayList<>();
			final List<String> additionalDbValues = new ArrayList<>();

			importedDataFileColumns = new ArrayList<>();
			importedCsvFileColumnIndexes = new ArrayList<>();
			importedDBColumns = new ArrayList<>();
			csvFileHeaders = new ArrayList<>();
			columnMappingsByCsvIndex = new ArrayList<>();
			
			processJsonColumnData(additionalDbColumns, additionalDbValues);
			checkJsonDataForRestrictedColumns(additionalDbColumns);
			
			// Check if keycolumns are part of the imported data
			for (String keyColumnName : importProfile.getKeyColumns()) {
				if (!importedDBColumns.contains(keyColumnName.toLowerCase())) {
					throw new ImportException(false, "error.import.missing.keyColumn", keyColumnName);
				}
			}
			
			jsonDetermineDbColumnsToTransfer(additionalDbColumns);
			
			return importJsonDataInTemporaryTable(jsonReader, connection, additionalDbColumns, additionalDbValues);
		}
	}
	
	private final void processJsonColumnData(final List<String> additionalDbColumns, final List<String> additionalDbValues) throws Exception {
		for (final ColumnMapping columnMapping : importProfile.getColumnMapping()) {
			if (StringUtils.isNotBlank(columnMapping.getDatabaseColumn()) && !ColumnMapping.DO_NOT_IMPORT.equalsIgnoreCase(columnMapping.getDatabaseColumn())) {
				if (StringUtils.isNotBlank(columnMapping.getFileColumn())) {
					importedDataFileColumns.add(columnMapping.getFileColumn());
					importedCsvFileColumnIndexes.add(importedDataFileColumns.size() - 1);
					importedDBColumns.add(columnMapping.getDatabaseColumn().toLowerCase());
				} else {
					// Import into db column without data in json file
					additionalDbColumns.add(columnMapping.getDatabaseColumn());

					if (StringUtils.isBlank(columnMapping.getDefaultValue())) {
						additionalDbValues.add("NULL");
					} else if (columnMapping.getDefaultValue().startsWith("'") && columnMapping.getDefaultValue().endsWith("'")
						&& (DbUtilities.getColumnDataType(importRecipientsDao.getDataSource(), "customer_" + importProfile.getCompanyId() + "_tbl", columnMapping.getDatabaseColumn()).getSimpleDataType() == SimpleDataType.Date
						|| DbUtilities.getColumnDataType(importRecipientsDao.getDataSource(), "customer_" + importProfile.getCompanyId() + "_tbl", columnMapping.getDatabaseColumn()).getSimpleDataType() == SimpleDataType.DateTime)) {
						// Format date default value for db
						String reformattedDate = new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM_SS).format(importDateFormat.parse(columnMapping.getDefaultValue().substring(1, columnMapping.getDefaultValue().length() - 1)));
						if (isOracleDB()) {
							additionalDbValues.add("TO_DATE('" + reformattedDate + "', 'DD.MM.YYYY HH24:MI')");
						} else {
							additionalDbValues.add("STR_TO_DATE('" + reformattedDate + "', '%d.%m.%Y %H:%i:%s')");
						}
					} else {
						additionalDbValues.add(columnMapping.getDefaultValue());
					}
				}
			}
		}
	}
	
	private final void checkJsonDataForRestrictedColumns(final List<String> additionalDbColumns) throws Exception {
		// Some fields that may not be imported by users via import data, but must be set by the system
		for (String dbColumnName : ImportUtils.getHiddenColumns(admin)) {
			if (importedDBColumns.contains(dbColumnName.toLowerCase()) || additionalDbColumns.contains(dbColumnName.toLowerCase())) {
				throw new Exception("Invalid not allowed dbcolumn to import: " + dbColumnName);
			}
		}
	}
	
	private final void jsonDetermineDbColumnsToTransfer(final List<String> additionalDbColumns) {
		transferDbColumns = new ArrayList<>();
		transferDbColumns.addAll(importedDBColumns);
		transferDbColumns.addAll(additionalDbColumns);
	}
	
	private final int importJsonDataInTemporaryTable(final JsonReader jsonReader, final Connection connection, final List<String> additionalDbColumns, final List<String> additionalDbValues) throws Exception {
		insertIntoTemporaryImportTableSqlString = "INSERT INTO " + temporaryImportTableName + " ("
				+ StringUtils.join(importedDBColumns, ", ")
				+ (importedDBColumns.size() > 0 && additionalDbColumns.size() > 0 ? ", " : "")
				+ StringUtils.join(additionalDbColumns, ", ")
				+ (importedDBColumns.size() > 0 || additionalDbColumns.size() > 0 ? ", " : "")
				+ importIndexColumn
				+ ") VALUES ("
				+ AgnUtils.repeatString("?", importedDBColumns.size(), ", ")
				+ (importedDBColumns.size() > 0 && additionalDbValues.size() > 0 ? ", " : "")
				+ StringUtils.join(additionalDbValues, ", ")
				+ (importedDBColumns.size() > 0 || additionalDbValues.size() > 0 ? ", " : "")
				+ "?"
				+ ")";
		
		try(final PreparedStatement preparedStatement = connection.prepareStatement(insertIntoTemporaryImportTableSqlString)) {
			List<List<Object>> batchValues = new ArrayList<>();
			final int batchBlockSize = 1000;

			final CaseInsensitiveMap<String, DbColumnType> columnDataTypes = DbUtilities.getColumnDataTypes(importRecipientsDao.getDataSource(), temporaryImportTableName);
			ImportModeHandler importModeHandler = importModeHandlerFactory.getImportModeHandler(ImportMode.getFromInt(importProfile.getImportMode()).getImportModeHandlerName());
			
			final Map<String, ColumnMapping> columnMappingByDbColumn = new HashMap<>();
			for (ColumnMapping columnMapping : importProfile.getColumnMapping()) {
				if (StringUtils.isNotBlank(columnMapping.getDatabaseColumn()) && !ColumnMapping.DO_NOT_IMPORT.equalsIgnoreCase(columnMapping.getDatabaseColumn())) {
					columnMappingByDbColumn.put(columnMapping.getDatabaseColumn(), columnMapping);
				}
			}
			
			// This JsonObject contains all the data values, also the not imported ones
			int jsonObjectCount = 0;
			boolean hasUnexecutedData = false;

			while (jsonReader.readNextJsonNode()) {
				List<Object> batchValueEntry = new ArrayList<>();
				batchValues.add(batchValueEntry);
				
				jsonObjectCount++;
				final Object currentObject = jsonReader.getCurrentObject();
				if (currentObject == null || !(currentObject instanceof JsonObject)) {
					throw new Exception("Json data does not contain expected JsonArray of JsonObjects");
				}
				final JsonObject jsonDataObject = (JsonObject) currentObject;
				for (String jsonAttributeName : jsonDataObject.keySet()) {
					if (!csvFileHeaders.contains(jsonAttributeName)) {
						csvFileHeaders.add(jsonAttributeName);
						columnMappingsByCsvIndex.add(importProfile.getMappingByDbColumn(jsonAttributeName));
					}
				}
				
				try {
					int columnIndex = -1;
					for (String importedDBColumn : importedDBColumns) {
						columnIndex++;
						if (StringUtils.isNotBlank(importedDBColumn) && !ColumnMapping.DO_NOT_IMPORT.equalsIgnoreCase(importedDBColumn)) {
							final Object jsonValue = jsonDataObject.get(columnMappingByDbColumn.get(importedDBColumn).getFileColumn());
							final String jsonValueString = jsonValue == null ? null : jsonValue.toString();
				        	validateAndSetInsertParameter(importModeHandler, preparedStatement, columnIndex, columnMappingByDbColumn.get(importedDBColumn), jsonValueString, columnDataTypes, batchValueEntry);
						}
					}

					// Add additional integer value to identify csv data item index
					preparedStatement.setInt(importedDBColumns.size() + 1, jsonObjectCount);
					
					preparedStatement.addBatch();
				} catch (ProfileImportCsvException e) {
					preparedStatement.clearParameters();

					if (temporaryErrorTableName == null) {
						createTemporaryErrorTable(importedDBColumns.size());
					}
		        	
					importRecipientsDao.addErroneousJsonObject(importProfile.getCompanyId(), temporaryErrorTableName, columnMappingByDbColumn, importedDBColumns, jsonDataObject, jsonObjectCount, e.getReasonCode(), e.getCsvFieldName());
				} catch (Exception e) {
					preparedStatement.clearParameters();

					if (temporaryErrorTableName == null) {
						createTemporaryErrorTable(importedDBColumns.size());
					}
		        	
					importRecipientsDao.addErroneousJsonObject(importProfile.getCompanyId(), temporaryErrorTableName, columnMappingByDbColumn, importedDBColumns, jsonDataObject, jsonObjectCount, null, null);
				}
				
				if (jsonObjectCount % batchBlockSize == 0) {
					try {
						int[] results = preparedStatement.executeBatch();
						connection.commit();
						for (int i = 0; i < results.length; i++) {
							if (results[i] != 1 && results[i] != Statement.SUCCESS_NO_INFO) {
								int lineIndex = (jsonObjectCount - batchBlockSize) + i;
								throw new Exception("Line could not be imported: " + lineIndex);
							}
						}
					} catch (BatchUpdateException e) {
						connection.rollback();
						executeSingleStepUpdates(preparedStatement, batchValues, (jsonObjectCount - (jsonObjectCount % batchBlockSize)));
						batchValues.clear();
						connection.commit();
					}
					hasUnexecutedData = false;
					setCompletedPercent(Math.round(jsonObjectCount * 100f / linesInFile));
				} else {
					hasUnexecutedData = true;
				}
			}
			
			if (hasUnexecutedData) {
				// Execute the last open sql batch block
				try {
					final int[] results = preparedStatement.executeBatch();
					connection.commit();
					for (int i = 0; i < results.length; i++) {
						if (results[i] != 1 && results[i] != Statement.SUCCESS_NO_INFO) {
							final int lineIndex = (jsonObjectCount - (jsonObjectCount % batchBlockSize)) + i;
							throw new Exception("Line could not be imported: " + lineIndex);
						}
					}
				} catch (BatchUpdateException e) {
					connection.rollback();
					executeSingleStepUpdates(preparedStatement, batchValues, (jsonObjectCount - (jsonObjectCount % batchBlockSize)));
					batchValues.clear();
					connection.commit();
				}
			}

			return jsonObjectCount;
		}
	}

	public boolean hasRepairableErrors() {
		if (temporaryErrorTableName != null) {
			return importRecipientsDao.hasRepairableErrors(temporaryErrorTableName);
		} else {
			return false;
		}
	}

	/**
	 * Change data in temporary error table and revalidate the data
	 * @throws Exception
	 */
	public void setBeansAfterEditOnErrorEditPage(Map<String, String> changedValues) throws Exception {
		List<Integer> updatedCsvIndexes = importRecipientsDao.updateTemporaryErrors(importProfile.getCompanyId(), temporaryErrorTableName, importedDataFileColumns, changedValues);
		revalidateTemporaryErrorTable(updatedCsvIndexes);
	}

	public void ignoreErroneousData() {
		ignoreErrors = true;
	}

	private void revalidateTemporaryErrorTable(List<Integer> revalidateCsvIndexes) throws Exception {
		setCompletedPercent(0);

		try (Connection connection = importRecipientsDao.getDataSource().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(insertIntoTemporaryImportTableSqlString)) {
			CaseInsensitiveMap<String, DbColumnType> columnDataTypes = DbUtilities.getColumnDataTypes(importRecipientsDao.getDataSource(), temporaryImportTableName);
			
			ImportModeHandler importModeHandler = importModeHandlerFactory.getImportModeHandler(ImportMode.getFromInt(importProfile.getImportMode()).getImportModeHandlerName());
			
			int doneLines = 0;
			for (int revalidateCsvIndex : revalidateCsvIndexes) {
				Map<String, Object> csvDataLine = importRecipientsDao.getErrorLine(temporaryErrorTableName, revalidateCsvIndex);
				// This csvValues are only the imported ones, not imported columns are lost on the way
				List<String> csvValues = new ArrayList<>();
				for (int columnIndex = 0; columnIndex < importedCsvFileColumnIndexes.size(); columnIndex++) {
					csvValues.add((String) csvDataLine.get("data_" + (columnIndex + 1)));
				}
				try {
					for (int columnIndex = 0; columnIndex < importedCsvFileColumnIndexes.size(); columnIndex++) {
						String importedDB = importedDBColumns.get(columnIndex);
						if (StringUtils.isNotBlank(importedDB) && !ColumnMapping.DO_NOT_IMPORT.equalsIgnoreCase(importedDB)) {
							String csvValue = (String) csvDataLine.get("data_" + (columnIndex + 1));
				        	validateAndSetInsertParameter(importModeHandler, preparedStatement, columnIndex, columnMappingsByCsvIndex.get(columnIndex), csvValue, columnDataTypes, null);
						}
					}

					// Add additional integer value to identify csv data item index
					preparedStatement.setInt(importedCsvFileColumnIndexes.size() + 1, revalidateCsvIndex);
					
					preparedStatement.executeUpdate();
					importRecipientsDao.markErrorLineAsRepaired(importProfile.getCompanyId(), temporaryErrorTableName, revalidateCsvIndex);
				} catch (ProfileImportCsvException e) {
					preparedStatement.clearParameters();

					if (temporaryErrorTableName == null) {
						createTemporaryErrorTable(importedDataFileColumns.size());
					}
		        	
					importRecipientsDao.addErroneousCsvEntry(importProfile.getCompanyId(), temporaryErrorTableName, csvValues, revalidateCsvIndex, e.getReasonCode(), e.getCsvFieldName());
				} catch (Exception e) {
					preparedStatement.clearParameters();

					if (temporaryErrorTableName == null) {
						createTemporaryErrorTable(importedDataFileColumns.size());
					}
		        	
					importRecipientsDao.addErroneousCsvEntry(importProfile.getCompanyId(), temporaryErrorTableName, importedCsvFileColumnIndexes, csvValues, revalidateCsvIndex, null, null);
				}
				
				doneLines++;
				setCompletedPercent(Math.round(doneLines * 100f / revalidateCsvIndexes.size()));
			}
		}
	}

	private void validateAndSetInsertParameter(ImportModeHandler importModeHandler, PreparedStatement preparedStatement, int columnIndex, ColumnMapping columnMapping, String dataValue, CaseInsensitiveMap<String, DbColumnType> columnDataTypes, List<Object> batchValueEntry) throws Exception {
		// Check mandatory import columns
		if (columnMapping.isMandatory() && StringUtils.isBlank(dataValue)) {
			status.addErrorColumn(columnMapping.getFileColumn());
			throw new ProfileImportCsvException(ReasonCode.MissingMandatory, columnMapping.getFileColumn(), "Missing mandatory value for customer field: " + columnMapping.getDatabaseColumn());
		} else if (StringUtils.isNotEmpty(columnMapping.getDefaultValue()) && StringUtils.isBlank(dataValue)) {
			dataValue = columnMapping.getDefaultValue();
			if (dataValue.startsWith("'") && dataValue.endsWith("'")) {
				dataValue = dataValue.substring(1, dataValue.length() - 1);
			}
		}
		
		// Decrypt encrypted csv data columns
		if (profileFieldEncryptor != null && importProfile.getEncryptedColumns().contains(columnMapping.getDatabaseColumn())) {
			try {
				dataValue = profileFieldEncryptor.decryptFromBase64(dataValue, admin.getCompanyID());
			} catch (Exception e) {
				status.addErrorColumn(columnMapping.getFileColumn());
				throw new ProfileImportCsvException(ReasonCode.InvalidEncryption, columnMapping.getFileColumn(), "Invalid encrypted value: " + dataValue);
			}
		}
		
		// Validate and normalize emails
		if ("email".equalsIgnoreCase(columnMapping.getDatabaseColumn())) {
			if (normalizeEmails) {
				dataValue = AgnUtils.normalizeEmail(dataValue);
			}
			if (StringUtils.isNotBlank(dataValue) && !isEmailValid(dataValue)) {
				status.addErrorColumn(columnMapping.getFileColumn());
				throw new ProfileImportCsvException(ReasonCode.InvalidEmail, columnMapping.getFileColumn(), "Invalid email: " + dataValue);
			}
		}
		
		// Check mailtype
		if ("mailtype".equalsIgnoreCase(columnMapping.getDatabaseColumn())) {
			int mailtypeValue;
			try {
				mailtypeValue = Integer.parseInt(dataValue);
			} catch (Exception e) {
				status.addErrorColumn(columnMapping.getFileColumn());
				throw new ProfileImportCsvException(ReasonCode.InvalidNumber, columnMapping.getFileColumn(), "Invalid non numeric mailtype: " + dataValue);
			}
			if (mailtypeValue < 0 || mailtypeValue > ConfigService.MAXIMUM_ALLOWED_MAILTYPE) {
				status.addErrorColumn(columnMapping.getFileColumn());
				throw new ProfileImportCsvException(ReasonCode.InvalidMailtype, columnMapping.getFileColumn(), "Invalid mailtype: " + dataValue);
			}
		}
		
		// Apply gender rules
		if ("gender".equalsIgnoreCase(columnMapping.getDatabaseColumn())) {
			Integer genderValue = importProfile.getGenderValueByFieldValue(dataValue);
			if (genderValue == null) {
				Gender estimatedGender = Gender.getGenderByDefaultGenderMapping(dataValue);
				if (estimatedGender != Gender.UNKNOWN) {
					genderValue = estimatedGender.getStorageValue();
				}
			}
			if (genderValue == null || genderValue < 0 || genderValue > maxGenderValue) {
				status.addErrorColumn(columnMapping.getFileColumn());
				throw new ProfileImportCsvException(ReasonCode.InvalidGender, columnMapping.getFileColumn(), "Invalid gender: " + dataValue);
			}
			dataValue = Integer.toString(genderValue);
		}
		
		// Check for non empty value for key columns
		if (StringUtils.isBlank(dataValue) && importProfile.getKeyColumns().contains(columnMapping.getDatabaseColumn())) {
			status.addErrorColumn(columnMapping.getFileColumn());
			throw new ProfileImportCsvException(ReasonCode.MissingMandatory, columnMapping.getFileColumn(), "Missing mandatory value for customer field: " + columnMapping.getDatabaseColumn());
		}

		DbColumnType columnType = columnDataTypes.get(columnMapping.getDatabaseColumn());
		// Check for empty and null value of data columns
		if (StringUtils.isBlank(dataValue) && !importModeHandler.isNullValueAllowedForData(columnType, NullValuesAction.getFromInt(importProfile.getNullValuesAction()))) {
			status.addErrorColumn(columnMapping.getFileColumn());
			throw new ProfileImportCsvException(ReasonCode.MissingMandatory, columnMapping.getFileColumn(), "Invalid NULL or empty value");
		}
		SimpleDataType simpleColumnDataType = columnType.getSimpleDataType();
		if (simpleColumnDataType == SimpleDataType.Date) {
			if (StringUtils.isBlank(dataValue)) {
				preparedStatement.setNull(columnIndex + 1, Types.TIMESTAMP);
				if (batchValueEntry != null) {
					batchValueEntry.add(null);
				}
			} else if (DbUtilities.isNowKeyword(dataValue)) {
				preparedStatement.setTimestamp(columnIndex + 1, new Timestamp(new Date().getTime()));
				if (batchValueEntry != null) {
					batchValueEntry.add(new Timestamp(new Date().getTime()));
				}
			} else {
				try {
					Date parsedDate;
					if ("CSV".equalsIgnoreCase(importProfile.getDatatype())) {
						parsedDate = importDateFormat.parse(dataValue);
					} else {
						parsedDate = DateUtilities.parseIso8601DateTimeString(dataValue);
					}
					GregorianCalendar calendar = new GregorianCalendar();
					calendar.setTime(parsedDate);
					if (calendar.get(Calendar.YEAR) < 100) {
						throw new ProfileImportCsvException(ReasonCode.InvalidDate, columnMapping.getFileColumn(), "Invalid date: " + dataValue);
					}
					preparedStatement.setTimestamp(columnIndex + 1, new Timestamp(parsedDate.getTime()));
					if (batchValueEntry != null) {
						batchValueEntry.add(new Timestamp(parsedDate.getTime()));
					}
				} catch (ParseException e) {
					status.addErrorColumn(columnMapping.getFileColumn());
					throw new ProfileImportCsvException(ReasonCode.InvalidDate, columnMapping.getFileColumn(), "Invalid date: " + dataValue);
				}
			}
		} else if (simpleColumnDataType == SimpleDataType.DateTime) {
			if (StringUtils.isBlank(dataValue)) {
				preparedStatement.setNull(columnIndex + 1, Types.TIMESTAMP);
				if (batchValueEntry != null) {
					batchValueEntry.add(null);
				}
			} else if (DbUtilities.isNowKeyword(dataValue)) {
				preparedStatement.setTimestamp(columnIndex + 1, new Timestamp(new Date().getTime()));
				if (batchValueEntry != null) {
					batchValueEntry.add(new Timestamp(new Date().getTime()));
				}
			} else {
				try {
					Date parsedDate;
					if ("CSV".equalsIgnoreCase(importProfile.getDatatype())) {
						parsedDate = importDateFormat.parse(dataValue);
					} else {
						parsedDate = DateUtilities.parseIso8601DateTimeString(dataValue);
					}
					GregorianCalendar calendar = new GregorianCalendar();
					calendar.setTime(parsedDate);
					if (calendar.get(Calendar.YEAR) < 100) {
						throw new ProfileImportCsvException(ReasonCode.InvalidDate, columnMapping.getFileColumn(), "Invalid date: " + dataValue);
					}
					preparedStatement.setTimestamp(columnIndex + 1, new Timestamp(parsedDate.getTime()));
					if (batchValueEntry != null) {
						batchValueEntry.add(new Timestamp(parsedDate.getTime()));
					}
				} catch (ParseException e) {
					status.addErrorColumn(columnMapping.getFileColumn());
					throw new ProfileImportCsvException(ReasonCode.InvalidDate, columnMapping.getFileColumn(), "Invalid date: " + dataValue);
				}
			}
		} else if (simpleColumnDataType == SimpleDataType.Numeric || simpleColumnDataType == SimpleDataType.Float) {
			if (StringUtils.isBlank(dataValue)) {
				preparedStatement.setNull(columnIndex + 1, Types.INTEGER);
				if (batchValueEntry != null) {
					batchValueEntry.add(null);
				}
			} else {
				if (importProfile.getDecimalSeparator() != '.') {
					dataValue = dataValue.replace(".", "").replace(Character.toString(importProfile.getDecimalSeparator()), ".");
				}

				if (dataValue.contains(".")) {
					double value;
					try {
						value = Double.parseDouble(dataValue);
					} catch (NumberFormatException e) {
						status.addErrorColumn(columnMapping.getFileColumn());
						throw new ProfileImportCsvException(ReasonCode.InvalidNumber, columnMapping.getFileColumn(), "Invalid number: " + dataValue);
					}
					
					if (isOracleDB()) {
						if ("integer".equalsIgnoreCase(columnType.getTypeName())) {
							if (value > 2147483647) {
								status.addErrorColumn(columnMapping.getFileColumn());
								throw new ProfileImportCsvException(ReasonCode.NumberTooLarge, columnMapping.getFileColumn(), "Number too large: " + dataValue);
							} else if (value < -2147483648) {
								status.addErrorColumn(columnMapping.getFileColumn());
								throw new ProfileImportCsvException(ReasonCode.NumberTooLarge, columnMapping.getFileColumn(), "Number too large: " + dataValue);
							}
						} else if (columnType.getNumericPrecision() < Double.toString(value).length()) {
							status.addErrorColumn(columnMapping.getFileColumn());
							throw new ProfileImportCsvException(ReasonCode.NumberTooLarge, columnMapping.getFileColumn(), "Number too large: " + dataValue);
						}
					} else {
						if ("smallint".equalsIgnoreCase(columnType.getTypeName())) {
							if (value > 32767) {
								throw new ProfileImportCsvException(ReasonCode.NumberTooLarge, columnMapping.getFileColumn(), "Number too large: " + value);
							} else if (value < -32768) {
								throw new ProfileImportCsvException(ReasonCode.NumberTooLarge, columnMapping.getFileColumn(), "Number too small: " + value);
							}
						} else if ("int".equalsIgnoreCase(columnType.getTypeName())) {
							if (value > 2147483647) {
								throw new ProfileImportCsvException(ReasonCode.NumberTooLarge, columnMapping.getFileColumn(), "Number too large: " + value);
							} else if (value < -2147483648) {
								throw new ProfileImportCsvException(ReasonCode.NumberTooLarge, columnMapping.getFileColumn(), "Number too small: " + value);
							}
						} else if ("bigint".equalsIgnoreCase(columnType.getTypeName())) {
							if (value > 9223372036854775807l) {
								throw new ProfileImportCsvException(ReasonCode.NumberTooLarge, columnMapping.getFileColumn(), "Number too large: " + value);
							} else if (value < -9223372036854775808l) {
								throw new ProfileImportCsvException(ReasonCode.NumberTooLarge, columnMapping.getFileColumn(), "Number too small: " + value);
							}
						}
					}
					
					preparedStatement.setDouble(columnIndex + 1, value);
					if (batchValueEntry != null) {
						batchValueEntry.add(value);
					}
				} else {
					long value;
					try {
						value = Long.parseLong(dataValue);
					} catch (NumberFormatException e) {
						status.addErrorColumn(columnMapping.getFileColumn());
						throw new ProfileImportCsvException(ReasonCode.InvalidNumber, columnMapping.getFileColumn(), "Invalid number: " + dataValue);
					}
					
					if (isOracleDB()) {
						if (columnType.getNumericPrecision() < Long.toString(value).length()) {
							status.addErrorColumn(columnMapping.getFileColumn());
							throw new ProfileImportCsvException(ReasonCode.NumberTooLarge, columnMapping.getFileColumn(), "Number too large: " + dataValue);
						}
					} else {
						if ("smallint".equalsIgnoreCase(columnType.getTypeName())) {
							if (value > 32767) {
								throw new ProfileImportCsvException(ReasonCode.NumberTooLarge, columnMapping.getFileColumn(), "Number too large: " + value);
							} else if (value < -32768) {
								throw new ProfileImportCsvException(ReasonCode.NumberTooLarge, columnMapping.getFileColumn(), "Number too small: " + value);
							}
						} else if ("int".equalsIgnoreCase(columnType.getTypeName())) {
							if (value > 2147483647) {
								throw new ProfileImportCsvException(ReasonCode.NumberTooLarge, columnMapping.getFileColumn(), "Number too large: " + value);
							} else if (value < -2147483648) {
								throw new ProfileImportCsvException(ReasonCode.NumberTooLarge, columnMapping.getFileColumn(), "Number too small: " + value);
							}
						} else if ("bigint".equalsIgnoreCase(columnType.getTypeName())) {
							if (value > 9223372036854775807l) {
								throw new ProfileImportCsvException(ReasonCode.NumberTooLarge, columnMapping.getFileColumn(), "Number too large: " + value);
							} else if (value < -9223372036854775808l) {
								throw new ProfileImportCsvException(ReasonCode.NumberTooLarge, columnMapping.getFileColumn(), "Number too small: " + value);
							}
						}
					}
					
					preparedStatement.setLong(columnIndex + 1, value);
					if (batchValueEntry != null) {
						batchValueEntry.add(value);
					}
				}
			}
		} else if (simpleColumnDataType == SimpleDataType.Characters) {
			if (StringUtils.isBlank(dataValue)) {
				preparedStatement.setNull(columnIndex + 1, Types.VARCHAR);
				if (batchValueEntry != null) {
					batchValueEntry.add(null);
				}
			} else if (dataValue.getBytes("UTF-8").length > columnDataTypes.get(columnMapping.getDatabaseColumn()).getCharacterLength()) {
				status.addErrorColumn(columnMapping.getFileColumn());
				throw new ProfileImportCsvException(ReasonCode.ValueTooLarge, columnMapping.getFileColumn(), "Value too large: " + dataValue);
			} else {
				preparedStatement.setString(columnIndex + 1, dataValue);
				if (batchValueEntry != null) {
					batchValueEntry.add(dataValue);
				}
			}
		} else {
			if (StringUtils.isBlank(dataValue)) {
				preparedStatement.setNull(columnIndex + 1, Types.VARCHAR);
				if (batchValueEntry != null) {
					batchValueEntry.add(null);
				}
			} else {
				preparedStatement.setString(columnIndex + 1, dataValue);
				if (batchValueEntry != null) {
					batchValueEntry.add(dataValue);
				}
			}
		}
	}

	private boolean isEmailValid(String email) {
		return email != null && emailValidator.isValid(email) && email.equals(email.trim());
	}

	private File createImportedRecipients(String fileBaseName, boolean csvOutput) throws Exception {
		String sqlPart = " FROM " + temporaryImportTableName;
		if (temporaryImportTableName != null && importRecipientsDao.getResultEntriesCount("SELECT COUNT(*)" + sqlPart) > 0) {
			if (csvOutput) {
				return createZippedCsvFile(fileBaseName, transferDbColumns, "SELECT " + StringUtils.join(transferDbColumns, ", ") + sqlPart);
			} else {
				return createZippedJsonFile(fileBaseName, transferDbColumns, "SELECT " + StringUtils.join(transferDbColumns, ", ") + sqlPart);
			}
		} else {
			return null;
		}
	}

	private File createInvalidRecipients(String fileBaseName, List<String> dataColumns, boolean csvOutput) throws Exception {
		List<String> exportColumns = new ArrayList<>();
		for (int i = 0; i < dataColumns.size(); i++) {
			exportColumns.add("data_" + (i + 1));
		}
		if (temporaryErrorTableName != null && importRecipientsDao.getResultEntriesCount("SELECT COUNT(*) FROM " + temporaryErrorTableName + " WHERE errorfixed = 0") > 0) {
			if (csvOutput) {
				return createZippedCsvFile(fileBaseName, dataColumns, "SELECT " + StringUtils.join(exportColumns, ", ") + " FROM " + temporaryErrorTableName + " WHERE errorfixed = 0");
			} else {
				return createZippedJsonFile(fileBaseName, dataColumns, "SELECT " + StringUtils.join(exportColumns, ", ") + " FROM " + temporaryErrorTableName + " WHERE errorfixed = 0");
			}
		} else {
			return null;
		}
	}

	/**
	 * Readout all customer data that was fixed by user in GUI.
	 * 
	 * Difference to method "createInvalidRecipients" is "... WHERE errorfixed = 1"
	 * 
	 * @param fileBaseName
	 * @param dataColumns
	 * @param csvOutput
	 * @return
	 * @throws Exception
	 */
	private File createFixedByUserRecipients(String fileBaseName, List<String> dataColumns, boolean csvOutput) throws Exception {
		List<String> exportColumns = new ArrayList<>();
		for (int i = 0; i < dataColumns.size(); i++) {
			exportColumns.add("data_" + (i + 1));
		}
		if (temporaryErrorTableName != null && importRecipientsDao.getResultEntriesCount("SELECT COUNT(*) FROM " + temporaryErrorTableName + " WHERE errorfixed = 1") > 0) {
			if (csvOutput) {
				return createZippedCsvFile(fileBaseName, dataColumns, "SELECT " + StringUtils.join(exportColumns, ", ") + " FROM " + temporaryErrorTableName + " WHERE errorfixed = 1");
			} else {
				return createZippedJsonFile(fileBaseName, dataColumns, "SELECT " + StringUtils.join(exportColumns, ", ") + " FROM " + temporaryErrorTableName + " WHERE errorfixed = 1");
			}
		} else {
			return null;
		}
	}

	private File createDuplicateInCsvOrDbRecipients(String fileBaseName, boolean csvOutput) throws Exception {
		String selectNumberOfDuplicatesSql = "SELECT COUNT(*) FROM " + temporaryImportTableName + " WHERE " + duplicateIndexColumn + " IS NOT NULL OR customer_id > 0";
		if (temporaryImportTableName != null && importRecipientsDao.getResultEntriesCount(selectNumberOfDuplicatesSql) > 0) {
			String selectDupliactesDataSql =
				"SELECT " + StringUtils.join(transferDbColumns, ", ") + ", 'in_file' AS " + duplicateIndexColumn + "_place FROM " + temporaryImportTableName + " WHERE " + duplicateIndexColumn + " IS NOT NULL AND customer_id = 0"
				+ " UNION ALL"
				+ " SELECT " + StringUtils.join(transferDbColumns, ", ") + ", 'in_db' AS " + duplicateIndexColumn + "_place FROM " + temporaryImportTableName + " WHERE " + duplicateIndexColumn + " IS NULL AND customer_id > 0"
				+ " UNION ALL"
				+ " SELECT " + StringUtils.join(transferDbColumns, ", ") + ", 'in_file_and_db' AS " + duplicateIndexColumn + "_place FROM " + temporaryImportTableName + " WHERE " + duplicateIndexColumn + " IS NOT NULL AND customer_id > 0";
			ArrayList<String> outputColumns = new ArrayList<>(transferDbColumns);
			outputColumns.add(duplicateIndexColumn + "_place");
			if (csvOutput) {
				return createZippedCsvFile(fileBaseName, outputColumns, selectDupliactesDataSql);
			} else {
				return createZippedJsonFile(fileBaseName, outputColumns, selectDupliactesDataSql);
			}
		} else {
			return null;
		}
	}

	private File createZippedCsvFile(String fileBaseName, List<String> csvColumns, String sql) throws Exception {
		String csvFileName = fileBaseName + ".csv";
		String zipFileName = fileBaseName + "_" + admin.getCompanyID() + "_" + datasourceId + ".zip";
		File path = AgnUtils.createDirectory(IMPORT_FILE_DIRECTORY + "/" + admin.getCompanyID());
		File zipFile = new File(path.getAbsolutePath() + File.separator + zipFileName);
		try (ZipOutputStream zipOutputStream = ZipUtilities.openNewZipOutputStream(zipFile)) {
			zipOutputStream.putNextEntry(new ZipEntry(csvFileName));
			char separator = Separator.getSeparatorById(importProfile.getSeparator()).getValueChar();
			Character stringQuote = TextRecognitionChar.getTextRecognitionCharById(importProfile.getTextRecognitionChar()).getValueCharacter();
			DbUtilities.readoutInOutputStream(importRecipientsDao.getDataSource(), sql, csvColumns, zipOutputStream, Charset.getCharsetById(importProfile.getCharset()).getCharsetName(), separator, stringQuote);
			return zipFile;
		} catch (Exception e) {
			logger.error("Cannot create file " + zipFile.getAbsolutePath() + ": " + e.getMessage(), e);
			return null;
		}
	}

	private File createZippedJsonFile(String fileBaseName, List<String> dataColumns, String sql) throws Exception {
		String csvFileName = fileBaseName + FileUtils.JSON_EXTENSION;
		String zipFileName = fileBaseName + "_" + admin.getCompanyID() + "_" + datasourceId + ".zip";
		File path = AgnUtils.createDirectory(IMPORT_FILE_DIRECTORY + "/" + admin.getCompanyID());
		File zipFile = new File(path.getAbsolutePath() + File.separator + zipFileName);
		try (ZipOutputStream zipOutputStream = ZipUtilities.openNewZipOutputStream(zipFile)) {
			zipOutputStream.putNextEntry(new ZipEntry(csvFileName));
			DbUtilities.readoutInJsonOutputStream(importRecipientsDao.getDataSource(), sql, dataColumns, zipOutputStream);
			return zipFile;
		} catch (Exception e) {
			logger.error("Cannot create file " + zipFile.getAbsolutePath() + ": " + e.getMessage(), e);
			return null;
		}
	}

	private void handleFileIsEmpty() throws Exception {
		assertEmptyFileIsAllowed();

		endTime = new Date();

		// Write logs and reports
		resultFile = profileImportReporter.writeProfileImportResultFile(this, importProfile.getCompanyId(), admin == null ? 0 : admin.getAdminID());

		// Create downloadable report files
		status.setImportedRecipientsCsv(null);
		status.setInvalidRecipientsCsv(null);
		status.setFixedByUserRecipientsCsv(null);
		status.setDuplicateInCsvOrDbRecipientsCsv(null);

		profileImportReporter.sendProfileImportReportMail(this, importProfile.getCompanyId(), importProfile.getReportLocale(), importProfile.getReportTimezone(), importProfile.getReportDateTimeFormatter());
		reportID = profileImportReporter.writeProfileImportReport(this, importProfile.getCompanyId(), admin == null ? 0 : admin.getAdminID(), importProfile.getReportLocale(), importProfile.getReportTimezone(), importProfile.getReportDateTimeFormatter(), false);
	}

	private void assertEmptyFileIsAllowed() {
		if(autoImport == null || !autoImport.isEmptyFileAllowed()) {
			throw new ImportException(false, "autoimport.error.emptyFile", importFile.getRemoteFilePath());
		}
	}
	
	public void cleanUp() {
		importRecipientsDao.dropTemporaryCustomerImportTable(importProfile.getCompanyId(), temporaryImportTableName);
		temporaryImportTableName = null;
		
		importRecipientsDao.dropTemporaryCustomerImportTable(importProfile.getCompanyId(), temporaryErrorTableName);
		temporaryErrorTableName = null;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public int getReportID() {
		return reportID;
	}
	
	/**
	 * Checks the db vendor of the dataSource and caches the result for further usage
	 * @return true if db vendor of dataSource is Oracle, false if any other vendor (e.g. mysql)
	 */
	protected final boolean isOracleDB() {
		if (IS_ORACLE_DB == null) {
			IS_ORACLE_DB = DbUtilities.checkDbVendorIsOracle(importRecipientsDao.getDataSource());
		}
		return IS_ORACLE_DB;
	}
}
