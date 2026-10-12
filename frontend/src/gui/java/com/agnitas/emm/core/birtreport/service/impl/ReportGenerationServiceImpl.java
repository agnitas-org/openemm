/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtreport.service.impl;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.dao.CompanyDao;
import com.agnitas.emm.core.birtreport.ReportGenerationTokenService;
import com.agnitas.emm.core.birtreport.dto.ComparisonReportRequest;
import com.agnitas.emm.core.birtreport.dto.MailingReportRequest;
import com.agnitas.emm.core.birtreport.dto.MailingStatisticReportResultDTO;
import com.agnitas.emm.core.birtreport.dto.RecipientReportRequest;
import com.agnitas.emm.core.birtreport.dto.StatisticsReportRequest;
import com.agnitas.emm.core.birtreport.dto.TopDomainsReportRequest;
import com.agnitas.emm.core.birtreport.service.MailingStatisticsCsvReportService;
import com.agnitas.emm.core.birtreport.service.MailingStatisticsReportService;
import com.agnitas.emm.core.birtreport.service.ReportGenerationService;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.service.PdfGenerationOptions;
import com.agnitas.service.PdfService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class ReportGenerationServiceImpl implements ReportGenerationService {

    private static final Logger logger = LogManager.getLogger(ReportGenerationServiceImpl.class);

    // Must stay above 1000px mobile/compact breakpoint
    private static final int PRINT_VIEWPORT_WIDTH = 1200;

    private static final String HEADER_TEXT = "EMM Report";
    private static final String FOOTER_DATE_FORMAT = "dd/MM/yy";
    private static final String HEADER_FONT_PATH = "/assets/fonts/poppins/Poppins-SemiBold.ttf";
    private static final float HEADER_FONT_SIZE_PT = 13.5f; // 18px at 96dpi -> pt (18 * 72 / 96)
    private static final Color HEADER_COLOR = new Color(0x1A, 0x32, 0x58);
    private static final Color FOOTER_COLOR = new Color(0x4B, 0x4B, 0x4B);

    private static final Duration PDF_TIMEOUT = Duration.ofMinutes(10);

    private final MailingService mailingService;
    private final MailingStatisticsReportService mailingStatisticsReportService;
    private final MailingStatisticsUIReportMapper mailingStatisticsMapper;
    private final MailingStatisticsCsvReportService mailingStatisticsCsvReportService;
    private final PdfService pdfService;
    private final ConfigService configService;
    private final CompanyDao companyDao;
    private final ReportGenerationTokenService reportGenerationTokenService;
    private final ObjectMapper objectMapper;

    public ReportGenerationServiceImpl(
            MailingService mailingService,
            MailingStatisticsReportService mailingStatisticsReportService,
            MailingStatisticsUIReportMapper mailingStatisticsMapper,
            MailingStatisticsCsvReportService mailingStatisticsCsvReportService,
            PdfService pdfService,
            ConfigService configService,
            ReportGenerationTokenService reportGenerationTokenService,
            CompanyDao companyDao
    ) {
        this.mailingService = mailingService;
        this.mailingStatisticsReportService = mailingStatisticsReportService;
        this.mailingStatisticsMapper = mailingStatisticsMapper;
        this.mailingStatisticsCsvReportService = mailingStatisticsCsvReportService;
        this.pdfService = pdfService;
        this.configService = configService;
        this.companyDao = companyDao;
        this.reportGenerationTokenService = reportGenerationTokenService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public byte[] generateReport(
            StatisticsReportRequest request,
            int reportId,
            int companyId
    ) throws IOException {
        return switch (request.format()) {
            case PDF -> generatePdfReport(request, reportId, companyId);
            case CSV -> generateCsvReport(request, companyId);
        };
    }

    private byte[] generateCsvReport(StatisticsReportRequest request, int companyId) {
        return switch (request) {
            case MailingReportRequest req -> mailingStatisticsCsvReportService.build(req, companyId);
            case ComparisonReportRequest ignored -> new byte[0]; // TODO: GWUA-7067: implement
            case RecipientReportRequest ignored -> new byte[0]; // TODO: GWUA-7067: implement
            case TopDomainsReportRequest ignored -> new byte[0]; // TODO: GWUA-7067: implement
        };
    }

    private byte[] generatePdfReport(
            StatisticsReportRequest request,
            int reportId,
            int companyId
    ) throws IOException {
        String printUrl = "%s/statistics/report/print.action?reportId=%d&companyId=%d&token=%s&reportRequest=%s"
                .formatted(
                        configService.getValue(ConfigValue.SystemUrl),
                        reportId,
                        companyId,
                        reportGenerationTokenService.sign(reportId, companyId),
                        URLEncoder.encode(writeValueAsString(request), StandardCharsets.UTF_8)
                );

        logger.info("Generating PDF report from URL: {}", printUrl);

        PdfGenerationOptions options = new PdfGenerationOptions(
                printUrl,
                false,
                HEADER_TEXT,
                buildFooterText(request, companyId),
                "/* no custom css needed for report print */",
                WINDOW_WAIT_STATUS,
                PRINT_VIEWPORT_WIDTH,
                1.0,
                HEADER_FONT_PATH,
                HEADER_FONT_SIZE_PT,
                HEADER_COLOR,
                FOOTER_COLOR,
                PDF_TIMEOUT.toMillis(),
                request.locale().toLanguageTag()
        );

        File file = pdfService.generatePDF(options);
        try {
            return Files.readAllBytes(file.toPath());
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }

    private String buildFooterText(StatisticsReportRequest request, int companyId) {
        String clientName = companyDao.getShortName(companyId);
        String dateStr = new SimpleDateFormat(FOOTER_DATE_FORMAT).format(new Date());
        return Stream.of(clientName, request.reportName(), dateStr)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(", "));
    }

    private String writeValueAsString(StatisticsReportRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize report request", e);
        }
    }

    @Override
    public StatisticsReportRequest parseRequest(String reportRequestJson) {
        try {
            return objectMapper.readValue(reportRequestJson, StatisticsReportRequest.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse report request", e);
        }
    }

    @Override
    public Map<Integer, MailingStatisticReportResultDTO> getMailingsReportData(MailingReportRequest request, int companyId) {
        return mailingStatisticsReportService.getReportData(request, companyId)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            String thumbnailUrl = mailingService.getThumbnailDataUri(entry.getKey(), companyId);
                            return mailingStatisticsMapper.toResultDto(thumbnailUrl, entry.getValue(), request.locale());
                        }
                ));
    }

}
