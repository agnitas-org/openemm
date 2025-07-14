/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.Date;

/**
 * Server commands which are used in interaction between servers through database.
 *
 */
public class ServerCommand{
    public enum Server {
        EMM,
        RDIR,
        WS,
        STATISTICS,
        JOBQUEUE,
        REST,
        BACKEND_CONSOLE,
        BACKEND_MAILER,
        BACKEND_MAILLOOP,
        BACKEND_MERGER,
        BACKEND_RDIR,
        RUNTIME,
        OTHER,
        ALL;
        
        public static Server getServerByName(String serverString) {
        	if (serverString != null) {
	        	for (Server serverItem : Server.values()) {
	    			if (serverItem.name().replace("-", "").equalsIgnoreCase(serverString.replace("-", ""))) {
	    				return serverItem;
	    			}
	    		}
	    		
	    	}
    		return null;
        }
    }

    /**
     * By now only commands are allowed that are also automatically served when the server is restarted
     */
    public enum Command {
        CLEAR_RDIR_CACHE,
        RELOAD_LICENSE_DATA
    }

	private Server serverName;
    private Command command;
    private Date executionDate;
    private int adminID;
    private String description;
    
    public ServerCommand() {
    	// empty constructor
    }

    public ServerCommand(Server serverName, Command command, Date executionDate, int adminID, String description) {
		this.serverName = serverName;
		this.command = command;
		this.executionDate = executionDate;
		this.adminID = adminID;
		this.description = description;
	}

    public Server getServerName() {
        return serverName;
    }

    public void setServerName(Server serverName) {
        this.serverName = serverName;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }

    public int getAdminID() {
        return adminID;
    }

    public void setAdminID(int adminID) {
        this.adminID = adminID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
