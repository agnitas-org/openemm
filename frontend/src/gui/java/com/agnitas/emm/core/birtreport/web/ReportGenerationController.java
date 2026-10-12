/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtreport.web;

import java.io.IOException;

import com.agnitas.emm.core.birtreport.ReportGenerationHeaders;
import com.agnitas.emm.core.birtreport.ReportGenerationTokenService;
import com.agnitas.emm.core.birtreport.dto.ComparisonReportRequest;
import com.agnitas.emm.core.birtreport.dto.MailingReportRequest;
import com.agnitas.emm.core.birtreport.dto.RecipientReportRequest;
import com.agnitas.emm.core.birtreport.dto.StatisticsReportRequest;
import com.agnitas.emm.core.birtreport.dto.TopDomainsReportRequest;
import com.agnitas.emm.core.birtreport.service.ReportGenerationService;
import com.agnitas.reporting.birt.external.utils.BirtReportUtils;
import com.agnitas.util.MvcUtils;
import com.agnitas.web.perm.annotations.Anonymous;
import com.agnitas.web.perm.exceptions.AuthorizationException;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/statistics")
@Anonymous
public class ReportGenerationController {

    private final ReportGenerationService reportGenerationService;
    private final ReportGenerationTokenService reportGenerationTokenService;

    public ReportGenerationController(
            ReportGenerationService reportGenerationService,
            ReportGenerationTokenService reportGenerationTokenService
    ) {
        this.reportGenerationService = reportGenerationService;
        this.reportGenerationTokenService = reportGenerationTokenService;
    }

    @PostMapping("/reports/{reportId}/generate.action")
    public ResponseEntity<InputStreamResource> generateReport(
            @PathVariable int reportId,
            @RequestParam int companyId,
            @RequestBody @Valid StatisticsReportRequest reportRequest,
            @RequestHeader(ReportGenerationHeaders.INTERNAL_TOKEN_HEADER) String token
    ) throws IOException {
        authorize(reportId, companyId, token);

        byte[] data = reportGenerationService.generateReport(reportRequest, reportId, companyId);

        return switch (reportRequest.format()) {
            case CSV -> MvcUtils.csvFileResponse(data, "birt_report.csv");
            case PDF -> MvcUtils.pdfFileResponse(data, "birt_report.pdf");
        };
    }

    @GetMapping("/report/print.action")
    public String print(
            @RequestParam int reportId,
            @RequestParam int companyId,
            @RequestParam String token,
            @RequestParam String reportRequest,
            Model model
    ) {
        authorize(reportId, companyId, token);

        StatisticsReportRequest statisticsReq = reportGenerationService.parseRequest(reportRequest);

        model.addAttribute("darkMode", statisticsReq.darkmode());
        model.addAttribute("figures", BirtReportUtils.unpackFigures(statisticsReq.figures()));
        model.addAttribute("windowWaitStatus", ReportGenerationService.WINDOW_WAIT_STATUS);
        model.addAttribute("reportLocale", statisticsReq.locale().toLanguageTag());

        switch (statisticsReq) {
            case MailingReportRequest req -> {
                model.addAttribute("mailingIds", req.mailings());
                model.addAttribute("mailingsData", reportGenerationService.getMailingsReportData(req, companyId));
            }
            case ComparisonReportRequest ignored -> {
                // TODO: GWUA-7067: implement
            }
            case RecipientReportRequest ignored -> {
                // TODO: GWUA-7067: implement
            }
            case TopDomainsReportRequest ignored -> {
                // TODO: GWUA-7067: implement
            }
        }

        return "report_print";
    }

    private void authorize(int reportId, int companyId, String token) {
        if (!reportGenerationTokenService.verify(reportId, companyId, token)) {
            throw new AuthorizationException();
        }
    }

}
