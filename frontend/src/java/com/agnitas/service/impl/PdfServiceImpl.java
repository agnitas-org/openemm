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
import java.net.ConnectException;
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
import com.agnitas.beans.AdminEntry;
import com.agnitas.beans.AdminGroup;
import com.agnitas.dao.AdminGroupDao;
import com.agnitas.emm.core.preview.dto.PreviewResult;
import com.agnitas.emm.core.preview.service.MailingWebPreviewService;
import com.agnitas.emm.core.preview.service.PreviewSettings;
import com.agnitas.emm.puppeteer.service.PuppeteerService;
import com.agnitas.service.PdfService;
import com.agnitas.service.exceptions.PdfCreationException;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.SafeString;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.ServletContextAware;

@Service("pdfService")
public class PdfServiceImpl implements PdfService, ServletContextAware {

    private static final Logger logger = LogManager.getLogger(PdfServiceImpl.class);

	private static final int MAX_RETRIES_COUNT = 3;
	private static final int PDF_TIMEOUT = 60_000; // 1 minute
	private static final String PREVIEW_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "Preview";
    private static final String USER_STYLESHEET_CONTENT = "body {\n" +
            "\tdisplay: inline-block !important;\n" +
            "\twidth: 100% !important;\n" +
            "}\n";

	private final AdminGroupDao adminGroupDao;
	private final MailingWebPreviewService mailingWebPreviewService;
	private final PuppeteerService puppeteerService;
	private ServletContext servletContext;

	public PdfServiceImpl(
			AdminGroupDao adminGroupDao,
			MailingWebPreviewService mailingWebPreviewService,
			@Autowired(required = false) PuppeteerService puppeteerService
	) {
		this.adminGroupDao = Objects.requireNonNull(adminGroupDao, "AdminGroupDao");
		this.mailingWebPreviewService = Objects.requireNonNull(mailingWebPreviewService, "MailingWebPreviewService");
		this.puppeteerService = puppeteerService;
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
	public File generatePDF(Admin admin, String url, boolean landscape, String title, String footerMsgKey, String customCss, String windowStatusForWaiting) throws IOException, DocumentException {
		File pdf = generatePDFWithPuppeteer(url, landscape, customCss, windowStatusForWaiting);
		return addAdditionalElements(admin.getLocale(), TimeZone.getTimeZone(admin.getAdminTimezone()), pdf, landscape, title, footerMsgKey);
	}

	@Override
	public File generatePDF(Admin admin, PreviewSettings previewSettings, boolean landscape, String title, String footerMsgKey) throws Exception {
		File pdf = generatePDFWithPuppeteer(previewSettings, landscape, admin);
		return addAdditionalElements(admin.getLocale(), TimeZone.getTimeZone(admin.getAdminTimezone()), pdf, landscape, title, footerMsgKey);
	}

	@Override
	public File generatePDF(Admin admin, String url, boolean landscape, String title, String footerMsgKey) throws IOException, DocumentException {
		return generatePDF(admin, url, landscape, title, footerMsgKey, USER_STYLESHEET_CONTENT, "");
	}

	@Override
	public File generatePDF(Admin admin, String url, boolean landscape, String title, String footerMsgKey, String windowStatusForWaiting) throws IOException, DocumentException {
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
	private File generatePDFWithPuppeteer(String url, boolean landscape, String customCss, String windowWaitStatus) {
		File customCssFile;
		try {
			customCssFile = createFileWithCustomCss(customCss);
		} catch (IOException e) {
			throw new PdfCreationException("Error when creating custom css file", e);
		}

		try {
			return createPdf(url, customCssFile.getAbsolutePath(), landscape, windowWaitStatus);
		} finally {
			tryDeleteFile(customCssFile);
		}
	}

	private File generatePDFWithPuppeteer(PreviewSettings previewSettings, boolean landscape, Admin admin) throws Exception {
		PreviewResult previewResult = this.mailingWebPreviewService.getPreview(previewSettings, admin.getCompanyID(), admin);

		File temporaryPreviewFile = writePreviewToTempFile(previewResult);
		File customCssFile = createFileWithCustomCss(USER_STYLESHEET_CONTENT);

		try {
			return createPdf(temporaryPreviewFile.toURI().toString(), customCssFile.getAbsolutePath(), landscape, "");
		} finally {
			tryDeleteFile(customCssFile);
			tryDeleteFile(temporaryPreviewFile);
		}
	}

	private File writePreviewToTempFile(PreviewResult previewResult) throws IOException {
		File tmpFile = File.createTempFile("preview-", ".html");
		try (FileWriter out = new FileWriter(tmpFile)) {
			String previewContent = previewResult.getPreviewContent().orElse("");
			out.write(previewContent);
		}

		return tmpFile;
	}

	private File createPdf(String url, String customCssPath, boolean landscape, String windowWaitStatus) {
		return createPdf(url, customCssPath, landscape, windowWaitStatus, 0);
	}

	private File createPdf(String url, String customCssPath, boolean landscape, String windowWaitStatus, int retryCount) {
		File pdf = null;
		try {
			pdf = File.createTempFile("preview_", ".pdf", AgnUtils.createDirectory(PREVIEW_FILE_DIRECTORY));
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
			ResponseEntity<String> response = restTemplate.postForEntity(puppeteerService.getPdfUrl(), request, String.class);

			if (response.getStatusCode().is2xxSuccessful() && pdf.exists() && pdf.length() > 0) {
				return pdf;
			}

			throw new PdfCreationException("Pdf generation failed. URL: '%s' Response: %s".formatted(url, response.getBody()));
		} catch (ResourceAccessException rae) {
			if (rae.getCause() instanceof ConnectException && !puppeteerService.isServiceRunning() && retryCount < MAX_RETRIES_COUNT) {
				puppeteerService.startService();
				tryDeleteFile(pdf);
				return createPdf(url, customCssPath, landscape, windowWaitStatus, retryCount + 1);
			}

			throw new PdfCreationException("Error while creating pdf via puppeteer - '%s'".formatted(url), rae);
		} catch (Exception e) {
			throw new PdfCreationException("Error while creating pdf via puppeteer - '%s'".formatted(url), e);
		}
	}

	private boolean tryDeleteFile(File file) {
		if (file == null) {
			return false;
		}

		try {
			return Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			return false;
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
