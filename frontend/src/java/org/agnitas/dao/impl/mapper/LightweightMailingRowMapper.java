/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.beans.LightweightMailingImpl;
import org.springframework.jdbc.core.RowMapper;

/**
 * {@link RowMapper} for {@link LightweightMailing}.
 */
public class LightweightMailingRowMapper implements RowMapper<LightweightMailing> {

	@Override
	public LightweightMailing mapRow(ResultSet resultSet, int index) throws SQLException {
		LightweightMailing mailing = new LightweightMailingImpl();

		mailing.setCompanyID(resultSet.getInt("company_id"));
		mailing.setMailingID(resultSet.getInt("mailing_id"));
		mailing.setShortname(resultSet.getString("shortname") != null ? resultSet.getString("shortname") : "");
		mailing.setMailingDescription(resultSet.getString("description") != null ? resultSet.getString("description") : "");

		return mailing;
	}

}
