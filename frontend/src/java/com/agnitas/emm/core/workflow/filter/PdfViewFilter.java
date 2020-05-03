/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.filter;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class PdfViewFilter implements Filter {

    private static final long DELAY = 20000l;
    private static AtomicInteger reqCount = new AtomicInteger();

    @Override
    public void init(FilterConfig filterConfig) {
        //Do nothing.
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if (Objects.nonNull(request.getSession(false))) {
            executeDelay();
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void executeDelay() {
        if (reqCount.incrementAndGet() > 1) {
            reqCount.set(0);
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void destroy() {
        //Do nothing.
    }
}
