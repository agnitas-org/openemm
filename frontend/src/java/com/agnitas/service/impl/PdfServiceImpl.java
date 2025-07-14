/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import com.agnitas.beans.Admin;
import com.agnitas.dao.AdminGroupDao;
import com.agnitas.emm.core.preview.dto.PreviewResult;
import com.agnitas.emm.core.preview.service.MailingWebPreviewService;
import com.agnitas.emm.core.preview.service.PreviewSettings;
import com.agnitas.service.PdfService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.ServletContext;
import com.agnitas.beans.AdminEntry;
import com.agnitas.beans.AdminGroup;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.SafeString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.ServletContextAware;

@Service("pdfService")
public class PdfServiceImpl implements PdfService, ServletContextAware {

    private static final Logger logger = LogManager.getLogger(PdfServiceImpl.class);

	private static final String PDF_SERVICE_URL = "http://localhost:3000/pdf";
	private static final int PDF_TIMEOUT = 60_000; // 1 minute
	private static final String PREVIEW_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "Preview";
    private static final String USER_STYLESHEET_CONTENT = "body {\n" +
            "\tdisplay: inline-block !important;\n" +
            "\twidth: 100% !important;\n" +
            "}\n";

    private final AdminGroupDao adminGroupDao;
    private ServletContext servletContext;
	private MailingWebPreviewService mailingWebPreviewService;

    public PdfServiceImpl(AdminGroupDao adminGroupDao, final MailingWebPreviewService mailingWebPreviewService) {
        this.adminGroupDao = Objects.requireNonNull(adminGroupDao, "AdminGroupDao");
		this.mailingWebPreviewService = Objects.requireNonNull(mailingWebPreviewService, "MailingWebPreviewService");
    }

    @Override
    public byte[] writeUsersToPdfAndGetByteArray(List<AdminEntry> users) throws DocumentException, IOException {
        Document document = new Document();
        byte[] pdfFileBytes;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            writeUsersPDF(users, document);
            document.close();
            pdfFileBytes = outputStream.toByteArray();
        }

