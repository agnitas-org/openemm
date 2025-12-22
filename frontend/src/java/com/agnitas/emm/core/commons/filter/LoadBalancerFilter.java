/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Filter that prevents session being created on disabled node.
 * 
 * This filter is required, because some users forces login to disabled node
 * by using bookmarked URLs, reusing timed-out sessions, ...
 * 
 * How this filter works:
 * <ol>
 *   <li>
 *   	The filter checks, if the request attribute <i>{@value #LOAD_BALANCER_ACTIVATION_ATTRIBUTE}</i> is present.
 *      If not, processing the request is continued with next filter in chain.
 *   </li>
 * 	 <li>
 * 		If request attribute is present, the activation state of current node is checked. If node is not disabled, processing
 * 		the request is continued with next filter in chain. (Note: The state <i>stopped</i> is not checked, because a stopped
 * 		does not process any requests.)
 *   </li>
 *   <li>
 *   	If current node is disabled, the validity of the given session ID is checked. If session is valid, processing the request
 *   	is continued with next filter in chain. (Note: With proceeding time all session will expire, either by logout by user or by
 *   	exceeding the maximum idle time of the session.)
 *   </li>
 *   <li>
 *     If current session is invalid, the filter sends a redirect to the load balancer. In this case, the user will see the front page
 *     (in most cases some login page) of the active node.
 *   </li>
 * </ol>
 * 
 * <p>
 *   <b>Note:</b> This filter works with mod_jk only. Version 1.2.32 or newer of mod_jk is required. Older versions do not provide the
 *   required request attribute.
 * </p>
 * <p>
 *   <b>Note:</b> Newer versions of mod_jk (current is 1.2.37) have a that randomly enables and disables nodes.
 * </p>
 * 
 * <p>
 *   There are three different activation states:
 *   <ul>
 *     <li></i>active</i>: The node is active and will receive any request (with valid and invalid session IDs). This is the normal running state of a node.</li>
 *     <li></i>disabled</i>: The node is active but will receive only request with session IDs pointing to that node. This is the state to shutdown the node in future.</li>
 *     <li></i>stopped</i>: The node is inactive and will not receive any request.</li>
 *   </ul>
 * 
 *   (See documentation of mod_jk for better descriptions.)
 * </p>
 */
public class LoadBalancerFilter implements Filter {

	/** Request attribute provided by mod_jk to read activation state of current node. */
	public static final String LOAD_BALANCER_ACTIVATION_ATTRIBUTE = "JK_LB_ACTIVATION";
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger( LoadBalancerFilter.class);
	
	/** URL to redirect to in case of invalid session on disabled node. */
	private String systemUrl;
	
	@Override
	public void destroy() {
		// Nothing to do on shutdown
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		String loadBalancerActivation = (String) request.getAttribute( LOAD_BALANCER_ACTIVATION_ATTRIBUTE);
		
		// First, check if the request attribute showing the activation state of the node is present
		if( loadBalancerActivation != null) {
			
			// Request attribute is present, so the node is behind a load balancer
			if( logger.isDebugEnabled()) {
				logger.debug( "Load balancer activation is " + loadBalancerActivation);
			}

			// Check, if activation state of current node is "disabled".
			if( "DIS".equals( loadBalancerActivation)) {
				
				// The node is disabled.
				try {
					HttpServletRequest req = (HttpServletRequest) request;

					// Check validity of session ID
					if( !req.isRequestedSessionIdValid()) {
						// Session ID is not valid for the current node, the requests sends a redirect to system URL specified in property "system.url"
						if( logger.isInfoEnabled()) {
							logger.info( "Requested session is invalid.");
						}
						
						HttpServletResponse resp = (HttpServletResponse) response;
						resp.sendRedirect( systemUrl);
					} else {
						
						// The session ID seems to be valid for the current node, so processing will continue.
						if( logger.isInfoEnabled()) {
							logger.info( "Requested session is valid. Proceeding with request.");
						}
						
						filterChain.doFilter( request, response);
					}
						
				} catch( ClassCastException e) {
					// We had some problem with the request. It may be no HTTP request. Processing will continue without further checks.
					logger.warn( "No HttpServletRequest?", e);
					
					filterChain.doFilter( request, response);
				}
			} else {
				// The node is either active or stopped.
				if( logger.isInfoEnabled()) {
					logger.info( "Node not disabled.");
				}

				filterChain.doFilter( request, response);
			}
		} else {
			/*
			 *  We got no information about the activation state of the current node, so we have to assume,
			 *  that the node is not behind a load balancer and is active all the time.
			 */
			
			if( logger.isInfoEnabled()) {
				logger.info( "No information about load balancer activation found.");
			}
			
			filterChain.doFilter( request, response);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		try {
			// Read system URL. This URL will be used for redirection in case of invalid sessions on disabled nodes.
			systemUrl = ConfigService.getInstance().getValue(ConfigValue.SystemUrl);
		} catch (Exception e) {
			throw new ServletException("Cannot read system url: " + e.getMessage(), e);
		}
		
		if( logger.isInfoEnabled()) {
			logger.info( "Using system url '" + systemUrl + "' for redirection on");
		}
	}

}
