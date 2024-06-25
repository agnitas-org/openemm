/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.agnitas.beans.Admin;
import com.lowagie.text.DocumentException;
import org.agnitas.beans.AdminEntry;

public interface PdfService {

    byte[] writeUsersToPdfAndGetByteArray(List<AdminEntry> users) throws DocumentException, IOException;

    /**
     * Generates a .pdf file of the given web page extended with Agnitas logo and provided title and footer
     *
     * @param admin current admin
     * @param url URL of the web page that should be rendered
     * @param landscape if resulting pdf should have landscape orientation (false - portrait)
     * @param title i.e. entity name
     * @param footerMsgKey key of the message to be displayed at footer
     * @param customCss custom CSS code to be applied to a web page
     * @param windowStatusForWaiting to be removed after GWUA-5471 testing
     * @return .pdf file extended with additional elements
     */
    File generatePDF(Admin admin, String url, boolean landscape, String title, String footerMsgKey, String customCss, String windowStatusForWaiting) throws IOException;

    File generatePDF(Admin admin, String url, boolean landscape, String title, String footerMsgKey, String windowStatusForWaiting) throws IOException;

    /**
     * Generates a .pdf file of the given web page
     *
     * @param url URL of the web page that should be rendered
     * @param landscape if resulting pdf should have landscape orientation (false - portrait)
     * @param customCss custom CSS code to be applied to a web page
     * @return .pdf file of the web page
     */
    File generatePDFWithPuppeteer(String url, boolean landscape, String customCss) throws Exception;

    /**
     * Extends given .pdf file with Agnitas logo at top right and provided title and footer
     *
     * @param admin current admin
     * @param pdf pdf to be extended
     * @param landscape if resulting pdf should have landscape orientation (false - portrait)
     * @param title i.e. entity name
     * @param footerMsgKey key of the message to be displayed at footer
     * @return new pdf file with added logo and footer
     * @throws IOException
     * @throws com.lowagie.text.DocumentException
     */
    File addAdditionalElements(Admin admin, File pdf, boolean landscape, String title, String footerMsgKey) throws IOException, DocumentException;
}