        return pdfFileBytes;
    }

    private void writeUsersPDF(List<AdminEntry> users, Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        writeUserTableHeaders(table);
        writeUserTableRows(users, table);
        document.add(table);
    }

    private void writeUserTableHeaders(PdfPTable table) {
        table.addCell("Username");
        table.addCell("Firstname");
        table.addCell("Lastname");
        table.addCell("Email");
        table.addCell("UserGroup");
    }

    private void writeUserTableRows(List<AdminEntry> users, PdfPTable table) {
        for (AdminEntry user : users) {
            table.addCell(user.getUsername());
            table.addCell(user.getFirstname());
            table.addCell(user.getFullname());
            table.addCell(user.getEmail());
            List<AdminGroup> adminGroups = adminGroupDao.getAdminGroupsByAdminID(user.getCompanyID(), user.getId());
            StringBuilder adminGroupsList = new StringBuilder();
        	for (AdminGroup adminGroup : adminGroups) {
        		if (!adminGroupsList.isEmpty()) {
        			adminGroupsList.append(", ");
        		}
        		adminGroupsList.append(adminGroup.getShortname());
        	}
            table.addCell(adminGroupsList.toString());
        }
    }

	@Override
    public File generatePDF(Admin admin, String url, boolean landscape, String title, String footerMsgKey, String customCss, String windowStatusForWaiting) throws IOException {
		File pdf = generatePDFWithPuppeteer(url, landscape, customCss, windowStatusForWaiting);
		try {
			return addAdditionalElements(admin.getLocale(), TimeZone.getTimeZone(admin.getAdminTimezone()), pdf, landscape, title, footerMsgKey);
		} catch (DocumentException ex) {
			throw new IOException("Error while extending .pdf", ex);
		} finally {
			Files.delete(pdf.toPath());
		}
    }

	public File generatePDF(final Admin admin, final PreviewSettings previewSettings, final boolean landscape, final String title, final String footerMsgKey) throws Exception {
		return generatePDF(admin, previewSettings, landscape, title, footerMsgKey, USER_STYLESHEET_CONTENT);
	}

	@Override
	public File generatePDF(final Admin admin, final PreviewSettings previewSettings, final boolean landscape, final String title, final String footerMsgKey, final String customCss) throws Exception {
		final File pdf = generatePDFWithPuppeteer(previewSettings, landscape, customCss, admin);
		try {
			return addAdditionalElements(admin.getLocale(), TimeZone.getTimeZone(admin.getAdminTimezone()), pdf, landscape, title, footerMsgKey);
		} catch (DocumentException ex) {
			throw new IOException("Error while extending .pdf", ex);
		} finally {
			Files.delete(pdf.toPath());
		}
	}

    @Override
    public File generatePDF(Admin admin, String url, boolean landscape, String title, String footerMsgKey) throws IOException {
		return generatePDF(admin, url, landscape, title, footerMsgKey, USER_STYLESHEET_CONTENT, "");
	}

	@Override
    public File generatePDF(Admin admin, String url, boolean landscape, String title, String footerMsgKey, String windowStatusForWaiting) throws IOException {
        return generatePDF(admin, url, landscape, title, footerMsgKey, USER_STYLESHEET_CONTENT, windowStatusForWaiting);
    }



	/**
	 * Generates a .pdf file of the given web page
	 *
	 * @param url                    URL of the web page that should be rendered
	 * @param landscape              if resulting pdf should have landscape orientation (false - portrait)
	 * @param customCss              custom CSS code to be applied to a web page
	 * @param windowWaitStatus 		 js window.waitStatus status to be waited before generation
	 * @return .pdf file of the web page
	 */
	@Deprecated
    private File generatePDFWithPuppeteer(String url, boolean landscape, String customCss, String windowWaitStatus) throws IOException {
        File customCssFile = createFileWithCustomCss(customCss);
        try {
			return createPdf(url, customCssFile.getAbsolutePath(), landscape, windowWaitStatus);
        } finally {
            Files.deleteIfExists(customCssFile.toPath());
        }
    }

	private File generatePDFWithPuppeteer(final PreviewSettings previewSettings, final boolean landscape, final String customCss, final Admin admin) throws Exception {
		// Get preview content
		final PreviewResult previewResult = this.mailingWebPreviewService.getPreview(previewSettings, admin.getCompanyID(), admin);

		// Write content to temporary file
		final File temporaryPreviewFile = File.createTempFile("preview-", ".html");
		try(final FileWriter out = new FileWriter(temporaryPreviewFile)) {
			final String previewContent = previewResult.getPreviewContent().orElse("");

			out.write(previewContent);
		}

		// Create file for custom CSS
		final File customCssFile = createFileWithCustomCss(customCss);

		// Create PDF
		try {
			try {
				return createPdf(temporaryPreviewFile, customCssFile.getAbsolutePath(), landscape);
			} finally {
				Files.deleteIfExists(customCssFile.toPath());
			}
		} finally {
			if(temporaryPreviewFile.exists()) {
				temporaryPreviewFile.delete();
			}
		}
	}

	@Deprecated
	private File createPdf(String url, String customCssPath, boolean landscape, String windowWaitStatus) throws IOException {
		File pdf = File.createTempFile("preview_", ".pdf", AgnUtils.createDirectory(PREVIEW_FILE_DIRECTORY));
		try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			JSONObject requestBody = new JSONObject();
			requestBody.put("url", url);
			requestBody.put("path", pdf.getAbsolutePath());
			requestBody.put("landscape", landscape);
			requestBody.put("customCssPath", customCssPath);
			requestBody.put("windowWaitStatus", windowWaitStatus);
			requestBody.put("timeout", PDF_TIMEOUT);

			HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
			ResponseEntity<String> response = restTemplate.postForEntity(PDF_SERVICE_URL, request, String.class);

			if (response.getStatusCode().is2xxSuccessful() && pdf.exists() && pdf.length() > 0) {
				return pdf;
			} else {
				throw new IOException("Pdf generation failed. Response: " + response.getBody());
			}
		} catch (Exception e) {
			throw new IOException("Error while creating pdf via puppeteer for URL '" + url + "' - " + e.getMessage());
		}
	}

	private File createPdf(final File previewFile, final String customCssPath, final boolean landscape) throws IOException {
		final File pdf = File.createTempFile("preview_", ".pdf", AgnUtils.createDirectory(PREVIEW_FILE_DIRECTORY));
		try {
			final RestTemplate restTemplate = new RestTemplate();
			final HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			final JSONObject requestBody = new JSONObject();
			requestBody.put("url", previewFile.toURI().toString());
			requestBody.put("path", pdf.getAbsolutePath());
			requestBody.put("landscape", landscape);
			requestBody.put("customCssPath", customCssPath);
			requestBody.put("timeout", PDF_TIMEOUT);

			final HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
			final ResponseEntity<String> response = restTemplate.postForEntity(PDF_SERVICE_URL, request, String.class);

			if (response.getStatusCode().is2xxSuccessful() && pdf.exists() && pdf.length() > 0) {
				return pdf;
			} else {
				throw new IOException("Pdf generation failed. Response: " + response.getBody());
			}
		} catch (Exception e) {
			throw new IOException("Error while creating pdf with a new puppeteer method - " + e.getMessage(), e);
		}
	}

    private File createFileWithCustomCss(String customCss) throws IOException {
        File customCssFile = File.createTempFile("preview_", ".css", AgnUtils.createDirectory(PREVIEW_FILE_DIRECTORY));
        try (FileWriter stylesheetFileWriter = new FileWriter(customCssFile)) {
            // we use an external styles to change a page breaking policy
            stylesheetFileWriter.write(StringUtils.defaultIfEmpty(customCss, USER_STYLESHEET_CONTENT));
        }
        return customCssFile;
    }

	/**
	 * Extends given .pdf file with Agnitas logo at top right and provided title and footer
	 *
	 * @param pdfInitialFile pdf to be extended
	 * @param landscape if resulting pdf should have landscape orientation (false - portrait)
	 * @param title i.e. entity name
	 * @param footerMsgKey key of the message to be displayed at footer
	 * @return new pdf file with added logo and footer
	 * @throws IOException
	 * @throws com.lowagie.text.DocumentException
	 */
    private File addAdditionalElements(final Locale locale, final TimeZone timeZone, File pdfInitialFile, boolean landscape, String title, String footerMsgKey) throws IOException, DocumentException {
        File finalFile = File.createTempFile("preview_final_", ".pdf", AgnUtils.createDirectory(PREVIEW_FILE_DIRECTORY));
        PdfReader pdfReader = new PdfReader(pdfInitialFile.getAbsolutePath());

        try {
	        try(FileOutputStream fos = new FileOutputStream(finalFile)) {
		        PdfStamper stamper = new PdfStamper(pdfReader, fos);

		        try {
			        //PdfContentByte overContent = stamper.getOverContent(1);
			        int borderGap = 20;

			        // draw agnitas logo at the top right corner
			        String logoPath = servletContext.getRealPath("/assets/core/images/facelift/report_logo.png");

			        if (logoPath == null) {
			        	logger.error("Missing logo file report_logo.png for PdfService");
			        }
			        
					try (InputStream logoStream = new FileInputStream(logoPath)) {
				        byte[] logoData = IOUtils.toByteArray(logoStream);
				        Image image = Image.getInstance(logoData);
				        image.scalePercent(31f);
				        float topLineStartX;
				        float topLineStartY;
				        float topLineEndX;
				        float topLineEndY;
				        float bottomLineStartX;
				        float bottomLineStartY;
				        float bottomLineEndX;
				        float bottomLineEndY;
				        if (!landscape) {
				            image.setAbsolutePosition(PageSize.A4.width() - image.scaledWidth() - borderGap, PageSize.A4.height() - image.scaledHeight() - borderGap);
				            topLineStartX = borderGap;
				            topLineStartY = PageSize.A4.height() - borderGap - 35;
				            topLineEndX = PageSize.A4.width() - borderGap;
				            topLineEndY = PageSize.A4.height() - borderGap - 35;
				            bottomLineStartX = borderGap;
				            bottomLineStartY = borderGap + 10;
				            bottomLineEndX = PageSize.A4.width() - borderGap;
				            bottomLineEndY = borderGap + 10;
				        } else {
				            image.setAbsolutePosition(PageSize.A4.height() - image.scaledWidth() - borderGap, PageSize.A4.width() - image.scaledHeight() - borderGap);
				            topLineStartX = borderGap;
				            topLineStartY = PageSize.A4.width() - borderGap - 35;
				            topLineEndX = PageSize.A4.height() - borderGap;
				            topLineEndY = PageSize.A4.width() - borderGap - 35;
				            bottomLineStartX = borderGap;
				            bottomLineStartY = borderGap + 10;
				            bottomLineEndX = PageSize.A4.height() - borderGap;
				            bottomLineEndY = borderGap + 10;
				        }

				        // draw bottom page text: workflow name + date
						String workflowTitle = String.format("%s \"%s\"", SafeString.getLocaleString(footerMsgKey, locale), title);
				        DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, locale);
				        Date currentDate = GregorianCalendar.getInstance(timeZone).getTime();
				        String dateStr = dateFormat.format(currentDate);
						String bottomText = workflowTitle + ", " + dateStr;
				        BaseFont font = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

				        for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
				            PdfContentByte overContent = stamper.getOverContent(i);
				            //add logo
				            overContent.addImage(image);

				            overContent.beginText();
				            overContent.setFontAndSize(font, 10); // set font and size
							overContent.setTextMatrix(borderGap + 5, topLineStartY + 10); // set x,y position (0,0 is at the bottom left)
							overContent.showText(workflowTitle); // set text
				            overContent.endText();

				            //add footer text
				            overContent.beginText();
				            overContent.setFontAndSize(font, 10); // set font and size
				            overContent.setTextMatrix(borderGap + 5, borderGap - 5); // set x,y position (0,0 is at the bottom left)
				            overContent.showText(bottomText); // set text
				            overContent.setTextMatrix(bottomLineEndX - 20, bottomLineEndY - 15);
				            overContent.showText("" + i + "/" + pdfReader.getNumberOfPages()); // set page number
				            overContent.endText();

				            // add light grey lines at the top and at the bottom
				            overContent.setRGBColorStroke(0xcc, 0xcc, 0xcc);
				            overContent.setLineWidth(0.5f);
				            overContent.moveTo(topLineStartX, topLineStartY);
				            overContent.lineTo(topLineEndX, topLineEndY);
				            overContent.moveTo(bottomLineStartX, bottomLineStartY);
				            overContent.lineTo(bottomLineEndX, bottomLineEndY);
				            overContent.stroke();
				        }

				        return finalFile;
			        }
		        } finally {
		        	stamper.close();
		        }
	        }
        } finally {
        	pdfReader.close();
        }
    }

    @Override
   	public void setServletContext(final ServletContext servletContext) {
   		this.servletContext = Objects.requireNonNull(servletContext, "servletContext is null");
   	}
}
