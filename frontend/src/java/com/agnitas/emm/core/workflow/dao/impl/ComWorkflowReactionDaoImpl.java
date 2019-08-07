/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.agnitas.beans.CompaniesConstraints;
import org.agnitas.dao.UserStatus;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DbUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.recipient.dao.impl.HistoryUpdateType;
import com.agnitas.emm.core.recipient.service.RecipientProfileHistoryService;
import com.agnitas.emm.core.workflow.beans.ComWorkflowReaction;
import com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus;
import com.agnitas.emm.core.workflow.beans.WorkflowActionMailingDeferral;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionStep;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionStepDeclaration;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionStepInstance;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionType;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowActionMailingDeferralImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowReactionStepImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowReactionStepInstanceImpl;
import com.agnitas.emm.core.workflow.dao.ComWorkflowReactionDao;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils.Deadline;

public class ComWorkflowReactionDaoImpl extends BaseDaoImpl implements ComWorkflowReactionDao {

    /**
     * The logger.
     */
    private static final transient Logger logger = Logger.getLogger(ComWorkflowReactionDaoImpl.class);

    /**
     * Service handling profile field history.
     */
    private RecipientProfileHistoryService recipientProfileHistoryService;

    @Override
    public boolean exists(int reactionId, @VelocityCheck int companyId) {
        String sqlGetCount = "SELECT COUNT(*) FROM workflow_reaction_tbl WHERE company_id = ? AND reaction_id = ?";
        return selectInt(logger, sqlGetCount, companyId, reactionId) > 0;
    }

    @Override
    public ComWorkflowReaction getReaction(int reactionId, @VelocityCheck int companyId) {
        String sqlGetReaction = "SELECT * FROM workflow_reaction_tbl WHERE reaction_id = ? AND company_id = ?";
        ComWorkflowReaction reaction = selectObjectDefaultNull(logger, sqlGetReaction, new ReactionRowMapper(), reactionId, companyId);
        if (reaction != null) {
            retrieveMailingsToSend(reaction);
        }
        return reaction;
    }

    @Override
    public List<ComWorkflowReaction> getReactionsToCheck(CompaniesConstraints constraints) {
        String sqlGetReactionsToCheck = "SELECT r.* FROM workflow_reaction_tbl r " +
                "JOIN company_tbl c ON c.company_id = r.company_id AND c.status NOT IN ('deleted', 'deletion in progress') " +
                "WHERE r.start_date <= CURRENT_TIMESTAMP AND r.active = 1 " +
                DbUtilities.asCondition("AND %s ", constraints, "r.company_id") +
                "ORDER BY reaction_id";
        List<ComWorkflowReaction> reactions = select(logger, sqlGetReactionsToCheck, new ReactionRowMapper());
        reactions.forEach(this::retrieveMailingsToSend);
        return reactions;
    }

    @Override
    public List<WorkflowReactionStep> getStepsToMake(CompaniesConstraints constraints) {
        String sqlGetStepsToMake = "SELECT s.case_id, s.step_id, s.previous_step_id, s.reaction_id, s.company_id, s.step_date, s.done, d.target_id, d.is_target_positive, d.mailing_id " +
            "FROM workflow_reaction_step_tbl s " +
            "JOIN workflow_reaction_decl_tbl d ON d.company_id = s.company_id AND d.reaction_id = s.reaction_id AND d.step_id = s.step_id " +
            "JOIN workflow_reaction_tbl r ON r.company_id = s.company_id AND r.reaction_id = s.reaction_id " +
            "JOIN workflow_tbl w ON w.company_id = s.company_id AND w.workflow_id = r.workflow_id AND w.status IN (?, ?) " +
            "JOIN company_tbl c ON c.company_id = s.company_id AND c.status NOT IN ('deleted', 'deletion in progress') " +
            "WHERE s.step_date <= CURRENT_TIMESTAMP AND s.done = 0 " + DbUtilities.asCondition("AND %s ", constraints, "s.company_id") +
            "ORDER BY s.company_id, s.reaction_id, s.case_id, s.step_date, s.step_id";
        return select(logger, sqlGetStepsToMake, new StepRowMapper(), WorkflowStatus.STATUS_ACTIVE.getId(), WorkflowStatus.STATUS_TESTING.getId());
    }

