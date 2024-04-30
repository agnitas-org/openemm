/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.beans.CompaniesConstraints;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.dao.impl.mapper.StringRowMapper;

import org.agnitas.util.DbUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.PredicateUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus;
import com.agnitas.emm.core.workflow.beans.WorkflowDependency;
import com.agnitas.emm.core.workflow.beans.WorkflowDependencyType;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionType;
import com.agnitas.emm.core.workflow.beans.WorkflowStart;
import com.agnitas.emm.core.workflow.beans.WorkflowStop;
import com.agnitas.emm.core.workflow.dao.ComWorkflowDao;

public class ComWorkflowDaoImpl extends BaseDaoImpl implements ComWorkflowDao {
	
	private static final Logger logger = LogManager.getLogger(ComWorkflowDaoImpl.class);

    protected ComTargetService targetService;

    @Override
    public boolean exists(int workflowId, int companyId) {
        String sqlGetCount = "SELECT COUNT(*) FROM workflow_tbl WHERE workflow_id = ? AND company_id = ?";
        return selectInt(logger, sqlGetCount, workflowId, companyId) > 0;
    }

	@Override
	public Workflow getWorkflow(int workflowId, int companyId) {
        return selectObjectDefaultNull(logger, "SELECT * FROM workflow_tbl WHERE workflow_id = ? AND company_id = ?", new WorkflowRowMapper(), workflowId, companyId);
	}

	@Override
    public String getSchema(int workflowId, int companyId) {
	    String sqlGetSchema = "SELECT workflow_schema FROM workflow_tbl WHERE workflow_id = ? AND company_id = ?";
	    return selectObjectDefaultNull(logger, sqlGetSchema, StringRowMapper.INSTANCE, workflowId, companyId);
    }

    @Override
    public boolean setSchema(String schema, int workflowId, int companyId) {
        String sqlSetSchema = "UPDATE workflow_tbl SET workflow_schema = ? WHERE workflow_id = ? AND company_id = ?";
        return update(logger, sqlSetSchema, Objects.requireNonNull(schema, "schema == null"), workflowId, companyId) > 0;
    }

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteWorkflow(int workflowId, int companyId) {
	    update(logger, "DELETE FROM workflow_tbl WHERE workflow_id = ? AND company_id = ?", workflowId, companyId);
	}

    @Override
    @DaoUpdateReturnValueCheck
    public void deleteWorkflow(int companyId) {
        update(logger, "DELETE FROM workflow_tbl WHERE company_id = ?", companyId);
    }

    @Override
	@DaoUpdateReturnValueCheck
	public boolean updateWorkflow(Workflow workflow) {
        String updateStatement = "UPDATE workflow_tbl SET " +
                "shortname = ?, " +
                "description = ?, " +
                "status = ?, " +
                "editor_position_left = ?, " +
                "editor_position_top = ?, " +
                "is_inner = ?, " +
                "general_start_date = ?, " +
                "general_end_date = ?, " +
                "end_type = ?, " +
                "general_start_reaction = ?, " +
                "general_start_event = ?, " +
                "created = ?, " +
                "workflow_schema = ? " +
                "WHERE workflow_id = ? AND company_id = ?";

        int rows = update(logger, updateStatement, workflow.getShortname(),
                workflow.getDescription(),
                workflow.getStatus().getId(),
                workflow.getEditorPositionLeft(),
                workflow.getEditorPositionTop(),
                BooleanUtils.toInteger(workflow.isInner()),
                workflow.getGeneralStartDate(),
                workflow.getGeneralEndDate(),
                workflow.getEndType() == null ? 0 : workflow.getEndType().getId(),
                workflow.getGeneralStartReaction() == null ? 0 : workflow.getGeneralStartReaction().getId(),
                workflow.getGeneralStartEvent() == null ? 0 : workflow.getGeneralStartEvent().getId(),
                new Date(),
                workflow.getWorkflowSchema(),
                workflow.getWorkflowId(),
                workflow.getCompanyId());

        return rows > 0;
    }

