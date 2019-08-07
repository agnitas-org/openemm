/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;

import com.agnitas.beans.ComAdmin;
import org.springframework.beans.factory.annotation.Required;

public class BaseDispatchAction extends DispatchAction {
    private static final Logger logger = Logger.getLogger(BaseDispatchAction.class);

    private UserActivityLogService userActivityLogService;

    protected List<String> getInitializedColumnWidthList(int size) {
        List<String> columnWidthList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            columnWidthList.add("-1");
        }
        return columnWidthList;
    }

    protected void showSavedMessage(HttpServletRequest request) {
    	showSavedMessage(request, "");
    }
    
    protected void showSavedMessage(HttpServletRequest request, String message) {
        ActionMessages messages = new ActionMessages();
        if (message.isEmpty()) {
        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
        } else {
        	messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(message));
        }
        saveMessages(request, messages);
    }

    protected void writeUserActivityLog(ComAdmin admin, String action, String description)  {
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
    
    protected void writeUserActivityLog(ComAdmin admin, UserAction userAction) {
        if (Objects.nonNull(userActivityLogService)) {
            userActivityLogService.writeUserActivityLog(admin, userAction, logger);
        } else {
            logger.error("Missing userActivityLogService in " + this.getClass().getSimpleName());
            logger.info(String.format("Userlog: %s %s %s", admin.getUsername(), userAction.getAction(),
                    userAction.getDescription()));
        }
    }

    @Required
    public final void setUserActivityLogService(UserActivityLogService userActivityLogService) {
        this.userActivityLogService = userActivityLogService;
    }
}
