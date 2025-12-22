/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.unsubscribe;

import java.util.Locale;

import com.agnitas.dao.BindingEntryDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUIDConstants;
import com.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import com.agnitas.emm.core.userform.service.UserFormExecutionResult;
import com.agnitas.emm.core.userform.service.UserFormExecutionService;
import com.agnitas.exception.FormNotFoundException;
import com.agnitas.util.SafeString;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet handling unsubscription requests.
 */
@MultipartConfig // <-- required to get Tomcat parsing message with mimetype "multipart/form-data"
public final class UnsubscribeServlet extends HttpServlet {
	
	private static final long serialVersionUID = -1335116656304676065L;

	private static final Logger logger = LogManager.getLogger(UnsubscribeServlet.class);
	
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
    	
       	final MailingDao mailingDao = applicationContext.getBean("MailingDao", MailingDao.class);
    	final BindingEntryDao bindingEntryDao = applicationContext.getBean("BindingEntryDao", BindingEntryDao.class);
    	
    	this.unsubscription = new Unsubscription(mailingDao, bindingEntryDao);
    	this.oneClickUnsubscription = new OneClickUnsubscription(this.unsubscription);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		try {
			final ExtensibleUID uid = extractAndParseUID(req);
			showUnsubscriptionLandingPage(uid, req, resp);
		} catch(final Exception e) {
            logger.error("Exception during one-click unsubscription", e);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
		try {
			final ExtensibleUID uid = extractAndParseUID(req);
			
			if(this.oneClickUnsubscription.isOneClickUnsubscritionRequest(req)) {
				doOneClickUnsubscription(uid, req);
			} else {
				showUnsubscriptionLandingPage(uid, req, resp);
			}
		} catch(final Exception e) {
            logger.error("Exception during one-click unsubscription", e);
		}
	}
	
	private final void doOneClickUnsubscription(final ExtensibleUID uid, final HttpServletRequest request) {
		final Locale loc = request.getLocale();
		final String remark = SafeString.getLocaleString("recipient.csa.optout.remark", loc);
		
		this.oneClickUnsubscription.performOneClickUnsubscription(uid, remark);
	}
	
    private final ExtensibleUID extractAndParseUID(final HttpServletRequest request) throws Exception {
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
    
    private void showUnsubscriptionLandingPage(ExtensibleUID uid, HttpServletRequest request, HttpServletResponse response) {
    	try {
    		CaseInsensitiveMap<String, Object> params = new CaseInsensitiveMap<>();
    		final UserFormExecutionResult result = userFormExecuteService.executeForm(uid.getCompanyID(), "unsubscribe", request, params, false);
    		
    		response.setContentType(result.responseMimeType);
    		
    		response.getWriter().println(result.responseContent);
    		response.getWriter().flush();
    	} catch (FormNotFoundException nfe) {
			logger.error("User form 'unsubscribe' not found for company ID {}!", uid.getCompanyID());
		} catch (Exception e) {
    		logger.error("Error showing landing page for unsubscription", e);
    	}
    }

}
