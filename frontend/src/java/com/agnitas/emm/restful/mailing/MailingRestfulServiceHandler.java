/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.mailing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.Mailinglist;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.useractivitylog.dao.UserActivityLogDao;
import org.agnitas.service.ImportResult;
import org.agnitas.service.MailingExporter;
import org.agnitas.service.MailingImporter;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.Mailing;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingType;
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
 * https://<system.url>/restful/mailing
 */
public class MailingRestfulServiceHandler implements RestfulServiceHandler {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(MailingRestfulServiceHandler.class);
	
	public static final String NAMESPACE = "mailing";

	public static final Object EXPORTED_TO_STREAM = new Object();

	private UserActivityLogDao userActivityLogDao;
	private ComMailingDao mailingDao;
	private MailinglistDao mailinglistDao;
	private MailingImporter mailingImporter;
	private MailingExporter mailingExporter;

	@Required
	public void setUserActivityLogDao(UserActivityLogDao userActivityLogDao) {
		this.userActivityLogDao = userActivityLogDao;
	}
	@Required
	public void setMailinglistDao(MailinglistDao mailinglistDao) {
		this.mailinglistDao = mailinglistDao;
	}
	
	@Required
	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	@Required
	public void setMailingImporter(MailingImporter mailingImporter) {
		this.mailingImporter = mailingImporter;
	}

