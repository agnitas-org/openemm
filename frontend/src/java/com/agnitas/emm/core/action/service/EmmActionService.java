/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

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
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.action.dto.EmmActionDto;

import net.sf.json.JSONArray;

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

	int copyEmmAction(EmmAction emmAction, int toCompanyId, Map<Integer, Integer> mailingIdReplacements) throws Exception;

	int saveEmmAction(int companyId, EmmAction action);

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

	EmmActionDto getCopyOfAction(ComAdmin admin, int originId);

    JSONArray getEmmActionsJson(ComAdmin admin);
}
