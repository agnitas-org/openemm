/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.actions.EmmAction;
import org.agnitas.emm.core.useractivitylog.UserAction;


import com.agnitas.beans.Admin;


public interface ComEmmActionService extends EmmActionService {

    void bulkDelete(Set<Integer> actionIds, int companyId);

    String getEmmActionName(int actionId, int companyId);

    boolean setActiveness(Map<Integer, Boolean> changeMap, int companyId, List<UserAction> userActions);
	
	List<EmmAction> getEmmNotLinkActions(int companyId, boolean includeInactive);

    List<EmmAction> getEmmNotFormActions(int companyId, boolean includeInactive);
    
    boolean canUserSaveAction(final Admin admin, final int actionId);
    boolean canUserSaveAction(final Admin admin, final EmmAction action);
}
