/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.mailing;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import org.agnitas.beans.Mailinglist;
import org.agnitas.dao.MailingStatus;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.service.CopyMailingService;
import org.agnitas.service.ImportResult;
import org.agnitas.service.MailingExporter;
import org.agnitas.service.MailingImporter;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingContentType;
import com.agnitas.beans.impl.ComTrackableLinkImpl;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.mailing.bean.ComMailingParameter;
import com.agnitas.emm.core.thumbnails.service.ThumbnailService;
import com.agnitas.emm.restful.BaseRequestResponse;
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
 * https://<system.url>/restful/mailing
 */
public class MailingRestfulServiceHandler implements RestfulServiceHandler {
	
	public static final String NAMESPACE = "mailing";

	public static final Object EXPORTED_TO_STREAM = new Object();

	private RestfulUserActivityLogDao userActivityLogDao;
	private ComMailingDao mailingDao;
	private MailinglistDao mailinglistDao;
	private MailingImporter mailingImporter;
	private MailingExporter mailingExporter;
	private CopyMailingService copyMailingService;
	private ThumbnailService thumbnailService;
	private ComMailingBaseService mailingBaseService;

	@Required
	public void setUserActivityLogDao(RestfulUserActivityLogDao userActivityLogDao) {
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

	@Required
	public void setCopyMailingService(CopyMailingService copyMailingService) {
		this.copyMailingService = copyMailingService;
	}

	@Required
	public void setThumbnailService(ThumbnailService thumbnailService) {
		this.thumbnailService = thumbnailService;
	}

	@Required
	public void setMailingBaseService(ComMailingBaseService mailingBaseService) {
		this.mailingBaseService = mailingBaseService;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) throws Exception {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, Admin admin, byte[] requestData, File requestDataFile, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod, boolean extendedLogging) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			Object result = getMailing(request, response, admin);
			if (result != null && result == EXPORTED_TO_STREAM) {
				restfulResponse.setExportedToStream(true);
			} else {
				((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(result));
			}
		} else if (requestMethod == RequestMethod.DELETE) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(deleteMailing(request, admin)));
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createNewMailing(request, requestData, requestDataFile, admin)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(updateMailing(request, requestData, requestDataFile, admin)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	/**
	 * Return a single or multiple mailing data sets
	 * 
	 */
	private Object getMailing(HttpServletRequest request, HttpServletResponse response, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_SHOW.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 2);
		
		if (restfulContext.length == 0) {
			// Show all mailings
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/mailing", new Date());
			writeActivityLog("ALL", request, admin);

			JsonArray mailingsJsonArray = new JsonArray();
			
			for (MailingType mailingType : MailingType.values()) {
				for (LightweightMailing mailing : mailingDao.getMailingsByType(mailingType.getCode(), admin.getCompanyID())) {
					JsonObject mailingJsonObject = new JsonObject();
					mailingJsonObject.add("mailing_id", mailing.getMailingID());
					mailingJsonObject.add("type", mailing.getMailingType().name());
					mailingJsonObject.add("name", mailing.getShortname());
					mailingJsonObject.add("description", mailing.getMailingDescription());
					mailingsJsonArray.add(mailingJsonObject);
				}
			}
			
			return mailingsJsonArray;
		} else if (restfulContext.length == 1) {
			// Export a single mailing
			if (!admin.permissionAllowed(Permission.MAILING_EXPORT)) {
				throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_EXPORT.toString() + "'");
			}
			
			String requestedMailingKeyValue = restfulContext[0];
			
			if (!AgnUtils.isNumber(requestedMailingKeyValue)) {
				throw new RestfulClientException("Invalid request");
			}

			userActivityLogDao.addAdminUseOfFeature(admin, "restful/mailing", new Date());
			writeActivityLog(requestedMailingKeyValue, request, admin);

			int mailingID = Integer.parseInt(requestedMailingKeyValue);
			
			if (mailingDao.exist(mailingID, admin.getCompanyID())) {
				mailingExporter.exportMailingToJson(admin.getCompanyID(), mailingID, response.getOutputStream(), false);
				return EXPORTED_TO_STREAM;
			} else {
				throw new RestfulNoDataFoundException("No data found");
			}
		} else if (restfulContext.length == 2) {
			if (AgnUtils.isNumber(restfulContext[0]) && "status".equalsIgnoreCase(restfulContext[1])) {
				int mailingID = Integer.parseInt(restfulContext[0]);
				String status = mailingDao.getWorkStatus(admin.getCompanyID(), mailingID);
				return MailingStatus.fromDbKey(status).name();
			} else {
				throw new RestfulClientException("Invalid request");
			}
		} else {
			throw new RestfulClientException("Invalid request");
		}
	}

	/**
	 * Delete a mailing
	 * 
	 */
	private Object deleteMailing(HttpServletRequest request, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_DELETE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_DELETE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		
		if (!AgnUtils.isNumber(restfulContext[0])) {
			throw new RestfulClientException("Invalid request");
		}

		userActivityLogDao.addAdminUseOfFeature(admin, "restful/mailing", new Date());
		writeActivityLog(restfulContext[0], request, admin);

		int mailingID = Integer.parseInt(restfulContext[0]);
		
		if (mailingDao.exist(mailingID, admin.getCompanyID())) {
			boolean success = mailingBaseService.deleteMailing(mailingID, admin.getCompanyID());
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
	 */
	private Object createNewMailing(HttpServletRequest request, byte[] requestData, File requestDataFile, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.MAILING_IMPORT)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_IMPORT.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 2);
		
		if (restfulContext.length == 0) {
			if ((requestData == null || requestData.length == 0) && (requestDataFile == null || requestDataFile.length() <= 0)) {
				throw new RestfulClientException("Missing request data");
			} else {
				try (InputStream inputStream = RestfulServiceHandler.getRequestDataStream(requestData, requestDataFile)) {
					ImportResult result = mailingImporter.importMailingFromJson(admin.getCompanyID(), inputStream, false, null, null, true, false, true);
					if (result.isSuccess()) {
						thumbnailService.updateMailingThumbnailByWebservice(admin.getCompanyID(), result.getMailingID());
						
						LightweightMailing mailing = mailingDao.getLightweightMailing(admin.getCompanyID(), result.getMailingID());
						
						JsonObject returnJsonObject = new JsonObject();
						returnJsonObject.add("mailing_id", mailing.getMailingID());
						returnJsonObject.add("type", mailing.getMailingType().name());
						returnJsonObject.add("name", mailing.getShortname());
						returnJsonObject.add("description", mailing.getMailingDescription());
						return returnJsonObject;
					} else {
						throw new RestfulClientException("Error while creating mailing: " + result.getErrors());
					}
				}
			}
		} else if (restfulContext.length == 2) {
			if (AgnUtils.isNumber(restfulContext[0]) && "copy".equalsIgnoreCase(restfulContext[1])) {
				int newMailingId = copyMailingService.copyMailing(admin.getCompanyID(), Integer.parseInt(restfulContext[0]), admin.getCompanyID(), null, null);
				thumbnailService.updateMailingThumbnailByWebservice(admin.getCompanyID(), newMailingId);
				JsonObject returnJsonObject = new JsonObject();
				returnJsonObject.add("mailing_id", newMailingId);
				return returnJsonObject;
			} else {
				throw new RestfulClientException("Invalid request");
			}
		} else {
			throw new RestfulClientException("Invalid request");
		}
	}

	/**
	 * Update an existing mailing
	 * 
	 */
	private Object updateMailing(HttpServletRequest request, byte[] requestData, File requestDataFile, Admin admin) throws Exception {
		if ((requestData == null || requestData.length == 0) && (requestDataFile == null || requestDataFile.length() <= 0)) {
			throw new RestfulClientException("Missing request data");
		}
		
		if (!admin.permissionAllowed(Permission.MAILING_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.MAILING_CHANGE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		
		if (!AgnUtils.isNumber(restfulContext[0])) {
			throw new RestfulClientException("Invalid request");
		}

		userActivityLogDao.addAdminUseOfFeature(admin, "restful/mailing", new Date());
		writeActivityLog(restfulContext[0], request, admin);

		int mailingID = Integer.parseInt(restfulContext[0]);
		
		Mailing mailing = mailingDao.getMailing(mailingID, admin.getCompanyID());
		if (mailing != null) {
			try (InputStream inputStream = RestfulServiceHandler.getRequestDataStream(requestData, requestDataFile)) {
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
								if (jsonObject.containsPropertyKey("mailinglist_shortname")) {
									throw new RestfulClientException("Invalid data for 'mailinglist_id'. Information is duplicated by 'mailinglist_shortname'");
								} else {
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
								}
							} else if ("mailinglist_shortname".equals(entry.getKey())) {
								if (jsonObject.containsPropertyKey("mailinglist_id")) {
									throw new RestfulClientException("Invalid data for 'mailinglist_shortname'. Information is duplicated by 'mailinglist_id'");
								} else {
									boolean mailinglistFound = false;
									String mailinglistShortname = (String) jsonObject.get("mailinglist_shortname");
									for (Mailinglist mailinglist : mailinglistDao.getMailinglists(admin.getCompanyID())) {
										if (StringUtils.equals(mailinglist.getShortname(), mailinglistShortname)) {
											if (mailinglistFound) {
												throw new RestfulClientException("Invalid value for 'mailinglist_id'. Mailinglist name exists multiple times: " + entry.getValue());
											} else {
												mailing.setMailinglistID(mailinglist.getId());
												mailinglistFound = true;
											}
										}
									}
									if (!mailinglistFound) {
										throw new RestfulClientException("Invalid value for 'mailinglist_id'. Mailinglist does not exist: " + entry.getValue());
									}
								}
							} else if ("mailingtype".equals(entry.getKey())) {
								if (entry.getValue() != null && entry.getValue() instanceof String) {
									MailingType mailingType;
									try {
										mailingType = MailingType.fromName((String) entry.getValue());
									} catch (Exception e) {
										throw new RestfulClientException("Invalid value for 'mailingtype': " + entry.getValue());
									}
									mailing.setMailingType(mailingType);
								} else {
									throw new RestfulClientException("Invalid data type for 'mailingtype'. String expected");
								}
							} else if ("mailing_content_type".equals(entry.getKey())) {
								if (entry.getValue() != null && entry.getValue() instanceof String) {
									MailingContentType mailingContentType;
									try {
										mailingContentType = MailingContentType.getFromString((String) jsonObject.get("mailing_content_type"));
									} catch (Exception e) {
										throw new RestfulClientException("Invalid value for 'mailing_content_type': " + entry.getValue());
									}
									mailing.setMailingContentType(mailingContentType);
								} else {
									throw new RestfulClientException("Invalid data type for 'mailing_content_type'. String expected");
								}
							} else if ("target_expression".equals(entry.getKey())) {
								if (entry.getValue() != null && entry.getValue() instanceof String) {
									String targetExpression;
									try {
										targetExpression = (String) jsonObject.get("target_expression");
									} catch (Exception e) {
										throw new RestfulClientException("Invalid value for 'target_expression': " + entry.getValue());
									}
									mailing.setTargetExpression(targetExpression);
								} else {
									throw new RestfulClientException("Invalid data type for 'target_expression'. String expected");
								}
							} else if ("is_template".equals(entry.getKey())) {
								if (entry.getValue() != null && entry.getValue() instanceof Boolean) {
									Boolean isTemplate;
									try {
										isTemplate = (Boolean) jsonObject.get("is_template");
									} catch (Exception e) {
										throw new RestfulClientException("Invalid value for 'is_template': " + entry.getValue());
									}
									mailing.setIsTemplate(isTemplate);
								} else {
									throw new RestfulClientException("Invalid data type for 'is_template'. Boolean expected");
								}
							} else if ("open_action_id".equals(entry.getKey())) {
								if (entry.getValue() != null && entry.getValue() instanceof Integer) {
									int openActionId;
									try {
										openActionId = (Integer) jsonObject.get("open_action_id");
									} catch (Exception e) {
										throw new RestfulClientException("Invalid value for 'open_action_id': " + entry.getValue());
									}
									mailing.setOpenActionID(openActionId);
								} else {
									throw new RestfulClientException("Invalid data type for 'open_action_id'. Integer expected");
								}
							} else if ("click_action_id".equals(entry.getKey())) {
								if (entry.getValue() != null && entry.getValue() instanceof Integer) {
									int clickActionId;
									try {
										clickActionId = (Integer) jsonObject.get("click_action_id");
									} catch (Exception e) {
										throw new RestfulClientException("Invalid value for 'click_action_id': " + entry.getValue());
									}
									mailing.setClickActionID(clickActionId);
								} else {
									throw new RestfulClientException("Invalid data type for 'click_action_id'. Integer expected");
								}
							} else if ("campaign_id".equals(entry.getKey())) {
								if (entry.getValue() != null && entry.getValue() instanceof Integer) {
									int campaignId;
									try {
										campaignId = (Integer) jsonObject.get("campaign_id");
									} catch (Exception e) {
										throw new RestfulClientException("Invalid value for 'campaign_id': " + entry.getValue());
									}
									mailing.setCampaignID(campaignId);
								} else {
									throw new RestfulClientException("Invalid data type for 'campaign_id'. Integer expected");
								}
							} else if ("parameters".equals(entry.getKey())) {
								if (entry.getValue() != null && entry.getValue() instanceof JsonArray) {
									try {
										List<ComMailingParameter> parameters = new ArrayList<>();
										for (Object parameterObject : (JsonArray) jsonObject.get("parameters")) {
											JsonObject parameterJsonObject = (JsonObject) parameterObject;
											ComMailingParameter mailingParameter = new ComMailingParameter();
											mailingParameter.setName((String) parameterJsonObject.get("name"));
											mailingParameter.setValue((String) parameterJsonObject.get("value"));
											mailingParameter.setDescription((String) parameterJsonObject.get("description"));
											parameters.add(mailingParameter);
										}
										mailing.setParameters(parameters);
									} catch (Exception e) {
										throw new RestfulClientException("Invalid value for 'parameters': " + entry.getValue());
									}
								} else {
									throw new RestfulClientException("Invalid data type for 'parameters'. JsonArray expected");
								}
							} else if ("links".equals(entry.getKey())) {
								Map<String, ComTrackableLink> trackableLinks = new HashMap<>();
								for (Object linkObject : (JsonArray) jsonObject.get("links")) {
									JsonObject linkJsonObject = (JsonObject) linkObject;
									ComTrackableLink trackableLink = new ComTrackableLinkImpl();
									trackableLink.setShortname((String) linkJsonObject.get("name"));
									String fullUrl = (String) linkJsonObject.get("url");
									fullUrl = fullUrl.replace("[COMPANY_ID]", Integer.toString(admin.getCompanyID())).replace("[RDIR_DOMAIN]", admin.getCompany().getRdirDomain());
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
									
									if (linkJsonObject.containsPropertyKey("administrative")) {
										trackableLink.setAdminLink((Boolean) linkJsonObject.get("administrative"));
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
								mailing.setTrackableLinks(trackableLinks);
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

	private void writeActivityLog(String description, HttpServletRequest request, Admin admin) {
		writeActivityLog(userActivityLogDao, description, request, admin);
	}
}
