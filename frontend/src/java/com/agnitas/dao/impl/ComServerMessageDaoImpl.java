/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.util.ServerCommand;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.ComServerMessageDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;

/**
 * Default implementation of ComServerMessageDao
 */
public class ComServerMessageDaoImpl extends BaseDaoImpl implements ComServerMessageDao {
    private static final transient Logger logger = Logger.getLogger(ComServerMessageDaoImpl.class);

    @Override
	@DaoUpdateReturnValueCheck
    public void pushCommand(ServerCommand command) {
        String query = "INSERT INTO server_command_tbl(command, server_name, execution_date, admin_id, description, timestamp) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        update(logger, query, command.getCommand().toString(), command.getServerName().toString(), command.getExecutionDate(), command.getAdminID(), command.getDescription());
    }

    @Override
    public List<ServerCommand> getCommand(Date since, Date till, ServerCommand.Server server, ServerCommand.Command command) {
    	if (since == null) {
	        String query = "SELECT command, server_name, execution_date, admin_id, description FROM server_command_tbl WHERE execution_date <= ? AND (server_name = ? OR server_name = 'ALL') AND command = ?";
	        return select(logger, query, new ServerCommandRowMapper(), till, server.toString(), command.toString());
    	} else {
	        String query = "SELECT command, server_name, execution_date, admin_id, description FROM server_command_tbl WHERE (? < execution_date AND execution_date <= ?) AND (server_name = ? OR server_name = 'ALL') AND command = ?";
            return select(logger, query, new ServerCommandRowMapper(), since, till, server.toString(), command.toString());
    	}
    }

    private class ServerCommandRowMapper implements RowMapper<ServerCommand> {
        @Override
        public ServerCommand mapRow(ResultSet rs, int rowNum) throws SQLException {
            ServerCommand serverCommand = new ServerCommand();
            serverCommand.setCommand(ServerCommand.Command.valueOf(rs.getString("command")));
            serverCommand.setServerName(ServerCommand.Server.valueOf(rs.getString("server_name")));
            serverCommand.setExecutionDate(rs.getTimestamp("execution_date"));
            serverCommand.setAdminID(rs.getInt("admin_id"));
            serverCommand.setDescription(rs.getString("description"));
            return serverCommand;
        }
    }
}
