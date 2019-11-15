/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.errorhandling;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ExceptionHandler;
import org.apache.struts.config.ExceptionConfig;

public class InvalidPathExceptionHandler extends ExceptionHandler {
	
	private static final transient Logger logger = Logger.getLogger(InvalidPathExceptionHandler.class);
	
	@Override
	public ActionForward execute(Exception ex, 
            ExceptionConfig exConfig,
            ActionMapping mapping,
            ActionForm formInstance,
            HttpServletRequest request,
            HttpServletResponse response
		) throws ServletException {
		
		logger.error("No path was found for this action.");
		// interesting point here, the ActionMapping mapping is "null".
		// Therefore mapping.findForward("error") quits with a null pointer exception :-(
		return null;
	}
}
