/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.agnitas.beans.CompaniesConstraints;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus;
import com.agnitas.emm.core.workflow.beans.WorkflowDependency;

public interface ComWorkflowDao {
    /**
     * Check whether a referenced workflow exists.
     *
     * @param workflowId a workflow identifier to be checked.
     * @param companyId an identifier of a company that owns referenced workflow.
     * @return {@code true} if a referenced workflow is there or {@code false} otherwise.
     */
    boolean exists(int workflowId, @VelocityCheck int companyId);

	Workflow getWorkflow(int workflowId, @VelocityCheck int companyId);

    /**
     * Get workflow schema or {@code null}.
     *
     * @param workflowId an identifier of a workflow to get schema for.
     * @param companyId an identifier of a company that owns referenced workflow.
     * @return a JSON string representing workflow's icons and connections or {@code null} if workflow doesn't exist.
     */
	String getSchema(int workflowId, @VelocityCheck int companyId);

    /**
     * Store workflow schema string representation (JSON).
     *
     * @param schema a workflow schema string representation (JSON).
     * @param workflowId an identifier of a workflow to update schema for.
     * @param companyId an identifier of a company that owns a referenced workflow.
     * @return whether ({@code true}) or not ({@code false}) schema has been updated.
     * @throws java.lang.NullPointerException if {@code schema} is {@code null}.
     */
	boolean setSchema(String schema, int workflowId, @VelocityCheck int companyId);

	void deleteWorkflow(int workflowId, @VelocityCheck int companyId);

	boolean updateWorkflow(Workflow workflow);

	void createWorkflow(Workflow workflow);

    /**
     * Check if a given entity (dependency) is allowed to be used by a given workflow.
     *
     * @param companyId an identifier of a company that owns referenced entities (the workflow and the dependency).
     * @param workflowId an identifier of a workflow (or {@code 0} if it's a new workflow which is not saved yet).
     * @param dependency an entity (type + id/name) to be checked (if it's allowed to be used by given workflow).
     * @param strict whether ({@code false}) or not ({@code true}) a dependency can be used by multiple non-active workflows.
     * @return whether ({@code true}) or not ({@code false}) an entity use is permitted for given workflow.
     */
    boolean validateDependency(@VelocityCheck int companyId, int workflowId, WorkflowDependency dependency, boolean strict);

    /**
     * Delete all the dependencies of a workflow referenced by {@code workflowId}.
     * @param companyId an identifier of a company that owns referenced entities (the workflow and its dependencies).
     * @param workflowId an identifier of a workflow whose dependencies should be deleted.
     * @param clearTargetConditions the indicator for control delete all dependencies or delete all except target conditions
     */
	void deleteDependencies(@VelocityCheck int companyId, int workflowId, boolean clearTargetConditions);

    /**
     * Delete all the dependencies of workflows that belong to a company referenced by {@code companyId}.
     *
     * @param companyId an identifier of a company that owns referenced entities (the workflow and its dependencies).
     */
	void deleteDependencies(@VelocityCheck int companyId);
    
    /**
     * Delete all target condition(see {@link com.agnitas.emm.core.workflow.beans.WorkflowDependencyType}) dependencies
     * of workflow that belong to a company
     * @param companyId a company identifier
     * @param workflowId a workflow identifier
     */
    void deleteTargetConditionDependencies(@VelocityCheck int companyId, int workflowId);

    /**
     * Save a set of references to entities (see {@link WorkflowDependency}) that a workflow depends on. All unlisted
     * dependencies will be removed from database (if something was saved earlier).
     *
     * @param companyId an identifier of a company that owns referenced entities (the workflow and its dependencies).
     * @param workflowId an identifier of a workflow whose dependencies are to be updated.
     * @param dependencies a set of references to entities to be marked as used by a workflow (use empty set or {@code null} to remove all previously saved dependencies).
     * @param clearTargetConditions the indicator for control delete all dependencies or delete all except target conditions before set new dependencies
     */
	void setDependencies(@VelocityCheck int companyId, int workflowId, Set<WorkflowDependency> dependencies, boolean clearTargetConditions);
    
