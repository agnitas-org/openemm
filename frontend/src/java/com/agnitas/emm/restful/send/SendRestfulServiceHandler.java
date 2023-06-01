/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.send;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TimeZone;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.beans.DatasourceDescription;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.Recipient;
import org.agnitas.beans.impl.BindingEntryImpl;
import org.agnitas.beans.impl.RecipientImpl;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.dao.SourceGroupType;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.commons.util.DateUtil;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.useractivitylog.dao.UserActivityLogDao;
import org.agnitas.preview.Page;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.agnitas.util.MailoutClient;
import org.agnitas.util.importvalues.Gender;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Admin;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.dao.ComBindingEntryDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.DatasourceDescriptionDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailgunOptions;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.restful.BaseRequestResponse;
import com.agnitas.emm.restful.ErrorCode;
import com.agnitas.emm.restful.JsonRequestResponse;
import com.agnitas.emm.restful.ResponseType;
import com.agnitas.emm.restful.RestfulClientException;
import com.agnitas.emm.restful.RestfulServiceHandler;
import com.agnitas.json.Json5Reader;
import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonDataType;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.mailing.preview.service.MailingPreviewService;
import com.agnitas.util.ClassicTemplateGenerator;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This restful service is available at:
 * https://<system.url>/restful/send
 */
public class SendRestfulServiceHandler implements RestfulServiceHandler {
	private static final transient Logger logger = LogManager.getLogger(SendRestfulServiceHandler.class);
	
	public static final String NAMESPACE = "send";

