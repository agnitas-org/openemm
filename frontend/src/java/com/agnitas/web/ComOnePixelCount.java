/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import org.agnitas.actions.EmmAction;
import org.agnitas.beans.impl.CompanyStatus;
import org.agnitas.dao.EmmActionDao;
import com.agnitas.dao.MailingDao;
import org.agnitas.emm.core.commons.daocache.CompanyDaoCache;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDConstants;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.velocity.Constants;
import org.agnitas.util.AgnUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.Company;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.mailtracking.service.OpenTrackingService;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.mobile.service.ClientService;
import com.agnitas.emm.core.mobile.service.ComAccessDataService;
import com.agnitas.emm.core.mobile.service.ComDeviceService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ComOnePixelCount extends HttpServlet {
	
	/** Serial version UID. */
	private static final long serialVersionUID = 9217593068580606726L;

	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ComOnePixelCount.class);

	/** Raw GIF data for 1x1 pixel transparent image. */
	public static final byte[] ONEPIXELGIF_DATA = { 71, 73, 70, 56, 57, 97, 1, 0, 1, 0, -128, -1, 0, -64, -64, -64, 0, 0, 0, 33, -7, 4, 1, 0, 0, 0, 0, 44, 0, 0, 0, 0, 1, 0, 1, 0, 0, 2, 2, 68, 1, 0, 59 };

	private CompanyDaoCache companyDaoCache;
	private ExtensibleUIDService uidService;
	private OpenTrackingService openTrackingService;

	private ComAccessDataService accessDataService;
	private ComDeviceService comDeviceService;
	private ClientService clientService;
	private MailingDao mailingDao;
	private EmmActionDao actionDao;
	
	private RecipientService recipientService;
	
	public void setComAccessDataService(ComAccessDataService accessDataService) {
		this.accessDataService = accessDataService;
	}

	private ComAccessDataService getComAccessDataService() {
		if (accessDataService == null) {
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			accessDataService = (ComAccessDataService) applicationContext.getBean("AccessDataService");
		}
		return accessDataService;
	}

	public void setComDeviceService(ComDeviceService comDeviceService) {
		this.comDeviceService = comDeviceService;
	}

	private ComDeviceService getComDeviceService() {
		if (comDeviceService == null) {
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			comDeviceService = (ComDeviceService) applicationContext.getBean("DeviceService");
		}
		return comDeviceService;
	}

	public void setClientService(ClientService clientService) {
		this.clientService = clientService;
	}

	private ClientService getClientService() {
		if (clientService == null) {
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			clientService = (ClientService) applicationContext.getBean("ClientService");
		}
		return clientService;
	}

	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	private MailingDao getMailingDao() {
		if (mailingDao == null) {
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			mailingDao = (MailingDao) applicationContext.getBean("MailingDao");
		}
		return mailingDao;
	}

	public void setActionDao(EmmActionDao actionDao) {
		this.actionDao = actionDao;
	}

	private EmmActionDao getActionDao() {
		if (actionDao == null) {
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			actionDao = (EmmActionDao) applicationContext.getBean("EmmActionDao");
		}
		return actionDao;
	}
	
	public final void setOpenTrackingService(final OpenTrackingService service) {
		this.openTrackingService = Objects.requireNonNull(service, "Open tracking service cannot be null");
	}
	
	private final OpenTrackingService getOpenTrackingService() {
		if(this.openTrackingService == null) {
			final ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());

			this.openTrackingService = (OpenTrackingService) applicationContext.getBean("MailOpenTrackingService");
		}
		
		return this.openTrackingService;
	}

	@Override
	public void init() throws ServletException {
		super.init();

		try {
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			companyDaoCache = (CompanyDaoCache) applicationContext.getBean("CompanyDaoCache");
			uidService = (ExtensibleUIDService) applicationContext.getBean(ExtensibleUIDConstants.SERVICE_BEAN_NAME);
			
			recipientService = applicationContext.getBean("recipientService", RecipientService.class);
			
		} catch (NoSuchBeanDefinitionException e) {
			logger.error("Cannot instantiate ComOnePixelCount servlet: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Servlet service-method, is invoked on calling the servlet. Parses data
	 * from uid parameter (company id, mailing id and customer id), stores the
	 * data from the request in database, writes one pixel gif image into
	 * response. Also executes mailing open action, if the mailing has one.
	 * Returns nothing if the company is not in status "active" or if some
	 * execution error occurs.
	 * 
	 * @param request
	 *            HTTP request; should contain "uid" parameter with values of
	 *            company id, mailing id and customer id
	 * @param res
	 *            HTTP response, contains one pixel gif image
	 * @throws IOException
	 *             if an input/output error occurs
	 * @throws ServletException
	 *             if a servlet exception occurs
	 */
	@Override
	public void service(HttpServletRequest request, HttpServletResponse res) throws IOException, ServletException {
		// Send onepixel gif to Browser.
		res.setContentType("image/gif");
		try (OutputStream out = res.getOutputStream()) {
			out.write(ONEPIXELGIF_DATA);
		}
		
		// Persist opening of mailing
		int deviceID = getComDeviceService().getDeviceId(request.getHeader("User-Agent"));
		int clientID = getClientService().getClientId(request.getHeader("User-Agent"));
		
		if (deviceID == ComDeviceService.DEVICE_BLACKLISTED_NO_SERVICE) {
			res.setContentType("text/plain");
			res.getOutputStream().write("No service".getBytes("UTF-8"));
		}

		ComExtensibleUID uid = null;
		String agnUidString = request.getParameter("uid");
		if (StringUtils.isBlank(agnUidString)) {
        	String[] uriParts = StringUtils.strip(request.getRequestURI(), "/").split("/");
			if (uriParts.length >= 2 && "g.html".equals(uriParts[uriParts.length - 1]) && uriParts[uriParts.length - 2].length() > 10) {
				agnUidString = uriParts[uriParts.length - 2];
			} else if (uriParts.length >= 1 && StringUtils.isNotBlank(uriParts[uriParts.length - 1]) && uriParts[uriParts.length - 1].length() > 10) {
				agnUidString = uriParts[uriParts.length - 1];
			}
        }
		if (agnUidString == null) {
			logger.error("OnepixelLog: no uid set");
		} else {
			try {
				// Validate uid
				try {
					uid = uidService.parse(agnUidString);
				} catch (UIDParseException e) {
					logger.info("OnepixelLog: Error parsing UID: " + agnUidString, e);
				}
	
				if (uid != null && uid.getCompanyID() > 0) {
					Company company = companyDaoCache.getItem(uid.getCompanyID());
					if (company == null) {
						logger.error("OnepixelLog error: Company with ID: " + uid.getCompanyID() + " not found");
					} else if (CompanyStatus.ACTIVE == company.getStatus()) {
						// noCount examples: ...&nocount ...&nocount= ...&nocount=true ...&nocount=jhg
			            String noCountString = request.getParameter("nocount");
			            boolean noCount = noCountString != null && !"false".equalsIgnoreCase(noCountString);
			            
						// Write db onepixellog
						DeviceClass deviceClass = getComDeviceService().getDeviceClassForStatistics(deviceID);
						if (deviceID != ComDeviceService.DEVICE_BLACKLISTED_NO_COUNT) {
							if (!noCount) {
								final boolean doNotTrack = !recipientService.isRecipientTrackingAllowed(uid.getCompanyID(), uid.getCustomerID());
								
								getOpenTrackingService().trackOpening(uid, doNotTrack, request.getRemoteAddr(), deviceClass, deviceID, clientID);
								if (logger.isInfoEnabled()) {
									logger.info("OnepixelLog: cust: " + uid.getCustomerID() + " mi: " + uid.getMailingID() + " ci: " + uid.getCompanyID());
								}
							} else {
								if(logger.isInfoEnabled()) {
									logger.info(String.format("Counting tracking pixel disabled by request parameter (customer id: %d, mailing id: %d, company id: %d)", uid.getCustomerID(), uid.getMailingID(), uid.getCompanyID()));
								}
							}
						}

						// Execute action for opening mailing, if set
						executeMailingOpenAction(uid, request);
					}
				}
			} catch (Exception e) {
				logger.error("OnepixelLog: Error occured: " + e.getMessage(), e);
			}
		}
		
		getComAccessDataService().logAccess(request, uid, deviceID);
	}

	/**
	 * Get the actionid to be executed on opening the mailing.
	 * If the action id > 0, execute the action with parameters from the request.
	 * 
	 * @param uid
	 *            ExtensibleUID object, contains parsed data from the "uid"
	 *            request parameter
	 * @param req
	 *            HTTP request
	 * @throws Exception
	 */
	protected void executeMailingOpenAction(final ComExtensibleUID uid, final HttpServletRequest req) throws Exception {
		int companyID = uid.getCompanyID();
		int mailingID = uid.getMailingID();
		int customerID = uid.getCustomerID();
		int openActionID = getMailingDao().getMailingOpenAction(mailingID, companyID);
		if (openActionID != 0) {
			EmmAction emmAction = getActionDao().getEmmAction(openActionID, companyID);
			if (emmAction != null) {
				final EmmActionOperationErrors actionOperationErrors = new EmmActionOperationErrors();

				// execute configured actions
				CaseInsensitiveMap<String, Object> params = new CaseInsensitiveMap<>();
				params.put("requestParameters", AgnUtils.getReqParameters(req));
				params.put("_request", req);
				params.put("_uid", uid);
				params.put("customerID", customerID);
				params.put("mailingID", mailingID);
				params.put(Constants.ACTION_OPERATION_ERRORS_CONTEXT_NAME, actionOperationErrors);
				
				ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
				EmmActionService emmActionService = (EmmActionService) applicationContext.getBean("EmmActionService");
				emmActionService.executeActions(openActionID, companyID, params, actionOperationErrors);
			}
		}
	}
}
