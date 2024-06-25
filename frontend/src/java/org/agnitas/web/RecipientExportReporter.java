/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Company;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;
import com.agnitas.emm.core.recipientsreport.service.RecipientsReportService;
import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.dataset.CommonKeys;
import org.agnitas.beans.ExportColumnMapping;
import org.agnitas.emm.core.autoexport.bean.AutoExport;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.RecipientExportWorker;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.importvalues.Separator;
import org.agnitas.util.importvalues.TextRecognitionChar;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class RecipientExportReporter {
	
	private final RecipientsReportService recipientsReportService;
	private final JavaMailService javaMailService;
	private final ComCompanyDao companyDao;
	private final AdminService adminService;
	private final ConfigService configService;
	private final MailinglistService mailinglistService;
	private final ComTargetDao targetDao;

	public RecipientExportReporter(RecipientsReportService recipientsReportService, JavaMailService javaMailService, ComCompanyDao companyDao, AdminService adminService,
								   ConfigService configService, MailinglistService mailinglistService, ComTargetDao targetDao) {

		this.recipientsReportService = recipientsReportService;
		this.javaMailService = javaMailService;
		this.companyDao = companyDao;
		this.adminService = adminService;
		this.configService = configService;
		this.mailinglistService = mailinglistService;
		this.targetDao = targetDao;
	}

	/**
	 * Send a report email about this export to the executing GUI-admin or the creator of the autoexport
	 */
	public void sendExportReportMail(RecipientExportWorker exportWorker) throws Exception {
		Set<String> emailRecipients = new HashSet<>();
		
		String additionalContent = "";
		int companyID = exportWorker.getExportProfile().getCompanyID();
		Company comp = companyDao.getCompany(companyID);
		if (comp.getExportNotifyAdmin() > 0) {
			final Admin notifyAdmin = adminService.getAdmin(comp.getExportNotifyAdmin(), companyID);

			if (notifyAdmin != null && StringUtils.isNotBlank(notifyAdmin.getEmail())) {
				emailRecipients.add(AgnUtils.normalizeEmail(notifyAdmin.getEmail()));
			} else {
				emailRecipients.add(AgnUtils.normalizeEmail(configService.getValue(ConfigValue.Mailaddress_Error)));
				additionalContent = "Admin User or Email for this export was not available: CompanyID: " + companyID + " / AdminID: " + comp.getExportNotifyAdmin() + " \n\n";
			}
		}
		
		if (StringUtils.isNotBlank(configService.getValue(ConfigValue.ExportAlwaysInformEmail, companyID))) {
			if (StringUtils.isNotBlank(configService.getValue(ConfigValue.ExportAlwaysInformEmail, companyID))) {
				for (String email : AgnUtils.splitAndTrimList(configService.getValue(ConfigValue.ExportAlwaysInformEmail, companyID))) {
					emailRecipients.add(email.toLowerCase());
				}
			}
		}

		if (exportWorker.getAutoExport() != null && StringUtils.isNotBlank(exportWorker.getAutoExport().getEmailForReport())) {
			for (String email : AgnUtils.splitAndTrimList(exportWorker.getAutoExport().getEmailForReport())) {
				emailRecipients.add(AgnUtils.normalizeEmail(email));
			}
		}

		if (!emailRecipients.isEmpty()) {
			Locale locale = exportWorker.getExportProfile().getLocale();

			if (exportWorker.getAutoExport() != null) {
				locale = exportWorker.getAutoExport().getLocale();
			}

			Company company = companyDao.getCompany(companyID);
			
			String subject;
			if (StringUtils.isEmpty(exportWorker.getExportProfile().getShortname())) {
				subject = I18nString.getLocaleString("ResultMsg", locale) + ": " + I18nString.getLocaleString("Export", locale) + " (" + I18nString.getLocaleString("Company", locale) + ": " + company.getShortname() + ")";
			} else {
				subject = I18nString.getLocaleString("ResultMsg", locale) + ": \"" + exportWorker.getExportProfile().getShortname() + "\" (" + I18nString.getLocaleString("Company", locale) + ": " + company.getShortname() + ")";
			}
			String bodyHtml = generateLocalizedExportHtmlReport(exportWorker) + "\n" + additionalContent;
			String bodyText = generateLocalizedExportTextReport(exportWorker) + "\n" + additionalContent;
			
			javaMailService.sendEmail(companyID, StringUtils.join(emailRecipients, ", "), subject, bodyText, bodyHtml);
		}
	}

	private String generateLocalizedExportTextReport(RecipientExportWorker exportWorker) throws Exception {
		Locale locale = exportWorker.getExportProfile().getLocale();
		String reportContent = I18nString.getLocaleString("ResultMsg", locale) + " \"" + exportWorker.getExportProfile().getShortname() + "\":\n\n";

		reportContent += I18nString.getLocaleString("decode.licenseID", locale) + ": " + configService.getValue(ConfigValue.System_Licence) + "\n";
		
		Company company = companyDao.getCompany(exportWorker.getExportProfile().getCompanyID());
		reportContent += I18nString.getLocaleString("Company", locale) + ": " + (company == null ? "Unknown" : company.getShortname() + " (ID: " + company.getId() + ")") + "\n";
		
		if (exportWorker.getAutoExport() != null) {
			reportContent += "AutoExport: " + exportWorker.getAutoExport().getShortname() + " (ID: " + exportWorker.getAutoExport().getAutoExportId() + ")" + "\n";
			reportContent += I18nString.getLocaleString("error.import.profile.email", locale) + ": " + (StringUtils.isBlank(exportWorker.getAutoExport().getEmailOnError()) ? I18nString.getLocaleString("default.none", locale) : exportWorker.getAutoExport().getEmailOnError());
			reportContent += I18nString.getLocaleString("autoImport.fileServer", locale) + ": " + exportWorker.getAutoExport().getFileServerWithoutCredentials();
			reportContent += I18nString.getLocaleString("autoImport.filePath", locale) + ": " + exportWorker.getAutoExport().getFilePath();
			reportContent += I18nString.getLocaleString("export.settings.FileNamePattern", locale) + ": " + (StringUtils.isBlank(exportWorker.getAutoExport().getFileNameWithPatterns()) ? AutoExport.DEFAULT_EXPORT_FILENAME_PATTERN : exportWorker.getAutoExport().getFileNameWithPatterns());
		}
		
		if (exportWorker.getUsername() != null) {
			reportContent += "User: " + exportWorker.getUsername();
		}
		
		reportContent += I18nString.getLocaleString("export.type", locale) + ": " + I18nString.getLocaleString("Recipients", locale) + "\n";
		
		reportContent += I18nString.getLocaleString("export.ExportProfile", locale) + ": " + exportWorker.getExportProfile().getShortname() + " (ID: " + exportWorker.getExportProfile().getId() + ")\n";
		
		String profileContent = "";

		profileContent += I18nString.getLocaleString("Charset", locale) + ": " + exportWorker.getExportProfile().getCharset() + "\n";

		try {
			profileContent += I18nString.getLocaleString("csv.Delimiter", locale) + ": " + Character.toString(Separator.getSeparatorByChar(exportWorker.getDelimiter()).getValueChar()) + "\n";
		} catch (Exception e) {
			profileContent += I18nString.getLocaleString("csv.Delimiter", locale) + ": " + "Invalid (\"" + e.getMessage() + "\")" + "\n";
		}
		
		try {
			profileContent += I18nString.getLocaleString("csv.StringQuote", locale) + ": " + I18nString.getLocaleString(TextRecognitionChar.getTextRecognitionCharByChar(exportWorker.getStringQuote()).getPublicValue(), locale) + "\n";
		} catch (Exception e) {
			profileContent += I18nString.getLocaleString("csv.StringQuote", locale) + ": " + "Invalid (\"" + e.getMessage() + "\")" + "\n";
		}
		profileContent += I18nString.getLocaleString("csv.alwaysQuote", locale) + ": " + I18nString.getLocaleString(exportWorker.getAlwaysQuote() ? "delimiter.always" : "delimiter.ifneeded", locale) + "\n";

		if (exportWorker.getDateFormat() != null) {
			profileContent += I18nString.getLocaleString("csv.DateFormat", locale) + ": " + ((SimpleDateFormat) exportWorker.getDateFormat()).toPattern() + "\n";
		} else if (exportWorker.getDateFormatter() != null){
			profileContent += I18nString.getLocaleString("csv.DateFormat", locale) + ": " + exportWorker.getDateFormatter().toString() + "\n";
		}
		if (exportWorker.getDateTimeFormat() != null) {
			profileContent += I18nString.getLocaleString("csv.DateTimeFormat", locale) + ": " + ((SimpleDateFormat) exportWorker.getDateTimeFormat()).toPattern() + "\n";
		} else if (exportWorker.getDateTimeFormatter() != null){
			profileContent += I18nString.getLocaleString("csv.DateTimeFormat", locale) + ": " + exportWorker.getDateTimeFormatter().toString() + "\n";
		}
		if (exportWorker.getExportTimezone() != null) {
			profileContent += I18nString.getLocaleString("Timezone", locale) + ": " + exportWorker.getExportTimezone().getId() + "\n";
		}
		if (exportWorker.getDecimalFormat() != null) {
			char decimalSeparator = ((DecimalFormat) exportWorker.getDecimalFormat()).getDecimalFormatSymbols().getDecimalSeparator();
			profileContent += I18nString.getLocaleString("csv.DecimalSeparator", locale) + ": " + Character.toString(decimalSeparator) + "\n";
		}
		
		if (exportWorker.getExportProfile().getExportColumnMappings() != null && !exportWorker.getExportProfile().getExportColumnMappings().isEmpty()) {
			String exportedColumns = StringUtils.join(exportWorker.getExportProfile().getExportColumnMappings().stream().map(ExportColumnMapping::getDbColumn).collect(Collectors.toList()), ";");
			profileContent += I18nString.getLocaleString("htmled.columns", locale) + ": " + StringUtils.join(AgnUtils.removeEmptyFromStringlist(AgnUtils.splitAndTrimList(exportedColumns)), ", ") + "\n";
		} else {
			profileContent += I18nString.getLocaleString("htmled.columns", locale) + ": " + I18nString.getLocaleString("default.none", locale) + "\n";
		}
		
		if (StringUtils.isNotBlank(exportWorker.getExportProfile().getMailinglists())) {
			profileContent += I18nString.getLocaleString("Mailinglists", locale) + ": " + StringUtils.join(AgnUtils.removeEmptyFromStringlist(AgnUtils.splitAndTrimList(exportWorker.getExportProfile().getMailinglists())), ", ") + "\n";
		}
		
		if (exportWorker.getExportProfile().getMailinglistID() > 0) {
			profileContent += I18nString.getLocaleString("Mailinglist", locale) + ": " + mailinglistService.getMailinglistName(exportWorker.getExportProfile().getMailinglistID(), exportWorker.getExportProfile().getCompanyID()) + " (ID: " + Integer.toString(exportWorker.getExportProfile().getMailinglistID()) + ")" + "\n";
		} else {
			profileContent += I18nString.getLocaleString("Mailinglist", locale) + ": " + I18nString.getLocaleString(CommonKeys.ALL_MAILINGLISTS, locale);
		}
		
		if (exportWorker.getExportProfile().getTargetID() > 0) {
			profileContent += I18nString.getLocaleString("Target", locale) + ": " + targetDao.getTargetName(exportWorker.getExportProfile().getTargetID(), exportWorker.getExportProfile().getCompanyID(), false) + " (ID: " + Integer.toString(exportWorker.getExportProfile().getTargetID()) + ")" + "\n";
		} else {
			profileContent += I18nString.getLocaleString("Target", locale) + ": " + I18nString.getLocaleString("statistic.all_subscribers", locale);
		}

		reportContent += "\t" + profileContent.replace("\n", "\n\t") + "\n";
		
		reportContent += I18nString.getLocaleString("StartTime", locale) + ": " + (exportWorker.getStartTime() == null ? I18nString.getLocaleString("Unknown", locale) : new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM_SS).format(exportWorker.getStartTime())) + "\n";
		reportContent += I18nString.getLocaleString("EndTime", locale) + ": " + (exportWorker.getEndTime() == null ? I18nString.getLocaleString("Unknown", locale) : new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM_SS).format(exportWorker.getEndTime())) + "\n";
				
		if (exportWorker.getRemoteFile() != null) {
			reportContent += I18nString.getLocaleString("settings.FileName", locale) + ": " + (StringUtils.isBlank(exportWorker.getRemoteFile().getRemoteFilePath()) ? I18nString.getLocaleString("Unknown", locale) : exportWorker.getRemoteFile().getRemoteFilePath()) + "\n";
			if (exportWorker.getRemoteFile().getDownloadDurationMillis() > -1) {
				reportContent += I18nString.getLocaleString("UploadDuration", locale) + ": " + AgnUtils.getHumanReadableDurationFromMillis(exportWorker.getRemoteFile().getDownloadDurationMillis()) + "\n";
			}
		}
		
		if (exportWorker.getError() != null) {
			reportContent += I18nString.getLocaleString("import.csv_fatal_error", locale) + ": " + exportWorker.getError().getClass().getSimpleName() + ": \n" + exportWorker.getError().getMessage() + "\n";
		}
		
		if (new File(exportWorker.getExportFile()).length() >= 0) {
			reportContent += I18nString.getLocaleString("mailing.Graphics_Component.FileSize", locale) + ": " + AgnUtils.getHumanReadableNumber(new File(exportWorker.getExportFile()).length(), "Byte", false, locale) + "\n";
		}
		
		reportContent += I18nString.getLocaleString("import.result.csvlines", locale) + ": " + AgnUtils.getHumanReadableNumber(exportWorker.getExportedLines(), locale) + " (+1 " + I18nString.getLocaleString("csv.ContainsHeaders", locale) + ")" + "\n";
		
		return reportContent;
	}

	private String generateLocalizedExportHtmlReport(RecipientExportWorker exportWorker) {
		Locale locale;
		String title;

		if (exportWorker.getAutoExport() != null) {
			locale = exportWorker.getAutoExport().getLocale();
			title = "AutoExport: " + exportWorker.getAutoExport().getShortname() + " (ID: " + exportWorker.getAutoExport().getAutoExportId() + ")";
		} else {
			locale = exportWorker.getExportProfile().getLocale();
			title = "Export: " + exportWorker.getExportProfile().getShortname();
		}
		
		StringBuilder htmlContent = new StringBuilder(HtmlReporterHelper.getHtmlPrefixWithCssStyles(title));
		
		htmlContent.append(HtmlReporterHelper.getHeader(title, I18nString.getLocaleString("default.version", locale)));
		
		// Fatal Error
		if (exportWorker.getError() != null) {
			htmlContent.append(HtmlReporterHelper.getOutputTableStart());
			htmlContent.append(HtmlReporterHelper.getOutputTableHeader(I18nString.getLocaleString("import.csv_fatal_error", locale)));
			htmlContent.append(HtmlReporterHelper.getOutputTableContentStart(false));
			
			htmlContent.append(HtmlReporterHelper.getOutputTableErrorContentLine(exportWorker.getError().getClass().getSimpleName() + ": \n" + exportWorker.getError().getMessage()));

			htmlContent.append(HtmlReporterHelper.getOutputTableContentEnd());
			htmlContent.append(HtmlReporterHelper.getOutputTableEnd());
		}

		// Results
		htmlContent.append(HtmlReporterHelper.getOutputTableStart());
		htmlContent.append(HtmlReporterHelper.getOutputTableHeader(I18nString.getLocaleString("ResultMsg", locale)));
		htmlContent.append(HtmlReporterHelper.getOutputTableContentStart(true));

		if (new File(exportWorker.getExportFile()).length() >= 0) {
			htmlContent.append(HtmlReporterHelper.getOutputTableResultContentLine(I18nString.getLocaleString("mailing.Graphics_Component.FileSize", locale), AgnUtils.getHumanReadableNumber(new File(exportWorker.getExportFile()).length(), "Byte", false, locale)));
		}

		htmlContent.append(HtmlReporterHelper.getOutputTableResultContentLine(I18nString.getLocaleString("import.result.csvlines", locale), AgnUtils.getHumanReadableNumber(exportWorker.getExportedLines(), locale) + " (+1 " + I18nString.getLocaleString("csv.ContainsHeaders", locale) + ")"));
		
		htmlContent.append(HtmlReporterHelper.getOutputTableContentEnd());
		htmlContent.append(HtmlReporterHelper.getOutputTableEnd());

		// Informations
		htmlContent.append(HtmlReporterHelper.getOutputTableStart());
		htmlContent.append(HtmlReporterHelper.getOutputTableHeader(I18nString.getLocaleString("Info", locale)));
		htmlContent.append(HtmlReporterHelper.getOutputTableContentStart(false));
		
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("decode.licenseID", locale), configService.getValue(ConfigValue.System_Licence)));

		Company company = companyDao.getCompany(exportWorker.getExportProfile().getCompanyID());
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Company", locale), (company == null ? "Unknown" : company.getShortname() + " (ID: " + company.getId() + ")")));
		
		if (exportWorker.getAutoExport() != null) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("AutoExport", exportWorker.getAutoExport().getShortname() + " (ID: " + exportWorker.getAutoExport().getAutoExportId() + ")"));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("error.import.profile.email", locale), (StringUtils.isBlank(exportWorker.getAutoExport().getEmailOnError()) ? I18nString.getLocaleString("default.none", locale) : exportWorker.getAutoExport().getEmailOnError())));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("autoImport.fileServer", locale), exportWorker.getAutoExport().getFileServerWithoutCredentials()));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("autoImport.filePath", locale), exportWorker.getAutoExport().getFilePath()));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("export.settings.FileNamePattern", locale), (StringUtils.isBlank(exportWorker.getAutoExport().getFileNameWithPatterns()) ? AutoExport.DEFAULT_EXPORT_FILENAME_PATTERN : exportWorker.getAutoExport().getFileNameWithPatterns())));
		}
		
		if (exportWorker.getUsername() != null) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("User", exportWorker.getUsername()));
		}
		
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("export.type", locale), I18nString.getLocaleString("Recipients", locale)));
		
		// Exportprofile
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("export.ExportProfile", locale), exportWorker.getExportProfile().getShortname() + " (ID: " + exportWorker.getExportProfile().getId() + ")"));
		
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Charset", locale), exportWorker.getExportProfile().getCharset()));

		try {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.Delimiter", locale), Character.toString(Separator.getSeparatorByChar(exportWorker.getDelimiter()).getValueChar())));
		} catch (Exception e) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.Delimiter", locale), "Invalid (\"" + e.getMessage() + "\")"));
		}
		
		try {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.StringQuote", locale), I18nString.getLocaleString(TextRecognitionChar.getTextRecognitionCharByChar(exportWorker.getStringQuote()).getPublicValue(), locale)));
		} catch (Exception e) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.StringQuote", locale), "Invalid (\"" + e.getMessage() + "\")"));
		}
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.alwaysQuote", locale), I18nString.getLocaleString(exportWorker.getAlwaysQuote() ? "delimiter.always" : "delimiter.ifneeded", locale)));

		if (exportWorker.getDateFormat() != null) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.DateFormat", locale), ((SimpleDateFormat) exportWorker.getDateFormat()).toPattern()));
		} else if (exportWorker.getDateFormatter() != null){
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.DateFormat", locale), exportWorker.getDateFormatter().toString()));
		}
		if (exportWorker.getDateTimeFormat() != null) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.DateTimeFormat", locale), ((SimpleDateFormat) exportWorker.getDateTimeFormat()).toPattern()));
		} else if (exportWorker.getDateTimeFormatter() != null){
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.DateTimeFormat", locale), exportWorker.getDateTimeFormatter().toString()));
		}
		if (exportWorker.getExportTimezone() != null) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Timezone", locale), exportWorker.getExportTimezone().getId()));
		}
		if (exportWorker.getDecimalFormat() != null) {
			char decimalSeparator = ((DecimalFormat) exportWorker.getDecimalFormat()).getDecimalFormatSymbols().getDecimalSeparator();
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("csv.DecimalSeparator", locale), Character.toString(decimalSeparator)));
		}
		
		if (exportWorker.getExportProfile().getExportColumnMappings() != null && !exportWorker.getExportProfile().getExportColumnMappings().isEmpty()) {
			String exportedColumns = StringUtils.join(exportWorker.getExportProfile().getExportColumnMappings().stream().map(ExportColumnMapping::getDbColumn).collect(Collectors.toList()), ";");
			htmlContent.append(HtmlReporterHelper.getOutputTableSubHeader(I18nString.getLocaleString("htmled.columns", locale), false));
			for (String columnName : AgnUtils.removeEmptyFromStringlist(AgnUtils.splitAndTrimList(exportedColumns))) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("", columnName));
			}
		} else {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("htmled.columns", locale), I18nString.getLocaleString("default.none", locale)));
		}
		
		if (StringUtils.isNotBlank(exportWorker.getExportProfile().getMailinglists())) {
			htmlContent.append(HtmlReporterHelper.getOutputTableSubHeader(I18nString.getLocaleString("Mailinglists", locale), false));
			for (String selectedMailinglistIDString : AgnUtils.removeEmptyFromStringlist(AgnUtils.splitAndTrimList(exportWorker.getExportProfile().getMailinglists()))) {
				int selectedMailinglistID = Integer.parseInt(selectedMailinglistIDString);
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("", mailinglistService.getMailinglistName(selectedMailinglistID, exportWorker.getExportProfile().getCompanyID()) + " (ID: " + Integer.toString(selectedMailinglistID) + ")"));
			}
		}
		
		if (exportWorker.getExportProfile().getMailinglistID() > 0) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Mailinglist", locale), mailinglistService.getMailinglistName(exportWorker.getExportProfile().getMailinglistID(), exportWorker.getExportProfile().getCompanyID()) + " (ID: " + Integer.toString(exportWorker.getExportProfile().getMailinglistID()) + ")"));
		} else {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Mailinglist", locale), I18nString.getLocaleString(CommonKeys.ALL_MAILINGLISTS, locale)));
		}
		
		if (exportWorker.getExportProfile().getTargetID() > 0) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Target", locale), targetDao.getTargetName(exportWorker.getExportProfile().getTargetID(), exportWorker.getExportProfile().getCompanyID(), false) + " (ID: " + Integer.toString(exportWorker.getExportProfile().getTargetID()) + ")"));
		} else {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Target", locale), I18nString.getLocaleString("statistic.all_subscribers", locale)));
		}
		
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("StartTime", locale), (exportWorker.getStartTime() == null ? I18nString.getLocaleString("Unknown", locale) : DateUtilities.getDateTimeString(exportWorker.getStartTime(), TimeZone.getTimeZone(exportWorker.getExportProfile().getTimezone()).toZoneId(), exportWorker.getExportProfile().getDateTimeFormatter()))));
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("EndTime", locale), (exportWorker.getEndTime() == null ? I18nString.getLocaleString("Unknown", locale) : DateUtilities.getDateTimeString(exportWorker.getEndTime(), TimeZone.getTimeZone(exportWorker.getExportProfile().getTimezone()).toZoneId(), exportWorker.getExportProfile().getDateTimeFormatter()))));

		if (exportWorker.getRemoteFile() != null) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("settings.FileName", locale), StringUtils.isBlank(exportWorker.getRemoteFile().getRemoteFilePath()) ? I18nString.getLocaleString("Unknown", locale) : exportWorker.getRemoteFile().getRemoteFilePath()));
			if (exportWorker.getRemoteFile().getDownloadDurationMillis() > -1) {
				htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("UploadDuration", locale), AgnUtils.getHumanReadableDurationFromMillis(exportWorker.getRemoteFile().getDownloadDurationMillis())));
			}
		}

		htmlContent.append(HtmlReporterHelper.getOutputTableContentEnd());
		htmlContent.append(HtmlReporterHelper.getOutputTableEnd());
		
		htmlContent.append(HtmlReporterHelper.getFooter(AgnUtils.getHostName(), configService.getValue(ConfigValue.ApplicationVersion)));
		
		return htmlContent.toString();
	}

	public void sendExportErrorMail(RecipientExportWorker exportWorker) throws Exception {
		Set<String> emailRecipients = new HashSet<>();

		String additionalContent = "";
		int companyID = exportWorker.getExportProfile().getCompanyID();
		Company comp = companyDao.getCompany(companyID);
		if (comp.getExportNotifyAdmin() > 0) {
			final Admin notifyAdmin = adminService.getAdmin(comp.getExportNotifyAdmin(), companyID);

			if (notifyAdmin != null && StringUtils.isNotBlank(notifyAdmin.getEmail())) {
				emailRecipients.add(AgnUtils.normalizeEmail(notifyAdmin.getEmail()));
			} else {
				emailRecipients.add(AgnUtils.normalizeEmail(configService.getValue(ConfigValue.Mailaddress_Error)));
				additionalContent = "Admin User or Email for this export was not available: CompanyID: " + companyID + " / AdminID: " + comp.getExportNotifyAdmin() + " \n\n";
			}
		}

		if (StringUtils.isNotBlank(configService.getValue(ConfigValue.ExportAlwaysInformEmail, companyID))) {
			for (String email : AgnUtils.splitAndTrimList(configService.getValue(ConfigValue.ExportAlwaysInformEmail, companyID))) {
				emailRecipients.add(email.toLowerCase());
			}
		}
		
		if (StringUtils.isNotBlank(configService.getValue(ConfigValue.Mailaddress_Error))) {
			emailRecipients.add(configService.getValue(ConfigValue.Mailaddress_Error).toLowerCase());
		}

		if (exportWorker.getAutoExport() != null && StringUtils.isNotBlank(exportWorker.getAutoExport().getEmailForReport())) {
			for (String email : AgnUtils.splitAndTrimList(exportWorker.getAutoExport().getEmailForReport())) {
				emailRecipients.add(AgnUtils.normalizeEmail(email));
			}
		}

		if (exportWorker.getAutoExport() != null && StringUtils.isNotBlank(exportWorker.getAutoExport().getEmailOnError())) {
			for (String email : AgnUtils.splitAndTrimList(exportWorker.getAutoExport().getEmailOnError())) {
				emailRecipients.add(AgnUtils.normalizeEmail(email));
			}
		}

		if (!emailRecipients.isEmpty()) {
			Locale locale = exportWorker.getExportProfile().getLocale();

			if (exportWorker.getAutoExport() != null) {
				locale = exportWorker.getAutoExport().getLocale();
			}

			Company company = companyDao.getCompany(companyID);
			
			String subject;
			if (StringUtils.isEmpty(exportWorker.getExportProfile().getShortname())) {
				subject = "Export-ERROR: " + I18nString.getLocaleString("ResultMsg", locale) + ": " + I18nString.getLocaleString("Export", locale) + " (" + I18nString.getLocaleString("Company", locale) + ": " + company.getShortname() + ")";
			} else {
				subject = "Export-ERROR: " + I18nString.getLocaleString("ResultMsg", locale) + ": \"" + exportWorker.getExportProfile().getShortname() + "\" (" + I18nString.getLocaleString("Company", locale) + ": " + company.getShortname() + ")";
			}
			String bodyHtml = generateLocalizedExportHtmlReport(exportWorker) + "\n" + additionalContent;
			String bodyText = "Export-ERROR:\n" + generateLocalizedExportTextReport(exportWorker) + "\n" + additionalContent;
						
			javaMailService.sendEmail(company.getId(),StringUtils.join(emailRecipients, ", "), subject, bodyText, bodyHtml);
		}
	}
	
	public void createAndSaveExportReport(RecipientExportWorker exportWorker, Admin admin, boolean isError) throws Exception {
		String fileToShow;
		if (exportWorker.getRemoteFile() != null && StringUtils.isNotBlank(exportWorker.getRemoteFile().getRemoteFilePath())) {
			// Remote file on ftp or sftp server
			fileToShow = exportWorker.getRemoteFile().getRemoteFilePath();
		} else {
			//Local exported File
			fileToShow = exportWorker.getExportFile();
		}

		if (configService.getBooleanValue(ConfigValue.WriteExtendedRecipientReport, exportWorker.getExportProfile().getCompanyID())) {
			boolean isAutoExport = exportWorker.getAutoExport() != null;

			RecipientsReport report = new RecipientsReport();
			report.setFilename(fileToShow);
			report.setReportDate(exportWorker.getEndTime());
			report.setIsError(isError);

			report.setEntityId(isAutoExport
					? exportWorker.getAutoExport().getAutoExportId()
					: exportWorker.getExportProfile().getId()
			);

			report.setEntityType(RecipientsReport.EntityType.EXPORT);
			report.setEntityExecution(isAutoExport ? RecipientsReport.EntityExecution.AUTOMATIC : RecipientsReport.EntityExecution.MANUAL);
			report.setEntityData(RecipientsReport.EntityData.PROFILE);

			recipientsReportService.saveNewReport(admin, exportWorker.getExportProfile().getCompanyID(), report, generateLocalizedExportHtmlReport(exportWorker));
		} else {
			recipientsReportService.createAndSaveExportReport(exportWorker.getExportProfile().getCompanyID(), admin, fileToShow, exportWorker.getEndTime(), generateLocalizedExportHtmlReport(exportWorker), isError);
		}
	}
}

