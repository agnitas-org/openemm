/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

/**
 * Implementation of <strong>Action</strong> that uses a custom request parameter {@link #METHOD_PARAMETER_NAME} (if present)
 * as an action method to be dispatched.
 */
public class StrutsDispatchActionBase extends DispatchAction {
    private static final String METHOD_PARAMETER_NAME = "action_forward";

    /**
     * Process a request and dispatch custom action method if request parameter {@link #METHOD_PARAMETER_NAME} is present.
     * Preserves a default behavior if a custom action method is not specified.
     */
    @Override
	protected ActionForward	dispatchMethod(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, String name) throws Exception {
        String method = StringUtils.defaultString(req.getParameter(METHOD_PARAMETER_NAME), name);
        return super.dispatchMethod(mapping, form, req, res, method);
    }
}
