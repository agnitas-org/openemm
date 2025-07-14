/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import static com.agnitas.util.Const.Mvc.ERROR_MSG;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.dao.EmmActionDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.common.service.BulkActionValidationService;
import com.agnitas.emm.core.action.bean.EmmAction;
import com.agnitas.emm.core.action.bean.EmmActionDependency;
import com.agnitas.emm.core.action.component.EmmActionChangesCollector;
import com.agnitas.emm.core.action.dao.EmmActionOperationDao;
import com.agnitas.emm.core.action.dto.EmmActionDto;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationIdentifyCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSendMailingParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSubscribeCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationUpdateCustomerParameters;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationService;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.commons.ActivenessStatus;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.core.userform.service.UserformService;
import com.agnitas.json.JsonObject;
import com.agnitas.json.JsonReader;
import com.agnitas.json.JsonWriter;
import com.agnitas.messages.I18nString;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.impl.ActionExporter;
import com.agnitas.service.impl.ActionImporter;
import com.agnitas.util.DateUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

public class EmmActionServiceImpl implements EmmActionService {
	
	private static final Logger LOGGER = LogManager.getLogger(EmmActionServiceImpl.class);
	
	private final ColumnInfoService columnInfoService;
    private UserformService userformService;
    private MailingService mailingService;
    private BulkActionValidationService<Integer, EmmAction> bulkActionValidationService;
    private EmmActionDao emmActionDao;
    private EmmActionOperationDao emmActionOperationDao;
    private MailingDao mailingDao;
    private EmmActionOperationService emmActionOperationService;
    private ExtendedConversionService conversionService;
    private ActionExporter actionExporter;
    private ActionImporter actionImporter;
    private EmmActionChangesCollector changesCollector;

    public EmmActionServiceImpl(final ColumnInfoService columnInfoService) {
		this.columnInfoService = Objects.requireNonNull(columnInfoService);
    }

    @Override
    public boolean actionExists(final int actionID, final int companyID) {
        return emmActionDao.actionExists(actionID, companyID);
    }

