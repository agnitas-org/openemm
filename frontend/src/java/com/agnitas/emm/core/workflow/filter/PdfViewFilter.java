/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.filter;

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

public class PdfViewFilter implements Filter {

    private static final long DELAY = 20000l;
//    private FilterConfig filterConfig;
    private AtomicInteger reqCount;

    @Override
    public void init(FilterConfig filterConfig) {
//        this.filterConfig = filterConfig;
        reqCount = new AtomicInteger();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) servletRequest;
        HttpSession session = httpReq.getSession(false);
        String method = httpReq.getParameter("method");
        boolean isCorrectMethod = StringUtils.isNotBlank(method) && method.equals("viewOnlyElements");

        if (nonNull(session) && isCorrectMethod) {
            executeDelay();
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void executeDelay() {
        if (reqCount.incrementAndGet() > 1) {
            reqCount = new AtomicInteger();
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void destroy() {
    	// do nothing
    }
}
