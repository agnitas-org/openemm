/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import static com.agnitas.emm.core.recipientsreport.service.impl.RecipientReportUtils.TXT_EXTENSION;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.CustomerImportStatus;
import org.agnitas.beans.ImportProfile;
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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComCompany;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.recipientsreport.service.RecipientsReportService;
import com.agnitas.emm.core.recipientsreport.service.impl.RecipientReportUtils;
import com.agnitas.messages.I18nString;
import com.agnitas.web.forms.ComNewImportWizardForm;

public class ProfileImportReporter {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ProfileImportReporter.class);
	
	private RecipientsReportService recipientsReportService;
	
	private JavaMailService javaMailService;

	private MailinglistDao mailinglistDao;
	
	private ComCompanyDao companyDao;
	
	private ConfigService configService;
	
	private ImportRecipientsDao importRecipientsDao;

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
	public void setImportRecipientsDao(ImportRecipientsDao importRecipientsDao) {
		this.importRecipientsDao = importRecipientsDao;
	}
	
	public void fillProfileImportForm(ProfileImportWorker profileImportWorker, ComNewImportWizardForm aForm, Locale locale) throws Exception {
		List<Mailinglist> assignedMailingLists = new ArrayList<>();
		for (Mailinglist mailinglist : aForm.getAllMailingLists()) {
			if (profileImportWorker.getMailinglistAssignStatistics() != null && profileImportWorker.getMailinglistAssignStatistics().containsKey(mailinglist.getId())) {
				assignedMailingLists.add(mailinglist);
			}
		}
		aForm.setAssignedMailingLists(assignedMailingLists);
		aForm.setMailinglistAssignStats(profileImportWorker.getMailinglistAssignStatistics());

		List<ImportReportEntry> reportStatusEntries = generateImportStatusEntries(profileImportWorker, profileImportWorker.getImportProfile().getCompanyId(), locale, profileImportWorker.getImportProfile().isNoHeaders());
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

	public File writeProfileImportResultFile(ProfileImportWorker profileImportWorker, ComAdmin admin) throws Exception {
		String resultFileContent = generateResultFileContent(profileImportWorker, admin.getAdminID());

		try {
			File resultFile = new File(profileImportWorker.getImportFile().getLocalFile().getAbsolutePath()
				+ "_CID" + admin.getCompanyID()
				+ "_" + new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS_FORFILENAMES).format(profileImportWorker.getStartTime())
				+ "_" + RecipientReportUtils.IMPORT_RESULT_FILE_PREFIX + TXT_EXTENSION);
			FileUtils.writeStringToFile(resultFile, resultFileContent, "UTF-8");
			return resultFile;
		} catch (Exception e) {
			logger.error("writeProfileImportResultFile: " + e, e);
			return null;
		}
	}
	
	public int writeProfileImportReport(ProfileImportWorker profileImportWorker, ComAdmin admin, boolean isError) throws Exception {
		String resultFileContent = generateLocalizedImportHtmlReport(profileImportWorker, admin, true);
		int autoImportID = -1;
		if (profileImportWorker.getAutoImport() != null) {
			autoImportID = profileImportWorker.getAutoImport().getAutoImportId();
		}
		if (profileImportWorker.getStatus().getInvalidRecipientsCsv() != null) {
			recipientsReportService.createSupplementalReportData(admin, RecipientReportUtils.INVALID_RECIPIENTS_FILE_PREFIX + ".csv.zip", profileImportWorker.getDatasourceId(), profileImportWorker.getEndTime(), profileImportWorker.getStatus().getInvalidRecipientsCsv(), "Downloadable file with invalid recipients data", autoImportID, true);
		}
		return recipientsReportService.createAndSaveImportReport(admin, RecipientReportUtils.IMPORT_RESULT_FILE_PREFIX, profileImportWorker.getDatasourceId(), profileImportWorker.getEndTime(), resultFileContent, autoImportID, isError).getId();
	}
	
	/**
	 * Send a report email about this import to the executing GUI-admin or the creator of the autoimport
	 * 
	 * @param profileImportWorker
	 * @param admin
	 * @throws Exception
	 */
	public void sendProfileImportReportMail(ProfileImportWorker profileImportWorker, ComAdmin admin) throws Exception {
		Set<String> emailRecipients = new HashSet<>();
		
		if (StringUtils.isNotBlank(profileImportWorker.getImportProfile().getMailForReport())) {
			for (String email : AgnUtils.splitAndTrimList(profileImportWorker.getImportProfile().getMailForReport())) {
				emailRecipients.add(AgnUtils.normalizeEmail(email));
			}
		}
		
		if (StringUtils.isNotBlank(configService.getValue(ConfigValue.ImportAlwaysInformEmail, admin.getCompanyID()))) {
			for (String email : AgnUtils.splitAndTrimList(configService.getValue(ConfigValue.ImportAlwaysInformEmail, admin.getCompanyID()))) {
				emailRecipients.add(AgnUtils.normalizeEmail(email));
			}
		}

		if (!emailRecipients.isEmpty()) {
			Locale emailLocale = admin != null ? admin.getLocale() : null;
			
			ComCompany company = companyDao.getCompany(profileImportWorker.getImportProfile().getCompanyId());
			
			String subject = I18nString.getLocaleString("import.recipients.report", emailLocale) + " \"" + profileImportWorker.getImportProfile().getName() + "\" (" + I18nString.getLocaleString("Company", emailLocale) + ": " + company.getShortname() + ")";
			String bodyHtml = generateLocalizedImportHtmlReport(profileImportWorker, admin, true);
			String bodyText = generateLocalizedImportTextReport(profileImportWorker, admin, true);
			
			// Deactivated attachment of InvalidRecipientsCsvZipFile for DSGVO reasons. Must be password secured if reintegrated!
//			if (profileImportWorker.getStatus().getInvalidRecipientsCsv() != null && profileImportWorker.getStatus().getInvalidRecipientsCsv().length() < 4 * 1024 * 1024) {
//				// Only send invalid recipientsfile in email if its size is below 4 MB
//				javaMailService.sendEmail(StringUtils.join(emailRecipients, ", "), subject, bodyText, bodyHtml, new MailAttachment("invalid_recipients.zip", FileUtils.readFileToByteArray(profileImportWorker.getStatus().getInvalidRecipientsCsv()), "application/zip"));
//			} else {
				javaMailService.sendEmail(StringUtils.join(emailRecipients, ", "), subject, bodyText, bodyHtml);
//			}
		}
	}

	private String generateLocalizedImportTextReport(ProfileImportWorker profileImportWorker, ComAdmin admin, boolean showVerboseProfileData) throws Exception {
		String reportContent = I18nString.getLocaleString("import.recipients.report", admin.getLocale()) + " \"" + profileImportWorker.getImportProfile().getName() + "\":\n\n";

		reportContent += I18nString.getLocaleString("decode.licenseID", admin.getLocale()) + ": " + configService.getValue(ConfigValue.System_Licence) + "\n";
		
		ComCompany company = companyDao.getCompany(profileImportWorker.getImportProfile().getCompanyId());
		reportContent += I18nString.getLocaleString("Company", admin.getLocale()) + ": " + (company == null ? "Unknown" : company.toString()) + "\n";
		
		if (profileImportWorker.getAutoImport() != null) {
			reportContent += "AutoImport: " + profileImportWorker.getAutoImport().toString() + "\n";
		}
		
		reportContent += I18nString.getLocaleString("import.type", admin.getLocale()) + ": " + I18nString.getLocaleString("Recipients", admin.getLocale()) + "\n";
		
		// Show ImportProfile data
		reportContent += I18nString.getLocaleString("import.ImportProfile", admin.getLocale()) + ": \"" + profileImportWorker.getImportProfile().getName() + "\" (ID: " + profileImportWorker.getImportProfile().getId() + ")\n";
		String profileContent = "";
		if (showVerboseProfileData) {
			try {
				profileContent += I18nString.getLocaleString("Charset", admin.getLocale()) + ": " + Charset.getCharsetById(profileImportWorker.getImportProfile().getCharset()).getCharsetName() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("Charset", admin.getLocale()) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			profileContent += I18nString.getLocaleString("csv.ContainsHeaders", admin.getLocale()) + ": " + !profileImportWorker.getImportProfile().isNoHeaders() + "\n";
			profileContent += I18nString.getLocaleString("import.zipped", admin.getLocale()) + ": " + profileImportWorker.getImportProfile().isZipped() + "\n";
			profileContent += I18nString.getLocaleString("import.zipPassword", admin.getLocale()) + ": " + (profileImportWorker.getImportProfile().getZipPassword() != null) + "\n";
			
			profileContent += I18nString.getLocaleString("import.autoMapping", admin.getLocale()) + ": " + profileImportWorker.getImportProfile().isAutoMapping() + "\n";
			
			try {
				profileContent += I18nString.getLocaleString("csv.Delimiter", admin.getLocale()) + ": " + Separator.getSeparatorById(profileImportWorker.getImportProfile().getSeparator()).getValueChar() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("csv.Delimiter", admin.getLocale()) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("csv.StringQuote", admin.getLocale()) + ": " + TextRecognitionChar.getTextRecognitionCharById(profileImportWorker.getImportProfile().getTextRecognitionChar()).name() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("csv.StringQuote", admin.getLocale()) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			profileContent += I18nString.getLocaleString("csv.DecimalSeparator", admin.getLocale()) + ": " + profileImportWorker.getImportProfile().getDecimalSeparator() + "\n";
			
			try {
				profileContent += I18nString.getLocaleString("import.dateFormat", admin.getLocale()) + ": " + DateFormat.getDateFormatById(profileImportWorker.getImportProfile().getDateFormat()).getValue() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("import.dateFormat", admin.getLocale()) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("Mode", admin.getLocale()) + ": " + I18nString.getLocaleString(ImportMode.getFromInt(profileImportWorker.getImportProfile().getImportMode()).getMessageKey(), admin.getLocale()) + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("Mode", admin.getLocale()) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("import.recipients.duplicate", admin.getLocale()) + ": " + CheckForDuplicates.getFromInt(profileImportWorker.getImportProfile().getCheckForDuplicates()).name() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("import.recipients.duplicate", admin.getLocale()) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("import.null_value_handling", admin.getLocale()) + ": " + NullValuesAction.getFromInt(profileImportWorker.getImportProfile().getNullValuesAction()).name() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("import.null_value_handling", admin.getLocale()) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("recipient.RecipientMailtype", admin.getLocale()) + " (" + I18nString.getLocaleString("import.profile.default", admin.getLocale()) + "): " + MailType.getFromInt(profileImportWorker.getImportProfile().getDefaultMailType()).name() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("recipient.RecipientMailtype", admin.getLocale()) + " (" + I18nString.getLocaleString("import.profile.default", admin.getLocale()) + "): Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			if (admin.permissionAllowed(Permission.IMPORT_MEDIATYPE)) {
				try {
					profileContent += I18nString.getLocaleString("mediatype", admin.getLocale()) + ": " + I18nString.getLocaleString("mailing.MediaType." + profileImportWorker.getImportProfile().getMediatype().getMediaCode(), admin.getLocale()) + "\n";
				} catch (Exception e) {
					profileContent += I18nString.getLocaleString("mediatype", admin.getLocale()) + ": Invalid (\"" + e.getMessage() + "\")\n";
				}
			}
			
			profileContent += I18nString.getLocaleString("import.datatype", admin.getLocale()) + ": " + profileImportWorker.getImportProfile().getDatatype() + "\n";
			
			profileContent += I18nString.getLocaleString("import.profile.updateAllDuplicates", admin.getLocale()) + ": " + profileImportWorker.getImportProfile().getUpdateAllDuplicates() + "\n";
			
			if (profileImportWorker.getImportProfile().getImportProcessActionID() > 0) {
				profileContent += "ImportProcessActionID" + ": " + profileImportWorker.getImportProfile().getImportProcessActionID() + "\n";
			}
			
			if (profileImportWorker.getImportProfile().getActionForNewRecipients() > 0) {
				profileContent += I18nString.getLocaleString("import.actionForNewRecipients", admin.getLocale()) + ": " + profileImportWorker.getImportProfile().getActionForNewRecipients() + "\n";
			}
			
			profileContent += I18nString.getLocaleString("import.profile.report.email", admin.getLocale()) + ": " + (StringUtils.isBlank(profileImportWorker.getImportProfile().getMailForReport()) ? I18nString.getLocaleString("default.none", admin.getLocale()) : profileImportWorker.getImportProfile().getMailForReport()) + "\n";
			profileContent += I18nString.getLocaleString("error.import.profile.email", admin.getLocale()) + ": " + (StringUtils.isBlank(profileImportWorker.getImportProfile().getMailForError()) ? I18nString.getLocaleString("default.none", admin.getLocale()) : profileImportWorker.getImportProfile().getMailForError()) + "\n";
			
			profileContent += I18nString.getLocaleString("import.profile.gender.settings", admin.getLocale()) + ": ";
			
			if (profileImportWorker.getImportProfile().getGenderMapping() != null && profileImportWorker.getImportProfile().getGenderMapping().size() > 0) {
				profileContent += "\n\t" + AgnUtils.mapToString(profileImportWorker.getImportProfile().getGenderMapping()).replace("\n", "\n\t") + "\n";
			} else {
				profileContent += "NONE\n";
			}
			
			profileContent += I18nString.getLocaleString("Mapping", admin.getLocale()) + ": \n";
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
			
			profileContent += I18nString.getLocaleString("KeyColumn", admin.getLocale()) + ": " + StringUtils.join(profileImportWorker.getImportProfile().getKeyColumns(), ", ");
		} else {
			try {
				profileContent += I18nString.getLocaleString("Mode", admin.getLocale()) + ": " + I18nString.getLocaleString(ImportMode.getFromInt(profileImportWorker.getImportProfile().getImportMode()).getMessageKey(), admin.getLocale()) + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("Mode", admin.getLocale()) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			profileContent += I18nString.getLocaleString("KeyColumn", admin.getLocale()) + ": " + StringUtils.join(profileImportWorker.getImportProfile().getKeyColumns(), ", ");
		}
		reportContent += "\t" + profileContent.replace("\n", "\n\t") + "\n";
		
		if (CollectionUtils.isNotEmpty(profileImportWorker.getImportProfile().getKeyColumns())) {
			if (!importRecipientsDao.isKeyColumnIndexed(profileImportWorker.getImportProfile().getCompanyId(), profileImportWorker.getImportProfile().getKeyColumns())) {
				reportContent += "\n*** " + I18nString.getLocaleString("warning.import.keyColumn.index", admin.getLocale()) + " ***\n\n";
			}
		}
		
		reportContent += I18nString.getLocaleString("StartTime", admin.getLocale()) + ": " + admin.getDateTimeFormat().format(profileImportWorker.getStartTime()) + "\n";
		reportContent += I18nString.getLocaleString("EndTime", admin.getLocale()) + ": " + admin.getDateTimeFormat().format(profileImportWorker.getEndTime()) + "\n";
		
		if (profileImportWorker.getAutoImport() != null) {
			reportContent += I18nString.getLocaleString("autoImport.fileServer", admin.getLocale()) + ": " + profileImportWorker.getAutoImport().getFileServerWithoutCredentials();
		}
		reportContent += I18nString.getLocaleString("settings.FileName", admin.getLocale()) + ": " + (StringUtils.isBlank(profileImportWorker.getImportFile().getRemoteFilePath()) ? I18nString.getLocaleString("Unknown", admin.getLocale()) : profileImportWorker.getImportFile().getRemoteFilePath()) + "\n";
		if (profileImportWorker.getImportFile().getDownloadDurationMillis() > -1) {
			reportContent += I18nString.getLocaleString("DownloadDuration", admin.getLocale()) + ": " + AgnUtils.getHumanReadableDurationFromMillis(profileImportWorker.getImportFile().getDownloadDurationMillis()) + "\n";
		}
		
		reportContent += "\n";
		
		List<ImportReportEntry> reportStatusEntries = generateImportStatusEntries(profileImportWorker, profileImportWorker.getImportProfile().getCompanyId(), admin.getLocale(), profileImportWorker.getImportProfile().isNoHeaders());
		for (ImportReportEntry entry : reportStatusEntries) {
			reportContent += I18nString.getLocaleString(entry.getKey(), admin.getLocale()) + ": " + entry.getValue() + "\n";
		}
		
		reportContent += "\n";
		
		if (profileImportWorker.getMailinglistAssignStatistics() != null && profileImportWorker.getMailinglistAssignStatistics().size() > 0) {
			reportContent += I18nString.getLocaleString("Mailinglists", admin.getLocale()) +":\n";
			for (Entry<Integer, Integer> entry : profileImportWorker.getMailinglistAssignStatistics().entrySet()) {
				reportContent += "\t\"" + mailinglistDao.getMailinglistName(entry.getKey(), profileImportWorker.getImportProfile().getCompanyId()) + "\" (ID: " + entry.getKey() + "): " + entry.getValue() + "\n";
			}
		}
		return reportContent;
	}
	private String generateLocalizedImportTextReportForAlreadyImportedFile(UsedFile alreadyImportedFile, AutoImport autoImport, ImportProfile importProfile, ComAdmin admin, boolean showVerboseProfileData) {
		String reportContent = I18nString.getLocaleString("import.recipients.report", admin.getLocale()) + " \"" + importProfile.getName() + "\":\n\n";
		
		reportContent += I18nString.getLocaleString("autoimport.error.fileWasAlreadyImported", admin.getLocale(), alreadyImportedFile.getRemoteFileName(), AgnUtils.getHumanReadableNumber(alreadyImportedFile.getFileSize(), "Byte", false, admin.getLocale()), admin.getDateTimeFormat().format(alreadyImportedFile.getFileDate()));

		reportContent += I18nString.getLocaleString("decode.licenseID", admin.getLocale()) + ": " + configService.getValue(ConfigValue.System_Licence) + "\n";
		
		ComCompany company = companyDao.getCompany(importProfile.getCompanyId());
		reportContent += I18nString.getLocaleString("Company", admin.getLocale()) + ": " + (company == null ? "Unknown" : company.toString()) + "\n";
		
		reportContent += "AutoImport: " + autoImport.toString() + "\n";
		
		reportContent += I18nString.getLocaleString("import.type", admin.getLocale()) + ": " + I18nString.getLocaleString("Recipients", admin.getLocale()) + "\n";
		
		// Show ImportProfile data
		reportContent += I18nString.getLocaleString("import.ImportProfile", admin.getLocale()) + ": \"" + importProfile.getName() + "\" (ID: " + importProfile.getId() + ")\n";
		String profileContent = "";
		if (showVerboseProfileData) {
			try {
				profileContent += I18nString.getLocaleString("Charset", admin.getLocale()) + ": " + Charset.getCharsetById(importProfile.getCharset()).getCharsetName() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("Charset", admin.getLocale()) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			profileContent += I18nString.getLocaleString("csv.ContainsHeaders", admin.getLocale()) + ": " + !importProfile.isNoHeaders() + "\n";
			profileContent += I18nString.getLocaleString("import.zipped", admin.getLocale()) + ": " + importProfile.isZipped() + "\n";
			profileContent += I18nString.getLocaleString("import.zipPassword", admin.getLocale()) + ": " + (importProfile.getZipPassword() != null) + "\n";
			
			profileContent += I18nString.getLocaleString("import.autoMapping", admin.getLocale()) + ": " + importProfile.isAutoMapping() + "\n";
			
			try {
				profileContent += I18nString.getLocaleString("csv.Delimiter", admin.getLocale()) + ": " + Separator.getSeparatorById(importProfile.getSeparator()).getValueChar() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("csv.Delimiter", admin.getLocale()) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("csv.StringQuote", admin.getLocale()) + ": " + TextRecognitionChar.getTextRecognitionCharById(importProfile.getTextRecognitionChar()).name() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("csv.StringQuote", admin.getLocale()) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			profileContent += I18nString.getLocaleString("csv.DecimalSeparator", admin.getLocale()) + ": " + importProfile.getDecimalSeparator() + "\n";
			
			try {
				profileContent += I18nString.getLocaleString("import.dateFormat", admin.getLocale()) + ": " + DateFormat.getDateFormatById(importProfile.getDateFormat()).getValue() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("import.dateFormat", admin.getLocale()) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("Mode", admin.getLocale()) + ": " + I18nString.getLocaleString(ImportMode.getFromInt(importProfile.getImportMode()).getMessageKey(), admin.getLocale()) + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("Mode", admin.getLocale()) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("import.recipients.duplicate", admin.getLocale()) + ": " + CheckForDuplicates.getFromInt(importProfile.getCheckForDuplicates()).name() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("import.recipients.duplicate", admin.getLocale()) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("import.null_value_handling", admin.getLocale()) + ": " + NullValuesAction.getFromInt(importProfile.getNullValuesAction()).name() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("import.null_value_handling", admin.getLocale()) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			try {
				profileContent += I18nString.getLocaleString("recipient.RecipientMailtype", admin.getLocale()) + " (" + I18nString.getLocaleString("import.profile.default", admin.getLocale()) + "): " + MailType.getFromInt(importProfile.getDefaultMailType()).name() + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("recipient.RecipientMailtype", admin.getLocale()) + " (" + I18nString.getLocaleString("import.profile.default", admin.getLocale()) + "): Invalid (\"" + e.getMessage() + "\")\n";
			}
			
			if (admin.permissionAllowed(Permission.IMPORT_MEDIATYPE)) {
				try {
					profileContent += I18nString.getLocaleString("mediatype", admin.getLocale()) + ": " + I18nString.getLocaleString("mailing.MediaType." + importProfile.getMediatype().getMediaCode(), admin.getLocale()) + "\n";
				} catch (Exception e) {
					profileContent += I18nString.getLocaleString("mediatype", admin.getLocale()) + ": Invalid (\"" + e.getMessage() + "\")\n";
				}
			}
			
			profileContent += I18nString.getLocaleString("import.datatype", admin.getLocale()) + ": " + importProfile.getDatatype() + "\n";
			
			profileContent += I18nString.getLocaleString("import.profile.updateAllDuplicates", admin.getLocale()) + ": " + importProfile.getUpdateAllDuplicates() + "\n";
			
			if (importProfile.getImportProcessActionID() > 0) {
				profileContent += "ImportProcessActionID" + ": " + importProfile.getImportProcessActionID() + "\n";
			}
			
			if (importProfile.getActionForNewRecipients() > 0) {
				profileContent += I18nString.getLocaleString("import.actionForNewRecipients", admin.getLocale()) + ": " + importProfile.getActionForNewRecipients() + "\n";
			}
			
			profileContent += I18nString.getLocaleString("import.profile.report.email", admin.getLocale()) + ": " + (StringUtils.isBlank(importProfile.getMailForReport()) ? I18nString.getLocaleString("default.none", admin.getLocale()) : importProfile.getMailForReport()) + "\n";
			profileContent += I18nString.getLocaleString("error.import.profile.email", admin.getLocale()) + ": " + (StringUtils.isBlank(importProfile.getMailForError()) ? I18nString.getLocaleString("default.none", admin.getLocale()) : importProfile.getMailForError()) + "\n";
			
			profileContent += I18nString.getLocaleString("import.profile.gender.settings", admin.getLocale()) + ": ";
			
			if (importProfile.getGenderMapping() != null && importProfile.getGenderMapping().size() > 0) {
				profileContent += "\n\t" + AgnUtils.mapToString(importProfile.getGenderMapping()).replace("\n", "\n\t") + "\n";
			} else {
				profileContent += "NONE\n";
			}
			
			profileContent += I18nString.getLocaleString("Mapping", admin.getLocale()) + ": \n";
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
			
			profileContent += I18nString.getLocaleString("KeyColumn", admin.getLocale()) + ": " + StringUtils.join(importProfile.getKeyColumns(), ", ");
		} else {
			try {
				profileContent += I18nString.getLocaleString("Mode", admin.getLocale()) + ": " + I18nString.getLocaleString(ImportMode.getFromInt(importProfile.getImportMode()).getMessageKey(), admin.getLocale()) + "\n";
			} catch (Exception e) {
				profileContent += I18nString.getLocaleString("Mode", admin.getLocale()) + ": Invalid (\"" + e.getMessage() + "\")\n";
			}
			profileContent += I18nString.getLocaleString("KeyColumn", admin.getLocale()) + ": " + StringUtils.join(importProfile.getKeyColumns(), ", ");
		}
		reportContent += "\t" + profileContent.replace("\n", "\n\t") + "\n";
		
		if (CollectionUtils.isNotEmpty(importProfile.getKeyColumns())) {
			if (!importRecipientsDao.isKeyColumnIndexed(importProfile.getCompanyId(), importProfile.getKeyColumns())) {
				reportContent += "\n*** " + I18nString.getLocaleString("warning.import.keyColumn.index", admin.getLocale()) + " ***\n\n";
			}
		}
		
		reportContent += I18nString.getLocaleString("StartTime", admin.getLocale()) + ": " + admin.getDateTimeFormat().format(new Date()) + "\n";
		
		if (autoImport != null) {
			reportContent += I18nString.getLocaleString("autoImport.fileServer", admin.getLocale()) + ": " + autoImport.getFileServerWithoutCredentials();
		}
		reportContent += I18nString.getLocaleString("settings.FileName", admin.getLocale()) + ": " + (StringUtils.isBlank(alreadyImportedFile.getRemoteFileName()) ? I18nString.getLocaleString("Unknown", admin.getLocale()) : alreadyImportedFile.getRemoteFileName()) + "\n";
		
		reportContent += "\n";
		
		return reportContent;
	}

	public String generateLocalizedImportHtmlReport(ProfileImportWorker importWorker, ComAdmin admin, boolean showVerboseProfileData) throws Exception {
		Locale locale = admin.getLocale();
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
		
		// Show "Customer without binding error"-Error only if ConfigValue for nightly deletey is activated. This ConfigValue is yet to come, so this part is commented
//		// Customer without binding error
//		if (importRecipientsDao.checkUnboundCustomersExist(profileImportWorker.getImportProfile().getCompanyId())) {
//			htmlContent.append("<table border=\"1\" bordercolor=\"" + borderColorCode + "\" cellspacing=\"0\" style=\"border-collapse: collapse; padding: 0px 5px 0px 5px;\">\n");
//			htmlContent.append("<tr bgcolor=\"" + redColorCode + "\">\n");
//			htmlContent.append("<font color=\"" + whiteColorCode + "\">\n");
//			htmlContent.append("<td style=\"padding: 0px 5px 0px 5px;\"><b>").append(StringEscapeUtils.escapeHtml4(I18nString.getLocaleString("error.export.recipient.binding.without.empty", locale))).append("</b></td>\n");
//			htmlContent.append("</font>\n");
//			htmlContent.append("</tr>\n");
//			htmlContent.append("</table>\n");
//			htmlContent.append("<br />\n");
//		}
		
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
		if (importWorker.getMailinglistAssignStatistics() != null && importWorker.getMailinglistAssignStatistics().size() > 0) {
			htmlContent.append(HtmlReporterHelper.getOutputTableSubHeader(I18nString.getLocaleString("Mailinglists", locale), true));
			for (Entry<Integer, Integer> entry : importWorker.getMailinglistAssignStatistics().entrySet()) {
				htmlContent.append(HtmlReporterHelper.getOutputTableResultContentLine("\"" + mailinglistDao.getMailinglistName(entry.getKey(), importWorker.getImportProfile().getCompanyId()) + "\" (ID: " + entry.getKey() + ")", Integer.toString(entry.getValue())));
			}
		}
		
		htmlContent.append(HtmlReporterHelper.getOutputTableContentEnd());
		htmlContent.append(HtmlReporterHelper.getOutputTableEnd());

		// Informations
		htmlContent.append(HtmlReporterHelper.getOutputTableStart());
		htmlContent.append(HtmlReporterHelper.getOutputTableHeader(I18nString.getLocaleString("Info", locale)));
		htmlContent.append(HtmlReporterHelper.getOutputTableContentStart(false));
		
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("decode.licenseID", locale), configService.getValue(ConfigValue.System_Licence)));

		ComCompany company = companyDao.getCompany(importWorker.getImportProfile().getCompanyId());
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
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.zipped", locale), I18nString.getLocaleString(importWorker.getImportProfile().isZipped() ? "default.Yes" : "No", locale)));
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
			
			if (admin.permissionAllowed(Permission.IMPORT_MEDIATYPE)) {
				try {
					htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("mediatype", locale), I18nString.getLocaleString("mailing.MediaType." + importWorker.getImportProfile().getMediatype().getMediaCode(), admin.getLocale())));
				} catch (Exception e) {
					htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("mediatype", locale), "Invalid (\"" + e.getMessage() + "\")"));
				}
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
		
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("StartTime", locale), DateUtilities.getDateTimeString(importWorker.getStartTime(), TimeZone.getTimeZone(admin.getAdminTimezone()).toZoneId(), admin.getDateTimeFormatter())));
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("EndTime", locale), DateUtilities.getDateTimeString(importWorker.getEndTime(), TimeZone.getTimeZone(admin.getAdminTimezone()).toZoneId(), admin.getDateTimeFormatter())));

		if (importWorker.getAutoImport() != null) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("autoImport.fileServer", locale), importWorker.getAutoImport().getFileServerWithoutCredentials()));
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
	
	private String generateLocalizedImportHtmlReportForAlreadyImportedFile(UsedFile alreadyImportedFile, AutoImport autoImport, ImportProfile importProfile, ComAdmin admin, boolean showVerboseProfileData) {
		Locale locale = admin.getLocale();
		String title = "AutoImport: " + autoImport.toString();
		
		StringBuilder htmlContent = new StringBuilder(HtmlReporterHelper.getHtmlPrefixWithCssStyles(title));
		
		htmlContent.append(HtmlReporterHelper.getHeader(title, I18nString.getLocaleString("default.version", locale)));
		
		// Already Imported File Warning
		htmlContent.append(HtmlReporterHelper.getOutputTableStart());
		htmlContent.append(HtmlReporterHelper.getOutputTableContentStart(false));
		
		htmlContent.append(HtmlReporterHelper.getOutputTableWarningContentLine(I18nString.getLocaleString("autoimport.error.fileWasAlreadyImported", locale, alreadyImportedFile.getRemoteFileName(), AgnUtils.getHumanReadableNumber(alreadyImportedFile.getFileSize(), "Byte", false, locale), admin.getDateTimeFormat().format(alreadyImportedFile.getFileDate()))));
		
		htmlContent.append(HtmlReporterHelper.getOutputTableContentEnd());
		htmlContent.append(HtmlReporterHelper.getOutputTableEnd());

		// Informations
		htmlContent.append(HtmlReporterHelper.getOutputTableStart());
		htmlContent.append(HtmlReporterHelper.getOutputTableHeader(I18nString.getLocaleString("Info", locale)));
		htmlContent.append(HtmlReporterHelper.getOutputTableContentStart(false));
		
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("decode.licenseID", locale), configService.getValue(ConfigValue.System_Licence)));

		ComCompany company = companyDao.getCompany(importProfile.getCompanyId());
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
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("import.zipped", locale), I18nString.getLocaleString(importProfile.isZipped() ? "default.Yes" : "No", locale)));
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
			
			if (admin.permissionAllowed(Permission.IMPORT_MEDIATYPE)) {
				try {
					htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("mediatype", locale), I18nString.getLocaleString("mailing.MediaType." + importProfile.getMediatype().getMediaCode(), admin.getLocale())));
				} catch (Exception e) {
					htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("mediatype", locale), "Invalid (\"" + e.getMessage() + "\")"));
				}
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
		
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("StartTime", locale), DateUtilities.getDateTimeString(new Date(), TimeZone.getTimeZone(admin.getAdminTimezone()).toZoneId(), admin.getDateTimeFormatter())));

		if (autoImport != null) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("autoImport.fileServer", locale), autoImport.getFileServerWithoutCredentials()));
		}
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("settings.FileName", locale), StringUtils.isBlank(alreadyImportedFile.getRemoteFileName()) ? I18nString.getLocaleString("Unknown", locale) : alreadyImportedFile.getRemoteFileName()));

		htmlContent.append(HtmlReporterHelper.getOutputTableContentEnd());
		htmlContent.append(HtmlReporterHelper.getOutputTableEnd());
		
		htmlContent.append(HtmlReporterHelper.getFooter(AgnUtils.getHostName(), configService.getValue(ConfigValue.ApplicationVersion)));
		
		return htmlContent.toString();
	}

	private List<ImportReportEntry> generateImportStatusEntries(ProfileImportWorker importWorker, int companyID, Locale locale, boolean noHeaders) {
		List<ImportReportEntry> reportStatusEntries = new ArrayList<>();

		CustomerImportStatus customerImportStatus = importWorker.getStatus();
		if (customerImportStatus.getFatalError() != null) {
			String errorMessage;
			if (importWorker.getError() != null && importWorker.getError() instanceof ImportException) {
				errorMessage = I18nString.getLocaleString(((ImportException) importWorker.getError()).getErrorMessageKey(), locale, ((ImportException) importWorker.getError()).getAdditionalErrorData());
			} else {
				errorMessage = importWorker.getStatus().getFatalError();
			}
			
			reportStatusEntries.add(new ImportReportEntry("import.csv_fatal_error", errorMessage));
		}

		// Show "Customer without binding error"-Error only if ConfigValue for nightly deletey is activated. This ConfigValue is yet to come, so this part is commented
//		if (importRecipientsDao.checkUnboundCustomersExist(companyID)) {
//			reportStatusEntries.add(new ImportReportEntry("error.export.recipient.binding.without.empty", "> 0"));
//		}
		
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
		resultFileContent += "Start time: " + new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM_SS).format(profileImportWorker.getStartTime()) + "\n";
		resultFileContent += "End time: " + new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM_SS).format(profileImportWorker.getEndTime()) + "\n";
		resultFileContent += "License id: " + configService.getValue(ConfigValue.System_Licence) + "\n";
		resultFileContent += "Company id: " + profileImportWorker.getImportProfile().getCompanyId() + "\n";
		resultFileContent += "Admin id: " + adminID + "\n";

		if (profileImportWorker.getAutoImport() != null) {
			resultFileContent += "AutoImport: " + profileImportWorker.getAutoImport().toString() + "\n";
			resultFileContent += "Remote file server: " + profileImportWorker.getAutoImport().getFileServerWithoutCredentials() + "\n";
			resultFileContent += "Remote file pattern: " + profileImportWorker.getAutoImport().getFilePath() + "\n";
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

		List<ImportReportEntry> reportStatusEntries = generateImportStatusEntries(profileImportWorker, profileImportWorker.getImportProfile().getCompanyId(), Locale.ENGLISH, profileImportWorker.getImportProfile().isNoHeaders());
		for (ImportReportEntry entry : reportStatusEntries) {
			if (resultFileContent.length() > 0) {
				resultFileContent += "\n";
			}
			resultFileContent += I18nString.getLocaleString(entry.getKey(), (Locale) null) + ": " + entry.getValue();
		}
		resultFileContent += "\n";

		if (profileImportWorker.getMailinglistAssignStatistics() != null && profileImportWorker.getMailinglistAssignStatistics().size() > 0) {
			resultFileContent += "Mailinglist assignments:\n";
			for (Entry<Integer, Integer> entry : profileImportWorker.getMailinglistAssignStatistics().entrySet()) {
				resultFileContent += "\t\"" + mailinglistDao.getMailinglistName(entry.getKey(), profileImportWorker.getImportProfile().getCompanyId()) + "\" (ID: " + entry.getKey() + "): " + entry.getValue() + "\n";
			}
		}
		return resultFileContent;
	}

	public void sendProfileImportErrorMail(ProfileImportWorker profileImportWorker, ComAdmin admin) throws Exception {
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
		
		if (StringUtils.isNotBlank(configService.getValue(ConfigValue.ImportAlwaysInformEmail, admin.getCompanyID()))) {
			for (String email : AgnUtils.splitAndTrimList(configService.getValue(ConfigValue.ImportAlwaysInformEmail, admin.getCompanyID()))) {
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
			ComCompany company = companyDao.getCompany(profileImportWorker.getImportProfile().getCompanyId());
			
			String subject = "Import-ERROR: " + I18nString.getLocaleString("import.recipients.report", admin.getLocale()) + ": " + " \"" + profileImportWorker.getImportProfile().getName() + "\" (" + I18nString.getLocaleString("Company", admin.getLocale()) + ": " + company.getShortname() + ")";
			String bodyHtml = generateLocalizedImportHtmlReport(profileImportWorker, admin, true);
			String bodyText = "Import-ERROR:\n" + generateLocalizedImportTextReport(profileImportWorker, admin, true);
			
			javaMailService.sendEmail(StringUtils.join(emailRecipients, ", "), subject, bodyText, bodyHtml);
		}
	}

	public void sendProfileImportReportAlreadyImportedFileMail(UsedFile alreadyImportedFile, AutoImport autoImport, ImportProfile importProfile, ComAdmin admin) {
		Set<String> emailRecipients = new HashSet<>();
		
		if (StringUtils.isNotBlank(importProfile.getMailForReport())) {
			for (String email : AgnUtils.splitAndTrimList(importProfile.getMailForReport())) {
				emailRecipients.add(AgnUtils.normalizeEmail(email));
			}
		}
		
		if (StringUtils.isNotBlank(configService.getValue(ConfigValue.ImportAlwaysInformEmail, admin.getCompanyID()))) {
			for (String email : AgnUtils.splitAndTrimList(configService.getValue(ConfigValue.ImportAlwaysInformEmail, admin.getCompanyID()))) {
				emailRecipients.add(AgnUtils.normalizeEmail(email));
			}
		}

		if (!emailRecipients.isEmpty()) {
			Locale emailLocale = admin != null ? admin.getLocale() : null;
			
			ComCompany company = companyDao.getCompany(importProfile.getCompanyId());
			
			String subject = I18nString.getLocaleString("import.recipients.report", emailLocale) + " \"" + importProfile.getName() + "\" (" + I18nString.getLocaleString("Company", emailLocale) + ": " + company.getShortname() + ")";
			String bodyHtml = generateLocalizedImportHtmlReportForAlreadyImportedFile(alreadyImportedFile, autoImport, importProfile, admin, true);
			String bodyText = generateLocalizedImportTextReportForAlreadyImportedFile(alreadyImportedFile, autoImport, importProfile, admin, true);
			
			javaMailService.sendEmail(StringUtils.join(emailRecipients, ", "), subject, bodyText, bodyHtml);
		}
	}

	public int writeProfileImportReportAlreadyImportedFile(UsedFile alreadyImportedFile, AutoImport autoImport, ImportProfile importProfile, ComAdmin admin) throws Exception {
		String resultFileContent = generateLocalizedImportHtmlReportForAlreadyImportedFile(alreadyImportedFile, autoImport, importProfile, admin, true);
		int autoImportID = -1;
		if (autoImport != null) {
			autoImportID = autoImport.getAutoImportId();
		}
		return recipientsReportService.createAndSaveImportReport(admin, RecipientReportUtils.IMPORT_RESULT_FILE_PREFIX, 0, new Date(), resultFileContent, autoImportID, false).getId();
	}
}
