/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.agnitas.beans.Recipient;
import org.agnitas.beans.TrackableLink;
import org.agnitas.beans.impl.CompanyStatus;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDConstants;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.uid.parser.exception.DeprecatedUIDVersionException;
import org.agnitas.emm.core.commons.uid.parser.exception.InvalidUIDException;
import org.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.PunycodeCodec;
import org.agnitas.util.TimeoutLRUMap;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.Company;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTrackableLinkDao;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.commons.intelliad.IntelliAdMailingSettings;
import com.agnitas.emm.core.commons.intelliad.IntelliAdMailingSettingsCache;
import com.agnitas.emm.core.commons.intelliad.IntelliAdTrackingData;
import com.agnitas.emm.core.commons.intelliad.IntelliAdTrackingStringParser;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID.NamedUidBit;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.core.deeptracking.web.DeepTrackingCookieUtil;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.mailing.cache.SnowflakeMailingCache;
import com.agnitas.emm.core.mailtracking.service.ClickTrackingService;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.mobile.service.ClientService;
import com.agnitas.emm.core.mobile.service.ComAccessDataService;
import com.agnitas.emm.core.mobile.service.ComDeviceService;
import com.agnitas.rdir.processing.SubstituteLinkRdirPostProcessor;
import com.agnitas.rdir.processing.SubstituteLinkResult;
import com.agnitas.util.DeepTrackingToken;
import com.agnitas.util.backend.Decrypt;
import com.agnitas.web.cookies.SameSiteCookiePolicy;
import com.agnitas.web.util.RequestUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RedirectServlet extends HttpServlet {
	/** Serial version UID. */
	private static final long serialVersionUID = 7767318643176056518L;

	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(RedirectServlet.class);

    protected TimeoutLRUMap<Integer, TrackableLink> urlCache;

	// Cache to save redirection URLs for deprecated UID version (mapping company-ID -> URL)
	protected TimeoutLRUMap<Integer, String> deprecatedUIDCache;

	private ApplicationContext applicationContext;
	private LinkService linkService;
	private ComAccessDataService accessDataService;
	private ComDeviceService deviceService;
	private ClientService clientService;
	private ExtensibleUIDService extensibleUIDService;
	private ComTrackableLinkDao trackableLinkDao;
	private EmmActionService emmActionService;
	private ComCompanyDao companyDao;
	private ConfigService configService;
	private ComMailingDao mailingDao;
	private IntelliAdMailingSettingsCache intelliAdMailingSettingsCache;
	private SnowflakeMailingCache snowflakeMailingCache;
	private RecipientService recipientService;
	private SubstituteLinkRdirPostProcessor substituteLinkRdirPostProcessor;
	
	private ClickTrackingService clickTrackingService;

	/**
	 * Service-Method, gets called everytime a User calls the servlet
	 */
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			doResolveLink(request, response);
		} catch(final Exception e) {
			logger.error("Error resolving RDIR link", e);
			
			throw e;
		}
	}
		
	private final void doResolveLink(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		/*
		 * Do not use a simple "return" in case of an error.
		 * 
		 * For error cases throw an Exception. This will send a redirect to a "404" page
		 */
        String agnUidString = request.getParameter("uid");
        if (StringUtils.isBlank(agnUidString)) {
        	String[] uriParts = StringUtils.strip(request.getRequestURI(), "/").split("/");
			if (uriParts.length >= 2 && "r.html".equals(uriParts[uriParts.length - 1]) && uriParts[uriParts.length - 2].length() > 10) {
				agnUidString = uriParts[uriParts.length - 2];
			} else if (uriParts.length >= 1 && StringUtils.isNotBlank(uriParts[uriParts.length - 1]) && uriParts[uriParts.length - 1].length() > 10) {
				agnUidString = uriParts[uriParts.length - 1];
			}
        }

        try {
			ComExtensibleUID uid = decodeUid(agnUidString, request);
			final int deviceID = getDeviceService().getDeviceId(request.getHeader("User-Agent"));
			final int clientID = getClientService().getClientId(request.getHeader("User-Agent"));
			
			if (deviceID == ComDeviceService.DEVICE_BLACKLISTED_NO_SERVICE) {
				response.setContentType("text/plain");
				response.getOutputStream().write("No service".getBytes("UTF-8"));
			}
			
			getAccessDataService().logAccess(request, uid, deviceID);

			if (uid == null) {
				logger.warn("got no UID for " + agnUidString);
				throw new RedirectException(String.format("Got no UID for '%s'", agnUidString));
			}

			final boolean cachingDisabled = StringUtils.equals(uid.getPrefix(), "nc"); // StringUtils.equals() is null-safe
			ComTrackableLink trackableLink = loadTrackableLink(uid, cachingDisabled);
			final Company company = getCompanyDao().getCompany(uid.getCompanyID());
			final String referenceTableRecordSelector = referenceTableRecordSelector(request, company, uid.getCustomerID());
			final String encryptedStaticValueMapOrNull = request.getParameter("stc");
			final Recipient recipientForUid = getRecipientService().getRecipient(uid.getCompanyID(), uid.getCustomerID());
			
			String fullUrl = getLinkService().personalizeLink(trackableLink, agnUidString, uid.getCustomerID(), referenceTableRecordSelector, !NamedUidBit.isBitSet(uid.getBitField(), NamedUidBit.NO_LINK_EXTENSION), encryptedStaticValueMapOrNull);
			
			if (fullUrl == null) {
				logger.error("service: could not personalize link");
				throw new RedirectException("service: could not personalize link");
			} else {
				// Create substitute link if needed. This link has all the properties from original link but a different id and full URL.
				if(getConfigService().getBooleanValue(ConfigValue.RedirectMakeAgnDynMultiLinksTrackable, trackableLink.getCompanyID())) {
					if(this.getSubstituteLinkRdirPostProcessor() != null) {
						final SubstituteLinkResult result = this.getSubstituteLinkRdirPostProcessor().createSubstituteLink(trackableLink, fullUrl, uid);
						
						trackableLink = result.getTrackableLink();
						fullUrl = result.getFullUrl();
						uid = result.getUid();
					} else {
						logger.fatal(String.format("Config key '%s' is set to true, but no SubstituteLinkRdirPostProcessor is defined", ConfigValue.RedirectMakeAgnDynMultiLinksTrackable.getName()));
					}
				}
				
				
				fullUrl = emitDeeptrackingToken(trackableLink, recipientForUid, uid, fullUrl, response, company);
				
				// Check for company-specific configuration to embed links in other measure system links like metalyzer
				String externalMeasureSystemBaseLink = getConfigService().getValue(ConfigValue.ExternalMeasureSystemBaseLinkMailing, trackableLink.getCompanyID());
				if (StringUtils.isNotBlank(externalMeasureSystemBaseLink)) {
					fullUrl = externalMeasureSystemBaseLink.replace("<link>", URLEncoder.encode(fullUrl, "UTF-8"));
				}

				if (isIntelliAdUsed(trackableLink.getMailingID(), trackableLink.getCompanyID())) {
					fullUrl = createIntelliAdLink(fullUrl, trackableLink.getMailingID(), trackableLink.getCompanyID());
				}

				sendRedirect(response, fullUrl, trackableLink.getCompanyID());

				DeviceClass deviceClass = getDeviceService().getDeviceClassForStatistics(deviceID);
				
				if (RequestUtils.hasRangeHeader(request)) {
					logger.warn("Got request with 'Range' header");
					
					RequestUtils.dumpRequest(request, logger);
				}
				
				// noCount examples: ...&nocount ...&nocount= ...&nocount=true ...&nocount=jhg
	            String noCountString = request.getParameter("nocount");
	            boolean noCount = noCountString != null && !"false".equalsIgnoreCase(noCountString);
				
				final int rangeHeaderStartByte = RequestUtils.getRangeRequestStart(request);
				if (rangeHeaderStartByte <= 0) {
	            	if (deviceID != ComDeviceService.DEVICE_BLACKLISTED_NO_COUNT) {
						if (!noCount && company != null && company.getId() != 0 && !(CompanyStatus.TODELETE == company.getStatus() || CompanyStatus.DELETED == company.getStatus())) {
							getClickTrackingService().trackLinkClick(uid, request.getRemoteAddr(), deviceClass, deviceID, clientID);
						}
	            	}
	
	            	executeLinkActions(uid, deviceID, deviceClass, trackableLink, request);
				}
			}
		} catch (final RedirectException e) {
        	if (logger.isInfoEnabled()) {
        		logger.info("Error resolving link: " + agnUidString, e);
        	}
        	
			final String redirectionUrl = getRdirUndecodableLinkUrl();
			
			sendRedirect(response, redirectionUrl, 0);
		} catch (final DeprecatedUIDVersionException e) {
        	if (logger.isInfoEnabled()) {
        		logger.info("Deprecated UID: " + agnUidString, e);
        	}
        	
			final String redirectionUrl = getRdirUndecodableLinkUrl();
			
			sendRedirect(response, redirectionUrl, e.getUID().getCompanyID());
		} catch(final InvalidUIDException | UIDParseException e) {
        	if (logger.isInfoEnabled()) {
        		logger.info(String.format("Error handling UID: %s", agnUidString), e);
        	}
        	
			final String redirectionUrl = getRdirUndecodableLinkUrl();
			
			sendRedirect(response, redirectionUrl, 0);
		} catch (final Exception e) {
        	if (logger.isInfoEnabled()) {
        		logger.error("Exception in RDIR", e);
        	}
        	
			final String redirectionUrl = getRdirUndecodableLinkUrl();
			
			sendRedirect(response, redirectionUrl, 0);
		}
	}
	
	private final SubstituteLinkRdirPostProcessor getSubstituteLinkRdirPostProcessor() {
		if(this.substituteLinkRdirPostProcessor == null) {
			try {
				this.substituteLinkRdirPostProcessor = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("SubstituteLinkRdirPostProcessor", SubstituteLinkRdirPostProcessor.class);
			} catch(final NoSuchBeanDefinitionException e) {
				this.substituteLinkRdirPostProcessor = null;
			}
		}

		return this.substituteLinkRdirPostProcessor;
	}

	private final String emitDeeptrackingToken(final TrackableLink trackableLink, final Recipient recipient, final ComExtensibleUID uid, final String fullUrl, final HttpServletResponse response, final Company company) {
		String newFullUrl = fullUrl;
		
		if(!recipient.isDoNotTrackMe()) {
			if (trackableLink.getDeepTracking() != TrackableLink.DEEPTRACKING_NONE) {
				final int deepTrackingSessionID = (int) (Math.random() * 10000000.0);
				final String deepTrackingUID = getLinkService().createDeepTrackingUID(uid.getCompanyID(), uid.getMailingID(), uid.getUrlID(), uid.getCustomerID());

				switch (trackableLink.getDeepTracking()) {
					case TrackableLink.DEEPTRACKING_NONE:
						// Do nothing
						break;
					
					case TrackableLink.DEEPTRACKING_ONLY_COOKIE:
						if (deepTrackingUID != null) {
							setDeepTrackingCookie(response, getConfigService().getIntegerValue(ConfigValue.CookieExpire, company.getId()), trackableLink.getCompanyID(), deepTrackingSessionID, deepTrackingUID);
						}
						break;
		
					default: // do nothing
				}
			}
		} else {
			// Remove tracking cookie
			DeepTrackingCookieUtil.removeTrackingCookie(response, company.getId());
		}
		
		return newFullUrl;
	}
	
	private final ComExtensibleUID decodeUid(final String uid, final HttpServletRequest request ) throws Exception {
		if (uid == null) {
			// See Wiki for DeepTracking documentation:
			// http://wiki.agnitas.local/doku.php?id=support:howtos:shopmessung
			final String uidFromRequest = request.getQueryString();
			if (uidFromRequest == null || uidFromRequest.length() < 33) {
				logger.error("service: uid missing");
				
				throw new Exception("UID is missing");
			}
			return decodeTag(uidFromRequest);
		} else {
			return getExtensibleUIDService().parse(uid);
		}
	}
	
	
	
	private final void sendRedirect(final HttpServletResponse response, final String redirectUrl, final int companyID) throws IOException {
		final String punycodeFullUrl = punycodeEncodeDomainInLink(redirectUrl);
		response.sendRedirect(punycodeFullUrl);
	}
	
	private final String punycodeEncodeDomainInLink(final String url) {
		try {
			return PunycodeCodec.encodeDomainInLink(url);
		} catch(final Exception e) {
			logger.warn(String.format("Error Punycode-encoding url '%s'. Using unencoded URL", url), e);
			
			return url;
		}
	}

	private final void executeLinkActions(final ComExtensibleUID uid, final int deviceID, final DeviceClass deviceClass, final TrackableLink trackableLink, final HttpServletRequest request) throws Exception {
		final int companyID = uid.getCompanyID();
        final int mailingID = uid.getMailingID();
        final int customerID = uid.getCustomerID();
        
        // Execute the mailing click action
        final int clickActionID = getMailingDao().getMailingClickAction(mailingID, companyID);
        executeLinkAction(clickActionID, deviceID, companyID, customerID, mailingID, deviceClass, request);
        
        final int linkActionID = trackableLink.getActionID();
        executeLinkAction(linkActionID, deviceID, companyID, customerID, mailingID, deviceClass, request);
	
	}
	
	private final void executeLinkAction(final int actionID, final int deviceID, final int companyID, final int customerID, final int mailingID, final DeviceClass deviceClass, final HttpServletRequest request) throws Exception {
		if (actionID != 0) {
			// "_request" is the original unmodified request which might be needed (and used) somewhere else ...
			final Map<String, String> tmpRequestParams = AgnUtils.getReqParameters(request);
			tmpRequestParams.put("mobileDevice", String.valueOf(deviceClass == DeviceClass.MOBILE ? deviceID : 0)); // for mobile detection
			
			final EmmActionOperationErrors actionOperationErrors = new EmmActionOperationErrors();
			
			final CaseInsensitiveMap<String, Object> params = new CaseInsensitiveMap<>();
			params.put("requestParameters", tmpRequestParams);
			params.put("_request", request);
			params.put("customerID", customerID);
			params.put("mailingID", mailingID);
			params.put("actionErrors", actionOperationErrors);
			getEmmActionService().executeActions(actionID, companyID, params, actionOperationErrors);
		}
		
	}
	
	// TODO Remove setting max-age? According to EMM-8086, the cookie is session-based. (-> max-age < 0)
	private void setDeepTrackingCookie(HttpServletResponse response, int maximumAge, int companyID, long deepTrackingSession, String deepTrackingUID) {
		final Optional<SameSiteCookiePolicy> sameSiteOpt = configService.getEnum(ConfigValue.DeepTrackingCookieSameSitePolicy, companyID, SameSiteCookiePolicy.class);
		final SameSiteCookiePolicy sameSite = sameSiteOpt.orElse(null);
		
		if (maximumAge != 0) {
			// Persist cookie for given period of time
			DeepTrackingCookieUtil.addTrackingCookie(response, maximumAge, companyID, deepTrackingSession, deepTrackingUID, sameSite);
		} else if (getConfigService().getIntegerValue(ConfigValue.CookieExpire) != 0) {
			// Persist cookie for configured period of time
			DeepTrackingCookieUtil.addTrackingCookie(response, getConfigService().getIntegerValue(ConfigValue.CookieExpire), companyID, deepTrackingSession, deepTrackingUID, sameSite);
		} else {
			// Do not persist cookie
			DeepTrackingCookieUtil.addTrackingCookie(response, -1, companyID, deepTrackingSession, deepTrackingUID, sameSite);
		}
	}
	
	

	private TimeoutLRUMap<Integer, TrackableLink> getUrlCache() {
		if (urlCache == null) {
			urlCache = new TimeoutLRUMap<>(getConfigService().getIntegerValue(ConfigValue.RedirectKeysMaxCache), getConfigService().getIntegerValue(ConfigValue.RedirectKeysMaxCacheTimeMillis));
		}
		return urlCache;
	}

	public void setUrlCache(TimeoutLRUMap<Integer, TrackableLink> urlCache) {
		this.urlCache = urlCache;
	}

	public void setDeprecatedUIDCache(TimeoutLRUMap<Integer, String> deprecatedUIDCache) {
		this.deprecatedUIDCache = deprecatedUIDCache;
	}

	private ComExtensibleUID decodeTag(String tag) {
		DeepTrackingToken deepTrackingToken;
		try {
			deepTrackingToken = DeepTrackingToken.parseTokenString(tag);
		} catch (Exception e) {
			if (logger.isInfoEnabled()) {
				logger.info("Invalid deep tracking token: " + e.getMessage(), e);
			}
			return null;
		}
		
		final ComExtensibleUID uid = UIDFactory.from(
				null,
				getConfigService().getLicenseID(),
				deepTrackingToken.getCompanyID(),
				deepTrackingToken.getCustomerID(),
				deepTrackingToken.getMailingID(),
				deepTrackingToken.getLinkID()
		);

		return uid;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	private ApplicationContext getApplicationContext() {
		if (applicationContext == null) {
			applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		}
		return applicationContext;
	}

	public void setLinkService(LinkService linkService) {
		this.linkService = linkService;
	}

	private LinkService getLinkService() {
		if (linkService == null) {
			linkService = (LinkService) getApplicationContext().getBean("LinkService");
		}
		return linkService;
	}

	public void setAccessDataService(ComAccessDataService accessDataService) {
		this.accessDataService = accessDataService;
	}

	private ComAccessDataService getAccessDataService() {
		if (accessDataService == null) {
			accessDataService = (ComAccessDataService) getApplicationContext().getBean("AccessDataService");
		}
		return accessDataService;
	}

	public void setDeviceService(ComDeviceService comDeviceService) {
		this.deviceService = comDeviceService;
	}

	private ComDeviceService getDeviceService() {
		if (deviceService == null) {
			deviceService = (ComDeviceService) getApplicationContext().getBean("DeviceService");
		}
		return deviceService;
	}

	public void setClientService(ClientService clientService) {
		this.clientService = clientService;
	}

	private ClientService getClientService() {
		if (clientService == null) {
			clientService = (ClientService) getApplicationContext().getBean("ClientService");
		}
		return clientService;
	}

	public void setExtensibleUIDService(ExtensibleUIDService extensibleUIDService) {
		this.extensibleUIDService = extensibleUIDService;
	}

	private ExtensibleUIDService getExtensibleUIDService() {
		if (extensibleUIDService == null) {
			extensibleUIDService = (ExtensibleUIDService) getApplicationContext().getBean(ExtensibleUIDConstants.SERVICE_BEAN_NAME);
		}
		return extensibleUIDService;
	}

	@Required
	public void setTrackableLinkDao(ComTrackableLinkDao trackableLinkDao) {
		this.trackableLinkDao = trackableLinkDao;
	}

	private ComTrackableLinkDao getTrackableLinkDao() {
		if (trackableLinkDao == null) {
			trackableLinkDao = (ComTrackableLinkDao) getApplicationContext().getBean("TrackableLinkDao");
		}
		return trackableLinkDao;
	}

	@Required
	public void setEmmActionService(EmmActionService emmActionService) {
		this.emmActionService = emmActionService;
	}

	private EmmActionService getEmmActionService() {
		if (emmActionService == null) {
			emmActionService = (EmmActionService) getApplicationContext().getBean("EmmActionService");
		}
		return emmActionService;
	}

	@Required
	public void setCompanyDao(ComCompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	private ComCompanyDao getCompanyDao() {
		if (companyDao == null) {
			companyDao = (ComCompanyDao) getApplicationContext().getBean("CompanyDao");
		}
		return companyDao;
	}

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	private ConfigService getConfigService() {
		if (configService == null) {
			configService = (ConfigService) getApplicationContext().getBean("ConfigService");
		}
		return configService;
	}

	@Required
	public void setMailingDao(ComMailingDao comMailingDao) {
		this.mailingDao = comMailingDao;
	}

	private ComMailingDao getMailingDao() {
		if (mailingDao == null) {
			mailingDao = (ComMailingDao) getApplicationContext().getBean("MailingDao");
		}
		return mailingDao;
	}

	@Required
	public final void setRecipientService(final RecipientService service) {
		this.recipientService = Objects.requireNonNull(service, "Recipient service is null");
	}
	
	private RecipientService getRecipientService() {
		if(recipientService == null) {
			this.recipientService = getApplicationContext().getBean(RecipientService.class, "RecipientService");
		}
		
		return this.recipientService;
	}
	
	public void setIntelliAdMailingSettingsCache(IntelliAdMailingSettingsCache intelliAdMailingSettingsCache) {
		this.intelliAdMailingSettingsCache = intelliAdMailingSettingsCache;
	}
	
	private IntelliAdMailingSettingsCache getIntelliAdMailingSettingsCache() {
		if (intelliAdMailingSettingsCache == null) {
			intelliAdMailingSettingsCache = (IntelliAdMailingSettingsCache) getApplicationContext().getBean("IntelliAdMailingSettingsCache");
		}

		return intelliAdMailingSettingsCache;
	}

	public void setSnowflakeMailingCache(SnowflakeMailingCache snowflakeMailingCache) {
		this.snowflakeMailingCache = snowflakeMailingCache;
	}

	private SnowflakeMailingCache getSnowflakeMailingCache() {
		if (snowflakeMailingCache == null) {
			snowflakeMailingCache = (SnowflakeMailingCache) getApplicationContext().getBean("SnowflakeMailingCache");
		}

		return snowflakeMailingCache;
	}
	
	public final ClickTrackingService getClickTrackingService() {
		if (clickTrackingService == null) {
			clickTrackingService = (ClickTrackingService) getApplicationContext().getBean("MailClickTrackingService");
		}
		
		return clickTrackingService;
	}

	private boolean isIntelliAdUsed(int mailingId, int companyId) {
		IntelliAdMailingSettings settings = getIntelliAdMailingSettingsCache().getIntelliAdSettings(companyId, mailingId);

		return settings != null && settings.isIntelliAdEnabled();
	}

	private String createIntelliAdLink(String targetUrl, int mailingId, int companyId) {
		IntelliAdMailingSettings settings = getIntelliAdMailingSettingsCache().getIntelliAdSettings(companyId, mailingId);

		try {
			IntelliAdTrackingData trackingData = IntelliAdTrackingStringParser.parse(settings.getTrackingString());

			LightweightMailing mailing = getSnowflakeMailingCache().getSnowflakeMailing(companyId, mailingId);
			String subId = mailingId + "|" + mailing.getShortname();

			StringBuffer buffer = new StringBuffer();
			buffer.append("http://t23.intelliad.de/index.php?redirect=");
			buffer.append(URLEncoder.encode(targetUrl, "UTF-8"));
			buffer.append("&cl=");
			buffer.append(trackingData.getCustomerId());
			buffer.append("&bm=");
			buffer.append(trackingData.getMarketId());
			buffer.append("&bmcl=");
			buffer.append(trackingData.getChannelId());
			buffer.append("&cp=");
			buffer.append(trackingData.getCampaignId());
			buffer.append("&ag=");
			buffer.append(trackingData.getAdgroupId());
			buffer.append("&cr=");
			buffer.append(trackingData.getCriterionId());
			buffer.append("&subid=" + URLEncoder.encode(subId, "UTF-8"));

			return buffer.toString();
		} catch(Exception e) {
			logger.error("Error creating IntelliAd link - do not create one", e);

			return targetUrl;
		}
	}
	
	private static final String referenceTableRecordSelector(final HttpServletRequest request, final Company company, final long customerID) {
		final String encrypted = request.getParameter("ref");
		
		if (encrypted == null) {
			return null;
		}

		try {
			final Decrypt decrypt = new Decrypt(company.getSecretKey());
			return decrypt.decrypt(encrypted, customerID);
		} catch(final Exception e) {
			final String msg = String.format("Cannot decrypt 'ref' paramater (value: %s)", encrypted);
			logger.error(msg, e);
			
			return "";
		}
	}
	
	private final String getRdirUndecodableLinkUrl() {
		return getConfigService().getValue(ConfigValue.RdirUndecodableLinkUrl);
	}
	
	private final ComTrackableLink loadTrackableLink(final ComExtensibleUID uid, final boolean cachingDisabled) throws Exception {
		ComTrackableLink trackableLink;
		
		if (!cachingDisabled) {
			// If caching not disabled, do normal job (get link from cache if there, otherwise get it from DB)
			trackableLink = (ComTrackableLink) getUrlCache().get(uid.getUrlID());
			if (trackableLink == null || trackableLink.getCompanyID() != uid.getCompanyID()) {
				// get link and do actions
				trackableLink = getTrackableLinkDao().getTrackableLink(uid.getUrlID(), uid.getCompanyID());
				if (trackableLink != null) {
					getUrlCache().put(uid.getUrlID(), trackableLink);
				}
			}
		} else {
			// If caching is disabled, always read link from DB and do not cache it.
			trackableLink = getTrackableLinkDao().getTrackableLink(uid.getUrlID(), uid.getCompanyID());
		}

		if (trackableLink == null) {
			logger.error("service: trackable link not found: CID=" + uid.getCompanyID() + " LID=" + uid.getUrlID());
			throw new Exception("service: trackable link not found: CID=" + uid.getCompanyID() + " LID=" + uid.getUrlID());
		}

		return trackableLink;
	}

}
