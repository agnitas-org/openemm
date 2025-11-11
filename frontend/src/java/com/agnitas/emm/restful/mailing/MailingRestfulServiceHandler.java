/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.mailing;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingContentType;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.TrackableLink;
import com.agnitas.beans.impl.TrackableLinkImpl;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.company.service.CompanyTokenService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.bean.MailingParameter;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.dao.MailinglistDao;
import com.agnitas.emm.core.thumbnails.service.ThumbnailService;
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
import com.agnitas.service.ImportResult;
import com.agnitas.service.MailingExporter;
import com.agnitas.service.MailingImporter;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.HttpUtils.RequestMethod;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.service.CopyMailingService;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.apache.commons.lang3.StringUtils;

/**
 * This restful service is available at:
 * https://<system.url>/restful/mailing
 */
public class MailingRestfulServiceHandler implements RestfulServiceHandler {
	
	public static final String NAMESPACE = "mailing";

	public static final Object EXPORTED_TO_STREAM = new Object();
	private static final String TEMPLATE_ID_PARAM = "templateId";

	private RestfulUserActivityLogDao userActivityLogDao;
	private MailingDao mailingDao;
	private MailinglistDao mailinglistDao;
	private MailingImporter mailingImporter;
	private MailingExporter mailingExporter;
	private CopyMailingService copyMailingService;
	private ThumbnailService thumbnailService;
	private MailingService mailingService;
	private MailingBaseService mailingBaseService;
	private CompanyTokenService companyTokenService;
    private MaildropService maildropService;

	public void setUserActivityLogDao(RestfulUserActivityLogDao userActivityLogDao) {
		this.userActivityLogDao = userActivityLogDao;
	}
	public void setMailinglistDao(MailinglistDao mailinglistDao) {
		this.mailinglistDao = mailinglistDao;
	}
	
	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	public void setMailingImporter(MailingImporter mailingImporter) {
		this.mailingImporter = mailingImporter;
	}

	public void setMailingExporter(MailingExporter mailingExporter) {
		this.mailingExporter = mailingExporter;
	}

	public void setCopyMailingService(CopyMailingService copyMailingService) {
		this.copyMailingService = copyMailingService;
	}

	public void setMailingService(MailingService mailingService) {
		this.mailingService = mailingService;
	}

	public void setThumbnailService(ThumbnailService thumbnailService) {
		this.thumbnailService = thumbnailService;
	}

	public void setMailingBaseService(MailingBaseService mailingBaseService) {
		this.mailingBaseService = mailingBaseService;
	}
	
	public void setCompanyTokenService(CompanyTokenService companyTokenService) {
		this.companyTokenService = companyTokenService;
	}
	
