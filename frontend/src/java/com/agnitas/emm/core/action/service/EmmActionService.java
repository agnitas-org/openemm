/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service;

import java.util.List;
import java.util.Map;

import org.agnitas.actions.EmmAction;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;

public interface EmmActionService {
	/**
	 * Checks if given action exists.
	 * 
	 * @param actionID action ID to check
	 * @param companyID company ID for action
	 * 
	 * @return <code>true</code> if action exists, otherwise false
	 */
	boolean actionExists(final int actionID, @VelocityCheck final int companyID);

	boolean executeActions(int actionID, @VelocityCheck int companyID, Map<String, Object> params, final EmmActionOperationErrors errors) throws Exception;

	int copyEmmAction(EmmAction emmAction, int toCompanyId);

	int saveEmmAction(EmmAction action);

	int saveEmmAction(EmmAction action, List<UserAction> userActions);

	EmmAction getEmmAction(int actionID, int companyID);

	boolean deleteEmmAction(int actionID, int companyID);

	int copyActionOperations(int sourceActionCompanyID, int sourceActionID, int destinationActionCompanyId, int destinationActionID);

	List<Integer> getReferencedMailinglistsFromAction(int companyID, int actionID);
}
