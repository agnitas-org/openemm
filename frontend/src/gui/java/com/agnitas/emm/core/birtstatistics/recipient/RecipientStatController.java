/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.recipient;

import java.time.LocalDate;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mediatypes.service.MediaTypesService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.UserActivityUtil;
import com.agnitas.web.mvc.XssCheckAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

public class RecipientStatController implements XssCheckAware {
    
    private static final Logger logger = LogManager.getLogger(RecipientStatController.class);

    protected final BirtStatisticsService birtStatisticsService;
    protected final MailinglistApprovalService mailinglistApprovalService;
    protected final RecipientStatsUiService recipientStatsUiService;
    private final TargetService targetService;
    private final MediaTypesService mediaTypesService;
    private final UserActivityLogService userActivityLogService;
    private final ConfigService configService;

    public RecipientStatController(
            BirtStatisticsService birtStatisticsService,
            TargetService targetService,
            MediaTypesService mediaTypesService,
            MailinglistApprovalService mailinglistApprovalService,
            UserActivityLogService userActivityLogService,
            ConfigService configService,
            RecipientStatsUiService recipientStatsUiService
    ) {
        this.birtStatisticsService = birtStatisticsService;
        this.targetService = targetService;
        this.mediaTypesService = mediaTypesService;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.userActivityLogService = userActivityLogService;
		this.configService = configService;
        this.recipientStatsUiService = recipientStatsUiService;
    }

    @GetMapping("/view.action")
    public String view(Admin admin, RecipientStatsForm statsForm, Model model) {
        LocalDate now = LocalDate.now(admin.getZoneId());
        statsForm.setMonth(now.getMonthValue());
        statsForm.setStartDate(now.minusMonths(1).format(admin.getDateFormatter()));
        statsForm.setEndDate(now.format(admin.getDateFormatter()));
        model
                .addAttribute("daysPeriodLimit", configService.getIntegerValue(
                        ConfigValue.MaximumRecipientDetailPeriodDays,
                        admin.getCompanyID())
                )
                .addAttribute("targets", targetService.getTargetLights(admin))
                .addAttribute("mailinglists", mailinglistApprovalService.getEnabledMailinglistsNamesForAdmin(admin))
                .addAttribute("mediatypes", mediaTypesService.getAllowedMediaTypes(admin))
                .addAttribute("years", AgnUtils.getYearList(AgnUtils.getStatStartYearForCompany(admin)))
                .addAttribute("months", AgnUtils.getMonthList());
        writeUAL(admin, "recipient statistics", "active submenu - recipient overview");
        return "recipient_stats";
    }

    @GetMapping("/progress.action")
    @ResponseBody
    public List<PeriodProgressUiRow> getProgressStats(RecipientStatsForm form, Admin admin) {
        writeViewUal(admin, "progress");
        return recipientStatsUiService.getProgress(form, admin);
    }

    @GetMapping("/status-info.action")
    @ResponseBody
    public List<StatusInfoUiRow> getStatusInfoStats(RecipientStatsForm form, Admin admin) {
        writeViewUal(admin, "status info");
        return recipientStatsUiService.getStatusInfo(form, admin);
    }

    @GetMapping("/mail-format.action")
    @ResponseBody
    public List<LongValueUiRow> getMailFormat(RecipientStatsForm form, Admin admin) {
        writeViewUal(admin, "mail format");
        return recipientStatsUiService.getMailFormat(form, admin);
    }

    @GetMapping("/recipient-status.action")
    @ResponseBody
    public List<LongValueUiRow> getRecipientStatus(RecipientStatsForm form, Admin admin) {
        writeViewUal(admin, "recipient status");
        return recipientStatsUiService.getRecipientStatus(form, admin);
    }

    protected void writeViewUal(Admin admin, String page) {
        writeUAL(admin, "view recipient statistics", page);
    }

    protected void writeUAL(Admin admin, String action, String description) {
        UserActivityUtil.log(userActivityLogService, admin, action, description, logger);
    }
}
