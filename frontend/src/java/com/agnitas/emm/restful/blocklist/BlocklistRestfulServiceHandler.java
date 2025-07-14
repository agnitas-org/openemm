/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.blocklist;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Map.Entry;

import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import com.agnitas.beans.BlackListEntry;
import com.agnitas.beans.impl.BlackListEntryImpl;
import com.agnitas.util.HttpUtils.RequestMethod;
import org.apache.commons.lang3.StringUtils;
import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.blacklist.dao.BlacklistDao;
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

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This restful service is available at:
 * https://<system.url>/restful/blacklist
 */
public class BlocklistRestfulServiceHandler implements RestfulServiceHandler {
	
	public static final String NAMESPACE = "blocklist";

	private RestfulUserActivityLogDao userActivityLogDao;
	private BlacklistDao blacklistDao;

	public void setUserActivityLogDao(RestfulUserActivityLogDao userActivityLogDao) {
		this.userActivityLogDao = userActivityLogDao;
	}
	
	public void setBlacklistDao(BlacklistDao blacklistDao) {
		this.blacklistDao = blacklistDao;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, Admin admin, byte[] requestData, File requestDataFile, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod, boolean extendedLogging) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(getBlocklistData(request, admin)));
		} else if (requestMethod == RequestMethod.DELETE) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(deleteBlocklistEntry(request, admin)));
		} else if ((requestData == null || requestData.length == 0) && (requestDataFile == null || requestDataFile.length() <= 0)) {
			restfulResponse.setError(new RestfulClientException("Missing request data"), ErrorCode.REQUEST_DATA_ERROR);
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createNewBlocklistEntry(request, requestData, requestDataFile, admin)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createOrUpdateBlocklistEntry(request, requestData, requestDataFile, admin)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	/**
	 * Return a single or multiple blocklist data sets
	 * 
	 */
	private Object getBlocklistData(HttpServletRequest request, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.BLACKLIST)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.BLACKLIST.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 1);
		
		if (restfulContext.length == 0) {
			// Show blocklist entries
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/blocklist", new Date());
			writeActivityLog("ALL", request, admin);

			JsonArray blocklistsJsonArray = new JsonArray();
			
			for (BlackListEntry blockListEntry : blacklistDao.getBlacklistedRecipients(admin.getCompanyID())) {
				JsonObject blocklistJsonObject = new JsonObject();
				blocklistJsonObject.add("email", blockListEntry.getEmail());
				blocklistJsonObject.add("reason", blockListEntry.getReason());
				blocklistJsonObject.add("date", blockListEntry.getDate());
				blocklistsJsonArray.add(blocklistJsonObject);
			}
			
			return blocklistsJsonArray;
		} else {
			// Show blocklist entries a single email is blocklisted by
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/blocklist", new Date());
			writeActivityLog(restfulContext[0], request, admin);

			JsonArray blocklistsJsonArray = new JsonArray();
			
			for (BlackListEntry blockListEntry : blacklistDao.getBlacklistCheckEntries(admin.getCompanyID(), restfulContext[0])) {
				JsonObject blocklistJsonObject = new JsonObject();
				blocklistJsonObject.add("email", blockListEntry.getEmail());
				blocklistJsonObject.add("reason", blockListEntry.getReason());
				blocklistJsonObject.add("date", blockListEntry.getDate());
				blocklistsJsonArray.add(blocklistJsonObject);
			}
			
			return blocklistsJsonArray;
		}
	}

	/**
	 * Delete a blocklist
	 * 
	 */
	private Object deleteBlocklistEntry(HttpServletRequest request, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.BLACKLIST)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.BLACKLIST.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		
		userActivityLogDao.addAdminUseOfFeature(admin, "restful/blocklist", new Date());
		writeActivityLog(restfulContext[0], request, admin);

		boolean success = blacklistDao.delete(admin.getCompanyID(), restfulContext[0]);
		
		if (success) {
			return "1 blocklist entry deleted";
		} else {
			throw new RestfulNoDataFoundException("No data found for deletion");
		}
	}

	/**
	 * Create a new blocklist
	 * 
	 */
	private Object createNewBlocklistEntry(HttpServletRequest request, byte[] requestData, File requestDataFile, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.BLACKLIST)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.BLACKLIST.toString() + "'");
		}
		
		RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 0);
		
		try (InputStream inputStream = RestfulServiceHandler.getRequestDataStream(requestData, requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				JsonNode jsonNode = jsonReader.read();
				if (JsonDataType.OBJECT == jsonNode.getJsonDataType()) {
					String email = null;
					String reason = null;
					JsonObject jsonObject = (JsonObject) jsonNode.getValue();
					for (Entry<String, Object> entry : jsonObject.entrySet()) {
						if ("email".equals(entry.getKey())) {
							if (entry.getValue() instanceof String) {
								email = (String) entry.getValue();
							} else {
								throw new RestfulClientException("Invalid data type for 'email'. String expected");
							}
						} else if ("reason".equals(entry.getKey())) {
							if (entry.getValue() instanceof String) {
								reason = (String) entry.getValue();
							} else {
								throw new RestfulClientException("Invalid data type for 'reason'. String expected");
							}
						} else {
							throw new RestfulClientException("Invalid property '" + entry.getKey() + "' for blocklist entry");
						}
					}
					
					BlackListEntry newBlocklistEntry = new BlackListEntryImpl(email, reason, new Date());
					
					if (StringUtils.isBlank(newBlocklistEntry.getEmail())) {
						throw new RestfulClientException("Missing mandatory value for property value for 'email'");
					} else if (StringUtils.isBlank(newBlocklistEntry.getReason())) {
						throw new RestfulClientException("Missing mandatory value for property value for 'reason'");
					} else {
						if (blacklistDao.exist(admin.getCompanyID(), newBlocklistEntry.getEmail())) {
							throw new RestfulClientException("Blocklist entry with email '" + newBlocklistEntry.getEmail() + "' already exists");
						}
						
						blacklistDao.insert(admin.getCompanyID(), newBlocklistEntry.getEmail(), newBlocklistEntry.getReason());
						
						for (BlackListEntry blockListEntry : blacklistDao.getBlacklistedRecipients(admin.getCompanyID())) {
							if (blockListEntry.getEmail().equals(newBlocklistEntry.getEmail())) {
								JsonObject blocklistJsonObject = new JsonObject();
								blocklistJsonObject.add("email", blockListEntry.getEmail());
								blocklistJsonObject.add("reason", blockListEntry.getReason());
								blocklistJsonObject.add("date", blockListEntry.getDate());
								return blocklistJsonObject;
							}
						}
						
						throw new RestfulNoDataFoundException("No data found");
					}
				} else {
					throw new RestfulClientException("Expected root JSON item type 'JsonObject' but was: " + jsonNode.getJsonDataType());
				}
			}
		}
	}

	/**
	 * Create a new blocklist or update an exiting blocklist
	 * 
	 */
	private Object createOrUpdateBlocklistEntry(HttpServletRequest request, byte[] requestData, File requestDataFile, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.BLACKLIST)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.BLACKLIST.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 1);
		
		try (InputStream inputStream = RestfulServiceHandler.getRequestDataStream(requestData, requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				JsonNode jsonNode = jsonReader.read();
				if (JsonDataType.OBJECT == jsonNode.getJsonDataType()) {
					String email = null;
					String reason = null;
					JsonObject jsonObject = (JsonObject) jsonNode.getValue();
					for (Entry<String, Object> entry : jsonObject.entrySet()) {
						if ("email".equals(entry.getKey())) {
							if (entry.getValue() instanceof String) {
								email = (String) entry.getValue();
							} else {
								throw new RestfulClientException("Invalid data type for 'email'. String expected");
							}
						} else if ("reason".equals(entry.getKey())) {
							if (entry.getValue() instanceof String) {
								reason = (String) entry.getValue();
							} else {
								throw new RestfulClientException("Invalid data type for 'reason'. String expected");
							}
						} else {
							throw new RestfulClientException("Invalid property '" + entry.getKey() + "' for blocklist entry");
						}
					}
					
					BlackListEntry newBlocklistEntry = new BlackListEntryImpl(email, reason, new Date());
					
					if (StringUtils.isBlank(newBlocklistEntry.getEmail())) {
						throw new RestfulClientException("Missing mandatory value for property value for 'email'");
					} else if (StringUtils.isBlank(newBlocklistEntry.getReason())) {
						throw new RestfulClientException("Missing mandatory value for property value for 'reason'");
					} else {
						if (restfulContext.length == 1) {
							String requestedBlocklistEmail = restfulContext[0];
							
							BlackListEntry existingBlockListEntry = null;
							for (BlackListEntry blockListEntry : blacklistDao.getBlacklistedRecipients(admin.getCompanyID())) {
								if (blockListEntry.getEmail().equals(requestedBlocklistEmail)) {
									existingBlockListEntry = blockListEntry;
									break;
								}
							}
							
							if (existingBlockListEntry == null) {
								throw new RestfulClientException("Blocklist entry with email '" + newBlocklistEntry.getEmail() + "' already exists");
							} else {
								blacklistDao.delete(admin.getCompanyID(), requestedBlocklistEmail);
								
								blacklistDao.insert(admin.getCompanyID(), newBlocklistEntry.getEmail(), newBlocklistEntry.getReason());
							
								for (BlackListEntry blockListEntry : blacklistDao.getBlacklistedRecipients(admin.getCompanyID())) {
									if (blockListEntry.getEmail().equals(newBlocklistEntry.getEmail())) {
										JsonObject blocklistJsonObject = new JsonObject();
										blocklistJsonObject.add("email", blockListEntry.getEmail());
										blocklistJsonObject.add("reason", blockListEntry.getReason());
										blocklistJsonObject.add("date", blockListEntry.getDate());
										return blocklistJsonObject;
									}
								}
								
								throw new RestfulNoDataFoundException("No data found");
							}
						} else {
							if (blacklistDao.exist(admin.getCompanyID(), newBlocklistEntry.getEmail())) {
								blacklistDao.update(admin.getCompanyID(), newBlocklistEntry.getEmail(), newBlocklistEntry.getReason());
							} else {
								blacklistDao.insert(admin.getCompanyID(), newBlocklistEntry.getEmail(), newBlocklistEntry.getReason());
							}
							
							for (BlackListEntry blockListEntry : blacklistDao.getBlacklistedRecipients(admin.getCompanyID())) {
								if (blockListEntry.getEmail().equals(newBlocklistEntry.getEmail())) {
									JsonObject blocklistJsonObject = new JsonObject();
									blocklistJsonObject.add("email", blockListEntry.getEmail());
									blocklistJsonObject.add("reason", blockListEntry.getReason());
									blocklistJsonObject.add("date", blockListEntry.getDate());
									return blocklistJsonObject;
								}
							}
							
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
