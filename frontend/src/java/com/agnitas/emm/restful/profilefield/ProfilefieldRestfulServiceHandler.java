/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.profilefield;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.agnitas.util.SafeString;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import com.agnitas.emm.restful.BaseRequestResponse;
import com.agnitas.emm.restful.ErrorCode;
import com.agnitas.emm.restful.JsonRequestResponse;
import com.agnitas.emm.restful.ResponseType;
import com.agnitas.emm.restful.RestfulClientException;
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
 * https://<system.url>/restful/profilefield
 */
public class ProfilefieldRestfulServiceHandler implements RestfulServiceHandler {
	public static final String NAMESPACE = "profilefield";

    private ConfigService configService;
    private AdminService adminService;
    private RecipientFieldService recipientFieldService;
	private RestfulUserActivityLogDao userActivityLogDao;
	
    public ProfilefieldRestfulServiceHandler(ConfigService configService, AdminService adminService, RecipientFieldService recipientFieldService, RestfulUserActivityLogDao userActivityLogDao) {
		this.configService = Objects.requireNonNull(configService);
		this.adminService = Objects.requireNonNull(adminService);
		this.recipientFieldService = Objects.requireNonNull(recipientFieldService);
		this.userActivityLogDao = Objects.requireNonNull(userActivityLogDao);
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) throws Exception {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, Admin admin, byte[] requestData, File requestDataFile, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod, boolean extendedLogging) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(getProfilefieldData(request, admin)));
		} else if (requestMethod == RequestMethod.DELETE) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(deleteProfilefield(request, admin)));
		} else if ((requestData == null || requestData.length == 0) && (requestDataFile == null || requestDataFile.length() <= 0)) {
			restfulResponse.setError(new RestfulClientException("Missing request data"), ErrorCode.REQUEST_DATA_ERROR);
		} else if (requestMethod == RequestMethod.POST) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createNewProfilefield(request, requestData, requestDataFile, admin)));
		} else if (requestMethod == RequestMethod.PUT) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(createOrUpdateProfilefield(request, requestData, requestDataFile, admin)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	/**
	 * Return a single or multiple profilefield data sets
	 * 
	 */
	private Object getProfilefieldData(HttpServletRequest request, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.PROFILEFIELD_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.PROFILEFIELD_SHOW.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 1);
		
		if (restfulContext.length == 1) {
			String requestedProfilefieldName = restfulContext[0];
			
			if (StringUtils.isBlank(requestedProfilefieldName)) {
				throw new RestfulClientException("Invalid empty requested profilefield name");
			} else {
				try {
					SafeString.getSafeDbColumnName (requestedProfilefieldName);
				} catch (Exception e) {
					throw new RestfulClientException("Invalid requested profilefield name: " + requestedProfilefieldName, e);
				}
			}
			
			RecipientFieldDescription profileField = recipientFieldService.getRecipientField(admin.getCompanyID(), requestedProfilefieldName);
			if (profileField == null) {
				throw new RestfulClientException("Invalid requested profilefield name: " + requestedProfilefieldName);
			} else {
				JsonObject profilefieldJsonObject = getProfileFieldJsonObject(admin.getCompanyID(), profileField);
				
				userActivityLogDao.addAdminUseOfFeature(admin, "restful/profilefield", new Date());
				writeActivityLog(requestedProfilefieldName, request, admin);

				return profilefieldJsonObject;
			}
		} else {
			JsonArray result = new JsonArray();
			List<RecipientFieldDescription> allProfileFields = recipientFieldService.getRecipientFields(admin.getCompanyID());
			for (RecipientFieldDescription profileField : sortProfileFields(allProfileFields, "customer_id", "email")) {
				JsonObject profilefieldJsonObject = new JsonObject();
				profilefieldJsonObject.add("name", profileField.getColumnName().toLowerCase());
				profilefieldJsonObject.add("shortname", profileField.getShortName());
				profilefieldJsonObject.add("type", profileField.getSimpleDataType().toString());

				if (profileField.getSimpleDataType() == SimpleDataType.Characters
						|| profileField.getSimpleDataType() == SimpleDataType.Blob) {
					profilefieldJsonObject.add("length", profileField.getCharacterLength());					
				}
				
				profilefieldJsonObject.add("nullable", profileField.isNullable());
				
				profilefieldJsonObject.add("sortIndex", profileField.getSortOrder());
				
				result.add(profilefieldJsonObject);
			}

			userActivityLogDao.addAdminUseOfFeature(admin, "restful/profilefield", new Date());
			writeActivityLog("ALL", request, admin);

			return result;
		}
	}

	private JsonObject getProfileFieldJsonObject(int companyID, RecipientFieldDescription profileField) {
		JsonObject profilefieldJsonObject = new JsonObject();
		profilefieldJsonObject.add("name", profileField.getColumnName().toLowerCase());
		profilefieldJsonObject.add("shortname", profileField.getShortName());
		profilefieldJsonObject.add("type", profileField.getSimpleDataType().toString());

		if (profileField.getSimpleDataType() == SimpleDataType.Characters
				|| profileField.getSimpleDataType() == SimpleDataType.Blob) {
			profilefieldJsonObject.add("length", profileField.getCharacterLength());					
		}
		
		profilefieldJsonObject.add("nullable", profileField.isNullable());
		
		profilefieldJsonObject.add("creation", profileField.getCreationDate());
		profilefieldJsonObject.add("change", profileField.getChangeDate());
		profilefieldJsonObject.add("historized", profileField.isHistorized());
		profilefieldJsonObject.add("modeEdit", profileField.getDefaultPermission().name());

		profilefieldJsonObject.add("description", profileField.getDescription());
		profilefieldJsonObject.add("defaultValue", profileField.getDefaultValue());
		
		if (profileField.getAllowedValues() != null && profileField.getAllowedValues().size() > 0) {
			JsonArray allowedValues = new JsonArray();
			for (String value : profileField.getAllowedValues()) {
				allowedValues.add(value);
			}
			profilefieldJsonObject.add("allowedValues", allowedValues);
		}
		
		Map<Integer, String> adminNamesMap = adminService.getAdminsNamesMap(companyID);
		
		if (profileField.getPermissions() != null) {
			Set<Integer> readOnlyUsers = new HashSet<>();
			Set<Integer> notVisibleUsers = new HashSet<>();
			Set<Integer> editableUsers = new HashSet<>();
			if (profileField.getPermissions() != null) {
				for (Entry<Integer, ProfileFieldMode> permission : profileField.getPermissions().entrySet()) {
					if (permission.getKey() > 0) {
						if (permission.getValue() == ProfileFieldMode.ReadOnly) {
							readOnlyUsers.add(permission.getKey());
						} else if (permission.getValue() == ProfileFieldMode.NotVisible) {
							notVisibleUsers.add(permission.getKey());
						} else if (permission.getValue() == ProfileFieldMode.Editable) {
							editableUsers.add(permission.getKey());
						}
					}
				}
			}
			
			if (readOnlyUsers.size() > 0 && profileField.getDefaultPermission() != ProfileFieldMode.ReadOnly) {
				JsonArray readOnlyUsersArray = new JsonArray();
				for (int adminID : readOnlyUsers) {
					readOnlyUsersArray.add(adminNamesMap.get(adminID));
				}
				profilefieldJsonObject.add("readOnlyUsers", readOnlyUsersArray);
			}
			
			if (notVisibleUsers.size() > 0 && profileField.getDefaultPermission() != ProfileFieldMode.NotVisible) {
				JsonArray notVisibleUsersArray = new JsonArray();
				for (int adminID : notVisibleUsers) {
					notVisibleUsersArray.add(adminNamesMap.get(adminID));
				}
				profilefieldJsonObject.add("notVisibleUsers", notVisibleUsersArray);
			}
			
			if (editableUsers.size() > 0 && profileField.getDefaultPermission() != ProfileFieldMode.Editable) {
				JsonArray editableUsersArray = new JsonArray();
				for (int adminID : editableUsers) {
					editableUsersArray.add(adminNamesMap.get(adminID));
				}
				profilefieldJsonObject.add("editableUsers", editableUsersArray);
			}
		}

		return profilefieldJsonObject;
	}

	/**
	 * Delete a single profilefield
	 * 
	 */
	private Object deleteProfilefield(HttpServletRequest request, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.PROFILEFIELD_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.PROFILEFIELD_SHOW.toString() + "'");
		}
		
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 1, 1);
		String requestedProfilefieldName = restfulContext[0];
		
		if (StringUtils.isBlank(requestedProfilefieldName)) {
			throw new RestfulClientException("Invalid empty requested profilefield name");
		} else {
			try {
				SafeString.getSafeDbColumnName (requestedProfilefieldName);
			} catch (Exception e) {
				throw new RestfulClientException("Invalid requested profilefield name: " + requestedProfilefieldName, e);
			}
		}
		
		RecipientFieldDescription profileField = recipientFieldService.getRecipientField(admin.getCompanyID(), requestedProfilefieldName);
		if (profileField == null) {
			throw new RestfulClientException("Invalid requested profilefield name: " + requestedProfilefieldName);
		} else {
			for (String standardField : RecipientFieldService.RecipientStandardField.getAllRecipientStandardFieldColumnNames()) {
				if (standardField.trim().equalsIgnoreCase(requestedProfilefieldName)) {
					throw new RestfulClientException("Invalid requested profilefield name: Cannot remove standard columns");
				}
			}
			
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/profilefield", new Date());
			writeActivityLog(requestedProfilefieldName, request, admin);

			recipientFieldService.deleteRecipientField(admin.getCompanyID(), requestedProfilefieldName);
			
			return "Profilefield '" + requestedProfilefieldName + "' deleted";
		}
	}

	/**
	 * Create a new profilefield
	 * 
	 */
	private Object createNewProfilefield(HttpServletRequest request, byte[] requestData, File requestDataFile, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.PROFILEFIELD_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.PROFILEFIELD_SHOW.toString() + "'");
		} else if (!mayAddNewColumn(admin.getCompanyID())) {
			throw new RestfulClientException("Maximum number of profilefields exceeded. Current number of profilefields: " + recipientFieldService.getRecipientFields(admin.getCompanyID()).size());
		}
		
		String requestedProfilefieldName = null;
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 1);
		if (restfulContext.length == 1) {
			requestedProfilefieldName = restfulContext[0].toLowerCase();
		}
		
		JsonObject jsonObject;
		try (InputStream inputStream = RestfulServiceHandler.getRequestDataStream(requestData, requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				JsonNode jsonNode = jsonReader.read();
				if (JsonDataType.OBJECT == jsonNode.getJsonDataType()) {
					jsonObject = (JsonObject) jsonNode.getValue();
				} else {
					throw new RestfulClientException("Expected root JSON item type 'JsonObject' but was: " + jsonNode.getJsonDataType());
				}
			}
		}
		
		if (jsonObject.containsPropertyKey("name")) {
			if (jsonObject.get("name") instanceof String) {
				if (requestedProfilefieldName == null) {
					requestedProfilefieldName = ((String) jsonObject.get("name")).toLowerCase();
				} else if (!requestedProfilefieldName.equalsIgnoreCase((String) jsonObject.get("name"))) {
					throw new RestfulClientException("Invalid data for 'name'. Mismatch of request context and JSON data");
				}
			} else {
				throw new RestfulClientException("Invalid data for 'name'. String expected");
			}
		}
		
		if (recipientFieldService.getRecipientField(admin.getCompanyID(), requestedProfilefieldName) != null) {
			throw new RestfulClientException("Invalid data for 'name'. Profilefield already exists: " + requestedProfilefieldName);
		}
		
		RecipientFieldDescription profileField = new RecipientFieldDescription();
		profileField.setColumnName(requestedProfilefieldName);
		
		if (RecipientFieldService.RecipientStandardField.getAllRecipientStandardFieldColumnNames().contains(profileField.getColumnName().toLowerCase())) {
			throw new RestfulClientException("Standard column, which may not be altered: " + profileField.getColumnName());
		}
		
		readJsonData(admin.getCompanyID(), jsonObject, profileField);
		
		validateProfileFieldData(admin.getCompanyID(), profileField);
		if (jsonObject.containsPropertyKey("length") && profileField.getSimpleDataType() != SimpleDataType.Characters) {
			throw new RestfulClientException("Invalid data value for 'length'. Only data type Characters has a length attribute");
		}

		userActivityLogDao.addAdminUseOfFeature(admin, "restful/profilefield", new Date());
		writeActivityLog(requestedProfilefieldName, request, admin);
		
		try {
			recipientFieldService.saveRecipientField(admin.getCompanyID(), profileField);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Storage of profilefield data failed", e);
		}

		RecipientFieldDescription currentProfilefield = recipientFieldService.getRecipientField(admin.getCompanyID(), requestedProfilefieldName);
		
		return getProfileFieldJsonObject(admin.getCompanyID(), currentProfilefield);
	}

	/**
	 * Create a new profilefield or update an existing profilefield
	 * 
	 */
	private Object createOrUpdateProfilefield(HttpServletRequest request, byte[] requestData, File requestDataFile, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.PROFILEFIELD_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.PROFILEFIELD_SHOW.toString() + "'");
		}
		
		String requestedProfilefieldName = null;
		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 1);
		if (restfulContext.length == 1) {
			requestedProfilefieldName = restfulContext[0].toLowerCase();
		}
		
		JsonObject jsonObject;
		try (InputStream inputStream = RestfulServiceHandler.getRequestDataStream(requestData, requestDataFile)) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				JsonNode jsonNode = jsonReader.read();
				if (JsonDataType.OBJECT == jsonNode.getJsonDataType()) {
					jsonObject = (JsonObject) jsonNode.getValue();
				} else {
					throw new RestfulClientException("Expected root JSON item type 'JsonObject' but was: " + jsonNode.getJsonDataType());
				}
			}
		}
		
		if (jsonObject.containsPropertyKey("name")) {
			if (jsonObject.get("name") instanceof String) {
				if (requestedProfilefieldName == null) {
					requestedProfilefieldName = ((String) jsonObject.get("name")).toLowerCase();
				} else if (!requestedProfilefieldName.equalsIgnoreCase((String) jsonObject.get("name"))) {
					throw new RestfulClientException("Invalid data for 'name'. Mismatch of request context and JSON data");
				}
			} else {
				throw new RestfulClientException("Invalid data type for 'name'. String expected");
			}
		}
		
		RecipientFieldDescription profileField = recipientFieldService.getRecipientField(admin.getCompanyID(), requestedProfilefieldName);

		if (profileField == null) {
			if (!mayAddNewColumn(admin.getCompanyID())) {
				throw new RestfulClientException("Maximum number of profilefields exceeded. Current number of profilefields: " + recipientFieldService.getRecipientFields(admin.getCompanyID()).size());
			}
			
			profileField = new RecipientFieldDescription();
			profileField.setNullable(true);
		}

		readJsonData(admin.getCompanyID(), jsonObject, profileField);
		
		if (requestedProfilefieldName != null) {
			if (StringUtils.isBlank(profileField.getColumnName())) {
				profileField.setColumnName(requestedProfilefieldName);
			} else if (!profileField.getColumnName().equalsIgnoreCase(requestedProfilefieldName)) {
				throw new RestfulClientException("Invalid data for 'name'. Cannot change name of existing profilefield");
			}
		}

		if (RecipientFieldService.RecipientStandardField.getAllRecipientStandardFieldColumnNames().contains(profileField.getColumnName().toLowerCase())) {
			throw new RestfulClientException("Standard column, which may not be altered: " + profileField.getColumnName());
		}
		
		validateProfileFieldData(admin.getCompanyID(), profileField);
		if (jsonObject.containsPropertyKey("length") && profileField.getSimpleDataType() != SimpleDataType.Characters) {
			throw new RestfulClientException("Invalid data value for 'length'. Only data type Characters has a length attribute");
		}

		userActivityLogDao.addAdminUseOfFeature(admin, "restful/profilefield", new Date());
		writeActivityLog(requestedProfilefieldName, request, admin);

		try {
			recipientFieldService.saveRecipientField(admin.getCompanyID(), profileField);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Storage of profilefield data failed", e);
		}

		RecipientFieldDescription currentProfilefield = recipientFieldService.getRecipientField(admin.getCompanyID(), requestedProfilefieldName);
		return getProfileFieldJsonObject(admin.getCompanyID(), currentProfilefield);
	}
	
	private Collection<RecipientFieldDescription> sortProfileFields(Collection<RecipientFieldDescription> profileFields, String... keepItemsFirst) {
		List<RecipientFieldDescription> list = new ArrayList<>(profileFields);
		Collections.sort(list, new Comparator<RecipientFieldDescription>() {
			@Override
			public int compare(RecipientFieldDescription o1, RecipientFieldDescription o2) {
				if (o1 == o2) {
					return 0;
				} else if (o1 == null) {
					return 1;
				} else if (o2 == null) {
					return -1;
				} else if (o1.equals(o2)) {
					return 0;
				} else {
					String profilefieldName1 = o1.getColumnName().toLowerCase();
					String profilefieldName2 = o2.getColumnName().toLowerCase();
					
					if (o1.getSortOrder() < 1000 && o2.getSortOrder() < 1000) {
						if (o1.getSortOrder() < o2.getSortOrder()) {
							return -1;
						} else if (o1.getSortOrder() == o2.getSortOrder()) {
							if (profilefieldName1 != null && profilefieldName2 != null) {
								return profilefieldName1.compareTo(profilefieldName2);
							} else if (profilefieldName1 != null) {
								return 1;
							} else {
								return -1;
							}
						} else {
							return 1;
						}
					} else if (o1.getSortOrder() < 1000) {
						return -1;
					} else if (o2.getSortOrder() < 1000) {
						return 1;
					} else {
						for (String item : keepItemsFirst) {
							if (profilefieldName1.equalsIgnoreCase(item)) {
								return -1;
							}
						}
						
						for (String item : keepItemsFirst) {
							if (profilefieldName2.equalsIgnoreCase(item)) {
								return 1;
							}
						}
						
						return profilefieldName1.compareTo(profilefieldName2);
					}
				}
			}
		});
		return list;
	}

	private void readJsonData(int companyID, JsonObject jsonObject, RecipientFieldDescription profileField) throws Exception, RestfulClientException, IOException, FileNotFoundException {
		Map<Integer, String> adminNamesMap = null;
		
		if (jsonObject.containsPropertyKey("notVisibleUsers") && jsonObject.containsPropertyKey("visibleUsers")) {
			throw new RestfulClientException("Invalid data. Only one of 'notVisibleUsers' and 'visibleUsers' expected");
		}

		Set<Integer> newEditableUsers = null;
		Set<Integer> newReadOnlyUsers = null;
		Set<Integer> newNotVisibleUsers = null;
		
		for (Entry<String, Object> entry : jsonObject.entrySet()) {
			if ("name".equals(entry.getKey())) {
				// Attribute "name" is skipped, because it was handled before
			} else if ("type".equals(entry.getKey())) {
				if (entry.getValue() instanceof String) {
					if (profileField.getDatabaseDataType() == null) {
						SimpleDataType simpleDataType;
						try {
							simpleDataType = SimpleDataType.getFromString((String) entry.getValue());
						} catch (Exception e) {
							throw new RestfulClientException("Invalid value for property 'type': " + (String) entry.getValue(), e);
						}
						profileField.setSimpleDataType(simpleDataType);
						if (simpleDataType == SimpleDataType.Characters) {
							profileField.setDatabaseDataType("VARCHAR");
							profileField.setCharacterLength(100);
						} else if (simpleDataType == SimpleDataType.Date) {
							profileField.setDatabaseDataType("DATE");
						} else if (simpleDataType == SimpleDataType.DateTime) {
							profileField.setDatabaseDataType("DATETIME");
						} else if (simpleDataType == SimpleDataType.Float) {
							profileField.setDatabaseDataType("FLOAT");
						} else if (simpleDataType == SimpleDataType.Numeric) {
							profileField.setDatabaseDataType("INTEGER");
						} else if (simpleDataType == SimpleDataType.Blob) {
							profileField.setDatabaseDataType("DATE");
						} else {
							throw new RestfulClientException("Invalid value for property 'type': " + (String) entry.getValue());
						}
					} else if (profileField.getSimpleDataType() != SimpleDataType.getFromString((String) entry.getValue())) {
						throw new RestfulClientException("Invalid value for property 'type'. Cannot change type of existing profilefield");
					}
				} else {
					throw new RestfulClientException("Invalid data type for 'type'. String expected");
				}
			} else if ("shortname".equals(entry.getKey())) {
				if (entry.getValue() instanceof String) {
					profileField.setShortName((String) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'shortname'. String expected");
				}
			} else if ("description".equals(entry.getKey())) {
				if (entry.getValue() instanceof String) {
					profileField.setDescription((String) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'description'. String expected");
				}
			} else if ("sortIndex".equals(entry.getKey())) {
				if (entry.getValue() instanceof Integer) {
					profileField.setSortOrder((Integer) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'sortIndex'. Integer expected");
				}
			} else if ("defaultValue".equals(entry.getKey())) {
				if (entry.getValue() instanceof String) {
					profileField.setDefaultValue((String) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'defaultValue'. String expected");
				}
			} else if ("length".equals(entry.getKey())) {
				if (entry.getValue() instanceof Integer) {
					profileField.setCharacterLength((Integer) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'length'. Integer expected");
				}
			} else if ("nullable".equals(entry.getKey())) {
				if (entry.getValue() instanceof Boolean) {
					profileField.setNullable((Boolean) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'nullable'. Boolean expected");
				}
			} else if ("historized".equals(entry.getKey())) {
				if (entry.getValue() instanceof Boolean) {
					profileField.setHistorized((Boolean) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'historized'. Boolean expected");
				}
			} else if ("modeEdit".equals(entry.getKey())) {
				if (entry.getValue() instanceof Integer) {
					profileField.setDefaultPermission(ProfileFieldMode.getProfileFieldModeForStorageCode((Integer) entry.getValue()));
				} else if (entry.getValue() instanceof String) {
					profileField.setDefaultPermission(ProfileFieldMode.getProfileFieldModeForName((String) entry.getValue()));
				} else{
					throw new RestfulClientException("Invalid data type for 'modeEdit'. Integer or String expected");
				}
			} else if ("allowedValues".equals(entry.getKey())) {
				if (entry.getValue() instanceof JsonArray) {
					List<String> allowedValues = new ArrayList<>();
					for (Object item : ((JsonArray) entry.getValue())) {
						if (item instanceof String
								|| item instanceof Integer
								|| item instanceof Double
								|| item instanceof Float) {
							allowedValues.add(item.toString());
						} else {
							throw new RestfulClientException("Invalid data value for 'allowedValues': " + entry.getValue());
						}
					}
					profileField.setAllowedValues(allowedValues);
				} else {
					throw new RestfulClientException("Invalid data type for 'allowedValues'. Array expected");
				}
			} else if ("editableUsers".equals(entry.getKey()) || "visibleUsers".equals(entry.getKey())) {
				if (newEditableUsers != null) {
					throw new RestfulClientException("Invalid multiple data for 'editableUsers' (alias 'visibleUsers'), because it is only allowed once");
				} else if (entry.getValue() instanceof JsonArray) {
					newEditableUsers = new HashSet<>();
					if (adminNamesMap == null) {
						adminNamesMap = adminService.getAdminsNamesMap(companyID);
					}
					
					for (Object item : ((JsonArray) entry.getValue())) {
						if (item instanceof Integer) {
							if (adminNamesMap.containsKey(item)) {
								newEditableUsers.add((Integer) item);
							} else {
								throw new RestfulClientException("Invalid userid item for 'editableUsers' (alias 'visibleUsers'): " + item.toString());
							}
						} else if (item instanceof String) {
							int adminID = 0;
							for (Entry<Integer, String> adminEntry : adminNamesMap.entrySet()) {
								if (adminEntry.getValue().equals(item)) {
									adminID = adminEntry.getKey();
									break;
								}
							}
							if (adminID != 0) {
								newEditableUsers.add(adminID);
							} else {
								throw new RestfulClientException("Invalid user item for 'editableUsers' (alias 'visibleUsers'): " + item.toString());
							}
						}
					}

					for (Integer adminID : newEditableUsers) {
						if (newReadOnlyUsers != null && newReadOnlyUsers.contains(adminID)) {
							throw new RestfulClientException("Invalid user item for 'editableUsers' (alias 'visibleUsers'), because it is already included in 'readOnlyUsers': " + adminNamesMap.get(adminID) + " (" + adminID + ")");
						} else if (newNotVisibleUsers != null && newNotVisibleUsers.contains(adminID)) {
							throw new RestfulClientException("Invalid user item for 'editableUsers' (alias 'visibleUsers'), because it is already included in 'notVisibleUsers': " + adminNamesMap.get(adminID) + " (" + adminID + ")");
						}
					}
				} else {
					throw new RestfulClientException("Invalid data type for 'visibleUsers'. Array expected");
				}
			} else if ("readOnlyUsers".equals(entry.getKey())) {
				if (newReadOnlyUsers != null) {
					throw new RestfulClientException("Invalid multiple data for 'readOnlyUsers', because it is only allowed once");
				} else if (entry.getValue() instanceof JsonArray) {
					newReadOnlyUsers = new HashSet<>();
					if (adminNamesMap == null) {
						adminNamesMap = adminService.getAdminsNamesMap(companyID);
					}
					
					for (Object item : ((JsonArray) entry.getValue())) {
						if (item instanceof Integer) {
							if (adminNamesMap.containsKey(item)) {
								newReadOnlyUsers.add((Integer) item);
							} else {
								throw new RestfulClientException("Invalid userid item for 'readOnlyUsers': " + item.toString());
							}
						} else if (item instanceof String) {
							int adminID = 0;
							for (Entry<Integer, String> adminEntry : adminNamesMap.entrySet()) {
								if (adminEntry.getValue().equals(item)) {
									adminID = adminEntry.getKey();
									break;
								}
							}
							if (adminID != 0) {
								newReadOnlyUsers.add(adminID);
							} else {
								throw new RestfulClientException("Invalid user item for 'readOnlyUsers': " + item.toString());
							}
						}
					}

					for (Integer adminID : newReadOnlyUsers) {
						if (newEditableUsers != null && newEditableUsers.contains(adminID)) {
							throw new RestfulClientException("Invalid user item for 'readOnlyUsers', because it is already included in 'editableUsers' (alias 'visibleUsers'): " + adminNamesMap.get(adminID) + " (" + adminID + ")");
						} else if (newNotVisibleUsers != null && newNotVisibleUsers.contains(adminID)) {
							throw new RestfulClientException("Invalid user item for 'readOnlyUsers', because it is already included in 'notVisibleUsers': " + adminNamesMap.get(adminID) + " (" + adminID + ")");
						}
					}
				} else {
					throw new RestfulClientException("Invalid data type for 'readOnlyUsers'. Array expected");
				}
			} else if ("notVisibleUsers".equals(entry.getKey())) {
				if (newNotVisibleUsers != null) {
					throw new RestfulClientException("Invalid multiple data for 'notVisibleUsers', because it is only allowed once");
				} else if (entry.getValue() instanceof JsonArray) {
					newNotVisibleUsers = new HashSet<>();
					if (adminNamesMap == null) {
						adminNamesMap = adminService.getAdminsNamesMap(companyID);
					}
					
					for (Object item : ((JsonArray) entry.getValue())) {
						if (item instanceof Integer) {
							if (adminNamesMap.containsKey(item)) {
								newNotVisibleUsers.add((Integer) item);
							} else {
								throw new RestfulClientException("Invalid userid item for 'notVisibleUsers': " + item.toString());
							}
						} else if (item instanceof String) {
							int adminID = 0;
							for (Entry<Integer, String> adminEntry : adminNamesMap.entrySet()) {
								if (adminEntry.getValue().equals(item)) {
									adminID = adminEntry.getKey();
									break;
								}
							}
							if (adminID != 0) {
								newNotVisibleUsers.add(adminID);
							} else {
								throw new RestfulClientException("Invalid user item for 'notVisibleUsers': " + item.toString());
							}
						}
					}

					for (Integer adminID : newNotVisibleUsers) {
						if (newEditableUsers != null && newEditableUsers.contains(adminID)) {
							throw new RestfulClientException("Invalid user item for 'notVisibleUsers', because it is already included in 'editableUsers' (alias 'visibleUsers'): " + adminNamesMap.get(adminID) + " (" + adminID + ")");
						} else if (newReadOnlyUsers != null && newReadOnlyUsers.contains(adminID)) {
							throw new RestfulClientException("Invalid user item for 'notVisibleUsers', because it is already included in 'readOnlyUsers': " + adminNamesMap.get(adminID) + " (" + adminID + ")");
						}
					}
				} else {
					throw new RestfulClientException("Invalid data type for 'notVisibleUsers'. Array expected");
				}
			} else {
				throw new RestfulClientException("Invalid property '" + entry.getKey() + "' for profilefield");
			}
		}

		Set<Integer> currentEditableUsers = new HashSet<>();
		Set<Integer> currentReadOnlyUsers = new HashSet<>();
		Set<Integer> currentNotVisibleUsers = new HashSet<>();
		if (profileField.getPermissions() != null) {
			for (Entry<Integer, ProfileFieldMode> permission : profileField.getPermissions().entrySet()) {
				if (permission.getValue() == ProfileFieldMode.Editable) {
					currentEditableUsers.add(permission.getKey());
				} else if (permission.getValue() == ProfileFieldMode.ReadOnly) {
					currentReadOnlyUsers.add(permission.getKey());
				} else if (permission.getValue() == ProfileFieldMode.NotVisible) {
					currentNotVisibleUsers.add(permission.getKey());
				}
			}
		}
		
		Map<Integer, ProfileFieldMode> permissions = new HashMap<>();
		permissions.put(0, profileField.getDefaultPermission());
		if (profileField.getDefaultPermission() != ProfileFieldMode.Editable) {
			if (newEditableUsers != null) {
				for (int adminID : newEditableUsers) {
					permissions.put(adminID, ProfileFieldMode.Editable);
				}
			} else if (currentEditableUsers != null) {
				for (int adminID : currentEditableUsers) {
					if ((newReadOnlyUsers == null || !newReadOnlyUsers.contains(adminID))
							&& (newNotVisibleUsers == null || !newNotVisibleUsers.contains(adminID))) {
						permissions.put(adminID, ProfileFieldMode.Editable);
					}
				}
			}
		}
		if (profileField.getDefaultPermission() != ProfileFieldMode.ReadOnly) {
			if (newReadOnlyUsers != null) {
				for (int adminID : newReadOnlyUsers) {
					permissions.put(adminID, ProfileFieldMode.ReadOnly);
				}
			} else if (currentReadOnlyUsers != null) {
				for (int adminID : currentReadOnlyUsers) {
					if ((newReadOnlyUsers == null || !newReadOnlyUsers.contains(adminID))
							&& (newNotVisibleUsers == null || !newNotVisibleUsers.contains(adminID))) {
						permissions.put(adminID, ProfileFieldMode.ReadOnly);
					}
				}
			}
		}
		if (profileField.getDefaultPermission() != ProfileFieldMode.NotVisible) {
			if (newNotVisibleUsers != null) {
				for (int adminID : newNotVisibleUsers) {
					permissions.put(adminID, ProfileFieldMode.NotVisible);
				}
			} else if (currentNotVisibleUsers != null) {
				for (int adminID : currentNotVisibleUsers) {
					if ((newReadOnlyUsers == null || !newReadOnlyUsers.contains(adminID))
							&& (newNotVisibleUsers == null || !newNotVisibleUsers.contains(adminID))) {
						permissions.put(adminID, ProfileFieldMode.NotVisible);
					}
				}
			}
		}
		profileField.setPermissions(permissions);
	}

	private void validateProfileFieldData(int companyID, RecipientFieldDescription profileField) throws RestfulClientException, Exception {
		try {
			SafeString.getSafeDbColumnName(profileField.getColumnName());
		} catch (Exception e) {
			throw new RestfulClientException("Invalid value for property 'name' for profilefield: " + profileField.getColumnName(), e);
		}
		
		if (StringUtils.isBlank(profileField.getShortName())) {
			profileField.setShortName(profileField.getColumnName());
		}
		
		RecipientFieldDescription profilefieldByShortname = recipientFieldService.getRecipientField(companyID, profileField.getShortName());
		if (profilefieldByShortname != null && !profilefieldByShortname.getColumnName().equalsIgnoreCase(profileField.getColumnName())) {
			throw new RestfulClientException("Invalid value for property 'shortname' for profilefield, already exists: " + profileField.getShortName());
		}
		
		if (profileField.getSimpleDataType() == null) {
			throw new RestfulClientException("Invalid empty value for property 'type' for profilefield");
		}
		
		if (StringUtils.isBlank(profileField.getShortName())) {
			profileField.setShortName(profileField.getColumnName());
		}
		
		if (profileField.getAllowedValues() != null) {
			for (String allowedValue : profileField.getAllowedValues()) {
				if (profileField.getSimpleDataType() == SimpleDataType.Numeric && !AgnUtils.isNumber(allowedValue)) {
					throw new RestfulClientException("Invalid allowedValue entry for data type 'Characters': " + allowedValue);
				} else if (profileField.getSimpleDataType() == SimpleDataType.Float && !AgnUtils.isDouble(allowedValue)) {
					throw new RestfulClientException("Invalid allowedValue entry for data type 'Float': " + allowedValue);
				} else if (profileField.getSimpleDataType() == SimpleDataType.Date) {
					throw new RestfulClientException("Invalid allowedValue entry for data type 'Date': " + allowedValue);
				} else if (profileField.getSimpleDataType() == SimpleDataType.DateTime) {
					throw new RestfulClientException("Invalid allowedValue entry for data type 'DateTime': " + allowedValue);
				} else if (profileField.getSimpleDataType() == SimpleDataType.Blob) {
					throw new RestfulClientException("Invalid allowedValue entry for data type 'Blob': " + allowedValue);
				}
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

    private boolean mayAddNewColumn(int companyID) throws Exception {
		if (companyID <= 0) {
    		return false;
    	} else {
    		int maxFields;
    		int systemMaxFields = configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfProfileFields, companyID);
    		int companyMaxFields = configService.getIntegerValue(ConfigValue.MaxFields, companyID);
    		if (companyMaxFields >= 0 && (companyMaxFields < systemMaxFields || systemMaxFields < 0)) {
    			maxFields = companyMaxFields;
    		} else {
    			maxFields = systemMaxFields;
    		}

    		List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(companyID);
    		List<RecipientFieldDescription> companySpecificFields = recipientFields.stream().filter(x -> !RecipientFieldService.RecipientStandardField.getAllRecipientStandardFieldColumnNames().contains(x.getColumnName())).collect(Collectors.toList());
			int currentFieldCount = companySpecificFields.size();
			
			if (currentFieldCount < maxFields) {
				return true;
			} else if (currentFieldCount < maxFields + ConfigValue.System_License_MaximumNumberOfProfileFields.getGracefulExtension()) {
				return true;
			} else {
				return false;
			}
    	}
	}
}
