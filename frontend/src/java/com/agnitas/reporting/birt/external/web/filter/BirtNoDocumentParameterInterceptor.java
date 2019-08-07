/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.web.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

/**
 * This filter's purpose is to fix RCE vulnerability that the BIRT servlets have.
 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=538142 for more detail.
 */
public class BirtNoDocumentParameterInterceptor implements Filter {
    /** Logger. */
    private static final transient Logger logger = Logger.getLogger(BirtNoDocumentParameterInterceptor.class);

    private String errorPage;

    @Override
    public void init(FilterConfig config) throws ServletException {
        errorPage = config.getInitParameter("errorpage");

        if (errorPage == null) {
            errorPage = "/error.do";
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request.getParameter("__document") == null) {
            chain.doFilter(request, response);
        } else {
            error(request, response);
        }
    }

    @Override
    public void destroy() {
        // Do nothing.
    }

    private void error(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute("error", new Exception("Using __document parameter is denied"));
        RequestDispatcher dispatcher = request.getRequestDispatcher(errorPage);
        dispatcher.forward(request, response);
    }
}
