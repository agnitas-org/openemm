/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.target;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;

import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.TargetLight;
import com.agnitas.beans.impl.ComTargetImpl;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.target.service.ComTargetService;
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
 * https://<system.url>/restful/target
 */
public class TargetRestfulServiceHandler implements RestfulServiceHandler {
	public static final String NAMESPACE = "target";

	protected RestfulUserActivityLogDao userActivityLogDao;
	protected ComTargetService targetService;
	protected ComTargetDao targetDao;
	protected ComRecipientDao recipientDao;
	protected ColumnInfoService columnInfoService;

	@Required
	public void setUserActivityLogDao(RestfulUserActivityLogDao userActivityLogDao) {
		this.userActivityLogDao = userActivityLogDao;
	}
	
	@Required
	public void setTargetService(ComTargetService targetService) {
		this.targetService = targetService;
	}
	
	@Required
	public void setTargetDao(ComTargetDao targetDao) {
		this.targetDao = targetDao;
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
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(getTarget(request, response, admin)));
		} else if (requestMethod == RequestMethod.DELETE) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(deleteTarget(request, admin)));
		} else if ((requestData == null || requestData.length == 0) && (requestDataFile == null || requestDataFile.length() <= 0)) {
			restfulResponse.setError(new RestfulClientException("Missing request data"), ErrorCode.REQUEST_DATA_ERROR);
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createNewTarget(request, requestData, requestDataFile, admin)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createOrUpdateTarget(request, requestData, requestDataFile, admin)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	/**
	 * Return a single or multiple target data sets
	 * 
	 */
	protected Object getTarget(HttpServletRequest request, HttpServletResponse response, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.TARGETS_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.TARGETS_SHOW.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 2);
		
		if (restfulContext.length == 0) {
			// Show index of all targets
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/target", new Date());
			writeActivityLog("ALL", request, admin);

			JsonArray targetsJsonArray = new JsonArray();
			
			for (TargetLight target : targetDao.getTargetLights(admin.getCompanyID())) {
				JsonObject targetJsonObject = new JsonObject();
				targetJsonObject.add("target_id", target.getId());
				targetJsonObject.add("name", target.getTargetName());
				if (StringUtils.isNotBlank(target.getTargetDescription())) {
					targetJsonObject.add("description", target.getTargetDescription());
				}
				targetsJsonArray.add(targetJsonObject);
			}
			
			return targetsJsonArray;
		} else {
			// Show single target
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/target", new Date());
			writeActivityLog(restfulContext[0], request, admin);

			ComTarget target;
			if (AgnUtils.isNumber(restfulContext[0])) {
				int targetID = Integer.parseInt(restfulContext[0]);
				target = targetDao.getTarget(targetID, admin.getCompanyID());
			} else {
				target = targetDao.getTargetByName(restfulContext[0], admin.getCompanyID());
			}
			
			boolean showTargetRecipients = false;
			if (restfulContext.length == 2) {
				if ("recipients".equalsIgnoreCase(restfulContext[1])) {
					showTargetRecipients = true;
				} else {
					throw new RestfulClientException("Invalid requestcontext: " + restfulContext[1]);
				}
			}
			
			if (target != null) {
				JsonObject targetJsonObject = getTargetJsonObject(admin, target);
				if (showTargetRecipients) {
					String targetSql = target.getTargetSQL();
					
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
					List<CaseInsensitiveMap<String, Object>> recipients = recipientDao.getTargetRecipients(admin.getCompanyID(), targetSql, profileFieldsToShow, TimeZone.getTimeZone(admin.getAdminTimezone()));
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
					targetJsonObject.add("recipients", recipientsArray);
				}
				return targetJsonObject;
			} else {
				throw new RestfulNoDataFoundException("No data found");
			}
		}
	}

	/**
	 * Delete a target
	 * 
	 */
	private Object deleteTarget(HttpServletRequest request, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.TARGETS_DELETE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.TARGETS_DELETE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		
		userActivityLogDao.addAdminUseOfFeature(admin, "restful/target", new Date());
		writeActivityLog(restfulContext[0], request, admin);

		ComTarget target;
		if (AgnUtils.isNumber(restfulContext[0])) {
			int targetID = Integer.parseInt(restfulContext[0]);
			target = targetDao.getTarget(targetID, admin.getCompanyID());
		} else {
			target = targetDao.getTargetByName(restfulContext[0], admin.getCompanyID());
		}
		
		boolean success = false;
		if (target != null) {
			success = targetDao.deleteTarget(Integer.parseInt(restfulContext[0]), admin.getCompanyID());
		} else {
			throw new RestfulNoDataFoundException("No data found for deletion");
		}
		
		if (success) {
			return "1 target deleted";
		} else {
			throw new RestfulNoDataFoundException("No data found for deletion");
		}
	}

	/**
	 * Create a new target
	 * 
	 */
	protected Object createNewTarget(HttpServletRequest request, byte[] requestData, File requestDataFile, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.TARGETS_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.TARGETS_CHANGE.toString() + "'");
		}
		
		RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 0);
		
		try (InputStream inputStream = RestfulServiceHandler.getRequestDataStream(requestData, requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				JsonNode jsonNode = jsonReader.read();
				if (JsonDataType.OBJECT == jsonNode.getJsonDataType()) {
					JsonObject jsonObject = (JsonObject) jsonNode.getValue();
					ComTarget target = new ComTargetImpl();
					target.setCompanyID(admin.getCompanyID());
					for (Entry<String, Object> entry : jsonObject.entrySet()) {
						if ("name".equals(entry.getKey())) {
							if (entry.getValue() instanceof String) {
								target.setTargetName((String) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'name'. String expected");
							}
						} else if ("description".equals(entry.getKey())) {
							if (entry.getValue() instanceof String) {
								target.setTargetDescription((String) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'description'. String expected");
							}
						} else if ("sql".equals(entry.getKey())) {
							throw new RestfulClientException("Invalid data type for 'sql'. Only 'eql' is allowed to be set.");
						} else if ("eql".equals(entry.getKey())) {
							if (entry.getValue() instanceof String) {
								String eql = (String) entry.getValue();
								if (!targetDao.isValidEql(admin.getCompanyID(), eql)) {
									throw new RestfulClientException("Invalid eql data for 'eql': " + eql);
								} else {
									target.setEQL(eql);
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'eql'. String expected");
							}
						} else if (handleExtendedAttribute(admin, target, entry.getKey(), entry.getValue())) {
							// This value was handled as an extended attribute
						} else {
							throw new RestfulClientException("Invalid property '" + entry.getKey() + "' for target");
						}
					}
					
					if (StringUtils.isBlank(target.getTargetName())) {
						throw new RestfulClientException("Missing mandatory value for property value for 'name'");
					} else if (StringUtils.isBlank(target.getEQL()) ) {
						throw new RestfulClientException("Missing mandatory value for property value for 'eql'");
					} else {
						ComTarget targetItem = targetDao.getTargetByName(target.getTargetName(), admin.getCompanyID());
						if (targetItem != null) {
							throw new RestfulClientException("Target with name '" + target.getTargetName() + "' already exists");
						}
						
						targetDao.saveTarget(target);
						
						target = targetDao.getTarget(target.getId(), admin.getCompanyID());
						
						if (target != null) {
							userActivityLogDao.addAdminUseOfFeature(admin, "restful/target", new Date());
							writeActivityLog(String.valueOf(target.getId()), request, admin);

							return getTargetJsonObject(admin, target);
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
	 * Update an existing target
	 * 
	 */
	protected Object createOrUpdateTarget(HttpServletRequest request, byte[] requestData, File requestDataFile, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.TARGETS_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.TARGETS_CHANGE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 1);
		
		try (InputStream inputStream = RestfulServiceHandler.getRequestDataStream(requestData, requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				JsonNode jsonNode = jsonReader.read();
				if (JsonDataType.OBJECT == jsonNode.getJsonDataType()) {
					JsonObject jsonObject = (JsonObject) jsonNode.getValue();
					ComTarget target;
					if (restfulContext.length == 1) {
						if (AgnUtils.isNumber(restfulContext[0])) {
							int targetID = Integer.parseInt(restfulContext[0]);
							target = targetDao.getTarget(targetID, admin.getCompanyID());
						} else {
							target = targetDao.getTargetByName(restfulContext[0], admin.getCompanyID());
						}
						
						if (target == null) {
							throw new RestfulNoDataFoundException("No data found for update");
						}
					} else {
						target = new ComTargetImpl();
						target.setCompanyID(admin.getCompanyID());
					}
					
					for (Entry<String, Object> entry : jsonObject.entrySet()) {
						if ("name".equals(entry.getKey())) {
							if (entry.getValue() instanceof String) {
								target.setTargetName((String) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'name'. String expected");
							}
						} else if ("description".equals(entry.getKey())) {
							if (entry.getValue() instanceof String) {
								target.setTargetDescription((String) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'description'. String expected");
							}
						} else if ("sql".equals(entry.getKey())) {
							throw new RestfulClientException("Invalid data type for 'sql'. Only 'eql' is allowed to be set.");
						} else if ("eql".equals(entry.getKey())) {
							if (entry.getValue() instanceof String) {
								String eql = (String) entry.getValue();
								if (!targetDao.isValidEql(admin.getCompanyID(), eql)) {
									throw new RestfulClientException("Invalid eql data for 'eql': " + eql);
								} else {
									target.setEQL(eql);
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'eql'. String expected");
							}
						} else if (handleExtendedAttribute(admin, target, entry.getKey(), entry.getValue())) {
							// This value was handled as an extended attribute
						} else {
							throw new RestfulClientException("Invalid property '" + entry.getKey() + "' for target");
						}
					}
					
					if (StringUtils.isBlank(target.getTargetName())) {
						throw new RestfulClientException("Missing mandatory value for property value for 'shortname'");
					} else if (StringUtils.isBlank(target.getEQL()) ) {
						throw new RestfulClientException("Missing mandatory value for property value for 'eql'");
					} else {
						if (restfulContext.length == 0) {
							ComTarget targetItem = targetDao.getTargetByName(target.getTargetName(), admin.getCompanyID());
							if (targetItem != null) {
								throw new RestfulClientException("Target with name '" + target.getTargetName() + "' already exists");
							}
						}
						
						targetDao.saveTarget(target);
						
						target = targetDao.getTarget(target.getId(), admin.getCompanyID());
						
						if (target != null) {
							userActivityLogDao.addAdminUseOfFeature(admin, "restful/target", new Date());
							writeActivityLog(String.valueOf(target.getId()), request, admin);

							return getTargetJsonObject(admin, target);
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

	protected JsonObject getTargetJsonObject(Admin admin, ComTarget target) {
		JsonObject targetJsonObject = new JsonObject();
		targetJsonObject.add("target_id", target.getId());
		targetJsonObject.add("name", target.getTargetName());
		if (StringUtils.isNotBlank(target.getTargetDescription())) {
			targetJsonObject.add("description", target.getTargetDescription());
		}
		targetJsonObject.add("sql", target.getTargetSQL());
		targetJsonObject.add("eql", target.getEQL());
		targetJsonObject.add("valid", target.isValid());
		targetJsonObject.add("creation_date", target.getCreationDate());
		targetJsonObject.add("change_date", target.getChangeDate());
		return targetJsonObject;
	}

	@SuppressWarnings("unused")
	protected boolean handleExtendedAttribute(Admin admin, ComTarget target, String key, Object value) throws RestfulClientException {
		return false;
	}

	@Override
	public ResponseType getResponseType() {
		return ResponseType.JSON;
	}

	private void writeActivityLog(String description, HttpServletRequest request, Admin admin) {
		writeActivityLog(userActivityLogDao, description, request, admin);
	}
}
