/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.agnitas.service.FormImportResult;
import com.agnitas.service.UserFormImporter;
import com.agnitas.util.DateUtilities;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.dao.UserFormDao;
import com.agnitas.emm.core.trackablelinks.dao.FormTrackableLinkDao;
import com.agnitas.emm.core.userform.service.UserformService;
import com.agnitas.emm.util.html.HtmlChecker;
import com.agnitas.emm.util.html.HtmlCheckerException;
import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.json.JsonReader;
import com.agnitas.messages.Message;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.userform.bean.impl.UserFormImpl;
import com.agnitas.userform.trackablelinks.bean.TrackableUserFormLink;
import com.agnitas.userform.trackablelinks.bean.impl.TrackableUserFormLinkImpl;

import jakarta.annotation.Resource;

public class UserFormImporterImpl extends ActionImporter implements UserFormImporter {

	private static final Logger logger = LogManager.getLogger(UserFormImporterImpl.class);
    private static final String FORM_NAME_KEY = "formname";
	
	@Resource(name="UserFormDao")
	protected UserFormDao userFormDao;

	@Resource(name="FormTrackableLinkDao")
	protected FormTrackableLinkDao trackableLinkDao;

	@Resource(name="userformService")
	protected UserformService userFormService;

    @Override
    public FormImportResult importUserForm(MultipartFile file, Locale locale, int companyId) {
        Message error = validateImportFile(file);
        if (error != null) {
            return FormImportResult.error(error);
        }
        return tryImportUserForm(file, locale, companyId);
    }

    private FormImportResult tryImportUserForm(MultipartFile file, Locale locale, int companyId) {
        try (InputStream input = file.getInputStream()) {
            return importUserForm(companyId, input, locale, null);
        } catch (Exception e) {
            logger.error("Mailing import failed", e);
            return FormImportResult.error("error.userform.import");
        }
    }

    private Message validateImportFile(MultipartFile uploadFile) {
        if (uploadFile.isEmpty()) {
            return Message.of("error.file.missingOrEmpty");
        }
        if (!"json".equalsIgnoreCase(FilenameUtils.getExtension(uploadFile.getOriginalFilename()))) {
            return Message.of("error.import.invalidDataType", "json");
        }
        return null;
    }
    
	@Override
	public FormImportResult importUserForm(int companyID, InputStream input, Locale locale, Map<Integer, Integer> actionIdReplacements) throws Exception {
		return importUserForm(companyID, input, null, null, locale, actionIdReplacements);
	}