    /**
     * For legacy mode only.
     */
    @Deprecated
    private void retrieveMailingsToSend(ComWorkflowReaction reaction) {
        if (reaction.isLegacyMode()) {
            reaction.setMailingsToSend(getMailingsToSend(reaction.getReactionId(), reaction.getCompanyId()));
        }
    }

    /**
     * For legacy mode only.
     */
    @Deprecated
    private List<Integer> getMailingsToSend(int reactionId, int companyId) {
        String sqlGetMailingsToSend = "SELECT send_mailing_id FROM workflow_reaction_mailing_tbl WHERE company_id = ? AND reaction_id = ?";
        return select(logger, sqlGetMailingsToSend, new IntegerRowMapper(), companyId, reactionId);
    }

    private void restoreReactionId(ComWorkflowReaction reaction) {
        if (reaction.getReactionId() == 0) {
            reaction.setReactionId(getReactionId(reaction.getWorkflowId(), reaction.getCompanyId()));
        }
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void saveReaction(ComWorkflowReaction reaction) {
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
            "rules_sql = ?, " +
            "is_legacy_mode = 0 " +
            "WHERE company_id = ? " +
            "AND workflow_id = ?";

        if (reaction.isLegacyMode()) {
            throw new IllegalArgumentException("The legacy mode must be disabled as soon as a reaction is updated");
        }

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

        int touched = update(logger, query, parameters.toArray());

        // Update an existing or create a new one
        if (touched > 0) {
            restoreReactionId(reaction);
        } else {
            createReaction(reaction);
        }
    }

    protected void createReaction(ComWorkflowReaction reaction) {
        int newId;

        if (reaction.isLegacyMode()) {
            throw new IllegalArgumentException("The legacy mode must not be used for new reactions");
        }

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
            newId = selectInt(logger, "SELECT workflow_reaction_tbl_seq.NEXTVAL FROM dual");
            params.add(0, newId);
            update(logger, "INSERT INTO workflow_reaction_tbl (reaction_id, workflow_id, company_id, mailinglist_id, trigger_mailing_id, " +
                "trigger_link_id, active, once, start_date, admin_timezone, reaction_type, profile_column, rules_sql, is_legacy_mode) VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)", params.toArray());
        } else {
            String insertStatement = "INSERT INTO workflow_reaction_tbl (workflow_id, company_id, mailinglist_id, trigger_mailing_id, trigger_link_id, " +
                "active, once, start_date, admin_timezone, reaction_type, profile_column, rules_sql, is_legacy_mode) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
            newId = insertIntoAutoincrementMysqlTable(logger, "reaction_id", insertStatement, params.toArray());
        }

        reaction.setReactionId(newId);
    }

