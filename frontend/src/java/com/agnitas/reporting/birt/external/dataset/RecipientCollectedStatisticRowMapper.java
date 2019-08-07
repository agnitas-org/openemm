/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

public class RecipientCollectedStatisticRowMapper implements RowMapper<RecipientCollectedStatisticRow> {
	private Map<Integer, String> mailingListNamesById;
	
	public RecipientCollectedStatisticRowMapper(Map<Integer, String> mailinglistNamesById) {
		this.mailingListNamesById = mailinglistNamesById;
	}
	
	@Override
	public RecipientCollectedStatisticRow mapRow(ResultSet resultSet, int rowNum) throws SQLException {
		RecipientCollectedStatisticRow row = new RecipientCollectedStatisticRow();
		row.setMailingList(mailingListNamesById.get(resultSet.getInt("mailinglist_id")));
		row.setMailingListId(resultSet.getInt("mailinglist_id"));
		row.setCategory(resultSet.getString("category"));
		row.setCategoryindex(resultSet.getInt("category_index"));
		row.setTargetgroup(resultSet.getString("targetgroup"));
		row.setTargetgroupindex(resultSet.getInt("targetgroup_index"));
		row.setCount(resultSet.getInt("value"));
		row.setRate(resultSet.getDouble("rate"));
		return row;
	}
}
