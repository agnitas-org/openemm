/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.component;

import com.agnitas.dao.ComMailingComponentDao;
import com.agnitas.emm.core.target.beans.TargetGroupDependencyType;
import com.agnitas.emm.core.target.beans.TargetGroupDependentEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TargetDependentMailingComponentsCollector implements TargetGroupDependenciesCollector {

    private final ComMailingComponentDao componentDao;

    @Autowired
    public TargetDependentMailingComponentsCollector(ComMailingComponentDao componentDao) {
        this.componentDao = componentDao;
    }

    @Override
    public void collect(List<TargetGroupDependentEntry> dependencies, int targetGroupId, int companyId) {
        List<Integer> components = componentDao.findTargetDependentMailingsComponents(targetGroupId, companyId);
        List<Integer> usedIn = componentDao.filterComponentsOfNotSentMailings(components);

        List<TargetGroupDependentEntry> dependentComponents = components.stream()
                .map(id -> new TargetGroupDependentEntry(
                        id,
                        TargetGroupDependencyType.MAILING_COMPONENT,
                        usedIn.contains(id),
                        companyId
                ))
                .collect(Collectors.toList());

        dependencies.addAll(dependentComponents);
    }
}
