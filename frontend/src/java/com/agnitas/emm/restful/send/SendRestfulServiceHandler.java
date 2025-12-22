/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

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
import java.util.Set;
import java.util.TimeZone;

import com.agnitas.beans.Admin;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.dao.BindingEntryDao;
import com.agnitas.dao.DatasourceDescriptionDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.components.service.MailingSendService;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailgunOptions;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.MailingStopService;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import com.agnitas.emm.restful.BaseRequestResponse;
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
import com.agnitas.beans.BindingEntry;
import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.beans.DatasourceDescription;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.Recipient;
import com.agnitas.beans.impl.BindingEntryImpl;
import com.agnitas.beans.impl.RecipientImpl;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.core.mailinglist.dao.MailinglistDao;
import com.agnitas.emm.core.datasource.enums.SourceGroupType;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.commons.util.DateUtil;
import com.agnitas.emm.core.mailing.bean.LightweightMailing;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.HttpUtils.RequestMethod;
import com.agnitas.util.MailoutClient;
import com.agnitas.util.importvalues.Gender;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This restful service is available at:
 * https://<system.url>/restful/send
 */
public class SendRestfulServiceHandler implements RestfulServiceHandler {
	private static final Logger logger = LogManager.getLogger(SendRestfulServiceHandler.class);
	
	public static final String NAMESPACE = "send";

	protected RestfulUserActivityLogDao userActivityLogDao;
	protected MailingService mailingService;
	protected MailingDao mailingDao;
	protected RecipientService recipientService;
	protected RecipientFieldService recipientFieldService;
	protected MailinglistDao mailinglistDao;
	protected MaildropService maildropService;
	protected ClassicTemplateGenerator classicTemplateGenerator;
	protected SendActionbasedMailingService sendActionbasedMailingService;
	protected ConfigService configService;
	protected MailingPreviewService mailingPreviewService;
	protected DatasourceDescriptionDao datasourceDescriptionDao;
	protected BindingEntryDao bindingEntryDao;
	protected MailingSendService mailingSendService;
	protected MailingStopService mailingStopService;