	@Override
	@DaoUpdateReturnValueCheck
	public void createWorkflow(Workflow workflow) {
	    String insertStatement;
		int newId;
		if (isOracleDB()) {
		    insertStatement ="INSERT INTO workflow_tbl (" +
                    "workflow_id, " +
                    "company_id, " +
                    "shortname, " +
                    "description, " +
                    "status, " +
                    "editor_position_left, " +
                    "editor_position_top, " +
                    "is_inner, " +
                    "general_start_date, " +
                    "general_end_date, " +
                    "end_type, " +
                    "general_start_reaction, " +
                    "general_start_event, " +
                    "workflow_schema) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			newId = selectInt(logger, "SELECT workflow_tbl_seq.NEXTVAL FROM dual");
            int affectedRows = update(logger, insertStatement,
                    newId,
                    workflow.getCompanyId(),
                    workflow.getShortname(),
                    workflow.getDescription(),
                    workflow.getStatus().getId(),
                    workflow.getEditorPositionLeft(),
                    workflow.getEditorPositionTop(),
                    BooleanUtils.toInteger(workflow.isInner()),
                    workflow.getGeneralStartDate(),
                    workflow.getGeneralEndDate(),
                    workflow.getEndType() == null ? 0 : workflow.getEndType().getId(),
                    workflow.getGeneralStartReaction() == null ? 0 : workflow.getGeneralStartReaction().getId(),
                    workflow.getGeneralStartEvent() == null ? 0 : workflow.getGeneralStartEvent().getId(),
                    workflow.getWorkflowSchema());
            if (affectedRows == 0) {
                throw new RuntimeException();
            }
            workflow.setWorkflowId(newId);
        } else {
            insertStatement = "INSERT INTO workflow_tbl (" +
                    "company_id, " +
                    "shortname, " +
                    "description, " +
                    "status, " +
                    "editor_position_left, " +
                    "editor_position_top, " +
                    "is_inner, " +
                    "general_start_date, " +
                    "general_end_date, " +
                    "end_type, " +
                    "general_start_reaction, " +
                    "general_start_event, " +
                    "created, " +
                    "workflow_schema) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            int generatedId =
                    insertIntoAutoincrementMysqlTable(logger, "workflow_id", insertStatement,
                            workflow.getCompanyId(),
                            workflow.getShortname(),
                            workflow.getDescription(),
                            workflow.getStatus().getId(),
                            workflow.getEditorPositionLeft(),
                            workflow.getEditorPositionTop(),
                            BooleanUtils.toInteger(workflow.isInner()),
                            workflow.getGeneralStartDate(),
                            workflow.getGeneralEndDate(),
                            workflow.getEndType() == null ? 0 : workflow.getEndType().getId(),
                            workflow.getGeneralStartReaction() == null ? 0 : workflow.getGeneralStartReaction().getId(),
                            workflow.getGeneralStartEvent() == null ? 0 : workflow.getGeneralStartEvent().getId(),
                            new Date(),
                            workflow.getWorkflowSchema()
                            );
            if (generatedId == 0) {
                throw new RuntimeException();
            }
            workflow.setWorkflowId(generatedId);
		}
	}

    @Override
    public void deleteDependencies(int companyId, int workflowId, boolean clearTargetConditions) {
        if (companyId > 0 && workflowId > 0) {
            removeWorkflowLinkForMailings(companyId, workflowId);
            
            if (clearTargetConditions) {
                String sqlDeleteAll = "DELETE FROM workflow_dependency_tbl WHERE company_id = ? AND workflow_id = ?";
    
                update(logger, sqlDeleteAll, companyId, workflowId);
            } else {
                String sqlDeleteAllWithoutTargetCondition = "DELETE FROM workflow_dependency_tbl WHERE company_id = ? AND workflow_id = ? and type != ?";
                
                update(logger, sqlDeleteAllWithoutTargetCondition, companyId, workflowId, WorkflowDependencyType.TARGET_GROUP_CONDITION.getId());
            }
        }
    }

    @Override
    public void deleteDependencies(int companyId) {
        if (companyId > 0) {
            String sqlDeleteAll = "DELETE FROM workflow_dependency_tbl WHERE company_id = ?";
            update(logger, sqlDeleteAll, companyId);
        }
    }
    
