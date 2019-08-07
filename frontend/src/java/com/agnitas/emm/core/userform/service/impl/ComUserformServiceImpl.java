/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.service.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.userforms.impl.UserformServiceImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import com.agnitas.emm.core.userform.service.ComUserformService;
import com.agnitas.userform.bean.UserForm;


public class ComUserformServiceImpl extends UserformServiceImpl implements ComUserformService {

    @Override
	public void bulkDelete(Set<Integer> userformIds, @VelocityCheck int companyId) {
        for (int userformId : userformIds)
            this.userFormDao.deleteUserForm(userformId, companyId);
    }

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
}
