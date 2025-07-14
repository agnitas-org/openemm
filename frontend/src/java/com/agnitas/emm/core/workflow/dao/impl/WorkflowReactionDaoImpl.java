/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.dao.impl;

import com.agnitas.beans.TrackableLink;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.recipient.dao.impl.HistoryUpdateType;
import com.agnitas.emm.core.recipient.service.RecipientProfileHistoryService;
import com.agnitas.emm.core.workflow.beans.WorkflowReaction;
import com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus;
import com.agnitas.emm.core.workflow.beans.WorkflowDependencyType;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionStep;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionStepDeclaration;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionStepInstance;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionType;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowReactionStepImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowReactionStepInstanceImpl;
import com.agnitas.emm.core.workflow.dao.WorkflowReactionDao;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils.Deadline;
import com.agnitas.beans.CompaniesConstraints;
import com.agnitas.beans.impl.CompanyStatus;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.util.DbUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class WorkflowReactionDaoImpl extends BaseDaoImpl implements WorkflowReactionDao {

    /**
     * Service handling profile field history.
     */
    private RecipientProfileHistoryService recipientProfileHistoryService;

    private WorkflowReactionDao selfRef; // for @Transactional invocations within the class

    @Override
    public boolean exists(int reactionId, int companyId) {
        String sqlGetCount = "SELECT COUNT(*) FROM workflow_reaction_tbl WHERE company_id = ? AND reaction_id = ?";
        return selectInt(sqlGetCount, companyId, reactionId) > 0;
    }

    @Override
    public WorkflowReaction getReaction(int reactionId, int companyId) {
        String sqlGetReaction = "SELECT * FROM workflow_reaction_tbl WHERE reaction_id = ? AND company_id = ?";
        return selectObjectDefaultNull(sqlGetReaction, new ReactionRowMapper(), reactionId, companyId);
    }

    @Override
    public List<WorkflowReaction> getReactionsToCheck(CompaniesConstraints constraints) {
        String sqlGetReactionsToCheck = "SELECT r.* FROM workflow_reaction_tbl r " +
                "JOIN company_tbl c ON c.company_id = r.company_id AND c.status NOT IN ('" + CompanyStatus.DELETED.getDbValue() + "', '" + CompanyStatus.DELETION_IN_PROGRESS.getDbValue() + "') " +
                "WHERE r.start_date <= CURRENT_TIMESTAMP AND r.active = 1 " +
                DbUtilities.asCondition("AND %s ", constraints, "r.company_id") +
                "ORDER BY reaction_id";
        return select(sqlGetReactionsToCheck, new ReactionRowMapper());
    }

    @Override
    public List<WorkflowReactionStep> getStepsToMake(CompaniesConstraints constraints) {
        String sqlGetStepsToMake = "SELECT s.case_id, s.step_id, s.previous_step_id, s.reaction_id, s.company_id, s.step_date, s.done, d.target_id, d.is_target_positive, d.mailing_id " +
            "FROM workflow_reaction_step_tbl s " +
            "JOIN workflow_reaction_decl_tbl d ON d.company_id = s.company_id AND d.reaction_id = s.reaction_id AND d.step_id = s.step_id " +
            "JOIN workflow_reaction_tbl r ON r.company_id = s.company_id AND r.reaction_id = s.reaction_id " +
            "JOIN workflow_tbl w ON w.company_id = s.company_id AND w.workflow_id = r.workflow_id AND w.status IN (?, ?) " +
            "JOIN company_tbl c ON c.company_id = s.company_id AND c.status NOT IN ('" + CompanyStatus.DELETED.getDbValue() + "', '" + CompanyStatus.DELETION_IN_PROGRESS.getDbValue() + "') " +
            "WHERE s.step_date <= CURRENT_TIMESTAMP AND s.done = 0 " + DbUtilities.asCondition("AND %s ", constraints, "s.company_id") +
            "ORDER BY s.company_id, s.reaction_id, s.case_id, s.step_date, s.step_id";
        return select(sqlGetStepsToMake, new StepRowMapper(), WorkflowStatus.STATUS_ACTIVE.getId(), WorkflowStatus.STATUS_TESTING.getId());
    }

    private void restoreReactionId(WorkflowReaction reaction) {
        if (reaction.getReactionId() == 0) {
            reaction.setReactionId(getReactionId(reaction.getWorkflowId(), reaction.getCompanyId()));
        }
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void saveReaction(WorkflowReaction reaction) {
        List<Object> parameters = new ArrayList<>();
        String query = "UPDATE workflow_reaction_tbl " +
            "SET active = ?, " +
            "start_date = ?, " +
            "admin_timezone = ?, " +
            "reaction_type = ?, " +
            "once = ?, " +
            "mailinglist_id = ?, " +
            "trigger_mailing_id = ?, " +
            "trigger_link_id = ?, " +
            "profile_column = ?, " +
            "rules_sql = ? " +
            "WHERE company_id = ? " +
            "AND workflow_id = ?";

        parameters.add(reaction.isActive() ? 1 : 0);
        parameters.add(reaction.getStartDate());
        parameters.add(getTimezoneId(reaction));
        parameters.add(reaction.getReactionType() == null ? 0 : reaction.getReactionType().getId());
        parameters.add(reaction.isOnce() ? 1 : 0);
        parameters.add(reaction.getMailinglistId());
        parameters.add(reaction.getTriggerMailingId());
        parameters.add(reaction.getTriggerLinkId());
        parameters.add(reaction.getProfileColumn());
        parameters.add(reaction.getRulesSQL());

        parameters.add(reaction.getCompanyId());
        parameters.add(reaction.getWorkflowId());

        int touched = update(query, parameters.toArray());

        // Update an existing or create a new one
        if (touched > 0) {
            restoreReactionId(reaction);
        } else {
            createReaction(reaction);
        }
    }

    protected void createReaction(WorkflowReaction reaction) {
        int newId;

        List<Object> params = new ArrayList<>();
        params.add(reaction.getWorkflowId());
        params.add(reaction.getCompanyId());
        params.add(reaction.getMailinglistId());
        params.add(reaction.getTriggerMailingId());
        params.add(reaction.getTriggerLinkId());
        params.add(reaction.isActive() ? 1 : 0);
        params.add(reaction.isOnce() ? 1 : 0);
        params.add(reaction.getStartDate());
        params.add(getTimezoneId(reaction));
        params.add(reaction.getReactionType() == null ? 0 : reaction.getReactionType().getId());
        params.add(reaction.getProfileColumn());
        params.add(reaction.getRulesSQL());

        if (isOracleDB()) {
            newId = selectInt("SELECT workflow_reaction_tbl_seq.NEXTVAL FROM dual");
            params.add(0, newId);
            update("INSERT INTO workflow_reaction_tbl (reaction_id, workflow_id, company_id, mailinglist_id, trigger_mailing_id, " +
                "trigger_link_id, active, once, start_date, admin_timezone, reaction_type, profile_column, rules_sql) VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", params.toArray());
        } else {
            String insertStatement = "INSERT INTO workflow_reaction_tbl (workflow_id, company_id, mailinglist_id, trigger_mailing_id, trigger_link_id, " +
                "active, once, start_date, admin_timezone, reaction_type, profile_column, rules_sql) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            newId = insertIntoAutoincrementMysqlTable("reaction_id", insertStatement, params.toArray());
        }

        reaction.setReactionId(newId);
    }

    private String getTimezoneId(WorkflowReaction reaction) {
        TimeZone timezone = reaction.getAdminTimezone();

        if (timezone == null) {
            return "Europe/Berlin";
        }

        return timezone.getID();
    }

    @Transactional
    @Override
    @DaoUpdateReturnValueCheck
    public void saveReactionStepDeclarations(List<WorkflowReactionStepDeclaration> declarations, int reactionId, int companyId) {
        if (exists(reactionId, companyId)) {
            deleteReactionStepDeclarations(reactionId, companyId);
            for (WorkflowReactionStepDeclaration declaration : declarations) {
                saveReactionStepDeclaration(declaration, reactionId, companyId);
            }
        }
    }

    protected void saveReactionStepDeclaration(WorkflowReactionStepDeclaration declaration, int reactionId, int companyId) {
        String sqlSaveStepDeclaration = "INSERT INTO workflow_reaction_decl_tbl (" +
            "step_id, previous_step_id, reaction_id, company_id, deadline_relative, deadline_hours, deadline_minutes, target_id, is_target_positive, mailing_id" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        List<Object> sqlParameters = new ArrayList<>(10);
        Deadline deadline = declaration.getDeadline();

        if (!deadline.isRelative()) {
            throw new IllegalArgumentException("Fixed (absolute) deadlines are not allowed within action-based campaign");
        }

        sqlParameters.add(declaration.getStepId());
        sqlParameters.add(declaration.getPreviousStepId());
        sqlParameters.add(reactionId);
        sqlParameters.add(companyId);
        sqlParameters.add(deadline.getValue());
        sqlParameters.add(deadline.getHours());
        sqlParameters.add(deadline.getMinutes());
        sqlParameters.add(declaration.getTargetId());
        sqlParameters.add(declaration.isTargetPositive() ? 1 : 0);
        sqlParameters.add(declaration.getMailingId());

        update(sqlSaveStepDeclaration, sqlParameters.toArray());

        declaration.setCompanyId(companyId);
        declaration.setReactionId(reactionId);
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void deactivateWorkflowReactions(int workflowId, int companyId, boolean keepReactionLog) {
        int reactionId = getReactionId(workflowId, companyId);
        if (reactionId > 0) {
            selfRef.deactivateReaction(reactionId, companyId, keepReactionLog);
        }
    }

    @Override
    public void updateStepsDeclarations(List<WorkflowReactionStepDeclaration> steps, int reactionId, int companyId) {
        List<Object[]> params = steps.stream().map(step -> new Object[]{
                step.getMailingId(),
                step.getTargetId(),
                companyId,
                reactionId,
                step.getStepId(),
                step.getPreviousStepId()
        }).collect(Collectors.toList());

        batchupdate("UPDATE workflow_reaction_decl_tbl SET mailing_id = ?, target_id = ? " +
                "WHERE company_id = ? AND reaction_id = ? AND step_id = ? AND previous_step_id = ?", params);
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void activateWorkflowReactions(int workflowId, int companyId) {
        int reactionId = getReactionId(workflowId, companyId);
        if (reactionId > 0) {
            activateReaction(reactionId, companyId);
        }
    }

    @Override
    public void activateReaction(int reactionId, int companyId) {
        update("UPDATE workflow_reaction_tbl SET active = 1 WHERE company_id = ? AND reaction_id = ?", companyId, reactionId);
    }

    @Transactional
    @Override
    @DaoUpdateReturnValueCheck
    public void deactivateReaction(int reactionId, int companyId, boolean keepReactionLog) {
        update("UPDATE workflow_reaction_tbl SET active = 0 WHERE company_id = ? AND reaction_id = ?", companyId, reactionId);
        if (!keepReactionLog) {
            clearReactionLog(reactionId, companyId, true);
        }
    }

    protected void deleteReactionStepDeclarations(int reactionId, int companyId) {
        update("DELETE FROM workflow_reaction_decl_tbl WHERE company_id = ? AND reaction_id = ?", companyId, reactionId);
    }

    protected void clearReactionLog(int reactionId, int companyId, boolean preserveTriggerStepLog) {
        String sqlPackTriggerStepLog = "INSERT INTO workflow_reaction_out_tbl (case_id, step_id, reaction_id, company_id, customer_id, step_date) " +
            "SELECT 0, 0, reaction_id, company_id, customer_id, MAX(step_date) FROM workflow_reaction_out_tbl " +
            "WHERE company_id = ? AND reaction_id = ? AND step_id = 0 AND case_id <> 0 " +
            "GROUP BY company_id, reaction_id, customer_id";

        String sqlClearSteps = "DELETE FROM workflow_reaction_step_tbl WHERE company_id = ? AND reaction_id = ?";
        String sqlClearRecipients = "DELETE FROM workflow_reaction_out_tbl WHERE company_id = ? AND reaction_id = ?";

        if (preserveTriggerStepLog) {
            String sqlClearOvertakenEntries = "DELETE FROM workflow_reaction_out_tbl WHERE company_id = ? AND reaction_id = ? AND case_id = 0 AND step_id = 0 AND customer_id IN (" +
                    "SELECT x.customer_id FROM (SELECT customer_id FROM workflow_reaction_out_tbl WHERE company_id = ? AND reaction_id = ? AND step_id = 0 AND case_id <> 0) x" +
                    ")";
            sqlClearRecipients += " AND (step_id <> 0 OR case_id <> 0)";
            update(sqlClearOvertakenEntries, companyId, reactionId, companyId, reactionId);
            update(sqlPackTriggerStepLog, companyId, reactionId);
        }

        update(sqlClearSteps, companyId, reactionId);
        update(sqlClearRecipients, companyId, reactionId);
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void deleteWorkflowReactions(int workflowId, int companyId) {
        int reactionId = getReactionId(workflowId, companyId);
        if (reactionId > 0) {
            selfRef.deleteReaction(reactionId, companyId);
        }
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void deleteReactions(int companyId) {
        update("DELETE FROM workflow_reaction_out_tbl WHERE company_id = ?", companyId);
        update("DELETE FROM workflow_reaction_step_tbl WHERE company_id = ?", companyId);
        update("DELETE FROM workflow_reaction_decl_tbl WHERE company_id = ?", companyId);
        update("DELETE FROM workflow_reaction_tbl WHERE company_id = ?", companyId);
    }

    @Transactional
    @Override
    @DaoUpdateReturnValueCheck
    public void deleteReaction(int reactionId, int companyId) {
        clearReactionLog(reactionId, companyId, false);
        deleteReactionStepDeclarations(reactionId, companyId);
        update("DELETE FROM workflow_reaction_tbl WHERE company_id = ? AND reaction_id = ?", companyId, reactionId);
    }

    @Override
    public List<Integer> getClickedRecipients(WorkflowReaction reaction, boolean excludeLoggedReactions) {
        List<Object> sqlParameters = new ArrayList<>();

        sqlParameters.add(reaction.getTriggerMailingId());

        String sqlGetRecipients = "SELECT DISTINCT(rlog.customer_id) FROM rdirlog_" + reaction.getCompanyId() + "_tbl rlog " +
                "WHERE rlog.mailing_id = ?";

        if (reaction.getReactionType() == WorkflowReactionType.CLICKED_LINK) {
            sqlGetRecipients += " AND rlog.url_id = ?";
            sqlParameters.add(reaction.getTriggerLinkId());
        }

        sqlParameters.add(reaction.getStartDate());

        if (excludeLoggedReactions) {
            sqlParameters.add(reaction.getReactionId());

            if (reaction.isOnce()) {
                sqlGetRecipients += " AND rlog.timestamp > ? AND NOT EXISTS (" +
                        "SELECT 1 FROM workflow_reaction_out_tbl rout " +
                        "WHERE rlog.customer_id = rout.customer_id AND rout.reaction_id = ? AND rout.step_id = 0" +
                        ")";
            } else {
                sqlGetRecipients += " AND rlog.timestamp > (" +
                        "SELECT COALESCE(MAX(rout.step_date), ?) FROM workflow_reaction_out_tbl rout " +
                        "WHERE rlog.customer_id = rout.customer_id AND rout.reaction_id = ? AND rout.step_id = 0" +
                        ")";
            }
        } else {
            sqlGetRecipients += " AND rlog.timestamp > ?";
        }

        if (reaction.getMailinglistId() > 0) {
            sqlGetRecipients += " AND EXISTS (SELECT 1 FROM customer_" + reaction.getCompanyId() + "_binding_tbl bind " +
                    "WHERE bind.customer_id = rlog.customer_id AND bind.mailinglist_id = ? AND bind.user_status = ?)";

            sqlParameters.add(reaction.getMailinglistId());
            sqlParameters.add(UserStatus.Active.getStatusCode());
        }

        return select(sqlGetRecipients, IntegerRowMapper.INSTANCE, sqlParameters.toArray());
    }

    @Override
    public List<Integer> getOpenedRecipients(WorkflowReaction reaction, boolean excludeLoggedReactions) {
        List<Object> sqlParameters = new ArrayList<>();

        String sqlGetRecipients = "SELECT DISTINCT(plog.customer_id) FROM onepixellog_" + reaction.getCompanyId() + "_tbl plog " +
                "WHERE plog.mailing_id = ?";

        sqlParameters.add(reaction.getTriggerMailingId());

        if (excludeLoggedReactions) {
            sqlParameters.add(reaction.getReactionId());

            if (reaction.isOnce()) {
                sqlGetRecipients += " AND NOT EXISTS (" +
                        "SELECT 1 FROM workflow_reaction_out_tbl rout " +
                        "WHERE rout.customer_id = plog.customer_id AND rout.reaction_id = ? AND rout.step_id = 0" +
                        ")";
            } else {
                sqlGetRecipients += " AND plog.open_count > (" +
                        "SELECT COUNT(*) FROM workflow_reaction_out_tbl rout " +
                        "WHERE rout.customer_id = plog.customer_id AND rout.reaction_id = ? AND rout.step_id = 0" +
                        ")";
            }
        }

        if (reaction.getMailinglistId() > 0) {
            sqlGetRecipients += " AND EXISTS (SELECT 1 FROM customer_" + reaction.getCompanyId() + "_binding_tbl bind " +
                    "WHERE bind.customer_id = plog.customer_id AND bind.mailinglist_id = ? AND bind.user_status = ?)";

            sqlParameters.add(reaction.getMailinglistId());
            sqlParameters.add(UserStatus.Active.getStatusCode());
        }

        return select(sqlGetRecipients, IntegerRowMapper.INSTANCE, sqlParameters.toArray());
    }

    @Override
    public List<Integer> getRecipientsWithChangedProfile(WorkflowReaction reaction, boolean excludeLoggedReactions) {
        final int companyId = reaction.getCompanyId();
        final String column = reaction.getProfileColumn();
        final String sqlRules = StringUtils.trimToNull(reaction.getRulesSQL());

        StringBuilder sqlBuilder = new StringBuilder();

        if (recipientProfileHistoryService.isProfileFieldHistoryEnabled(companyId)) {
            List<Object> sqlParameters = new ArrayList<>();

            sqlBuilder.append("SELECT DISTINCT(cust.customer_id)");
            sqlBuilder.append(" FROM customer_").append(companyId).append("_tbl cust, hst_customer_").append(companyId).append("_tbl hst");
            sqlBuilder.append(" WHERE hst.customer_id = cust.customer_id");
            sqlBuilder.append(" AND hst.change_date > ?");
            sqlBuilder.append(" AND LOWER(hst.name) = LOWER(?)");
            sqlBuilder.append(" AND hst.change_type IN (?, ?)");

            if (sqlRules != null) {
                sqlBuilder.append(" AND ").append(sqlRules);
            }

            sqlParameters.add(reaction.getStartDate());
            sqlParameters.add(column);
            sqlParameters.add(HistoryUpdateType.UPDATE.typeCode);
            sqlParameters.add(HistoryUpdateType.INSERT.typeCode);

            if (excludeLoggedReactions) {
                sqlParameters.add(reaction.getReactionId());

                sqlBuilder.append(" AND NOT EXISTS (");
                sqlBuilder.append("SELECT rout.customer_id FROM workflow_reaction_out_tbl rout ");

                if (reaction.isOnce()) {
                    sqlBuilder.append("WHERE rout.reaction_id = ? AND rout.step_id = 0 AND rout.customer_id = cust.customer_id");
                } else {
                    // Exclude customer from "reacted" set if
                    // - either a reaction is registered for that profile change (reaction's timestamp is after profile change's timestamp),
                    // - or some valuable (having incoming recipients) step instance(s) for that customer is(are) in the future (or marked as undone).
                    sqlBuilder.append("LEFT JOIN workflow_reaction_step_tbl stp ON stp.reaction_id = rout.reaction_id AND stp.case_id = rout.case_id ");
                    sqlBuilder.append("WHERE rout.reaction_id = ? AND rout.customer_id = hst.customer_id ");
                    sqlBuilder.append("AND (rout.step_id = 0 AND rout.step_date > hst.change_date OR rout.step_id = stp.previous_step_id AND (stp.done = 0 OR stp.step_date > hst.change_date))");
                }
                sqlBuilder.append(")");
            }

            if (reaction.getMailinglistId() > 0) {
                sqlBuilder.append(" AND EXISTS (SELECT 1 FROM customer_").append(reaction.getCompanyId()).append("_binding_tbl bind ")
                        .append("WHERE bind.customer_id = cust.customer_id AND bind.mailinglist_id = ? AND bind.user_status = ?)");

                sqlParameters.add(reaction.getMailinglistId());
                sqlParameters.add(UserStatus.Active.getStatusCode());
            }

            return select(sqlBuilder.toString(), IntegerRowMapper.INSTANCE, sqlParameters.toArray());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Integer> getRecipientsWithChangedBinding(WorkflowReaction reaction, boolean excludeLoggedReactions) {
        String status;
        String joinType = "LEFT";

        switch (reaction.getReactionType()) {
            case OPT_IN:
                status = String.valueOf(UserStatus.Active.getStatusCode());
                break;

            case OPT_OUT:
                status = UserStatus.UserOut.getStatusCode() + ", " + UserStatus.AdminOut.getStatusCode();
                break;

            case WAITING_FOR_CONFIRM:
                status = String.valueOf(UserStatus.WaitForConfirm.getStatusCode());
                // Also react on status "5" values, which have been set via webservice with no later status change (no HST entry)
                joinType = "FULL";
                break;

            default:
                return Collections.emptyList();
        }

        int companyId = reaction.getCompanyId();

        // Get recipients with changed bindings.
		StringBuilder sqlGetRecipientsBuilder = new StringBuilder();
        List<Object> sqlParameters = new ArrayList<>();

		sqlGetRecipientsBuilder
			.append("SELECT DISTINCT(bind.customer_id) FROM customer_").append(companyId).append("_binding_tbl bind ")
			.append(joinType).append(" JOIN hst_customer_").append(companyId).append("_binding_tbl hst ")
			.append("ON bind.customer_id = hst.customer_id AND bind.mailinglist_id = hst.mailinglist_id AND hst.user_status NOT IN (").append(status).append(") ")
			.append("WHERE COALESCE(hst.timestamp_change, bind.creation_date) > ? ")
			.append("AND bind.user_status IN (").append(status).append(")");

        sqlParameters.add(reaction.getStartDate());

		if (reaction.getMailinglistId() > 0) {
		    sqlGetRecipientsBuilder.append(" AND bind.mailinglist_id = ?");
		    sqlParameters.add(reaction.getMailinglistId());
        }

		if (excludeLoggedReactions) {
            sqlParameters.add(reaction.getReactionId());

			sqlGetRecipientsBuilder.append(" AND NOT EXISTS (");
            sqlGetRecipientsBuilder.append("SELECT rout.customer_id FROM workflow_reaction_out_tbl rout ")
                    .append("WHERE rout.reaction_id = ? AND rout.customer_id = bind.customer_id");

            if (!reaction.isOnce() || reaction.getReactionType() == WorkflowReactionType.OPT_IN) {
                sqlGetRecipientsBuilder.append(" AND rout.step_date > COALESCE(hst.timestamp_change, bind.creation_date)");
            }

			sqlGetRecipientsBuilder.append(")");
        }

        if (joinType.equals("FULL") && !isOracleDB()) {
            // Duplicate parameters for UNION that combines two similar SELECT clauses having same placeholders.
            sqlParameters.addAll(Arrays.asList(sqlParameters.toArray()));
            return select(convertFullJoinToUnion(sqlGetRecipientsBuilder), IntegerRowMapper.INSTANCE, sqlParameters.toArray());
        } else {
            return select(sqlGetRecipientsBuilder.toString(), IntegerRowMapper.INSTANCE, sqlParameters.toArray());
        }
    }

    private String convertFullJoinToUnion(StringBuilder sqlGetRecipientsBuilder) {
        return convertFullJoinToUnion(sqlGetRecipientsBuilder.toString());
    }

    private String convertFullJoinToUnion(String sqlGetRecipients) {
        if (sqlGetRecipients.contains("FULL JOIN")) {
            String leftJoin = sqlGetRecipients.replace("FULL JOIN", "LEFT JOIN");
            String rightJoin = sqlGetRecipients.replace("FULL JOIN","RIGHT JOIN");

            return leftJoin + " UNION " + rightJoin;
        }
        return sqlGetRecipients;
    }

    @Override
    @Transactional
    public void trigger(WorkflowReaction reaction, List<Integer> recipients) {
        if (CollectionUtils.isEmpty(recipients)) {
            return;
        }

        Date reactionDate = new Date();
        int newCaseId = getMaxCaseId(reaction) + 1;

        createNewCase(reaction, newCaseId, reactionDate);
        setTriggerStepRecipients(newCaseId, reaction.getReactionId(), reaction.getCompanyId(), recipients, reactionDate);
    }

    @Override
    public void setStepDone(WorkflowReactionStepInstance step) {
        selfRef.setStepDone(step, 0, false, null);
    }

    @Override
    public void setStepDone(WorkflowReactionStepInstance step, String sqlTargetExpression) {
        selfRef.setStepDone(step, 0, false, sqlTargetExpression);
    }

    @Transactional
    @Override
    public void setStepDone(WorkflowReactionStepInstance step, int mailingListId, boolean requireActiveBinding, String sqlTargetExpression) {
        String sqlMarkAsDone = "UPDATE workflow_reaction_step_tbl SET done = 1 " +
            "WHERE company_id = ? AND reaction_id = ? AND step_id = ? AND case_id = ? AND done = 0";

        if (update(sqlMarkAsDone, step.getCompanyId(), step.getReactionId(), step.getStepId(), step.getCaseId()) > 0) {
            List<Object> sqlParameters = new ArrayList<>();
            StringBuilder builder = new StringBuilder();

            builder.append("INSERT INTO workflow_reaction_out_tbl (case_id, step_id, reaction_id, company_id, customer_id, step_date) ")
                    .append("SELECT rout.case_id, ?, rout.reaction_id, rout.company_id, rout.customer_id, CURRENT_TIMESTAMP FROM workflow_reaction_out_tbl rout ")
                    .append("JOIN customer_").append(step.getCompanyId()).append("_tbl cust ON cust.customer_id = rout.customer_id ")
                    .append("WHERE rout.company_id = ? AND rout.reaction_id = ? AND rout.step_id = ? AND rout.case_id = ? ");

            sqlParameters.add(step.getStepId());
            sqlParameters.add(step.getCompanyId());
            sqlParameters.add(step.getReactionId());
            sqlParameters.add(step.getPreviousStepId());
            sqlParameters.add(step.getCaseId());

            if (mailingListId > 0) {
                builder.append("AND cust.customer_id IN (");
                builder.append("SELECT customer_id FROM customer_").append(step.getCompanyId()).append("_binding_tbl bind WHERE bind.mailinglist_id = ? ");

                sqlParameters.add(mailingListId);
                if (requireActiveBinding) {
                    builder.append("AND bind.user_status = ?");
                    sqlParameters.add(UserStatus.Active.getStatusCode());
                }

                builder.append(")");
            }

            if (StringUtils.isNotBlank(sqlTargetExpression)) {
                builder.append(" AND (").append(sqlTargetExpression).append(")");
            }

            update(builder.toString(), sqlParameters.toArray());
        }

        step.setDone(true);
    }

    @Override
    public void setUselessStepsDone(CompaniesConstraints constraints) {
        String sqlSetDone = "UPDATE workflow_reaction_step_tbl SET done = 1 WHERE company_id = ? AND reaction_id = ? AND case_id = ? AND step_id = ?";

        String sqlGetUselessSteps = "SELECT dst.company_id, dst.reaction_id, dst.case_id, dst.step_id FROM workflow_reaction_step_tbl dst " +
            "JOIN workflow_reaction_step_tbl src " +
            "ON src.company_id = dst.company_id AND src.reaction_id = dst.reaction_id AND src.case_id = dst.case_id AND src.step_id = dst.previous_step_id " +
            "LEFT JOIN workflow_reaction_out_tbl wro " +
            "ON wro.company_id = dst.company_id AND wro.reaction_id = dst.reaction_id AND wro.case_id = dst.case_id AND wro.step_id = dst.previous_step_id " +
            "WHERE src.done = 1 AND dst.done = 0 AND wro.step_id IS NULL" +
            DbUtilities.asCondition(" AND %s", constraints, "dst.company_id");

        List<Object[]> sqlSetDoneParameters = null;

        while (sqlSetDoneParameters == null || sqlSetDoneParameters.size() > 0) {
            sqlSetDoneParameters = select(sqlGetUselessSteps, new UselessStepRowMapper());

            if (sqlSetDoneParameters.size() > 0) {
                batchupdate(sqlSetDone, sqlSetDoneParameters);
            }
        }
    }

    @Override
    public List<Integer> getStepRecipients(WorkflowReactionStepInstance step) {
        String sqlGetRecipients = "SELECT customer_id FROM workflow_reaction_out_tbl WHERE company_id = ? AND reaction_id = ? AND case_id = ? AND step_id = ?";
        return select(sqlGetRecipients, IntegerRowMapper.INSTANCE, step.getCompanyId(), step.getReactionId(), step.getCaseId(), step.getStepId());
    }

    protected void setTriggerStepRecipients(int caseId, int reactionId, int companyId, List<Integer> recipients, Date reactionDate) {
        String sqlClearLog = "DELETE FROM workflow_reaction_out_tbl WHERE company_id = ? AND reaction_id = ? AND case_id = ? AND step_id = 0";
        update(sqlClearLog, companyId, reactionId, caseId);

        String sqlLogRecipients = "INSERT INTO workflow_reaction_out_tbl (case_id, step_id, reaction_id, company_id, customer_id, step_date) " +
            "VALUES (?, 0, ?, ?, ?, ?)";

        if (CollectionUtils.isNotEmpty(recipients)) {
            List<Object[]> sqlParameters = new ArrayList<>(recipients.size());
            for (int recipientId : new HashSet<>(recipients)) {
                sqlParameters.add(new Object[]{caseId, reactionId, companyId, recipientId, reactionDate});
            }
            batchupdate(sqlLogRecipients, sqlParameters);
        }
    }

    protected void createNewCase(WorkflowReaction reaction, int newCaseId, Date reactionDate) {
        List<WorkflowReactionStepInstance> instances = getNewStepInstances(reaction, newCaseId, reactionDate);

        if (instances.size() > 0) {
            String sqlStoreNewStepInstances = "INSERT INTO workflow_reaction_step_tbl (case_id, step_id, previous_step_id, reaction_id, company_id, step_date, done) " +
                "VALUES (?, ?, ?, ?, ?, ?, 0)";
            List<Object[]> sqlParameters = new ArrayList<>(instances.size());

            for (WorkflowReactionStepInstance instance : instances) {
                sqlParameters.add(new Object[]{
                    instance.getCaseId(),
                    instance.getStepId(),
                    instance.getPreviousStepId(),
                    instance.getReactionId(),
                    instance.getCompanyId(),
                    instance.getDate()
                });
            }

            batchupdate(sqlStoreNewStepInstances, sqlParameters);
        }
    }

    private List<WorkflowReactionStepInstance> getNewStepInstances(WorkflowReaction reaction, int newCaseId, Date reactionDate) {
        NewStepInstanceFromDeclarationRowMapper mapper = new NewStepInstanceFromDeclarationRowMapper(reactionDate, reaction.getAdminTimezone(), newCaseId);

        String sqlGetNewStepInstances = "SELECT step_id, previous_step_id, reaction_id, company_id, deadline_relative, deadline_hours, deadline_minutes " +
            "FROM workflow_reaction_decl_tbl " +
            "WHERE company_id = ? AND reaction_id = ?";

        return select(sqlGetNewStepInstances, mapper, reaction.getCompanyId(), reaction.getReactionId());
    }

    private int getMaxCaseId(WorkflowReaction reaction) {
        String sqlGetMaxCaseId = "SELECT COALESCE(MAX(case_id), 0) " +
                "FROM workflow_reaction_out_tbl " +
                "WHERE company_id = ? AND reaction_id = ? AND step_id = 0";

        return selectInt(sqlGetMaxCaseId, reaction.getCompanyId(), reaction.getReactionId());
    }

    @Override
    public int getReactionId(int workflowId, int companyId) {
        String sqlGetReactionId = "SELECT reaction_id FROM workflow_reaction_tbl WHERE workflow_id = ? AND company_id = ?";
        return selectInt(sqlGetReactionId, workflowId, companyId);
    }
    
    @Override
    public boolean isLinkUsedInActiveWorkflow(TrackableLink link) {
        String query = "SELECT " + (isOracleDB() ? "1 FROM DUAL WHERE" : "") +
                " EXISTS (SELECT 1 FROM dyn_target_tbl t, workflow_reaction_tbl wr" +
                "    WHERE (t.target_id IN (" +
                "        SELECT wd.entity_id FROM workflow_tbl w JOIN workflow_dependency_tbl wd ON w.workflow_id = wd.workflow_id" +
                "        WHERE w.status IN (?, ?, ?) AND wd.type = ?)" +
                "    AND eql LIKE '%CLICKED LINK " + link.getId() + " IN MAILING " + link.getMailingID() + "%')) " +
                "OR EXISTS (SELECT 1 FROM workflow_reaction_tbl WHERE trigger_link_id = ? AND active = 1)";
        
        return selectInt(query,
                WorkflowStatus.STATUS_ACTIVE.getId(),
                WorkflowStatus.STATUS_TESTING.getId(),
                WorkflowStatus.STATUS_PAUSED.getId(),
                WorkflowDependencyType.TARGET_GROUP_CONDITION.getId(), link.getId()) == 1;
    }

    public void setRecipientProfileHistoryService(final RecipientProfileHistoryService service) {
        this.recipientProfileHistoryService = service;
    }

    public final void setSelfRef(final WorkflowReactionDao dao) {
    	this.selfRef = Objects.requireNonNull(dao, "Self reference is null");
    }

    private static class ReactionRowMapper implements RowMapper<WorkflowReaction> {
        @Override
        public WorkflowReaction mapRow(ResultSet resultSet, int row) throws SQLException {
            WorkflowReaction reaction = new WorkflowReaction();
            reaction.setWorkflowId(resultSet.getInt("workflow_id"));
            reaction.setCompanyId(resultSet.getInt("company_id"));
            reaction.setReactionId(resultSet.getInt("reaction_id"));
            reaction.setMailinglistId(resultSet.getInt("mailinglist_id"));
            reaction.setTriggerMailingId(resultSet.getInt("trigger_mailing_id"));
            reaction.setTriggerLinkId(resultSet.getInt("trigger_link_id"));
            reaction.setActive(resultSet.getInt("active") == 1);
            reaction.setOnce(resultSet.getInt("once") == 1);
            reaction.setStartDate(resultSet.getTimestamp("start_date"));
            reaction.setAdminTimezone(TimeZone.getTimeZone(resultSet.getString("admin_timezone")));
            reaction.setReactionType(WorkflowReactionType.fromId(resultSet.getInt("reaction_type")));
            reaction.setProfileColumn(resultSet.getString("profile_column"));
            reaction.setRulesSQL(resultSet.getString("rules_sql"));
            return reaction;
        }
    }

    private static class StepRowMapper implements RowMapper<WorkflowReactionStep> {
        @Override
        public WorkflowReactionStep mapRow(ResultSet rs, int i) throws SQLException {
            WorkflowReactionStep step = new WorkflowReactionStepImpl();

            step.setCaseId(rs.getInt("case_id"));
            step.setStepId(rs.getInt("step_id"));
            step.setPreviousStepId(rs.getInt("previous_step_id"));
            step.setReactionId(rs.getInt("reaction_id"));
            step.setCompanyId(rs.getInt("company_id"));
            step.setDate(rs.getTimestamp("step_date"));
            step.setDone(rs.getInt("done") == 1);
            step.setTargetId(rs.getInt("target_id"));
            step.setTargetPositive(rs.getInt("is_target_positive") == 1);
            step.setMailingId(rs.getInt("mailing_id"));

            return step;
        }
    }

    private static class NewStepInstanceFromDeclarationRowMapper implements RowMapper<WorkflowReactionStepInstance> {
        private Date reactionDate;
        private TimeZone timezone;
        private int caseId;

        public NewStepInstanceFromDeclarationRowMapper(Date reactionDate, TimeZone timezone, int caseId) {
            this.reactionDate = reactionDate;
            this.timezone = timezone;
            this.caseId = caseId;
        }

        @Override
        public WorkflowReactionStepInstance mapRow(ResultSet rs, int i) throws SQLException {
            WorkflowReactionStepInstance step = new WorkflowReactionStepInstanceImpl();

            step.setCaseId(caseId);
            step.setStepId(rs.getInt("step_id"));
            step.setPreviousStepId(rs.getInt("previous_step_id"));
            step.setReactionId(rs.getInt("reaction_id"));
            step.setCompanyId(rs.getInt("company_id"));
            step.setDate(getDeadlineAsDate(rs));
            step.setDone(false);

            return step;
        }

        private Date getDeadlineAsDate(ResultSet rs) throws SQLException {
            long ms = rs.getLong("deadline_relative");
            int hours = rs.getInt("deadline_hours");
            int minutes = rs.getInt("deadline_minutes");

            return Deadline.toDate(reactionDate, new Deadline(ms, hours, minutes), timezone);
        }
    }

    private static class UselessStepRowMapper implements RowMapper<Object[]> {
        @Override
        public Object[] mapRow(ResultSet rs, int i) throws SQLException {
            return new Object[]{
                rs.getInt("company_id"),
                rs.getInt("reaction_id"),
                rs.getInt("case_id"),
                rs.getInt("step_id")
            };
        }
    }
}
