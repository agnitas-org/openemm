/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.recipient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.DatasourceDescription;
import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.ImportStatus;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.Recipient;
import org.agnitas.beans.impl.BindingEntryImpl;
import org.agnitas.beans.impl.DatasourceDescriptionImpl;
import org.agnitas.beans.impl.ImportProfileImpl;
import org.agnitas.beans.impl.ImportStatusImpl;
import org.agnitas.beans.impl.RecipientImpl;
import org.agnitas.beans.impl.ViciousFormDataException;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.dao.SourceGroupType;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.autoimport.service.RemoteFile;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.recipient.RecipientUtils;
import org.agnitas.emm.core.recipient.service.SubscriberLimitCheck;
import org.agnitas.emm.core.recipient.service.SubscriberLimitExceededException;
import org.agnitas.emm.core.useractivitylog.dao.UserActivityLogDao;
import org.agnitas.service.ColumnInfoService;
import org.agnitas.service.ProfileImportWorker;
import org.agnitas.service.ProfileImportWorkerFactory;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.agnitas.util.ImportUtils;
import org.agnitas.util.ImportUtils.ImportErrorType;
import org.agnitas.util.importvalues.Charset;
import org.agnitas.util.importvalues.CheckForDuplicates;
import org.agnitas.util.importvalues.DateFormat;
import org.agnitas.util.importvalues.Gender;
import org.agnitas.util.importvalues.ImportMode;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComRecipientMailing;
import com.agnitas.beans.ProfileField;
import com.agnitas.dao.ComBindingEntryDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.DatasourceDescriptionDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.mailing.service.FullviewService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.restful.BaseRequestResponse;
import com.agnitas.emm.restful.ErrorCode;
import com.agnitas.emm.restful.JsonRequestResponse;
import com.agnitas.emm.restful.ResponseType;
import com.agnitas.emm.restful.RestfulClientException;
import com.agnitas.emm.restful.RestfulNoDataFoundException;
import com.agnitas.emm.restful.RestfulServiceHandler;
import com.agnitas.emm.util.html.xssprevention.HtmlXSSPreventer;
import com.agnitas.emm.util.html.xssprevention.XSSHtmlException;
import com.agnitas.json.Json5Reader;
import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.json.JsonReader.JsonToken;
import com.agnitas.json.JsonWriter;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This restful service is available at:
 * https://<system.url>/restful/recipient
 */
public class RecipientRestfulServiceHandler implements RestfulServiceHandler {
	
	public static final String NAMESPACE = "recipient";
	
	private static final String IMPORT_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "RecipientImport";

	private UserActivityLogDao userActivityLogDao;
	private ComRecipientDao recipientDao;
	private ComBindingEntryDao bindingEntryDao;
	private ColumnInfoService columnInfoService;
	private MailinglistDao mailinglistDao;
	private ProfileImportWorkerFactory profileImportWorkerFactory;
	private DatasourceDescriptionDao datasourceDescriptionDao;
	private FullviewService fullviewService;
	private ConfigService configService;
	private SubscriberLimitCheck subscriberLimitCheck;

	@Required
	public void setUserActivityLogDao(UserActivityLogDao userActivityLogDao) {
		this.userActivityLogDao = userActivityLogDao;
	}
	
	@Required
	public void setRecipientDao(ComRecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}
	
	@Required
	public void setBindingEntryDao(ComBindingEntryDao bindingEntryDao) {
		this.bindingEntryDao = bindingEntryDao;
	}
	
	@Required
	public void setColumnInfoService(ColumnInfoService columnInfoService) {
		this.columnInfoService = columnInfoService;
	}
	
	@Required
	public void setMailinglistDao(MailinglistDao mailinglistDao) {
		this.mailinglistDao = mailinglistDao;
	}
	
	@Required
	public void setProfileImportWorkerFactory(ProfileImportWorkerFactory profileImportWorkerFactory) {
		this.profileImportWorkerFactory = profileImportWorkerFactory;
	}

	@Required
	public void setDatasourceDescriptionDao(final DatasourceDescriptionDao datasourceDescriptionDao) {
		this.datasourceDescriptionDao = datasourceDescriptionDao;
	}

