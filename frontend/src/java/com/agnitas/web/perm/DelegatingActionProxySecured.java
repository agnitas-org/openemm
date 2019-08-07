/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.perm;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.util.AgnUtils;
import org.agnitas.web.StrutsActionBase;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.struts.DelegatingActionProxy;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;

public class DelegatingActionProxySecured extends DelegatingActionProxy {
	private static final transient Logger logger = Logger.getLogger(DelegatingActionProxySecured.class);
	
	private static ApplicationContext ctx =  null;
	private static ActionsTokenResolver tokenResolver = null;

	private static ApplicationContext getContext(HttpServletRequest request) {
		if (ctx == null) {
			ctx = WebApplicationContextUtils.getWebApplicationContext(request.getSession().getServletContext());
		}
		return ctx;
	}
	private static ActionsTokenResolver getTokenResolver(HttpServletRequest request) {
		if (tokenResolver == null) {
			ApplicationContext ctx =  getContext(request);
			tokenResolver = (ActionsTokenResolver) ctx.getBean("actionsTokenResolver");
		}
		return tokenResolver;
	}
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		if (!AgnUtils.isUserLoggedIn(request)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return mapping.findForward("logon");
		}

		Action delegateAction = getDelegateAction(mapping);
		
		String path = mapping.getPath();
		String method = "";
		
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
			
		if (delegateAction instanceof StrutsActionBase) {
			try {
				StrutsActionBase actionBase = (StrutsActionBase) delegateAction;
				int subAction = Integer.parseInt(request.getParameter("action"));
				method = actionBase.subActionMethodName(subAction);
			} catch (NumberFormatException e) {
				method = "unspecified";
			}
		} else if (delegateAction instanceof DispatchAction) {
			method = request.getParameter(mapping.getParameter());
			if (StringUtils.isBlank(method)) {
				method = "unspecified";
			}
		} else {
		    method = "unspecified";
		}

		ActionsTokenResolver tokenResolver = getTokenResolver(request);
		
		boolean isAllowed = false;
		
		// Check for any complex auth token matching
		List<ComplexToken> complexTokens = tokenResolver.getComplex(path);
		if (complexTokens != null && checkComplexTokens(complexTokens, method, request)) {
			isAllowed = true;
		}
		
		String mappedToken = null;
		if (!isAllowed) {
			// Check for the last simple auth token matching (last, because the xml is imported in a map)
			String token = path + "." + method;
			mappedToken = tokenResolver.get(token);
			if (mappedToken == null) {
				if (complexTokens == null) {
					// No complex token was found and no simple token was found
					ComAdmin admin = AgnUtils.getAdmin(request);
					String errMsg = String.format("Permission denied: for user %s because no mapping configuration found for token: %s", 
							admin.getUsername(), token);
					logger.error(errMsg);
					throw new NotAllowedActionException(admin.getUsername(), token);
				}
			} else if (Permission.ALWAYS_ALLOWED.toString().equals(mappedToken) || AgnUtils.allowed(request, Permission.getPermissionsByToken(mappedToken))) {
				// Simple token was found and the user rights are valid for it
				isAllowed = true;
			}
		}

		if (isAllowed) {
			return delegateAction.execute(mapping, form, request, response);
		} else {
			ComAdmin admin = AgnUtils.getAdmin(request);
			String errMsg = String.format("Permission denied: %s does not have sufficient privileges to perform operation: %s", 
					admin.getUsername(), path);
			if (mappedToken != null) {
				errMsg += " SecurityToken: " + mappedToken;
			}
			logger.error(errMsg);
			throw new NotAllowedActionException(admin.getUsername(), path);
		}
	}
	
	private boolean checkComplexTokens(List<ComplexToken> complexTokens, 
			String method, HttpServletRequest request) throws Exception {
		for (ComplexToken complexToken : complexTokens) {
			if ("*".equals(complexToken.getSubaction()) || method.equals(complexToken.getSubaction())) {
				if ("OR".equalsIgnoreCase(complexToken.getAggregation())) {
					if (checkORedTokens(complexToken, request)) {
						return true;
					}
				} else if ("AND".equalsIgnoreCase(complexToken.getAggregation())) {
					if (checkANDedTokens(complexToken, request)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean checkORedTokens(ComplexToken complexToken, HttpServletRequest request) throws Exception {
		for (String token : complexToken.getTokens()) {
			if (AgnUtils.allowed(request, Permission.getPermissionsByToken(token))) {
				return true;
			}
		}
		return false;
	}

	private boolean checkANDedTokens(ComplexToken complexToken, HttpServletRequest request) throws Exception {
		for (String token : complexToken.getTokens()) {
			if (!AgnUtils.allowed(request, Permission.getPermissionsByToken(token))) {
				return false;
			}
		}
		return true;
	}

}
