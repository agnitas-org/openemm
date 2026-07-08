/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.objectusage.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.agnitas.beans.ExportPredef;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.mailing.bean.LightweightMailing;
import com.agnitas.emm.core.objectusage.common.ObjectUsage;
import com.agnitas.emm.core.objectusage.common.ObjectUsageType;
import com.agnitas.emm.core.workflow.beans.Workflow;

/**
 * Utility methods used by {@link ObjectUsageServiceImpl} and {@link ObjectUsageServiceImplExtended}.
 */
final class ObjectUsageServiceHelper {

    private ObjectUsageServiceHelper() {
        // utility class
    }
	
	/**
	 * Converts list of target groups to list of {@link ObjectUsage}.
	 * 
	 * @param targetGroups list of target groups
	 * 
	 * @return list of {@link ObjectUsage}
	 */
	public static List<ObjectUsage> targetGroupsToObjectUsage(final List<TargetLight> targetGroups) {
		return targetGroups.stream()
				.map(targetGroup -> new ObjectUsage(ObjectUsageType.TARGET_GROUP, targetGroup.getId(), targetGroup.getTargetName()))
				.collect(Collectors.toList());
	}

	/**
	 * Converts list of mailings to list of {@link ObjectUsage}.
	 * 
	 * @param mailings list of mailings
	 * 
	 * @return list of {@link ObjectUsage}
	 */
	public static List<ObjectUsage> mailingsToObjectUsage(final List<LightweightMailing> mailings) {
		return mailings.stream()
				.map(mailing -> new ObjectUsage(ObjectUsageType.MAILING, mailing.getMailingID(), mailing.getShortname()))
				.collect(Collectors.toList());
	}

	public static List<ObjectUsage> mailingsDtoToObjectUsage(final List<LightweightMailing> mailings) {
		return mailings.stream()
				.map(mailing -> new ObjectUsage(ObjectUsageType.MAILING, mailing.getMailingID(),mailing.getShortname()))
				.collect(Collectors.toList());
	}

	public static List<ObjectUsage> mailinglistsToObjectUsage(final List<Mailinglist> mailinglists) {
		return mailinglists.stream()
				.map(ml -> new ObjectUsage(ObjectUsageType.MAILINGLIST, ml.getId(), ml.getShortname()))
				.collect(Collectors.toList());
	}

	public static List<ObjectUsage> exportProfilesToObjectUsage(List<ExportPredef> exportProfiles) {
		return exportProfiles.stream()
				.map(ml -> new ObjectUsage(ObjectUsageType.EXPORT_PROFILE, ml.getId(), ml.getShortname()))
				.collect(Collectors.toList());
	}

    /**
   	 * Converts list of workflows to list of {@link ObjectUsage}.
   	 *
   	 * @param workflows Map of workflows <workflow_id, shortname>
   	 *
   	 * @return list of {@link ObjectUsage}
   	 */
   	public static List<ObjectUsage> workflowToObjectUsage(List<Workflow> workflows) {
   		return workflows.stream()
   				.map(workflow -> new ObjectUsage(ObjectUsageType.WORKFLOW, workflow.getWorkflowId(), workflow.getShortname()))
   				.collect(Collectors.toList());
   	}
}