    @Override
    public void deleteTargetConditionDependencies(int companyId, int workflowId) {
        update(logger, "DELETE FROM workflow_dependency_tbl WHERE company_id = ? AND workflow_id = ? AND type = ?",
                companyId, workflowId, WorkflowDependencyType.TARGET_GROUP_CONDITION.getId());
    }

    @Override
    public void setDependencies(int companyId, int workflowId, Set<WorkflowDependency> dependencies, boolean clearTargetConditions) {
        String sqlInsertNew = "INSERT INTO workflow_dependency_tbl (company_id, workflow_id, type, entity_id, entity_name) VALUES (?, ?, ?, ?, ?)";

        if (companyId > 0 && workflowId > 0) {
            deleteDependencies(companyId, workflowId, clearTargetConditions);

            if (CollectionUtils.isNotEmpty(dependencies)) {
                List<Object[]> sqlParametersList = new ArrayList<>(dependencies.size());

                for (WorkflowDependency dependency : dependencies) {
                    sqlParametersList.add(new Object[]{
                        companyId,
                        workflowId,
                        dependency.getType().getId(),
                        dependency.getEntityId(),
                        dependency.getEntityName()
                    });
                }

                batchupdate(logger, sqlInsertNew, sqlParametersList);
                addWorkflowLinkForDependencies(dependencies, workflowId, companyId);
            }
        }
    }

    private void addWorkflowLinkForDependencies(Set<WorkflowDependency> dependencies, int workflowId, int companyId) {
        String mailingIds = dependencies.stream()
                .filter(d -> WorkflowDependencyType.MAILING_DELIVERY.equals(d.getType()))
                .map(d -> String.valueOf(d.getEntityId()))
                .collect(Collectors.joining(","));

        if (!mailingIds.isBlank()) {
            String query = "UPDATE mailing_tbl SET workflow_id = ? WHERE company_id = ? AND mailing_id IN (" + mailingIds + ")";
            update(logger, query, workflowId, companyId);
        }
    }

    private void removeWorkflowLinkForMailings(int companyId, int workflowId) {
        if (companyId > 0 && workflowId > 0) {
        	List<Integer> referencedMailingIds = select(logger, "SELECT entity_id FROM workflow_dependency_tbl WHERE company_id = ? AND workflow_id = ? AND type = ?",
        		IntegerRowMapper.INSTANCE, companyId, workflowId, WorkflowDependencyType.MAILING_DELIVERY.getId());
        	
        	for (Integer mailingID : referencedMailingIds) {
            	// Detect if this mailing is still referenced by some other workflow
            	Integer otherReferencedWorkflowId = select(logger, "SELECT MAX(workflow_id) FROM workflow_dependency_tbl WHERE company_id = ? AND entity_id = ? AND type = ? AND workflow_id != ?",
            		Integer.class, companyId, mailingID, WorkflowDependencyType.MAILING_DELIVERY.getId(), workflowId);
            	if (otherReferencedWorkflowId != null) {
                    update(logger, "UPDATE mailing_tbl SET workflow_id = ? WHERE company_id = ? AND mailing_id = ?", otherReferencedWorkflowId, companyId, mailingID);
            	} else {
                    update(logger, "UPDATE mailing_tbl SET workflow_id = 0 WHERE company_id = ? AND mailing_id = ?", companyId, mailingID);
            	}
        	}
        }
    }

    @Override
    public void addDependency(int companyId, int workflowId, WorkflowDependency dependency) {
        if (companyId > 0 && workflowId > 0) {
            update(logger, "INSERT INTO workflow_dependency_tbl (company_id, workflow_id, type, entity_id, entity_name) VALUES (?, ?, ?, ?, ?)",
            	companyId, workflowId, dependency.getType().getId(), dependency.getEntityId(), dependency.getEntityName());
        }
    }

