/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

/**
 * Title:        Optimization
 * Copyright:    Copyright (c) AGNITAS AG
 * Company:      AGNITAS AG
 */

package com.agnitas.mailing.autooptimization.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowAutoOptimizationCriteria;
import com.agnitas.mailing.autooptimization.beans.Optimization;
import com.agnitas.mailing.autooptimization.beans.impl.AutoOptimizationStatus;
import com.agnitas.mailing.autooptimization.beans.impl.OptimizationImpl;
import com.agnitas.mailing.autooptimization.dao.OptimizationDao;
import com.agnitas.util.AgnUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

public class OptimizationDaoImpl extends BaseDaoImpl implements OptimizationDao {

	private static final String [] TABLE_COLUMNS = new String[] {
			"optimization_id",
			"company_id",
			"campaign_id",
			"mailinglist_id",
			"shortname",
			"description",
			"eval_type",
			"group1_id",
			"group2_id",
			"group3_id",
			"group4_id",
			"group5_id",
			"split_type",
			"status",
			"result_mailing_id",
			"result_senddate",
			"test_senddate",
			"threshold",
			"double_check",
			"target_expression",
			"target_mode",
			"final_mailing_id",
			"test_run",
			"workflow_id"
	};

	protected static class OptimizationRowMapper implements RowMapper<Optimization> {
		@Override
		public Optimization mapRow(ResultSet resultSet, int row) throws SQLException {
			Optimization optimization = new OptimizationImpl();

			optimization.setId(resultSet.getInt("optimization_id"));
			optimization.setCompanyID(resultSet.getInt("company_id"));
			optimization.setCampaignID(resultSet.getInt("campaign_id"));
			optimization.setMailinglistID(resultSet.getInt("mailinglist_id"));
			optimization.setShortname(resultSet.getString("shortname"));
			optimization.setDescription(resultSet.getString("description"));
			optimization.setEvalType(WorkflowDecision.WorkflowAutoOptimizationCriteria.fromId(resultSet.getInt("eval_type")));
			optimization.setGroup1(resultSet.getInt("group1_id"));
			optimization.setGroup2(resultSet.getInt("group2_id"));
			optimization.setGroup3(resultSet.getInt("group3_id"));
			optimization.setGroup4(resultSet.getInt("group4_id"));
			optimization.setGroup5(resultSet.getInt("group5_id"));
			optimization.setSplitType(resultSet.getString("split_type"));
			optimization.setStatus(resultSet.getInt("status"));
	        optimization.setTargetExpression(resultSet.getString("target_expression"));
	        optimization.setTargetMode(resultSet.getInt("target_mode"));
			//optimization.setTargetID(resultSet.getBigDecimal("target_id").intValue());
			optimization.setResultMailingID(resultSet.getInt("result_mailing_id"));
			optimization.setSendDate(resultSet.getTimestamp("result_senddate"));
			optimization.setTestMailingsSendDate(resultSet.getTimestamp("test_senddate"));
			optimization.setThreshold(resultSet.getInt("threshold"));
			optimization.setFinalMailingId(resultSet.getInt( "final_mailing_id"));

			optimization.setDoubleCheckingActivated(BooleanUtils.toBoolean(resultSet.getInt("double_check")));
			optimization.setTestRun(BooleanUtils.toBoolean(resultSet.getInt("test_run")));
			optimization.setWorkflowId(resultSet.getInt("workflow_id"));

			return optimization;
		}
	}

	@Override
	public Optimization get(int optimizationID, int companyID) {
	   String query = "SELECT " + StringUtils.join(TABLE_COLUMNS, ", ") + " FROM auto_optimization_tbl" +
			   " WHERE optimization_id = ? AND company_id = ? AND (deleted = 0 OR deleted IS NULL)";
		return selectObject(query, new OptimizationRowMapper(), optimizationID, companyID);
	}

	@Override
	public Map<Integer, Integer> getDueOnDateOptimizations(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds) {
		// query for optimizations which are due on their final senddate
		String sql = createDueOnDateOrThresholdSqlQuery(false, includedCompanyIds, excludedCompanyIds);

		try	{
			List<Map<String, Object>> list = select(sql, AutoOptimizationStatus.TEST_SEND.getCode(), AutoOptimizationStatus.SCHEDULED.getCode());

			Map<Integer, Integer> result = new HashMap<>();
			for (Map<String, Object> map : list) {
				int id = ((Number) map.get("optimization_id")).intValue();
				int companyID = ((Number) map.get("company_id")).intValue();
				result.put(id, companyID);
			}
			return result;
		} catch(Exception e) {
			logger.error("Error retrieving on-due optimizations", e);
		}

		return null;
	}