	@Override
	public FormImportResult importUserForm(int companyID, InputStream input, String formName, String description, Locale locale, Map<Integer, Integer> actionIdReplacements) throws Exception {
		try (JsonReader reader = new JsonReader(input, "UTF-8")) {
			JsonNode jsonNode = reader.read();

			checkIsJsonObject(jsonNode);

			JsonObject jsonObject = (JsonObject) jsonNode.getValue();

            if (!jsonObject.containsPropertyKey(FORM_NAME_KEY)) {
                return FormImportResult.error("error.import.userform.missing");
            }
            
			String version = (String) jsonObject.get("version");
			checkJsonVersion(version);

			Map<Integer, Integer> actionIdMappings = new HashMap<>();
			if (jsonObject.containsPropertyKey("actions")) {
				for (Object actionObject : (JsonArray) jsonObject.get("actions")) {
					Integer actionObjectId = (Integer) ((JsonObject) actionObject).get("id");
					if (actionIdReplacements != null && actionObjectId != null && actionIdReplacements.containsKey(actionObjectId)) {
						actionIdMappings.put(actionObjectId, actionIdReplacements.get(actionObjectId));
					} else {
						int importedActionId = importAction(companyID, (JsonObject) actionObject);
						if (actionObjectId != null) {
	            			actionIdMappings.put(actionObjectId, importedActionId);
	            		}
					}
				}
			}

			UserForm userForm = new UserFormImpl();
			
			String userFormName = StringUtils.defaultIfEmpty(formName, (String) jsonObject.get(FORM_NAME_KEY));
			// Check for unallowed html tags
			try {
				HtmlChecker.checkForUnallowedHtmlTags(userFormName, false);
			} catch(@SuppressWarnings("unused") final HtmlCheckerException e) {
				throw new Exception("Invalid userform data containing HTML for field: " + FORM_NAME_KEY);
			}

			if (!userFormService.isValidFormName(userFormName)) {
				return FormImportResult.error("error.form.invalid_name");
			}
			
			description = StringUtils.defaultIfEmpty(description, (String) jsonObject.get("description"));
			// Check for unallowed html tags
			try {
				HtmlChecker.checkForUnallowedHtmlTags(description, false);
			} catch(@SuppressWarnings("unused") final HtmlCheckerException e) {
				throw new Exception("Invalid userform data containing HTML for field: " + "description");
			}

			FormImportResult.Builder importResult = FormImportResult.builder();

			boolean formNameInUse = userFormService.isFormNameInUse(userFormName, companyID);
			if (formNameInUse) {
				importResult.addWarning("error.form.name_in_use");
				String originName = userFormName;
				userFormName = userFormService.getCloneUserFormName(userFormName, companyID, locale);
				logger.warn("User form name \"" + originName + "\" already exists, changing to \"" + userFormName + "\"");
			}
			importResult.setUserFormName(userFormName);
			userForm.setFormName(userFormName);
			userForm.setDescription(description);
			if (jsonObject.containsPropertyKey("active")) {
				userForm.setActive((boolean) jsonObject.get("active"));
			}

			importFormSettings(userForm, jsonObject, actionIdMappings);

			List<TrackableUserFormLink> links = new ArrayList<>();
			if (jsonObject.containsPropertyKey("links")) {
				links = getUserFormLinks(jsonObject, actionIdMappings);
			}

			if (description == null) {
				// Mark userform as newly imported
				String importDescription = "Imported at " + new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM).format(new Date());
				userForm.setDescription(importDescription);
			}

			int importedFormId = userFormDao.createUserForm(companyID, userForm);
			if (importedFormId > 0) {
				trackableLinkDao.saveUserFormTrackableLinks(importedFormId, companyID, links);
				return importResult.setUserFormID(importedFormId).setSuccess(true).build();
			} else {
				logger.error("Cannot save userform");
				return importResult.setSuccess(false).addError("error.userform.import").build();
			}

		} catch (Exception e) {
			logger.error("Error in userform import: " + e.getMessage(), e);
			throw e;
		}
	}

	private List<TrackableUserFormLink> getUserFormLinks(JsonObject jsonObject, Map<Integer, Integer> actionIdMappings) throws Exception {
		Map<String, TrackableUserFormLink> trackableLinks = new HashMap<>();
		for (Object linkObject : (JsonArray) jsonObject.get("links")) {
			JsonObject linkJsonObject = (JsonObject) linkObject;
			TrackableUserFormLink trackableLink = new TrackableUserFormLinkImpl();
			
			trackableLink.setShortname((String) linkJsonObject.get("name"));
			// Check for unallowed html tags
			try {
				HtmlChecker.checkForUnallowedHtmlTags(trackableLink.getShortname(), false);
			} catch(@SuppressWarnings("unused") final HtmlCheckerException e) {
				throw new Exception("Link name contains unallowed HTML tags");
			}
			
			trackableLink.setFullUrl((String) linkJsonObject.get("url"));

			if (linkJsonObject.containsPropertyKey("deep_tracking")) {
				trackableLink.setDeepTracking((Integer) linkJsonObject.get("deep_tracking"));
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
					String propertyName = (String) propertyJsonObject.get("name");
					// Check for unallowed html tags
					try {
						HtmlChecker.checkForUnallowedHtmlTags(propertyName, false);
					} catch(@SuppressWarnings("unused") final HtmlCheckerException e) {
						throw new Exception("Link property name contains unallowed HTML tags");
					}
					if (propertyName == null) {
						propertyName = "";
					}
					String propertyValue = (String) propertyJsonObject.get("value");
					if (propertyValue == null) {
						propertyValue = "";
					}
					if (StringUtils.isNotEmpty(propertyName) || StringUtils.isNotEmpty(propertyValue)) {
						LinkProperty linkProperty = new LinkProperty(PropertyType.parseString((String) propertyJsonObject.get("type")), propertyName, propertyValue);
						linkProperties.add(linkProperty);
					}
				}
				trackableLink.setProperties(linkProperties);
			}

			trackableLinks.put(trackableLink.getFullUrl(), trackableLink);
		}

		if (!trackableLinks.containsKey("Form")) {
			TrackableUserFormLink dummyStatisticLinks = new TrackableUserFormLinkImpl();
			dummyStatisticLinks.setFullUrl("Form");
			trackableLinks.put("Form", dummyStatisticLinks);
		}

		return new ArrayList<>(trackableLinks.values());
	}

	private void importFormSettings(UserForm userForm, JsonObject jsonObject, Map<Integer, Integer> actionMappings) {
		if (jsonObject.containsPropertyKey("startaction")) {
			userForm.setStartActionID(actionMappings.get(jsonObject.get("startaction")));
		}

		if (jsonObject.containsPropertyKey("endaction")) {
			userForm.setEndActionID(actionMappings.get(jsonObject.get("endaction")));
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
	}

	public void setUserFormDao(UserFormDao userFormDao) {
		this.userFormDao = userFormDao;
	}

	public void setTrackableLinkDao(FormTrackableLinkDao trackableLinkDao) {
		this.trackableLinkDao = trackableLinkDao;
	}

	public void setUserFormService(UserformService userFormService) {
		this.userFormService = userFormService;
	}
}
