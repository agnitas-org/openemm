/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.Campaign;
import org.agnitas.beans.CompaniesConstraints;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComMailing;
import com.agnitas.beans.ComProfileField;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.workflow.beans.ComWorkflowReaction;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus;
import com.agnitas.emm.core.workflow.beans.WorkflowDependency;
import com.agnitas.emm.core.workflow.beans.WorkflowFollowupMailing;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.beans.WorkflowRule;
import com.agnitas.emm.core.workflow.dao.ComWorkflowReactionDao;
import com.agnitas.emm.core.workflow.graph.WorkflowNode;
import com.agnitas.userform.bean.UserForm;

public interface ComWorkflowService {

    void saveWorkflow(ComAdmin admin, Workflow workflow, List<WorkflowIcon> icons);

	void saveWorkflow(Workflow workflow);

	Workflow getWorkflow(int workflowId, int companyId);

    /**
     * Get object representation of workflow schema (icons and connections) or {@code null} if referenced workflow doesn't exist.
     *
     * @param workflowId an identifier of a workflow to get schema for.
     * @param companyId an identifier of a company that owns a referenced workflow.
     * @return a list of workflow icons representing a workflow schema or {@code null} if workflow doesn't exist.
     */
	List<WorkflowIcon> getIcons(int workflowId, @VelocityCheck int companyId);

    /**
     * Retrieve icons of a workflow referenced by {@code workflowId} and prepare them to be used in another workflow.
     * In case {@code isWithContent == true} all the mailings used by workflow will be cloned and all the mailing references
     * replaced by created copies.
     * In case {@code isWithContent == false} all the icons will be cleared (all the options/settings).
     *
     * @param admin currently authorized admin.
     * @param workflowId an identifier of a workflow to get existing icons from.
     * @param isWithContent whether ({@code true}) or not ({@code false}) to preserve options/settings within icons.
     * @return cloned icons list or {@code null} if a referenced workflow doesn't exist.
     */
	List<WorkflowIcon> getIconsForClone(ComAdmin admin, int workflowId, boolean isWithContent);

    /**
     * A shortcut for {@link com.agnitas.emm.core.workflow.dao.ComWorkflowDao#validateDependency(int, int, WorkflowDependency, boolean)} where
     * a {@code strict} argument will be determined by a type of given dependency (see {@link WorkflowDependency#getType()}).
     *
     * @param companyId an identifier of a company that owns referenced entities (the workflow and the dependency).
     * @param workflowId an identifier of a workflow (or {@code 0} if it's a new workflow which is not saved yet).
     * @param dependency an entity (type + id/name) to be checked (if it's allowed to be used by given workflow).
     * @return whether ({@code true}) or not ({@code false}) an entity use is permitted for given workflow.
     */
    boolean validateDependency(@VelocityCheck int companyId, int workflowId, WorkflowDependency dependency);

	void deleteWorkflow(int workflowId, int companyId);

    List<Workflow> getWorkflowsOverview(int companyId);

	List<LightweightMailing> getAllMailings(int companyId);

    List<LightweightMailing> getAllMailingsSorted(int companyId, String sortFiled, String sortDirection);

    List<Map<String, Object>> getAllMailings(int companyId, List<Integer> mailingTypes, String status,
                                                    String mailingStatus, boolean takeMailsForPeriod, String sort,
                                                    String order);

    List<Map<String, Object>> getMailings(@VelocityCheck int companyId, String commaSeparatedMailingIds);

	Map<Integer, String> getMailingLinks(int mailingId, int companyId);

	Map<Integer, String> getAllReports(int companyId);

	List<TargetLight> getAllTargets(int companyId);

	List<ComProfileField> getHistorizedProfileFields(int companyId) throws Exception;

	List<ComProfileField> getProfileFields(int companyId) throws Exception;

	List<AdminEntry> getAdmins(int companyId);

    List<UserForm> getAllUserForms(int companyId);

    LightweightMailing getMailing(int mailingId);

    String getMailingName(int mailingId);

    ComMailing getMailing(int mailingId, int companyId);

