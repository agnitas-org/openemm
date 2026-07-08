/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.web;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.archive.service.CampaignService;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingComparisonFilter;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingComparisonSearchParams;
import com.agnitas.emm.core.birtstatistics.service.MailingComparisonStatisticsService;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.reporting.birt.external.beans.MailingComparisonStatisticsData;
import com.agnitas.service.WebStorage;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.MvcUtils;
import com.agnitas.web.forms.BulkActionForm;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.RequiredPermission;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/statistics/mailing/comparison")
@RequiredPermission("stats.mailing")
@SessionAttributes(types = MailingComparisonSearchParams.class)
@SuppressWarnings("squid:S3753") // search parameters intentionally kept in session
public class MailingComparisonStatisticController implements XssCheckAware {

    private static final String REDIRECT_TO_OVERVIEW = "redirect:/statistics/mailing/comparison/list.action?restoreSort=true";
    public static final int MAX_MAILINGS_SELECTED = 10;
    private static final int MIN_MAILINGS_SELECTED = 2;

    private final MailingBaseService mailingBaseService;
    private final WebStorage webStorage;
    private final CampaignService campaignService;
    private final MailingComparisonStatisticsService statisticsService;

    public MailingComparisonStatisticController(
            MailingBaseService mailingBaseService,
            WebStorage webStorage,
            CampaignService campaignService,
            MailingComparisonStatisticsService statisticsService
    ) {
        this.mailingBaseService = mailingBaseService;
        this.webStorage = webStorage;
        this.campaignService = campaignService;
        this.statisticsService = statisticsService;
    }

    @InitBinder
    public void initBinder(Admin admin, WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(admin.getDateFormat(), true));
    }

    // In case of adding target select on ui, see history to rollback model attrs
    @RequestMapping("/list.action")
    public String list(MailingComparisonFilter filter, MailingComparisonSearchParams searchParams,
                       @RequestParam(required = false) Boolean restoreSort, Admin admin, Model model) {
        FormUtils.syncPaginationData(webStorage, WebStorage.MAILING_COMPARISON_OVERVIEW, filter, restoreSort);
        searchParams.restoreParams(filter);

        model.addAttribute("mailings", mailingBaseService.getMailingsForComparison(filter, admin));
        model.addAttribute("selectionMax", MAX_MAILINGS_SELECTED);
        model.addAttribute("availableArchives", campaignService.getCampaigns(admin.getCompanyID()));

        AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
        return "stats_mailing_comp_list";
    }

    @GetMapping("/search.action")
    public String search(MailingComparisonFilter filter, MailingComparisonSearchParams searchParams, RedirectAttributes ra) {
        searchParams.storeParams(filter);
        ra.addFlashAttribute("mailingComparisonFilter", filter);
        return REDIRECT_TO_OVERVIEW;
    }

    @GetMapping("/compare.action")
    public String compare() {
        return REDIRECT_TO_OVERVIEW;
    }

    @PostMapping("/compare.action")
    public String compare(@ModelAttribute("form") BulkActionForm form, Admin admin, Model model, Popups popups) {
        if (!validate(form.getBulkIds(), popups)) {
            return REDIRECT_TO_OVERVIEW;
        }

        model.addAttribute("mailingNames", mailingBaseService.getMailingNames(form.getBulkIds(), admin.getCompanyID()).values());

        return "mailings_comparison_stat_view";
    }

    @GetMapping("/data.action")
    public ResponseEntity<List<MailingComparisonStatisticsData>> getData(@RequestParam Set<Integer> mailingIds, Admin admin) {
        return ResponseEntity.ok(statisticsService.getData(mailingIds, admin.getCompanyID()));
    }

    @RequestMapping(value = "/export.action", method = {RequestMethod.POST, RequestMethod.GET})
    public Object export(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Popups popups) throws Exception {
        if (!validate(bulkIds, popups)) {
            return REDIRECT_TO_OVERVIEW;
        }

        return MvcUtils.csvFileResponse(statisticsService.csv(bulkIds, admin), "mailing_comparison.csv");
    }
    
    private boolean validate(Collection<Integer> ids, Popups popups) {
        if (CollectionUtils.size(ids) < MIN_MAILINGS_SELECTED || CollectionUtils.size(ids) > MAX_MAILINGS_SELECTED) {
            popups.alert("error.NrOfMailings");
        }

        return !popups.hasAlertPopups();
    }

}
