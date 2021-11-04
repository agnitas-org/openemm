/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * 
 * Security filter to protect Struts 1 web application against classload manipulation. 
 * 
 * Security issue: CVE-2014-0114
 * 
 * @see <a href="http://h30499.www3.hp.com/t5/HP-Security-Research-Blog/Protect-your-Struts1-applications/ba-p/6463188#.U39R3nI-PtS">Article to filter</a>
 * @see <a href="http://lmgtfy.com/?q=CVE-2014-0114">Articles to security issue</a>
 */
public class Struts1SecurityFilter implements Filter {

	/**
	 * Filter configuration (comes from web.xml).
	 */
    private FilterConfig filterConfig = null;

    /**
     * Wrapper around HttpServletRequest.
     * This wrapper excludes all parameters that are considered to be insecure.
     */
    private static class ParamFilteredRequest extends HttpServletRequestWrapper {
        
        /**
         * Regular expression used to filter request parameters.
         */
        private String regex;

        /**
         * Creates a new request wrapper for given request.
         *  
         * @param request request to wrap
         * @param regex regular expression for filtered parameter names
         */
        public ParamFilteredRequest(ServletRequest request, String regex) {
            super((HttpServletRequest)request);
            this.regex = regex;
        }
        
        @Override
        public Enumeration<String> getParameterNames() {
            List<String> requestParameterNames = Collections.list(super.getParameterNames());
            List<String> finalParameterNames = new Vector<>();

            for (String parameterName:requestParameterNames) {
                if (!parameterName.matches(regex)) {
                    finalParameterNames.add(parameterName);
                }
            }
            return Collections.enumeration(finalParameterNames);
        }
        
    }
    
    @Override
	public void init(FilterConfig config) throws ServletException {
        this.filterConfig = config;
    }

    @Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String regex = this.filterConfig.getInitParameter("excludeParams");
        chain.doFilter(new ParamFilteredRequest(request, regex), response);
    }

    @Override
	public void destroy() { 
    	// Nothing to do on destruction of the filter
    }
}
