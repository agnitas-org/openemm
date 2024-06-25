/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service.impl;

import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.beans.Dependent;
import com.agnitas.emm.core.bounce.dto.BounceFilterDto;
import com.agnitas.emm.core.bounce.service.BounceFilterService;
import com.agnitas.emm.core.components.service.MailingDependencyService;
import com.agnitas.emm.core.mailing.bean.MailingDependentType;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("MailingDependencyService")
public class MailingDependencyServiceImpl implements MailingDependencyService {

    private final BounceFilterService bounceFilterService;
    private final ComWorkflowService workflowService;
    private final EmmActionService actionService;

    @Autowired
    public MailingDependencyServiceImpl(@Qualifier("BounceFilterService") BounceFilterService bounceFilterService, ComWorkflowService workflowService, EmmActionService actionService) {
        this.bounceFilterService = bounceFilterService;
        this.workflowService = workflowService;
        this.actionService = actionService;
    }

    @Override
    // TODO: EMMGUI-714: remove when old design will be removed
    public List<MailingDependentType> detectActiveFilters(String[] selectedFilters, MailingDependentType... types) {
        if (ArrayUtils.isEmpty(selectedFilters)) {
            return Arrays.asList(types);
        }

        return Stream.of(types)
                .filter(t -> ArrayUtils.contains(selectedFilters, t.name()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Dependent<MailingDependentType>> load(int companyId, int mailingId, List<MailingDependentType> types) {
        List<Dependent<MailingDependentType>> result = new ArrayList<>();

        for (MailingDependentType type : types) {
            if (type.equals(MailingDependentType.ACTION)) {
                result.addAll(getDependentType(actionService.getActionListBySendMailingId(companyId, mailingId), item -> type.forId(item.getId(), item.getShortname())));
            } else if (type.equals(MailingDependentType.WORKFLOW)) {
                result.addAll(getDependentType(workflowService.getDependentWorkflowOnMailing(companyId, mailingId), item -> type.forId(item.getWorkflowId(), item.getShortname())));
            } else if (type.equals(MailingDependentType.BOUNCE_FILTER)) {
                List<BounceFilterDto> bounceFilters = bounceFilterService.getDependentBounceFiltersWithActiveAutoResponder(companyId, mailingId);
                result.addAll(getDependentType(bounceFilters, item -> type.forId(item.getId(), item.getShortName())));
            }
        }

        return result;
    }

    private <T> List<Dependent<MailingDependentType>> getDependentType(List<T> list, Function<T, Dependent<MailingDependentType>> callback) {
        if (callback != null) {
            return list.stream().map(callback).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