	protected UserActivityLogDao userActivityLogDao;
	protected MailingService mailingService;
	protected ComMailingDao mailingDao;
	protected RecipientService recipientService;
	protected MailinglistDao mailinglistDao;
	protected MaildropService maildropService;
	protected ClassicTemplateGenerator classicTemplateGenerator;
	protected SendActionbasedMailingService sendActionbasedMailingService;
	protected ConfigService configService;
	protected MailingPreviewService mailingPreviewService;
	protected DatasourceDescriptionDao datasourceDescriptionDao;
	protected ComBindingEntryDao bindingEntryDao;

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
	public void setRecipientService(RecipientService recipientService) {
		this.recipientService = recipientService;
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

	@Required
	public void setDatasourceDescriptionDao(final DatasourceDescriptionDao datasourceDescriptionDao) {
		this.datasourceDescriptionDao = datasourceDescriptionDao;
	}

	@Required
	public void setBindingEntryDao(final ComBindingEntryDao bindingEntryDao) {
		this.bindingEntryDao = bindingEntryDao;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) throws Exception {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, Admin admin, byte[] requestData, File requestDataFile, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod, boolean extendedLogging) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			throw new RestfulClientException("Invalid http request method 'GET'. Only 'PUT' or 'POST' are supported for 'send'.");
		} else if (requestMethod == RequestMethod.DELETE) {
			throw new RestfulClientException("Invalid http request method 'DELETE'. Only 'PUT' or 'POST' are supported for 'send'.");
		} else if ((requestData == null || requestData.length == 0) && (requestDataFile == null || requestDataFile.length() <= 0)) {
			restfulResponse.setError(new RestfulClientException("Missing request data"), ErrorCode.REQUEST_DATA_ERROR);
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(sendMailing(request, requestData, requestDataFile, admin, extendedLogging)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(sendMailing(request, requestData, requestDataFile, admin, extendedLogging)));
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
	protected Object sendMailing(HttpServletRequest request, byte[] requestData, File requestDataFile, Admin admin, boolean extendedLogging) throws Exception {
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
		boolean alwaysCreateRecipient = false;
		List<String> keyColumns = new ArrayList<>(Arrays.asList(new String[] { "email" }));
		UserStatus userStatus = null;
		
		JsonObject profileData = null;
		
		try (InputStream inputStream = RestfulServiceHandler.getRequestDataStream(requestData, requestDataFile)) {
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

								// Normalize email, if configured so
								if (!configService.getBooleanValue(ConfigValue.AllowUnnormalizedEmails, admin.getCompanyID())) {
									customerEmail = AgnUtils.normalizeEmail((String) entry.getValue());
								} else {
									customerEmail = (String) entry.getValue();
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'email'. Email address expected");
							}
						} else if ("user_status".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof Integer) {
								userStatus = UserStatus.getUserStatusByID((Integer) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'user_status'. Integer expected");
							}
						} else if ("data".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof JsonObject) {
								profileData = (JsonObject) entry.getValue();
							} else {
								throw new RestfulClientException("Invalid data type for 'data'. JsonObject expected");
							}
						} else if ("keyColumns".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof String) {
								keyColumns = AgnUtils.splitAndTrimList((String) entry.getValue());
							} else if (entry.getValue() != null && entry.getValue() instanceof JsonArray) {
								keyColumns = new ArrayList<>();
								for (Object keyColumn : (JsonArray) entry.getValue()) {
									if (keyColumn != null && keyColumn instanceof String) {
										keyColumns.add((String) keyColumn);
									} else {
										throw new RestfulClientException("Invalid data type for 'keyColumns'. String or Array of Strings expected");
									}
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'keyColumns'. String or Array of Strings expected");
							}
						} else if ("alwaysCreateRecipient".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof Boolean) {
								alwaysCreateRecipient = (Boolean) entry.getValue();
							} else {
								throw new RestfulClientException("Invalid data type for 'alwaysCreateRecipient'. Boolean expected");
							}
						} else {
							throw new RestfulClientException("Invalid property '" + entry.getKey() + "' for 'send'");
						}
					}
				}
			}
		}
		
		if (maildropStatus == null) {
			MailingType mailingType = mailingDao.getMailingType(mailingID);
			if (mailingType == MailingType.NORMAL) {
				maildropStatus = MaildropStatus.WORLD;
			} else if (mailingType == MailingType.ACTION_BASED) {
				maildropStatus = MaildropStatus.ACTION_BASED;
			} else if (mailingType == MailingType.DATE_BASED) {
				maildropStatus = MaildropStatus.DATE_BASED;
			} else if (mailingType == MailingType.INTERVAL) {
				maildropStatus = MaildropStatus.ON_DEMAND;
			} else {
				throw new RestfulClientException("Missing property value for 'send_type'. String 'W', 'A', 'T', 'E' or 'R' expected");
			}
		} else if (customerID != 0 && StringUtils.isNotBlank(customerEmail)) {
			throw new RestfulClientException("Colliding parameters customer_id and email detected. Only one of both is allowed");
		}
		
		if (customerID == 0 && profileData != null && profileData.size() > 0) {
			customerID = createRecipient(admin.getCompanyID(), mailingID, keyColumns, profileData, alwaysCreateRecipient);
		}

		if (customerID == 0 && StringUtils.isNotBlank(customerEmail)) {
			List<Integer> result = recipientService.getRecipientIds(admin.getCompanyID(), "email", customerEmail);
			if (result.size() > 1) {
				throw new RestfulClientException("More than one recipient found for this email address");
			} else if (result.size() < 1) {
				throw new RestfulClientException("No recipient found for this email address");
			} else {
				customerID = result.get(0);
			}
		}
		
		return sendMailing(admin, maildropStatus, mailingID, sendDate, stepping, blockSize, customerID, userStatus, profileData);
	}

	protected String sendMailing(Admin admin, MaildropStatus maildropStatus, int mailingID, Date sendDate, int stepping, int blockSize, int customerID, UserStatus userStatus, JsonObject profileData) throws Exception {
		boolean adminSend = false;
		boolean testSend = false;
		boolean worldSend = false;
		boolean isPreserveTrackableLinks = false;
		Date genDate = new Date();
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
	
			case ON_DEMAND:
				// Interval mailing
				if (customerID > 0) {
					return "Enlisted recipient for interval mailing. CustomerID: " + customerID;
				} else {
					// Activate intervall mailing
				}
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
        final Page previewPage = this.mailingPreviewService.renderPreview(mailing.getId(), customerID);
		preview = previewPage.getText();
		if (StringUtils.isBlank(preview)) {
			if (mailingService.isTextVersionRequired(admin.getCompanyID(), mailingID)) {
				throw new RestfulClientException("Mandatory TEXT version is missing in mailing");
			}
		}
		preview = previewPage.getHTML();
		boolean isHtmlMailing = false;
		MediatypeEmail emailMediaType = (MediatypeEmail) mailing.getMediatypes().get(MediaTypes.EMAIL.getMediaCode());
		if (emailMediaType != null && (emailMediaType.getMailFormat() == MailType.HTML.getIntValue() || emailMediaType.getMailFormat() == MailType.HTML_OFFLINE.getIntValue())) {
			isHtmlMailing = true;
		}
		if (isHtmlMailing && preview.trim().length() == 0) {
			throw new RestfulClientException("Mandatory HTML version is missing in mailing");
		}
		
		preview = this.mailingPreviewService.renderPreviewFor(mailing.getId(), customerID, mailing.getEmailParam().getSubject());
		if (StringUtils.isBlank(mailing.getEmailParam().getSubject())) {
			throw new RestfulClientException("Mailing subject is too short");
		}
		
		preview = this.mailingPreviewService.renderPreviewFor(mailing.getId(), customerID, mailing.getEmailParam().getFromAdr());
		if (preview.trim().length() == 0) {
			throw new RestfulClientException("Mandatory mailing sender address is missing");
		}

		if (maildropStatus == MaildropStatus.ACTION_BASED || maildropStatus == MaildropStatus.ON_DEMAND) {
			if (!maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID())) {
				maildropEntry.setGenStatus(startGen);
				maildropEntry.setGenDate(genDate);
				maildropEntry.setGenChangeDate(new Date());
				maildropEntry.setMailingID(mailing.getId());
				maildropEntry.setCompanyID(mailing.getCompanyID());
				maildropEntry.setStepping(stepping);
				maildropEntry.setBlocksize(blockSize);
	
				mailing.getMaildropStatus().add(maildropEntry);
	
				mailingDao.saveMailing(mailing, isPreserveTrackableLinks);
			}
			
			if (customerID > 0) {
				try {
					List<UserStatus> userStatusList = null;
					if (userStatus != null) {
						userStatusList = new ArrayList<>();
	
						if (userStatus == UserStatus.Active || userStatus == UserStatus.WaitForConfirm) {
							// This block is for backward compatibility only!
							userStatusList.add(UserStatus.Active);
							userStatusList.add(UserStatus.WaitForConfirm);
						} else {
							userStatusList.add(userStatus);
						}
					}
					
					final MailgunOptions mailgunOptions = new MailgunOptions();
					if (userStatusList != null) {
						mailgunOptions.withAllowedUserStatus(userStatusList);
					}

					if (profileData != null && profileData.size() > 0) {
						Map<String, String> profileDataOverrides = new HashMap<>();
						for (Entry<String, Object> item : profileData.entrySet()) {
							profileDataOverrides.put(item.getKey(), item.getValue() == null ? "" : item.getValue().toString());
						}
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
					&& ((mailing.getMailingType() == MailingType.NORMAL) || (mailing.getMailingType() == MailingType.FOLLOW_UP))) {
				startGen = 0;
			}
			
			if (!worldSend || !maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID())) {
				maildropEntry.setGenStatus(startGen);
				maildropEntry.setGenDate(genDate);
				maildropEntry.setGenChangeDate(new Date());
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

    private int createRecipient(int companyID, int mailingID, List<String> keyColumns, JsonObject profileData, boolean alwaysCreateRecipient) throws Exception {
		LightweightMailing mailing = mailingService.getLightweightMailing(companyID, mailingID);

		if (mailing == null) {
			throw new RestfulClientException("Cannot find mailing: " + mailingID);
		}

		int mailinglistID = mailingDao.getMailinglistId(mailingID, companyID);

		String title = (String) profileData.get("title");
		String email = (String) profileData.get("email");

		// Normalize email, if configured so
		if (!configService.getBooleanValue(ConfigValue.AllowUnnormalizedEmails, companyID)) {
			email = AgnUtils.normalizeEmail(email);
		}
		
		String firstName = (String) profileData.get("firstname");
		String lastName = (String) profileData.get("lastname");

		int gender = Gender.getGenderByDefaultGenderMapping(title).getStorageValue();

		Map<String, Object> dataMap = new HashMap<>();
		if (keyColumns != null && keyColumns.size() > 0) {
			for (String keyColumn : keyColumns) {
				dataMap.put(keyColumn, profileData.get(keyColumn));
			}
		} else {
			dataMap.put("email", email);
		}

		// Check if this customer is already blocked
		List<Recipient> existingRecipientList;
		try {
			existingRecipientList = recipientService.findRecipientByData(companyID, dataMap);
		} catch (Exception e) {
			throw new RestfulClientException("Recipient search failed: " +  e.getMessage(), e);
		}
		for (Recipient recipient : existingRecipientList) {
			if (!alwaysCreateRecipient) {
				return recipient.getCustomerID();
			}
			BindingEntry bindingEntry;
			try {
				bindingEntry = recipientService.getMailinglistBinding(companyID, recipient.getCustomerID(), mailinglistID, MediaTypes.EMAIL.getMediaCode());
			} catch (Exception e) {
				throw new RestfulClientException("Cannot read recipient subscription status: " + e.getMessage(), e);
			}
			if (bindingEntry != null && bindingEntry.getUserStatus() != UserStatus.Active.getStatusCode()) {
				throw new RestfulClientException("Recipient is already unsubscribed");
			}
		}

		// Create new customer
		int datasourceID = 0;
		DatasourceDescription datasourceDescription = datasourceDescriptionDao.getByDescription(SourceGroupType.RestfulService, companyID, "RestfulService");
		if (datasourceDescription == null) {
			// Use fallback datasource for companyid 0
			datasourceDescription = datasourceDescriptionDao.getByDescription(SourceGroupType.RestfulService, 0, "RestfulService");
		}
		if (datasourceDescription != null) {
			datasourceID = datasourceDescription.getId();
		}

		final Recipient recipient = new RecipientImpl();
		recipient.setCompanyID(companyID);
		recipient.getCustParameters().put("title", title);
		recipient.getCustParameters().put("email", email.toLowerCase());
		recipient.getCustParameters().put("firstname", firstName);
		recipient.getCustParameters().put("lastname", lastName);
		recipient.getCustParameters().put("gender", Integer.toString(gender));
		recipient.getCustParameters().put("mailtype", "1");
		recipient.getCustParameters().put("datasource_id", Integer.toString(datasourceID));
		recipient.getCustParameters().put("latest_datasource_id", Integer.toString(datasourceID));
		recipient.getCustParameters().put("mailing_id", mailingID);

		// set all other optional data
		for (Entry<String, Object> entry : profileData.entrySet()) {
			if (!"title".equals(entry.getKey()) && !"email".equals(entry.getKey()) && !"firstname".equals(entry.getKey()) && !"lastname".equals(entry.getKey())) {
				recipient.getCustParameters().put(entry.getKey(), entry.getValue());
			}
		}

		int customerID = recipientService.saveNewCustomer(recipient);
		if (customerID <= 0) {
			throw new RestfulClientException("Cannot create customer");
		} else {
			// Logon new customer to mailinglist
			final BindingEntry bindingEntry = new BindingEntryImpl();
			bindingEntry.setCustomerID(customerID);
			bindingEntry.setMailinglistID(mailinglistID);
			bindingEntry.setMediaType(MediaTypes.EMAIL.getMediaCode());
			bindingEntry.setUserRemark("Added by restful service CollectedDelivery");
			bindingEntry.setUserStatus(UserStatus.Active.getStatusCode());
			bindingEntry.setUserType(UserType.World.getTypeCode());
			bindingEntryDao.insertNewBinding(bindingEntry, companyID);

			return customerID;
		}
	}

	@Override
	public ResponseType getResponseType() {
		return ResponseType.JSON;
	}
}