    Map<String, Object> getMailingWithWorkStatus(int mailingId, int companyId);

    String getTargetSplitName(int splitId);

    void updateMailings(Workflow workflow, ComAdmin admin) throws Exception;

    Workflow copyWorkflow(ComAdmin admin, int workflowId, boolean isWithContent);

    boolean hasCompanyDeepTrackingTables(int companyId);

    Date getChainDate(List<WorkflowNode> chain);

    Date getChainDate(List<WorkflowNode> chain, WorkflowIcon terminatingIcon);

    Date getMaxPossibleDate(WorkflowIcon icon, List<WorkflowIcon> icons);

    Date getMaxPossibleDate(List<List<WorkflowNode>> chains);

    void bulkDelete(Set<Integer> workflowIds, @VelocityCheck int companyId);

    void bulkDeactivate(Set<Integer> workflowIds, @VelocityCheck int companyId) throws Exception;

	void changeWorkflowStatus(int workflowId, @VelocityCheck int companyId, WorkflowStatus newStatus) throws Exception;

	List<Workflow> getWorkflowsToDeactivate(CompaniesConstraints constraints);

    List<Workflow> getWorkflowsByIds(Set<Integer> workflowIds, @VelocityCheck int companyId);

    /**
     * Collect workflow-driven settings provided by different workflow icons and set to {@code mailing} object.
     *
     * @param admin an admin who makes the changes (or the one who created a workflow).
     * @param mailing a mailing object to set collected settings to.
     * @param workflowId an identifier of a workflow to take settings from.
     * @param iconId an identifier of a workflow mailing icon to collect settings for.
     * @return {@code true} if data was collected or {@code false} if some arguments were invalid
     *  or workflow has invalid structure so settings can't be collected.
     */
    boolean assignWorkflowDrivenSettings(ComAdmin admin, ComMailing mailing, int workflowId, int iconId);

    List<List<WorkflowNode>> getChains(WorkflowIcon icon, List<WorkflowIcon> icons, boolean isForwardDirection);

    List<Integer> getWorkflowIdsByAssignedMailingId(@VelocityCheck int companyId, int mailingId);

    boolean hasDeletedMailings(List<WorkflowIcon> workflowIcons, @VelocityCheck int companyId);

    List<WorkflowFollowupMailing> getFollowupMailingIcon(List<WorkflowIcon> workflowIcons);

    boolean isAdditionalRuleDefined(@VelocityCheck int companyId, int mailingId, int workflowId);

    /**
     * Check whether customers should have a binding status {@link org.agnitas.beans.BindingEntry#USER_STATUS_ACTIVE} to
     * receive a triggered mailing. Normally they should but unsubscribed customers and the ones who are waiting for confirmation
     * should receive the mailing though they are not active.
     *
     * @param reaction an entity representing a watched reaction.
     * @return whether ({@code true}) or not ({@code false}) customers should be active.
     */
    boolean checkReactionNeedsActiveBinding(ComWorkflowReaction reaction);

    /**
     * User statuses are required to compose correct sql for email recipients selection.
     */
    List<Integer> getProperUserStatusList(ComWorkflowReaction reaction);

    /**
     * Get an entity representing a trigger for the referenced action-based workflow or {@code null} if the workflow
     * has a different start type.
     * See {@link com.agnitas.emm.core.workflow.dao.ComWorkflowReactionDao#getReaction(int, int)}.
     *
     * @param workflowId an identifier of the workflow.
     * @param companyId an identifier of a company that owns the workflow.
     * @return an entity representing a trigger of the action-based workflow.
     */
    ComWorkflowReaction getWorkflowReaction(int workflowId, @VelocityCheck int companyId);

    /**
     * Get recipients reacted (since the previously logged reaction (if any) or since the reaction start date)
     * depending on the type of reaction.
     *
     * @param reaction an entity representing the trigger for an action-based mailing.
     * @param excludeLoggedReactions whether ({@code true}) or not ({@code false}) processed recipients (reacted and logged) should be excluded.
     * @return a list of the reacted recipients' identifiers (or an empty list if no one reacted).
     */
    List<Integer> getReactedRecipients(ComWorkflowReaction reaction, boolean excludeLoggedReactions);