    @Override
    public List<Workflow> getWorkflowsOverview(Admin admin) {
        List<Object> parameters = new ArrayList<>();
		String query = "SELECT wf.workflow_id, wf.company_id, wf.shortname, wf.description, wf.status, wf.editor_position_left," +
				" wf.editor_position_top, wf.is_inner, wf.general_start_date, wf.general_end_date, wf.end_type, wf.general_start_reaction, " +
				" wf.general_start_event, wf.workflow_schema FROM workflow_tbl wf" +
				" WHERE wf.company_id = ? AND  wf.is_inner = 0 ";
		parameters.add(admin.getCompanyID());

		if (admin.getAdminID() > 0 && isDisabledMailingListsSupported()) {
			query += "AND wf.workflow_id NOT IN (SELECT workflow_id FROM workflow_dependency_tbl WHERE company_id = wf.company_id AND type = ? AND " +
					" entity_id IN (SELECT mailinglist_id FROM disabled_mailinglist_tbl WHERE admin_id = ?))";
			parameters.add(WorkflowDependencyType.MAILINGLIST.getId());
			parameters.add(admin.getAdminID());
		}

        query += getWorkflowOverviewAdditionalRestrictions(admin, parameters);

		query += " ORDER BY CASE WHEN  wf.status = 2 THEN 3 WHEN  wf.status = 3 THEN 2 WHEN  wf.status = 1 OR  wf.status = 4 THEN  wf.status END, " +
				" CASE WHEN  wf.status = 2 OR  wf.status = 3 OR  wf.status = 4 THEN  wf.general_start_date WHEN  wf.status = 1 THEN  wf.created END DESC";


		return select(logger, query, new WorkflowRowMapper(), parameters.toArray());
    }

    protected String getWorkflowOverviewAdditionalRestrictions(Admin admin, List<Object> params) {
        return StringUtils.EMPTY;
    }

    @Override
	@DaoUpdateReturnValueCheck
    public void changeWorkflowStatus(int workflowId, int companyId, WorkflowStatus newStatus) {
        update(logger, "UPDATE workflow_tbl SET status = ? WHERE workflow_id = ? AND company_id = ?",
                newStatus.getId(), workflowId, companyId);
    }

	@Override
	public List<Workflow> getWorkflowsToDeactivate(CompaniesConstraints constraints) {
        final String sqlGetWorkflowsToDeactivate = "SELECT * FROM workflow_tbl " +
                "WHERE (status = ? " +
                "OR status = ? AND ((general_end_date < CURRENT_TIMESTAMP AND end_type = ?) OR (general_end_date IS NULL AND end_type = ?)))" +
                DbUtilities.asCondition(" AND %s", constraints);

        final Object[] sqlParameters = new Object[] {
            WorkflowStatus.STATUS_TESTING.getId(),
            WorkflowStatus.STATUS_ACTIVE.getId(),
            WorkflowStop.WorkflowEndType.DATE.getId(),
            WorkflowStop.WorkflowEndType.AUTOMATIC.getId()
        };

		return select(logger, sqlGetWorkflowsToDeactivate, new WorkflowRowMapper(), sqlParameters);
	}

    @Override
    public List<Workflow> getWorkflows(Set<Integer> workflowIds, int companyId) {
        if (workflowIds == null || workflowIds.size() <= 0) {
            return new LinkedList<>();
        }
        String workflowIdsStr = StringUtils.join(new LinkedHashSet<>(workflowIds).toArray(), ", ");
        String sql = "SELECT * FROM workflow_tbl WHERE company_id = ? AND workflow_id in (" + workflowIdsStr + ") AND is_inner = 0";
        return select(logger, sql, new WorkflowRowMapper(), companyId);
    }

    @Override
    public List<Integer> getWorkflowIdsByAssignedMailingId(int companyId, int mailingId) {
        if (companyId <= 0 || mailingId <= 0) {
            return Collections.emptyList();
        }

        String sqlGetDependentIds = "SELECT w.workflow_id FROM workflow_tbl w " +
                "JOIN workflow_dependency_tbl dep ON dep.workflow_id = w.workflow_id AND dep.company_id = w.company_id " +
                "WHERE w.company_id = ? AND dep.type = ? AND dep.entity_id = ?";

        return select(logger, sqlGetDependentIds, IntegerRowMapper.INSTANCE, companyId, WorkflowDependencyType.MAILING_DELIVERY.getId(), mailingId);
    }