	public SendRestfulServiceHandler(final ConfigService configService, final MailingPreviewService mailingPreviewService, final RestfulUserActivityLogDao userActivityLogDao, final MailingService mailingService,
			final MailingDao mailingDao, final RecipientService recipientService, final MailinglistDao mailinglistDao, final MaildropService maildropService, final ClassicTemplateGenerator classicTemplateGenerator,
			final SendActionbasedMailingService sendActionbasedMailingService, final DatasourceDescriptionDao datasourceDescriptionDao, final BindingEntryDao bindingEntryDao,
			final RecipientFieldService recipientFieldService, final MailingSendService mailingSendService, final MailingStopService mailingStopService) {
		this.configService = Objects.requireNonNull(configService, "configService is null");
		this.mailingPreviewService = Objects.requireNonNull(mailingPreviewService, "mailingPreviewService is null");
		this.userActivityLogDao = Objects.requireNonNull(userActivityLogDao, "userActivityLogDao is null");
		this.mailingService = Objects.requireNonNull(mailingService, "mailingService is null");
		this.mailingDao = Objects.requireNonNull(mailingDao, "mailingDao is null");
		this.recipientService = Objects.requireNonNull(recipientService, "recipientService is null");
		this.mailinglistDao = Objects.requireNonNull(mailinglistDao, "mailinglistDao is null");
		this.maildropService = Objects.requireNonNull(maildropService, "maildropService is null");
		this.classicTemplateGenerator = Objects.requireNonNull(classicTemplateGenerator, "classicTemplateGenerator is null");
		this.sendActionbasedMailingService = Objects.requireNonNull(sendActionbasedMailingService, "sendActionbasedMailingService is null");
		this.datasourceDescriptionDao = Objects.requireNonNull(datasourceDescriptionDao, "datasourceDescriptionDao is null");
		this.bindingEntryDao = Objects.requireNonNull(bindingEntryDao, "bindingEntryDao is null");
		this.recipientFieldService = Objects.requireNonNull(recipientFieldService, "recipientFieldService cannot be null");
		this.mailingSendService = Objects.requireNonNull(mailingSendService, "mailingSendService is null");
		this.mailingStopService = Objects.requireNonNull(mailingStopService, "mailingStopService");
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, Admin admin, byte[] requestData, File requestDataFile, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod, boolean extendedLogging) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			throw new RestfulClientException("Invalid http request method 'GET'. Only 'PUT' or 'POST' are supported for 'send'.");
		} else if (requestMethod == RequestMethod.DELETE) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(deactivateMailing(request, requestData, requestDataFile, admin, extendedLogging)));
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(sendMailing(request, requestData, requestDataFile, admin, extendedLogging)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(sendMailing(request, requestData, requestDataFile, admin, extendedLogging)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	protected Object deactivateMailing(HttpServletRequest request, byte[] requestData, File requestDataFile, Admin admin, @SuppressWarnings("unused") boolean extendedLogging) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_SEND_WORLD)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_SEND_WORLD.toString() + "'");
		}

		final String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		final int companyID = admin.getCompanyID();
		final int mailingID = Integer.parseInt(restfulContext[0]);

		try {
			final Mailing mailing = this.mailingService.getMailing(companyID, mailingID);

			switch (mailing.getMailingType()) {
				case NORMAL:
					mailingStopService.stopMailing(mailing.getCompanyID(), mailing.getId(), false);
					return "Mailing stopped";

				default:
					mailingSendService.deactivateMailing(mailing, mailing.getCompanyID(), false);
					return "Mailing deactivated";
			}
		} catch(final Exception e) {
			throw new RestfulClientException(String.format("Unknown mailing id %d", mailingID));
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
	protected Object sendMailing(HttpServletRequest request, byte[] requestData, File requestDataFile, Admin admin, @SuppressWarnings("unused") boolean extendedLogging) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_SEND_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_SEND_SHOW.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		
		if (!AgnUtils.isNumber(restfulContext[0])) {
			throw new RestfulClientException("Invalid MailingID: " + restfulContext[0]);
		}
		int companyID = admin.getCompanyID();
		int mailingID = Integer.parseInt(restfulContext[0]);
		if (!mailingDao.exist(mailingID, companyID)) {
			throw new RestfulClientException("Invalid not existing MailingID: " + mailingID);
		}
		
		// Send Admin, Test or World Mailing
		userActivityLogDao.addAdminUseOfFeature(admin, "restful/send", new Date());
		writeActivityLog(String.valueOf(mailingID), request, admin);

		MaildropStatus maildropStatus = null;
		Date sendDate = null;
		int stepping = 0;
		int blockSize = 0;
		int customerID = 0;
		String customerEmail = null;
		String customerEmailFromData = null;
		boolean alwaysCreateRecipient = false;
		boolean cleanupAdminAndTestActions = false;
		List<String> keyColumns = new ArrayList<>(Arrays.asList(new String[] { "email" }));
		UserStatus userStatus = null;
		
		JsonObject profileData = null;
		
		if ((requestData != null && requestData.length > 0) || (requestDataFile != null && requestDataFile.length() > 0)) {
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
									} catch (@SuppressWarnings("unused") Exception e) {
										throw new RestfulClientException("Invalid value for 'send_type'. String 'W', 'A', 'T', 'E' or 'R' expected");
									}
								} else {
									throw new RestfulClientException("Invalid data type for 'send_type'. String 'W', 'A', 'T', 'E' or 'R' expected");
								}
							} else if ("send_date".equals(entry.getKey())) {
								if (entry.getValue() != null && entry.getValue() instanceof String) {
									try {
										sendDate = DateUtilities.parseIso8601DateTimeString((String) entry.getValue());
									} catch (@SuppressWarnings("unused") Exception e) {
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
									if (!configService.getBooleanValue(ConfigValue.AllowUnnormalizedEmails, companyID)) {
										customerEmail = AgnUtils.normalizeEmail((String) entry.getValue());
									} else {
										customerEmail = (String) entry.getValue();
									}
								} else {
									throw new RestfulClientException("Invalid data type for 'email'. Email address expected");
								}
							} else if ("user_status".equals(entry.getKey())) {
								if (entry.getValue() instanceof Integer userStatusCode) {
									userStatus = UserStatus.getByCode(userStatusCode);
								} else {
									throw new RestfulClientException("Invalid data type for 'user_status'. Integer expected");
								}
							} else if ("data".equals(entry.getKey())) {
								if (entry.getValue() != null && entry.getValue() instanceof JsonObject) {
									profileData = (JsonObject) entry.getValue();
									if (profileData.get("email") != null) {
										customerEmailFromData = (String) profileData.get("email");
									}
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
							} else if ("cleanupAdminAndTestActions".equals(entry.getKey())) {
								if (entry.getValue() != null && entry.getValue() instanceof Boolean) {
									cleanupAdminAndTestActions = (Boolean) entry.getValue();
								} else {
									throw new RestfulClientException("Invalid data type for 'cleanupAdminAndTestActions'. Boolean expected");
								}
							} else {
								throw new RestfulClientException("Invalid property '" + entry.getKey() + "' for 'send'");
							}
						}
					}
				}
			}
		}
		
		if (customerID != 0 && StringUtils.isNotBlank(customerEmail)) {
			throw new RestfulClientException("Colliding parameters customer_id and email detected. Only one of both is allowed");
		}

		if (customerID == 0) {
			if (StringUtils.isNotBlank(customerEmail)) {
				List<Integer> result = recipientService.getRecipientIds(companyID, "email", customerEmail);
				if (result.size() > 1) {
					throw new RestfulClientException("More than one recipient found for this email address");
				} else if (result.size() < 1) {
					throw new RestfulClientException("No recipient found for this email address");
				} else {
					customerID = result.get(0);
				}
			} else if (StringUtils.isNotBlank(customerEmailFromData)) {
				List<Integer> result = recipientService.getRecipientIds(companyID, "email", customerEmailFromData);
				if (result.size() == 1) {
					customerID = result.get(0);
				}
			}
		}
		
		if ((customerID == 0 || alwaysCreateRecipient) && profileData != null && profileData.size() > 0) {
			customerID = createRecipient(companyID, mailingID, keyColumns, profileData, alwaysCreateRecipient);
		} else if (customerID != 0) {
			if (!recipientService.recipientExists(companyID, customerID)) {
				throw new RestfulClientException("No matching recipient found");
			}
		}
		
		
		if (cleanupAdminAndTestActions) {
			mailingSendService.clearTestActionsData(mailingID, companyID);
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

		MailingType mailingType = mailingDao.getMailingType(mailingID);
		
		if (maildropStatus == null) {
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
		} else {
			if (mailingType == MailingType.NORMAL
				&& (maildropStatus != MaildropStatus.WORLD && maildropStatus != MaildropStatus.ADMIN && maildropStatus != MaildropStatus.TEST)) {
				throw new RestfulClientException("Invalid optional property value for 'send_type'. String 'W', 'A' or 'T' expected for normal mailings");
			} else if (mailingType == MailingType.ACTION_BASED
				&& (maildropStatus != MaildropStatus.ACTION_BASED)) {
				throw new RestfulClientException("Invalid optional property value for 'send_type'. String 'E' expected for actionbased mailings");
			} else if (mailingType == MailingType.DATE_BASED
				&& (maildropStatus != MaildropStatus.DATE_BASED)) {
				throw new RestfulClientException("Invalid optional property value for 'send_type'. String 'R' expected for datebased mailings");
			} else if (mailingType == MailingType.INTERVAL
				&& (maildropStatus != MaildropStatus.ON_DEMAND)) {
				throw new RestfulClientException("Invalid optional property value for 'send_type'. String 'D' expected for interval mailings");
			}
		}

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

		final int recipientCount = userStatus != null
				? mailinglistDao.countSubscribers(aList.getId(), aList.getCompanyID(), mailing.getTargetID(), worldSend, adminSend, testSend, Set.of(userStatus.getStatusCode()))
				: mailinglistDao.countSubscribers(aList.getId(), aList.getCompanyID(), mailing.getTargetID(), worldSend, adminSend, testSend, Set.of());

		if (recipientCount == 0) {
			throw new RestfulClientException("This mailing has no subscribers");
		}

		if (maildropStatus == MaildropStatus.ACTION_BASED || maildropStatus == MaildropStatus.ON_DEMAND) {
			if (sendDate != null) {
				throw new RestfulClientException("This is an actionbased mailing. Therefore parameter send_date is not allowed. An actionbased Mailing will allways be sent immediatelly");
			}
			
			if (!maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID())) {
				maildropEntry.setGenStatus(startGen);
				maildropEntry.setGenDate(genDate);
				maildropEntry.setGenChangeDate(new Date());
				maildropEntry.setMailingID(mailing.getId());
				maildropEntry.setCompanyID(mailing.getCompanyID());
				maildropEntry.setStepping(stepping);
				maildropEntry.setBlocksize(blockSize);
	
				mailing.getMaildropStatus().add(maildropEntry);
				
				mailingDao.updateStatus(mailing.getCompanyID(), mailing.getId(), MailingStatus.ACTIVE, new Date());
	
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
			} else {
				sendDate = new Date();
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
				
				mailingDao.updateStatus(mailing.getCompanyID(), mailing.getId(), MailingStatus.SCHEDULED, new Date());
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
		List<Recipient> existingRecipientList = recipientService.findRecipientByData(companyID, dataMap);
		for (Recipient recipient : existingRecipientList) {
			if (!alwaysCreateRecipient) {
				return recipient.getCustomerID();
			}
			BindingEntry bindingEntry = recipientService.getMailinglistBinding(
					companyID,
					recipient.getCustomerID(),
					mailinglistID,
					MediaTypes.EMAIL.getMediaCode()
			);

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

		if (StringUtils.isBlank(email)) {
			throw new RestfulClientException("Cannot create new recipient: 'email' value is missing");
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
			bindingEntryDao.insertBindings(companyID, bindingEntry);

			return customerID;
		}
	}

	@Override
	public ResponseType getResponseType() {
		return ResponseType.JSON;
	}

	private void writeActivityLog(String description, HttpServletRequest request, Admin admin) {
		writeActivityLog(userActivityLogDao, description, request, admin);
	}
}
