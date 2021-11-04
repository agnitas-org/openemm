/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.TrackableLinkListItem;

public class TrackableLinkListItemRowMapper implements RowMapper<TrackableLinkListItem> {
	
	// TODO Performance improvement: Introduce static constant INSTANCE and reduce visibility of constructor to private.

	@Override
	public TrackableLinkListItem mapRow(ResultSet rs, int row) throws SQLException {
		int id = rs.getInt("url_id");
		String fullUrl = rs.getString("full_url");

		TrackableLinkListItem trackableLink = new TrackableLinkListItem(id, fullUrl);
		trackableLink.setShortname(rs.getString("shortname"));
		trackableLink.setAltText(rs.getString("alt_text"));
		trackableLink.setOriginalUrl(rs.getString("original_url"));

		return trackableLink;
	}
	
}