    private String getTimezoneId(ComWorkflowReaction reaction) {
        TimeZone timezone = reaction.getAdminTimezone();

        if (timezone == null) {
            return "Europe/Berlin";
        }

        return timezone.getID();
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void saveReactionStepDeclarations(List<WorkflowReactionStepDeclaration> declarations, int reactionId, @VelocityCheck int companyId) {
        if (exists(reactionId, companyId)) {
            deleteReactionStepDeclarations(reactionId, companyId);
            for (WorkflowReactionStepDeclaration declaration : declarations) {
                saveReactionStepDeclaration(declaration, reactionId, companyId);
            }
        }
    }

    protected void saveReactionStepDeclaration(WorkflowReactionStepDeclaration declaration, int reactionId, @VelocityCheck int companyId) {
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

        update(logger, sqlSaveStepDeclaration, sqlParameters.toArray());

        declaration.setCompanyId(companyId);
        declaration.setReactionId(reactionId);
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void deactivateWorkflowReactions(int workflowId, @VelocityCheck int companyId) {
        int reactionId = getReactionId(workflowId, companyId);
        if (reactionId > 0) {
            deactivateReaction(reactionId, companyId);
        }
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void deactivateReaction(int reactionId, @VelocityCheck int companyId) {
        update(logger, "UPDATE workflow_reaction_tbl SET active = 0 WHERE company_id = ? AND reaction_id = ?", companyId, reactionId);
        clearReactionLog(reactionId, companyId, true);
        clearReactionLegacyLog(reactionId, companyId);
    }

    protected void deleteReactionStepDeclarations(int reactionId, @VelocityCheck int companyId) {
        update(logger, "DELETE FROM workflow_reaction_decl_tbl WHERE company_id = ? AND reaction_id = ?", companyId, reactionId);
    }

    protected void clearReactionLog(int reactionId, @VelocityCheck int companyId, boolean preserveTriggerStepLog) {
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
            update(logger, sqlClearOvertakenEntries, companyId, reactionId, companyId, reactionId);
            update(logger, sqlPackTriggerStepLog, companyId, reactionId);
        }

        update(logger, sqlClearSteps, companyId, reactionId);
        update(logger, sqlClearRecipients, companyId, reactionId);
    }

    /**
     * For legacy mode only.
     */
    @Deprecated
    private void clearReactionLegacyLog(int reactionId, @VelocityCheck int companyId) {
        String sqlClearLog = "DELETE FROM workflow_reaction_log_tbl WHERE company_id = ? AND reaction_id = ?";
        update(logger, sqlClearLog, companyId, reactionId);
        String sqlClearMailingsToSend = "DELETE FROM workflow_reaction_mailing_tbl WHERE company_id = ? AND reaction_id = ?";
        update(logger, sqlClearMailingsToSend, companyId, reactionId);
        String sqlClearDeferrals = "DELETE FROM workflow_def_mailing_tbl WHERE company_id = ? AND reaction_id = ?";
        update(logger, sqlClearDeferrals, companyId, reactionId);
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void deleteWorkflowReactions(int workflowId, @VelocityCheck int companyId) {
        int reactionId = getReactionId(workflowId, companyId);
        if (reactionId > 0) {
            deleteReaction(reactionId, companyId);
        }
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void deleteReactions(@VelocityCheck int companyId) {
        update(logger, "DELETE FROM workflow_reaction_mailing_tbl WHERE company_id = ?", companyId);
        update(logger, "DELETE FROM workflow_reaction_log_tbl WHERE company_id = ?", companyId);
        update(logger, "DELETE FROM workflow_reaction_out_tbl WHERE company_id = ?", companyId);
        update(logger, "DELETE FROM workflow_reaction_step_tbl WHERE company_id = ?", companyId);
        update(logger, "DELETE FROM workflow_reaction_decl_tbl WHERE company_id = ?", companyId);
        update(logger, "DELETE FROM workflow_def_mailing_tbl WHERE company_id = ?", companyId);
        update(logger, "DELETE FROM workflow_reaction_tbl WHERE company_id = ?", companyId);
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void deleteReaction(int reactionId, @VelocityCheck int companyId) {
        clearReactionLog(reactionId, companyId, false);
        clearReactionLegacyLog(reactionId, companyId);
        deleteReactionStepDeclarations(reactionId, companyId);
        update(logger, "DELETE FROM workflow_reaction_tbl WHERE company_id = ? AND reaction_id = ?", companyId, reactionId);
    }

    @Override
    public List<Integer> getClickedRecipients(ComWorkflowReaction reaction, boolean excludeLoggedReactions) {
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

            if (reaction.isLegacyMode()) {
                if (reaction.isOnce()) {
                    sqlGetRecipients += " AND rlog.timestamp > ? AND NOT EXISTS (" +
                            "SELECT 1 FROM workflow_reaction_log_tbl tlog " +
                            "WHERE rlog.customer_id = tlog.customer_id AND tlog.reaction_id = ?" +
                            ")";
                } else {
                    sqlGetRecipients += " AND rlog.timestamp > (" +
                            "SELECT COALESCE(MAX(tlog.reaction_date), ?) FROM workflow_reaction_log_tbl tlog " +
                            "WHERE rlog.customer_id = tlog.customer_id AND tlog.reaction_id = ?" +
                            ")";
                }
            } else {
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

        return select(logger, sqlGetRecipients, new IntegerRowMapper(), sqlParameters.toArray());
    }

    @Override
    public List<Integer> getOpenedRecipients(ComWorkflowReaction reaction, boolean excludeLoggedReactions) {
        List<Object> sqlParameters = new ArrayList<>();

        String sqlGetRecipients = "SELECT DISTINCT(plog.customer_id) FROM onepixellog_" + reaction.getCompanyId() + "_tbl plog " +
                "WHERE plog.mailing_id = ?";

        sqlParameters.add(reaction.getTriggerMailingId());

        if (excludeLoggedReactions) {
            sqlParameters.add(reaction.getReactionId());

            if (reaction.isLegacyMode()) {
                if (reaction.isOnce()) {
                    sqlGetRecipients += " AND NOT EXISTS (" +
                            "SELECT 1 FROM workflow_reaction_log_tbl rlog " +
                            "WHERE rlog.customer_id = plog.customer_id AND rlog.reaction_id = ?" +
                            ")";
                } else {
                    sqlGetRecipients += " AND plog.open_count > (" +
                            "SELECT COUNT(*) FROM workflow_reaction_log_tbl rlog " +
                            "WHERE rlog.reaction_id = ? AND rlog.customer_id = plog.customer_id" +
                            ")";
                }
            } else {
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
        }

        if (reaction.getMailinglistId() > 0) {
            sqlGetRecipients += " AND EXISTS (SELECT 1 FROM customer_" + reaction.getCompanyId() + "_binding_tbl bind " +
                    "WHERE bind.customer_id = plog.customer_id AND bind.mailinglist_id = ? AND bind.user_status = ?)";

            sqlParameters.add(reaction.getMailinglistId());
            sqlParameters.add(UserStatus.Active.getStatusCode());
        }

        return select(logger, sqlGetRecipients, new IntegerRowMapper(), sqlParameters.toArray());
    }

    @Override
    public List<Integer> getRecipientsWithChangedProfile(ComWorkflowReaction reaction, boolean excludeLoggedReactions) {
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
                if (reaction.isLegacyMode()) {
                    sqlBuilder.append("SELECT rlog.customer_id FROM workflow_reaction_log_tbl rlog WHERE rlog.reaction_id = ? ");
                    if (reaction.isOnce()) {
                        sqlBuilder.append("AND rlog.customer_id = cust.customer_id");
                    } else {
                        sqlBuilder.append("AND rlog.customer_id = hst.customer_id AND rlog.reaction_date > hst.change_date");
                    }
                } else {
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
                }
                sqlBuilder.append(")");
            }

            if (reaction.getMailinglistId() > 0) {
                sqlBuilder.append(" AND EXISTS (SELECT 1 FROM customer_").append(reaction.getCompanyId()).append("_binding_tbl bind ")
                        .append("WHERE bind.customer_id = cust.customer_id AND bind.mailinglist_id = ? AND bind.user_status = ?)");

                sqlParameters.add(reaction.getMailinglistId());
                sqlParameters.add(UserStatus.Active.getStatusCode());
            }

            return select(logger, sqlBuilder.toString(), new IntegerRowMapper(), sqlParameters.toArray());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Integer> getRecipientsWithChangedBinding(ComWorkflowReaction reaction, boolean excludeLoggedReactions) {
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
			if (reaction.isLegacyMode()) {
                sqlGetRecipientsBuilder.append("SELECT rlog.customer_id FROM workflow_reaction_log_tbl rlog ")
                        .append("WHERE rlog.reaction_id = ? AND rlog.customer_id = bind.customer_id");

                if (!reaction.isOnce() || reaction.getReactionType() == WorkflowReactionType.OPT_IN) {
                    sqlGetRecipientsBuilder.append(" AND rlog.reaction_date > COALESCE(hst.timestamp_change, bind.creation_date)");
                }
            } else {
                sqlGetRecipientsBuilder.append("SELECT rout.customer_id FROM workflow_reaction_out_tbl rout ")
                        .append("WHERE rout.reaction_id = ? AND rout.customer_id = bind.customer_id");

                if (!reaction.isOnce() || reaction.getReactionType() == WorkflowReactionType.OPT_IN) {
                    sqlGetRecipientsBuilder.append(" AND rout.step_date > COALESCE(hst.timestamp_change, bind.creation_date)");
                }
            }
			sqlGetRecipientsBuilder.append(")");
        }

        if (joinType.equals("FULL") && isMariaDB()) {
		    // Duplicate parameters for UNION that combines two similar SELECT clauses having same placeholders.
            sqlParameters.addAll(Arrays.asList(sqlParameters.toArray()));
            return select(logger, convertFullJoinToUnion(sqlGetRecipientsBuilder), new IntegerRowMapper(), sqlParameters.toArray());
        } else {
            return select(logger, sqlGetRecipientsBuilder.toString(), new IntegerRowMapper(), sqlParameters.toArray());
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
    public void trigger(ComWorkflowReaction reaction, List<Integer> recipients) {
        if (CollectionUtils.isEmpty(recipients)) {
            return;
        }

        if (reaction.isLegacyMode()) {
            triggerLegacyMode(reaction, recipients);
        } else {
            Date reactionDate = new Date();
            int newCaseId = getMaxCaseId(reaction) + 1;

            createNewCase(reaction, newCaseId, reactionDate);
            setTriggerStepRecipients(newCaseId, reaction.getReactionId(), reaction.getCompanyId(), recipients, reactionDate);
        }
    }

    @Override
    public void setStepDone(WorkflowReactionStepInstance step) {
        setStepDone(step, 0, false, null);
    }

    @Override
    public void setStepDone(WorkflowReactionStepInstance step, String sqlTargetExpression) {
        setStepDone(step, 0, false, sqlTargetExpression);
    }

    @Override
    public void setStepDone(WorkflowReactionStepInstance step, int mailingListId, boolean requireActiveBinding, String sqlTargetExpression) {
        String sqlMarkAsDone = "UPDATE workflow_reaction_step_tbl SET done = 1 " +
            "WHERE company_id = ? AND reaction_id = ? AND step_id = ? AND case_id = ? AND done = 0";

        if (update(logger, sqlMarkAsDone, step.getCompanyId(), step.getReactionId(), step.getStepId(), step.getCaseId()) > 0) {
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

            update(logger, builder.toString(), sqlParameters.toArray());
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
            sqlSetDoneParameters = select(logger, sqlGetUselessSteps, new UselessStepRowMapper());

            if (sqlSetDoneParameters.size() > 0) {
                batchupdate(logger, sqlSetDone, sqlSetDoneParameters);
            }
        }
    }

    @Override
    public List<Integer> getStepRecipients(WorkflowReactionStepInstance step) {
        String sqlGetRecipients = "SELECT customer_id FROM workflow_reaction_out_tbl WHERE company_id = ? AND reaction_id = ? AND case_id = ? AND step_id = ?";
        return select(logger, sqlGetRecipients, new IntegerRowMapper(), step.getCompanyId(), step.getReactionId(), step.getCaseId(), step.getStepId());
    }

    protected void setTriggerStepRecipients(int caseId, int reactionId, int companyId, List<Integer> recipients, Date reactionDate) {
        String sqlClearLog = "DELETE FROM workflow_reaction_out_tbl WHERE company_id = ? AND reaction_id = ? AND case_id = ? AND step_id = 0";
        update(logger, sqlClearLog, companyId, reactionId, caseId);

        String sqlLogRecipients = "INSERT INTO workflow_reaction_out_tbl (case_id, step_id, reaction_id, company_id, customer_id, step_date) " +
            "VALUES (?, 0, ?, ?, ?, ?)";

        if (CollectionUtils.isNotEmpty(recipients)) {
            List<Object[]> sqlParameters = new ArrayList<>(recipients.size());
            for (int recipientId : recipients) {
                sqlParameters.add(new Object[]{caseId, reactionId, companyId, recipientId, reactionDate});
            }
            batchupdate(logger, sqlLogRecipients, sqlParameters);
        }
    }

    protected void createNewCase(ComWorkflowReaction reaction, int newCaseId, Date reactionDate) {
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

            batchupdate(logger, sqlStoreNewStepInstances, sqlParameters);
        }
    }

    private List<WorkflowReactionStepInstance> getNewStepInstances(ComWorkflowReaction reaction, int newCaseId, Date reactionDate) {
        NewStepInstanceFromDeclarationRowMapper mapper = new NewStepInstanceFromDeclarationRowMapper(reactionDate, reaction.getAdminTimezone(), newCaseId);

        String sqlGetNewStepInstances = "SELECT step_id, previous_step_id, reaction_id, company_id, deadline_relative, deadline_hours, deadline_minutes " +
            "FROM workflow_reaction_decl_tbl " +
            "WHERE company_id = ? AND reaction_id = ?";

        return select(logger, sqlGetNewStepInstances, mapper, reaction.getCompanyId(), reaction.getReactionId());
    }

    private int getMaxCaseId(ComWorkflowReaction reaction) {
        String sqlGetMaxCaseId = "SELECT COALESCE(MAX(case_id), 0) " +
                "FROM workflow_reaction_out_tbl " +
                "WHERE company_id = ? AND reaction_id = ? AND step_id = 0";

        return selectInt(logger, sqlGetMaxCaseId, reaction.getCompanyId(), reaction.getReactionId());
    }

    private void triggerLegacyMode(ComWorkflowReaction reaction, List<Integer> reactedRecipients) {
        final String sqlInsertReactedRecipients = "INSERT INTO workflow_reaction_log_tbl " +
                "(reaction_id, company_id, customer_id, reaction_date) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        final String sqlUpdateReactedRecipients = "UPDATE workflow_reaction_log_tbl SET reaction_date = CURRENT_TIMESTAMP " +
                "WHERE reaction_id = ? AND company_id = ? AND customer_id = ?";

        for (Integer recipientId : reactedRecipients) {
            if (update(logger, sqlUpdateReactedRecipients, reaction.getReactionId(), reaction.getCompanyId(), recipientId) <= 0) {
                update(logger, sqlInsertReactedRecipients, reaction.getReactionId(), reaction.getCompanyId(), recipientId);
            }
        }
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void addDeferredActionMailings(int reactionId, int mailingId, List<Integer> customersId, Date sendDate, @VelocityCheck int companyId) {
        ArrayList<Object[]> params = new ArrayList<>();
        for (Integer customerId : customersId) {
            if (isOracleDB()) {
                int newId = selectInt(logger, "SELECT workflow_def_mailing_tbl_seq.NEXTVAL FROM dual");
                params.add(new Object[]{newId, companyId, reactionId, customerId, mailingId, sendDate, 0});
            } else {
                params.add(new Object[]{companyId, reactionId, customerId, mailingId, sendDate, 0});
            }
        }
        String idPart = isOracleDB() ? "id, " : "";
        String lastParam = isOracleDB() ? ", ? " : "";
        String query = "INSERT INTO workflow_def_mailing_tbl (" + idPart + "company_id, reaction_id, customer_id, " +
                "mailing_id, send_date, sent) VALUES (?, ?, ?, ?, ?, ?" + lastParam + ")";
        batchupdate(logger, query, params);
    }

    @Override
    public List<WorkflowActionMailingDeferral> getDeferredActionMailings(CompaniesConstraints constraints) {
        String sqlGetDeferrals = "SELECT * FROM workflow_def_mailing_tbl " +
            "WHERE sent = 0 AND send_date < CURRENT_TIMESTAMP" +
            DbUtilities.asCondition(" AND %s", constraints);
        return select(logger, sqlGetDeferrals, new ActionMailingDeferralMapper());
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void markDeferredActionMailingsAsSent(List<Integer> deferralsIds) {
        if (CollectionUtils.isNotEmpty(deferralsIds)) {
            List<Object[]> args = new ArrayList<>(deferralsIds.size());
            for (int id : deferralsIds) {
                args.add(new Object[]{id});
            }
            batchupdate(logger, "UPDATE workflow_def_mailing_tbl SET sent = 1 WHERE id = ?", args);
        }
    }

    @Override
    public int getReactionId(int workflowId, @VelocityCheck int companyId) {
        String sqlGetReactionId = "SELECT reaction_id FROM workflow_reaction_tbl WHERE workflow_id = ? AND company_id = ?";
        return selectInt(logger, sqlGetReactionId, workflowId, companyId);
    }

    @Required
    public void setRecipientProfileHistoryService(final RecipientProfileHistoryService service) {
        this.recipientProfileHistoryService = service;
    }

    private static class ActionMailingDeferralMapper implements RowMapper<WorkflowActionMailingDeferral> {
        @Override
        public WorkflowActionMailingDeferral mapRow(ResultSet rs, int index) throws SQLException {
            WorkflowActionMailingDeferral deferral = new WorkflowActionMailingDeferralImpl();
            deferral.setId(rs.getInt("id"));
            deferral.setCompanyId(rs.getInt("company_id"));
            deferral.setReactionId(rs.getInt("reaction_id"));
            deferral.setMailingId(rs.getInt("mailing_id"));
            deferral.setCustomerId(rs.getInt("customer_id"));
            deferral.setSendDate(rs.getTimestamp("send_date"));
            deferral.setSent(rs.getInt("sent") == 1);
            return deferral;
        }
    }

    private static class ReactionRowMapper implements RowMapper<ComWorkflowReaction> {
        @Override
        public ComWorkflowReaction mapRow(ResultSet resultSet, int row) throws SQLException {
            ComWorkflowReaction reaction = new ComWorkflowReaction();
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
            reaction.setLegacyMode(resultSet.getInt("is_legacy_mode") == 1);
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
