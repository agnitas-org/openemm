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

import org.agnitas.actions.EmmAction;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationUpdateCustomerParameters;
import com.agnitas.emm.core.action.service.ComEmmActionService;
import com.agnitas.service.ColumnInfoService;


public class ComEmmActionServiceImpl extends EmmActionServiceImpl implements ComEmmActionService {
	
	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(ComEmmActionServiceImpl.class);
	
	private final ColumnInfoService columnInfoService;
	
	public ComEmmActionServiceImpl(final ColumnInfoService columnInfoService) {
		this.columnInfoService = Objects.requireNonNull(columnInfoService);
	}

    @Override
    public void bulkDelete(Set<Integer> actionIds, @VelocityCheck int companyId) {
        for (int actionId : actionIds) {
            this.emmActionDao.deleteEmmAction(actionId, companyId);
        }
    }

    @Override
    public String getEmmActionName(int actionId, @VelocityCheck int companyId) {
        return emmActionDao.getEmmActionName(actionId, companyId);
    }

    @Override
    @Transactional
    public boolean setActiveness(Map<Integer, Boolean> changeMap, @VelocityCheck int companyId, List<UserAction> userActions) {
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
	public boolean canUserSaveAction(Admin admin, int actionId) {
		if(actionId == 0) {	// New actions can always be saved
			return true;
		}
		
		final EmmAction action = getEmmAction(actionId, admin.getCompanyID());
		
		return canUserSaveAction(admin, action);
	}

	@Override
	public boolean canUserSaveAction(Admin admin, EmmAction action) {
		if(action == null) {
			return true;
		}
		
		for(final AbstractActionOperationParameters params : action.getActionOperations()) {
			if(params instanceof ActionOperationUpdateCustomerParameters) {
				final ActionOperationUpdateCustomerParameters actionParameters = (ActionOperationUpdateCustomerParameters) params;
				
				try {
					final ProfileField field = this.columnInfoService.getColumnInfo(admin.getCompanyID(), actionParameters.getColumnName(), admin.getAdminID());
					
					if(field == null || field.getModeEdit() == ProfileFieldMode.ReadOnly || field.getModeEdit() == ProfileFieldMode.NotVisible) {
						return false;
					}
				} catch(final Exception e) {
					LOGGER.warn(String.format("Error reading meta data for profile field '%s' (company ID %d, admin ID %d)", actionParameters.getColumnName(), admin.getCompanyID(), admin.getAdminID()), e);
				}
			}
		}
		
		return true;
	}
    
}
