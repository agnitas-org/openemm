/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.temporary.actions;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.actions.EmmAction;
import org.agnitas.dao.EmmActionDao;
import org.agnitas.util.AgnUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.action.service.EmmActionService;

public class ComConvertActionsAction extends Action {
    @SuppressWarnings("unused")
    private static final transient Logger logger = Logger.getLogger(ComConvertActionsAction.class);

    private EmmActionDao actionDao;
    private EmmActionService actionService;

    public void setEmmActionDao(EmmActionDao dao) {
        this.actionDao = dao;
    }

    public void setEmmActionService(EmmActionService service) {
        this.actionService = service;
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

        ComAdmin admin = AgnUtils.getAdmin(request);
        if (admin != null && admin.getAdminID() == 1) {
            response.setContentType("text/plain");

            if (request.getParameter("companyID") != null) {
                int companyID = Integer.parseInt(request.getParameter("companyID"));

                /*
                 *  Conversion can simply be done by loading and saving all found actions, because either
                 *  there is only the serialized action or serialized + table-based action.
                 *
                 *  => Currently, the serialized action is always the latest state of an action.
                 */

                if (request.getParameter("actionID") == null) {
                    response.getWriter().println("Converting all actions for company " + companyID);

                    List<EmmAction> actions = actionDao.getEmmActions(companyID);
                    response.getWriter().println("Found " + actions.size() + " actions to convert.");

                    for (EmmAction action : actions) {
                        response.getWriter().println("Converting action '" + action.getShortname() + "' (" + action.getId() + ")");
                        try {
                            action = actionService.getEmmAction(action.getId(), companyID);
                            actionService.saveEmmAction(companyID, action);
                        } catch (Exception e) {
                            response.getWriter().println("Error converting action '" + action.getShortname() + "' (" + action.getId() + "):");
                            e.printStackTrace(response.getWriter());
                        }
                        response.getWriter().flush();
                    }
                } else {
                    int actionID = Integer.parseInt(request.getParameter("actionID"));
                    EmmAction action = actionDao.getEmmAction(actionID, companyID);

                    if (action != null && action.getId() != 0) {
                        response.getWriter().println("Converting action '" + action.getShortname() + "' (" + action.getId() + ")");

                        try {
                            action = actionService.getEmmAction(action.getId(), companyID);
                            actionService.saveEmmAction(companyID, action);
                        } catch (Exception e) {
                            response.getWriter().println("Error converting action '" + action.getShortname() + "' (" + action.getId() + "):");
                            e.printStackTrace(response.getWriter());
                        }
                    } else {
                        response.getWriter().println("No action ID " + actionID + " for company " + companyID);
                    }
                }

            } else {
                response.getWriter().println("companyID missing");
            }
        } else {
            response.getWriter().println("Insufficient permissions");
        }

        response.getWriter().println("End of action.");
        response.getWriter().flush();

        return null;
    }
}
