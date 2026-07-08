/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.domain.web;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtstatistics.domain.form.DomainStatisticForm;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.reporting.birt.external.dataset.DomainStatDataSet;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.MvcUtils;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.RequiredPermission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/statistics/domain")
@RequiredPermission("stats.domains")
public class DomainStatisticController implements XssCheckAware {
	
	private static final Logger logger = LogManager.getLogger(DomainStatisticController.class);

	private final TargetService targetService;
	private final MailinglistApprovalService mailinglistApprovalService;
	private final UserActivityLogService userActivityLogService;
	private final DomainStatDataSet domainStatDataSet;

	public DomainStatisticController(
			TargetService targetService,
			MailinglistApprovalService mailinglistApprovalService,
			UserActivityLogService userActivityLogService,
			DomainStatDataSet domainStatDataSet
	) {
		this.targetService = targetService;
		this.userActivityLogService = userActivityLogService;
		this.mailinglistApprovalService = mailinglistApprovalService;
		this.domainStatDataSet = domainStatDataSet;
	}
	
	@RequestMapping("/view.action")
	public String view(Admin admin, @ModelAttribute("form") DomainStatisticForm form, Model model) {
		model.addAttribute("targetList", targetService.getTargetLights(admin));
		model.addAttribute("mailingLists", mailinglistApprovalService.getEnabledMailinglistsNamesForAdmin(admin));
		model.addAttribute("statistics", domainStatDataSet.getDomainStat(form, admin));

		userActivityLogService.writeUserActivityLog(admin, "domain statistics", "active submenu - domain overview", logger);
		return "stats_birt_domain_stat";
	}

	@GetMapping("/csv.action")
	public ResponseEntity<InputStreamResource> getCsvStatistics(DomainStatisticForm form, Admin admin) throws Exception {
		userActivityLogService.writeUserActivityLog(admin, "profile field statistics", "csv export");
		return MvcUtils.csvFileResponse(domainStatDataSet.csv(form, admin), "domain_overview.csv");
	}
	
}
