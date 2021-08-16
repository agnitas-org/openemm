/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.send;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.Mailinglist;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.commons.util.DateUtil;
import org.agnitas.emm.core.useractivitylog.dao.UserActivityLogDao;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.agnitas.util.MailoutClient;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailgunOptions;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingService;
import com.agnitas.emm.core.mailing.web.MailingPreviewHelper;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.report.enums.fields.MailingTypes;
import com.agnitas.emm.restful.BaseRequestResponse;
import com.agnitas.emm.restful.ErrorCode;
import com.agnitas.emm.restful.JsonRequestResponse;
import com.agnitas.emm.restful.ResponseType;
import com.agnitas.emm.restful.RestfulClientException;
import com.agnitas.emm.restful.RestfulServiceHandler;
import com.agnitas.json.Json5Reader;
import com.agnitas.json.JsonDataType;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.mailing.preview.service.MailingPreviewService;
import com.agnitas.util.ClassicTemplateGenerator;

/**
 * This restful service is available at:
 * https://<system.url>/restful/send
 */
public class SendRestfulServiceHandler implements RestfulServiceHandler, ApplicationContextAware {
	private static final transient Logger logger = Logger.getLogger(SendRestfulServiceHandler.class);
	
	public static final String NAMESPACE = "send";

	protected UserActivityLogDao userActivityLogDao;
	protected MailingService mailingService;
	protected ComMailingDao mailingDao;
	protected ComRecipientDao recipientDao;
	protected MailinglistDao mailinglistDao;
	protected MaildropService maildropService;
	protected ClassicTemplateGenerator classicTemplateGenerator;
	protected SendActionbasedMailingService sendActionbasedMailingService;
	protected ConfigService configService;
	protected MailingPreviewService mailingPreviewService;
	
	/**
	 * @deprecated Replace this general dependency by specific bean references
	 */
	@Deprecated
	private ApplicationContext applicationContext;

