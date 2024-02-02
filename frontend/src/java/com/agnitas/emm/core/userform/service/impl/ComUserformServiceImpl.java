/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.agnitas.dao.EmmActionDao;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.userforms.impl.UserformServiceImpl;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.FileUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.impl.ComRecipientLiteImpl;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.emm.core.commons.ActivenessStatus;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.core.profilefields.service.ProfileFieldService;
import com.agnitas.emm.core.servicemail.UnknownCompanyIdException;
import com.agnitas.emm.core.trackablelinks.service.FormTrackableLinkService;
import com.agnitas.emm.core.userform.dto.UserFormDto;
import com.agnitas.emm.core.userform.service.ComUserformService;
import com.agnitas.emm.core.userform.service.UserFormTestUrl;
import com.agnitas.emm.core.userform.web.WebFormUrlBuilder;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.ServiceResult;
import com.agnitas.userform.bean.UserForm;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ComUserformServiceImpl extends UserformServiceImpl implements ComUserformService {

    private static final Logger logger = LogManager.getLogger(ComUserformServiceImpl.class);

    private static final int USER_FORM_NAME_MAX_LENGTH = 50;

    private EmmActionDao emmActionDao;
    private ExtendedConversionService conversionService;
    private FormTrackableLinkService trackableLinkService;
    private ConfigService configService;
    private ExtensibleUIDService uidService;
    private ComRecipientDao comRecipientDao;				// TODO Replace by RecipientService
    private ProfileFieldService profileFieldService;
    private RecipientService recipentService;

    @Override
	public String getUserFormName(int formId, int companyId) {
        return userFormDao.getUserFormName(formId, companyId);
    }

    @Override
    public List<UserForm> getUserForms(int companyId) {
        return userFormDao.getUserForms(companyId);
    }

    @Override
    public UserAction setActiveness(int companyId, Map<Integer, Boolean> activeness) {
        if (MapUtils.isEmpty(activeness) || companyId <= 0) {
            return null;
        }

        String action = "edited user form activeness";
        String description = StringUtils.EMPTY;

        int affectedRows = 0;
        List<Integer> activeFormIds = new LinkedList<>();
        List<Integer> inactiveFormIds = new LinkedList<>();

        List<UserForm> oldStateOfUserForms = userFormDao.getByIds(companyId, activeness.keySet());
        oldStateOfUserForms.stream()
                // excluding forms which has identical active value
                .filter(oldStateOfUserForm -> {
                    boolean oldValueOfActiveness = oldStateOfUserForm.getIsActive();
                    boolean newValueOfActiveness = activeness.getOrDefault(oldStateOfUserForm.getId(), oldValueOfActiveness);
                    return oldValueOfActiveness != newValueOfActiveness;
                })
                // distribution between active ids and inactive ids
                .forEach(oldStateOfUserForm -> {
                    int formId = oldStateOfUserForm.getId();
                    if (!oldStateOfUserForm.isActive()) {
                        activeFormIds.add(formId);
                    } else {
                        inactiveFormIds.add(formId);
                    }
                });

        // make certain forms active
        if (CollectionUtils.isNotEmpty(activeFormIds)) {
            affectedRows += userFormDao.updateActiveness(companyId, activeFormIds, true);
            description += "Made active: " + StringUtils.join(activeFormIds, ", ");
        }

        // make certain form inactive
        if (CollectionUtils.isNotEmpty(inactiveFormIds)) {
            affectedRows +=  userFormDao.updateActiveness(companyId, inactiveFormIds, false);
            description += StringUtils.isNotBlank(description) ? "\n" : "";
            description += "Made inactive: " + StringUtils.join(inactiveFormIds, ", ");
        }

        if (BooleanUtils.toBoolean(affectedRows)) {
            return new UserAction(action, description);
        }

        return null;
    }

    @Override
    public JSONArray getUserFormsJson(Admin admin) {
    	final String placeholder = "{%FORMNAME%}";
    	
    	final Optional<String> companyTokenOpt = companyTokenForAdmin(admin);
    	
        final JSONArray actionsJson = new JSONArray();
		final List<UserForm> userForms = userFormDao.getUserForms(admin.getCompanyID());

		//collect action ID which are used by forms
        final List<Integer> usedActionIds = new ArrayList<>();
        for (final UserForm userForm : userForms) {
            if (userForm.isUsesActions()) {
                usedActionIds.addAll(userForm.getUsedActionIds());
            }
        }

        //set action names
        final Map<Integer, String> actionNames = emmActionDao.getEmmActionNames(admin.getCompanyID(), usedActionIds);

        final String userFormUrlPattern = getUserFormUrlPattern(admin, placeholder, false, companyTokenOpt);
        
		for (final UserForm userForm: userForms) {
            final JSONObject entry = new JSONObject();

			entry.element("id", userForm.getId());
			entry.element("name", userForm.getFormName());
			entry.element("description", userForm.getDescription());

			final JSONArray actions = new JSONArray();
			int actionId = userForm.getStartActionID();
            if (actionId > 0) {
                actions.add(actionNames.get(actionId));
            }
            actionId = userForm.getEndActionID();
            if (actionId > 0) {
                actions.add(actionNames.get(actionId));
            }

            final String url = userFormUrlPattern.replace(URLEncoder.encode(placeholder, StandardCharsets.UTF_8), userForm.getFormName());

			entry.element("actionNames", actions);
			entry.element("creationDate", DateUtilities.toLong(userForm.getCreationDate()));
			entry.element("changeDate", DateUtilities.toLong(userForm.getChangeDate()));
			entry.element("activeStatus", userForm.getIsActive() ? ActivenessStatus.ACTIVE : ActivenessStatus.INACTIVE);
			entry.element("webformUrl", url);

			actionsJson.add(entry);
		}
		return actionsJson;
    }

    @Override
	public UserFormDto getUserForm(int companyId, int formId) {
        UserForm userForm = userFormDao.getUserForm(formId, companyId);
        if (userForm != null) {
            return conversionService.convert(userForm, UserFormDto.class);
        }
        return null;
	}

	@Override
	public boolean isFormNameUnique(String formName, int formId, int companyId) {
		String existingName = StringUtils.defaultString(userFormDao.getUserFormName(formId, companyId));

		// Allow to keep existing name anyway.
		return existingName.equals(formName) || !userFormDao.isFormNameInUse(formName, formId, companyId);
	}
	
	@Override
	public ServiceResult<Integer> saveUserForm(Admin admin, UserFormDto userFormDto) {
        int companyId = admin.getCompanyID();

        int userFormId = userFormDto.getId();
        if (userFormId > 0 && userFormDao.existsUserForm(companyId, userFormDto.getId())) {
            userFormDao.updateUserForm(companyId, conversionService.convert(userFormDto, UserForm.class));
        } else {
            userFormId = userFormDao.createUserForm(companyId, conversionService.convert(userFormDto, UserForm.class));
        }
        userFormDto.setId(userFormId);

        final List<Message> errors = new ArrayList<>();
        final List<Message> warnings = new ArrayList<>();
        trackableLinkService.saveTrackableLinks(admin, userFormDto, errors, warnings);

        return new ServiceResult<>(userFormId, userFormId > 0 && errors.isEmpty(), null, warnings, errors);
	}

    @Override
    public List<UserFormDto> bulkDeleteUserForm(List<Integer> bulkIds, int companyId) {
        List<UserFormDto> deletedUserForms = new ArrayList<>();
        for (int userFormId : bulkIds) {
            UserFormDto userForm = getUserForm(companyId, userFormId);
            if (userForm != null) {
                userFormDao.deleteUserForm(userFormId, companyId);
                deletedUserForms.add(userForm);
            }
        }

        return deletedUserForms;
    }

    @Override
    public boolean deleteUserForm(int formId, int companyId) {
        UserFormDto userForm = getUserForm(companyId, formId);
        if (userForm != null) {
            userFormDao.deleteUserForm(formId, companyId);
            return true;
        }
        return false;
    }

    @Override
    public ServiceResult<Integer> cloneUserForm(Admin admin, int userFormId) {
		UserFormDto userForm = getUserForm(admin.getCompanyID(), userFormId);
		if (userForm == null) {
            throw new IllegalArgumentException("userForm == null (invalid userFormId)");
        }

		userForm.setId(0);
		userForm.setName(getCloneUserFormName(userForm.getName(), admin.getCompanyID(), admin.getLocale()));
		return saveUserForm(admin, userForm);
	}

	@Override
	public String getCloneUserFormName(String name, int companyId, Locale locale) {
        String prefix = I18nString.getLocaleString("mailing.CopyOf", locale) + " ";
        return AgnUtils.getUniqueCloneName(name, StringUtils.replaceChars(prefix, " ", "_"),
                USER_FORM_NAME_MAX_LENGTH,
                newName -> isFormNameInUse(newName, companyId));
    }

    @Override
    public File exportUserForm(Admin admin, int userFormId, String userFormName) {
        try {
            int companyId = admin.getCompanyID();

            File tmpFile = File.createTempFile(
                    String.format("Template_%s_%d_%d", StringUtils.replace(userFormName, "/", "_"), companyId, userFormId),
                    FileUtils.JSON_EXTENSION);
            try (FileOutputStream outputStream = new FileOutputStream(tmpFile)) {
                userFormExporter.exportUserFormToJson(companyId, userFormId, outputStream, true);
            }

            return tmpFile;
        } catch (Exception e) {
            logger.error("Export user form failed!", e);
        }
        return null;
    }

	@Override
	public String getUserFormUrlPattern(final Admin admin, final String formName, final boolean resolveUID, final Optional<String> companyToken) {
		try {
			int testCustomerId = comRecipientDao.getAdminOrTestRecipientId(admin.getCompanyID(), admin.getAdminID());
			final int licenseID = configService.getLicenseID();
			final ComExtensibleUID uid = UIDFactory.from(licenseID, admin.getCompanyID(), testCustomerId);
            final String uidString = uidService.buildUIDString(uid);


			return WebFormUrlBuilder.from(admin.getCompany(), formName)
				.withCompanyToken(companyToken)
				.withResolvedUID(resolveUID)
				.withUID(uidString)
				.build();
        } catch (Exception e) {
            logger.error(String.format("Could not generate user form URL (company ID %d, admin ID %d)", admin.getCompanyID(), admin.getAdminID()), e);
            
            throw new RuntimeException(e);
        }
	}

	@Override
	public final List<UserFormTestUrl> getUserFormUrlForAllAdminAndTestRecipients(final Admin admin, final String formName, final Optional<String> companyToken) {
		try {
			final List<ComRecipientLiteImpl> recipients = this.recipentService.listAdminAndTestRecipients(admin);
			final List<UserFormTestUrl> urls = new ArrayList<>();
			
			final int licenseID = configService.getLicenseID();
			
			for(final ComRecipientLiteImpl recipient : recipients) {
				final ComExtensibleUID uid = UIDFactory.from(licenseID, admin.getCompanyID(), recipient.getId());
	            final String uidString = uidService.buildUIDString(uid);

	            final String url = WebFormUrlBuilder.from(admin.getCompany(), formName)
					.withCompanyToken(companyToken)
					.withResolvedUID(true)
					.withUID(uidString)
					.build();
	            
	            urls.add(new UserFormTestUrl(recipient.getFirstname(), recipient.getLastname(), recipient.getEmail(), url));
			}
			
			return urls;
        } catch (Exception e) {
            logger.error(String.format("Could not generate user form URL (company ID %d, admin ID %d)", admin.getCompanyID(), admin.getAdminID()), e);
            
            throw new RuntimeException(e);
        }
	}
	
    @Override
	public final String getUserFormUrlWithoutUID(Admin admin, String formName, Optional<String> companyToken) {
    	try {
	        return WebFormUrlBuilder.from(admin.getCompany(), formName)
				.withCompanyToken(companyToken)
				.withResolvedUID(true)
				.build();
    	} catch(final Exception e) {
            logger.error(String.format("Could not generate user form URL (company ID %d, admin ID %d)", admin.getCompanyID(), admin.getAdminID()), e);
            
            throw new RuntimeException(e);
    	}
	}

	@Override
    public List<String> getUserFormNames(final int companyId) {
        final List<UserForm> userForms = userFormDao.getUserForms(companyId);

        return userForms.stream().map(UserForm::getFormName).collect(Collectors.toList());
    }

    @Override
    public Map<String, String> getMediapoolImages(Admin admin) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getProfileFields(Admin admin, DbColumnType.SimpleDataType... allowedTypes) {
        final Map<String, String> resultMap = new LinkedHashMap<>();
        resultMap.put("none", I18nString.getLocaleString("default.none", admin.getLocale()));

        final List<ProfileField> profileFields = profileFieldService.getSortedColumnInfo(admin.getCompanyID());
        for (ProfileField field : profileFields) {
            if(ArrayUtils.isNotEmpty(allowedTypes)) {
                if(ArrayUtils.contains(allowedTypes, DbColumnType.getSimpleDataType(field.getDataType(), field.getNumericScale()))) {
                    resultMap.put(field.getColumn(), field.getShortname());
                }
            } else {
                resultMap.put(field.getColumn(), field.getShortname());
            }
        }
        return resultMap;
    }
    
    private Optional<String> companyTokenForAdmin(final Admin admin) {
    	try {
    		return companyTokenService.getCompanyToken(admin.getCompanyID());
    	} catch(final UnknownCompanyIdException e) {
    		return Optional.empty();
    	}
    }

    @Required
    public void setEmmActionDao(EmmActionDao emmActionDao) {
        this.emmActionDao = emmActionDao;
    }
    
    @Required
    public void setConversionService(ExtendedConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Required
    public void setTrackableLinkService(FormTrackableLinkService trackableLinkService) {
        this.trackableLinkService = trackableLinkService;
    }
    
    @Required
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    @Required
    public void setUidService(ExtensibleUIDService uidService) {
        this.uidService = uidService;
    }

    @Required
    public void setComRecipientDao(ComRecipientDao comRecipientDao) {
        this.comRecipientDao = comRecipientDao;
    }

    @Required
    public void setProfileFieldService(ProfileFieldService profileFieldService) {
        this.profileFieldService = profileFieldService;
    }

    @Required
    public final void setRecipientService(final RecipientService service) {
    	this.recipentService = Objects.requireNonNull(service, "RecipientService is null");
    }
}
