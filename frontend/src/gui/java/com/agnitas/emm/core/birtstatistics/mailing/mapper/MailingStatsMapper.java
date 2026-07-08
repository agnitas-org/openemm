/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.mapper;

import java.util.List;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.birtstatistics.dao.MailingTopDomainsStatsDao;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatisticForm;
import com.agnitas.emm.core.birtstatistics.service.MailingBounceStatsService.BounceStatsRequest;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.birtstatistics.service.MailingTopDomainsStatsService.TopDomainsStatsRequest;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class MailingStatsMapper {

    @Autowired
    protected AdminService adminService;

    @Autowired
    protected MailingStatisticsService statisticsService;

    @Mapping(target = "filter", expression = "java(mapTopDomainsFilter(mailingId, admin, form))")
    @Mapping(target = "targets", expression = "java(getTargets(form, admin))")
    @Mapping(target = "locale", source = "admin.locale")
    public abstract TopDomainsStatsRequest toTopDomainsRequest(
            int mailingId,
            Admin admin,
            MailingStatisticForm form
    );

    @Mapping(target = "mailingId", source = "mailingId")
    @Mapping(target = "companyId", source = "admin.companyID")
    @Mapping(target = "locale", source = "admin.locale")
    @Mapping(target = "targets", expression = "java(getTargets(form, admin))")
    @Mapping(target = "altgSql", expression = "java(getAltgs(admin))")
    public abstract BounceStatsRequest toBounceRequest(
            int mailingId,
            Admin admin,
            MailingStatisticForm form
    );

    protected MailingTopDomainsStatsDao.TopDomainsFilter mapTopDomainsFilter(int mailingId, Admin admin, MailingStatisticForm form) {
        return new MailingTopDomainsStatsDao.TopDomainsFilter(
                mailingId,
                admin.getCompanyID(),
                form.getMaxDomains(),
                form.isTopLevelDomain()
        );
    }

    protected List<LightTarget> getTargets(MailingStatisticForm form, Admin admin) {
        return statisticsService.getTargetsWithAllSubscribersTarget(form.getTargetIds(), admin);
    }

    protected String getAltgs(Admin admin) {
        Set<Integer> altgs = adminService.isExtendedAltgEnabled(admin)
                ? admin.getAltgIds()
                : Set.of(adminService.getAccessLimitTargetId(admin));
        return statisticsService.getTargetSql(altgs, admin.getCompanyID());
    }
}
