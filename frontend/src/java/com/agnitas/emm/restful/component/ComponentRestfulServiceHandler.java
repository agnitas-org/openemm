/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingComponentType;
import org.agnitas.beans.impl.MailingComponentImpl;
import org.agnitas.emm.core.useractivitylog.dao.UserActivityLogDao;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingComponentDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTargetDao;
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
 * https:/<system.url>/restful/component
 */
public class ComponentRestfulServiceHandler implements RestfulServiceHandler {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(ComponentRestfulServiceHandler.class);
	
	public static final String NAMESPACE = "component";

	public static final Object EXPORTED_TO_STREAM = new Object();

	private UserActivityLogDao userActivityLogDao;
	private ComMailingDao mailingDao;
	private ComMailingComponentDao mailingComponentDao;
	private ComTargetDao targetDao;
	private ComCompanyDao companyDao;

	@Required
	public void setUserActivityLogDao(UserActivityLogDao userActivityLogDao) {
		this.userActivityLogDao = userActivityLogDao;
	}
	
	@Required
	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
	
	@Required
	public void setMailingComponentDao(ComMailingComponentDao mailingComponentDao) {
		this.mailingComponentDao = mailingComponentDao;
	}
	
	@Required
	public void setTargetDao(ComTargetDao targetDao) {
		this.targetDao = targetDao;
	}
	
