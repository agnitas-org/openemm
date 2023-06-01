/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

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
import java.util.TimeZone;

import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.MailinglistImpl;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.useractivitylog.dao.UserActivityLogDao;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.restful.BaseRequestResponse;
import com.agnitas.emm.restful.ErrorCode;
import com.agnitas.emm.restful.JsonRequestResponse;
import com.agnitas.emm.restful.ResponseType;
import com.agnitas.emm.restful.RestfulClientException;
import com.agnitas.emm.restful.RestfulNoDataFoundException;
import com.agnitas.emm.restful.RestfulServiceHandler;
import com.agnitas.json.Json5Reader;
import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonDataType;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.service.ColumnInfoService;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This restful service is available at:
 * https://<system.url>/restful/mailinglist
 */
public class MailinglistRestfulServiceHandler implements RestfulServiceHandler {
	
	public static final String NAMESPACE = "mailinglist";

	private UserActivityLogDao userActivityLogDao;
	private MailinglistDao mailinglistDao;
	private ComRecipientDao recipientDao;
	private ColumnInfoService columnInfoService;

	@Required
	public void setUserActivityLogDao(UserActivityLogDao userActivityLogDao) {
		this.userActivityLogDao = userActivityLogDao;
	}
	
	@Required
	public void setMailinglistDao(MailinglistDao mailinglistDao) {
		this.mailinglistDao = mailinglistDao;
	}
	
	@Required
	public void setRecipientDao(ComRecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}
	
