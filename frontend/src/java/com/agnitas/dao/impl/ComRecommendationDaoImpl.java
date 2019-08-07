/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.ComRecommendation;
import com.agnitas.dao.ComRecommendationDao;

/**
 * DAO handler for Recommendation-Objects
 * This class is compatible with oracle and mysql datasources and databases
 */
public class ComRecommendationDaoImpl extends BaseDaoImpl implements ComRecommendationDao {
	private static final transient Logger logger = Logger.getLogger(ComRecommendationDaoImpl.class);
	
	private static final String TABLE = "recommendation_tbl";
		
	private static final String FIELD_RECOMMENDATION_ID = "recommendation_id";
	private static final String FIELD_CREATION_DATE = "creation_date";
	private static final String FIELD_COMPANY_ID = "company_id";
	private static final String FIELD_KEYNAME = "keyname";
	private static final String FIELD_SHORTNAME = "shortname";
	private static final String FIELD_START_DATE = "start_date";
	private static final String FIELD_END_DATE = "end_date";
	
	private static final String[] FIELD_NAMES = new String[]{FIELD_RECOMMENDATION_ID, FIELD_CREATION_DATE, FIELD_COMPANY_ID, FIELD_KEYNAME, FIELD_SHORTNAME, FIELD_START_DATE, FIELD_END_DATE};

	private static final String SELECT_BY_KEYNAME = "SELECT " + StringUtils.join(FIELD_NAMES, ", ") + " FROM " + TABLE + " WHERE " + FIELD_KEYNAME + " = ?";

	@Override
	public ComRecommendation getRecommendationByKeyName(String keyName) {
		return selectObjectDefaultNull(logger, SELECT_BY_KEYNAME, new ComRecommendation_RowMapper(), keyName);
	}
	
	private class ComRecommendation_RowMapper implements RowMapper<ComRecommendation> {
		@Override
		public ComRecommendation mapRow(ResultSet resultSet, int row) throws SQLException {
			ComRecommendation readComRecommendation = new ComRecommendation();
			readComRecommendation.setId(resultSet.getInt(FIELD_RECOMMENDATION_ID));
			readComRecommendation.setCreationDate(resultSet.getTimestamp(FIELD_CREATION_DATE));
			readComRecommendation.setCompanyId(resultSet.getInt(FIELD_COMPANY_ID));
			readComRecommendation.setKeyname(resultSet.getString(FIELD_KEYNAME));
			readComRecommendation.setShortname(resultSet.getString(FIELD_SHORTNAME));
			readComRecommendation.setStartDate(resultSet.getTimestamp(FIELD_START_DATE));
			readComRecommendation.setEndDate(resultSet.getTimestamp(FIELD_END_DATE));
			return readComRecommendation;
		}
	}
}
