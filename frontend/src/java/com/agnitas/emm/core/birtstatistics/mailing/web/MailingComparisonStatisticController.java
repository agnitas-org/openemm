/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.web;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.agnitas.beans.MailingBase;
import org.agnitas.util.AgnUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingComparisonDto;
import com.agnitas.emm.core.birtstatistics.mailing.forms.BulkMailingComparisonForm;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingComparisonForm;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.web.mvc.DeleteFileAfterSuccessReadResource;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/statistics/mailing/comparison")
@PermissionMapping("mailing.comparison.statistics")
public class MailingComparisonStatisticController {
    
	/** The logger. */
    private static final Logger logger = LogManager.getLogger(MailingComparisonStatisticController.class);
    
    public static final int MAX_MAILINGS_SELECTED = 10;
    private static final int MIN_MAILINGS_SELECTED = 2;
    
    private ComTargetService targetService;
    private ComMailingBaseService mailingBaseService;
    private BirtStatisticsService birtStatisticsService;
    private ConversionService conversionService;
    
    public MailingComparisonStatisticController(ComTargetService targetService, ComMailingBaseService mailingBaseService, BirtStatisticsService birtStatisticsService, ConversionService conversionService) {
        this.targetService = targetService;
        this.mailingBaseService = mailingBaseService;
        this.birtStatisticsService = birtStatisticsService;
        this.conversionService = conversionService;
    }
    
    @RequestMapping("/list.action")
    public String list(ComAdmin admin, Model model, @ModelAttribute("form") BulkMailingComparisonForm form, Popups popups) {
        try {
            List<TargetLight> targetGroupList = targetService.getTargetLights(admin);
            List<MailingBase> mailings = mailingBaseService.getMailingsForComparison(admin);
    
            model.addAttribute("targetGroupList", targetGroupList);
            model.addAttribute("mailings", mailings);
            model.addAttribute("selectionMax", MAX_MAILINGS_SELECTED);
        } catch (Exception e) {
            logger.error("Mailing list for comparison exception: ", e);
            popups.alert("error.exception");
        }
        AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
        return "stats_mailing_comp_list";
    }
    
    @GetMapping("/compare.action")
    public String compare() {
        return "redirect:/statistics/mailing/comparison/list.action";
    }
    
    @PostMapping("/compare.action")
    public String compare(ComAdmin admin, @ModelAttribute("form") MailingComparisonForm form, Model model, Popups popups) {
        if (!validate(form, popups)) {
            return "messages";
        }
    
        try {
            String sessionId = RequestContextHolder.getRequestAttributes().getSessionId();
            
            MailingComparisonDto comparisonDto = conversionService.convert(form, MailingComparisonDto.class);
            String birtReportUrl = birtStatisticsService.getMailingComparisonStatisticUrl(admin, sessionId, comparisonDto);
            
            int companyId = admin.getCompanyID();
            List<TargetLight> targetGroupList = targetService.getTargetLights(admin);
            List<String> mailingNames = new ArrayList<>(mailingBaseService.getMailingNames(form.getBulkIds(), companyId).values());
    
            model.addAttribute("targetGroupList", targetGroupList);
            model.addAttribute("mailingNames", mailingNames);
            model.addAttribute("birtReportUrl", birtReportUrl);
            model.addAttribute("birtExportReportUrl", birtStatisticsService.changeFormat(birtReportUrl, "csv"));

        } catch (Exception e) {
            logger.error("Mailings comparison exception: ", e);
            popups.alert("error.exception");
        }
    
        return "stats_mailing_comp_view";
    }
    
    
    @PostMapping("/export.action")
    public Object export(ComAdmin admin, @ModelAttribute("form") MailingComparisonForm form, Popups popups, RedirectAttributes model) throws Exception {
        if (!validate(form, popups)) {
            model.addFlashAttribute("form", form);
            return new ModelAndView("redirect:/statistics/mailing/comparison/list.action");
        }
        
        String sessionId = RequestContextHolder.getRequestAttributes().getSessionId();
        MailingComparisonDto comparisonDto = conversionService.convert(form, MailingComparisonDto.class);
        String birtUrl = birtStatisticsService.getMailingComparisonStatisticUrl(admin, sessionId, comparisonDto);
    
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