	@Required
	public void setCompanyDao(ComCompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) throws Exception {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, ComAdmin admin, String requestDataFilePath, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(getComponent(request, response, admin)));
		} else if (requestMethod == RequestMethod.DELETE) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(deleteComponent(request, admin)));
		} else if (requestDataFilePath == null || new File(requestDataFilePath).length() <= 0) {
			restfulResponse.setError(new RestfulClientException("Missing request data"), ErrorCode.REQUEST_DATA_ERROR);
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createNewComponent(request, new File(requestDataFilePath), admin)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createOrUpdateComponent(request, new File(requestDataFilePath), admin)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	/**
	 * Return a single or multiple component data sets
	 * 
	 * @param request
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object getComponent(HttpServletRequest request, HttpServletResponse response, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_COMPONENTS_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_COMPONENTS_SHOW.toString() + "'");
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
			// Show all components of a mailing
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/component", new Date());
			userActivityLogDao.writeUserActivityLog(admin, "restful/component GET", "MID:" + mailingID + " ALL");
			
			JsonArray componentsJsonArray = new JsonArray();
			
			for (MailingComponent component : mailingComponentDao.getMailingComponents(mailingID, admin.getCompanyID())) {
				componentsJsonArray.add(createComponentJsonObject(component));
			}
			
			return componentsJsonArray;
		} else {
			// Export a single component of a mailing
			String requestedComponentKeyValue = restfulContext[1];
			
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/component", new Date());
			userActivityLogDao.writeUserActivityLog(admin, "restful/component GET", "MID: " + mailingID + " " + requestedComponentKeyValue);
			
			MailingComponent component;
			
			if (AgnUtils.isNumber(requestedComponentKeyValue)) {
				int componentID = Integer.parseInt(requestedComponentKeyValue);
				component = mailingComponentDao.getMailingComponent(mailingID, componentID, admin.getCompanyID());
			} else {
				component = mailingComponentDao.getMailingComponentByName(mailingID, admin.getCompanyID(), requestedComponentKeyValue);
			}
			
			if (component != null) {
				return createComponentJsonObject(component);
			} else {
				throw new RestfulNoDataFoundException("No data found");
			}
		}
	}

	/**
	 * Delete a component
	 * 
	 * @param request
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object deleteComponent(HttpServletRequest request, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_COMPONENTS_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_COMPONENTS_CHANGE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 2, 2);
		
		if (!AgnUtils.isNumber(restfulContext[0])) {
			throw new RestfulClientException("Invalid MailingID: " + restfulContext[0]);
		}
		int mailingID = Integer.parseInt(restfulContext[0]);
		if (!mailingDao.exist(mailingID, admin.getCompanyID())) {
			throw new RestfulClientException("Invalid not existing MailingID: " + mailingID);
		}
		
		String requestedComponentKeyValue = restfulContext[1];
		
		userActivityLogDao.addAdminUseOfFeature(admin, "restful/component", new Date());
		userActivityLogDao.writeUserActivityLog(admin, "restful/component DELETE", "MID: " + mailingID + " " + requestedComponentKeyValue);
		
		MailingComponent component;
		
		if (AgnUtils.isNumber(requestedComponentKeyValue)) {
			component = mailingComponentDao.getMailingComponent(mailingID, Integer.parseInt(requestedComponentKeyValue), admin.getCompanyID());
		} else {
			component = mailingComponentDao.getMailingComponentByName(mailingID, admin.getCompanyID(), requestedComponentKeyValue);
		}
		
		if (component != null) {
			mailingComponentDao.deleteMailingComponent(component);
			return "1 component deleted";
		} else {
			throw new RestfulNoDataFoundException("No data found for deletion");
		}
	}

	/**
	 * Create a new component
	 * 
	 * @param request
	 * @param requestDataFile
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object createNewComponent(HttpServletRequest request, File requestDataFile, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_COMPONENTS_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_COMPONENTS_CHANGE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		
		if (!AgnUtils.isNumber(restfulContext[0])) {
			throw new RestfulClientException("Invalid MailingID: " + restfulContext[0]);
		}
		int mailingID = Integer.parseInt(restfulContext[0]);
		if (!mailingDao.exist(mailingID, admin.getCompanyID())) {
			throw new RestfulClientException("Invalid not existing MailingID: " + mailingID);
		}
				
		userActivityLogDao.addAdminUseOfFeature(admin, "restful/component", new Date());
		userActivityLogDao.writeUserActivityLog(admin, "restful/component POST", "MID: " + mailingID);
		
		MailingComponent newMailingComponent = parseComponentJsonObject(requestDataFile, admin);
		newMailingComponent.setMailingID(mailingID);
		
		MailingComponent existingMailingComponent = mailingComponentDao.getMailingComponentByName(mailingID, admin.getCompanyID(), newMailingComponent.getComponentName());
		
		if (existingMailingComponent != null) {
			throw new RestfulClientException("Component already exists: " + newMailingComponent.getComponentName());
		} else {
			mailingComponentDao.saveMailingComponent(newMailingComponent);
			return createComponentJsonObject(mailingComponentDao.getMailingComponent(mailingID, newMailingComponent.getId(), admin.getCompanyID()));
		}
	}

	/**
	 * Update an existing component
	 * 
	 * @param request
	 * @param requestDataFile
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object createOrUpdateComponent(HttpServletRequest request, File requestDataFile, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_COMPONENTS_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_COMPONENTS_CHANGE.toString() + "'");
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
			// Insert component to a mailing or update an existing component of a mailing by name
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/component", new Date());
			userActivityLogDao.writeUserActivityLog(admin, "restful/component PUT", "MID: " + mailingID);
			
			MailingComponent newMailingComponent = parseComponentJsonObject(requestDataFile, admin);
			newMailingComponent.setMailingID(mailingID);
			
			MailingComponent existingMailingComponent = mailingComponentDao.getMailingComponentByName(mailingID, admin.getCompanyID(), newMailingComponent.getComponentName());
			if (existingMailingComponent != null) {
				newMailingComponent.setId(existingMailingComponent.getId());
			}
			
			mailingComponentDao.saveMailingComponent(newMailingComponent);
			return createComponentJsonObject(mailingComponentDao.getMailingComponentByName(mailingID, admin.getCompanyID(), newMailingComponent.getComponentName()));
		} else {
			// Update component of a mailing by name or component_id
			String requestedComponentKeyValue = restfulContext[1];
			
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/component", new Date());
			userActivityLogDao.writeUserActivityLog(admin, "restful/component PUT", "MID: " + mailingID + " " + requestedComponentKeyValue);
			
			MailingComponent newMailingComponent = parseComponentJsonObject(requestDataFile, admin);
			newMailingComponent.setMailingID(mailingID);
			
			MailingComponent existingMailingComponent;
			if (AgnUtils.isNumber(requestedComponentKeyValue)) {
				int componentID = Integer.parseInt(requestedComponentKeyValue);
				existingMailingComponent = mailingComponentDao.getMailingComponent(mailingID, componentID, admin.getCompanyID());
				if (existingMailingComponent == null) {
					throw new RestfulClientException("Invalid non existing component_id: " + requestedComponentKeyValue);
				}
			} else {
				existingMailingComponent = mailingComponentDao.getMailingComponentByName(mailingID, admin.getCompanyID(), requestedComponentKeyValue);
				
				if (existingMailingComponent == null) {
					throw new RestfulClientException("Invalid non existing component name: " + requestedComponentKeyValue);
				}
			}
			
			newMailingComponent.setId(existingMailingComponent.getId());
			
			mailingComponentDao.saveMailingComponent(newMailingComponent);
			
			MailingComponent storedComponent = mailingComponentDao.getMailingComponent(mailingID, newMailingComponent.getId(), admin.getCompanyID());
			
			if (storedComponent != null) {
				return createComponentJsonObject(storedComponent);
			} else {
				throw new RestfulNoDataFoundException("No data found");
			}
		}
	}

	private JsonObject createComponentJsonObject(MailingComponent component) throws Exception {
		JsonObject componentJsonObject = new JsonObject();
		
		componentJsonObject.add("component_id", component.getId());
		componentJsonObject.add("name", component.getComponentName());
		componentJsonObject.add("description", component.getDescription());
		componentJsonObject.add("type", MailingComponentType.getMailingComponentTypeByCode(component.getType()).name());
		if (component.getTargetID() > 0) {
			componentJsonObject.add("target_id", component.getTargetID());
		}
		if (component.getUrlID() > 0) {
			componentJsonObject.add("url_id", component.getUrlID());
			componentJsonObject.add("url", component.getLink());
		}
		if (component.getEmmBlock() != null && component.getEmmBlock().length() > 0) {
			componentJsonObject.add("mimetype", component.getMimeType());
			componentJsonObject.add("emm_block", component.getEmmBlock().replace("\r\n", "\n").replace("\r", "\n"));
		} else if (component.getBinaryBlock() != null && component.getBinaryBlock().length > 0) {
			// Do not store empty 1-byte arrays (they are created by clone-mailing-method for no purpose)
			if (!(component.getBinaryBlock().length == 1 && component.getBinaryBlock()[0] == 0)) {
				componentJsonObject.add("mimetype", component.getMimeType());
				componentJsonObject.add("bin_block", AgnUtils.encodeZippedBase64(component.getBinaryBlock()));
			}
		}
		
		return componentJsonObject;
	}

	private MailingComponent parseComponentJsonObject(File requestDataFile, ComAdmin admin) throws Exception {
		MailingComponent mailingComponent = new MailingComponentImpl();
		mailingComponent.setCompanyID(admin.getCompanyID());
		
		try (InputStream inputStream = new FileInputStream(requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				JsonNode jsonNode = jsonReader.read();
				if (JsonDataType.OBJECT == jsonNode.getJsonDataType()) {
					JsonObject jsonObject = (JsonObject) jsonNode.getValue();
					boolean mimeTypePropertyFound = false;
					boolean mimeTypePropertyUsed = false;
					for (Entry<String, Object> entry : jsonObject.entrySet()) {
						if ("name".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof String) {
								mailingComponent.setComponentName((String) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'name'. String expected");
							}
						} else if ("description".equals(entry.getKey())) {
							if (entry.getValue() == null || entry.getValue() instanceof String) {
								mailingComponent.setDescription((String) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'description'. String expected");
							}
						} else if ("type".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof String) {
								try {
									mailingComponent.setType(MailingComponentType.getMailingComponentTypeByName((String) entry.getValue()).getCode());
								} catch (Exception e) {
									throw new RestfulClientException("Invalid value for 'type'");
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'type'. String expected");
							}
						} else if ("emm_block".equals(entry.getKey())) {
							if (mimeTypePropertyUsed) {
								throw new RestfulClientException("Invalid exclusive property 'emm_block' collides with 'bin_block'");
							} else if (entry.getValue() != null && entry.getValue() instanceof String) {
								String emmBlock = (String) entry.getValue();
								emmBlock = emmBlock.replace("[COMPANY_ID]", Integer.toString(admin.getCompanyID())).replace("[RDIR_DOMAIN]", companyDao.getRedirectDomain(admin.getCompanyID()));
								if (jsonObject.get("mimetype") != null  && jsonObject.get("mimetype") instanceof String) {
									mailingComponent.setEmmBlock(emmBlock, (String) jsonObject.get("mimetype"));
									mimeTypePropertyUsed = true;
								} else {
									throw new RestfulClientException("Invalid data type for 'mimetype'. String expected");
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'emm_block'. String expected");
							}
						} else if ("target_id".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof Integer) {
								int targetID = (Integer) entry.getValue();
								if (targetDao.getTarget(targetID, admin.getCompanyID()) == null) {
									throw new RestfulClientException("Invalid non existing value for 'target_id': " + targetID);
								} else {
									mailingComponent.setTargetID(targetID);
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'target_id'. Integer expected");
							}
						} else if ("url".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof String) {
								mailingComponent.setLink((String) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'url'. String expected");
							}
						} else if ("bin_block".equals(entry.getKey())) {
							if (mimeTypePropertyUsed) {
								throw new RestfulClientException("Invalid exclusive property 'bin_block' collides with 'emm_block'");
							} else if (entry.getValue() != null && entry.getValue() instanceof String) {
								byte[] data;
								try {
									data = AgnUtils.decodeZippedBase64((String) entry.getValue());
								} catch (IOException e) {
									throw new RestfulClientException("Invalid value for 'bin_block'. String(Base64) expected");
								}
								if (jsonObject.get("mimetype") != null && jsonObject.get("mimetype") instanceof String) {
									mailingComponent.setBinaryBlock(data, (String) jsonObject.get("mimetype"));
									mimeTypePropertyUsed = true;
								} else {
									throw new RestfulClientException("Invalid data type for 'mimetype'. String expected");
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'bin_block'. String(Base64) expected");
							}
						} else if ("mimetype".equals(entry.getKey())) {
							mimeTypePropertyFound = true;
						} else {
							throw new RestfulClientException("Unexpected property: " + entry.getKey());
						}
					}
					
					if (mimeTypePropertyFound && !mimeTypePropertyUsed) {
						throw new RestfulClientException("Unexpected property: mimetype");
					}
					
					if (StringUtils.isBlank(mailingComponent.getComponentName())) {
						throw new RestfulClientException("Missing value for property 'name'. String expected");
					} else if (mailingComponent.getType() <= 0) {
						throw new RestfulClientException("Missing value for property 'type'. String expected");
					}
				} else {
					throw new RestfulClientException("Invalid request data");
				}
			}
		}
		
		return mailingComponent;
	}

	@Override
	public ResponseType getResponseType() {
		return ResponseType.JSON;
	}
}
