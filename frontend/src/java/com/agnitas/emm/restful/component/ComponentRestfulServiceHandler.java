/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.component;

import static com.agnitas.beans.impl.MailingComponentImpl.COMPONENT_NAME_MAX_LENGTH;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Optional;

import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.MailingComponentType;
import com.agnitas.beans.impl.MailingComponentImpl;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.HttpUtils.RequestMethod;
import org.apache.commons.lang3.StringUtils;
import com.agnitas.beans.Admin;
import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.MailingComponentDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.dao.TargetDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.company.service.CompanyTokenService;
import com.agnitas.emm.core.thumbnails.service.ThumbnailService;
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
 * https://<system.url>/restful/component
 */
public class ComponentRestfulServiceHandler implements RestfulServiceHandler {
	
	public static final String NAMESPACE = "component";

	public static final Object EXPORTED_TO_STREAM = new Object();

	private RestfulUserActivityLogDao userActivityLogDao;
	private MailingDao mailingDao;
	private MailingComponentDao mailingComponentDao;
	private TargetDao targetDao;
	private CompanyDao companyDao;
	private ThumbnailService thumbnailService;
	private CompanyTokenService companyTokenService;

	public void setUserActivityLogDao(RestfulUserActivityLogDao userActivityLogDao) {
		this.userActivityLogDao = userActivityLogDao;
	}
	
	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
	
	public void setMailingComponentDao(MailingComponentDao mailingComponentDao) {
		this.mailingComponentDao = mailingComponentDao;
	}
	
	public void setTargetDao(TargetDao targetDao) {
		this.targetDao = targetDao;
	}
	
