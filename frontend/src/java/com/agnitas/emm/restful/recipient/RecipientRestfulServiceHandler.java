/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.recipient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.CustomerImportStatus;
import org.agnitas.beans.DatasourceDescription;
import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.Recipient;
import org.agnitas.beans.impl.BindingEntryImpl;
import org.agnitas.beans.impl.CustomerImportStatusImpl;
import org.agnitas.beans.impl.DatasourceDescriptionImpl;
import org.agnitas.beans.impl.ImportProfileImpl;
import org.agnitas.beans.impl.RecipientImpl;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.autoimport.service.RemoteFile;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.useractivitylog.dao.UserActivityLogDao;
import org.agnitas.service.ColumnInfoService;
import org.agnitas.service.ProfileImportWorker;
import org.agnitas.service.ProfileImportWorkerFactory;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.agnitas.util.ImportUtils.ImportErrorType;
import org.agnitas.util.importvalues.Charset;
import org.agnitas.util.importvalues.CheckForDuplicates;
import org.agnitas.util.importvalues.DateFormat;
import org.agnitas.util.importvalues.Gender;
import org.agnitas.util.importvalues.ImportMode;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ProfileField;
import com.agnitas.dao.ComBindingEntryDao;
import com.agnitas.dao.ComDatasourceDescriptionDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.restful.BaseRequestResponse;
import com.agnitas.emm.restful.ErrorCode;
import com.agnitas.emm.restful.JsonRequestResponse;
import com.agnitas.emm.restful.ResponseType;
import com.agnitas.emm.restful.RestfulClientException;
import com.agnitas.emm.restful.RestfulNoDataFoundException;
import com.agnitas.emm.restful.RestfulServiceHandler;
import com.agnitas.json.Json5Reader;
import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.json.JsonReader.JsonToken;
import com.agnitas.json.JsonWriter;

/**
 * This restful service is available at:
 * https:/<system.url>/restful/recipient
 */
public class RecipientRestfulServiceHandler implements RestfulServiceHandler {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(RecipientRestfulServiceHandler.class);
	
	public static final String NAMESPACE = "recipient";
	
	private static final String IMPORT_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "RecipientImport";

	private UserActivityLogDao userActivityLogDao;
	private ComRecipientDao recipientDao;
	private ComBindingEntryDao bindingEntryDao;
	private ColumnInfoService columnInfoService;
	private MailinglistDao mailinglistDao;
	private ProfileImportWorkerFactory profileImportWorkerFactory;
	private ComDatasourceDescriptionDao datasourceDescriptionDao;

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
	public void setColumnInfoService(ColumnInfoService columnInfoService) {
		this.columnInfoService = columnInfoService;
	}
	
	@Required
	public void setMailinglistDao(MailinglistDao mailinglistDao) {
		this.mailinglistDao = mailinglistDao;
	}
	
	@Required
	public void setProfileImportWorkerFactory(ProfileImportWorkerFactory profileImportWorkerFactory) {
		this.profileImportWorkerFactory = profileImportWorkerFactory;
	}

