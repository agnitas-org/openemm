/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mobile.dao.impl;

import com.agnitas.emm.core.mobile.bean.Client;
import com.agnitas.emm.core.mobile.dao.ClientDao;
import com.agnitas.dao.impl.BaseDaoImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ClientDaoImpl extends BaseDaoImpl implements ClientDao {
	
	/**
	 * Returns all Clients of this
	 * 
	 * @return
	 */
	@Override
	public List<Client> getClients() {
		String sql = "SELECT client_id, description, regex FROM client_tbl ORDER BY client_order ASC";
		return select(sql, new ClientRowMapper());
	}

	private class ClientRowMapper implements RowMapper<Client> {
		@Override
		public Client mapRow(ResultSet resultSet, int row) throws SQLException {
			Client client = new Client();
			client.setClientID(resultSet.getInt("client_id"));
			client.setDescription(resultSet.getString("description"));
			client.setRegEx(resultSet.getString("regex"));
			return client;
		}
	}
}