	@Required
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "ConfigService is null");
	}
	
	@Required
	public final void setMailingPreviewService(final MailingPreviewService service) {
		this.mailingPreviewService = Objects.requireNonNull(service, "MailingPreviewService is null");
	}
	
	@Required
	public void setUserActivityLogDao(UserActivityLogDao userActivityLogDao) {
		this.userActivityLogDao = userActivityLogDao;
	}
	
	@Required
	public void setMailingService(MailingService mailingService) {
		this.mailingService = mailingService;
	}
	
	@Required
	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
	
	@Required
	public void setRecipientDao(ComRecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}
	
	@Required
	public void setMailinglistDao(MailinglistDao mailinglistDao) {
		this.mailinglistDao = mailinglistDao;
	}
	
	@Required
	public void setMaildropService(MaildropService maildropService) {
		this.maildropService = maildropService;
	}
	
	@Required
	public void setClassicTemplateGenerator(ClassicTemplateGenerator classicTemplateGenerator) {
		this.classicTemplateGenerator = classicTemplateGenerator;
	}
	
	@Required
	public void setSendActionbasedMailingService(SendActionbasedMailingService sendActionbasedMailingService) {
		this.sendActionbasedMailingService = sendActionbasedMailingService;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) throws Exception {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, ComAdmin admin, String requestDataFilePath, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			throw new RestfulClientException("Invalid http request method 'GET'. Only 'PUT' or 'POST' are supported for 'send'.");
		} else if (requestMethod == RequestMethod.DELETE) {
			throw new RestfulClientException("Invalid http request method 'DELETE'. Only 'PUT' or 'POST' are supported for 'send'.");
		} else if (requestDataFilePath == null || new File(requestDataFilePath).length() <= 0) {
			restfulResponse.setError(new RestfulClientException("Missing request data"), ErrorCode.REQUEST_DATA_ERROR);
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(sendMailing(request, new File(requestDataFilePath), admin)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(sendMailing(request, new File(requestDataFilePath), admin)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	/**
	 * Return a single or multiple send data sets
	 * 
	 * @param request
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	protected Object sendMailing(HttpServletRequest request, File requestDataFile, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_SEND_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_SEND_SHOW.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		
		if (!AgnUtils.isNumber(restfulContext[0])) {
			throw new RestfulClientException("Invalid MailingID: " + restfulContext[0]);
		}
		int mailingID = Integer.parseInt(restfulContext[0]);
		if (!mailingDao.exist(mailingID, admin.getCompanyID())) {
			throw new RestfulClientException("Invalid not existing MailingID: " + mailingID);
		}
		
		// Send Admin, Test or World Mailing
		userActivityLogDao.addAdminUseOfFeature(admin, "restful/send", new Date());
		userActivityLogDao.writeUserActivityLog(admin, "restful/send", "" + mailingID);

		MaildropStatus maildropStatus = null;
		Date sendDate = new Date();
		int stepping = 0;
		int blockSize = 0;
		int customerID = 0;
		String customerEmail = null;
		int userStatus = 0;
		
		Map<String, String> profileDataOverrides = null;
		
		try (InputStream inputStream = new FileInputStream(requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				JsonNode jsonNode = jsonReader.read();
				if (JsonDataType.OBJECT == jsonNode.getJsonDataType()) {
					JsonObject jsonObject = (JsonObject) jsonNode.getValue();
					for (Entry<String, Object> entry : jsonObject.entrySet()) {
						if ("send_type".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof String && ((String) entry.getValue()).length() == 1) {
								try {
									maildropStatus = MaildropStatus.fromCode(((String) entry.getValue()).charAt(0));
								} catch (Exception e) {
									throw new RestfulClientException("Invalid value for 'send_type'. String 'W', 'A', 'T', 'E' or 'R' expected");
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'send_type'. String 'W', 'A', 'T', 'E' or 'R' expected");
							}
						} else if ("send_date".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof String) {
								try {
									sendDate = DateUtilities.parseIso8601DateTimeString((String) entry.getValue());
								} catch (Exception e) {
									throw new RestfulClientException("Invalid value for 'send_date'. String(Date) expected");
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'send_date'. String(Date) expected");
							}
						} else if ("stepping".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof Integer) {
								stepping = (Integer) entry.getValue();
							} else {
								throw new RestfulClientException("Invalid data type for 'stepping'. Integer expected");
							}
						} else if ("block_size".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof Integer) {
								blockSize = (Integer) entry.getValue();
							} else {
								throw new RestfulClientException("Invalid data type for 'block_size'. Integer expected");
							}
						} else if ("customer_id".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof Integer) {
								customerID = (Integer) entry.getValue();
							} else {
								throw new RestfulClientException("Invalid data type for 'customer_id'. Integer expected");
							}
						} else if ("email".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof String && AgnUtils.isEmailValid((String) entry.getValue())) {
								customerEmail = AgnUtils.normalizeEmail((String) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'email'. Email address expected");
							}
						} else if ("user_status".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof Integer) {
								userStatus = (Integer) entry.getValue();
							} else {
								throw new RestfulClientException("Invalid data type for 'user_status'. Integer expected");
							}
						} else if ("data".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof JsonObject) {
								JsonObject profileData = (JsonObject) entry.getValue();
								profileDataOverrides = new HashMap<>();
								for (Entry<String, Object> item : profileData.entrySet()) {
									profileDataOverrides.put(item.getKey(), item.getValue() == null ? "" : item.getValue().toString());
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'data'. JsonObject expected");
							}
						} else {
							throw new RestfulClientException("Invalid property '" + entry.getKey() + "' for 'send'");
						}
					}
				}
			}
		}
		
		if (maildropStatus == null) {
			throw new RestfulClientException("Missing property value for 'send_type'. String 'W', 'A', 'T', 'E' or 'R' expected");
		} else if (customerID != 0 && StringUtils.isNotBlank(customerEmail)) {
			throw new RestfulClientException("Colliding parameters customer_id and email detected. Only one of both is allowed");
		}
		
		if (customerID == 0 && StringUtils.isNotBlank(customerEmail)) {
			List<Integer> result = recipientDao.getRecipientIDs(admin.getCompanyID(), "email", customerEmail);
			if (result.size() > 1) {
				throw new RestfulClientException("More than one recipient found for this email address");
			} else if (result.size() < 1) {
				throw new RestfulClientException("No recipient found for this email address");
			} else {
				customerID = result.get(0);
			}
		}
		
		return sendMailing(admin, maildropStatus, mailingID, sendDate, stepping, blockSize, customerID, userStatus, profileDataOverrides);
	}
	
    protected String sendMailing(ComAdmin admin, MaildropStatus maildropStatus, int mailingID, Date sendDate, int stepping, int blockSize, int customerID, int userStatus, Map<String, String> profileDataOverrides) throws Exception {
		boolean adminSend = false;
		boolean testSend = false;
		boolean worldSend = false;
		boolean isPreserveTrackableLinks = false;
		java.util.Date genDate = new java.util.Date();
		int startGen = 1;
		MaildropEntry maildropEntry = new MaildropEntryImpl();
		
		final boolean useBackendPreview = configService.getBooleanValue(ConfigValue.Development.UseBackendMailingPreview, admin.getCompanyID());

		switch (maildropStatus) {
			case ADMIN:
				maildropEntry.setStatus(MaildropStatus.ADMIN.getCode());
				adminSend = true;
				break;
	
			case TEST:
				maildropEntry.setStatus(MaildropStatus.TEST.getCode());
				adminSend = true;
				testSend = true;
				break;
	
			case WORLD:
				if (!admin.permissionAllowed(Permission.MAILING_SEND_WORLD)) {
					throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_SEND_WORLD.toString() + "'");
				}
				
				maildropEntry.setStatus(MaildropStatus.WORLD.getCode());
				adminSend = true;
				testSend = true;
				worldSend = true;
				isPreserveTrackableLinks = true;
				break;
	
			case DATE_BASED:
				if (!admin.permissionAllowed(Permission.MAILING_SEND_WORLD)) {
					throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_SEND_WORLD.toString() + "'");
				}
				
				maildropEntry.setStatus(MaildropStatus.DATE_BASED.getCode());
				worldSend = true;
				break;
	
			case ACTION_BASED:
				if (!admin.permissionAllowed(Permission.MAILING_SEND_WORLD)) {
					throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_SEND_WORLD.toString() + "'");
				}
				
				maildropEntry.setStatus(MaildropStatus.ACTION_BASED.getCode());
				worldSend = true;
				break;
	
			default:
				break;
		}

		Mailing mailing = mailingDao.getMailing(mailingID, admin.getCompanyID());

		if (mailing == null) {
			throw new RestfulClientException("This mailing not found. MailingID: " + mailingID);
		}

		Mailinglist aList = mailinglistDao.getMailinglist(mailing.getMailinglistID(), admin.getCompanyID());
		String preview = null;

		if (mailinglistDao.getNumberOfActiveSubscribers(adminSend, testSend, worldSend, mailing.getTargetID(), aList.getCompanyID(), aList.getId()) == 0) {
			throw new RestfulClientException("This mailing has no subscribers");
		}

		// check syntax of mailing by generating dummy preview
		preview = useBackendPreview
				? this.mailingPreviewService.renderTextPreview(mailing.getId(), customerID)
				: mailing.getPreview(mailing.getTextTemplate().getEmmBlock(), MailingPreviewHelper.INPUT_TYPE_HTML, 0, applicationContext);
		if (StringUtils.isBlank(preview)) {
			if (mailingService.isTextVersionRequired(admin.getCompanyID(), mailingID)) {
				throw new RestfulClientException("Mandatory TEXT version is missing in mailing");
			}
		}
		preview = useBackendPreview
				? this.mailingPreviewService.renderHtmlPreview(mailing.getId(), customerID)
				: mailing.getPreview(mailing.getHtmlTemplate().getEmmBlock(), MailingPreviewHelper.INPUT_TYPE_HTML, 0, applicationContext);
		boolean isHtmlMailing = false;
		MediatypeEmail emailMediaType = (MediatypeEmail) mailing.getMediatypes().get(MediaTypes.EMAIL.getMediaCode());
		if (emailMediaType != null && (emailMediaType.getMailFormat() == MailType.HTML.getIntValue() || emailMediaType.getMailFormat() == MailType.HTML_OFFLINE.getIntValue())) {
			isHtmlMailing = true;
		}
		if (isHtmlMailing && preview.trim().length() == 0) {
			throw new RestfulClientException("Mandatory HTML version is missing in mailing");
		}
		
		preview = useBackendPreview
				? this.mailingPreviewService.renderPreviewFor(mailing.getId(), customerID, mailing.getEmailParam().getSubject())
				: mailing.getPreview(mailing.getEmailParam().getSubject(), MailingPreviewHelper.INPUT_TYPE_HTML, 0, applicationContext);
		if (StringUtils.isBlank(mailing.getEmailParam().getSubject())) {
			throw new RestfulClientException("Mailing subject is too short");
		}
		
		preview = useBackendPreview
				? this.mailingPreviewService.renderPreviewFor(mailing.getId(), customerID, mailing.getEmailParam().getFromAdr())
				: mailing.getPreview(mailing.getEmailParam().getFromAdr(), MailingPreviewHelper.INPUT_TYPE_HTML, 0, applicationContext);
		if (preview.trim().length() == 0) {
			throw new RestfulClientException("Mandatory mailing sender address is missing");
		}

		if (maildropStatus == MaildropStatus.ACTION_BASED) {
			if (!maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID())) {
				maildropEntry.setGenStatus(startGen);
				maildropEntry.setGenDate(genDate);
				maildropEntry.setGenChangeDate(new java.util.Date());
				maildropEntry.setMailingID(mailing.getId());
				maildropEntry.setCompanyID(mailing.getCompanyID());
				maildropEntry.setStepping(stepping);
				maildropEntry.setBlocksize(blockSize);
	
				mailing.getMaildropStatus().add(maildropEntry);
	
				mailingDao.saveMailing(mailing, isPreserveTrackableLinks);
			}
			
			if (customerID > 0) {
				try {
					List<Integer> userStatusList = null;
					if (userStatus > 0) {
						userStatusList = new ArrayList<>();
	
						if (userStatus == UserStatus.Active.getStatusCode() || userStatus == UserStatus.WaitForConfirm.getStatusCode()) {
							// This block is for backward compatibility only!
							userStatusList.add(UserStatus.Active.getStatusCode());
							userStatusList.add(UserStatus.WaitForConfirm.getStatusCode());
						} else {
							userStatusList.add(userStatus);
						}
					}
					
					final MailgunOptions mailgunOptions = new MailgunOptions();
					if (userStatusList != null) {
						mailgunOptions.withAllowedUserStatus(userStatusList);
					}
					
					if (profileDataOverrides != null && profileDataOverrides.size() > 0) {
						mailgunOptions.withProfileFieldValues(profileDataOverrides);
					}
	
					try {
						sendActionbasedMailingService.sendActionbasedMailing(mailing.getCompanyID(), mailing.getId(), customerID, 0, mailgunOptions);
						return "Mailing sent";
					} catch(final Exception e) {
						logger.error("Cannot fire campaign-/event-mail", e);
						throw new RestfulClientException("Error sending action-based mailing: " + mailing.getId() + "/" + customerID, e);
					}
				} catch(final Exception e) {
					logger.error("Error sending action-based mailing", e);
					throw new RestfulClientException("Error sending action-based mailing: " + mailing.getId() + "/" + customerID, e);
				}
			} else {
				return "Mailing activated";
			}
		} else {
			if (sendDate != null) {
				GregorianCalendar aCal = new GregorianCalendar(TimeZone.getTimeZone(admin.getAdminTimezone()));
				aCal.setTime(sendDate);
				sendDate = aCal.getTime();
			}
			
			maildropEntry.setSendDate(sendDate);

			if (!DateUtil.isSendDateForImmediateDelivery(sendDate)) {
				// sent gendate if senddate is in future
				GregorianCalendar tmpGen = new GregorianCalendar();
				GregorianCalendar now = new GregorianCalendar();

				tmpGen.setTime(sendDate);
				tmpGen.add(Calendar.MINUTE, -mailingService.getMailGenerationMinutes(mailing.getCompanyID()));
				if (tmpGen.before(now)) {
					tmpGen = now;
				}
				genDate = tmpGen.getTime();
			}

			if (!DateUtil.isDateForImmediateGeneration(genDate)
					&& ((mailing.getMailingType() == MailingTypes.NORMAL.getCode()) || (mailing.getMailingType() == MailingTypes.FOLLOW_UP.getCode()))) {
				startGen = 0;
			}
			
			if (!worldSend || !maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID())) {
				maildropEntry.setGenStatus(startGen);
				maildropEntry.setGenDate(genDate);
				maildropEntry.setGenChangeDate(new java.util.Date());
				maildropEntry.setMailingID(mailing.getId());
				maildropEntry.setCompanyID(mailing.getCompanyID());
				maildropEntry.setStepping(stepping);
				maildropEntry.setBlocksize(blockSize);
	
				mailing.getMaildropStatus().add(maildropEntry);
	
				mailingDao.saveMailing(mailing, isPreserveTrackableLinks);
				if (startGen == 1 && maildropEntry.getStatus() != MaildropStatus.ACTION_BASED.getCode() && maildropEntry.getStatus() != MaildropStatus.DATE_BASED.getCode()) {
					classicTemplateGenerator.generate(mailingID, admin.getAdminID(), admin.getCompanyID(), true, true);
					
					MailoutClient aClient = new MailoutClient();
					aClient.invoke("fire", Integer.toString(maildropEntry.getId()));
				}
			}
			
			if (maildropStatus == MaildropStatus.WORLD || maildropStatus == MaildropStatus.ADMIN || maildropStatus == MaildropStatus.TEST) {
				return "Mailing started";
			} else {
				return "Mailing activated";
			}
		}
    }

	@Override
	public ResponseType getResponseType() {
		return ResponseType.JSON;
	}
}