	@Override
	public List<Optimization> getDueOnThresholdOptimizationCandidates(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds) {
		// query for optimizations which have a threshold , are scheduled ( not send ) and test mailings have been sent
		String sql = createDueOnDateOrThresholdSqlQuery(true, includedCompanyIds, excludedCompanyIds);
		return select(sql, new OptimizationRowMapper(), AutoOptimizationStatus.TEST_SEND.getCode(), AutoOptimizationStatus.SCHEDULED.getCode());
	}

	private String createDueOnDateOrThresholdSqlQuery(boolean thresholdQuery, List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds) {
		StringBuilder sqlQueryBuilder = new StringBuilder();
		final String [] groups = { "ao.group1_id", "ao.group2_id", "ao.group3_id", "ao.group4_id", "ao.group5_id" };

		List<String> sqlCaseClauses = new ArrayList<>();
		for (String group : groups) {
			sqlCaseClauses.add("(CASE WHEN " + group + " > 0 THEN 1 ELSE 0 END)");
		}

		sqlQueryBuilder
				.append("SELECT ")
				.append(thresholdQuery ? StringUtils.join(TABLE_COLUMNS, ", ") : "ao.optimization_id, ao.company_id")
				.append(" FROM auto_optimization_tbl ao")
				.append(" WHERE ao.status IN (?, ?)")
				.append(" AND (deleted = 0 OR deleted IS NULL)");

		if (thresholdQuery) {
			sqlQueryBuilder.append(" AND ao.threshold > 0");
		} else {
			sqlQueryBuilder.append(" AND ao.result_senddate < ").append(isOracleDB() ? "SYSDATE" : "CURRENT_TIMESTAMP");
		}

		sqlQueryBuilder
				.append(" AND (")
				.append(StringUtils.join(sqlCaseClauses, " + "))
				.append(") = (")
				.append("SELECT COUNT(*) FROM maildrop_status_tbl md")
				.append(" JOIN mailing_backend_log_tbl mb")
				.append(" ON mb.status_id = md.status_id");

		if (!thresholdQuery) {
			sqlQueryBuilder. append(" AND mb.current_mails = mb.total_mails");
		}

		sqlQueryBuilder
				.append(" WHERE md.mailing_id IN (").append(StringUtils.join(groups, ", ")).append(")")
				.append(" AND ((ao.test_run = 0 AND md.status_field = 'W') OR (ao.test_run = 1 AND md.status_field = 'T'))")
				.append(")")
				.append((includedCompanyIds != null && !includedCompanyIds.isEmpty() ? " AND ao.company_id IN(" + StringUtils.join(includedCompanyIds, ", ") + ")" : ""))
				.append((excludedCompanyIds != null && !excludedCompanyIds.isEmpty() ? " AND ao.company_id NOT IN(" + StringUtils.join(excludedCompanyIds, ", ") + ")" : ""));

		return sqlQueryBuilder.toString();
	}

	@Override
	public int getFinalMailingId(int companyId, int workflowId) {
		String sql = """
                SELECT MAX(final_mailing_id)
                FROM auto_optimization_tbl
                WHERE company_id = ? AND workflow_id = ?
                """.stripIndent();
		return selectInt(sql, companyId, workflowId);
	}

	@Override
	public String findName(int optimizationId, int companyId) {
		String query = "SELECT shortname FROM auto_optimization_tbl WHERE optimization_id = ? AND company_id = ?";
		return selectWithDefaultValue(query, String.class, "", optimizationId, companyId);
	}

	@Override
	public List<Integer> findTargetDependentAutoOptimizations(int targetGroupId, int companyId) {
		String query = "SELECT optimization_id FROM auto_optimization_tbl WHERE target_id = ? AND company_id = ? AND status NOT IN (?, ?)";
		return select(query, IntegerRowMapper.INSTANCE,
				targetGroupId, companyId, AutoOptimizationStatus.NOT_STARTED.getCode(), AutoOptimizationStatus.FINISHED.getCode());
	}

