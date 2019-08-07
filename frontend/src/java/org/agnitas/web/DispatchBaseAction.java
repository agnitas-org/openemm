/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.web.forms.StrutsFormBase;
import org.apache.log4j.Logger;
import org.apache.struts.actions.DispatchAction;

import com.agnitas.beans.ComAdmin;

public class DispatchBaseAction extends DispatchAction {
    private static final Logger logger = Logger.getLogger(DispatchBaseAction.class);

    private UserActivityLogService userActivityLogService;
	
    /**
     * Initialize the list which keeps the current width of the columns, with a default value of '-1'
     * A JavaScript in the corresponding jsp will set the style.width of the column.
     *
     * @param size number of columns
     * @return the list of column width
     */
    protected List<String> getInitializedColumnWidthList(int size) {
		List<String> columnWidthList = new ArrayList<>();
		for ( int i=0; i< size ; i++ ) {
			columnWidthList.add("-1");
		}
		return columnWidthList;
	}
    

    /**
     * Checks if sort property is contained in request, if yes - puts it also to form, if not - gets it from form;
     * returns the obtained sort property.
     *
     * @param request servlet request object
     * @param aForm StrutsFormBase object
     * @return String value of sort
     */
    protected String getSort(HttpServletRequest request, StrutsFormBase aForm) {
        String sort = request.getParameter("sort");
        if (sort == null) {
            sort = aForm.getSort();
        } else {
            aForm.setSort(sort);
        }
        return sort;
	}

    protected void writeUserActivityLog(ComAdmin admin, String action, String description, Logger callerLog)  {
        try {
            if (userActivityLogService != null) {
                userActivityLogService.writeUserActivityLog(admin, action, description);
            } else {
                callerLog.error("Missing userActivityLogService in " + this.getClass().getSimpleName());
                callerLog.info("Userlog: " + admin.getUsername() + " " + action + " " +  description);
            }
        } catch (Exception e) {
            callerLog.error("Error writing ActivityLog: " + e.getMessage(), e);
            callerLog.info("Userlog: " + admin.getUsername() + " " + action + " " +  description);
        }
    }

    protected void writeUserActivityLog(ComAdmin admin, String action, String description)  {
        writeUserActivityLog(admin, action, description, logger);
    }

    protected void writeUserActivityLog(ComAdmin admin, UserAction action)  {
        writeUserActivityLog(admin, action.getAction(), action.getDescription());
    }

    protected void writeUserActivityLog(ComAdmin admin, String action, int description)  {
        writeUserActivityLog(admin, action, Integer.toString(description));
    }

    public void setUserActivityLogService(UserActivityLogService userActivityLogService) {
        this.userActivityLogService = userActivityLogService;
    }
}
