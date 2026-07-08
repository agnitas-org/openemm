/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.landingpage.dao;

import java.util.List;
import java.util.Optional;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.landingpage.beans.RedirectSettings;
import org.springframework.jdbc.core.RowMapper;

public class LandingpageDaoImpl extends BaseDaoImpl implements LandingpageDao {

	private static final RowMapper<RedirectSettings> ROW_MAPPER = (rs, rowNum) -> new RedirectSettings(
			rs.getString("landingpage"),
			rs.getInt("http_code")
	);

	@Override
	public Optional<RedirectSettings> getLandingPageRedirectionForDomain(String domain) {
		if(domain == null) {
			return Optional.empty();
		} else {
			final String sql = isOracleDB()
					? "SELECT * FROM landingpage_tbl WHERE LOWER(domain) = ? OR ? LIKE '%.' || LOWER(domain) ORDER BY LENGTH(domain) DESC"
					: "SELECT * FROM landingpage_tbl WHERE LOWER(domain) = ? OR ? LIKE CONCAT('%.', LOWER(domain)) ORDER BY LENGTH(domain) DESC";
			
			final List<RedirectSettings> result = select(sql, ROW_MAPPER, domain, domain);

			return !result.isEmpty()
					? Optional.of(result.get(0))
					: Optional.empty();
		}
	}
}
