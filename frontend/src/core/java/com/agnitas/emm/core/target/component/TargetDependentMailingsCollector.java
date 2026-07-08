/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.component;

import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.target.beans.TargetGroupDependencyType;
import com.agnitas.emm.core.target.beans.TargetGroupDependentEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TargetDependentMailingsCollector implements TargetGroupDependenciesCollector {

    private final MailingService mailingService;

    @Autowired
    public TargetDependentMailingsCollector(MailingService mailingService) {
        this.mailingService = mailingService;
    }

    @Override
    public void collect(List<TargetGroupDependentEntry> dependencies, int targetGroupId, int companyId) {
        List<Integer> mailings = mailingService.findTargetDependentMailings(targetGroupId, companyId);
        List<Integer> usedIn = mailingService.filterNotSentMailings(mailings);

        List<TargetGroupDependentEntry> dependentMailings = mailings.stream()
                .map(id -> new TargetGroupDependentEntry(
                        id,
                        TargetGroupDependencyType.MAILING,
                        usedIn.contains(id),
                        companyId
                ))
                .collect(Collectors.toList());

        dependencies.addAll(dependentMailings);
    }
}