	public void setCompanyDao(CompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	public void setThumbnailService(ThumbnailService thumbnailService) {
		this.thumbnailService = thumbnailService;
	}

	public void setCompanyTokenService(CompanyTokenService companyTokenService) {
		this.companyTokenService = companyTokenService;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, Admin admin, byte[] requestData, File requestDataFile, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod, boolean extendedLogging) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(getComponent(request, response, admin)));
		} else if (requestMethod == RequestMethod.DELETE) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(deleteComponent(request, admin)));
		} else if ((requestData == null || requestData.length == 0) && (requestDataFile == null || requestDataFile.length() <= 0)) {
			restfulResponse.setError(new RestfulClientException("Missing request data"), ErrorCode.REQUEST_DATA_ERROR);
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createNewComponent(request, requestData, requestDataFile, admin)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createOrUpdateComponent(request, requestData, requestDataFile, admin)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	/**
	 * Return a single or multiple component data sets
	 * 
	 */
	private Object getComponent(HttpServletRequest request, HttpServletResponse response, Admin admin) throws Exception {
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
			writeActivityLog("MID:" + mailingID + " ALL", request, admin);

			JsonArray componentsJsonArray = new JsonArray();
			
			for (MailingComponent component : mailingComponentDao.getMailingComponents(mailingID, admin.getCompanyID())) {
				componentsJsonArray.add(createComponentJsonObject(component));
			}
			
			return componentsJsonArray;
		} else {
			// Export a single component of a mailing
			String requestedComponentKeyValue = restfulContext[1];
			
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/component", new Date());
			writeActivityLog("MID: " + mailingID + " " + requestedComponentKeyValue, request, admin);

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
	 */
	private Object deleteComponent(HttpServletRequest request, Admin admin) throws Exception {
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
		writeActivityLog("MID: " + mailingID + " " + requestedComponentKeyValue, request, admin);

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
	 */
	private Object createNewComponent(HttpServletRequest request, byte[] requestData, File requestDataFile, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_COMPONENTS_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_COMPONENTS_CHANGE + "'");
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
		writeActivityLog("MID: " + mailingID, request, admin);

		MailingComponent newMailingComponent = parseComponentJsonObject(requestData, requestDataFile, admin);
		newMailingComponent.setMailingID(mailingID);
		
		MailingComponent existingMailingComponent = mailingComponentDao.getMailingComponentByName(mailingID, admin.getCompanyID(), newMailingComponent.getComponentName());
		
		if (existingMailingComponent != null) {
			throw new RestfulClientException("Component already exists: " + newMailingComponent.getComponentName());
		} else {
			mailingComponentDao.saveMailingComponent(newMailingComponent);
			thumbnailService.updateMailingThumbnailByWebservice(admin.getCompanyID(), mailingID);
			return createComponentJsonObject(mailingComponentDao.getMailingComponent(mailingID, newMailingComponent.getId(), admin.getCompanyID()));
		}
	}

	/**
	 * Update an existing component
	 * 
	 */
	private Object createOrUpdateComponent(HttpServletRequest request, byte[] requestData, File requestDataFile, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_COMPONENTS_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_COMPONENTS_CHANGE + "'");
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
			writeActivityLog("MID: " + mailingID, request, admin);

			MailingComponent newMailingComponent = parseComponentJsonObject(requestData,  requestDataFile, admin);
			newMailingComponent.setMailingID(mailingID);
			
			MailingComponent existingMailingComponent = mailingComponentDao.getMailingComponentByName(mailingID, admin.getCompanyID(), newMailingComponent.getComponentName());
			if (existingMailingComponent != null) {
				newMailingComponent.setId(existingMailingComponent.getId());
			}
			
			mailingComponentDao.saveMailingComponent(newMailingComponent);
			thumbnailService.updateMailingThumbnailByWebservice(admin.getCompanyID(), mailingID);
			return createComponentJsonObject(mailingComponentDao.getMailingComponentByName(mailingID, admin.getCompanyID(), newMailingComponent.getComponentName()));
		} else {
			// Update component of a mailing by name or component_id
			String requestedComponentKeyValue = restfulContext[1];
			
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/component", new Date());
			writeActivityLog("MID: " + mailingID + " " + requestedComponentKeyValue, request, admin);

			MailingComponent newMailingComponent = parseComponentJsonObject(requestData, requestDataFile, admin);
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
			
			thumbnailService.updateMailingThumbnailByWebservice(admin.getCompanyID(), mailingID);
			
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
		componentJsonObject.add("type", component.getType().getCode());
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

	private MailingComponent parseComponentJsonObject(byte[] requestData, File requestDataFile, Admin admin) throws Exception {
		MailingComponent mailingComponent = new MailingComponentImpl();
		mailingComponent.setCompanyID(admin.getCompanyID());

		Optional<String> companyTokenOptional = companyTokenService.getCompanyToken(admin.getCompanyID());
		String companyToken = companyTokenOptional.isPresent() ? companyTokenOptional.get() : null;
		
		try (InputStream inputStream = RestfulServiceHandler.getRequestDataStream(requestData, requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				JsonNode jsonNode = jsonReader.read();
				if (JsonDataType.OBJECT == jsonNode.getJsonDataType()) {
					JsonObject jsonObject = (JsonObject) jsonNode.getValue();
					boolean mimeTypePropertyFound = false;
					boolean mimeTypePropertyUsed = false;
					for (Entry<String, Object> entry : jsonObject.entrySet()) {
						if ("name".equals(entry.getKey())) {
							if (!(entry.getValue() instanceof String)) {
                                throw new RestfulClientException("Invalid data type for 'name'. String expected");
                            } else if (StringUtils.length(((String) entry.getValue())) > COMPONENT_NAME_MAX_LENGTH) {
                                throw new RestfulClientException("Invalid value for 'name'. Max length = " + COMPONENT_NAME_MAX_LENGTH);
                            }
                            mailingComponent.setComponentName((String) entry.getValue());
							// Check for unallowed html tags
							try {
								HtmlChecker.checkForNoHtmlTags(mailingComponent.getComponentName());
							} catch(final HtmlCheckerException e) {
								throw new RestfulClientException("Component name contains unallowed HTML tags", e);
							}
						} else if ("description".equals(entry.getKey())) {
							if (entry.getValue() == null || entry.getValue() instanceof String) {
								mailingComponent.setDescription((String) entry.getValue());
								// Check for unallowed html tags
								try {
									HtmlChecker.checkForUnallowedHtmlTags(mailingComponent.getDescription(), false);
								} catch(final HtmlCheckerException e) {
									throw new RestfulClientException("Component description contains unallowed HTML tags", e);
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'description'. String expected");
							}
						} else if ("type".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof String) {
								try {
									mailingComponent.setType(MailingComponentType.getMailingComponentTypeByName((String) entry.getValue()));
								} catch (Exception e) {
									throw new RestfulClientException("Invalid value for 'type'");
								}
							} else if (entry.getValue() != null && entry.getValue() instanceof Integer) {
								try {
									mailingComponent.setType(MailingComponentType.getMailingComponentTypeByCode((Integer) entry.getValue()));
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
								if (StringUtils.isNotBlank(companyToken)) {
									emmBlock = emmBlock.replace("[CTOKEN]", companyToken);
								} else {
									emmBlock = emmBlock.replace("agnCTOKEN=[CTOKEN]", "agnCI=" + admin.getCompanyID());
								}
								// Check for unallowed html tags
								try {
									HtmlChecker.checkForUnallowedHtmlTags(emmBlock, true);
								} catch(final HtmlCheckerException e) {
									throw new RestfulClientException("Mailing component contains unallowed HTML tags", e);
								}
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
								if (data.length == 0 && ((String) entry.getValue()).length() > 0) {
									// Data was not zipped before base64, so AgnUtils.decodeZippedBase64 returned an empty byte[]
									// Gracefully allow unzipped base64 in that case
									data = AgnUtils.decodeBase64((String) entry.getValue());
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
					} else if (mailingComponent.getType() == null) {
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

	private void writeActivityLog(String description, HttpServletRequest request, Admin admin) {
		writeActivityLog(userActivityLogDao, description, request, admin);
	}
}