    /**
     * Add new one dependency to workflow
     *
     * @param companyId a company identifier
     * @param workflowId a workflow identifier
     * @param dependency the dependency reference (see {@link WorkflowDependency})
     */
    void addDependency(@VelocityCheck int companyId, int workflowId, WorkflowDependency dependency);

    List<Workflow> getWorkflowsOverview(Admin admin);

	List<Workflow> getWorkflowsToDeactivate(CompaniesConstraints constraints);

    List<Workflow> getWorkflows(Set<Integer> workflowIds, @VelocityCheck int companyId);

    void changeWorkflowStatus(int workflowId, @VelocityCheck int companyId, WorkflowStatus newStatus);

    List<Integer> getWorkflowIdsByAssignedMailingId(@VelocityCheck int companyId, int mailingId);

    int countCustomers(@VelocityCheck int companyId, int mailinglistId, String targetSQL);

    void deleteWorkflow(int companyID);

    /**
     * Get all workflows having given {@code dependency}.
     *
     * When {@code exceptInactive} is set to {@code true} then all inactive workflows will be ignored.
     *
     * @param companyId an identifier of the current user's company.
     * @param dependency a dependency to look for.
     * @param exceptInactive whether ({@code true}) or not ({@code false}) inactive workflows should be ignored.
     * @return a list of dependent workflows sorted by {@link Workflow#getWorkflowId()} (descending).
     */
    List<Workflow> getDependentWorkflows(@VelocityCheck int companyId, WorkflowDependency dependency, boolean exceptInactive);

    List<Workflow> getDependentWorkflows(@VelocityCheck int companyId, Collection<WorkflowDependency> dependencies, boolean exceptInactive);

    /**
     * Get a list of active workflows depending on customer profile field (referenced by {@code column}) that is required
     * to be trackable (see {@link com.agnitas.beans.ProfileField#getHistorize()}).
     *
     * @param column the column name of the profile field to check.
     * @param companyId an identifier of the current user's company.
     * @return a list of dependent workflows sorted by {@link Workflow#getWorkflowId()} (descending).
     */
    List<Workflow> getActiveWorkflowsTrackingProfileField(String column, @VelocityCheck int companyId);

    /**
     * Get a list of active workflows depending on customer profile field referenced by {@code column}.
     *
     * @param column the column name of the profile field to check.
     * @param companyId an identifier of the current user's company.
     * @return a list of dependent workflows sorted by {@link Workflow#getWorkflowId()} (descending).
     */
    List<Workflow> getActiveWorkflowsUsingProfileField(String column, @VelocityCheck int companyId);

    /**
     * Get active (see {@link WorkflowStatus#STATUS_ACTIVE} and {@link WorkflowStatus#STATUS_TESTING}) workflows being
     * triggered on a referenced {@code column} changes for customers who belong to given mailing list referenced by {@code mailingListId}.
     *
     * @param companyId an identifier of the current user's company.
     * @param mailingListId an identifier of a mailing list that target workflows should use.
     * @param column an profiled field name (DB column) that target workflows should watch.
     * @param isUseRules whether or not target workflows should use specific rules to watch profile field changes.
     * @return a list of workflows watching given profile field for customers of a given mailing list.
     */
    List<Workflow> getActiveWorkflowsDrivenByProfileChange(@VelocityCheck int companyId, int mailingListId, String column, boolean isUseRules);
    
    List<Integer> getAllWorkflowUsedTargets(@VelocityCheck int companyId);
    
    int bulkDeleteTargetCondition(List<Integer> targetIds, int companyId);
    
    void removeMailingsTargetExpressions(int companyId, Set<Integer> mailingIds);
    
    void deactivateWorkflowScheduledReports(int workflowId, @VelocityCheck int companyId);
    
    void deleteWorkflowScheduledReports(int workflowId, int companyId);
}