	@Required
	public void setDatasourceDescriptionDao(final ComDatasourceDescriptionDao datasourceDescriptionDao) {
		this.datasourceDescriptionDao = datasourceDescriptionDao;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) throws Exception {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, ComAdmin admin, String requestDataFilePath, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(getCustomerData(request, admin)));
		} else if (requestMethod == RequestMethod.DELETE) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(deleteCustomer(request, admin)));
		} else if (requestDataFilePath == null || new File(requestDataFilePath).length() <= 0) {
			restfulResponse.setError(new RestfulClientException("Missing request data"), ErrorCode.REQUEST_DATA_ERROR);
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createNewCustomer(request, new File(requestDataFilePath), admin)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createOrUpdateCustomer(request, new File(requestDataFilePath), admin)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	/**
	 * Return a single or multiple customer data sets
	 * 
	 * @param request
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object getCustomerData(HttpServletRequest request, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.RECIPIENT_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.RECIPIENT_SHOW.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		
		String requestedRecipientKeyValue = restfulContext[0];
		List<Integer> customerIDs;
		if (AgnUtils.isEmailValid(requestedRecipientKeyValue)) {
			customerIDs = recipientDao.getRecipientIDs(admin.getCompanyID(), "email", requestedRecipientKeyValue);
		} else if (AgnUtils.isNumber(requestedRecipientKeyValue)) {
			customerIDs = recipientDao.getRecipientIDs(admin.getCompanyID(), "customer_id", requestedRecipientKeyValue);
		} else {
			throw new RestfulClientException("Invalid requested recipient key: " + requestedRecipientKeyValue);
		}
		
		userActivityLogDao.addAdminUseOfFeature(admin, "restful/recipient", new Date());
		userActivityLogDao.writeUserActivityLog(admin, "restful/recipient GET", requestedRecipientKeyValue);
		
		if (customerIDs.size() > 1) {
			JsonArray result = new JsonArray();
			for (CaseInsensitiveMap<String, Object> customerDataMap : recipientDao.getCustomers(customerIDs, admin.getCompanyID())) {
				JsonObject customerJsonObject = new JsonObject();
				for (String key : AgnUtils.sortCollectionWithItemsFirst(customerDataMap.keySet(), "customer_id", "email")) {
					customerJsonObject.add(key.toLowerCase(), customerDataMap.get(key));
				}
				result.add(customerJsonObject);
			}
			return result;
		} else if (customerIDs.size() == 1) {
			CaseInsensitiveMap<String, Object> customerDataMap = recipientDao.getCustomerData(admin.getCompanyID(), customerIDs.get(0), TimeZone.getTimeZone(admin.getAdminTimezone()));
			JsonObject customerJsonObject = new JsonObject();
			for (String key : AgnUtils.sortCollectionWithItemsFirst(customerDataMap.keySet(), "customer_id", "email")) {
				customerJsonObject.add(key.toLowerCase(), customerDataMap.get(key));
			}
			return customerJsonObject;
		} else {
			throw new RestfulNoDataFoundException("No data found");
		}
	}

	/**
	 * Delete a single or multiple customer data sets
	 * 
	 * @param request
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object deleteCustomer(HttpServletRequest request, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.RECIPIENT_DELETE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.RECIPIENT_DELETE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		
		String requestedRecipientKeyValue = restfulContext[0];
		List<Integer> customerIDs;
		if (AgnUtils.isEmailValid(requestedRecipientKeyValue)) {
			customerIDs = recipientDao.getRecipientIDs(admin.getCompanyID(), "email", requestedRecipientKeyValue);
		} else if (AgnUtils.isNumber(requestedRecipientKeyValue)) {
			customerIDs = recipientDao.getRecipientIDs(admin.getCompanyID(), "customer_id", requestedRecipientKeyValue);
		} else {
			throw new RestfulClientException("Invalid recipient key for deletion: " + requestedRecipientKeyValue);
		}
		
		userActivityLogDao.addAdminUseOfFeature(admin, "restful/recipient", new Date());
		userActivityLogDao.writeUserActivityLog(admin, "restful/recipient DELETE", requestedRecipientKeyValue);
		
		if (customerIDs.size() > 0) {
			recipientDao.deleteRecipients(admin.getCompanyID(), customerIDs);
			return customerIDs.size() + " customer datasets deleted";
		} else {
			throw new RestfulNoDataFoundException("No data found for deletion");
		}
	}

	/**
	 * Create a new customer entry (also bulk/multiple)
	 * 
	 * @param request
	 * @param requestDataFile
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object createNewCustomer(HttpServletRequest request, File requestDataFile, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.RECIPIENT_CREATE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.RECIPIENT_CREATE.toString() + "'");
		}
		
		RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 0);
		
		Integer mailinglistToSubscribe = null;
		if (StringUtils.isNotBlank(request.getParameter("mailinglist"))) {
			if (!AgnUtils.isNumber(request.getParameter("mailinglist"))) {
				throw new RestfulClientException("Invalid parameter mailinglist: '" + request.getParameter("mailinglist") + "'");
			}
			mailinglistToSubscribe = Integer.parseInt(request.getParameter("mailinglist"));
			Mailinglist mailinglist = mailinglistDao.getMailinglist(mailinglistToSubscribe, admin.getCompanyID());
			if (mailinglist == null || mailinglist.isRemoved()) {
				throw new RestfulClientException("Invalid mailinglist: '" + mailinglistToSubscribe + "'");
			}
		}
		
		try (InputStream inputStream = new FileInputStream(requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				// Read root JSON element
				JsonToken readJsonToken = jsonReader.readNextToken();
				if (JsonToken.JsonObject_Open == readJsonToken) {
					// Single customer data
					CaseInsensitiveMap<String, ProfileField> profileFields = columnInfoService.getColumnInfoMap(admin.getCompanyID(), admin.getAdminID());
					JsonObject jsonObject = new JsonObject();
					while ((readJsonToken = jsonReader.readNextToken()) == JsonToken.JsonObject_PropertyKey) {
						String propertyKey = (String) jsonReader.getCurrentObject();
						if (!profileFields.containsKey(propertyKey)) {
							throw new RestfulClientException("Expected recipient property key '" + StringUtils.join(profileFields.keySet(), "', '") + "' but was: '" + jsonReader.getCurrentObject() + "'");
						} else if (jsonObject.containsPropertyKey(propertyKey)) {
							throw new RestfulClientException("Duplicate recipient property key: " + jsonReader.getCurrentObject());
						}
						readJsonToken = jsonReader.readNextToken();
						if (JsonToken.JsonSimpleValue != readJsonToken) {
							throw new RestfulClientException("Expected JSON item type 'JsonSimpleValue' but was: " + readJsonToken);
						}
						jsonObject.add(propertyKey, jsonReader.getCurrentObject());
					}

					if (JsonToken.JsonObject_Close != readJsonToken) {
						throw new RestfulClientException("Expected JSON item type 'JsonObject_Close' was: " + readJsonToken);
					}

					Recipient recipient = new RecipientImpl();
					recipient.setCompanyID(admin.getCompanyID());
					Map<String, Object> custParameters = recipient.getCustParameters();
					for (Entry<String, Object> entry : jsonObject.entrySet()) {
						if (!"customer_id".equalsIgnoreCase(entry.getKey()) && !ComCompanyDaoImpl.STANDARD_FIELD_BOUNCELOAD.equalsIgnoreCase(entry.getKey())) {
							custParameters.put(entry.getKey(), entry.getValue());
						}
					}

					// Set default data if missing
					if (!custParameters.containsKey("gender")) {
						custParameters.put("gender", Gender.UNKNOWN.getStorageValue());
					}
					if (!custParameters.containsKey("mailtype")) {
						custParameters.put("mailtype", MailType.HTML.getIntValue());
					}
					
					if (custParameters.get("email") == null) {
						throw new RestfulClientException("Invalid missing data for 'email'");
					} else if (!(custParameters.get("email") instanceof String)) {
						throw new RestfulClientException("Invalid data for 'email'");
					} else if (StringUtils.isBlank((String) custParameters.get("email"))) {
						throw new RestfulClientException("Invalid empty data for 'email'");
					} else {
						int customerID = recipientDao.findByColumn(admin.getCompanyID(), "email", (String) custParameters.get("email"));
						if (customerID > 0) {
							throw new RestfulClientException("Recipient email '" + ((String) custParameters.get("email")) + "' already exists");
						} else {
							recipientDao.updateInDB(recipient, false);
						}
					}
					
					if (mailinglistToSubscribe != null) {
						BindingEntry bindingEntry = new BindingEntryImpl();
						bindingEntry.setCustomerID(recipient.getCustomerID());
						bindingEntry.setMailinglistID(mailinglistToSubscribe);
						bindingEntry.setUserStatus(UserStatus.Active.getStatusCode());
						bindingEntry.setUserType(BindingEntry.UserType.World.getTypeCode());
						bindingEntry.setMediaType(MediaTypes.EMAIL.getMediaCode());
						bindingEntry.setUserRemark("Set by " + admin.getUsername() + " via restful");
						
						boolean success = bindingEntryDao.updateBinding(bindingEntry, admin.getCompanyID());
						if (!success) {
							bindingEntryDao.insertNewBinding(bindingEntry, admin.getCompanyID());
						}
					}
					
					userActivityLogDao.addAdminUseOfFeature(admin, "restful/recipient", new Date());
					userActivityLogDao.writeUserActivityLog(admin, "restful/recipient POST", "" + recipient.getCustomerID());
					
					CaseInsensitiveMap<String, Object> customerDataMap = recipientDao.getCustomerData(admin.getCompanyID(), recipient.getCustomerID(), TimeZone.getTimeZone(admin.getAdminTimezone()));
					JsonObject customerJsonObject = new JsonObject();
					for (String key : AgnUtils.sortCollectionWithItemsFirst(customerDataMap.keySet(), "customer_id", "email")) {
						customerJsonObject.add(key.toLowerCase(), customerDataMap.get(key));
					}
					return customerJsonObject;
				} else if (JsonToken.JsonArray_Open == readJsonToken) {
					// Multiple customer data
					if (!admin.permissionAllowed(Permission.IMPORT_MODE_ADD)) {
						throw new RestfulClientException("Authorization failed: Access denied '" + Permission.IMPORT_MODE_ADD.toString() + "'");
					}
					
					CaseInsensitiveMap<String, ProfileField> profileFields = columnInfoService.getColumnInfoMap(admin.getCompanyID(), admin.getAdminID());
			
					File importTempFileDirectory = new File(IMPORT_FILE_DIRECTORY + "/" + admin.getCompanyID());
					if (!importTempFileDirectory.exists()) {
						importTempFileDirectory.mkdirs();
					}
					
					String requestUUID = AgnUtils.generateNewUUID().toString().replace("-", "").toUpperCase();
					
					File temporaryImportFile = new File(importTempFileDirectory.getAbsolutePath() + "/Recipientimportjson_" + requestUUID + ".json");
					int mailinglistID = 0;
					if (mailinglistToSubscribe != null) {
						mailinglistID = mailinglistToSubscribe;
					}
					ImportMode importMode = ImportMode.ADD;
					String keyColumn = "email";
					
					int foundItems = 0;
					// Read recipient data entries and write them to a importable json file
					try (FileOutputStream importFileOutputStream = new FileOutputStream(temporaryImportFile);
						JsonWriter jsonWriter = new JsonWriter(importFileOutputStream)) {
						jsonWriter.openJsonArray();
						while ((readJsonToken = jsonReader.readNextToken()) == JsonToken.JsonObject_Open) {
							JsonObject jsonObject = new JsonObject();
	
							while ((readJsonToken = jsonReader.readNextToken()) == JsonToken.JsonObject_PropertyKey) {
								String propertyKey = (String) jsonReader.getCurrentObject();
								if (!profileFields.containsKey(propertyKey)) {
									throw new RestfulClientException("Expected recipient property key '" + StringUtils.join(profileFields.keySet(), "', '") + "' but was: '" + jsonReader.getCurrentObject() + "'");
								} else if (jsonObject.containsPropertyKey(propertyKey)) {
									throw new RestfulClientException("Duplicate recipient property key: " + jsonReader.getCurrentObject());
								}
								readJsonToken = jsonReader.readNextToken();
								if (JsonToken.JsonSimpleValue != readJsonToken) {
									throw new RestfulClientException("Expected JSON item type 'JsonSimpleValue' but was: " + readJsonToken);
								}
								jsonObject.add(propertyKey, jsonReader.getCurrentObject());
							}
	
							if (JsonToken.JsonObject_Close != readJsonToken) {
								throw new RestfulClientException("Expected JSON item type 'JsonObject_Close' was: " + readJsonToken);
							}
	
							jsonWriter.add(jsonObject);
							foundItems++;
						}
						jsonWriter.closeJsonArray();
					}
		
					if (JsonToken.JsonArray_Close != readJsonToken) {
						throw new RestfulClientException("Expected JSON item type 'JsonArray_Close' was: " + readJsonToken);
					}
			
					CustomerImportStatus status = importRecipients(admin, importMode, keyColumn, mailinglistID, temporaryImportFile, requestUUID);
					
					userActivityLogDao.addAdminUseOfFeature(admin, "restful/recipient", new Date());
					userActivityLogDao.writeUserActivityLog(admin, "restful/recipient POST", "IMPORT");
					
					JsonObject result = new JsonObject();
					if (status.getFatalError() != null) {
						throw new RestfulClientException(status.getFatalError());
					} else {
						result.add("Recipients", foundItems);
						result.add("Inserted", status.getInserted());
						
						boolean foundErrors = false;
						JsonObject errorsObject = new JsonObject();
						for (Entry<ImportErrorType, Integer> errorEntry : status.getErrors().entrySet()) {
							errorsObject.add(errorEntry.getKey().name(), errorEntry.getValue());
							if (errorEntry.getValue() > 0) {
								foundErrors = true;
							}
						}
						if (foundErrors) {
							result.add("Errors", errorsObject);
						}
					}
					return result;
				} else {
					throw new RestfulClientException("Expected root JSON item type 'JsonObject' or 'JsonArray' but was: " + readJsonToken);
				}
			}
		}
	}

	/**
	 * Create a new customer entry or update an existing customer entry (also bulk/multiple)
	 * 
	 * @param request
	 * @param requestDataFile
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object createOrUpdateCustomer(HttpServletRequest request, File requestDataFile, ComAdmin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.RECIPIENT_CHANGE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.RECIPIENT_CHANGE.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 1);
		
		Integer mailinglistToSubscribe = null;
		if (StringUtils.isNotBlank(request.getParameter("mailinglist"))) {
			if (!AgnUtils.isNumber(request.getParameter("mailinglist"))) {
				throw new RestfulClientException("Invalid parameter mailinglist: '" + request.getParameter("mailinglist") + "'");
			}
			mailinglistToSubscribe = Integer.parseInt(request.getParameter("mailinglist"));
			Mailinglist mailinglist = mailinglistDao.getMailinglist(mailinglistToSubscribe, admin.getCompanyID());
			if (mailinglist == null || mailinglist.isRemoved()) {
				throw new RestfulClientException("Invalid mailinglist: '" + mailinglistToSubscribe + "'");
			}
		}
		
		try (InputStream inputStream = new FileInputStream(requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				// Read root JSON element
				JsonToken readJsonToken = jsonReader.readNextToken();
				if (JsonToken.JsonObject_Open == readJsonToken) {
					// Single customer data
					CaseInsensitiveMap<String, ProfileField> profileFields = columnInfoService.getColumnInfoMap(admin.getCompanyID(), admin.getAdminID());
					JsonObject jsonObject = new JsonObject();
					while ((readJsonToken = jsonReader.readNextToken()) == JsonToken.JsonObject_PropertyKey) {
						String propertyKey = (String) jsonReader.getCurrentObject();
						if (!profileFields.containsKey(propertyKey)) {
							throw new RestfulClientException("Expected recipient property key '" + StringUtils.join(profileFields.keySet(), "', '") + "' but was: '" + jsonReader.getCurrentObject() + "'");
						} else if (jsonObject.containsPropertyKey(propertyKey)) {
							throw new RestfulClientException("Duplicate recipient property key: " + jsonReader.getCurrentObject());
						}
						readJsonToken = jsonReader.readNextToken();
						if (JsonToken.JsonSimpleValue != readJsonToken) {
							throw new RestfulClientException("Expected JSON item type 'JsonSimpleValue' but was: " + readJsonToken);
						}
						jsonObject.add(propertyKey, jsonReader.getCurrentObject());
					}

					if (JsonToken.JsonObject_Close != readJsonToken) {
						throw new RestfulClientException("Expected JSON item type 'JsonObject_Close' was: " + readJsonToken);
					}

					Recipient recipient = new RecipientImpl();
					recipient.setCompanyID(admin.getCompanyID());
					Map<String, Object> custParameters = recipient.getCustParameters();
					for (Entry<String, Object> entry : jsonObject.entrySet()) {
						if (!"customer_id".equalsIgnoreCase(entry.getKey()) && !ComCompanyDaoImpl.STANDARD_FIELD_BOUNCELOAD.equalsIgnoreCase(entry.getKey())) {
							custParameters.put(entry.getKey(), entry.getValue());
						}
					}
					
					// Set default data if missing
					if (!custParameters.containsKey("gender")) {
						custParameters.put("gender", Gender.UNKNOWN.getStorageValue());
					}
					if (!custParameters.containsKey("mailtype")) {
						custParameters.put("mailtype", MailType.HTML.getIntValue());
					}
					
					if (restfulContext.length == 1) {
						String requestedRecipientKeyValue = restfulContext[0];
						if (AgnUtils.isEmailValid(requestedRecipientKeyValue)) {
							int customerID = recipientDao.findByColumn(admin.getCompanyID(), "email", requestedRecipientKeyValue);
							if (customerID > 0) {
								custParameters.put("customer_id", customerID);
							}
							recipientDao.updateInDB(recipient, false);
						} else if (AgnUtils.isNumber(requestedRecipientKeyValue)) {
							int customerID = recipientDao.findByColumn(admin.getCompanyID(), "customer_id", requestedRecipientKeyValue);
							if (customerID > 0) {
								custParameters.put("customer_id", customerID);
							}
							recipientDao.updateInDB(recipient, false);
						} else {
							throw new RestfulClientException("Invalid requested recipient key: " + requestedRecipientKeyValue);
						}
					} else {
						if (custParameters.get("email") == null) {
							throw new RestfulClientException("Invalid missing data for 'email'");
						} else if (!(custParameters.get("email") instanceof String)) {
							throw new RestfulClientException("Invalid data for 'email'");
						} else if (StringUtils.isBlank((String) custParameters.get("email"))) {
							throw new RestfulClientException("Invalid empty data for 'email'");
						} else {
							int customerID = recipientDao.findByColumn(admin.getCompanyID(), "email", (String) custParameters.get("email"));
							if (customerID > 0) {
								custParameters.put("customer_id", customerID);
							}
							recipientDao.updateInDB(recipient, false);
						}
					}
					
					if (mailinglistToSubscribe != null) {
						BindingEntry bindingEntry = new BindingEntryImpl();
						bindingEntry.setCustomerID(recipient.getCustomerID());
						bindingEntry.setMailinglistID(mailinglistToSubscribe);
						bindingEntry.setUserStatus(UserStatus.Active.getStatusCode());
						bindingEntry.setUserType(BindingEntry.UserType.World.getTypeCode());
						bindingEntry.setMediaType(MediaTypes.EMAIL.getMediaCode());
						bindingEntry.setUserRemark("Set by " + admin.getUsername() + " via restful");
						
						boolean success = bindingEntryDao.updateBinding(bindingEntry, admin.getCompanyID());
						if (!success) {
							bindingEntryDao.insertNewBinding(bindingEntry, admin.getCompanyID());
						}
					}
					
					userActivityLogDao.addAdminUseOfFeature(admin, "restful/recipient", new Date());
					userActivityLogDao.writeUserActivityLog(admin, "restful/recipient PUT", "" + recipient.getCustomerID());
					
					CaseInsensitiveMap<String, Object> customerDataMap = recipientDao.getCustomerData(admin.getCompanyID(), recipient.getCustomerID(), TimeZone.getTimeZone(admin.getAdminTimezone()));
					JsonObject customerJsonObject = new JsonObject();
					for (String key : AgnUtils.sortCollectionWithItemsFirst(customerDataMap.keySet(), "customer_id", "email")) {
						customerJsonObject.add(key.toLowerCase(), customerDataMap.get(key));
					}
					return customerJsonObject;
				} else if (JsonToken.JsonArray_Open == readJsonToken) {
					// Multiple customer data
					if (!admin.permissionAllowed(Permission.IMPORT_MODE_ADD_UPDATE)) {
						throw new RestfulClientException("Authorization failed: Access denied '" + Permission.IMPORT_MODE_ADD_UPDATE.toString() + "'");
					}
					
					CaseInsensitiveMap<String, ProfileField> profileFields = columnInfoService.getColumnInfoMap(admin.getCompanyID(), admin.getAdminID());
			
					File importTempFileDirectory = new File(IMPORT_FILE_DIRECTORY + "/" + admin.getCompanyID());
					if (!importTempFileDirectory.exists()) {
						importTempFileDirectory.mkdirs();
					}
					
					String requestUUID = AgnUtils.generateNewUUID().toString().replace("-", "").toUpperCase();
					
					File temporaryImportFile = new File(importTempFileDirectory.getAbsolutePath() + "/Recipientimportjson_" + requestUUID + ".json");
					int mailinglistID = 0;
					if (mailinglistToSubscribe != null) {
						mailinglistID = mailinglistToSubscribe;
					}
					ImportMode importMode = ImportMode.ADD_AND_UPDATE;
					String keyColumn = "email";
						
					// Read recipient data entries and write them to a importable json file
					int foundItems = 0;
					try (FileOutputStream importFileOutputStream = new FileOutputStream(temporaryImportFile);
						JsonWriter jsonWriter = new JsonWriter(importFileOutputStream)) {
						jsonWriter.openJsonArray();
						while ((readJsonToken = jsonReader.readNextToken()) == JsonToken.JsonObject_Open) {
							JsonObject jsonObject = new JsonObject();
	
							while ((readJsonToken = jsonReader.readNextToken()) == JsonToken.JsonObject_PropertyKey) {
								String propertyKey = (String) jsonReader.getCurrentObject();
								if (!profileFields.containsKey(propertyKey)) {
									throw new RestfulClientException("Expected recipient property key '" + StringUtils.join(profileFields.keySet(), "', '") + "' but was: '" + jsonReader.getCurrentObject() + "'");
								} else if (jsonObject.containsPropertyKey(propertyKey)) {
									throw new RestfulClientException("Duplicate recipient property key: " + jsonReader.getCurrentObject());
								}
								readJsonToken = jsonReader.readNextToken();
								if (JsonToken.JsonSimpleValue != readJsonToken) {
									throw new RestfulClientException("Expected JSON item type 'JsonSimpleValue' but was: " + readJsonToken);
								}
								jsonObject.add(propertyKey, jsonReader.getCurrentObject());
							}
	
							if (JsonToken.JsonObject_Close != readJsonToken) {
								throw new RestfulClientException("Expected JSON item type 'JsonObject_Close' was: " + readJsonToken);
							}
	
							jsonWriter.add(jsonObject);
							foundItems++;
						}
						jsonWriter.closeJsonArray();
					}
		
					if (JsonToken.JsonArray_Close != readJsonToken) {
						throw new RestfulClientException("Expected JSON item type 'JsonArray_Close' was: " + readJsonToken);
					}
			
					CustomerImportStatus status = importRecipients(admin, importMode, keyColumn, mailinglistID, temporaryImportFile, requestUUID);
					
					userActivityLogDao.addAdminUseOfFeature(admin, "restful/recipient", new Date());
					userActivityLogDao.writeUserActivityLog(admin, "restful/recipient PUT", "IMPORT");
					
					JsonObject result = new JsonObject();
					if (status.getFatalError() != null) {
						throw new RestfulClientException(status.getFatalError());
					} else {
						result.add("Recipients", foundItems);
						result.add("Inserted", status.getInserted());
						result.add("Updated", status.getUpdated());
						
						boolean foundErrors = false;
						JsonObject errorsObject = new JsonObject();
						for (Entry<ImportErrorType, Integer> errorEntry : status.getErrors().entrySet()) {
							errorsObject.add(errorEntry.getKey().name(), errorEntry.getValue());
							if (errorEntry.getValue() > 0) {
								foundErrors = true;
							}
						}
						if (foundErrors) {
							result.add("Errors", errorsObject);
						}
					}
					return result;
				} else {
					throw new RestfulClientException("Expected root JSON item type 'JsonObject' or 'JsonArray' but was: " + readJsonToken);
				}
			}
		}
	}
	
	private CustomerImportStatus importRecipients(ComAdmin admin, ImportMode importMode, String keyColumn, int mailinglistID, File temporaryImportFile, String sessionID) throws Exception {
		ImportProfile importProfile = new ImportProfileImpl();
		importProfile.setCompanyId(admin.getCompanyID());
		importProfile.setAdminId(admin.getAdminID());
		importProfile.setUpdateAllDuplicates(true);
		importProfile.setDateFormat(DateFormat.ISO8601.getIntValue());
		importProfile.setImportMode(importMode.getIntValue());
		importProfile.setCheckForDuplicates(CheckForDuplicates.COMPLETE.getIntValue());
		importProfile.setNullValuesAction(0);
		importProfile.setKeyColumn(keyColumn);
		importProfile.setAutoMapping(true);
		importProfile.setDefaultMailType(MailType.HTML.getIntValue());
		importProfile.setDatatype("JSON"); // use JSON Import
		importProfile.setCharset(Charset.UTF_8.getIntValue());

		DatasourceDescription dsDescription = new DatasourceDescriptionImpl();
		dsDescription.setId(0);
		dsDescription.setCompanyID(admin.getCompanyID());
		dsDescription.setSourcegroupID(2);
		dsDescription.setCreationDate(new Date());
		dsDescription.setDescription(temporaryImportFile.getName());
		dsDescription.setDescription2("Restful-Import");
		datasourceDescriptionDao.save(dsDescription);

		List<Integer> mailingListIdsToAssign = new ArrayList<>();
		if (mailinglistID != 0) {
			mailingListIdsToAssign.add(mailinglistID);
		}

		ProfileImportWorker profileImportWorker = profileImportWorkerFactory.getProfileImportWorker(
			false, // Not interactive mode, because there is no error edit GUI
			mailingListIdsToAssign,
			sessionID,
			admin,
			dsDescription.getId(),
			importProfile,
			new RemoteFile(temporaryImportFile.getName(), temporaryImportFile, -1),
			new CustomerImportStatusImpl());

		if (admin.permissionAllowed(Permission.RECIPIENT_GENDER_EXTENDED)) {
			profileImportWorker.setMaxGenderValue(ConfigService.MAX_GENDER_VALUE_EXTENDED);
		} else {
			profileImportWorker.setMaxGenderValue(ConfigService.MAX_GENDER_VALUE_BASIC);
		}

		profileImportWorker.call();

		if (profileImportWorker.getError() != null) {
			throw profileImportWorker.getError();
		} else {
			return profileImportWorker.getStatus();
		}
	}

	@Override
	public ResponseType getResponseType() {
		return ResponseType.JSON;
	}
}
