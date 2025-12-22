/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.stat.dao.impl;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.stat.beans.MailingStatisticTgtGrp;
import com.agnitas.emm.core.stat.beans.StatisticValue;
import com.agnitas.emm.core.stat.dao.MailingStatTgtGrpDao;
import com.agnitas.dao.impl.BaseDaoImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MailingStatTgtGrpDaoImpl extends BaseDaoImpl implements MailingStatTgtGrpDao {
	
	@Override
	@DaoUpdateReturnValueCheck
	public int saveMalingStatTgtGrp(MailingStatisticTgtGrp stat) {
		int newId;
		if (isOracleDB()) {
			newId = selectInt("SELECT mailing_stat_tgtgrp_tbl_seq.NEXTVAL FROM dual");
			update("INSERT INTO mailing_statistic_tgtgrp_tbl" +
			               " (mailing_stat_tgtgrp_id, mailing_stat_job_id, mailing_id, target_group_id, revenue)" +
					       " VALUES (?, ?, ?, ?, ?)",
					newId, stat.getJobId(), stat.getMailingId(), stat.getTargetGroupId(), stat.getRevenue());
       } else {
        	String insertStatement = "INSERT INTO mailing_statistic_tgtgrp_tbl" +
		               " (mailing_stat_job_id, mailing_id, target_group_id, revenue)" +
				       " VALUES (?, ?, ?, ?)";
			
	        Object[] values = new Object[] { stat.getJobId(), stat.getMailingId(), stat.getTargetGroupId(), stat.getRevenue() };

	        newId = insert("mailing_stat_tgtgrp_id", insertStatement, values);
 		}
		
		String insStm = "INSERT INTO mailing_statistic_value_tbl (mailing_stat_tgtgrp_id, category_index, stat_value, stat_quotient)"
				+ "  VALUES (?, ?, ?, ?)";
		List<Object[]> parametersValue = new ArrayList<>();
		for (Entry<Integer, StatisticValue> entry : stat.getStatValues().entrySet()) {
			parametersValue.add(new Object[]{newId, entry.getKey(), entry.getValue().getValue(), 
					entry.getValue().getQuotient()});
		}
		batchupdate(insStm, parametersValue);
		
		return newId;
	}

	@Override
	public MailingStatisticTgtGrp getMailingStatTgtGrpByJobId(int jobId, int targetGroupId) {
		MailingStatisticTgtGrp statTgtGrp = selectObject("SELECT * FROM mailing_statistic_tgtgrp_tbl WHERE mailing_stat_job_id = ? AND target_group_id = ?",
				new MailingStatTgtGrpMapper(), jobId, targetGroupId);
		List<Map<String,Object>> stats = select("SELECT * FROM mailing_statistic_value_tbl WHERE mailing_stat_tgtgrp_id = ?",
				statTgtGrp.getId());
		for (Map<String, Object> map : stats) {
			if (isOracleDB()) {
				statTgtGrp.getStatValues().put(((Number)map.get("category_index")).intValue(), 
						new StatisticValue(((Number)map.get("stat_value")).intValue(), 
										   ((Number)map.get("stat_quotient")).doubleValue()));
			} else {
				statTgtGrp.getStatValues().put((Integer)map.get("category_index"), 
						new StatisticValue((Integer)map.get("stat_value"), 
										   ((Number)map.get("stat_quotient")).doubleValue()));
			}
		}
		return statTgtGrp;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public final void removeExpiredMailingStatTgtGrp(final ZonedDateTime threshold) {
		/*
		 * Deletes by job ID based on given threshold.
		 */
		final Date thresholdDate = Date.from(threshold.toInstant());
		final String queryJobsSql = "SELECT mailing_stat_job_id FROM mailing_statistic_job_tbl WHERE creation_date < ?";

		// Step 1: Deleted values
		final String targetGroupIdsSql = "SELECT mailing_stat_tgtgrp_id FROM mailing_statistic_tgtgrp_tbl WHERE mailing_stat_job_id IN (" + queryJobsSql + ")";
		final String deleteValuesSql = 
				"DELETE FROM mailing_statistic_value_tbl "
				+ "WHERE mailing_stat_tgtgrp_id IN (" + targetGroupIdsSql + ")";
		update(deleteValuesSql, thresholdDate);
		
		// Step 2: Delete target groups
		final String deleteTargetGroupsSql = "DELETE FROM mailing_statistic_tgtgrp_tbl WHERE mailing_stat_job_id IN (" + queryJobsSql + ")";
		update(deleteTargetGroupsSql, thresholdDate);
	}

    private static class MailingStatTgtGrpMapper implements RowMapper<MailingStatisticTgtGrp> {

		@Override
		public MailingStatisticTgtGrp mapRow(ResultSet resultSet, int row) throws SQLException {
			MailingStatisticTgtGrp stat = new MailingStatisticTgtGrp();
			stat.setId(resultSet.getBigDecimal("mailing_stat_tgtgrp_id").intValue());
			stat.setJobId(resultSet.getBigDecimal("mailing_stat_job_id").intValue());
			stat.setMailingId(resultSet.getBigDecimal("mailing_id").intValue());
			stat.setTargetGroupId(resultSet.getBigDecimal("target_group_id").intValue());
			stat.setCreationDate(resultSet.getTimestamp("creation_date"));
			stat.setRevenue(resultSet.getDouble("revenue"));

			return stat;
		}
    }

}
