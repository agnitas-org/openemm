/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.EmmActionDao;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.userforms.impl.UserformServiceImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.service.UserFormExporter;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.FileUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.commons.ActivenessStatus;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.core.trackablelinks.service.FormTrackableLinkService;
import com.agnitas.emm.core.userform.dto.UserFormDto;
import com.agnitas.emm.core.userform.service.ComUserformService;
import com.agnitas.emm.core.userform.util.WebFormUtils;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.ServiceResult;
import com.agnitas.userform.bean.UserForm;

public class ComUserformServiceImpl extends UserformServiceImpl implements ComUserformService {

    private static final Logger logger = Logger.getLogger(ComUserformServiceImpl.class);

    private static final int USER_FORM_NAME_MAX_LENGTH = 50;

    private EmmActionDao emmActionDao;
    private ExtendedConversionService conversionService;
    private FormTrackableLinkService trackableLinkService;
    private UserFormExporter userFormExporter;
    private ConfigService configService;
    private ExtensibleUIDService uidService;

    @Override
	public String getUserFormName(int formId, @VelocityCheck int companyId) {
        return userFormDao.getUserFormName(formId, companyId);
    }

    @Override
    public List<UserForm> getUserForms(@VelocityCheck int companyId) {
        return userFormDao.getUserForms(companyId);
    }

    @Override
    public UserAction setActiveness(@VelocityCheck int companyId, Map<Integer, Boolean> activeness) {
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
    public PaginatedListImpl<UserFormDto> getUserFormsWithActionData(ComAdmin admin, String sort,
			String order, int page, int numberOfRows, ActivenessStatus filter) {
        
        PaginatedListImpl<UserForm> userForms = userFormDao
                .getUserFormsWithActionIdsNew(sort, order, page, numberOfRows, filter == null ? ActivenessStatus.NONE : filter, admin.getCompanyID());
        
        //collect action ID which are used by forms
        List<Integer> usedActionIds = new ArrayList<>();
        for (UserForm userForm : userForms.getList()) {
            int actionId = userForm.getStartActionID();
            if (actionId > 0) {
                usedActionIds.add(actionId);
            }
            actionId = userForm.getEndActionID();
            if (actionId > 0) {
                usedActionIds.add(actionId);
            }
        }
        
        PaginatedListImpl<UserFormDto> convertedList = conversionService
                .convertPaginatedList(userForms, UserForm.class, UserFormDto.class);
        
        //set action names
        Map<Integer, String> actionNames = emmActionDao.getEmmActionNamesNew(admin.getCompanyID(), usedActionIds);
        for (UserFormDto dto : convertedList.getList()) {
            int actionId = dto.getSuccessSettings().getStartActionId();
            dto.setActionName(actionNames.get(actionId));
            actionId = dto.getSuccessSettings().getFinalActionId();
            dto.setActionName(actionNames.get(actionId));
        }
        
        return convertedList;
	}

	@Override
	public UserFormDto getUserForm(@VelocityCheck int companyId, int formId) {
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
	public ServiceResult<Integer> saveUserForm(ComAdmin admin, UserFormDto userFormDto) {
        int companyId = admin.getCompanyID();

        int userFormId = userFormDto.getId();
        if (userFormId > 0 && userFormDao.existsUserForm(companyId, userFormDto.getId())) {
            userFormDao.updateUserForm(companyId, conversionService.convert(userFormDto, UserForm.class));
        } else {
            userFormId = userFormDao.createUserForm(companyId, conversionService.convert(userFormDto, UserForm.class));
        }
        userFormDto.setId(userFormId);

        List<Message> errors = new ArrayList<>();
        trackableLinkService.saveTrackableLinks(admin, userFormDto, errors);

        return new ServiceResult<>(userFormId, userFormId > 0 && errors.isEmpty(), errors);
	}

    @Override
    public List<UserFormDto> bulkDeleteUserForm(List<Integer> bulkIds, @VelocityCheck int companyId) {
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
    public ServiceResult<Integer> cloneUserForm(ComAdmin admin, int userFormId) {
		UserFormDto userForm = getUserForm(admin.getCompanyID(), userFormId);
		if (userForm == null) {
            throw new IllegalArgumentException("userForm == null (invalid userFormId)");
        }

		userForm.setId(0);
		userForm.setName(getCloneUserFormName(userForm.getName(), admin.getCompanyID(), admin.getLocale()));
		return saveUserForm(admin, userForm);
	}

	@Override
	public String getCloneUserFormName(String name, @VelocityCheck int companyId, Locale locale) {
        String prefix = I18nString.getLocaleString("mailing.CopyOf", locale) + " ";
        return AgnUtils.getUniqueCloneName(name, StringUtils.replaceChars(prefix, " ", "_"),
                USER_FORM_NAME_MAX_LENGTH,
                newName -> isFormNameInUse(newName, companyId));
    }

    @Override
    public File exportUserForm(ComAdmin admin, int userFormId, String userFormName) {
        try {
            int companyId = admin.getCompanyID();

            File tmpFile = File.createTempFile(
                    String.format("Template_%s_%d_%d", StringUtils.replace(userFormName, "/", "_"), companyId, userFormId),
                    FileUtils.JSON_EXTENSION);
            try (FileOutputStream outputStream = new FileOutputStream(tmpFile)) {
                userFormExporter.exportUserFormToJson(companyId, userFormId, outputStream);
            }

            return tmpFile;
        } catch (Exception e) {
            logger.error("Export user form failed!", e);
        }
        return null;
    }

	@Override
	public String getUserFormUrlPattern(ComAdmin admin, boolean resolveUID) {
        if (resolveUID) {
            try {
                final int licenseID = configService.getLicenseID();
                final ComExtensibleUID uid = UIDFactory.from(licenseID, admin.getCompanyID(), admin.getAdminID());
                final String urlEncodedUID = URLEncoder.encode(uidService.buildUIDString(uid), "UTF-8");
                return WebFormUtils.getFormFullViewPattern(AgnUtils.getRedirectDomain(admin.getCompany()), admin.getCompanyID(), urlEncodedUID);
            } catch (Exception e) {
                logger.error("Could not generate UID for companyID/adminID " + admin.getCompanyID() + "/" + admin.getAdminID());
            }
        }

        return WebFormUtils.getFormFullViewPattern(AgnUtils.getRedirectDomain(admin.getCompany()), admin.getCompanyID(), "##AGNUID##");
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
    public void setUserFormExporter(UserFormExporter userFormExporter) {
        this.userFormExporter = userFormExporter;
    }

    @Required
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    @Required
    public void setUidService(ExtensibleUIDService uidService) {
        this.uidService = uidService;
    }
}