    /**
     * Found all undone step instances whose date is in the past and process them (calculate recipients, mark steps as done, send mailings).
     * See {@link ComWorkflowReactionDao#getStepsToMake(CompaniesConstraints)} and {@link ComWorkflowReactionDao#setUselessStepsDone(CompaniesConstraints)}.
     */
    void processPendingReactionSteps(CompaniesConstraints constraints);

    /**
     * See {@link com.agnitas.emm.core.workflow.dao.ComWorkflowDao#getActiveWorkflowsTrackingProfileField(String, int)}.
     *
     * @param column the column name of the profile field to check.
     * @param companyId an identifier of the current user's company.
     * @return a list of dependent workflows sorted by {@link Workflow#getWorkflowId()} (descending).
     */
    List<Workflow> getActiveWorkflowsTrackingProfileField(String column, @VelocityCheck int companyId);

    /**
     * See {@link com.agnitas.emm.core.workflow.dao.ComWorkflowDao#getActiveWorkflowsUsingProfileField(String, int)}.
     *
     * @param column the column name of the profile field to check.
     * @param companyId an identifier of the current user's company.
     * @return a list of dependent workflows sorted by {@link Workflow#getWorkflowId()} (descending).
     */
    List<Workflow> getActiveWorkflowsDependentOnProfileField(String column, @VelocityCheck int companyId);

    /**
     * Get active (see {@link WorkflowStatus#STATUS_ACTIVE} and {@link WorkflowStatus#STATUS_TESTING}) workflows being
     * triggered on particular changes (see {@code rules}) of a referenced {@code column} for customers who belong to
     * given mailing list referenced by {@code mailingListId}.
     *
     * @param companyId an identifier of the current user's company.
     * @param mailingListId an identifier of a mailing list that target workflows should use.
     * @param column an profiled field name (DB column) that target workflows should watch.
     * @param rules an object representation of a condition that target workflows should use to watch a profile field.
     * @return a list of workflows watching given profile field using given rules and mailing list.
     */
    List<Workflow> getActiveWorkflowsDrivenByProfileChange(@VelocityCheck int companyId, int mailingListId, String column, List<WorkflowRule> rules);

    /**
     * Add a report referenced by {@code reportId} to an workflow's report icon referenced by {@code workflowId} and {@code iconId}.
     * If report icon already contains {@code reportId} then no changes are made (to prevent duplication) but method returns {@code true} anyway.
     *
     * @param admin currently authorized admin.
     * @param workflowId an identifier of a workflow to update.
     * @param iconId an identifier of a report icon within a workflow.
     * @param reportId an identifier of a report to add to report icon.
     * @return {@code true} if succeeded or {@code false} if something went wrong (e.g. workflow or referenced icon doesn't exist).
     */
    boolean addReportToIcon(ComAdmin admin, int workflowId, int iconId, int reportId);

    List<Campaign> getCampaignList(@VelocityCheck int companyId, String sort, int order);
    
    /**
     * Add to workflow dependencies target group that is created by CM and belongs to company
     * @param target created by CM target group
     * @param companyId  company identifier
     * @param workflowId a workflow identifier
     */
    void setTargetConditionDependency(ComTarget target, @VelocityCheck int companyId, int workflowId);
    
    /**
     * Delete target groups with
     * type TARGET_GROUP_CONDITION (* see {@link com.agnitas.emm.core.workflow.beans.WorkflowDependencyType#TARGET_GROUP_CONDITION})
     * and delete them from workflow dependencies as well that belongs to company
     * @param companyId  company identifier
     * @param workflowId a workflow identifier
     */
    void deleteWorkflowTargetConditions(@VelocityCheck int companyId, int workflowId);
    
    void convertTargetRepresentationToEQL(@VelocityCheck int companyId);

    JSONArray getWorkflowListJson(int companyId);
}