	public void setMaildropService(MaildropService maildropService) {
		this.maildropService = maildropService;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) {
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
			
			Map<String, String[]> requestParameters = request.getParameterMap();
			Pattern filterPatternMailingName = null;
			if (requestParameters.get("name") != null && requestParameters.get("name").length > 0 && StringUtils.isNotBlank(requestParameters.get("name")[0])) {
				filterPatternMailingName = Pattern.compile(".*" + requestParameters.get("name")[0].toLowerCase().replace("*", ".*") + ".*");
			}
			
			final Pattern filterPatternMailingType;
			if (requestParameters.get("type") != null && requestParameters.get("type").length > 0 && StringUtils.isNotBlank(requestParameters.get("type")[0])) {
				filterPatternMailingType = Pattern.compile(".*" + requestParameters.get("type")[0].toLowerCase().replace("*", ".*") + ".*");
			} else {
				filterPatternMailingType = null;
			}
			
			Pattern filterPatternMailinglistID = null;
			if (requestParameters.get("mailinglistid") != null && requestParameters.get("mailinglistid").length > 0 && StringUtils.isNotBlank(requestParameters.get("mailinglistid")[0])) {
				filterPatternMailinglistID = Pattern.compile(".*" + requestParameters.get("mailinglistid")[0].replace("*", ".*") + ".*");
			}
			
			Pattern filterPatternMailinglistName = null;
			if (requestParameters.get("mailinglistname") != null && requestParameters.get("mailinglistname").length > 0 && StringUtils.isNotBlank(requestParameters.get("mailinglistname")[0])) {
				filterPatternMailinglistName = Pattern.compile(".*" + requestParameters.get("mailinglistname")[0].toLowerCase().replace("*", ".*") + ".*");
			}
			
			Pattern filterPatternMailingStatus = null;
			if (requestParameters.get("status") != null && requestParameters.get("status").length > 0 && StringUtils.isNotBlank(requestParameters.get("status")[0])) {
				filterPatternMailingStatus = Pattern.compile(requestParameters.get("status")[0].toLowerCase().replace("*", ".*"));
			}
			
			Pattern filterPatternCreationDate = null;
			if (requestParameters.get("creationdate") != null && requestParameters.get("creationdate").length > 0 && StringUtils.isNotBlank(requestParameters.get("creationdate")[0])) {
				filterPatternCreationDate = Pattern.compile(".*" + requestParameters.get("creationdate")[0].replace("*", ".*") + ".*");
			}
			
			Pattern filterPatternSendDate = null;
			if (requestParameters.get("senddate") != null && requestParameters.get("senddate").length > 0 && StringUtils.isNotBlank(requestParameters.get("senddate")[0])) {
				filterPatternSendDate = Pattern.compile(".*" + requestParameters.get("senddate")[0].replace("*", ".*") + ".*");
			}
			
			List<MailingType> mailingTypes = Arrays.asList(MailingType.values());
			
			if (filterPatternMailingType != null) {
				mailingTypes = mailingTypes.stream().filter(x -> filterPatternMailingType.matcher(x.name().toLowerCase()).matches()).collect(Collectors.toList());
			}
			
			for (MailingType mailingType : mailingTypes) {
				for (LightweightMailing mailing : mailingDao.getMailingsByType(mailingType.getCode(), admin.getCompanyID())) {
					JsonObject mailingJsonObject = getBasicJson(mailing);

					Mailing fullMailing = null;
					
					if (filterPatternMailingName != null && !filterPatternMailingName.matcher(mailing.getShortname().toLowerCase()).matches()) {
						continue;
					}
					
					if (filterPatternMailingType != null) {
						mailingJsonObject.add("type", mailing.getMailingType().name());
					}
					
					if (filterPatternMailinglistID != null) {
						if (fullMailing == null) {
							fullMailing = mailingDao.getMailing(mailing.getMailingID(), mailing.getCompanyID());
						}
						
						if (!filterPatternMailinglistID.matcher(Integer.toString(fullMailing.getMailinglistID())).matches()) {
							continue;
						} else {
							mailingJsonObject.add("mailinglistid", fullMailing.getMailinglistID());
						}
					}
					
					if (filterPatternMailinglistName != null) {
						if (fullMailing == null) {
							fullMailing = mailingDao.getMailing(mailing.getMailingID(), mailing.getCompanyID());
						}
						
						String mailinglistName = mailinglistDao.getMailinglistName(fullMailing.getMailinglistID(), mailing.getCompanyID());
						if (!filterPatternMailinglistName.matcher(mailinglistName.toLowerCase()).matches()) {
							continue;
						}
						mailingJsonObject.add("mailinglistid", fullMailing.getMailinglistID());
						mailingJsonObject.add("mailinglist", mailinglistName);
					}
					
					if (filterPatternMailingStatus != null) {
						if (mailing.getWorkStatus().isEmpty() || !filterPatternMailingStatus.matcher(MailingStatus.fromDbKey(mailing.getWorkStatus().get()).name().toLowerCase()).matches()) {
							continue;
						}
						mailingJsonObject.add("status", MailingStatus.fromDbKey(mailing.getWorkStatus().get()).name());
					}
					
					if (filterPatternCreationDate != null) {
						if (fullMailing == null) {
							fullMailing = mailingDao.getMailing(mailing.getMailingID(), mailing.getCompanyID());
						}
						
						SimpleDateFormat mailingCreationDateFormat = new SimpleDateFormat(DateUtilities.ISO_8601_DATETIME_FORMAT);
						if (!filterPatternCreationDate.matcher(mailingCreationDateFormat.format(fullMailing.getCreationDate())).matches()) {
							continue;
						}
						mailingJsonObject.add("creation_date", fullMailing.getCreationDate());
					}
					
					if (filterPatternSendDate != null) {
						if (fullMailing == null) {
							fullMailing = mailingDao.getMailing(mailing.getMailingID(), mailing.getCompanyID());
						}
						
						SimpleDateFormat mailingSendDateFormat = new SimpleDateFormat(DateUtilities.ISO_8601_DATETIME_FORMAT);
						if (fullMailing.getSenddate() == null || !filterPatternSendDate.matcher(mailingSendDateFormat.format(fullMailing.getSenddate())).matches()) {
							continue;
						}
						mailingJsonObject.add("send_date", fullMailing.getSenddate());
					}
					
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
				return getMailingByName(requestedMailingKeyValue, admin.getCompanyID());
			}

			userActivityLogDao.addAdminUseOfFeature(admin, "restful/mailing", new Date());
			writeActivityLog(requestedMailingKeyValue, request, admin);

			int mailingID = Integer.parseInt(requestedMailingKeyValue);
			
			if (mailingDao.exist(mailingID, admin.getCompanyID())) {
				if ("light".equals(request.getParameter("view"))) {
					return getBasicJson(mailingDao.getLightweightMailing(admin.getCompanyID(), mailingID));
				}
				mailingExporter.exportMailingToJson(admin.getCompanyID(), mailingID, response.getOutputStream(), false, false);
				return EXPORTED_TO_STREAM;
			} else {
				throw new RestfulNoDataFoundException("No data found");
			}
		} else if (restfulContext.length == 2) {
			if (AgnUtils.isNumber(restfulContext[0]) && "status".equalsIgnoreCase(restfulContext[1])) {
				int mailingID = Integer.parseInt(restfulContext[0]);
				MailingStatus status = mailingDao.getStatus(admin.getCompanyID(), mailingID);
				return status.name();
			} else {
				throw new RestfulClientException("Invalid request");
			}
		} else {
			throw new RestfulClientException("Invalid request");
		}
	}

