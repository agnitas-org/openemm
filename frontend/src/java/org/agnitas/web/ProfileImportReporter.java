/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import static com.agnitas.emm.core.recipientsreport.service.impl.RecipientReportUtils.TXT_EXTENSION;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import com.agnitas.service.DataSourceService;
import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.ImportStatus;
import org.agnitas.beans.Mailinglist;
import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.emm.core.autoimport.bean.AutoImport;
import org.agnitas.emm.core.autoimport.bean.AutoImport.UsedFile;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.ImportException;
import org.agnitas.service.ProfileImportWorker;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.ImportReportEntry;
import org.agnitas.util.ImportUtils.ImportErrorType;
import org.agnitas.util.importvalues.Charset;
import org.agnitas.util.importvalues.CheckForDuplicates;
import org.agnitas.util.importvalues.DateFormat;
import org.agnitas.util.importvalues.ImportMode;
import org.agnitas.util.importvalues.MailType;
import org.agnitas.util.importvalues.NullValuesAction;
import org.agnitas.util.importvalues.Separator;
import org.agnitas.util.importvalues.TextRecognitionChar;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Company;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.recipientsreport.service.RecipientsReportService;
import com.agnitas.emm.core.recipientsreport.service.impl.RecipientReportUtils;
import com.agnitas.messages.I18nString;
import com.agnitas.web.forms.ComNewImportWizardForm;

