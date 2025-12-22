/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.web;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingComparisonDto;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingComparisonFilter;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingComparisonForm;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingComparisonSearchParams;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.service.WebStorage;
import com.agnitas.util.AgnUtils;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.mvc.DeleteFileAfterSuccessReadResource;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.RequiredPermission;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/statistics/mailing/comparison")
@RequiredPermission("stats.mailing")
@SessionAttributes(types = MailingComparisonSearchParams.class)
public class MailingComparisonStatisticController implements XssCheckAware {
    
    private static final Logger logger = LogManager.getLogger(MailingComparisonStatisticController.class);

    private static final String REDIRECT_TO_OVERVIEW = "redirect:/statistics/mailing/comparison/list.action?restoreSort=true";
    public static final int MAX_MAILINGS_SELECTED = 10;
    private static final int MIN_MAILINGS_SELECTED = 2;
    
    private final MailingBaseService mailingBaseService;
    private final BirtStatisticsService birtStatisticsService;
    private final ConversionService conversionService;
    private final WebStorage webStorage;

    public MailingComparisonStatisticController(MailingBaseService mailingBaseService, BirtStatisticsService birtStatisticsService, ConversionService conversionService, WebStorage webStorage) {
        this.mailingBaseService = mailingBaseService;
        this.birtStatisticsService = birtStatisticsService;
        this.conversionService = conversionService;
        this.webStorage = webStorage;
    }

    @InitBinder
    public void initBinder(Admin admin, WebDataBinder binder) {
        SimpleDateFormat dateFormat = admin.getDateFormat();
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    // In case of adding target select on ui, see history to rollback model attrs
    @RequestMapping("/list.action")
    public String list(@RequestParam(required = false) Boolean restoreSort, Admin admin, Model model, MailingComparisonFilter filter, MailingComparisonSearchParams searchParams, Popups popups) {
        try {
            FormUtils.syncPaginationData(webStorage, WebStorage.MAILING_COMPARISON_OVERVIEW, filter, restoreSort);
            searchParams.restoreParams(filter);

            model.addAttribute("mailings", mailingBaseService.getMailingsForComparison(filter, admin));
            model.addAttribute("selectionMax", MAX_MAILINGS_SELECTED);
        } catch (Exception e) {
            logger.error("Mailing list for comparison exception: ", e);
            popups.alert("error.exception");
        }
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
    public String compare(Admin admin, @ModelAttribute("form") MailingComparisonForm form, HttpSession session, Model model, Popups popups) {
        if (!validate(form, popups)) {
            return REDIRECT_TO_OVERVIEW;
        }
    
        MailingComparisonDto comparisonDto = conversionService.convert(form, MailingComparisonDto.class);
        String birtReportUrl = birtStatisticsService.getMailingComparisonStatisticUrl(admin, session.getId(), comparisonDto);

        int companyId = admin.getCompanyID();
        List<String> mailingNames = new ArrayList<>(mailingBaseService.getMailingNames(form.getBulkIds(), companyId).values());

        model.addAttribute("mailingNames", mailingNames);
        model.addAttribute("birtReportUrl", birtReportUrl);
        model.addAttribute("birtExportReportUrl", birtStatisticsService.changeFormat(birtReportUrl, "csv"));

        return "stats_mailing_comp_view";
    }
    
    @PostMapping("/export.action")
    public Object export(Admin admin, @ModelAttribute("form") MailingComparisonForm form, Popups popups, HttpSession session, RedirectAttributes model) {
        if (!validate(form, popups)) {
            model.addFlashAttribute("form", form);
            return new ModelAndView(REDIRECT_TO_OVERVIEW);
        }
        
        MailingComparisonDto comparisonDto = conversionService.convert(form, MailingComparisonDto.class);
        String birtUrl = birtStatisticsService.getMailingComparisonStatisticUrl(admin, session.getId(), comparisonDto);
    
        File file = birtStatisticsService.getBirtMailingComparisonTmpFile(birtUrl, comparisonDto, admin.getCompanyID());
    
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"mailing_comparison.csv\";")
            .contentLength(file.length())
            .contentType(MediaType.parseMediaType("application/csv"))
            .body(new DeleteFileAfterSuccessReadResource(file));
    }
    
    private boolean validate(MailingComparisonForm form, Popups popups) {
        if (form.getBulkIds().size() < MIN_MAILINGS_SELECTED || form.getBulkIds().size() > MAX_MAILINGS_SELECTED) {
            popups.alert("error.NrOfMailings");
        }
        
        return !popups.hasAlertPopups();
    }

}
