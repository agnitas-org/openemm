/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.dao.ServerMessageDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.ServerCommand;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Default implementation of ServerMessageDao
 */
public class ServerMessageDaoImpl extends BaseDaoImpl implements ServerMessageDao {
	
    @Override
	@DaoUpdateReturnValueCheck
    public void pushCommand(ServerCommand command) {
        String query = "INSERT INTO server_command_tbl(command, server_name, execution_date, admin_id, description, timestamp) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        update(query, command.getCommand().toString(), command.getServerName().toString(), command.getExecutionDate(), command.getAdminID(), command.getDescription());
    }

    @Override
    public List<ServerCommand> getCommand(Date since, Date till, ServerCommand.Server server, ServerCommand.Command command) {
    	if (since == null) {
    		since = DateUtilities.getDateOfHoursAgo(1);
    	}

        String query = "SELECT command, server_name, execution_date, admin_id, description FROM server_command_tbl WHERE (? < execution_date AND execution_date <= ?) AND (server_name = ? OR server_name = 'ALL') AND command = ?";
        return select(query, new ServerCommandRowMapper(), since, till, server.toString(), command.toString());
    }
    
    @Override
    public void cleanupOldCommands() {
        update("DELETE FROM server_command_tbl WHERE execution_date < ?", DateUtilities.getDateOfDaysAgo(1));
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
