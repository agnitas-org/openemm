/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import org.agnitas.service.UserActivityLogService;
import org.apache.log4j.Logger;
import org.apache.struts.actions.DispatchAction;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;

public class BaseDispatchActionSupport extends DispatchAction {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(BaseDispatchActionSupport.class);
	
	/** Service for accessing user activity log. */
	private UserActivityLogService userActivityLogService;

	@Required
	public void setUserActivityLogService(UserActivityLogService userActivityLogService) {
		this.userActivityLogService = userActivityLogService;
	}
    
	protected void writeUserActivityLog(ComAdmin admin, String action, String description) {
    	try {
			if (userActivityLogService != null) {
				userActivityLogService.writeUserActivityLog(admin, action, description);
			} else {
				logger.error("Missing userActivityLogService in " + this.getClass().getSimpleName());
				logger.info("Userlog: " + admin.getUsername() + " " + action + " " +  description);
			}
		} catch (Exception e) {
			logger.error("Error writing ActivityLog: " + e.getMessage(), e);
			logger.info("Userlog: " + admin.getUsername() + " " + action + " " +  description);
		}
    }
    
    protected void writeUserActivityLog(ComAdmin admin, String action, int description)  {
    	try {
	    	if (userActivityLogService != null) {
	    		userActivityLogService.writeUserActivityLog(admin, action, Integer.toString(description));
	    	} else {
	    		logger.error("Missing userActivityLogService in " + this.getClass().getSimpleName());
	    		logger.info("Userlog: " + admin.getUsername() + " " + action + " " +  description);
	    	}
		} catch (Exception e) {
			logger.error("Error writing ActivityLog: " + e.getMessage(), e);
			logger.info("Userlog: " + admin.getUsername() + " " + action + " " +  description);
		}
    }
}
