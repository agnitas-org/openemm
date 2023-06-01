/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.recipient.web;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.forms.FormDate;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtstatistics.recipient.dto.RecipientStatisticDto;
import com.agnitas.emm.core.birtstatistics.recipient.forms.RecipientStatisticForm;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mediatypes.service.MediaTypesService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.messages.Message;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/statistics/recipient")
@PermissionMapping("recipient.stats")
public class RecipientStatController implements XssCheckAware {
    
    private static final Logger logger = LogManager.getLogger(RecipientStatController.class);

    private final BirtStatisticsService birtStatisticsService;
    private final ComTargetService targetService;
    private final MediaTypesService mediaTypesService;
    private final ConversionService conversionService;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final UserActivityLogService userActivityLogService;
    private final ConfigService configService;
    
    public RecipientStatController(BirtStatisticsService birtStatisticsService, ComTargetService targetService,
                                   @Qualifier("MediaTypesService") MediaTypesService mediaTypesService, ConversionService conversionService,
                                   MailinglistApprovalService mailinglistApprovalService, UserActivityLogService userActivityLogService,
                                   ConfigService configService) {
        this.birtStatisticsService = birtStatisticsService;
        this.targetService = targetService;
        this.mediaTypesService = mediaTypesService;
        this.conversionService = conversionService;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.userActivityLogService = userActivityLogService;
		this.configService = configService;
    }
    
    @RequestMapping("/view.action")
    public String view(Admin admin, @ModelAttribute(name="form") RecipientStatisticForm form, Model model, Popups popups) throws Exception {
        if (!validateDates(admin, form.getStartDate(), form.getEndDate(), popups)) {
            return "messages";
        }
    
        SimpleDateFormat datePickerFormat = admin.getDateFormat();
        setDefaultDateValuesIfEmpty(form, datePickerFormat);
    
        String sessionId = RequestContextHolder.getRequestAttributes().getSessionId();
        
        // Admins date format is missing in FormDate, so add it
        form.getStartDate().setFormatter(admin.getDateFormatter());
        form.getEndDate().setFormatter(admin.getDateFormatter());
        
        RecipientStatisticDto statisticDto = convertToDto(form, admin);
        
		int maxPeriodDays = configService.getIntegerValue(ConfigValue.MaximumRecipientDetailPeriodDays, admin.getCompanyID());
    	long selectedDays = statisticDto.getLocaleStartDate().until(statisticDto.getLocalEndDate(), ChronoUnit.DAYS);
    	if (selectedDays > maxPeriodDays) {
    		LocalDate newEndDate = statisticDto.getLocaleStartDate().plus(maxPeriodDays, ChronoUnit.DAYS);
			statisticDto.setLocalEndDate(newEndDate);
    		popups.warning(Message.of("statistics.maximumRecipientDetailPeriodDays.exceeded", maxPeriodDays));
    		form.getEndDate().set(newEndDate, DateTimeFormatter.ofPattern(datePickerFormat.toPattern()));
    	}

        model.addAttribute("birtStatisticUrlWithoutFormat", birtStatisticsService.getRecipientStatisticUrlWithoutFormat(admin, sessionId, statisticDto));
        
        model.addAttribute("localeDatePattern", datePickerFormat.toPattern());
        
        model.addAttribute("targetlist", targetService.getTargetLights(admin));
        model.addAttribute("mailinglists", mailinglistApprovalService.getEnabledMailinglistsNamesForAdmin(admin));
        model.addAttribute("mediatypes", mediaTypesService.getAllowedMediaTypes(admin));
        
        model.addAttribute("yearlist", AgnUtils.getYearList(AgnUtils.getStatStartYearForCompany(admin)));
        model.addAttribute("monthlist", AgnUtils.getMonthList());
        
        userActivityLogService.writeUserActivityLog(admin, "recipient statistics", "active submenu - recipient overview", logger);

        return "stats_birt_recipient_stat";
    }

    private RecipientStatisticDto convertToDto(final RecipientStatisticForm form, final Admin admin) {
        final RecipientStatisticDto statisticDto = conversionService.convert(form, RecipientStatisticDto.class);
        resolveDateModeDateRestrictions(statisticDto, form, admin);
        return statisticDto;
    }

    private void resolveDateModeDateRestrictions(final RecipientStatisticDto dto, final RecipientStatisticForm form, final Admin admin) {
        switch (form.getDateMode()) {
            case LAST_WEEK:
                LocalDate now = LocalDate.now();
                dto.setLocalEndDate(now.minusDays(1));
                dto.setLocalStartDate(now.minusWeeks(1));
                break;

            case SELECT_PERIOD:
                dto.setLocalStartDate(form.getStartDate().get(admin.getDateFormatter()));
                dto.setLocalEndDate(form.getEndDate().get(admin.getDateFormatter()));
                break;

            case SELECT_MONTH:
            default:
                LocalDate dateByParams = LocalDate.of(form.getYear(), form.getMonthValue(), 1);
                dto.setLocalStartDate(dateByParams);
                dto.setLocalEndDate(dateByParams.plusMonths(1).minusDays(1));
        }
    }

    private void setDefaultDateValuesIfEmpty(final RecipientStatisticForm form, SimpleDateFormat datePickerFormat) {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datePickerFormat.toPattern());
    
        if (form.getMonth() < 0) {
            form.setMonth(now.getMonthValue()-1);
        }
        if (form.getYear() <= 0) {
            form.setYear(now.getYear());
        }
        
        LocalDate endDate = form.getEndDate().get(formatter);
        if (StringUtils.isBlank(form.getEndDate().getDate())) {
            endDate = now;
            form.getEndDate().set(endDate, formatter);
        }
        
        if (StringUtils.isBlank(form.getStartDate().getDate())) {
            form.getStartDate().set(endDate.minusMonths(1), formatter);
        }
    }

    private boolean validateDates(final Admin admin, final FormDate startDate, final FormDate endDate, final Popups popups) {
        if(StringUtils.isBlank(startDate.getDate()) && StringUtils.isBlank(endDate.getDate())){
            return true;
        }
        
        String pattern = admin.getDateFormat().toPattern();
        if (!AgnUtils.isDateValid(startDate.getDate(), pattern) || !AgnUtils.isDateValid(endDate.getDate(), pattern)) {
            popups.alert("error.date.format");
            return false;
        }

        if (StringUtils.isNotBlank(startDate.getDate()) && StringUtils.isNotBlank(endDate.getDate())
                && !AgnUtils.isDatePeriodValid(startDate.getDate(), endDate.getDate(), pattern)) {
            popups.alert("error.period.format");
            return false;
        }
        return true;
    }
}
