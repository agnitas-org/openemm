/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.objectusage.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.agnitas.emm.core.mailing.beans.LightweightMailing;

import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.objectusage.common.ObjectUsage;
import com.agnitas.emm.core.objectusage.common.ObjectUserType;

/**
 * Utility methods used by {@link ObjectUsageServiceImpl} and {@link ObjectUsageServiceImplExtended}.
 */
final class ObjectUsageServiceHelper {
	
	/**
	 * Converts list of target groups to list of {@link ObjectUsage}.
	 * 
	 * @param targetGroups list of target groups
	 * 
	 * @return list of {@link ObjectUsage}
	 */
	public static final List<ObjectUsage> targetGroupsToObjectUsage(final List<TargetLight> targetGroups) {
		return targetGroups.stream()
				.map(targetGroup -> new ObjectUsage(ObjectUserType.TARGET_GROUP, targetGroup.getId(), targetGroup.getTargetName()))
				.collect(Collectors.toList());
	}

	/**
	 * Converts list of mailings to list of {@link ObjectUsage}.
	 * 
	 * @param mailings list of mailings
	 * 
	 * @return list of {@link ObjectUsage}
	 */
	public static final List<ObjectUsage> mailingsToObjectUsage(final List<LightweightMailing> mailings) {
		return mailings.stream()
				.map(mailing -> new ObjectUsage(ObjectUserType.MAILING, mailing.getMailingID(), mailing.getShortname()))
				.collect(Collectors.toList());
	}

}
