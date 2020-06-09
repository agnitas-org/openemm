/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.reporting.birt.external.beans.LightMailing;
import com.agnitas.reporting.birt.external.dao.LightMailingDao;

public class LightMailingDaoImpl extends BaseDaoImpl implements LightMailingDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(LightMailingDaoImpl.class);
	
	public LightMailingDaoImpl(DataSource dataSource) {
		setDataSource(dataSource);
	}	
	
	@Override
	public LightMailing getMailing(int mailingId, @VelocityCheck int companyId) {
		String targetSql = "SELECT mailing_id, shortname, description, mailinglist_id, target_expression, campaign_id, mailing_type"
			+ " FROM mailing_tbl WHERE company_id = ? AND mailing_id = ?";
				
		return selectObjectDefaultNull(logger, targetSql, new LightMailingRowMapper(), companyId, mailingId);
	}
	
	private class LightMailingRowMapper implements RowMapper<LightMailing> {
		@Override
		public LightMailing mapRow(ResultSet resultSet, int row) throws SQLException {
			LightMailing mailing = new LightMailing();
			if (resultSet.getBigDecimal("mailing_id") != null) {
				mailing.setMailingID(resultSet.getBigDecimal("mailing_id").intValue());
			}

			if (resultSet.getBigDecimal("mailinglist_id") != null) {
				mailing.setMailinglistId(resultSet.getBigDecimal("mailinglist_id").intValue());
			}

			if (StringUtils.isNotEmpty(resultSet.getString("shortname"))) {
				mailing.setShortname(resultSet.getString("shortname"));
			}

			if (StringUtils.isNotEmpty(resultSet.getString("target_expression"))) {
				mailing.setTargetExpression(resultSet.getString("target_expression"));
			}

			if (StringUtils.isNotEmpty(resultSet.getString("description"))) {
				mailing.setDescription(resultSet.getString("description"));
			}

			if (StringUtils.isNotEmpty(resultSet.getString("campaign_id"))) {
				mailing.setArchiveId(resultSet.getBigDecimal("campaign_id").intValue());
			}

			if (StringUtils.isNotEmpty(resultSet.getString("mailing_type"))) {
				mailing.setMailingType(resultSet.getBigDecimal("mailing_type").intValue());
			}

			return mailing;
		}
	}
}
