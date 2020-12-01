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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.Mailinglist;
import org.agnitas.dao.MailinglistDao;
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
import com.agnitas.beans.ComMailing;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailingService;
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
import com.agnitas.util.ClassicTemplateGenerator;

/**
 * This restful service is available at:
 * https:/<system.url>/restful/send
 */
public class SendRestfulServiceHandler implements RestfulServiceHandler, ApplicationContextAware {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(SendRestfulServiceHandler.class);
	
	public static final String NAMESPACE = "send";

	private UserActivityLogDao userActivityLogDao;
	private MailingService mailingService;
	private ComMailingDao mailingDao;
	private MailinglistDao mailinglistDao;
	private MaildropService maildropService;
	private ClassicTemplateGenerator classicTemplateGenerator;
	/**
	 * @deprecated Replace this general dependency by specific bean references
	 */
	@Deprecated
	private ApplicationContext applicationContext;

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
	private Object sendMailing(HttpServletRequest request, File requestDataFile, ComAdmin admin) throws Exception {
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
						} else {
							throw new RestfulClientException("Invalid property '" + entry.getKey() + "' for 'send'");
						}
					}
				}
			}
		}
		
		if (maildropStatus == null) {
			throw new RestfulClientException("Missing property value for 'send_type'. String 'W', 'A', 'T', 'E' or 'R' expected");
		}
		
		sendMailing(admin, maildropStatus, mailingID, sendDate, stepping, blockSize);
		
		if (maildropStatus == MaildropStatus.WORLD || maildropStatus == MaildropStatus.ADMIN || maildropStatus == MaildropStatus.TEST) {
			return "Mailing started";
		} else {
			return "Mailing activated";
		}
	}
	
    protected void sendMailing(ComAdmin admin, MaildropStatus maildropStatus, int mailingID, Date sendDate, int stepping, int blockSize) throws Exception {
		boolean adminSend = false;
		boolean testSend = false;
		boolean worldSend = false;
		boolean isPreserveTrackableLinks = false;
		java.util.Date genDate = new java.util.Date();
		int startGen = 1;
		MaildropEntry maildropEntry = new MaildropEntryImpl();

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

		if (sendDate != null) {
			GregorianCalendar aCal = new GregorianCalendar(TimeZone.getTimeZone(admin.getAdminTimezone()));
			aCal.setTime(sendDate);
			sendDate = aCal.getTime();
		}

		ComMailing mailing = mailingDao.getMailing(mailingID, admin.getCompanyID());

		if (mailing == null) {
			return;
		}

		Mailinglist aList = mailinglistDao.getMailinglist(mailing.getMailinglistID(), admin.getCompanyID());
		String preview = null;

		if (mailinglistDao.getNumberOfActiveSubscribers(adminSend, testSend, worldSend, mailing.getTargetID(), aList.getCompanyID(), aList.getId()) == 0) {
			throw new RestfulClientException("This mailing has no subscribers");
		}

		// check syntax of mailing by generating dummy preview
		preview = mailing.getPreview(mailing.getTextTemplate().getEmmBlock(), MailingPreviewHelper.INPUT_TYPE_HTML, 0, applicationContext);
		if (StringUtils.isBlank(preview)) {
			if (mailingService.isTextVersionRequired(admin.getCompanyID(), mailingID)) {
				throw new RestfulClientException("Mandatory TEXT version is missing in mailing");
			}
		}
		preview = mailing.getPreview(mailing.getHtmlTemplate().getEmmBlock(), MailingPreviewHelper.INPUT_TYPE_HTML, 0, applicationContext);
		boolean isHtmlMailing = false;
		MediatypeEmail emailMediaType = (MediatypeEmail) mailing.getMediatypes().get(MediaTypes.EMAIL.getMediaCode());
		if (emailMediaType != null && (emailMediaType.getMailFormat() == MailType.HTML.getIntValue() || emailMediaType.getMailFormat() == MailType.HTML_OFFLINE.getIntValue())) {
			isHtmlMailing = true;
		}
		if (isHtmlMailing && preview.trim().length() == 0) {
			throw new RestfulClientException("Mandatory HTML version is missing in mailing");
		}
		preview = mailing.getPreview(mailing.getEmailParam().getSubject(), MailingPreviewHelper.INPUT_TYPE_HTML, 0, applicationContext);
		if (StringUtils.isBlank(mailing.getEmailParam().getSubject())) {
			throw new RestfulClientException("Mailing subject is too short");
		}
		preview = mailing.getPreview(mailing.getEmailParam().getFromAdr(), MailingPreviewHelper.INPUT_TYPE_HTML, 0, applicationContext);
		if (preview.trim().length() == 0) {
			throw new RestfulClientException("Mandatory mailing sender address is missing");
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

		if (worldSend && maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID())) {
			return;
		}

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

	@Override
	public ResponseType getResponseType() {
		return ResponseType.JSON;
	}
}
