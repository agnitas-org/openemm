/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.mailinglist;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.MailinglistImpl;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.useractivitylog.dao.UserActivityLogDao;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;
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

/**
 * This restful service is available at:
 * https:/<system.url>/restful/mailinglist
 */
public class MailinglistRestfulServiceHandler implements RestfulServiceHandler {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(MailinglistRestfulServiceHandler.class);
	
	public static final String NAMESPACE = "mailinglist";

	private UserActivityLogDao userActivityLogDao;
	private MailinglistDao mailinglistDao;

	@Required
	public void setUserActivityLogDao(UserActivityLogDao userActivityLogDao) {
		this.userActivityLogDao = userActivityLogDao;
	}
	
	@Required
	public void setMailinglistDao(MailinglistDao mailinglistDao) {
		this.mailinglistDao = mailinglistDao;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) throws Exception {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, ComAdmin admin, String requestDataFilePath, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(getMailinglistData(request, admin)));
		} else if (requestMethod == RequestMethod.DELETE) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(deleteMailinglist(request, admin)));
		} else if (requestDataFilePath == null || new File(requestDataFilePath).length() <= 0) {
			restfulResponse.setError(new RestfulClientException("Missing request data"), ErrorCode.REQUEST_DATA_ERROR);
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createNewMailinglist(request, new File(requestDataFilePath), admin)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createOrUpdateMailinglist(request, new File(requestDataFilePath), admin)));
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
	private Object getMailinglistData(HttpServletRequest request, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILINGLIST_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILINGLIST_SHOW.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 1);
		
		if (restfulContext.length == 0) {
			// Show index of all mailinglists
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/mailinglist", new Date());
			userActivityLogDao.writeUserActivityLog(admin, "restful/mailinglist GET", "ALL");
			
			JsonArray mailinglistsJsonArray = new JsonArray();
			
			for (Mailinglist mailinglist : mailinglistDao.getMailinglists(admin.getCompanyID())) {
				JsonObject mailinglistJsonObject = new JsonObject();
				mailinglistJsonObject.add("mailinglist_id", mailinglist.getId());
				mailinglistJsonObject.add("name", mailinglist.getShortname());
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
	private Object deleteMailinglist(HttpServletRequest request, ComAdmin admin) throws Exception {
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
	private Object createNewMailinglist(HttpServletRequest request, File requestDataFile, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILINGLIST_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.RECIPIENT_CREATE.toString() + "'");
		}
		
		try (InputStream inputStream = new FileInputStream(requestDataFile)) {
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
	private Object createOrUpdateMailinglist(HttpServletRequest request, File requestDataFile, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILINGLIST_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.RECIPIENT_CREATE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 1);
		
		try (InputStream inputStream = new FileInputStream(requestDataFile)) {
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