	@Required
	public void setMailingExporter(MailingExporter mailingExporter) {
		this.mailingExporter = mailingExporter;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) throws Exception {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, ComAdmin admin, String requestDataFilePath, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			Object result = getMailing(request, response, admin);
			if (result != null && result == EXPORTED_TO_STREAM) {
				restfulResponse.setExportedToStream(true);
			} else {
				((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(result));
			}
		} else if (requestMethod == RequestMethod.DELETE) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(deleteMailing(request, admin)));
		} else if (requestDataFilePath == null || new File(requestDataFilePath).length() <= 0) {
			restfulResponse.setError(new RestfulClientException("Missing request data"), ErrorCode.REQUEST_DATA_ERROR);
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(importMailing(request, new File(requestDataFilePath), admin)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(updateMailing(request, new File(requestDataFilePath), admin)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	/**
	 * Return a single or multiple mailing data sets
	 * 
	 * @param request
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object getMailing(HttpServletRequest request, HttpServletResponse response, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_SHOW.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 1);
		
		if (restfulContext.length == 0) {
			// Show all mailings
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/mailing", new Date());
			userActivityLogDao.writeUserActivityLog(admin, "restful/mailing GET", "ALL");
			
			JsonArray mailingsJsonArray = new JsonArray();
			
			for (MailingType mailingType : MailingType.values()) {
				for (LightweightMailing mailing : mailingDao.getMailingsByType(mailingType.getCode(), admin.getCompanyID())) {
					JsonObject mailingJsonObject = new JsonObject();
					mailingJsonObject.add("mailing_id", mailing.getMailingID());
					mailingJsonObject.add("type", MailingType.fromCode(mailing.getMailingType()).name());
					mailingJsonObject.add("name", mailing.getShortname());
					mailingJsonObject.add("description", mailing.getMailingDescription());
					mailingsJsonArray.add(mailingJsonObject);
				}
			}
			
			return mailingsJsonArray;
		} else {
			// Export a single mailing
			if (!admin.permissionAllowed(Permission.MAILING_EXPORT)) {
				throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_EXPORT.toString() + "'");
			}
			
			String requestedMailingKeyValue = restfulContext[0];
			
			if (!AgnUtils.isNumber(requestedMailingKeyValue)) {
				throw new RestfulClientException("Invalid request");
			}

			userActivityLogDao.addAdminUseOfFeature(admin, "restful/mailing", new Date());
			userActivityLogDao.writeUserActivityLog(admin, "restful/mailing GET", requestedMailingKeyValue);
			
			int mailingID = Integer.parseInt(requestedMailingKeyValue);
			
			if (mailingDao.exist(mailingID, admin.getCompanyID())) {
				mailingExporter.exportMailingToJson(admin.getCompanyID(), mailingID, response.getOutputStream(), false);
				return EXPORTED_TO_STREAM;
			} else {
				throw new RestfulNoDataFoundException("No data found");
			}
		}
	}

	/**
	 * Delete a mailing
	 * 
	 * @param request
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object deleteMailing(HttpServletRequest request, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_DELETE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_DELETE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		
		if (!AgnUtils.isNumber(restfulContext[0])) {
			throw new RestfulClientException("Invalid request");
		}

		userActivityLogDao.addAdminUseOfFeature(admin, "restful/mailing", new Date());
		userActivityLogDao.writeUserActivityLog(admin, "restful/mailing DELETE", restfulContext[0]);
		
		int mailingID = Integer.parseInt(restfulContext[0]);
		
		if (mailingDao.exist(mailingID, admin.getCompanyID())) {
			boolean success = mailingDao.deleteMailing(mailingID, admin.getCompanyID());
			if (success) {
				return "1 mailing deleted";
			} else {
				throw new RestfulClientException("Cannot delete mailing: " + mailingID);
			}
		} else {
			throw new RestfulNoDataFoundException("No data found for deletion");
		}
	}

	/**
	 * Create a new mailing
	 * 
	 * @param request
	 * @param requestDataFile
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object importMailing(HttpServletRequest request, File requestDataFile, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_IMPORT)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_IMPORT.toString() + "'");
		}
		
		try (InputStream inputStream = new FileInputStream(requestDataFile)) {
			ImportResult result = mailingImporter.importMailingFromJson(admin.getCompanyID(), inputStream, false, null, null, true, false, true);
			if (result.isSuccess()) {
				LightweightMailing mailing = mailingDao.getLightweightMailing(admin.getCompanyID(), result.getMailingID());
				
				JsonObject returnJsonObject = new JsonObject();
				returnJsonObject.add("mailing_id", mailing.getMailingID());
				returnJsonObject.add("type", MailingType.fromCode(mailing.getMailingType()).name());
				returnJsonObject.add("name", mailing.getShortname());
				returnJsonObject.add("description", mailing.getMailingDescription());
				return returnJsonObject;
			} else {
				throw new RestfulClientException("Error while creating mailing: " + result.getErrors());
			}
		}
	}

	/**
	 * Update an existing mailing
	 * 
	 * @param request
	 * @param requestDataFile
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object updateMailing(HttpServletRequest request, File requestDataFile, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_CHANGE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		
		if (!AgnUtils.isNumber(restfulContext[0])) {
			throw new RestfulClientException("Invalid request");
		}

		userActivityLogDao.addAdminUseOfFeature(admin, "restful/mailing", new Date());
		userActivityLogDao.writeUserActivityLog(admin, "restful/mailing PUT", restfulContext[0]);
		
		int mailingID = Integer.parseInt(restfulContext[0]);
		
		Mailing mailing = mailingDao.getMailing(mailingID, admin.getCompanyID());
		if (mailing != null) {
			try (InputStream inputStream = new FileInputStream(requestDataFile)) {
				try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
					JsonNode jsonNode = jsonReader.read();
					if (JsonDataType.OBJECT == jsonNode.getJsonDataType()) {
						JsonObject jsonObject = (JsonObject) jsonNode.getValue();
						for (Entry<String, Object> entry : jsonObject.entrySet()) {
							if ("shortname".equals(entry.getKey())) {
								if (entry.getValue() != null && entry.getValue() instanceof String) {
									mailing.setShortname((String) entry.getValue());
								} else {
									throw new RestfulClientException("Invalid data type for 'shortname'. String expected");
								}
							} else if ("description".equals(entry.getKey())) {
								if (entry.getValue() instanceof String) {
									mailing.setDescription((String) entry.getValue());
								} else {
									throw new RestfulClientException("Invalid data type for 'description'. String expected");
								}
							} else if ("mailinglist_id".equals(entry.getKey())) {
								if (entry.getValue() != null && entry.getValue() instanceof Integer) {
									Mailinglist mailinglist = mailinglistDao.getMailinglist((Integer) entry.getValue(), admin.getCompanyID());
									if (mailinglist == null) {
										throw new RestfulClientException("Invalid value for 'mailinglist_id'. Mailinglist does not exist: " + entry.getValue());
									} else {
										mailing.setMailinglistID((Integer) entry.getValue());
									}
								} else {
									throw new RestfulClientException("Invalid data type for 'mailinglist_id'. Integer expected");
								}
							} else {
								throw new RestfulClientException("Invalid property '" + entry.getKey() + "' for mailing");
							}
						}
						
						mailingDao.saveMailing(mailing, true);
						
						return "1 mailing updated";
					} else {
						throw new RestfulClientException("Invalid request");
					}
				}
			}
		} else {
			throw new RestfulNoDataFoundException("No data found");
		}
	}

	@Override
	public ResponseType getResponseType() {
		return ResponseType.JSON;
	}
}
