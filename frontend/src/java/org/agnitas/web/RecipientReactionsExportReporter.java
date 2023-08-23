/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.agnitas.emm.core.autoexport.bean.AutoExport;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.RecipientReactionsAndStatusExportWorker;
import org.agnitas.service.RecipientReactionsExportWorker;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.importvalues.Separator;
import org.agnitas.util.importvalues.TextRecognitionChar;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Company;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.recipientsreport.service.RecipientsReportService;
import com.agnitas.messages.I18nString;

public class RecipientReactionsExportReporter {
	
	private final JavaMailService javaMailService;
	private final ComCompanyDao companyDao;
	private final AdminService adminService;
	private final RecipientsReportService recipientsReportService;
	private final ConfigService configService;

	public RecipientReactionsExportReporter(JavaMailService javaMailService, ComCompanyDao companyDao, AdminService adminService, RecipientsReportService recipientsReportService,
											ConfigService configService) {

		this.javaMailService = javaMailService;
		this.companyDao = companyDao;
		this.adminService = adminService;
		this.recipientsReportService = recipientsReportService;
		this.configService = configService;
	}

	/**
	 * Send a report email about this export to the executing GUI-admin or the creator of the autoexport
	 */
	public void sendExportReportMail(RecipientReactionsExportWorker exportWorker) throws Exception {
		Locale locale = exportWorker.getAutoExport().getLocale();
		int companyID = exportWorker.getAutoExport().getCompanyId();
		
		Set<String> emailRecipients = new HashSet<>();
		
		String additionalContent = "";
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
					emailRecipients.add(AgnUtils.normalizeEmail(email));
				}
			}
		}

		if (exportWorker.getAutoExport() != null && StringUtils.isNotBlank(exportWorker.getAutoExport().getEmailForReport())) {
			for (String email : AgnUtils.splitAndTrimList(exportWorker.getAutoExport().getEmailForReport())) {
				emailRecipients.add(AgnUtils.normalizeEmail(email));
			}
		}

		if (!emailRecipients.isEmpty()) {
			Company company = companyDao.getCompany(companyID);
			
			String subject = I18nString.getLocaleString("ResultMsg", locale) + " \"" + I18nString.getLocaleString(exportWorker instanceof RecipientReactionsAndStatusExportWorker ? "statistic.reactionsAndStatus" : "statistic.reactions", locale) + "\" (" + I18nString.getLocaleString("Company", locale) + ": " + company.getShortname() + ")";
			String bodyHtml = generateLocalizedExportHtmlReport(exportWorker) + "\n" + additionalContent;
			String bodyText = generateLocalizedExportTextReport(exportWorker) + "\n" + additionalContent;
			
			javaMailService.sendEmail(company.getId(), StringUtils.join(emailRecipients, ", "), subject, bodyText, bodyHtml);
		}
	}

	private String generateLocalizedExportTextReport(RecipientReactionsExportWorker exportWorker) throws Exception {
		Locale locale = exportWorker.getAutoExport().getLocale();
		
		String reportContent = I18nString.getLocaleString("ResultMsg", locale) + " \"" + I18nString.getLocaleString(exportWorker instanceof RecipientReactionsAndStatusExportWorker ? "statistic.reactionsAndStatus" : "statistic.reactions", locale) + "\":\n\n";

		reportContent += I18nString.getLocaleString("decode.licenseID", locale) + ": " + configService.getValue(ConfigValue.System_Licence) + "\n";
		
		Company company = companyDao.getCompany(exportWorker.getAutoExport().getCompanyId());
		reportContent += I18nString.getLocaleString("Company", locale) + ": " + (company == null ? "Unknown" : company.getShortname() + " (ID: " + company.getId() + ")") + "\n";
		
		
		if (exportWorker.getDateTimeFormat() != null) {
			reportContent += I18nString.getLocaleString("report.data", locale) + " " + I18nString.getLocaleString("StartTime", locale) + ": " + (exportWorker.getExportDataStartDate() == null ? I18nString.getLocaleString("Unknown", locale) : exportWorker.getDateTimeFormat().format(exportWorker.getExportDataStartDate())) + "\n";
			reportContent += I18nString.getLocaleString("report.data", locale) + " " + I18nString.getLocaleString("EndTime", locale) + ": " + (exportWorker.getExportDataEndDate() == null ? I18nString.getLocaleString("Unknown", locale) : exportWorker.getDateTimeFormat().format(exportWorker.getExportDataEndDate())) + "\n";
		} else {
			reportContent += I18nString.getLocaleString("report.data", locale) + " " + I18nString.getLocaleString("StartTime", locale) + ": " + (exportWorker.getExportDataStartDate() == null ? I18nString.getLocaleString("Unknown", locale) : DateUtilities.getDateTimeString(exportWorker.getExportDataStartDate(), TimeZone.getTimeZone(exportWorker.getExportTimezone()).toZoneId(), exportWorker.getDateTimeFormatter())) + "\n";
			reportContent += I18nString.getLocaleString("report.data", locale) + " " + I18nString.getLocaleString("EndTime", locale) + ": " + (exportWorker.getExportDataEndDate() == null ? I18nString.getLocaleString("Unknown", locale) : DateUtilities.getDateTimeString(exportWorker.getExportDataEndDate(), TimeZone.getTimeZone(exportWorker.getExportTimezone()).toZoneId(), exportWorker.getDateTimeFormatter())) + "\n";
		}
				
		if (exportWorker.getAutoExport() != null) {
			reportContent += "AutoExport: " + exportWorker.getAutoExport().getShortname() + " (ID: " + exportWorker.getAutoExport().getAutoExportId() + ")" + "\n";
			reportContent += I18nString.getLocaleString("error.import.profile.email", locale) + ": " + (StringUtils.isBlank(exportWorker.getAutoExport().getEmailOnError()) ? I18nString.getLocaleString("default.none", locale) : exportWorker.getAutoExport().getEmailOnError());
			reportContent += I18nString.getLocaleString("autoImport.fileServer", locale) + ": " + exportWorker.getAutoExport().getFileServerWithoutCredentials();
			reportContent += I18nString.getLocaleString("autoImport.filePath", locale) + ": " + exportWorker.getAutoExport().getFilePath();
			reportContent += I18nString.getLocaleString("autoExport.fileNamePattern", locale) + ": " + (StringUtils.isBlank(exportWorker.getAutoExport().getFileNameWithPatterns()) ? AutoExport.DEFAULT_EXPORT_FILENAME_PATTERN : exportWorker.getAutoExport().getFileNameWithPatterns());
		}
		
		if (exportWorker.getUsername() != null) {
			reportContent += "User: " + exportWorker.getUsername();
		}
		
		reportContent += I18nString.getLocaleString("export.type", locale) + ": " + I18nString.getLocaleString(exportWorker instanceof RecipientReactionsAndStatusExportWorker ? "statistic.reactionsAndStatus" : "statistic.reactions", locale) + "\n";
		
		String profileContent = "";

		profileContent += I18nString.getLocaleString("Charset", locale) + ": " + exportWorker.getEncoding() + "\n";

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

	private String generateLocalizedExportHtmlReport(RecipientReactionsExportWorker exportWorker) {
		Locale locale = exportWorker.getAutoExport().getLocale();
		
		String title;
		if (exportWorker.getAutoExport() != null) {
			title = "AutoExport: " + exportWorker.getAutoExport().getShortname() + " (ID: " + exportWorker.getAutoExport().getAutoExportId() + ")";
		} else {
			title = "Export: \"" + I18nString.getLocaleString(exportWorker instanceof RecipientReactionsAndStatusExportWorker ? "statistic.reactionsAndStatus" : "statistic.reactions", locale) + "\"";
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

		Company company = companyDao.getCompany(exportWorker.getAutoExport().getCompanyId());
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Company", locale), (company == null ? "Unknown" : company.getShortname() + " (ID: " + company.getId() + ")")));
		
		if (exportWorker.getDateTimeFormat() != null) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("report.data", locale) + " " + I18nString.getLocaleString("StartTime", locale), (exportWorker.getStartTime() == null ? I18nString.getLocaleString("Unknown", locale) : exportWorker.getDateTimeFormat().format(exportWorker.getStartTime()))));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("report.data", locale) + " " + I18nString.getLocaleString("EndTime", locale), (exportWorker.getEndTime() == null ? I18nString.getLocaleString("Unknown", locale) : exportWorker.getDateTimeFormat().format(exportWorker.getEndTime()))));
		} else {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("report.data", locale) + " " + I18nString.getLocaleString("StartTime", locale), (exportWorker.getStartTime() == null ? I18nString.getLocaleString("Unknown", locale) : DateUtilities.getDateTimeString(exportWorker.getStartTime(), TimeZone.getTimeZone(exportWorker.getExportTimezone()).toZoneId(), exportWorker.getDateTimeFormatter()))));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("report.data", locale) + " " + I18nString.getLocaleString("EndTime", locale), (exportWorker.getEndTime() == null ? I18nString.getLocaleString("Unknown", locale) : DateUtilities.getDateTimeString(exportWorker.getEndTime(), TimeZone.getTimeZone(exportWorker.getExportTimezone()).toZoneId(), exportWorker.getDateTimeFormatter()))));
		}
		
		if (exportWorker.getAutoExport() != null) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("AutoExport", exportWorker.getAutoExport().getShortname() + " (ID: " + exportWorker.getAutoExport().getAutoExportId() + ")"));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("error.import.profile.email", locale), (StringUtils.isBlank(exportWorker.getAutoExport().getEmailOnError()) ? I18nString.getLocaleString("default.none", locale) : exportWorker.getAutoExport().getEmailOnError())));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("autoImport.fileServer", locale), exportWorker.getAutoExport().getFileServerWithoutCredentials()));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("autoImport.filePath", locale), exportWorker.getAutoExport().getFilePath()));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("autoExport.fileNamePattern", locale), (StringUtils.isBlank(exportWorker.getAutoExport().getFileNameWithPatterns()) ? AutoExport.DEFAULT_EXPORT_FILENAME_PATTERN : exportWorker.getAutoExport().getFileNameWithPatterns())));
		}
		
		if (exportWorker.getUsername() != null) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine("User", exportWorker.getUsername()));
		}
		
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("export.type", locale), I18nString.getLocaleString(exportWorker instanceof RecipientReactionsAndStatusExportWorker ? "statistic.reactionsAndStatus" : "statistic.reactions", locale)));
				
		htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("Charset", locale), exportWorker.getEncoding()));

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
		
		if (exportWorker.getDateTimeFormat() != null) {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("StartTime", locale), (exportWorker.getStartTime() == null ? I18nString.getLocaleString("Unknown", locale) : exportWorker.getDateTimeFormat().format(exportWorker.getStartTime()))));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("EndTime", locale), (exportWorker.getEndTime() == null ? I18nString.getLocaleString("Unknown", locale) : exportWorker.getDateTimeFormat().format(exportWorker.getEndTime()))));
		} else {
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("StartTime", locale), (exportWorker.getStartTime() == null ? I18nString.getLocaleString("Unknown", locale) : DateUtilities.getDateTimeString(exportWorker.getStartTime(), TimeZone.getTimeZone(exportWorker.getExportTimezone()).toZoneId(), exportWorker.getDateTimeFormatter()))));
			htmlContent.append(HtmlReporterHelper.getOutputTableInfoContentLine(I18nString.getLocaleString("EndTime", locale), (exportWorker.getEndTime() == null ? I18nString.getLocaleString("Unknown", locale) : DateUtilities.getDateTimeString(exportWorker.getEndTime(), TimeZone.getTimeZone(exportWorker.getExportTimezone()).toZoneId(), exportWorker.getDateTimeFormatter()))));
		}
		
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

	public void sendExportErrorMail(RecipientReactionsExportWorker exportWorker) throws Exception {
		Locale locale = exportWorker.getAutoExport().getLocale();
		int companyID = exportWorker.getAutoExport().getCompanyId();
		
		Set<String> emailRecipients = new HashSet<>();

		String additionalContent = "";
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
				emailRecipients.add(AgnUtils.normalizeEmail(email));
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
			Company company = companyDao.getCompany(exportWorker.getAutoExport().getCompanyId());
			
			String subject = "Export-ERROR: " + I18nString.getLocaleString("ResultMsg", locale) + ": " + " \"" + I18nString.getLocaleString(exportWorker instanceof RecipientReactionsAndStatusExportWorker ? "statistic.reactionsAndStatus" : "statistic.reactions", locale) + "\" (" + I18nString.getLocaleString("Company", locale) + ": " + company.getShortname() + ")";
			String bodyHtml = generateLocalizedExportHtmlReport(exportWorker) + "\n" + additionalContent;
			String bodyText = "Export-ERROR:\n" + generateLocalizedExportTextReport(exportWorker) + "\n" + additionalContent;
						
			javaMailService.sendEmail(company.getId(), StringUtils.join(emailRecipients, ", "), subject, bodyText, bodyHtml);
		}
	}

	public void createAndSaveExportReport(RecipientReactionsExportWorker exportWorker, int adminID, boolean isError) throws Exception {
		String fileToShow;
		if (exportWorker.getRemoteFile() != null && StringUtils.isNotBlank(exportWorker.getRemoteFile().getRemoteFilePath())) {
			// Remote file on ftp or sftp server
			fileToShow = exportWorker.getRemoteFile().getRemoteFilePath();
		} else {
			//Local exported File
			fileToShow = exportWorker.getExportFile();
		}
		recipientsReportService.createAndSaveExportReport(exportWorker.getAutoExport().getCompanyId(), adminID, fileToShow, exportWorker.getEndTime(), generateLocalizedExportHtmlReport(exportWorker), isError);
	}
}
