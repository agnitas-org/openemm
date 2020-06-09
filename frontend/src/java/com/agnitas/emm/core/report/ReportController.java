/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComRecipientHistory;
import com.agnitas.beans.ComRecipientMailing;
import com.agnitas.beans.ComRecipientReaction;
import com.agnitas.beans.WebtrackingHistoryEntry;
import com.agnitas.emm.core.recipientsreport.service.impl.RecipientReportUtils;
import com.agnitas.emm.core.report.bean.RecipientEntity;
import com.agnitas.emm.core.report.converter.impl.RecipientDeviceHistoryDtoConverter;
import com.agnitas.emm.core.report.converter.impl.RecipientEntityDtoConverter;
import com.agnitas.emm.core.report.converter.impl.RecipientMailingHistoryDtoConverter;
import com.agnitas.emm.core.report.converter.impl.RecipientRetargetingHistoryDtoConverter;
import com.agnitas.emm.core.report.converter.impl.RecipientStatusHistoryDtoConverter;
import com.agnitas.emm.core.report.dto.RecipientDeviceHistoryDto;
import com.agnitas.emm.core.report.dto.RecipientEntityDto;
import com.agnitas.emm.core.report.dto.RecipientMailingHistoryDto;
import com.agnitas.emm.core.report.dto.RecipientRetargetingHistoryDto;
import com.agnitas.emm.core.report.dto.RecipientStatusHistoryDto;
import com.agnitas.emm.core.report.generator.TableGenerator;
import com.agnitas.emm.core.report.printer.RecipientEntityDtoPrinter;
import com.agnitas.emm.core.report.services.RecipientReportService;
import com.agnitas.messages.I18nString;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.emm.core.velocity.VelocityCheck;
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
public class ReportController {

    private static final String RECIPIENT_REPORT_RIGHTOFACCESS = "recipient.report.rightOfAccess";

    private final RecipientReportService recipientReportService;

    @javax.annotation.Resource
    private RecipientStatusHistoryDtoConverter recipientStatusHistoryConverter;

    @javax.annotation.Resource
    private RecipientMailingHistoryDtoConverter recipientMailingHistoryDtoConverter;

    @javax.annotation.Resource
    private RecipientRetargetingHistoryDtoConverter recipientRetargetingHistoryDtoConverter;

    @javax.annotation.Resource
    private RecipientDeviceHistoryDtoConverter recipientDeviceHistoryDtoConverter;

    @javax.annotation.Resource
    private RecipientEntityDtoConverter recipientEntityDtoConverter;

    @javax.annotation.Resource
    private RecipientEntityDtoPrinter recipientEntityDtoPrinter;

    @javax.annotation.Resource(name = "txtTableGenerator")
    private TableGenerator txtTableGenerator;

    public ReportController(RecipientReportService recipientReportService) {
        this.recipientReportService = recipientReportService;
    }

    @PermissionMapping("recipients")
    @GetMapping(value = "/recipients")
    public ResponseEntity<Resource> getRecipientReport(@RequestParam("id") @VelocityCheck int recipientId, ComAdmin admin) {

        Locale locale = admin.getLocale();
        int companyId = admin.getCompanyID();

        // Recipient Info
        RecipientEntity recipientEntity = recipientReportService.getRecipientInfo(recipientId, companyId);
        RecipientEntityDto recipientEntityDto = recipientEntityDtoConverter.convert(recipientEntity, locale);
        String recipientInfo = recipientEntityDtoPrinter.print(recipientEntityDto, locale);

        // Status History
        List<ComRecipientHistory> statusHistory = recipientReportService.getStatusHistory(recipientId, companyId);
        List<RecipientStatusHistoryDto> statusHistoryDto = recipientStatusHistoryConverter.convert(statusHistory, locale);
        String statusHistoryTable = txtTableGenerator.generate(statusHistoryDto, locale);

        // Mailing History
        List<ComRecipientMailing> mailingHistory = recipientReportService.getMailingHistory(recipientId, companyId);
        List<RecipientMailingHistoryDto> mailingHistoryDto = recipientMailingHistoryDtoConverter.convert(mailingHistory, locale);
        String mailingHistoryTable = txtTableGenerator.generate(mailingHistoryDto, locale);

        // Deep Tracking (Retargeting) History
        List<WebtrackingHistoryEntry> trackingHistory = recipientReportService.getRetargetingHistory(recipientId, companyId);
        List<RecipientRetargetingHistoryDto> trackingHistoryDto = recipientRetargetingHistoryDtoConverter.convert(trackingHistory);
        String trackingHistoryTable = txtTableGenerator.generate(trackingHistoryDto, locale);

        // Device history
        List<ComRecipientReaction> deviceHistory = recipientReportService.getDeviceHistory(recipientId, companyId);
        List<RecipientDeviceHistoryDto> deviceHistoryDto = recipientDeviceHistoryDtoConverter.convert(deviceHistory, locale);
        String deviceHistoryTable = txtTableGenerator.generate(deviceHistoryDto, locale);

        // Union of report parts
        StringBuilder report = new StringBuilder();
        report.append(recipientInfo);
        report.append(statusHistoryTable);
        report.append(mailingHistoryTable);
        report.append(trackingHistoryTable);
        report.append(deviceHistoryTable);

        byte[] byteResource = report.toString().getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(byteResource);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + makeReportFileName(recipientEntity.getEmail(), admin.getLocale()))
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
