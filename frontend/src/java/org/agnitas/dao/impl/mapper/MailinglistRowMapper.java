/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.MailinglistImpl;
import org.springframework.jdbc.core.RowMapper;

public class MailinglistRowMapper implements RowMapper<Mailinglist> {

	@Override
	public Mailinglist mapRow( ResultSet rs, int row) throws SQLException {
		Mailinglist mailinglist = new MailinglistImpl();

		mailinglist.setCompanyID( rs.getInt( "company_id"));
		mailinglist.setDescription( rs.getString( "description"));
		mailinglist.setId( rs.getInt( "mailinglist_id"));
		mailinglist.setShortname( rs.getString( "shortname"));

		return mailinglist;
	}


}
