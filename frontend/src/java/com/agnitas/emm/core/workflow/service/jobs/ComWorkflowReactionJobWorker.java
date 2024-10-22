/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service.jobs;

import com.agnitas.emm.core.workflow.beans.ComWorkflowReaction;
import com.agnitas.emm.core.workflow.dao.ComWorkflowReactionDao;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import org.agnitas.beans.CompaniesConstraints;
import org.agnitas.service.JobWorker;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * Handles start-icon reactions
 */
public class ComWorkflowReactionJobWorker extends JobWorker {

    private ComWorkflowReactionDao reactionDao;
    private ComWorkflowService workflowService;

    @Override
    public String runJob() throws Exception {
        initBeans();

        final CompaniesConstraints constraints = getCompaniesConstraints();

        // Get reactions we need to check (active reactions with start date in the past)
        for (final ComWorkflowReaction reaction : reactionDao.getReactionsToCheck(constraints)) {
            final List<Integer> reactedRecipients = workflowService.getReactedRecipients(reaction, true);

            if (CollectionUtils.isNotEmpty(reactedRecipients)) {
                processReaction(reaction, reactedRecipients);
            }
        }

        workflowService.processPendingReactionSteps(constraints);
		
		return null;
    }

    private void processReaction(final ComWorkflowReaction reaction, final List<Integer> reactedRecipients) {
        reactionDao.trigger(reaction, reactedRecipients);
    }

    private void initBeans() {
        reactionDao = daoLookupFactory.getBeanWorkflowReactionDao();
        workflowService = serviceLookupFactory.getBeanWorkflowService();
    }
}
