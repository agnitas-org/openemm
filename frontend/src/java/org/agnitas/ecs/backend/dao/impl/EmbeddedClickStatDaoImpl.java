/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.ecs.backend.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.ecs.EcsGlobals;
import org.agnitas.ecs.backend.beans.ClickStatColor;
import org.agnitas.ecs.backend.beans.ClickStatInfo;
import org.agnitas.ecs.backend.beans.impl.ClickStatColorImpl;
import org.agnitas.ecs.backend.beans.impl.ClickStatInfoImpl;
import org.agnitas.ecs.backend.dao.EmbeddedClickStatDao;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

/**
 * Implementation of {@link org.agnitas.ecs.backend.dao.EmbeddedClickStatDao}
 * 
 * Selects the data for the heatmap.
 */
public class EmbeddedClickStatDaoImpl extends BaseDaoImpl implements EmbeddedClickStatDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(EmbeddedClickStatDaoImpl.class);
	
	@Override
	public List<ClickStatColor> getClickStatColors(@VelocityCheck int companyId) {
		String sqlStatement = "SELECT * FROM click_stat_colors_tbl WHERE company_id = ? ORDER BY range_end";
        return select(logger, sqlStatement, new ClickStatColor_RowMapper(), companyId);
	}

	@Override
	public ClickStatInfo getClickStatInfo(@VelocityCheck int companyId, int mailingId, int mode) throws Exception {
        try {
			String sqlClicksPerMail;
			String sqlClicksPerLink;
			
			if (mode == EcsGlobals.MODE_GROSS_CLICKS) {
				sqlClicksPerMail = "SELECT COUNT(customer_id) clicks"
					+ " FROM rdirlog_" + companyId + "_tbl"
					+ " WHERE mailing_id = ?";
				
				sqlClicksPerLink = "SELECT url_id, COUNT(customer_id) clicks"
					+ " FROM rdirlog_" + companyId + "_tbl"
					+ " WHERE mailing_id = ?"
					+ " GROUP BY url_id"
					+ " ORDER BY clicks DESC";
			} else if (mode == EcsGlobals.MODE_NET_CLICKS) {
				sqlClicksPerMail = "SELECT COUNT(DISTINCT customer_id) clicks"
					+ " FROM rdirlog_" + companyId + "_tbl"
					+ " WHERE mailing_id = ?";
				
				sqlClicksPerLink = "SELECT url_id, COUNT(DISTINCT customer_id) clicks"
					+ " FROM rdirlog_" + companyId + "_tbl"
					+ " WHERE mailing_id = ?"
					+ " GROUP BY url_id"
					+ " ORDER BY clicks DESC";
			} else {
				throw new Exception("Invalid mode: " + mode);
			}

			int clicksPerMail = selectInt(logger, sqlClicksPerMail, mailingId);
			List<Map<String, Object>> resultClicksPerLink = select(logger, sqlClicksPerLink, mailingId);

			ClickStatInfo clickStatInfo = new ClickStatInfoImpl();
			for (Map<String, Object> row : resultClicksPerLink) {
				int urlId = ((Number) row.get("url_id")).intValue();
				int clicks = ((Number) row.get("clicks")).intValue();
				double clicksPercent = 0;
				if (clicksPerMail > 0) {
					clicksPercent = ((double) clicks / (double) clicksPerMail) * 100;
				}
				clickStatInfo.addURLInfo(urlId, clicks, clicksPerMail, clicksPercent);
			}
			
			return clickStatInfo;
		} catch (Exception e) {
			logger.error("Cannot getClickStatInfo: " + e.getMessage(), e);
			throw e;
		}
	}
	
    protected class ClickStatColor_RowMapper implements RowMapper<ClickStatColor> {
		@Override
		public ClickStatColor mapRow(ResultSet resultSet, int row) throws SQLException {
			ClickStatColor colorBean = new ClickStatColorImpl();
			colorBean.setId((int) resultSet.getLong("id"));
			colorBean.setCompanyId((int) resultSet.getLong("company_id"));
			colorBean.setColor(resultSet.getString("color"));
			colorBean.setRangeStart(resultSet.getBigDecimal("range_start").doubleValue());
			colorBean.setRangeEnd(resultSet.getBigDecimal("range_end").doubleValue());
			return colorBean;
		}
	}
}