    @Override
    public int countCustomers(int companyId, int mailinglistId, String targetSQL) {
        int result;
        StringBuilder query = new StringBuilder("SELECT COUNT(DISTINCT cust.customer_id) count FROM customer_" + companyId + "_tbl cust ");
        if (StringUtils.isBlank(targetSQL)) {
            query.append("INNER JOIN customer_").append(companyId).append("_binding_tbl bind ON cust.customer_id = bind.customer_id WHERE bind.mailinglist_id = ?");
            result = selectInt(logger, query.toString(), mailinglistId);
        } else {
            query.append("WHERE (").append(targetSQL).append(")");
            result =  selectInt(logger, query.toString());
        }
        return result;
    }

    @Override
    public boolean validateDependency(int companyId, int workflowId, WorkflowDependency dependency, boolean strict) {
        if (companyId <= 0 || dependency == null) {
            return false;
        }

        StringBuilder sqlBuilder = new StringBuilder();
        List<Object> sqlParameters = new ArrayList<>();

        sqlBuilder.append("SELECT COUNT(*) FROM workflow_dependency_tbl dep ");

        if (!strict) {
            // When a non-strict mode selected ignore dependencies related to inactive workflows.
            sqlBuilder.append("LEFT JOIN workflow_tbl w ON w.company_id = dep.company_id AND w.workflow_id = dep.workflow_id ")
                    .append("AND w.status IN (?, ?) ");

            sqlParameters.add(WorkflowStatus.STATUS_ACTIVE.getId());
            sqlParameters.add(WorkflowStatus.STATUS_TESTING.getId());
        }

        sqlBuilder.append("WHERE dep.company_id = ? AND dep.type = ? ");

        sqlParameters.add(companyId);
        sqlParameters.add(dependency.getType().getId());

        if (dependency.getEntityId() > 0) {
            sqlBuilder.append("AND dep.entity_id = ?");
            sqlParameters.add(dependency.getEntityId());
        } else {
            sqlBuilder.append("AND dep.entity_name = ?");
            sqlParameters.add(dependency.getEntityName());
        }

        // When a valid identifier specified then ignore dependencies related to given workflow.
        if (workflowId > 0) {
            sqlBuilder.append(" AND dep.workflow_id <> ?");
            sqlParameters.add(workflowId);
        }

        // Make sure there are no existing dependencies matching given criteria.
        return selectInt(logger, sqlBuilder.toString(), sqlParameters.toArray()) == 0;
    }

    @Override
    public List<Workflow> getDependentWorkflows(int companyId, WorkflowDependency dependency, boolean exceptInactive) {
        return getDependentWorkflows(companyId, Collections.singletonList(dependency), exceptInactive);
    }

    @Override
    public List<Workflow> getDependentWorkflows(int companyId, Collection<WorkflowDependency> dependencies, boolean exceptInactive) {
        final Set<WorkflowDependency> filteredDependencies = new HashSet<>(CollectionUtils.emptyIfNull(dependencies));
        CollectionUtils.filter(filteredDependencies, PredicateUtils.notNullPredicate());
        if (companyId <= 0 || filteredDependencies.isEmpty()) {
            return Collections.emptyList();
        }

        StringBuilder sqlBuilder = new StringBuilder();
        List<Object> sqlParameters = new ArrayList<>();

        sqlBuilder.append("SELECT w.* FROM workflow_tbl w ")
                .append("JOIN workflow_dependency_tbl dep ON dep.workflow_id = w.workflow_id AND dep.company_id = w.company_id ")
                .append("WHERE w.company_id = ?  ");

        sqlParameters.add(companyId);

        sqlBuilder.append("AND ( ");
        boolean isFirstDependency = true;
        for(WorkflowDependency dependency : filteredDependencies) {
            if(!isFirstDependency){
                sqlBuilder.append(" OR");
            }
            isFirstDependency = false;

            sqlBuilder.append("(dep.type = ? ");
            sqlParameters.add(dependency.getType().getId());

            if (dependency.getEntityId() > 0) {
                sqlBuilder.append("AND dep.entity_id = ? ");
                sqlParameters.add(dependency.getEntityId());
            } else {
                sqlBuilder.append("AND dep.entity_name = ? ");
                sqlParameters.add(dependency.getEntityName());
            }
            sqlBuilder.append(" )");
        }
        sqlBuilder.append(" )");

        if (exceptInactive) {
            sqlBuilder.append("AND w.status IN (?, ?) ");
            sqlParameters.add(WorkflowStatus.STATUS_ACTIVE.getId());
            sqlParameters.add(WorkflowStatus.STATUS_TESTING.getId());
        }

        sqlBuilder.append("ORDER BY w.workflow_id DESC");

        return select(logger, sqlBuilder.toString(), new WorkflowRowMapper(), sqlParameters.toArray());
    }

