/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.service.impl;

import static com.agnitas.util.Const.Mvc.ERROR_MSG;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.impl.RecipientLiteImpl;
import com.agnitas.dao.EmmActionDao;
import com.agnitas.dao.RecipientDao;
import com.agnitas.dao.UserFormDao;
import com.agnitas.emm.common.service.BulkActionValidationService;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.company.service.CompanyTokenService;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.profilefields.service.ProfileFieldService;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.core.trackablelinks.service.FormTrackableLinkService;
import com.agnitas.emm.core.userform.dto.ResultSettings;
import com.agnitas.emm.core.userform.dto.UserFormDto;
import com.agnitas.emm.core.userform.exception.UserFormCopyException;
import com.agnitas.emm.core.userform.form.UserFormForm;
import com.agnitas.emm.core.userform.service.UserFormTestUrl;
import com.agnitas.emm.core.userform.service.UserformService;
import com.agnitas.emm.core.userform.web.WebFormUrlBuilder;
import com.agnitas.emm.core.velocity.scriptvalidator.IllegalVelocityDirectiveException;
import com.agnitas.emm.core.velocity.scriptvalidator.ScriptValidationException;
import com.agnitas.emm.core.velocity.scriptvalidator.VelocityDirectiveScriptValidator;
import com.agnitas.exception.FormNotFoundException;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.UserFormExporter;
import com.agnitas.service.UserFormImporter;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.DbColumnType;
import com.agnitas.util.FileUtils;
import com.agnitas.util.Tuple;
import com.helger.commons.url.URLValidator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserformServiceImpl implements UserformService {

    private static final Logger logger = LogManager.getLogger(UserformServiceImpl.class);

    /** Regular expression for validation of form name. */
    private static final Pattern FORM_NAME_PATTERN = Pattern.compile( "^[a-zA-Z0-9\\-_]+$");
    private static final int USER_FORM_NAME_MAX_LENGTH = 50;

    private EmmActionDao emmActionDao;
    private ExtendedConversionService conversionService;
    private FormTrackableLinkService trackableLinkService;
    private ConfigService configService;
    private ExtensibleUIDService uidService;
    private RecipientDao recipientDao;				// TODO Replace by RecipientService
    private ProfileFieldService profileFieldService;
    private RecipientService recipentService;
    private BulkActionValidationService<Integer, UserForm> bulkActionValidationService;
    private UserFormDao userFormDao;
    private UserFormExporter userFormExporter;
    private UserFormImporter userFormImporter;
    private CompanyTokenService companyTokenService;
    private VelocityDirectiveScriptValidator velocityValidator;
    private LinkService linkService;

    @Override
    public boolean isFormNameInUse(String formName, int companyId) {
        return userFormDao.isFormNameInUse(formName, 0, companyId);
    }

    @Override
    public List<Message> validateUserForm(Admin admin, UserFormForm form) throws ScriptValidationException {
        List<Message> errors = new ArrayList<>();
        if (!isValidFormName(form.getFormName())) {
            errors.add(Message.of("error.form.invalid_name"));
        } else if (!isFormNameUnique(form.getFormName(), form.getFormId(), admin.getCompanyID())) {
            errors.add(Message.of("error.form.name_in_use"));
        }

        errors.addAll(validateSettings(form.getSuccessSettings()));
        errors.addAll(validateSettings(form.getErrorSettings()));
        return errors;
    }

    @Override
    public boolean isValidFormName(String formName) {
        Matcher matcher = FORM_NAME_PATTERN.matcher(formName);
        return matcher.matches();
    }

    private boolean isFormNameUnique(String formName, int formId, int companyId) {
        String existingName = StringUtils.defaultString(userFormDao.getUserFormName(formId, companyId));

        // Allow to keep existing name anyway.
        return existingName.equals(formName) || !userFormDao.isFormNameInUse(formName, formId, companyId);
    }

    private List<Message> validateSettings(ResultSettings settings) throws ScriptValidationException {
        List<Message> errors = new ArrayList<>();

        if (settings.isUseUrl() && !URLValidator.isValid(settings.getUrl())) {
            errors.add(Message.of("error.userform." + (settings.isSuccess() ? "success" : "error") + ".url.missing"));
        }

        if (!settings.isUseUrl() && StringUtils.isBlank(settings.getTemplate())) {
            errors.add(Message.of("error.userform." + (settings.isSuccess() ? "success" : "error") + ".html.missing"));
        }

        try {
            velocityValidator.validateScript(settings.getTemplate());
        } catch (IllegalVelocityDirectiveException e) {
            errors.add(Message.of("error.form.illegal_directive", e.getDirective()));
        }

        int invalidLineNumber = linkService.getLineNumberOfFirstInvalidLink(settings.getTemplate());
        if (invalidLineNumber != -1) {
            errors.add(Message.of("error.invalid_link", settings.isSuccess() ? "SUCCESS" : "ERROR", invalidLineNumber));
        }
        return errors;
    }

    @Override
    public UserForm getUserForm(int companyID, String formName) {
        final UserForm form = userFormDao.getUserFormByName(formName, companyID);

        if(form == null) {
            throw new FormNotFoundException(companyID, formName);
        }

        return form;
    }

    @Override
    public List<Tuple<Integer, String>> getUserFormNamesByActionID(int companyID, int actionID) {
        if (companyID > 0 && actionID > 0) {
            return userFormDao.getUserFormNamesByActionID(companyID, actionID);
        }

        return new ArrayList<>();
    }

    @Override
    public void copyUserForm(int id, int companyId, int newCompanyId, int mailinglistID, String rdirDomain, Map<Integer, Integer> actionIdReplacements) {
        File userFormTempFile = null;
        try {
            userFormTempFile = File.createTempFile("UserFormTempFile_", ".json");
            try (OutputStream userFormOutputStream = new FileOutputStream(userFormTempFile)) {
                userFormExporter.exportUserFormToJson(companyId, id, userFormOutputStream, true);
            }

            replacePlaceholdersInFile(userFormTempFile, newCompanyId, mailinglistID, rdirDomain);

            try (InputStream userFormInputStream = new FileInputStream(userFormTempFile)) {
                userFormImporter.importUserForm(newCompanyId, userFormInputStream, null, actionIdReplacements);
            }
        } catch (Exception e) {
            throw new UserFormCopyException("Could not copy user form (%d) for new company (%d): %s".formatted(id, newCompanyId, e.getMessage()), e);
        } finally {
            if (userFormTempFile != null && userFormTempFile.exists()) {
                userFormTempFile.delete();
            }
        }
    }

    private void replacePlaceholdersInFile(File userFormTempFile, int companyID, int mailinglistID, String rdirDomain) throws Exception {
        String companyToken = companyTokenService.getCompanyToken(companyID)
                .orElse(null);

        String content = Files.readString(userFormTempFile.toPath());

        String cid = Integer.toString(companyID);
        content = StringUtils.replaceEach(content, new String[]{"<CID>", "<cid>", "[COMPANY_ID]", "[company_id]", "[Company_ID]"},
                new String[]{cid, cid, cid, cid, cid});

        String mlid = Integer.toString(mailinglistID);
        content = StringUtils.replaceEach(content, new String[]{"<MLID>", "<mlid>", "[MAILINGLIST_ID]", "[mailinglist_id]", "[Mailinglist_ID]"},
                new String[]{mlid, mlid, mlid, mlid, mlid});

        if (StringUtils.isNotBlank(companyToken)) {
            content = content.replace("[CTOKEN]", companyToken);
        } else {
            content = content.replace("agnCTOKEN=[CTOKEN]", "agnCI=" + companyID);
        }

        content = content.replace("<rdir-domain>", StringUtils.defaultIfBlank(rdirDomain, "RDIR-Domain"));

        org.apache.commons.io.FileUtils.writeStringToFile(userFormTempFile, content, StandardCharsets.UTF_8);
    }

    @Override
    public void restore(Set<Integer> bulkIds, int companyId) {
        userFormDao.restore(bulkIds, companyId);
    }

    @Override
    public void deleteExpired(Date expireDate, int companyId) {
        userFormDao.getMarkedAsDeletedBefore(expireDate, companyId)
                .forEach(formId -> userFormDao.deleteUserForm(formId, companyId));
    }

    @Override
	public String getUserFormName(int formId, int companyId) {
        return userFormDao.getUserFormName(formId, companyId);
    }

    @Override
    public List<UserForm> getUserForms(int companyId) {
        return userFormDao.getUserForms(companyId);
    }

    @Override
    public ServiceResult<List<UserForm>> setActiveness(Set<Integer> ids, int companyId, boolean activate) {
        Function<Integer, ServiceResult<UserForm>> validationFunction = id -> {
            UserForm userForm = userFormDao.getUserForm(id, companyId);
            if (userForm == null) {
                return ServiceResult.errorKeys("error.general.missing");
            }

            if (userForm.isActive() == activate) {
                return ServiceResult.errorKeys(ERROR_MSG);
            }

            return ServiceResult.success(userForm);
        };

        ServiceResult<List<UserForm>> validationResult = activate
                ? bulkActionValidationService.checkAllowedForActivation(ids, validationFunction)
                : bulkActionValidationService.checkAllowedForDeactivation(ids, validationFunction);

        if (validationResult.isSuccess()) {
            List<Integer> allowedIds = validationResult.getResult().stream()
                    .map(UserForm::getId)
                    .toList();

            updateActiveness(companyId, allowedIds, activate);
        }

        return validationResult;
    }

    @Override
    public int updateActiveness(int companyId, Collection<Integer> formIds, boolean isActive) {
        return userFormDao.updateActiveness(companyId, formIds, isActive);
    }

    @Override
    public JSONArray getUserFormsJson(Admin admin) {
    	final String placeholder = "{%FORMNAME%}";

        final Optional<String> companyTokenOpt = companyTokenService.getCompanyToken(admin.getCompanyID());
    	
        final JSONArray actionsJson = new JSONArray();
		final List<UserForm> userForms = userFormDao.overview(admin.getCompanyID());

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

			entry.put("id", userForm.getId());
			entry.put("name", userForm.getFormName());
			entry.put("description", userForm.getDescription());

			final JSONArray actions = new JSONArray();
			int actionId = userForm.getStartActionID();
            if (actionId > 0) {
                actions.put(actionNames.get(actionId));
            }
            actionId = userForm.getEndActionID();
            if (actionId > 0) {
                actions.put(actionNames.get(actionId));
            }

            final String url = userFormUrlPattern.replace(URLEncoder.encode(placeholder, StandardCharsets.UTF_8), userForm.getFormName());

			entry.put("actionNames", actions);
			entry.put("creationDate", DateUtilities.toLong(userForm.getCreationDate()));
			entry.put("changeDate", DateUtilities.toLong(userForm.getChangeDate()));
            entry.put("deleted", userForm.isDeleted());
            entry.put("active", String.valueOf(userForm.isActive()));
			entry.put("webformUrl", url);

			actionsJson.put(entry);
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
                userFormDao.markDeleted(userFormId, companyId);
                deletedUserForms.add(userForm);
            }
        }

        return deletedUserForms;
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
			int testCustomerId = recipientDao.getAdminOrTestRecipientId(admin.getCompanyID(), admin.getAdminID());
			final int licenseID = configService.getLicenseID();
			final ExtensibleUID uid = UIDFactory.from(licenseID, admin.getCompanyID(), testCustomerId);
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
			final List<RecipientLiteImpl> recipients = this.recipentService.listAdminAndTestRecipients(admin);
			final List<UserFormTestUrl> urls = new ArrayList<>();
			
			final int licenseID = configService.getLicenseID();
			
			for(final RecipientLiteImpl recipient : recipients) {
				final ExtensibleUID uid = UIDFactory.from(licenseID, admin.getCompanyID(), recipient.getId());
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

        return userForms.stream().map(UserForm::getFormName).toList();
    }

    @Override
    public List<String> getUserFormNames(Set<Integer> bulkIds, int companyID) {
        return bulkIds.stream()
                .map(id -> getUserFormName(id, companyID))
                .toList();
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

    @Override
    public boolean isActive(int formId) {
        return userFormDao.isActive(formId);
    }

    public void setEmmActionDao(EmmActionDao emmActionDao) {
        this.emmActionDao = emmActionDao;
    }
    
    public void setConversionService(ExtendedConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public void setTrackableLinkService(FormTrackableLinkService trackableLinkService) {
        this.trackableLinkService = trackableLinkService;
    }
    
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    public void setUidService(ExtensibleUIDService uidService) {
        this.uidService = uidService;
    }

    public void setRecipientDao(RecipientDao recipientDao) {
        this.recipientDao = recipientDao;
    }

    public void setProfileFieldService(ProfileFieldService profileFieldService) {
        this.profileFieldService = profileFieldService;
    }

    public final void setRecipientService(final RecipientService service) {
    	this.recipentService = Objects.requireNonNull(service, "RecipientService is null");
    }

    public void setBulkActionValidationService(BulkActionValidationService<Integer, UserForm> bulkActionValidationService) {
        this.bulkActionValidationService = bulkActionValidationService;
    }

    public final void setUserFormDao(final UserFormDao dao) {
        this.userFormDao = Objects.requireNonNull(dao, "User form DAO cannot be null");
    }

    public void setUserFormExporter(UserFormExporter userFormExporter) {
        this.userFormExporter = userFormExporter;
    }

    public void setUserFormImporter(UserFormImporter userFormImporter) {
        this.userFormImporter = userFormImporter;
    }

    public void setCompanyTokenService(CompanyTokenService companyTokenService) {
        this.companyTokenService = companyTokenService;
    }

    public void setVelocityValidator(VelocityDirectiveScriptValidator velocityValidator) {
        this.velocityValidator = velocityValidator;
    }

    public void setLinkService(LinkService linkService) {
        this.linkService = linkService;
    }
}
