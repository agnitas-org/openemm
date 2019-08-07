/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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
import java.util.TreeMap;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowAutoOptimizationCriteria;
import com.agnitas.mailing.autooptimization.beans.ComOptimization;
import com.agnitas.mailing.autooptimization.beans.impl.AutoOptimizationStatus;
import com.agnitas.mailing.autooptimization.beans.impl.ComOptimizationImpl;
import com.agnitas.mailing.autooptimization.dao.ComOptimizationDao;

/**
 * Implementation of {@link ComOptimization}.
 *
 */
public class ComOptimizationDaoImpl extends BaseDaoImpl implements ComOptimizationDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComOptimizationDaoImpl.class);

	private static final String [] tableColumns = new String[] {
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

	protected class ComOptimizationRowMapper implements RowMapper<ComOptimization> {
		@Override
		public ComOptimization mapRow(ResultSet resultSet, int row) throws SQLException {
			ComOptimization optimization = new ComOptimizationImpl();

			optimization.setId(resultSet.getBigDecimal("optimization_id").intValue());
			optimization.setCompanyID(resultSet.getBigDecimal("company_id").intValue());
			optimization.setCampaignID(resultSet.getBigDecimal("campaign_id").intValue());
			optimization.setMailinglistID(resultSet.getBigDecimal("mailinglist_id").intValue());
			optimization.setShortname(resultSet.getString("shortname"));
			optimization.setDescription(resultSet.getString("description"));
			optimization.setEvalType(WorkflowDecision.WorkflowAutoOptimizationCriteria.fromId(resultSet.getBigDecimal("eval_type").intValue()));
			optimization.setGroup1(resultSet.getBigDecimal("group1_id").intValue());
			optimization.setGroup2(resultSet.getBigDecimal("group2_id").intValue());
			optimization.setGroup3(resultSet.getBigDecimal("group3_id").intValue());
			optimization.setGroup4(resultSet.getBigDecimal("group4_id").intValue());
			optimization.setGroup5(resultSet.getBigDecimal("group5_id").intValue());
			optimization.setSplitType(resultSet.getString("split_type"));
			optimization.setStatus(resultSet.getBigDecimal("status").intValue());
	        optimization.setTargetExpression(resultSet.getString("target_expression"));
	        optimization.setTargetMode(resultSet.getBigDecimal("target_mode").intValue());
			//optimization.setTargetID(resultSet.getBigDecimal("target_id").intValue());
			optimization.setResultMailingID(resultSet.getBigDecimal("result_mailing_id").intValue());
			optimization.setSendDate(resultSet.getTimestamp("result_senddate"));
			optimization.setTestMailingsSendDate(resultSet.getTimestamp("test_senddate"));
			optimization.setThreshold(resultSet.getBigDecimal("threshold") != null ? resultSet.getBigDecimal("threshold").intValue() : 0);		
			optimization.setFinalMailingId( resultSet.getBigDecimal( "final_mailing_id") != null ? resultSet.getBigDecimal( "final_mailing_id").intValue() : 0);
			boolean doubleCheckingActivated = false;

			if( resultSet.getBigDecimal("double_check") != null ) {
				doubleCheckingActivated = (resultSet.getBigDecimal("double_check").intValue() > 0 );
				
			}

			optimization.setDoubleCheckingActivated(doubleCheckingActivated);
			optimization.setTestRun(resultSet.getBigDecimal("test_run").intValue() == 1);
			optimization.setWorkflowId(resultSet.getBigDecimal("workflow_id").intValue());

			return optimization;
		}
	}

	@Override
	public ComOptimization get(int optimizationID, @VelocityCheck int companyID) {
	   String query = "SELECT " + StringUtils.join(tableColumns, ", ") + " FROM auto_optimization_tbl" +
			   " WHERE optimization_id = ? AND company_id = ? AND (deleted = 0 OR deleted IS NULL)";
		return selectObject(logger, query, new ComOptimizationRowMapper(), optimizationID, companyID);
	}

	@Override
	public Map<Integer, String>	getGroups(int campaignID, @VelocityCheck int companyID, int optimizationID)	{
		String sql = "SELECT m.mailing_id, m.shortname" +
				" FROM mailing_tbl m" +
				" WHERE m.company_id = ? AND m.campaign_id = ? AND m.deleted = 0 AND m.is_template = 0 AND mailing_type = 0" +
				" AND m.mailing_id NOT IN (" +
					" SELECT mailing_id FROM mailing_account_tbl c WHERE m.mailing_id = c.mailing_id AND c.status_field = 'W'" +
				" )" +
				" AND NOT EXISTS (" +
					" SELECT 1 FROM auto_optimization_tbl" +
					" WHERE (optimization_id <> ? OR ? = 0)" +
					" AND (deleted <> 1 OR deleted IS NULL)" +
					" AND m.mailing_id IN (group1_id, group2_id, group3_id, group4_id, group5_id)" +
				" )" +
				" ORDER BY mailing_id DESC";

		try	{
			List<Map<String, Object>> list = select(logger, sql, companyID, campaignID, optimizationID, optimizationID);
			if (CollectionUtils.isEmpty(list)) {
				return null;
			}

			Map<Integer, String> result = new TreeMap<>();
			for (Map<String, Object> map : list) {
				Integer mailingID = ((Number) map.get("mailing_id")).intValue();
				String shortname = (String) map.get("shortname");

				result.put(mailingID, shortname);
			}
			return result;
		} catch(Exception e) {
			logger.error("Error getting split-groups", e);
			javaMailService.sendExceptionMail("SQL: "+sql+", "+companyID+", "+campaignID, e);
			return null;
		}
	}

	@Override
	public Map<Integer, Integer> getDueOnDateOptimizations(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds) {
		// query for optimizations which are due on their final senddate
		String sql = createDueOnDateOrThresholdSqlQuery(false, includedCompanyIds, excludedCompanyIds);

		try	{
			List<Map<String, Object>> list = select(logger, sql, ComOptimization.STATUS_TEST_SEND, ComOptimization.STATUS_SCHEDULED);

			Map<Integer, Integer> result = new HashMap<>();
			for (Map<String, Object> map : list) {
				int id = ((Number) map.get("optimization_id")).intValue();
				int companyID = ((Number) map.get("company_id")).intValue();
				result.put(id, companyID);
			}
			return result;
		} catch(Exception e) {
			logger.error("Error retrieving on-due optimizations", e);
			javaMailService.sendExceptionMail("SQL: " + sql, e);
		}

		return null;
	}

	@Override
	public List<ComOptimization> getDueOnThresholdOptimizationCandidates(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds) {
		// query for optimizations which have a threshold , are scheduled ( not send ) and test mailings have been sent
		String sql = createDueOnDateOrThresholdSqlQuery(true, includedCompanyIds, excludedCompanyIds);
		return select(logger, sql, new ComOptimizationRowMapper(), ComOptimization.STATUS_TEST_SEND, ComOptimization.STATUS_SCHEDULED);
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
				.append(thresholdQuery ? StringUtils.join(tableColumns, ", ") : "ao.optimization_id, ao.company_id")
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
	public int getFinalMailingID(int companyID, int workflowID, int oneOfTheSplitMailingID){
		String sql = "" +
				"SELECT MAX(final_mailing_id) " +
				"FROM auto_optimization_tbl " +
				"WHERE company_id = ? " +
				"      AND workflow_id = ? " +
				"      AND (group1_id = ?  " +
				"           OR group2_id = ?  " +
				"           OR group3_id = ?  " +
				"           OR group4_id = ?  " +
				"           OR group5_id = ?)";
		return selectInt(logger, sql, companyID, workflowID, oneOfTheSplitMailingID, oneOfTheSplitMailingID, oneOfTheSplitMailingID, oneOfTheSplitMailingID, oneOfTheSplitMailingID);
	}

	@Override
	@DaoUpdateReturnValueCheck
    public int save(ComOptimization optimization) {
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
	            newOptimizationId = selectInt(logger, sequenceQuery);

	            String insertQuery = "INSERT INTO auto_optimization_tbl (" + StringUtils.join(tableColumns, ", ") + ") VALUES ( "+ AgnUtils.repeatString("?", tableColumns.length, ", ") + " )";

                update(logger, insertQuery,
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

				newOptimizationId = insertIntoAutoincrementMysqlTable(logger, "optimization_id", insertStatement, columnValues);
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

            int rowsAffected = update(logger, updateQuery,
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
            	logger.warn( "Auto-optimization ID " + optimization + " could no tbe updated");
                throw new RuntimeException("Could not update Optimization with ID = " + optimization);
            }

            return optimization.getId();
        }
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean delete(ComOptimization optimization) {
		String deleteQuery = "update auto_optimization_tbl set deleted = 1 where optimization_id = ?";
		int rownums = update(logger, deleteQuery, optimization.getId());
		if( rownums > 0) {
			return true;
		}
		return false;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int deleteByCompanyID(@VelocityCheck int companyID) {
		String deleteQuery = "delete from auto_optimization_tbl where company_id = ?";
		return update(logger, deleteQuery, companyID);
	}

	@Override
	public List<ComOptimization> list(int campaignID, @VelocityCheck int companyID) {
		return list(companyID, campaignID, 0);
	}

	@Override
	public List<ComOptimization> listWorkflowManaged(int workflowId, @VelocityCheck int companyID) {
		return list(companyID, null, workflowId);
	}

	private List<ComOptimization> list(@VelocityCheck int companyID, Integer campaignId, Integer workflowId) {
		StringBuilder sqlQueryBuilder = new StringBuilder();
		List<Object> sqlParameters = new ArrayList<>();

		sqlQueryBuilder.append("SELECT ").append(StringUtils.join(tableColumns, ", "))
				.append(" FROM auto_optimization_tbl")
				.append(" WHERE (deleted = 0 OR deleted IS NULL) AND company_id = ?");
		sqlParameters.add(companyID);

		if (campaignId != null) {
			sqlQueryBuilder.append(" AND campaign_id = ?");
			sqlParameters.add(campaignId);
		}

		if (workflowId != null) {
			sqlQueryBuilder.append(" AND workflow_id = ?");
			sqlParameters.add(workflowId);
		}

		return select(logger, sqlQueryBuilder.toString(), new ComOptimizationRowMapper(), sqlParameters.toArray());
	}

	@Override
	public int countByCompanyID(@VelocityCheck int companyID) {
		String query = "select count(*) from auto_optimization_tbl where company_id = ?";
		return selectInt(logger, query, companyID);
	}

	@Override
	public List<ComOptimization> getOptimizationsForCalendar(@VelocityCheck int companyId, Date startDate, Date endDate) {
		StringBuilder querySb = new StringBuilder();
		querySb.append("SELECT ao.campaign_id,")
				.append(" CASE WHEN ao.workflow_id = 0 OR ao.workflow_id IS NULL THEN ao.shortname ELSE (SELECT w.shortname FROM workflow_tbl w WHERE w.workflow_id = ao.workflow_id) END AS shortname,")
				.append(" ao.status, ao.optimization_id, ao.result_mailing_id, ao.result_senddate, ao.workflow_id")
				.append(" FROM auto_optimization_tbl ao")
				.append(" WHERE ao.company_id = ?")
				.append(" AND (ao.deleted = 0 OR ao.deleted IS NULL)")
				.append(" AND ao.result_senddate IS NOT NULL");

		String sqlTruncDate = isOracleDB() ? "TRUNC(ao.result_senddate)" : "DATE(ao.result_senddate)";
		String sqlTruncDateParam = isOracleDB() ? "TRUNC(?)" : "DATE(?)";

		querySb.append(" AND ").append(sqlTruncDate).append(" >= ").append(sqlTruncDateParam)
				.append(" AND ").append(sqlTruncDate).append(" <= ").append(sqlTruncDateParam)
				.append(" ORDER BY ao.result_senddate");

		return select(logger, querySb.toString(), new MinimizedOptimizationRowMapper(), companyId, startDate, endDate);
	}

	@Override
	public List<ComOptimization> getOptimizationsForCalendar_New(@VelocityCheck int companyId, Date startDate, Date endDate) {
		StringBuilder querySb = new StringBuilder();
		querySb.append("SELECT ao.campaign_id,")
				.append(" CASE WHEN ao.workflow_id = 0 OR ao.workflow_id IS NULL THEN ao.shortname ELSE (SELECT w.shortname FROM workflow_tbl w WHERE w.workflow_id = ao.workflow_id) END AS shortname,")
				.append(" ao.status, ao.optimization_id, ao.result_mailing_id, ao.result_senddate, ao.workflow_id")
				.append(" FROM auto_optimization_tbl ao")
				.append(" WHERE ao.company_id = ?")
				.append(" AND (ao.deleted = 0 OR ao.deleted IS NULL)")
				.append(" AND ao.result_senddate IS NOT NULL")
				.append(" AND ao.result_senddate >= ?")
				.append(" AND ao.result_senddate <= ?")
				.append(" ORDER BY ao.result_senddate");

		return select(logger, querySb.toString(), new MinimizedOptimizationRowMapper(), companyId, startDate, endDate);
	}

	private static class MinimizedOptimizationRowMapper implements RowMapper<ComOptimization> {

		@Override
		public ComOptimization mapRow(ResultSet rs, int i) throws SQLException {
			AutoOptimizationStatus status = AutoOptimizationStatus.get(rs.getBigDecimal("status").intValue());

			ComOptimization optimization = new ComOptimizationImpl();
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
