/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.dao;

import java.util.List;

import com.agnitas.beans.TrackableLink;
import org.agnitas.beans.CompaniesConstraints;

import com.agnitas.emm.core.workflow.beans.ComWorkflowReaction;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionStep;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionStepDeclaration;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionStepInstance;

public interface ComWorkflowReactionDao {
	/**
	 * Check if there's a reaction having such an identifier.
	 *
	 * @param reactionId an identifier of the reaction to check.
	 * @param companyId an identifier of a company that owns referenced reaction.
	 * @return whether ({@code true}) or not ({@code false}) the referenced reaction exists.
	 */
	boolean exists(int reactionId, int companyId);

	ComWorkflowReaction getReaction(int reactionId, int companyId);

	List<ComWorkflowReaction> getReactionsToCheck(CompaniesConstraints constraints);

	/**
	 * Get all undone steps whose time has come (calculated step date is in the past).
	 *
	 * Notice that {@link com.agnitas.emm.core.workflow.beans.ComWorkflowReaction#isActive()} doesn't enable/disable
	 * already scheduled steps. Undone steps are ignored if company has status 'deleted' or 'deletion in progress', or
	 * if a workflow is not active (has status other than {@link com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus#STATUS_ACTIVE}
	 * and {@link com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus#STATUS_TESTING}).
	 *
	 * @return a list of entities representing the retrieved steps.
	 */
	List<WorkflowReactionStep> getStepsToMake(CompaniesConstraints constraints);

    void saveReaction(ComWorkflowReaction reaction);

    /**
   	 * Updates properties of the steps declarations (mailing_id and target_id).
     * For example used in case when mailing icons updated during pause and declarations of the steps
     * should take into account new mailings and appropriate 'received previous mailing' targets 
     *
   	 * @param steps stepDeclarations with updated mailing ids and targets
   	 * @param reactionId an identifier of the reaction to be deactivated.
   	 * @param companyId an identifier of a company that owns referenced reaction.
   	 */
    void updateStepsDeclarations(List<WorkflowReactionStepDeclaration> steps, int reactionId, int companyId);

    void saveReactionStepDeclarations(List<WorkflowReactionStepDeclaration> declarations, int reactionId, int companyId);

	/**
	 * See {@link #deactivateReaction(int, int)} and {@link #getReactionId(int, int)}.
	 *
	 * @param workflowId an identifier of the action-based workflow whose trigger should be deactivated.
	 * @param companyId an identifier of a company that owns referenced workflow.
	 */
	void deactivateWorkflowReactions(int workflowId, int companyId, boolean keepReactionLog);

	void activateWorkflowReactions(int workflowId, int companyId);

	/**
	 * Mark the reaction as inactive and clear all logs (except the entries for trigger-step).
	 *
	 * @param reactionId an identifier of the reaction to be deactivated.
	 * @param companyId an identifier of a company that owns referenced reaction.
	 * @param keepReactionLog whether or not to keep reaction logs.
	 */
	void deactivateReaction(int reactionId, int companyId, boolean keepReactionLog);

	void activateReaction(int reactionId, int companyId);

	/**
	 * See {@link #deleteReaction(int, int)} and {@link #getReactionId(int, int)}.
	 *
	 * @param workflowId an identifier of the action-based workflow whose trigger should be deleted.
	 * @param companyId an identifier of a company that owns referenced workflow.
	 */
	void deleteWorkflowReactions(int workflowId, int companyId);

	/**
	 * Delete all the data related to reactions within specified company.
	 *
	 * @param companyId an identifier of a company that owns data to be deleted.
	 */
	void deleteReactions(int companyId);

	/**
	 * Delete reaction and all connected data (declarations, logs, etc).
	 *
	 * @param reactionId an identifier of the reaction to be deleted.
	 * @param companyId an identifier of a company that owns referenced reaction.
	 */
	void deleteReaction(int reactionId, int companyId);

	List<Integer> getClickedRecipients(ComWorkflowReaction reaction, boolean excludeLoggedReactions);

	List<Integer> getOpenedRecipients(ComWorkflowReaction reaction, boolean excludeLoggedReactions);

	List<Integer> getRecipientsWithChangedProfile(ComWorkflowReaction reaction, boolean excludeLoggedReactions);

	List<Integer> getRecipientsWithChangedBinding(ComWorkflowReaction reaction, boolean excludeLoggedReactions);

	/**
	 * Register triggered (reacted) recipients, generate action-based campaign execution plan from declarations (see {@link #saveReactionStepDeclarations(List, int, int)}).
	 * Use "now" as reaction timestamp and calculate deadlines (if any) for steps (decisions and mailings).
	 *
	 * @param reaction the reaction that has been triggered.
	 * @param recipients recipients who triggered the reaction.
	 */
	void trigger(ComWorkflowReaction reaction, List<Integer> recipients);

	/**
	 * Mark the step as done and register recipients who made this step. Recipients are taken from previous step.
	 * Use "now" as step timestamp for recipients.
	 * Attention: recipients who are not present in customers table are ignored.
	 *
	 * @param step the step to be marked as done.
	 */
	void setStepDone(WorkflowReactionStepInstance step);

	/**
	 * Mark the step as done and register recipients who made this step.
	 * Recipients are taken from previous step (see {@link WorkflowReactionStepInstance#getPreviousStepId()}) and filtered with {@code sqlTargetExpression}.
	 * Use "now" as step timestamp for recipients.
	 *
	 * Attention: recipients who are not present in customers table are ignored.
	 *
	 * @param step the step to be marked as done.
	 * @param sqlTargetExpression and SQL code to be used to filter recipients (see {@link com.agnitas.dao.ComTargetDao#getTargetSQL(int, int)}).
	 */
	void setStepDone(WorkflowReactionStepInstance step, String sqlTargetExpression);

	/**
	 * Mark the step as done and register recipients who made this step.
	 * Recipients are taken from previous step (see {@link WorkflowReactionStepInstance#getPreviousStepId()}) and filtered
	 * with {@code sqlTargetExpression} and {@code mailingListId}. If {@code mailingListId > 0 and requireActiveBinding == true} then
	 * recipients must have active binding (see {@link org.agnitas.beans.BindingEntry#USER_STATUS_ACTIVE}) to pass the filter.
	 * Use "now" as step timestamp for recipients.
	 *
	 * Attention: recipients who are not present in customers table are ignored anyway.
	 *
	 * @param step the step to be marked as done.
	 * @param mailingListId an identifier of mailing list that recipients must have binding to (or 0 to ignore binding check).
	 * @param requireActiveBinding whether ({@code true}) or not ({@code false}) a binding must be active (ignored if {@code mailingListId <= 0}).
	 * @param sqlTargetExpression and SQL code to be used to filter recipients (see {@link com.agnitas.dao.ComTargetDao#getTargetSQL(int, int)}).
	 */
	void setStepDone(WorkflowReactionStepInstance step, int mailingListId, boolean requireActiveBinding, String sqlTargetExpression);

	/**
	 * Mark as done all useless steps — an undone steps whose previous steps are marked as done but have no logged recipients.
	 */
	void setUselessStepsDone(CompaniesConstraints constraints);

	/**
	 * Retrieve all the recipients available at this step.
	 *
	 * @param step a step that recipients should be taken from.
	 * @return list of recipients (ids).
	 */
	List<Integer> getStepRecipients(WorkflowReactionStepInstance step);

    int getReactionId(int workflowId, int companyId);

    boolean isLinkUsedInActiveWorkflow(TrackableLink linkId);
}