    @Override
    public List<Workflow> getActiveWorkflowsTrackingProfileField(String column, int companyId) {
        return listActiveWorkflowsUsingDependencyName(column, companyId, WorkflowDependencyType.PROFILE_FIELD_HISTORY);
    }

    @Override
    public List<Workflow> getActiveWorkflowsUsingProfileField(String column, int companyId) {
        return listActiveWorkflowsUsingDependencyName(column, companyId,
                WorkflowDependencyType.PROFILE_FIELD, WorkflowDependencyType.PROFILE_FIELD_HISTORY);
    }

    private List<Workflow> listActiveWorkflowsUsingDependencyName(String name, int companyId, WorkflowDependencyType... types) {
        if (StringUtils.isBlank(name) || companyId <= 0) {
            return Collections.emptyList();
        }
        List<Object> params = new ArrayList<>(6);
        params.add(companyId);
        params.add(WorkflowStatus.STATUS_ACTIVE.getId());
        params.add(WorkflowStatus.STATUS_TESTING.getId());
        List.of(types).forEach(type -> params.add(type.getId()));
        params.add(name);

        return select(logger, "SELECT w.* FROM workflow_tbl w " +
                "JOIN workflow_dependency_tbl d ON d.workflow_id = w.workflow_id AND d.company_id = w.company_id " +
                "WHERE w.company_id = ? AND w.status IN (?, ?) " +
                (types.length > 0 ? "AND d.type IN (" + StringUtils.repeat("?",  ", ", types.length) + ") " : "") +
                "AND d.entity_name = ? " +
                "ORDER BY w.workflow_id DESC", new WorkflowRowMapper(), params.toArray());
    }

    @Override
    public List<Workflow> getActiveWorkflowsDrivenByProfileChange(int companyId, int mailingListId, String column, boolean isUseRules) {
        if (StringUtils.isBlank(column) || mailingListId <= 0 || companyId <= 0) {
            return Collections.emptyList();
        }

        String sqlGetDependentWorkflows = "SELECT w.* FROM workflow_tbl w " +
                "JOIN workflow_reaction_tbl rc ON rc.workflow_id = w.workflow_id AND rc.company_id = w.company_id " +
                "WHERE w.company_id = ? AND w.status IN (?, ?) AND rc.reaction_type = ? AND LOWER(rc.profile_column) = ? AND rc.mailinglist_id = ? " +
                "AND " + getIsEmpty("rc.rules_sql", !isUseRules) + " ORDER BY w.workflow_id DESC";

        Object[] sqlParameters = new Object[] {
                companyId,
                WorkflowStatus.STATUS_ACTIVE.getId(),
                WorkflowStatus.STATUS_TESTING.getId(),
                WorkflowReactionType.CHANGE_OF_PROFILE.getId(),
                column.trim().toLowerCase(),
                mailingListId
        };

        return select(logger, sqlGetDependentWorkflows, new WorkflowRowMapper(), sqlParameters);
    }
    
