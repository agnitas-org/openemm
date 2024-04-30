/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.SafeString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.beans.Admin;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class GenerationPDFService {

	/** The logger. */
    private static final transient Logger logger = LogManager.getLogger(GenerationPDFService.class);
    
	public static final String PREVIEW_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "Preview";

    public static final String USER_STYLESHEET_CONTENT = "body {\n" +
            "\tdisplay: inline-block !important;\n" +
            "\twidth: 100% !important;\n" +
            "}\n";

    public File generatePDF(String wkhtmltopdf, String url, String title, Admin admin, String windowStatusForWaiting, String orientation, String footerTitleMessageKey) {
    	return generatePDF(wkhtmltopdf, url, title, admin, windowStatusForWaiting, orientation, footerTitleMessageKey, USER_STYLESHEET_CONTENT);
	}
    
    public File generatePDF(String wkhtmltopdf, String url, String title, Admin admin, String windowStatusForWaiting, String orientation, String footerTitleMessageKey, String customCssStyle) {
    	if (StringUtils.isBlank(wkhtmltopdf)) {
        	logger.error("Missing path to wkhtmltopdf tool");
        } else if (!new File(wkhtmltopdf).exists()) {
        	logger.error("Missing wkhtmltopdf tool at path: '" + wkhtmltopdf + "'");
        }

		try {
			int responseStatusCode = HttpUtils.getResponseStatusCode(url);
			if (responseStatusCode != HttpURLConnection.HTTP_OK) {
				throw new Exception("Missing or wrong url for generating PDF: " + url + ", response code: " + responseStatusCode);
			}
			// render workflow into PDF
            File pdfInitialFile = File.createTempFile("preview_", ".pdf", AgnUtils.createDirectory(PREVIEW_FILE_DIRECTORY));
            String pdfName = pdfInitialFile.getAbsolutePath();

            File stylesheetFile = File.createTempFile("preview_", ".css", AgnUtils.createDirectory(PREVIEW_FILE_DIRECTORY));
            String stylesheetName = stylesheetFile.getAbsolutePath();
            try(FileWriter stylesheetFileWriter = new FileWriter(stylesheetFile)) {
	            // we use an external styles to change a page breaking policy
	            // wkhtmltopdf doesn't support an http:// URLs for user stylesheets
	            stylesheetFileWriter.write(StringUtils.defaultIfEmpty(customCssStyle, USER_STYLESHEET_CONTENT));
            }
            
            String proxyString = "None";
    		String proxyHost = System.getProperty("http.proxyHost");
    		List<String> nonProxyHosts = new ArrayList<>();
    		if (StringUtils.isNotBlank(proxyHost)) {
    			String nonProxyHostsString = System.getProperty("http.nonProxyHosts");
    			if (StringUtils.isNotBlank(nonProxyHostsString)) {
	    			for (String nonProxyHost : nonProxyHostsString.split("\\||,|;| ")) {
						nonProxyHost = nonProxyHost.trim().toLowerCase();
						nonProxyHosts.add(nonProxyHost);
					}
    			}
    			
    			proxyString = proxyHost;
    			if (!proxyString.contains("://")) {
    				proxyString = "http://" + proxyString;
    			}
    			
    			String proxyPort = System.getProperty("http.proxyPort");
    			if (StringUtils.isNotBlank(proxyPort)) {
    				proxyString = proxyString + ":" + proxyPort;
    			} else {
    				proxyString = proxyString + ":" + 8080;
    			}
    		}

			List<String> command = new ArrayList<>();
			
			// path to pdf-generator executable
			command.add(wkhtmltopdf);

			// size: A4
			command.add("-s");
			command.add("A4");

			// orientation
			command.add("-O");
			command.add(orientation);

			// top and bottom margins
			command.add("-T");
			command.add("25mm");
			command.add("-B");
			command.add("12mm");

			command.add("--print-media-type");

			command.add("--enable-smart-shrinking");

			if (StringUtils.isNotBlank(windowStatusForWaiting)) {
				// wait until WM is loaded
				command.add("--load-error-handling");
				command.add("ignore");
				command.add("--window-status");
				command.add(windowStatusForWaiting);
			}

			// use an additional external stylesheet
			command.add("--user-style-sheet");
			command.add(stylesheetName);

			if (StringUtils.isNotBlank(proxyString) && !"None".equals(proxyString)) {
				// Proxy settings
				command.add("--proxy");
				command.add(proxyString);
				for (String nonProxyHost : nonProxyHosts) {
					command.add("--bypass-proxy-for");
					command.add(nonProxyHost);
				}
			}

			// url - source for generation
			command.add(url);

			// file path of pdf to generate
			command.add(pdfName);
			
			if (logger.isDebugEnabled()) {
				logger.debug("wkhtmltopdf command:\n" + StringUtils.join(command, " "));
			}
            
            Process process = Runtime.getRuntime().exec(command.toArray(new String[0]));
            process.waitFor();
            
            if (!pdfInitialFile.exists() || pdfInitialFile.length() == 0) {
            	throw new Exception("Preview generation via wkhtmltopdf was unsuccessful: \n" + StringUtils.join(command, "\n"));
            }

            // draw additional elements to PDF (agnitas logo, footer)
            File finalPdfFile = drawAdditionalElements(title, admin, pdfInitialFile, orientation, footerTitleMessageKey);

        	if (pdfInitialFile.exists()) {
        		try {
					pdfInitialFile.delete();
				} catch (Exception e) {
					logger.error("Cannot delete temporary pdf file: " + pdfInitialFile.getAbsolutePath(), e);
				}
        	}
        	
            if (stylesheetFile.exists()) {
                try {
                    stylesheetFile.delete();
                } catch (Exception e) {
                    logger.error("Cannot delete temporary css file: " + stylesheetFile.getAbsolutePath(), e);
                }
            }
            
            return finalPdfFile;
        } catch (Exception e) {
            logger.error("generatePDF: " + e, e);
            return null;
        }
    }

    /**
     * Draws additional elements in PDF file: agnitas logo and footer
     *
     * @param title name of workflow
     * @param admin current admin
     * @param pdfInitialFile path to initial pdf file
     * @return new pdf file with added logo and footer
     * @throws IOException
     * @throws DocumentException
     */
    private File drawAdditionalElements(String title, Admin admin, File pdfInitialFile, String orientation,
                                        String footerTitleMessageKey) throws IOException, DocumentException {
        File finalFile = File.createTempFile("preview_final_", ".pdf", AgnUtils.createDirectory(PREVIEW_FILE_DIRECTORY));
        PdfReader pdfReader = new PdfReader(pdfInitialFile.getAbsolutePath());
        
        try {
	        try(FileOutputStream fos = new FileOutputStream(finalFile)) {
		        PdfStamper stamper = new PdfStamper(pdfReader, fos);
		        
		        try {
			        //PdfContentByte overContent = stamper.getOverContent(1);
			        int borderGap = 20;
			
			        // draw agnitas logo at the top right corner
			        try(InputStream logoStream = GenerationPDFService.class.getResourceAsStream("/com/agnitas/emm/core/workflow/service/resources/preview_pdf_logo.png")) {
			        
				        if (logoStream == null) {
				        	logger.error("Missing logo file preview_pdf_logo.png for GenerationPDFService");
				        }
				        
				        byte[] logoData = IOUtils.toByteArray(logoStream);
				        Image image = Image.getInstance(logoData);
				        image.scalePercent(14.0f);
				        float topLineStartX;
				        float topLineStartY;
				        float topLineEndX;
				        float topLineEndY;
				        float bottomLineStartX;
				        float bottomLineStartY;
				        float bottomLineEndX;
				        float bottomLineEndY;
				        if ("Portrait".equals(orientation)) {
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
						String workflowTitle = String.format("%s \"%s\"", SafeString.getLocaleString(footerTitleMessageKey, admin.getLocale()), title);
				        DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, admin.getLocale());
				        Date currentDate = GregorianCalendar.getInstance(TimeZone.getTimeZone(admin.getAdminTimezone())).getTime();
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
}