public class ProfileImportReporter {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ProfileImportReporter.class);
	
	private RecipientsReportService recipientsReportService;
	
	private JavaMailService javaMailService;

	private MailinglistDao mailinglistDao;
	
	private ComCompanyDao companyDao;
	
	private ConfigService configService;
	
	private ImportRecipientsDao importRecipientsDao;
	
	private DataSourceService dataSourceService;

	@Required
	public void setRecipientsReportService(RecipientsReportService recipientsReportService) {
		this.recipientsReportService = recipientsReportService;
	}

	@Required
	public void setJavaMailService(JavaMailService javaMailService) {
		this.javaMailService = javaMailService;
	}

	@Required
	public void setMailinglistDao(MailinglistDao mailinglistDao) {
		this.mailinglistDao = mailinglistDao;
	}

	@Required
	public void setCompanyDao(ComCompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setDataSourceService(DataSourceService dataSourceService) {
		this.dataSourceService= dataSourceService;
	}
	
	@Required
	public void setImportRecipientsDao(ImportRecipientsDao importRecipientsDao) {
		this.importRecipientsDao = importRecipientsDao;
	}
	
	public void fillProfileImportForm(ProfileImportWorker profileImportWorker, ComNewImportWizardForm aForm) throws Exception {
		List<Mailinglist> assignedMailingLists = new ArrayList<>();
		if (profileImportWorker.getMailinglistAssignStatistics() != null) {
			for (MediaTypes mediaType : profileImportWorker.getMailinglistAssignStatistics().keySet()) {
				for (Mailinglist mailinglist : aForm.getAllMailingLists()) {
					if (profileImportWorker.getMailinglistAssignStatistics().get(mediaType).containsKey(mailinglist.getId())) {
						if (!assignedMailingLists.contains(mailinglist)) {
							assignedMailingLists.add(mailinglist);
						}
					}
				}
			}
		}
		aForm.setAssignedMailingLists(assignedMailingLists);
		aForm.setMailinglistAssignStats(profileImportWorker.getMailinglistAssignStatistics());

		List<ImportReportEntry> reportStatusEntries = generateImportStatusEntries(profileImportWorker, profileImportWorker.getImportProfile().isNoHeaders());
		aForm.setReportEntries(reportStatusEntries);

		// generate valid recipients file (valid unique recipients and duplicate
		// recipients)
		File validRecipients = profileImportWorker.getStatus().getImportedRecipientsCsv();
		aForm.setValidRecipientsFile(validRecipients);

		// generate invalid recipients file (invalid by wrong field values +
		// other invalid: blacklisted etc.)
		File invalidRecipientsFile = profileImportWorker.getStatus().getInvalidRecipientsCsv();
		aForm.setInvalidRecipientsFile(invalidRecipientsFile);

		// generate fixed recipients file (fixed on error edit page)
		File fixedRecipientsFile = profileImportWorker.getStatus().getFixedByUserRecipientsCsv();
		aForm.setFixedRecipientsFile(fixedRecipientsFile);

		// generate duplicate recipients file
		File duplicateRecipients = profileImportWorker.getStatus().getDuplicateInCsvOrDbRecipientsCsv();
		aForm.setDuplicateRecipientsFile(duplicateRecipients);

		// assign result file
		aForm.setResultFile(profileImportWorker.getResultFile());
	}

	public File writeProfileImportResultFile(ProfileImportWorker profileImportWorker, int companyID, int adminID) throws Exception {
		String resultFileContent = generateResultFileContent(profileImportWorker, adminID);

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
	
	public int writeProfileImportReport(ProfileImportWorker profileImportWorker, int companyID, int adminID, Locale reportLocale, String reportTimezone, DateTimeFormatter reportDateTimeFormatter, boolean isError) throws Exception {
		String resultFileContent = generateLocalizedImportHtmlReport(profileImportWorker, reportLocale, reportTimezone, reportDateTimeFormatter, true);
		int autoImportID = -1;
		if (profileImportWorker.getAutoImport() != null) {
			autoImportID = profileImportWorker.getAutoImport().getAutoImportId();
		}
		if (profileImportWorker.getStatus().getInvalidRecipientsCsv() != null) {
			recipientsReportService.createSupplementalReportData(companyID, adminID, RecipientReportUtils.INVALID_RECIPIENTS_FILE_PREFIX + ".csv.zip", profileImportWorker.getDatasourceId(), profileImportWorker.getEndTime(), profileImportWorker.getStatus().getInvalidRecipientsCsv(), "Downloadable file with invalid recipients data", autoImportID, true);
		}
        String filename = generateProfileImportReportFileName(profileImportWorker.getDatasourceId(), companyID);
        return recipientsReportService.createAndSaveImportReport(companyID, adminID, filename, profileImportWorker.getDatasourceId(), profileImportWorker.getEndTime(), resultFileContent, autoImportID, isError).getId();
	}
	
    private String generateProfileImportReportFileName(int datasourceId, int companyId) {
        return dataSourceService.getDatasourceDescription(datasourceId, companyId).getDescription();
    }

	/**
	 * Send a report email about this import to the executing GUI-admin or the creator of the autoimport
	 * 
	 * @param profileImportWorker
	 * @param admin
	 * @throws Exception
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
			String bodyHtml = generateLocalizedImportHtmlReport(profileImportWorker, reportLocale, reportTimezone, reportDateTimeFormatter, true);
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
			try {
				profileContent += I18nString.getLocaleString("Charset", reportLocale) + ": " + Charset.getCharsetById(profileImportWorker.getImportProfile().getCharset()).getCharsetName() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("Charset", reportLocale) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			profileContent += I18nString.getLocaleString("csv.ContainsHeaders", reportLocale) + ": " + !profileImportWorker.getImportProfile().isNoHeaders() + "\n";
			profileContent += I18nString.getLocaleString("import.zipped", reportLocale) + ": " + AgnUtils.isZipArchiveFile(profileImportWorker.getImportFile().getLocalFile()) + "\n";
			profileContent += I18nString.getLocaleString("import.zipPassword", reportLocale) + ": " + (profileImportWorker.getImportProfile().getZipPassword() != null) + "\n";
			
			profileContent += I18nString.getLocaleString("import.autoMapping", reportLocale) + ": " + profileImportWorker.getImportProfile().isAutoMapping() + "\n";
			
			try {
				profileContent += I18nString.getLocaleString("csv.Delimiter", reportLocale) + ": " + Separator.getSeparatorById(profileImportWorker.getImportProfile().getSeparator()).getValueChar() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("csv.Delimiter", reportLocale) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("csv.StringQuote", reportLocale) + ": " + TextRecognitionChar.getTextRecognitionCharById(profileImportWorker.getImportProfile().getTextRecognitionChar()).name() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("csv.StringQuote", reportLocale) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			profileContent += I18nString.getLocaleString("csv.DecimalSeparator", reportLocale) + ": " + profileImportWorker.getImportProfile().getDecimalSeparator() + "\n";
			
			try {
				profileContent += I18nString.getLocaleString("import.dateFormat", reportLocale) + ": " + DateFormat.getDateFormatById(profileImportWorker.getImportProfile().getDateFormat()).getValue() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("import.dateFormat", reportLocale) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("Mode", reportLocale) + ": " + I18nString.getLocaleString(ImportMode.getFromInt(profileImportWorker.getImportProfile().getImportMode()).getMessageKey(), reportLocale) + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("Mode", reportLocale) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("import.recipients.duplicate", reportLocale) + ": " + CheckForDuplicates.getFromInt(profileImportWorker.getImportProfile().getCheckForDuplicates()).name() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("import.recipients.duplicate", reportLocale) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("import.null_value_handling", reportLocale) + ": " + NullValuesAction.getFromInt(profileImportWorker.getImportProfile().getNullValuesAction()).name() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("import.null_value_handling", reportLocale) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("recipient.RecipientMailtype", reportLocale) + " (" + I18nString.getLocaleString("import.profile.default", reportLocale) + "): " + MailType.getFromInt(profileImportWorker.getImportProfile().getDefaultMailType()).name() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("recipient.RecipientMailtype", reportLocale) + " (" + I18nString.getLocaleString("import.profile.default", reportLocale) + "): Invalid (\"" + e.getMessage() + "\")\n";
			}
			
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
				if (mapping.isEncrypted()) {
					mappingEntryContent += ", encrypted = " + mapping.isEncrypted();
				}
				
				profileContent += "\t" + mappingEntryContent + "\n";
			}
			
			profileContent += I18nString.getLocaleString("KeyColumn", reportLocale) + ": " + StringUtils.join(profileImportWorker.getImportProfile().getKeyColumns(), ", ");
		} else {
			try {
				profileContent += I18nString.getLocaleString("Mode", reportLocale) + ": " + I18nString.getLocaleString(ImportMode.getFromInt(profileImportWorker.getImportProfile().getImportMode()).getMessageKey(), reportLocale) + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("Mode", reportLocale) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
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
			reportContent += I18nString.getLocaleString("Mailinglists", reportLocale) +":\n";
			for (MediaTypes mediaType : mailinglistAssignStatistics.keySet()) {
				if (mailinglistAssignStatistics.keySet().size() > 1) {
					reportContent += "\t" + mediaType.name() + ":";
				}
				for (Entry<Integer, Integer> entry : mailinglistAssignStatistics.get(mediaType).entrySet()) {
					reportContent += "\t\"" + mailinglistDao.getMailinglistName(entry.getKey(), profileImportWorker.getImportProfile().getCompanyId()) + "\" (ID: " + entry.getKey() + "): " + entry.getValue() + "\n";
				}
			}
		}
		return reportContent;
	}
	
	private String generateLocalizedImportTextReportForAlreadyImportedFile(UsedFile alreadyImportedFile, AutoImport autoImport, ImportProfile importProfile, Locale reportLocale, String reportTimezone, DateTimeFormatter reportDateTimeFormatter, boolean showVerboseProfileData) {
		String reportContent = I18nString.getLocaleString("import.recipients.report", reportLocale) + " \"" + importProfile.getName() + "\":\n\n";
		
		reportContent += I18nString.getLocaleString("autoimport.error.fileWasAlreadyImported", reportLocale, alreadyImportedFile.getRemoteFileName(), AgnUtils.getHumanReadableNumber(alreadyImportedFile.getFileSize(), "Byte", false, reportLocale), DateUtilities.getDateTimeString(alreadyImportedFile.getFileDate(), TimeZone.getTimeZone(reportTimezone).toZoneId(), reportDateTimeFormatter));

		reportContent += I18nString.getLocaleString("decode.licenseID", reportLocale) + ": " + configService.getValue(ConfigValue.System_Licence) + "\n";
		
		Company company = companyDao.getCompany(importProfile.getCompanyId());
		reportContent += I18nString.getLocaleString("Company", reportLocale) + ": " + (company == null ? "Unknown" : company.toString()) + "\n";
		
		reportContent += "AutoImport: " + autoImport.toString() + "\n";
		
		reportContent += I18nString.getLocaleString("import.type", reportLocale) + ": " + I18nString.getLocaleString("Recipients", reportLocale) + "\n";
		
		// Show ImportProfile data
		reportContent += I18nString.getLocaleString("import.ImportProfile", reportLocale) + ": \"" + importProfile.getName() + "\" (ID: " + importProfile.getId() + ")\n";
		String profileContent = "";
		if (showVerboseProfileData) {
			try {
				profileContent += I18nString.getLocaleString("Charset", reportLocale) + ": " + Charset.getCharsetById(importProfile.getCharset()).getCharsetName() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("Charset", reportLocale) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			profileContent += I18nString.getLocaleString("csv.ContainsHeaders", reportLocale) + ": " + !importProfile.isNoHeaders() + "\n";
			profileContent += I18nString.getLocaleString("import.zipPassword", reportLocale) + ": " + (importProfile.getZipPassword() != null) + "\n";
			
			profileContent += I18nString.getLocaleString("import.autoMapping", reportLocale) + ": " + importProfile.isAutoMapping() + "\n";
			
			try {
				profileContent += I18nString.getLocaleString("csv.Delimiter", reportLocale) + ": " + Separator.getSeparatorById(importProfile.getSeparator()).getValueChar() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("csv.Delimiter", reportLocale) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("csv.StringQuote", reportLocale) + ": " + TextRecognitionChar.getTextRecognitionCharById(importProfile.getTextRecognitionChar()).name() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("csv.StringQuote", reportLocale) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			profileContent += I18nString.getLocaleString("csv.DecimalSeparator", reportLocale) + ": " + importProfile.getDecimalSeparator() + "\n";
			
			try {
				profileContent += I18nString.getLocaleString("import.dateFormat", reportLocale) + ": " + DateFormat.getDateFormatById(importProfile.getDateFormat()).getValue() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("import.dateFormat", reportLocale) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("Mode", reportLocale) + ": " + I18nString.getLocaleString(ImportMode.getFromInt(importProfile.getImportMode()).getMessageKey(), reportLocale) + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("Mode", reportLocale) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("import.recipients.duplicate", reportLocale) + ": " + CheckForDuplicates.getFromInt(importProfile.getCheckForDuplicates()).name() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("import.recipients.duplicate", reportLocale) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("import.null_value_handling", reportLocale) + ": " + NullValuesAction.getFromInt(importProfile.getNullValuesAction()).name() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("import.null_value_handling", reportLocale) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("recipient.RecipientMailtype", reportLocale) + " (" + I18nString.getLocaleString("import.profile.default", reportLocale) + "): " + MailType.getFromInt(importProfile.getDefaultMailType()).name() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("recipient.RecipientMailtype", reportLocale) + " (" + I18nString.getLocaleString("import.profile.default", reportLocale) + "): Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("mediatype", reportLocale) + ": " + getMediaTypesText(importProfile.getMediatypes(), reportLocale) + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("mediatype", reportLocale) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			profileContent += I18nString.getLocaleString("import.datatype", reportLocale) + ": " + importProfile.getDatatype() + "\n";
			
			profileContent += I18nString.getLocaleString("import.profile.updateAllDuplicates", reportLocale) + ": " + importProfile.getUpdateAllDuplicates() + "\n";
			
			if (importProfile.getImportProcessActionID() > 0) {
				profileContent += "ImportProcessActionID" + ": " + importProfile.getImportProcessActionID() + "\n";
			}
			
			if (importProfile.getActionForNewRecipients() > 0) {
				profileContent += I18nString.getLocaleString("import.actionForNewRecipients", reportLocale) + ": " + importProfile.getActionForNewRecipients() + "\n";
			}
			
			profileContent += I18nString.getLocaleString("import.profile.report.email", reportLocale) + ": " + (StringUtils.isBlank(importProfile.getMailForReport()) ? I18nString.getLocaleString("default.none", reportLocale) : importProfile.getMailForReport()) + "\n";
			profileContent += I18nString.getLocaleString("error.import.profile.email", reportLocale) + ": " + (StringUtils.isBlank(importProfile.getMailForError()) ? I18nString.getLocaleString("default.none", reportLocale) : importProfile.getMailForError()) + "\n";
			
			profileContent += I18nString.getLocaleString("import.profile.gender.settings", reportLocale) + ": ";
			
			if (importProfile.getGenderMapping() != null && importProfile.getGenderMapping().size() > 0) {
				profileContent += "\n\t" + AgnUtils.mapToString(importProfile.getGenderMapping()).replace("\n", "\n\t") + "\n";
			} else {
				profileContent += "NONE\n";
			}
			
			profileContent += I18nString.getLocaleString("Mapping", reportLocale) + ": \n";
			for (ColumnMapping mapping : importProfile.getColumnMapping()) {
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
				if (mapping.isEncrypted()) {
					mappingEntryContent += ", encrypted = " + mapping.isEncrypted();
				}
				
				profileContent += "\t" + mappingEntryContent + "\n";
			}
			
			profileContent += I18nString.getLocaleString("KeyColumn", reportLocale) + ": " + StringUtils.join(importProfile.getKeyColumns(), ", ");
		} else {
			try {
				profileContent += I18nString.getLocaleString("Mode", reportLocale) + ": " + I18nString.getLocaleString(ImportMode.getFromInt(importProfile.getImportMode()).getMessageKey(), reportLocale) + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("Mode", reportLocale) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			profileContent += I18nString.getLocaleString("KeyColumn", reportLocale) + ": " + StringUtils.join(importProfile.getKeyColumns(), ", ");
		}
		reportContent += "\t" + profileContent.replace("\n", "\n\t") + "\n";
		
		if (CollectionUtils.isNotEmpty(importProfile.getKeyColumns())) {
			if (!importRecipientsDao.isKeyColumnIndexed(importProfile.getCompanyId(), importProfile.getKeyColumns())) {
				reportContent += "\n*** " + I18nString.getLocaleString("warning.import.keyColumn.index", reportLocale) + " ***\n\n";
			}
		}
		
		reportContent += I18nString.getLocaleString("StartTime", reportLocale) + ": " + DateUtilities.getDateTimeString(new Date(), TimeZone.getTimeZone(reportTimezone).toZoneId(), reportDateTimeFormatter) + "\n";
		
		if (autoImport != null) {
			reportContent += I18nString.getLocaleString("autoImport.fileServer", reportLocale) + ": " + autoImport.getFileServerWithoutCredentials();
			reportContent += I18nString.getLocaleString("autoImport.filePath", reportLocale) + ": " + autoImport.getFilePath();
			reportContent += I18nString.getLocaleString("autoImport.importMultipleFiles", reportLocale) + ": " + I18nString.getLocaleString(autoImport.isImportMultipleFiles() ? "default.Yes" : "No", reportLocale);
			reportContent += I18nString.getLocaleString("autoImport.removeImportedFiles", reportLocale) + ": " + I18nString.getLocaleString(autoImport.isRemoveImportedFiles() ? "default.Yes" : "No", reportLocale);
		}
		reportContent += I18nString.getLocaleString("settings.FileName", reportLocale) + ": " + (StringUtils.isBlank(alreadyImportedFile.getRemoteFileName()) ? I18nString.getLocaleString("Unknown", reportLocale) : alreadyImportedFile.getRemoteFileName()) + "\n";
		
		reportContent += "\n";
		
		return reportContent;
	}

	public String generateLocalizedImportHtmlReport(ProfileImportWorker importWorker, Locale reportLocale, String reportTimezone, DateTimeFormatter reportDateTimeFormatter, boolean showVerboseProfileData) throws Exception {
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
			if (importWorker.getError() != null && importWorker.getError() instanceof ImportException) {
				errorMessage = I18nString.getLocaleString(((ImportException) importWorker.getError()).getErrorMessageKey(), locale, ((ImportException) importWorker.getError()).getAdditionalErrorData());
			} else {
				errorMessage = importWorker.getStatus().getFatalError();
			}
			htmlContent.append(HtmlReporterHelper.getOutputTableErrorContentLine(errorMessage));

			htmlContent.append(HtmlReporterHelper.getOutputTableContentEnd());
			htmlContent.append(HtmlReporterHelper.getOutputTableEnd());
		}
		
		// Index Warning
		if (CollectionUtils.isNotEmpty(importWorker.getImportProfile().getKeyColumns())) {
			if (!importRecipientsDao.isKeyColumnIndexed(importWorker.getImportProfile().getCompanyId(), importWorker.getImportProfile().getKeyColumns())) {
				htmlContent.append(HtmlReporterHelper.getOutputTableStart());
				htmlContent.append(HtmlReporterHelper.getOutputTableContentStart(false));
				
				htmlContent.append(HtmlReporterHelper.getOutputTableWarningContentLine(I18nString.getLocaleString("warning.import.keyColumn.index", locale)));
				
				htmlContent.append(HtmlReporterHelper.getOutputTableContentEnd());
				htmlContent.append(HtmlReporterHelper.getOutputTableEnd());
			}
		}

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
		htmlContent.append(HtmlReporterHelper.getOutputTableSummaryContentLine(I18nString.getLocaleString("import.encryption_errors", locale), String.valueOf(importWorker.getStatus().getError(ImportErrorType.ENCRYPTION_ERROR)), ((Integer) importWorker.getStatus().getError(ImportErrorType.ENCRYPTION_ERROR)) > 0));
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
				htmlContent.append(HtmlReporterHelper.getOutputTableResultContentLine(I18nString.getLocaleString("import.result.csvlines", locale), AgnUtils.getHumanReadableNumber(importWorker.getStatus().getCsvLines() - 1, locale) + " (+1 " + I18nString.getLocaleString("csv.ContainsHeaders", locale) + ")"));
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

		// Mailinglists
		Map<MediaTypes, Map<Integer, Integer>> mailinglistAssignStatistics = importWorker.getMailinglistAssignStatistics();
		if (mailinglistAssignStatistics != null && mailinglistAssignStatistics.size() > 0) {
			htmlContent.append(HtmlReporterHelper.getOutputTableSubHeader(I18nString.getLocaleString("Mailinglists", locale), true));
			for (MediaTypes mediaType : mailinglistAssignStatistics.keySet()) {
				if (mailinglistAssignStatistics.keySet().size() > 1) {
					htmlContent.append(mediaType.name() + ":\n");
				}
				for (Entry<Integer, Integer> entry : mailinglistAssignStatistics.get(mediaType).entrySet()) {
					htmlContent.append(HtmlReporterHelper.getOutputTableResultContentLine("\"" + mailinglistDao.getMailinglistName(entry.getKey(), importWorker.getImportProfile().getCompanyId()) + "\" (ID: " + entry.getKey() + ")", Integer.toString(entry.getValue())));
				}
			}
		}
		
		htmlContent.append(HtmlReporterHelper.getOutputTableContentEnd());
		htmlContent.append(HtmlReporterHelper.getOutputTableEnd());

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
			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Charset", locale), Charset.getCharsetById(importWorker.getImportProfile().getCharset()).getCharsetName()));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Charset", locale), "Invalid (\"" + e.getMessage() + "\")"));
			}
			
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.ContainsHeaders", locale), I18nString.getLocaleString(!importWorker.getImportProfile().isNoHeaders() ? "default.Yes" : "No", locale)));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.zipped", locale), I18nString.getLocaleString(AgnUtils.isZipArchiveFile(importWorker.getImportFile().getLocalFile()) ? "default.Yes" : "No", locale)));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.zipPassword", locale), I18nString.getLocaleString(importWorker.getImportProfile().getZipPassword() != null ? "default.Yes" : "No", locale)));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.autoMapping", locale), I18nString.getLocaleString(importWorker.getImportProfile().isAutoMapping() ? "default.Yes" : "No", locale)));

			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.Delimiter", locale), Character.toString(Separator.getSeparatorById(importWorker.getImportProfile().getSeparator()).getValueChar())));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.Delimiter", locale), "Invalid (\"" + e.getMessage() + "\")"));
			}
			
			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.StringQuote", locale), I18nString.getLocaleString(TextRecognitionChar.getTextRecognitionCharById(importWorker.getImportProfile().getTextRecognitionChar()).getPublicValue(), locale)));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.StringQuote", locale), "Invalid (\"" + e.getMessage() + "\")"));
			}
			
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.DecimalSeparator", locale), Character.toString(importWorker.getImportProfile().getDecimalSeparator())));
			
			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.dateFormat", locale), DateFormat.getDateFormatById(importWorker.getImportProfile().getDateFormat()).getValue()));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.dateFormat", locale), "Invalid (\"" + e.getMessage() + "\")"));
			}
			
			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Mode", locale), I18nString.getLocaleString(ImportMode.getFromInt(importWorker.getImportProfile().getImportMode()).getMessageKey(), locale)));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Mode", locale), "Invalid (\"" + e.getMessage() + "\")"));
			}
			
			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.recipients.duplicate", locale), I18nString.getLocaleString(CheckForDuplicates.getFromInt(importWorker.getImportProfile().getCheckForDuplicates()).getMessageKey(), locale)));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.recipients.duplicate", locale), "Invalid (\"" + e.getMessage() + "\")"));
			}
			
			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.null_value_handling", locale), I18nString.getLocaleString(NullValuesAction.getFromInt(importWorker.getImportProfile().getNullValuesAction()).getMessageKey(), locale)));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.null_value_handling", locale), "Invalid (\"" + e.getMessage() + "\")"));
			}
			
			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("recipient.RecipientMailtype", locale) + " (" + I18nString.getLocaleString("import.profile.default", locale) + ")", MailType.getFromInt(importWorker.getImportProfile().getDefaultMailType()).name()));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("recipient.RecipientMailtype", locale) + " (" + I18nString.getLocaleString("import.profile.default", locale) + ")", "Invalid (\"" + e.getMessage() + "\")"));
			}
			
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
				if (mapping.isEncrypted()) {
					mappingEntryContent += ", encrypted = " + mapping.isEncrypted();
				}
				
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("", mappingEntryContent));
			}

			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("KeyColumn", locale), StringUtils.join(importWorker.getImportProfile().getKeyColumns(), ", ")));
		} else {
			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Mode", locale), I18nString.getLocaleString(ImportMode.getFromInt(importWorker.getImportProfile().getImportMode()).getMessageKey(), locale)));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Mode", locale), "Invalid (\"" + e.getMessage() + "\")"));
			}
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
	
	private String generateLocalizedImportHtmlReportForAlreadyImportedFile(UsedFile alreadyImportedFile, AutoImport autoImport, ImportProfile importProfile, Locale reportLocale, String reportTimezone, DateTimeFormatter reportDateTimeFormatter, boolean showVerboseProfileData) {
		Locale locale = reportLocale;
		String title = "AutoImport: " + autoImport.toString();
		
		StringBuilder htmlContent = new StringBuilder(HtmlReporterHelper.getHtmlPrefixWithCssStyles(title));
		
		htmlContent.append(HtmlReporterHelper.getHeader(title, I18nString.getLocaleString("default.version", locale)));
		
		// Already Imported File Warning
		htmlContent.append(HtmlReporterHelper.getOutputTableStart());
		htmlContent.append(HtmlReporterHelper.getOutputTableContentStart(false));
		
		htmlContent.append(HtmlReporterHelper.getOutputTableWarningContentLine(I18nString.getLocaleString("autoimport.error.fileWasAlreadyImported", locale, alreadyImportedFile.getRemoteFileName(), AgnUtils.getHumanReadableNumber(alreadyImportedFile.getFileSize(), "Byte", false, locale), DateUtilities.getDateTimeString(alreadyImportedFile.getFileDate(), TimeZone.getTimeZone(reportTimezone).toZoneId(), reportDateTimeFormatter))));
		
		htmlContent.append(HtmlReporterHelper.getOutputTableContentEnd());
		htmlContent.append(HtmlReporterHelper.getOutputTableEnd());

		// Informations
		htmlContent.append(HtmlReporterHelper.getOutputTableStart());
		htmlContent.append(HtmlReporterHelper.getOutputTableHeader(I18nString.getLocaleString("Info", locale)));
		htmlContent.append(HtmlReporterHelper.getOutputTableContentStart(false));
		
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("decode.licenseID", locale), configService.getValue(ConfigValue.System_Licence)));

		Company company = companyDao.getCompany(importProfile.getCompanyId());
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Company", locale), (company == null ? "Unknown" : company.toString())));
		
		if (autoImport != null) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("AutoImport", autoImport.toString()));
		}
		
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.type", locale), I18nString.getLocaleString("Recipients", locale)));
		
		// Importprofile
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.ImportProfile", locale), "\"" + importProfile.getName() + "\" (ID: " + importProfile.getId() + ")"));
		
		if (showVerboseProfileData) {
			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Charset", locale), Charset.getCharsetById(importProfile.getCharset()).getCharsetName()));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Charset", locale), "Invalid (\"" + e.getMessage() + "\")"));
			}
			
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.ContainsHeaders", locale), I18nString.getLocaleString(!importProfile.isNoHeaders() ? "default.Yes" : "No", locale)));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.zipPassword", locale), I18nString.getLocaleString(importProfile.getZipPassword() != null ? "default.Yes" : "No", locale)));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.autoMapping", locale), I18nString.getLocaleString(importProfile.isAutoMapping() ? "default.Yes" : "No", locale)));

			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.Delimiter", locale), Character.toString(Separator.getSeparatorById(importProfile.getSeparator()).getValueChar())));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.Delimiter", locale), "Invalid (\"" + e.getMessage() + "\")"));
			}
			
			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.StringQuote", locale), I18nString.getLocaleString(TextRecognitionChar.getTextRecognitionCharById(importProfile.getTextRecognitionChar()).getPublicValue(), locale)));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.StringQuote", locale), "Invalid (\"" + e.getMessage() + "\")"));
			}
			
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.DecimalSeparator", locale), Character.toString(importProfile.getDecimalSeparator())));
			
			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.dateFormat", locale), DateFormat.getDateFormatById(importProfile.getDateFormat()).getValue()));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.dateFormat", locale), "Invalid (\"" + e.getMessage() + "\")"));
			}
			
			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Mode", locale), I18nString.getLocaleString(ImportMode.getFromInt(importProfile.getImportMode()).getMessageKey(), locale)));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Mode", locale), "Invalid (\"" + e.getMessage() + "\")"));
			}
			
			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.recipients.duplicate", locale), I18nString.getLocaleString(CheckForDuplicates.getFromInt(importProfile.getCheckForDuplicates()).getMessageKey(), locale)));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.recipients.duplicate", locale), "Invalid (\"" + e.getMessage() + "\")"));
			}
			
			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.null_value_handling", locale), I18nString.getLocaleString(NullValuesAction.getFromInt(importProfile.getNullValuesAction()).getMessageKey(), locale)));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.null_value_handling", locale), "Invalid (\"" + e.getMessage() + "\")"));
			}
			
			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("recipient.RecipientMailtype", locale) + " (" + I18nString.getLocaleString("import.profile.default", locale) + ")", MailType.getFromInt(importProfile.getDefaultMailType()).name()));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("recipient.RecipientMailtype", locale) + " (" + I18nString.getLocaleString("import.profile.default", locale) + ")", "Invalid (\"" + e.getMessage() + "\")"));
			}
			
			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("mediatype", locale), getMediaTypesText(importProfile.getMediatypes(), reportLocale)));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("mediatype", locale), "Invalid (\"" + e.getMessage() + "\")"));
			}
			
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.datatype", locale), importProfile.getDatatype()));
			
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.profile.updateAllDuplicates", locale), I18nString.getLocaleString(importProfile.getUpdateAllDuplicates() ? "default.Yes" : "No", locale)));
			
			if (importProfile.getImportProcessActionID() > 0) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("ImportProcessActionID", Integer.toString(importProfile.getImportProcessActionID())));
			}
			
			if (importProfile.getActionForNewRecipients() > 0) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.actionForNewRecipients", locale), Integer.toString(importProfile.getActionForNewRecipients())));
			}
			
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.profile.report.email", locale), StringUtils.isBlank(importProfile.getMailForReport()) ? I18nString.getLocaleString("default.none", locale) : importProfile.getMailForReport()));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("error.import.profile.email", locale), StringUtils.isBlank(importProfile.getMailForError()) ? I18nString.getLocaleString("default.none", locale) : importProfile.getMailForError()));

			htmlContent.append(HtmlReporterHelper.getOutputTableSubHeader(I18nString.getLocaleString("import.profile.gender.settings", locale), false));
			if (importProfile.getGenderMapping() != null && importProfile.getGenderMapping().size() > 0) {
				for (Entry<String, Integer> genderEntry : importProfile.getGenderMapping().entrySet()) {
					htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("", genderEntry.getKey() + " = " + genderEntry.getValue()));
				}
			} else {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("", I18nString.getLocaleString("default.none", locale)));
			}

			htmlContent.append(HtmlReporterHelper.getOutputTableSubHeader(I18nString.getLocaleString("Mapping", locale), false));
			for (ColumnMapping mapping : importProfile.getColumnMapping()) {
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
				if (mapping.isEncrypted()) {
					mappingEntryContent += ", encrypted = " + mapping.isEncrypted();
				}
				
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("", mappingEntryContent));
			}

			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("KeyColumn", locale), StringUtils.join(importProfile.getKeyColumns(), ", ")));
		} else {
			try {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Mode", locale), I18nString.getLocaleString(ImportMode.getFromInt(importProfile.getImportMode()).getMessageKey(), locale)));
			} catch (Exception e) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Mode", locale), "Invalid (\"" + e.getMessage() + "\")"));
			}
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("KeyColumn", locale), StringUtils.join(importProfile.getKeyColumns(), ", ")));
		}
		
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("StartTime", locale), DateUtilities.getDateTimeString(new Date(), TimeZone.getTimeZone(reportTimezone).toZoneId(), reportDateTimeFormatter)));

		if (autoImport != null) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("autoImport.fileServer", locale), autoImport.getFileServerWithoutCredentials()));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("autoImport.filePath", locale), autoImport.getFilePath()));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("autoImport.importMultipleFiles", locale), I18nString.getLocaleString(autoImport.isImportMultipleFiles() ? "default.Yes" : "No", locale)));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("autoImport.removeImportedFiles", locale), I18nString.getLocaleString(autoImport.isRemoveImportedFiles() ? "default.Yes" : "No", locale)));
		}
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("settings.FileName", locale), StringUtils.isBlank(alreadyImportedFile.getRemoteFileName()) ? I18nString.getLocaleString("Unknown", locale) : alreadyImportedFile.getRemoteFileName()));

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
		reportStatusEntries.add(new ImportReportEntry("import.encryption_errors", String.valueOf(customerImportStatus.getError(ImportErrorType.ENCRYPTION_ERROR))));
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
				reportStatusEntries.add(new ImportReportEntry("import.result.csvlines",  AgnUtils.getHumanReadableNumber(customerImportStatus.getCsvLines() - 1, locale) + " (+1 " + I18nString.getLocaleString("csv.ContainsHeaders", locale) + ")"));
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

	private String generateResultFileContent(ProfileImportWorker profileImportWorker, int adminID) throws Exception {
		String resultFileContent = "";

		resultFileContent += "Importname: " + profileImportWorker.getImportProfile().getName() + "\n";
		resultFileContent += "Start time: " + DateUtilities.getDateTimeString(profileImportWorker.getStartTime(), TimeZone.getTimeZone("Europe/Berlin").toZoneId(), AgnUtils.getDateTimeFormatterByPattern(DateUtilities.DD_MM_YYYY_HH_MM_SS, Locale.US, true)) + "\n";
		resultFileContent += "End time: " + DateUtilities.getDateTimeString(profileImportWorker.getEndTime(), TimeZone.getTimeZone("Europe/Berlin").toZoneId(), AgnUtils.getDateTimeFormatterByPattern(DateUtilities.DD_MM_YYYY_HH_MM_SS, Locale.US, true)) + "\n";
		resultFileContent += "License id: " + configService.getValue(ConfigValue.System_Licence) + "\n";
		resultFileContent += "Company id: " + profileImportWorker.getImportProfile().getCompanyId() + "\n";
		resultFileContent += "Admin id: " + adminID + "\n";

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
			resultFileContent += "Mailinglist assignments:\n";
			for (MediaTypes mediaType : mailinglistAssignStatistics.keySet()) {
				if (mailinglistAssignStatistics.keySet().size() > 1) {
					resultFileContent += "\t" + mediaType.name() + ":";
				}
				for (Entry<Integer, Integer> entry : mailinglistAssignStatistics.get(mediaType).entrySet()) {
					resultFileContent += "\t\"" + mailinglistDao.getMailinglistName(entry.getKey(), profileImportWorker.getImportProfile().getCompanyId()) + "\" (ID: " + entry.getKey() + "): " + entry.getValue() + "\n";
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
			String bodyHtml = generateLocalizedImportHtmlReport(profileImportWorker, reportLocale, reportTimezone, reportDateTimeFormatter, true);
			String bodyText = "Import-ERROR:\n" + generateLocalizedImportTextReport(profileImportWorker, reportLocale, reportTimezone, reportDateTimeFormatter, true);
			
			javaMailService.sendEmail(profileImportWorker.getImportProfile().getCompanyId(), StringUtils.join(emailRecipients, ", "), subject, bodyText, bodyHtml);
		}
	}

	public void sendProfileImportReportAlreadyImportedFileMail(UsedFile alreadyImportedFile, AutoImport autoImport, ImportProfile importProfile, int companyID, Locale reportLocale, String reportTimezone, DateTimeFormatter reportDateTimeFormatter) {
		Set<String> emailRecipients = new HashSet<>();
		
		if (StringUtils.isNotBlank(importProfile.getMailForReport())) {
			for (String email : AgnUtils.splitAndTrimList(importProfile.getMailForReport())) {
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
			
			Company company = companyDao.getCompany(importProfile.getCompanyId());
			
			String subject = I18nString.getLocaleString("import.recipients.report", emailLocale) + " \"" + importProfile.getName() + "\" (" + I18nString.getLocaleString("Company", emailLocale) + ": " + company.getShortname() + ")";
			String bodyHtml = generateLocalizedImportHtmlReportForAlreadyImportedFile(alreadyImportedFile, autoImport, importProfile, reportLocale, reportTimezone, reportDateTimeFormatter, true);
			String bodyText = generateLocalizedImportTextReportForAlreadyImportedFile(alreadyImportedFile, autoImport, importProfile, reportLocale, reportTimezone, reportDateTimeFormatter, true);
			
			javaMailService.sendEmail(autoImport.getCompanyId(), StringUtils.join(emailRecipients, ", "), subject, bodyText, bodyHtml);
		}
	}

	public int writeProfileImportReportAlreadyImportedFile(UsedFile alreadyImportedFile, AutoImport autoImport, ImportProfile importProfile, int adminID, Locale reportLocale, String reportTimezone, DateTimeFormatter reportDateTimeFormatter) throws Exception {
		String resultFileContent = generateLocalizedImportHtmlReportForAlreadyImportedFile(alreadyImportedFile, autoImport, importProfile, reportLocale, reportTimezone, reportDateTimeFormatter, true);
		int autoImportID = -1;
		if (autoImport != null) {
			autoImportID = autoImport.getAutoImportId();
		}
		return recipientsReportService.createAndSaveImportReport(importProfile.getCompanyId(), adminID, RecipientReportUtils.IMPORT_RESULT_FILE_PREFIX, 0, new Date(), resultFileContent, autoImportID, false).getId();
	}
}
