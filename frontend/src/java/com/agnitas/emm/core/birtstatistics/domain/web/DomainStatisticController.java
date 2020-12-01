/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.domain.web;

import org.agnitas.service.UserActivityLogService;
import org.apache.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.birtstatistics.domain.dto.DomainStatisticDto;
import com.agnitas.emm.core.birtstatistics.domain.form.DomainStatisticForm;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping(value = "/statistics/domain")
@PermissionMapping("domain.statistics")
public class DomainStatisticController {
	
	private static final Logger logger = Logger.getLogger(DomainStatisticController.class);
	
	private static final String BIRT_STATISTIC_URL_WITHOUT_FORMAT = "birtStatisticUrlWithoutFormat";
	private static final String TARGET_LIST = "targetList";
	private static final String MAILING_LISTS = "mailingLists";
	
	private final BirtStatisticsService birtStatisticsService;
	private final ComTargetService targetService;
	private final MailinglistApprovalService mailinglistApprovalService;
	private final ConversionService conversionService;
	private final UserActivityLogService userActivityLogService;
	
	public DomainStatisticController(BirtStatisticsService birtStatisticsService,
	                                 ComTargetService targetService,
	                                 final MailinglistApprovalService mailinglistApprovalService,
	                                 ConversionService conversionService,
	                                 UserActivityLogService userActivityLogService) {
		this.birtStatisticsService = birtStatisticsService;
		this.targetService = targetService;
		this.conversionService = conversionService;
		this.userActivityLogService = userActivityLogService;
		this.mailinglistApprovalService = mailinglistApprovalService;
	}
	
	@RequestMapping("/view.action")
	public String view(ComAdmin admin, DomainStatisticForm form, Model model) throws Exception {
		String sessionId = RequestContextHolder.getRequestAttributes().getSessionId();
		model.addAttribute(TARGET_LIST, targetService.getTargetLights(admin));
		model.addAttribute(MAILING_LISTS, mailinglistApprovalService.getEnabledMailinglistsNamesForAdmin(admin));
		model.addAttribute(BIRT_STATISTIC_URL_WITHOUT_FORMAT,
				birtStatisticsService.getDomainStatisticsUrlWithoutFormat(
						admin, sessionId, conversionService.convert(form, DomainStatisticDto.class), false));
		userActivityLogService.writeUserActivityLog(admin, "domain statistics", "active submenu - domain overview", logger);
		return "stats_birt_domain_stat";
	}
	
}
