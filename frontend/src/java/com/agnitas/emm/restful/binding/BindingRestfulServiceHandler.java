/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.binding;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.impl.BindingEntryImpl;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.useractivitylog.dao.UserActivityLogDao;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.ComBindingEntryDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.recipient.service.RecipientType;
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
 * https:/<system.url>/restful/binding
 */
public class BindingRestfulServiceHandler implements RestfulServiceHandler {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(BindingRestfulServiceHandler.class);
	
	public static final String NAMESPACE = "binding";

	private UserActivityLogDao userActivityLogDao;
	private ComRecipientDao recipientDao;
	private ComBindingEntryDao bindingEntryDao;
	private MailinglistDao mailinglistDao;

	@Required
	public void setUserActivityLogDao(UserActivityLogDao userActivityLogDao) {
		this.userActivityLogDao = userActivityLogDao;
	}
	
	@Required
	public void setRecipientDao(ComRecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}
	
	@Required
	public void setBindingEntryDao(ComBindingEntryDao bindingEntryDao) {
		this.bindingEntryDao = bindingEntryDao;
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
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(getBindingEntry(request, admin)));
		} else if (requestMethod == RequestMethod.DELETE) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(deleteBindingEntry(request, admin)));
		} else if (requestDataFilePath == null || new File(requestDataFilePath).length() <= 0) {
			restfulResponse.setError(new RestfulClientException("Missing request data"), ErrorCode.REQUEST_DATA_ERROR);
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createNewBindingEntry(request, new File(requestDataFilePath), admin)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createOrUpdateBindingEntry(request, new File(requestDataFilePath), admin)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	/**
	 * Return a single or multiple binding data sets
	 * 
	 * @param request
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object getBindingEntry(HttpServletRequest request, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.RECIPIENT_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.RECIPIENT_SHOW.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 2);

		int requestedCustomerID;
		if (AgnUtils.isNumber(restfulContext[0])) {
			requestedCustomerID = Integer.parseInt(restfulContext[0]);
			if (!recipientDao.exist(requestedCustomerID, admin.getCompanyID())) {
				throw new RestfulNoDataFoundException("No data found");
			}
		} else {
			requestedCustomerID = recipientDao.findByColumn(admin.getCompanyID(), "email", restfulContext[0]);
			if (requestedCustomerID <= 0) {
				throw new RestfulNoDataFoundException("No data found");
			}
		}
		
		if (restfulContext.length == 1) {
			// Show binding entries for an email or customerID
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/binding", new Date());
			userActivityLogDao.writeUserActivityLog(admin, "restful/binding GET", "" + requestedCustomerID);
			
			JsonArray bindingsJsonArray = new JsonArray();
			
			for (BindingEntry bindingEntry : bindingEntryDao.getBindings(admin.getCompanyID(), requestedCustomerID)) {
				JsonObject bindingJsonObject = new JsonObject();
				bindingJsonObject.add("mailinglist_id", bindingEntry.getMailinglistID());
				if (bindingEntry.getUserType() != RecipientType.NORMAL_RECIPIENT.getLetter()) {
					bindingJsonObject.add("user_type", RecipientType.getRecipientTypeByLetter(bindingEntry.getUserType()).name());
				}
				if (bindingEntry.getMediaType() != MediaTypes.EMAIL.getMediaCode()) {
					bindingJsonObject.add("mediatype", MediaTypes.getMediaTypeForCode(bindingEntry.getMediaType()).name());
				}
				bindingJsonObject.add("user_status", UserStatus.getUserStatusByID(bindingEntry.getUserStatus()).name());
				if (StringUtils.isNotBlank(bindingEntry.getUserRemark())) {
					bindingJsonObject.add("user_remark", bindingEntry.getUserRemark());
				}
				if (StringUtils.isNotBlank(bindingEntry.getReferrer())) {
					bindingJsonObject.add("referrer", bindingEntry.getReferrer());
				}
				if (bindingEntry.getEntryMailingID() > 0) {
					bindingJsonObject.add("entry_mailing_id", bindingEntry.getEntryMailingID());
				}
				if (bindingEntry.getExitMailingID() > 0) {
					bindingJsonObject.add("exit_mailing_id", bindingEntry.getExitMailingID());
				}
				bindingJsonObject.add("creation_date", bindingEntry.getCreationDate());
				bindingJsonObject.add("change_date", bindingEntry.getChangeDate());
				bindingsJsonArray.add(bindingJsonObject);
			}
				
			return bindingsJsonArray;
		} else {
			int requestedMailinglistID;
			if (!AgnUtils.isNumber(restfulContext[1])) {
				throw new RestfulClientException("Invalid request");
			} else {
				requestedMailinglistID = Integer.parseInt(restfulContext[1]);
			}
			
			// Show binding entries for an email or customerID for a specific mailinglist
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/binding", new Date());
			userActivityLogDao.writeUserActivityLog(admin, "restful/binding GET", requestedCustomerID + " MID: " + requestedMailinglistID);
			
			JsonArray bindingsJsonArray = new JsonArray();
			
			for (BindingEntry bindingEntry : bindingEntryDao.getBindings(admin.getCompanyID(), requestedCustomerID)) {
				if (requestedMailinglistID == bindingEntry.getMailinglistID()) {
					JsonObject bindingJsonObject = new JsonObject();
					bindingJsonObject.add("mailinglist_id", bindingEntry.getMailinglistID());
					if (bindingEntry.getUserType() != RecipientType.NORMAL_RECIPIENT.getLetter()) {
						bindingJsonObject.add("user_type", RecipientType.getRecipientTypeByLetter(bindingEntry.getUserType()).name());
					}
					bindingJsonObject.add("mediatype", bindingEntry.getUserType());
					if (bindingEntry.getMediaType() != MediaTypes.EMAIL.getMediaCode()) {
						bindingJsonObject.add("mediatype", MediaTypes.getMediaTypeForCode(bindingEntry.getMediaType()).name());
					}
					bindingJsonObject.add("user_status", UserStatus.getUserStatusByID(bindingEntry.getUserStatus()).name());
					if (StringUtils.isNotBlank(bindingEntry.getUserRemark())) {
						bindingJsonObject.add("user_remark", bindingEntry.getUserRemark());
					}
					if (StringUtils.isNotBlank(bindingEntry.getReferrer())) {
						bindingJsonObject.add("referrer", bindingEntry.getReferrer());
					}
					if (bindingEntry.getEntryMailingID() > 0) {
						bindingJsonObject.add("entry_mailing_id", bindingEntry.getEntryMailingID());
					}
					if (bindingEntry.getExitMailingID() > 0) {
						bindingJsonObject.add("exit_mailing_id", bindingEntry.getExitMailingID());
					}
					bindingJsonObject.add("creation_date", bindingEntry.getCreationDate());
					bindingJsonObject.add("change_date", bindingEntry.getChangeDate());
					bindingsJsonArray.add(bindingJsonObject);
				}
			}
				
			return bindingsJsonArray;
		}
	}

	/**
	 * Delete a binding
	 * 
	 * @param request
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object deleteBindingEntry(HttpServletRequest request, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.RECIPIENT_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.RECIPIENT_CHANGE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 2);
		
		int requestedCustomerID;
		if (AgnUtils.isNumber(restfulContext[0])) {
			requestedCustomerID = Integer.parseInt(restfulContext[0]);
			if (!recipientDao.exist(requestedCustomerID, admin.getCompanyID())) {
				throw new RestfulNoDataFoundException("No data found");
			}
		} else {
			requestedCustomerID = recipientDao.findByColumn(admin.getCompanyID(), "email", restfulContext[0]);
			if (requestedCustomerID <= 0) {
				throw new RestfulNoDataFoundException("No data found");
			}
		}
		
		if (restfulContext.length == 2) {
			String requestedMailingID = restfulContext[1];
			if (AgnUtils.isNumber(requestedMailingID)) {
				int requestedMailinglistID = Integer.parseInt(requestedMailingID);
				if (mailinglistDao.exist(requestedMailinglistID, admin.getCompanyID())) {
					int count = 0;
					for (BindingEntry bindingEntry : bindingEntryDao.getBindings(admin.getCompanyID(), requestedCustomerID)) {
						if (bindingEntry.getMailinglistID() == requestedMailinglistID) {
							bindingEntryDao.delete(requestedCustomerID, admin.getCompanyID(), requestedMailinglistID, bindingEntry.getMediaType());
							count++;
						}
					}
					return count + " binding entries deleted";
				} else {
					throw new RestfulClientException("Mailinglist with id '" + requestedMailinglistID + "' does not exist");
				}
			} else {
				throw new RestfulClientException("Invalid request");
			}
		} else {
			int count = 0;
			for (BindingEntry bindingEntry : bindingEntryDao.getBindings(admin.getCompanyID(), requestedCustomerID)) {
				bindingEntryDao.delete(requestedCustomerID, admin.getCompanyID(), bindingEntry.getMailinglistID(), bindingEntry.getMediaType());
				count++;
			}
			return count + " binding entries deleted";
		}
	}

	/**
	 * Create a new binding
	 * 
	 * @param request
	 * @param requestDataFile
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object createNewBindingEntry(HttpServletRequest request, File requestDataFile, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.RECIPIENT_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.RECIPIENT_CHANGE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		
		int requestedCustomerID;
		if (AgnUtils.isNumber(restfulContext[0])) {
			requestedCustomerID = Integer.parseInt(restfulContext[0]);
			if (!recipientDao.exist(requestedCustomerID, admin.getCompanyID())) {
				throw new RestfulNoDataFoundException("No data found");
			}
		} else {
			requestedCustomerID = recipientDao.findByColumn(admin.getCompanyID(), "email", restfulContext[0]);
			if (requestedCustomerID <= 0) {
				throw new RestfulNoDataFoundException("No data found");
			}
		}

		BindingEntry newBindingEntry = new BindingEntryImpl();
		newBindingEntry.setCustomerID(requestedCustomerID);
		
		try (InputStream inputStream = new FileInputStream(requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				JsonNode jsonNode = jsonReader.read();
				if (JsonDataType.OBJECT == jsonNode.getJsonDataType()) {
					JsonObject jsonObject = (JsonObject) jsonNode.getValue();
					for (Entry<String, Object> entry : jsonObject.entrySet()) {
						if ("mailinglist_id".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof Integer) {
								int requestedMailinglistID = (Integer) entry.getValue();
								if (!mailinglistDao.exist(requestedMailinglistID, admin.getCompanyID())) {
									throw new RestfulClientException("Mailinglist with id '" + requestedMailinglistID + "' does not exist");
								}
								newBindingEntry.setMailinglistID(requestedMailinglistID);
							} else {
								throw new RestfulClientException("Invalid data type for 'mailinglist_id'. Integer expected");
							}
						} else if ("mediatype".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof Integer) {
								int mediaType = (Integer) entry.getValue();
								if (MediaTypes.getMediaTypeForCode(mediaType) == null) {
									throw new RestfulClientException("Invalid value for 'mediatype'");
								} else {
									newBindingEntry.setMediaType(mediaType);
								}
							} else if (entry.getValue() != null && entry.getValue() instanceof String) {
								MediaTypes mediaTypeValue = MediaTypes.getMediatypeByName((String) entry.getValue());
								if (mediaTypeValue == null) {
									throw new RestfulClientException("Invalid value for 'mediatype': " + entry.getValue());
								} else {
									newBindingEntry.setMediaType(mediaTypeValue.getMediaCode());
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'user_status'. Integer or String expected");
							}
						} else if ("user_status".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof Integer) {
								int userStatus = (Integer) entry.getValue();
								try {
									UserStatus.getUserStatusByID(userStatus);
								} catch(Exception e) {
									throw new RestfulClientException("Invalid value for 'user_status'");
								}
								newBindingEntry.setUserStatus(userStatus);
							} else if (entry.getValue() != null && entry.getValue() instanceof String) {
								UserStatus userStatusValue = UserStatus.getUserStatusByName((String) entry.getValue());
								if (userStatusValue == null) {
									throw new RestfulClientException("Invalid value for 'user_status': " + entry.getValue());
								} else {
									newBindingEntry.setUserStatus(userStatusValue.getStatusCode());
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'user_status'. Integer or String expected");
							}
						} else if ("user_remark".equals(entry.getKey())) {
							if (entry.getValue() == null || entry.getValue() instanceof String) {
								newBindingEntry.setUserRemark((String) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'user_remark'. String expected");
							}
						} else if ("referrer".equals(entry.getKey())) {
							if (entry.getValue() == null || entry.getValue() instanceof String) {
								newBindingEntry.setReferrer((String) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'referrer'. String expected");
							}
						} else if ("entry_mailing_id".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof Integer) {
								newBindingEntry.setEntryMailingID((Integer) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'entry_mailing_id'. Integer expected");
							}
						} else if ("exit_mailing_id".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof Integer) {
								newBindingEntry.setExitMailingID((Integer) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'exit_mailing_id'. Integer expected");
							}
						} else {
							throw new RestfulClientException("Invalid property '" + entry.getKey() + "' for binding entry");
						}
					}
				} else {
					throw new RestfulClientException("Expected root JSON item type 'JsonObject' but was: " + jsonNode.getJsonDataType());
				}
			}
		}
		
		if (newBindingEntry.getMailinglistID() <= 0) {
			throw new RestfulClientException("Missing mandatory value for 'mailinglist_id'");
		} else if (newBindingEntry.getUserStatus() <= 0) {
			throw new RestfulClientException("Missing mandatory value for 'user_status'");
		} else if (bindingEntryDao.exist(newBindingEntry.getCustomerID(), admin.getCompanyID(), newBindingEntry.getMailinglistID(), newBindingEntry.getMediaType())) {
			throw new RestfulClientException("Binding entry already exists");
		} else {
			bindingEntryDao.insertNewBinding(newBindingEntry, admin.getCompanyID());
			return "1 binding entry created";
		}
	}

	/**
	 * Create a new binding or update an exiting binding
	 * 
	 * @param request
	 * @param requestDataFile
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object createOrUpdateBindingEntry(HttpServletRequest request, File requestDataFile, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.RECIPIENT_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.RECIPIENT_CHANGE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		
		int requestedCustomerID;
		if (AgnUtils.isNumber(restfulContext[0])) {
			requestedCustomerID = Integer.parseInt(restfulContext[0]);
			if (!recipientDao.exist(requestedCustomerID, admin.getCompanyID())) {
				throw new RestfulNoDataFoundException("No data found");
			}
		} else {
			requestedCustomerID = recipientDao.findByColumn(admin.getCompanyID(), "email", restfulContext[0]);
			if (requestedCustomerID <= 0) {
				throw new RestfulNoDataFoundException("No data found");
			}
		}

		BindingEntry newBindingEntry = new BindingEntryImpl();
		newBindingEntry.setCustomerID(requestedCustomerID);
		
		try (InputStream inputStream = new FileInputStream(requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				JsonNode jsonNode = jsonReader.read();
				if (JsonDataType.OBJECT == jsonNode.getJsonDataType()) {
					JsonObject jsonObject = (JsonObject) jsonNode.getValue();
					for (Entry<String, Object> entry : jsonObject.entrySet()) {
						if ("mailinglist_id".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof Integer) {
								int requestedMailinglistID = (Integer) entry.getValue();
								if (!mailinglistDao.exist(requestedMailinglistID, admin.getCompanyID())) {
									throw new RestfulClientException("Mailinglist with id '" + requestedMailinglistID + "' does not exist");
								}
								newBindingEntry.setMailinglistID(requestedMailinglistID);
							} else {
								throw new RestfulClientException("Invalid data type for 'mailinglist_id'. Integer expected");
							}
						} else if ("mediatype".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof Integer) {
								int mediaType = (Integer) entry.getValue();
								if (MediaTypes.getMediaTypeForCode(mediaType) == null) {
									throw new RestfulClientException("Invalid value for 'mediatype'");
								} else {
									newBindingEntry.setMediaType(mediaType);
								}
							} else if (entry.getValue() != null && entry.getValue() instanceof String) {
								MediaTypes mediaTypeValue = MediaTypes.getMediatypeByName((String) entry.getValue());
								if (mediaTypeValue == null) {
									throw new RestfulClientException("Invalid value for 'mediatype': " + entry.getValue());
								} else {
									newBindingEntry.setMediaType(mediaTypeValue.getMediaCode());
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'user_status'. Integer or String expected");
							}
						} else if ("user_status".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof Integer) {
								int userStatus = (Integer) entry.getValue();
								try {
									UserStatus.getUserStatusByID(userStatus);
								} catch(Exception e) {
									throw new RestfulClientException("Invalid value for 'user_status'");
								}
								newBindingEntry.setUserStatus(userStatus);
							} else if (entry.getValue() != null && entry.getValue() instanceof String) {
								UserStatus userStatusValue = UserStatus.getUserStatusByName((String) entry.getValue());
								if (userStatusValue == null) {
									throw new RestfulClientException("Invalid value for 'user_status': " + entry.getValue());
								} else {
									newBindingEntry.setUserStatus(userStatusValue.getStatusCode());
								}
							} else {
								throw new RestfulClientException("Invalid data type for 'user_status'. Integer or String expected");
							}
						} else if ("user_remark".equals(entry.getKey())) {
							if (entry.getValue() == null || entry.getValue() instanceof String) {
								newBindingEntry.setUserRemark((String) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'user_remark'. String expected");
							}
						} else if ("referrer".equals(entry.getKey())) {
							if (entry.getValue() == null || entry.getValue() instanceof String) {
								newBindingEntry.setReferrer((String) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'referrer'. String expected");
							}
						} else if ("entry_mailing_id".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof Integer) {
								newBindingEntry.setEntryMailingID((Integer) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'entry_mailing_id'. Integer expected");
							}
						} else if ("exit_mailing_id".equals(entry.getKey())) {
							if (entry.getValue() != null && entry.getValue() instanceof Integer) {
								newBindingEntry.setExitMailingID((Integer) entry.getValue());
							} else {
								throw new RestfulClientException("Invalid data type for 'exit_mailing_id'. Integer expected");
							}
						} else {
							throw new RestfulClientException("Invalid property '" + entry.getKey() + "' for binding entry");
						}
					}
				} else {
					throw new RestfulClientException("Expected root JSON item type 'JsonObject' but was: " + jsonNode.getJsonDataType());
				}
			}
		}
		
		if (newBindingEntry.getMailinglistID() <= 0) {
			throw new RestfulClientException("Missing mandatory value for 'mailinglist_id'");
		} else if (newBindingEntry.getUserStatus() <= 0) {
			throw new RestfulClientException("Missing mandatory value for 'user_status'");
		} else if (bindingEntryDao.exist(newBindingEntry.getCustomerID(), admin.getCompanyID(), newBindingEntry.getMailinglistID(), newBindingEntry.getMediaType())) {
			bindingEntryDao.updateBinding(newBindingEntry, admin.getCompanyID());
			return "1 binding entry updated";
		} else {
			bindingEntryDao.insertNewBinding(newBindingEntry, admin.getCompanyID());
			return "1 binding entry created";
		}
	}

	@Override
	public ResponseType getResponseType() {
		return ResponseType.JSON;
	}
}
