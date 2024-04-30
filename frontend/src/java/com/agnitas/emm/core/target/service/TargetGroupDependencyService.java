/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.service;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.target.beans.TargetGroupDependentEntry;
import com.agnitas.messages.Message;

import java.util.List;
import java.util.Optional;

public interface TargetGroupDependencyService {

    List<TargetGroupDependentEntry> findDependencies(int targetGroupId, int companyId);

    Optional<TargetGroupDependentEntry> findAnyActualDependency(List<TargetGroupDependentEntry> dependencies);

    Message buildErrorMessage(TargetGroupDependentEntry dependency, String targetName, Admin admin);

}
