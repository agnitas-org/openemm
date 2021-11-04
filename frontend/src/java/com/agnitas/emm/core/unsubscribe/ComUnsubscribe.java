/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.unsubscribe;

import java.io.IOException;
import java.util.Locale;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.agnitas.emm.core.commons.uid.ExtensibleUIDConstants;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.util.SafeString;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.dao.ComBindingEntryDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.userform.service.UserFormExecutionResult;
import com.agnitas.emm.core.userform.service.UserFormExecutionService;

/**
 * Servlet handling unsubscription requests.
 */
@MultipartConfig // <-- required to get Tomcat parsing message with mimetype "multipart/form-data"
public final class ComUnsubscribe extends HttpServlet {
	
	/** Serial version UID. */
	private static final long serialVersionUID = -1335116656304676065L;
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComUnsubscribe.class);
	
	
	/** Service handling UIDs. */
	private ExtensibleUIDService uidService;

	private ApplicationContext applicationContext;
	private Unsubscription unsubscription;
	private OneClickUnsubscription oneClickUnsubscription;
	
	private UserFormExecutionService userFormExecuteService;
	
    @Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
    	this.applicationContext = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
    	this.uidService = applicationContext.getBean( ExtensibleUIDConstants.SERVICE_BEAN_NAME, ExtensibleUIDService.class);
    	this.userFormExecuteService = applicationContext.getBean("UserFormExecutionService", UserFormExecutionService.class);
    	
       	final ComMailingDao mailingDao = applicationContext.getBean("MailingDao", ComMailingDao.class);
    	final ComBindingEntryDao bindingEntryDao = applicationContext.getBean("BindingEntryDao", ComBindingEntryDao.class);
    	
    	this.unsubscription = new Unsubscription(mailingDao, bindingEntryDao);
    	this.oneClickUnsubscription = new OneClickUnsubscription(this.unsubscription);
	}

	@Override
	protected final void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		try {
			final ComExtensibleUID uid = extractAndParseUID(req);
			showUnsubscriptionLandingPage(uid, req, resp);
		} catch(final Exception e) {
            logger.error("Exception during one-click unsubscription", e);
		}
	}

	@Override
	protected final void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		try {
			final ComExtensibleUID uid = extractAndParseUID(req);
			
			if(this.oneClickUnsubscription.isOneClickUnsubscritionRequest(req)) {
				doOneClickUnsubscription(uid, req);
			} else {
				showUnsubscriptionLandingPage(uid, req, resp);
			}
		} catch(final Exception e) {
            logger.error("Exception during one-click unsubscription", e);
		}
	}
	
	private final void doOneClickUnsubscription(final ComExtensibleUID uid, final HttpServletRequest request) {
		final Locale loc = request.getLocale();
		final String remark = SafeString.getLocaleString("recipient.csa.optout.remark", loc);
		
		this.oneClickUnsubscription.performOneClickUnsubscription(uid, remark);
	}
	
	@SuppressWarnings("unused")
	private final void doUnsubscription(final ComExtensibleUID uid, final HttpServletRequest request) {
		final Locale loc = request.getLocale();
		final String remark = SafeString.getLocaleString("recipient.csa.optout.remark", loc);
		
		this.unsubscription.performUnsubscription(uid, remark);
	}
	
    private final ComExtensibleUID extractAndParseUID(final HttpServletRequest request) throws Exception {
        String uidString = request.getParameter("uid");
        if (StringUtils.isBlank(uidString)) {
        	String[] uriParts = StringUtils.strip(request.getRequestURI(), "/").split("/");
			if (uriParts.length >= 2 && "uq.html".equals(uriParts[uriParts.length - 1]) && uriParts[uriParts.length - 2].length() > 10) {
				uidString = uriParts[uriParts.length - 2];
			} else if (uriParts.length >= 1 && StringUtils.isNotBlank(uriParts[uriParts.length - 1]) && uriParts[uriParts.length - 1].length() > 10) {
				uidString = uriParts[uriParts.length - 1];
			}
        }

        if(uidString != null) {
        	return uidService.parse(uidString);
        } else {
        	throw new Exception("No UID parameter");
        }
    }
    
    private final void showUnsubscriptionLandingPage(final ComExtensibleUID uid, final HttpServletRequest request, final HttpServletResponse response) {
    	try {
    		CaseInsensitiveMap<String, Object> params = new CaseInsensitiveMap<>();
    		final UserFormExecutionResult result = userFormExecuteService.executeForm(uid.getCompanyID(), "unsubscribe", request, params, false);
    		
    		response.setContentType(result.responseMimeType);
    		
    		response.getWriter().println(result.responseContent);
    		response.getWriter().flush();
    	} catch(final Exception e) {
    		logger.error("Error showing landing page for unsubscription", e);
    	}
    }

}
