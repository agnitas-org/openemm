/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

public class HtmlReporterHelper {
	public static String WHITE_COLORCODE = "FFFFFF";
	public static String RED_COLORCODE = "DF3939";
	public static String GREEN_COLORCODE = "92D050";
	public static String YELLOW_COLORCODE = "FFFF00";
	public static String BORDER_COLORCODE = "64C3FF";
	
	public static String getHtmlPrefixWithCssStyles(String title) {
		return new StringBuilder("<!DOCTYPE html>\n")
			.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">\n")
			.append("<head>\n")
			.append("	<meta charset=\"utf-8\">\n")
			.append("	<meta name=\"viewport\" content=\"width=device-width\">\n")
			.append("	<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n")
			.append("	<meta name=\"x-apple-disable-message-reformatting\">\n")
			.append("	<title>").append(StringEscapeUtils.escapeHtml4(title)).append("</title>\n")
			.append("	<!-- Desktop Outlook chokes on web font references and defaults to Times New Roman, so we force a safe fallback font. -->\n")
			.append("	<!--[if mso]>\n")
			.append("		<style>\n")
			.append("			* {\n")
			.append("				font-family: Arial, sans-serif !important;\n")
			.append("			}\n")
			.append("		</style>\n")
			.append("	<![endif]-->\n")
			.append("	<!-- CSS Reset : BEGIN -->\n")
			.append("	<style>\n")
			.append("		html, body {\n")
			.append("			margin: 0 auto !important;\n")
			.append("			padding: 0 !important;\n")
			.append("			height: 100% !important;\n")
			.append("			width: 100% !important;\n")
			.append("		}\n")
			.append("		* {\n")
			.append("			-ms-text-size-adjust: 100%;\n")
			.append("			-webkit-text-size-adjust: 100%;\n")
			.append("		}\n")
			.append("		div[style*=\"margin: 16px 0\"] {\n")
			.append("			margin: 0 !important;\n")
			.append("		}\n")
			.append("		table, td {\n")
			.append("			mso-table-lspace: 0pt !important;\n")
			.append("			mso-table-rspace: 0pt !important;\n")
			.append("		}\n")
			.append("		table {\n")
			.append("			border-spacing: 0 !important;\n")
			.append("			border-collapse: collapse !important;\n")
			.append("			table-layout: fixed !important;\n")
			.append("			margin: 0 auto !important;\n")
			.append("		}\n")
			.append("		table table table {\n")
			.append("			table-layout: auto;\n")
			.append("		}\n")
			.append("		img {\n")
			.append("			-ms-interpolation-mode:bicubic;\n")
			.append("		}\n")
			.append("		*[x-apple-data-detectors], .x-gmail-data-detectors, .x-gmail-data-detectors *, .aBn {\n")
			.append("			border-bottom: 0 !important;\n")
			.append("			cursor: default !important;\n")
			.append("			color: inherit !important;\n")
			.append("			text-decoration: none !important;\n")
			.append("			font-size: inherit !important;\n")
			.append("			font-family: inherit !important;\n")
			.append("			font-weight: inherit !important;\n")
			.append("			line-height: inherit !important;\n")
			.append("		}\n")
			.append("		.a6S {\n")
			.append("			display: none !important;\n")
			.append("			opacity: 0.01 !important;\n")
			.append("		}\n")
			.append("		img.g-img + div {\n")
			.append("			display: none !important;\n")
			.append("		}\n")
			.append("\n")
			.append("		@media only screen and (min-device-width: 375px) and (max-device-width: 413px) { /* iPhone 6 and 6+ */\n")
			.append("			.email-container {\n")
			.append("			min-width: 375px !important;\n")
			.append("			}\n")
			.append("		}\n")
			.append("\n")
			.append("		@media screen and (max-width: 600px) {\n")
			.append("			.email-container {\n")
			.append("			width: 100% !important;\n")
			.append("			margin: auto !important;\n")
			.append("			}\n")
			.append("\n")
			.append("			/* What it does: Forces elements to resize to the full width of their container. Useful for resizing images beyond their max-width. */\n")
			.append("			.fluid {\n")
			.append("				max-width: 100% !important;\n")
			.append("				height: auto !important;\n")
			.append("				margin-left: auto !important;\n")
			.append("				margin-right: auto !important;\n")
			.append("			}\n")
			.append("\n")
			.append("			/* What it does: Forces table cells into full-width rows. */\n")
			.append("			.stack-column,\n")
			.append("			.stack-column-center {\n")
			.append("				display: block !important;\n")
			.append("				width: 100% !important;\n")
			.append("				max-width: 100% !important;\n")
			.append("				direction: ltr !important;\n")
			.append("			}\n")
			.append("			/* And center justify these ones. */\n")
			.append("			.stack-column-center {\n")
			.append("				text-align: center !important;\n")
			.append("			}\n")
			.append("\n")
			.append("			/* What it does: Generic utility class for centering. Useful for images, buttons, and nested tables. */\n")
			.append("			.center-on-narrow {\n")
			.append("				text-align: center !important;\n")
			.append("				display: block !important;\n")
			.append("				margin-left: auto !important;\n")
			.append("				margin-right: auto !important;\n")
			.append("				float: none !important;\n")
			.append("			}\n")
			.append("			table.center-on-narrow {\n")
			.append("				display: inline-block !important;\n")
			.append("			}\n")
			.append("\n")
			.append("			/* What it does: Adjust typography on small screens to improve readability */\n")
			.append("			.email-container p {\n")
			.append("				font-size: 17px !important;\n")
			.append("				line-height: 22px !important;\n")
			.append("			}\n")
			.append("		}\n")
			.append("\n")
			.append("	</style>\n")
			.append("	<!-- Progressive Enhancements : END -->\n")
			.append("\n")
			.append("	<!-- What it does: Makes background images in 72ppi Outlook render at correct size. -->\n")
			.append("	<!--[if gte mso 9]>\n")
			.append("	<xml>\n")
			.append("		<o:OfficeDocumentSettings>\n")
			.append("			<o:AllowPNG/>\n")
			.append("			<o:PixelsPerInch>96</o:PixelsPerInch>\n")
			.append("		</o:OfficeDocumentSettings>\n")
			.append("	</xml>\n")
			.append("	<![endif]-->\n")
			.append("\n")
			.append("</head>\n")
			.append("<body width=\"100%\" bgcolor=\"#f2f2f2\" style=\"margin: 0; mso-line-height-rule: exactly;\">\n")
			.append("	<center style=\"width: 100%; background: #f2f2f2; text-align: left;\">\n")
			.toString();
	}
	
	public static String getHtmlSuffix() {
		return "	</center>\n</body>\n</html>\n";
	}
	
	public static String getHeader(String title, String versionText) {
		// Do not html-escape "versionText"-parameter, because it may contain html tags
		return new StringBuilder()
			.append("<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\" width=\"600\" style=\"margin: auto;\" class=\"email-container\">\n")
			.append("	<tr>\n")
			.append("		<td bgcolor=\"#ffffff\" dir=\"rtl\" align=\"center\" valign=\"top\" width=\"100%\" style=\"padding: 10px;\">\n")
			.append("			<table role=\"presentation\" align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n")
			.append("				<tr>\n")
			.append("					<td width=\"33.33%\" class=\"stack-column\">\n")
			.append("						<table role=\"presentation\" align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n")
			.append("							<tr>\n")
			.append("								<td dir=\"ltr\" valign=\"top\" style=\"padding: 0;\">\n")
			.append("									<div align=\"right\" style=\"float:right;\">\n")
			.append("										<img src=\"" + ConfigService.getInstance().getValue(ConfigValue.SystemUrl) + "/layout/0/logo.svg\" align=\"left\" style=\"padding-top:5px; padding-right:5px;\">\n")
			.append("									<div style=\"float: right; font-family: Proxima Nova Helvetica Neue,Arial,sans-serif; font-size: 28px; white-space: nowrap; color: #0071b9; text-align:left;\">EMM<div style=\"font-size: 13px; white-space: nowrap; color: #8d8d8f;\">").append(versionText).append("</div></div>\n")
			.append("									</div>\n")
			.append("								</td>\n")
			.append("							</tr>\n")
			.append("						</table>\n")
			.append("					</td>\n")
			.append("					<td width=\"66.66%\" class=\"stack-column\">\n")
			.append("						<table role=\"presentation\" align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n")
			.append("							<tr>\n")
			.append("								<td dir=\"ltr\" valign=\"top\" style=\"font-family: sans-serif; font-size: 15px; line-height: 20px; color: #555555; padding: 10px; text-align: left;\" class=\"center-on-narrow\">\n")
			.append("									<h1 style=\"margin: 0 0 10px 0; font-family: sans-serif; font-size: 18px; line-height: 21px; color: #555555; font-weight: bold;\">").append(StringEscapeUtils.escapeHtml4(title)).append("</h1>\n")
			.append("								</td>\n")
			.append("							</tr>\n")
			.append("						</table>\n")
			.append("					</td>\n")
			.append("				</tr>\n")
			.append("			</table>\n")
			.append("		</td>\n")
			.append("	</tr>\n")
			.append("	<tr>\n")
			.append("		<td aria-hidden=\"true\" height=\"20\" style=\"font-size: 0; line-height: 0;\">&nbsp;</td>\n")
			.append("	</tr>\n")
			.append("</table>\n")
			.toString();
	}
	
	public static String getFooter(String hostname, String emmVersionString) {
		return new StringBuilder()
			.append("<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\" width=\"600\" style=\"margin: auto;\" class=\"email-container\">\n")
			.append("	<tr>\n")
			.append("		<td>\n")
			.append("			<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\">\n")
			.append("				<tr>\n")
			.append("					<td style=\"padding: 20px; font-family: Arial, sans-serif; font-size: 9px; color: #999999;\">").append(StringEscapeUtils.escapeHtml4(hostname)).append(" ").append(StringEscapeUtils.escapeHtml4(emmVersionString)).append("</td>\n")
			.append("				</tr>\n")
			.append("			</table>\n")
			.append("		</td>\n")
			.append("	</tr>\n")
			.append("</table>\n")
			.toString();
	}
	
	public static String getOutputTableStart() {
		return "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\" width=\"600\" style=\"margin: auto;\" class=\"email-container\">\n";
	}
	
	public static String getOutputTableHeader(String headerText) {
		return new StringBuilder()
			.append("	<tr>\n")
			.append("		<td bgcolor=\"#ffffff\" style=\"padding: 20px; text-align:left;\">\n")
			.append("			<h2 style=\"margin: 0; font-family: sans-serif; font-size: 16px; line-height: 18px; color: #333333;\">").append(StringEscapeUtils.escapeHtml4(headerText)).append(":</h2>\n")
			.append("		</td>\n")
			.append("	</tr>\n")
			.toString();
	}
	
	public static String getOutputTableSubHeader(String headerText, boolean continueWithBorder) {
		if (continueWithBorder) {
			return new StringBuilder()
				.append("	</table>\n")
				.append("	<p style=\"font-family: Arial, sans-serif; font-size:12px;\">").append(StringEscapeUtils.escapeHtml4(headerText)).append(":</p>\n")
				.append("	<table width=\"100%\" border=\"1\" bordercolor=\"#64C3FF\" cellspacing=\"0\" cellpadding=\"3\" style=\"border-collapse: collapse; font-family: Arial, sans-serif; font-size:12px;\">\n")
				.toString();
		} else {
			return new StringBuilder()
				.append("	</table>\n")
				.append("	<p style=\"font-family: Arial, sans-serif; font-size:12px;\">").append(StringEscapeUtils.escapeHtml4(headerText)).append(":</p>\n")
				.append("	<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"3\" style=\"font-family: Arial, sans-serif; font-size:12px;\">\n")
				.toString();
		}
	}
	
	public static String getOutputTableContentStart(boolean withBorder) {
		if (withBorder) {
			return new StringBuilder()
				.append("	<tr>\n")
				.append("		<td bgcolor=\"#ffffff\" style=\"padding: 0 20px 20px; text-align:left;\">\n")
				.append("			<table width=\"100%\" border=\"1\" bordercolor=\"#64C3FF\" cellspacing=\"0\" cellpadding=\"3\" style=\"border-collapse: collapse; font-family: Arial, sans-serif; font-size:12px;\">\n")
				.toString();
		} else {
			return new StringBuilder()
				.append("	<tr>\n")
				.append("		<td bgcolor=\"#ffffff\" style=\"padding: 0 20px 20px; text-align:left;\">\n")
				.append("			<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"3\" style=\"font-family: Arial, sans-serif; font-size:12px;\">\n")
				.toString();
		}
	}
	
	public static String getOutputTableErrorContentLine(String errorText) {
		return new StringBuilder()
			.append("	<tr>\n")
			.append("		<td bgcolor=\"#").append(RED_COLORCODE).append("\" style=\"color:#").append(WHITE_COLORCODE).append(";\">").append(StringEscapeUtils.escapeHtml4(errorText)).append("</td>\n")
			.append("	</tr>\n")
			.toString();
	}
	
	public static String getOutputTableWarningContentLine(String warningText) {
		return new StringBuilder()
			.append("	<tr>\n")
			.append("		<td aria-hidden=\"true\" height=\"20\" style=\"font-size: 0; line-height: 0;\">&nbsp;</td>\n")
			.append("	</tr>\n")
			.append("	<tr>\n")
			.append("		<td align=\"right\" bgcolor=\"#").append(YELLOW_COLORCODE).append("\">").append(StringEscapeUtils.escapeHtml4(warningText)).append("</td>\n")
			.append("	</tr>\n")
			.toString();
	}
	
	public static String getOutputTableSummaryContentLine(String contentItemName, String contentItemValue, boolean isError) {
		String textColorCode = null;
		String backgroundColorCode = isError ? RED_COLORCODE : GREEN_COLORCODE;
		if (RED_COLORCODE.equalsIgnoreCase(backgroundColorCode) || GREEN_COLORCODE.equalsIgnoreCase(backgroundColorCode)) {
			textColorCode = WHITE_COLORCODE;
		}
		return new StringBuilder()
			.append("	<tr>\n")
			.append("		<td>").append(StringEscapeUtils.escapeHtml4(contentItemName)).append("</td>\n")
			.append("		<td width=\"25%\" align=\"right\" bgcolor=\"#").append(backgroundColorCode).append("\"").append(textColorCode != null ? (" style=\"color:#" + textColorCode + ";\"") : "").append(">").append(StringEscapeUtils.escapeHtml4(contentItemValue)).append("</td>\n")
			.append("	</tr>\n")
			.toString();
	}
	
	public static String getOutputTableSummaryContentLine(String contentItemName, String contentItemValue) {
		return new StringBuilder()
			.append("	<tr>\n")
			.append("		<td>").append(StringEscapeUtils.escapeHtml4(contentItemName)).append("</td>\n")
			.append("		<td width=\"25%\" align=\"right\">").append(StringEscapeUtils.escapeHtml4(contentItemValue)).append("</td>\n")
			.append("	</tr>\n")
			.toString();
	}
	
	public static String getOutputTableResultContentLine(String contentItemName, String contentItemValue) {
		return new StringBuilder()
			.append("	<tr>\n")
			.append("		<td>").append(StringEscapeUtils.escapeHtml4(contentItemName)).append("</td>\n")
			.append("		<td width=\"40%\" align=\"right\">").append(StringEscapeUtils.escapeHtml4(contentItemValue)).append("</td>\n")
			.append("	</tr>\n")
			.toString();
	}
	
	public static String getOutputTableInfoContentLine(String contentItemName, String contentItemValue) {
		return new StringBuilder()
			.append("	<tr>\n")
			.append("		<td width=\"140\" class=\"stack-column\"><b>").append(StringUtils.isNotEmpty(contentItemName) ? StringEscapeUtils.escapeHtml4(contentItemName) + ":" : "").append("</b></td>\n")
			.append("		<td class=\"stack-column\">").append(StringEscapeUtils.escapeHtml4(contentItemValue)).append("</td>\n")
			.append("	</tr>\n")
			.toString();
	}
	
	public static String getOutputTableContentEnd() {
		return new StringBuilder()
			.append("			</table>\n")
			.append("		</td>\n")
			.append("	</tr>\n")
			.toString();
	}
	
	public static String getOutputTableEnd() {
		return "	<tr>\n		<td aria-hidden=\"true\" height=\"20\" style=\"font-size: 0; line-height: 0;\">&nbsp;</td>\n	</tr>\n</table>\n";
	}
}
