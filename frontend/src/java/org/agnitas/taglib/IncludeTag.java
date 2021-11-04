/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.taglib;

import java.io.File;
import java.io.IOException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;

public class IncludeTag extends TagSupport {
    private static final long serialVersionUID = 5419856852964274327L;
    private static final Logger logger = Logger.getLogger(IncludeTag.class);

    private String page;
    private boolean flush;

    public void setPage(String page) {
        this.page = page;
    }

    public void setFlush(boolean flush) {
        this.flush = flush;
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            pageContext.include(page, flush);
        } catch (ServletException | IOException e) {
            if (checkPageExists()) {
                throw new JspException(e);
            } else {
                logger.debug(String.format("File `%s` not found", page), e);
            }
        }
        return EVAL_PAGE;
    }

    private boolean checkPageExists() {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        String path = request.getServletContext().getRealPath(getPageLocation(request, page));

        return new File(path).exists();
    }

    private String getPageLocation(HttpServletRequest request, String relativeUrlPath) {
        if (relativeUrlPath == null || relativeUrlPath.startsWith("/")) {
            return relativeUrlPath;
        }

        String uri = (String) request.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);

        if (uri == null) {
            uri = request.getServletPath();
        }

        return uri.substring(0, uri.lastIndexOf('/')) + '/' + relativeUrlPath;
    }
}