	private JsonObject getMailingByName(String name, int companyId) throws RestfulNoDataFoundException {
		LightweightMailing mailing = mailingDao.getMailingByName(name, companyId);
		if (mailing == null) {
			throw new RestfulNoDataFoundException("No data found");
		}
		return getBasicJson(mailing);
	}

	private static JsonObject getBasicJson(LightweightMailing mailing) {
		JsonObject json = new JsonObject();
		json.add("mailing_id", mailing.getMailingID());
		json.add("type", mailing.getMailingType().name());
		json.add("name", mailing.getShortname());
		json.add("description", mailing.getMailingDescription());
		return json;
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
		Map<String, String[]> requestParameters = request.getParameterMap();
		
		if (restfulContext.length == 0) {
			if ((requestData == null || requestData.length == 0) && (requestDataFile == null || requestDataFile.length() <= 0) && requestParameters.get(TEMPLATE_ID_PARAM) == null) {
				throw new RestfulClientException("Missing request data");
			} else {
				if (requestParameters.get(TEMPLATE_ID_PARAM) != null) {
					int templateId = Integer.parseInt(requestParameters.get(TEMPLATE_ID_PARAM)[0]);
					Mailing template = mailingDao.getMailing(templateId, admin.getCompanyID());
					if (template == null || !template.isIsTemplate()) {
						throw new RestfulClientException("Error while creating mailing: template not exists");
					}
					MailingModel model = new MailingModel();
					model.setCompanyId(admin.getCompanyID());
					model.setTemplateId(templateId);
					model.setShortname(template.getShortname());
					model.setDescription(template.getDescription());
					JsonObject returnJsonObject = new JsonObject();
					returnJsonObject.add("mailing_id", mailingService.addMailingFromTemplate(model));
					return returnJsonObject;
				}
				try (InputStream inputStream = RestfulServiceHandler.getRequestDataStream(requestData, requestDataFile)) {
					ImportResult result = mailingImporter.importMailingFromJson(admin.getCompanyID(), inputStream, false, null, null, true, false, true);
					if (result.isSuccess()) {
						thumbnailService.updateMailingThumbnailByWebservice(admin.getCompanyID(), result.getMailingID());
						
						LightweightMailing mailing = mailingDao.getLightweightMailing(admin.getCompanyID(), result.getMailingID());

                        return getBasicJson(mailing);
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
			if (!isMailingEditable(mailingID, admin)) {
				throw new RestfulClientException("This mailing may not be changed, because it was already sent or is an active actionbased mailing");
			}
			
			try (InputStream inputStream = RestfulServiceHandler.getRequestDataStream(requestData, requestDataFile)) {
				try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
					JsonNode jsonNode = jsonReader.read();
					if (JsonDataType.OBJECT == jsonNode.getJsonDataType()) {
						JsonObject jsonObject = (JsonObject) jsonNode.getValue();
						for (Entry<String, Object> entry : jsonObject.entrySet()) {
							if ("shortname".equals(entry.getKey())) {
								if (entry.getValue() != null && entry.getValue() instanceof String) {
									mailing.setShortname((String) entry.getValue());
									// Check for unallowed html tags
									try {
										HtmlChecker.checkForUnallowedHtmlTags(mailing.getShortname(), false);
									} catch(final HtmlCheckerException e) {
										throw new RestfulClientException("Mailing name contains unallowed HTML tags", e);
									}
								} else {
									throw new RestfulClientException("Invalid data type for 'shortname'. String expected");
								}
							} else if ("description".equals(entry.getKey())) {
								if (entry.getValue() instanceof String) {
									mailing.setDescription((String) entry.getValue());
									// Check for unallowed html tags
									try {
										HtmlChecker.checkForUnallowedHtmlTags(mailing.getDescription(), false);
									} catch(final HtmlCheckerException e) {
										throw new RestfulClientException("Mailing description contains unallowed HTML tags", e);
									}
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
							} else if ("subject".equals(entry.getKey())) {
								if (entry.getValue() instanceof String subject) {
									mailing.getEmailParam().setSubject(subject);
								} else {
									throw new RestfulClientException("Invalid data type for 'subject'. String expected");
								}
							} else if ("sender_address".equals(entry.getKey())) {
								if (entry.getValue() instanceof String senderAddress) {
									mailing.getEmailParam().setFromEmail(senderAddress);
								} else {
									throw new RestfulClientException("Invalid data type for 'sender_address'. String expected");
								}
							} else if ("reply_address".equals(entry.getKey())) {
								if (entry.getValue() instanceof String replyAddress) {
									mailing.getEmailParam().setReplyEmail(replyAddress);
								} else {
									throw new RestfulClientException("Invalid data type for 'sender_address'. String expected");
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
										List<MailingParameter> parameters = new ArrayList<>();
										for (Object parameterObject : (JsonArray) jsonObject.get("parameters")) {
											JsonObject parameterJsonObject = (JsonObject) parameterObject;
											MailingParameter mailingParameter = new MailingParameter();
											mailingParameter.setName((String) parameterJsonObject.get("name"));
											// Check for unallowed html tags
											try {
												HtmlChecker.checkForUnallowedHtmlTags(mailingParameter.getName(), false);
											} catch(final HtmlCheckerException e) {
												throw new RestfulClientException("Mailing parameter name contains unallowed HTML tags", e);
											}
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
								Optional<String> companyTokenOptional = companyTokenService.getCompanyToken(admin.getCompanyID());
								String companyToken = companyTokenOptional.isPresent() ? companyTokenOptional.get() : null;
								
								Map<String, TrackableLink> trackableLinks = new HashMap<>();
								for (Object linkObject : (JsonArray) jsonObject.get("links")) {
									JsonObject linkJsonObject = (JsonObject) linkObject;
									TrackableLink trackableLink = new TrackableLinkImpl();
									for (Entry<String, Object> linkDataEntry : linkJsonObject.entrySet()) {
										if (linkDataEntry.getKey().equals("id")) {
											trackableLink.setId((Integer) linkDataEntry.getValue());
										} else if (linkDataEntry.getKey().equals("name")) {
											trackableLink.setShortname((String) linkDataEntry.getValue());
											// Check for unallowed html tags
											try {
												HtmlChecker.checkForUnallowedHtmlTags(trackableLink.getShortname(), false);
											} catch(final HtmlCheckerException e) {
												throw new RestfulClientException("Link name contains unallowed HTML tags", e);
											}
										} else if (linkDataEntry.getKey().equals("url")) {
											String fullUrl = (String) linkDataEntry.getValue();
											fullUrl = fullUrl.replace("[COMPANY_ID]", Integer.toString(admin.getCompanyID())).replace("[RDIR_DOMAIN]", admin.getCompany().getRdirDomain());
											if (StringUtils.isNotBlank(companyToken)) {
												fullUrl = fullUrl.replace("[CTOKEN]", companyToken);
											} else {
												fullUrl = fullUrl.replace("agnCTOKEN=[CTOKEN]", "agnCI=" + admin.getCompanyID());
											}
											trackableLink.setFullUrl(fullUrl);
										} else if (linkDataEntry.getKey().equals("deep_tracking")) {
											trackableLink.setDeepTracking((Integer) linkDataEntry.getValue());
										} else if (linkDataEntry.getKey().equals("usage")) {
											trackableLink.setUsage((Integer) linkDataEntry.getValue());
										} else if (linkDataEntry.getKey().equals("action_id")) {
											trackableLink.setActionID((Integer) linkDataEntry.getValue());
										} else if (linkDataEntry.getKey().equals("administrative")) {
											trackableLink.setAdminLink((Boolean) linkDataEntry.getValue());
										} else if (linkDataEntry.getKey().equals("properties")) {
											List<LinkProperty> linkProperties = new ArrayList<>();
											for (Object propertyObject : (JsonArray) linkDataEntry.getValue()) {
												JsonObject propertyJsonObject = (JsonObject) propertyObject;
												String propertyName = null;
												String propertyValue = null;
												PropertyType propertyType = null;
												for (Entry<String, Object> propertyDataEntry : propertyJsonObject.entrySet()) {
													if (propertyDataEntry.getKey().equals("name")) {
														propertyName = (String) propertyDataEntry.getValue();
														// Check for unallowed html tags
														try {
															HtmlChecker.checkForUnallowedHtmlTags(propertyName, false);
														} catch(final HtmlCheckerException e) {
															throw new RestfulClientException("Link property name contains unallowed HTML tags", e);
														}
													} else if (propertyDataEntry.getKey().equals("value")) {
														propertyValue = (String) propertyDataEntry.getValue();
													} else if (propertyDataEntry.getKey().equals("type")) {
														String propertyTypeString = (String) propertyDataEntry.getValue();
														try {
															propertyType = PropertyType.parseString(propertyTypeString);
														} catch (Exception e) {
															throw new RestfulClientException("Invalid property type '" + propertyTypeString + "' for mailing link property", e);
														}
													} else {
														throw new RestfulClientException("Invalid property '" + propertyDataEntry.getKey() + "' for mailing link property");
													}
												}
												
												if (propertyValue == null) {
													propertyValue = "";
												}
													
												if (propertyName == null) {
													propertyName = "";
												}
												
												if (propertyType == null) {
													propertyType = PropertyType.LinkExtension;
												}
													
												LinkProperty linkProperty = new LinkProperty(propertyType, propertyName, propertyName);
												linkProperties.add(linkProperty);
											}
											trackableLink.setProperties(linkProperties);
										} else {
											throw new RestfulClientException("Invalid link data property '" + linkDataEntry.getKey() + "' for mailing link");
										}
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

    /**
     * Check whether or not a mailing is editable.
     * Basically a world sent mailing is not editable but there's a permission {@link com.agnitas.emm.core.Permission#MAILING_CONTENT_CHANGE_ALWAYS}
     * that unlocks sent mailing so it could be edited anyway.
     *
     * @return whether ({@code true}) or not ({@code false}) mailing editing is permitted.
     */
    private boolean isMailingEditable(int mailingId, Admin admin) {
        if (maildropService.isActiveMailing(mailingId, admin.getCompanyID())) {
            return admin.permissionAllowed(Permission.MAILING_CONTENT_CHANGE_ALWAYS);
        } else {
            return true;
        }
    }
}
