/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.emm.core.action.bean.EmmAction;
import com.agnitas.emm.core.action.dto.EmmActionDto;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.ServiceResult;
import org.json.JSONArray;
import org.springframework.transaction.annotation.Transactional;

public interface EmmActionService {

    /**
     * Checks if given action exists.
     *
     * @param actionID action ID to check
     * @param companyID company ID for action
     *
     * @return <code>true</code> if action exists, otherwise false
     */
    boolean actionExists(final int actionID, final int companyID);

    boolean executeActions(int actionID, int companyID, Map<String, Object> params, final EmmActionOperationErrors errors) throws Exception;

    int copyEmmAction(EmmAction emmAction, int toCompanyId, Map<Integer, Integer> mailingIdReplacements) throws Exception;

    @Transactional
    int saveEmmAction(int companyId, EmmAction action, List<UserAction> userActions);

    EmmAction getEmmAction(int actionID, int companyID);

    boolean deleteEmmAction(int actionID, int companyID);

    List<Integer> getReferencedMailinglistsFromAction(int companyID, int actionID);

    /**
     * Loads list of emm actions that uses the mailing with concrete mailing id.
     * @return List of emm actions
     */
    List<EmmAction> getActionListBySendMailingId(int companyId, int mailingId);

    EmmActionDto getCopyOfAction(Admin admin, int originId);

    JSONArray getEmmActionsJson(Admin admin);

    boolean isAdvertising(int id, int companyId);

    void restore(Set<Integer> ids, int companyId);

    void deleteExpired(Date date, int companyId);

    void bulkDelete(Set<Integer> actionIds, int companyId);

    String getEmmActionName(int actionId, int companyId);

    // TODO: EMMGUI-714 remove after remove of old design
    boolean setActiveness(Map<Integer, Boolean> changeMap, int companyId, List<UserAction> userActions);

    ServiceResult<List<EmmAction>> setActiveness(Set<Integer> ids, int companyId, boolean activate);

    List<EmmAction> getEmmNotLinkActions(int companyId, boolean includeInactive);

    List<EmmAction> getEmmNotFormActions(int companyId, boolean includeInactive);
    
    boolean isReadonlyOperationRecipientField(ProfileField field);

    Map<Integer, String> getEmmNotFormActionsMap(int companyId);

    boolean containsReadonlyOperations(int actionId, Admin admin);

    boolean isReadonlyOperation(AbstractActionOperationParameters operation, Admin admin);

    List<String> getActionsNames(Set<Integer> bulkIds, int companyID);

    JSONArray getDependencies(int actionId, int companyId);

    boolean isActive(int id);

    List<Integer> findActionsUsingProfileField(String fieldName, int companyId);
}
