/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.ComEmmLayoutBaseDao;

/**
 * DAO handler for EmmLayoutBase-Objects
 */
public class ComEmmLayoutBaseDaoImpl extends BaseDaoImpl implements ComEmmLayoutBaseDao {
	private static final transient Logger logger = Logger.getLogger(ComEmmLayoutBaseDaoImpl.class);
	
	private static final String TABLE = "emm_layout_base_tbl";

	private static final String FIELD_BASE_ID = "layout_base_id";
	private static final String FIELD_BASE_URL = "base_url";
	private static final String FIELD_SHORTNAME = "shortname";
	private static final String FIELD_MENU_POSITION = "menu_position";
	private static final String FIELD_LIVEPREVIEW_POSITION = "livepreview_position";
	private static final String FIELD_COMPANY_ID = "company_id";
	
	private static final String[] FIELD_NAMES = new String[]{FIELD_BASE_ID, FIELD_BASE_URL, FIELD_SHORTNAME, FIELD_MENU_POSITION, FIELD_LIVEPREVIEW_POSITION, FIELD_COMPANY_ID};	
	private static final String SELECT = "SELECT " + StringUtils.join(FIELD_NAMES, ", ") + " FROM " + TABLE + " WHERE " + FIELD_BASE_ID + " = ? AND " + FIELD_COMPANY_ID + " = ?";
	private static final String SELECT_BY_COMPANYID = "SELECT " + StringUtils.join(FIELD_NAMES, ", ") + " FROM " + TABLE + " WHERE " + FIELD_COMPANY_ID + " = ? OR " + FIELD_COMPANY_ID + " = 0 ORDER BY " + FIELD_COMPANY_ID + ", " + FIELD_MENU_POSITION;

	private static final Map<String, String> LAYOUT_CACHE = new HashMap<>();

	@Override
	public EmmLayoutBase getEmmLayoutBase(@VelocityCheck int companyID, int emmLayoutBaseID) {
		if (emmLayoutBaseID == 0) {
			companyID = 0;
		}

		try {
			List<EmmLayoutBase> result = select(logger, SELECT, new ComEmmLayoutBaseRowMapper(), emmLayoutBaseID, companyID);
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
			return select(logger, SELECT_BY_COMPANYID, new ComEmmLayoutBaseRowMapper(), companyID);
		} catch (Exception e) {
			logger.debug("Error:" + e);
			return new ArrayList<>();
		}
	}
	
	protected class ComEmmLayoutBaseRowMapper implements RowMapper<EmmLayoutBase> {
		@Override
		public EmmLayoutBase mapRow(ResultSet resultSet, int row) throws SQLException {
			int id = resultSet.getInt(FIELD_BASE_ID);
			String baseUrl = resultSet.getString(FIELD_BASE_URL);
			
			EmmLayoutBaseImpl layoutBase = new EmmLayoutBaseImpl(id, baseUrl);
			
			layoutBase.setShortname(resultSet.getString(FIELD_SHORTNAME));
			layoutBase.setMenuPosition(resultSet.getInt(FIELD_MENU_POSITION));
			layoutBase.setLivepreviewPosition(resultSet.getInt(FIELD_LIVEPREVIEW_POSITION));
			
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
}
