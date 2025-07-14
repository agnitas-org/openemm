/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service.jobs;

import com.agnitas.emm.core.workflow.beans.WorkflowReaction;
import com.agnitas.emm.core.workflow.dao.WorkflowReactionDao;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.beans.CompaniesConstraints;
import com.agnitas.service.JobWorker;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * Handles start-icon reactions
 */
public class WorkflowReactionJobWorker extends JobWorker {

    private WorkflowReactionDao reactionDao;
    private WorkflowService workflowService;

    @Override
    public String runJob() {
        initBeans();

        final CompaniesConstraints constraints = getCompaniesConstraints();

        // Get reactions we need to check (active reactions with start date in the past)
        for (final WorkflowReaction reaction : reactionDao.getReactionsToCheck(constraints)) {
            final List<Integer> reactedRecipients = workflowService.getReactedRecipients(reaction, true);

            if (CollectionUtils.isNotEmpty(reactedRecipients)) {
                processReaction(reaction, reactedRecipients);
            }
        }

        workflowService.processPendingReactionSteps(constraints);
		
		return null;
    }

    private void processReaction(final WorkflowReaction reaction, final List<Integer> reactedRecipients) {
        reactionDao.trigger(reaction, reactedRecipients);
    }

    private void initBeans() {
        reactionDao = daoLookupFactory.getBeanWorkflowReactionDao();
        workflowService = serviceLookupFactory.getBeanWorkflowService();
    }
}