    @Override
    public boolean executeActions(int actionID, int companyID, Map<String, Object> params, final EmmActionOperationErrors errors) throws Exception {
        if (actionID == 0 || companyID <= 0) {
            return false;
        }

        List<AbstractActionOperationParameters> operations = emmActionOperationDao.getOperations(actionID, companyID);
        if (!operations.isEmpty()) {
            for (AbstractActionOperationParameters operation : operations) {
                if (!emmActionOperationService.executeOperation(operation, params, errors)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    @Transactional
    public int copyEmmAction(EmmAction emmAction, int toCompanyId, Map<Integer, Integer> mailingIdReplacements) throws Exception {
        File actionTempFile = File.createTempFile("ActionTempFile_", ".json");
        try {
            try (JsonWriter jsonWriter = new JsonWriter(new FileOutputStream(actionTempFile))) {
                actionExporter.exportAction(emmAction.getCompanyID(), emmAction.getId(), jsonWriter);
            }

            int newActionId;
            try (JsonReader jsonReader = new JsonReader(new FileInputStream(actionTempFile))) {
                JsonObject actionJsonObject = (JsonObject) jsonReader.read().getValue();
                newActionId = actionImporter.importAction(toCompanyId, actionJsonObject, mailingIdReplacements);
            }

            return newActionId;
        } catch (Exception e) {
            throw new Exception("Could not copy action (" + emmAction.getId() + ") for new company (" + toCompanyId + "): " + e.getMessage(), e);
        } finally {
            if (actionTempFile.exists()) {
                actionTempFile.delete();
            }
        }
    }

    @Override
    @Transactional
    public int saveEmmAction(int companyId, EmmAction action, List<UserAction> userActions) {
        EmmAction oldAction = action.getId() > 0 ? getEmmAction(action.getId(), action.getCompanyID()) : null;

        int actionId = emmActionDao.saveEmmAction(action);
        if (actionId > 0) {
            for (AbstractActionOperationParameters operation : action.getActionOperations()) {
                operation.setCompanyId(companyId);
                operation.setActionId(actionId);
                emmActionOperationDao.saveOperation(operation);
            }

            if (oldAction != null && CollectionUtils.isNotEmpty(oldAction.getActionOperations())) {
                for (AbstractActionOperationParameters operation : ListUtils.removeAll(oldAction.getActionOperations(), action.getActionOperations())) {
                    emmActionOperationDao.deleteOperation(operation);
                }
            }

            String changesDescription = changesCollector.collectChanges(action, oldAction);
            if (StringUtils.isNotBlank(changesDescription)) {
                userActions.add(new UserAction(
                        oldAction == null ? "create action" : "edit action",
                        String.format("%s (%d)%n%s", action.getShortname(), action.getId(), changesDescription)
                ));
            }
        }

        return actionId;
    }

    @Override
    public EmmAction getEmmAction(int actionID, int companyID) {
        EmmAction action = emmActionDao.getEmmAction(actionID, companyID);
        if (action != null) {
            List<AbstractActionOperationParameters> operations = emmActionOperationDao.getOperations(actionID, companyID);

            action.setActionOperations(operations);
        }
        return action;
    }

    @Override
    @Transactional
    public boolean deleteEmmAction(int actionID, int companyID) {
        // Action operations must not be deleted when action itself is marked as deleted.
        // emmActionOperationDao.deleteOperations(actionID, companyID);

        return emmActionDao.markAsDeleted(actionID, companyID);
    }

    @Override
    public List<Integer> getReferencedMailinglistsFromAction(int companyID, int actionID) {
        List<Integer> mailinglistIDs = new ArrayList<>();
        if (actionID != 0) {
            List<AbstractActionOperationParameters> operations = emmActionOperationDao.getOperations(actionID, companyID);
            for (AbstractActionOperationParameters operation : operations) {
                if (operation instanceof ActionOperationSendMailingParameters sendMailingParams) {
                    mailinglistIDs.add(mailingDao.getMailinglistId(sendMailingParams.getMailingID(), companyID));
                }
            }
        }
        return mailinglistIDs;
    }

    @Override
    public List<EmmAction> getActionListBySendMailingId(int companyId, int mailingId) {
        return emmActionDao.getActionListBySendMailingId(companyId, mailingId);
    }

    @Override
    public EmmActionDto getCopyOfAction(Admin admin, int originId) {
        EmmAction originAction = emmActionDao.getEmmAction(originId, admin.getCompanyID());
        if (originAction != null) {
            String copyShortname = I18nString.getLocaleString("mailing.CopyOf", admin.getLocale()) + " " + originAction.getShortname();
            originAction.setId(0);
            originAction.setShortname(copyShortname);

            // An operations should be cloned, not referenced
            List<AbstractActionOperationParameters> operations = emmActionOperationDao.getOperations(originId, admin.getCompanyID());
            for (AbstractActionOperationParameters operation : CollectionUtils.emptyIfNull(operations)) {
                operation.setId(0);
                operation.setActionId(0);
            }
            originAction.setActionOperations(operations);

            return conversionService.convert(originAction, EmmActionDto.class);
        } else {
            return new EmmActionDto();
        }
    }

    @Override
    public JSONArray getEmmActionsJson(Admin admin) {
        JSONArray actionsJson = new JSONArray();
        for (EmmAction action: emmActionDao.getEmmActions(admin.getCompanyID(), true)) {
            JSONObject entry = new JSONObject();

            entry.put("id", action.getId());
            entry.put("shortname", action.getShortname());
            entry.put("description", action.getDescription());
            entry.put("formNames", emmActionDao.getActionUserFormNames(action.getId(), admin.getCompanyID()));
            entry.put("creationDate", DateUtilities.toLong(action.getCreationDate()));
            entry.put("changeDate", DateUtilities.toLong(action.getChangeDate()));
            entry.put("deleted", action.isDeleted());
            if (admin.isRedesignedUiUsed()) {
                entry.put("active", String.valueOf(action.getIsActive()));
                entry.put("operationTypes", emmActionOperationDao.getOperationsTypes(action.getId(), action.getCompanyID()));
            } else {
                entry.put("activeStatus", action.getIsActive() ? ActivenessStatus.ACTIVE : ActivenessStatus.INACTIVE);
            }

            actionsJson.put(entry);
        }
        return actionsJson;
    }

    @Override
    public boolean isAdvertising(int id, int companyId) {
        return emmActionDao.isAdvertising(id, companyId);
    }

    @Override
    public void restore(Set<Integer> ids, int companyId) {
        emmActionDao.restore(ids, companyId);
    }

    @Override
    public void deleteExpired(Date expireDate, int companyId) {
        List<Integer> idsToDelete = emmActionDao.getMarkedAsDeletedBefore(expireDate, companyId);
        for (Integer actionId : idsToDelete) {
            emmActionOperationDao.deleteOperations(actionId, companyId);
            emmActionDao.deleteEmmActionReally(actionId, companyId);
        }
    }

    @Override
    public void bulkDelete(Set<Integer> actionIds, int companyId) {
        for (int actionId : actionIds) {
            this.emmActionDao.markAsDeleted(actionId, companyId);
        }
    }

    @Override
    public String getEmmActionName(int actionId, int companyId) {
        return emmActionDao.getEmmActionName(actionId, companyId);
    }

    @Override
    @Transactional
    @Deprecated
    public boolean setActiveness(Map<Integer, Boolean> changeMap, int companyId, List<UserAction> userActions) {
        if (MapUtils.isEmpty(changeMap) || companyId <= 0) {
            return false;
        }

        Map<Integer, Boolean> activenessMap = emmActionDao.getActivenessMap(changeMap.keySet(), companyId);

        List<Integer> entriesToActivate = new ArrayList<>();
        List<Integer> entriesToDeactivate = new ArrayList<>();

        changeMap.forEach((actionId, active) -> {
            Boolean wasActive = activenessMap.get(actionId);

            // Ensure its exists and should be changed (wasActive != active).
            if (wasActive != null && !wasActive.equals(active)) {
                if (active) {
                    entriesToActivate.add(actionId);
                } else {
                    entriesToDeactivate.add(actionId);
                }
            }
        });

        if (entriesToActivate.size() > 0 || entriesToDeactivate.size() > 0) {
            String description = "";

            if (entriesToActivate.size() > 0) {
                emmActionDao.setActiveness(entriesToActivate, true, companyId);
                description += "Made active: " + StringUtils.join(entriesToActivate, ", ");

                if (entriesToDeactivate.size() > 0) {
                    description += "\n";
                }
            }

            if (entriesToDeactivate.size() > 0) {
                emmActionDao.setActiveness(entriesToDeactivate, false, companyId);
                description += "Made inactive: " + StringUtils.join(entriesToDeactivate, ", ");
            }

            userActions.add(new UserAction("edit action activeness", description));

            return true;
        }

        return false;
    }

    @Override
    public ServiceResult<List<EmmAction>> setActiveness(Set<Integer> ids, int companyId, boolean activate) {
        Function<Integer, ServiceResult<EmmAction>> validationFunction = id -> {
            EmmAction action = getEmmAction(id, companyId);
            if (action == null) {
                return ServiceResult.errorKeys("error.general.missing");
            }

            if (action.getIsActive() == activate) {
                return ServiceResult.errorKeys(ERROR_MSG);
            }

            return ServiceResult.success(action);
        };

        ServiceResult<List<EmmAction>> validationResult = activate
                ? bulkActionValidationService.checkAllowedForActivation(ids, validationFunction)
                : bulkActionValidationService.checkAllowedForDeactivation(ids, validationFunction);

        if (validationResult.isSuccess()) {
            List<Integer> allowedIds = validationResult.getResult().stream()
                    .map(EmmAction::getId)
                    .toList();

            emmActionDao.setActiveness(allowedIds, activate, companyId);
        }

        return validationResult;
    }

    @Override
    public List<EmmAction> getEmmNotLinkActions(int companyId, boolean includeInactive) {
        if (companyId > 0) {
            return emmActionDao.getEmmNotLinkActions(companyId, includeInactive);
        }
        return new ArrayList<>();
    }

    @Override
    public List<EmmAction> getEmmNotFormActions(int companyId, boolean includeInactive) {
        return emmActionDao.getEmmNotFormActions(companyId, includeInactive);
    }

    @Override
    public boolean containsReadonlyOperations(int actionId, Admin admin) {
        if (actionId <= 0) {
            return false;
        }
        return getEmmAction(actionId, admin.getCompanyID())
                .getActionOperations().stream()
                .anyMatch(o -> isReadonlyOperation(o, admin));
    }
	
    @Override
    public boolean isReadonlyOperation(AbstractActionOperationParameters operation, Admin admin) {
        switch (operation.getOperationType()) {
            case UPDATE_CUSTOMER:
                return operation instanceof ActionOperationUpdateCustomerParameters updateCustomerParams
                        && isReadonlyOperationRecipientField(updateCustomerParams.getColumnName(), admin);
            case SUBSCRIBE_CUSTOMER:
                return operation instanceof ActionOperationSubscribeCustomerParameters subscribeCustomerParams
                        && isReadonlyOperationRecipientField(subscribeCustomerParams.getKeyColumn(), admin);
            case IDENTIFY_CUSTOMER:
                return operation instanceof ActionOperationIdentifyCustomerParameters identifyCustomerParams
                        && isReadonlyOperationRecipientField(identifyCustomerParams.getKeyColumn(), admin);
            default:
                return false;
        }
   	}

    private boolean isReadonlyOperationRecipientField(String column, Admin admin) {
        try {
            ProfileField field = columnInfoService.getColumnInfo(admin.getCompanyID(), column, admin.getAdminID());
            return isReadonlyOperationRecipientField(field);
        } catch (Exception e) {
            LOGGER.warn("Error reading meta data for profile field '{}' (company ID {}, admin ID {})",
                    column, admin.getCompanyID(), admin.getAdminID(), e);
            return false;
        }
    }

    @Override
    public boolean isReadonlyOperationRecipientField(ProfileField field) {
        return field.getModeEdit() == ProfileFieldMode.ReadOnly || field.getModeEdit() == ProfileFieldMode.NotVisible;
    }

    @Override
    public List<String> getActionsNames(Set<Integer> bulkIds, int companyID) {
        return bulkIds.stream()
                .map(id -> getEmmActionName(id, companyID))
                .toList();
    }

    @Override
    public JSONArray getDependencies(int actionId, int companyId) {
        List<EmmActionDependency> forms = userformService.getUserFormNamesByActionID(companyId, actionId)
                .stream()
                .map(t -> new EmmActionDependency(t.getFirst(), EmmActionDependency.Type.FORM, t.getSecond()))
                .toList();

        List<EmmActionDependency> mailings = mailingService.getMailingsUsingEmmAction(actionId, companyId)
                .stream()
                .map(m -> new EmmActionDependency(m.getMailingID(), EmmActionDependency.Type.MAILING, m.getShortname()))
                .toList();

        JSONArray jsonArray = new JSONArray();
        for (EmmActionDependency dependency : CollectionUtils.union(forms, mailings)) {
            JSONObject entry = new JSONObject();
            entry.put("id", dependency.getId());
            entry.put("name", dependency.getName());
            entry.put("type", dependency.getType());
            jsonArray.put(entry);
        }

        return jsonArray;
    }

    @Override
    public boolean isActive(int id) {
        return emmActionDao.isActive(id);
    }

    @Override
    public List<Integer> findActionsUsingProfileField(String fieldName, int companyId) {
        return emmActionDao.findActionsUsingProfileField(fieldName, companyId);
    }

    public void setUserformService(UserformService userformService) {
        this.userformService = userformService;
    }

    public void setBulkActionValidationService(BulkActionValidationService<Integer, EmmAction> bulkActionValidationService) {
        this.bulkActionValidationService = bulkActionValidationService;
    }

    public void setMailingService(MailingService mailingService) {
        this.mailingService = mailingService;
    }

    public void setEmmActionDao(EmmActionDao emmActionDao) {
        this.emmActionDao = emmActionDao;
    }

    public void setEmmActionOperationDao(EmmActionOperationDao emmActionOperationDao) {
        this.emmActionOperationDao = emmActionOperationDao;
    }

    public void setEmmActionOperationService(EmmActionOperationService emmActionOperationService) {
        this.emmActionOperationService = emmActionOperationService;
    }

    public void setMailingDao(MailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    public void setConversionService(ExtendedConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public void setActionExporter(ActionExporter actionExporter) {
        this.actionExporter = actionExporter;
    }

    public void setActionImporter(ActionImporter actionImporter) {
        this.actionImporter = actionImporter;
    }

    public void setChangesCollector(EmmActionChangesCollector changesCollector) {
        this.changesCollector = changesCollector;
    }
}