	@Required
	public void setFullviewService(FullviewService fullviewService) {
		this.fullviewService = fullviewService;
	}

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setSubscriberLimitCheck(SubscriberLimitCheck subscriberLimitCheck) {
		this.subscriberLimitCheck = subscriberLimitCheck;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) throws Exception {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, ComAdmin admin, byte[] requestData, File requestDataFile, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod, boolean extendedLogging) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(getCustomerData(request, admin)));
		} else if (requestMethod == RequestMethod.DELETE) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(deleteCustomer(request, admin)));
		} else if ((requestData == null || requestData.length == 0) && (requestDataFile == null || requestDataFile.length() <= 0)) {
			restfulResponse.setError(new RestfulClientException("Missing request data"), ErrorCode.REQUEST_DATA_ERROR);
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createNewCustomer(request, requestData, requestDataFile, admin)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createOrUpdateCustomer(request, requestData, requestDataFile, admin)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	/**
	 * Return a single or multiple customer data sets
	 * 
	 * @param request
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object getCustomerData(HttpServletRequest request, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.RECIPIENT_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.RECIPIENT_SHOW.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 2);

		boolean showReceivedMailings = false;
		if (restfulContext.length == 2) {
			if ("mailings".equalsIgnoreCase(restfulContext[1])) {
				showReceivedMailings = true;
			} else {
				throw new RestfulClientException("Invalid requestcontext: " + restfulContext[1]);
			}
		}
		
		String requestedRecipientKeyValue = restfulContext[0];
		List<Integer> customerIDs;
		if (AgnUtils.isEmailValid(requestedRecipientKeyValue)) {
			// Normalize email, if configured so
			if (!configService.getBooleanValue(ConfigValue.AllowUnnormalizedEmails, admin.getCompanyID())) {
				requestedRecipientKeyValue = AgnUtils.normalizeEmail(requestedRecipientKeyValue);
			}
			customerIDs = recipientDao.getRecipientIDs(admin.getCompanyID(), "email",requestedRecipientKeyValue);
		} else if (AgnUtils.isNumber(requestedRecipientKeyValue)) {
			customerIDs = recipientDao.getRecipientIDs(admin.getCompanyID(), "customer_id", requestedRecipientKeyValue);
		} else {
			throw new RestfulClientException("Invalid requested recipient key: " + requestedRecipientKeyValue);
		}
		
		userActivityLogDao.addAdminUseOfFeature(admin, "restful/recipient", new Date());
		userActivityLogDao.writeUserActivityLog(admin, "restful/recipient GET", requestedRecipientKeyValue);
		
		CaseInsensitiveMap<String, ProfileField> profileFields = columnInfoService.getColumnInfoMap(admin.getCompanyID(), admin.getAdminID());
		
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DateUtilities.ISO_8601_DATETIME_FORMAT).withZone(TimeZone.getTimeZone(admin.getAdminTimezone()).toZoneId());
		
		if (customerIDs.size() > 1) {
			JsonArray result = new JsonArray();
			for (CaseInsensitiveMap<String, Object> customerDataMap : recipientDao.getCustomersData(customerIDs, admin.getCompanyID())) {
				JsonObject customerJsonObject = new JsonObject();
				for (String key : AgnUtils.sortCollectionWithItemsFirst(customerDataMap.keySet(), "customer_id", "email")) {
					if (profileFields.get(key) != null && profileFields.get(key).getSimpleDataType() == SimpleDataType.Date && customerDataMap.get(key) instanceof Date) {
						customerJsonObject.add(key.toLowerCase(), new SimpleDateFormat(DateUtilities.ISO_8601_DATE_FORMAT_NO_TIMEZONE).format(customerDataMap.get(key)));
					} else if (profileFields.get(key) != null && profileFields.get(key).getSimpleDataType() == SimpleDataType.DateTime && customerDataMap.get(key) instanceof Date) {
						ZonedDateTime systemZonedDateTime = ZonedDateTime.ofInstant(((Date) customerDataMap.get(key)).toInstant(), ZoneId.systemDefault());
						customerJsonObject.add(key.toLowerCase(), dateTimeFormatter.format(systemZonedDateTime));
					} else {
						customerJsonObject.add(key.toLowerCase(), customerDataMap.get(key));
					}
				}
				if (showReceivedMailings) {
					addReceivedMailingsToCustomer(admin.getCompanyID(), customerJsonObject);
				}
				result.add(customerJsonObject);
			}
			return result;
		} else if (customerIDs.size() == 1) {
			CaseInsensitiveMap<String, Object> customerDataMap = recipientDao.getCustomerData(admin.getCompanyID(), customerIDs.get(0));
			JsonObject customerJsonObject = new JsonObject();
			for (String key : AgnUtils.sortCollectionWithItemsFirst(customerDataMap.keySet(), "customer_id", "email")) {
				if (profileFields.get(key) != null && profileFields.get(key).getSimpleDataType() == SimpleDataType.Date && customerDataMap.get(key) instanceof Date) {
					customerJsonObject.add(key.toLowerCase(), new SimpleDateFormat(DateUtilities.ISO_8601_DATE_FORMAT_NO_TIMEZONE).format(customerDataMap.get(key)));
				} else if (profileFields.get(key) != null && profileFields.get(key).getSimpleDataType() == SimpleDataType.DateTime && customerDataMap.get(key) instanceof Date) {
					ZonedDateTime systemZonedDateTime = ZonedDateTime.ofInstant(((Date) customerDataMap.get(key)).toInstant(), ZoneId.systemDefault());
					customerJsonObject.add(key.toLowerCase(), dateTimeFormatter.format(systemZonedDateTime));
				} else {
					customerJsonObject.add(key.toLowerCase(), customerDataMap.get(key));
				}
			}
			if (showReceivedMailings) {
				addReceivedMailingsToCustomer(admin.getCompanyID(), customerJsonObject);
			}
			return customerJsonObject;
		} else {
			throw new RestfulNoDataFoundException("No data found");
		}
	}

	private void addReceivedMailingsToCustomer(int companyID, JsonObject customerJsonObject) {
		int customerID = ((Number) customerJsonObject.get("customer_id")).intValue();
		JsonArray mailingsJsonArray = new JsonArray();
		for (ComRecipientMailing mailing : recipientDao.getMailingsSentToRecipient(customerID, companyID)) {
			JsonObject mailingJsonObject = new JsonObject();

			mailingJsonObject.add("mailing_id", mailing.getMailingId());
			mailingJsonObject.add("senddate", new SimpleDateFormat(DateUtilities.ISO_8601_DATE_FORMAT_NO_TIMEZONE).format(mailing.getSendDate()));
			mailingJsonObject.add("mailing_type", mailing.getMailingType().name());
			mailingJsonObject.add("mailing_name", mailing.getShortName());
			mailingJsonObject.add("mailing_subject", mailing.getSubject());
			mailingJsonObject.add("openings", mailing.getNumberOfOpenings());
			mailingJsonObject.add("clicks", mailing.getNumberOfClicks());
			
			try {
				String fullviewLink = fullviewService.getFullviewUrl(companyID, mailing.getMailingId(), customerID, null);
				mailingJsonObject.add("fullview_url", fullviewLink);
			} catch (Exception e) {
				mailingJsonObject.add("fullview_url_error", "Not available");
			}

			mailingsJsonArray.add(mailingJsonObject);
		}
		customerJsonObject.add("mailingsReceived", mailingsJsonArray);
	}

	/**
	 * Delete a single or multiple customer data sets
	 * 
	 * @param request
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object deleteCustomer(HttpServletRequest request, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.RECIPIENT_DELETE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.RECIPIENT_DELETE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		
		String requestedRecipientKeyValue = restfulContext[0];
		List<Integer> customerIDs;
		if (AgnUtils.isEmailValid(requestedRecipientKeyValue)) {
			// Normalize email, if configured so
			if (!configService.getBooleanValue(ConfigValue.AllowUnnormalizedEmails, admin.getCompanyID())) {
				requestedRecipientKeyValue = AgnUtils.normalizeEmail(requestedRecipientKeyValue);
			}
			customerIDs = recipientDao.getRecipientIDs(admin.getCompanyID(), "email", requestedRecipientKeyValue);
		} else if (AgnUtils.isNumber(requestedRecipientKeyValue)) {
			customerIDs = recipientDao.getRecipientIDs(admin.getCompanyID(), "customer_id", requestedRecipientKeyValue);
		} else {
			throw new RestfulClientException("Invalid recipient key for deletion: " + requestedRecipientKeyValue);
		}
		
		userActivityLogDao.addAdminUseOfFeature(admin, "restful/recipient", new Date());
		userActivityLogDao.writeUserActivityLog(admin, "restful/recipient DELETE", requestedRecipientKeyValue);
		
		if (customerIDs.size() > 0) {
			recipientDao.deleteRecipients(admin.getCompanyID(), customerIDs);
			return customerIDs.size() + " customer datasets deleted";
		} else {
			throw new RestfulNoDataFoundException("No data found for deletion");
		}
	}

	/**
	 * Create a new customer entry (also bulk/multiple)
	 * 
	 * @param request
	 * @param requestDataFile
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object createNewCustomer(HttpServletRequest request, byte[] requestData, File requestDataFile, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.RECIPIENT_CREATE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.RECIPIENT_CREATE.toString() + "'");
		}
		
		RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 0);
		
		boolean onlyUpdateExistingBindings = false;
		List<Integer> mailinglistsToSubscribe = null;
		if (StringUtils.isNotBlank(request.getParameter("mailinglist"))) {
			if ("*".equals(request.getParameter("mailinglist"))) {
				mailinglistsToSubscribe = mailinglistDao.getMailinglistIds(admin.getCompanyID());
				onlyUpdateExistingBindings = true;
			} else {
				if (!AgnUtils.isNumber(request.getParameter("mailinglist"))) {
					throw new RestfulClientException("Invalid parameter mailinglist: '" + request.getParameter("mailinglist") + "'");
				}
				int mailinglistToSubscribe = Integer.parseInt(request.getParameter("mailinglist"));
				Mailinglist mailinglist = mailinglistDao.getMailinglist(mailinglistToSubscribe, admin.getCompanyID());
				if (mailinglist == null || mailinglist.isRemoved()) {
					throw new RestfulClientException("Invalid mailinglist: '" + mailinglistToSubscribe + "'");
				} else {
					mailinglistsToSubscribe = new ArrayList<>();
					mailinglistsToSubscribe.add(mailinglistToSubscribe);
				}
			}
		}
		
		UserStatus newUserStatus = UserStatus.Active;
		if (StringUtils.isNotBlank(request.getParameter("status"))) {
			try {
				if (AgnUtils.isNumber(request.getParameter("status"))) {
					newUserStatus = UserStatus.getUserStatusByID(Integer.parseInt(request.getParameter("status")));
				} else {
					newUserStatus = UserStatus.getUserStatusByName(request.getParameter("status"));
				}
			} catch (Exception e) {
				throw new RestfulClientException("Invalid parameter subscribtion status: '" + request.getParameter("status") + "'");
			}
		}
		
		MediaTypes newMediaType = MediaTypes.EMAIL;
		if (StringUtils.isNotBlank(request.getParameter("mediaType"))) {
			try {
				if (AgnUtils.isNumber(request.getParameter("mediaType"))) {
					newMediaType = MediaTypes.getMediaTypeForCode(Integer.parseInt(request.getParameter("mediaType")));
				} else {
					newMediaType = MediaTypes.getMediatypeByName(request.getParameter("mediaType"));
				}
			} catch (Exception e) {
				throw new RestfulClientException("Invalid parameter mediaType: '" + request.getParameter("mediaType") + "'");
			}
		}
		
		Map<String, String> additionalProfileValues = new HashMap<>();
		if (StringUtils.isNotBlank(request.getParameter("testuser"))) {
			additionalProfileValues.put("testuser", request.getParameter("testuser"));
		}
		
		try (InputStream inputStream = RestfulServiceHandler.getRequestDataStream(requestData, requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				// Read root JSON element
				JsonToken readJsonToken = jsonReader.readNextToken();
				if (JsonToken.JsonObject_Open == readJsonToken) {
					// Single customer data
					CaseInsensitiveMap<String, ProfileField> profileFields = columnInfoService.getColumnInfoMap(admin.getCompanyID(), admin.getAdminID());
					JsonObject jsonObject = new JsonObject();
					while ((readJsonToken = jsonReader.readNextToken()) == JsonToken.JsonObject_PropertyKey) {
						String propertyKey = (String) jsonReader.getCurrentObject();
						if (!profileFields.containsKey(propertyKey)) {
							throw new RestfulClientException("Expected recipient property key '" + StringUtils.join(profileFields.keySet(), "', '") + "' but was: '" + jsonReader.getCurrentObject() + "'");
						} else if (jsonObject.containsPropertyKey(propertyKey)) {
							throw new RestfulClientException("Duplicate recipient property key: " + jsonReader.getCurrentObject());
						}
						readJsonToken = jsonReader.readNextToken();
						if (JsonToken.JsonSimpleValue != readJsonToken) {
							throw new RestfulClientException("Expected JSON item type 'JsonSimpleValue' but was: " + readJsonToken);
						}
						Object propertyValue = jsonReader.getCurrentObject();
						if (propertyValue != null && propertyValue instanceof String && !admin.permissionAllowed(Permission.RECIPIENT_PROFILEFIELD_HTML_ALLOWED)) {
							// Check for unallowed html content
							try {
								HtmlXSSPreventer.checkString((String) propertyValue);
							} catch(final XSSHtmlException e) {
								throw new RestfulClientException("Invalid recipient data containing HTML for recipient field: " + propertyKey);
							}
						}
						jsonObject.add(propertyKey, jsonReader.getCurrentObject());
					}
					
					for (Entry<String, String> entry : additionalProfileValues.entrySet()) {
						jsonObject.add(entry.getKey(), entry.getValue());
					}

					if (JsonToken.JsonObject_Close != readJsonToken) {
						throw new RestfulClientException("Expected JSON item type 'JsonObject_Close' was: " + readJsonToken);
					}

					Recipient recipient = new RecipientImpl();
					((RecipientImpl) recipient).setRecipientDao(recipientDao);
					recipient.setCompanyID(admin.getCompanyID());
					
					List<String> hiddenColumns = ImportUtils.getHiddenColumns(admin);
					for (String key : jsonObject.keySet()) {
						if ("customer_id".equalsIgnoreCase(key)) {
							throw new RestfulClientException("Invalid recipient data for new recipient. Internal key field " + key + " is included");
						} else if (hiddenColumns.contains(key)) {
							throw new RestfulClientException("Invalid recipient data for new recipient. Internal key field " + key + " is included");
						}
					}
					
					Map<String, Object> custParameters = recipient.getCustParameters();
					removeTripleDateEntries(custParameters);
					
					if (configService.getBooleanValue(ConfigValue.AnonymizeAllRecipients, admin.getCompanyID())) {
						custParameters.put(RecipientUtils.COLUMN_DO_NOT_TRACK, 1);
					}
					
					for (Entry<String, Object> entry : jsonObject.entrySet()) {
						custParameters.put(entry.getKey(), entry.getValue());
					}

					// Set default data if missing
					if (!custParameters.containsKey("gender")) {
						custParameters.put("gender", Gender.UNKNOWN.getStorageValue());
					}
					if (!custParameters.containsKey("mailtype")) {
						custParameters.put("mailtype", MailType.HTML.getIntValue());
					}
					
					// Normalize email, if configured so
					if (custParameters.containsKey("email") && !configService.getBooleanValue(ConfigValue.AllowUnnormalizedEmails, admin.getCompanyID())) {
						custParameters.put("email", AgnUtils.normalizeEmail((String) custParameters.get("email")));
					}
					
					DatasourceDescription datasourceDescription = datasourceDescriptionDao.getByDescription(SourceGroupType.RestfulService, admin.getCompanyID(), "RestfulService");
					if (datasourceDescription == null) {
						// Use fallback datasource for companyid 0
						datasourceDescription = datasourceDescriptionDao.getByDescription(SourceGroupType.RestfulService, 0, "RestfulService");
					}
					if (datasourceDescription != null) {
						recipient.getCustParameters().put("datasource_id", datasourceDescription.getId());
						recipient.getCustParameters().put("latest_datasource_id", datasourceDescription.getId());
					}
					
					recipient.setChangeFlag(true);
					
					try {
						subscriberLimitCheck.checkSubscriberLimit(admin.getCompanyID(), 1);
					} catch (SubscriberLimitExceededException e) {
						throw new RestfulClientException("Number of customer entries allowed is going to be exceeded. Number of existing customers would be " + e.getActual() + ". Maximum customer number limit is " + e.getMaximum() + ".");
					}
					
					try {
						if (!recipientDao.updateInDbWithException(recipient, false)) {
							throw new RestfulClientException("Invalid recipient data for recipient");
						}
					} catch (ViciousFormDataException e) {
						throw new RestfulClientException("Invalid recipient data for recipient: " + e.getMessage(), e);
					}
					
					if (mailinglistsToSubscribe != null) {
						for (int mailinglistToSubscribe : mailinglistsToSubscribe) {
							BindingEntry bindingEntry = new BindingEntryImpl();
							bindingEntry.setCustomerID(recipient.getCustomerID());
							bindingEntry.setMailinglistID(mailinglistToSubscribe);
							bindingEntry.setUserStatus(newUserStatus.getStatusCode());
							bindingEntry.setUserType(BindingEntry.UserType.World.getTypeCode());
							bindingEntry.setMediaType(newMediaType.getMediaCode());
							bindingEntry.setUserRemark("Set by " + admin.getUsername() + " via restful");
							
							boolean success = bindingEntryDao.updateBinding(bindingEntry, admin.getCompanyID());
							if (!success && !onlyUpdateExistingBindings) {
								bindingEntryDao.insertNewBinding(bindingEntry, admin.getCompanyID());
							}
						}
					}
					
					userActivityLogDao.addAdminUseOfFeature(admin, "restful/recipient", new Date());
					userActivityLogDao.writeUserActivityLog(admin, "restful/recipient POST", "" + recipient.getCustomerID());
					
					DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DateUtilities.ISO_8601_DATETIME_FORMAT).withZone(TimeZone.getTimeZone(admin.getAdminTimezone()).toZoneId());
					
					CaseInsensitiveMap<String, Object> customerDataMap = recipientDao.getCustomerData(admin.getCompanyID(), recipient.getCustomerID());
					JsonObject customerJsonObject = new JsonObject();
					for (String key : AgnUtils.sortCollectionWithItemsFirst(customerDataMap.keySet(), "customer_id", "email")) {
						if (profileFields.get(key) != null && profileFields.get(key).getSimpleDataType() == SimpleDataType.Date && customerDataMap.get(key) instanceof Date) {
							customerJsonObject.add(key.toLowerCase(), new SimpleDateFormat(DateUtilities.ISO_8601_DATE_FORMAT_NO_TIMEZONE).format(customerDataMap.get(key)));
						} else if (profileFields.get(key) != null && profileFields.get(key).getSimpleDataType() == SimpleDataType.DateTime && customerDataMap.get(key) instanceof Date) {
							ZonedDateTime systemZonedDateTime = ZonedDateTime.ofInstant(((Date) customerDataMap.get(key)).toInstant(), ZoneId.systemDefault());
							customerJsonObject.add(key.toLowerCase(), dateTimeFormatter.format(systemZonedDateTime));
						} else {
							customerJsonObject.add(key.toLowerCase(), customerDataMap.get(key));
						}
					}
					return customerJsonObject;
				} else if (JsonToken.JsonArray_Open == readJsonToken) {
					if (StringUtils.isNotBlank(request.getParameter("status"))) {
						throw new RestfulClientException("Parameter status is not allowed for bulk creation of recipient. Use the import mode to define which subscription status to set");
					}
					
					// Multiple customer data
					if (!admin.permissionAllowed(Permission.IMPORT_MODE_ADD)) {
						throw new RestfulClientException("Authorization failed: Access denied '" + Permission.IMPORT_MODE_ADD.toString() + "'");
					}
					
					CaseInsensitiveMap<String, ProfileField> profileFields = columnInfoService.getColumnInfoMap(admin.getCompanyID(), admin.getAdminID());
			
					File importTempFileDirectory = new File(IMPORT_FILE_DIRECTORY + "/" + admin.getCompanyID());
					if (!importTempFileDirectory.exists()) {
						importTempFileDirectory.mkdirs();
					}
					
					String requestUUID = AgnUtils.generateNewUUID().toString().replace("-", "").toUpperCase();
					
					File temporaryImportFile = new File(importTempFileDirectory.getAbsolutePath() + "/Recipientimportjson_" + requestUUID + ".json");
					ImportMode importMode = ImportMode.ADD;
					String keyColumn = null;
					
					int foundItems = 0;
					// Read recipient data entries and write them to a importable json file
					try (FileOutputStream importFileOutputStream = new FileOutputStream(temporaryImportFile);
						JsonWriter jsonWriter = new JsonWriter(importFileOutputStream)) {
						jsonWriter.openJsonArray();
						while ((readJsonToken = jsonReader.readNextToken()) == JsonToken.JsonObject_Open) {
							JsonObject jsonObject = new JsonObject();
	
							while ((readJsonToken = jsonReader.readNextToken()) == JsonToken.JsonObject_PropertyKey) {
								String propertyKey = (String) jsonReader.getCurrentObject();
								if (!profileFields.containsKey(propertyKey)) {
									throw new RestfulClientException("Expected recipient property key '" + StringUtils.join(profileFields.keySet(), "', '") + "' but was: '" + jsonReader.getCurrentObject() + "'");
								} else if (jsonObject.containsPropertyKey(propertyKey)) {
									throw new RestfulClientException("Duplicate recipient property key: " + jsonReader.getCurrentObject());
								}
								readJsonToken = jsonReader.readNextToken();
								if (JsonToken.JsonSimpleValue != readJsonToken) {
									throw new RestfulClientException("Expected JSON item type 'JsonSimpleValue' but was: " + readJsonToken);
								}
								Object propertyValue = jsonReader.getCurrentObject();
								if (propertyValue != null && propertyValue instanceof String && !admin.permissionAllowed(Permission.RECIPIENT_PROFILEFIELD_HTML_ALLOWED)) {
									// Check for unallowed html content
									try {
										HtmlXSSPreventer.checkString((String) propertyValue);
									} catch(final XSSHtmlException e) {
										throw new RestfulClientException("Invalid recipient data containing HTML for recipient field: " + propertyKey);
									}
								}
								jsonObject.add(propertyKey, jsonReader.getCurrentObject());
							}

							for (Entry<String, String> entry : additionalProfileValues.entrySet()) {
								jsonObject.add(entry.getKey(), entry.getValue());
							}
	
							if (JsonToken.JsonObject_Close != readJsonToken) {
								throw new RestfulClientException("Expected JSON item type 'JsonObject_Close' was: " + readJsonToken);
							}
	
							jsonWriter.add(jsonObject);
							foundItems++;
						}
						jsonWriter.closeJsonArray();
					}
		
					if (JsonToken.JsonArray_Close != readJsonToken) {
						throw new RestfulClientException("Expected JSON item type 'JsonArray_Close' was: " + readJsonToken);
					}
			
					Set<MediaTypes> newMediaTypes = new HashSet<>();
					newMediaTypes.add(newMediaType);
					ImportStatus status = importRecipients(admin, importMode, keyColumn, mailinglistsToSubscribe, temporaryImportFile, requestUUID, newMediaTypes);
					
					userActivityLogDao.addAdminUseOfFeature(admin, "restful/recipient", new Date());
					userActivityLogDao.writeUserActivityLog(admin, "restful/recipient POST", "IMPORT");
					
					JsonObject result = new JsonObject();
					if (status.getFatalError() != null) {
						throw new RestfulClientException(status.getFatalError());
					} else {
						result.add("Recipients", foundItems);
						result.add("Inserted", status.getInserted());
						
						boolean foundErrors = false;
						JsonObject errorsObject = new JsonObject();
						for (Entry<ImportErrorType, Integer> errorEntry : status.getErrors().entrySet()) {
							errorsObject.add(errorEntry.getKey().name(), errorEntry.getValue());
							if (errorEntry.getValue() > 0) {
								foundErrors = true;
							}
						}
						if (foundErrors) {
							result.add("Errors", errorsObject);
						}
					}
					return result;
				} else {
					throw new RestfulClientException("Expected root JSON item type 'JsonObject' or 'JsonArray' but was: " + readJsonToken);
				}
			}
		}
	}

	/**
	 * Create a new customer entry or update an existing customer entry (also bulk/multiple)
	 * 
	 * @param request
	 * @param requestDataFile
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object createOrUpdateCustomer(HttpServletRequest request, byte[] requestData, File requestDataFile, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.RECIPIENT_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.RECIPIENT_CHANGE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 1);

		boolean onlyUpdateExistingBindings = false;
		List<Integer> mailinglistsToSubscribe = null;
		if (StringUtils.isNotBlank(request.getParameter("mailinglist"))) {
			if ("*".equals(request.getParameter("mailinglist"))) {
				mailinglistsToSubscribe = mailinglistDao.getMailinglistIds(admin.getCompanyID());
				onlyUpdateExistingBindings = true;
			} else {
				if (!AgnUtils.isNumber(request.getParameter("mailinglist"))) {
					throw new RestfulClientException("Invalid parameter mailinglist: '" + request.getParameter("mailinglist") + "'");
				}
				int mailinglistToSubscribe = Integer.parseInt(request.getParameter("mailinglist"));
				Mailinglist mailinglist = mailinglistDao.getMailinglist(mailinglistToSubscribe, admin.getCompanyID());
				if (mailinglist == null || mailinglist.isRemoved()) {
					throw new RestfulClientException("Invalid mailinglist: '" + mailinglistToSubscribe + "'");
				} else {
					mailinglistsToSubscribe = new ArrayList<>();
					mailinglistsToSubscribe.add(mailinglistToSubscribe);
				}
			}
		}
		
		UserStatus newUserStatus = UserStatus.Active;
		if (StringUtils.isNotBlank(request.getParameter("status"))) {
			try {
				if (AgnUtils.isNumber(request.getParameter("status"))) {
					newUserStatus = UserStatus.getUserStatusByID(Integer.parseInt(request.getParameter("status")));
				} else {
					newUserStatus = UserStatus.getUserStatusByName(request.getParameter("status"));
				}
			} catch (Exception e) {
				throw new RestfulClientException("Invalid parameter subscribtion status: '" + request.getParameter("status") + "'");
			}
		}
		
		MediaTypes newMediaType = MediaTypes.EMAIL;
		if (StringUtils.isNotBlank(request.getParameter("mediaType"))) {
			try {
				if (AgnUtils.isNumber(request.getParameter("mediaType"))) {
					newMediaType = MediaTypes.getMediaTypeForCode(Integer.parseInt(request.getParameter("mediaType")));
				} else {
					newMediaType = MediaTypes.getMediatypeByName(request.getParameter("mediaType"));
				}
			} catch (Exception e) {
				throw new RestfulClientException("Invalid parameter mediaType: '" + request.getParameter("mediaType") + "'");
			}
		}
		
		Map<String, String> additionalProfileValues = new HashMap<>();
		if (StringUtils.isNotBlank(request.getParameter("testuser"))) {
			additionalProfileValues.put("testuser", request.getParameter("testuser"));
		}
		
		try (InputStream inputStream = RestfulServiceHandler.getRequestDataStream(requestData, requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				// Read root JSON element
				JsonToken readJsonToken = jsonReader.readNextToken();
				if (JsonToken.JsonObject_Open == readJsonToken) {
					// Single customer data
					CaseInsensitiveMap<String, ProfileField> profileFields = columnInfoService.getColumnInfoMap(admin.getCompanyID(), admin.getAdminID());
					JsonObject jsonObject = new JsonObject();
					while ((readJsonToken = jsonReader.readNextToken()) == JsonToken.JsonObject_PropertyKey) {
						String propertyKey = (String) jsonReader.getCurrentObject();
						if (!profileFields.containsKey(propertyKey)) {
							throw new RestfulClientException("Expected recipient property key '" + StringUtils.join(profileFields.keySet(), "', '") + "' but was: '" + jsonReader.getCurrentObject() + "'");
						} else if (jsonObject.containsPropertyKey(propertyKey)) {
							throw new RestfulClientException("Duplicate recipient property key: " + jsonReader.getCurrentObject());
						}
						readJsonToken = jsonReader.readNextToken();
						if (JsonToken.JsonSimpleValue != readJsonToken) {
							throw new RestfulClientException("Expected JSON item type 'JsonSimpleValue' but was: " + readJsonToken);
						}
						Object propertyValue = jsonReader.getCurrentObject();
						if (propertyValue != null && propertyValue instanceof String && !admin.permissionAllowed(Permission.RECIPIENT_PROFILEFIELD_HTML_ALLOWED)) {
							// Check for unallowed html content
							try {
								HtmlXSSPreventer.checkString((String) propertyValue);
							} catch(final XSSHtmlException e) {
								throw new RestfulClientException("Invalid recipient data containing HTML for recipient field: " + propertyKey);
							}
						}
						jsonObject.add(propertyKey, jsonReader.getCurrentObject());
					}
					
					for (Entry<String, String> entry : additionalProfileValues.entrySet()) {
						jsonObject.add(entry.getKey(), entry.getValue());
					}

					if (JsonToken.JsonObject_Close != readJsonToken) {
						throw new RestfulClientException("Expected JSON item type 'JsonObject_Close' was: " + readJsonToken);
					}

					Recipient recipient = new RecipientImpl();
					((RecipientImpl) recipient).setRecipientDao(recipientDao);
					recipient.setCompanyID(admin.getCompanyID());
					
					if (restfulContext.length == 1) {
						String requestedRecipientKeyValue = restfulContext[0];
						if (AgnUtils.isEmailValid(requestedRecipientKeyValue)) {
							// Normalize email, if configured so
							if (!configService.getBooleanValue(ConfigValue.AllowUnnormalizedEmails, admin.getCompanyID())) {
								requestedRecipientKeyValue = AgnUtils.normalizeEmail(requestedRecipientKeyValue);
							}
							int customerID = recipientDao.findByKeyColumn(recipient, "email", requestedRecipientKeyValue);
							if (customerID <= 0) {
								throw new RestfulClientException("No recipient found for update with email: " + requestedRecipientKeyValue);
							}
							
							List<String> hiddenColumns = ImportUtils.getHiddenColumns(admin);
							for (String key : jsonObject.keySet()) {
								if ("customer_id".equalsIgnoreCase(key)) {
									throw new RestfulClientException("Invalid recipient data for new recipient. Internal key field " + key + " is included");
								} else if (hiddenColumns.contains(key.toLowerCase())) {
									throw new RestfulClientException("Invalid recipient data for new recipient. Internal key field " + key + " is included");
								}
							}
							
							recipient.setCustParameters(recipientDao.getCustomerDataFromDb(admin.getCompanyID(), customerID, recipient.getDateFormat()));
							Map<String, Object> custParameters = recipient.getCustParameters();
							removeTripleDateEntries(custParameters);
							for (Entry<String, Object> entry : jsonObject.entrySet()) {
								custParameters.put(entry.getKey(), entry.getValue());
							}
							
							DatasourceDescription datasourceDescription = datasourceDescriptionDao.getByDescription(SourceGroupType.RestfulService, admin.getCompanyID(), "RestfulService");
							if (datasourceDescription == null) {
								// Use fallback datasource for companyid 0
								datasourceDescription = datasourceDescriptionDao.getByDescription(SourceGroupType.RestfulService, 0, "RestfulService");
							}
							if (datasourceDescription != null) {
								recipient.getCustParameters().put("latest_datasource_id", datasourceDescription.getId());
							}
							
							recipient.setChangeFlag(true);
							
							try {
								if (!recipientDao.updateInDbWithException(recipient, false)) {
									throw new RestfulClientException("Invalid recipient data for recipient: " + requestedRecipientKeyValue);
								}
							} catch (ViciousFormDataException e) {
								throw new RestfulClientException("Invalid recipient data for recipient: " + e.getMessage(), e);
							}
						} else if (AgnUtils.isNumber(requestedRecipientKeyValue)) {
							int customerID = recipientDao.findByKeyColumn(recipient, "customer_id", requestedRecipientKeyValue);
							if (customerID <= 0) {
								throw new RestfulClientException("No recipient found for update with id: " + requestedRecipientKeyValue);
							}

							List<String> hiddenColumns = ImportUtils.getHiddenColumns(admin);
							for (String key : jsonObject.keySet()) {
								if ("customer_id".equalsIgnoreCase(key)) {
									throw new RestfulClientException("Invalid recipient data for new recipient. Internal key field " + key + " is included");
								} else if (hiddenColumns.contains(key.toLowerCase())) {
									throw new RestfulClientException("Invalid recipient data for new recipient. Internal key field " + key + " is included");
								}
							}
							
							recipient.setCustParameters(recipientDao.getCustomerDataFromDb(admin.getCompanyID(), customerID, recipient.getDateFormat()));
							Map<String, Object> custParameters = recipient.getCustParameters();
							removeTripleDateEntries(custParameters);
							for (Entry<String, Object> entry : jsonObject.entrySet()) {
								custParameters.put(entry.getKey(), entry.getValue());
							}
							
							DatasourceDescription datasourceDescription = datasourceDescriptionDao.getByDescription(SourceGroupType.RestfulService, admin.getCompanyID(), "RestfulService");
							if (datasourceDescription == null) {
								// Use fallback datasource for companyid 0
								datasourceDescription = datasourceDescriptionDao.getByDescription(SourceGroupType.RestfulService, 0, "RestfulService");
							}
							if (datasourceDescription != null) {
								recipient.getCustParameters().put("latest_datasource_id", datasourceDescription.getId());
							}
							
							recipient.setChangeFlag(true);
							
							try {
								if (!recipientDao.updateInDbWithException(recipient, false)) {
									throw new RestfulClientException("Invalid recipient data for recipient: " + requestedRecipientKeyValue);
								}
							} catch (ViciousFormDataException e) {
								throw new RestfulClientException("Invalid recipient data for recipient: " + e.getMessage(), e);
							}
						} else {
							throw new RestfulClientException("Invalid requested recipient key: " + requestedRecipientKeyValue);
						}
					} else {
						int customerID = 0;
						if (jsonObject.containsPropertyKey("customer_id")) {
							customerID = recipientDao.findByKeyColumn(recipient, "customer_id", jsonObject.get("customer_id").toString());
							// Remove customer_id from data for update
							jsonObject.remove("customer_id");
						} else if (jsonObject.containsPropertyKey("email")) {
							String recipientsEmail = (String) jsonObject.get("email");
							// Normalize email, if configured so
							if (!configService.getBooleanValue(ConfigValue.AllowUnnormalizedEmails, admin.getCompanyID())) {
								recipientsEmail = AgnUtils.normalizeEmail(recipientsEmail);
							}
							customerID = recipientDao.findByKeyColumn(recipient, "email", recipientsEmail);
						}
						
						if (customerID > 0) {
							recipient.setCustParameters(recipientDao.getCustomerDataFromDb(admin.getCompanyID(), customerID, recipient.getDateFormat()));
						} else {
							if (configService.getBooleanValue(ConfigValue.AnonymizeAllRecipients, admin.getCompanyID())) {
								recipient.getCustParameters().put(RecipientUtils.COLUMN_DO_NOT_TRACK, 1);
							}
						}
						
						List<String> hiddenColumns = ImportUtils.getHiddenColumns(admin);
						for (String key : jsonObject.keySet()) {
							if (hiddenColumns.contains(key.toLowerCase())) {
								throw new RestfulClientException("Invalid recipient data for new recipient. Internal key field " + key + " is included");
							}
						}
						
						Map<String, Object> custParameters = recipient.getCustParameters();
						removeTripleDateEntries(custParameters);
						for (Entry<String, Object> entry : jsonObject.entrySet()) {
							custParameters.put(entry.getKey(), entry.getValue());
						}

						// Set default data if missing
						if (!custParameters.containsKey("gender")) {
							custParameters.put("gender", Gender.UNKNOWN.getStorageValue());
						}
						if (!custParameters.containsKey("mailtype")) {
							custParameters.put("mailtype", MailType.HTML.getIntValue());
						}
						
						// Normalize email, if configured so
						if (custParameters.containsKey("email") && !configService.getBooleanValue(ConfigValue.AllowUnnormalizedEmails, admin.getCompanyID())) {
							custParameters.put("email", AgnUtils.normalizeEmail((String) custParameters.get("email")));
						}
						
						DatasourceDescription datasourceDescription = datasourceDescriptionDao.getByDescription(SourceGroupType.RestfulService, admin.getCompanyID(), "RestfulService");
						if (datasourceDescription == null) {
							// Use fallback datasource for companyid 0
							datasourceDescription = datasourceDescriptionDao.getByDescription(SourceGroupType.RestfulService, 0, "RestfulService");
						}
						if (datasourceDescription != null) {
							if (customerID <= 0) {
								recipient.getCustParameters().put("datasource_id", datasourceDescription.getId());
							}
							recipient.getCustParameters().put("latest_datasource_id", datasourceDescription.getId());
						}
						
						recipient.setChangeFlag(true);
						
						if (customerID <= 0) {
							try {
								subscriberLimitCheck.checkSubscriberLimit(admin.getCompanyID(), 1);
							} catch (SubscriberLimitExceededException e) {
								throw new RestfulClientException("Number of customer entries allowed is going to be exceeded. Number of existing customers would be " + e.getActual() + ". Maximum customer number limit is " + e.getMaximum() + ".");
							}
						}
						
						try {
							if (!recipientDao.updateInDbWithException(recipient, false)) {
								throw new RestfulClientException("Invalid recipient data for recipient");
							}
						} catch (ViciousFormDataException e) {
							throw new RestfulClientException("Invalid recipient data for recipient: " + e.getMessage(), e);
						}
					}
					
					if (mailinglistsToSubscribe != null) {
						for (int mailinglistToSubscribe : mailinglistsToSubscribe) {
							BindingEntry bindingEntry = new BindingEntryImpl();
							bindingEntry.setCustomerID(recipient.getCustomerID());
							bindingEntry.setMailinglistID(mailinglistToSubscribe);
							bindingEntry.setUserStatus(newUserStatus.getStatusCode());
							bindingEntry.setUserType(BindingEntry.UserType.World.getTypeCode());
							bindingEntry.setMediaType(newMediaType.getMediaCode());
							bindingEntry.setUserRemark("Set by " + admin.getUsername() + " via restful");
							
							boolean success = bindingEntryDao.updateBinding(bindingEntry, admin.getCompanyID());
							if (!success && !onlyUpdateExistingBindings) {
								bindingEntryDao.insertNewBinding(bindingEntry, admin.getCompanyID());
							}
						}
					}
					
					userActivityLogDao.addAdminUseOfFeature(admin, "restful/recipient", new Date());
					userActivityLogDao.writeUserActivityLog(admin, "restful/recipient PUT", "" + recipient.getCustomerID());
					
					DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DateUtilities.ISO_8601_DATETIME_FORMAT).withZone(TimeZone.getTimeZone(admin.getAdminTimezone()).toZoneId());

					CaseInsensitiveMap<String, Object> customerDataMap = recipientDao.getCustomerData(admin.getCompanyID(), recipient.getCustomerID());
					JsonObject customerJsonObject = new JsonObject();
					for (String key : AgnUtils.sortCollectionWithItemsFirst(customerDataMap.keySet(), "customer_id", "email")) {
						if (profileFields.get(key) != null && profileFields.get(key).getSimpleDataType() == SimpleDataType.Date && customerDataMap.get(key) instanceof Date) {
							customerJsonObject.add(key.toLowerCase(), new SimpleDateFormat(DateUtilities.ISO_8601_DATE_FORMAT_NO_TIMEZONE).format(customerDataMap.get(key)));
						} else if (profileFields.get(key) != null && profileFields.get(key).getSimpleDataType() == SimpleDataType.DateTime && customerDataMap.get(key) instanceof Date) {
							ZonedDateTime systemZonedDateTime = ZonedDateTime.ofInstant(((Date) customerDataMap.get(key)).toInstant(), ZoneId.systemDefault());
							customerJsonObject.add(key.toLowerCase(), dateTimeFormatter.format(systemZonedDateTime));
						} else {
							customerJsonObject.add(key.toLowerCase(), customerDataMap.get(key));
						}
					}
					return customerJsonObject;
				} else if (JsonToken.JsonArray_Open == readJsonToken) {
					if (StringUtils.isNotBlank(request.getParameter("status"))) {
						throw new RestfulClientException("Parameter status is not allowed for bulk creation of recipient. Use the import mode to define which subscription status to set");
					}
					
					// Multiple customer data
					if (!admin.permissionAllowed(Permission.IMPORT_MODE_ADD_UPDATE)) {
						throw new RestfulClientException("Authorization failed: Access denied '" + Permission.IMPORT_MODE_ADD_UPDATE.toString() + "'");
					}
					
					CaseInsensitiveMap<String, ProfileField> profileFields = columnInfoService.getColumnInfoMap(admin.getCompanyID(), admin.getAdminID());
			
					File importTempFileDirectory = new File(IMPORT_FILE_DIRECTORY + "/" + admin.getCompanyID());
					if (!importTempFileDirectory.exists()) {
						importTempFileDirectory.mkdirs();
					}
					
					String requestUUID = AgnUtils.generateNewUUID().toString().replace("-", "").toUpperCase();
					
					File temporaryImportFile = new File(importTempFileDirectory.getAbsolutePath() + "/Recipientimportjson_" + requestUUID + ".json");
					ImportMode importMode = ImportMode.ADD_AND_UPDATE;
					String keyColumn = "email";
						
					// Read recipient data entries and write them to a importable json file
					int foundItems = 0;
					try (FileOutputStream importFileOutputStream = new FileOutputStream(temporaryImportFile);
						JsonWriter jsonWriter = new JsonWriter(importFileOutputStream)) {
						jsonWriter.openJsonArray();
						while ((readJsonToken = jsonReader.readNextToken()) == JsonToken.JsonObject_Open) {
							JsonObject jsonObject = new JsonObject();
	
							while ((readJsonToken = jsonReader.readNextToken()) == JsonToken.JsonObject_PropertyKey) {
								String propertyKey = (String) jsonReader.getCurrentObject();
								if (!profileFields.containsKey(propertyKey)) {
									throw new RestfulClientException("Expected recipient property key '" + StringUtils.join(profileFields.keySet(), "', '") + "' but was: '" + jsonReader.getCurrentObject() + "'");
								} else if (jsonObject.containsPropertyKey(propertyKey)) {
									throw new RestfulClientException("Duplicate recipient property key: " + jsonReader.getCurrentObject());
								}
								readJsonToken = jsonReader.readNextToken();
								if (JsonToken.JsonSimpleValue != readJsonToken) {
									throw new RestfulClientException("Expected JSON item type 'JsonSimpleValue' but was: " + readJsonToken);
								}
								Object propertyValue = jsonReader.getCurrentObject();
								if (propertyValue != null && propertyValue instanceof String && !admin.permissionAllowed(Permission.RECIPIENT_PROFILEFIELD_HTML_ALLOWED)) {
									// Check for unallowed html content
									try {
										HtmlXSSPreventer.checkString((String) propertyValue);
									} catch(final XSSHtmlException e) {
										throw new RestfulClientException("Invalid recipient data containing HTML for recipient field: " + propertyKey);
									}
								}
								jsonObject.add(propertyKey, propertyValue);
							}
							
							for (Entry<String, String> entry : additionalProfileValues.entrySet()) {
								jsonObject.add(entry.getKey(), entry.getValue());
							}
	
							if (JsonToken.JsonObject_Close != readJsonToken) {
								throw new RestfulClientException("Expected JSON item type 'JsonObject_Close' was: " + readJsonToken);
							}
	
							jsonWriter.add(jsonObject);
							foundItems++;
						}
						jsonWriter.closeJsonArray();
					}
		
					if (JsonToken.JsonArray_Close != readJsonToken) {
						throw new RestfulClientException("Expected JSON item type 'JsonArray_Close' was: " + readJsonToken);
					}

					Set<MediaTypes> newMediaTypes = new HashSet<>();
					newMediaTypes.add(newMediaType);
					ImportStatus status = importRecipients(admin, importMode, keyColumn, mailinglistsToSubscribe, temporaryImportFile, requestUUID, newMediaTypes);
					
					userActivityLogDao.addAdminUseOfFeature(admin, "restful/recipient", new Date());
					userActivityLogDao.writeUserActivityLog(admin, "restful/recipient PUT", "IMPORT");
					
					JsonObject result = new JsonObject();
					if (status.getFatalError() != null) {
						throw new RestfulClientException(status.getFatalError());
					} else {
						result.add("Recipients", foundItems);
						result.add("Inserted", status.getInserted());
						result.add("Updated", status.getUpdated());
						
						boolean foundErrors = false;
						JsonObject errorsObject = new JsonObject();
						for (Entry<ImportErrorType, Integer> errorEntry : status.getErrors().entrySet()) {
							errorsObject.add(errorEntry.getKey().name(), errorEntry.getValue());
							if (errorEntry.getValue() > 0) {
								foundErrors = true;
							}
						}
						if (foundErrors) {
							result.add("Errors", errorsObject);
						}
					}
					return result;
				} else {
					throw new RestfulClientException("Expected root JSON item type 'JsonObject' or 'JsonArray' but was: " + readJsonToken);
				}
			}
		}
	}
	
	private void removeTripleDateEntries(Map<String, Object> custParameters) {
		custParameters.entrySet().removeIf(item -> StringUtils.endsWithIgnoreCase(item.getKey(), ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY)
			|| StringUtils.endsWithIgnoreCase(item.getKey(), ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH)
			|| StringUtils.endsWithIgnoreCase(item.getKey(), ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR)
			|| StringUtils.endsWithIgnoreCase(item.getKey(), ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR)
			|| StringUtils.endsWithIgnoreCase(item.getKey(), ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE)
			|| StringUtils.endsWithIgnoreCase(item.getKey(), ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND));
	}

	private ImportStatus importRecipients(ComAdmin admin, ImportMode importMode, String keyColumn, List<Integer> mailingListIdsToAssign, File temporaryImportFile, String sessionID, Set<MediaTypes> mediaTypes) throws Exception {
		ImportProfile importProfile = new ImportProfileImpl();
		importProfile.setCompanyId(admin.getCompanyID());
		importProfile.setAdminId(admin.getAdminID());
		importProfile.setUpdateAllDuplicates(true);
		importProfile.setDateFormat(DateFormat.ISO8601.getIntValue());
		importProfile.setImportMode(importMode.getIntValue());
		importProfile.setCheckForDuplicates(CheckForDuplicates.COMPLETE.getIntValue());
		importProfile.setNullValuesAction(0);
		importProfile.setKeyColumn(keyColumn);
		importProfile.setAutoMapping(true);
		importProfile.setDefaultMailType(MailType.HTML.getIntValue());
		importProfile.setDatatype("JSON"); // use JSON Import
		importProfile.setCharset(Charset.UTF_8.getIntValue());
		importProfile.setMediatypes(mediaTypes);

		DatasourceDescription dsDescription = new DatasourceDescriptionImpl();
		dsDescription.setId(0);
		dsDescription.setCompanyID(admin.getCompanyID());
		dsDescription.setSourceGroupType(SourceGroupType.File);
		dsDescription.setCreationDate(new Date());
		dsDescription.setDescription(temporaryImportFile.getName());
		dsDescription.setDescription2("Restful-Import");
		datasourceDescriptionDao.save(dsDescription);

		ProfileImportWorker profileImportWorker = profileImportWorkerFactory.getProfileImportWorker(
			false, // Not interactive mode, because there is no error edit GUI
			mailingListIdsToAssign,
			sessionID,
			admin,
			dsDescription.getId(),
			importProfile,
			new RemoteFile(temporaryImportFile.getName(), temporaryImportFile, -1),
			new ImportStatusImpl());

		if (admin.permissionAllowed(Permission.RECIPIENT_GENDER_EXTENDED)) {
			profileImportWorker.setMaxGenderValue(ConfigService.MAX_GENDER_VALUE_EXTENDED);
		} else {
			profileImportWorker.setMaxGenderValue(ConfigService.MAX_GENDER_VALUE_BASIC);
		}

		profileImportWorker.call();

		if (profileImportWorker.getError() != null) {
			throw profileImportWorker.getError();
		} else {
			return profileImportWorker.getStatus();
		}
	}

	@Override
	public ResponseType getResponseType() {
		return ResponseType.JSON;
	}
}