	@Override
	@DaoUpdateReturnValueCheck
    public int save(Optimization optimization) {
        int newOptimizationId;
        
        if(optimization.getEvalType() == null) {
        	// Throwing and catching exception to get a valid stack trace
        	try	{
        		throw new NullPointerException(String.format("Evaluation type of auto optimization %d is null. Defaulting to %s", optimization.getId(), WorkflowAutoOptimizationCriteria.AO_CRITERIA_CLICKRATE));
        	} catch(final NullPointerException e) {
        		logger.warn(String.format("Evaluation type of auto optimization %d is null. Defaulting to %s", optimization.getId(), WorkflowAutoOptimizationCriteria.AO_CRITERIA_CLICKRATE), e);
        	}
        	
        	optimization.setEvalType(WorkflowAutoOptimizationCriteria.AO_CRITERIA_CLICKRATE);
        }

        if (optimization.getId() == 0) { // does not exist ? -> insert
            if (isOracleDB()) {
	            String sequenceQuery = "SELECT auto_optimization_tbl_seq.nextval FROM DUAL";
	            newOptimizationId = selectInt(sequenceQuery);

	            String insertQuery = "INSERT INTO auto_optimization_tbl (" + StringUtils.join(TABLE_COLUMNS, ", ") + ") VALUES ( "+ AgnUtils.repeatString("?", TABLE_COLUMNS.length, ", ") + " )";

                update(insertQuery,
                        newOptimizationId,
                        optimization.getCompanyID(), optimization.getCampaignID(),
                        optimization.getMailinglistID(), optimization.getShortname(), optimization.getDescription(),
						optimization.getEvalType() == null ? 0 : optimization.getEvalType().getId(), optimization.getGroup1(), optimization.getGroup2(), optimization.getGroup3(),
                        optimization.getGroup4(), optimization.getGroup5(), optimization.getSplitType(), optimization.getStatus(),
                        optimization.getResultMailingID(), optimization.getSendDate(), optimization.getTestMailingsSendDate(), optimization.getThreshold(),
                        optimization.isDoubleCheckingActivated() ? 1 : 0,
                   		optimization.getTargetExpression(), optimization.getTargetMode(),
                   		optimization.getFinalMailingId(),
						optimization.isTestRun() ? 1 : 0,
						optimization.getWorkflowId()
                );
            } else {
				String[] columnNames = new String[] {
						"company_id",
						"campaign_id",
						"mailinglist_id",
						"shortname",
						"description",
						"eval_type",
						"group1_id",
						"group2_id",
						"group3_id",
						"group4_id",
						"group5_id",
						"split_type",
						"status",
						"result_mailing_id",
						"result_senddate",
						"test_senddate",
						"threshold",
						"double_check",
						"target_expression",
						"target_mode",
						"final_mailing_id",
						"test_run",
						"workflow_id"
				};

				Object[] columnValues = new Object[] {
						optimization.getCompanyID(),
						optimization.getCampaignID(),
						optimization.getMailinglistID(),
						optimization.getShortname(),
						optimization.getDescription(),
						optimization.getEvalType() == null ? 0 : optimization.getEvalType().getId(),
						optimization.getGroup1(),
						optimization.getGroup2(),
						optimization.getGroup3(),
						optimization.getGroup4(),
						optimization.getGroup5(),
						optimization.getSplitType(),
						optimization.getStatus(),
						optimization.getResultMailingID(),
						optimization.getSendDate(),
						optimization.getTestMailingsSendDate(),
						optimization.getThreshold(),
						optimization.isDoubleCheckingActivated() ? 1 : 0,
						optimization.getTargetExpression(),
						optimization.getTargetMode(),
						optimization.getFinalMailingId(),
						optimization.isTestRun() ? 1 : 0,
						optimization.getWorkflowId()
				};

                String insertStatement = "INSERT INTO auto_optimization_tbl (" + StringUtils.join(columnNames, ", ") + ")"
                	+ " VALUES ( " + AgnUtils.repeatString("?", columnNames.length, ", ") + ")";

				newOptimizationId = insert("optimization_id", insertStatement, columnValues);
            }
			optimization.setId(newOptimizationId);

            return newOptimizationId;
        } else { // update
            String updateQuery = "UPDATE auto_optimization_tbl " +
                    " SET " +
					" company_id = ?, " +
                    " campaign_id = ?, " +
                    " mailinglist_id = ?, " +
                    " shortname = ?, " +
                    " description = ?, " +
                    " eval_type = ?, " +
                    " group1_id = ?, " +
                    " group2_id = ?, " +
                    " group3_id = ?, " +
                    " group4_id = ?, " +
                    " group5_id = ?, " +
                    " split_type = ?, " +
                    " status = ?, " +
                    " result_mailing_id = ?, " +
                    " result_senddate = ?, " +
					" test_senddate = ?, " +
                    " threshold = ?, " +
                    " double_check = ?, " +
                    " target_expression = ?, " +
                    " target_mode = ?, " +
                    " final_mailing_id = ?, " +
					" test_run = ?, " +
					" workflow_id = ? " +
                    " WHERE optimization_id = ?";

            int rowsAffected = update(updateQuery,
                    optimization.getCompanyID(),
                    optimization.getCampaignID(), optimization.getMailinglistID(),
                    optimization.getShortname(), optimization.getDescription(), optimization.getEvalType() == null ? 0 : optimization.getEvalType().getId(),
                    optimization.getGroup1(), optimization.getGroup2(), optimization.getGroup3(), optimization.getGroup4(),
                    optimization.getGroup5(), optimization.getSplitType(), optimization.getStatus(), optimization.getResultMailingID(),
                    optimization.getSendDate(), optimization.getTestMailingsSendDate(), optimization.getThreshold(),
					optimization.isDoubleCheckingActivated() ? 1 : 0, optimization.getTargetExpression(), optimization.getTargetMode(),
                    optimization.getFinalMailingId(),
					optimization.isTestRun() ? 1 : 0,
					optimization.getWorkflowId(),
                    optimization.getId()
            );

            if (rowsAffected == 0) {
            	logger.warn("Auto-optimization ID {} could no tbe updated", optimization);
                throw new IllegalStateException("Could not update Optimization with ID = " + optimization);
            }

            return optimization.getId();
        }
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean delete(Optimization optimization) {
		String deleteQuery = "UPDATE auto_optimization_tbl SET deleted = 1 WHERE optimization_id = ?";
        return update(deleteQuery, optimization.getId()) > 0;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int deleteByCompanyID(int companyID) {
        return update("DELETE FROM auto_optimization_tbl WHERE company_id = ?", companyID);
	}

	@Override
	public List<Optimization> listWorkflowManaged(int workflowId, int companyID) {
		StringBuilder sqlQueryBuilder = new StringBuilder();

		sqlQueryBuilder.append("SELECT ").append(StringUtils.join(TABLE_COLUMNS, ", "))
				.append(" FROM auto_optimization_tbl")
				.append(" WHERE (deleted = 0 OR deleted IS NULL) AND company_id = ? AND workflow_id = ?");

		return select(sqlQueryBuilder.toString(), new OptimizationRowMapper(), companyID, workflowId);
	}

	@Override
	public int countByCompanyID(int companyID) {
        return selectInt("SELECT COUNT(*) FROM auto_optimization_tbl WHERE company_id = ?", companyID);
	}

	@Override
	public List<Optimization> getAutoOptimizations(int companyId, Date startDate, Date endDate) {
		return select("""
            SELECT ao.campaign_id,
                CASE WHEN ao.workflow_id = 0 OR ao.workflow_id IS NULL
                     THEN ao.shortname
                     ELSE (SELECT w.shortname FROM workflow_tbl w WHERE w.workflow_id = ao.workflow_id) END AS shortname,
                ao.status, ao.optimization_id, ao.result_mailing_id, ao.result_senddate, ao.workflow_id
            FROM auto_optimization_tbl ao
            WHERE ao.company_id = ?
                AND (ao.deleted = 0 OR ao.deleted IS NULL)
                AND ao.result_senddate IS NOT NULL
                AND ao.result_senddate >= ?
                AND ao.result_senddate < ?
            ORDER BY ao.result_senddate""", new MinimizedOptimizationRowMapper(), companyId, startDate, endDate);
	}
    
    @Override
    public int getOptimizationByFinalMailingId(int finalMailingId, int companyId) {
		return selectInt("SELECT optimization_id FROM auto_optimization_tbl WHERE final_mailing_id = ? AND company_id = ?",
				finalMailingId, companyId);
    }
    
    private static class MinimizedOptimizationRowMapper implements RowMapper<Optimization> {

		@Override
		public Optimization mapRow(ResultSet rs, int i) throws SQLException {
			AutoOptimizationStatus status = AutoOptimizationStatus.get(rs.getBigDecimal("status").intValue());

			Optimization optimization = new OptimizationImpl();
			optimization.setCampaignID(rs.getBigDecimal("campaign_id").intValue());
			optimization.setShortname(rs.getString("shortname"));
			optimization.setAutoOptimizationStatus(status);
			optimization.setId(rs.getBigDecimal("optimization_id").intValue());
			optimization.setResultMailingID(rs.getBigDecimal("result_mailing_id").intValue());
			optimization.setSendDate(rs.getTimestamp("result_senddate"));
			optimization.setWorkflowId(rs.getBigDecimal("workflow_id").intValue());
			return optimization;
		}
	}
}
