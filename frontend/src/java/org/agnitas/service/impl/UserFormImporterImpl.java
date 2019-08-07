/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service.impl;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;

import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.dao.UserFormDao;
import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.json.JsonReader;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.userform.bean.impl.UserFormImpl;
import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;
import com.agnitas.userform.trackablelinks.bean.impl.ComTrackableUserFormLinkImpl;
import org.agnitas.service.UserFormImporter;
import org.agnitas.util.DateUtilities;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class UserFormImporterImpl extends ActionImporter implements UserFormImporter {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(UserFormImporterImpl.class);
	
	@Resource(name="UserFormDao")
	protected UserFormDao userFormDao;
    
    /**
     * Import userform
     */
	@Override
	public UserFormImportResult importUserFormFromJson(int companyID, InputStream input) throws Exception {
		return importUserFormFromJson(companyID, input, null, null);
	}

    /**
     * Import userform
     */
	@Override
	public UserFormImportResult importUserFormFromJson(int companyID, InputStream input, String formName, String description) throws Exception {
		Set<String> warningKeys = new HashSet<>();
		try (JsonReader reader = new JsonReader(input, "UTF-8")) {
			JsonNode jsonNode = reader.read();
			
			checkIsJsonObject(jsonNode);
			
			JsonObject jsonObject = (JsonObject) jsonNode.getValue();

			String version = (String) jsonObject.get("version");
			checkJsonVersion(version);

			Map<Integer, Integer> actionIdMappings = new HashMap<>();
			if (jsonObject.containsPropertyKey("actions")) {
				for (Object actionObject : (JsonArray) jsonObject.get("actions")) {
					importAction(companyID, (JsonObject) actionObject, actionIdMappings);
				}
			}

			UserForm userForm = new UserFormImpl();
			userForm.setCompanyID(companyID);
			
			if (formName == null) {
				userForm.setFormName((String) jsonObject.get("formname"));
			} else {
				userForm.setFormName(formName);
			}
			
			int nameCollisionIterationsCount = 0;
			String originalFormName = userForm.getFormName();
			while (userFormDao.getUserFormByName(userForm.getFormName(), companyID) != null && nameCollisionIterationsCount < 10) {
				nameCollisionIterationsCount++;
				if (!warningKeys.contains("userform.name.already.exists")) {
					warningKeys.add("userform.name.already.exists");
				}
				userForm.setFormName(originalFormName + "_" + nameCollisionIterationsCount);
			}
			if (nameCollisionIterationsCount >= 10) {
				throw new Exception("UserForm import had unresolvable collision with userformname: '" + originalFormName + "' in company: " + companyID);
			}
			
			if (description == null) {
				userForm.setDescription((String) jsonObject.get("description"));
			} else {
				userForm.setDescription(description);
			}
			
			userForm.setIsActive((boolean) jsonObject.get("active"));
			
			if (jsonObject.containsPropertyKey("startaction")) {
				userForm.setStartActionID(actionIdMappings.get(jsonObject.get("startaction")));
			}
			
			if (jsonObject.containsPropertyKey("endaction")) {
				userForm.setEndActionID(actionIdMappings.get(jsonObject.get("endaction")));
			}

			userForm.setSuccessTemplate((String) jsonObject.get("success_template"));
			if (jsonObject.containsPropertyKey("success_mimetype")) {
				userForm.setSuccessMimetype((String) jsonObject.get("success_mimetype"));
			}
			
			if (jsonObject.containsPropertyKey("success_url")) {
				userForm.setSuccessUrl((String) jsonObject.get("success_url"));
				if (jsonObject.containsPropertyKey("success_use_url")) {
					userForm.setSuccessUseUrl((boolean) jsonObject.get("success_use_url"));
				}
			}

			userForm.setErrorTemplate((String) jsonObject.get("error_template"));
			if (jsonObject.containsPropertyKey("error_mimetype")) {
				userForm.setErrorMimetype((String) jsonObject.get("error_mimetype"));
			}
			
			if (jsonObject.containsPropertyKey("success_url")) {
				userForm.setErrorUrl((String) jsonObject.get("error_url"));
				if (jsonObject.containsPropertyKey("error_use_url")) {
					userForm.setErrorUseUrl((boolean) jsonObject.get("error_use_url"));
				}
			}
			
			if (jsonObject.containsPropertyKey("links")) {
				Map<String, ComTrackableUserFormLink> trackableLinks = new HashMap<>();
				for (Object linkObject : (JsonArray) jsonObject.get("links")) {
					JsonObject linkJsonObject = (JsonObject) linkObject;
					ComTrackableUserFormLink trackableLink = new ComTrackableUserFormLinkImpl();
					trackableLink.setShortname((String) linkJsonObject.get("name"));
					trackableLink.setFullUrl((String) linkJsonObject.get("url"));
					
					if (linkJsonObject.containsPropertyKey("deep_tracking")) {
						trackableLink.setDeepTracking((Integer) linkJsonObject.get("deep_tracking"));
					}
					
					if (linkJsonObject.containsPropertyKey("relevance")) {
						trackableLink.setRelevance((Integer) linkJsonObject.get("relevance"));
					}
					
					if (linkJsonObject.containsPropertyKey("usage")) {
						trackableLink.setUsage((Integer) linkJsonObject.get("usage"));
					}
					
					if (linkJsonObject.containsPropertyKey("action_id")) {
						trackableLink.setActionID(actionIdMappings.get(jsonObject.get("action_id")));
					}

					if (linkJsonObject.containsPropertyKey("properties")) {
						List<LinkProperty> linkProperties = new ArrayList<>();
						for (Object propertyObject : (JsonArray) linkJsonObject.get("properties")) {
							JsonObject propertyJsonObject = (JsonObject) propertyObject;
							LinkProperty linkProperty = new LinkProperty(PropertyType.parseString((String) propertyJsonObject.get("type")), (String) propertyJsonObject.get("name"), (String) propertyJsonObject.get("value"));
							linkProperties.add(linkProperty);
						}
						trackableLink.setProperties(linkProperties);
					}
					
					trackableLinks.put(trackableLink.getFullUrl(), trackableLink);
				}
				userForm.setTrackableLinks(trackableLinks);
			}
			
			// Mark mailing as newly imported
			userForm.setDescription("Imported at " + new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM).format(new Date()) + (StringUtils.isEmpty(userForm.getDescription()) ? "" : "\n" + userForm.getDescription()));
		
			int importedUserFormID = userFormDao.storeUserForm(userForm);
			
			if (importedUserFormID <= 0) {
				throw new Exception("Cannot save userform");
			} else {
				return new UserFormImportResult(importedUserFormID, warningKeys);
			}
		} catch (Exception e) {
			logger.error("Error in userform import: " + e.getMessage(), e);
			throw e;
		}
	}
}
