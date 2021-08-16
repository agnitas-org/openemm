/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.impl.DynamicTagContentImpl;
import org.agnitas.emm.core.useractivitylog.dao.UserActivityLogDao;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.impl.DynamicTagImpl;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.dao.DynamicTagDao;
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
 * https://<system.url>/restful/content
 */
public class ContentRestfulServiceHandler implements RestfulServiceHandler {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(ContentRestfulServiceHandler.class);
	
	public static final String NAMESPACE = "content";

	public static final Object EXPORTED_TO_STREAM = new Object();

	private UserActivityLogDao userActivityLogDao;
	private ComMailingDao mailingDao;
	private DynamicTagDao dynamicTagDao;
	private ComTargetDao targetDao;

	@Required
	public void setUserActivityLogDao(UserActivityLogDao userActivityLogDao) {
		this.userActivityLogDao = userActivityLogDao;
	}
	
	@Required
	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
	
	@Required
	public void setDynamicTagDao(DynamicTagDao dynamicTagDao) {
		this.dynamicTagDao = dynamicTagDao;
	}
	
	@Required
	public void setTargetDao(ComTargetDao targetDao) {
		this.targetDao = targetDao;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) throws Exception {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, ComAdmin admin, String requestDataFilePath, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(getContent(request, response, admin)));
		} else if (requestMethod == RequestMethod.DELETE) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(deleteContent(request, admin)));
		} else if (requestDataFilePath == null || new File(requestDataFilePath).length() <= 0) {
			restfulResponse.setError(new RestfulClientException("Missing request data"), ErrorCode.REQUEST_DATA_ERROR);
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createNewContent(request, new File(requestDataFilePath), admin)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createOrUpdateContent(request, new File(requestDataFilePath), admin)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	/**
	 * Return a single or multiple content data sets
	 * 
	 * @param request
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object getContent(HttpServletRequest request, HttpServletResponse response, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_CONTENT_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_CONTENT_SHOW.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 2);
		
		if (!AgnUtils.isNumber(restfulContext[0])) {
			throw new RestfulClientException("Invalid MailingID: " + restfulContext[0]);
		}
		int mailingID = Integer.parseInt(restfulContext[0]);
		if (!mailingDao.exist(mailingID, admin.getCompanyID())) {
			throw new RestfulClientException("Invalid not existing MailingID: " + mailingID);
		}
		
		if (restfulContext.length == 1) {
			// Show all contents of a mailing
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/content", new Date());
			userActivityLogDao.writeUserActivityLog(admin, "restful/content GET", "MID:" + mailingID + " ALL");
			
			JsonArray contentsJsonArray = new JsonArray();
			
			for (DynamicTag dynamicTag : mailingDao.getDynamicTags(mailingID, admin.getCompanyID())) {
				contentsJsonArray.add(createContentJsonObject(dynamicTag));
			}
			
			return contentsJsonArray;
		} else {
			// Export a single content of a mailing
			String requestedContentKeyValue = restfulContext[1];
			
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/content", new Date());
			userActivityLogDao.writeUserActivityLog(admin, "restful/content GET", "MID: " + mailingID + " " + requestedContentKeyValue);
			
			int dynTagNameID;
			if (AgnUtils.isNumber(requestedContentKeyValue)) {
				dynTagNameID = Integer.parseInt(requestedContentKeyValue);
			} else {
				dynTagNameID = dynamicTagDao.getId(admin.getCompanyID(), mailingID, requestedContentKeyValue);
			}
			
			if (dynTagNameID <= 0) {
				throw new RestfulNoDataFoundException("No data found");
			}
			
			DynamicTag dynamicTag = mailingDao.getDynamicTag(dynTagNameID, admin.getCompanyID());
			
			if (dynamicTag == null) {
				throw new RestfulNoDataFoundException("No data found");
			}
			
			return createContentJsonObject(dynamicTag);
		}
	}

	/**
	 * Delete a content
	 * 
	 * @param request
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object deleteContent(HttpServletRequest request, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_CHANGE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 2, 2);
		
		if (!AgnUtils.isNumber(restfulContext[0])) {
			throw new RestfulClientException("Invalid MailingID: " + restfulContext[0]);
		}
		int mailingID = Integer.parseInt(restfulContext[0]);
		if (!mailingDao.exist(mailingID, admin.getCompanyID())) {
			throw new RestfulClientException("Invalid not existing MailingID: " + mailingID);
		}
		
		String requestedContentKeyValue = restfulContext[1];
		
		userActivityLogDao.addAdminUseOfFeature(admin, "restful/content", new Date());
		userActivityLogDao.writeUserActivityLog(admin, "restful/content DELETE", "MID: " + mailingID + " " + requestedContentKeyValue);
		
		String dynTagName;
		if (AgnUtils.isNumber(requestedContentKeyValue)) {
			int dynTagNameID = Integer.parseInt(requestedContentKeyValue);
			dynTagName = dynamicTagDao.getDynamicTagName(admin.getCompanyID(), mailingID, dynTagNameID);
		} else {
			dynTagName = requestedContentKeyValue;
			if (dynamicTagDao.getId(admin.getCompanyID(), mailingID, dynTagName) <= 0) {
				throw new RestfulNoDataFoundException("No data found for deletion");
			}
		}
		
		if (StringUtils.isNotBlank(dynTagName)) {
			dynamicTagDao.markNameAsDeleted(mailingID, dynTagName);
			return "1 content deleted";
		} else {
			throw new RestfulNoDataFoundException("No data found for deletion");
		}
	}

	/**
	 * Create a new content
	 * 
	 * @param request
	 * @param requestDataFile
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object createNewContent(HttpServletRequest request, File requestDataFile, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_CHANGE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		
		if (!AgnUtils.isNumber(restfulContext[0])) {
			throw new RestfulClientException("Invalid MailingID: " + restfulContext[0]);
		}
		int mailingID = Integer.parseInt(restfulContext[0]);
		if (!mailingDao.exist(mailingID, admin.getCompanyID())) {
			throw new RestfulClientException("Invalid not existing MailingID: " + mailingID);
		}
				
		userActivityLogDao.addAdminUseOfFeature(admin, "restful/content", new Date());
		userActivityLogDao.writeUserActivityLog(admin, "restful/content POST", "MID: " + mailingID);
		
		DynamicTag dynamicTag = parseContentJsonObject(requestDataFile, admin);
		dynamicTag.setMailingID(mailingID);
		
		if (dynamicTagDao.getId(admin.getCompanyID(), mailingID, dynamicTag.getDynName()) > 0) {
			throw new RestfulClientException("Content already exists: " + dynamicTag.getDynName());
		} else {
			mailingDao.createDynamicTags(admin.getCompanyID(), mailingID, "UTF-8", Collections.singletonList(dynamicTag));
			return createContentJsonObject(mailingDao.getDynamicTag(dynamicTag.getId(), admin.getCompanyID()));
		}
	}

	/**
	 * Update an existing content
	 * 
	 * @param request
	 * @param requestDataFile
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object createOrUpdateContent(HttpServletRequest request, File requestDataFile, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_CHANGE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 2);
		
		if (!AgnUtils.isNumber(restfulContext[0])) {
			throw new RestfulClientException("Invalid MailingID: " + restfulContext[0]);
		}
		int mailingID = Integer.parseInt(restfulContext[0]);
		if (!mailingDao.exist(mailingID, admin.getCompanyID())) {
			throw new RestfulClientException("Invalid not existing MailingID: " + mailingID);
		}
		
		if (restfulContext.length == 1) {
			// Insert content to a mailing or update an existing content of a mailing by name
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/content", new Date());
			userActivityLogDao.writeUserActivityLog(admin, "restful/content PUT", "MID: " + mailingID);
			
			DynamicTag dynamicTag = parseContentJsonObject(requestDataFile, admin);
			dynamicTag.setMailingID(mailingID);
			
			if (dynamicTagDao.getId(admin.getCompanyID(), mailingID, dynamicTag.getDynName()) > 0) {
				throw new RestfulClientException("Content already exists: " + dynamicTag.getDynName());
			} else {
				mailingDao.createDynamicTags(admin.getCompanyID(), mailingID, "UTF-8", Collections.singletonList(dynamicTag));
				return createContentJsonObject(mailingDao.getDynamicTag(dynamicTag.getId(), admin.getCompanyID()));
			}
		} else {
			// Update content of a mailing by name or content_id
			String requestedContentKeyValue = restfulContext[1];
			
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/content", new Date());
			userActivityLogDao.writeUserActivityLog(admin, "restful/content PUT", "MID: " + mailingID + " " + requestedContentKeyValue);
			
			DynamicTag dynamicTag = parseContentJsonObject(requestDataFile, admin);
			dynamicTag.setMailingID(mailingID);
			
			DynamicTag existingDynamicTag;
			if (AgnUtils.isNumber(requestedContentKeyValue)) {
				int contentID = Integer.parseInt(requestedContentKeyValue);
				if (contentID > 0) {
					existingDynamicTag = mailingDao.getDynamicTag(contentID, admin.getCompanyID());
				} else {
					existingDynamicTag = null;
				}
				
				if (existingDynamicTag == null) {
					throw new RestfulClientException("Invalid non existing content_id: " + requestedContentKeyValue);
				}
			} else {
				int dynTagId = dynamicTagDao.getId(admin.getCompanyID(), mailingID, dynamicTag.getDynName());
				if (dynTagId > 0) {
					existingDynamicTag = mailingDao.getDynamicTag(dynTagId, admin.getCompanyID());
				} else {
					existingDynamicTag = null;
				}
				
				if (existingDynamicTag == null) {
					throw new RestfulClientException("Invalid non existing content name: " + requestedContentKeyValue);
				}
			}
			
			dynamicTag.setId(existingDynamicTag.getId());
			
			mailingDao.updateDynamicTags(admin.getCompanyID(), mailingID, "UTF-8", Collections.singletonList(dynamicTag));
			
			DynamicTag storedDynamicTag = mailingDao.getDynamicTag(dynamicTag.getId(), admin.getCompanyID());
			
			if (storedDynamicTag != null) {
				return createContentJsonObject(storedDynamicTag);
			} else {
				throw new RestfulNoDataFoundException("No data found");
			}
		}
	}

	private JsonObject createContentJsonObject(DynamicTag dynamicTag) throws Exception {
		JsonObject dynamicTagJsonObject = new JsonObject();
		
		dynamicTagJsonObject.add("id", dynamicTag.getId());
		dynamicTagJsonObject.add("name", dynamicTag.getDynName());
		if (dynamicTag.isDisableLinkExtension()) {
			dynamicTagJsonObject.add("disableLinkExtension", dynamicTag.isDisableLinkExtension());
		}
		
		if (dynamicTag.getDynContent().size() > 0) {
			JsonArray contentArray = new JsonArray();
			for (DynamicTagContent dynamicTagContent : dynamicTag.getDynContent().values()) {
				JsonObject contentJsonObject = new JsonObject();
				
				contentJsonObject.add("id", dynamicTagContent.getId());
				if (dynamicTagContent.getTargetID() > 0) {
					contentJsonObject.add("target_id", dynamicTagContent.getTargetID());
				}
				if (dynamicTagContent.getDynOrder() > 0) {
					contentJsonObject.add("order", dynamicTagContent.getDynOrder());
				}
				contentJsonObject.add("text", dynamicTagContent.getDynContent());
				
				contentArray.add(contentJsonObject);
			}
			dynamicTagJsonObject.add("content", contentArray);
		}
		
		return dynamicTagJsonObject;
	}

	private DynamicTag parseContentJsonObject(File requestDataFile, ComAdmin admin) throws Exception {
		DynamicTag dynamicTag = new DynamicTagImpl();
		dynamicTag.setCompanyID(admin.getCompanyID());
		
		try (InputStream inputStream = new FileInputStream(requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				JsonNode jsonNode = jsonReader.read();
				if (JsonDataType.OBJECT == jsonNode.getJsonDataType()) {
					JsonObject jsonObject = (JsonObject) jsonNode.getValue();
					for (Entry<String, Object> entry : jsonObject.entrySet()) {
						if ("name".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof String) {
								dynamicTag.setDynName((String) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'name'. String expected");
							}
						} else if ("disableLinkExtension".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof Boolean) {
								dynamicTag.setDisableLinkExtension((Boolean) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'disableLinkExtension'. Boolean expected");
							}
						} else if ("content".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof JsonArray && ((JsonArray) entry.getValue()).size() > 0) {
								for (Object contentObject : (JsonArray) entry.getValue()) {
									if (contentObject != null && contentObject instanceof JsonObject) {
										DynamicTagContent dynamicTagContent = new DynamicTagContentImpl();
										
										for (Entry<String, Object> contentItemEntry : ((JsonObject) contentObject).entrySet()) {
											if ("target_id".equals(contentItemEntry.getKey())) {
												if (contentItemEntry.getValue() != null && contentItemEntry.getValue() instanceof Integer) {
													dynamicTagContent.setTargetID((Integer) contentItemEntry.getValue());
												} else {
													throw new RestfulClientException("Invalid data type for 'target_id'. Integer expected");
												}
												
												if (targetDao.getTarget(dynamicTagContent.getTargetID(), admin.getCompanyID()) == null) {
													throw new RestfulClientException("Invalid not existing 'target_id': " + dynamicTagContent.getTargetID());
												}
											} else if ("order".equals(contentItemEntry.getKey())) {
												if (contentItemEntry.getValue() != null && contentItemEntry.getValue() instanceof Integer) {
													dynamicTagContent.setDynOrder((Integer) contentItemEntry.getValue());
												} else {
													throw new RestfulClientException("Invalid data type for 'order'. Integer expected");
												}
											} else if ("text".equals(contentItemEntry.getKey())) {
												if (contentItemEntry.getValue() != null && contentItemEntry.getValue() instanceof String) {
													dynamicTagContent.setDynContent((String) contentItemEntry.getValue());
												} else {
													throw new RestfulClientException("Invalid data type for 'text'. String expected");
												}
											} else {
												throw new RestfulClientException("Unexpected 'content' item property: " + contentItemEntry.getKey());
											}
										}
										
										dynamicTag.addContent(dynamicTagContent);
									} else {
										throw new RestfulClientException("Invalid data type for 'content' item. JsonObject expected");
									}
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'content'. JsonArray expected");
							}
						} else {
							throw new RestfulClientException("Unexpected property: " + entry.getKey());
						}
					}
					
					if (StringUtils.isBlank(dynamicTag.getDynName())) {
						throw new RestfulClientException("Missing value for property 'name'. String expected");
					}
				} else {
					throw new RestfulClientException("Invalid request data");
				}
			}
		}
		
		return dynamicTag;
	}

	@Override
	public ResponseType getResponseType() {
		return ResponseType.JSON;
	}
}
