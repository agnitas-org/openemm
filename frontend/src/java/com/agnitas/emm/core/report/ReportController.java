/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.recipientsreport.service.impl.RecipientReportUtils;
import com.agnitas.emm.core.report.services.RecipientReportService;
import com.agnitas.messages.I18nString;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;

import org.agnitas.emm.core.recipient.service.RecipientService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/report")
@PermissionMapping("report")
public class ReportController implements XssCheckAware {

    private static final String RECIPIENT_REPORT_RIGHTOFACCESS = "recipient.report.rightOfAccess";

    private final RecipientReportService recipientReportService;
    private final RecipientService recipientService;

    public ReportController(RecipientReportService recipientReportService, RecipientService recipientService) {
        this.recipientReportService = recipientReportService;
        this.recipientService = recipientService;
    }

    @PermissionMapping("recipients")
    @GetMapping(value = "/recipients.action")
    public ResponseEntity<Resource> getRecipientReport(@RequestParam("id") int recipientId, Admin admin) {
        String report = recipientReportService.getRecipientTxtReport(recipientId, admin.getCompanyID(), admin.getLocale());

        byte[] byteResource = report.getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(byteResource);
        String email = recipientService.getEmail(recipientId, admin.getCompanyID());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + makeReportFileName(email, admin.getLocale()))
                .contentLength(byteResource.length)
                .contentType(MediaType.TEXT_PLAIN)
                .body(resource);
    }

    private String makeReportFileName(String recipientEmail, Locale locale) {
        String prefix = I18nString.getLocaleString(RECIPIENT_REPORT_RIGHTOFACCESS, locale)
                .toLowerCase()
                .replaceAll(" ", "_");
        recipientEmail = recipientEmail.replace("@", "at");
        return prefix + "_" + recipientEmail + RecipientReportUtils.TXT_EXTENSION;
    }
}
