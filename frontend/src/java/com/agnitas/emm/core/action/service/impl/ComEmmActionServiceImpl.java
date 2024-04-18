/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.actions.EmmAction;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.emm.core.action.bean.EmmActionDependency;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationIdentifyCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSubscribeCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationUpdateCustomerParameters;
import com.agnitas.emm.core.action.service.ComEmmActionService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.userform.service.ComUserformService;
import com.agnitas.service.ColumnInfoService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class ComEmmActionServiceImpl extends EmmActionServiceImpl implements ComEmmActionService {
	
	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(ComEmmActionServiceImpl.class);
	
	private final ColumnInfoService columnInfoService;
    private ComUserformService userformService;
    private MailingService mailingService;
	
	public ComEmmActionServiceImpl(final ColumnInfoService columnInfoService) {
		this.columnInfoService = Objects.requireNonNull(columnInfoService);
    }

    @Required
    public void setUserformService(ComUserformService userformService) {
        this.userformService = userformService;
    }

    @Required
    public void setMailingService(MailingService mailingService) {
        this.mailingService = mailingService;
    }

    @Override
    public void bulkDelete(Set<Integer> actionIds, int companyId) {
        for (int actionId : actionIds) {
            this.emmActionDao.deleteEmmAction(actionId, companyId);
        }
    }

    @Override
    public String getEmmActionName(int actionId, int companyId) {
        return emmActionDao.getEmmActionName(actionId, companyId);
    }

    @Override
    @Transactional
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
                return operation instanceof ActionOperationUpdateCustomerParameters
                        && isReadonlyOperationRecipientField(
                                ((ActionOperationUpdateCustomerParameters)operation).getColumnName(), admin);
            case SUBSCRIBE_CUSTOMER:
                return operation instanceof ActionOperationSubscribeCustomerParameters
                        && isReadonlyOperationRecipientField(
                                ((ActionOperationSubscribeCustomerParameters)operation).getKeyColumn(), admin);
            case IDENTIFY_CUSTOMER:
                return operation instanceof ActionOperationIdentifyCustomerParameters
                        && isReadonlyOperationRecipientField(
                                ((ActionOperationIdentifyCustomerParameters)operation).getKeyColumn(), admin);
            default:
                return false;
        }
   	}

    private boolean isReadonlyOperationRecipientField(String column, Admin admin) {
        try {
            ProfileField field = columnInfoService.getColumnInfo(admin.getCompanyID(), column, admin.getAdminID());
            return isReadonlyOperationRecipientField(field);
        } catch (Exception e) {
            LOGGER.warn(
                    "Error reading meta data for profile field '{}' (company ID {}, admin ID {})",
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
                .collect(Collectors.toList());
    }

    @Override
    public JSONArray getDependencies(int actionId, int companyId) {
        List<EmmActionDependency> forms = userformService.getUserFormNamesByActionID(companyId, actionId)
                .stream()
                .map(t -> new EmmActionDependency(t.getFirst(), EmmActionDependency.Type.FORM, t.getSecond()))
                .collect(Collectors.toList());

        List<EmmActionDependency> mailings = mailingService.getMailingsUsingEmmAction(actionId, companyId)
                .stream()
                .map(m -> new EmmActionDependency(m.getMailingID(), EmmActionDependency.Type.MAILING, m.getShortname()))
                .collect(Collectors.toList());

        JSONArray jsonArray = new JSONArray();
        for (EmmActionDependency dependency : CollectionUtils.union(forms, mailings)) {
            JSONObject entry = new JSONObject();
            entry.element("id", dependency.getId());
            entry.element("name", dependency.getName());
            entry.element("type", dependency.getType());
            jsonArray.add(entry);
        }

        return jsonArray;
    }
}
