/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.form;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.agnitas.service.UserFormExporter;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Admin;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.dao.UserFormDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.company.service.CompanyTokenService;
import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import com.agnitas.emm.restful.BaseRequestResponse;
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
import com.agnitas.userform.bean.UserForm;
import com.agnitas.userform.bean.impl.UserFormImpl;
import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;
import com.agnitas.userform.trackablelinks.bean.impl.ComTrackableUserFormLinkImpl;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This restful service is available at:
 * https://<system.url>/restful/form
 */
public class FormRestfulServiceHandler implements RestfulServiceHandler {
	
	public static final String NAMESPACE = "form";

	public static final Object EXPORTED_TO_STREAM = new Object();

	private RestfulUserActivityLogDao userActivityLogDao;
	private UserFormDao userFormDao;
	private UserFormExporter userFormExporter;
	private CompanyTokenService companyTokenService;

	@Required
	public void setUserActivityLogDao(RestfulUserActivityLogDao userActivityLogDao) {
		this.userActivityLogDao = userActivityLogDao;
	}
	
	@Required
	public void setUserFormDao(UserFormDao userFormDao) {
		this.userFormDao = userFormDao;
	}

	@Required
	public void setUserFormExporter(UserFormExporter userFormExporter) {
		this.userFormExporter = userFormExporter;
	}
	
