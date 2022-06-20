/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.web.forms.StrutsFormBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.Action;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;

/**
 * Base action class which is used as a base class for other OpenEMM actions<br><br>
 * Contains predefined set of action values which can be used by subclasses:<br>
 * ACTION_LIST - usually used for overview pages for showing the list of elements in table<br>
 * ACTION_VIEW - used for loading data of some entity and showing it on edit-page<br>
 * ACTION_SAVE - used for saving entity after editing on edit-page<br>
 * ACTION_NEW - used for creation of new entity<br>
 * ACTION_CONFIRM_DELETE - used for forwarding to page with deletion confirmation<br>
 * ACTION_DELETE - used for removing entity from database<br>
 * ACTION_LAST - just indicates the last number-value used for default actions (the actions of subclasses should start<br>
 *     from ACTION_LAST + 1)<br><br>
 * Also contains util methods which can be useful for using in subclasses:<br>
 * - getBean: gets the bean via applicationContext<br>
 * - allowed: checks user permission<br>
 * - setNumberOfRows: initializes the number of rows to be shown in tables<br>
 * - getInitializedColumnWidthList: resets the width of columns<br>
 * - getSort: gets the sort for tables<br>
 */
public class StrutsActionBase extends Action {
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(StrutsActionBase.class);

    public static final int ACTION_UNSPECIFIED = 0;

    public static final int ACTION_LIST = 1;

    public static final int ACTION_VIEW = 2;

    public static final int ACTION_SAVE = 3;

    public static final int ACTION_NEW = 4;

    public static final int ACTION_DELETE = 5;

    public static final int ACTION_CONFIRM_DELETE = 6;

    public static final int ACTION_LAST = 6;

	private UserActivityLogService userActivityLogService;

    public String subActionMethodName(int subAction) {
		switch (subAction) {
			case StrutsActionBase.ACTION_UNSPECIFIED:
				return "unspecified";
			case StrutsActionBase.ACTION_LIST:
				return "list";
			case StrutsActionBase.ACTION_VIEW:
				return "view";
			case StrutsActionBase.ACTION_SAVE:
				return "save";
			case StrutsActionBase.ACTION_NEW:
				return "new";
			case StrutsActionBase.ACTION_DELETE:
				return "delete";
			case StrutsActionBase.ACTION_CONFIRM_DELETE:
				return "confirm_delete";
			default:
				return "action-" + subAction;
		}
	}
	
    /**
     * Initialize the list which keeps the current width of the columns, with a default value of '-1'
     * A JavaScript in the corresponding jsp will set the style.width of the column.
     *
     * @param size number of columns
     * @return the list of column width
     */
    protected List<String> getInitializedColumnWidthList(int size) {
		List<String> columnWidthList = new ArrayList<>();
		for (int i=0; i< size ; i++) {
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
		if (userActivityLogService != null) {
			userActivityLogService.writeUserActivityLog(admin, action, description, callerLog);
		} else {
			callerLog.error("Missing userActivityLogService in " + this.getClass().getSimpleName());
			callerLog.info("Userlog: " + admin.getUsername() + " " + action + " " +  description);
		}
    }

    protected void writeUserActivityLog(ComAdmin admin, UserAction action, Logger callerLog)  {
    	writeUserActivityLog(admin, action.getAction(), action.getDescription(), callerLog);
    }

	protected void writeUserActivityLog(ComAdmin admin, String action, String description)  {
		writeUserActivityLog(admin, action, description, logger);
	}

	protected void writeUserActivityLog(ComAdmin admin, UserAction action)  {
		writeUserActivityLog(admin, action, logger);
	}

    protected void writeUserActivityLog(ComAdmin admin, String action, int description)  {
		writeUserActivityLog(admin, action, Integer.toString(description));
    }

	@Required
	public final void setUserActivityLogService(UserActivityLogService userActivityLogService) {
		this.userActivityLogService = userActivityLogService;
	}

	public UserActivityLogService getUserActivityLogService() {
		return userActivityLogService;
	}
}
