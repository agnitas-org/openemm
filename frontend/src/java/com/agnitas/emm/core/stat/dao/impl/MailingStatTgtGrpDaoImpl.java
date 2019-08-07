/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.stat.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.stat.beans.MailingStatisticTgtGrp;
import com.agnitas.emm.core.stat.beans.StatisticValue;
import com.agnitas.emm.core.stat.dao.MailingStatTgtGrpDao;

public class MailingStatTgtGrpDaoImpl extends BaseDaoImpl implements MailingStatTgtGrpDao {
	private static final transient Logger logger = Logger.getLogger(MailingStatTgtGrpDaoImpl.class);

	@Override
	@DaoUpdateReturnValueCheck
	public int saveMalingStatTgtGrp(MailingStatisticTgtGrp stat) {
		int newId = 0;
		if (isOracleDB()) {
			newId = selectInt(logger, "SELECT mailing_stat_tgtgrp_tbl_seq.NEXTVAL FROM dual");
			update(logger, "INSERT INTO mailing_statistic_tgtgrp_tbl" +
			               " (mailing_stat_tgtgrp_id, mailing_stat_job_id, mailing_id, target_group_id, revenue)" +
					       " VALUES (?, ?, ?, ?, ?)",
					newId, stat.getJobId(), stat.getMailingId(), stat.getTargetGroupId(), stat.getRevenue());
       } else {
        	String insertStatement = "INSERT INTO mailing_statistic_tgtgrp_tbl" +
		               " (mailing_stat_job_id, mailing_id, target_group_id, revenue)" +
				       " VALUES (?, ?, ?, ?)";
			
	        Object[] values = new Object[] { stat.getJobId(), stat.getMailingId(), stat.getTargetGroupId(), stat.getRevenue() };

	        newId = insertIntoAutoincrementMysqlTable(logger, "mailing_stat_tgtgrp_id", insertStatement, values);
 		}
		
		String insStm = "INSERT INTO mailing_statistic_value_tbl (mailing_stat_tgtgrp_id, category_index, stat_value, stat_quotient)"
				+ "  VALUES (?, ?, ?, ?)";
		List<Object[]> parametersValue = new ArrayList<>();
		for (Entry<Integer, StatisticValue> entry : stat.getStatValues().entrySet()) {
			parametersValue.add(new Object[]{newId, entry.getKey(), entry.getValue().getValue(), 
					entry.getValue().getQuotient()});
		}
		batchupdate(logger, insStm, parametersValue);
		
		return newId;
	}

	@Override
	public MailingStatisticTgtGrp getMailingStatTgtGrpByJobId(int jobId, int targetGroupId) {
		MailingStatisticTgtGrp statTgtGrp = selectObject(logger, "SELECT * FROM mailing_statistic_tgtgrp_tbl WHERE mailing_stat_job_id = ? AND target_group_id = ?", 
				new MailingStatTgtGrpMapper(), jobId, targetGroupId);
		List<Map<String,Object>> stats = select(logger, "SELECT * FROM mailing_statistic_value_tbl WHERE mailing_stat_tgtgrp_id = ?", 
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
	public void removeExpiredMailingStatTgtGrp(int maxAgeSeconds) {
		List<MailingStatisticTgtGrp> tgtGrps;
		if (isOracleDB()) {
			tgtGrps = select(logger, "SELECT * FROM mailing_statistic_tgtgrp_tbl WHERE creation_date < SYSDATE - ?/24/60/60", 
								new MailingStatTgtGrpMapper(), maxAgeSeconds);
		} else {
			tgtGrps = select(logger, "SELECT * FROM mailing_statistic_tgtgrp_tbl WHERE creation_date < (NOW() - INTERVAL ? SECOND)", 
								new MailingStatTgtGrpMapper(), maxAgeSeconds);
		}
		
		if (tgtGrps == null || tgtGrps.size() < 1) {
			return;
		}
		
		StringBuffer tgtGrpsStr = new StringBuffer("(");
		boolean firstTime = true;
		for (MailingStatisticTgtGrp mailingStatTgtGrp : tgtGrps) {
			if (!firstTime) {
				tgtGrpsStr.append(",");
			} else {
				firstTime = false;
			}
			tgtGrpsStr.append(mailingStatTgtGrp.getId());
		}
		tgtGrpsStr.append(")");
		update(logger, "DELETE FROM mailing_statistic_value_tbl WHERE mailing_stat_tgtgrp_id in " + tgtGrpsStr.toString());
		update(logger, "DELETE FROM mailing_statistic_tgtgrp_tbl WHERE mailing_stat_tgtgrp_id in " + tgtGrpsStr.toString());
	}

    private class MailingStatTgtGrpMapper implements RowMapper<MailingStatisticTgtGrp> {

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
