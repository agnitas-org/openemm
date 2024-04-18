/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.ecs.backend.dao.impl;

import com.agnitas.emm.ecs.EcsModeType;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.ecs.backend.beans.ClickStatInfo;
import org.agnitas.ecs.backend.beans.impl.ClickStatInfoImpl;
import org.agnitas.ecs.backend.dao.EmbeddedClickStatDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link org.agnitas.ecs.backend.dao.EmbeddedClickStatDao}
 * 
 * Selects the data for the heatmap.
 */
public class EmbeddedClickStatDaoImpl extends BaseDaoImpl implements EmbeddedClickStatDao {

	private static final Logger logger = LogManager.getLogger(EmbeddedClickStatDaoImpl.class);
	
	@Override
	public ClickStatInfo getClickStatInfo(int companyId, int mailingId, int mode, int deviceClass) throws Exception {
        try {
			String sqlClicksPerMail;
			String sqlClicksPerLink;
			List<Object> optionsClicksPerMail = new ArrayList<>();
			List<Object> optionsClicksPerLink = new ArrayList<>();
			
			if (EcsModeType.GROSS_CLICKS.getId() == mode) {
				sqlClicksPerMail = "SELECT COUNT(customer_id) clicks"
					+ " FROM rdirlog_" + companyId + "_tbl"
					+ " WHERE mailing_id = ?";
				optionsClicksPerMail.add(mailingId);

				sqlClicksPerMail += getDeviceClassClause(deviceClass, optionsClicksPerMail);
				
				sqlClicksPerLink = "SELECT url_id, COUNT(customer_id) clicks"
					+ " FROM rdirlog_" + companyId + "_tbl"
					+ " WHERE mailing_id = ?";
				optionsClicksPerLink.add(mailingId);
				
				sqlClicksPerLink += getDeviceClassClause(deviceClass, optionsClicksPerLink);
				sqlClicksPerLink += " GROUP BY url_id"
					+ " ORDER BY clicks DESC";
			} else if (EcsModeType.NET_CLICKS.getId() == mode) {
				sqlClicksPerMail = "SELECT COUNT(DISTINCT customer_id) clicks"
					+ " FROM rdirlog_" + companyId + "_tbl"
					+ " WHERE mailing_id = ?";
				optionsClicksPerMail.add(mailingId);
				
				sqlClicksPerLink = "SELECT url_id, COUNT(DISTINCT customer_id) clicks"
					+ " FROM rdirlog_" + companyId + "_tbl"
					+ " WHERE mailing_id = ?";
				optionsClicksPerLink.add(mailingId);
				
				sqlClicksPerLink += getDeviceClassClause(deviceClass, optionsClicksPerLink);
				sqlClicksPerLink += " GROUP BY url_id"
					+ " ORDER BY clicks DESC";
			} else {
				throw new Exception("Invalid mode: " + mode);
			}

			int clicksPerMail = selectInt(logger, sqlClicksPerMail, optionsClicksPerMail.toArray());
			List<Map<String, Object>> resultClicksPerLink = select(logger, sqlClicksPerLink, optionsClicksPerLink.toArray());

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
	
    private String getDeviceClassClause(int deviceClass, List<Object> options) {
		if (deviceClass > 0) {
			String sql = " AND device_class_id = ?";
			options.add(deviceClass);
			return sql;
		}
		return "";
	}
}