	@Required
	public void setCompanyTokenService(CompanyTokenService companyTokenService) {
		this.companyTokenService = companyTokenService;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) throws Exception {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, Admin admin, byte[] requestData, File requestDataFile, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod, boolean extendedLogging) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			Object result = getUserForm(request, response, admin);
			if (result != null && result == EXPORTED_TO_STREAM) {
				restfulResponse.setExportedToStream(true);
			} else {
				((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(result));
			}
		} else if (requestMethod == RequestMethod.DELETE) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(deleteUserForm(request, admin)));
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createNewUserForm(request, requestData, requestDataFile, admin)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(updateUserForm(request, requestData, requestDataFile, admin)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	/**
	 * Return a single or multiple userform data sets
	 * 
	 */
	private Object getUserForm(HttpServletRequest request, HttpServletResponse response, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.FORMS_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.FORMS_SHOW.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 1);
		
		if (restfulContext.length == 0) {
			// Show all userforms
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/form", new Date());
			writeActivityLog("ALL", request, admin);

			JsonArray userFormJsonArray = new JsonArray();
			
			for (UserForm userForm : userFormDao.getUserForms(admin.getCompanyID())) {
				JsonObject userFormJsonObject = new JsonObject();
				userFormJsonObject.add("id", userForm.getId());
				userFormJsonObject.add("formname", userForm.getFormName());
				userFormJsonObject.add("description", userForm.getDescription());
				userFormJsonArray.add(userFormJsonObject);
			}
			
			return userFormJsonArray;
		} else if (restfulContext.length == 1) {
			// Export a single userform
			if (!admin.permissionAllowed(Permission.FORMS_EXPORT)) {
				throw new RestfulClientException("Authorization failed: Access denied '" + Permission.FORMS_EXPORT.toString() + "'");
			}
			
			String requestedUserFormKeyValue = restfulContext[0];
			
			if (!AgnUtils.isNumber(requestedUserFormKeyValue)) {
				throw new RestfulClientException("Invalid request");
			}

			userActivityLogDao.addAdminUseOfFeature(admin, "restful/form", new Date());
			writeActivityLog(requestedUserFormKeyValue, request, admin);

			int userFormID = Integer.parseInt(requestedUserFormKeyValue);
			
			if (userFormDao.existsUserForm(admin.getCompanyID(), userFormID)) {
				userFormExporter.exportUserFormToJson(admin.getCompanyID(), userFormID, response.getOutputStream(), false);
				return EXPORTED_TO_STREAM;
			} else {
				throw new RestfulNoDataFoundException("No data found");
			}
		} else {
			throw new RestfulClientException("Invalid request");
		}
	}

	/**
	 * Delete a userform
	 * 
	 */
	private Object deleteUserForm(HttpServletRequest request, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.FORMS_DELETE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.FORMS_DELETE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		
		if (!AgnUtils.isNumber(restfulContext[0])) {
			throw new RestfulClientException("Invalid request");
		}

		userActivityLogDao.addAdminUseOfFeature(admin, "restful/form", new Date());
		writeActivityLog(restfulContext[0], request, admin);

		int userFormID = Integer.parseInt(restfulContext[0]);
		
		if (userFormDao.existsUserForm(admin.getCompanyID(), userFormID)) {
			boolean success = userFormDao.deleteUserForm(userFormID, admin.getCompanyID());
			if (success) {
				return "1 form deleted";
			} else {
				throw new RestfulClientException("Cannot delete form: " + userFormID);
			}
		} else {
			throw new RestfulNoDataFoundException("No data found for deletion");
		}
	}

	/**
	 * Create a new userform
	 * 
	 */
	private Object createNewUserForm(HttpServletRequest request, byte[] requestData, File requestDataFile, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.FORMS_IMPORT)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.FORMS_IMPORT.toString() + "'");
		}
		
		RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 0);
		
		if ((requestData == null || requestData.length == 0) && (requestDataFile == null || requestDataFile.length() <= 0)) {
			throw new RestfulClientException("Missing request data");
		} else {
			UserForm userForm = new UserFormImpl();
			userForm.setCompanyID(admin.getCompanyID());
			
			try (InputStream inputStream = RestfulServiceHandler.getRequestDataStream(requestData, requestDataFile);
					Json5Reader jsonReader = new Json5Reader(inputStream)) {
				JsonNode jsonNode = jsonReader.read();
				if (JsonDataType.OBJECT == jsonNode.getJsonDataType()) {
					JsonObject jsonObject = (JsonObject) jsonNode.getValue();
					fillUserformObject(admin, userForm, jsonObject);
				} else {
					throw new RestfulClientException("Invalid request");
				}
				
				if (StringUtils.isBlank(userForm.getFormName())) {
					throw new RestfulClientException("Mandatory formname is missing or empty");
				} else if (userFormDao.getUserFormByName(userForm.getFormName(), userForm.getCompanyID()) != null) {
					throw new RestfulClientException("Form with name '" + userForm.getFormName() + "' already exists"); 
				} else if (userForm.getDescription() == null) {
					throw new RestfulClientException("Mandatory description is missing"); 
				}
				
				userFormDao.storeUserForm(userForm);
				
				return "1 form created";
			}
		}
	}

	/**
	 * Update an existing userform
	 * 
	 */
	private Object updateUserForm(HttpServletRequest request, byte[] requestData, File requestDataFile, Admin admin) throws Exception {
		if ((requestData == null || requestData.length == 0) && (requestDataFile == null || requestDataFile.length() <= 0)) {
			throw new RestfulClientException("Missing request data");
		}
		
		if (!admin.permissionAllowed(Permission.FORMS_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.FORMS_CHANGE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 1);
		
		Integer userFormID = null;
		if (restfulContext.length >=1) {
			if (!AgnUtils.isNumber(restfulContext[0])) {
				throw new RestfulClientException("Invalid request");
			} else {
				userFormID = Integer.parseInt(restfulContext[0]);
			}
		}

		UserForm userForm = null;
		String previousFormName = null;
		if (userFormID != null) {
			userForm = userFormDao.getUserForm(userFormID, admin.getCompanyID());
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/form", new Date());
			writeActivityLog(restfulContext[0], request, admin);
			if (userForm == null) {
				throw new RestfulNoDataFoundException("No data found");
			} else {
				previousFormName = userForm.getFormName();
			}
		} else {
			userForm = new UserFormImpl();
			userForm.setCompanyID(admin.getCompanyID());
		}
			
		try (InputStream inputStream = RestfulServiceHandler.getRequestDataStream(requestData, requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				JsonNode jsonNode = jsonReader.read();
				if (JsonDataType.OBJECT == jsonNode.getJsonDataType()) {
					JsonObject jsonObject = (JsonObject) jsonNode.getValue();
					fillUserformObject(admin, userForm, jsonObject);
					
					if (StringUtils.isBlank(userForm.getFormName())) {
						throw new RestfulClientException("Mandatory formname is missing or empty");
					} else if (previousFormName != null && !userForm.getFormName().equals(previousFormName) && userFormDao.getUserFormByName(userForm.getFormName(), userForm.getCompanyID()) != null) {
						throw new RestfulClientException("Form with name '" + userForm.getFormName() + "' already exists"); 
					} else if (userForm.getDescription() == null) {
						throw new RestfulClientException("Mandatory description is missing"); 
					}
					
					userFormDao.storeUserForm(userForm);
					
					return userFormID == null ? "1 form created" : "1 form updated";
				} else {
					throw new RestfulClientException("Invalid request");
				}
			}
		}
	}

	private void fillUserformObject(Admin admin, UserForm userForm, JsonObject jsonObject) throws RestfulClientException, Exception {
		Optional<String> companyTokenOptional = companyTokenService.getCompanyToken(admin.getCompanyID());
		String companyToken = companyTokenOptional.isPresent() ? companyTokenOptional.get() : null;
		
		for (Entry<String, Object> entry : jsonObject.entrySet()) {
			if ("formname".equals(entry.getKey()) || "name".equals(entry.getKey())) {
				if (entry.getValue() != null && entry.getValue() instanceof String) {
					userForm.setFormName((String) entry.getValue());
					// Check for unallowed html tags
					try {
						HtmlChecker.checkForNoHtmlTags(userForm.getFormName());
					} catch(@SuppressWarnings("unused") final HtmlCheckerException e) {
						throw new RestfulClientException("Userform name contains unallowed HTML tags");
					}
				} else {
					throw new RestfulClientException("Invalid data type for 'formname'. String expected");
				}
			} else if ("description".equals(entry.getKey())) {
				if (entry.getValue() instanceof String) {
					userForm.setDescription((String) entry.getValue());
					// Check for unallowed html tags
					try {
						HtmlChecker.checkForUnallowedHtmlTags(userForm.getDescription(), false);
					} catch(@SuppressWarnings("unused") final HtmlCheckerException e) {
						throw new RestfulClientException("Userform description contains unallowed HTML tags");
					}
				} else {
					throw new RestfulClientException("Invalid data type for 'description'. String expected");
				}
			} else if ("startaction".equals(entry.getKey())) {
				if (entry.getValue() != null && entry.getValue() instanceof Integer) {
					int openActionId;
					try {
						openActionId = (Integer) jsonObject.get("startaction");
					} catch (Exception e) {
						throw new RestfulClientException("Invalid value for 'startaction': " + entry.getValue());
					}
					userForm.setStartActionID(openActionId);
				} else {
					throw new RestfulClientException("Invalid data type for 'start_action_id'. Integer expected");
				}
			} else if ("endaction".equals(entry.getKey())) {
				if (entry.getValue() != null && entry.getValue() instanceof Integer) {
					int clickActionId;
					try {
						clickActionId = (Integer) jsonObject.get("endaction");
					} catch (Exception e) {
						throw new RestfulClientException("Invalid value for 'endaction': " + entry.getValue());
					}
					userForm.setEndActionID(clickActionId);
				} else {
					throw new RestfulClientException("Invalid data type for 'end_action_id'. Integer expected");
				}
			} else if ("success_template".equals(entry.getKey())) {
				if (entry.getValue() instanceof String) {
					userForm.setSuccessTemplate((String) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'success_template'. String expected");
				}
			} else if ("success_url".equals(entry.getKey())) {
				if (entry.getValue() instanceof String) {
					userForm.setSuccessUrl((String) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'success_url'. String expected");
				}
			} else if ("success_use_url".equals(entry.getKey())) {
				if (entry.getValue() instanceof Boolean) {
					userForm.setSuccessUseUrl((Boolean) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'success_use_url'. Boolean expected");
				}
			} else if ("success_mimetype".equals(entry.getKey())) {
				if (entry.getValue() instanceof String) {
					userForm.setSuccessMimetype((String) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'success_mimetype'. String expected");
				}
			} else if ("error_template".equals(entry.getKey())) {
				if (entry.getValue() instanceof String) {
					userForm.setErrorTemplate((String) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'error_template'. String expected");
				}
			} else if ("error_url".equals(entry.getKey())) {
				if (entry.getValue() instanceof String) {
					userForm.setErrorUrl((String) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'error_url'. String expected");
				}
			} else if ("error_use_url".equals(entry.getKey())) {
				if (entry.getValue() instanceof Boolean) {
					userForm.setErrorUseUrl((Boolean) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'error_use_url'. Boolean expected");
				}
			} else if ("error_mimetype".equals(entry.getKey())) {
				if (entry.getValue() instanceof String) {
					userForm.setErrorMimetype((String) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'error_mimetype'. String expected");
				}
			} else if ("active".equals(entry.getKey())) {
				if (entry.getValue() instanceof Boolean) {
					userForm.setActive((Boolean) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'active'. Boolean expected");
				}
			} else if ("links".equals(entry.getKey())) {
				Map<String, ComTrackableUserFormLink> trackableLinks = new HashMap<>();
				for (Object linkObject : (JsonArray) jsonObject.get("links")) {
					JsonObject linkJsonObject = (JsonObject) linkObject;
					ComTrackableUserFormLink trackableLink = new ComTrackableUserFormLinkImpl();
					trackableLink.setShortname((String) linkJsonObject.get("name"));
					String fullUrl = (String) linkJsonObject.get("url");
					fullUrl = fullUrl.replace("[COMPANY_ID]", Integer.toString(admin.getCompanyID())).replace("[RDIR_DOMAIN]", admin.getCompany().getRdirDomain());
					if (StringUtils.isNotBlank(companyToken)) {
						fullUrl = fullUrl.replace("[CTOKEN]", companyToken);
					} else {
						fullUrl = fullUrl.replace("agnCTOKEN=[CTOKEN]", "agnCI=" + admin.getCompanyID());
					}
					trackableLink.setFullUrl(fullUrl);

					if (linkJsonObject.containsPropertyKey("deep_tracking")) {
						trackableLink.setDeepTracking((Integer) linkJsonObject.get("deep_tracking"));
					}

					if (linkJsonObject.containsPropertyKey("usage")) {
						trackableLink.setUsage((Integer) linkJsonObject.get("usage"));
					}

					if (linkJsonObject.containsPropertyKey("action_id")) {
						trackableLink.setActionID((Integer) linkJsonObject.get("action_id"));
					}

					if (linkJsonObject.containsPropertyKey("properties")) {
						List<LinkProperty> linkProperties = new ArrayList<>();
						for (Object propertyObject : (JsonArray) linkJsonObject.get("properties")) {
							JsonObject propertyJsonObject = (JsonObject) propertyObject;
							String propertyName = (String) propertyJsonObject.get("name");
							if (propertyName == null) {
								propertyName = "";
							}
							String propertyValue = (String) propertyJsonObject.get("value");
							if (propertyValue == null) {
								propertyValue = "";
							}
							LinkProperty linkProperty = new LinkProperty(PropertyType.parseString((String) propertyJsonObject.get("type")), propertyName, propertyName);
							linkProperties.add(linkProperty);
						}
						trackableLink.setProperties(linkProperties);
					}

					trackableLinks.put(trackableLink.getFullUrl(), trackableLink);
				}
				userForm.setTrackableLinks(trackableLinks);
			} else {
				throw new RestfulClientException("Invalid property '" + entry.getKey() + "' for form");
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
