/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.mailinglist;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.impl.MailinglistImpl;
import com.agnitas.emm.common.UserStatus;
import org.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.DbColumnType.SimpleDataType;
import com.agnitas.util.HttpUtils.RequestMethod;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import com.agnitas.emm.restful.BaseRequestResponse;
import com.agnitas.emm.restful.ErrorCode;
import com.agnitas.emm.restful.JsonRequestResponse;
import com.agnitas.emm.restful.ResponseType;
import com.agnitas.emm.restful.RestfulClientException;
import com.agnitas.emm.restful.RestfulNoDataFoundException;
import com.agnitas.emm.restful.RestfulServiceHandler;
import com.agnitas.emm.util.html.HtmlChecker;
import com.agnitas.emm.util.html.HtmlCheckerException;
import com.agnitas.json.Json5Reader;
import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonDataType;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This restful service is available at:
 * https://<system.url>/restful/mailinglist
 */
public class MailinglistRestfulServiceHandler implements RestfulServiceHandler {
	public static final String NAMESPACE = "mailinglist";

	private RestfulUserActivityLogDao userActivityLogDao;
	private MailinglistService mailinglistService;
	private RecipientService recipientService;
	private RecipientFieldService recipientFieldService;

	public MailinglistRestfulServiceHandler(
			RestfulUserActivityLogDao userActivityLogDao,
			MailinglistService mailinglistService,
			RecipientService recipientService,
			RecipientFieldService recipientFieldService) {
		this.userActivityLogDao = userActivityLogDao;
		this.mailinglistService = mailinglistService;
		this.recipientService = recipientService;
		this.recipientFieldService = recipientFieldService;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, Admin admin, byte[] requestData, File requestDataFile, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod, boolean extendedLogging) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(getMailinglistData(request, admin)));
		} else if (requestMethod == RequestMethod.DELETE) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(deleteMailinglist(request, admin)));
		} else if ((requestData == null || requestData.length == 0) && (requestDataFile == null || requestDataFile.length() <= 0)) {
			restfulResponse.setError(new RestfulClientException("Missing request data"), ErrorCode.REQUEST_DATA_ERROR);
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createNewMailinglist(request, requestData, requestDataFile, admin)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createOrUpdateMailinglist(request, requestData, requestDataFile, admin)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	/**
	 * Return a single or multiple mailinglist data sets
	 * 
	 */
	private Object getMailinglistData(HttpServletRequest request, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILINGLIST_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILINGLIST_SHOW.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 2);
		
		if (restfulContext.length == 0) {
			// Show index of all mailinglists
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/mailinglist", new Date());
			writeActivityLog("ALL", request, admin);

			JsonArray mailinglistsJsonArray = new JsonArray();
			
			for (Mailinglist mailinglist : mailinglistService.getMailinglists(admin.getCompanyID())) {
				JsonObject mailinglistJsonObject = new JsonObject();
				mailinglistJsonObject.add("mailinglist_id", mailinglist.getId());
				mailinglistJsonObject.add("name", mailinglist.getShortname());
				mailinglistJsonObject.add("description", mailinglist.getDescription());
				mailinglistsJsonArray.add(mailinglistJsonObject);
			}
			
			return mailinglistsJsonArray;
		} else {
			// Show single mailinglist
			Mailinglist mailinglist = null;
			if (AgnUtils.isNumber(restfulContext[0])) {
				mailinglist = mailinglistService.getMailinglist(Integer.parseInt(restfulContext[0]), admin.getCompanyID());
			} else {
				for (Mailinglist mailinglistItem : mailinglistService.getMailinglists(admin.getCompanyID())) {
					if (mailinglistItem.getShortname().equals(restfulContext[0])) {
						mailinglist = mailinglistItem;
						break;
					}
				}
			}
			
			boolean showMailinglistRecipients = false;
			if (restfulContext.length == 2) {
				if ("recipients".equalsIgnoreCase(restfulContext[1])) {
					showMailinglistRecipients = true;
				} else {
					throw new RestfulClientException("Invalid requestcontext: " + restfulContext[1]);
				}
			}
			
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/mailinglist", new Date());
			writeActivityLog(restfulContext[0], request, admin);

			if (mailinglist != null) {
				JsonObject mailinglistJsonObject = new JsonObject();
				mailinglistJsonObject.add("mailinglist_id", mailinglist.getId());
				mailinglistJsonObject.add("name", mailinglist.getShortname());
				mailinglistJsonObject.add("description", mailinglist.getDescription());
				mailinglistJsonObject.add("creation_date", mailinglist.getCreationDate());
				mailinglistJsonObject.add("change_date", mailinglist.getChangeDate());

				JsonObject mailinglistStatisticsJsonObject = new JsonObject();
				for (Entry<Integer, Integer> entry : mailinglistService.getMailinglistWorldSubscribersStatistics(admin.getCompanyID(), mailinglist.getId()).entrySet()) {
					mailinglistStatisticsJsonObject.add(UserStatus.getUserStatusByID(entry.getKey()).name().toLowerCase(), entry.getValue());
				}
				mailinglistJsonObject.add("statistics", mailinglistStatisticsJsonObject);
				
				if (showMailinglistRecipients) {
					List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(admin.getCompanyID());
					CaseInsensitiveMap<String, RecipientFieldDescription> recipientFieldsMap = new CaseInsensitiveMap<>(recipientFields.stream().collect(Collectors.toMap(RecipientFieldDescription::getColumnName, Function.identity())));
					
					List<String> profileFieldsToShow = null;
					String fieldsString = request.getParameter("fields");
					if (StringUtils.isNotBlank(fieldsString)) {
						if ("*".equals(fieldsString)) {
							profileFieldsToShow = new ArrayList<>();
							for (RecipientFieldDescription profileField : recipientFields) {
								if (profileField.getAdminPermission(admin.getAdminID()) != ProfileFieldMode.NotVisible) {
									profileFieldsToShow.add(profileField.getColumnName());
								}
							}
						} else {
							profileFieldsToShow = AgnUtils.splitAndTrimList(fieldsString);
							for (String profileFieldName : profileFieldsToShow) {
								if (recipientFieldsMap.get(profileFieldName) == null || recipientFieldsMap.get(profileFieldName).getAdminPermission(admin.getAdminID()) == ProfileFieldMode.NotVisible) {
									throw new RestfulClientException("Unknown profile field: " + profileFieldName);
								}
							}
						}
					}
					
					JsonArray recipientsArray = new JsonArray();
					List<CaseInsensitiveMap<String, Object>> recipients = recipientService.getMailinglistRecipients(admin.getCompanyID(), mailinglist.getId(), MediaTypes.EMAIL, null, profileFieldsToShow, Arrays.asList(new UserStatus[] { UserStatus.Active }), TimeZone.getTimeZone(admin.getAdminTimezone()));
					for (CaseInsensitiveMap<String, Object> customerDataMap : recipients) {
						JsonObject customerJsonObject = new JsonObject();
						for (String key : AgnUtils.sortCollectionWithItemsFirst(customerDataMap.keySet(), "customer_id", "email")) {
							if (recipientFieldsMap.get(key) != null && recipientFieldsMap.get(key).getSimpleDataType() == SimpleDataType.Date && customerDataMap.get(key) instanceof Date) {
								customerJsonObject.add(key.toLowerCase(), new SimpleDateFormat(DateUtilities.ISO_8601_DATE_FORMAT_NO_TIMEZONE).format(customerDataMap.get(key)));
							} else if (recipientFieldsMap.get(key) != null && recipientFieldsMap.get(key).getSimpleDataType() == SimpleDataType.DateTime && customerDataMap.get(key) instanceof Date) {
								customerJsonObject.add(key.toLowerCase(), new SimpleDateFormat(DateUtilities.ISO_8601_DATETIME_FORMAT).format(customerDataMap.get(key)));
							} else {
								customerJsonObject.add(key.toLowerCase(), customerDataMap.get(key));
							}
						}
						recipientsArray.add(customerJsonObject);
					}
					mailinglistJsonObject.add("recipients", recipientsArray);
				}
				return mailinglistJsonObject;
			} else {
				throw new RestfulNoDataFoundException("No data found");
			}
		}
	}

	/**
	 * Delete a mailinglist
	 */
	private Object deleteMailinglist(HttpServletRequest request, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILINGLIST_DELETE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILINGLIST_DELETE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		
		userActivityLogDao.addAdminUseOfFeature(admin, "restful/mailinglist", new Date());
		writeActivityLog(restfulContext[0], request, admin);

		int mailinglistIdToDelete = 0;
		
		if (AgnUtils.isNumber(restfulContext[0])) {
			mailinglistIdToDelete = Integer.parseInt(restfulContext[0]);
			
		} else {
			for (Mailinglist mailinglistItem : mailinglistService.getMailinglists(admin.getCompanyID())) {
				if (mailinglistItem.getShortname().equals(restfulContext[0])) {
					mailinglistIdToDelete = mailinglistItem.getId();
					break;
				}
			}
		}
		
		List<Mailing> dependentMailings = mailinglistService.getUsedMailings(Set.of(mailinglistIdToDelete), admin.getCompanyID());
        if (!dependentMailings.isEmpty()) {
        	String mailinglistInfo = "\"" + mailinglistService.getMailinglistName(mailinglistIdToDelete, admin.getCompanyID()) + "\" (ID: " + Integer.toString(mailinglistIdToDelete) + ")";
        	String mailingInfos = dependentMailings.stream().limit(10).map(x -> "\"" + x.getShortname() + "\" (ID: " + Integer.toString(x.getId()) + ")").collect(Collectors.joining(", "));
        	throw new RestfulClientException("Deletion of mailinglist " + mailinglistInfo + " failed, because there are mailings depending on it: " + mailingInfos);
        } else {
			boolean success = mailinglistService.deleteMailinglist(mailinglistIdToDelete, admin.getCompanyID());
			
			if (success) {
				return "1 mailinglist deleted";
			} else {
				throw new RestfulNoDataFoundException("No data found for deletion");
			}
        }
	}

	/**
	 * Create a new mailinglist
	 * 
	 */
	private Object createNewMailinglist(HttpServletRequest request, byte[] requestData, File requestDataFile, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILINGLIST_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILINGLIST_CHANGE.toString() + "'");
		}
		
		try (InputStream inputStream = RestfulServiceHandler.getRequestDataStream(requestData, requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				JsonNode jsonNode = jsonReader.read();
				if (JsonDataType.OBJECT == jsonNode.getJsonDataType()) {
					JsonObject jsonObject = (JsonObject) jsonNode.getValue();
					Mailinglist mailinglist = new MailinglistImpl();
					mailinglist.setCompanyID(admin.getCompanyID());
					for (Entry<String, Object> entry : jsonObject.entrySet()) {
						if ("name".equals(entry.getKey())) {
							if (entry.getValue() instanceof String) {
								mailinglist.setShortname((String) entry.getValue());
								// Check for unallowed html tags
								try {
									HtmlChecker.checkForUnallowedHtmlTags(mailinglist.getShortname(), false);
								} catch(final HtmlCheckerException e) {
									throw new RestfulClientException("Mailinglist name contains unallowed HTML tags", e);
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'name'. String expected");
							}
						} else if ("description".equals(entry.getKey())) {
							if (entry.getValue() instanceof String) {
								mailinglist.setDescription((String) entry.getValue());
								// Check for unallowed html tags
								try {
									HtmlChecker.checkForUnallowedHtmlTags(mailinglist.getDescription(), false);
								} catch(final HtmlCheckerException e) {
									throw new RestfulClientException("Mailinglist description contains unallowed HTML tags", e);
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'description'. String expected");
							}
						} else {
							throw new RestfulClientException("Invalid property '" + entry.getKey() + "' for mailinglist");
						}
					}
					
					if (StringUtils.isBlank(mailinglist.getShortname())) {
						throw new RestfulClientException("Missing mandatory value for property value for 'name'");
					} else {
						for (Mailinglist mailinglistItem : mailinglistService.getMailinglists(admin.getCompanyID())) {
							if (mailinglistItem.getShortname().equals(mailinglist.getShortname())) {
								throw new RestfulClientException("Mailinglist with name '" + mailinglist.getShortname() + "' already exists");
							}
						}
						
						mailinglistService.saveMailinglist(mailinglist);
						
						mailinglist = mailinglistService.getMailinglist(mailinglist.getId(), admin.getCompanyID());
						
						if (mailinglist != null) {
							userActivityLogDao.addAdminUseOfFeature(admin, "restful/mailinglist", new Date());
							writeActivityLog(String.valueOf(mailinglist.getId()), request, admin);

							JsonObject mailinglistJsonObject = new JsonObject();
							mailinglistJsonObject.add("mailinglist_id", mailinglist.getId());
							mailinglistJsonObject.add("name", mailinglist.getShortname());
							mailinglistJsonObject.add("description", mailinglist.getDescription());
							mailinglistJsonObject.add("creation_date", mailinglist.getCreationDate());
							mailinglistJsonObject.add("change_date", mailinglist.getChangeDate());
							
							return mailinglistJsonObject;
						} else {
							throw new RestfulNoDataFoundException("No data found");
						}
					}
				} else {
					throw new RestfulClientException("Expected root JSON item type 'JsonObject' but was: " + jsonNode.getJsonDataType());
				}
			}
		}
	}

	/**
	 * Create a new mailinglist or update an exiting mailinglist
	 * 
	 */
	private Object createOrUpdateMailinglist(HttpServletRequest request, byte[] requestData, File requestDataFile, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILINGLIST_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILINGLIST_CHANGE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 1);
		
		Mailinglist existingMailinglist = null;
		
		if (restfulContext.length == 1) {
			String requestedMailinglistKeyValue = restfulContext[0];
			if (AgnUtils.isNumber(requestedMailinglistKeyValue)) {
				existingMailinglist = mailinglistService.getMailinglist(Integer.parseInt(requestedMailinglistKeyValue), admin.getCompanyID());
			} else {
				for (Mailinglist mailinglistItem : mailinglistService.getMailinglists(admin.getCompanyID())) {
					if (mailinglistItem.getShortname().equals(requestedMailinglistKeyValue)) {
						existingMailinglist = mailinglistItem;
						break;
					}
				}
			}
		}
		
		try (InputStream inputStream = RestfulServiceHandler.getRequestDataStream(requestData, requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				JsonNode jsonNode = jsonReader.read();
				if (JsonDataType.OBJECT == jsonNode.getJsonDataType()) {
					JsonObject jsonObject = (JsonObject) jsonNode.getValue();
					Mailinglist mailinglist = new MailinglistImpl();
					mailinglist.setCompanyID(admin.getCompanyID());
					
					if (jsonObject.containsPropertyKey("mailinglist_id")) {
						if (jsonObject.get("mailinglist_id") instanceof Integer) {
							if (existingMailinglist == null) {
								existingMailinglist = mailinglistService.getMailinglist((Integer) jsonObject.get("mailinglist_id"), admin.getCompanyID());
								if (existingMailinglist == null) {
									throw new RestfulClientException("No such mailinglist for update");
								}
							} else {
								if (existingMailinglist.getId() != (Integer) jsonObject.get("mailinglist_id")) {
									throw new RestfulClientException("Invalid data for existing mailinglist for 'mailinglist_id'");
								}
							}
						} else {
							throw new RestfulClientException("Invalid data type for 'mailinglist_id'. Integer expected");
						}
					}
					
					if (jsonObject.containsPropertyKey("name")) {
						if (jsonObject.get("name") instanceof String) {
							if (StringUtils.isBlank((String) jsonObject.get("name"))) {
								throw new RestfulClientException("Invalid empty data for 'name'. String expected");
							} else {
								if (existingMailinglist == null) {
									for (Mailinglist mailinglistItem : mailinglistService.getMailinglists(admin.getCompanyID())) {
										if (mailinglistItem.getShortname().equals(jsonObject.get("name"))) {
											existingMailinglist = mailinglistItem;
											break;
										}
									}
								}
							}
						} else {
							throw new RestfulClientException("Invalid data type for 'name'. String expected");
						}
					}
					
					if (existingMailinglist != null) {
						if (mailinglist.getCompanyID() != existingMailinglist.getCompanyID()) {
							throw new RestfulClientException("Invalid data for 'clientID'. Cannot change existing mailinglist of other client");
						}
						
						mailinglist = existingMailinglist;
					}
					
					for (Entry<String, Object> entry : jsonObject.entrySet()) {
						if ("mailinglist_id".equals(entry.getKey())) {
							if (entry.getValue() instanceof Integer) {
								if (mailinglist.getId() != (Integer) entry.getValue()) {
									throw new RestfulClientException("Invalid new data for internal value 'mailinglist_id'");
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'mailinglist_id'. Integer expected");
							}
						} else if ("name".equals(entry.getKey())) {
							if (entry.getValue() instanceof String) {
								mailinglist.setShortname((String) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'name'. String expected");
							}
						} else if ("description".equals(entry.getKey())) {
							if (entry.getValue() instanceof String) {
								mailinglist.setDescription((String) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'description'. String expected");
							}
						} else {
							throw new RestfulClientException("Invalid property '" + entry.getKey() + "' for mailinglist");
						}
					}
					
					if (StringUtils.isBlank(mailinglist.getShortname())) {
						throw new RestfulClientException("Missing mandatory value for property value for 'name'");
					} else {
						mailinglistService.saveMailinglist(mailinglist);
						
						mailinglist = mailinglistService.getMailinglist(mailinglist.getId(), admin.getCompanyID());
						
						if (mailinglist != null) {
							userActivityLogDao.addAdminUseOfFeature(admin, "restful/mailinglist", new Date());
							writeActivityLog(String.valueOf(mailinglist.getId()), request, admin);

							JsonObject mailinglistJsonObject = new JsonObject();
							mailinglistJsonObject.add("mailinglist_id", mailinglist.getId());
							mailinglistJsonObject.add("name", mailinglist.getShortname());
							mailinglistJsonObject.add("description", mailinglist.getDescription());
							mailinglistJsonObject.add("creation_date", mailinglist.getCreationDate());
							mailinglistJsonObject.add("change_date", mailinglist.getChangeDate());
							
							return mailinglistJsonObject;
						} else {
							throw new RestfulNoDataFoundException("No data found");
						}
					}
				} else {
					throw new RestfulClientException("Expected root JSON item type 'JsonObject' but was: " + jsonNode.getJsonDataType());
				}
			}
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
