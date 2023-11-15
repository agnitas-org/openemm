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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.agnitas.util.SafeString;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.beans.impl.ProfileFieldImpl;
import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.profilefields.service.ProfileFieldService;
import com.agnitas.emm.core.profilefields.service.ProfileFieldValidationService;
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
import com.agnitas.service.ColumnInfoService;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This restful service is available at:
 * https://<system.url>/restful/profilefield
 */
public class ProfilefieldRestfulServiceHandlerOld implements RestfulServiceHandler {
	public static final String NAMESPACE = "profilefield";

	private RestfulUserActivityLogDao userActivityLogDao;
	private ColumnInfoService columnInfoService;
    private ProfileFieldService profileFieldService;
    private AdminService adminService;
    private ProfileFieldValidationService profileFieldValidationService;
	
	@Required
	public void setUserActivityLogDao(RestfulUserActivityLogDao userActivityLogDao) {
		this.userActivityLogDao = userActivityLogDao;
	}
	
	@Required
	public void setColumnInfoService(ColumnInfoService columnInfoService) {
		this.columnInfoService = columnInfoService;
	}

	@Required
	public void setProfileFieldService(ProfileFieldService profileFieldService) {
		this.profileFieldService = profileFieldService;
	}

	@Required
	public void setAdminService(AdminService adminService) {
		this.adminService = adminService;
	}

