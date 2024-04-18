/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core;

import com.agnitas.beans.Admin;
import com.agnitas.web.perm.annotations.Anonymous;
import jakarta.servlet.http.HttpServletRequest;
import org.agnitas.util.AgnUtils;
import org.apache.jasper.runtime.JspRuntimeLibrary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Enumeration;

@Controller
public class ErrorController {

    private static final Logger logger = LogManager.getLogger(ErrorController.class);

    @RequestMapping("/error.action")
    @Anonymous
    public String show(Admin admin, @RequestParam(value = "allowRedesigned", required = false) boolean allowRedesigned) {
        if (allowRedesigned && admin != null && admin.isRedesignedUiUsed()) {
            return "forward:/errorRedesigned.action";
        }

        return "error";
    }

    @RequestMapping("/csrf/error.action")
    @Anonymous
    public String csrfError() {
        return "csrf_error";
    }

    @RequestMapping("/errorRedesigned.action")
    @Anonymous
    public String showRedesigned(HttpServletRequest req, Model model) {
        Throwable exception = JspRuntimeLibrary.getThrowable(req);
        logger.error(getErrorLog(exception, req), exception);
        return "error_page";
    }

    private String getErrorLog(Throwable exception, HttpServletRequest req) {
        StringBuilder errorBuilder = new StringBuilder();

        if (exception != null) {
            errorBuilder.append(exception.getMessage() + "\n" + AgnUtils.throwableToString(exception, -1));
        }

        errorBuilder.append("\nRequest Parameters:\n")
                .append(getRequestParametersAsStr(req))
                .append("\nRequest Attributes:\n")
                .append("IP: ")
                .append(req.getRemoteAddr())
                .append("\n")
                .append(getRequestAttributesAsStr(req));

        return errorBuilder.toString();
    }

    private String getRequestAttributesAsStr(HttpServletRequest req) {
        final StringBuilder builder = new StringBuilder();

        Enumeration<String> attrNames = req.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String currentAttrName = attrNames.nextElement();
            builder.append(currentAttrName).append(": ").append(req.getAttribute(currentAttrName)).append("\n");
        }

        return builder.toString();
    }

    private String getRequestParametersAsStr(HttpServletRequest req) {
        final StringBuilder builder = new StringBuilder();

        Enumeration<String> parameterNames = req.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String currentParamName = parameterNames.nextElement();
            builder.append(currentParamName).append(": ").append(req.getParameter(currentParamName)).append("\n");
        }

        return builder.toString();
    }
}
