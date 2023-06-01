/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.web;

import com.agnitas.beans.Admin;
import com.agnitas.mailing.autooptimization.beans.ComOptimization;
import com.agnitas.mailing.autooptimization.service.ComOptimizationService;
import com.agnitas.mailing.autooptimization.service.ComOptimizationStatService;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.stat.CampaignStatEntry;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Enumeration;
import java.util.Hashtable;

@Controller
@RequestMapping("/optimization/ajax")
@PermissionMapping("optimization.ajax")
public class OptimizationAjaxController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(OptimizationAjaxController.class);
    private static final String[] AVAILABLE_GROUPS = {"group1", "group2", "group3", "group4", "group5"};

    private final ComOptimizationService optimizationService;
    private final ComOptimizationStatService optimizationStatService;

    public OptimizationAjaxController(ComOptimizationService optimizationService, ComOptimizationStatService optimizationStatService) {
        this.optimizationService = optimizationService;
        this.optimizationStatService = optimizationStatService;
    }

    @RequestMapping("/splits.action")
    public ResponseEntity<String> splits(@RequestParam("splitType") String splitType, Admin admin) {
        int groupsCount = optimizationService.getSplitNumbers(admin.getCompanyID(), splitType) - 1;

        String groupsAsString = "";
        if (groupsCount > 0 && groupsCount <= AVAILABLE_GROUPS.length) {
            groupsAsString = StringUtils.join(AVAILABLE_GROUPS, ';', 0, groupsCount);
        } else {
            logger.error("Invalid split groups count: {}", groupsCount);
        }

        return new ResponseEntity<>(groupsAsString, HttpStatus.OK);
    }

    @RequestMapping("/{id:\\d+}/load-statistic.action")
    public String loadStatistic(@PathVariable("id") int id, Model model, Admin admin) {
        if (logger.isInfoEnabled()) {
            logger.info("Starting generation of statistics");
        }

        ComOptimization optimization = optimizationService.get(id, admin.getCompanyID());

        Hashtable<Integer, CampaignStatEntry> stats = optimizationStatService.getStat(optimization);
        Enumeration<Integer> keys = stats.keys();

        model.addAttribute("evalType", optimization.getEvalType());
        model.addAttribute("stats", stats);
        model.addAttribute("keys", keys);

        if (logger.isInfoEnabled()) {
            logger.info("Finished generation of statistics");
        }

        return "campaign_autooptimization_stats";
    }
}
