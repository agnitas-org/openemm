/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.component;

import com.agnitas.emm.core.target.beans.TargetGroupDependencyType;
import com.agnitas.emm.core.target.beans.TargetGroupDependentEntry;
import org.agnitas.dao.ExportPredefDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TargetDependentExportProfilesCollector implements TargetGroupDependenciesCollector {

    private final ExportPredefDao exportPredefDao;

    @Autowired
    public TargetDependentExportProfilesCollector(ExportPredefDao exportPredefDao) {
        this.exportPredefDao = exportPredefDao;
    }

    @Override
    public void collect(List<TargetGroupDependentEntry> dependencies, int targetGroupId, int companyId) {
        List<Integer> profiles = exportPredefDao.findTargetDependentExportProfiles(targetGroupId, companyId);

        List<TargetGroupDependentEntry> dependentProfiles = profiles.stream()
                .map(id -> new TargetGroupDependentEntry(
                        id,
                        TargetGroupDependencyType.EXPORT_PROFILE,
                        companyId
                ))
                .collect(Collectors.toList());

        dependencies.addAll(dependentProfiles);
    }
}
