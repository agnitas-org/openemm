/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.actions.EmmAction;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.emm.core.action.service.ComEmmActionService;


public class ComEmmActionServiceImpl extends EmmActionServiceImpl implements ComEmmActionService {

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

}
