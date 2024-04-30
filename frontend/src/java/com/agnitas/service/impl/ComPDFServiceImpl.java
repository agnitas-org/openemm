/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.AdminGroup;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.AdminGroupDao;
import com.agnitas.service.ComPDFService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class ComPDFServiceImpl implements ComPDFService {
    /** DAO for accessing admin group data. */
    protected AdminGroupDao adminGroupDao;
    
    @Required
    public void setAdminGroupDao(AdminGroupDao adminGroupDao) {
        this.adminGroupDao = adminGroupDao;
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
        		if (adminGroupsList.length() > 0) {
        			adminGroupsList.append(", ");
        		}
        		adminGroupsList.append(adminGroup.getShortname());
        	}
            table.addCell(adminGroupsList.toString());
        }
    }
}