    @Override
    public List<Integer> getAllWorkflowUsedTargets(int companyId) {
        List<Integer> targetConditionsInUse = new ArrayList<>();
    
        List<Integer> targetIdsUsedByReactionDecl =
                select(logger, "SELECT target_id FROM workflow_reaction_decl_tbl WHERE company_id = ? AND target_id <> 0", IntegerRowMapper.INSTANCE, companyId);
        
        List<Integer> targetIdsUsedByOptimization =
                select(logger, "SELECT target_id FROM auto_optimization_tbl WHERE company_id = ? AND target_id <> 0", IntegerRowMapper.INSTANCE, companyId);
        
        List<Integer> targetIdsUsedByExportPredef =
                select(logger, "SELECT target_id FROM export_predef_tbl WHERE company_id = ? AND target_id <> 0", IntegerRowMapper.INSTANCE, companyId);
    
        targetConditionsInUse.addAll(targetIdsUsedByReactionDecl);
        targetConditionsInUse.addAll(targetIdsUsedByOptimization);
        targetConditionsInUse.addAll(targetIdsUsedByExportPredef);
        
        return targetConditionsInUse;
    }
    
    @Override
    public int bulkDeleteTargetCondition(List<Integer> targetIds, int companyId) {
        if(targetIds.size() > 0) {
        	/*
            String sql = "DELETE FROM dyn_target_tbl WHERE " +
                    makeBulkInClauseForInteger("target_id", targetIds) +
                    " AND company_id = ?";
            */
            
        	// TODO deleted=5 is a special marker for EMM-6545!
        	final String sql = "UPDATE dyn_target_tbl SET deleted=5 WHERE " + makeBulkInClauseForInteger("target_id", targetIds) + " AND company_id=?";
            
            return update(logger, sql, companyId);
        }
        
        return 0;
    }
    
    @Override
    public void removeMailingsTargetExpressions(int companyId, Set<Integer> mailingIds) {
        if (CollectionUtils.isNotEmpty(mailingIds)) {
            String sql = "UPDATE mailing_tbl SET target_expression = '', split_id = ? WHERE company_id = ? AND " +
                     makeBulkInClauseForInteger("mailing_id", mailingIds);
            update(logger, sql, Mailing.NONE_SPLIT_ID, companyId);
        }
    }
    
    @Override
    public void deactivateWorkflowScheduledReports(int workflowId, int companyId) {
        update(logger, "DELETE FROM workflow_report_schedule_tbl WHERE sent = 0 AND company_id = ? AND workflow_id = ?", companyId, workflowId);
    }
    
    @Override
    public void deleteWorkflowScheduledReports(int workflowId, int companyId) {
        update(logger, "DELETE FROM workflow_report_schedule_tbl WHERE company_id = ? AND workflow_id = ?", companyId, workflowId);
    }

    protected class WorkflowRowMapper implements RowMapper<Workflow> {
		@Override
		public Workflow mapRow(ResultSet resultSet, int row) throws SQLException {
            Workflow workflow = new Workflow();
            workflow.setWorkflowId(resultSet.getBigDecimal("workflow_id").intValue());
            workflow.setCompanyId(resultSet.getBigDecimal("company_id").intValue());
			workflow.setShortname(resultSet.getString("shortname"));
			workflow.setDescription(resultSet.getString("description"));
			workflow.setStatus(WorkflowStatus.fromId(resultSet.getBigDecimal("status").intValue(), false));
            workflow.setEditorPositionLeft(resultSet.getBigDecimal("editor_position_left").intValue());
            workflow.setEditorPositionTop(resultSet.getBigDecimal("editor_position_top").intValue());
			workflow.setInner(resultSet.getBigDecimal("is_inner").intValue() == 1);
            workflow.setGeneralStartDate(resultSet.getTimestamp("general_start_date"));
            workflow.setGeneralEndDate(resultSet.getTimestamp("general_end_date"));
            workflow.setEndType(WorkflowStop.WorkflowEndType.fromId(resultSet.getInt("end_type")));
            workflow.setGeneralStartReaction(WorkflowReactionType.fromId(resultSet.getBigDecimal("general_start_reaction").intValue()));
            workflow.setGeneralStartEvent(WorkflowStart.WorkflowStartEventType.fromId(resultSet.getBigDecimal("general_start_event").intValue()));
            workflow.setWorkflowSchema(resultSet.getString("workflow_schema"));
			return workflow;
		}
	}
	
    @Required
    public void setTargetService(ComTargetService targetService) {
        this.targetService = targetService;
    }
}