	@Required
	public void setColumnInfoService(ColumnInfoService columnInfoService) {
		this.columnInfoService = columnInfoService;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) throws Exception {
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
	 * @param request
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object getMailinglistData(HttpServletRequest request, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILINGLIST_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILINGLIST_SHOW.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 2);
		
		if (restfulContext.length == 0) {
			// Show index of all mailinglists
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/mailinglist", new Date());
			userActivityLogDao.writeUserActivityLog(admin, "restful/mailinglist GET", "ALL");
			
			JsonArray mailinglistsJsonArray = new JsonArray();
			
			for (Mailinglist mailinglist : mailinglistDao.getMailinglists(admin.getCompanyID())) {
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
				mailinglist = mailinglistDao.getMailinglist(Integer.parseInt(restfulContext[0]), admin.getCompanyID());
			} else {
				for (Mailinglist mailinglistItem : mailinglistDao.getMailinglists(admin.getCompanyID())) {
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
			userActivityLogDao.writeUserActivityLog(admin, "restful/mailinglist GET", restfulContext[0]);
			
			if (mailinglist != null) {
				JsonObject mailinglistJsonObject = new JsonObject();
				mailinglistJsonObject.add("mailinglist_id", mailinglist.getId());
				mailinglistJsonObject.add("name", mailinglist.getShortname());
				mailinglistJsonObject.add("description", mailinglist.getDescription());
				mailinglistJsonObject.add("creation_date", mailinglist.getCreationDate());
				mailinglistJsonObject.add("change_date", mailinglist.getChangeDate());

				JsonObject mailinglistStatisticsJsonObject = new JsonObject();
				for (Entry<Integer, Integer> entry : mailinglistDao.getMailinglistWorldSubscribersStatistics(admin.getCompanyID(), mailinglist.getId()).entrySet()) {
					mailinglistStatisticsJsonObject.add(UserStatus.getUserStatusByID(entry.getKey()).name().toLowerCase(), entry.getValue());
				}
				mailinglistJsonObject.add("statistics", mailinglistStatisticsJsonObject);
				
				if (showMailinglistRecipients) {
					CaseInsensitiveMap<String, ProfileField> profileFields = columnInfoService.getColumnInfoMap(admin.getCompanyID(), admin.getAdminID());
					
					List<String> profileFieldsToShow = null;
					String fieldsString = request.getParameter("fields");
					if (StringUtils.isNotBlank(fieldsString)) {
						if ("*".equals(fieldsString)) {
							profileFieldsToShow = new ArrayList<>();
							for (String profileField : profileFields.keySet()) {
								profileFieldsToShow.add(profileField);
							}
						} else {
							profileFieldsToShow = AgnUtils.splitAndTrimList(fieldsString);
							for (String profileField : profileFieldsToShow) {
								if (!profileFields.containsKey(profileField)) {
									throw new RestfulClientException("Unknown profile field: " + profileField);
								}
							}
						}
					}
					
					JsonArray recipientsArray = new JsonArray();
					List<CaseInsensitiveMap<String, Object>> recipients = recipientDao.getMailinglistRecipients(admin.getCompanyID(), mailinglist.getId(), MediaTypes.EMAIL, null, profileFieldsToShow, Arrays.asList(new UserStatus[] { UserStatus.Active }), TimeZone.getTimeZone(admin.getAdminTimezone()));
					for (CaseInsensitiveMap<String, Object> customerDataMap : recipients) {
						JsonObject customerJsonObject = new JsonObject();
						for (String key : AgnUtils.sortCollectionWithItemsFirst(customerDataMap.keySet(), "customer_id", "email")) {
							if (profileFields.get(key) != null && profileFields.get(key).getSimpleDataType() == SimpleDataType.Date && customerDataMap.get(key) instanceof Date) {
								customerJsonObject.add(key.toLowerCase(), new SimpleDateFormat(DateUtilities.ISO_8601_DATE_FORMAT_NO_TIMEZONE).format(customerDataMap.get(key)));
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
	 * 
	 * @param request
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object deleteMailinglist(HttpServletRequest request, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILINGLIST_DELETE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILINGLIST_DELETE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		
		userActivityLogDao.addAdminUseOfFeature(admin, "restful/mailinglist", new Date());
		userActivityLogDao.writeUserActivityLog(admin, "restful/mailinglist DELETE", restfulContext[0]);
		
		boolean success = false;
		if (AgnUtils.isNumber(restfulContext[0])) {
			success = mailinglistDao.deleteMailinglist(Integer.parseInt(restfulContext[0]), admin.getCompanyID());
		} else {
			for (Mailinglist mailinglistItem : mailinglistDao.getMailinglists(admin.getCompanyID())) {
				if (mailinglistItem.getShortname().equals(restfulContext[0])) {
					success = mailinglistDao.deleteMailinglist(mailinglistItem.getId(), admin.getCompanyID());
					break;
				}
			}
		}
		
		if (success) {
			return "1 mailinglist deleted";
		} else {
			throw new RestfulNoDataFoundException("No data found for deletion");
		}
	}

	/**
	 * Create a new mailinglist
	 * 
	 * @param request
	 * @param requestDataFile
	 * @param admin
	 * @return
	 * @throws Exception
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
							} else {
								throw new RestfulClientException("Invalid data type for 'shortname'. String expected");
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
						throw new RestfulClientException("Missing mandatory value for property value for 'shortname'");
					} else {
						for (Mailinglist mailinglistItem : mailinglistDao.getMailinglists(admin.getCompanyID())) {
							if (mailinglistItem.getShortname().equals(mailinglist.getShortname())) {
								throw new RestfulClientException("Mailinglist with name '" + mailinglist.getShortname() + "' already exists");
							}
						}
						
						mailinglistDao.saveMailinglist(mailinglist);
						
						mailinglist = mailinglistDao.getMailinglist(mailinglist.getId(), admin.getCompanyID());
						
						if (mailinglist != null) {
							userActivityLogDao.addAdminUseOfFeature(admin, "restful/mailinglist", new Date());
							userActivityLogDao.writeUserActivityLog(admin, "restful/mailinglist POST", "" + mailinglist.getId());
							
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
	 * @param request
	 * @param requestDataFile
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object createOrUpdateMailinglist(HttpServletRequest request, byte[] requestData, File requestDataFile, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILINGLIST_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILINGLIST_CHANGE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 1);
		
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
							} else {
								throw new RestfulClientException("Invalid data type for 'shortname'. String expected");
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
						throw new RestfulClientException("Missing mandatory value for property value for 'shortname'");
					} else {
						if (restfulContext.length == 1) {
							String requestedMailinglistKeyValue = restfulContext[0];
							Mailinglist requestedMailinglist = null;
							if (AgnUtils.isNumber(requestedMailinglistKeyValue)) {
								requestedMailinglist = mailinglistDao.getMailinglist(Integer.parseInt(requestedMailinglistKeyValue), admin.getCompanyID());
							} else {
								for (Mailinglist mailinglistItem : mailinglistDao.getMailinglists(admin.getCompanyID())) {
									if (mailinglistItem.getShortname().equals(requestedMailinglistKeyValue)) {
										requestedMailinglist = mailinglistItem;
										break;
									}
								}
							}
							
							if (requestedMailinglist != null) {
								mailinglist.setId(requestedMailinglist.getId());
							}
						}
						
						for (Mailinglist mailinglistItem : mailinglistDao.getMailinglists(admin.getCompanyID())) {
							if (mailinglistItem.getShortname().equals(mailinglist.getShortname()) && mailinglistItem.getId() != mailinglist.getId()) {
								throw new RestfulClientException("Mailinglist with name '" + mailinglist.getShortname() + "' already exists");
							}
						}
						
						mailinglistDao.saveMailinglist(mailinglist);
						
						mailinglist = mailinglistDao.getMailinglist(mailinglist.getId(), admin.getCompanyID());
						
						if (mailinglist != null) {
							userActivityLogDao.addAdminUseOfFeature(admin, "restful/mailinglist", new Date());
							userActivityLogDao.writeUserActivityLog(admin, "restful/mailinglist PUT", "" + mailinglist.getId());
							
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
}
