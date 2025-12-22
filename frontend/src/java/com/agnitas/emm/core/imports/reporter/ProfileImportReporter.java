/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.imports.reporter;

import static com.agnitas.emm.core.recipientsreport.service.impl.RecipientReportUtils.TXT_EXTENSION;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ColumnMapping;
import com.agnitas.beans.Company;
import com.agnitas.beans.ImportStatus;
import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.ImportRecipientsDao;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.importquota.service.ImportQuotaCheckService;
import com.agnitas.emm.core.mailinglist.dao.MailinglistDao;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;
import com.agnitas.emm.core.recipientsreport.service.RecipientsReportService;
import com.agnitas.emm.core.recipientsreport.service.impl.RecipientReportUtils;
import com.agnitas.emm.reporter.HtmlReporterHelper;
import com.agnitas.messages.I18nString;
import com.agnitas.service.DataSourceService;
import com.agnitas.service.ImportException;
import com.agnitas.service.ProfileImportWorker;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.ImportReportEntry;
import com.agnitas.util.ImportUtils.ImportErrorType;
import com.agnitas.util.importvalues.Charset;
import com.agnitas.util.importvalues.CheckForDuplicates;
import com.agnitas.util.importvalues.DateFormat;
import com.agnitas.util.importvalues.ImportMode;
import com.agnitas.util.importvalues.MailType;
import com.agnitas.util.importvalues.NullValuesAction;
import com.agnitas.util.importvalues.Separator;
import com.agnitas.util.importvalues.TextRecognitionChar;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProfileImportReporter {

	private static final Logger logger = LogManager.getLogger(ProfileImportReporter.class);
	
	private RecipientsReportService recipientsReportService;
	
	private JavaMailService javaMailService;

	private MailinglistDao mailinglistDao;
	
	private CompanyDao companyDao;
	
	private ConfigService configService;
	
	private ImportRecipientsDao importRecipientsDao;
	
	private DataSourceService dataSourceService;
	
	private ImportQuotaCheckService importQuotaCheckService;
	
	public final void setImportQuotaCheckService(final ImportQuotaCheckService service) {
		this.importQuotaCheckService = Objects.requireNonNull(service, "ImportQuotaCheckService");
	}

	public void setRecipientsReportService(RecipientsReportService recipientsReportService) {
		this.recipientsReportService = recipientsReportService;
	}

	public void setJavaMailService(JavaMailService javaMailService) {
		this.javaMailService = javaMailService;
	}

	public void setMailinglistDao(MailinglistDao mailinglistDao) {
		this.mailinglistDao = mailinglistDao;
	}

	public void setCompanyDao(CompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public void setDataSourceService(DataSourceService dataSourceService) {
		this.dataSourceService= dataSourceService;
	}
	
	public void setImportRecipientsDao(ImportRecipientsDao importRecipientsDao) {
		this.importRecipientsDao = importRecipientsDao;
	}
	
	public File writeProfileImportResultFile(ProfileImportWorker profileImportWorker, int companyID, Admin admin) {
		String resultFileContent = generateResultFileContent(profileImportWorker, admin);

		try {
			File resultFile = new File(profileImportWorker.getImportFile().getLocalFile().getAbsolutePath()
				+ "_CID" + companyID
				+ "_" + DateUtilities.getDateTimeString(profileImportWorker.getStartTime(), TimeZone.getTimeZone("Europe/Berlin").toZoneId(), AgnUtils.getDateTimeFormatterByPattern(DateUtilities.YYYY_MM_DD_HH_MM_SS_FORFILENAMES, Locale.US, true))
				+ "_" + RecipientReportUtils.IMPORT_RESULT_FILE_PREFIX + TXT_EXTENSION);
			FileUtils.writeStringToFile(resultFile, resultFileContent, "UTF-8");
			return resultFile;
		} catch (Exception e) {
			logger.error("writeProfileImportResultFile: " + e, e);
			return null;
		}
	}
	
	public int writeProfileImportReport(ProfileImportWorker profileImportWorker, int companyID, Admin admin, Locale reportLocale, String reportTimezone, DateTimeFormatter reportDateTimeFormatter, boolean isError) throws Exception {
		/*
		 * Disabled again by EMM-9770
		if(!isError) {
			this.quotaRegisterService.registerImportSize(companyID, profileImportWorker.getImportProfile().getId(), importType, profileImportWorker.getLinesInFile() - headerLines);
		}
		*/
		
		String resultFileContent = generateLocalizedImportHtmlReport(profileImportWorker, reportLocale, reportTimezone, reportDateTimeFormatter, true, profileImportWorker.getStatus());
		boolean isAutoImport = profileImportWorker.getAutoImport() != null;
		int autoImportID = -1;
		if (isAutoImport) {
			autoImportID = profileImportWorker.getAutoImport().getAutoImportId();
		}
		if (profileImportWorker.getStatus().getInvalidRecipientsCsv() != null) {
			createInvalidRecipientsReport(profileImportWorker, admin, companyID);
		}

		RecipientsReport report = new RecipientsReport();

		report.setDatasourceId(profileImportWorker.getDatasourceId());
		report.setFilename(generateProfileImportReportFileName(profileImportWorker.getDatasourceId(), companyID));
		report.setReportDate(profileImportWorker.getEndTime());
		report.setIsError(isError);

		report.setEntityId(isAutoImport ? autoImportID : profileImportWorker.getImportProfileId());
		report.setEntityType(RecipientsReport.EntityType.IMPORT);
		report.setEntityExecution(isAutoImport ? RecipientsReport.EntityExecution.AUTOMATIC : RecipientsReport.EntityExecution.MANUAL);
		report.setEntityData(RecipientsReport.EntityData.PROFILE);

		return recipientsReportService.saveNewReport(admin, companyID, report, resultFileContent).getId();
	}

	private void createInvalidRecipientsReport(ProfileImportWorker profileImportWorker, Admin admin, int companyID){
		boolean isAutoImport = profileImportWorker.getAutoImport() != null;

		RecipientsReport report = new RecipientsReport();

		report.setDatasourceId(profileImportWorker.getDatasourceId());
		report.setFilename(RecipientReportUtils.INVALID_RECIPIENTS_FILE_PREFIX + ".csv.zip");
		report.setReportDate(profileImportWorker.getEndTime());
		report.setIsError(true);

		report.setEntityId(isAutoImport ? profileImportWorker.getAutoImport().getAutoImportId() : profileImportWorker.getImportProfileId());
		report.setEntityType(RecipientsReport.EntityType.IMPORT);
		report.setEntityExecution(isAutoImport ? RecipientsReport.EntityExecution.AUTOMATIC : RecipientsReport.EntityExecution.MANUAL);
		report.setEntityData(RecipientsReport.EntityData.PROFILE);

		recipientsReportService.saveNewSupplementalReport(
				admin,
				companyID,
				report,
				"Downloadable file with invalid recipients data",
				profileImportWorker.getStatus().getInvalidRecipientsCsv()
		);
	}
	
    private String generateProfileImportReportFileName(int datasourceId, int companyId) {
        return dataSourceService.getDatasourceDescription(datasourceId, companyId).getDescription();
    }

	/**
	 * Send a report email about this import to the executing GUI-admin or the creator of the autoimport
	 */
	public void sendProfileImportReportMail(ProfileImportWorker profileImportWorker, int companyID, Locale reportLocale, String reportTimezone, DateTimeFormatter reportDateTimeFormatter) throws Exception {
		Set<String> emailRecipients = new HashSet<>();
		
		if (StringUtils.isNotBlank(profileImportWorker.getImportProfile().getMailForReport())) {
			for (String email : AgnUtils.splitAndTrimList(profileImportWorker.getImportProfile().getMailForReport())) {
				emailRecipients.add(AgnUtils.normalizeEmail(email));
			}
		}
		
		if (StringUtils.isNotBlank(configService.getValue(ConfigValue.ImportAlwaysInformEmail, companyID))) {
			for (String email : AgnUtils.splitAndTrimList(configService.getValue(ConfigValue.ImportAlwaysInformEmail, companyID))) {
				emailRecipients.add(AgnUtils.normalizeEmail(email));
			}
		}

		if (!emailRecipients.isEmpty()) {
			Locale emailLocale = reportLocale;
			
			Company company = companyDao.getCompany(profileImportWorker.getImportProfile().getCompanyId());
			
			String subject = I18nString.getLocaleString("import.recipients.report", emailLocale) + " \"" + profileImportWorker.getImportProfile().getName() + "\" (" + I18nString.getLocaleString("Company", emailLocale) + ": " + company.getShortname() + ")";
			String bodyHtml = generateLocalizedImportHtmlReport(profileImportWorker, reportLocale, reportTimezone, reportDateTimeFormatter, true, profileImportWorker.getStatus());
			String bodyText = generateLocalizedImportTextReport(profileImportWorker, reportLocale, reportTimezone, reportDateTimeFormatter, true);
			
			// Deactivated attachment of InvalidRecipientsCsvZipFile for DSGVO reasons. Must be password secured if reintegrated!
//			if (profileImportWorker.getStatus().getInvalidRecipientsCsv() != null && profileImportWorker.getStatus().getInvalidRecipientsCsv().length() < 4 * 1024 * 1024) {
//				// Only send invalid recipientsfile in email if its size is below 4 MB
//				javaMailService.sendEmail(StringUtils.join(emailRecipients, ", "), subject, bodyText, bodyHtml, new MailAttachment("invalid_recipients.zip", FileUtils.readFileToByteArray(profileImportWorker.getStatus().getInvalidRecipientsCsv()), "application/zip"));
//			} else {
				javaMailService.sendEmail(profileImportWorker.getImportProfile().getCompanyId(), StringUtils.join(emailRecipients, ", "), subject, bodyText, bodyHtml);
//			}
		}
	}

	private String generateLocalizedImportTextReport(ProfileImportWorker profileImportWorker, Locale reportLocale, String reportTimezone, DateTimeFormatter reportDateTimeFormatter, boolean showVerboseProfileData) throws Exception {
		String reportContent = I18nString.getLocaleString("import.recipients.report", reportLocale) + " \"" + profileImportWorker.getImportProfile().getName() + "\":\n\n";

		reportContent += I18nString.getLocaleString("decode.licenseID", reportLocale) + ": " + configService.getValue(ConfigValue.System_Licence) + "\n";
		
		Company company = companyDao.getCompany(profileImportWorker.getImportProfile().getCompanyId());
		reportContent += I18nString.getLocaleString("Company", reportLocale) + ": " + (company == null ? "Unknown" : company.toString()) + "\n";
		
		if (profileImportWorker.getAutoImport() != null) {
			reportContent += "AutoImport: " + profileImportWorker.getAutoImport().toString() + "\n";
		}
		
		reportContent += I18nString.getLocaleString("import.type", reportLocale) + ": " + I18nString.getLocaleString("Recipients", reportLocale) + "\n";
		
		// Show ImportProfile data
		reportContent += I18nString.getLocaleString("import.ImportProfile", reportLocale) + ": \"" + profileImportWorker.getImportProfile().getName() + "\" (ID: " + profileImportWorker.getImportProfile().getId() + ")\n";
		String profileContent = "";
		if (showVerboseProfileData) {
			profileContent += I18nString.getLocaleString("Charset", reportLocale) + ": " + Charset.getCharsetById(profileImportWorker.getImportProfile().getCharset()).getCharsetName() + "\n";

			profileContent += I18nString.getLocaleString("csv.ContainsHeaders", reportLocale) + ": " + !profileImportWorker.getImportProfile().isNoHeaders() + "\n";
			profileContent += I18nString.getLocaleString("import.zipped", reportLocale) + ": " + AgnUtils.isZipArchiveFile(profileImportWorker.getImportFile().getLocalFile()) + "\n";
			profileContent += I18nString.getLocaleString("import.zipPassword", reportLocale) + ": " + (profileImportWorker.getImportProfile().getZipPassword() != null) + "\n";
			
			profileContent += I18nString.getLocaleString("import.autoMapping", reportLocale) + ": " + profileImportWorker.getImportProfile().isAutoMapping() + "\n";
			
			profileContent += I18nString.getLocaleString("csv.Delimiter", reportLocale) + ": " + Separator.getSeparatorById(profileImportWorker.getImportProfile().getSeparator()).getValueChar() + "\n";

			profileContent += I18nString.getLocaleString("csv.StringQuote", reportLocale) + ": " + TextRecognitionChar.getTextRecognitionCharById(profileImportWorker.getImportProfile().getTextRecognitionChar()).name() + "\n";

			profileContent += I18nString.getLocaleString("csv.DecimalSeparator", reportLocale) + ": " + profileImportWorker.getImportProfile().getDecimalSeparator() + "\n";
			
			profileContent += I18nString.getLocaleString("import.dateFormat", reportLocale) + ": " + DateFormat.getDateFormatById(profileImportWorker.getImportProfile().getDateFormat()).getValue() + "\n";

			profileContent += "HtmlTagCheck: " + (configService.getBooleanValue(ConfigValue.NoHtmlCheckOnReferenceImport, profileImportWorker.getImportProfile().getCompanyId()) ? "yes" : "no") + "\n";
			profileContent += "AllowSimpleHtmlTags: " + (configService.getBooleanValue(ConfigValue.AllowHtmlTagsInReferenceAndProfileFields, profileImportWorker.getImportProfile().getCompanyId()) ? "yes" : "no") + "\n";
			
			profileContent += I18nString.getLocaleString("Mode", reportLocale) + ": " + I18nString.getLocaleString(ImportMode.getFromInt(profileImportWorker.getImportProfile().getImportMode()).getMessageKey(), reportLocale) + "\n";

			profileContent += I18nString.getLocaleString("import.recipients.duplicate", reportLocale) + ": " + CheckForDuplicates.getFromInt(profileImportWorker.getImportProfile().getCheckForDuplicates()).name() + "\n";

			profileContent += I18nString.getLocaleString("import.null_value_handling", reportLocale) + ": " + NullValuesAction.getFromInt(profileImportWorker.getImportProfile().getNullValuesAction()).name() + "\n";

			profileContent += I18nString.getLocaleString("recipient.RecipientMailtype", reportLocale) + " (" + I18nString.getLocaleString("import.profile.default", reportLocale) + "): " + MailType.getFromInt(profileImportWorker.getImportProfile().getDefaultMailType()).name() + "\n";

			try {
				profileContent += I18nString.getLocaleString("mediatype", reportLocale) + ": " + getMediaTypesText(profileImportWorker.getImportProfile().getMediatypes(), reportLocale) + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("mediatype", reportLocale) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			profileContent += I18nString.getLocaleString("import.datatype", reportLocale) + ": " + profileImportWorker.getImportProfile().getDatatype() + "\n";
			
			profileContent += I18nString.getLocaleString("import.profile.updateAllDuplicates", reportLocale) + ": " + profileImportWorker.getImportProfile().getUpdateAllDuplicates() + "\n";
			
			if (profileImportWorker.getImportProfile().getImportProcessActionID() > 0) {
				profileContent += "ImportProcessActionID" + ": " + profileImportWorker.getImportProfile().getImportProcessActionID() + "\n";
			}
			
			if (profileImportWorker.getImportProfile().getActionForNewRecipients() > 0) {
				profileContent += I18nString.getLocaleString("import.actionForNewRecipients", reportLocale) + ": " + profileImportWorker.getImportProfile().getActionForNewRecipients() + "\n";
			}
			
			profileContent += I18nString.getLocaleString("import.profile.report.email", reportLocale) + ": " + (StringUtils.isBlank(profileImportWorker.getImportProfile().getMailForReport()) ? I18nString.getLocaleString("default.none", reportLocale) : profileImportWorker.getImportProfile().getMailForReport()) + "\n";
			profileContent += I18nString.getLocaleString("error.import.profile.email", reportLocale) + ": " + (StringUtils.isBlank(profileImportWorker.getImportProfile().getMailForError()) ? I18nString.getLocaleString("default.none", reportLocale) : profileImportWorker.getImportProfile().getMailForError()) + "\n";
			
			profileContent += I18nString.getLocaleString("import.profile.gender.settings", reportLocale) + ": ";
			
			if (profileImportWorker.getImportProfile().getGenderMapping() != null && profileImportWorker.getImportProfile().getGenderMapping().size() > 0) {
				profileContent += "\n\t" + AgnUtils.mapToString(profileImportWorker.getImportProfile().getGenderMapping()).replace("\n", "\n\t") + "\n";
			} else {
				profileContent += "NONE\n";
			}
			
			profileContent += I18nString.getLocaleString("Mapping", reportLocale) + ": \n";
			for (ColumnMapping mapping : profileImportWorker.getImportProfile().getColumnMapping()) {
				String mappingEntryContent = mapping.getDatabaseColumn() + " = " + mapping.getFileColumn();
				if (mapping.isKeyColumn()) {
					mappingEntryContent += ", keycolumn";
				}
				if (StringUtils.isNotEmpty(mapping.getDefaultValue())) {
					mappingEntryContent += ", default = \"" + mapping.getDefaultValue() + "\"";
				}
				if (StringUtils.isNotEmpty(mapping.getFormat())) {
					mappingEntryContent += ", format = \"" + mapping.getFormat() + "\"";
				}
				if (mapping.isMandatory()) {
					mappingEntryContent += ", mandatory = " + mapping.isMandatory();
				}

				profileContent += "\t" + mappingEntryContent + "\n";
			}
			
			profileContent += I18nString.getLocaleString("KeyColumn", reportLocale) + ": " + StringUtils.join(profileImportWorker.getImportProfile().getKeyColumns(), ", ");
		} else {
			profileContent += I18nString.getLocaleString("Mode", reportLocale) + ": " + I18nString.getLocaleString(ImportMode.getFromInt(profileImportWorker.getImportProfile().getImportMode()).getMessageKey(), reportLocale) + "\n";

			profileContent += I18nString.getLocaleString("KeyColumn", reportLocale) + ": " + StringUtils.join(profileImportWorker.getImportProfile().getKeyColumns(), ", ");
		}
		reportContent += "\t" + profileContent.replace("\n", "\n\t") + "\n";
		
		if (CollectionUtils.isNotEmpty(profileImportWorker.getImportProfile().getKeyColumns())) {
			if (!importRecipientsDao.isKeyColumnIndexed(profileImportWorker.getImportProfile().getCompanyId(), profileImportWorker.getImportProfile().getKeyColumns())) {
				reportContent += "\n*** " + I18nString.getLocaleString("warning.import.keyColumn.index", reportLocale) + " ***\n\n";
			}
		}
		
		reportContent += I18nString.getLocaleString("StartTime", reportLocale) + ": " + DateUtilities.getDateTimeString(profileImportWorker.getStartTime(), TimeZone.getTimeZone(reportTimezone).toZoneId(), reportDateTimeFormatter) + "\n";
		reportContent += I18nString.getLocaleString("EndTime", reportLocale) + ": " + DateUtilities.getDateTimeString(profileImportWorker.getEndTime(), TimeZone.getTimeZone(reportTimezone).toZoneId(), reportDateTimeFormatter) + "\n";
		
		if (profileImportWorker.getAutoImport() != null) {
			reportContent += I18nString.getLocaleString("autoImport.fileServer", reportLocale) + ": " + profileImportWorker.getAutoImport().getFileServerWithoutCredentials();
			reportContent += I18nString.getLocaleString("autoImport.filePath", reportLocale) + ": " + profileImportWorker.getAutoImport().getFilePath();
			reportContent += I18nString.getLocaleString("autoImport.importMultipleFiles", reportLocale) + ": " + I18nString.getLocaleString(profileImportWorker.getAutoImport().isImportMultipleFiles() ? "default.Yes" : "No", reportLocale);
			reportContent += I18nString.getLocaleString("autoImport.removeImportedFiles", reportLocale) + ": " + I18nString.getLocaleString(profileImportWorker.getAutoImport().isRemoveImportedFiles() ? "default.Yes" : "No", reportLocale);
		}
		reportContent += I18nString.getLocaleString("settings.FileName", reportLocale) + ": " + (StringUtils.isBlank(profileImportWorker.getImportFile().getRemoteFilePath()) ? I18nString.getLocaleString("Unknown", reportLocale) : profileImportWorker.getImportFile().getRemoteFilePath()) + "\n";
		if (profileImportWorker.getImportFile().getDownloadDurationMillis() > -1) {
			reportContent += I18nString.getLocaleString("DownloadDuration", reportLocale) + ": " + AgnUtils.getHumanReadableDurationFromMillis(profileImportWorker.getImportFile().getDownloadDurationMillis()) + "\n";
		}
		
		reportContent += "\n";
		
		List<ImportReportEntry> reportStatusEntries = generateImportStatusEntries(profileImportWorker, profileImportWorker.getImportProfile().isNoHeaders());
		for (ImportReportEntry entry : reportStatusEntries) {
			reportContent += I18nString.getLocaleString(entry.getKey(), reportLocale) + ": " + entry.getValue() + "\n";
		}
		
		reportContent += "\n";

		Map<MediaTypes, Map<Integer, Integer>> mailinglistAssignStatistics = profileImportWorker.getMailinglistAssignStatistics();
		if (mailinglistAssignStatistics != null && mailinglistAssignStatistics.size() > 0) {
			reportContent += I18nString.getLocaleString("import.result.mailinglist.data", reportLocale) +":\n";
			for (MediaTypes mediaType : mailinglistAssignStatistics.keySet()) {
				if (mailinglistAssignStatistics.keySet().size() > 1) {
					reportContent += "\t" + mediaType.name() + ":";
				}
				
				for (Entry<Integer, Integer> entry : mailinglistAssignStatistics.get(mediaType).entrySet()) {
					reportContent += "\t\"" + I18nString.getLocaleString("Mailinglist", reportLocale) + " " + mailinglistDao.getMailinglistName(entry.getKey(), profileImportWorker.getImportProfile().getCompanyId()) + "\" (ID: " + entry.getKey() + "): \n";
					
					Map<Integer, Map<MediaTypes, Map<UserStatus, Integer>>> importedRecipientStatuses = profileImportWorker.getMailinglistStatusesForImportedRecipients();
					for (UserStatus userStatus : UserStatus.values()) {
						Integer amount = importedRecipientStatuses.get(entry.getKey()).get(mediaType).get(userStatus);
						reportContent += "\t\t" + I18nString.getLocaleString(userStatus.getMessageKey(), reportLocale) + ": " + Integer.toString(amount != null ? amount : 0);
					}
				}
			}
		}
		return reportContent;
	}
	
	public String generateLocalizedImportHtmlReport(ProfileImportWorker importWorker, Locale reportLocale, String reportTimezone, DateTimeFormatter reportDateTimeFormatter, boolean showVerboseProfileData, final ImportStatus status) throws Exception {
		Locale locale = reportLocale;
		String title;
		if (importWorker.getAutoImport() != null) {
			title = "AutoImport: " + importWorker.getAutoImport().toString();
		} else {
			title = "Import: " + importWorker.getImportProfile().getName();
		}
		
		StringBuilder htmlContent = new StringBuilder(HtmlReporterHelper.getHtmlPrefixWithCssStyles(title));
		
		htmlContent.append(HtmlReporterHelper.getHeader(title, I18nString.getLocaleString("default.version", locale)));
		
		// Fatal Error
		if (importWorker.getStatus().getFatalError() != null) {
			htmlContent.append(HtmlReporterHelper.getOutputTableStart());
			htmlContent.append(HtmlReporterHelper.getOutputTableHeader(I18nString.getLocaleString("import.csv_fatal_error", locale)));
			htmlContent.append(HtmlReporterHelper.getOutputTableContentStart(false));
			
			String errorMessage;
			if (importWorker.getError() != null && importWorker.getError() instanceof ImportException importException) {
				errorMessage = I18nString.getLocaleString(importException.getErrorMessageKey(), locale, importException.getAdditionalErrorData());
			} else {
				errorMessage = importWorker.getStatus().getFatalError();
			}
			htmlContent.append(HtmlReporterHelper.getOutputTableErrorContentLine(errorMessage));

			htmlContent.append(HtmlReporterHelper.getOutputTableContentEnd());
			htmlContent.append(HtmlReporterHelper.getOutputTableEnd());
		}
		
		// Warning messages
		final List<String> warningMessages = new ArrayList<>();
		
		// Index Warning
		if (CollectionUtils.isNotEmpty(importWorker.getImportProfile().getKeyColumns())) {
			if (!importRecipientsDao.isKeyColumnIndexed(importWorker.getImportProfile().getCompanyId(), importWorker.getImportProfile().getKeyColumns())) {
				warningMessages.add(I18nString.getLocaleString("warning.import.keyColumn.index", locale));
			}
		}
		
		if(importWorker.getError() == null || ((importWorker.getError() instanceof ImportException importException) && !importException.getErrorMessageKey().equals("error.import.lineQuotaExceeded"))) {
			if(this.importQuotaCheckService.checkWarningLimitReached(importWorker.getImportProfile().getCompanyId(), status.getCsvLines())) {
				warningMessages.add(I18nString.getLocaleString(
						"warning.import.lineQuota", 
						locale, 
						this.importQuotaCheckService.getImportLimit(importWorker.getImportProfile().getCompanyId()),
						this.importQuotaCheckService.totalImportLinesCount(importWorker.getImportProfile().getCompanyId(), importWorker.getStatus().getCsvLines())));			
			}
		}
		
		if(!warningMessages.isEmpty()) {
			htmlContent.append(HtmlReporterHelper.getOutputTableStart());
			htmlContent.append(HtmlReporterHelper.getOutputTableContentStart(false));

			for(final String warning : warningMessages) {
				htmlContent.append(HtmlReporterHelper.getOutputTableWarningContentLine(warning));
			}
			
			htmlContent.append(HtmlReporterHelper.getOutputTableContentEnd());
			htmlContent.append(HtmlReporterHelper.getOutputTableEnd());
		}
		
		if (importWorker.getError() == null) {
			// Errors
			htmlContent.append(HtmlReporterHelper.getOutputTableStart());
			htmlContent.append(HtmlReporterHelper.getOutputTableHeader(I18nString.getLocaleString("report.summary", locale)));
			htmlContent.append(HtmlReporterHelper.getOutputTableContentStart(true));
	
			htmlContent.append(HtmlReporterHelper.getOutputTableSummaryContentLine(I18nString.getLocaleString("import.csv_errors_email", locale), String.valueOf(importWorker.getStatus().getError(ImportErrorType.EMAIL_ERROR)), ((Integer) importWorker.getStatus().getError(ImportErrorType.EMAIL_ERROR)) > 0));
			htmlContent.append(HtmlReporterHelper.getOutputTableSummaryContentLine(I18nString.getLocaleString("import.csv_errors_invalidNullValues", locale), Integer.toString(importWorker.getStatus().getInvalidNullValues()), importWorker.getStatus().getInvalidNullValues() > 0));
			htmlContent.append(HtmlReporterHelper.getOutputTableSummaryContentLine(I18nString.getLocaleString("import.csv_errors_blacklist", locale), String.valueOf(importWorker.getStatus().getError(ImportErrorType.BLACKLIST_ERROR))));
			htmlContent.append(HtmlReporterHelper.getOutputTableSummaryContentLine(I18nString.getLocaleString("import.csv_errors_double", locale), String.valueOf(importWorker.getStatus().getError(ImportErrorType.KEYDOUBLE_ERROR)), ((Integer) importWorker.getStatus().getError(ImportErrorType.KEYDOUBLE_ERROR)) > 0));
			htmlContent.append(HtmlReporterHelper.getOutputTableSummaryContentLine(I18nString.getLocaleString("import.csv_errors_numeric", locale), String.valueOf(importWorker.getStatus().getError(ImportErrorType.NUMERIC_ERROR)), ((Integer) importWorker.getStatus().getError(ImportErrorType.NUMERIC_ERROR)) > 0));
			htmlContent.append(HtmlReporterHelper.getOutputTableSummaryContentLine(I18nString.getLocaleString("import.csv_errors_mailtype", locale), String.valueOf(importWorker.getStatus().getError(ImportErrorType.MAILTYPE_ERROR)), ((Integer) importWorker.getStatus().getError(ImportErrorType.MAILTYPE_ERROR)) > 0));
			htmlContent.append(HtmlReporterHelper.getOutputTableSummaryContentLine(I18nString.getLocaleString("import.csv_errors_gender", locale), String.valueOf(importWorker.getStatus().getError(ImportErrorType.GENDER_ERROR)), ((Integer) importWorker.getStatus().getError(ImportErrorType.GENDER_ERROR)) > 0));
			htmlContent.append(HtmlReporterHelper.getOutputTableSummaryContentLine(I18nString.getLocaleString("import.csv_errors_date", locale), String.valueOf(importWorker.getStatus().getError(ImportErrorType.DATE_ERROR)), ((Integer) importWorker.getStatus().getError(ImportErrorType.DATE_ERROR)) > 0));
			htmlContent.append(HtmlReporterHelper.getOutputTableSummaryContentLine(I18nString.getLocaleString("csv_errors_linestructure", locale), String.valueOf(importWorker.getStatus().getError(ImportErrorType.STRUCTURE_ERROR)), ((Integer) importWorker.getStatus().getError(ImportErrorType.STRUCTURE_ERROR)) > 0));
			htmlContent.append(HtmlReporterHelper.getOutputTableSummaryContentLine(I18nString.getLocaleString("error.import.value.large", locale), String.valueOf(importWorker.getStatus().getError(ImportErrorType.VALUE_TOO_LARGE_ERROR)), ((Integer) importWorker.getStatus().getError(ImportErrorType.VALUE_TOO_LARGE_ERROR)) > 0));
			htmlContent.append(HtmlReporterHelper.getOutputTableSummaryContentLine(I18nString.getLocaleString("error.import.number.large", locale), String.valueOf(importWorker.getStatus().getError(ImportErrorType.NUMBER_TOO_LARGE_ERROR)), ((Integer) importWorker.getStatus().getError(ImportErrorType.NUMBER_TOO_LARGE_ERROR)) > 0));
			htmlContent.append(HtmlReporterHelper.getOutputTableSummaryContentLine(I18nString.getLocaleString("error.import.invalidFormat", locale), String.valueOf(importWorker.getStatus().getError(ImportErrorType.INVALID_FORMAT_ERROR)), ((Integer) importWorker.getStatus().getError(ImportErrorType.INVALID_FORMAT_ERROR)) > 0));
			htmlContent.append(HtmlReporterHelper.getOutputTableSummaryContentLine(I18nString.getLocaleString("error.import.missingMandatory", locale), String.valueOf(importWorker.getStatus().getError(ImportErrorType.MISSING_MANDATORY_ERROR)), ((Integer) importWorker.getStatus().getError(ImportErrorType.MISSING_MANDATORY_ERROR)) > 0));
			
			if (importWorker.getStatus().getErrorColumns().size() > 0) {
				htmlContent.append(HtmlReporterHelper.getOutputTableSummaryContentLine(I18nString.getLocaleString("error.import.errorColumns", locale), StringUtils.join(importWorker.getStatus().getErrorColumns(), ", "), true));
			}
	
			htmlContent.append(HtmlReporterHelper.getOutputTableSummaryContentLine(I18nString.getLocaleString("import.action_errors", locale), String.valueOf(importWorker.getStatus().getError(ImportErrorType.ACTION_ERROR)), ((Integer) importWorker.getStatus().getError(ImportErrorType.ACTION_ERROR)) > 0));
			
			htmlContent.append(HtmlReporterHelper.getOutputTableContentEnd());
	
			// Results
			htmlContent.append(HtmlReporterHelper.getOutputTableHeader(I18nString.getLocaleString("ResultMsg", locale)));
			htmlContent.append(HtmlReporterHelper.getOutputTableContentStart(true));

			if (importWorker.getStatus().getFileSize() >= 0) {
				htmlContent.append(HtmlReporterHelper.getOutputTableResultContentLine(I18nString.getLocaleString("mailing.Graphics_Component.FileSize", locale), AgnUtils.getHumanReadableNumber(importWorker.getStatus().getFileSize(), "Byte", false, locale)));
			}
	
			if ("CSV".equalsIgnoreCase(importWorker.getImportProfile().getDatatype())) {
				if (importWorker.getImportProfile().isNoHeaders()) {
					htmlContent.append(HtmlReporterHelper.getOutputTableResultContentLine(I18nString.getLocaleString("import.result.csvlines", locale), AgnUtils.getHumanReadableNumber(importWorker.getStatus().getCsvLines(), locale)));
				} else if (importWorker.getStatus().getCsvLines() <= 0) {
					// Maybe there was an error in csv structure
					htmlContent.append(HtmlReporterHelper.getOutputTableResultContentLine(I18nString.getLocaleString("import.result.csvlines", locale), I18nString.getLocaleString("Unknown", locale)));
				} else {
					htmlContent.append(HtmlReporterHelper.getOutputTableResultContentLine(I18nString.getLocaleString("import.result.csvlines", locale), AgnUtils.getHumanReadableNumber(importWorker.getStatus().getCsvLines(), locale) + " (+1 " + I18nString.getLocaleString("csv.ContainsHeaders", locale) + ")"));
				}
			} else {
				if (importWorker.getStatus().getCsvLines() <= 0) {
					// Maybe there was an error in data file structure
					htmlContent.append(HtmlReporterHelper.getOutputTableResultContentLine(I18nString.getLocaleString("import.result.filedataitems", locale), I18nString.getLocaleString("Unknown", locale)));
				} else {
					htmlContent.append(HtmlReporterHelper.getOutputTableResultContentLine(I18nString.getLocaleString("import.result.filedataitems", locale), AgnUtils.getHumanReadableNumber(importWorker.getStatus().getCsvLines(), locale)));
				}
			}
		
			htmlContent.append(HtmlReporterHelper.getOutputTableResultContentLine(I18nString.getLocaleString("import.RecipientsAllreadyinDB", locale), Integer.toString(importWorker.getStatus().getAlreadyInDb())));
			htmlContent.append(HtmlReporterHelper.getOutputTableResultContentLine(I18nString.getLocaleString("import.result.imported", locale), Integer.toString(importWorker.getStatus().getInserted())));
			htmlContent.append(HtmlReporterHelper.getOutputTableResultContentLine(I18nString.getLocaleString("import.result.updated", locale), Integer.toString(importWorker.getStatus().getUpdated())));
	
			if (importWorker.getStatus().getBlacklisted() >= 0) {
				htmlContent.append(HtmlReporterHelper.getOutputTableResultContentLine(I18nString.getLocaleString("import.result.blacklisted", locale), Integer.toString(importWorker.getStatus().getBlacklisted())));
			}
			
			htmlContent.append(HtmlReporterHelper.getOutputTableResultContentLine(I18nString.getLocaleString("import.result.datasourceId", locale), Integer.toString(importWorker.getStatus().getDatasourceID())));
	
			htmlContent.append(HtmlReporterHelper.getOutputTableContentEnd());
			
			// Mailinglists
			Map<MediaTypes, Map<Integer, Integer>> mailinglistAssignStatistics = importWorker.getMailinglistAssignStatistics();
			if (mailinglistAssignStatistics != null && mailinglistAssignStatistics.size() > 0) {
				htmlContent.append(HtmlReporterHelper.getOutputTableHeader(I18nString.getLocaleString("import.result.mailinglist.data", locale)));
		
				for (MediaTypes mediaType : mailinglistAssignStatistics.keySet()) {
					if (mailinglistAssignStatistics.keySet().size() > 1) {
						htmlContent.append(mediaType.name() + ":\n");
					}
					for (Entry<Integer, Integer> entry : mailinglistAssignStatistics.get(mediaType).entrySet()) {
						htmlContent.append(HtmlReporterHelper.getOutputTableHeader(I18nString.getLocaleString("Mailinglist", locale) + " " + "\"" + mailinglistDao.getMailinglistName(entry.getKey(), importWorker.getImportProfile().getCompanyId()) + "\" (ID: " + entry.getKey() + ")"));
	
						htmlContent.append(HtmlReporterHelper.getOutputTableContentStart(true));
						Map<Integer, Map<MediaTypes, Map<UserStatus, Integer>>> importedRecipientStatuses = importWorker.getMailinglistStatusesForImportedRecipients();
						for (UserStatus userStatus : UserStatus.values()) {
							Integer amount = importedRecipientStatuses.get(entry.getKey()).get(mediaType).get(userStatus);
							htmlContent.append(HtmlReporterHelper.getOutputTableResultContentLine(I18nString.getLocaleString(userStatus.getMessageKey(), locale), Integer.toString(amount != null ? amount : 0)));
						}
						htmlContent.append(HtmlReporterHelper.getOutputTableContentEnd());
					}
				}
			}
		
			htmlContent.append(HtmlReporterHelper.getOutputTableEnd());
		}

		// Informations
		htmlContent.append(HtmlReporterHelper.getOutputTableStart());
		htmlContent.append(HtmlReporterHelper.getOutputTableHeader(I18nString.getLocaleString("Info", locale)));
		htmlContent.append(HtmlReporterHelper.getOutputTableContentStart(false));
		
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("decode.licenseID", locale), configService.getValue(ConfigValue.System_Licence)));

		Company company = companyDao.getCompany(importWorker.getImportProfile().getCompanyId());
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Company", locale), (company == null ? "Unknown" : company.toString())));
		
		if (importWorker.getAutoImport() != null) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("AutoImport", importWorker.getAutoImport().toString()));
		}
		
		if (importWorker.getUsername() != null) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("User", importWorker.getUsername()));
		}
		
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.type", locale), I18nString.getLocaleString("Recipients", locale)));
		
		// Importprofile
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.ImportProfile", locale), "\"" + importWorker.getImportProfile().getName() + "\" (ID: " + importWorker.getImportProfile().getId() + ")"));
		
		if (showVerboseProfileData) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Charset", locale), Charset.getCharsetById(importWorker.getImportProfile().getCharset()).getCharsetName()));

			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.ContainsHeaders", locale), I18nString.getLocaleString(!importWorker.getImportProfile().isNoHeaders() ? "default.Yes" : "No", locale)));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.zipped", locale), I18nString.getLocaleString(AgnUtils.isZipArchiveFile(importWorker.getImportFile().getLocalFile()) ? "default.Yes" : "No", locale)));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.zipPassword", locale), I18nString.getLocaleString(importWorker.getImportProfile().getZipPassword() != null ? "default.Yes" : "No", locale)));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.autoMapping", locale), I18nString.getLocaleString(importWorker.getImportProfile().isAutoMapping() ? "default.Yes" : "No", locale)));

			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.Delimiter", locale), Character.toString(Separator.getSeparatorById(importWorker.getImportProfile().getSeparator()).getValueChar())));

			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.StringQuote", locale), I18nString.getLocaleString(TextRecognitionChar.getTextRecognitionCharById(importWorker.getImportProfile().getTextRecognitionChar()).getPublicValue(), locale)));

			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.DecimalSeparator", locale), Character.toString(importWorker.getImportProfile().getDecimalSeparator())));
			
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.dateFormat", locale), DateFormat.getDateFormatById(importWorker.getImportProfile().getDateFormat()).getValue()));

			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("HtmlTagCheck", I18nString.getLocaleString(configService.getBooleanValue(ConfigValue.NoHtmlCheckOnReferenceImport, importWorker.getImportProfile().getCompanyId()) ? "default.Yes" : "No", locale)));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("AllowSimpleHtmlTags", I18nString.getLocaleString(configService.getBooleanValue(ConfigValue.AllowHtmlTagsInReferenceAndProfileFields, importWorker.getImportProfile().getCompanyId()) ? "default.Yes" : "No", locale)));
			
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Mode", locale), I18nString.getLocaleString(ImportMode.getFromInt(importWorker.getImportProfile().getImportMode()).getMessageKey(), locale)));

			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.recipients.duplicate", locale), I18nString.getLocaleString(CheckForDuplicates.getFromInt(importWorker.getImportProfile().getCheckForDuplicates()).getMessageKey(), locale)));

			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.null_value_handling", locale), I18nString.getLocaleString(NullValuesAction.getFromInt(importWorker.getImportProfile().getNullValuesAction()).getMessageKey(), locale)));

			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("recipient.RecipientMailtype", locale) + " (" + I18nString.getLocaleString("import.profile.default", locale) + ")", MailType.getFromInt(importWorker.getImportProfile().getDefaultMailType()).name()));

			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("mediatype", locale), getMediaTypesText(importWorker.getImportProfile().getMediatypes(), reportLocale)));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("mediatype", locale), "Invalid (\"" + e.getMessage() + "\")"));
			}
			
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.datatype", locale), importWorker.getImportProfile().getDatatype()));
			
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.profile.updateAllDuplicates", locale), I18nString.getLocaleString(importWorker.getImportProfile().getUpdateAllDuplicates() ? "default.Yes" : "No", locale)));
			
			if (importWorker.getImportProfile().getImportProcessActionID() > 0) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("ImportProcessActionID", Integer.toString(importWorker.getImportProfile().getImportProcessActionID())));
			}
			
			if (importWorker.getImportProfile().getActionForNewRecipients() > 0) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.actionForNewRecipients", locale), Integer.toString(importWorker.getImportProfile().getActionForNewRecipients())));
			}
			
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.profile.report.email", locale), StringUtils.isBlank(importWorker.getImportProfile().getMailForReport()) ? I18nString.getLocaleString("default.none", locale) : importWorker.getImportProfile().getMailForReport()));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("error.import.profile.email", locale), StringUtils.isBlank(importWorker.getImportProfile().getMailForError()) ? I18nString.getLocaleString("default.none", locale) : importWorker.getImportProfile().getMailForError()));

			htmlContent.append(HtmlReporterHelper.getOutputTableSubHeader(I18nString.getLocaleString("import.profile.gender.settings", locale), false));
			if (importWorker.getImportProfile().getGenderMapping() != null && importWorker.getImportProfile().getGenderMapping().size() > 0) {
				for (Entry<String, Integer> genderEntry : importWorker.getImportProfile().getGenderMapping().entrySet()) {
					htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("", genderEntry.getKey() + " = " + genderEntry.getValue()));
				}
			} else {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("", I18nString.getLocaleString("default.none", locale)));
			}

			htmlContent.append(HtmlReporterHelper.getOutputTableSubHeader(I18nString.getLocaleString("Mapping", locale), false));
			for (ColumnMapping mapping : importWorker.getImportProfile().getColumnMapping()) {
				String mappingEntryContent = mapping.getDatabaseColumn() + " = " + mapping.getFileColumn();
				if (mapping.isKeyColumn()) {
					mappingEntryContent += ", keycolumn";
				}
				if (StringUtils.isNotEmpty(mapping.getDefaultValue())) {
					mappingEntryContent += ", default = \"" + mapping.getDefaultValue() + "\"";
				}
				if (StringUtils.isNotEmpty(mapping.getFormat())) {
					mappingEntryContent += ", format = \"" + mapping.getFormat() + "\"";
				}
				if (mapping.isMandatory()) {
					mappingEntryContent += ", mandatory = " + mapping.isMandatory();
				}

				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("", mappingEntryContent));
			}

			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("KeyColumn", locale), StringUtils.join(importWorker.getImportProfile().getKeyColumns(), ", ")));
		} else {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Mode", locale), I18nString.getLocaleString(ImportMode.getFromInt(importWorker.getImportProfile().getImportMode()).getMessageKey(), locale)));

			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("KeyColumn", locale), StringUtils.join(importWorker.getImportProfile().getKeyColumns(), ", ")));
		}
		
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("StartTime", locale), DateUtilities.getDateTimeString(importWorker.getStartTime(), TimeZone.getTimeZone(reportTimezone).toZoneId(), reportDateTimeFormatter)));
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("EndTime", locale), DateUtilities.getDateTimeString(importWorker.getEndTime(), TimeZone.getTimeZone(reportTimezone).toZoneId(), reportDateTimeFormatter)));

		if (importWorker.getAutoImport() != null) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("autoImport.fileServer", locale), importWorker.getAutoImport().getFileServerWithoutCredentials()));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("autoImport.filePath", locale), importWorker.getAutoImport().getFilePath()));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("autoImport.importMultipleFiles", locale), I18nString.getLocaleString(importWorker.getAutoImport().isImportMultipleFiles() ? "default.Yes" : "No", locale)));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("autoImport.removeImportedFiles", locale), I18nString.getLocaleString(importWorker.getAutoImport().isRemoveImportedFiles() ? "default.Yes" : "No", locale)));
		}
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("settings.FileName", locale), StringUtils.isBlank(importWorker.getImportFile().getRemoteFilePath()) ? I18nString.getLocaleString("Unknown", locale) : importWorker.getImportFile().getRemoteFilePath()));
		if (importWorker.getImportFile().getDownloadDurationMillis() > -1) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("DownloadDuration", locale), AgnUtils.getHumanReadableDurationFromMillis(importWorker.getImportFile().getDownloadDurationMillis())));
		}

		htmlContent.append(HtmlReporterHelper.getOutputTableContentEnd());
		htmlContent.append(HtmlReporterHelper.getOutputTableEnd());
		
		htmlContent.append(HtmlReporterHelper.getFooter(AgnUtils.getHostName(), configService.getValue(ConfigValue.ApplicationVersion)));
		
		return htmlContent.toString();
	}
	
	private String getMediaTypesText(Set<MediaTypes> mediatypes, Locale locale) {
		StringBuilder result = new StringBuilder();
		for (MediaTypes mediaType : mediatypes) {
			if (result.length() > 0) {
				result.append(", ");
			}
			result.append(I18nString.getLocaleString("mailing.MediaType." + mediaType.getMediaCode(), locale));
		}
		return result.toString();
	}

	public List<ImportReportEntry> generateImportStatusEntries(ProfileImportWorker importWorker, boolean noHeaders) {
		Locale locale = importWorker.getImportProfile().getReportLocale();
		List<ImportReportEntry> reportStatusEntries = new ArrayList<>();

		ImportStatus customerImportStatus = importWorker.getStatus();
		if (customerImportStatus.getFatalError() != null) {
			String errorMessage;
			if (importWorker.getError() != null && importWorker.getError() instanceof ImportException) {
				errorMessage = I18nString.getLocaleString(((ImportException) importWorker.getError()).getErrorMessageKey(), locale, ((ImportException) importWorker.getError()).getAdditionalErrorData());
			} else {
				errorMessage = importWorker.getStatus().getFatalError();
			}
			
			reportStatusEntries.add(new ImportReportEntry("import.csv_fatal_error", errorMessage));
		}

		reportStatusEntries.add(new ImportReportEntry("import.csv_errors_email", String.valueOf(customerImportStatus.getError(ImportErrorType.EMAIL_ERROR))));
		reportStatusEntries.add(new ImportReportEntry("import.csv_errors_invalidNullValues", String.valueOf(customerImportStatus.getInvalidNullValues())));
		reportStatusEntries.add(new ImportReportEntry("import.csv_errors_blacklist", String.valueOf(customerImportStatus.getError(ImportErrorType.BLACKLIST_ERROR))));
		reportStatusEntries.add(new ImportReportEntry("import.csv_errors_double", String.valueOf(customerImportStatus.getError(ImportErrorType.KEYDOUBLE_ERROR))));
		reportStatusEntries.add(new ImportReportEntry("import.csv_errors_numeric", String.valueOf(customerImportStatus.getError(ImportErrorType.NUMERIC_ERROR))));
		reportStatusEntries.add(new ImportReportEntry("import.csv_errors_mailtype", String.valueOf(customerImportStatus.getError(ImportErrorType.MAILTYPE_ERROR))));
		reportStatusEntries.add(new ImportReportEntry("import.csv_errors_gender", String.valueOf(customerImportStatus.getError(ImportErrorType.GENDER_ERROR))));
		reportStatusEntries.add(new ImportReportEntry("import.csv_errors_date", String.valueOf(customerImportStatus.getError(ImportErrorType.DATE_ERROR))));
		reportStatusEntries.add(new ImportReportEntry("csv_errors_linestructure", String.valueOf(customerImportStatus.getError(ImportErrorType.STRUCTURE_ERROR))));
		reportStatusEntries.add(new ImportReportEntry("error.import.value.large", String.valueOf(customerImportStatus.getError(ImportErrorType.VALUE_TOO_LARGE_ERROR))));
		reportStatusEntries.add(new ImportReportEntry("error.import.number.large", String.valueOf(customerImportStatus.getError(ImportErrorType.NUMBER_TOO_LARGE_ERROR))));
		reportStatusEntries.add(new ImportReportEntry("error.import.invalidFormat", String.valueOf(customerImportStatus.getError(ImportErrorType.INVALID_FORMAT_ERROR))));
		reportStatusEntries.add(new ImportReportEntry("error.import.missingMandatory", String.valueOf(customerImportStatus.getError(ImportErrorType.MISSING_MANDATORY_ERROR))));
		
		if (customerImportStatus.getErrorColumns().size() > 0) {
			reportStatusEntries.add(new ImportReportEntry("error.import.errorColumns", StringUtils.join(customerImportStatus.getErrorColumns(), ", ")));
		}
		
		reportStatusEntries.add(new ImportReportEntry("import.action_errors", String.valueOf(customerImportStatus.getError(ImportErrorType.ACTION_ERROR))));
		if (customerImportStatus.getFileSize() >= 0) {
			reportStatusEntries.add(new ImportReportEntry("mailing.Graphics_Component.FileSize", AgnUtils.getHumanReadableNumber(customerImportStatus.getFileSize(), "Byte", false, locale)));
		}
		
		if ("CSV".equalsIgnoreCase(importWorker.getImportProfile().getDatatype())) {
			if (noHeaders) {
				reportStatusEntries.add(new ImportReportEntry("import.result.csvlines",  AgnUtils.getHumanReadableNumber(customerImportStatus.getCsvLines(), locale)));
			} else if (customerImportStatus.getCsvLines() <= 0) {
				// Maybe there was an error in csv structure
				reportStatusEntries.add(new ImportReportEntry("import.result.csvlines", I18nString.getLocaleString("Unknown", locale)));
			} else {
				reportStatusEntries.add(new ImportReportEntry("import.result.csvlines",  AgnUtils.getHumanReadableNumber(customerImportStatus.getCsvLines(), locale) + " (+1 " + I18nString.getLocaleString("csv.ContainsHeaders", locale) + ")"));
			}
		} else {
			if (customerImportStatus.getCsvLines() <= 0) {
				// Maybe there was an error in data file structure
				reportStatusEntries.add(new ImportReportEntry("import.result.filedataitems", I18nString.getLocaleString("Unknown", locale)));
			} else {
				reportStatusEntries.add(new ImportReportEntry("import.result.filedataitems",  AgnUtils.getHumanReadableNumber(customerImportStatus.getCsvLines(), locale)));
			}
		}
		
		reportStatusEntries.add(new ImportReportEntry("import.RecipientsAllreadyinDB", String.valueOf(customerImportStatus.getAlreadyInDb())));
		reportStatusEntries.add(new ImportReportEntry("import.result.imported", String.valueOf(customerImportStatus.getInserted())));
		reportStatusEntries.add(new ImportReportEntry("import.result.updated", String.valueOf(customerImportStatus.getUpdated())));
		if (customerImportStatus.getBlacklisted() > -1) {
			reportStatusEntries.add(new ImportReportEntry("import.result.blacklisted", String.valueOf(customerImportStatus.getBlacklisted())));
		}
		reportStatusEntries.add(new ImportReportEntry("import.result.datasourceId", String.valueOf(customerImportStatus.getDatasourceID())));
		
		return reportStatusEntries;
	}

	private String generateResultFileContent(ProfileImportWorker profileImportWorker, Admin admin) {
		String resultFileContent = "";

		resultFileContent += "Importname: " + profileImportWorker.getImportProfile().getName() + "\n";
		resultFileContent += "Start time: " + DateUtilities.getDateTimeString(profileImportWorker.getStartTime(), TimeZone.getTimeZone("Europe/Berlin").toZoneId(), AgnUtils.getDateTimeFormatterByPattern(DateUtilities.DD_MM_YYYY_HH_MM_SS, Locale.US, true)) + "\n";
		resultFileContent += "End time: " + DateUtilities.getDateTimeString(profileImportWorker.getEndTime(), TimeZone.getTimeZone("Europe/Berlin").toZoneId(), AgnUtils.getDateTimeFormatterByPattern(DateUtilities.DD_MM_YYYY_HH_MM_SS, Locale.US, true)) + "\n";
		resultFileContent += "License id: " + configService.getValue(ConfigValue.System_Licence) + "\n";
		resultFileContent += "Company id: " + profileImportWorker.getImportProfile().getCompanyId() + "\n";
		if (admin != null) {
			resultFileContent += "Admin id: " + admin.getAdminID() + "\n";
		}

		if (profileImportWorker.getAutoImport() != null) {
			resultFileContent += "AutoImport: " + profileImportWorker.getAutoImport().toString() + "\n";
			resultFileContent += "Remote file server: " + profileImportWorker.getAutoImport().getFileServerWithoutCredentials() + "\n";
			resultFileContent += "Remote file pattern: " + profileImportWorker.getAutoImport().getFilePath() + "\n";
			resultFileContent += "Import multiple files: " + profileImportWorker.getAutoImport().isImportMultipleFiles() + "\n";
			resultFileContent += "Remove imported files: " + profileImportWorker.getAutoImport().isRemoveImportedFiles() + "\n";
		}
		
		if (profileImportWorker.getUsername() != null) {
			resultFileContent += "User: " + profileImportWorker.getUsername() + "\n";
		}
		
		resultFileContent += "Remote filename: " + (StringUtils.isBlank(profileImportWorker.getImportFile().getRemoteFilePath()) ? "unknown" : profileImportWorker.getImportFile().getRemoteFilePath()) + "\n";
		if (profileImportWorker.getImportFile().getDownloadDurationMillis() > -1) {
			resultFileContent += "Download duration: " + AgnUtils.getHumanReadableDurationFromMillis(profileImportWorker.getImportFile().getDownloadDurationMillis()) + "\n";
		}
		resultFileContent += "Local import file: " + profileImportWorker.getImportFile().getLocalFile().getAbsolutePath() + "\n";

		resultFileContent += "ImportProfile: \n\t" + profileImportWorker.getImportProfile().toString().replace("\n", "\n\t") + "\n";
		
		resultFileContent += "HtmlTagCheck: " + (configService.getBooleanValue(ConfigValue.NoHtmlCheckOnReferenceImport, profileImportWorker.getImportProfile().getCompanyId()) ? "yes" : "no") + "\n";
		resultFileContent += "AllowSimpleHtmlTags: " + (configService.getBooleanValue(ConfigValue.AllowHtmlTagsInReferenceAndProfileFields, profileImportWorker.getImportProfile().getCompanyId()) ? "yes" : "no") + "\n";
		
		if (CollectionUtils.isNotEmpty(profileImportWorker.getImportProfile().getKeyColumns())) {
			if (!importRecipientsDao.isKeyColumnIndexed(profileImportWorker.getImportProfile().getCompanyId(), profileImportWorker.getImportProfile().getKeyColumns())) {
				resultFileContent += "\n*** Keycolumn has no index ***\n\n";
			}
		}

		List<ImportReportEntry> reportStatusEntries = generateImportStatusEntries(profileImportWorker, profileImportWorker.getImportProfile().isNoHeaders());
		for (ImportReportEntry entry : reportStatusEntries) {
			if (resultFileContent.length() > 0) {
				resultFileContent += "\n";
			}
			resultFileContent += I18nString.getLocaleString(entry.getKey(), (Locale) null) + ": " + entry.getValue();
		}
		resultFileContent += "\n";

		Map<MediaTypes, Map<Integer, Integer>> mailinglistAssignStatistics = profileImportWorker.getMailinglistAssignStatistics();
		if (mailinglistAssignStatistics != null && mailinglistAssignStatistics.size() > 0) {
			resultFileContent += "Imported data per mailinglist:\n";
			for (MediaTypes mediaType : mailinglistAssignStatistics.keySet()) {
				if (mailinglistAssignStatistics.keySet().size() > 1) {
					resultFileContent += "\t" + mediaType.name() + ":\n";
				}
				for (Entry<Integer, Integer> entry : mailinglistAssignStatistics.get(mediaType).entrySet()) {
					resultFileContent += "\t\"Mailinglist " + mailinglistDao.getMailinglistName(entry.getKey(), profileImportWorker.getImportProfile().getCompanyId()) + "\" (ID: " + entry.getKey() + "):\n";
					
					Map<Integer, Map<MediaTypes, Map<UserStatus, Integer>>> importedRecipientStatuses = profileImportWorker.getMailinglistStatusesForImportedRecipients();
					for (UserStatus userStatus : UserStatus.values()) {
						Integer amount = importedRecipientStatuses.get(entry.getKey()).get(mediaType).get(userStatus);
						resultFileContent += "\t\t" + userStatus.name() + ": " + Integer.toString(amount != null ? amount : 0) + "\n";
							
					}
				}
			}
		}
		return resultFileContent;
	}

	public void sendProfileImportErrorMail(ProfileImportWorker profileImportWorker, int companyID, Locale reportLocale, String reportTimezone, DateTimeFormatter reportDateTimeFormatter) throws Exception {
		Set<String> emailRecipients = new HashSet<>();
		
		if (StringUtils.isNotBlank(profileImportWorker.getImportProfile().getMailForReport())) {
			for (String email : AgnUtils.splitAndTrimList(profileImportWorker.getImportProfile().getMailForReport())) {
				emailRecipients.add(AgnUtils.normalizeEmail(email));
			}
		}
		if (StringUtils.isNotBlank(profileImportWorker.getImportProfile().getMailForError())) {
			for (String email : AgnUtils.splitAndTrimList(profileImportWorker.getImportProfile().getMailForError())) {
				emailRecipients.add(AgnUtils.normalizeEmail(email));
			}
		}
		
		if (StringUtils.isNotBlank(configService.getValue(ConfigValue.ImportAlwaysInformEmail, companyID))) {
			for (String email : AgnUtils.splitAndTrimList(configService.getValue(ConfigValue.ImportAlwaysInformEmail, companyID))) {
				emailRecipients.add(AgnUtils.normalizeEmail(email));
			}
		}
		
		if (StringUtils.isNotBlank(configService.getValue(ConfigValue.Mailaddress_Error))) {
			emailRecipients.add(configService.getValue(ConfigValue.Mailaddress_Error).toLowerCase());
		}
		
		if (profileImportWorker.getAutoImport() != null && StringUtils.isNotBlank(profileImportWorker.getAutoImport().getEmailOnError())) {
			for (String email : AgnUtils.splitAndTrimList(profileImportWorker.getAutoImport().getEmailOnError())) {
				emailRecipients.add(AgnUtils.normalizeEmail(email));
			}
		}

		if (!emailRecipients.isEmpty()) {
			Company company = companyDao.getCompany(profileImportWorker.getImportProfile().getCompanyId());
			
			String subject = "Import-ERROR: " + I18nString.getLocaleString("import.recipients.report", reportLocale) + ": " + " \"" + profileImportWorker.getImportProfile().getName() + "\" (" + I18nString.getLocaleString("Company", reportLocale) + ": " + company.getShortname() + ")";
			String bodyHtml = generateLocalizedImportHtmlReport(profileImportWorker, reportLocale, reportTimezone, reportDateTimeFormatter, true, profileImportWorker.getStatus());
			String bodyText = "Import-ERROR:\n" + generateLocalizedImportTextReport(profileImportWorker, reportLocale, reportTimezone, reportDateTimeFormatter, true);
			
			javaMailService.sendEmail(profileImportWorker.getImportProfile().getCompanyId(), StringUtils.join(emailRecipients, ", "), subject, bodyText, bodyHtml);
		}
	}

	public List<String> generateImportWarningEntries(ProfileImportWorker worker) {
		final Locale locale = worker.getImportProfile().getReportLocale();
		final List<String> list = new ArrayList<>();
		
		if(this.importQuotaCheckService.checkWarningLimitReached(worker.getImportProfile().getCompanyId(), worker.getStatus().getCsvLines())) {
			list.add(I18nString.getLocaleString(
					"warning.import.lineQuota", 
					locale, 
					this.importQuotaCheckService.getImportLimit(worker.getImportProfile().getCompanyId()),
					this.importQuotaCheckService.totalImportLinesCount(worker.getImportProfile().getCompanyId(), worker.getStatus().getCsvLines())));			
		}

		return list;
	}
}
