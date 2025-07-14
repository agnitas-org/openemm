/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.beans.TagDefinition;
import com.agnitas.dao.TagDao;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.text.MessageFormat.format;

public class TagDaoImpl extends BaseDaoImpl implements TagDao {

	@Override
    public TagDefinition getTag(int companyID, String name) {
		return selectObjectDefaultNull("SELECT tagname, type, selectvalue FROM tag_tbl WHERE company_id IN (0, ?) AND tagname = ? ORDER BY tagname", new TagRowMapper(), companyID, name);
	}

    @Override
    public Set<String> extractDeprecatedTags(int companyID, Set<String> tagNames) {

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
    public List<TagDefinition> getTagDefinitions(int companyID) {
		return select("SELECT tagname, type, selectvalue FROM tag_tbl WHERE company_id IN (0, ?) ORDER BY tagname", new TagRowMapper(), companyID);
	}
	
	@Override
    public boolean deleteTagsByCompany(int companyId) {
		if (companyId == 0) {
			return false;
		} else {
			update("DELETE FROM tag_tbl WHERE company_id = ?", companyId);
			return selectInt("SELECT COUNT(*) FROM tag_tbl WHERE company_id = ?", companyId) == 0;
		}
    }
	
	@Override
    public Map<String, TagDefinition> getTagDefinitionsMap(int companyID) {
		List<TagDefinition> tagDefinitions = getTagDefinitions(companyID);
		Map<String, TagDefinition> returnMap = new HashMap<>();
		for (TagDefinition tagDefinition : tagDefinitions) {
			returnMap.put(tagDefinition.getName(), tagDefinition);
		}
		return returnMap;
	}

	@Override
	public Map<String, String> getSelectValues(int companyID) {
		String sql = "SELECT tagname, selectvalue FROM tag_tbl WHERE company_id IN (0, ?) AND deprecated = 0 AND tagname NOT IN ('agnLASTNAME', 'agnFIRSTNAME', 'agnMAILTYPE') ORDER BY tagname";

		// Preserve sorting order.
		Map<String, String> result = new LinkedHashMap<>();

		try {
			for (Map<String, Object> map : select(sql, companyID)) {
				String tagName = (String) map.get("tagname");
				String selectValue = (String) map.get("selectvalue");

				result.put(tagName, selectValue);
			}
		} catch (Exception e) {
			logger.error(format("getTags: {0}", e.getMessage()), e);
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
}
