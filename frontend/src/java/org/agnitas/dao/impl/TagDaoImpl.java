/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.beans.TagDefinition;
import org.agnitas.dao.TagDao;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnTagUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class TagDaoImpl extends BaseDaoImpl implements TagDao {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(TagDaoImpl.class);

	@Override
    public TagDefinition getTag(@VelocityCheck int companyID, String name) {
		return selectObjectDefaultNull(logger, "SELECT tagname, type, selectvalue FROM tag_tbl WHERE company_id IN (0, ?) AND tagname = ? ORDER BY tagname", new TagRowMapper(), companyID, name);
	}

    @Override
    public Set<String> extractDeprecatedTags(@VelocityCheck int companyID, Set<String> tagNames) {

        if (CollectionUtils.isEmpty(tagNames)) {
            return Collections.emptySet();
        } else if (tagNames.size() > 800) {
            logger.warn("Please note that more than 800 tags are checked, but maximum for SQL is 1000.");
        }

        final String query = "SELECT tagname FROM tag_tbl WHERE company_id IN (0, :companyId) AND tagname IN (:tagnames) AND deprecated = 1";

        final MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("companyId", companyID);
        parameters.addValue("tagnames", tagNames);

        final List<String> deprecatedTags = new NamedParameterJdbcTemplate(getDataSource()).queryForList(query, parameters, String.class);
        return new HashSet<>(deprecatedTags);
    }

    @Override
    public List<TagDefinition> getTagDefinitions(@VelocityCheck int companyID) {
		return select(logger, "SELECT tagname, type, selectvalue FROM tag_tbl WHERE company_id IN (0, ?) ORDER BY tagname", new TagRowMapper(), companyID);
	}
	
	@Override
    public boolean deleteTagsByCompany(@VelocityCheck int companyId) {
		if (companyId == 0) {
			return false;
		} else {
			update(logger, "DELETE FROM tag_tbl WHERE company_id = ?", companyId);
			return selectInt(logger, "SELECT COUNT(*) FROM tag_tbl WHERE company_id = ?", companyId) == 0;
		}
    }
	
	@Override
    public Map<String, TagDefinition> getTagDefinitionsMap(@VelocityCheck int companyID) {
		List<TagDefinition> tagDefinitions = getTagDefinitions(companyID);
		Map<String, TagDefinition> returnMap = new HashMap<>();
		for (TagDefinition tagDefinition : tagDefinitions) {
			returnMap.put(tagDefinition.getName(), tagDefinition);
		}
		return returnMap;
	}

	@Override
	public Map<String, String> getSelectValues(@VelocityCheck int companyID) {
		String sql = "SELECT tagname, selectvalue FROM tag_tbl WHERE company_id IN (0, ?) AND deprecated = 0 AND tagname NOT IN ('agnLASTNAME', 'agnFIRSTNAME', 'agnMAILTYPE') ORDER BY tagname";

		// Preserve sorting order.
		Map<String, String> result = new LinkedHashMap<>();

		try {
			for (Map<String, Object> map : select(logger, sql, companyID)) {

				String tagName = (String) map.get("tagname");
				String selectValue = (String) map.get("selectvalue");
				StringBuilder selectValueBuilder = new StringBuilder(StringUtils.defaultString(selectValue));

				for (String param : AgnTagUtils.getParametersForTag(tagName)) {
					selectValueBuilder.append('{').append(param).append('}');
				}

				result.put(tagName, selectValueBuilder.toString());
			}
		} catch (Exception e) {
			logger.error("getTags: " + e.getMessage(), e);
		}

		return result;
	}

	protected static class TagRowMapper implements RowMapper<TagDefinition> {
		@Override
		public TagDefinition mapRow(ResultSet resultSet, int row) throws SQLException {
			TagDefinition readObject = new TagDefinition();

			readObject.setName(resultSet.getString("tagname"));
			try {
				readObject.setTypeString(resultSet.getString("type"));
			} catch (Exception e) {
				throw new SQLException("Error in TagDefinitionType", e);
			}
			readObject.setSelectValue(resultSet.getString("selectvalue"));
			
			return readObject;
		}
	}
    
	@Deprecated /* Used by unit tests only. */
    @Override
    public int insertTag(String tagName, String tagSelectValue, String tagType, int companyId, String tagDescription) {
        List<Object> values = new ArrayList<>();
        values.add(tagName);
        values.add(tagSelectValue);
        values.add(tagType);
        values.add(companyId);
        values.add(tagDescription);
        values.add(new Date());

        if (isOracleDB()) {
        	int id = selectInt(logger, "SELECT MAX(tag_id) FROM tag_tbl") + 1;
            values.add(0, id);
            update(logger, "INSERT INTO tag_tbl (tag_id, tagname, selectvalue, type, company_id, description, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)", values.toArray());
            return id;
        } else {
        	return insertIntoAutoincrementMysqlTable(logger, "tag_id", "INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description, change_date) VALUES (?, ?, ?, ?, ?, ?)", values.toArray());
        }
    }
}
