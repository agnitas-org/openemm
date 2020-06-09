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
import java.util.List;
import java.util.Map;
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
import org.agnitas.service.FormImportResult;
import org.agnitas.service.UserFormImporter;
import org.agnitas.util.DateUtilities;
import org.apache.commons.lang3.StringUtils;
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
	public FormImportResult importUserFormFromJson(int companyID, InputStream input) throws Exception {
		return importUserFormFromJson(companyID, input, null, null);
	}

    /**
     * Import userform
     */
	@Override
	public FormImportResult importUserFormFromJson(int companyID, InputStream input, String formName, String description) throws Exception {
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
			userForm.setFormName(StringUtils.defaultIfEmpty(formName, (String) jsonObject.get("formname")));
			
			FormImportResult.Builder importResult = FormImportResult.builder();
			
			int nameCollisionIterationsCount = 0;
			String originalFormName = userForm.getFormName();
			while (userFormDao.getUserFormByName(userForm.getFormName(), companyID) != null && nameCollisionIterationsCount < 10) {
				nameCollisionIterationsCount++;
				importResult.addWarning("error.form.name_in_use");
				userForm.setFormName(originalFormName + "_" + nameCollisionIterationsCount);
			}
			if (nameCollisionIterationsCount >= 10) {
				logger.error("UserForm import had unresolvable collision with userformname: '" + originalFormName + "' in company: " + companyID);
				return importResult.setSuccess(false).addError("error.userform.import").build();
			}
			
			userForm.setDescription(StringUtils.defaultIfEmpty(description, (String) jsonObject.get("description")));
			
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
				logger.error("Cannot save userform");
				return importResult.setSuccess(false).addError("error.userform.import").build();
			} else {
				return importResult.setUserFormID(importedUserFormID).setSuccess(true).build();
			}
		} catch (Exception e) {
			logger.error("Error in userform import: " + e.getMessage(), e);
			throw e;
		}
	}
}