	@Required
	public void setProfileFieldValidationService(ProfileFieldValidationService profileFieldValidationService) {
		this.profileFieldValidationService = profileFieldValidationService;
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
					throw new RestfulClientException("Invalid requested profilefield name: " + requestedProfilefieldName);
				}
			}
			
			ProfileField profileField = columnInfoService.getColumnInfo(admin.getCompanyID(), requestedProfilefieldName);
			if (profileField == null) {
				throw new RestfulClientException("Invalid requested profilefield name: " + requestedProfilefieldName);
			} else {
				Set<Integer> editableUsers = new HashSet<>();
				Set<Integer> readOnlyUsers = new HashSet<>();
				Set<Integer> notVisibleUsers = new HashSet<>();
				readProfilefieldAdminPermissions(profileField.getCompanyID(), profileField.getColumn(), editableUsers, readOnlyUsers, notVisibleUsers);
				JsonObject profilefieldJsonObject = getProfileFieldJsonObject(profileField, editableUsers, readOnlyUsers, notVisibleUsers);
				
				userActivityLogDao.addAdminUseOfFeature(admin, "restful/profilefield", new Date());
				writeActivityLog(requestedProfilefieldName, request, admin);

				return profilefieldJsonObject;
			}
		} else {
			JsonArray result = new JsonArray();
			CaseInsensitiveMap<String, ProfileField> allProfileFields = columnInfoService.getColumnInfoMap(admin.getCompanyID());
			for (ProfileField profileField : sortProfileFields(allProfileFields.values(), "customer_id", "email")) {
				JsonObject profilefieldJsonObject = new JsonObject();
				profilefieldJsonObject.add("name", profileField.getColumn().toLowerCase());
				profilefieldJsonObject.add("shortname", profileField.getShortname());
				profilefieldJsonObject.add("type", profileField.getSimpleDataType().toString());

				if (profileField.getSimpleDataType() == SimpleDataType.Characters
						|| profileField.getSimpleDataType() == SimpleDataType.Blob) {
					profilefieldJsonObject.add("length", profileField.getDataTypeLength());					
				}
				
				profilefieldJsonObject.add("nullable", profileField.getNullable());
				
				profilefieldJsonObject.add("sortIndex", profileField.getSort());
				
				result.add(profilefieldJsonObject);
			}

			userActivityLogDao.addAdminUseOfFeature(admin, "restful/profilefield", new Date());
			writeActivityLog("ALL", request, admin);

			return result;
		}
	}

	private JsonObject getProfileFieldJsonObject(ProfileField profileField, Set<Integer> editableUsers, Set<Integer> readOnlyUsers, Set<Integer> notVisibleUsers) {
		JsonObject profilefieldJsonObject = new JsonObject();
		profilefieldJsonObject.add("name", profileField.getColumn().toLowerCase());
		profilefieldJsonObject.add("shortname", profileField.getShortname());
		profilefieldJsonObject.add("type", profileField.getSimpleDataType().toString());

		if (profileField.getSimpleDataType() == SimpleDataType.Characters
				|| profileField.getSimpleDataType() == SimpleDataType.Blob) {
			profilefieldJsonObject.add("length", profileField.getDataTypeLength());					
		}
		
		profilefieldJsonObject.add("nullable", profileField.getNullable());
		
		profilefieldJsonObject.add("creation", profileField.getCreationDate());
		profilefieldJsonObject.add("change", profileField.getChangeDate());
		profilefieldJsonObject.add("historized", profileField.getHistorize());
		profilefieldJsonObject.add("modeEdit", profileField.getModeEdit().name());

		profilefieldJsonObject.add("description", profileField.getDescription());
		profilefieldJsonObject.add("defaultValue", profileField.getDefaultValue());
		
		if (profileField.getAllowedValues() != null && profileField.getAllowedValues().length > 0) {
			JsonArray allowedValues = new JsonArray();
			for (String value : profileField.getAllowedValues()) {
				allowedValues.add(value);
			}
			profilefieldJsonObject.add("allowedValues", allowedValues);
		}
		
		Map<Integer, String> adminNamesMap = adminService.getAdminsNamesMap(profileField.getCompanyID());

		if (editableUsers.size() > 0 && profileField.getModeEdit() != ProfileFieldMode.Editable) {
			JsonArray editableUsersArray = new JsonArray();
			for (int adminID : editableUsers) {
				editableUsersArray.add(adminNamesMap.get(adminID));
			}
			profilefieldJsonObject.add("editableUsers", editableUsersArray);
		}
		
		if (readOnlyUsers.size() > 0 && profileField.getModeEdit() != ProfileFieldMode.ReadOnly) {
			JsonArray readOnlyUsersArray = new JsonArray();
			for (int adminID : readOnlyUsers) {
				readOnlyUsersArray.add(adminNamesMap.get(adminID));
			}
			profilefieldJsonObject.add("readOnlyUsers", readOnlyUsersArray);
		}
		
		if (notVisibleUsers.size() > 0 && profileField.getModeEdit() != ProfileFieldMode.NotVisible) {
			JsonArray notVisibleUsersArray = new JsonArray();
			for (int adminID : notVisibleUsers) {
				notVisibleUsersArray.add(adminNamesMap.get(adminID));
			}
			profilefieldJsonObject.add("notVisibleUsers", notVisibleUsersArray);
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
				throw new RestfulClientException("Invalid requested profilefield name: " + requestedProfilefieldName);
			}
		}
		
		ProfileField profileField = columnInfoService.getColumnInfo(admin.getCompanyID(), requestedProfilefieldName);
		if (profileField == null) {
			throw new RestfulClientException("Invalid requested profilefield name: " + requestedProfilefieldName);
		} else {
			for (String standardField : ComCompanyDaoImpl.STANDARD_CUSTOMER_FIELDS) {
				if (standardField.trim().equalsIgnoreCase(requestedProfilefieldName)) {
					throw new RestfulClientException("Invalid requested profilefield name: Cannot remove standard columns");
				}
			}
			
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/profilefield", new Date());
			writeActivityLog(requestedProfilefieldName, request, admin);

			profileFieldService.removeProfileField(admin.getCompanyID(), requestedProfilefieldName);
			
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
		
		if (profileFieldService.exists(admin.getCompanyID(), requestedProfilefieldName)) {
			throw new RestfulClientException("Invalid data type for 'name'. Profilefield already exists: " + requestedProfilefieldName);
		}
		
		if (!profileFieldValidationService.mayAddNewColumn(admin.getCompanyID())) {
			throw new RestfulClientException("Maximum number of profilefields exceeded. Current number of profilefields: " + columnInfoService.getColumnInfos(admin.getCompanyID()).size());
		}
		
		ProfileField profileField = new ProfileFieldImpl();
		profileField.setCompanyID(admin.getCompanyID());
		profileField.setColumn(requestedProfilefieldName);
		Set<Integer> editableUsers = new HashSet<>();
		Set<Integer> readOnlyUsers = new HashSet<>();
		Set<Integer> notVisibleUsers = new HashSet<>();
		
		if (profileFieldValidationService.isStandardColumn(profileField.getColumn().toLowerCase())) {
			throw new RestfulClientException("Standard column, which may not be altered: " + profileField.getColumn());
		}
		
		readJsonData(jsonObject, profileField, editableUsers, readOnlyUsers, notVisibleUsers);
		if (!jsonObject.containsPropertyKey("visibleUsers") && !jsonObject.containsPropertyKey("editableUsers")) {
			editableUsers = null;
		}
		if (!jsonObject.containsPropertyKey("readOnlyUsers")) {
			readOnlyUsers = null;
		}
		if (!jsonObject.containsPropertyKey("notVisibleUsers")) {
			notVisibleUsers = null;
		}
		
		validateProfileFieldData(profileField);
		if (jsonObject.containsPropertyKey("length") && profileField.getSimpleDataType() != SimpleDataType.Characters) {
			throw new RestfulClientException("Invalid data value for 'length'. Only data type Characters has a length attribute");
		}

		userActivityLogDao.addAdminUseOfFeature(admin, "restful/profilefield", new Date());
		writeActivityLog(requestedProfilefieldName, request, admin);

		if (!profileFieldService.updateField(profileField, admin)) {
			throw new Exception("Storage of profilefield data failed");
		}
		
		columnInfoService.storeProfileFieldAdminPermissions(admin.getCompanyID(), profileField.getColumn(), editableUsers, readOnlyUsers, notVisibleUsers);

		ProfileField currentProfilefield = columnInfoService.getColumnInfo(admin.getCompanyID(), requestedProfilefieldName);
		Set<Integer> currentEditableUsers = new HashSet<>();
		Set<Integer> currentReadOnlyUsers = new HashSet<>();
		Set<Integer> currentNotVisibleUsers = new HashSet<>();
		readProfilefieldAdminPermissions(currentProfilefield.getCompanyID(), currentProfilefield.getColumn(), currentEditableUsers, currentReadOnlyUsers, currentNotVisibleUsers);
		return getProfileFieldJsonObject(currentProfilefield, currentEditableUsers, currentReadOnlyUsers, currentNotVisibleUsers);
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
		
		ProfileField profileField = columnInfoService.getColumnInfo(admin.getCompanyID(), requestedProfilefieldName);
		Set<Integer> editableUsers = new HashSet<>();
		Set<Integer> readOnlyUsers = new HashSet<>();
		Set<Integer> notVisibleUsers = new HashSet<>();
		if (profileField == null) {
			if (!profileFieldValidationService.mayAddNewColumn(admin.getCompanyID())) {
				throw new RestfulClientException("Maximum number of profilefields exceeded. Current number of profilefields: " + columnInfoService.getColumnInfos(admin.getCompanyID()).size());
			}
			
			profileField = new ProfileFieldImpl();
			profileField.setCompanyID(admin.getCompanyID());
		} else {
			readProfilefieldAdminPermissions(profileField.getCompanyID(), profileField.getColumn(), editableUsers, readOnlyUsers, notVisibleUsers);
		}
		
		if (StringUtils.isBlank(profileField.getColumn())) {
			profileField.setColumn(requestedProfilefieldName);
		} else if (!profileField.getColumn().equalsIgnoreCase(requestedProfilefieldName)) {
			throw new RestfulClientException("Invalid data type for 'name'. Cannot change name of existing profilefield");
		}

		if (profileFieldValidationService.isStandardColumn(profileField.getColumn().toLowerCase())) {
			throw new RestfulClientException("Standard column, which may not be altered: " + profileField.getColumn());
		}
		
		readJsonData(jsonObject, profileField, editableUsers, readOnlyUsers, notVisibleUsers);
		if (!jsonObject.containsPropertyKey("visibleUsers") && !jsonObject.containsPropertyKey("editableUsers")) {
			editableUsers = null;
		}
		if (!jsonObject.containsPropertyKey("readOnlyUsers")) {
			readOnlyUsers = null;
		}
		if (!jsonObject.containsPropertyKey("notVisibleUsers")) {
			notVisibleUsers = null;
		}
		
		validateProfileFieldData(profileField);
		if (jsonObject.containsPropertyKey("length") && profileField.getSimpleDataType() != SimpleDataType.Characters) {
			throw new RestfulClientException("Invalid data value for 'length'. Only data type Characters has a length attribute");
		}

		userActivityLogDao.addAdminUseOfFeature(admin, "restful/profilefield", new Date());
		writeActivityLog(requestedProfilefieldName, request, admin);

		if (!profileFieldService.updateField(profileField, admin)) {
			throw new Exception("Storage of profilefield data failed");
		}
		
		columnInfoService.storeProfileFieldAdminPermissions(admin.getCompanyID(), profileField.getColumn(), editableUsers, readOnlyUsers, notVisibleUsers);

		ProfileField currentProfilefield = columnInfoService.getColumnInfo(admin.getCompanyID(), requestedProfilefieldName);
		Set<Integer> currentEditableUsers = new HashSet<>();
		Set<Integer> currentReadOnlyUsers = new HashSet<>();
		Set<Integer> currentNotVisibleUsers = new HashSet<>();
		readProfilefieldAdminPermissions(currentProfilefield.getCompanyID(), currentProfilefield.getColumn(), currentEditableUsers, currentReadOnlyUsers, currentNotVisibleUsers);
		return getProfileFieldJsonObject(currentProfilefield, currentEditableUsers, currentReadOnlyUsers, currentNotVisibleUsers);
	}

	private void readProfilefieldAdminPermissions(int companyID, String columnName, Set<Integer> editableUsers, Set<Integer> readOnlyUsers, Set<Integer> notVisibleUsers) throws Exception {
		Map<Integer, ProfileFieldMode> profileFieldAdminPermissions = columnInfoService.getProfileFieldAdminPermissions(companyID, columnName);
		if (profileFieldAdminPermissions != null && profileFieldAdminPermissions.size() > 0) {
			for (Entry<Integer, ProfileFieldMode> entry : profileFieldAdminPermissions.entrySet()) {
				if (ProfileFieldMode.Editable == entry.getValue()) {
					editableUsers.add(entry.getKey());
				} else if (ProfileFieldMode.ReadOnly == entry.getValue()) {
					readOnlyUsers.add(entry.getKey());
				} else if (ProfileFieldMode.NotVisible == entry.getValue()) {
					notVisibleUsers.add(entry.getKey());
				}
			}
		}
	}
	
	private Collection<ProfileField> sortProfileFields(Collection<ProfileField> profileFields, String... keepItemsFirst) {
		List<ProfileField> list = new ArrayList<>(profileFields);
		Collections.sort(list, new Comparator<ProfileField>() {
			@Override
			public int compare(ProfileField o1, ProfileField o2) {
				if (o1 == o2) {
					return 0;
				} else if (o1 == null) {
					return 1;
				} else if (o2 == null) {
					return -1;
				} else if (o1.equals(o2)) {
					return 0;
				} else {
					String profilefieldName1 = o1.getColumn().toLowerCase();
					String profilefieldName2 = o2.getColumn().toLowerCase();
					
					if (o1.getSort() < ProfileFieldDao.MAX_SORT_INDEX && o2.getSort() < ProfileFieldDao.MAX_SORT_INDEX) {
						if (o1.getSort() < o2.getSort()) {
							return -1;
						} else if (o1.getSort() == o2.getSort()) {
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
					} else if (o1.getSort() < ProfileFieldDao.MAX_SORT_INDEX) {
						return -1;
					} else if (o2.getSort() < ProfileFieldDao.MAX_SORT_INDEX) {
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

	private void readJsonData(JsonObject jsonObject, ProfileField profileField, Set<Integer> editableUsers, Set<Integer> readOnlyUsers, Set<Integer> notVisibleUsers) throws Exception, RestfulClientException, IOException, FileNotFoundException {
		Map<Integer, String> adminNamesMap = null;

		editableUsers.clear();
		readOnlyUsers.clear();
		notVisibleUsers.clear();
		
		for (Entry<String, Object> entry : jsonObject.entrySet()) {
			if ("name".equals(entry.getKey())) {
				// Attribute "name" is skipped, because it was handled before
			} else if ("type".equals(entry.getKey())) {
				if (entry.getValue() instanceof String) {
					if (profileField.getDataType() == null) {
						SimpleDataType simpleDataType;
						try {
							simpleDataType = SimpleDataType.getFromString((String) entry.getValue());
						} catch (Exception e) {
							throw new RestfulClientException("Invalid value for property 'type': " + (String) entry.getValue());
						}
						if (simpleDataType == SimpleDataType.Characters) {
							profileField.setDataType("VARCHAR");
							profileField.setDataTypeLength(100);
						} else if (simpleDataType == SimpleDataType.Date) {
							profileField.setDataType("DATE");
						} else if (simpleDataType == SimpleDataType.DateTime) {
							profileField.setDataType("DATETIME");
						} else if (simpleDataType == SimpleDataType.Float) {
							profileField.setDataType("FLOAT");
						} else if (simpleDataType == SimpleDataType.Numeric) {
							profileField.setDataType("INTEGER");
						} else if (simpleDataType == SimpleDataType.Blob) {
							profileField.setDataType("DATE");
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
					profileField.setShortname((String) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'shortname'. String expected");
				}
			} else if ("description".equals(entry.getKey())) {
				if (entry.getValue() instanceof String) {
					profileField.setDescription((String) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'description'. String expected");
				}
			} else if ("defaultValue".equals(entry.getKey())) {
				if (entry.getValue() instanceof String) {
					profileField.setDefaultValue((String) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'defaultValue'. String expected");
				}
			} else if ("length".equals(entry.getKey())) {
				if (entry.getValue() instanceof Integer) {
					profileField.setDataTypeLength((Integer) entry.getValue());
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
					profileField.setHistorize((Boolean) entry.getValue());
				} else {
					throw new RestfulClientException("Invalid data type for 'historized'. Boolean expected");
				}
			} else if ("modeEdit".equals(entry.getKey())) {
				if (entry.getValue() instanceof Integer) {
					profileField.setModeEdit(ProfileFieldMode.getProfileFieldModeForStorageCode((Integer) entry.getValue()));
				} else if (entry.getValue() instanceof String) {
					profileField.setModeEdit(ProfileFieldMode.getProfileFieldModeForName((String) entry.getValue()));
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
					profileField.setAllowedValues(allowedValues.toArray(new String[0]));
				} else {
					throw new RestfulClientException("Invalid data type for 'allowedValues'. Array expected");
				}
			} else if ("editableUsers".equals(entry.getKey()) || "visibleUsers".equals(entry.getKey())) {
				if (editableUsers != null && editableUsers.size() > 0) {
					throw new RestfulClientException("Invalid multiple data for 'editableUsers' (alias 'visibleUsers'), because it is only allowed once");
				} else if (entry.getValue() instanceof JsonArray) {
					if (adminNamesMap == null) {
						adminNamesMap = adminService.getAdminsNamesMap(profileField.getCompanyID());
					}
					
					for (Object item : ((JsonArray) entry.getValue())) {
						if (item instanceof Integer) {
							if (adminNamesMap.containsKey(item)) {
								editableUsers.add((Integer) item);
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
								editableUsers.add(adminID);
							} else {
								throw new RestfulClientException("Invalid user item for 'editableUsers' (alias 'visibleUsers'): " + item.toString());
							}
						}
					}

					for (Integer adminID : editableUsers) {
						if (readOnlyUsers != null && readOnlyUsers.contains(adminID)) {
							throw new RestfulClientException("Invalid user item for 'editableUsers' (alias 'visibleUsers'), because it is already included in 'readOnlyUsers': " + adminNamesMap.get(adminID) + " (" + adminID + ")");
						} else if (notVisibleUsers != null && notVisibleUsers.contains(adminID)) {
							throw new RestfulClientException("Invalid user item for 'editableUsers' (alias 'visibleUsers'), because it is already included in 'notVisibleUsers': " + adminNamesMap.get(adminID) + " (" + adminID + ")");
						}
					}
				} else {
					throw new RestfulClientException("Invalid data type for 'visibleUsers'. Array expected");
				}
			} else if ("readOnlyUsers".equals(entry.getKey())) {
				if (readOnlyUsers != null && readOnlyUsers.size() > 0) {
					throw new RestfulClientException("Invalid multiple data for 'readOnlyUsers', because it is only allowed once");
				} else if (entry.getValue() instanceof JsonArray) {
					if (adminNamesMap == null) {
						adminNamesMap = adminService.getAdminsNamesMap(profileField.getCompanyID());
					}
					
					for (Object item : ((JsonArray) entry.getValue())) {
						if (item instanceof Integer) {
							if (adminNamesMap.containsKey(item)) {
								readOnlyUsers.add((Integer) item);
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
								readOnlyUsers.add(adminID);
							} else {
								throw new RestfulClientException("Invalid user item for 'readOnlyUsers': " + item.toString());
							}
						}
					}

					for (Integer adminID : readOnlyUsers) {
						if (editableUsers != null && editableUsers.contains(adminID)) {
							throw new RestfulClientException("Invalid user item for 'readOnlyUsers', because it is already included in 'editableUsers' (alias 'visibleUsers'): " + adminNamesMap.get(adminID) + " (" + adminID + ")");
						} else if (notVisibleUsers != null && notVisibleUsers.contains(adminID)) {
							throw new RestfulClientException("Invalid user item for 'readOnlyUsers', because it is already included in 'notVisibleUsers': " + adminNamesMap.get(adminID) + " (" + adminID + ")");
						}
					}
				} else {
					throw new RestfulClientException("Invalid data type for 'readOnlyUsers'. Array expected");
				}
			} else if ("notVisibleUsers".equals(entry.getKey())) {
				if (notVisibleUsers != null && notVisibleUsers.size() > 0) {
					throw new RestfulClientException("Invalid multiple data for 'notVisibleUsers', because it is only allowed once");
				} else if (entry.getValue() instanceof JsonArray) {
					if (adminNamesMap == null) {
						adminNamesMap = adminService.getAdminsNamesMap(profileField.getCompanyID());
					}
					
					for (Object item : ((JsonArray) entry.getValue())) {
						if (item instanceof Integer) {
							if (adminNamesMap.containsKey(item)) {
								notVisibleUsers.add((Integer) item);
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
								notVisibleUsers.add(adminID);
							} else {
								throw new RestfulClientException("Invalid user item for 'notVisibleUsers': " + item.toString());
							}
						}
					}

					for (Integer adminID : notVisibleUsers) {
						if (editableUsers != null && editableUsers.contains(adminID)) {
							throw new RestfulClientException("Invalid user item for 'notVisibleUsers', because it is already included in 'editableUsers' (alias 'visibleUsers'): " + adminNamesMap.get(adminID) + " (" + adminID + ")");
						} else if (readOnlyUsers != null && readOnlyUsers.contains(adminID)) {
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
	}

	private void validateProfileFieldData(ProfileField profileField) throws RestfulClientException, Exception {
		try {
			SafeString.getSafeDbColumnName(profileField.getColumn());
		} catch (Exception e) {
			throw new RestfulClientException("Invalid value for property 'name' for profilefield: " + profileField.getColumn());
		}
		
		ProfileField profilefieldByShortname = profileFieldService.getProfileFieldByShortname(profileField.getCompanyID(), profileField.getShortname());
		if (profilefieldByShortname != null && !profilefieldByShortname.getColumn().equalsIgnoreCase(profileField.getColumn())) {
			throw new RestfulClientException("Invalid value for property 'shortname' for profilefield, already exists: " + profileField.getShortname());
		}
		
		if (profileField.getSimpleDataType() == null) {
			throw new RestfulClientException("Invalid empty value for property 'type' for profilefield");
		}
		
		if (StringUtils.isBlank(profileField.getShortname())) {
			profileField.setShortname(profileField.getColumn());
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
}
