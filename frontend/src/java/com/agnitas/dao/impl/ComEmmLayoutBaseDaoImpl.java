/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.agnitas.beans.EmmLayoutBase;
import org.agnitas.beans.impl.EmmLayoutBaseImpl;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.ComEmmLayoutBaseDao;

/**
 * DAO handler for EmmLayoutBase-Objects
 */
public class ComEmmLayoutBaseDaoImpl extends BaseDaoImpl implements ComEmmLayoutBaseDao {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ComEmmLayoutBaseDaoImpl.class);
	
	private static final Map<String, String> LAYOUT_CACHE = new HashMap<>();

	@Override
	public EmmLayoutBase getEmmLayoutBase(@VelocityCheck int companyID, int emmLayoutBaseID) {
		if (emmLayoutBaseID == 0) {
			companyID = 0;
		}

		try {
			List<EmmLayoutBase> result = select(logger, "SELECT layout_base_id, base_url, shortname, menu_position, livepreview_position, company_id, theme_type FROM emm_layout_base_tbl WHERE layout_base_id = ? AND company_id IN(0, ?)", new ComEmmLayoutBaseRowMapper(), emmLayoutBaseID, companyID);
			if (result.size() > 0) {
				return result.get(0);
			} else {
				logger.info("No LayoutBase for id " + emmLayoutBaseID + " and CompanyID " + companyID + ", using default ('assets/core')");
				return new EmmLayoutBaseImpl(0, "assets/core");
			}
		} catch (Exception e) {
			logger.info("No LayoutBase for id " + emmLayoutBaseID + " and CompanyID " + companyID + ", using default ('assets/core')", e);
			return new EmmLayoutBaseImpl(0, "assets/core");
		}
	}

	@Override
	public List<EmmLayoutBase> getEmmLayoutsBase(@VelocityCheck int companyID) {
		try {
			return select(logger, "SELECT layout_base_id, base_url, shortname, menu_position, livepreview_position, company_id, theme_type FROM emm_layout_base_tbl WHERE company_id = ? OR company_id = 0 ORDER BY company_id, menu_position", new ComEmmLayoutBaseRowMapper(), companyID);
		} catch (Exception e) {
			logger.debug("Error:" + e);
			return new ArrayList<>();
		}
	}
	
	protected class ComEmmLayoutBaseRowMapper implements RowMapper<EmmLayoutBase> {
		@Override
		public EmmLayoutBase mapRow(ResultSet resultSet, int row) throws SQLException {
			int id = resultSet.getInt("layout_base_id");
			String baseUrl = resultSet.getString("base_url");
			
			EmmLayoutBaseImpl layoutBase = new EmmLayoutBaseImpl(id, baseUrl);
			
			layoutBase.setShortname(resultSet.getString("shortname"));
			layoutBase.setMenuPosition(resultSet.getInt("menu_position"));
			layoutBase.setLivepreviewPosition(resultSet.getInt("livepreview_position"));
			layoutBase.setThemeType(EmmLayoutBase.ThemeType.valueOf(resultSet.getInt("theme_type")));
			
			return layoutBase;
		}
	}
	
	@Override
	public boolean deleteLayoutByCompany(@VelocityCheck int companyID) {
		try {
			if (companyID > 0) {
				update(logger, "DELETE FROM emm_layout_base_tbl WHERE company_id = ?", companyID);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public String getLayoutDirectory(String requestDomain) {
		try {
			String directory = LAYOUT_CACHE.get(requestDomain);
			if (directory == null) {
				directory = selectWithDefaultValue(logger, "SELECT layoutdirectory FROM emm_layout_base_tbl WHERE domain = ?", String.class, "assets/core", requestDomain.toLowerCase());
				if (StringUtils.isBlank(directory)) {
					directory = "assets/core";
				}
				LAYOUT_CACHE.put(requestDomain, directory);
			}
			return directory;
		} catch (Exception e) {
			return "assets/core";
		}
	}

	@Override
	public Map<String, Integer> getMappedDomains() {
		try {
			Map<String, Integer> returnMap = new HashMap<>();
			List<Map<String,Object>> result = select(logger, "SELECT domain, company_id FROM emm_layout_base_tbl WHERE domain IS NOT NULL");
			for (Map<String,Object> row : result) {
				int companyID = ((Number) row.get("company_id")).intValue();
				String domain = (String) row.get("domain");
				returnMap.put(domain, companyID);
			}
			return returnMap;
		} catch (Exception e) {
			logger.debug("Error:" + e);
			return new HashMap<>();
		}
	}
}
