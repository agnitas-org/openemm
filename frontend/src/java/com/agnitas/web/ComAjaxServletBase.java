/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.AgnUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.config.impl.ModuleConfigImpl;
import org.apache.struts.upload.CommonsMultipartRequestHandler;
import org.apache.struts.upload.MultipartRequestHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;

public abstract class ComAjaxServletBase extends HttpServlet {
	private static final long serialVersionUID = -2027880269232397616L;

	private static final Logger logger = LogManager.getLogger(ComAjaxServletBase.class);

    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String ENCODING = "UTF-8";

    private ApplicationContext applicationContext;
    private UserActivityLogService userActivityLogService;

    protected abstract void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setMultipartHandlerClass(request, CommonsMultipartRequestHandler.class);
        processRequestSecured(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setMultipartHandlerClass(request, CommonsMultipartRequestHandler.class);
        processRequestSecured(request, response);
    }

    protected final void processRequestSecured(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (checkAllowed(request, response)) {
            processRequest(request, response);
        }
    }

    /**
     * Ensure that a request is allowed to be performed.
     * This method is to be overridden in sub-classes in order to provide desired security restrictions.
     * See {@link #checkAuthenticated(HttpServletRequest, HttpServletResponse)} and {@link #checkPermitted(HttpServletRequest, HttpServletResponse, Permission...)}.
     *
     * @return {@code true} if a request is allowed or {@code false} otherwise.
     */
    protected boolean checkAllowed(HttpServletRequest request, HttpServletResponse response) {
        return checkAuthenticated(request, response);
    }

    /**
     * Ensure that a request is performed by an authenticated (logged in) user.
     * Set an {@link HttpServletResponse#SC_UNAUTHORIZED} response status otherwise.
     *
     * @return {@code true} if user is authenticated or {@code false} otherwise.
     */
    protected final boolean checkAuthenticated(HttpServletRequest request, HttpServletResponse response) {
    	ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
		    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            try {
                request.getRequestDispatcher("/logon.action?method=init")
                        .forward(request, response);
            } catch (ServletException | IOException e) {
                logger.error("Error occurred: " + e.getMessage(), e);
            }
            return false;
        }
        return true;
    }

    /**
     * Ensure that a request is performed by an authenticated (logged in) user having required permission(s).
     * Set an {@link HttpServletResponse#SC_UNAUTHORIZED} response status if a user is not authenticated (logged in)
     * or {@link HttpServletResponse#SC_FORBIDDEN} response status if a user is authenticated but doesn't have
     * required permission(s).
     *
     * @return {@code true} if user is authenticated and has required permission(s) or {@code false} otherwise.
     */
    protected final boolean checkPermitted(HttpServletRequest request, HttpServletResponse response, Permission... permission) {
        if (checkAuthenticated(request, response)) {
            if (AgnUtils.allowed(request, permission)) {
                return true;
            }
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
        return false;
    }

    protected void writeUserActivityLog(ComAdmin admin, String action, String description)  {
        try {
            if (userActivityLogService == null) {
                userActivityLogService = getApplicationContext().getBean("UserActivityLogService", UserActivityLogService.class);
            }
            userActivityLogService.writeUserActivityLog(admin, action, description);
        } catch (Exception e) {
            logger.error("Error writing ActivityLog: " + e.getMessage(), e);
            logger.info("Userlog: " + admin.getUsername() + " " + action + " " +  description);
        }
    }

    protected void writeUserActivityLog(ComAdmin admin, String action, int description)  {
        writeUserActivityLog(admin, action, Integer.toString(description));
    }

    protected ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        }
        return applicationContext;
    }

    private void setMultipartHandlerClass(HttpServletRequest request, Class<? extends MultipartRequestHandler> cls) {
        request.setAttribute(Globals.MODULE_KEY, new ModuleConfigImpl());
        request.setAttribute(Globals.MULTIPART_KEY, cls.getName());
    }
}
